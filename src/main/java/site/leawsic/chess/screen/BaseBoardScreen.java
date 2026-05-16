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

import java.util.UUID;

public class BaseBoardScreen extends HandledScreen<BaseBoardScreenHandler> {
    private final ChessGameConfig config;
    private final BlockPos boardPos;
    private int boardLeft, boardTop, boardWidth, boardHeight, cellSize;
    private ButtonWidget clearButton, editModeButton;
    private ButtonWidget[] pieceSelectButtons;
    private ButtonWidget joinButton, leaveButton, hostBlackButton, hostWhiteButton;
    private int selectedPieceType = 1; // 默认选择第一种棋子（黑棋）
    private boolean localEditMode = false; // 本地编辑模式状态

    public BaseBoardScreen(BaseBoardScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
        this.config = handler.getConfig();
        this.boardPos = handler.getBoardPos();
        this.boardWidth = (config.getCols() - 1) * config.getBoardCellPixelSize();
        this.boardHeight = (config.getRows() - 1) * config.getBoardCellPixelSize();
        this.backgroundWidth = Math.max(360, Math.max(boardWidth, config.getBoardTextureWidth()) + 32);
        this.backgroundHeight = Math.max(280, Math.max(boardHeight, config.getBoardTextureHeight()) + 80);
    }

    @Override
    protected void init() {
        super.init();
        this.playerInventoryTitleY = -1000;

        this.boardLeft = x + (backgroundWidth - config.getBoardTextureWidth()) / 2;
        this.boardTop = y + 30;
        this.cellSize = config.getBoardCellPixelSize();

        int buttonY1 = y + backgroundHeight - 50; // 第一行
        int buttonY2 = y + backgroundHeight - 25; // 第二行
        int buttonSpacing = 5; // 按钮间距

        // 左侧按钮组总宽度
        int leftButtonsWidth = 60 + buttonSpacing + 80;

        // 检查是否有足够空间放置右侧联机按钮（至少需要150px空间）
        boolean hasSpaceForRightButtons = (backgroundWidth - leftButtonsWidth - 40) >= 150;

        clearButton = ButtonWidget.builder(Text.translatable("gui.chess.clear"), btn -> sendPacket(ChessNetwork.CLEAR_BOARD))
                .dimensions(x + 10, buttonY1, 60, 20).build();
        editModeButton = ButtonWidget.builder(Text.translatable("gui.chess.edit_mode"), btn -> {
            sendPacket(ChessNetwork.TOGGLE_EDIT_MODE);
            // 立即切换本地状态并更新按钮
            localEditMode = !localEditMode;
            updatePieceSelectButtons();
        }).dimensions(x + 75, buttonY1, 80, 20).build();

        if (hasSpaceForRightButtons) {
            joinButton = ButtonWidget.builder(Text.translatable("gui.chess.join"), btn -> sendPacket(ChessNetwork.JOIN_GAME))
                    .dimensions(x + backgroundWidth - 145, buttonY1, 65, 20).build();
            leaveButton = ButtonWidget.builder(Text.translatable("gui.chess.leave"), btn -> sendPacket(ChessNetwork.LEAVE_GAME))
                    .dimensions(x + backgroundWidth - 75, buttonY1, 65, 20).build();
        } else {
            joinButton = ButtonWidget.builder(Text.translatable("gui.chess.join"), btn -> sendPacket(ChessNetwork.JOIN_GAME))
                    .dimensions(x + backgroundWidth - 145, buttonY2, 65, 20).build();
            leaveButton = ButtonWidget.builder(Text.translatable("gui.chess.leave"), btn -> sendPacket(ChessNetwork.LEAVE_GAME))
                    .dimensions(x + backgroundWidth - 75, buttonY2, 65, 20).build();
        }

        hostBlackButton = ButtonWidget.builder(Text.translatable("gui.chess.host_black"), btn -> sendPacket(ChessNetwork.SET_PIECE_TYPES, 1, 2))
                .dimensions(x + 160, buttonY2, 75, 20).build();
        hostWhiteButton = ButtonWidget.builder(Text.translatable("gui.chess.host_white"), btn -> sendPacket(ChessNetwork.SET_PIECE_TYPES, 2, 1))
                .dimensions(x + 240, buttonY2, 75, 20).build();

        addDrawableChild(clearButton);
        addDrawableChild(editModeButton);
        addDrawableChild(joinButton);
        addDrawableChild(leaveButton);
        addDrawableChild(hostBlackButton);
        addDrawableChild(hostWhiteButton);

        // 初始化棋子选择按钮
        initPieceSelectButtons();

        // 从服务端同步初始的编辑模式状态
        BaseBoardBlockEntity be = getBlockEntity();
        if (be != null) {
            localEditMode = be.isEditMode();
        }
    }

    private void initPieceSelectButtons() {
        // 创建棋子选择按钮
        int playerCount = config.getPlayerCount();
        pieceSelectButtons = new ButtonWidget[playerCount];

        int buttonY = y + backgroundHeight - 25; // 第二行
        int startX = x + 10;
        int buttonSpacing = 5;
        int buttonWidth = 55;

        for (int i = 0; i < playerCount; i++) {
            int pieceType = i + 1; // 棋子类型从1开始
            String pieceName = config.getPieceType(pieceType).name();

            pieceSelectButtons[i] = ButtonWidget.builder(
                            Text.translatable("gui.chess.piece." + pieceName),
                            btn -> {
                                selectedPieceType = pieceType;
                                updatePieceSelectButtons();
                            })
                    .dimensions(startX + i * (buttonWidth + buttonSpacing), buttonY, buttonWidth, 20)
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
            buf.writeByte(extra[0]);
            buf.writeByte(extra[1]);
            buf.writeByte(extra[2]);
        } else if (channel.equals(ChessNetwork.SET_PIECE_TYPES) && extra.length >= 2) {
            buf.writeByte(extra[0]);
            buf.writeByte(extra[1]);
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
                BaseBoardBlockEntity be = getBlockEntity();

                // 检查玩家是否已加入对局
                if (client != null && client.player != null && be != null) {
                    if (!be.isInGame(client.player.getUuid())) {
                        // 玩家未加入对局，不允许下棋
                        return false;
                    }
                } else {
                    return false;
                }

                int pieceType;

                if (be.isMultiplayer()) {
                    // 多人模式：使用玩家的棋子类型
                    if (client != null && client.player != null) {
                        pieceType = be.getPlayerPieceType(client.player.getUuid());
                    } else {
                        return false;
                    }
                } else if (be.isEditMode()) {
                    // 编辑模式：使用选中的棋子类型
                    pieceType = selectedPieceType;
                } else {
                    // 单人模式：使用当前玩家
                    pieceType = be.getCurrentPlayer();
                }

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
            // 更新联机按钮可见性
            updateMultiplayerButtons(be);

            // 游戏结束时显示胜利反馈
            if (be.isGameOver()) {
                drawGameOverScreen(context, be);
            } else {
                // 正常状态显示
                String status;
                if (be.isMultiplayer()) {
                    // 多人模式显示玩家名称
                    String hostName = "?";
                    String guestName = "?";

                    if (client != null && client.world != null) {
                        if (be.getHostPlayer() != null) {
                            var hostPlayer = client.world.getPlayerByUuid(be.getHostPlayer());
                            if (hostPlayer != null) {
                                hostName = hostPlayer.getName().getString();
                            }
                        }
                        if (be.getGuestPlayer() != null) {
                            var guestPlayer = client.world.getPlayerByUuid(be.getGuestPlayer());
                            if (guestPlayer != null) {
                                guestName = guestPlayer.getName().getString();
                            }
                        }
                    }

                    // 检查当前玩家是否在対局中
                    if (client != null && client.player != null) {
                        if (!be.isInGame(client.player.getUuid())) {
                            // 旁观者
                            status = String.format("%s vs %s | %s: %s",
                                    hostName, guestName,
                                    Text.translatable("gui.chess.turn").getString(),
                                    be.getCurrentPlayer() == be.getHostPieceType() ? hostName : guestName);
                        } else {
                            String turnPlayer = be.getCurrentPlayer() == be.getHostPieceType() ? hostName : guestName;
                            status = String.format("%s vs %s | %s: %s",
                                    hostName, guestName,
                                    Text.translatable("gui.chess.turn").getString(), turnPlayer);
                        }
                    } else {
                        String turnPlayer = be.getCurrentPlayer() == be.getHostPieceType() ? hostName : guestName;
                        status = String.format("%s vs %s | %s: %s",
                                hostName, guestName,
                                Text.translatable("gui.chess.turn").getString(), turnPlayer);
                    }
                } else if (be.isEditMode()) {
                    status = Text.translatable("gui.chess.edit_mode").getString();
                } else {
                    // 从配置中获取当前玩家的棋子名称
                    String pieceName = config.getPieceType(be.getCurrentPlayer()).name();
                    status = Text.translatable("gui.chess.turn_format",
                            Text.translatable("gui.chess.piece." + pieceName).getString()).getString();
                }
                context.drawText(textRenderer, status, 10, 10, 0xFFFFFF, false);
            }
        }
    }

    /**
     * 绘制游戏结束胜利反馈
     */
    private void drawGameOverScreen(DrawContext context, BaseBoardBlockEntity be) {
        // 半透明背景遮罩
        int overlayAlpha = 180;
        context.fill(0, 0, backgroundWidth, backgroundHeight, (overlayAlpha << 24));

        // 获取获胜者
        int winner = 0;
        // 从最后一步棋判断获胜者
        if (!be.getMoveHistory().isEmpty()) {
            winner = be.getMoveHistory().get(be.getMoveHistory().size() - 1).player();
        }

        // 中央显示获胜信息
        String winnerText;
        int winnerColor;

        if (be.isMultiplayer()) {
            // 多人模式：显示玩家名称
            UUID winnerUuid;
            if (winner == be.getHostPieceType()) {
                winnerUuid = be.getHostPlayer();
            } else {
                winnerUuid = be.getGuestPlayer();
            }

            String winnerName = "Unknown";
            if (client != null && client.world != null && winnerUuid != null) {
                var player = client.world.getPlayerByUuid(winnerUuid);
                if (player != null) {
                    winnerName = player.getName().getString();
                }
            }

            winnerText = winnerName + Text.translatable("gui.chess.winner_suffix").getString();
            winnerColor = winner == 1 ? 0x000000 : 0xFFFFFF;
        } else {
            // 单人模式：从配置中获取获胜棋子名称
            if (winner > 0 && winner <= config.getPlayerCount()) {
                String winnerPieceName = config.getPieceType(winner).name();
                winnerText = Text.translatable("gui.chess.piece." + winnerPieceName).getString() +
                        Text.translatable("gui.chess.winner_suffix").getString();
            } else {
                // 防御性编程：如果winner无效，显示默认文本
                winnerText = Text.translatable("gui.chess.game_over").getString();
            }
            winnerColor = winner == 1 ? 0x404040 : 0xFFFFFF;
        }

        // 绘制标题
        int centerX = backgroundWidth / 2;
        int centerY = backgroundHeight / 2 - 20;

        // 标题背景
        int titleWidth = textRenderer.getWidth(winnerText) + 40;
        int titleHeight = 50;
        context.fill(centerX - titleWidth / 2, centerY - 15, centerX + titleWidth / 2, centerY + titleHeight - 15, 0x80000000);

        // 标题文字
        context.drawCenteredTextWithShadow(textRenderer, winnerText, centerX, centerY, winnerColor);

        // 副标题
        String subtitle = Text.translatable("gui.chess.game_over").getString();
        context.drawCenteredTextWithShadow(textRenderer, subtitle, centerX, centerY + 25, 0xAAAAAA);

        // 底部提示
        String hint = Text.translatable("gui.chess.clear_hint").getString();
        context.drawCenteredTextWithShadow(textRenderer, hint, centerX, backgroundHeight - 60, 0x888888);
    }

    private void updateMultiplayerButtons(BaseBoardBlockEntity be) {
        if (client == null || client.player == null) return;

        UUID playerUuid = client.player.getUuid();
        boolean isHost = be.isHost(playerUuid);
        boolean isInGame = be.isInGame(playerUuid);
        boolean isMultiplayer = be.isMultiplayer();
        boolean isFull = be.isMultiplayer() && be.getHostPlayer() != null && be.getGuestPlayer() != null;

        hostBlackButton.visible = isMultiplayer && isHost;
        hostWhiteButton.visible = isMultiplayer && isHost;
        hostBlackButton.active = isMultiplayer && isHost && be.getHostPieceType() != 1;
        hostWhiteButton.active = isMultiplayer && isHost && be.getHostPieceType() != 2;

        if (!isInGame && isFull) {
            clearButton.visible = false;
            editModeButton.visible = false;
            joinButton.visible = false;
            leaveButton.visible = false;
            hostBlackButton.visible = false;
            hostWhiteButton.visible = false;
            return;
        }

        joinButton.visible = !isInGame;
        joinButton.active = !isInGame;

        leaveButton.visible = isMultiplayer && isInGame;
        leaveButton.active = isMultiplayer && isInGame;

        editModeButton.visible = true;
        editModeButton.active = !isMultiplayer && isInGame && !be.isGameOver();
        if (!editModeButton.active) localEditMode = false;

        if (isMultiplayer) {
            clearButton.active = isHost && be.isGameOver();
        } else {
            clearButton.active = isInGame && hasPieces(be);
        }
    }

    private boolean hasPieces(BaseBoardBlockEntity be) {
        int[][] board = be.getBoard();
        for (int row = 0; row < config.getRows(); row++) {
            for (int col = 0; col < config.getCols(); col++) {
                if (board[row][col] != config.getEmptyValue()) return true;
            }
        }
        return false;
    }

    private BaseBoardBlockEntity getBlockEntity() {
        if (client == null || client.world == null) return null;
        if (client.world.getBlockEntity(boardPos) instanceof BaseBoardBlockEntity boardEntity) {
            return boardEntity;
        }
        return null;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        renderBackground(context);
        super.render(context, mouseX, mouseY, delta);
        drawMouseoverTooltip(context, mouseX, mouseY);

        // 每帧更新按钮状态，确保与当前游戏状态同步
        BaseBoardBlockEntity be = getBlockEntity();
        if (be != null) {
            updateMultiplayerButtons(be);
        }
    }
}