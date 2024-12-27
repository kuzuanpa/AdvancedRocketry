package zmaster587.advancedRocketry.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockPistonBase;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import zmaster587.libVulpes.interfaces.IRecipe;
import zmaster587.libVulpes.recipe.RecipesMachine;

import java.util.List;

public class BlockPress extends BlockPistonBase {

	public BlockPress() {
		super(false);
	}

	@Override
	public void onBlockAdded(World world, int x, int y, int z)
	{
		if (!world.isRemote && world.getTileEntity(x, y, z) == null)
		{
			this.updatePistonState(world, x, y, z);
		}
	}

	@Override
	public void onNeighborBlockChange(World world, int x, int y, int z, Block block)
	{
		if (!world.isRemote)
		{
			this.updatePistonState(world, x, y, z);
		}
	}

	@Override
	public void onBlockPlacedBy(World world, int x, int y, int z, EntityLivingBase entity, ItemStack stack)
	{
		int l = 0;
		world.setBlockMetadataWithNotify(x, y, z, l, 2);

		if (!world.isRemote)
		{
			this.updatePistonState(world, x, y, z);
		}
	}

	protected boolean isIndirectlyPowered(World world, int x, int y, int z, int p_150072_5_)
	{
		return p_150072_5_ != 0 && world.getIndirectPowerOutput(x, y - 1, z, 0)
				|| (p_150072_5_ != 1 && world.getIndirectPowerOutput(x, y + 1, z, 1)
				|| (p_150072_5_ != 2 && world.getIndirectPowerOutput(x, y, z - 1, 2)
				|| (p_150072_5_ != 3 && world.getIndirectPowerOutput(x, y, z + 1, 3)
				|| (p_150072_5_ != 5 && world.getIndirectPowerOutput(x + 1, y, z, 5)
				|| (p_150072_5_ != 4 && world.getIndirectPowerOutput(x - 1, y, z, 4)
				|| (world.getIndirectPowerOutput(x, y, z, 0)
				|| (world.getIndirectPowerOutput(x, y + 2, z, 1)
				|| (world.getIndirectPowerOutput(x, y + 1, z - 1, 2)
				|| (world.getIndirectPowerOutput(x, y + 1, z + 1, 3)
				|| (world.getIndirectPowerOutput(x - 1, y + 1, z, 4)
				||  world.getIndirectPowerOutput(x + 1, y + 1, z, 5)))))))))));
	}

	private ItemStack getRecipe(@NotNull World world, int x, int y, int z, int meta) {
		
		if(world.isAirBlock(x, y-1, z))
			return null;
		
		Item item = Item.getItemFromBlock(world.getBlock(x, y-1, z));
		if(item == null)
			return null;
		
		ItemStack stackInWorld = new ItemStack(item, 1, world.getBlockMetadata(x, y-1, z));

		List<IRecipe> recipes = RecipesMachine.getInstance().getRecipes(this.getClass());
		ItemStack stack = null;

		for(IRecipe recipe : recipes) {
			for(@NotNull ItemStack stack2 : recipe.getIngredients().get(0))
				if(stack2.isItemEqual(stackInWorld)) {
					stack = recipe.getOutput().get(0);
					break;
				}
		}


		if(world.getBlock(x, y-2, z) == Blocks.obsidian)
			return stack;

		return null;
	}

	protected void updatePistonState(World world, int x, int y, int z) {
		int l = world.getBlockMetadata(x, y, z);
		int i1 = 0;
			if (!isExtended(l))
			{
				ItemStack output;
				if (this.isIndirectlyPowered(world, x, y, z, i1) && (output = getRecipe(world, x, y, z, i1)) != null)
				{
					world.setBlock(x, y-1, z,Blocks.air,0, 1);
					if(!world.isRemote) {
						world.spawnEntityInWorld(new EntityItem(world, x, y-0.5f, z, output));
					}

					world.addBlockEvent(x, y, z, this, 0, i1);
				}
			}
			else
			{
				world.setBlockMetadataWithNotify(x, y, z, i1, 2);
			}

	}

}
