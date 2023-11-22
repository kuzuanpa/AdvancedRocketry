package zmaster587.advancedRocketry.util;

import net.minecraft.entity.EntityLiving;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.nbt.NBTException;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.biome.BiomeGenBase;
import org.w3c.dom.DOMException;

public class SpawnListEntryNBT extends BiomeGenBase.SpawnListEntry {

    NBTTagCompound nbt;
    String nbtString;

    public SpawnListEntryNBT(Class<? extends EntityLiving> entityclassIn, int weight, int groupCountMin,
                             int groupCountMax) {
        super(entityclassIn, weight, groupCountMin, groupCountMax);
        nbt = null;
        nbtString = "";
    }

    public void setNbt(String nbtString) throws DOMException, NBTException {

        this.nbtString = nbtString;
        if(nbtString.isEmpty())
            this.nbt = null;
        else
            this.nbt = (NBTTagCompound) JsonToNBT.func_150315_a(nbtString);
    }

    public String getNBTString() {
        return this.nbtString;
    }

}