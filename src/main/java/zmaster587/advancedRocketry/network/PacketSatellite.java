package zmaster587.advancedRocketry.network;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import zmaster587.advancedRocketry.AdvancedRocketry;
import zmaster587.advancedRocketry.api.SatelliteRegistry;
import zmaster587.advancedRocketry.api.satellite.SatelliteBase;
import zmaster587.libVulpes.network.BasePacket;

import java.io.IOException;

public class PacketSatellite extends BasePacket {

	SatelliteBase machine;

	final NBTTagCompound nbt;

	byte packetId;

	public PacketSatellite() {
		nbt = new NBTTagCompound();
	}

    public PacketSatellite(SatelliteBase machine) {
		this();
		this.machine = machine;
	}


	@Override
	public void write(ByteBuf outline) {
		PacketBuffer packetBuffer = new PacketBuffer(outline);
		NBTTagCompound nbt = new NBTTagCompound();
		machine.writeToNBT(nbt);
		
		try {
			packetBuffer.writeNBTTagCompoundToBuffer(nbt);
		} catch (IOException e) {
			AdvancedRocketry.logger.error(e);
		}
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void readClient(ByteBuf in) {
		
		PacketBuffer packetBuffer = new PacketBuffer(in);
		NBTTagCompound nbt;
		
		//TODO: error handling
		try {
			nbt = packetBuffer.readNBTTagCompoundFromBuffer();
			SatelliteBase satellite = SatelliteRegistry.createFromNBT(nbt);
			
			zmaster587.advancedRocketry.dimension.DimensionManager.getInstance().getDimensionProperties(satellite.getDimensionId()).addSatallite(satellite);
		} catch (IOException e) {
			AdvancedRocketry.logger.error(e);
        }
	}

	@Override
	public void read(ByteBuf in) {
		//Should never happen
		
	}

	public void executeClient(EntityPlayer player) {
	}

	public void executeServer(EntityPlayerMP player) {
	}

	public void execute(EntityPlayer player, Side side) {
	}

}
