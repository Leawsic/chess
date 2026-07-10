package site.leawsic.chess.block;

import net.minecraft.block.Block;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.screen.ScreenHandlerType;
import site.leawsic.chess.config.GoConfig;
import site.leawsic.chess.config.GomokuConfig;
import site.leawsic.chess.screen.BaseBoardScreenHandler;

public class ModBlocks {
    public static BoardGameObjects GOMOKU;

    public static Block GOMOKU_BOARD;
    public static BlockEntityType<BaseBoardBlockEntity> GOMOKU_BOARD_BLOCK_ENTITY;
    public static ScreenHandlerType<BaseBoardScreenHandler> GOMOKU_BOARD_SCREEN_HANDLER;

    public static void register() {
        GOMOKU = BoardGameRegistry.register("gomoku_board", GomokuConfig.CONFIG, GoConfig.CONFIG);
        GOMOKU_BOARD = GOMOKU.block();
        GOMOKU_BOARD_BLOCK_ENTITY = GOMOKU.blockEntityType();
        GOMOKU_BOARD_SCREEN_HANDLER = GOMOKU.screenHandlerType();
    }
}
