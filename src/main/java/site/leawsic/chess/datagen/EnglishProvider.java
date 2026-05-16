package site.leawsic.chess.datagen;

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricLanguageProvider;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKey;
import site.leawsic.chess.Chess;
import site.leawsic.chess.block.ModBlocks;

public class EnglishProvider extends FabricLanguageProvider {
    public EnglishProvider(FabricDataOutput dataOutput) {
        super(dataOutput);
    }

    @Override
    public void generateTranslations(TranslationBuilder translationBuilder) {
        translationBuilder.add(ModBlocks.GO_BOARD, "Gomoku Board");
        translationBuilder.add(RegistryKey.of(Registries.ITEM_GROUP.getKey(), Chess.id("chess_group")), "Chess Game");
        translationBuilder.add("gui.chess.clear", "Clear");
        translationBuilder.add("gui.chess.edit_mode", "Edit Mode");
        translationBuilder.add("gui.chess.game_over", "Game Over");
        translationBuilder.add("block.chess.generic_board","Generic Board");
        
        // 五子棋棋子翻译
        translationBuilder.add("gui.chess.piece.black", "Black");
        translationBuilder.add("gui.chess.piece.black_selected", "- Black");
        translationBuilder.add("gui.chess.piece.white", "White");
        translationBuilder.add("gui.chess.piece.white_selected", "- White");

        translationBuilder.add("gui.chess.join", "Join");
        translationBuilder.add("gui.chess.leave", "Leave");
        translationBuilder.add("gui.chess.multiplayer", "Multiplayer");
        translationBuilder.add("gui.chess.singleplayer", "Single Player");
        translationBuilder.add("gui.chess.host_black", "Host Black");
        translationBuilder.add("gui.chess.host_white", "Host White");

        translationBuilder.add("gui.chess.turn", "Turn");
        translationBuilder.add("gui.chess.turn_format", "%s's Turn");
        translationBuilder.add("gui.chess.winner_suffix", " Wins!");
        translationBuilder.add("gui.chess.clear_hint", "Click Clear to start a new game");
    }
}
