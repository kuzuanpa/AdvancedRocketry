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

	/**
	 * In the space dimension the player's own coordinates are the simulation's coordinates - that is the whole
	 * point of the dimension - so the sky is drawn straight off the camera position.
	 */
	@Override
	public Vector3F<Double> getViewpoint(float partialTicks, EntityPlayer player){
		if(player == null) return null;
		double px = player.prevPosX + (player.posX - player.prevPosX) * partialTicks;
		double py = player.prevPosY + (player.posY - player.prevPosY) * partialTicks;
		double pz = player.prevPosZ + (player.posZ - player.prevPosZ) * partialTicks;
		return new Vector3F<>(px, py, pz);
	}
}
