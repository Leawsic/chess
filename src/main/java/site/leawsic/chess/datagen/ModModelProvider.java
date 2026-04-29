package site.leawsic.chess.datagen;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricModelProvider;
import net.minecraft.block.Block;
import net.minecraft.data.client.BlockStateModelGenerator;
import net.minecraft.data.client.ItemModelGenerator;
import net.minecraft.data.client.Model;
import net.minecraft.data.client.ModelIds;
import net.minecraft.util.Identifier;
import site.leawsic.chess.Chess;
import site.leawsic.chess.block.ModBlocks;
import site.leawsic.chess.config.ChessGameConfig;
import site.leawsic.chess.config.GomokuConfig;

import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

public class ModModelProvider extends FabricModelProvider {
    public ModModelProvider(FabricDataOutput output) {
        super(output);
    }

    // 创建 [x, y, z] 数组
    private static JsonArray vec3(float x, float y, float z) {
        JsonArray arr = new JsonArray();
        arr.add(x);
        arr.add(y);
        arr.add(z);
        return arr;
    }

    // 创建 [u1, v1, u2, v2] 数组
    private static JsonArray uvArray(int u1, int v1, int u2, int v2) {
        JsonArray arr = new JsonArray();
        arr.add(u1);
        arr.add(v1);
        arr.add(u2);
        arr.add(v2);
        return arr;
    }

    private static JsonObject getFace(String textureRef, int u1, int v1, int u2, int v2) {
        JsonObject face = new JsonObject();
        face.add("uv", uvArray(u1, v1, u2, v2));
        face.addProperty("texture", textureRef);
        return face;
    }

    private static JsonObject getFaces() {
        JsonObject faces = new JsonObject();
        faces.add("up", getFace("#top", 0, 0, 16, 16));
        faces.add("down", getFace("#bottom", 0, 0, 16, 16));
        faces.add("north", getFace("#side", 0, 15, 16, 16));
        faces.add("south", getFace("#side", 0, 15, 16, 16));
        faces.add("west", getFace("#side", 0, 15, 16, 16));
        faces.add("east", getFace("#side", 0, 15, 16, 16));
        return faces;
    }

    @Override
    public void generateBlockStateModels(BlockStateModelGenerator generator) {
        ChessGameConfig config = GomokuConfig.CONFIG;
        Identifier modelId = generateThinBoardModel(ModBlocks.GO_BOARD, config, generator.modelCollector);
        generator.blockStateCollector.accept(
                BlockStateModelGenerator.createSingletonBlockState(ModBlocks.GO_BOARD, modelId)
        );
    }

    @Override
    public void generateItemModels(ItemModelGenerator generator) {
        generator.register(
                ModBlocks.GO_BOARD.asItem(),
                new Model(Optional.of(Chess.id("block/go_board_thin")), Optional.empty())
        );
    }

    private Identifier generateThinBoardModel(Block block, ChessGameConfig config,
                                              BiConsumer<Identifier, Supplier<JsonElement>> modelCollector) {
        Identifier modelId = ModelIds.getBlockModelId(block).withSuffixedPath("_thin");
        JsonObject json = new JsonObject();

        // 纹理定义
        JsonObject textures = new JsonObject();
        textures.addProperty("top", config.getBoardTopTexture().toString());
        textures.addProperty("bottom", config.getBoardBottomTexture().toString());
        textures.addProperty("side", config.getBoardSideTexture().toString());
        textures.addProperty("particle", config.getBoardTopTexture().toString());
        json.add("textures", textures);

        // 从 [0,0,0] 到 [16,1,16] 的立方体
        JsonArray elements = new JsonArray();
        JsonObject element = new JsonObject();
        element.add("from", vec3(0, 0, 0));
        element.add("to", vec3(16, 1, 16));
        element.add("faces", getFaces());
        elements.add(element);
        json.add("elements", elements);

        modelCollector.accept(modelId, () -> json);
        return modelId;
    }
}