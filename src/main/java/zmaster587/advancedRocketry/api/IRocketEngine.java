package zmaster587.advancedRocketry.api;

import net.minecraft.world.World;

/**
 * Must be implemented by any block that is to be treated like a rocket engine
 *
 */
public interface IRocketEngine {
	/**
	 * TODO: adjust.<br>  
	 * amount of thrust per engine
	 * @return meters per tick per block
	 */
    int getThrust(World world, int x, int y, int z);
	
	/**
	 * @return base fuel consumption in mb/tick
	 */
    int getFuelConsumptionRate(World world, int x, int y, int z);
}
