package zmaster587.advancedRocketry.enchant;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnumEnchantmentType;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public class EnchantmentSpaceBreathing extends Enchantment {
	   
	public EnchantmentSpaceBreathing(int id) {
		super(id, 0, EnumEnchantmentType.armor);
		this.setName("spaceBreathing");
	}
	
	@Override
	public boolean canApplyAtEnchantingTable(ItemStack stack) {
		return false;
	}

	@Override
	public boolean canApply(@Nullable ItemStack stack) {
		return stack != null && stack.getItem() instanceof ItemArmor;
	}
	
	@Override
	public int getMaxLevel() {
		return 1;
	}

	@Override
	public boolean isAllowedOnBooks() {
		return false;
	}
}
