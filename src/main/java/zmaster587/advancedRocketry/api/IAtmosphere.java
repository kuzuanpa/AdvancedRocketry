package zmaster587.advancedRocketry.api;

import net.minecraft.entity.EntityLivingBase;

public interface IAtmosphere {

	/**
	 * @param player living entity inside this atmosphere we are ticking
	 * @return true if the atmosphere does not affect the entity in any way
	 */
	public boolean isImmune(EntityLivingBase player);
	
	/**
	 * To be used to check if combustion can occur in this atmosphere, furnaces, torches, engines, etc could run this check
	 * @return true if the atmosphere is combustable
	 */
	public boolean allowsCombustion();
	
	/**
	 * Should the gas run a tick on every player in it?  Calls onTick(EntityLiving base)
	 * @return true if the atmosphere performs an action every tick
	 */
	public boolean canTick();
	
	/**
	 * If the canTick() returns true then then this is called every tick on EntityLivingBase objects located inside this atmosphere
	 * @param player entity being ticked
	 */
	public void onTick(EntityLivingBase player);
	
	/**
	 * @return unlocalized name of the gas
	 */
	public String getUnlocalizedName();
	
	
	/**
	 * @return true if the atmosphere is normally breathable without a suit
	 */
	public boolean isBreathable();
	
	/**
	 * @return unlocalized message to display when player is in the gas with no protection
	 */
	public String getDisplayMessage();
}
