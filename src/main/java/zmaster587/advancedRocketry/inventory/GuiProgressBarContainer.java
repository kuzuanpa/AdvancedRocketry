package zmaster587.advancedRocketry.inventory;

import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.inventory.Container;
import net.minecraft.util.IIcon;
import net.minecraftforge.common.util.ForgeDirection;

public abstract class GuiProgressBarContainer extends GuiContainer {
	
	public GuiProgressBarContainer(Container container) {
		super(container);
	}
	
	public void drawProgressBar(int xLoc, int yLoc, int textureOffsetX, int textureOffsetY, int xSize, int ySize,float percent, ForgeDirection direction) {
		if(ForgeDirection.WEST == direction) {
			drawProgressBarHorizontal(xLoc, yLoc, textureOffsetX, textureOffsetY, xSize, ySize, percent);
		}
		else if(ForgeDirection.UP == direction) {
			drawProgressBarVertical(xLoc, yLoc, textureOffsetX, textureOffsetY, xSize, ySize, percent);
		}
		else if(ForgeDirection.DOWN == direction) {
			this.drawTexturedModalRect(xLoc, yLoc, textureOffsetX, textureOffsetY, xSize, (int)(percent*ySize));
		}
		else if (ForgeDirection.EAST == direction) {
			this.drawTexturedModalRect(xLoc, yLoc, textureOffsetX, textureOffsetY, (int)(percent*xSize), ySize);
		}
	}
	
	public void drawProgressBarVertical(int xLoc, int yLoc, int textureOffsetX, int textureOffsetY, int xSize, int ySize,float percent) {
		this.drawTexturedModalRect(xLoc, yLoc + (ySize-(int)(percent*ySize)), textureOffsetX, ySize- (int)(percent*ySize) + textureOffsetY, xSize, (int)(percent*ySize));
	}
	
	public void drawProgressBarHorizontal(int xLoc, int yLoc, int textureOffsetX, int textureOffsetY, int xSize, int ySize,float percent) {
		this.drawTexturedModalRect(xLoc + (xSize-(int)(percent*xSize)), yLoc, xSize- (int)(percent*xSize) + textureOffsetX, textureOffsetY, (int)(percent*xSize), ySize);
	}
	
	public void drawProgressBarIconVertical(int xLoc, int yLoc, IIcon icon, int xSize, int ySize,float percent) {
		this.drawTexturedModelRectFromIcon(xLoc, yLoc + (ySize-(int)(percent*ySize)), icon, xSize, (int)(percent*ySize));
	}
	
    /**
     * Draws a textured rectangle at the stored z-value. Args: x, y, u, v, width, height
     */
    public void drawTexturedModalRectWithCustomSize(int x, int y, int u, int v, int width, int height)
    {
        float f = 0.00390625F;
        float f1 = 0.00390625F;
        Tessellator tessellator = Tessellator.instance;
        tessellator.startDrawingQuads();
        tessellator.addVertexWithUV(x, y + height, this.zLevel, (float)(u) * f, (float)(v + height) * f1);
        tessellator.addVertexWithUV(x + width, y + height, this.zLevel, (float)(u + width) * f, (float)(v + height) * f1);
        tessellator.addVertexWithUV(x + width, y, this.zLevel, (float)(u + width) * f, (float)(v) * f1);
        tessellator.addVertexWithUV(x, y, this.zLevel, (float)(u) * f, (float)(v) * f1);
        tessellator.draw();
    }
}
