package site.leawsic.chess.render;

import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import org.joml.Matrix4f;
import site.leawsic.chess.block.BaseBoardBlockEntity;
import site.leawsic.chess.config.ChessGameConfig;

public class BaseBoardBlockEntityRenderer implements BlockEntityRenderer<BaseBoardBlockEntity> {
    public BaseBoardBlockEntityRenderer(BlockEntityRendererFactory.Context ctx) {
    }

    @Override
    public void render(BaseBoardBlockEntity entity, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay) {
        int[][] board = entity.getBoard();
        ChessGameConfig config = entity.getConfig();
        if (board == null) return;

        matrices.push();
        float margin = 0.08f;
        float spacingX = (1.0f - 2 * margin) / (config.getCols() - 1);
        float spacingZ = (1.0f - 2 * margin) / (config.getRows() - 1);
        float pieceSize = Math.min(spacingX, spacingZ) * 0.7f;
        float thickness = 1 / 64f;
        float yBase = 1.001f;

        for (int row = 0; row < config.getRows(); row++) {
            for (int col = 0; col < config.getCols(); col++) {
                int piece = board[row][col];
                if (piece == config.getEmptyValue()) continue;

                Identifier texture = config.getPieceTexture(piece);
                if (texture == null) continue;
                VertexConsumer buffer = vertexConsumers.getBuffer(RenderLayer.getEntityCutout(texture));

                float xCenter = margin + col * spacingX;
                float zCenter = margin + row * spacingZ;
                float minX = xCenter - pieceSize / 2;
                float maxX = xCenter + pieceSize / 2;
                float minZ = zCenter - pieceSize / 2;
                float maxZ = zCenter + pieceSize / 2;
                float yTop = yBase + thickness;

                Matrix4f mat = matrices.peek().getPositionMatrix();
                // 顶面四边形
                buffer.vertex(mat, minX, yTop, minZ).color(255, 255, 255, 255).texture(0, 0).overlay(overlay).light(light).normal(0, 1, 0).next();
                buffer.vertex(mat, maxX, yTop, minZ).color(255, 255, 255, 255).texture(1, 0).overlay(overlay).light(light).normal(0, 1, 0).next();
                buffer.vertex(mat, maxX, yTop, maxZ).color(255, 255, 255, 255).texture(1, 1).overlay(overlay).light(light).normal(0, 1, 0).next();
                buffer.vertex(mat, minX, yTop, maxZ).color(255, 255, 255, 255).texture(0, 1).overlay(overlay).light(light).normal(0, 1, 0).next();
            }
        }
        matrices.pop();
    }
}