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
import site.leawsic.chess.config.XiangqiConfig;

public class XiangqiScreen extends HandledScreen<XiangqiScreenHandler> {
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
        scale = Math.min((backgroundWidth - 16) / (float) XiangqiConfig.BOARD_TEXTURE_SIZE, (backgroundHeight - 48) / (float) XiangqiConfig.BOARD_TEXTURE_SIZE);
        boardLeft = x + (backgroundWidth - Math.round(XiangqiConfig.BOARD_TEXTURE_SIZE * scale)) / 2;
        boardTop = y + 24;
        resetButton = ButtonWidget.builder(Text.translatable("gui.chess.clear"), button -> sendReset()).dimensions(x + 10, y + backgroundHeight - 24, 60, 20).build();
        addDrawableChild(resetButton);
    }

    @Override protected void drawBackground(DrawContext context, float delta, int mouseX, int mouseY) {
        Identifier texture = Chess.id("textures/" + XiangqiConfig.BOARD_TEXTURE.getPath() + ".png");
        RenderSystem.setShader(GameRenderer::getPositionTexProgram);
        RenderSystem.setShaderTexture(0, texture);
        context.getMatrices().push();
        context.getMatrices().translate(boardLeft, boardTop, 0);
        context.getMatrices().scale(scale, scale, 1);
        context.drawTexture(texture, 0, 0, 0, 0, XiangqiConfig.BOARD_TEXTURE_SIZE, XiangqiConfig.BOARD_TEXTURE_SIZE, XiangqiConfig.BOARD_TEXTURE_SIZE, XiangqiConfig.BOARD_TEXTURE_SIZE);
        XiangqiBoardBlockEntity board = getBoard();
        if (board != null) drawPieces(context, board);
        context.getMatrices().pop();
    }

    private void drawPieces(DrawContext context, XiangqiBoardBlockEntity board) {
        int[][] pieces = board.getBoard();
        for (int row = 0; row < XiangqiConfig.ROWS; row++) for (int col = 0; col < XiangqiConfig.COLS; col++) {
            int piece = pieces[row][col];
            if (piece == 0) continue;
            int centerX = XiangqiConfig.textureX(col);
            int centerY = XiangqiConfig.textureY(row);
            Identifier texture = Chess.id("textures/" + XiangqiConfig.pieceTexture(piece).getPath() + ".png");
            RenderSystem.setShaderTexture(0, texture);
            context.drawTexture(texture, centerX - XiangqiConfig.PIECE_PIXELS / 2, centerY - XiangqiConfig.PIECE_PIXELS / 2, 0, 0, XiangqiConfig.PIECE_PIXELS, XiangqiConfig.PIECE_PIXELS, XiangqiConfig.PIECE_PIXELS, XiangqiConfig.PIECE_PIXELS);
            if (col == selectedX && row == selectedY) context.drawBorder(centerX - XiangqiConfig.PIECE_PIXELS / 2 - 1, centerY - XiangqiConfig.PIECE_PIXELS / 2 - 1, XiangqiConfig.PIECE_PIXELS + 2, XiangqiConfig.PIECE_PIXELS + 2, 0xFFFFFF00);
        }
    }

    @Override public boolean mouseClicked(double mouseX, double mouseY, int button) {
        float logicalX = (float) ((mouseX - boardLeft) / scale);
        float logicalY = (float) ((mouseY - boardTop) / scale);
        int col = Math.round((logicalX - XiangqiConfig.BOARD_LEFT_U) / XiangqiConfig.CELL_PIXELS);
        int row = Math.round((logicalY - XiangqiConfig.BOARD_TOP_V) / XiangqiConfig.CELL_PIXELS);
        if (XiangqiConfig.inBounds(col, row)) {
            XiangqiBoardBlockEntity board = getBoard();
            if (board == null || client == null || client.player == null || board.isGameOver()) return true;
            int piece = board.getBoard()[row][col];
            if (selectedX < 0) {
                if (piece != 0 && XiangqiConfig.color(piece) == board.getCurrentPlayer()) { selectedX = col; selectedY = row; }
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
                ? Text.translatable(board.getWinner() == XiangqiConfig.RED ? "gui.chess.xq.red_wins" : "gui.chess.xq.black_wins").getString()
                : Text.translatable(board.getCurrentPlayer() == XiangqiConfig.RED ? "gui.chess.xq.red_turn" : "gui.chess.xq.black_turn").getString();
        context.drawText(textRenderer, status, 10, 10, 0xFFFFFF, false);
        resetButton.active = board != null && client != null && client.player != null && client.player.getUuid().equals(board.getHostPlayer());
    }

    private XiangqiBoardBlockEntity getBoard() { return client != null && client.world != null && client.world.getBlockEntity(handler.getBoardPos()) instanceof XiangqiBoardBlockEntity board ? board : null; }
    private void sendMove(int fromX, int fromY, int toX, int toY) { PacketByteBuf buf = PacketByteBufs.create(); buf.writeBlockPos(handler.getBoardPos()); buf.writeByte(fromX); buf.writeByte(fromY); buf.writeByte(toX); buf.writeByte(toY); ClientPlayNetworking.send(ChessNetwork.XIANGQI_MOVE, buf); }
    private void sendReset() { PacketByteBuf buf = PacketByteBufs.create(); buf.writeBlockPos(handler.getBoardPos()); ClientPlayNetworking.send(ChessNetwork.XIANGQI_RESET, buf); }
}
