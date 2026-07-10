package site.leawsic.chess.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.state.property.IntProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;

public class BoardPlaceholderBlock extends Block {
    public static final DirectionProperty FACING = Properties.HORIZONTAL_FACING;
    public static final IntProperty OFFSET_X = IntProperty.of("offset_x", 0, 2);
    public static final IntProperty OFFSET_Z = IntProperty.of("offset_z", 0, 2);
    private static VoxelShape getFullBoardShape(BlockState state) {
        int offsetX = state.get(OFFSET_X) - 1;
        int offsetZ = state.get(OFFSET_Z) - 1;
        return Block.createCuboidShape(
                -16.0 - 16.0 * offsetX,
                0.0,
                -16.0 - 16.0 * offsetZ,
                32.0 - 16.0 * offsetX,
                1.0,
                32.0 - 16.0 * offsetZ
        );
    }

    public BoardPlaceholderBlock(Settings settings) {
        super(settings);
        setDefaultState(getDefaultState()
                .with(FACING, Direction.NORTH)
                .with(OFFSET_X, 0)
                .with(OFFSET_Z, 0));
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(FACING, OFFSET_X, OFFSET_Z);
    }

    private BlockPos getOrigin(BlockState state, BlockPos pos) {
        return pos.add(1 - state.get(OFFSET_X), 0, 1 - state.get(OFFSET_Z));
    }

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        if (world.isClient) return ActionResult.SUCCESS;
        BlockPos origin = getOrigin(state, pos);
        BlockState originState = world.getBlockState(origin);
        if (originState.getBlock() instanceof BaseBoardBlock boardBlock) {
            return boardBlock.onUse(originState, world, origin, player, hand, hit);
        }
        return ActionResult.PASS;
    }

    @Override
    public void onBreak(World world, BlockPos pos, BlockState state, PlayerEntity player) {
        if (!world.isClient) {
            BlockPos origin = getOrigin(state, pos);
            BlockState originState = world.getBlockState(origin);
            if (originState.getBlock() instanceof BaseBoardBlock) {
                world.breakBlock(origin, !player.isCreative(), player);
                BoardMultiblock.removeAll(world, origin);
            } else {
                BoardMultiblock.removeAll(world, origin);
            }
        }
        super.onBreak(world, pos, state, player);
    }

    @Override
    public BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.INVISIBLE;
    }

    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return getFullBoardShape(state);
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return getFullBoardShape(state);
    }
}
