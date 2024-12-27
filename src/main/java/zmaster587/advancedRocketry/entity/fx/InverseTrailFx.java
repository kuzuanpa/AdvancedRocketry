package zmaster587.advancedRocketry.entity.fx;

import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.EntityFX;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

import org.lwjgl.opengl.GL11;

public class InverseTrailFx extends EntityFX {
	public static final ResourceLocation icon = new ResourceLocation("advancedrocketry:textures/particle/soft.png");

	
	public InverseTrailFx(World world, double x,
			double y, double z, double motx, double moty, double motz) {
		super(world, x, y, z, motx, moty, motz);
		
		float chroma = this.rand.nextFloat()*0.2f;
        this.particleRed = .8F + chroma;
        this.particleGreen = .8F + chroma;
        this.particleBlue = .8F + chroma;
        this.setSize(0.12F, 0.12F);
        this.particleMaxAge = (int)(100.0D);
        this.particleScale *= this.rand.nextFloat() * 0.6F + 6F + Math.pow(1.04f, this.particleMaxAge);
        this.motionX = -motx;
        this.motionY = -moty;
        this.motionZ = -motz;
        
        
		this.prevPosX = this.posX = this.lastTickPosX = x + motx*this.particleMaxAge;
		this.prevPosY = this.posY = this.lastTickPosY = y + moty*this.particleMaxAge;
		this.prevPosZ = this.posZ = this.lastTickPosZ = z + motz*this.particleMaxAge;
	}

	@Override
	public void renderParticle(Tessellator tess, float x1,
			float y1, float z1, float x2,
			float y2, float z2) {
		
		Minecraft.getMinecraft().getTextureManager().bindTexture(icon);
		
		GL11.glPushMatrix();
		//GL11.glBlendFunc( GL11.GL_ZERO, GL11.GL_ONE_MINUS_SRC_ALPHA );
		//tess.setBrightness(0);
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		
        float f11 = (float)(this.prevPosX + (this.posX - this.prevPosX) * (double)x1 - interpPosX);
        float f12 = (float)(this.prevPosY + (this.posY - this.prevPosY) * (double)x1 - interpPosY);
        float f13 = (float)(this.prevPosZ + (this.posZ - this.prevPosZ) * (double)x1 - interpPosZ);
        float f10 = 0.1F * this.particleScale;
        
        
        
        tess.setColorRGBA_F(this.particleRed, this.particleGreen, this.particleBlue, this.particleAlpha);
        
        tess.addVertexWithUV(f11 - y1 * f10 - y2 * f10, f12 - z1 * f10, f13 - x2 * f10 - z2 * f10, 1, 1);
        tess.addVertexWithUV(f11 - y1 * f10 + y2 * f10, f12 + z1 * f10, f13 - x2 * f10 + z2 * f10, 1, 0);
        tess.addVertexWithUV(f11 + y1 * f10 + y2 * f10, f12 + z1 * f10, f13 + x2 * f10 + z2 * f10, 0, 0);
        tess.addVertexWithUV(f11 + y1 * f10 - y2 * f10, f12 - z1 * f10, f13 + x2 * f10 - z2 * f10, 0, 1);
        //GL11.glEnable(GL11.GL_BLEND);
        
		GL11.glPopMatrix();
	}
	
	@Override
	public int getFXLayer() {
		return 1;
	}
	
	@Override
	public void onUpdate() {
        this.prevPosX = this.posX;
        this.prevPosY = this.posY;
        this.prevPosZ = this.posZ;
        
        //Change color and alpha over lifespan
        this.particleAlpha =  0.25f*this.particleAge/ (float)this.particleMaxAge;
        this.particleScale /= 1.02f;
        
        if (this.particleAge++ >= this.particleMaxAge)
        {
            this.setDead();
        }
        
        this.setPosition(posX + this.motionX, posY + this.motionY, posZ  + this.motionZ);
	}
}