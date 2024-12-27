package zmaster587.advancedRocketry.world.util;

import org.jetbrains.annotations.Nullable;
import zmaster587.advancedRocketry.api.AdvancedRocketryBiomes;
import net.minecraft.world.WorldProvider;
import net.minecraft.world.biome.BiomeGenBase;

public class ProviderDummy extends WorldProvider {

	@Override
	public @Nullable String getDimensionName() {
		return null;
	}
	
	@Override
	public BiomeGenBase getBiomeGenForCoords(int x, int z) {
		return AdvancedRocketryBiomes.spaceBiome;
	}

}
