package site.leawsic.chess.block;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;
import site.leawsic.chess.config.ChessGameConfig;
import site.leawsic.chess.config.Move;

import java.util.ArrayList;
import java.util.List;

public abstract class BaseBoardBlockEntity extends BlockEntity {
    private final ChessGameConfig config;
    private int[][] board;
    private int currentPlayer;
    private boolean gameOver;
    private boolean editMode;
    private final List<Move> moveHistory = new ArrayList<>();

    public BaseBoardBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state, ChessGameConfig config) {
        super(type, pos, state);
        this.config = config;
        this.board = new int[config.getRows()][config.getCols()];
        this.currentPlayer = config.getInitialPlayer();
        this.gameOver = false;
        this.editMode = false;
    }

    public ChessGameConfig getConfig() { return config; }
    public int[][] getBoard() { return board; }
    public int getCurrentPlayer() { return currentPlayer; }
    public boolean isGameOver() { return gameOver; }
    public boolean isEditMode() { return editMode; }
    public List<Move> getMoveHistory() { return moveHistory; }

    public boolean placePiece(int x, int y, int player) {
        if (x < 0 || x >= config.getCols() || y < 0 || y >= config.getRows()) return false;
        if (board[y][x] != config.getEmptyValue()) return false;
        if (!editMode && player != currentPlayer) return false;

        ChessGameConfig.PlaceResult result = config.checkPlacement(this, new Move(x, y, player));
        if (!result.success()) return false;

        board[y][x] = player;
        moveHistory.add(new Move(x, y, player));

        if (result.gameOver()) {
            gameOver = true;
        } else if (result.switchPlayer() && !editMode) {
            currentPlayer = nextPlayer(currentPlayer);
        }
        markDirtyAndSync();
        return true;
    }

    private int nextPlayer(int current) {
        int next = current + 1;
        if (next > config.getPlayerCount()) next = 1;
        return next;
    }

    public void clearBoard() {
        board = new int[config.getRows()][config.getCols()];
        moveHistory.clear();
        gameOver = false;
        currentPlayer = config.getInitialPlayer();
        markDirtyAndSync();
    }

    public boolean undoMove() {
        if (moveHistory.isEmpty()) return false;
        Move last = moveHistory.remove(moveHistory.size() - 1);
        board[last.y()][last.x()] = config.getEmptyValue();
        gameOver = false;
        currentPlayer = last.player();
        markDirtyAndSync();
        return true;
    }

    public void toggleEditMode() {
        editMode = !editMode;
        markDirtyAndSync();
    }

    protected void markDirtyAndSync() {
        markDirty();
        if (world != null) {
            world.updateListeners(pos, getCachedState(), getCachedState(), 3);
        }
    }

    @Override
    protected void writeNbt(NbtCompound nbt) {
        super.writeNbt(nbt);
        NbtList boardList = new NbtList();
        for (int y = 0; y < config.getRows(); y++) {
            NbtCompound row = new NbtCompound();
            row.putIntArray("row", board[y]);
            boardList.add(row);
        }
        nbt.put("Board", boardList);
        nbt.putInt("CurrentPlayer", currentPlayer);
        nbt.putBoolean("GameOver", gameOver);
        nbt.putBoolean("EditMode", editMode);
    }

    @Override
    public void readNbt(NbtCompound nbt) {
        super.readNbt(nbt);
        NbtList boardList = nbt.getList("Board", 10);
        board = new int[config.getRows()][config.getCols()];
        for (int y = 0; y < boardList.size(); y++) {
            int[] row = boardList.getCompound(y).getIntArray("row");
            System.arraycopy(row, 0, board[y], 0, Math.min(row.length, config.getCols()));
        }
        currentPlayer = nbt.getInt("CurrentPlayer");
        gameOver = nbt.getBoolean("GameOver");
        editMode = nbt.getBoolean("EditMode");
    }

    @Nullable
    @Override
    public Packet<ClientPlayPacketListener> toUpdatePacket() {
        return BlockEntityUpdateS2CPacket.create(this);
    }

    @Override
    public NbtCompound toInitialChunkDataNbt() {
        return createNbt();
    }
}