package zmaster587.advancedRocketry.client.render.entity;

import net.minecraft.client.renderer.entity.Render;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.ModelFormatException;
import net.minecraftforge.client.model.obj.WavefrontObject;
import org.lwjgl.opengl.GL11;
import zmaster587.advancedRocketry.client.render.RenderLaser;
import zmaster587.advancedRocketry.entity.EntityElevatorCapsule;

public class RenderElevatorCapsule extends Render {

	private static WavefrontObject sphere;
	public final ResourceLocation capsuleTexture =  new ResourceLocation("advancedRocketry:textures/models/spaceElevatorCapsule.png");

	RenderLaser laser;
	static {

		try {
			sphere = new WavefrontObject(new ResourceLocation("advancedrocketry:models/spaceElevator.obj"));
		} catch(ModelFormatException e) {
			sphere = null;
			e.printStackTrace();
		}
	}

	public RenderElevatorCapsule() {
		laser = new RenderLaser(1, new float[] { 0,0 , 0, 0}, new float[] { 1, 1 , 0, 0.11f} );
	}

	

	@Override
	public void doRender(Entity entityIn, double x, double y, double z,
			float entityYaw, float partialTicks) {
		laser.doRender(entityIn, x - 0.5, y+2.5, z - 0.5, entityYaw, partialTicks);

		EntityElevatorCapsule entity = (EntityElevatorCapsule) entityIn;
		
		GL11.glPushMatrix();
		GL11.glTranslated(x, y + 1, z);
		GL11.glRotated(entityYaw, 0, 1, 0);
		bindTexture(capsuleTexture);
		sphere.renderOnly("Capsule");

		if(entity.isInMotion())
			sphere.renderOnly("Door");

		GL11.glPopMatrix();
	}



	@Override
	protected ResourceLocation getEntityTexture(Entity p_110775_1_) {
		return capsuleTexture;
	}
}
