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
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.state.property.Properties;
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
import site.leawsic.chess.screen.BaseBoardScreenHandler;

import java.util.function.Supplier;

public abstract class BaseBoardBlock extends BlockWithEntity {
    public static final DirectionProperty FACING = Properties.HORIZONTAL_FACING;
    protected static final VoxelShape SHAPE = Block.createCuboidShape(0.0, 0.0, 0.0, 16.0, 1.0, 16.0);
    private final Supplier<BlockEntityType<?>> blockEntityTypeSupplier;
    private final Supplier<ScreenHandlerType<?>> screenHandlerTypeSupplier;

    protected BaseBoardBlock(Settings settings,
                             Supplier<BlockEntityType<?>> blockEntityTypeSupplier,
                             Supplier<ScreenHandlerType<?>> screenHandlerTypeSupplier) {
        super(settings);
        this.blockEntityTypeSupplier = blockEntityTypeSupplier;
        this.screenHandlerTypeSupplier = screenHandlerTypeSupplier;
        setDefaultState(getDefaultState().with(FACING, Direction.NORTH));
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        return getDefaultState().with(FACING, ctx.getHorizontalPlayerFacing().getOpposite());
    }

    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return blockEntityTypeSupplier.get().instantiate(pos, state);
    }

    @Override
    public BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.MODEL;
    }

    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return SHAPE;
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return SHAPE;
    }

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        if (!world.isClient) {
            BlockEntity be = world.getBlockEntity(pos);
            if (be instanceof BaseBoardBlockEntity boardEntity) {
                // 如果棋盘没有被占用，自动设置为房主
                if (boardEntity.getHostPlayer() == null) {
                    boardEntity.setHost(player.getUuid());
                }
            }

            player.openHandledScreen(new ExtendedScreenHandlerFactory() {
                @Nullable
                @Override
                public ScreenHandler createMenu(int syncId, PlayerInventory inv, PlayerEntity player) {
                    BlockEntity be = world.getBlockEntity(pos);
                    if (be instanceof BaseBoardBlockEntity base) {
                        return new BaseBoardScreenHandler(screenHandlerTypeSupplier.get(),syncId, inv, pos,
                                base.getConfig());
                    }
                    return null;
                }

                @Override
                public Text getDisplayName() {
                    BlockEntity be = world.getBlockEntity(pos);
                    if (be instanceof BaseBoardBlockEntity base) {
                        return Text.translatable(base.getConfig().getTranslationKey());
                    }
                    return Text.translatable("block.chess.generic_board");
                }

                @Override
                public void writeScreenOpeningData(ServerPlayerEntity player, PacketByteBuf buf) {
                    buf.writeBlockPos(pos);
                }
            });
        }
        return ActionResult.SUCCESS;
    }
}