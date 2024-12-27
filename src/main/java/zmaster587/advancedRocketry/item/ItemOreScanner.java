package zmaster587.advancedRocketry.item;

import java.util.LinkedList;
import java.util.List;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import zmaster587.advancedRocketry.AdvancedRocketry;
import zmaster587.advancedRocketry.api.satellite.SatelliteBase;
import zmaster587.advancedRocketry.dimension.DimensionManager;
import zmaster587.advancedRocketry.inventory.GuiHandler;
import zmaster587.advancedRocketry.inventory.modules.ModuleOreMapper;
import zmaster587.advancedRocketry.satellite.SatelliteOreMapping;
import zmaster587.libVulpes.LibVulpes;
import zmaster587.libVulpes.inventory.modules.IModularInventory;
import zmaster587.libVulpes.inventory.modules.ModuleBase;

public class ItemOreScanner extends Item implements IModularInventory {


	@Override
	public void addInformation(@NotNull ItemStack stack, EntityPlayer player,
							   @NotNull List list, boolean arg5) {
		
		SatelliteBase sat = DimensionManager.getInstance().getSatellite(this.getSatelliteID(stack));
		
		SatelliteOreMapping mapping = null;
		if(sat instanceof SatelliteOreMapping)
			mapping = (SatelliteOreMapping)sat;
		
		if(!stack.hasTagCompound())
			list.add(LibVulpes.proxy.getLocalizedString("msg.unprogrammed"));
		else if(mapping == null)
			list.add(LibVulpes.proxy.getLocalizedString("msg.itemorescanner.nosat"));
		else if(mapping.getDimensionId() == player.worldObj.provider.dimensionId) {
			list.add(LibVulpes.proxy.getLocalizedString("msg.connected"));
			list.add(LibVulpes.proxy.getLocalizedString("msg.maxzoom") + mapping.getZoomRadius());
			list.add(LibVulpes.proxy.getLocalizedString("msg.filter") + mapping.canFilterOre());
		}
		else
			list.add(LibVulpes.proxy.getLocalizedString("msg.notconnected"));

		super.addInformation(stack, player, list, arg5);
	}
	
	public void setSatelliteID(ItemStack stack, long id) {
		NBTTagCompound nbt;
		if(!stack.hasTagCompound())
			nbt = new NBTTagCompound();
		else
			nbt = stack.getTagCompound();
		
		nbt.setLong("id", id);
		stack.setTagCompound(nbt);
	}

	public long getSatelliteID(ItemStack stack) {
		NBTTagCompound nbt;
		if(!stack.hasTagCompound())
			return -1;
		
		nbt = stack.getTagCompound();
		
		return nbt.getLong("id");
	}
	
	@Override
	public ItemStack onItemRightClick(@NotNull ItemStack stack, World world,
									  EntityPlayer player) {

		if(!player.worldObj.isRemote)
			player.openGui(AdvancedRocketry.instance, GuiHandler.guiId.OreMappingSatellite.ordinal(), world, (int)player.posX, (int)getSatelliteID(stack), (int)player.posZ);

		return super.onItemRightClick(stack, world, player);
	}

	@Override
	public boolean onItemUse(ItemStack stack, EntityPlayer player,World world, int x, int y, int z,int p_77648_7_, float p_77648_8_, float p_77648_9_, float p_77648_10_) {

		if(!player.worldObj.isRemote)
			player.openGui(AdvancedRocketry.instance, GuiHandler.guiId.OreMappingSatellite.ordinal(), world, x, (int)getSatelliteID(stack), z);


		return super.onItemUse(stack, player, world, x, y,z, p_77648_7_, p_77648_8_, p_77648_9_, p_77648_10_);
	}


	public void interactSatellite(@NotNull SatelliteBase satellite, EntityPlayer player, World world, int x, int y, int z) {
		satellite.performAction(player, world,x,y,z);
	}

	@Override
	public List<ModuleBase> getModules(int id, EntityPlayer player) {
		List<ModuleBase> modules = new LinkedList<>();
		modules.add(new ModuleOreMapper(0, 0));
		return modules;
	}

	@Override
	public @Nullable String getModularInventoryName() {
		return null;
	}

	@Override
	public boolean canInteractWithContainer(EntityPlayer entity) {
		return true;
	}

}
