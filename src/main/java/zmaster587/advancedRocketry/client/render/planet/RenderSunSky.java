package zmaster587.advancedRocketry.client.render.planet;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Vec3;
import net.minecraftforge.common.util.ForgeDirection;
import org.lwjgl.opengl.GL11;
import zmaster587.advancedRocketry.api.stations.ISpaceObject;
import zmaster587.advancedRocketry.dimension.DimensionProperties;
import zmaster587.advancedRocketry.inventory.TextureResources;
import zmaster587.advancedRocketry.stations.SpaceObjectManager;
import zmaster587.advancedRocketry.util.AstronomicalBodyHelper;
import zmaster587.libVulpes.render.RenderHelper;
import zmaster587.libVulpes.util.Vector3F;

public class RenderSunSky extends RenderPlanetarySky {
	public RenderSunSky() {
		super();
	}

	final Minecraft mc = Minecraft.getMinecraft();

	@Override
	protected void rotateAroundAxis() {
		Vector3F<Float> axis = getRotateAxis();
		//GL11.glRotatef(90f, axis.x, axis.y, axis.z);
		ISpaceObject obj = SpaceObjectManager.getSpaceManager().getSpaceStationFromBlockCoords((int)mc.thePlayer.posX, (int)mc.thePlayer.posZ);
		if(obj != null)
		{
		GL11.glRotated(obj.getRotation(ForgeDirection.UP)*360, 0, 1, 0);
		GL11.glRotated(obj.getRotation(ForgeDirection.EAST)*360, 1, 0, 0);
		}
		
		//GL11.glRotated(360, obj.getRotation(EnumFacing.EAST), obj.getRotation(EnumFacing.UP), obj.getRotation(EnumFacing.NORTH));
		
	}


	@Override
	protected ResourceLocation getTextureForPlanet(DimensionProperties properties) {
		return TextureResources.locationSunLEO;
	}

	@Override
	protected void drawExtra(Tessellator buffer, DimensionProperties properties, float alphaMultiplier, Vec3 sunColor) {
		//The star's own surface has to sit in front of everything the base sky drew, and the base class clears
		//depth again afterwards, so nothing here needs to write to it
		GL11.glDepthMask(false);

		float planetOrbitalDistance = 0.8F;

		GL11.glPushMatrix();
		GL11.glEnable(GL11.GL_BLEND);

		GL11.glDisable(GL11.GL_FOG);
		GL11.glDisable(GL11.GL_ALPHA_TEST);

		//GL11.glDisable(GL11.GL_LIGHTING);

		GL11.glBlendFunc(GL11.GL_ONE, GL11.GL_ZERO);
		mc.renderEngine.bindTexture(TextureResources.locationSunLEO);

		GL11.glTexParameterf(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
		GL11.glTexParameterf(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
		//int k = mc.theWorld.getMoonPhase();
		//int l = k % 4;
		//int i1 = k / 4 % 2;

		//Set planet Orbiting distance; size
		float f10 = 2F* AstronomicalBodyHelper.getBodySizeMultiplier(planetOrbitalDistance);

		float Xoffset = (float)((System.currentTimeMillis()/200000d % 1));

		float f14 = 1f + Xoffset;
		float f15 = 0f + Xoffset;
		float f16 = f15;
		float f17 = f14;

		//TODO: draw sky planets

		buffer.startDrawingQuads();

		buffer.setColorRGBA_F((float) sunColor.xCoord, (float) sunColor.yCoord, (float) sunColor.zCoord, 1F);

		buffer.addVertexWithUV(-f10, -10.0D, f10, f16, f17);
		buffer.addVertexWithUV(f10, -10.0D, f10, f14, f17);
		buffer.addVertexWithUV(f10, -10.0D, -f10, f14, f15);
		buffer.addVertexWithUV(-f10, -10.0D, -f10, f16, f15);



		buffer.draw();

		//Draw atmosphere
		{
			GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);
			//GL11.glBlendFunc(GL11.GL_ONE, GL11.GL_ONE);

			buffer.startDrawingQuads();
			mc.renderEngine.bindTexture(DimensionProperties.getAtmosphereLEOResource());
			buffer.setColorRGBA_F((float) sunColor.xCoord, (float) sunColor.yCoord, (float) sunColor.zCoord, .8f);

			Xoffset = (float) ((System.currentTimeMillis() / 20000d % 1));

			f14 = 1f + Xoffset;
			f15 = 0f + Xoffset;
			f16 = f15;
			f17 = f14;

			RenderHelper.renderTopFaceWithUV(buffer, -9D, -f10, -f10, 0, 0, f14, f15, f16, f17);
			RenderHelper.renderTopFaceWithUV(buffer, -9D, 0, 0, f10, f10, f14, f15, f16, f17);
			RenderHelper.renderTopFaceWithUV(buffer, -9D, -f10, 0, 0, f10, f14, f15, f16, f17);
			RenderHelper.renderTopFaceWithUV(buffer, -9D, 0, -f10, f10, 0, f14, f15, f16, f17);

			buffer.draw();


			GL11.glDisable(GL11.GL_TEXTURE_2D);
			//GL11.glDisable(GL11.GL_BLEND);
			GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

			buffer.startDrawingQuads();
			buffer.setColorRGBA_F((float) sunColor.xCoord, (float) sunColor.yCoord, (float) sunColor.zCoord, 0.08f);

			double dist = -5D - 4 * (planetOrbitalDistance) / 200D;
			double scalingMult = 1D - 0.9 * (planetOrbitalDistance) / 200D;
			for (int i = 0; i < 5; i++) {
				RenderHelper.renderTopFaceWithUV(buffer, dist + i * scalingMult, -f10, -f10, 0, 0, f14, f15, f16, f17);
				RenderHelper.renderTopFaceWithUV(buffer, dist + i * scalingMult, 0, 0, f10, f10, f14, f15, f16, f17);
				RenderHelper.renderTopFaceWithUV(buffer, dist + i * scalingMult, -f10, 0, 0, f10, f14, f15, f16, f17);
				RenderHelper.renderTopFaceWithUV(buffer, dist + i * scalingMult, 0, -f10, f10, 0, f14, f15, f16, f17);
			}
			buffer.draw();
			GL11.glEnable(GL11.GL_TEXTURE_2D);
		}
		buffer.setColorRGBA_F(1f,1f,1f,1f);
		GL11.glEnable(GL11.GL_FOG);
		//GL11.glEnable(GL11.GL_LIGHTING);
		GL11.glPopMatrix();
		GL11.glDepthMask(true);
	}
}
