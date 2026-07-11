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
import site.leawsic.chess.config.XiangqiConfig;

import java.util.UUID;

public class XiangqiBoardBlockEntity extends BlockEntity {
    private int[][] board = XiangqiConfig.createInitialBoard();
    private int currentPlayer = XiangqiConfig.RED;
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
        if (!XiangqiConfig.inBounds(fromX, fromY) || !XiangqiConfig.inBounds(toX, toY)) return false;
        int piece = board[fromY][fromX];
        if (piece == 0 || XiangqiConfig.color(piece) != currentPlayer || XiangqiConfig.color(board[toY][toX]) == currentPlayer) return false;
        if (!XiangqiConfig.isLegalMove(board, fromX, fromY, toX, toY)) return false;

        int captured = board[toY][toX];
        board[toY][toX] = piece;
        board[fromY][fromX] = 0;
        if (XiangqiConfig.isInCheck(board, currentPlayer)) {
            board[fromY][fromX] = piece;
            board[toY][toX] = captured;
            return false;
        }

        if (Math.abs(captured) == XiangqiConfig.GENERAL) {
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
        board = XiangqiConfig.createInitialBoard();
        currentPlayer = XiangqiConfig.RED;
        gameOver = false;
        winner = 0;
    }


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
            board = new int[XiangqiConfig.ROWS][XiangqiConfig.COLS];
            for (int y = 0; y < Math.min(XiangqiConfig.ROWS, rows.size()); y++) System.arraycopy(rows.getCompound(y).getIntArray("row"), 0, board[y], 0, Math.min(XiangqiConfig.COLS, rows.getCompound(y).getIntArray("row").length));
        }
        currentPlayer = nbt.contains("CurrentPlayer") ? nbt.getInt("CurrentPlayer") : XiangqiConfig.RED;
        gameOver = nbt.getBoolean("GameOver");
        winner = nbt.getInt("Winner");
        hostPlayer = nbt.containsUuid("HostPlayer") ? nbt.getUuid("HostPlayer") : null;
    }

    @Nullable @Override public Packet<ClientPlayPacketListener> toUpdatePacket() { return BlockEntityUpdateS2CPacket.create(this); }
    @Override public NbtCompound toInitialChunkDataNbt() { return createNbt(); }
}
