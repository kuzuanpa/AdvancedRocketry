package zmaster587.advancedRocketry.satellite;

import org.jetbrains.annotations.NotNull;
import zmaster587.advancedRocketry.api.DataStorage;

public class SatelliteMassScanner extends SatelliteData {

	public SatelliteMassScanner() {
		data = new DataStorage(DataStorage.DataType.MASS);
		data.lockDataType(DataStorage.DataType.MASS);
	}
	
	@Override
	public @NotNull String getName() {
		return "Mass Scanner";
	}

	@Override
	public double failureChance() {
		return 0;
	}

}
