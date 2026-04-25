package site.leawsic.chess.block;

import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.block.Block;
import net.minecraft.block.MapColor;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.resource.featuretoggle.FeatureSet;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.screen.ScreenHandlerType;
import site.leawsic.chess.Chess;
import site.leawsic.chess.config.GomokuConfig;
import site.leawsic.chess.screen.BaseBoardScreenHandler;

import java.util.function.Supplier;

public class ModBlocks {
    // 延迟初始化的静态字段
    public static Block GO_BOARD;
    public static BlockEntityType<BaseBoardBlockEntity> GO_BOARD_BLOCK_ENTITY;
    public static ScreenHandlerType<BaseBoardScreenHandler> GO_BOARD_SCREEN_HANDLER;

    //在模组初始化时调用，完成所有方块、方块实体、屏幕处理器的注册。使用明确的注册顺序来避免静态字段的前向引用。
    public static void register() {
        // 创建方块实体类型构建器（此时还不提供方块，稍后添加）
        FabricBlockEntityTypeBuilder<BaseBoardBlockEntity> builder =
                FabricBlockEntityTypeBuilder.create(
                        (pos, state) -> new BaseBoardBlockEntity(GO_BOARD_BLOCK_ENTITY, pos, state, GomokuConfig.CONFIG) {
                            @Override
                            public BlockEntityType<?> getType() {
                                return GO_BOARD_BLOCK_ENTITY;
                            }
                        }
                        // 没有立即传入方块，将通过 addBlock 添加
                );
        final BlockEntityType<?>[] holder = new BlockEntityType[1];
        Supplier<BlockEntityType<?>> beTypeSupplier = () -> holder[0];
        GO_BOARD = Registry.register(
                Registries.BLOCK,
                Chess.id("go_board"),
                new BaseBoardBlock(FabricBlockSettings.create()
                        .mapColor(MapColor.OAK_TAN)
                        .strength(2.0f)
                        .requiresTool(),
                        beTypeSupplier) {
                }
        );

        // 将方块添加到构建器并完成方块实体类型的注册
        builder.addBlock(GO_BOARD);
        GO_BOARD_BLOCK_ENTITY = Registry.register(
                Registries.BLOCK_ENTITY_TYPE,
                Chess.id("go_board_be"),
                builder.build()
        );
        // 将创建好的类型填入 holder，使得先前提供的供应商可以正常工作
        holder[0] = GO_BOARD_BLOCK_ENTITY;

        // 注册屏幕处理器类型
        GO_BOARD_SCREEN_HANDLER = Registry.register(
                Registries.SCREEN_HANDLER,
                Chess.id("go_board"),
                new ScreenHandlerType<>(
                        (syncId, inv) -> new BaseBoardScreenHandler(syncId, ScreenHandlerContext.EMPTY, GomokuConfig.CONFIG),
                        FeatureSet.empty()
                )
        );
    }
}