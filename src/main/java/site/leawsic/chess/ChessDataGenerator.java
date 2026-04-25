package site.leawsic.chess;

import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import site.leawsic.chess.datagen.EnglishProvider;
import site.leawsic.chess.datagen.ModModelProvider;

public class ChessDataGenerator implements DataGeneratorEntrypoint {
	@Override
	public void onInitializeDataGenerator(FabricDataGenerator fabricDataGenerator) {
        FabricDataGenerator.Pack pack= fabricDataGenerator.createPack();

        pack.addProvider(EnglishProvider::new);
        pack.addProvider(ModModelProvider::new);
	}
}
