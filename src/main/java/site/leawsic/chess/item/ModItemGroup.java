package site.leawsic.chess.item;

import site.leawsic.chess.Chess;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.text.Text;
import site.leawsic.chess.block.ModBlocks;

public class ModItemGroup {
    public static final ItemGroup CHESS_GROUP = Registry.register(
            Registries.ITEM_GROUP,
            Chess.id("chess_group"),
            FabricItemGroup.builder()
                    .displayName(Text.translatable("itemGroup.chess.chess_group"))
                    .icon(() -> new ItemStack(ModItems.GO_BOARD))
                    .entries((displayContext, entries) -> {
                        entries.add(ModItems.GO_BOARD);
                        entries.add(ModBlocks.GO_BOARD);
                    })
                    .build()
    );
    public static void register() {}
}