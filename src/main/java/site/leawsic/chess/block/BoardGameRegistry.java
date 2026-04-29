package site.leawsic.chess.block;

import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerType;
import net.minecraft.block.Block;
import net.minecraft.block.MapColor;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.util.math.BlockPos;
import site.leawsic.chess.Chess;
import site.leawsic.chess.config.ChessGameConfig;
import site.leawsic.chess.screen.BaseBoardScreenHandler;

import java.util.function.Supplier;

public class BoardGameRegistry {
    public static BoardGameObjects register(String name, ChessGameConfig config) {
        // 提前声明 holders，用于打破循环引用
        BlockEntityType<?>[] beHolder = new BlockEntityType<?>[1];
        ScreenHandlerType<?>[] shHolder = new ScreenHandlerType<?>[1];

        // 屏幕处理器类型供应商
        Supplier<ScreenHandlerType<?>> shSupplier = () -> shHolder[0];
        // 方块实体类型供应商
        Supplier<BlockEntityType<?>> beTypeSupplier = () -> beHolder[0];

        // 注册方块（需要两个供应商）
        Block block = Registry.register(
                Registries.BLOCK,
                Chess.id(name),
                new BaseBoardBlock(FabricBlockSettings.create()
                        .mapColor(MapColor.OAK_TAN)
                        .strength(2.0f)
                        .requiresTool(),
                        beTypeSupplier,
                        shSupplier) {
                }
        );

        // 注册方块实体类型
        FabricBlockEntityTypeBuilder<BaseBoardBlockEntity> builder = FabricBlockEntityTypeBuilder.create(
                (pos, state) -> new BaseBoardBlockEntity(null, pos, state, config) {
                    @Override
                    public BlockEntityType<?> getType() {
                        return beHolder[0];
                    }
                },
                block
        );
        BlockEntityType<BaseBoardBlockEntity> blockEntityType = Registry.register(
                Registries.BLOCK_ENTITY_TYPE,
                Chess.id(name + "_be"),
                builder.build()
        );
        beHolder[0] = blockEntityType;

        // 注册物品
        Item item = Registry.register(
                Registries.ITEM,
                Chess.id(name),
                new BlockItem(block, new Item.Settings())
        );

        // 注册屏幕处理器类型
        ScreenHandlerType<BaseBoardScreenHandler> screenHandlerType = Registry.register(
                Registries.SCREEN_HANDLER,
                Chess.id(name),
                new ExtendedScreenHandlerType<>(
                        (syncId, inv, buf) -> {
                            BlockPos pos = buf.readBlockPos();
                            return new BaseBoardScreenHandler(shSupplier.get(),syncId, inv, pos, config);
                        }
                )
        );
        shHolder[0] = screenHandlerType;

        return new BoardGameObjects(block, item, blockEntityType, screenHandlerType);
    }
}