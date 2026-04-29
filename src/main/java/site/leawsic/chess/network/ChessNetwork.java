package site.leawsic.chess.network;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import site.leawsic.chess.Chess;
import site.leawsic.chess.block.BaseBoardBlockEntity;

public class ChessNetwork {
    public static final Identifier PLACE_PIECE = Chess.id("place_piece");
    public static final Identifier CLEAR_BOARD = Chess.id("clear_board");
    public static final Identifier UNDO_MOVE = Chess.id("undo_move");
    public static final Identifier TOGGLE_EDIT_MODE = Chess.id("toggle_edit");

    public static void registerServerReceivers() {
        ServerPlayNetworking.registerGlobalReceiver(PLACE_PIECE, (server, player, handler, buf, responseSender) -> {
            BlockPos pos = buf.readBlockPos();
            int x = buf.readByte();
            int y = buf.readByte();
            int pieceType = buf.readByte(); // 读取棋子类型
            server.execute(() -> {
                if (player.getWorld().getBlockEntity(pos) instanceof BaseBoardBlockEntity boardEntity) {
                    // 在编辑模式下使用传入的棋子类型，否则使用当前玩家
                    int finalPieceType = boardEntity.isEditMode() ? pieceType : boardEntity.getCurrentPlayer();
                    boardEntity.placePiece(x, y, finalPieceType);
                }
            });
        });

        ServerPlayNetworking.registerGlobalReceiver(CLEAR_BOARD, (server, player, handler, buf, responseSender) -> {
            BlockPos pos = buf.readBlockPos();
            server.execute(() -> {
                if (player.getWorld().getBlockEntity(pos) instanceof BaseBoardBlockEntity boardEntity) {
                    boardEntity.clearBoard();
                }
            });
        });

        ServerPlayNetworking.registerGlobalReceiver(UNDO_MOVE, (server, player, handler, buf, responseSender) -> {
            BlockPos pos = buf.readBlockPos();
            server.execute(() -> {
                if (player.getWorld().getBlockEntity(pos) instanceof BaseBoardBlockEntity boardEntity) {
                    boardEntity.undoMove();
                }
            });
        });

        ServerPlayNetworking.registerGlobalReceiver(TOGGLE_EDIT_MODE, (server, player, handler, buf, responseSender) -> {
            BlockPos pos = buf.readBlockPos();
            server.execute(() -> {
                if (player.getWorld().getBlockEntity(pos) instanceof BaseBoardBlockEntity boardEntity) {
                    boardEntity.toggleEditMode();
                }
            });
        });
    }
}