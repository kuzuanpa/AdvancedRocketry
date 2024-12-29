package zmaster587.advancedRocketry.integration.nei;

import java.awt.Point;
import java.util.List;

import org.lwjgl.opengl.GL11;

import zmaster587.advancedRocketry.api.AdvancedRocketryFluids;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IIcon;
import net.minecraftforge.fluids.FluidStack;
import codechicken.lib.gui.GuiDraw;
import codechicken.nei.recipe.GuiCraftingRecipe;
import codechicken.nei.recipe.GuiUsageRecipe;

public class PositionedFluidStack {

	public final FluidStack tank;
	public final int posX;
    public final int posY;
	public final String overlayTexture;
	public final Point overlayTexturePos;
	public final boolean flowingTexture = false;
	public final boolean showAmount = true;
	public final boolean perTick = false;
	public static final int size = 16;

	public PositionedFluidStack(FluidStack tank, int x, int y, String overlayTexture, Point overlayTexturePos) {
		this.posX = x; this.posY = y;
		this.tank = tank;
		this.overlayTexture = overlayTexture;
		this.overlayTexturePos = overlayTexturePos;
	}
	
	public PositionedFluidStack(FluidStack tank, int x, int y) {
		this(tank, x,y, null ,null);
	}

	public List<String> handleTooltip(List<String> currenttip) {
		if (this.tank == null || this.tank.getFluid() == null || this.tank.amount <= 0) {
			return currenttip;
		}
		currenttip.add(this.tank.getLocalizedName());
		if (this.showAmount) {
			currenttip.add(EnumChatFormatting.GRAY.toString() + this.tank.amount + (this.perTick ? " mB/t" : " mB"));
		}
		return currenttip;
	}

	public boolean transfer(boolean usage) {
		if (this.tank != null && this.tank.amount > 0) {
			if (usage) {
                return GuiUsageRecipe.openRecipeGui("liquid", this.tank);
			} else {
                return GuiCraftingRecipe.openRecipeGui("liquid", this.tank);
			}
        }
		return false;
	}

	public void draw() {
		if (this.tank == null || this.tank.getFluid() == null || this.tank.amount <= 0) {
			return;
		}
		IIcon fluidIcon;
		if (this.flowingTexture && this.tank.getFluid().getFlowingIcon() != null) {
			fluidIcon = this.tank.getFluid().getFlowingIcon();
		} else if (this.tank.getFluid().getStillIcon() != null) {
			fluidIcon = this.tank.getFluid().getStillIcon();
		} else {
			//Really hacky
			fluidIcon = AdvancedRocketryFluids.fluidRocketFuel.getFlowingIcon();
		}

		GuiDraw.changeTexture(TextureMap.locationBlocksTexture);
		int color = this.tank.getFluid().getColor(this.tank);
		GL11.glColor3ub((byte) (color >> 16 & 0xFF), (byte) (color >> 8 & 0xFF), (byte) (color & 0xFF));
		GL11.glDisable(GL11.GL_BLEND);

		int amount = Math.max(size, 1);
		int posY = this.posY + size - amount;

		for (int i = 0; i < size; i += 16) {
			for (int j = 0; j < amount; j += 16) {
				int drawWidth = Math.min(size - i, 16);
				int drawHeight = Math.min(amount - j, 16);

				int drawX = this.posX + i;
				int drawY = posY + j;

				double minU = fluidIcon.getMinU();
				double maxU = fluidIcon.getMaxU();
				double minV = fluidIcon.getMinV();
				double maxV = fluidIcon.getMaxV();

				Tessellator tessellator = Tessellator.instance;
				tessellator.startDrawingQuads();
				tessellator.addVertexWithUV(drawX, drawY + drawHeight, 0, minU, minV + (maxV - minV) * drawHeight / 16F);
				tessellator.addVertexWithUV(drawX + drawWidth, drawY + drawHeight, 0, minU + (maxU - minU) * drawWidth / 16F, minV + (maxV - minV) * drawHeight / 16F);
				tessellator.addVertexWithUV(drawX + drawWidth, drawY, 0, minU + (maxU - minU) * drawWidth / 16F, minV);
				tessellator.addVertexWithUV(drawX, drawY, 0, minU, minV);
				tessellator.draw();
			}
		}

		GL11.glEnable(GL11.GL_BLEND);

		if (this.overlayTexture != null && this.overlayTexturePos != null) {
			GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
			GuiDraw.changeTexture(this.overlayTexture);
			GuiDraw.drawTexturedModalRect(this.posX, this.posY, this.overlayTexturePos.x, this.overlayTexturePos.y, size, size);
		}
	}

}
