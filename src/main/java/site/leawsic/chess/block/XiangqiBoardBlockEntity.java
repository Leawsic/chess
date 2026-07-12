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
    private UUID guestPlayer;
    private boolean multiplayer;
    private int hostPieceType = XiangqiConfig.RED;
    private int guestPieceType = XiangqiConfig.BLACK;

    public XiangqiBoardBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
        resetBoard();
    }

    public int[][] getBoard() { return board; }
    public int getCurrentPlayer() { return currentPlayer; }
    public boolean isGameOver() { return gameOver; }
    public int getWinner() { return winner; }
    public UUID getHostPlayer() { return hostPlayer; }
    public UUID getGuestPlayer() { return guestPlayer; }
    public boolean isMultiplayer() { return multiplayer; }
    public boolean isGameStarted() { return guestPlayer != null; }
    public int getHostPieceType() { return hostPieceType; }
    public int getGuestPieceType() { return guestPieceType; }
    public boolean isHost(UUID playerUuid) { return hostPlayer != null && hostPlayer.equals(playerUuid); }
    public boolean isInGame(UUID playerUuid) { return isHost(playerUuid) || guestPlayer != null && guestPlayer.equals(playerUuid); }
    public int getPlayerPieceType(UUID playerUuid) {
        if (isHost(playerUuid)) return hostPieceType;
        if (guestPlayer != null && guestPlayer.equals(playerUuid)) return guestPieceType;
        return 0;
    }

    public void setHost(UUID playerUuid) {
        if (hostPlayer == null) {
            clearSession();
            hostPlayer = playerUuid;
            sync();
        }
    }

    public String tryMove(int fromX, int fromY, int toX, int toY, UUID playerUuid) {
        if (gameOver) return "gui.chess.xq.game_over";
        if (multiplayer) {
            if (!isInGame(playerUuid)) return "gui.chess.xq.not_player";
            if (getPlayerPieceType(playerUuid) != currentPlayer) return "gui.chess.xq.not_your_turn";
        } else if (hostPlayer == null || !hostPlayer.equals(playerUuid)) {
            return "gui.chess.xq.not_host";
        }
        if (!XiangqiConfig.inBounds(fromX, fromY) || !XiangqiConfig.inBounds(toX, toY)) return "gui.chess.xq.invalid_position";
        int piece = board[fromY][fromX];
        if (piece == 0) return "gui.chess.xq.empty_position";
        if (XiangqiConfig.color(piece) != currentPlayer) return "gui.chess.xq.not_your_turn";
        if (XiangqiConfig.color(board[toY][toX]) == currentPlayer) return "gui.chess.xq.own_piece";
        if (!XiangqiConfig.isLegalMove(board, fromX, fromY, toX, toY) ) return XiangqiConfig.moveRuleKey(piece);

        int captured = board[toY][toX];
        board[toY][toX] = piece;
        board[fromY][fromX] = 0;
        if (Math.abs(captured) == XiangqiConfig.GENERAL) {
            gameOver = true;
            winner = currentPlayer;
        } else if (XiangqiConfig.isInCheck(board, currentPlayer)) {
            gameOver = true;
            winner = -currentPlayer;
        } else {
            currentPlayer = -currentPlayer;
            if (XiangqiConfig.isInCheck(board, currentPlayer) && !XiangqiConfig.hasLegalResponse(board, currentPlayer)) {
                gameOver = true;
                winner = -currentPlayer;
            }
        }
        sync();
        return null;
    }

    public String joinGame(UUID playerUuid) {
        if (isInGame(playerUuid)) return null;
        if (hostPlayer == null) {
            setHost(playerUuid);
            return null;
        }
        if (guestPlayer != null) return "gui.chess.xq.game_full";
        guestPlayer = playerUuid;
        multiplayer = true;
        resetBoard(XiangqiConfig.RED);
        sync();
        return null;
    }

    public String leaveGame(UUID playerUuid) {
        if (isHost(playerUuid)) {
            if (guestPlayer != null) {
                hostPlayer = guestPlayer;
                hostPieceType = guestPieceType;
                guestPieceType = -hostPieceType;
                guestPlayer = null;
                multiplayer = false;
            } else {
                clearSession();
            }
        } else if (guestPlayer != null && guestPlayer.equals(playerUuid)) {
            guestPlayer = null;
            multiplayer = false;
        } else {
            return "gui.chess.xq.not_player";
        }
        sync();
        return null;
    }

    public String setPieceTypes(int hostType, int guestType, UUID playerUuid) {
        if (!isHost(playerUuid)) return "gui.chess.xq.host_only";
        if (hostType == guestType || (hostType != XiangqiConfig.RED && hostType != XiangqiConfig.BLACK)
                || (guestType != XiangqiConfig.RED && guestType != XiangqiConfig.BLACK)) {
            return "gui.chess.xq.invalid_colors";
        }
        hostPieceType = hostType;
        guestPieceType = guestType;
        resetBoard(isGameStarted() ? XiangqiConfig.RED : hostPieceType);
        sync();
        return null;
    }

    public boolean resetBoard(UUID playerUuid) {
        if (hostPlayer == null || !hostPlayer.equals(playerUuid)) return false;
        resetBoard();
        sync();
        return true;
    }

    private void resetBoard() {
        resetBoard(isGameStarted() ? XiangqiConfig.RED : hostPieceType);
    }

    private void resetBoard(int firstPlayer) {
        board = XiangqiConfig.createInitialBoard();
        currentPlayer = firstPlayer;
        gameOver = false;
        winner = 0;
    }

    private void clearSession() {
        hostPlayer = null;
        guestPlayer = null;
        multiplayer = false;
        hostPieceType = XiangqiConfig.RED;
        guestPieceType = XiangqiConfig.BLACK;
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
        if (guestPlayer != null) nbt.putUuid("GuestPlayer", guestPlayer);
        nbt.putBoolean("Multiplayer", multiplayer);
        nbt.putInt("HostPieceType", hostPieceType);
        nbt.putInt("GuestPieceType", guestPieceType);
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
        guestPlayer = nbt.containsUuid("GuestPlayer") ? nbt.getUuid("GuestPlayer") : null;
        multiplayer = nbt.getBoolean("Multiplayer");
        hostPieceType = nbt.contains("HostPieceType") ? nbt.getInt("HostPieceType") : XiangqiConfig.RED;
        guestPieceType = nbt.contains("GuestPieceType") ? nbt.getInt("GuestPieceType") : XiangqiConfig.BLACK;
    }

    @Nullable @Override public Packet<ClientPlayPacketListener> toUpdatePacket() { return BlockEntityUpdateS2CPacket.create(this); }
    @Override public NbtCompound toInitialChunkDataNbt() { return createNbt(); }
}
