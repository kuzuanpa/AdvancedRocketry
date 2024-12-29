package zmaster587.advancedRocketry.api;

import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import zmaster587.advancedRocketry.api.util.IBlobHandler;
import zmaster587.libVulpes.util.AdjacencyGraph;
import zmaster587.libVulpes.util.BlockPosition;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class AreaBlob {
	//Graph containing the acutal area enclosed
	protected final AdjacencyGraph<BlockPosition> graph;
	//Object to call back to when events happen, usually a tileentity
	protected final IBlobHandler blobHandler;
	//Data stored by this blob
	@Nullable
	Object data;

	public AreaBlob(IBlobHandler blobHandler) {
		this.blobHandler = blobHandler;
		graph = new AdjacencyGraph<>();
		data = null;
	}

	public void setData(@Nullable Object obj) {
		data = obj;
	}
	
	public boolean isPositionAllowed(World world, BlockPosition pos,  List<AreaBlob> otherBlobs) {
		return true;
	}
	
	public @Nullable Object getData() {
		return data;
	}
	
	public int getBlobMaxRadius() {
		return blobHandler.getMaxBlobRadius();
	}
	
	/**
	 * Adds a block position to the blob
	 * @param x
	 * @param y
	 * @param z
	 */
	public void addBlock(int x, int y , int z, List<AreaBlob> otherBlobs) {
		BlockPosition blockPos = new BlockPosition(x, y, z);
		addBlock(blockPos, otherBlobs);
	}
	
	/**
	 * Adds a block to the graph
	 * @param blockPos block to add
	 */
	public void addBlock(@NotNull BlockPosition blockPos, List<AreaBlob> otherBlobs) {
		if(!graph.contains(blockPos) && blobHandler.canFormBlob()) {
			graph.add(blockPos, getPositionsToAdd(blockPos));
		}
	}
	
	/**
	 * @return the BlockPosition of the root of the blob
	 */
	public BlockPosition getRootPosition() {
		return blobHandler.getRootPosition();
	}
	
	/**
	 * Gets adjacent blocks if they exist in the blob
	 * @param blockPos block to find things adjacent to
	 * @return list containing valid adjacent blocks
	 */
	protected HashSet<BlockPosition> getPositionsToAdd(BlockPosition blockPos) {
		HashSet<BlockPosition> set = new HashSet<>();
		
		for(ForgeDirection direction : ForgeDirection.VALID_DIRECTIONS) {
			
			BlockPosition offset = blockPos.getPositionAtOffset(direction.offsetX, direction.offsetY, direction.offsetZ);
			if(graph.contains(offset))
				set.add(offset);
		}
		
		return set;
	}

	/**
	 * Given a block position returns whether or not it exists in the graph
	 * @return true if the block exists in the blob
	 */
	public boolean contains(BlockPosition position) {
		boolean contains;
		
		synchronized (graph) {
			contains = graph.contains(position);
		}
		return contains;
	}
	
	/**
	 * Given a block position returns whether or not it exists in the graph
	 * @param x
	 * @param y
	 * @param z
	 * @return true if the block exists in the blob
	 */
	public boolean contains(int x, int y, int z) {
		return contains(new BlockPosition(x, y, z));
	}

	/**
	 * Called when this blob is about to overlap another blob
	 * @param otherBlob other blob about to be overlapped
	 * @return true if this blob is allowed to overlap the otherBlob
	 */
	public boolean canBlobsOverlap(int x, int y, int z, AreaBlob otherBlob) {
		return blobHandler.canBlobsOverlap(new BlockPosition(x, y, z), otherBlob);
	}

	/**
	 * Removes the block at the given coords for this blob
	 * @param x
	 * @param y
	 * @param z
	 */
	public void removeBlock(int x, int y, int z) {
		@NotNull BlockPosition blockPos = new BlockPosition(x, y, z);
		graph.remove(blockPos);
		
		for(ForgeDirection direction : ForgeDirection.VALID_DIRECTIONS) {

			BlockPosition newBlock = blockPos.getPositionAtOffset(direction.offsetX, direction.offsetY, direction.offsetZ);
			if(graph.contains(newBlock) && !graph.doesPathExist(newBlock, blobHandler.getRootPosition()))
				graph.removeAllNodesConnectedTo(newBlock);
		}
		
	}
	
	/**
	 * Removes all nodes from the blob
	 */
	public void clearBlob() {
		graph.clear();
	}
	
	/**
	 * @return a set containing all locations
	 */
	public Set<BlockPosition> getLocations() {
		return graph.getKeys();
	}
	
	/**
	 * @return the number of elements in the blob
	 */
	public int getBlobSize() {
		return graph.size();
	}
}
