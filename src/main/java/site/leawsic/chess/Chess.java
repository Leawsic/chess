package site.leawsic.chess;

import net.fabricmc.api.ModInitializer;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import site.leawsic.chess.block.ModBlocks;
import site.leawsic.chess.item.ModItemGroup;
import site.leawsic.chess.item.ModItems;
import site.leawsic.chess.network.ChessNetwork;

public class Chess implements ModInitializer {
    public static final String MOD_ID = "chess";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static Identifier id(String path) {
        return new Identifier(MOD_ID, path);
    }

    @Override
    public void onInitialize() {
        LOGGER.info("Let's play chess!");

        ModBlocks.register();
        ModItems.register();
        ModItemGroup.register();
        ChessNetwork.registerServerReceivers();
    }
}