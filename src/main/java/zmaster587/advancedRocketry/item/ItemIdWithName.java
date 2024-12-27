package zmaster587.advancedRocketry.item;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumChatFormatting;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class ItemIdWithName extends Item {
	
	public void setName(ItemStack stack, String name) {

		if(stack.hasTagCompound()) {
			NBTTagCompound nbt = stack.getTagCompound();
			nbt.setString("name", name);
			stack.setTagCompound(nbt);
		}
	}

	public String getName(ItemStack stack) {
		if(stack.hasTagCompound()) {
			NBTTagCompound nbt = stack.getTagCompound();
			return nbt.getString("name");
		}

		return "";
	}
	
	
	@Override
	public void addInformation(ItemStack stack, EntityPlayer player,
                               @NotNull List list, boolean bool) {
		if(stack.getItemDamage() == -1) {
			list.add(EnumChatFormatting.GRAY + "Unprogrammed");
		}
		else {
			list.add(getName(stack));
		}
	}
}
