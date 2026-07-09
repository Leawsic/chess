package site.leawsic.chess.config;

import net.minecraft.util.Identifier;
import site.leawsic.chess.Chess;

import java.util.*;

public class GoConfig {
    public static final ChessGameConfig CONFIG = new ChessGameConfig.Builder()
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
                    null,
                    Chess.id("block/piece_black"),
                    Chess.id("block/piece_white")
            )
            .boardTopTexture(Chess.id("block/weiqi_board_top"))
            .boardBottomTexture(Chess.id("block/go_board_bottom"))
            .boardSideTexture(new Identifier("minecraft","block/birch_planks"))
            .translationKey("block.chess.go_board")
            .starPoints(List.of(
                    new int[]{3, 3}, new int[]{3, 9}, new int[]{3, 15},
                    new int[]{9, 3}, new int[]{9, 9}, new int[]{9, 15},
                    new int[]{15, 3}, new int[]{15, 9}, new int[]{15, 15}
            ))
            .boardTextureWidth(256)
            .boardTextureHeight(256)
            .boardLeftU(14)
            .boardTopV(14)
            .boardCellPixelSize(13)
            .pieceTextureSize(12)
            .placeRule((entity, move) -> {
                int[][] board = entity.getBoard();
                int x = move.x();
                int y = move.y();
                int player = move.player();
                int opponent = player == 1 ? 2 : 1;
                int cols = entity.getConfig().getCols();
                int rows = entity.getConfig().getRows();

                // 检查落子位置是否为空
                if (board[y][x] != 0) return ChessGameConfig.PlaceResult.fail();
                if (entity.getKoX() == x && entity.getKoY() == y) return ChessGameConfig.PlaceResult.fail();

                int[][] testBoard = copyBoard(board, rows, cols);
                testBoard[y][x] = player;

                // 获取被提掉的对方棋子
                Set<Long> captured = new HashSet<>();
                int[][] dirs = {{1, 0}, {-1, 0}, {0, 1}, {0, -1}};

                // 检查四方向邻子是否能被提
                for (int[] d : dirs) {
                    int nx = x + d[0], ny = y + d[1];
                    if (nx >= 0 && nx < cols && ny >= 0 && ny < rows && testBoard[ny][nx] == opponent) {
                        Set<Long> group = new HashSet<>();
                        if (!hasLiberty(nx, ny, opponent, testBoard, cols, rows, group)) {
                            captured.addAll(group);
                        }
                    }
                }

                // 移除被提的对方棋子
                for (long pos : captured) {
                    int cx = (int) (pos >> 32);
                    int cy = (int) (pos & 0xFFFFFFFFL);
                    testBoard[cy][cx] = 0;
                }

                // 检查落子方自身是否有气；如果无气且提子数为 0，则为禁着点（自杀）
                Set<Long> selfGroup = new HashSet<>();
                if (!hasLiberty(x, y, player, testBoard, cols, rows, selfGroup)) {
                    return ChessGameConfig.PlaceResult.fail();
                }

                List<Move> capturedMoves = new ArrayList<>();
                for (long pos : captured) {
                    int cx = (int) (pos >> 32);
                    int cy = (int) (pos & 0xFFFFFFFFL);
                    capturedMoves.add(new Move(cx, cy, opponent));
                }

                int koX = -1;
                int koY = -1;
                if (captured.size() == 1) {
                    long capturedPos = captured.iterator().next();
                    int capturedX = (int) (capturedPos >> 32);
                    int capturedY = (int) (capturedPos & 0xFFFFFFFFL);
                    if (selfGroup.size() == 1) {
                        koX = capturedX;
                        koY = capturedY;
                    }
                }

                // 正常落子，切换玩家
                return ChessGameConfig.PlaceResult.success(true, capturedMoves, koX, koY);
            })
            .passRule((entity, player) -> {
                if (entity.getConsecutivePasses() < 1) {
                    return ChessGameConfig.PlaceResult.success(true);
                }

                Score score = score(entity.getBoard(), entity.getConfig().getRows(), entity.getConfig().getCols());
                int winner = score.blackScore() > score.whiteScore() ? 1 : score.whiteScore() > score.blackScore() ? 2 : 0;
                return ChessGameConfig.PlaceResult.gameOver(winner, score.blackScore(), score.whiteScore());
            })
            .build();

    private static final int[][] DIRS = {{1, 0}, {-1, 0}, {0, 1}, {0, -1}};

    private static boolean hasLiberty(int x, int y, int player, int[][] board, int cols, int rows, Set<Long> group) {
        long key = ((long) x << 32) | (y & 0xFFFFFFFFL);
        if (group.contains(key)) return false;
        group.add(key);

        for (int[] d : DIRS) {
            int nx = x + d[0], ny = y + d[1];
            if (nx < 0 || nx >= cols || ny < 0 || ny >= rows) continue;
            if (board[ny][nx] == 0) return true;
            if (board[ny][nx] == player) {
                if (hasLiberty(nx, ny, player, board, cols, rows, group)) return true;
            }
        }
        return false;
    }

    private static int[][] copyBoard(int[][] board, int rows, int cols) {
        int[][] copy = new int[rows][cols];
        for (int row = 0; row < rows; row++) {
            System.arraycopy(board[row], 0, copy[row], 0, cols);
        }
        return copy;
    }

    private static Score score(int[][] board, int rows, int cols) {
        boolean[][] visited = new boolean[rows][cols];
        double black = 0;
        double white = 6.5; // 简化日/中式贴目，避免平局偏黑。

        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                if (board[row][col] == 1) {
                    black++;
                } else if (board[row][col] == 2) {
                    white++;
                } else if (!visited[row][col]) {
                    Territory territory = collectTerritory(col, row, board, visited, rows, cols);
                    if (territory.borderColor() == 1) {
                        black += territory.size();
                    } else if (territory.borderColor() == 2) {
                        white += territory.size();
                    }
                }
            }
        }
        return new Score(black, white);
    }

    private static Territory collectTerritory(int startX, int startY, int[][] board, boolean[][] visited, int rows, int cols) {
        Deque<long[]> queue = new ArrayDeque<>();
        queue.add(new long[]{startX, startY});
        visited[startY][startX] = true;
        int size = 0;
        int borderColor = 0;
        boolean mixedBorder = false;

        while (!queue.isEmpty()) {
            long[] current = queue.removeFirst();
            int x = (int) current[0];
            int y = (int) current[1];
            size++;

            for (int[] d : DIRS) {
                int nx = x + d[0];
                int ny = y + d[1];
                if (nx < 0 || nx >= cols || ny < 0 || ny >= rows) continue;

                int value = board[ny][nx];
                if (value == 0 && !visited[ny][nx]) {
                    visited[ny][nx] = true;
                    queue.add(new long[]{nx, ny});
                } else if (value != 0) {
                    if (borderColor == 0) {
                        borderColor = value;
                    } else if (borderColor != value) {
                        mixedBorder = true;
                    }
                }
            }
        }

        return new Territory(size, mixedBorder ? 0 : borderColor);
    }

    private record Score(double blackScore, double whiteScore) {}

    private record Territory(int size, int borderColor) {}
}
