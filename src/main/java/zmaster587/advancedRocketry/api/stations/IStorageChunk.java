package zmaster587.advancedRocketry.api.stations;

import java.util.List;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

public interface IStorageChunk {
	/**
	 * Pastes the contents of the storage chunk into the world at the given coordinates
	 * @param world
	 * @param xCoord
	 * @param yCoord
	 * @param zCoord
	 */
    void pasteInWorld(World world, int xCoord, int yCoord, int zCoord);
	
	int getSizeX();
	int getSizeY();
	int getSizeZ();
	
	List<TileEntity> getTileEntityList();
	
	void rotateBy(ForgeDirection dir);
}
