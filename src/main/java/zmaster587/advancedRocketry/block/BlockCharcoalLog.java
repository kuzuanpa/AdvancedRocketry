package zmaster587.advancedRocketry.block;

import java.util.Random;

import net.minecraft.block.BlockLog;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.common.util.ForgeDirection;
import org.jetbrains.annotations.NotNull;

public class BlockCharcoalLog extends BlockLog {

	IIcon topIcon, sideIcon;
	
	public BlockCharcoalLog() {
		super();
	}
	
	@Override
	public void registerBlockIcons(@NotNull IIconRegister icon) {
		topIcon = icon.registerIcon("advancedrocketry:log_charcoal_top");
		sideIcon = icon.registerIcon("advancedrocketry:log_charcoal");
	}
	
	@Override
	public int getFlammability(IBlockAccess world, int x, int y, int z,
			ForgeDirection face) {
		return 0;
	}
	
	@Override
	protected IIcon getTopIcon(int p_150161_1_) {
		return topIcon;
	}
	
	@Override
	protected IIcon getSideIcon(int p_150163_1_) {
		return sideIcon;
	}
	
	@Override
	public Item getItemDropped(int p_149650_1_, Random p_149650_2_,
			int p_149650_3_) {
		return Items.coal;
	}
	
	@Override
	public int damageDropped(int p_149692_1_) {
		return 1;
	}
	
    public int quantityDroppedWithBonus(int i, Random rand)
    {
        return this.quantityDropped(rand) + (i > 0 ? rand.nextInt(i) : 0);
    }
}
