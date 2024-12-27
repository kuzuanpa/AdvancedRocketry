package zmaster587.advancedRocketry.atmosphere;

import zmaster587.advancedRocketry.api.IAtmosphere;
import zmaster587.advancedRocketry.api.atmosphere.AtmosphereRegister;
import net.minecraft.entity.EntityLivingBase;

public class AtmosphereBase implements IAtmosphere {

	static {
		AtmosphereRegister.getInstance().registerAtmosphere(AtmosphereTypes.AIR);
		AtmosphereRegister.getInstance().registerAtmosphere(AtmosphereTypes.PRESSURIZEDAIR);
		AtmosphereRegister.getInstance().registerAtmosphere(AtmosphereTypes.VACUUM);
		AtmosphereRegister.getInstance().registerAtmosphere(AtmosphereTypes.LOWOXYGEN);
		AtmosphereRegister.getInstance().registerAtmosphere(AtmosphereTypes.HIGHPRESSURE);
		AtmosphereRegister.getInstance().registerAtmosphere(AtmosphereTypes.SUPERHIGHPRESSURE);
		AtmosphereRegister.getInstance().registerAtmosphere(AtmosphereTypes.VERYHOT);
		AtmosphereRegister.getInstance().registerAtmosphere(AtmosphereTypes.SUPERHEATED);
		AtmosphereRegister.getInstance().registerAtmosphere(AtmosphereTypes.NOO2);
		AtmosphereRegister.getInstance().registerAtmosphere(AtmosphereTypes.HIGHPRESSURENOO2);
		AtmosphereRegister.getInstance().registerAtmosphere(AtmosphereTypes.SUPERHIGHPRESSURENOO2);
		AtmosphereRegister.getInstance().registerAtmosphere(AtmosphereTypes.VERYHOTNOO2);
		AtmosphereRegister.getInstance().registerAtmosphere(AtmosphereTypes.SUPERHEATEDNOO2);
	}
	
	private boolean allowsCombustion;
	private boolean isBreathable;
	private final boolean canTick;
	private final String name;

	public AtmosphereBase(boolean canTick, boolean isBreathable, String name) {
		this.allowsCombustion = isBreathable;
		this.isBreathable = isBreathable;
		this.canTick = canTick;
		this.name = name;
	}
	
	public AtmosphereBase(boolean canTick, boolean isBreathable, boolean allowsCombustion, String name) {
		this(canTick, isBreathable, name);
		this.allowsCombustion = allowsCombustion;
	}

	/**
	 * Should the gas run a tick on every player in it?  Calls onTick(EntityLiving base)
	 * @return true if the atmosphere performs an action every tick
	 */
	public boolean canTick() {
		return canTick;
	}

	//TODO: check for all entities
	/**
	 * 
	 * @param player living entity inside this atmosphere we are ticking
	 * @return true if the atmosphere does not affect the entity in any way
	 */
	public boolean isImmune(EntityLivingBase player) {
		return isBreathable;
	}
	
	@Override
	public boolean isBreathable() {
		return isBreathable;
	}
	
	/**
	 * To be used to check if combustion can occur in this atmosphere, furnaces, torches, engines, etc could run this check
	 * @return true if the atmosphere is combustable
	 */
	public boolean allowsCombustion() {
		return allowsCombustion;
	}

	/**
	 * Sets the atmosphere to be breathable or not breathable
	 * @param isBreathable
	 */
	public void setIsBreathable(boolean isBreathable) {
		this.isBreathable = isBreathable;
	}

	/**
	 * Sets the atmosphere to allow combustion or not to allow combustion
	 * @param allowsCombustion
	 */
	public void setAllowsCombustion(boolean allowsCombustion) {
		this.allowsCombustion = allowsCombustion;
	}
	
	/**
	 * @return unlocalized message to display when player is in the gas with no protection
	 */
	public String getDisplayMessage() {
		return "";
	}

	//TODO: tick for all entities
	/**
	 * If the canTick() returns true then then this is called every tick on EntityLivingBase objects located inside this atmosphere
	 * @param player entity being ticked
	 */
	public void onTick(EntityLivingBase player) {
	}

	@Override
	public String getUnlocalizedName() {
		return name;
	}
}
