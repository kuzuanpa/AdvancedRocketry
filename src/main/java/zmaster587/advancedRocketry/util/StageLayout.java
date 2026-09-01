package zmaster587.advancedRocketry.util;

import net.minecraft.nbt.NBTTagCompound;
import org.jetbrains.annotations.NotNull;

/**
 * Which stage each block of a rocket belongs to.
 *
 * Stored as one byte per block using the same addressing as {@link StorageChunk#writeToNBT}:
 * {@code z + sizeZ*y + sizeZ*sizeY*x}. The value is {@code level + 1} so that 0 means "no stage",
 * which covers both air and blocks not connected to the guidance computer.
 *
 * Level 0 is the group holding the guidance computer; higher levels sit further down the rocket and
 * separate first.
 */
public class StageLayout {

	public static final @NotNull StageLayout EMPTY = new StageLayout(new byte[0], 0, 0, 0, 0);

	public final byte[] levels;
	public final int maxLevel;
	public final int sizeX, sizeY, sizeZ;

	public StageLayout(byte[] levels, int maxLevel, int sizeX, int sizeY, int sizeZ) {
		this.levels = levels;
		this.maxLevel = maxLevel;
		this.sizeX = sizeX;
		this.sizeY = sizeY;
		this.sizeZ = sizeZ;
	}

	public boolean isEmpty() {
		return levels.length == 0;
	}

	/**@return the stage level at the given local coordinates, or -1 if the block belongs to no stage*/
	public int getLevel(int x, int y, int z) {
		if(x < 0 || x >= sizeX || y < 0 || y >= sizeY || z < 0 || z >= sizeZ)
			return -1;

		return levels[z + (sizeZ*y) + (sizeZ*sizeY*x)] - 1;
	}

	/**@return true if this layout describes a chunk of the given dimensions*/
	public boolean matches(int sizeX, int sizeY, int sizeZ) {
		return this.sizeX == sizeX && this.sizeY == sizeY && this.sizeZ == sizeZ && levels.length == sizeX*sizeY*sizeZ;
	}

	public void writeToNBT(@NotNull NBTTagCompound nbt) {
		nbt.setByteArray("stageLevels", levels);
		nbt.setInteger("stageMaxLevel", maxLevel);
		nbt.setInteger("stageSizeX", sizeX);
		nbt.setInteger("stageSizeY", sizeY);
		nbt.setInteger("stageSizeZ", sizeZ);
	}

	public static @NotNull StageLayout readFromNBT(@NotNull NBTTagCompound nbt) {
		if(!nbt.hasKey("stageLevels"))
			return EMPTY;

		return new StageLayout(nbt.getByteArray("stageLevels"), nbt.getInteger("stageMaxLevel"),
				nbt.getInteger("stageSizeX"), nbt.getInteger("stageSizeY"), nbt.getInteger("stageSizeZ"));
	}
}
