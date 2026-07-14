package site.leawsic.chess.config;

/**
 * Lightweight Gomoku move selection based on the same threat-first ordering
 * used by the reference AI: win immediately, block an immediate loss, then
 * favor open lines and central positions.
 */
public final class GomokuAi {
    private static final int[][] DIRECTIONS = {{1, 0}, {0, 1}, {1, 1}, {1, -1}};

    private GomokuAi() {
    }

    public static Move chooseMove(int[][] board, int aiPlayer) {
        int rows = board.length;
        int cols = board[0].length;
        int opponent = aiPlayer == 1 ? 2 : 1;
        int centerX = cols / 2;
        int centerY = rows / 2;
        Move best = null;
        int bestScore = Integer.MIN_VALUE;

        for (int y = 0; y < rows; y++) {
            for (int x = 0; x < cols; x++) {
                if (board[y][x] != 0) continue;
                int attack = scorePoint(board, x, y, aiPlayer);
                int defense = scorePoint(board, x, y, opponent);
                int score = Math.max(attack, defense >= 1_000_000 ? defense : attack + defense);
                score -= Math.abs(x - centerX) + Math.abs(y - centerY);
                if (score > bestScore) {
                    bestScore = score;
                    best = new Move(x, y, aiPlayer);
                }
            }
        }
        return best;
    }

    private static int scorePoint(int[][] board, int x, int y, int player) {
        int score = 0;
        for (int[] direction : DIRECTIONS) {
            int forward = count(board, x, y, direction[0], direction[1], player);
            int backward = count(board, x, y, -direction[0], -direction[1], player);
            int stones = forward + backward + 1;
            int openEnds = isOpen(board, x + (forward + 1) * direction[0], y + (forward + 1) * direction[1]) ? 1 : 0;
            openEnds += isOpen(board, x - (backward + 1) * direction[0], y - (backward + 1) * direction[1]) ? 1 : 0;
            score += lineScore(stones, openEnds);
        }
        return score;
    }

    private static int count(int[][] board, int x, int y, int dx, int dy, int player) {
        int total = 0;
        for (x += dx, y += dy; y >= 0 && y < board.length && x >= 0 && x < board[0].length && board[y][x] == player; x += dx, y += dy) {
            total++;
        }
        return total;
    }

    private static boolean isOpen(int[][] board, int x, int y) {
        return y >= 0 && y < board.length && x >= 0 && x < board[0].length && board[y][x] == 0;
    }

    private static int lineScore(int stones, int openEnds) {
        if (stones >= 5) return 1_000_000;
        if (stones == 4) return openEnds == 2 ? 100_000 : openEnds == 1 ? 10_000 : 0;
        if (stones == 3) return openEnds == 2 ? 5_000 : openEnds == 1 ? 500 : 0;
        if (stones == 2) return openEnds == 2 ? 200 : openEnds == 1 ? 30 : 0;
        return openEnds == 2 ? 10 : 0;
    }
}
