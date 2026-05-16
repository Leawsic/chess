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
import site.leawsic.chess.config.ChessGameConfig;
import site.leawsic.chess.config.Move;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class BaseBoardBlockEntity extends BlockEntity {
    private final ChessGameConfig config;
    private final List<Move> moveHistory = new ArrayList<>();
    private int[][] board;
    private int currentPlayer;
    private boolean gameOver;
    private boolean editMode;
    
    // 联机相关字段
    private UUID hostPlayer; // 房主的UUID
    private UUID guestPlayer; // 加入的玩家UUID
    private boolean isMultiplayer; // 是否为多人对局模式
    private int hostPieceType; // 房主的棋子类型
    private int guestPieceType; // 客人的棋子类型

    public BaseBoardBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state, ChessGameConfig config) {
        super(type, pos, state);
        this.config = config;
        this.board = new int[config.getRows()][config.getCols()];
        this.currentPlayer = config.getInitialPlayer();
        this.gameOver = false;
        this.editMode = false;
        this.hostPlayer = null;
        this.guestPlayer = null;
        this.isMultiplayer = false;
        this.hostPieceType = 1;
        this.guestPieceType = 2;
    }

    public ChessGameConfig getConfig() {
        return config;
    }

    public int[][] getBoard() {
        return board;
    }

    public int getCurrentPlayer() {
        return currentPlayer;
    }

    public boolean isGameOver() {
        return gameOver;
    }

    public boolean isEditMode() {
        return editMode;
    }
    
    // 联机相关方法
    public UUID getHostPlayer() {
        return hostPlayer;
    }
    
    public UUID getGuestPlayer() {
        return guestPlayer;
    }
    
    public boolean isMultiplayer() {
        return isMultiplayer;
    }
    
    public int getHostPieceType() {
        return hostPieceType;
    }
    
    public int getGuestPieceType() {
        return guestPieceType;
    }
    
    /**
     * 检查玩家是否是房主
     */
    public boolean isHost(UUID playerUuid) {
        return hostPlayer != null && hostPlayer.equals(playerUuid);
    }
    
    /**
     * 检查玩家是否是对局中的玩家
     */
    public boolean isInGame(UUID playerUuid) {
        return isHost(playerUuid) || (guestPlayer != null && guestPlayer.equals(playerUuid));
    }
    
    /**
     * 设置房主（第一个交互的玩家）
     */
    public void setHost(UUID playerUuid) {
        if (hostPlayer == null) {
            this.hostPlayer = playerUuid;
            this.isMultiplayer = false;
            this.editMode = false;
            markDirtyAndSync();
        }
    }
    
    /**
     * 客人加入对局
     */
    public void joinGame(UUID playerUuid) {
        if (isInGame(playerUuid)) return;

        if (hostPlayer == null) {
            setHost(playerUuid);
            return;
        }

        if (guestPlayer == null) {
            this.guestPlayer = playerUuid;
            this.isMultiplayer = true;
            this.editMode = false; // 多人模式下禁止编辑
            resetBoard();
            markDirtyAndSync();
        }
    }
    
    /**
     * 设置先后手（由房主决定）
     */
    public boolean setPieceTypes(int hostType, int guestType, UUID playerUuid) {
        if (!isMultiplayer || !isHost(playerUuid)) return false;
        if (!isValidPiecePair(hostType, guestType)) return false;

        this.hostPieceType = hostType;
        this.guestPieceType = guestType;
        // 切换先后手会改变棋子归属，必须重开一盘以避免旧棋子含义变化。
        resetBoard();
        markDirtyAndSync();
        return true;
    }
    
    /**
     * 玩家退出对局
     */
    public void leaveGame(UUID playerUuid) {
        if (isHost(playerUuid)) {
            if (guestPlayer != null) {
                // 保留对局占位，避免观战者用旧同步状态加入到一个不存在的房主名下。
                this.hostPlayer = this.guestPlayer;
                this.hostPieceType = this.guestPieceType;
                this.guestPieceType = nextPlayer(this.hostPieceType);
            } else {
                this.hostPlayer = null;
                this.hostPieceType = 1;
                this.guestPieceType = 2;
            }
            this.guestPlayer = null;
            this.isMultiplayer = false;
            this.editMode = false;
            this.gameOver = false;
        } else if (guestPlayer != null && guestPlayer.equals(playerUuid)) {
            this.guestPlayer = null;
            this.isMultiplayer = false;
            this.editMode = false;
            this.gameOver = false;
        } else {
            return;
        }
        markDirtyAndSync();
    }
    
    /**
     * 获取玩家的棋子类型
     */
    public int getPlayerPieceType(UUID playerUuid) {
        if (isHost(playerUuid)) {
            return hostPieceType;
        } else if (guestPlayer != null && guestPlayer.equals(playerUuid)) {
            return guestPieceType;
        }
        return 0; // 不是对局中的玩家
    }

    public List<Move> getMoveHistory() {
        return moveHistory;
    }

    public boolean placePiece(int x, int y, int player, UUID playerUuid) {
        if (x < 0 || x >= config.getCols() || y < 0 || y >= config.getRows()) return false;
        if (board[y][x] != config.getEmptyValue()) return false;
        
        // 游戏结束后不允许下棋
        if (gameOver) return false;
        
        // 多人模式下验证玩家权限
        if (isMultiplayer) {
            // 必须是対局中的玩家
            if (!isInGame(playerUuid)) return false;
            // 必须轮到该玩家下棋
            int playerPieceType = getPlayerPieceType(playerUuid);
            if (playerPieceType != currentPlayer) return false;
            // 使用玩家的棋子类型
            player = playerPieceType;
        } else {
            // 单人模式下，如果不是房主则不允许
            if (hostPlayer != null && !isHost(playerUuid)) return false;
            // 单人模式下可以编辑或使用当前玩家
            if (!editMode && player != currentPlayer) return false;
        }

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

    public boolean clearBoard(UUID playerUuid) {
        if (!canClearBoard(playerUuid)) return false;
        resetBoard();
        markDirtyAndSync();
        return true;
    }

    public boolean toggleEditMode(UUID playerUuid) {
        if (!canToggleEditMode(playerUuid)) return false;
        editMode = !editMode;
        markDirtyAndSync();
        return true;
    }

    public void clearEditMode(UUID playerUuid) {
        if (!isHost(playerUuid) || !editMode) return;
        editMode = false;
        markDirtyAndSync();
    }

    private void resetBoard() {
        board = new int[config.getRows()][config.getCols()];
        moveHistory.clear();
        gameOver = false;
        currentPlayer = config.getInitialPlayer();
    }

    private boolean canClearBoard(UUID playerUuid) {
        if (!isInGame(playerUuid)) return false;
        return !isMultiplayer || isHost(playerUuid);
    }

    private boolean canToggleEditMode(UUID playerUuid) {
        return !isMultiplayer && isHost(playerUuid) && !gameOver;
    }

    private boolean isValidPiecePair(int hostType, int guestType) {
        return hostType >= 1 && hostType <= config.getPlayerCount()
                && guestType >= 1 && guestType <= config.getPlayerCount()
                && hostType != guestType;
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
        
        // 保存联机状态
        if (hostPlayer != null) {
            nbt.putUuid("HostPlayer", hostPlayer);
        }
        if (guestPlayer != null) {
            nbt.putUuid("GuestPlayer", guestPlayer);
        }
        nbt.putBoolean("IsMultiplayer", isMultiplayer);
        nbt.putInt("HostPieceType", hostPieceType);
        nbt.putInt("GuestPieceType", guestPieceType);
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
        
        // 读取联机状态
        if (nbt.containsUuid("HostPlayer")) {
            hostPlayer = nbt.getUuid("HostPlayer");
        } else {
            hostPlayer = null;
        }
        if (nbt.containsUuid("GuestPlayer")) {
            guestPlayer = nbt.getUuid("GuestPlayer");
        } else {
            guestPlayer = null;
        }
        isMultiplayer = nbt.getBoolean("IsMultiplayer");
        hostPieceType = nbt.getInt("HostPieceType");
        guestPieceType = nbt.getInt("GuestPieceType");
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