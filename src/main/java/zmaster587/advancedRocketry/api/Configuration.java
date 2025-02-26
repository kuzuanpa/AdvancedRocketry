package zmaster587.advancedRocketry.api;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.jetbrains.annotations.NotNull;
import zmaster587.advancedRocketry.util.AsteroidSmall;
import net.minecraft.block.Block;

/**
 * Stores config variables
 *
 */
public class Configuration {
	public static final String configFolder = "advRocketry";
	
	public static final int orbit = 1000;
	public static int MoonId = -1;
	public static int spaceDimId = -2;
	public static final int fuelPointsPer10Mb = 10;
	public static int stationSize = 1024;
	
	public static double rocketThrustMultiplier;
	public static double fuelCapacityMultiplier;
	
	public static final int maxBiomes = 512;
	public static boolean rocketRequireFuel = true;
	public static boolean enableOxygen = true;
	public static boolean enableNausea = true;
	public static float buildSpeedMultiplier = 1f;
	
	public static boolean generateCopper;
	public static int copperPerChunk; 
	public static int copperClumpSize;
	
	public static boolean generateTin;
	public static int tinPerChunk;
	public static int tinClumpSize;
	
	public static boolean generateDilithium;
	public static int dilithiumClumpSize;
	public static int dilithiumPerChunk;
	public static int dilithiumPerChunkMoon;
	
	public static int aluminumPerChunk;
	public static int aluminumClumpSize;
	public static boolean generateAluminum;
	
	public static boolean generateIridium;
	public static int IridiumClumpSize;
	public static int IridiumPerChunk;
	
	public static boolean generateRutile;
	public static int rutilePerChunk;
	public static int rutileClumpSize;
	public static boolean allowMakingItemsForOtherMods;
	public static boolean scrubberRequiresCartrige;
	public static float EUMult;
	public static float RFMult;
	public static boolean overrideGCAir;
	public static int fuelPointsPerDilithium;
	public static boolean electricPlantsSpawnLightning;

	public static boolean allowSawmillVanillaWood;
	
	public static int atmosphereHandleBitMask;

	public static boolean automaticRetroRockets;

	public static boolean advancedVFX;

	public static boolean enableLaserDrill;

	public static int spaceSuitOxygenTime;

	public static float travelTimeMultiplier;

	public static int maxBiomesPerPlanet;

	public static boolean enableTerraforming;

	public static double gasCollectionMult;
	public static boolean allowTerraforming;

	public static int terraformingBlockSpeed;
	public static double terraformSpeed;
	public static boolean terraformRequiresFluid;
	public static float microwaveRecieverMulitplier;
	public static boolean blackListAllVanillaBiomes;
	public static boolean canPlayerRespawnInSpace;
	public static float spaceLaserPowerMult;
	public static final List<Integer> laserBlackListDims = new LinkedList<>();
	public static final List<String> standardLaserDrillOres = new LinkedList<>();
	public static boolean laserDrillPlanet;
	public static double asteroidMiningTimeMult;

	/** list of entities of which atmospheric effects should not be applied **/
	public static final List<Class> bypassEntity = new LinkedList<>();
	public static final @NotNull List<Block> torchBlocks = new LinkedList<>();
	public static final List<Block> blackListRocketBlocks = new LinkedList<>();
	public static final @NotNull List<String> standardGeodeOres = new LinkedList<>();
	public static final Set<Integer> initiallyKnownPlanets = new HashSet<>();

	public static boolean geodeOresBlackList;

	public static boolean laserDrillOresBlackList;

	public static boolean lockUI;

	public static final HashMap<String, AsteroidSmall> asteroidTypes = new HashMap<>();
	public static @NotNull HashMap<String, AsteroidSmall> prevAsteroidTypes = new HashMap<>();
	public static int oxygenVentSize;

	public static int solarGeneratorMult;

	public static boolean gravityAffectsFuel;
	public static boolean lowGravityBoots;

	public static float jetPackThrust;
	public static boolean enableGravityController;
	public static boolean generateGeodes;
	public static int geodeBaseSize;
	public static int geodeVariation;
	public static int terraformliquidRate;
	public static boolean planetsMustBeDiscovered;
	public static boolean dropExTorches;

	public static double oxygenVentConsumptionMult;

	public static int terraformPlanetSpeed;

	public static int planetDiscoveryChance;

	public static double oxygenVentPowerMultiplier;

	public static boolean skyOverride;
	public static boolean planetSkyOverride;
	public static boolean stationSkyOverride;

	public static boolean allowTerraformNonAR;
	public static boolean forcePlayerRespawnInSpace;
}
