package site.leawsic.chess.datagen;

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricModelProvider;
import net.minecraft.data.client.*;
import net.minecraft.util.Identifier;
import site.leawsic.chess.Chess;
import site.leawsic.chess.block.ModBlocks;

public class ModModelProvider extends FabricModelProvider {
    public ModModelProvider(FabricDataOutput output) {
        super(output);
    }

    @Override
    public void generateBlockStateModels(BlockStateModelGenerator generator) {
        // 使用 cube_bottom_top 模型，分别指定 top, bottom, side 纹理
        TextureMap textures = new TextureMap()
                .put(TextureKey.BOTTOM, Chess.id("block/go_board_bottom"))
                .put(TextureKey.TOP, Chess.id("block/go_board_top"))
                .put(TextureKey.SIDE, Chess.id("block/go_board_side"));

        // 上传模型并获取其 ID
        Identifier modelId = Models.CUBE_BOTTOM_TOP.upload(
                ModBlocks.GO_BOARD,
                textures,
                generator.modelCollector
        );

        generator.blockStateCollector.accept(
                BlockStateModelGenerator.createSingletonBlockState(ModBlocks.GO_BOARD, modelId)
        );
    }

    @Override
    public void generateItemModels(ItemModelGenerator generator) {
        // 创建一个模型，其 parent 指向方块模型
        Model model = new Model(
                java.util.Optional.of(Chess.id("block/go_board")),
                java.util.Optional.empty()
        );
        // 使用 register 方法注册物品模型
        generator.register(ModBlocks.GO_BOARD.asItem(), model);
    }
}