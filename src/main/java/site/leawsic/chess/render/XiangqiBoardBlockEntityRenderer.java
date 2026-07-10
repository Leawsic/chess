package site.leawsic.chess.render;

import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import org.joml.Matrix4f;
import site.leawsic.chess.Chess;
import site.leawsic.chess.block.XiangqiBoardBlockEntity;

public class XiangqiBoardBlockEntityRenderer implements BlockEntityRenderer<XiangqiBoardBlockEntity> {
    private static final float BOARD_START = -1.0f;
    private static final float BOARD_SIZE = 3.0f;
    private static final float CELL = 45.0f;
    private static final float PIECE_SIZE = 40.0f / 512.0f * BOARD_SIZE;

    public XiangqiBoardBlockEntityRenderer(BlockEntityRendererFactory.Context context) {}

    @Override public void render(XiangqiBoardBlockEntity entity, float tickDelta, MatrixStack matrices, VertexConsumerProvider consumers, int light, int overlay) {
        matrices.push();
        Matrix4f matrix = matrices.peek().getPositionMatrix();
        int[][] board = entity.getBoard();
        for (int row = 0; row < 10; row++) for (int col = 0; col < 9; col++) {
            int piece = board[row][col];
            if (piece == 0) continue;
            Identifier texture = Chess.id("textures/xq_pieces/" + textureName(piece) + ".png");
            VertexConsumer buffer = consumers.getBuffer(RenderLayer.getEntityTranslucent(texture));
            float x = BOARD_START + ((56.0f + col * CELL) / 512.0f) * BOARD_SIZE;
            float z = BOARD_START + ((31.0f + row * CELL + (row >= 5 ? CELL : 0.0f)) / 512.0f) * BOARD_SIZE;
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

    private static String textureName(int piece) {
        String color = piece > 0 ? "hong_" : "hei_";
        return color + switch (Math.abs(piece)) {
            case XiangqiBoardBlockEntity.GENERAL -> "jiang";
            case XiangqiBoardBlockEntity.ADVISOR -> "shi";
            case XiangqiBoardBlockEntity.ELEPHANT -> "xiang";
            case XiangqiBoardBlockEntity.HORSE -> "ma";
            case XiangqiBoardBlockEntity.ROOK -> "ju";
            case XiangqiBoardBlockEntity.CANNON -> "pao";
            default -> "zu";
        };
    }

    @Override public boolean rendersOutsideBoundingBox(XiangqiBoardBlockEntity entity) { return true; }
}
