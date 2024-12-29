package zmaster587.advancedRocketry.util;

import java.util.List;

import net.minecraft.item.ItemStack;

public interface ITilePlanetSystemSelectable {
	ItemStack getChipWithId(int id);
	
	void setSelectedPlanetId(int id);
	
	List<Integer> getVisiblePlanets();
}
