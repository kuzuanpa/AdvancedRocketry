package zmaster587.advancedRocketry.client.render.planet;

import net.minecraft.entity.Entity;
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
	 * In the space dimension the viewer's own coordinates are the simulation's coordinates - that is the whole
	 * point of the dimension - so the sky is drawn straight off them.
	 *
	 * A passenger is not at their vessel's origin: EntityRocket.updateRiderPosition offsets them by the seat
	 * position, which is several blocks up and off to one side.  Arrival is tested against the vessel, so the sky
	 * has to be drawn from the vessel too, or bodies appear a seat's worth away from where they can be reached.
	 */
	@Override
	public Vector3F<Double> getViewpoint(float partialTicks, EntityPlayer player){
		if(player == null) return null;

		Entity viewer = player;
		while(viewer.ridingEntity != null) viewer = viewer.ridingEntity;

		double px = viewer.prevPosX + (viewer.posX - viewer.prevPosX) * partialTicks;
		double py = viewer.prevPosY + (viewer.posY - viewer.prevPosY) * partialTicks;
		double pz = viewer.prevPosZ + (viewer.posZ - viewer.prevPosZ) * partialTicks;
		return new Vector3F<>(px, py, pz);
	}
}
