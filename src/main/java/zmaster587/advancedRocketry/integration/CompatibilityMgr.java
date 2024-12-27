package zmaster587.advancedRocketry.integration;

import cpw.mods.fml.common.Loader;

public class CompatibilityMgr {

	public static boolean gregtechLoaded;
	public static boolean thermalExpansion;
	public static boolean powerSuits;
	
	public CompatibilityMgr() {
		gregtechLoaded = false;
		thermalExpansion = false;
		powerSuits = false;
	}
	
	public static void getLoadedMods() {
		thermalExpansion = Loader.isModLoaded("ThermalExpansion");
		gregtechLoaded = Loader.isModLoaded("gregtech");
		powerSuits = Loader.isModLoaded("powersuits");
	}
	
	public static void initCompatRecipies() {
		if(gregtechLoaded) {

		}
	}
}
