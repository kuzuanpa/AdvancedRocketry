package zmaster587.advancedRocketry.integration.nei;

import net.minecraft.client.resources.I18n;
import org.jetbrains.annotations.NotNull;
import zmaster587.advancedRocketry.inventory.TextureResources;
import zmaster587.advancedRocketry.tile.multiblock.machine.TileElectricArcFurnace;
import zmaster587.libVulpes.client.util.ProgressBarImage;

public class BlastFurnaceNEI extends TemplateNEI {
	@Override
	public String getRecipeName() {
		return I18n.format("gui.nei.BlastFurnace");
	}

	@Override
	protected @NotNull Class getMachine() {
		return TileElectricArcFurnace.class;
	}


	@Override
	protected ProgressBarImage getProgressBar() {
		return TextureResources.arcFurnaceProgressBar;
	}
}
