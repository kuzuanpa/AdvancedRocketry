package zmaster587.advancedRocketry.api;

import net.minecraft.item.ItemStack;
import zmaster587.advancedRocketry.api.satellite.SatelliteProperties;

public interface ISatelliteIdItem {
	void setSatellite(ItemStack stack, SatelliteProperties properties);
}
