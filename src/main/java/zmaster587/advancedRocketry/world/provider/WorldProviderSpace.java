package zmaster587.advancedRocketry.world.provider;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.world.biome.WorldChunkManagerHell;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraftforge.client.IRenderHandler;
import org.jetbrains.annotations.NotNull;
import zmaster587.advancedRocketry.AdvancedRocketry;
import zmaster587.advancedRocketry.api.AdvancedRocketryBiomes;
import zmaster587.advancedRocketry.api.Configuration;
import zmaster587.advancedRocketry.client.render.planet.RenderSpaceSky;
import zmaster587.advancedRocketry.dimension.DimensionManager;
import zmaster587.advancedRocketry.dimension.DimensionProperties;
import zmaster587.advancedRocketry.world.ChunkProviderSpace;

public class WorldProviderSpace extends WorldProviderPlanet {
	private IRenderHandler skyRender;
	private final DimensionProperties properties = (DimensionProperties) DimensionManager.defaultSpaceDimensionProperties.clone();
	
	@Override
	public double getHorizon() {
		return 0;
	}

	@Override
	public boolean isPlanet() {
		return false;
	}

	public int getAverageGroundLevel() {
		return 0;
	}
	
	@Override
	public @NotNull IChunkProvider createChunkGenerator() {
		return new ChunkProviderSpace(this.worldObj, this.worldObj.getSeed());
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public IRenderHandler getSkyRenderer() {
		if(Configuration.stationSkyOverride)
			return skyRender == null ? skyRender = new RenderSpaceSky() : skyRender;
		
		return super.getSkyRenderer();
	}


	@Override
	public float getSunBrightness(float partialTicks) {
		return 1.0F;
	}

	@Override
	public float getAtmosphereDensity(int x, int z) {
		return 0;
	}

	@Override
	protected void registerWorldChunkManager() {
		worldObj.getWorldInfo().setTerrainType(AdvancedRocketry.spaceWorldType);
		this.worldChunkMgr = new WorldChunkManagerHell(AdvancedRocketryBiomes.spaceBiome, 0.0F);
		this.hasNoSky = false;
	}
	
	@Override
	public @NotNull DimensionProperties getDimensionProperties(int x , int z) {
		return DimensionManager.defaultSpaceDimensionProperties;
	}
}
