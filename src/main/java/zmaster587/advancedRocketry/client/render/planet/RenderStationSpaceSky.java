package zmaster587.advancedRocketry.client.render.planet;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.Vec3;
import net.minecraftforge.common.util.ForgeDirection;
import org.lwjgl.opengl.GL11;
import zmaster587.advancedRocketry.api.IPlanetaryProvider;
import zmaster587.advancedRocketry.api.dimension.IDimensionProperties;
import zmaster587.advancedRocketry.api.stations.ISpaceObject;
import zmaster587.advancedRocketry.dimension.DimensionProperties;
import zmaster587.advancedRocketry.stations.SpaceObjectManager;
import zmaster587.advancedRocketry.util.AstronomicalBodyHelper;
import zmaster587.libVulpes.render.RenderHelper;
import zmaster587.libVulpes.util.Vector3F;

public class RenderStationSpaceSky extends RenderPlanetarySky {

	//Mostly vanilla code
	//TODO: make usable on other planets
	public RenderStationSpaceSky() {
		super();
	}
	float oldRotateX,oldRotateY;

	final Minecraft mc = Minecraft.getMinecraft();

	public Vector3F<Double> getPlayerPos(float partialTicks, EntityPlayer player){
		double px = player.prevPosX + (player.posX - player.prevPosX) * partialTicks;
		double py = player.prevPosY + (player.posY - player.prevPosY) * partialTicks;
		double pz = player.prevPosZ + (player.posZ - player.prevPosZ) * partialTicks;
		return new Vector3F<>(px,py,pz);
	}
	@Override
	protected void drawExtra(Tessellator tessellator1, DimensionProperties properties, float alphaMultiplier, Vec3 sunColor)  {
		if(!(mc.theWorld.provider instanceof IPlanetaryProvider))return;

		IDimensionProperties stationProperties = ((IPlanetaryProvider) mc.theWorld.provider).getDimensionProperties((int)mc.thePlayer.posX, (int)mc.thePlayer.posZ);
		if(stationProperties == null || stationProperties.getParentProperties() == null) return;

		properties = (DimensionProperties) stationProperties.getParentProperties();

		float planetOrbitalDistance = 2 + 0.1F;

		GL11.glPushMatrix();
		//GL11.glDisable(GL11.GL_BLEND);

		GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);
		GL11.glDisable(GL11.GL_FOG);

		//GL11.glDisable(GL11.GL_LIGHTING);

		GL11.glBlendFunc(GL11.GL_ONE, GL11.GL_ZERO);
		mc.renderEngine.bindTexture(properties.getPlanetIconLEO());
		GL11.glTexParameterf(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
		GL11.glTexParameterf(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
		//int k = mc.theWorld.getMoonPhase();
		//int l = k % 4;
		//int i1 = k / 4 % 2;

		//Set planet Orbiting distance; size
		float f10 = 2F*AstronomicalBodyHelper.getBodySizeMultiplier(planetOrbitalDistance);

		float Xoffset = (float)((System.currentTimeMillis()%1000000/1000000d % 1));

		float f14 = 1f + Xoffset;
		float f15 = 0f + Xoffset;
		float f16 = 0f;
		float f17 = 1f;
		tessellator1.startDrawingQuads();

		RenderHelper.renderTopFaceWithUV(tessellator1, -10D, -f10, -f10, f10, f10, f14, f15, f16, f17);

		tessellator1.draw();

		GL11.glPopAttrib();

		//Draw atmosphere if applicable
		if(properties.isGasGiant()) {
			GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);
			//GL11.glBlendFunc(GL11.GL_ONE, GL11.GL_ONE);

			tessellator1.startDrawingQuads();
			mc.renderEngine.bindTexture(DimensionProperties.getAtmosphereLEOResource());
			double dist = -5D - 4*(planetOrbitalDistance)/200D;
			double scalingMult = 1D - 2*(planetOrbitalDistance)/200D;
			
			int maxAmt = 6;
			float lng = (float) (Minecraft.getSystemTime()%100000/100000d % 1);
			for(int i = 0; i < maxAmt; i++) {
				tessellator1.setColorRGBA_F(0.05f*(maxAmt-i/6f), .4f*(i/6f), 1f, 0.4f);

				//IDK it looks pretty
				Xoffset = lng*(i-(maxAmt/4f));
				float Yoffset = -lng*i;

				f14 = i + Yoffset;
				f15 = 0f + Yoffset;
				f16 = 0f + Xoffset;
				f17 = i + Xoffset;

				
				
				RenderHelper.renderTopFaceWithUV(tessellator1, -10D + i*scalingMult, -f10, -f10, 0, 0, f14, f15, f16, f17);
				RenderHelper.renderTopFaceWithUV(tessellator1, -10D+ i*scalingMult, 0, 0, f10, f10, f14, f15, f16, f17);
				RenderHelper.renderTopFaceWithUV(tessellator1, -10D+ i*scalingMult, -f10, 0, 0, f10, f14, f15, f16, f17);
				RenderHelper.renderTopFaceWithUV(tessellator1, -10D+ i*scalingMult, 0, -f10, f10, 0, f14, f15, f16, f17);
			}

			tessellator1.draw();


			GL11.glDisable(GL11.GL_TEXTURE_2D);
			//GL11.glDisable(GL11.GL_BLEND);
			GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

			tessellator1.startDrawingQuads();
			tessellator1.setColorRGBA_F(0.5f,0.5f,1, 0.08f);


			for(int i = 0; i < 5 ; i++) {
				RenderHelper.renderTopFaceWithUV(tessellator1, dist + i*scalingMult, -f10, -f10, 0, 0, f14, f15, f16, f17);
				RenderHelper.renderTopFaceWithUV(tessellator1, dist + i*scalingMult, 0, 0, f10, f10, f14, f15, f16, f17);
				RenderHelper.renderTopFaceWithUV(tessellator1, dist + i*scalingMult, -f10, 0, 0, f10, f14, f15, f16, f17);
				RenderHelper.renderTopFaceWithUV(tessellator1, dist + i*scalingMult, 0, -f10, f10, 0, f14, f15, f16, f17);
			}
			tessellator1.draw();
			GL11.glEnable(GL11.GL_TEXTURE_2D);
		}
		else if(properties.hasAtmosphere()) {
			GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);
			//GL11.glBlendFunc(GL11.GL_ONE, GL11.GL_ONE);

			tessellator1.startDrawingQuads();
			mc.renderEngine.bindTexture(DimensionProperties.getAtmosphereLEOResource());
			tessellator1.setColorRGBA_F(1f, 1f, 1f, .8f);

			Xoffset = (float)((System.currentTimeMillis()%100000/100000d % 1));

			f14 = 1f + Xoffset;
			f15 = 0f + Xoffset;
			f16 = 1f;
			f17 = 0f;

			RenderHelper.renderTopFaceWithUV(tessellator1, -10D, -f10, -f10, 0, 0, f14, f15, f16, f17);
			RenderHelper.renderTopFaceWithUV(tessellator1, -10D, 0, 0, f10, f10, f14, f15, f16, f17);
			RenderHelper.renderTopFaceWithUV(tessellator1, -10D, -f10, 0, 0, f10, f14, f15, f16, f17);
			RenderHelper.renderTopFaceWithUV(tessellator1, -10D, 0, -f10, f10, 0, f14, f15, f16, f17);

			tessellator1.draw();


			GL11.glDisable(GL11.GL_TEXTURE_2D);
			//GL11.glDisable(GL11.GL_BLEND);
			GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

			tessellator1.startDrawingQuads();
			tessellator1.setColorRGBA_F(properties.skyColor[0], properties.skyColor[1], properties.skyColor[2], 0.08f);

			double dist = -5D - 4*(planetOrbitalDistance)/200D;
			double scalingMult = 1D - 0.9*(planetOrbitalDistance)/200D;
			for(int i = 0; i < 5 ; i++) {
				RenderHelper.renderTopFaceWithUV(tessellator1, dist + i*scalingMult, -f10, -f10, 0, 0, f14, f15, f16, f17);
				RenderHelper.renderTopFaceWithUV(tessellator1, dist + i*scalingMult, 0, 0, f10, f10, f14, f15, f16, f17);
				RenderHelper.renderTopFaceWithUV(tessellator1, dist + i*scalingMult, -f10, 0, 0, f10, f14, f15, f16, f17);
				RenderHelper.renderTopFaceWithUV(tessellator1, dist + i*scalingMult, 0, -f10, f10, 0, f14, f15, f16, f17);
			}
			tessellator1.draw();
			GL11.glEnable(GL11.GL_TEXTURE_2D);
		}


		tessellator1.setColorRGBA_F(1f,1f,1f,1f);
		GL11.glEnable(GL11.GL_FOG);
		//GL11.glEnable(GL11.GL_LIGHTING);
		GL11.glPopMatrix();
	}

	@Override
	protected ForgeDirection getRotationAxis(DimensionProperties properties,
			int x, int z) {
		try {
			return SpaceObjectManager.getSpaceManager().getSpaceStationFromBlockCoords(x,z).getForwardDirection().getRotation(ForgeDirection.UP);
		} catch(Exception e) {
			return super.getRotationAxis(properties, x, z);
		}
	}

	@Override
	protected void rotateAroundAxis() {
		Vector3F<Float> axis = getRotateAxis();
		//GL11.glRotatef(90f, axis.x, axis.y, axis.z);
		ISpaceObject obj = SpaceObjectManager.getSpaceManager().getSpaceStationFromBlockCoords((int)mc.thePlayer.posX, (int)mc.thePlayer.posZ);
		if(obj != null)
		{
			float rotateXToGo= (float) (obj.getRotation(ForgeDirection.UP)*360);
			float rotateYToGo= (float) (obj.getRotation(ForgeDirection.EAST)*360);
			if(oldRotateX==0||oldRotateY==0){oldRotateX=rotateXToGo+0.0001f;oldRotateY=rotateYToGo+0.0001f;}
			if(rotateXToGo==0||rotateYToGo==0){rotateXToGo+=0.0001f;rotateYToGo+=0.0001f;}
			float rotateX= oldRotateX+2*(rotateXToGo-oldRotateX)/Math.abs(rotateXToGo);
			float rotateY= oldRotateY+2*(rotateYToGo-oldRotateY)/Math.abs(rotateYToGo);
			GL11.glRotated(rotateY, 0, 1, 0);
			GL11.glRotated(rotateX, 1, 0, 0);
			oldRotateY=rotateY;
			oldRotateX=rotateX;
		}else super.rotateAroundAxis();
		
		//GL11.glRotated(360, obj.getRotation(EnumFacing.EAST), obj.getRotation(EnumFacing.UP), obj.getRotation(EnumFacing.NORTH));
		
	}
}
