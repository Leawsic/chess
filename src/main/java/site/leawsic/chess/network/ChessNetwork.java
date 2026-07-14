package site.leawsic.chess.network;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import site.leawsic.chess.Chess;
import site.leawsic.chess.block.BaseBoardBlockEntity;

public class ChessNetwork {
    public static final Identifier PLACE_PIECE = Chess.id("place_piece");
    public static final Identifier CLEAR_BOARD = Chess.id("clear_board");
    public static final Identifier TOGGLE_EDIT_MODE = Chess.id("toggle_edit");
    public static final Identifier PASS_TURN = Chess.id("pass_turn");
    public static final Identifier FINISH_GO_GAME = Chess.id("finish_go_game");
    // 联机相关网络包
    public static final Identifier JOIN_GAME = Chess.id("join_game");
    public static final Identifier LEAVE_GAME = Chess.id("leave_game");
    public static final Identifier SET_PIECE_TYPES = Chess.id("set_piece_types");
    public static final Identifier SET_GAME_MODE = Chess.id("set_game_mode");
    public static final Identifier TOGGLE_AI = Chess.id("toggle_ai");
    public static final Identifier XIANGQI_MOVE = Chess.id("xiangqi_move");
    public static final Identifier XIANGQI_RESET = Chess.id("xiangqi_reset");
    public static final Identifier GUI_NOTICE = Chess.id("gui_notice");

    private static void sendNotice(net.minecraft.server.network.ServerPlayerEntity player, String translationKey) {
        var buf = net.fabricmc.fabric.api.networking.v1.PacketByteBufs.create();
        buf.writeString(translationKey);
        ServerPlayNetworking.send(player, GUI_NOTICE, buf);
    }

    public static void registerServerReceivers() {
        ServerPlayNetworking.registerGlobalReceiver(PLACE_PIECE, (server, player, handler, buf, responseSender) -> {
            BlockPos pos = buf.readBlockPos();
            int x = buf.readByte();
            int y = buf.readByte();
            int pieceType = buf.readByte(); // 读取棋子类型
            server.execute(() -> {
                if (player.getWorld().getBlockEntity(pos) instanceof BaseBoardBlockEntity boardEntity) {
                    // 在编辑模式下使用传入的棋子类型，否则使用当前玩家
                    // 注意：placePiece 方法内部会根据多人模式验证权限
                    boardEntity.placePiece(x, y, pieceType, player.getUuid());
                }
            });
        });

        ServerPlayNetworking.registerGlobalReceiver(CLEAR_BOARD, (server, player, handler, buf, responseSender) -> {
            BlockPos pos = buf.readBlockPos();
            server.execute(() -> {
                if (player.getWorld().getBlockEntity(pos) instanceof BaseBoardBlockEntity boardEntity) {
                    boardEntity.clearBoard(player.getUuid());
                }
            });
        });

        ServerPlayNetworking.registerGlobalReceiver(TOGGLE_EDIT_MODE, (server, player, handler, buf, responseSender) -> {
            BlockPos pos = buf.readBlockPos();
            server.execute(() -> {
                if (player.getWorld().getBlockEntity(pos) instanceof BaseBoardBlockEntity boardEntity) {
                    boardEntity.toggleEditMode(player.getUuid());
                }
            });
        });

        ServerPlayNetworking.registerGlobalReceiver(PASS_TURN, (server, player, handler, buf, responseSender) -> {
            BlockPos pos = buf.readBlockPos();
            server.execute(() -> {
                if (player.getWorld().getBlockEntity(pos) instanceof BaseBoardBlockEntity boardEntity) {
                    boardEntity.passTurn(player.getUuid());
                }
            });
        });

        ServerPlayNetworking.registerGlobalReceiver(FINISH_GO_GAME, (server, player, handler, buf, responseSender) -> {
            BlockPos pos = buf.readBlockPos();
            server.execute(() -> {
                if (player.getWorld().getBlockEntity(pos) instanceof BaseBoardBlockEntity boardEntity) {
                    boardEntity.finishGoGame(player.getUuid());
                }
            });
        });
        
        // 加入游戏
        ServerPlayNetworking.registerGlobalReceiver(JOIN_GAME, (server, player, handler, buf, responseSender) -> {
            BlockPos pos = buf.readBlockPos();
            server.execute(() -> {
                if (player.getWorld().getBlockEntity(pos) instanceof BaseBoardBlockEntity boardEntity) {
                    boardEntity.joinGame(player.getUuid());
                } else if (player.getWorld().getBlockEntity(pos) instanceof site.leawsic.chess.block.XiangqiBoardBlockEntity board) {
                    String errorKey = board.joinGame(player.getUuid());
                    if (errorKey != null) sendNotice(player, errorKey);
                }
            });
        });
        
        // 退出游戏
        ServerPlayNetworking.registerGlobalReceiver(LEAVE_GAME, (server, player, handler, buf, responseSender) -> {
            BlockPos pos = buf.readBlockPos();
            server.execute(() -> {
                if (player.getWorld().getBlockEntity(pos) instanceof BaseBoardBlockEntity boardEntity) {
                    boardEntity.leaveGame(player.getUuid());
                } else if (player.getWorld().getBlockEntity(pos) instanceof site.leawsic.chess.block.XiangqiBoardBlockEntity board) {
                    String errorKey = board.leaveGame(player.getUuid());
                    if (errorKey != null) sendNotice(player, errorKey);
                }
            });
        });
        
        // 设置先后手（由房主决定）
        ServerPlayNetworking.registerGlobalReceiver(SET_PIECE_TYPES, (server, player, handler, buf, responseSender) -> {
            BlockPos pos = buf.readBlockPos();
            int hostType = buf.readByte();
            int guestType = buf.readByte();
            server.execute(() -> {
                if (player.getWorld().getBlockEntity(pos) instanceof BaseBoardBlockEntity boardEntity) {
                    boardEntity.setPieceTypes(hostType, guestType, player.getUuid());
                } else if (player.getWorld().getBlockEntity(pos) instanceof site.leawsic.chess.block.XiangqiBoardBlockEntity board) {
                    String errorKey = board.setPieceTypes(hostType == 1 ? site.leawsic.chess.config.XiangqiConfig.RED : site.leawsic.chess.config.XiangqiConfig.BLACK,
                            guestType == 1 ? site.leawsic.chess.config.XiangqiConfig.RED : site.leawsic.chess.config.XiangqiConfig.BLACK, player.getUuid());
                    if (errorKey != null) sendNotice(player, errorKey);
                }
            });
        });

        // 切换游戏模式（五子棋 / 围棋）
        ServerPlayNetworking.registerGlobalReceiver(SET_GAME_MODE, (server, player, handler, buf, responseSender) -> {
            BlockPos pos = buf.readBlockPos();
            int mode = buf.readByte();
            server.execute(() -> {
                if (player.getWorld().getBlockEntity(pos) instanceof BaseBoardBlockEntity boardEntity) {
                    boardEntity.setGameMode(mode, player.getUuid());
                }
            });
        });
        ServerPlayNetworking.registerGlobalReceiver(TOGGLE_AI, (server, player, handler, buf, responseSender) -> {
            BlockPos pos = buf.readBlockPos();
            server.execute(() -> {
                if (player.getWorld().getBlockEntity(pos) instanceof BaseBoardBlockEntity boardEntity) {
                    boardEntity.toggleAi(player.getUuid());
                } else if (player.getWorld().getBlockEntity(pos) instanceof site.leawsic.chess.block.XiangqiBoardBlockEntity board) {
                    board.toggleAi(player.getUuid());
                }
            });
        });
        ServerPlayNetworking.registerGlobalReceiver(XIANGQI_MOVE, (server, player, handler, buf, responseSender) -> {
            BlockPos pos = buf.readBlockPos();
            int fromX = buf.readByte(), fromY = buf.readByte(), toX = buf.readByte(), toY = buf.readByte();
            server.execute(() -> {
                if (player.getWorld().getBlockEntity(pos) instanceof site.leawsic.chess.block.XiangqiBoardBlockEntity board) {
                    String errorKey = board.tryMove(fromX, fromY, toX, toY, player.getUuid());
                    if (errorKey != null) sendNotice(player, errorKey);
                }
            });
        });
        ServerPlayNetworking.registerGlobalReceiver(XIANGQI_RESET, (server, player, handler, buf, responseSender) -> {
            BlockPos pos = buf.readBlockPos();
            server.execute(() -> { if (player.getWorld().getBlockEntity(pos) instanceof site.leawsic.chess.block.XiangqiBoardBlockEntity board) board.resetBoard(player.getUuid()); });
        });
    }
}
