package zmaster587.advancedRocketry.api;

import zmaster587.advancedRocketry.api.stations.ISpaceObject;

public interface ISpaceObjectManager {
	ISpaceObject getSpaceStationFromBlockCoords(int x, int z);
}
