package site.leawsic.chess.block;

import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import site.leawsic.chess.screen.BaseBoardScreenHandler;

import java.util.function.Supplier;

/**
 * 通用棋盘方块，通过提供方实体类型和配置来适配不同游戏。
 * 使用时需要子类化并传入具体类型，或直接通过匿名类注册。
 */
public abstract class BaseBoardBlock extends BlockWithEntity {
    private final Supplier<BlockEntityType<?>> blockEntityTypeSupplier;

    protected BaseBoardBlock(Settings settings, Supplier<BlockEntityType<?>> blockEntityTypeSupplier) {
        super(settings);
        this.blockEntityTypeSupplier = blockEntityTypeSupplier;
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
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        if (!world.isClient) {
            NamedScreenHandlerFactory factory = this.createScreenHandlerFactory(state, world, pos);
            if (factory != null) {
                player.openHandledScreen(factory);
            }
        }
        return ActionResult.SUCCESS;
    }

    @Nullable
    @Override
    public NamedScreenHandlerFactory createScreenHandlerFactory(BlockState state, World world, BlockPos pos) {
        return new NamedScreenHandlerFactory() {
            @Override
            public Text getDisplayName() {
                BlockEntity be = world.getBlockEntity(pos);
                if (be instanceof BaseBoardBlockEntity base) {
                    return Text.translatable(base.getConfig().getTranslationKey());
                }
                return Text.translatable("block.chess.generic_board");
            }

            @Nullable
            @Override
            public ScreenHandler createMenu(int syncId, PlayerInventory inv, PlayerEntity player) {
                BlockEntity be = world.getBlockEntity(pos);
                if (be instanceof BaseBoardBlockEntity base) {
                    return new BaseBoardScreenHandler(syncId, ScreenHandlerContext.create(world, pos), base.getConfig());
                }
                return null;
            }
        };
    }
}