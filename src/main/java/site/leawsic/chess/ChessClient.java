package site.leawsic.chess;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.minecraft.client.gui.screen.ingame.HandledScreens;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactories;
import site.leawsic.chess.block.ModBlocks;
import site.leawsic.chess.render.BaseBoardBlockEntityRenderer;
import site.leawsic.chess.screen.BaseBoardScreen;

public class ChessClient implements ClientModInitializer {
    /**
     * Runs the mod initializer on the client environment.
     */
    @Override
    public void onInitializeClient() {
        BlockEntityRendererFactories.register(ModBlocks.GO_BOARD_BLOCK_ENTITY, BaseBoardBlockEntityRenderer::new);
        HandledScreens.register(ModBlocks.GO_BOARD_SCREEN_HANDLER, BaseBoardScreen::new);
        BlockRenderLayerMap.INSTANCE.putBlock(ModBlocks.GO_BOARD, RenderLayer.getCutout());
    }
}
