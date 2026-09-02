package zmaster587.advancedRocketry.dimension.sim;

import zmaster587.advancedRocketry.api.dimension.IDimensionProperties;
import zmaster587.advancedRocketry.api.dimension.solar.StellarBody;

/**
 * Converts between AR's abstract celestial numbers and the block coordinates the space dimension uses.
 *
 * AR stores orbital distance as "100 = 1 AU" and star positions as a few hundred units across the galaxy map.
 * Both are used verbatim as block offsets, so a 1 AU orbit is 100 blocks wide and neighbouring stars are a
 * few hundred to a couple of thousand blocks apart.  With the rocket capped at 1 block/tick that is a flight
 * of tens of seconds within a system and a couple of minutes between them.
 *
 * Star Y is signed - real saves contain -912 - and vanilla kills any entity below y=-64, so the whole universe
 * is lifted by an offset {@link SimUniverse} works out from the data at build time.
 */
public final class SimScale {

	private SimScale() {}

	/**
	 * Blocks per sim unit.  Kept at 1 so distances read the same in the XML and in game; turn it down to
	 * shrink the whole galaxy for testing.  Must match on both sides.
	 */
	public static double SIM_TO_BLOCK = 1.0D;

	/** Nothing is ever placed below this; vanilla kills entities under y=-64. */
	public static final int MIN_Y = 64;

	/** Earth's radius in blocks.  Other bodies scale off this by the cube root of their mass. */
	public static final double EARTH_RADIUS = 8.0D;

	/** Gas giants get a flat bonus on top of the mass-derived radius so they read as big targets. */
	public static final double GAS_GIANT_MULTIPLIER = 2.5D;

	/** Blocks of radius per unit of star size. */
	public static final double STAR_RADIUS_PER_SIZE = 24.0D;

	/**
	 * How much wider than the body itself the "you have arrived" region is.  Without gravity to funnel the
	 * player in, this is the only thing making a body hittable, so it is deliberately generous - an earthlike
	 * world in open space is a 32 block sphere.
	 */
	public static final double CAPTURE_MULTIPLIER = 4.0D;

	/** A capture sphere is never smaller than this, even for a body that is only a few blocks across */
	public static final double MIN_CAPTURE = 2.0D;

	/** Nor larger, so a big star does not swallow a whole system */
	public static final double MAX_CAPTURE = 32.0D;

	/**
	 * Largest share of the distance to the nearest neighbouring orbit a body's capture sphere may claim.
	 *
	 * The XML uses "100 = 1 AU" but nothing stops a pack from putting a planet at 8 - the bundled Sol has
	 * Mercury there, eight blocks out.  Below half, two neighbours' spheres can never overlap, so which body a
	 * pilot arrives at is never a matter of rounding.
	 */
	public static final double MAX_CAPTURE_GAP_SHARE = 0.45D;

	/** Extra room left when a rocket departs, so it does not immediately re-enter the capture sphere */
	public static final double DEPARTURE_MARGIN = 8.0D;

	public static double toBlock(double sim) {
		return sim * SIM_TO_BLOCK * (AdvanceRocketrySimulateUniverseCompact.debugMode ? 0.1D : 1.0D);
	}

	public static double clampY(double y) {
		return Math.max(MIN_Y, y);
	}

	/** Radius in blocks of a planet, moon or gas giant, before it is fitted to the space around it */
	public static double planetRadius(IDimensionProperties properties) {
		double mass = Math.max(0.05D, properties.getMass());
		double radius = EARTH_RADIUS * Math.cbrt(mass);
		if(properties.isGasGiant())
			radius *= GAS_GIANT_MULTIPLIER;
		return radius;
	}

	/** Radius in blocks of a star, before it is fitted to the space around it */
	public static double starRadius(StellarBody star) {
		return Math.max(EARTH_RADIUS, star.getSize() * STAR_RADIUS_PER_SIZE);
	}

	/**
	 * Capture sphere for a body, fitted to how much room it actually has.
	 *
	 * The gap share is applied after the floor, not before, so a cramped orbit always wins - overlapping spheres
	 * would make arrival depend on iteration order.
	 *
	 * @param bodyRadius the body's own radius in blocks
	 * @param neighbourGap distance in blocks to the nearest thing it could collide with, or
	 *                     {@link Double#MAX_VALUE} for a body with nothing near it
	 */
	public static double captureRadius(double bodyRadius, double neighbourGap) {
		double capture = Math.max(MIN_CAPTURE, Math.min(MAX_CAPTURE, bodyRadius * CAPTURE_MULTIPLIER));

		if(neighbourGap < Double.MAX_VALUE)
			capture = Math.min(capture, neighbourGap * MAX_CAPTURE_GAP_SHARE);

		//Still never zero: a body with no room at all is a point target rather than an unreachable one
		return Math.max(0.5D, capture);
	}
}
