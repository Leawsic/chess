package site.leawsic.chess.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import site.leawsic.chess.Chess;
import site.leawsic.chess.block.BaseBoardBlockEntity;
import site.leawsic.chess.config.ChessGameConfig;
import site.leawsic.chess.network.ChessNetwork;

public class BaseBoardScreen extends HandledScreen<BaseBoardScreenHandler> {
    private final ChessGameConfig config;
    private final BlockPos boardPos;
    private int boardLeft, boardTop, boardWidth, boardHeight, cellSize;
    private ButtonWidget clearButton, undoButton, editModeButton;
    private ButtonWidget[] pieceSelectButtons;
    private int selectedPieceType = 1; // 默认选择第一种棋子（黑棋）
    private boolean localEditMode = false; // 本地编辑模式状态

    public BaseBoardScreen(BaseBoardScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
        this.config = handler.getConfig();
        this.boardPos = handler.getBoardPos();
        this.boardWidth = (config.getCols() - 1) * config.getBoardCellPixelSize();
        this.boardHeight = (config.getRows() - 1) * config.getBoardCellPixelSize();
        this.backgroundWidth = Math.max(240, Math.max(boardWidth, config.getBoardTextureWidth()) + 32);
        this.backgroundHeight = Math.max(280, Math.max(boardHeight, config.getBoardTextureHeight()) + 80);
    }

    @Override
    protected void init() {
        super.init();
        this.playerInventoryTitleY = -1000;

        this.boardLeft = x + (backgroundWidth - config.getBoardTextureWidth()) / 2;
        this.boardTop = y + 30;
        this.cellSize = config.getBoardCellPixelSize();

        clearButton = ButtonWidget.builder(Text.translatable("gui.chess.clear"), btn -> sendPacket(ChessNetwork.CLEAR_BOARD))
                .dimensions(x + 10, y + backgroundHeight - 30, 50, 20).build();
        undoButton = ButtonWidget.builder(Text.translatable("gui.chess.undo"), btn -> sendPacket(ChessNetwork.UNDO_MOVE))
                .dimensions(x + 70, y + backgroundHeight - 30, 50, 20).build();
        editModeButton = ButtonWidget.builder(Text.translatable("gui.chess.edit_mode"), btn -> {
            sendPacket(ChessNetwork.TOGGLE_EDIT_MODE);
            // 立即切换本地状态并更新按钮
            localEditMode = !localEditMode;
            updatePieceSelectButtons();
        })
                .dimensions(x + 130, y + backgroundHeight - 30, 80, 20).build();
        addDrawableChild(clearButton);
        addDrawableChild(undoButton);
        addDrawableChild(editModeButton);
        
        // 初始化棋子选择按钮（只在编辑模式下显示）
        initPieceSelectButtons();
        
        // 从服务端同步初始的编辑模式状态
        BaseBoardBlockEntity be = getBlockEntity();
        if (be != null) {
            localEditMode = be.isEditMode();
        }
    }
    
    private void initPieceSelectButtons() {
        // 创建棋子选择按钮（从类型1开始，跳过EMPTY）
        int playerCount = config.getPlayerCount();
        pieceSelectButtons = new ButtonWidget[playerCount];
        
        for (int i = 0; i < playerCount; i++) {
            int pieceType = i + 1; // 棋子类型从1开始
            String pieceName = config.getPieceType(pieceType).name();
            
            pieceSelectButtons[i] = ButtonWidget.builder(
                    Text.translatable("gui.chess.piece." + pieceName),
                    btn -> {
                        selectedPieceType = pieceType;
                        updatePieceSelectButtons();
                    })
                    .dimensions(x + 10 + i * 60, y + backgroundHeight - 55, 50, 20)
                    .build();
        }
        
        // 初始状态下隐藏按钮（非编辑模式）
        updatePieceSelectButtons();
    }
    
    private void updatePieceSelectButtons() {
        if (pieceSelectButtons == null) return;
        
        // 使用本地状态而不是服务端状态，避免同步延迟
        boolean isEditMode = localEditMode;
        
        for (int i = 0; i < pieceSelectButtons.length; i++) {
            ButtonWidget btn = pieceSelectButtons[i];
            
            // 高亮当前选中的棋子类型
            int pieceType = i + 1;
            if (pieceType == selectedPieceType && isEditMode) {
                btn.setMessage(Text.translatable("gui.chess.piece." + config.getPieceType(pieceType).name() + "_selected"));
            } else {
                btn.setMessage(Text.translatable("gui.chess.piece." + config.getPieceType(pieceType).name()));
            }
            
            // 根据编辑模式动态添加或移除按钮
            if (isEditMode) {
                // 如果按钮不在子组件列表中，则添加
                if (!children().contains(btn)) {
                    addDrawableChild(btn);
                }
            } else {
                // 如果按钮在子组件列表中，则移除
                if (children().contains(btn)) {
                    remove(btn);
                }
            }
        }
    }

    private void sendPacket(Identifier channel, int... extra) {
        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeBlockPos(boardPos);
        if (channel.equals(ChessNetwork.PLACE_PIECE) && extra.length >= 3) {
            buf.writeByte(extra[0]); // x
            buf.writeByte(extra[1]); // y
            buf.writeByte(extra[2]); // pieceType
        }
        ClientPlayNetworking.send(channel, buf);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        int gridLeft = boardLeft + config.getBoardLeftU();
        int gridTop = boardTop + config.getBoardTopV();
        if (mouseX >= gridLeft - 4 && mouseX <= gridLeft + boardWidth + 4 &&
                mouseY >= gridTop - 4 && mouseY <= gridTop + boardHeight + 4) {
            int col = Math.round((float) (mouseX - gridLeft) / cellSize);
            int row = Math.round((float) (mouseY - gridTop) / cellSize);
            if (col >= 0 && col < config.getCols() && row >= 0 && row < config.getRows()) {
                // 在编辑模式下使用选中的棋子类型，否则使用当前玩家
                BaseBoardBlockEntity be = getBlockEntity();
                int pieceType = (be != null && be.isEditMode()) ? selectedPieceType : be.getCurrentPlayer();
                sendPacket(ChessNetwork.PLACE_PIECE, col, row, pieceType);
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    protected void drawBackground(DrawContext context, float delta, int mouseX, int mouseY) {
        Identifier boardTex = Chess.id("textures/" + config.getBoardTopTexture().getPath() + ".png");
        RenderSystem.setShader(GameRenderer::getPositionTexProgram);
        RenderSystem.setShaderTexture(0, boardTex);
        context.drawTexture(
                boardTex,
                boardLeft, boardTop,
                0, 0,
                config.getBoardTextureWidth(), config.getBoardTextureHeight(),
                config.getBoardTextureWidth(), config.getBoardTextureHeight()
        );
        
        // 在背景层绘制棋子，确保坐标系统一致
        BaseBoardBlockEntity be = getBlockEntity();
        if (be != null) {
            int gridLeft = boardLeft + config.getBoardLeftU();
            int gridTop = boardTop + config.getBoardTopV();
            int[][] board = be.getBoard();

            for (int row = 0; row < config.getRows(); row++) {
                for (int col = 0; col < config.getCols(); col++) {
                    int piece = board[row][col];
                    if (piece == config.getEmptyValue()) continue;

                    Identifier pieceTex = Chess.id("textures/" + config.getPieceTexture(piece).getPath() + ".png");
                    // 计算棋子绘制大小：根据配置的棋盘格子像素大小自动适配
                    int drawSize = config.getBoardCellPixelSize();
                    
                    // 计算棋子中心位置（在网格交叉点上）
                    int centerX = gridLeft + col * config.getBoardCellPixelSize();
                    int centerY = gridTop + row * config.getBoardCellPixelSize();
                    
                    // 棋子居中绘制
                    int drawX = centerX - drawSize / 2;
                    int drawY = centerY - drawSize / 2;

                    RenderSystem.setShader(GameRenderer::getPositionTexProgram);
                    RenderSystem.setShaderTexture(0, pieceTex);
                    // 根据配置的pieceTextureSize和实际绘制大小进行缩放
                    context.drawTexture(
                            pieceTex,
                            drawX, drawY,
                            0, 0,
                            drawSize, drawSize,
                            config.getPieceTextureSize(), config.getPieceTextureSize()
                    );
                }
            }
        }
    }

    @Override
    protected void drawForeground(DrawContext context, int mouseX, int mouseY) {
        BaseBoardBlockEntity be = getBlockEntity();
        if (be != null) {
            // 状态文字相对于背景左上角绘制
            String status = be.isGameOver() ? Text.translatable("gui.chess.game_over").getString() :
                    be.isEditMode() ? Text.translatable("gui.chess.edit_mode").getString() :
                            (be.getCurrentPlayer() == 1 ? "Black's turn" : "White's turn");
            context.drawText(textRenderer, status, 10, 10, 0xFFFFFF, false);
        }
    }

    private BaseBoardBlockEntity getBlockEntity() {
        if (client == null || client.world == null) return null;
        return (BaseBoardBlockEntity) client.world.getBlockEntity(boardPos);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        renderBackground(context);
        super.render(context, mouseX, mouseY, delta);
        drawMouseoverTooltip(context, mouseX, mouseY);
    }
}