package zmaster587.advancedRocketry.block;

import java.util.Random;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import zmaster587.advancedRocketry.AdvancedRocketry;
import zmaster587.advancedRocketry.dimension.DimensionManager;
import zmaster587.advancedRocketry.tile.multiblock.TileBeacon;
import zmaster587.libVulpes.block.RotatableBlock;
import zmaster587.libVulpes.block.multiblock.BlockMultiblockMachine;
import zmaster587.libVulpes.tile.multiblock.TileMultiBlock;
import zmaster587.libVulpes.util.BlockPosition;

public class BlockBeacon extends BlockMultiblockMachine {

	public BlockBeacon(Class<? extends TileMultiBlock> tileClass, int guiId) {
		super(tileClass, guiId);
	}

	@Override
	public void breakBlock(World world, int x, int y, int z, Block block,
			int meta) {
		TileEntity tile = world.getTileEntity(x,y,z);
		if(tile instanceof TileBeacon && DimensionManager.getInstance().isDimensionCreated(world.provider.dimensionId)) {
			DimensionManager.getInstance().getDimensionProperties(world.provider.dimensionId).removeBeaconLocation(world,new BlockPosition(x,y,z));
		}
		super.breakBlock(world, x, y, z, block, meta);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void randomDisplayTick(World world, int x,
			int y, int z, Random random) {

		if(world.getTileEntity(x, y, z) instanceof TileBeacon && ((TileBeacon)world.getTileEntity(x, y, z)).getMachineEnabled()) {
			ForgeDirection dir = RotatableBlock.getFront(world.getBlockMetadata(x, y, z));
			for(int i = 0; i < 10; i++)
				AdvancedRocketry.proxy.spawnParticle("reddust", world,  x - dir.offsetX + random.nextDouble(), y + 5 - world.rand.nextDouble(), z - dir.offsetZ + random.nextDouble(), 0, 0, 0);
		}
	}   
}
