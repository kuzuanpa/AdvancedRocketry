package zmaster587.advancedRocketry.client.render.planet;

import net.minecraft.entity.player.EntityPlayer;
import zmaster587.libVulpes.util.Vector3F;

public class RenderSpaceSky extends RenderPlanetarySky {
	public RenderSpaceSky() {
		super();
	}

	@Override
	protected void rotateAroundAxis() {
	}

	public Vector3F<Double> getPlayerPos(float partialTicks, EntityPlayer player){
		double px = player.prevPosX + (player.posX - player.prevPosX) * partialTicks;
		double py = player.prevPosY + (player.posY - player.prevPosY) * partialTicks;
		double pz = player.prevPosZ + (player.posZ - player.prevPosZ) * partialTicks;
		return new Vector3F<>(px,py,pz);
	}
}
