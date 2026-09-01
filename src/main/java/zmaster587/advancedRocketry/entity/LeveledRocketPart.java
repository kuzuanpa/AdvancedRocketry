package zmaster587.advancedRocketry.entity;

import net.minecraft.nbt.NBTTagCompound;
import org.jetbrains.annotations.NotNull;
import zmaster587.advancedRocketry.util.StageLayout;

/**
 * The aggregate properties of one rocket stage.
 *
 * Deliberately holds no blocks: which blocks belong to this stage lives in {@link StageLayout}, and the
 * actual StorageChunk split happens only when the stage separates. Storing a chunk per stage would put a
 * full copy of the rocket in the entity's NBT for every stage.
 */
public class LeveledRocketPart {
    /**0 is the payload stage holding the guidance computer, higher levels separate first*/
    public final int level;
    public final int thrust;
    public final int fuelRate;
    public final int fuelCapacity;
    public final int blockCount;
    /**This stage separates once the rocket's remaining fuel drops to this amount*/
    public int separationThreshold;

    public LeveledRocketPart(int level, int thrust, int fuelRate, int fuelCapacity, int blockCount, int separationThreshold) {
        this.level = level;
        this.thrust = thrust;
        this.fuelRate = fuelRate;
        this.fuelCapacity = fuelCapacity;
        this.blockCount = blockCount;
        this.separationThreshold = separationThreshold;
    }

    public NBTTagCompound writeToNBT() {
        NBTTagCompound nbt = new NBTTagCompound();
        nbt.setInteger("level", level);
        nbt.setInteger("thrust", thrust);
        nbt.setInteger("fuelRate", fuelRate);
        nbt.setInteger("fuelCapacity", fuelCapacity);
        nbt.setInteger("blockCount", blockCount);
        nbt.setInteger("separationThreshold", separationThreshold);
        return nbt;
    }

    public static @NotNull LeveledRocketPart readFromNBT(@NotNull NBTTagCompound nbt) {
        return new LeveledRocketPart(nbt.getInteger("level"), nbt.getInteger("thrust"), nbt.getInteger("fuelRate"),
                nbt.getInteger("fuelCapacity"), nbt.getInteger("blockCount"), nbt.getInteger("separationThreshold"));
    }
}
