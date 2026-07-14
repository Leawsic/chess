package site.leawsic.chess.config;

/**
 * A small, deterministic Xiangqi AI suitable for the server thread. It only
 * considers legal moves, then favors checks, captures, and active positions.
 */
public final class XiangqiAi {
    private XiangqiAi() {
    }

    public static Move chooseMove(int[][] board, int side) {
        Move best = null;
        int bestScore = Integer.MIN_VALUE;
        for (int fromY = 0; fromY < XiangqiConfig.ROWS; fromY++) {
            for (int fromX = 0; fromX < XiangqiConfig.COLS; fromX++) {
                int piece = board[fromY][fromX];
                if (XiangqiConfig.color(piece) != side) continue;
                for (int toY = 0; toY < XiangqiConfig.ROWS; toY++) {
                    for (int toX = 0; toX < XiangqiConfig.COLS; toX++) {
                        if (XiangqiConfig.color(board[toY][toX]) == side
                                || !XiangqiConfig.isLegalMove(board, fromX, fromY, toX, toY)) continue;
                        int captured = board[toY][toX];
                        board[toY][toX] = piece;
                        board[fromY][fromX] = 0;
                        boolean legal = !XiangqiConfig.isInCheck(board, side);
                        int score = legal ? scoreMove(board, piece, captured, toX, toY, side) : Integer.MIN_VALUE;
                        board[fromY][fromX] = piece;
                        board[toY][toX] = captured;
                        if (score > bestScore) {
                            bestScore = score;
                            best = new Move(fromX, fromY, toX, toY);
                        }
                    }
                }
            }
        }
        return best;
    }

    private static int scoreMove(int[][] board, int piece, int captured, int toX, int toY, int side) {
        int score = pieceValue(Math.abs(captured)) * 100 - pieceValue(Math.abs(piece));
        if (Math.abs(captured) == XiangqiConfig.GENERAL) score += 1_000_000;
        if (XiangqiConfig.isInCheck(board, -side)) score += 20_000;
        score += 12 - Math.abs(toX - 4) * 2;
        score += side == XiangqiConfig.RED ? (9 - toY) : toY;
        return score;
    }

    private static int pieceValue(int piece) {
        return switch (piece) {
            case XiangqiConfig.ROOK -> 90;
            case XiangqiConfig.CANNON -> 45;
            case XiangqiConfig.HORSE -> 40;
            case XiangqiConfig.ELEPHANT, XiangqiConfig.ADVISOR -> 20;
            case XiangqiConfig.SOLDIER -> 10;
            case XiangqiConfig.GENERAL -> 10_000;
            default -> 0;
        };
    }

    public record Move(int fromX, int fromY, int toX, int toY) {
    }
}
