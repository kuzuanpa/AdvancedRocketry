package zmaster587.advancedRocketry.api;

import zmaster587.advancedRocketry.api.IInfrastructure;
import net.minecraft.world.World;

public interface IMission {
	
	/**
	 * Called when the misson is complete
	 */
    void onMissionComplete();

	/**
	 * @return Normallized progress of the the mission
	 */
    double getProgress(World world);
	
	/**
	 * @return Satellite ID of the mission
	 */
    long getMissionId();
	
	/**
	 * @return the dimension the mission originated from
	 */
    int getOriginatingDimention();

	void unlinkInfrastructure(IInfrastructure tile);

	int getTimeRemainingInSeconds();
}
