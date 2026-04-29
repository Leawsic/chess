package site.leawsic.chess.block;

import net.minecraft.block.Block;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.screen.ScreenHandlerType;
import site.leawsic.chess.config.GomokuConfig;

public class ModBlocks {
    // 游戏对象
    public static BoardGameObjects GO;

    public static Block GO_BOARD;
    public static BlockEntityType<BaseBoardBlockEntity> GO_BOARD_BLOCK_ENTITY;
    public static ScreenHandlerType<site.leawsic.chess.screen.BaseBoardScreenHandler> GO_BOARD_SCREEN_HANDLER;

    public static void register() {
        GO = BoardGameRegistry.register("go_board", GomokuConfig.CONFIG);
        GO_BOARD = GO.block();
        GO_BOARD_BLOCK_ENTITY = GO.blockEntityType();
        GO_BOARD_SCREEN_HANDLER = GO.screenHandlerType();
    }
}