package site.leawsic.chess.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import site.leawsic.chess.Chess;
import site.leawsic.chess.block.XiangqiBoardBlockEntity;
import site.leawsic.chess.network.ChessNetwork;

public class XiangqiScreen extends HandledScreen<XiangqiScreenHandler> {
    private static final int TEXTURE_SIZE = 512;
    private static final int CELL = 45;
    private static final int LEFT_U = 56;
    private static final int TOP_V = 31;
    private int boardLeft, boardTop;
    private float scale;
    private int selectedX = -1, selectedY = -1;
    private ButtonWidget resetButton;

    public XiangqiScreen(XiangqiScreenHandler handler, PlayerInventory inventory, Text title) { super(handler, inventory, title); backgroundWidth = 360; backgroundHeight = 340; }

    @Override protected void init() {
        super.init();
        backgroundWidth = Math.min(width - 32, 544);
        backgroundHeight = Math.min(height - 32, 580);
        x = (width - backgroundWidth) / 2;
        y = (height - backgroundHeight) / 2;
        scale = Math.min((backgroundWidth - 16) / (float) TEXTURE_SIZE, (backgroundHeight - 48) / (float) TEXTURE_SIZE);
        boardLeft = x + (backgroundWidth - Math.round(TEXTURE_SIZE * scale)) / 2;
        boardTop = y + 24;
        resetButton = ButtonWidget.builder(Text.translatable("gui.chess.clear"), button -> sendReset()).dimensions(x + 10, y + backgroundHeight - 24, 60, 20).build();
        addDrawableChild(resetButton);
    }

    @Override protected void drawBackground(DrawContext context, float delta, int mouseX, int mouseY) {
        Identifier texture = Chess.id("textures/block/xq_board_top.png");
        RenderSystem.setShader(GameRenderer::getPositionTexProgram);
        RenderSystem.setShaderTexture(0, texture);
        context.getMatrices().push();
        context.getMatrices().translate(boardLeft, boardTop, 0);
        context.getMatrices().scale(scale, scale, 1);
        context.drawTexture(texture, 0, 0, 0, 0, TEXTURE_SIZE, TEXTURE_SIZE, TEXTURE_SIZE, TEXTURE_SIZE);
        XiangqiBoardBlockEntity board = getBoard();
        if (board != null) drawPieces(context, board);
        context.getMatrices().pop();
    }

    private void drawPieces(DrawContext context, XiangqiBoardBlockEntity board) {
        int[][] pieces = board.getBoard();
        for (int row = 0; row < 10; row++) for (int col = 0; col < 9; col++) {
            int piece = pieces[row][col];
            if (piece == 0) continue;
            int centerX = LEFT_U + col * CELL;
            int centerY = TOP_V + row * CELL + (row >= 5 ? CELL : 0);
            Identifier texture = Chess.id("textures/xq_pieces/" + textureName(piece) + ".png");
            RenderSystem.setShaderTexture(0, texture);
            context.drawTexture(texture, centerX - 20, centerY - 20, 0, 0, 40, 40, 40, 40);
            if (col == selectedX && row == selectedY) context.drawBorder(centerX - 21, centerY - 21, 42, 42, 0xFFFFFF00);
        }
    }

    @Override public boolean mouseClicked(double mouseX, double mouseY, int button) {
        float logicalX = (float) ((mouseX - boardLeft) / scale);
        float logicalY = (float) ((mouseY - boardTop) / scale);
        int col = Math.round((logicalX - LEFT_U) / CELL);
        int adjustedY = Math.round((logicalY - TOP_V) / CELL);
        int row = adjustedY >= 5 ? adjustedY - 1 : adjustedY;
        if (col >= 0 && col < 9 && row >= 0 && row < 10) {
            XiangqiBoardBlockEntity board = getBoard();
            if (board == null || client == null || client.player == null || board.isGameOver()) return true;
            int piece = board.getBoard()[row][col];
            if (selectedX < 0) {
                if (piece != 0 && Integer.compare(piece, 0) == board.getCurrentPlayer()) { selectedX = col; selectedY = row; }
            } else {
                sendMove(selectedX, selectedY, col, row);
                selectedX = selectedY = -1;
            }
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override protected void drawForeground(DrawContext context, int mouseX, int mouseY) {
        XiangqiBoardBlockEntity board = getBoard();
        String status = board == null ? "" : board.isGameOver()
                ? Text.translatable(board.getWinner() == XiangqiBoardBlockEntity.RED ? "gui.chess.xq.red_wins" : "gui.chess.xq.black_wins").getString()
                : Text.translatable(board.getCurrentPlayer() == XiangqiBoardBlockEntity.RED ? "gui.chess.xq.red_turn" : "gui.chess.xq.black_turn").getString();
        context.drawText(textRenderer, status, 10, 10, 0xFFFFFF, false);
        resetButton.active = board != null && client != null && client.player != null && client.player.getUuid().equals(board.getHostPlayer());
    }

    private XiangqiBoardBlockEntity getBoard() { return client != null && client.world != null && client.world.getBlockEntity(handler.getBoardPos()) instanceof XiangqiBoardBlockEntity board ? board : null; }
    private void sendMove(int fromX, int fromY, int toX, int toY) { PacketByteBuf buf = PacketByteBufs.create(); buf.writeBlockPos(handler.getBoardPos()); buf.writeByte(fromX); buf.writeByte(fromY); buf.writeByte(toX); buf.writeByte(toY); ClientPlayNetworking.send(ChessNetwork.XIANGQI_MOVE, buf); }
    private void sendReset() { PacketByteBuf buf = PacketByteBufs.create(); buf.writeBlockPos(handler.getBoardPos()); ClientPlayNetworking.send(ChessNetwork.XIANGQI_RESET, buf); }
    private static String textureName(int piece) { String color = piece > 0 ? "hong_" : "hei_"; return color + switch (Math.abs(piece)) { case 1 -> "jiang"; case 2 -> "shi"; case 3 -> "xiang"; case 4 -> "ma"; case 5 -> "ju"; case 6 -> "pao"; default -> "zu"; }; }
}
