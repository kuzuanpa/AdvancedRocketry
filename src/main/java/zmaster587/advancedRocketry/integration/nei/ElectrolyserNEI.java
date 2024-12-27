package zmaster587.advancedRocketry.integration.nei;

import net.minecraft.client.resources.I18n;
import org.jetbrains.annotations.NotNull;
import zmaster587.advancedRocketry.inventory.TextureResources;
import zmaster587.advancedRocketry.tile.multiblock.machine.TileElectrolyser;
import zmaster587.libVulpes.client.util.ProgressBarImage;

public class ElectrolyserNEI extends TemplateNEI {
	@Override
	public String getRecipeName() {
		return I18n.format("gui.nei.Electrolyser");
	}

	@Override
	protected Class getMachine() {
		return TileElectrolyser.class;
	}

	@Override
	protected @NotNull ProgressBarImage getProgressBar() {
		return TextureResources.crystallizerProgressBar;
	}
}
