package zmaster587.advancedRocketry.integration.nei;

import net.minecraft.client.resources.I18n;
import zmaster587.advancedRocketry.inventory.TextureResources;
import zmaster587.advancedRocketry.tile.multiblock.machine.TileRollingMachine;
import zmaster587.libVulpes.client.util.ProgressBarImage;

public class RollingMachineNEI extends TemplateNEI {
	
	@Override
	public String getRecipeName() {
		return I18n.format("gui.nei.RollingMachine");
	}

	@Override
	protected Class getMachine() {
		return TileRollingMachine.class;
	}

	@Override
	protected ProgressBarImage getProgressBar() {
		return TextureResources.rollingMachineProgressBar;
	}
}
