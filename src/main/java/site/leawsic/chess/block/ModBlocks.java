package site.leawsic.chess.block;

import net.minecraft.block.Block;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.screen.ScreenHandlerType;
import site.leawsic.chess.config.GoConfig;
import site.leawsic.chess.config.GomokuConfig;
import site.leawsic.chess.screen.BaseBoardScreenHandler;

public class ModBlocks {
    public static BoardGameObjects GOMOKU;
    public static BoardGameObjects GO;

    public static Block GOMOKU_BOARD;
    public static BlockEntityType<BaseBoardBlockEntity> GOMOKU_BOARD_BLOCK_ENTITY;
    public static ScreenHandlerType<BaseBoardScreenHandler> GOMOKU_BOARD_SCREEN_HANDLER;

    public static Block GO_BOARD;
    public static BlockEntityType<BaseBoardBlockEntity> GO_BOARD_BLOCK_ENTITY;
    public static ScreenHandlerType<BaseBoardScreenHandler> GO_BOARD_SCREEN_HANDLER;

    public static void register() {
        GOMOKU = BoardGameRegistry.register("gomoku_board", GomokuConfig.CONFIG);
        GOMOKU_BOARD = GOMOKU.block();
        GOMOKU_BOARD_BLOCK_ENTITY = GOMOKU.blockEntityType();
        GOMOKU_BOARD_SCREEN_HANDLER = GOMOKU.screenHandlerType();

        GO = BoardGameRegistry.register("go_board", GoConfig.CONFIG);
        GO_BOARD = GO.block();
        GO_BOARD_BLOCK_ENTITY = GO.blockEntityType();
        GO_BOARD_SCREEN_HANDLER = GO.screenHandlerType();
    }
}
