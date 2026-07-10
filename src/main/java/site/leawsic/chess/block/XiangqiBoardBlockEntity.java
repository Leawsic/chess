package site.leawsic.chess.block;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class XiangqiBoardBlockEntity extends BlockEntity {
    public static final int RED = 1;
    public static final int BLACK = -1;
    public static final int GENERAL = 1;
    public static final int ADVISOR = 2;
    public static final int ELEPHANT = 3;
    public static final int HORSE = 4;
    public static final int ROOK = 5;
    public static final int CANNON = 6;
    public static final int SOLDIER = 7;
    private int[][] board = new int[10][9];
    private int currentPlayer = RED;
    private boolean gameOver;
    private int winner;
    private UUID hostPlayer;

    public XiangqiBoardBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
        resetBoard();
    }

    public int[][] getBoard() { return board; }
    public int getCurrentPlayer() { return currentPlayer; }
    public boolean isGameOver() { return gameOver; }
    public int getWinner() { return winner; }
    public UUID getHostPlayer() { return hostPlayer; }

    public void setHost(UUID playerUuid) {
        if (hostPlayer == null) {
            hostPlayer = playerUuid;
            sync();
        }
    }

    public boolean tryMove(int fromX, int fromY, int toX, int toY, UUID playerUuid) {
        if (gameOver || hostPlayer == null || !hostPlayer.equals(playerUuid)) return false;
        if (!inBounds(fromX, fromY) || !inBounds(toX, toY)) return false;
        int piece = board[fromY][fromX];
        if (piece == 0 || color(piece) != currentPlayer || color(board[toY][toX]) == currentPlayer) return false;
        if (!canMove(board, fromX, fromY, toX, toY)) return false;

        int captured = board[toY][toX];
        board[toY][toX] = piece;
        board[fromY][fromX] = 0;
        if (isInCheck(board, currentPlayer)) {
            board[fromY][fromX] = piece;
            board[toY][toX] = captured;
            return false;
        }

        if (Math.abs(captured) == GENERAL) {
            gameOver = true;
            winner = currentPlayer;
        } else {
            currentPlayer = -currentPlayer;
        }
        sync();
        return true;
    }

    public boolean resetBoard(UUID playerUuid) {
        if (hostPlayer == null || !hostPlayer.equals(playerUuid)) return false;
        resetBoard();
        sync();
        return true;
    }

    private void resetBoard() {
        board = new int[10][9];
        int[] back = {ROOK, HORSE, ELEPHANT, ADVISOR, GENERAL, ADVISOR, ELEPHANT, HORSE, ROOK};
        for (int x = 0; x < 9; x++) {
            board[0][x] = -back[x];
            board[9][x] = back[x];
        }
        board[2][1] = -CANNON;
        board[2][7] = -CANNON;
        board[7][1] = CANNON;
        board[7][7] = CANNON;
        for (int x = 0; x < 9; x += 2) {
            board[3][x] = -SOLDIER;
            board[6][x] = SOLDIER;
        }
        currentPlayer = RED;
        gameOver = false;
        winner = 0;
    }

    private static boolean canMove(int[][] state, int fromX, int fromY, int toX, int toY) {
        int piece = state[fromY][fromX];
        int type = Math.abs(piece);
        int side = color(piece);
        int dx = toX - fromX;
        int dy = toY - fromY;
        int adx = Math.abs(dx);
        int ady = Math.abs(dy);
        return switch (type) {
            case GENERAL -> (adx + ady == 1 && inPalace(toX, toY, side)) || (dx == 0 && Math.abs(state[toY][toX]) == GENERAL && clearPath(state, fromX, fromY, toX, toY, 0));
            case ADVISOR -> adx == 1 && ady == 1 && inPalace(toX, toY, side);
            case ELEPHANT -> adx == 2 && ady == 2 && !crossedRiver(toY, side) && state[fromY + dy / 2][fromX + dx / 2] == 0;
            case HORSE -> (adx == 2 && ady == 1 && state[fromY][fromX + dx / 2] == 0) || (adx == 1 && ady == 2 && state[fromY + dy / 2][fromX] == 0);
            case ROOK -> (dx == 0 || dy == 0) && clearPath(state, fromX, fromY, toX, toY, 0);
            case CANNON -> (dx == 0 || dy == 0) && clearPath(state, fromX, fromY, toX, toY, state[toY][toX] == 0 ? 0 : 1);
            case SOLDIER -> soldierMove(dx, dy, fromY, side);
            default -> false;
        };
    }

    private static boolean soldierMove(int dx, int dy, int fromY, int side) {
        int forward = side == RED ? -1 : 1;
        return dy == forward && dx == 0 || crossedRiver(fromY, side) && dy == 0 && Math.abs(dx) == 1;
    }

    private static boolean isInCheck(int[][] state, int side) {
        int generalX = -1;
        int generalY = -1;
        for (int y = 0; y < 10; y++) for (int x = 0; x < 9; x++) {
            if (state[y][x] == side * GENERAL) { generalX = x; generalY = y; break; }
        }
        if (generalX < 0) return true;
        for (int y = 0; y < 10; y++) for (int x = 0; x < 9; x++) {
            if (color(state[y][x]) == -side && canMove(state, x, y, generalX, generalY)) return true;
        }
        return false;
    }

    private static boolean clearPath(int[][] state, int fromX, int fromY, int toX, int toY, int expectedPieces) {
        int stepX = Integer.compare(toX, fromX);
        int stepY = Integer.compare(toY, fromY);
        int pieces = 0;
        for (int x = fromX + stepX, y = fromY + stepY; x != toX || y != toY; x += stepX, y += stepY) {
            if (state[y][x] != 0) pieces++;
        }
        return pieces == expectedPieces;
    }

    private static boolean inPalace(int x, int y, int side) { return x >= 3 && x <= 5 && (side == RED ? y >= 7 && y <= 9 : y >= 0 && y <= 2); }
    private static boolean crossedRiver(int y, int side) { return side == RED ? y <= 4 : y >= 5; }
    private static boolean inBounds(int x, int y) { return x >= 0 && x < 9 && y >= 0 && y < 10; }
    private static int color(int piece) { return Integer.compare(piece, 0); }

    private void sync() { markDirty(); if (world != null) world.updateListeners(pos, getCachedState(), getCachedState(), 3); }

    @Override protected void writeNbt(NbtCompound nbt) {
        super.writeNbt(nbt);
        NbtList rows = new NbtList();
        for (int[] row : board) { NbtCompound tag = new NbtCompound(); tag.putIntArray("row", row); rows.add(tag); }
        nbt.put("Board", rows);
        nbt.putInt("CurrentPlayer", currentPlayer);
        nbt.putBoolean("GameOver", gameOver);
        nbt.putInt("Winner", winner);
        if (hostPlayer != null) nbt.putUuid("HostPlayer", hostPlayer);
    }

    @Override public void readNbt(NbtCompound nbt) {
        super.readNbt(nbt);
        NbtList rows = nbt.getList("Board", 10);
        if (!rows.isEmpty()) {
            board = new int[10][9];
            for (int y = 0; y < Math.min(10, rows.size()); y++) System.arraycopy(rows.getCompound(y).getIntArray("row"), 0, board[y], 0, Math.min(9, rows.getCompound(y).getIntArray("row").length));
        }
        currentPlayer = nbt.contains("CurrentPlayer") ? nbt.getInt("CurrentPlayer") : RED;
        gameOver = nbt.getBoolean("GameOver");
        winner = nbt.getInt("Winner");
        hostPlayer = nbt.containsUuid("HostPlayer") ? nbt.getUuid("HostPlayer") : null;
    }

    @Nullable @Override public Packet<ClientPlayPacketListener> toUpdatePacket() { return BlockEntityUpdateS2CPacket.create(this); }
    @Override public NbtCompound toInitialChunkDataNbt() { return createNbt(); }
}
