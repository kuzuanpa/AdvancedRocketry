package zmaster587.advancedRocketry.util;

import net.minecraft.block.Block;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import org.jetbrains.annotations.NotNull;
import zmaster587.advancedRocketry.AdvancedRocketry;
import zmaster587.advancedRocketry.api.AdvancedRocketryBlocks;
import zmaster587.advancedRocketry.api.AreaBlob;
import zmaster587.advancedRocketry.api.Configuration;
import zmaster587.advancedRocketry.api.util.IBlobHandler;
import zmaster587.advancedRocketry.atmosphere.AtmosphereHandler;
import zmaster587.advancedRocketry.network.PacketAirParticle;
import zmaster587.libVulpes.network.PacketHandler;
import zmaster587.libVulpes.util.BlockPosition;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Stack;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class AtmosphereBlob extends AreaBlob implements Runnable {


	static ThreadPoolExecutor pool = (Configuration.atmosphereHandleBitMask & 1) == 1 ? new ThreadPoolExecutor(3, 16, 60, TimeUnit.SECONDS, new LinkedBlockingQueue<>(32)) : null;

	boolean executing;
	BlockPosition blockPos;
	List<AreaBlob> nearbyBlobs;

	public AtmosphereBlob(IBlobHandler blobHandler) {
		super(blobHandler);
		executing = false;
	}

	@Override
	public void removeBlock(int x, int y, int z) {
		BlockPosition blockPos = new BlockPosition(x, y, z);
		synchronized (graph) {
			graph.remove(blockPos);
			graph.contains(blockPos);


			for(ForgeDirection direction : ForgeDirection.VALID_DIRECTIONS) {

				BlockPosition newBlock = blockPos.getPositionAtOffset(direction.offsetX, direction.offsetY, direction.offsetZ);
				if(graph.contains(newBlock) && !graph.doesPathExist(newBlock, blobHandler.getRootPosition()))
					runEffectOnWorldBlocks(blobHandler.getWorld(), graph.removeAllNodesConnectedTo(newBlock));
			}
		}
	}

	@Override
	public boolean isPositionAllowed(World world, BlockPosition pos,  List<AreaBlob> otherBlobs) {
		for(@NotNull AreaBlob blob : otherBlobs) {
			if(blob.contains(pos) && blob != this)
				return false;
		}

		return !SealableBlockHandler.INSTANCE.isBlockSealed(world, pos);
	}

	@Override
	public void addBlock(@NotNull BlockPosition blockPos, List<AreaBlob> otherBlobs) {

		if(blobHandler.canFormBlob()) {

			if(!this.contains(blockPos) &&
					(this.graph.size() == 0 || contains(blockPos.getPositionAtOffset(0, 1, 0)) || contains(blockPos.getPositionAtOffset(0, -1, 0)) ||
							contains(blockPos.getPositionAtOffset(1, 0, 0)) || contains(blockPos.getPositionAtOffset(-1, 0, 0)) ||
							contains(blockPos.getPositionAtOffset(0, 0, 1)) || contains(blockPos.getPositionAtOffset(0, 0, -1)))) {
				if(!executing) {
					this.nearbyBlobs = otherBlobs;
					this.blockPos = blockPos;
					executing = true;
					if((Configuration.atmosphereHandleBitMask & 1) == 1)
						try {
							pool.execute(this);
						} catch (RejectedExecutionException e) {
							AdvancedRocketry.logger.warn("Atmosphere calculation at " + this.getRootPosition() + " aborted due to oversize queue!");
						}
					else
						this.run();
				}
			}
		}
	}	


	@Override
	public void run() {

		Stack<BlockPosition> stack = new Stack<>();
		stack.push(blockPos);

		final int maxSize = (Configuration.atmosphereHandleBitMask & 2) != 0 ? (int)(Math.pow(this.getBlobMaxRadius(), 3)*((4f/3f)*Math.PI)) : this.getBlobMaxRadius();
		final HashSet<BlockPosition> addableBlocks = new HashSet<>();

		//Breadth first search; non recursive
		while(!stack.isEmpty()) {
			BlockPosition stackElement = stack.pop();
			addableBlocks.add(stackElement);

			for(ForgeDirection dir2 : ForgeDirection.VALID_DIRECTIONS) {
				BlockPosition searchNextPosition = stackElement.getPositionAtOffset(dir2.offsetX, dir2.offsetY, dir2.offsetZ);

				//Don't path areas we have already scanned
				if(!graph.contains(searchNextPosition) && !addableBlocks.contains(searchNextPosition)) {

					boolean sealed;

					try {

						sealed = !isPositionAllowed(blobHandler.getWorld(), searchNextPosition, nearbyBlobs);//SealableBlockHandler.INSTANCE.isBlockSealed(blobHandler.getWorld(), searchNextPosition);

						if(blobHandler.getTraceDistance() > 0 && blobHandler.getWorld().getTotalWorldTime() % 20 == 0) {
							if((int)searchNextPosition.getDistance(this.getRootPosition()) == blobHandler.getTraceDistance())	{
								PacketHandler.sendToNearby(new PacketAirParticle(searchNextPosition), blobHandler.getWorld().provider.dimensionId, blobHandler.getRootPosition().x,blobHandler.getRootPosition().y, blobHandler.getRootPosition().z, 128);
							}
								
						}
						

						if(!sealed) {
							if(((Configuration.atmosphereHandleBitMask & 2) == 0 && searchNextPosition.getDistance(this.getRootPosition()) <= maxSize) ||
									((Configuration.atmosphereHandleBitMask & 2) != 0 && addableBlocks.size() <= maxSize)) {
								stack.push(searchNextPosition);
								addableBlocks.add(searchNextPosition);
							}
							else {
								//Failed to seal, void
								clearBlob();
								executing = false;
								return;
							}
						}
					} catch (Exception e){
						//Catches errors with additional information
						AdvancedRocketry.logger.info("Error: AtmosphereBlob has failed to form correctly due to an error. \nCurrentBlock: " + stackElement + "\tNextPos: " + searchNextPosition + "\tDir: " + dir2 + "\tStackSize: " + stack.size());
						e.printStackTrace();
						//Failed to seal, void
						clearBlob();
						executing = false;
						return;
					}
				}
			}
		}

		//only one instance can editing this at a time because this will not run again b/c "worker" is not null

		synchronized(graph) {
			for(BlockPosition blockPos2 : addableBlocks) {
				super.addBlock(blockPos2, nearbyBlobs);
			}
		}
		executing = false;
	}


	/**
	 * @param world
	 * @param blocks Collection containing affected locations
	 */
	protected void runEffectOnWorldBlocks(World world, Collection<BlockPosition> blocks) {
		if(!AtmosphereHandler.getOxygenHandler(world.provider.dimensionId).getDefaultAtmosphereType().allowsCombustion()) {
			List<BlockPosition> list;

			synchronized (graph) {
				list = new LinkedList<>(blocks);
			}

			for(BlockPosition pos : list) {
				Block block = world.getBlock(pos.x, pos.y, pos.z);
				if(block== Blocks.torch) {
					world.setBlock(pos.x, pos.y, pos.z, AdvancedRocketryBlocks.blockUnlitTorch);
				}
				else if(Configuration.torchBlocks.contains(block)) {
					EntityItem item = new EntityItem(world, pos.x, pos.y, pos.z, new ItemStack(block));
					world.setBlockToAir(pos.x, pos.y, pos.z);
					world.spawnEntityInWorld(item);
				}
			}
		}
	}

	@Override
	public void clearBlob() {
		World world = blobHandler.getWorld();

		runEffectOnWorldBlocks(world, getLocations());

		super.clearBlob();
	}

	public int getPressure() {
		return 100;
	}
}
