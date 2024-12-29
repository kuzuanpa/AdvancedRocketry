package zmaster587.advancedRocketry.block;

import net.minecraft.block.Block;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import zmaster587.libVulpes.block.BlockTile;

public class BlockSuitWorkstation extends BlockTile {

	public BlockSuitWorkstation(Class<? extends TileEntity> tileClass, int guiId) {
		super(tileClass, guiId);
	}

	@Override
	public void breakBlock(World world, int x, int y, int z, Block block,
			int meta)
	{
		TileEntity tile = world.getTileEntity(x,y,z);

		//This code could use some optimization -Dark
		if (tile instanceof IInventory)
		{
			IInventory inventory = (IInventory)tile;
			int i1 = 0;
			ItemStack itemstack = inventory.getStackInSlot(i1);

			if (itemstack != null)
			{
				float f = world.rand.nextFloat() * 0.8F + 0.1F;
				float f1 = world.rand.nextFloat() * 0.8F + 0.1F;
				EntityItem entityitem;

				for (float f2 = world.rand.nextFloat() * 0.8F + 0.1F; itemstack.stackSize > 0; world.spawnEntityInWorld(entityitem))
				{
					int j1 = world.rand.nextInt(21) + 10;

					if (j1 > itemstack.stackSize)
					{
						j1 = itemstack.stackSize;
					}

					itemstack.stackSize -= j1;
					entityitem = new EntityItem(world, x + f, y + f1, z + f2, new ItemStack(itemstack.getItem(), j1, itemstack.getItemDamage()));
					float f3 = 0.05F;
					entityitem.motionX = (float)world.rand.nextGaussian() * f3;
					entityitem.motionY = (float)world.rand.nextGaussian() * f3 + 0.2F;
					entityitem.motionZ = (float)world.rand.nextGaussian() * f3;

					if (itemstack.hasTagCompound())
					{
						entityitem.getEntityItem().setTagCompound((NBTTagCompound)itemstack.getTagCompound().copy());
					}
				}
			}
		}

		world.removeTileEntity(x,y,z);

	}
}
