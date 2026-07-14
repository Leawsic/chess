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
        translationBuilder.add(ModBlocks.GOMOKU_BOARD, "Go/Gomoku Board");
        translationBuilder.add(ModBlocks.XIANGQI_BOARD, "Xiangqi Board");
        translationBuilder.add("block.chess.go_board", "Go Board");
        translationBuilder.add(RegistryKey.of(Registries.ITEM_GROUP.getKey(), Chess.id("chess_group")), "Chess Game");
        translationBuilder.add("gui.chess.clear", "Clear");
        translationBuilder.add("gui.chess.edit_mode", "Edit Mode");
        translationBuilder.add("gui.chess.ai", "Vs AI");
        translationBuilder.add("gui.chess.ai_on", "Vs AI: On");
        translationBuilder.add("gui.chess.ai_black", "AI: You Black");
        translationBuilder.add("gui.chess.ai_white", "AI: You White");
        translationBuilder.add("gui.chess.ai_thinking", "AI is thinking...");
        translationBuilder.add("gui.chess.game_over", "Game Over");
        translationBuilder.add("block.chess.generic_board","Generic Board");
        translationBuilder.add("gui.chess.mode.gomoku", "Gomoku");
        translationBuilder.add("gui.chess.mode.go", "Go");
        translationBuilder.add("gui.chess.mode.tooltip", "Click to switch game mode");
        
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
        translationBuilder.add("gui.chess.pass", "Pass");
        translationBuilder.add("gui.chess.go.finish", "Score Game");
        translationBuilder.add("gui.chess.go.black_score", "Black: %s");
        translationBuilder.add("gui.chess.go.white_score", "White: %s");
        translationBuilder.add("gui.chess.exit_confirm", "Press Esc again to leave the board; the game will be preserved");
        translationBuilder.add("gui.chess.draw", "Draw");
        translationBuilder.add("gui.chess.xq.red_turn", "Red's Turn");
        translationBuilder.add("gui.chess.xq.black_turn", "Black's Turn");
        translationBuilder.add("gui.chess.xq.red_wins", "Red Wins!");
        translationBuilder.add("gui.chess.xq.black_wins", "Black Wins!");
        translationBuilder.add("gui.chess.xq.red", "Red");
        translationBuilder.add("gui.chess.xq.black", "Black");
        translationBuilder.add("gui.chess.xq.multiplayer_turn", "%s (%s) vs %s (%s) | Turn: %s");
        translationBuilder.add("gui.chess.xq.host_red", "Host Red");
        translationBuilder.add("gui.chess.xq.host_black", "Host Black");
        translationBuilder.add("gui.chess.xq.ai", "Vs AI");
        translationBuilder.add("gui.chess.xq.ai_red", "AI: You Red");
        translationBuilder.add("gui.chess.xq.ai_black", "AI: You Black");
        translationBuilder.add("gui.chess.xq.player_turn", "Your Turn");
        translationBuilder.add("gui.chess.xq.ai_turn", "AI Thinking");
        translationBuilder.add("gui.chess.xq.not_player", "You are not a player in this game");
        translationBuilder.add("gui.chess.xq.game_full", "This game already has two players");
        translationBuilder.add("gui.chess.xq.host_only", "Only the host can set the piece colors");
        translationBuilder.add("gui.chess.xq.invalid_colors", "Invalid piece color assignment");
        translationBuilder.add("gui.chess.xq.game_over", "The game is over");
        translationBuilder.add("gui.chess.xq.not_host", "Only the host can operate this board");
        translationBuilder.add("gui.chess.xq.invalid_position", "Invalid board position");
        translationBuilder.add("gui.chess.xq.empty_position", "There is no piece at the starting position");
        translationBuilder.add("gui.chess.xq.not_your_turn", "It is not your turn");
        translationBuilder.add("gui.chess.xq.own_piece", "The destination contains your own piece");
        translationBuilder.add("gui.chess.xq.self_check", "Your general would still be in check; you must respond to the check");
        translationBuilder.add("gui.chess.xq.red_in_check", "Red is in check and must respond!");
        translationBuilder.add("gui.chess.xq.black_in_check", "Black is in check and must respond!");
        translationBuilder.add("gui.chess.xq.invalid_move", "This move violates the rules");
        translationBuilder.add("gui.chess.xq.rule.general", "The general moves one square orthogonally inside the palace, or flies along an open file");
        translationBuilder.add("gui.chess.xq.rule.advisor", "The advisor moves one square diagonally inside the palace");
        translationBuilder.add("gui.chess.xq.rule.elephant", "The elephant moves two squares diagonally, cannot cross the river, and its eye must be clear");
        translationBuilder.add("gui.chess.xq.rule.horse", "The horse moves in an L shape and its leg must be clear");
        translationBuilder.add("gui.chess.xq.rule.rook", "The rook moves orthogonally with no pieces in its path");
        translationBuilder.add("gui.chess.xq.rule.cannon", "The cannon moves orthogonally; it needs no screen to move and exactly one screen to capture");
        translationBuilder.add("gui.chess.xq.rule.soldier", "The soldier moves forward; after crossing the river it may also move sideways, never backward");
    }
}
