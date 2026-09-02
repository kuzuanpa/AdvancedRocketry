package zmaster587.advancedRocketry.dimension.sim;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import zmaster587.advancedRocketry.api.Configuration;
import zmaster587.advancedRocketry.dimension.DimensionManager;
import zmaster587.advancedRocketry.entity.EntityRocket;
import zmaster587.advancedRocketry.util.TeleportHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * Watches everything flying around the space dimension and drops it onto a body when it gets close enough.
 *
 * Bodies are not entities.  They are hundreds to thousands of blocks apart, far outside any entity tracking
 * range, so a proxy entity could never stay in sync with the client - the positions are recomputed identically
 * on both sides instead (see {@link SimUniverse}) and collision is a plain distance test done here.
 *
 * There is no gravity: a body is captured purely by flying into its capture sphere, which is
 * {@link SimScale#CAPTURE_MULTIPLIER} times the body's own radius.  Because a rocket can cover a block a tick
 * the test sweeps the whole path travelled since the last tick rather than just looking at where it ended up.
 */
public final class SpaceTravelHandler {

	private SpaceTravelHandler() {}

	/** Ticks a rocket may drift with no fuel and nothing in range before it is sent home */
	private static final int STRANDED_TIMEOUT = 20 * 90;

	/**
	 * Runs the capture test for every entity in the space dimension.
	 *
	 * @param world the space dimension, server side
	 */
	public static void tickWorld(World world) {
		if (world.isRemote || world.provider.dimensionId != Configuration.spaceDimId) return;
		if (SimUniverse.getInstance().isEmpty()) return;

		//Copied out first: landing teleports the entity away, which mutates loadedEntityList
		List<Entity> candidates = new ArrayList<>();

		//A rocket carries its passengers, so anything riding something else is skipped
		for (Object obj : world.loadedEntityList) {
			Entity entity = (Entity) obj;
			if (entity == null || entity.isDead || entity.ridingEntity != null) continue;
			if (entity instanceof EntityRocket || entity instanceof EntityPlayerMP)
				candidates.add(entity);
		}

		for (Entity entity : candidates) {
			if (entity.isDead || entity.worldObj != world) continue;

			if (entity instanceof EntityRocket) handleRocket((EntityRocket) entity);
			else handleLooseEntity(entity);
		}
	}

	private static void handleRocket(EntityRocket rocket) {
		SimUniverse.SimBody hit = sweep(rocket);

		if (hit != null) {
			land(rocket, hit);
			return;
		}

		keepInBounds(rocket);
		checkStranded(rocket);
	}

	/** A player who lost their rocket still gets picked up by a body rather than drifting forever */
	private static void handleLooseEntity(Entity entity) {
		SimUniverse.SimBody hit = sweep(entity);
		if (hit != null) land(entity, hit);
		else keepInBounds(entity);
	}

	@Nullable
	private static SimUniverse.SimBody sweep(Entity entity) {
		//The tick an entity arrives, prevPos is still wherever it came from - possibly another dimension - so
		//trust the current position alone until it has moved once under its own steam
		if (entity.ticksExisted <= 1)
			return SimUniverse.getInstance().findContaining(entity.posX, entity.posY, entity.posZ, null);

		return SimUniverse.getInstance().findCapturing(
				entity.prevPosX, entity.prevPosY, entity.prevPosZ,
				entity.posX, entity.posY, entity.posZ, null);
	}

	/**
	 * Sends the entity down to the body's surface.  The rocket picks the spot, so a return trip arrives back on
	 * whichever pad it launched from.
	 */
	private static void land(Entity entity, SimUniverse.SimBody body) {
		int dimId = body.getLandingDimId();

		//A gas giant, a star with no dimension, or a world another mod never registered has no surface to land
		//on.  Bounce off it rather than eating the rocket or retrying the same failed transfer every tick.
		if (dimId == SimUniverse.NO_DIMENSION || !DimensionManager.getInstance().canTravelTo(dimId)) {
			deflect(entity, body);
			return;
		}

		if (entity instanceof EntityRocket) {
			((EntityRocket) entity).deorbitTo(dimId);
			return;
		}

		if (entity instanceof EntityPlayerMP) {
			//A player with no rocket has no guidance computer to ask, so aim at spawn
			ChunkCoordinates spawn = MinecraftServer.getServer().worldServerForDimension(dimId).getSpawnPoint();
			TeleportHelper.teleportEntityWithRiding(entity, dimId, spawn.posX, Configuration.orbit, spawn.posZ);
		}
	}

	/**
	 * Pushes an entity back out of a body it can never land on, so it cannot get stuck orbiting inside the
	 * capture sphere of a gas giant.
	 */
	private static void deflect(Entity entity, SimUniverse.SimBody body) {
		//Leave along the line we came in on; the sim finds a spot clear of everything else nearby
		double[] out = SimUniverse.getInstance().findClearSpot(body, entity.posX - body.x, entity.posZ - body.z);

		//setLocationAndAngles rather than setPosition: it also resets prevPos, so next tick's sweep starts from
		//outside the sphere instead of tracing back into it and deflecting again forever
		entity.setLocationAndAngles(out[0], out[1], out[2], entity.rotationYaw, entity.rotationPitch);

		//Kill the inward velocity rather than reflecting it - a bounce reads as the game fighting the player
		entity.motionX = 0;
		entity.motionY = 0;
		entity.motionZ = 0;
		entity.velocityChanged = true;

		if (entity instanceof EntityPlayerMP)
			((EntityPlayerMP) entity).playerNetServerHandler.setPlayerLocation(entity.posX, entity.posY, entity.posZ, entity.rotationYaw, entity.rotationPitch);
	}

	/** Keeps entities above the height at which vanilla would delete them */
	private static void keepInBounds(Entity entity) {
		if (entity.posY < SimScale.MIN_Y) {
			entity.setLocationAndAngles(entity.posX, SimScale.MIN_Y, entity.posZ, entity.rotationYaw, entity.rotationPitch);
			if (entity.motionY < 0) entity.motionY = 0;
			entity.velocityChanged = true;

			if (entity instanceof EntityPlayerMP)
				((EntityPlayerMP) entity).playerNetServerHandler.setPlayerLocation(entity.posX, entity.posY, entity.posZ, entity.rotationYaw, entity.rotationPitch);
		}
	}

	/**
	 * A rocket with no fuel cannot manoeuvre and would drift forever, taking its pilot with it.  After a grace
	 * period it falls back to the dimension it launched from - the same rescue the old out-of-world handler did.
	 */
	private static void checkStranded(EntityRocket rocket) {
		if (rocket.getFuelAmount() > 0 || !Configuration.rocketRequireFuel) {
			rocket.resetStrandedTimer();
			return;
		}

		if (rocket.incrementStrandedTimer() < STRANDED_TIMEOUT) return;

		rocket.resetStrandedTimer();

		int home = rocket.getLastDimensionFrom();
		if (home == Configuration.spaceDimId || !DimensionManager.getInstance().canTravelTo(home)) {
			//Nowhere sensible to send it; the overworld always exists
			home = 0;
		}

		if (rocket.riddenByEntity instanceof EntityPlayer)
			((EntityPlayer) rocket.riddenByEntity).addChatMessage(
					new ChatComponentTranslation("msg.entity.rocket.stranded"));

		rocket.deorbitTo(home);
	}
}
