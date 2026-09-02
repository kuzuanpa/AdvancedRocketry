package zmaster587.advancedRocketry.api.stations;

import cpw.mods.fml.relauncher.Side;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import zmaster587.libVulpes.interfaces.INetworkEntity;

/**
 * Something that can jump itself across the space dimension under its own power.
 *
 * Implemented by the rocket; a warp core is what makes the jump possible.  The dedicated packet id keeps the
 * request out of the implementor's own numbering, which is dense with button ids.
 */
public interface ISpaceTraveler extends INetworkEntity {

    byte PACKET_ID = -30;

    String NBT_TARGET = "tId";
    String NBT_DISTANCE = "tDist";

    /**
     * @param dimID dimension of the body to jump to
     * @param distance requested range, only a hint - the implementor recomputes it before charging for it
     */
    void travelTo(int dimID, int distance);

    @Override
    default void useNetworkData(EntityPlayer player, Side side, byte id, NBTTagCompound nbt){
        if(id == PACKET_ID){
            //Server side only: a client must not be able to move itself
            if(side == Side.SERVER && nbt != null)
                travelTo(nbt.getInteger(NBT_TARGET), nbt.getInteger(NBT_DISTANCE));
            return;
        }
        useNetworkData2(player, side, id, nbt);
    }

    void useNetworkData2(EntityPlayer player, Side side, byte id, NBTTagCompound nbt);
}
