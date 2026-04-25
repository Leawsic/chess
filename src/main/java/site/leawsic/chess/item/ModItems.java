package site.leawsic.chess.item;

import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import site.leawsic.chess.Chess;
import site.leawsic.chess.block.ModBlocks;

public class ModItems {
    public static final Item GO_BOARD = Registry.register(
            Registries.ITEM,
            Chess.id("go_board"),
            new BlockItem(ModBlocks.GO_BOARD, new Item.Settings())
    );

    public static void register() {
    }
}