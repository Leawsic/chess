package site.leawsic.chess.config;

import net.minecraft.util.Identifier;
import site.leawsic.chess.Chess;

import java.util.*;

public class GomokuConfig {
    public static final ChessGameConfig CONFIG = baseConfig()
            .translationKey("block.chess.gomoku_board")
            .placeRule(GomokuConfig::placeGomokuPiece)
            .build();

    public static final ChessGameConfig GO_CONFIG = baseConfig()
            .translationKey("block.chess.go_board")
            .placeRule(GomokuConfig::placeGoPiece)
            .passRule(GomokuConfig::passGoTurn)
            .build();

    private static ChessGameConfig.Builder baseConfig() {
        return new ChessGameConfig.Builder()
            .rows(19)
            .cols(19)
            .playerCount(2)
            .initialPlayer(1) // 黑先
            .pieceTypes(
                    ChessGameConfig.PieceType.EMPTY,
                    new ChessGameConfig.PieceType(1, "black"),
                    new ChessGameConfig.PieceType(2, "white")
            )
            .pieceTextures(
                    null,  // EMPTY 不使用
                    Chess.id("block/piece_black"),
                    Chess.id("block/piece_white")
            )
            .boardTopTexture(Chess.id("block/gomoku_board_top"))
            .boardBottomTexture(Chess.id("block/go_board_bottom"))
            .boardSideTexture(new Identifier("minecraft","block/birch_planks"))
            .starPoints(List.of(
                    new int[]{3, 3}, new int[]{3, 9}, new int[]{3, 15},
                    new int[]{9, 3}, new int[]{9, 9}, new int[]{9, 15},
                    new int[]{15, 3}, new int[]{15, 9}, new int[]{15, 15}
            ))
            .boardTextureWidth(256)
            .boardTextureHeight(256)
            .boardLeftU(18)
            .boardTopV(20)
            .boardCellPixelSize(12)
            .pieceTextureSize(12);
    }

    private static ChessGameConfig.PlaceResult placeGomokuPiece(site.leawsic.chess.block.BaseBoardBlockEntity entity, Move move) {
        int[][] board = entity.getBoard();
        int x = move.x(), y = move.y(), player = move.player();
        int cols = entity.getConfig().getCols(), rows = entity.getConfig().getRows();
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
            if (count >= 5) return ChessGameConfig.PlaceResult.gameOver(player);
        }
        return ChessGameConfig.PlaceResult.success(true);
    }

    private static ChessGameConfig.PlaceResult placeGoPiece(site.leawsic.chess.block.BaseBoardBlockEntity entity, Move move) {
        int[][] board = entity.getBoard();
        int x = move.x(), y = move.y(), player = move.player();
        int opponent = player == 1 ? 2 : 1;
        int cols = entity.getConfig().getCols(), rows = entity.getConfig().getRows();
        if (board[y][x] != 0 || entity.getKoX() == x && entity.getKoY() == y) return ChessGameConfig.PlaceResult.fail();

        int[][] testBoard = copyBoard(board, rows, cols);
        testBoard[y][x] = player;
        Set<Long> captured = new HashSet<>();
        for (int[] d : DIRS) {
            int nx = x + d[0], ny = y + d[1];
            if (nx >= 0 && nx < cols && ny >= 0 && ny < rows && testBoard[ny][nx] == opponent) {
                Set<Long> group = new HashSet<>();
                if (!hasLiberty(nx, ny, opponent, testBoard, cols, rows, group)) captured.addAll(group);
            }
        }
        for (long pos : captured) testBoard[(int) pos][(int) (pos >> 32)] = 0;
        Set<Long> selfGroup = new HashSet<>();
        if (!hasLiberty(x, y, player, testBoard, cols, rows, selfGroup)) return ChessGameConfig.PlaceResult.fail();

        List<Move> capturedMoves = new ArrayList<>();
        for (long pos : captured) capturedMoves.add(new Move((int) (pos >> 32), (int) pos, opponent));
        int koX = -1, koY = -1;
        if (captured.size() == 1 && selfGroup.size() == 1) {
            long pos = captured.iterator().next();
            koX = (int) (pos >> 32);
            koY = (int) pos;
        }
        return ChessGameConfig.PlaceResult.success(true, capturedMoves, koX, koY);
    }

    private static ChessGameConfig.PlaceResult passGoTurn(site.leawsic.chess.block.BaseBoardBlockEntity entity, int player) {
        if (entity.getConsecutivePasses() < 1) return ChessGameConfig.PlaceResult.success(true);
        Score score = score(entity.getBoard(), entity.getConfig().getRows(), entity.getConfig().getCols());
        int winner = score.blackScore() > score.whiteScore() ? 1 : score.whiteScore() > score.blackScore() ? 2 : 0;
        return ChessGameConfig.PlaceResult.gameOver(winner, score.blackScore(), score.whiteScore());
    }

    private static final int[][] DIRS = {{1, 0}, {-1, 0}, {0, 1}, {0, -1}};

    private static boolean hasLiberty(int x, int y, int player, int[][] board, int cols, int rows, Set<Long> group) {
        long key = ((long) x << 32) | (y & 0xFFFFFFFFL);
        if (!group.add(key)) return false;
        for (int[] d : DIRS) {
            int nx = x + d[0], ny = y + d[1];
            if (nx < 0 || nx >= cols || ny < 0 || ny >= rows) continue;
            if (board[ny][nx] == 0 || board[ny][nx] == player && hasLiberty(nx, ny, player, board, cols, rows, group)) return true;
        }
        return false;
    }

    private static int[][] copyBoard(int[][] board, int rows, int cols) {
        int[][] copy = new int[rows][cols];
        for (int row = 0; row < rows; row++) System.arraycopy(board[row], 0, copy[row], 0, cols);
        return copy;
    }

    private static Score score(int[][] board, int rows, int cols) {
        boolean[][] visited = new boolean[rows][cols];
        double black = 0, white = 6.5;
        for (int row = 0; row < rows; row++) for (int col = 0; col < cols; col++) {
            if (board[row][col] == 1) black++;
            else if (board[row][col] == 2) white++;
            else if (!visited[row][col]) {
                Territory territory = collectTerritory(col, row, board, visited, rows, cols);
                if (territory.borderColor() == 1) black += territory.size();
                else if (territory.borderColor() == 2) white += territory.size();
            }
        }
        return new Score(black, white);
    }

    private static Territory collectTerritory(int startX, int startY, int[][] board, boolean[][] visited, int rows, int cols) {
        Deque<Long> queue = new ArrayDeque<>();
        queue.add(((long) startX << 32) | (startY & 0xFFFFFFFFL));
        visited[startY][startX] = true;
        int size = 0, borderColor = 0;
        boolean mixedBorder = false;
        while (!queue.isEmpty()) {
            long current = queue.removeFirst();
            int x = (int) (current >> 32), y = (int) current;
            size++;
            for (int[] d : DIRS) {
                int nx = x + d[0], ny = y + d[1];
                if (nx < 0 || nx >= cols || ny < 0 || ny >= rows) continue;
                int value = board[ny][nx];
                if (value == 0 && !visited[ny][nx]) {
                    visited[ny][nx] = true;
                    queue.add(((long) nx << 32) | (ny & 0xFFFFFFFFL));
                } else if (value != 0) {
                    if (borderColor == 0) borderColor = value;
                    else if (borderColor != value) mixedBorder = true;
                }
            }
        }
        return new Territory(size, mixedBorder ? 0 : borderColor);
    }

    private record Score(double blackScore, double whiteScore) {}
    private record Territory(int size, int borderColor) {}
}
