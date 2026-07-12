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
import site.leawsic.chess.block.BaseBoardBlockEntity;
import site.leawsic.chess.config.ChessGameConfig;

public class BaseBoardBlockEntityRenderer implements BlockEntityRenderer<BaseBoardBlockEntity> {
    public BaseBoardBlockEntityRenderer(BlockEntityRendererFactory.Context ctx) {
    }

    @Override
    public void render(BaseBoardBlockEntity entity, float tickDelta, MatrixStack matrices,
                       VertexConsumerProvider vertexConsumers, int light, int overlay) {
        int[][] board = entity.getBoard();
        ChessGameConfig config = entity.getConfig();
        if (board == null) return;

        matrices.push();
        applyBoardRotation(matrices, entity.getCachedState().get(site.leawsic.chess.block.BaseBoardBlock.FACING));

        // 升高棋子，确保不被棋盘模型遮挡
        float yBase = 0.05f;
        float boardSize = 3.0f; // 3x3 格占地
        float boardStart = -1.0f;
        float texW = config.getBoardTextureWidth();
        float texH = config.getBoardTextureHeight();
        // GUI 和世界渲染共用配置中的棋子中心与显示尺寸。
        // 3D 模型将整张纹理 (0..texW, 0..texH) 映射到方块坐标 (boardStart..boardStart+boardSize)
        float pieceSize = (config.getPieceDrawSize() / texW) * boardSize;
        float thickness = 1 / 64f;

        for (int row = 0; row < config.getRows(); row++) {
            for (int col = 0; col < config.getCols(); col++) {
                int piece = board[row][col];
                if (piece == config.getEmptyValue()) continue;

                Identifier tex = config.getPieceTexture(piece);
                if (tex == null) continue;
                Identifier fullTex = Chess.id("textures/" + tex.getPath() + ".png");

                // 使用双面渲染层，确保从上方和下方都能看到
                VertexConsumer buffer = vertexConsumers.getBuffer(RenderLayer.getEntityTranslucent(fullTex));

                float xCenter = boardStart + (config.getPieceCenterU(col) / texW) * boardSize;
                float zCenter = boardStart + (config.getPieceCenterV(row) / texH) * boardSize;
                float minX = xCenter - pieceSize / 2;
                float maxX = xCenter + pieceSize / 2;
                float minZ = zCenter - pieceSize / 2;
                float maxZ = zCenter + pieceSize / 2;
                float yTop = yBase + thickness;

                Matrix4f mat = matrices.peek().getPositionMatrix();
                
                // 绘制朝上的面（从上方俯视可见）
                buffer.vertex(mat, minX, yTop, minZ).color(255, 255, 255, 255).texture(0, 0).overlay(overlay).light(light).normal(0, 1, 0).next();
                buffer.vertex(mat, maxX, yTop, minZ).color(255, 255, 255, 255).texture(1, 0).overlay(overlay).light(light).normal(0, 1, 0).next();
                buffer.vertex(mat, maxX, yTop, maxZ).color(255, 255, 255, 255).texture(1, 1).overlay(overlay).light(light).normal(0, 1, 0).next();
                buffer.vertex(mat, minX, yTop, maxZ).color(255, 255, 255, 255).texture(0, 1).overlay(overlay).light(light).normal(0, 1, 0).next();
                
                // 绘制朝下的面（从下方仰视可见）
                buffer.vertex(mat, minX, yBase, minZ).color(255, 255, 255, 255).texture(0, 0).overlay(overlay).light(light).normal(0, -1, 0).next();
                buffer.vertex(mat, maxX, yBase, minZ).color(255, 255, 255, 255).texture(1, 0).overlay(overlay).light(light).normal(0, -1, 0).next();
                buffer.vertex(mat, maxX, yBase, maxZ).color(255, 255, 255, 255).texture(1, 1).overlay(overlay).light(light).normal(0, -1, 0).next();
                buffer.vertex(mat, minX, yBase, maxZ).color(255, 255, 255, 255).texture(0, 1).overlay(overlay).light(light).normal(0, -1, 0).next();
            }
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

    @Override
    public boolean rendersOutsideBoundingBox(BaseBoardBlockEntity blockEntity) {
        return true;
    }
}
