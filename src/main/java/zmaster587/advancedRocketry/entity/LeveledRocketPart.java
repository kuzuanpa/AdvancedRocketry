package zmaster587.advancedRocketry.entity;

import net.minecraft.nbt.NBTTagCompound;
import org.jetbrains.annotations.NotNull;
import zmaster587.advancedRocketry.util.StorageChunk;

public class LeveledRocketPart {
    public final StorageChunk storage;
    public final long fuelRemaining;
    public final boolean isActived;
    public final int level;

    public LeveledRocketPart(StorageChunk storage, long fuelRemaining, boolean isActived, int level) {
         this.storage =storage;
        this.fuelRemaining =fuelRemaining;
        this.isActived =isActived;
        this.level =level;
    }
    public NBTTagCompound writeToNBT() {
        NBTTagCompound nbt = new NBTTagCompound();
        nbt.setLong("fuelRemaining", fuelRemaining);
        nbt.setBoolean("isActived", isActived);
        nbt.setInteger("level", level);
        storage.writeToNBT(nbt);
        return nbt;
    }
    public static LeveledRocketPart readFromNBT(@NotNull NBTTagCompound nbt) {
        long fuelRemaining = nbt.getLong("fuelRemaining");
        boolean isActived = nbt.getBoolean("isActived");
        int level = nbt.getInteger("level");
        StorageChunk storage= new StorageChunk();
        storage.readFromNBT(nbt);
        return new LeveledRocketPart(storage,fuelRemaining, isActived, level);
    }
}
