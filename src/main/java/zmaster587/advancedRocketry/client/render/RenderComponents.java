package zmaster587.advancedRocketry.client.render;

import org.jetbrains.annotations.NotNull;
import zmaster587.advancedRocketry.api.AdvancedRocketryItems;
import zmaster587.advancedRocketry.client.render.armor.RenderJetPack;
import zmaster587.libVulpes.api.IModularArmor;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import net.minecraft.item.ItemStack;
import net.minecraftforge.client.event.RenderPlayerEvent;

public class RenderComponents {

	static RenderJetPack renderJetPack = new RenderJetPack();
	@SubscribeEvent
	public void renderPostSpecial(RenderPlayerEvent.Specials.@NotNull Post event) {
		//RenderJet pack
		ItemStack chest = event.entityLiving.getEquipmentInSlot(3);
		if(chest != null && chest.getItem() instanceof IModularArmor) {
			for(ItemStack stack : ((IModularArmor)chest.getItem()).getComponents(chest)) {
				if(stack.getItem() == AdvancedRocketryItems.itemJetpack)
					renderJetPack.render(event.entityLiving, 0, 0, 0, 0, 0, 0);
			}
		}
		
	}
}
