package zmaster587.advancedRocketry.dimension.sim;

import cpw.mods.fml.common.FMLCommonHandler;
import zmaster587.advancedRocketry.api.dimension.solar.StellarBody;
import zmaster587.advancedRocketry.dimension.DimensionManager;
import zmaster587.advancedRocketry.dimension.DimensionProperties;

import java.util.ArrayList;
import java.util.List;

/**
 * Bridges {@link DimensionManager}'s stars and dimensions onto {@link SimUniverse}.
 *
 * The views handed to the universe read through to the live DimensionProperties/StellarBody rather than copying
 * their numbers, so terraforming a world or moving a star with /planet takes effect without a rebuild.  Only a
 * change in the set of bodies, or in who orbits whom, needs {@link #markDirty()}.
 */
public class AdvanceRocketrySimulateUniverseCompact {

	/** Shrinks everything by 10x, which makes the whole galaxy walkable while testing */
	public static boolean debugMode = false;

	/** Per side, for the same reason {@link SimUniverse} is: single player runs both on one JVM, two threads */
	private static volatile boolean clientDirty, serverDirty;

	private static boolean isClient() {
		return FMLCommonHandler.instance().getEffectiveSide().isClient();
	}

	/**
	 * Requests a rebuild on the next tick.
	 *
	 * Both sides are flagged regardless of who called: the caller may be a netty thread handling a packet, and
	 * in single player the two sides read the same {@link DimensionManager} anyway.  Each side clears its own
	 * flag when it rebuilds, so a spurious rebuild costs one pass over the dimension list.
	 */
	public static void markDirty() {
		clientDirty = true;
		serverDirty = true;
	}

	public static void init() {
		boolean client = isClient();
		if (client) clientDirty = false;
		else serverDirty = false;

		List<ISimStellar> bodies = new ArrayList<>();

		for (StellarBody star : DimensionManager.getInstance().getStars()) {
			bodies.add(new SimStar(star));
		}

		for (DimensionProperties properties : DimensionManager.getInstance().getDimensionProperties()) {
			//A star's surface dimension is the star, not something orbiting it
			if (properties.isSun()) continue;
			bodies.add(new SimPlanet(properties));
		}

		SimUniverse.getInstance(client).init(bodies);
	}

	public static void stop() {
		boolean client = isClient();
		if (client) clientDirty = false;
		else serverDirty = false;
		SimUniverse.getInstance(client).stop();
	}

	/**
	 * Rebuilds if needed, then repositions everything.  Safe to call from either side every tick.
	 *
	 * @param worldTime total world time of the reference world
	 */
	public static void tick(long worldTime) {
		boolean client = isClient();
		if (client ? clientDirty : serverDirty) init();
		SimUniverse.getInstance(client).update(worldTime);
	}

	/**
	 * A star.  Sits at the fixed galactic coordinates from the XML; sub stars are not simulated separately
	 * because they are drawn as a decoration of their primary.
	 */
	public static class SimStar implements ISimStellar {

		private final StellarBody star;

		public SimStar(StellarBody star) {
			this.star = star;
		}

		@Override public String getID() { return SimUniverse.starKey(star.getId()); }
		@Override public String getName() { return star.getName(); }
		@Override public double getMass() { return star.getMass(); }
		@Override public double getRadius() { return SimScale.starRadius(star); }
		@Override public double getOrbitRadius() { return 0; }
		@Override public double getOrbitAngle() { return 0; }
		@Override public double getInclination() { return 0; }
		@Override public double getStaticX() { return star.getPosX(); }
		@Override public double getStaticY() { return star.getPosY(); }
		@Override public double getStaticZ() { return star.getPosZ(); }
		@Override public String getParentID() { return null; }
		@Override public boolean isStar() { return true; }
		@Override public int getStarId() { return star.getId(); }
		@Override public int getPropertiesId() { return SimUniverse.NO_DIMENSION; }

		@Override
		public int getLandingDimId() {
			//Stars get their own dimension so a dyson sphere has somewhere to stand; only offer a landing if
			//that dimension really exists
			DimensionProperties dim = star.getStarDim();
			return dim != null && dim.isSun() ? dim.getId() : SimUniverse.NO_DIMENSION;
		}
	}

	/** A planet, moon or gas giant.  Angle and radius are read live off the DimensionProperties. */
	public static class SimPlanet implements ISimStellar {

		private final DimensionProperties properties;

		public SimPlanet(DimensionProperties properties) {
			this.properties = properties;
		}

		@Override public String getID() { return SimUniverse.dimKey(properties.getId()); }
		@Override public String getName() { return properties.getName(); }
		@Override public double getMass() { return properties.getMass(); }
		@Override public double getRadius() { return SimScale.planetRadius(properties); }
		@Override public boolean isStar() { return false; }
		@Override public int getStarId() { return SimUniverse.NO_DIMENSION; }
		@Override public int getPropertiesId() { return properties.getId(); }
		@Override public double getStaticX() { return 0; }
		@Override public double getStaticY() { return 0; }
		@Override public double getStaticZ() { return 0; }

		/**
		 * Distance from whatever this body actually orbits.  getSolarOrbitalDistance() would give a moon its
		 * planet's heliocentric distance, which is not what we want here.
		 */
		@Override
		public double getOrbitRadius() {
			return properties.getParentOrbitalDistance();
		}

		/**
		 * Recomputed from the world clock rather than read off the cached orbitTheta, so a position never depends
		 * on whether the owning side has ticked its dimensions yet this frame.
		 */
		@Override
		public double getOrbitAngle() {
			return properties.computeOrbitTheta();
		}

		/** orbitalPhi is stored in degrees, unlike orbitTheta */
		@Override
		public double getInclination() {
			return Math.toRadians(properties.getOrbitPhi());
		}

		@Override
		public String getParentID() {
			int parent = properties.getParentPlanet();
			if (parent != -1) return SimUniverse.dimKey(parent);
			return SimUniverse.starKey(properties.getStarId());
		}

		@Override
		public int getLandingDimId() {
			//Gas giants have no surface and never get a dimension registered
			return properties.isGasGiant() ? SimUniverse.NO_DIMENSION : properties.getId();
		}
	}
}
