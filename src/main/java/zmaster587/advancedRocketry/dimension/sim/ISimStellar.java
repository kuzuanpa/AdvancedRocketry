package zmaster587.advancedRocketry.dimension.sim;

import org.jetbrains.annotations.Nullable;

/**
 * A live view onto one body of the simulated universe.
 *
 * Implementations read straight through to {@link zmaster587.advancedRocketry.dimension.DimensionProperties}
 * or {@link zmaster587.advancedRocketry.api.dimension.solar.StellarBody} rather than snapshotting them, so
 * moving a star with /planet or terraforming a world is picked up without rebuilding the universe.
 */
public interface ISimStellar {

	/** Unique key.  Use {@link SimUniverse#starKey(int)} / {@link SimUniverse#dimKey(int)} to build one. */
	String getID();

	String getName();

	/**
	 * Mass of this body, in solar masses for a star and earth masses for a planet.  Only used to work out how
	 * fast its children orbit.
	 */
	double getMass();

	/** Physical radius, in blocks.  Doubles as the render size and the landing threshold. */
	double getRadius();

	/** Radius of this body's own orbit in AR distance units (100 = 1 AU); 0 for a body at a system centre. */
	double getOrbitRadius();

	/** Where along that orbit the body is right now, in radians. */
	double getOrbitAngle();

	/** Inclination of the orbital plane, in radians. */
	double getInclination();

	/** Absolute position in sim units.  Only consulted for bodies with no parent. */
	double getStaticX();

	double getStaticY();

	double getStaticZ();

	/** Key of the body this one orbits, or null if it sits at a system centre. */
	@Nullable
	String getParentID();

	boolean isStar();

	/**
	 * Dimension a rocket reaching this body should land in, or {@link SimUniverse#NO_DIMENSION} for a body with
	 * no surface (a gas giant, or a star with no dimension registered).
	 */
	int getLandingDimId();

	/**
	 * Dimension whose {@link zmaster587.advancedRocketry.dimension.DimensionProperties} describe this body, or
	 * {@link SimUniverse#NO_DIMENSION} for a star.  Unlike {@link #getLandingDimId()} this is set even for a gas
	 * giant, which has properties and an icon but nowhere to land.
	 */
	int getPropertiesId();

	/** Star id if this body is a star, else {@link SimUniverse#NO_DIMENSION}. */
	int getStarId();
}
