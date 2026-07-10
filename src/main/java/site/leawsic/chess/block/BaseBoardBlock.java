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
    protected static final VoxelShape SHAPE = Block.createCuboidShape(-16.0, 0.0, -16.0, 32.0, 1.0, 32.0);
    private final Supplier<BlockEntityType<?>> blockEntityTypeSupplier;
    private final Supplier<ScreenHandlerType<?>> screenHandlerTypeSupplier;
    private final Supplier<Block> placeholderSupplier;

    protected BaseBoardBlock(Settings settings,
                             Supplier<BlockEntityType<?>> blockEntityTypeSupplier,
                             Supplier<ScreenHandlerType<?>> screenHandlerTypeSupplier,
                             Supplier<Block> placeholderSupplier) {
        super(settings);
        this.blockEntityTypeSupplier = blockEntityTypeSupplier;
        this.screenHandlerTypeSupplier = screenHandlerTypeSupplier;
        this.placeholderSupplier = placeholderSupplier;
        setDefaultState(getDefaultState().with(FACING, Direction.NORTH));
    }

    protected BlockState getPlaceholderState(BlockState mainState, BlockPos offset) {
        Block placeholder = placeholderSupplier.get();
        if (placeholder instanceof BoardPlaceholderBlock) {
            return placeholder.getDefaultState()
                    .with(BoardPlaceholderBlock.FACING, mainState.get(FACING))
                    .with(BoardPlaceholderBlock.OFFSET_X, offset.getX() + 1)
                    .with(BoardPlaceholderBlock.OFFSET_Z, offset.getZ() + 1);
        }
        return Blocks.AIR.getDefaultState();
    }

    protected Block getPlaceholderBlock() {
        Block placeholder = placeholderSupplier.get();
        return placeholder != null ? placeholder : Blocks.AIR;
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        if (!BoardMultiblock.canPlace(ctx.getWorld(), ctx.getBlockPos())) {
            return null;
        }
        return getDefaultState().with(FACING, ctx.getHorizontalPlayerFacing());
    }

    @Override
    public void onPlaced(World world, BlockPos pos, BlockState state, net.minecraft.entity.LivingEntity placer, net.minecraft.item.ItemStack itemStack) {
        super.onPlaced(world, pos, state, placer, itemStack);
        if (!world.isClient) {
            BoardMultiblock.placePlaceholders(world, pos, offset -> getPlaceholderState(state, offset));
        }
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
    public void onBreak(World world, BlockPos pos, BlockState state, PlayerEntity player) {
        if (!world.isClient) {
            BoardMultiblock.removePlaceholders(world, pos, getPlaceholderBlock());
        }
        super.onBreak(world, pos, state, player);
    }

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        if (!world.isClient) {
            BlockEntity be = world.getBlockEntity(pos);
            if (be instanceof BaseBoardBlockEntity boardEntity) {
                // 如果棋盘没有被占用，自动设置为房主。
                if (boardEntity.getHostPlayer() == null) {
                    boardEntity.setHost(player.getUuid());
                } else if (!boardEntity.isMultiplayer()
                        && !boardEntity.isInGame(player.getUuid())
                        && player instanceof ServerPlayerEntity serverPlayer
                        && serverPlayer.getServer().getPlayerManager().getPlayer(boardEntity.getHostPlayer()) == null) {
                    boardEntity.replaceHost(player.getUuid());
                }
            }

            player.openHandledScreen(new ExtendedScreenHandlerFactory() {
                @Nullable
                @Override
                public ScreenHandler createMenu(int syncId, PlayerInventory inv, PlayerEntity player) {
                    BlockEntity be = world.getBlockEntity(pos);
                    if (be instanceof BaseBoardBlockEntity base) {
                        return new BaseBoardScreenHandler(screenHandlerTypeSupplier.get(), syncId, inv, pos,
                                base.getConfig(), base.isInGame(player.getUuid()));
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
                    BlockEntity be = world.getBlockEntity(pos);
                    buf.writeBoolean(be instanceof BaseBoardBlockEntity base && base.isInGame(player.getUuid()));
                }
            });
        }
        return ActionResult.SUCCESS;
    }
}
