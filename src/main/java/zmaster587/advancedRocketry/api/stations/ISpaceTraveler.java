package zmaster587.advancedRocketry.api.stations;

import cpw.mods.fml.relauncher.Side;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import zmaster587.libVulpes.interfaces.INetworkEntity;

public interface ISpaceTraveler extends INetworkEntity {
    public static final byte PACKET_ID = -30;
    public void travelTo(int dimID, int distance);

    @Override
    default void useNetworkData(EntityPlayer player, Side side, byte id, NBTTagCompound nbt){
        if(id == PACKET_ID){
            int targetId = nbt.getInteger("tId");
            int dist = nbt.getInteger("tDist");
            travelTo(targetId,dist);
        }
        useNetworkData2(player, side, id, nbt);
    }

    void useNetworkData2(EntityPlayer player, Side side, byte id, NBTTagCompound nbt);
}
