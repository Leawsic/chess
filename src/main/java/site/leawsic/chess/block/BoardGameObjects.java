package site.leawsic.chess.block;

import net.minecraft.block.Block;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.item.Item;
import net.minecraft.screen.ScreenHandlerType;
import site.leawsic.chess.screen.BaseBoardScreenHandler;

/**
 * 封装一种棋盘游戏注册后的所有对象。
 */
public record BoardGameObjects(Block block, Item item, BlockEntityType<BaseBoardBlockEntity> blockEntityType, ScreenHandlerType<BaseBoardScreenHandler> screenHandlerType) {
}