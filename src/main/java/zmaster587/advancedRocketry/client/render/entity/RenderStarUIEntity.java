package zmaster587.advancedRocketry.client.render.entity;

import org.jetbrains.annotations.NotNull;
import org.lwjgl.opengl.GL11;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.entity.Entity;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.ResourceLocation;
import zmaster587.advancedRocketry.api.dimension.solar.StellarBody;
import zmaster587.advancedRocketry.client.render.multiblocks.RendererWarpCore;
import zmaster587.advancedRocketry.dimension.DimensionProperties;
import zmaster587.advancedRocketry.entity.EntityUIStar;
import zmaster587.advancedRocketry.inventory.TextureResources;
import zmaster587.libVulpes.render.RenderHelper;

public class RenderStarUIEntity extends RenderPlanetUIEntity {



	@Override
	protected ResourceLocation getEntityTexture(Entity entity) {
		return DimensionProperties.PlanetIcons.EARTHLIKE.getResource();
	}

	@Override
	public void doRender(@NotNull Entity entity, double x, double y, double z,
                         float entityYaw, float partialTicks) {
		
		EntityUIStar starEntity = (EntityUIStar)entity;
		
		StellarBody body = starEntity.getStarProperties();
		if(body == null)
			return;
		float sizeScale = starEntity.getScale();
		GL11.glPushMatrix();
		GL11.glTranslated(x,y,z);
		GL11.glScalef(sizeScale,sizeScale,sizeScale);
		
		RenderHelper.setupPlayerFacingMatrix(Minecraft.getMinecraft().thePlayer.getDistanceSqToEntity(entity), 0,-.45,0);
		Minecraft.getMinecraft().renderEngine.bindTexture(TextureResources.locationSunNew);
		
		Tessellator buffer = Tessellator.instance;
		
		GL11.glDisable(GL11.GL_LIGHTING);
		GL11.glEnable(GL11.GL_BLEND);
		GL11.glDisable(GL11.GL_DEPTH_TEST);
		OpenGlHelper.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, 0, 0);
		
		GL11.glColor3d(body.getColor()[0], body.getColor()[1], body.getColor()[2]);
		//GL11.glColor3ub((byte)(body.getColorRGB8() & 0xff), (byte)((body.getColorRGB8() >>> 8) & 0xff), (byte)((body.getColorRGB8() >>> 16) & 0xff));
		//GlStateManager.color();
		
		buffer.startDrawingQuads();
		RenderHelper.renderNorthFaceWithUV(buffer, 0, -5, -5, 5, 5, 0, 1, 0, 1);
		buffer.draw();
		
		
		RenderHelper.cleanupPlayerFacingMatrix();
		
		
		//Render hololines
		GL11.glPushMatrix();
		GL11.glScaled(.1, .1, .1);
		OpenGlHelper.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, 0, 0);

		GL11.glDisable(GL11.GL_TEXTURE_2D);

		float myTime;
		
		for(int i = 0; i < 4; i++ ) {
			myTime = ((i*4 + entity.worldObj.getTotalWorldTime() & 0xF)/16f);

			GL11.glColor4f(0, 1f, 1f, .2f*(1-myTime));
			buffer.startDrawingQuads();
			RenderHelper.renderTopFace(buffer, myTime, -.5f, -.5f, .5f, .5f);
			RenderHelper.renderBottomFace(buffer, myTime - 0.5, -.5f, -.5f, .5f, .5f);
			buffer.draw();
		}
		GL11.glAlphaFunc(GL11.GL_GREATER, .1f);
		GL11.glEnable(GL11.GL_TEXTURE_2D);
	
		
		//RenderSelection
		if(starEntity.isSelected()) {
			GL11.glDisable(GL11.GL_TEXTURE_2D);
			double speedRotate = 0.025d;
			GL11.glColor4f(0.4f, 0.4f, 1f, 0.6f);
			GL11.glTranslated(0, -.75f, 0);
			GL11.glPushMatrix();
			GL11.glRotated(speedRotate*System.currentTimeMillis() % 360, 0f, 1f, 0f);
			RendererWarpCore.model.renderOnly("Rotate1");
			GL11.glPopMatrix();

			GL11.glPushMatrix();
			GL11.glRotated(180 + speedRotate*System.currentTimeMillis() % 360, 0f, 1f, 0f);
			RendererWarpCore.model.renderOnly("Rotate1");
			GL11.glPopMatrix();
			GL11.glEnable(GL11.GL_TEXTURE_2D);
		}
		
		GL11.glPopMatrix();
		GL11.glPopMatrix();
		GL11.glEnable(GL11.GL_DEPTH_TEST);
		
		MovingObjectPosition hitObj = Minecraft.getMinecraft().objectMouseOver;
		if(hitObj != null && hitObj.entityHit == entity) {
			
			GL11.glPushMatrix();
			GL11.glColor3f(1, 1, 1);
			GL11.glTranslated(x, y + sizeScale*0.03f, z);
			sizeScale = .1f*sizeScale;
			GL11.glScaled(sizeScale,sizeScale,sizeScale);
			
			//Render atmosphere UI/planet info
			
			//GL11.glDepthMask(false);
			GL11.glDisable(GL11.GL_DEPTH_TEST);
			
			RenderHelper.setupPlayerFacingMatrix(Minecraft.getMinecraft().thePlayer.getDistanceSq(hitObj.hitVec.xCoord, hitObj.hitVec.yCoord, hitObj.hitVec.zCoord), 0, 0, 0);
			
			//Draw Mass indicator
			Minecraft.getMinecraft().renderEngine.bindTexture(RenderPlanetUIEntity.planetUIFG);
			GL11.glColor4f(1, 1, 1,0.8f);
			renderMassIndicator(buffer, body.getTemperature()/200f);
			
			//Draw background
			GL11.glColor4f(1, 1, 1,1);
			Minecraft.getMinecraft().renderEngine.bindTexture(RenderPlanetUIEntity.planetUIBG);
			buffer.startDrawingQuads();
			RenderHelper.renderNorthFaceWithUV(buffer, 1, -40, -25, 40, 55, 1, 0, 1, 0);
			buffer.draw();
			
			//Render planet name
			GL11.glEnable(GL11.GL_DEPTH_TEST);
			//GL11.glDepthMask(true);
			RenderHelper.cleanupPlayerFacingMatrix();
			RenderHelper.renderTag(Minecraft.getMinecraft().thePlayer.getDistanceSq(hitObj.hitVec.xCoord, hitObj.hitVec.yCoord, hitObj.hitVec.zCoord), body.getName(), 0, .9, 0, 5);
			RenderHelper.renderTag(Minecraft.getMinecraft().thePlayer.getDistanceSq(hitObj.hitVec.xCoord, hitObj.hitVec.yCoord, hitObj.hitVec.zCoord), "Num Planets: " + body.getNumPlanets(), 0, .6, 0, 5);

			GL11.glPopMatrix();
		}

		//Clean up and make player not transparent
		GL11.glEnable(GL11.GL_LIGHTING);
		GL11.glDisable(GL11.GL_BLEND);
		GL11.glColor4f(1, 1, 1, 1);
		OpenGlHelper.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, 0, 0);
	}
}
