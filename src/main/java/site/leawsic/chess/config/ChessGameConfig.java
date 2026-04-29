package site.leawsic.chess.config;

import net.minecraft.util.Identifier;
import site.leawsic.chess.block.BaseBoardBlockEntity;

import java.util.List;
import java.util.function.BiFunction;

public class ChessGameConfig {
    private final int rows;
    private final int cols;
    private final int playerCount;          // 实际玩家数（不含空位）
    private final int initialPlayer;        // 初始执子方
    private final List<PieceType> pieceTypes; // 棋子类型定义，索引 0 为 EMPTY
    private final Identifier[] pieceTextures; // 棋子纹理，长度与 pieceTypes 一致，索引 0 可为 null
    private final Identifier boardTopTexture;
    private final Identifier boardBottomTexture;
    private final Identifier boardSideTexture;
    private final String translationKey;    // 用于方块名称和 GUI 标题
    private final List<int[]> starPoints;   // 星位坐标 {col, row}（可选）
    // 落子规则：返回 PlaceResult（是否成功、是否切换玩家、游戏是否结束）
    private final BiFunction<BaseBoardBlockEntity, Move, PlaceResult> placeRule;

    private final int boardTextureWidth;   // 纹理总宽度（像素）
    private final int boardTextureHeight;  // 纹理总高度
    private final int boardLeftU;          // 棋盘区域在纹理中的左边界
    private final int boardTopV;           // 棋盘区域在纹理中的上边界
    private final int boardCellPixelSize;
    private final int pieceTextureSize;

    private ChessGameConfig(Builder builder) {
        this.rows = builder.rows;
        this.cols = builder.cols;
        this.playerCount = builder.playerCount;
        this.initialPlayer = builder.initialPlayer;
        this.pieceTypes = List.of(builder.pieceTypes);
        this.pieceTextures = builder.pieceTextures.clone();
        this.boardTopTexture = builder.boardTopTexture;
        this.boardBottomTexture = builder.boardBottomTexture;
        this.boardSideTexture = builder.boardSideTexture;
        this.translationKey = builder.translationKey;
        this.starPoints = builder.starPoints != null ? List.copyOf(builder.starPoints) : List.of();
        this.placeRule = builder.placeRule;
        this.boardTextureWidth = builder.boardTextureWidth;
        this.boardTextureHeight = builder.boardTextureHeight;
        this.boardLeftU = builder.boardLeftU;
        this.boardTopV = builder.boardTopV;
        this.boardCellPixelSize = builder.boardCellPixelSize;
        this.pieceTextureSize = builder.pieceTextureSize;
    }

    public int getBoardTextureWidth() {
        return boardTextureWidth;
    }

    public int getBoardTextureHeight() {
        return boardTextureHeight;
    }

    public int getBoardLeftU() {
        return boardLeftU;
    }

    public int getBoardTopV() {
        return boardTopV;
    }

    public int getBoardCellPixelSize() {
        return boardCellPixelSize;
    }

    public int getRows() {
        return rows;
    }

    public int getCols() {
        return cols;
    }

    public int getPlayerCount() {
        return playerCount;
    }

    public int getInitialPlayer() {
        return initialPlayer;
    }

    public int getEmptyValue() {
        return 0;
    }

    public PieceType getPieceType(int id) {
        return pieceTypes.get(id);
    }

    public Identifier getPieceTexture(int id) {
        return pieceTextures[id];
    }

    public Identifier getBoardTopTexture() {
        return boardTopTexture;
    }

    public Identifier getBoardBottomTexture() {
        return boardBottomTexture;
    }

    public Identifier getBoardSideTexture() {
        return boardSideTexture;
    }

    public String getTranslationKey() {
        return translationKey;
    }

    public List<int[]> getStarPoints() {
        return starPoints;
    }

    /**
     * 调用规则函数处理落子。
     *
     * @param boardEntity 当前的方块实体，可读取棋盘状态
     * @param move        落子信息
     * @return PlaceResult 操作结果
     */
    public PlaceResult checkPlacement(BaseBoardBlockEntity boardEntity, Move move) {
        return placeRule.apply(boardEntity, move);
    }

    public int getPieceTextureSize() {
        return pieceTextureSize;
    }

    // 棋子类型定义，可扩展（如名称、颜色等），目前仅存储ID和显示名
    public record PieceType(int id, String name) {
        public static final PieceType EMPTY = new PieceType(0, "empty");
    }

    // 落子结果
    public record PlaceResult(boolean success, boolean switchPlayer, boolean gameOver, int winner) {
        public static PlaceResult success(boolean switchPlayer) {
            return new PlaceResult(true, switchPlayer, false, -1);
        }

        public static PlaceResult gameOver(int winner) {
            return new PlaceResult(true, false, true, winner);
        }

        public static PlaceResult fail() {
            return new PlaceResult(false, false, false, -1);
        }
    }

    public static class Builder {
        private int rows = 15;
        private int cols = 15;
        private int playerCount = 2;
        private int initialPlayer = 1; // 黑先
        private PieceType[] pieceTypes = new PieceType[]{PieceType.EMPTY, new PieceType(1, "black"), new PieceType(2, "white")};
        private Identifier[] pieceTextures;
        private Identifier boardTopTexture;
        private Identifier boardBottomTexture;
        private Identifier boardSideTexture;
        private String translationKey = "board.generic";
        private List<int[]> starPoints = List.of();
        private BiFunction<BaseBoardBlockEntity, Move, PlaceResult> placeRule;

        private int boardTextureWidth = 256;   // 纹理总宽度（像素）
        private int boardTextureHeight = 256;  // 纹理总高度
        private int boardLeftU = 19;          // 棋盘区域在纹理中的左边界
        private int boardTopV = 19;           // 棋盘区域在纹理中的上边界
        private int boardCellPixelSize = 12;
        private int pieceTextureSize = 32;

        public Builder boardTextureWidth(int boardTextureWidth) {
            this.boardTextureWidth = boardTextureWidth;
            return this;
        }

        public Builder boardTextureHeight(int boardTextureHeight) {
            this.boardTextureHeight = boardTextureHeight;
            return this;
        }

        public Builder boardLeftU(int boardLeftU) {
            this.boardLeftU = boardLeftU;
            return this;
        }

        public Builder boardTopV(int boardTopV) {
            this.boardTopV = boardTopV;
            return this;
        }

        public Builder boardCellPixelSize(int boardCellPixelSize) {
            this.boardCellPixelSize = boardCellPixelSize;
            return this;
        }

        public Builder pieceTextureSize(int pieceTextureSize) {
            this.pieceTextureSize = pieceTextureSize;
            return this;
        }

        public Builder rows(int rows) {
            this.rows = rows;
            return this;
        }

        public Builder cols(int cols) {
            this.cols = cols;
            return this;
        }

        public Builder playerCount(int playerCount) {
            this.playerCount = playerCount;
            return this;
        }

        public Builder initialPlayer(int initialPlayer) {
            this.initialPlayer = initialPlayer;
            return this;
        }

        public Builder pieceTypes(PieceType... pieceTypes) {
            this.pieceTypes = pieceTypes;
            return this;
        }

        public Builder pieceTextures(Identifier... textures) {
            this.pieceTextures = textures;
            return this;
        }

        public Builder boardTopTexture(Identifier texture) {
            this.boardTopTexture = texture;
            return this;
        }

        public Builder boardBottomTexture(Identifier texture) {
            this.boardBottomTexture = texture;
            return this;
        }

        public Builder boardSideTexture(Identifier texture) {
            this.boardSideTexture = texture;
            return this;
        }

        public Builder translationKey(String key) {
            this.translationKey = key;
            return this;
        }

        public Builder starPoints(List<int[]> starPoints) {
            this.starPoints = starPoints;
            return this;
        }

        public Builder placeRule(BiFunction<BaseBoardBlockEntity, Move, PlaceResult> rule) {
            this.placeRule = rule;
            return this;
        }

        public ChessGameConfig build() {
            if (pieceTextures == null || boardTopTexture == null || boardBottomTexture == null || boardSideTexture == null || placeRule == null) {
                throw new IllegalStateException("Missing required textures or rule");
            }
            return new ChessGameConfig(this);
        }
    }
}