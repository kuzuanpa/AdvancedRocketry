package zmaster587.advancedRocketry.client.render.planet;

import net.minecraft.client.renderer.Tessellator;

/**
 * Draws the flat quads the simulated bodies are painted onto.
 *
 * Bodies live on a shell around the camera, so each quad is built facing straight back at the origin rather
 * than being rotated into place - cheaper than a matrix push per body, and there are a lot of them.
 */
public final class SkyBillboard {

	private SkyBillboard() {}

	/**
	 * Draws a square of the given half-size centred on (x,y,z) and facing the origin.
	 *
	 * @param size half the edge length, in the same units as the position
	 */
	public static void drawFacing(Tessellator tessellator, double x, double y, double z, double size) {
		double len = Math.sqrt(x * x + y * y + z * z);
		if (len < 0.001) return;

		//Unit vector from the camera towards the body; the quad's normal
		double vx = x / len;
		double vy = y / len;
		double vz = z / len;

		//Right vector: the normal crossed with world up, except straight overhead where that degenerates
		double rx, ry, rz;
		if (Math.abs(vx) < 0.001 && Math.abs(vz) < 0.001) {
			rx = 1.0; ry = 0; rz = 0;
		} else {
			rx = vz;
			ry = 0;
			rz = -vx;
			double rLen = Math.sqrt(rx * rx + rz * rz);
			rx /= rLen;
			rz /= rLen;
		}

		//Up vector: right crossed with the normal
		double ux = ry * vz - rz * vy;
		double uy = rz * vx - rx * vz;
		double uz = rx * vy - ry * vx;

		double dxR = rx * size, dyR = ry * size, dzR = rz * size;
		double dxU = ux * size, dyU = uy * size, dzU = uz * size;

		tessellator.startDrawingQuads();
		tessellator.addVertexWithUV(x - dxR - dxU, y - dyR - dyU, z - dzR - dzU, 0, 1);
		tessellator.addVertexWithUV(x + dxR - dxU, y + dyR - dyU, z + dzR - dzU, 1, 1);
		tessellator.addVertexWithUV(x + dxR + dxU, y + dyR + dyU, z + dzR + dzU, 1, 0);
		tessellator.addVertexWithUV(x - dxR + dxU, y - dyR + dyU, z - dzR + dzU, 0, 0);
		tessellator.draw();
	}
}
