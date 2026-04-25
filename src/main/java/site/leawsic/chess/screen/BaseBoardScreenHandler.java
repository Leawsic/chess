package site.leawsic.chess.screen;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerContext;
import site.leawsic.chess.block.ModBlocks;
import site.leawsic.chess.config.ChessGameConfig;

public class BaseBoardScreenHandler extends ScreenHandler {
    private final ScreenHandlerContext context;
    private final ChessGameConfig config;

    public BaseBoardScreenHandler(int syncId, ScreenHandlerContext context, ChessGameConfig config) {
        super(ModBlocks.GO_BOARD_SCREEN_HANDLER, syncId); // 注意：此处暂用GO_BOARD，多棋类时需扩展
        this.context = context;
        this.config = config;
    }

    public ChessGameConfig getConfig() { return config; }
    public ScreenHandlerContext getContext() { return context; }

    @Override
    public ItemStack quickMove(PlayerEntity player, int slot) { return ItemStack.EMPTY; }

    @Override
    public boolean canUse(PlayerEntity player) { return true; }
}