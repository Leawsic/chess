package site.leawsic.chess.render;

import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.Identifier;
import org.joml.Matrix4f;
import site.leawsic.chess.Chess;
import site.leawsic.chess.block.XiangqiBoardBlockEntity;
import site.leawsic.chess.config.XiangqiConfig;

public class XiangqiBoardBlockEntityRenderer implements BlockEntityRenderer<XiangqiBoardBlockEntity> {
    private static final float BOARD_START = -1.0f;
    private static final float BOARD_SIZE = 3.0f;
    private static final float PIECE_SIZE = (float) XiangqiConfig.PIECE_PIXELS / XiangqiConfig.BOARD_TEXTURE_SIZE * BOARD_SIZE;

    public XiangqiBoardBlockEntityRenderer(BlockEntityRendererFactory.Context context) {}

    @Override public void render(XiangqiBoardBlockEntity entity, float tickDelta, MatrixStack matrices, VertexConsumerProvider consumers, int light, int overlay) {
        matrices.push();
        applyBoardRotation(matrices, entity.getCachedState().get(site.leawsic.chess.block.XiangqiBoardBlock.FACING));
        Matrix4f matrix = matrices.peek().getPositionMatrix();
        int[][] board = entity.getBoard();
        for (int row = 0; row < XiangqiConfig.ROWS; row++) for (int col = 0; col < XiangqiConfig.COLS; col++) {
            int piece = board[row][col];
            if (piece == 0) continue;
            Identifier texture = Chess.id("textures/" + XiangqiConfig.pieceTexture(piece).getPath() + ".png");
            VertexConsumer buffer = consumers.getBuffer(RenderLayer.getEntityTranslucent(texture));
            float x = BOARD_START + (XiangqiConfig.textureX(col) / (float) XiangqiConfig.BOARD_TEXTURE_SIZE) * BOARD_SIZE;
            float z = BOARD_START + (XiangqiConfig.textureY(row) / (float) XiangqiConfig.BOARD_TEXTURE_SIZE) * BOARD_SIZE;
            float minX = x - PIECE_SIZE / 2, maxX = x + PIECE_SIZE / 2;
            float minZ = z - PIECE_SIZE / 2, maxZ = z + PIECE_SIZE / 2;
            float y = 0.065f;
            buffer.vertex(matrix, minX, y, minZ).color(255, 255, 255, 255).texture(0, 0).overlay(overlay).light(light).normal(0, 1, 0).next();
            buffer.vertex(matrix, maxX, y, minZ).color(255, 255, 255, 255).texture(1, 0).overlay(overlay).light(light).normal(0, 1, 0).next();
            buffer.vertex(matrix, maxX, y, maxZ).color(255, 255, 255, 255).texture(1, 1).overlay(overlay).light(light).normal(0, 1, 0).next();
            buffer.vertex(matrix, minX, y, maxZ).color(255, 255, 255, 255).texture(0, 1).overlay(overlay).light(light).normal(0, 1, 0).next();
        }
        matrices.pop();
    }

    private static void applyBoardRotation(MatrixStack matrices, Direction facing) {
        float degrees = switch (facing) {
            case EAST -> 270.0f;
            case SOUTH -> 180.0f;
            case WEST -> 90.0f;
            default -> 0.0f;
        };
        if (degrees != 0.0f) {
            matrices.translate(0.5f, 0.0f, 0.5f);
            matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(degrees));
            matrices.translate(-0.5f, 0.0f, -0.5f);
        }
    }

    @Override public boolean rendersOutsideBoundingBox(XiangqiBoardBlockEntity entity) { return true; }
}
