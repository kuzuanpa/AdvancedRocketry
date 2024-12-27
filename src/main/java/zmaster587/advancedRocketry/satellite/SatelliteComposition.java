package zmaster587.advancedRocketry.satellite;

import org.jetbrains.annotations.NotNull;
import zmaster587.advancedRocketry.api.DataStorage;

public class SatelliteComposition extends SatelliteData{
	
	public SatelliteComposition() {
		data = new DataStorage(DataStorage.DataType.COMPOSITION);
		data.lockDataType(DataStorage.DataType.COMPOSITION);
	}
	
	@Override
	public @NotNull String getName() {
		return "Composition Scanner";
	}

	@Override
	public double failureChance() {
		return 0;
	}
}
