package site.leawsic.chess.screen;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.util.math.BlockPos;
import site.leawsic.chess.config.ChessGameConfig;

public class BaseBoardScreenHandler extends ScreenHandler {
    private final ChessGameConfig config;
    private final BlockPos boardPos;

    public BaseBoardScreenHandler(ScreenHandlerType<?> type, int syncId,
                                  PlayerInventory playerInventory,
                                  BlockPos boardPos, ChessGameConfig config) {
        super(type, syncId);
        this.boardPos = boardPos;
        this.config = config;
    }

    public ChessGameConfig getConfig() { return config; }
    public BlockPos getBoardPos() { return boardPos; }

    @Override
    public ItemStack quickMove(PlayerEntity player, int slot) { return ItemStack.EMPTY; }
    @Override
    public boolean canUse(PlayerEntity player) { return true; }
}