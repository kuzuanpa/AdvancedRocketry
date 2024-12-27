package zmaster587.advancedRocketry.network;

import java.io.IOException;

import zmaster587.advancedRocketry.api.atmosphere.AtmosphereRegister;
import zmaster587.advancedRocketry.atmosphere.AtmosphereHandler;
import zmaster587.libVulpes.network.BasePacket;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;

public class PacketAtmSync extends BasePacket {

	String type;
	int pressure;
	
	public PacketAtmSync(String type, int pressure) {
		this.type = type;
		this.pressure = pressure;
	}
	
	public PacketAtmSync() {
	}
	
	@Override
	public void write(ByteBuf out) {
		NBTTagCompound nbt = new NBTTagCompound();
		
		nbt.setString("type", type);
		nbt.setShort("pressure", (short)pressure);
		PacketBuffer packetBuffer = new PacketBuffer(out);
		
		try {
			packetBuffer.writeNBTTagCompoundToBuffer(nbt);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void readClient(ByteBuf in) {
		NBTTagCompound nbt = new NBTTagCompound();
		PacketBuffer packetBuffer = new PacketBuffer(in);
		
		try {
			nbt = packetBuffer.readNBTTagCompoundFromBuffer();
			type = nbt.getString("type");
			pressure = nbt.getShort("pressure");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void read(ByteBuf in) {
		//Do nothing on server, we don't want hackers now do we ;)
	}

	@Override
	public void executeClient(EntityPlayer thePlayer) {
		AtmosphereHandler.currentAtm = AtmosphereRegister.getInstance().getAtmosphere(type);
		AtmosphereHandler.currentPressure = pressure;
	}

	@Override
	public void executeServer(EntityPlayerMP player) {
		
	}

}
