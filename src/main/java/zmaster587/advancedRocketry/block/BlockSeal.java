package zmaster587.advancedRocketry.block;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import org.jetbrains.annotations.NotNull;
import zmaster587.advancedRocketry.api.AreaBlob;
import zmaster587.advancedRocketry.api.util.IBlobHandler;
import zmaster587.advancedRocketry.atmosphere.AtmosphereHandler;
import zmaster587.advancedRocketry.tile.oxygen.TileSeal;
import zmaster587.libVulpes.util.BlockPosition;

import java.util.HashMap;
import java.util.LinkedList;

public class BlockSeal extends Block {

	private final HashMap<BlockPosition,BlobHandler> blobList = new HashMap<>();
	
	public BlockSeal(@NotNull Material materialIn) {
		super(materialIn);
	}
	
	public void clearMap() {
		blobList.clear();
	}
	
	@Override
	public boolean hasTileEntity(int meta) {
		return true;
	}
	
	@Override
	public TileEntity createTileEntity(World world, int meta) {
		return new TileSeal();
	}
	
	@Override
	public void breakBlock(World worldIn, int x, int y, int z, Block block, int meta) {
		super.breakBlock(worldIn, x, y, z, block, meta);
		
		AtmosphereHandler atmhandler = AtmosphereHandler.getOxygenHandler(worldIn.provider.dimensionId);
		if(atmhandler == null)
			return;
		
		for(@NotNull ForgeDirection dir : ForgeDirection.VALID_DIRECTIONS) {
			BlobHandler handler = blobList.remove(new BlockPosition(x + dir.offsetX, y + dir.offsetY, z + dir.offsetZ));
			if (handler != null) atmhandler.unregisterBlob(handler);
			
			fireCheckAllDirections(worldIn, x + dir.offsetX, y + dir.offsetY, z + dir.offsetZ, dir);
		}
	}
	
	
	public void clearBlob(World worldIn, int x, int y, int z) {
		
		AtmosphereHandler atmhandler = AtmosphereHandler.getOxygenHandler(worldIn.provider.dimensionId);
		if(atmhandler == null)
			return;
		
		for(ForgeDirection dir : ForgeDirection.VALID_DIRECTIONS) {
			BlobHandler handler = blobList.remove(new BlockPosition(x + dir.offsetX, y + dir.offsetY, z + dir.offsetZ));
			if (handler != null) atmhandler.unregisterBlob(handler);
		}
	}

	
	@Override
	public void onBlockAdded(World worldIn, int x, int y, int z) {
		super.onBlockAdded(worldIn,x,y,z);

		checkCompleteness(worldIn, x,y,z);
		
		for(ForgeDirection dir : ForgeDirection.VALID_DIRECTIONS) {
			fireCheckAllDirections(worldIn, x + dir.offsetX, y + dir.offsetY, z + dir.offsetZ, dir);
		}
	}
	
	public void fireCheckAllDirections(World worldIn, int x, int y, int z, ForgeDirection directionFrom) {
		for(ForgeDirection dir : ForgeDirection.VALID_DIRECTIONS) {
			if(directionFrom.getOpposite() != dir)
				fireCheck(worldIn, x + dir.offsetX, y + dir.offsetY, z + dir.offsetZ);
		}
	}
	
	private void fireCheck(World worldIn, int x, int y, int z) {
		Block block = worldIn.getBlock(x,y,z);
		if(block == this) {
			BlockSeal blockSeal = (BlockSeal)block;
			blockSeal.checkCompleteness(worldIn, x,y,z);
		}
	}

	private boolean checkCompleteness(World worldIn, int x, int y, int z) {
		AtmosphereHandler atmhandler = AtmosphereHandler.getOxygenHandler(worldIn.provider.dimensionId);
		if(atmhandler == null)
			return false;
		
		// check along XY axis
		if(((worldIn.getBlock(x - 1, y + 1, z) == this && 
				worldIn.getBlock(x + 1, y + 1, z) == this &&
				worldIn.getBlock(x, y + 2, z) == this) ||
				
				(worldIn.getBlock(x, y + 1, z + 1) == this && 
				worldIn.getBlock(x, y + 1, z - 1) == this &&
				worldIn.getBlock(x, y + 2, z) == this)) &&
				!blobList.containsKey(new BlockPosition(x, y + 1,z))) {
			
			y++;
			BlockPosition hashPos = new BlockPosition(x,y,z);
			BlobHandler handler = new BlobHandler(worldIn, hashPos);
			blobList.put(hashPos, handler);
			
			AreaBlob blob = new AreaBlob(handler);
			blob.addBlock(hashPos, new LinkedList<>());
			atmhandler.registerBlob(handler, x, y, z, blob);
			
			return true;
		}
		
		// check along XZ axis
		if(worldIn.getBlock(x + 1, y, z + 1) == this && 
				worldIn.getBlock(x + 1, y, z - 1) == this &&
				worldIn.getBlock(x + 2, y, z) == this &&
				!blobList.containsKey(new BlockPosition(x + 1, y, z))) {
			
			x++;
			BlockPosition hashPos = new BlockPosition(x,y,z);
			BlobHandler handler = new BlobHandler(worldIn, hashPos);
			blobList.put(hashPos, handler);
			
			@NotNull AreaBlob blob = new AreaBlob(handler);
			blob.addBlock(hashPos, new LinkedList<>());
			atmhandler.registerBlob(handler, x,y,z, blob);
			return true;
		}
		return false;
	}
	
	private static class BlobHandler implements IBlobHandler {
		
		final World world;
		final BlockPosition pos;
		
		public BlobHandler(World world, BlockPosition pos) {
			this.world = world;
			this.pos = pos;
		}
		
		@Override
		public boolean canFormBlob() {
			return true;
		}
		@Override
		public World getWorld() {
			return world;
		}

		@Override
		public boolean canBlobsOverlap(BlockPosition blockPosition, AreaBlob blob) {
			return false;
		}

		@Override
		public int getMaxBlobRadius() {
			return 0;
		}

		@Override
		public BlockPosition getRootPosition() {
			return pos;
		}
		
		@Override
		public int getTraceDistance() {
			return -1;
		}
	}
}
