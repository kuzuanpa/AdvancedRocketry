package zmaster587.advancedRocketry.api;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import net.minecraft.world.biome.BiomeGenBase;
import org.jetbrains.annotations.NotNull;

/**
 * Stores information relating to the biomes and biome registry of AdvancedRocketry
 */
public class AdvancedRocketryBiomes {
	
	public static final AdvancedRocketryBiomes instance = new AdvancedRocketryBiomes();
	private List<BiomeGenBase> registeredBiomes;
	private List<BiomeGenBase> registeredHighPressureBiomes;
	private List<BiomeGenBase> registeredSingleBiome;
	private static List<Integer> blackListedBiomeIds;
	
	public static BiomeGenBase moonBiome;
	public static BiomeGenBase hotDryBiome;
	public static BiomeGenBase alienForest;
	public static BiomeGenBase spaceBiome;
	public static BiomeGenBase stormLandsBiome;
	public static BiomeGenBase crystalChasms;
	public static BiomeGenBase swampDeepBiome;
	public static BiomeGenBase marsh;
	public static BiomeGenBase oceanSpires;
	public static BiomeGenBase moonBiomeDark;
	private AdvancedRocketryBiomes() {
		registeredBiomes = new ArrayList<>();
		registeredHighPressureBiomes = new LinkedList<>();
		blackListedBiomeIds = new ArrayList<>();
		registeredSingleBiome = new ArrayList<>();
	
	}
	
	/**
	 * TODO: support id's higher than 255.  
	 * Any biome registered through vanilla forge does not need to be registered here
	 * @param biome BiomeGenBase to register with AdvancedRocketry's Biome registry
	 */
	public void registerBiome(BiomeGenBase biome) {
		registeredBiomes.add(biome);
	}
	
	
	/**
	 * Registers biomes you don't want to spawn on any planet unless registered with highpressure or similar feature
	 */
	public void registerBlackListBiome(@NotNull BiomeGenBase biome) {
		blackListedBiomeIds.add(biome.biomeID);
	}
	
	/**
	 * Gets a list of the blacklisted Biome Ids
	 */
	public List<Integer> getBlackListedBiomes() {
		return blackListedBiomeIds;
	}
	
	/**
	 * Registers a biome as high pressure for use with the planet generators (It will only spawn on planets with high pressure)
	 * @param biome
	 */
	public void registerHighPressureBiome(BiomeGenBase biome) {
		registeredHighPressureBiomes.add(biome);
		registerBlackListBiome(biome);
	}
	
	public List<BiomeGenBase> getHighPressureBiomes() {
		return registeredHighPressureBiomes;	
	}
	
	/**
	 * Registers a biome to have a chance to spawn as the only biome on a planet
	 * @param biome
	 */
	public void registerSingleBiome(BiomeGenBase biome) {
		registeredSingleBiome.add(biome);
	}
	
	public List<BiomeGenBase> getSingleBiome() {
		return registeredSingleBiome;	
	}
	
	/**
	 * Gets Biomes from Advanced Rocketry's biomes registry.  If it does not exist attepts to retrieve from vanilla forge
	 * @param id biome id
	 * @return BiomeGenBase retrieved from the biome ID
	 */
	public BiomeGenBase getBiomeById(int id) {
		
		for(BiomeGenBase biome : registeredBiomes) {
			if( biome.biomeID == id)
				return biome;
		}
		
		return BiomeGenBase.getBiome(id);
	}

	public void blackListVanillaBiomes() {
		//Good grief... this is long, better than making users do it though..
		for(int i = 0; i < 40; i++)
			blackListedBiomeIds.add(i);
		
		blackListedBiomeIds.add(129);
		blackListedBiomeIds.add(130);
		blackListedBiomeIds.add(131);
		blackListedBiomeIds.add(132);
		blackListedBiomeIds.add(133);
		blackListedBiomeIds.add(134);
		blackListedBiomeIds.add(140);
		blackListedBiomeIds.add(149);
		blackListedBiomeIds.add(151);
		blackListedBiomeIds.add(155);
		blackListedBiomeIds.add(156);
		blackListedBiomeIds.add(157);
		blackListedBiomeIds.add(158);
		blackListedBiomeIds.add(160);
		blackListedBiomeIds.add(161);
		blackListedBiomeIds.add(162);
		blackListedBiomeIds.add(163);
		blackListedBiomeIds.add(164);
		blackListedBiomeIds.add(165);
		blackListedBiomeIds.add(166);
		blackListedBiomeIds.add(167);
	}
	
}
