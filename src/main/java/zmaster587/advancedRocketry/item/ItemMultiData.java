package zmaster587.advancedRocketry.item;

import java.util.List;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.ForgeDirection;
import org.jetbrains.annotations.NotNull;
import zmaster587.advancedRocketry.api.DataStorage;
import zmaster587.advancedRocketry.world.util.MultiData;

public class ItemMultiData extends Item {

	public ItemMultiData() {
		super();
	}

	public void setMaxData(ItemStack stack, int amount) {
		MultiData data = getDataStorage(stack);
		data.setMaxData(amount);

		NBTTagCompound nbt;

		if(!stack.hasTagCompound()) {
			nbt= new NBTTagCompound();
		}
		else
			nbt = stack.getTagCompound();
		data.writeToNBT(nbt);
		stack.setTagCompound(nbt);
	}

	public int getData(ItemStack stack, DataStorage.DataType type) {
		return getDataStorage(stack).getDataAmount(type);
	}
	
	public int getMaxData(@NotNull ItemStack stack) {
		return getDataStorage(stack).getMaxData();
	}

	private MultiData getDataStorage(ItemStack item) {

		MultiData data = new MultiData();

		if(!item.hasTagCompound()) {
			NBTTagCompound nbt = new NBTTagCompound();
			data.writeToNBT(nbt);
		}
		else
			data.readFromNBT(item.getTagCompound());

		return data;
	}

	public boolean isFull(ItemStack item,  DataStorage.DataType dataType) {
		return getDataStorage(item).getMaxData() == getData(item, dataType);
		
	}
	
	public int addData(ItemStack item, int amount, DataStorage.DataType dataType) {
		MultiData data = getDataStorage(item);

		int amt = data.addData(amount, dataType, ForgeDirection.UNKNOWN,true);

		NBTTagCompound nbt;
		if(item.hasTagCompound())
			nbt = item.getTagCompound();
		else
			nbt = new NBTTagCompound();
		
		data.writeToNBT(nbt);
		item.setTagCompound(nbt);

		return amt;
	}

	public int removeData(@NotNull ItemStack item, int amount, DataStorage.DataType dataType) {
		MultiData data = getDataStorage(item);

		int amt = data.extractData(amount, dataType, ForgeDirection.UNKNOWN, true);
		
		NBTTagCompound nbt;
		if(item.hasTagCompound())
			nbt = item.getTagCompound();
		else
			nbt = new NBTTagCompound();
		
		data.writeToNBT(nbt);
		item.setTagCompound(nbt);

		return amt;
	}

	public void setData(ItemStack item, int amount, DataStorage.DataType dataType) {
		@NotNull MultiData data = getDataStorage(item);

		data.setDataAmount(amount, dataType);

		NBTTagCompound nbt;
		if(item.hasTagCompound())
			nbt = item.getTagCompound();
		else
			nbt = new NBTTagCompound();
		
		data.writeToNBT(nbt);
		item.setTagCompound(nbt);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack stack, EntityPlayer player,
			List list, boolean bool) {
		super.addInformation(stack, player, list, bool);

		MultiData data = getDataStorage(stack);

		for(DataStorage.DataType type : DataStorage.DataType.values()) {
			if(type != DataStorage.DataType.UNDEFINED)
				list.add(data.getDataAmount(type) + " / " + data.getMaxData() + " " + I18n.format(type.toString(), new Object[0]) + " Data");
		}
	}
}
