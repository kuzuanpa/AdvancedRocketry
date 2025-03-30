package zmaster587.advancedRocketry.api.stations;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;
import zmaster587.advancedRocketry.AdvancedRocketry;
import zmaster587.advancedRocketry.api.Constants;

import java.util.Random;

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
    boolean listNeedUpdate = true;
    ResourceLocation texture = new ResourceLocation(Constants.modId, "textures/gui/DysonSphere/node.png");
    public void draw (int x,int y,int distanceFromStarBase,int z,int offsetRotateZ,float scale,float distanceFromStarMultiplier,float rotate) {
        int layerCount = 8;
        GL11.glPushMatrix();
        GL11.glColor4f(1,1,1,0.9F);
        Minecraft.getMinecraft().getTextureManager().bindTexture(texture);
        Tessellator tessellator = Tessellator.instance;
        GL11.glTranslatef(x, y, z);
        GL11.glRotatef(offsetRotateZ, 0, 0, 1);
        if(listNeedUpdate) {
            if(bodyList != -1)GL11.glDeleteLists(bodyList,layerCount);
            bodyList = GL11.glGenLists(layerCount);
            for (int layer = 0; layer < layerCount; layer++) {
                GL11.glNewList(bodyList + layer, GL11.GL_COMPILE);
                for (int i = 0; i < Math.min(Math.pow(count, 0.9),32768) / layerCount; i++) {
                    GL11.glPushMatrix();

                    int size = 1;
                    float s = (layer*1F/layerCount) * 0.2F;
                    float angle = rng.nextFloat() * 360;
                    float nodeY = 3F + rng.nextFloat() * 4.8F;

                    GL11.glRotatef(angle, 0, 1, 0);
                    float f1 = (distanceFromStarBase + distanceFromStarMultiplier * size * distanceFromStarBase / 2F);
                    GL11.glTranslatef(0, (f1 * (nodeY / 12)) - f1 / 2F, -f1 * (0.2F - s) * (float) Math.sin(3.14F * (nodeY + 0.5F) / (12)));
                    GL11.glRotatef(90 * ((nodeY + 1 - 12 / 2F) / 12), 1, 0, 0);
                    GL11.glScalef(scale, scale, 1);
                    GL11.glTranslatef(-16, -16, -f1 / 2);
                    //if(drawNodesCoord)fontRendererObj.drawString(nodeX+","+nodeY, 2,3,0x44aaff);
                    tessellator.startDrawingQuads();
                    tessellator.addVertexWithUV(0, 0, 0, 0, 0);
                    tessellator.addVertexWithUV(0, 8, 0, 0, 0);
                    tessellator.addVertexWithUV(8, 8, 0, 0, 0);
                    tessellator.addVertexWithUV(8, 0, 0, 0, 0);
                    tessellator.draw();
                    GL11.glPopMatrix();
                }
                listNeedUpdate = false;
                GL11.glEndList();
            }
        }

        for (int layer = 0; layer < layerCount; layer++) {
            GL11.glRotatef( (-180- rotate)/18f, 0, 1, 0);
            GL11.glCallList(bodyList+layer);
        }
        GL11.glPopMatrix();
    }
}
