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
        translationBuilder.add(ModBlocks.GOMOKU_BOARD, "围棋/五子棋盘");
        translationBuilder.add(ModBlocks.XIANGQI_BOARD, "象棋盘");
        translationBuilder.add("block.chess.go_board", "围棋盘");
        translationBuilder.add(RegistryKey.of(Registries.ITEM_GROUP.getKey(), Chess.id("chess_group")), "棋类游戏");
        translationBuilder.add("gui.chess.clear", "清空");
        translationBuilder.add("gui.chess.edit_mode", "编辑模式");
        translationBuilder.add("gui.chess.ai", "人机对战");
        translationBuilder.add("gui.chess.ai_on", "人机对战：开");
        translationBuilder.add("gui.chess.ai_black", "人机：玩家黑方");
        translationBuilder.add("gui.chess.ai_white", "人机：玩家白方");
        translationBuilder.add("gui.chess.ai_thinking", "电脑正在思考...");
        translationBuilder.add("gui.chess.game_over", "游戏结束");
        translationBuilder.add("block.chess.generic_board", "通用棋盘");
        translationBuilder.add("gui.chess.mode.gomoku", "五子棋");
        translationBuilder.add("gui.chess.mode.go", "围棋");
        translationBuilder.add("gui.chess.mode.tooltip", "点击切换棋类");
        
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
        translationBuilder.add("gui.chess.pass", "停一手");
        translationBuilder.add("gui.chess.go.finish", "结算目数");
        translationBuilder.add("gui.chess.go.black_score", "黑方：%s目");
        translationBuilder.add("gui.chess.go.white_score", "白方：%s目");
        translationBuilder.add("gui.chess.exit_confirm", "再次按 Esc 退出棋盘；棋局将保留");
        translationBuilder.add("gui.chess.draw", "平局");
        translationBuilder.add("gui.chess.xq.red_turn", "红方回合");
        translationBuilder.add("gui.chess.xq.black_turn", "黑方回合");
        translationBuilder.add("gui.chess.xq.red_wins", "红方获胜！");
        translationBuilder.add("gui.chess.xq.black_wins", "黑方获胜！");
        translationBuilder.add("gui.chess.xq.red", "红方");
        translationBuilder.add("gui.chess.xq.black", "黑方");
        translationBuilder.add("gui.chess.xq.multiplayer_turn", "%s（%s） 对 %s（%s） | 轮到：%s");
        translationBuilder.add("gui.chess.xq.host_red", "房主红方");
        translationBuilder.add("gui.chess.xq.host_black", "房主黑方");
        translationBuilder.add("gui.chess.xq.ai", "人机对战");
        translationBuilder.add("gui.chess.xq.ai_red", "人机：玩家红方");
        translationBuilder.add("gui.chess.xq.ai_black", "人机：玩家黑方");
        translationBuilder.add("gui.chess.xq.player_turn", "轮到你走棋");
        translationBuilder.add("gui.chess.xq.ai_turn", "电脑思考中");
        translationBuilder.add("gui.chess.xq.not_player", "你不是本局玩家");
        translationBuilder.add("gui.chess.xq.game_full", "对局已有两名玩家");
        translationBuilder.add("gui.chess.xq.host_only", "只有房主可以设置棋色");
        translationBuilder.add("gui.chess.xq.invalid_colors", "棋色设置无效");
        translationBuilder.add("gui.chess.xq.game_over", "对局已结束");
        translationBuilder.add("gui.chess.xq.not_host", "只有房主可以操作棋盘");
        translationBuilder.add("gui.chess.xq.invalid_position", "无效的棋盘位置");
        translationBuilder.add("gui.chess.xq.empty_position", "起点没有棋子");
        translationBuilder.add("gui.chess.xq.not_your_turn", "还没轮到你的回合");
        translationBuilder.add("gui.chess.xq.own_piece", "目标位置已有己方棋子");
        translationBuilder.add("gui.chess.xq.self_check", "走子后己方将帅仍被将军，必须应将");
        translationBuilder.add("gui.chess.xq.red_in_check", "红方被将军，必须应将！");
        translationBuilder.add("gui.chess.xq.black_in_check", "黑方被将军，必须应将！");
        translationBuilder.add("gui.chess.xq.invalid_move", "这步棋不符合走棋规则");
        translationBuilder.add("gui.chess.xq.rule.general", "将帅只能在九宫内横向或纵向走一格，或在无阻挡时飞将");
        translationBuilder.add("gui.chess.xq.rule.advisor", "士只能在九宫内斜走一格");
        translationBuilder.add("gui.chess.xq.rule.elephant", "象只能斜走两格，不能过河，且象眼不能有棋子");
        translationBuilder.add("gui.chess.xq.rule.horse", "马走日字，且马腿不能有棋子");
        translationBuilder.add("gui.chess.xq.rule.rook", "车只能沿横线或竖线走，路径不能有棋子");
        translationBuilder.add("gui.chess.xq.rule.cannon", "炮只能沿横线或竖线走；不吃子时不能隔子，吃子时必须隔一个棋子");
        translationBuilder.add("gui.chess.xq.rule.soldier", "兵卒只能向前走；过河后才能横走，不能后退");
    }
}
