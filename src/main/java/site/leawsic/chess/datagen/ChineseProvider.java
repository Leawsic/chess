package site.leawsic.chess.datagen;

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricLanguageProvider;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKey;
import site.leawsic.chess.Chess;
import site.leawsic.chess.block.ModBlocks;

public class ChineseProvider extends FabricLanguageProvider {
    public ChineseProvider(FabricDataOutput dataOutput) {
        super(dataOutput, "zh_cn");
    }

    @Override
    public void generateTranslations(TranslationBuilder translationBuilder) {
        translationBuilder.add(ModBlocks.GO_BOARD, "五子棋盘");
        translationBuilder.add(RegistryKey.of(Registries.ITEM_GROUP.getKey(), Chess.id("chess_group")), "棋类游戏");
        translationBuilder.add("gui.chess.clear", "清空");
        translationBuilder.add("gui.chess.edit_mode", "编辑模式");
        translationBuilder.add("gui.chess.game_over", "游戏结束");
        translationBuilder.add("block.chess.generic_board", "通用棋盘");
        
        // 五子棋棋子翻译
        translationBuilder.add("gui.chess.piece.black", "黑棋");
        translationBuilder.add("gui.chess.piece.black_selected", "- 黑棋");
        translationBuilder.add("gui.chess.piece.white", "白棋");
        translationBuilder.add("gui.chess.piece.white_selected", "- 白棋");
        
        translationBuilder.add("gui.chess.join", "加入");
        translationBuilder.add("gui.chess.leave", "退出");
        translationBuilder.add("gui.chess.multiplayer", "双人模式");
        translationBuilder.add("gui.chess.singleplayer", "单人模式");
        translationBuilder.add("gui.chess.host_black", "房主执黑");
        translationBuilder.add("gui.chess.host_white", "房主执白");

        translationBuilder.add("gui.chess.turn", "回合");
        translationBuilder.add("gui.chess.turn_format", "%s回合");
        translationBuilder.add("gui.chess.winner_suffix", "获胜！");
        translationBuilder.add("gui.chess.clear_hint", "点击 清空 开始新游戏");
    }
}
