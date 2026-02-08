package zmaster587.advancedRocketry.client.render.entity;

import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import zmaster587.advancedRocketry.inventory.TextureResources;

public class RenderCelestialBody extends Render {
    public RenderCelestialBody() {
        this.shadowSize = 0.0F;
    }

    @Override
    public void doRender(Entity entity, double x, double y, double z, float yaw, float partialTicks) {
    }

    public static void drawFacedRect(Tessellator tessellator, double x, double y, double z, double size) {
        double len = Math.sqrt(x * x + y * y + z * z);
        if (len < 0.001) return;

        double vx = x / len;
        double vy = y / len;
        double vz = z / len;

        double rx, ry, rz;
        if (Math.abs(vx) < 0.001 && Math.abs(vz) < 0.001) {
            rx = 1.0; ry = 0; rz = 0;
        } else {
            //(vx, vy, vz) x (0, 1, 0)
            rx = vz;
            ry = 0;
            rz = -vx;
            double rLen = Math.sqrt(rx * rx + rz * rz);
            rx /= rLen;
            rz /= rLen;
        }
        //(rx, ry, rz) x (vx, vy, vz)
        double ux = ry * vz - rz * vy;
        double uy = rz * vx - rx * vz;
        double uz = rx * vy - ry * vx;

        double dxR = rx * size;
        double dyR = ry * size;
        double dzR = rz * size;

        double dxU = ux * size;
        double dyU = uy * size;
        double dzU = uz * size;

        tessellator.startDrawingQuads();
        tessellator.addVertexWithUV(x - dxR - dxU, y - dyR - dyU, z - dzR - dzU, 0, 1);
        tessellator.addVertexWithUV(x + dxR - dxU, y + dyR - dyU, z + dzR - dzU, 1, 1);
        tessellator.addVertexWithUV(x + dxR + dxU, y + dyR + dyU, z + dzR + dzU, 1, 0);
        tessellator.addVertexWithUV(x - dxR + dxU, y - dyR + dyU, z - dzR + dzU, 0, 0);
        tessellator.draw();
    }

    @Override
    protected ResourceLocation getEntityTexture(Entity entity) {
        return TextureResources.locationSunNew;
    }
}