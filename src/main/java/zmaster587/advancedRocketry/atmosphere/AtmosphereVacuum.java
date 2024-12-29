package zmaster587.advancedRocketry.atmosphere;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import zmaster587.advancedRocketry.api.Configuration;
import zmaster587.advancedRocketry.network.PacketOxygenState;
import zmaster587.libVulpes.LibVulpes;
import zmaster587.libVulpes.network.PacketHandler;

/**
 * Atmosphere type for vaccum (No air)
 * @author Zmaster
 */
public class AtmosphereVacuum extends AtmosphereNeedsSuit {

	public static int damageValue;
	public static final boolean enableNausea = Configuration.enableNausea;

	public AtmosphereVacuum() {
		super(true, false, false, "vacuum");
	}

	@Override
	public void onTick(EntityLivingBase player) {
		if(player.worldObj.getTotalWorldTime() % 10  == 0 && !isImmune(player)) {
			player.attackEntityFrom(AtmosphereHandler.vacuumDamage, damageValue);
			player.addPotionEffect(new PotionEffect(Potion.digSlowdown.id, 40, 4));
			player.addPotionEffect(new PotionEffect(Potion.moveSlowdown.id, 40, 4));
			if(enableNausea) {
				player.addPotionEffect(new PotionEffect(Potion.confusion.id, 400, 1));
			}
			if(player instanceof EntityPlayer)
				PacketHandler.sendToPlayer(new PacketOxygenState(), (EntityPlayer)player);
		}
	}

	@Override
	public String getDisplayMessage() {
		return LibVulpes.proxy.getLocalizedString("msg.noOxygen");
	}
}
