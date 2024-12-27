package zmaster587.advancedRocketry.inventory;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import zmaster587.advancedRocketry.api.satellite.SatelliteBase;
import zmaster587.advancedRocketry.dimension.DimensionManager;
import zmaster587.advancedRocketry.satellite.SatelliteOreMapping;
import zmaster587.libVulpes.inventory.modules.IModularInventory;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import cpw.mods.fml.common.network.IGuiHandler;

public class GuiHandler implements IGuiHandler {

	public enum guiId {
		RocketBuilder,
		BlastFurnace,
		OreMappingSatellite
	}

	//X coord is entity ID num if entity
	@Override
	public Object getServerGuiElement(int ID, EntityPlayer player, @NotNull World world,
                                      int x, int y, int z) {

		Object tile;

		if(y > -1)
			tile = world.getTileEntity(x, y, z);
		else if(x == -1) {
			ItemStack stack = player.getHeldItem();
			
			//If there is latency or some desync odd things can happen so check for that
			if(stack == null || !(stack.getItem() instanceof IModularInventory)) {
				return null;
			}
			
			tile = player.getHeldItem().getItem();
		}
		else
			tile = world.getEntityByID(x);

		if(ID == guiId.OreMappingSatellite.ordinal()) {
			SatelliteBase satellite = DimensionManager.getInstance().getSatellite(y);
			
			if(satellite == null || !(satellite instanceof SatelliteOreMapping) || satellite.getDimensionId() != world.provider.dimensionId)
				satellite = null;
			
			return new ContainerOreMappingSatallite((SatelliteOreMapping) satellite, player.inventory);
		}
		return null;
	}

	@Override
	public @Nullable Object getClientGuiElement(int ID, @NotNull EntityPlayer player, World world,
                                                int x, int y, int z) {

		Object tile;
		
		if(y > -1)
			tile = world.getTileEntity(x, y, z);
		else if(x == -1) {
			ItemStack stack = player.getHeldItem();
			
			//If there is latency or some desync odd things can happen so check for that
			if(stack == null || !(stack.getItem() instanceof IModularInventory)) {
				return null;
			}
			
			tile = player.getHeldItem().getItem();
		}
		else
			tile = world.getEntityByID(x);

		if(ID == guiId.OreMappingSatellite.ordinal()) {
			
			@Nullable SatelliteBase satellite = DimensionManager.getInstance().getSatellite(y);
			
			if(satellite == null || !(satellite instanceof SatelliteOreMapping) || satellite.getDimensionId() != world.provider.dimensionId)
				satellite = null;
			
			return new GuiOreMappingSatellite((SatelliteOreMapping) satellite, player);
		}
		return null;
	}
}