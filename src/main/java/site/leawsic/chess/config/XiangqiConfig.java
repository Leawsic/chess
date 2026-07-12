package site.leawsic.chess.config;

import net.minecraft.util.Identifier;
import site.leawsic.chess.Chess;

public final class XiangqiConfig {
    private XiangqiConfig() {}

    public static final int COLS = 9;
    public static final int ROWS = 10;
    public static final int RED = 1;
    public static final int BLACK = -1;
    public static final int GENERAL = 1;
    public static final int ADVISOR = 2;
    public static final int ELEPHANT = 3;
    public static final int HORSE = 4;
    public static final int ROOK = 5;
    public static final int CANNON = 6;
    public static final int SOLDIER = 7;

    public static final int BOARD_TEXTURE_SIZE = 512;
    public static final int BOARD_LEFT_U = 56;
    public static final int BOARD_TOP_V = 31;
    public static final int CELL_PIXELS = 50;
    public static final int PIECE_PIXELS = 40;
    public static final Identifier BOARD_TEXTURE = Chess.id("block/xq_board_top");

    public static int[][] createInitialBoard() {
        int[][] board = new int[ROWS][COLS];
        int[] backRank = {ROOK, HORSE, ELEPHANT, ADVISOR, GENERAL, ADVISOR, ELEPHANT, HORSE, ROOK};
        for (int x = 0; x < COLS; x++) {
            board[0][x] = -backRank[x];
            board[ROWS - 1][x] = backRank[x];
        }
        board[2][1] = board[2][7] = -CANNON;
        board[7][1] = board[7][7] = CANNON;
        for (int x = 0; x < COLS; x += 2) {
            board[3][x] = -SOLDIER;
            board[6][x] = SOLDIER;
        }
        return board;
    }

    public static int textureX(int col) { return BOARD_LEFT_U + col * CELL_PIXELS; }
    // 10 条横线由 9 个格距组成，楚河汉界就是其中的中间一格，不额外增加间距。
    public static int textureY(int row) { return BOARD_TOP_V + row * CELL_PIXELS; }
    public static Identifier pieceTexture(int piece) { return Chess.id("xq_pieces/" + textureName(piece)); }
    public static String textureName(int piece) {
        String color = piece > 0 ? "hong_" : "hei_";
        return color + switch (Math.abs(piece)) {
            case GENERAL -> "jiang";
            case ADVISOR -> "shi";
            case ELEPHANT -> "xiang";
            case HORSE -> "ma";
            case ROOK -> "ju";
            case CANNON -> "pao";
            default -> "zu";
        };
    }

    public static boolean isLegalMove(int[][] board, int fromX, int fromY, int toX, int toY) {
        int piece = board[fromY][fromX];
        int type = Math.abs(piece);
        int side = color(piece);
        int dx = toX - fromX, dy = toY - fromY;
        int adx = Math.abs(dx), ady = Math.abs(dy);
        return switch (type) {
            case GENERAL -> (adx + ady == 1 && inPalace(toX, toY, side)) || (dx == 0 && Math.abs(board[toY][toX]) == GENERAL && clearPath(board, fromX, fromY, toX, toY, 0));
            case ADVISOR -> adx == 1 && ady == 1 && inPalace(toX, toY, side);
            case ELEPHANT -> adx == 2 && ady == 2 && !crossedRiver(toY, side) && board[fromY + dy / 2][fromX + dx / 2] == 0;
            case HORSE -> (adx == 2 && ady == 1 && board[fromY][fromX + dx / 2] == 0) || (adx == 1 && ady == 2 && board[fromY + dy / 2][fromX] == 0);
            case ROOK -> (dx == 0 || dy == 0) && clearPath(board, fromX, fromY, toX, toY, 0);
            case CANNON -> (dx == 0 || dy == 0) && clearPath(board, fromX, fromY, toX, toY, board[toY][toX] == 0 ? 0 : 1);
            case SOLDIER -> (dy == (side == RED ? -1 : 1) && dx == 0) || (crossedRiver(fromY, side) && dy == 0 && adx == 1);
            default -> false;
        };
    }

    public static String moveRuleKey(int piece) {
        return switch (Math.abs(piece)) {
            case GENERAL -> "gui.chess.xq.rule.general";
            case ADVISOR -> "gui.chess.xq.rule.advisor";
            case ELEPHANT -> "gui.chess.xq.rule.elephant";
            case HORSE -> "gui.chess.xq.rule.horse";
            case ROOK -> "gui.chess.xq.rule.rook";
            case CANNON -> "gui.chess.xq.rule.cannon";
            case SOLDIER -> "gui.chess.xq.rule.soldier";
            default -> "gui.chess.xq.invalid_move";
        };
    }

    public static boolean isInCheck(int[][] board, int side) {
        int generalX = -1, generalY = -1;
        for (int y = 0; y < ROWS; y++) for (int x = 0; x < COLS; x++) {
            if (board[y][x] == side * GENERAL) { generalX = x; generalY = y; break; }
        }
        if (generalX < 0) return true;
        for (int y = 0; y < ROWS; y++) for (int x = 0; x < COLS; x++) {
            if (color(board[y][x]) == -side && isLegalMove(board, x, y, generalX, generalY)) return true;
        }
        return false;
    }

    public static boolean hasLegalResponse(int[][] board, int side) {
        for (int fromY = 0; fromY < ROWS; fromY++) {
            for (int fromX = 0; fromX < COLS; fromX++) {
                if (color(board[fromY][fromX]) != side) continue;
                for (int toY = 0; toY < ROWS; toY++) {
                    for (int toX = 0; toX < COLS; toX++) {
                        if (color(board[toY][toX]) == side || !isLegalMove(board, fromX, fromY, toX, toY)) continue;
                        int captured = board[toY][toX];
                        board[toY][toX] = board[fromY][fromX];
                        board[fromY][fromX] = 0;
                        boolean safe = !isInCheck(board, side);
                        board[fromY][fromX] = board[toY][toX];
                        board[toY][toX] = captured;
                        if (safe) return true;
                    }
                }
            }
        }
        return false;
    }

    public static boolean inBounds(int x, int y) { return x >= 0 && x < COLS && y >= 0 && y < ROWS; }
    public static int color(int piece) { return Integer.compare(piece, 0); }
    private static boolean inPalace(int x, int y, int side) { return x >= 3 && x <= 5 && (side == RED ? y >= 7 && y <= 9 : y <= 2); }
    private static boolean crossedRiver(int y, int side) { return side == RED ? y <= 4 : y >= 5; }
    private static boolean clearPath(int[][] board, int fromX, int fromY, int toX, int toY, int expectedPieces) {
        int stepX = Integer.compare(toX, fromX), stepY = Integer.compare(toY, fromY), pieces = 0;
        for (int x = fromX + stepX, y = fromY + stepY; x != toX || y != toY; x += stepX, y += stepY) if (board[y][x] != 0) pieces++;
        return pieces == expectedPieces;
    }
}
