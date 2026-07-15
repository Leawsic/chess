package site.leawsic.chess.config;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public final class XiangqiAi {
    private static final int ROOT_WIDTH = 24;
    private static final int REPLY_WIDTH = 16;
    private static final int FOLLOW_UP_WIDTH = 8;

    private XiangqiAi() {
    }

    public static Move chooseMove(int[][] board, int side) {
        List<ScoredMove> moves = legalMoves(board, side, ROOT_WIDTH);
        Move best = null;
        int bestScore = Integer.MIN_VALUE;
        for (ScoredMove candidate : moves) {
            int captured = makeMove(board, candidate.move);
            int score;
            if (Math.abs(captured) == XiangqiConfig.GENERAL) {
                score = 10_000_000;
            } else {
                List<ScoredMove> replies = legalMoves(board, -side, REPLY_WIDTH);
                if (replies.isEmpty()) {
                    score = 9_000_000;
                } else {
                    int worstReply = Integer.MAX_VALUE;
                    for (ScoredMove reply : replies) {
                        int replyCaptured = makeMove(board, reply.move);
                        int replyScore = Math.abs(replyCaptured) == XiangqiConfig.GENERAL
                                ? -10_000_000 : bestFollowUp(board, side);
                        undoMove(board, reply.move, replyCaptured);
                        worstReply = Math.min(worstReply, replyScore);
                    }
                    score = worstReply + candidate.orderScore / 4;
                }
            }
            undoMove(board, candidate.move, captured);
            if (score > bestScore) {
                bestScore = score;
                best = candidate.move;
            }
        }
        return best;
    }

    private static int bestFollowUp(int[][] board, int side) {
        List<ScoredMove> moves = legalMoves(board, side, FOLLOW_UP_WIDTH);
        if (moves.isEmpty()) return -9_000_000;
        int best = Integer.MIN_VALUE;
        for (ScoredMove move : moves) {
            int captured = makeMove(board, move.move);
            int score = Math.abs(captured) == XiangqiConfig.GENERAL
                    ? 10_000_000 : evaluate(board, side) + move.orderScore / 3;
            undoMove(board, move.move, captured);
            best = Math.max(best, score);
        }
        return best;
    }

    private static List<ScoredMove> legalMoves(int[][] board, int side, int limit) {
        List<ScoredMove> moves = new ArrayList<>();
        for (int fromY = 0; fromY < XiangqiConfig.ROWS; fromY++) for (int fromX = 0; fromX < XiangqiConfig.COLS; fromX++) {
            int piece = board[fromY][fromX];
            if (XiangqiConfig.color(piece) != side) continue;
            for (int toY = 0; toY < XiangqiConfig.ROWS; toY++) for (int toX = 0; toX < XiangqiConfig.COLS; toX++) {
                if (XiangqiConfig.color(board[toY][toX]) == side || !XiangqiConfig.isLegalMove(board, fromX, fromY, toX, toY)) continue;
                Move move = new Move(fromX, fromY, toX, toY);
                int captured = makeMove(board, move);
                boolean legal = !XiangqiConfig.isInCheck(board, side);
                int score = legal ? orderScore(board, piece, captured, toX, toY, side) : Integer.MIN_VALUE;
                undoMove(board, move, captured);
                if (legal) moves.add(new ScoredMove(move, score));
            }
        }
        moves.sort(Comparator.comparingInt(ScoredMove::orderScore).reversed());
        return moves.size() > limit ? new ArrayList<>(moves.subList(0, limit)) : moves;
    }

    private static int orderScore(int[][] board, int piece, int captured, int toX, int toY, int side) {
        int score = value(Math.abs(captured)) * 12 - value(Math.abs(piece));
        if (Math.abs(captured) == XiangqiConfig.GENERAL) return 10_000_000;
        if (XiangqiConfig.isInCheck(board, -side)) score += 8_000;
        score += 20 - Math.abs(toX - 4) * 3;
        score += side == XiangqiConfig.RED ? 9 - toY : toY;
        return score;
    }

    private static int evaluate(int[][] board, int side) {
        int score = 0;
        for (int[] row : board) for (int piece : row) {
            if (piece != 0) score += XiangqiConfig.color(piece) == side ? value(Math.abs(piece)) : -value(Math.abs(piece));
        }
        if (XiangqiConfig.isInCheck(board, -side)) score += 1_200;
        if (XiangqiConfig.isInCheck(board, side)) score -= 1_500;
        return score;
    }

    private static int makeMove(int[][] board, Move move) {
        int captured = board[move.toY][move.toX];
        board[move.toY][move.toX] = board[move.fromY][move.fromX];
        board[move.fromY][move.fromX] = 0;
        return captured;
    }

    private static void undoMove(int[][] board, Move move, int captured) {
        board[move.fromY][move.fromX] = board[move.toY][move.toX];
        board[move.toY][move.toX] = captured;
    }

    private static int value(int piece) {
        return switch (piece) {
            case XiangqiConfig.ROOK -> 900;
            case XiangqiConfig.CANNON -> 475;
            case XiangqiConfig.HORSE -> 425;
            case XiangqiConfig.ELEPHANT, XiangqiConfig.ADVISOR -> 225;
            case XiangqiConfig.SOLDIER -> 120;
            case XiangqiConfig.GENERAL -> 100_000;
            default -> 0;
        };
    }

    public record Move(int fromX, int fromY, int toX, int toY) {}
    private record ScoredMove(Move move, int orderScore) {}
}
