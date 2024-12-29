package zmaster587.advancedRocketry.world.type;

import zmaster587.advancedRocketry.api.AdvancedRocketryBiomes;
import net.minecraft.world.World;
import net.minecraft.world.WorldType;
import net.minecraft.world.biome.WorldChunkManager;
import net.minecraft.world.biome.WorldChunkManagerHell;

public class WorldTypeSpace extends WorldType {

	public WorldTypeSpace(String string) {
		super(string);
	}

	@Override
	public WorldChunkManager getChunkManager(World world)
	{
		return new WorldChunkManagerHell(AdvancedRocketryBiomes.spaceBiome, 0.5F);
	}

	
	@Override
	public boolean getCanBeCreated() {
		return false;
	}
}
