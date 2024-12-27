package zmaster587.advancedRocketry.block;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import zmaster587.advancedRocketry.tile.station.TileLandingPad;
import zmaster587.libVulpes.LibVulpes;
import zmaster587.libVulpes.inventory.GuiHandler;

public class BlockLandingPad extends Block {

	public BlockLandingPad(Material mat) {
		super(mat);
	}
	
	@Override
	public boolean hasTileEntity(int metadata) {
		return true;
	}
	
	@Override
	public TileEntity createTileEntity(World world, int metadata) {
		return new TileLandingPad();
	}
	
	@Override
	public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int par6, float par7, float par8, float par9)
	{
		if(!world.isRemote)
			player.openGui(LibVulpes.instance, GuiHandler.guiId.MODULAR.ordinal(), world, x, y, z);
		return true;
	}


    @Override
	public void onBlockAdded(World world, int x,
			int y, int z) {
		super.onBlockAdded(world, x, y, z);
		
		TileEntity tile = world.getTileEntity(x, y, z);
		if(tile instanceof TileLandingPad) {
			((TileLandingPad) tile).registerTileWithStation(world, x, y, z);
		}
	}
	
	@Override
	public void onBlockPreDestroy(World world, int x,
			int y, int z, int oldMeta) {
		super.onBlockPreDestroy(world, x, y, z,
				oldMeta);
		
		TileEntity tile = world.getTileEntity(x, y, z);
		if(tile instanceof TileLandingPad) {
			((TileLandingPad) tile).unregisterTileWithStation(world, x, y, z);
		}
	}
}
