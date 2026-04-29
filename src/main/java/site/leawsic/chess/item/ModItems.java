package site.leawsic.chess.item;

import net.minecraft.item.Item;
import site.leawsic.chess.block.ModBlocks;

public class ModItems {
    public static Item GO_BOARD;

    public static void register() {
        GO_BOARD = ModBlocks.GO.item();
    }
}