package zmaster587.advancedRocketry.integration.nei;

import net.minecraft.client.resources.I18n;
import org.jetbrains.annotations.NotNull;
import zmaster587.advancedRocketry.inventory.TextureResources;
import zmaster587.advancedRocketry.tile.multiblock.machine.TileCuttingMachine;
import zmaster587.libVulpes.client.util.ProgressBarImage;

public class CuttingMachineNEI extends TemplateNEI {
	
	@Override
	public @NotNull String getRecipeName() {
		return I18n.format("gui.nei.CuttingMachine");
	}

	@Override
	protected Class getMachine() {
		return TileCuttingMachine.class;
	}

	@Override
	protected ProgressBarImage getProgressBar() {
		return TextureResources.cuttingMachineProgressBar;
	}
}
