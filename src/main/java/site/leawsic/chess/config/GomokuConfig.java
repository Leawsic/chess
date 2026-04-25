package site.leawsic.chess.config;

import net.minecraft.util.Identifier;
import site.leawsic.chess.Chess;

import java.util.List;

public class GomokuConfig {
    public static final ChessGameConfig CONFIG = new ChessGameConfig.Builder()
            .rows(15)
            .cols(15)
            .playerCount(2)
            .initialPlayer(1) // 黑先
            .pieceTypes(
                    ChessGameConfig.PieceType.EMPTY,
                    new ChessGameConfig.PieceType(1, "black"),
                    new ChessGameConfig.PieceType(2, "white")
            )
            .pieceTextures(
                    null,  // EMPTY 不使用
                    Chess.id("textures/block/piece_black.png"),
                    Chess.id("textures/block/piece_white.png")
            )
            .boardTopTexture(Chess.id("textures/block/go_board_top.png"))
            .boardBottomTexture(new Identifier("minecraft","textures/block/oak_planks.png"))
            .boardSideTexture(Chess.id("textures/block/birch_planks.png"))
            .translationKey("block.chess.go_board")
            .starPoints(List.of(
                    new int[]{3, 3}, new int[]{3, 7}, new int[]{3, 11},
                    new int[]{7, 3}, new int[]{7, 7}, new int[]{7, 11},
                    new int[]{11, 3}, new int[]{11, 7}, new int[]{11, 11}
            ))
            .placeRule((entity, move) -> {
                // 五子棋胜负判定
                int[][] board = entity.getBoard();
                int x = move.x();
                int y = move.y();
                int player = move.player();
                int cols = entity.getConfig().getCols();
                int rows = entity.getConfig().getRows();
                int[][] dirs = {{1, 0}, {0, 1}, {1, 1}, {1, -1}};
                for (int[] d : dirs) {
                    int count = 1;
                    for (int i = 1; i < 5; i++) {
                        int nx = x + d[0] * i, ny = y + d[1] * i;
                        if (nx >= 0 && nx < cols && ny >= 0 && ny < rows && board[ny][nx] == player) count++;
                        else break;
                    }
                    for (int i = 1; i < 5; i++) {
                        int nx = x - d[0] * i, ny = y - d[1] * i;
                        if (nx >= 0 && nx < cols && ny >= 0 && ny < rows && board[ny][nx] == player) count++;
                        else break;
                    }
                    if (count >= 5) {
                        return ChessGameConfig.PlaceResult.gameOver(player);
                    }
                }
                // 正常落子，切换玩家
                return ChessGameConfig.PlaceResult.success(true);
            })
            .build();
}