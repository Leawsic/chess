package site.leawsic.chess.datagen;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricModelProvider;
import net.minecraft.block.Block;
import net.minecraft.data.client.BlockStateModelGenerator;
import net.minecraft.data.client.BlockStateSupplier;
import net.minecraft.data.client.BlockStateVariant;
import net.minecraft.data.client.ItemModelGenerator;
import net.minecraft.data.client.ModelIds;
import net.minecraft.data.client.Models;
import net.minecraft.data.client.VariantSettings;
import net.minecraft.data.client.VariantsBlockStateSupplier;
import net.minecraft.util.Identifier;
import site.leawsic.chess.block.ModBlocks;
import site.leawsic.chess.config.ChessGameConfig;
import site.leawsic.chess.config.GomokuConfig;

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
        // 五子棋 + 围棋（共用同一棋盘，GUI 切换模式）
        ChessGameConfig gomokuConfig = GomokuConfig.CONFIG;
        Identifier gomokuModelId = generateThinBoardModel(ModBlocks.GOMOKU_BOARD, gomokuConfig, generator.modelCollector);
        generator.blockStateCollector.accept(VariantsBlockStateSupplier.create(
                ModBlocks.GOMOKU_BOARD,
                BlockStateVariant.create().put(VariantSettings.MODEL, gomokuModelId)
        ).coordinate(BlockStateModelGenerator.createNorthDefaultHorizontalRotationStates()));
        generateInvisiblePlaceholderModel(ModBlocks.GOMOKU.placeholderBlock(), generator);
    }

    @Override
    public void generateItemModels(ItemModelGenerator generator) {
        generator.register(ModBlocks.GOMOKU.item(), Models.GENERATED);
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

        // 从 [-16,0,-16] 到 [32,1,32] 的 3x3 薄棋盘，保持模型坐标合法。
        JsonArray elements = new JsonArray();
        JsonObject element = new JsonObject();
        element.add("from", vec3(-16, 0, -16));
        element.add("to", vec3(32, 1, 32));
        element.add("faces", getFaces());
        elements.add(element);
        json.add("elements", elements);

        modelCollector.accept(modelId, () -> json);
        return modelId;
    }

    private void generateInvisiblePlaceholderModel(Block block, BlockStateModelGenerator generator) {
        Identifier modelId = ModelIds.getBlockModelId(block);
        JsonObject json = new JsonObject();
        json.add("elements", new JsonArray());
        generator.modelCollector.accept(modelId, () -> json);
        generator.blockStateCollector.accept(new BlockStateSupplier() {
            @Override
            public Block getBlock() {
                return block;
            }

            @Override
            public JsonElement get() {
                JsonObject variants = new JsonObject();
                for (String facing : new String[]{"north", "south", "west", "east"}) {
                    for (int offsetX = 0; offsetX <= 2; offsetX++) {
                        for (int offsetZ = 0; offsetZ <= 2; offsetZ++) {
                            String key = "facing=" + facing + ",offset_x=" + offsetX + ",offset_z=" + offsetZ;
                            JsonObject variant = new JsonObject();
                            variant.addProperty("model", modelId.toString());
                            variants.add(key, variant);
                        }
                    }
                }
                JsonObject root = new JsonObject();
                root.add("variants", variants);
                return root;
            }
        });
    }
}
