package site.leawsic.chess.config;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public final class GomokuAi {
    private static final int[][] DIRECTIONS = {{1, 0}, {0, 1}, {1, 1}, {1, -1}};
    private static final int CANDIDATE_LIMIT = 20;

    private GomokuAi() {
    }

    public static Move chooseMove(int[][] board, int aiPlayer) {
        int opponent = other(aiPlayer);
        List<ScoredMove> candidates = candidates(board, aiPlayer, CANDIDATE_LIMIT);
        if (candidates.isEmpty()) return null;

        ScoredMove best = null;
        int bestScore = Integer.MIN_VALUE;
        for (ScoredMove candidate : candidates) {
            int x = candidate.move.x(), y = candidate.move.y();
            board[y][x] = aiPlayer;
            int score;
            if (isWin(board, x, y, aiPlayer)) {
                score = 10_000_000;
            } else {
                int worstReply = 0;
                for (ScoredMove reply : candidates(board, opponent, 14)) {
                    board[reply.move.y()][reply.move.x()] = opponent;
                    int replyScore = isWin(board, reply.move.x(), reply.move.y(), opponent)
                            ? 9_000_000 : reply.score * 2 + bestFollowUp(board, aiPlayer);
                    board[reply.move.y()][reply.move.x()] = 0;
                    worstReply = Math.max(worstReply, replyScore);
                }
                // Give forcing attacks priority over merely matching the opponent's shape.
                score = scorePoint(board, x, y, aiPlayer) * 5 + threatBonus(board, x, y, aiPlayer) - worstReply;
            }
            board[y][x] = 0;
            if (score > bestScore) {
                bestScore = score;
                best = candidate;
            }
        }
        return best == null ? null : best.move;
    }

    private static int bestFollowUp(int[][] board, int player) {
        List<ScoredMove> moves = candidates(board, player, 6);
        return moves.isEmpty() ? 0 : moves.get(0).score / 2;
    }

    private static List<ScoredMove> candidates(int[][] board, int player, int limit) {
        int opponent = other(player);
        int centerX = board[0].length / 2, centerY = board.length / 2;
        boolean empty = true;
        List<ScoredMove> moves = new ArrayList<>();
        for (int y = 0; y < board.length; y++) for (int x = 0; x < board[0].length; x++) {
            if (board[y][x] != 0) { empty = false; continue; }
            if (!hasNeighbor(board, x, y) && !(x == centerX && y == centerY)) continue;
            int attack = scorePoint(board, x, y, player);
            int defense = scorePoint(board, x, y, opponent);
            int score = attack * 4 + defense * 2 - Math.abs(x - centerX) - Math.abs(y - centerY);
            moves.add(new ScoredMove(new Move(x, y, player), score));
        }
        if (empty) return List.of(new ScoredMove(new Move(centerX, centerY, player), 1));
        moves.sort(Comparator.comparingInt(ScoredMove::score).reversed());
        return moves.size() > limit ? new ArrayList<>(moves.subList(0, limit)) : moves;
    }

    private static boolean hasNeighbor(int[][] board, int x, int y) {
        for (int dy = -2; dy <= 2; dy++) for (int dx = -2; dx <= 2; dx++) {
            int nx = x + dx, ny = y + dy;
            if (ny >= 0 && ny < board.length && nx >= 0 && nx < board[0].length && board[ny][nx] != 0) return true;
        }
        return false;
    }

    private static int scorePoint(int[][] board, int x, int y, int player) {
        int score = 0;
        for (int[] direction : DIRECTIONS) {
            int forward = count(board, x, y, direction[0], direction[1], player);
            int backward = count(board, x, y, -direction[0], -direction[1], player);
            int stones = forward + backward + 1;
            int open = isOpen(board, x + (forward + 1) * direction[0], y + (forward + 1) * direction[1]) ? 1 : 0;
            open += isOpen(board, x - (backward + 1) * direction[0], y - (backward + 1) * direction[1]) ? 1 : 0;
            score += lineScore(stones, open);
        }
        return score;
    }

    private static int threatBonus(int[][] board, int x, int y, int player) {
        int openFours = 0;
        int openThrees = 0;
        for (int[] direction : DIRECTIONS) {
            int forward = count(board, x, y, direction[0], direction[1], player);
            int backward = count(board, x, y, -direction[0], -direction[1], player);
            int stones = forward + backward + 1;
            int open = isOpen(board, x + (forward + 1) * direction[0], y + (forward + 1) * direction[1]) ? 1 : 0;
            open += isOpen(board, x - (backward + 1) * direction[0], y - (backward + 1) * direction[1]) ? 1 : 0;
            if (stones == 4 && open > 0) openFours++;
            if (stones == 3 && open == 2) openThrees++;
        }
        if (openFours >= 2) return 2_500_000;
        if (openFours == 1) return 450_000;
        if (openThrees >= 2) return 180_000;
        return openThrees == 1 ? 12_000 : 0;
    }

    private static boolean isWin(int[][] board, int x, int y, int player) {
        for (int[] direction : DIRECTIONS) {
            if (1 + count(board, x, y, direction[0], direction[1], player)
                    + count(board, x, y, -direction[0], -direction[1], player) >= 5) return true;
        }
        return false;
    }

    private static int count(int[][] board, int x, int y, int dx, int dy, int player) {
        int total = 0;
        for (x += dx, y += dy; y >= 0 && y < board.length && x >= 0 && x < board[0].length && board[y][x] == player; x += dx, y += dy) total++;
        return total;
    }

    private static boolean isOpen(int[][] board, int x, int y) {
        return y >= 0 && y < board.length && x >= 0 && x < board[0].length && board[y][x] == 0;
    }

    private static int lineScore(int stones, int open) {
        if (stones >= 5) return 2_000_000;
        if (stones == 4) return open == 2 ? 300_000 : open == 1 ? 40_000 : 0;
        if (stones == 3) return open == 2 ? 15_000 : open == 1 ? 1_500 : 0;
        if (stones == 2) return open == 2 ? 600 : open == 1 ? 80 : 0;
        return open == 2 ? 15 : 0;
    }

    private static int other(int player) { return player == 1 ? 2 : 1; }
    private record ScoredMove(Move move, int score) {}
}
