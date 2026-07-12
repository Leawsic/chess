package site.leawsic.chess.block;

import net.minecraft.block.Block;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerType;
import net.minecraft.block.Blocks;
import site.leawsic.chess.Chess;
import site.leawsic.chess.config.GomokuConfig;
import site.leawsic.chess.screen.BaseBoardScreenHandler;
import site.leawsic.chess.screen.XiangqiScreenHandler;

public class ModBlocks {
    public static BoardGameObjects GOMOKU;

    public static Block GOMOKU_BOARD;
    public static BlockEntityType<BaseBoardBlockEntity> GOMOKU_BOARD_BLOCK_ENTITY;
    public static ScreenHandlerType<BaseBoardScreenHandler> GOMOKU_BOARD_SCREEN_HANDLER;
    public static Block XIANGQI_BOARD;
    public static Block XIANGQI_PLACEHOLDER;
    public static Item XIANGQI_BOARD_ITEM;
    public static BlockEntityType<XiangqiBoardBlockEntity> XIANGQI_BOARD_BLOCK_ENTITY;
    public static ScreenHandlerType<XiangqiScreenHandler> XIANGQI_BOARD_SCREEN_HANDLER;

    public static void register() {
        GOMOKU = BoardGameRegistry.register("gomoku_board", GomokuConfig.CONFIG, GomokuConfig.GO_CONFIG);
        GOMOKU_BOARD = GOMOKU.block();
        GOMOKU_BOARD_BLOCK_ENTITY = GOMOKU.blockEntityType();
        GOMOKU_BOARD_SCREEN_HANDLER = GOMOKU.screenHandlerType();

        @SuppressWarnings("unchecked")
        BlockEntityType<XiangqiBoardBlockEntity>[] xiangqiType = new BlockEntityType[1];
        ScreenHandlerType<?>[] xiangqiScreen = new ScreenHandlerType<?>[1];
        XIANGQI_BOARD = Registry.register(Registries.BLOCK, Chess.id("xq_board"), new XiangqiBoardBlock(
                FabricBlockSettings.copyOf(Blocks.OAK_PLANKS),
                () -> xiangqiType[0], () -> xiangqiScreen[0]));
        XIANGQI_PLACEHOLDER = Registry.register(Registries.BLOCK, Chess.id("xq_board_placeholder"), new BoardPlaceholderBlock(
                FabricBlockSettings.copyOf(Blocks.OAK_PLANKS).nonOpaque()));
        XIANGQI_BOARD_BLOCK_ENTITY = Registry.register(Registries.BLOCK_ENTITY_TYPE, Chess.id("xq_board_be"),
                FabricBlockEntityTypeBuilder.<XiangqiBoardBlockEntity>create((pos, state) -> new XiangqiBoardBlockEntity(null, pos, state) {
                    @Override public BlockEntityType<?> getType() { return xiangqiType[0]; }
                }, XIANGQI_BOARD).build());
        xiangqiType[0] = XIANGQI_BOARD_BLOCK_ENTITY;
        XIANGQI_BOARD_ITEM = Registry.register(Registries.ITEM, Chess.id("xq_board"), new BlockItem(XIANGQI_BOARD, new Item.Settings()));
        XIANGQI_BOARD_SCREEN_HANDLER = Registry.register(Registries.SCREEN_HANDLER, Chess.id("xq_board"),
                new ExtendedScreenHandlerType<>((syncId, inventory, buf) -> new XiangqiScreenHandler(xiangqiScreen[0], syncId, inventory, buf.readBlockPos())));
        xiangqiScreen[0] = XIANGQI_BOARD_SCREEN_HANDLER;
    }
}
