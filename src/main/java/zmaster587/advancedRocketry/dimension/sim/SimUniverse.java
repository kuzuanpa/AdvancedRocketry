package zmaster587.advancedRocketry.dimension.sim;

import cpw.mods.fml.common.FMLCommonHandler;
import org.jetbrains.annotations.Nullable;
import zmaster587.advancedRocketry.AdvancedRocketry;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Positions of every star, planet and moon, expressed in the block coordinates of the space dimension.
 *
 * Nothing here is integrated over time.  Each body's angle comes from
 * {@link zmaster587.advancedRocketry.dimension.DimensionProperties#getOrbitTheta()}, which
 * {@link zmaster587.advancedRocketry.util.AstronomicalBodyHelper} derives from the total world time.  That
 * makes a position a pure function of the world clock, so the client and the server agree bit for bit without
 * a single packet, and a reconnect or restart lands on exactly the same sky.
 */
public class SimUniverse {

	/**
	 * One universe per side.  In single player the client and the server run on separate threads inside one JVM,
	 * so a single instance would be rebuilt by one thread while the other walked it.  Each side builds its own
	 * from its own copy of the dimension list, which costs nothing extra: positions are a pure function of the
	 * world clock, so the two agree without being synced.
	 */
	private static final SimUniverse SERVER = new SimUniverse();
	private static final SimUniverse CLIENT = new SimUniverse();

	public static SimUniverse getInstance() {
		return FMLCommonHandler.instance().getEffectiveSide().isClient() ? CLIENT : SERVER;
	}

	/** The instance belonging to the given side, for the rare caller that has to be explicit */
	public static SimUniverse getInstance(boolean client) {
		return client ? CLIENT : SERVER;
	}

	/** Returned wherever a dimension or star id is expected but the body has no surface to land on */
	public static final int NO_DIMENSION = Integer.MIN_VALUE;

	private final Map<String, SimBody> bodiesMap = new LinkedHashMap<>();
	private final Collection<SimBody> bodiesView = Collections.unmodifiableCollection(bodiesMap.values());
	private final List<SimBody> rootBodies = new ArrayList<>();

	/** Guards against recomputing the whole tree more than once for the same world tick */
	private long lastUpdateStamp = Long.MIN_VALUE;

	/**
	 * Lifts the whole universe so nothing sits below {@link SimScale#MIN_Y}.  Star Y is signed and real saves
	 * contain values as low as -912; clamping each body individually instead would silently flatten a system
	 * onto the floor and break the geometry the pilot navigates by.
	 */
	private double yOffset;

	/**
	 * Stars and dimensions share one integer namespace - a star's surface dimension deliberately reuses the
	 * star's id - so keys are prefixed to keep the two apart where they are genuinely different bodies.
	 */
	public static String starKey(int starId) {
		return "S" + starId;
	}

	public static String dimKey(int dimId) {
		return "D" + dimId;
	}

	public void init(List<? extends ISimStellar> configs) {
		bodiesMap.clear();
		rootBodies.clear();
		lastUpdateStamp = Long.MIN_VALUE;

		for (ISimStellar config : configs) {
			if (config == null) continue;
			bodiesMap.put(config.getID(), new SimBody(config));
		}

		for (SimBody body : bodiesMap.values()) {
			String parentID = body.config.getParentID();
			SimBody parent = parentID == null ? null : bodiesMap.get(parentID);

			//A body whose parent is missing (an orphaned moon, a planet whose star left the XML), or one caught in
			//a cycle (two planets each claiming to orbit the other), becomes a system centre rather than being
			//dropped.  A cycle would otherwise leave it unreachable from any root and so never positioned.
			if (parent == null) {
				rootBodies.add(body);
			}
			else if (parent == body || wouldCycle(body, parent, bodiesMap)) {
				AdvancedRocketry.logger.warn("Body {} is its own ancestor; treating it as a system centre", body.config.getID());
				rootBodies.add(body);
			}
			else {
				body.setParent(parent);
				parent.addChild(body);
			}
		}

		//Work out how far up the whole universe has to be shifted.  Roots are the only bodies with an absolute
		//Y; children hang off them, and the deepest anything can dip below its root is the sum of the orbit
		//radii down that branch.
		double lowest = 0;
		for (SimBody root : rootBodies) {
			lowest = Math.min(lowest, SimScale.toBlock(root.config.getStaticY()) - root.branchReach());
		}
		yOffset = Math.max(0, SimScale.MIN_Y - lowest);

		//Capture spheres are sized against the space each body has, which needs the tree in place
		for (SimBody root : rootBodies) {
			root.resolveCaptureRadii();
		}

		//Give everything a position immediately; callers should not have to wait for the first tick
		update(0L, true);
	}

	/**
	 * @return true if making parent the parent of body would close a loop, which would leave the whole loop
	 * unreachable from any root and so never positioned or drawn
	 */
	private static boolean wouldCycle(SimBody body, SimBody parent, Map<String, SimBody> pool) {
		//Walk the prospective ancestry.  Bounded by the pool size, so a pre-existing loop cannot hang us.
		SimBody node = parent;
		for (int steps = pool.size(); node != null && steps >= 0; steps--) {
			if (node == body) return true;

			String nextID = node.config.getParentID();
			node = nextID == null ? null : pool.get(nextID);
		}
		return node != null;
	}

	public void stop() {
		bodiesMap.clear();
		rootBodies.clear();
		yOffset = 0;
		lastUpdateStamp = Long.MIN_VALUE;
	}

	public boolean isEmpty() {
		return bodiesMap.isEmpty();
	}

	/**
	 * Recomputes every position.  Cheap and idempotent within a tick, so both sides can call it every tick.
	 *
	 * @param worldTime total world time, used only to skip repeated work within one tick
	 */
	public void update(long worldTime) {
		update(worldTime, false);
	}

	private void update(long worldTime, boolean force) {
		if (!force && worldTime == lastUpdateStamp) return;
		lastUpdateStamp = worldTime;

		for (SimBody root : rootBodies) {
			root.update(yOffset);
		}
	}

	@Nullable
	public SimBody getBody(String id) {
		return bodiesMap.get(id);
	}

	/**
	 * Looks up the body a player standing in the given dimension is on.  Handles both ordinary worlds and the
	 * surface dimension of a star.
	 */
	@Nullable
	public SimBody getBodyForDim(int dimId) {
		SimBody body = bodiesMap.get(dimKey(dimId));
		return body != null ? body : bodiesMap.get(starKey(dimId));
	}

	/** Live view; do not mutate, and do not hold it across an {@link #init} */
	public Collection<SimBody> getAllBodies() {
		return bodiesView;
	}

	/**
	 * @return the body whose capture sphere the segment from p1 to p2 enters first, or null.  Sweeping the
	 * whole segment rather than testing the end point matters because a rocket moves up to a block a tick and
	 * would otherwise step straight over a small moon.
	 */
	@Nullable
	public SimBody findCapturing(double x1, double y1, double z1, double x2, double y2, double z2, @Nullable SimBody ignore) {
		SimBody best = null;
		double bestFraction = Double.MAX_VALUE;
		double bestDistSq = Double.MAX_VALUE;

		for (SimBody body : bodiesMap.values()) {
			if (body == ignore) continue;

			double fraction = body.sweepFraction(x1, y1, z1, x2, y2, z2);
			if (fraction < 0) continue;

			//Overlapping spheres are possible in a cramped system; break the tie on which centre is closer so
			//the answer does not depend on map order
			double distSq = body.distanceSqTo(x2, y2, z2);
			if (fraction < bestFraction || (fraction == bestFraction && distSq < bestDistSq)) {
				bestFraction = fraction;
				bestDistSq = distSq;
				best = body;
			}
		}
		return best;
	}

	/** @return the body whose capture sphere contains this point, or null */
	@Nullable
	public SimBody findContaining(double x, double y, double z, @Nullable SimBody ignore) {
		for (SimBody body : bodiesMap.values()) {
			if (body == ignore) continue;
			double capture = body.getCaptureRadius();
			if (body.distanceSqTo(x, y, z) <= capture * capture) return body;
		}
		return null;
	}

	/**
	 * Picks a spot just outside a body's capture sphere to arrive at or leave from.
	 *
	 * The naive "offset along the pilot's heading" lands inside the parent star for anything on a tight orbit -
	 * the bundled Mercury sits eight blocks out - so the heading is only used for the component pointing away
	 * from the parent, and the result is pushed clear of anything else it happens to land inside.
	 *
	 * @param dirX preferred direction to leave in; need not be normalised
	 * @return {x, y, z} in block coordinates
	 */
	public double[] findClearSpot(SimBody body, double dirX, double dirZ) {
		double ux = dirX, uz = dirZ;
		double len = Math.sqrt(ux * ux + uz * uz);

		if (len < 1.0E-4D) {
			ux = 1;
			uz = 0;
		} else {
			ux /= len;
			uz /= len;
		}

		//Flip the heading if it points back towards the parent, so leaving never means flying into the star
		if (body.parent != null) {
			double outX = body.x - body.parent.x;
			double outZ = body.z - body.parent.z;
			if (ux * outX + uz * outZ < 0) {
				ux = -ux;
				uz = -uz;
			}
		}

		double offset = body.getCaptureRadius() + SimScale.DEPARTURE_MARGIN;

		//A handful of attempts is plenty; each one clears the body that was in the way
		for (int attempt = 0; attempt < 8; attempt++) {
			double px = body.x + ux * offset;
			double py = SimScale.clampY(body.y + offset * 0.25D);
			double pz = body.z + uz * offset;

			SimBody blocking = findContaining(px, py, pz, body);
			if (blocking == null) return new double[] { px, py, pz };

			offset += blocking.getCaptureRadius() + SimScale.DEPARTURE_MARGIN;
		}

		//Give up and go far enough out that nothing can reach
		return new double[] { body.x + ux * offset, SimScale.clampY(body.y + offset * 0.25D), body.z + uz * offset };
	}

	/** @return the body closest to the given point, ignoring capture radii.  Drives the navigation readout. */
	@Nullable
	public SimBody findNearest(double x, double y, double z) {
		SimBody best = null;
		double bestDistSq = Double.MAX_VALUE;

		for (SimBody body : bodiesMap.values()) {
			double distSq = body.distanceSqTo(x, y, z);
			if (distSq < bestDistSq) {
				bestDistSq = distSq;
				best = body;
			}
		}
		return best;
	}

	/**
	 * @return the body sitting closest to the given look direction, or null if nothing falls inside the cone.
	 *
	 * A cone rather than the body's drawn size: everything is a handful of skybox pixels at any real distance,
	 * so matching the painted quad would make a body practically impossible to point at.
	 *
	 * @param maxAngle half angle of the cone, in radians
	 */
	@Nullable
	public SimBody findLookingAt(double x, double y, double z, double lookX, double lookY, double lookZ, double maxAngle) {
		double lookLen = Math.sqrt(lookX * lookX + lookY * lookY + lookZ * lookZ);
		if (lookLen < 1.0E-4D) return null;

		SimBody best = null;

		//Compared as cosines, so the cone test and "closest to the crosshair" are the same comparison
		double bestCos = Math.cos(maxAngle);

		for (SimBody body : bodiesMap.values()) {
			double dx = body.x - x;
			double dy = body.y - y;
			double dz = body.z - z;
			double dist = Math.sqrt(dx * dx + dy * dy + dz * dz);
			if (dist < 1.0E-4D) continue;

			double cos = (lookX * dx + lookY * dy + lookZ * dz) / (lookLen * dist);
			if (cos > bestCos) {
				bestCos = cos;
				best = body;
			}
		}
		return best;
	}

	public static class SimBody {

		private final ISimStellar config;

		/** Position in block coordinates of the space dimension */
		public double x, y, z;
		private double prevX, prevY, prevZ;
		private boolean positioned;

		private SimBody parent;
		private final List<SimBody> children = new ArrayList<>();

		/** Fitted to the space around the body once the tree is built; see {@link #resolveCaptureRadii()} */
		private double captureRadius;

		public SimBody(ISimStellar config) {
			this.config = config;
			this.captureRadius = SimScale.captureRadius(config.getRadius());
		}

		void setParent(SimBody parent) {
			this.parent = parent;
		}

		void addChild(SimBody child) {
			this.children.add(child);
		}

		/** How far below this body the deepest thing orbiting it can reach, in blocks */
		double branchReach() {
			double reach = 0;
			for (SimBody child : children) {
				reach = Math.max(reach, SimScale.toBlock(child.config.getOrbitRadius()) + child.branchReach());
			}
			return reach;
		}

		/**
		 * Sizes each capture sphere against the room the body actually has.  A pack is free to put a planet eight
		 * distance units out - the bundled Sol does exactly that with Mercury - and at that spacing the default
		 * spheres would engulf each other, making which body a pilot reached a matter of rounding.
		 *
		 * The sky draws bodies at their capture radius rather than their physical one, so a big target looks like
		 * one; see RenderPlanetarySky.
		 */
		void resolveCaptureRadii() {
			captureRadius = fitCaptureRadius();

			for (SimBody child : children) {
				child.resolveCaptureRadii();
			}
		}

		/** Capture sphere shrunk until it cannot reach any neighbour's */
		private double fitCaptureRadius() {
			double capture = SimScale.captureRadius(config.getRadius());
			double myOrbit = SimScale.toBlock(config.getOrbitRadius());

			if (parent != null) {
				capture = Math.min(capture, SimScale.captureShare(config.getRadius(), parent.config.getRadius(), myOrbit));

				//Siblings: two circular orbits never get closer than the difference in their radii.  Two bodies
				//given the same orbit - easy with integer distances - can collide anywhere, so they get nothing.
				for (SimBody sibling : parent.children) {
					if (sibling == this) continue;
					double separation = Math.abs(SimScale.toBlock(sibling.config.getOrbitRadius()) - myOrbit);
					capture = Math.min(capture, SimScale.captureShare(config.getRadius(), sibling.config.getRadius(), separation));
				}
			}

			//Whatever orbits this body
			for (SimBody child : children) {
				double separation = SimScale.toBlock(child.config.getOrbitRadius());
				capture = Math.min(capture, SimScale.captureShare(config.getRadius(), child.config.getRadius(), separation));
			}

			//A body with no room at all still gets a point target rather than becoming unreachable
			return Math.max(0.5D, capture);
		}

		public ISimStellar getConfig() {
			return config;
		}

		@Nullable
		public SimBody getParent() {
			return parent;
		}

		public String getName() {
			return config.getName();
		}

		/**
		 * Physical size, in blocks.  Only meaningful relative to other bodies - the sky draws to
		 * {@link #getCaptureRadius()} instead, so that what a pilot aims at is what they see.
		 */
		public double getRadius() {
			return config.getRadius();
		}

		public double getCaptureRadius() {
			return captureRadius;
		}

		public int getLandingDimId() {
			return config.getLandingDimId();
		}

		public boolean isStar() {
			return config.isStar();
		}

		/** Movement over the last tick, in blocks.  A departing rocket inherits this so it stays in formation. */
		public double getVelX() {
			return x - prevX;
		}

		public double getVelY() {
			return y - prevY;
		}

		public double getVelZ() {
			return z - prevZ;
		}

		void update(double yOffset) {
			prevX = x;
			prevY = y;
			prevZ = z;

			if (parent == null) {
				x = SimScale.toBlock(config.getStaticX());
				y = SimScale.toBlock(config.getStaticY()) + yOffset;
				z = SimScale.toBlock(config.getStaticZ());
			} else {
				double r = SimScale.toBlock(config.getOrbitRadius());
				double theta = config.getOrbitAngle();
				double inclination = config.getInclination();

				double localX = Math.cos(theta) * r;
				double localZ = Math.sin(theta) * r;

				//Tilt the orbit out of the parent's equatorial plane
				double tiltedY = -localZ * Math.sin(inclination);
				double tiltedZ = localZ * Math.cos(inclination);

				x = parent.x + localX;
				y = parent.y + tiltedY;
				z = parent.z + tiltedZ;
			}

			//A degenerate orbit upstream (a zero period, a missing star) can produce a NaN angle; letting that
			//through would end up in an entity position, which corrupts the entity outright
			if (Double.isNaN(x) || Double.isNaN(y) || Double.isNaN(z)) {
				x = parent != null ? parent.x : 0;
				y = parent != null ? parent.y : yOffset;
				z = parent != null ? parent.z : 0;
			}

			if (!positioned) {
				prevX = x;
				prevY = y;
				prevZ = z;
				positioned = true;
			}

			for (SimBody child : children) {
				child.update(yOffset);
			}
		}

		public double distanceSqTo(double px, double py, double pz) {
			double dx = x - px, dy = y - py, dz = z - pz;
			return dx * dx + dy * dy + dz * dz;
		}

		public double distanceTo(double px, double py, double pz) {
			return Math.sqrt(distanceSqTo(px, py, pz));
		}

		/**
		 * Sweeps the segment from p1 to p2 against this body's capture sphere.
		 *
		 * @return fraction along the segment at which it first enters, or -1 if it never does
		 */
		double sweepFraction(double x1, double y1, double z1, double x2, double y2, double z2) {
			double radius = getCaptureRadius();
			double radiusSq = radius * radius;

			//Already inside at the start - treat as an immediate hit
			if (distanceSqTo(x1, y1, z1) <= radiusSq) return 0;

			double dx = x2 - x1, dy = y2 - y1, dz = z2 - z1;
			double lengthSq = dx * dx + dy * dy + dz * dz;
			if (lengthSq < 1.0E-8D) return -1;

			//Project the centre onto the segment and reject if the nearest approach is off either end
			double t = ((x - x1) * dx + (y - y1) * dy + (z - z1) * dz) / lengthSq;
			if (t < 0 || t > 1) return -1;

			double closestX = x1 + dx * t;
			double closestY = y1 + dy * t;
			double closestZ = z1 + dz * t;

			return distanceSqTo(closestX, closestY, closestZ) <= radiusSq ? t : -1;
		}
	}
}
