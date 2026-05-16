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
    // 联机相关网络包
    public static final Identifier JOIN_GAME = Chess.id("join_game");
    public static final Identifier LEAVE_GAME = Chess.id("leave_game");
    public static final Identifier SET_PIECE_TYPES = Chess.id("set_piece_types");

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
        
        // 加入游戏
        ServerPlayNetworking.registerGlobalReceiver(JOIN_GAME, (server, player, handler, buf, responseSender) -> {
            BlockPos pos = buf.readBlockPos();
            server.execute(() -> {
                if (player.getWorld().getBlockEntity(pos) instanceof BaseBoardBlockEntity boardEntity) {
                    boardEntity.joinGame(player.getUuid());
                }
            });
        });
        
        // 退出游戏
        ServerPlayNetworking.registerGlobalReceiver(LEAVE_GAME, (server, player, handler, buf, responseSender) -> {
            BlockPos pos = buf.readBlockPos();
            server.execute(() -> {
                if (player.getWorld().getBlockEntity(pos) instanceof BaseBoardBlockEntity boardEntity) {
                    boardEntity.leaveGame(player.getUuid());
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
                }
            });
        });
    }
}