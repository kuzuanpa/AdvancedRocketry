package zmaster587.advancedRocketry.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.Vec3;
import org.jetbrains.annotations.NotNull;
import zmaster587.advancedRocketry.AdvancedRocketry;
import zmaster587.libVulpes.network.BasePacket;

public class PacketLaserGun extends BasePacket {

	Entity fromEntity;
	Vec3 toPos;
	int entityId;

	public PacketLaserGun(Entity fireFrom, Vec3 toPos) {
		this.fromEntity = fireFrom;
		this.toPos = toPos;
	}

	public PacketLaserGun() {
	}

	@Override
	public void write(@NotNull ByteBuf out) {
		out.writeInt(fromEntity.getEntityId());
		out.writeFloat((float) toPos.xCoord);
		out.writeFloat((float) toPos.yCoord);
		out.writeFloat((float) toPos.zCoord);
	}

	@Override
	public void readClient(ByteBuf in) {
		entityId = in.readInt();
		toPos = Vec3.createVectorHelper(in.readFloat(), in.readFloat(), in.readFloat());
	}

	@Override
	public void read(ByteBuf in) {

	}

	@Override
	public void executeClient(EntityPlayer thePlayer) {
		Entity entity = thePlayer.worldObj.getEntityByID(entityId);
		if(entity != null) {
			AdvancedRocketry.proxy.spawnLaser(entity, toPos);
		}
	}

	@Override
	public void executeServer(EntityPlayerMP player) {

	}

}
