package zmaster587.advancedRocketry.util;

import java.util.LinkedList;
import java.util.List;

import org.jetbrains.annotations.NotNull;
import zmaster587.advancedRocketry.dimension.DimensionProperties;
import zmaster587.advancedRocketry.dimension.DimensionProperties.AtmosphereTypes;
import zmaster587.advancedRocketry.dimension.DimensionProperties.Temps;
import zmaster587.libVulpes.block.BlockMeta;

public class OreGenProperties {

	private final List<OreEntry> oreEntries;
	
	/**
	 * Array of properties for [pressure][temperature]
	 * @see DimensionProperties.AtmosphereTypes
	 * @see DimensionProperties.Temps
	 */
	private static final OreGenProperties[][] oreGenPropertyMap = new OreGenProperties[DimensionProperties.AtmosphereTypes.values().length][DimensionProperties.Temps.values().length];
	
	public OreGenProperties() {
		oreEntries = new LinkedList<>();
	}
	
	public void addEntry(BlockMeta state, int minHeight, int maxHeight, int clumpSize, int chancePerChunk) {
		oreEntries.add(new OreEntry(state, minHeight, maxHeight, clumpSize, chancePerChunk));
	}
	
	public List<OreEntry> getOreEntries() {
		return oreEntries;
	}
	
	/**
	 * Sets any planet with temperature temp to use these properties regardless of pressure
	 * @param temp Temperature to set
	 * @param properties
	 */
	public static void setOresForTemperature(Temps temp, OreGenProperties properties) {
		for(int i = 0; i < AtmosphereTypes.values().length; i++)
			oreGenPropertyMap[i][temp.ordinal()] = properties;
	}
	
	public static void setOresForPressure(@NotNull AtmosphereTypes atmType, OreGenProperties properties) {
		for(int i = 0; i < Temps.values().length; i++)
			oreGenPropertyMap[atmType.ordinal()][i] = properties;
	}
	
	public static void setOresForPressureAndTemp(AtmosphereTypes atmType, @NotNull Temps temp, OreGenProperties properties) {
		oreGenPropertyMap[atmType.ordinal()][temp.ordinal()] = properties;
	}
	
	public static OreGenProperties getOresForPressure(AtmosphereTypes atmType, Temps temp) {
		return oreGenPropertyMap[atmType.ordinal()][temp.ordinal()];
	}
	
	public static class OreEntry {
		private final BlockMeta state;
		final int minHeight;
		final int maxHeight;
		final int clumpSize;
		final int chancePerChunk;
		
		public OreEntry(BlockMeta state, int minHeight, int maxHeight, int clumpSize, int chancePerChunk) {
			this.state = state;
			this.minHeight = minHeight;
			this.maxHeight = maxHeight;
			this.clumpSize = clumpSize;
			this.chancePerChunk = chancePerChunk;
		}
		
		public BlockMeta getBlockState() {
			return state;
		}
		
		public int getMinHeight() {
			return minHeight;
		}
		
		public int getMaxHeight() {
			return maxHeight;
		}
		public int getClumpSize() {
			return clumpSize;
		}
		
		public int getChancePerChunk() {
			return chancePerChunk;
		}
	}
	
}
