package site.leawsic.chess.config;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashSet;
import java.util.Set;

public final class GoAi {
    private static final int[][] DIRECTIONS = {{1, 0}, {-1, 0}, {0, 1}, {0, -1}};

    private GoAi() {
    }

    public static Move chooseMove(int[][] board, int player, int koX, int koY) {
        int opponent = player == 1 ? 2 : 1;
        int bestScore = Integer.MIN_VALUE;
        Move best = null;
        for (int y = 0; y < board.length; y++) for (int x = 0; x < board[0].length; x++) {
            if (board[y][x] != 0 || (x == koX && y == koY)) continue;
            Position position = play(board, x, y, player);
            if (position == null) continue;

            int score = position.captured * 2_000 + position.liberties * 32;
            score += adjacent(board, x, y, player) * 120;
            score += adjacent(board, x, y, opponent) * 70;
            score -= Math.abs(x - board[0].length / 2) + Math.abs(y - board.length / 2);
            if (score > bestScore) {
                bestScore = score;
                best = new Move(x, y, player);
            }
        }
        return best;
    }

    private static Position play(int[][] board, int x, int y, int player) {
        int[][] copy = copy(board);
        int opponent = player == 1 ? 2 : 1;
        copy[y][x] = player;
        Set<Long> captured = new HashSet<>();
        for (int[] direction : DIRECTIONS) {
            int nx = x + direction[0], ny = y + direction[1];
            if (inBounds(copy, nx, ny) && copy[ny][nx] == opponent) {
                Set<Long> group = group(copy, nx, ny, opponent);
                if (liberties(copy, group) == 0) captured.addAll(group);
            }
        }
        for (long stone : captured) copy[(int) stone][(int) (stone >> 32)] = 0;
        Set<Long> ownGroup = group(copy, x, y, player);
        int ownLiberties = liberties(copy, ownGroup);
        return ownLiberties == 0 ? null : new Position(captured.size(), ownLiberties);
    }

    private static int adjacent(int[][] board, int x, int y, int player) {
        int count = 0;
        for (int[] direction : DIRECTIONS) {
            int nx = x + direction[0], ny = y + direction[1];
            if (inBounds(board, nx, ny) && board[ny][nx] == player) count++;
        }
        return count;
    }

    private static Set<Long> group(int[][] board, int startX, int startY, int player) {
        Set<Long> stones = new HashSet<>();
        Deque<Long> queue = new ArrayDeque<>();
        queue.add(key(startX, startY));
        while (!queue.isEmpty()) {
            long stone = queue.removeFirst();
            if (!stones.add(stone)) continue;
            int x = (int) (stone >> 32), y = (int) stone;
            for (int[] direction : DIRECTIONS) {
                int nx = x + direction[0], ny = y + direction[1];
                if (inBounds(board, nx, ny) && board[ny][nx] == player) queue.add(key(nx, ny));
            }
        }
        return stones;
    }

    private static int liberties(int[][] board, Set<Long> group) {
        Set<Long> liberties = new HashSet<>();
        for (long stone : group) {
            int x = (int) (stone >> 32), y = (int) stone;
            for (int[] direction : DIRECTIONS) {
                int nx = x + direction[0], ny = y + direction[1];
                if (inBounds(board, nx, ny) && board[ny][nx] == 0) liberties.add(key(nx, ny));
            }
        }
        return liberties.size();
    }

    private static int[][] copy(int[][] board) {
        int[][] copy = new int[board.length][];
        for (int y = 0; y < board.length; y++) copy[y] = board[y].clone();
        return copy;
    }

    private static boolean inBounds(int[][] board, int x, int y) {
        return y >= 0 && y < board.length && x >= 0 && x < board[0].length;
    }

    private static long key(int x, int y) {
        return ((long) x << 32) | (y & 0xFFFFFFFFL);
    }

    private record Position(int captured, int liberties) {
    }
}
