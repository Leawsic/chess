package site.leawsic.chess.screen;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import site.leawsic.chess.block.BaseBoardBlockEntity;
import site.leawsic.chess.config.ChessGameConfig;
import site.leawsic.chess.network.ChessNetwork;

import java.util.Optional;

public class BaseBoardScreen extends HandledScreen<BaseBoardScreenHandler> {
    private final ChessGameConfig config;
    private int boardLeft, boardTop, cellSize;
    private BlockPos boardPos;
    private ButtonWidget clearButton, undoButton, editModeButton;

    public BaseBoardScreen(BaseBoardScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
        this.config = handler.getConfig();
        this.backgroundWidth = Math.max(240, config.getCols() * 16 + 32);
        this.backgroundHeight = Math.max(280, config.getRows() * 16 + 80);
    }

    @Override
    protected void init() {
        super.init();
        this.playerInventoryTitleY = -1000;
        // 根据配置计算棋盘位置
        cellSize = 16;
        boardLeft = x + (backgroundWidth - cellSize * (config.getCols() - 1)) / 2;
        boardTop = y + 30;

        Optional<BlockPos> opt = handler.getContext().get((world, pos) -> pos);
        boardPos = opt.orElse(BlockPos.ORIGIN);

        clearButton = ButtonWidget.builder(Text.translatable("gui.chess.clear"), btn -> sendPacket(ChessNetwork.CLEAR_BOARD))
                .dimensions(x + 10, y + backgroundHeight - 30, 50, 20).build();
        undoButton = ButtonWidget.builder(Text.translatable("gui.chess.undo"), btn -> sendPacket(ChessNetwork.UNDO_MOVE))
                .dimensions(x + 70, y + backgroundHeight - 30, 50, 20).build();
        editModeButton = ButtonWidget.builder(Text.translatable("gui.chess.edit_mode"), btn -> sendPacket(ChessNetwork.TOGGLE_EDIT_MODE))
                .dimensions(x + 130, y + backgroundHeight - 30, 80, 20).build();

        addDrawableChild(clearButton);
        addDrawableChild(undoButton);
        addDrawableChild(editModeButton);
    }

    private void sendPacket(Identifier channel, int... extra) {
        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeBlockPos(boardPos);
        if (channel.equals(ChessNetwork.PLACE_PIECE) && extra.length >= 2) {
            buf.writeByte(extra[0]);
            buf.writeByte(extra[1]);
        }
        ClientPlayNetworking.send(channel, buf);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (isPointOnBoard(mouseX, mouseY)) {
            int col = Math.round((float)(mouseX - boardLeft) / cellSize);
            int row = Math.round((float)(mouseY - boardTop) / cellSize);
            if (col >= 0 && col < config.getCols() && row >= 0 && row < config.getRows()) {
                sendPacket(ChessNetwork.PLACE_PIECE, col, row);
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    private boolean isPointOnBoard(double mouseX, double mouseY) {
        return mouseX >= boardLeft - 4 && mouseX <= boardLeft + (config.getCols()-1)*cellSize + 4 &&
                mouseY >= boardTop - 4 && mouseY <= boardTop + (config.getRows()-1)*cellSize + 4;
    }

    @Override
    protected void drawBackground(DrawContext context, float delta, int mouseX, int mouseY) {
        int gridColor = 0xFF000000;
        int boardRight = boardLeft + (config.getCols()-1) * cellSize;
        int boardBottom = boardTop + (config.getRows()-1) * cellSize;
        // 横线
        for (int i = 0; i < config.getRows(); i++) {
            int yPos = boardTop + i * cellSize;
            context.fill(boardLeft, yPos, boardRight + 1, yPos + 1, gridColor);
        }
        // 竖线
        for (int i = 0; i < config.getCols(); i++) {
            int xPos = boardLeft + i * cellSize;
            context.fill(xPos, boardTop, xPos + 1, boardBottom + 1, gridColor);
        }
        // 星位
        for (int[] pt : config.getStarPoints()) {
            int cx = boardLeft + pt[0] * cellSize;
            int cy = boardTop + pt[1] * cellSize;
            context.fill(cx-2, cy-2, cx+3, cy+3, gridColor);
        }
    }

    @Override
    protected void drawForeground(DrawContext context, int mouseX, int mouseY) {
        BaseBoardBlockEntity be = getBlockEntity();
        if (be != null) {
            int[][] board = be.getBoard();
            for (int row = 0; row < config.getRows(); row++) {
                for (int col = 0; col < config.getCols(); col++) {
                    int piece = board[row][col];
                    if (piece != config.getEmptyValue()) {
                        int px = boardLeft + col * cellSize;
                        int py = boardTop + row * cellSize;
                        // 根据棋子类型决定颜色（可后续改为贴图）
                        int color = piece == 1 ? 0xFF000000 :
                                piece == 2 ? 0xFFFFFFFF : 0xFFFF0000;
                        context.fill(px - 4, py - 4, px + 5, py + 5, color);
                    }
                }
            }
            String status = be.isGameOver() ? Text.translatable("gui.chess.game_over").getString() :
                    be.isEditMode() ? Text.translatable("gui.chess.edit_mode").getString() :
                            (be.getCurrentPlayer() == 1 ? "Black's turn" : "White's turn");
            context.drawText(textRenderer, status, x + 10, y + 10, 0xFFFFFF, false);
        }
    }

    private BaseBoardBlockEntity getBlockEntity() {
        if (client == null || client.world == null) return null;
        return (BaseBoardBlockEntity) client.world.getBlockEntity(boardPos);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        renderBackground(context);
        super.render(context, mouseX, mouseY, delta);
        drawMouseoverTooltip(context, mouseX, mouseY);
    }
}