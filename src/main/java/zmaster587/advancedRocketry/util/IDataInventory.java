package zmaster587.advancedRocketry.util;

import zmaster587.advancedRocketry.api.satellite.IDataHandler;
import net.minecraft.inventory.IInventory;

public interface IDataInventory extends IInventory, IDataHandler {
	
	/**
	 * stores from external into this
	 */
    void loadData(int id);
	
	
	/**
	 * Stores in external
	 * @param storeTo IDataInventory to store data to
	 */
    void storeData(int id);
}
