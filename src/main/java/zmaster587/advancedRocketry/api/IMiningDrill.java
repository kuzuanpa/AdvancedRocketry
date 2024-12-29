package zmaster587.advancedRocketry.api;

import net.minecraft.world.World;

public interface IMiningDrill {
	/**
	 * @return mining speed of the drill in blocks/tick
	 */
    float getMiningSpeed(World world, int x, int y, int z);
	
	/**
	 * @return power consumption in units/tick
	 */
    int powerConsumption();
}
