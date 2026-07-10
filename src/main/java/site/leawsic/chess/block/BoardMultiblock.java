package site.leawsic.chess.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public final class BoardMultiblock {
    public static final int SIZE = 3;

    public static List<BlockPos> getPlaceholderOffsets() {
        List<BlockPos> offsets = new ArrayList<>();
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                if (dx == 0 && dz == 0) continue;
                offsets.add(new BlockPos(dx, 0, dz));
            }
        }
        return offsets;
    }

    public static boolean canPlace(World world, BlockPos origin) {
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                BlockPos pos = origin.add(dx, 0, dz);
                if (!world.getBlockState(pos).isAir()) return false;
            }
        }
        return true;
    }

    public static void placePlaceholders(World world, BlockPos origin, Function<BlockPos, BlockState> placeholderStateFactory) {
        for (BlockPos offset : getPlaceholderOffsets()) {
            BlockPos pos = origin.add(offset);
            BlockState placeholderState = placeholderStateFactory.apply(offset);
            world.setBlockState(pos, placeholderState, 3);
        }
    }

    public static void removePlaceholders(World world, BlockPos origin, Block placeholderBlock) {
        for (BlockPos offset : getPlaceholderOffsets()) {
            BlockPos pos = origin.add(offset);
            world.setBlockState(pos, Blocks.AIR.getDefaultState(), 35);
        }
    }

    public static void removeAll(World world, BlockPos origin) {
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                world.setBlockState(origin.add(dx, 0, dz), Blocks.AIR.getDefaultState(), 35);
            }
        }
    }
}
