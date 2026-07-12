package site.leawsic.chess.block;

import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import site.leawsic.chess.screen.XiangqiScreenHandler;

import java.util.function.Supplier;

public class XiangqiBoardBlock extends BlockWithEntity {
    public static final DirectionProperty FACING = Properties.HORIZONTAL_FACING;
    private static final VoxelShape SHAPE = Block.createCuboidShape(-16, 0, -16, 32, 1, 32);
    private final Supplier<BlockEntityType<?>> typeSupplier;
    private final Supplier<ScreenHandlerType<?>> screenSupplier;

    public XiangqiBoardBlock(Settings settings, Supplier<BlockEntityType<?>> typeSupplier, Supplier<ScreenHandlerType<?>> screenSupplier) {
        super(settings);
        this.typeSupplier = typeSupplier;
        this.screenSupplier = screenSupplier;
        setDefaultState(getDefaultState().with(FACING, Direction.NORTH));
    }
    @Override protected void appendProperties(StateManager.Builder<Block, BlockState> builder) { builder.add(FACING); }
    @Override public BlockState getPlacementState(ItemPlacementContext ctx) {
        return BoardMultiblock.canPlace(ctx.getWorld(), ctx.getBlockPos())
                ? getDefaultState().with(FACING, ctx.getHorizontalPlayerFacing())
                : null;
    }
    @Override public void onPlaced(World world, BlockPos pos, BlockState state, net.minecraft.entity.LivingEntity placer, net.minecraft.item.ItemStack stack) {
        super.onPlaced(world, pos, state, placer, stack);
        if (!world.isClient) {
            BoardMultiblock.placePlaceholders(world, pos, offset -> ModBlocks.XIANGQI_PLACEHOLDER.getDefaultState()
                    .with(BoardPlaceholderBlock.FACING, state.get(FACING))
                    .with(BoardPlaceholderBlock.OFFSET_X, offset.getX() + 1)
                    .with(BoardPlaceholderBlock.OFFSET_Z, offset.getZ() + 1));
        }
    }
    @Override public BlockEntity createBlockEntity(BlockPos pos, BlockState state) { return typeSupplier.get().instantiate(pos, state); }
    @Override public BlockRenderType getRenderType(BlockState state) { return BlockRenderType.MODEL; }
    @Override public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) { return SHAPE; }
    @Override public VoxelShape getCollisionShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) { return SHAPE; }
    @Override public void onBreak(World world, BlockPos pos, BlockState state, PlayerEntity player) { if (!world.isClient) BoardMultiblock.removeAll(world, pos); super.onBreak(world, pos, state, player); }
    @Override public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        if (!world.isClient && world.getBlockEntity(pos) instanceof XiangqiBoardBlockEntity board) {
            if (board.getHostPlayer() == null) board.setHost(player.getUuid());
            player.openHandledScreen(new ExtendedScreenHandlerFactory() {
                @Override public @Nullable ScreenHandler createMenu(int syncId, PlayerInventory inventory, PlayerEntity ignored) { return new XiangqiScreenHandler(screenSupplier.get(), syncId, inventory, pos); }
                @Override public Text getDisplayName() { return Text.translatable("block.chess.xq_board"); }
                @Override public void writeScreenOpeningData(ServerPlayerEntity ignored, PacketByteBuf buf) { buf.writeBlockPos(pos); }
            });
        }
        return ActionResult.SUCCESS;
    }
}
