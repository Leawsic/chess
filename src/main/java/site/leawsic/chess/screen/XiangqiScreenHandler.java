package site.leawsic.chess.screen;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.util.math.BlockPos;
import site.leawsic.chess.block.XiangqiBoardBlockEntity;

public class XiangqiScreenHandler extends ScreenHandler {
    private final BlockPos boardPos;
    public XiangqiScreenHandler(ScreenHandlerType<?> type, int syncId, PlayerInventory inventory, BlockPos boardPos) { super(type, syncId); this.boardPos = boardPos; }
    public BlockPos getBoardPos() { return boardPos; }
    @Override public ItemStack quickMove(PlayerEntity player, int slot) { return ItemStack.EMPTY; }
    @Override public boolean canUse(PlayerEntity player) { return player.getWorld().getBlockEntity(boardPos) instanceof XiangqiBoardBlockEntity && player.squaredDistanceTo(boardPos.getX() + .5, boardPos.getY() + .5, boardPos.getZ() + .5) <= 64; }
}
