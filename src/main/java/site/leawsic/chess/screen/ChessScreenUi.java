package site.leawsic.chess.screen;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;

final class ChessScreenUi {
    private ChessScreenUi() {
    }

    static void drawGameOver(DrawContext context, TextRenderer textRenderer, int width, int height, String winner) {
        context.fill(0, 0, width, height, 0xD2000000);
        int centerX = width / 2;
        int centerY = height / 2 - 20;
        int titleWidth = Math.max(150, textRenderer.getWidth(winner) + 40);
        context.fill(centerX - titleWidth / 2, centerY - 17, centerX + titleWidth / 2, centerY + 37, 0xE8120F0A);
        context.drawBorder(centerX - titleWidth / 2, centerY - 17, titleWidth, 54, 0xFFFFD54F);
        context.drawCenteredTextWithShadow(textRenderer, winner, centerX, centerY, 0xFFD54F);
        context.drawCenteredTextWithShadow(textRenderer, Text.translatable("gui.chess.game_over"), centerX, centerY + 25, 0xFFFFFF);
        context.drawCenteredTextWithShadow(textRenderer, Text.translatable("gui.chess.clear_hint"), centerX, height - 60, 0xDDDDDD);
    }
}
