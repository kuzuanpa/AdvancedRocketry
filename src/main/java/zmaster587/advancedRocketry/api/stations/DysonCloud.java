package zmaster587.advancedRocketry.api.stations;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;
import zmaster587.advancedRocketry.AdvancedRocketry;

import java.util.Random;

import static org.lwjgl.opengl.GL11.*;
import static zmaster587.advancedRocketry.client.render.planet.RenderPlanetarySky.drawTextureRect;

public class DysonCloud implements IDysonSphere{
    public long count = 2048;
    public short yaw = 0, pitch = 0;
    public NBTTagCompound writeToNBT(){
        NBTTagCompound nbt = new NBTTagCompound();
        nbt.setLong("count",count);
        nbt.setShort("yaw",yaw);
        nbt.setShort("pitch",pitch);
        return nbt;
    }

    public DysonCloud readFromNBT(NBTTagCompound nbt){
        try {
            count = nbt.getLong("count");
            yaw = nbt.getShort("yaw");
            pitch = nbt.getShort("pitch");
        }catch (Exception e){
            AdvancedRocketry.logger.error(e);
            return null;
        }

        return this;
    }
    Random rng = new Random();
    int bodyList = -1;
    float lastDrawListArgument = 0;
    ResourceLocation texture = new ResourceLocation("advancedrocketry:textures/env/dyson_sphere_front.png");
    public void draw (int x,int y,float distanceFromStarBase,int z,int offsetRotateZ,float scale,float distanceFromStarMultiplier,float rotate) {
        int layerCount = 8;
        GL11.glPushMatrix();
        Minecraft.getMinecraft().getTextureManager().bindTexture(texture);
        Tessellator tessellator = Tessellator.instance;
        GL11.glTranslatef(x, y, z);
        GL11.glRotatef(offsetRotateZ, 0, 0, 1);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        GL11.glDisable(GL_CULL_FACE);

        GL11.glRotatef( (-180- rotate)/16f, 0, 1, 0);

        if(bodyList == -1)redrawList(layerCount,distanceFromStarBase,scale,distanceFromStarMultiplier,rotate);
        GL11.glScalef(distanceFromStarBase * 0.3F, distanceFromStarBase* 0.3F, distanceFromStarBase* 0.3F);

        GL11.glCallList(bodyList);

        GL11.glEnable(GL_CULL_FACE);

        GL11.glPopMatrix();
    }

    public void redrawList(int layerCount, float distanceFromStarBase,float scale,float distanceFromStarMultiplier,float rotate) {
        if (bodyList != -1) GL11.glDeleteLists(bodyList, 1);
        bodyList = GL11.glGenLists(1);

        GL11.glNewList(bodyList, GL11.GL_COMPILE);
            GL11.glRotatef((-180 - rotate) / 16f, 0, 1, 0);

            for (int i = 0; i < Math.min(Math.pow(count, 0.9), 32768) / layerCount; i++) {
                GL11.glPushMatrix();

                int size = 1;
                float s = (rng.nextFloat() * 8F / layerCount) * 0.4F;
                float angle = rng.nextFloat() * 360;
                float nodeY = 4.8F + rng.nextFloat() * 1.8F;

                GL11.glRotatef(angle, 0, 1, 0);
                float f1 = (distanceFromStarBase + distanceFromStarMultiplier * size * distanceFromStarBase / 2F);
                GL11.glTranslatef(0, (f1 * (nodeY / 12)) - f1 / 2F, -f1 * (0.2F - s) * (float) Math.sin(3.14F * (nodeY + 0.5F) / (12)));
                GL11.glRotatef(90 * ((nodeY + 1 - 12 / 2F) / 12), 1, 0, 0);
                GL11.glScalef(scale, scale, 1);
                GL11.glTranslatef(-16, -16, -f1 / 2);
                drawTextureRect(Tessellator.instance, 0, 0, 0, 0, 0, 8, 8);
                GL11.glPopMatrix();
            }
            lastDrawListArgument = distanceFromStarBase * distanceFromStarMultiplier * scale;
        GL11.glEndList();
    }
}
