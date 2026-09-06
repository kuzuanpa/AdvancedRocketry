package zmaster587.advancedRocketry.client.render.planet;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.renderer.GLAllocation;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Vec3;
import net.minecraftforge.client.IRenderHandler;
import net.minecraftforge.client.model.AdvancedModelLoader;
import net.minecraftforge.client.model.IModelCustom;
import net.minecraftforge.common.util.ForgeDirection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.opengl.GL11;
import zmaster587.advancedRocketry.api.Configuration;
import zmaster587.advancedRocketry.api.IPlanetaryProvider;
import zmaster587.advancedRocketry.api.dimension.solar.StellarBody;
import zmaster587.advancedRocketry.dimension.DimensionManager;
import zmaster587.advancedRocketry.dimension.DimensionProperties;
import zmaster587.advancedRocketry.dimension.sim.SimUniverse;
import zmaster587.advancedRocketry.event.RocketEventHandler;
import zmaster587.advancedRocketry.inventory.TextureResources;
import zmaster587.advancedRocketry.stations.SpaceObject;
import zmaster587.advancedRocketry.stations.SpaceObjectManager;
import zmaster587.advancedRocketry.util.AstronomicalBodyHelper;
import zmaster587.libVulpes.util.Vector3F;

import java.util.*;

public class RenderPlanetarySky extends IRenderHandler {

	private static int sunList;
	final int starGLCallList;
	final int glSkyList;
	final int glSkyList2;
	float celestialAngle;
	final Vector3F<Float> axis;

	IModelCustom sunModel = AdvancedModelLoader.loadModel(new ResourceLocation("advancedrocketry:models/star.obj"));
	ResourceLocation sunTexture = new ResourceLocation("advancedrocketry:textures/env/sunLEO.png");

	/** Scratch list for depth sorting the simulated bodies, reused every frame */
	private final List<SimUniverse.SimBody> sortedBodies = new ArrayList<>();

	private static final double BODY_APPARENT_SIZE = 0.3D;

	private static final double MAX_APPARENT_SHARE = 0.5D;

	private static final double STAR_MODEL_SCALE = 4.0D;

	final Minecraft mc = Minecraft.getMinecraft();

	public RenderPlanetarySky() {
		axis = new Vector3F<>(1f, 0f, 0f);

		GL11.glNewList(sunList = GL11.glGenLists(1), GL11.GL_COMPILE);
		sunModel.renderPart("Cube");
		GL11.glEndList();

		this.starGLCallList = GLAllocation.generateDisplayLists(3);
		GL11.glPushMatrix();
		GL11.glNewList(this.starGLCallList, GL11.GL_COMPILE);
		this.drawStarList();
		GL11.glEndList();
		GL11.glPopMatrix();
		Tessellator tessellator = Tessellator.instance;
		this.glSkyList = this.starGLCallList + 1;
		GL11.glNewList(this.glSkyList, GL11.GL_COMPILE);
		byte b2 = 64;
		int i = 256 / b2 + 2;
		float f = 16.0F;
		int j;
		int k;

		for (j = -b2 * i; j <= b2 * i; j += b2)
		{
			for (k = -b2 * i; k <= b2 * i; k += b2)
			{
				tessellator.startDrawingQuads();
				tessellator.addVertex(j, f, k);
				tessellator.addVertex(j + b2, f, k);
				tessellator.addVertex(j + b2, f, k + b2);
				tessellator.addVertex(j, f, k + b2);
				tessellator.draw();
			}
		}

		GL11.glEndList();
		this.glSkyList2 = this.starGLCallList + 2;
		GL11.glNewList(this.glSkyList2, GL11.GL_COMPILE);
		f = -16.0F;
		tessellator.startDrawingQuads();

		for (j = -b2 * i; j <= b2 * i; j += b2)
		{
			for (k = -b2 * i; k <= b2 * i; k += b2)
			{
				tessellator.addVertex(j + b2, f, k);
				tessellator.addVertex(j, f, k);
				tessellator.addVertex(j, f, k + b2);
				tessellator.addVertex(j + b2, f, k + b2);
			}
		}

		tessellator.draw();
		GL11.glEndList();
	}
	private void drawStarList()	{
		Random random = new Random(10842L);
		Tessellator tessellator = Tessellator.instance;
		tessellator.startDrawingQuads();

		for (int i = 0; i < 2000; ++i)
		{
			double d0 = random.nextFloat() * 2.0F - 1.0F;
			double d1 = random.nextFloat() * 2.0F - 1.0F;
			double d2 = random.nextFloat() * 2.0F - 1.0F;
			double d3 = 0.15F + random.nextFloat() * 0.1F;
			double d4 = d0 * d0 + d1 * d1 + d2 * d2;

			if (d4 < 1.0D && d4 > 0.01D)
			{
				d4 = 1.0D / Math.sqrt(d4);
				d0 *= d4;
				d1 *= d4;
				d2 *= d4;
				double d5 = d0 * 100.0D;
				double d6 = d1 * 100.0D;
				double d7 = d2 * 100.0D;
				double d8 = Math.atan2(d0, d2);
				double d9 = Math.sin(d8);
				double d10 = Math.cos(d8);
				double d11 = Math.atan2(Math.sqrt(d0 * d0 + d2 * d2), d1);
				double d12 = Math.sin(d11);
				double d13 = Math.cos(d11);
				double d14 = random.nextDouble() * Math.PI * 2.0D;
				double d15 = Math.sin(d14);
				double d16 = Math.cos(d14);

				for (int j = 0; j < 4; ++j)
				{
					double d17 = 0.0D;
					double d18 = (double)((j & 2) - 1) * d3;
					double d19 = (double)((j + 1 & 2) - 1) * d3;
					double d20 = d18 * d16 - d19 * d15;
					double d21 = d19 * d16 + d18 * d15;
					double d22 = d20 * d12 + d17 * d13;
					double d23 = d17 * d12 - d20 * d13;
					double d24 = d23 * d9 - d21 * d10;
					double d25 = d21 * d9 + d23 * d10;
					tessellator.addVertex(d5 + d24, d6 + d22, d7 + d25);
				}
			}
		}

		tessellator.draw();
	}

	public static void drawTextureRect(Tessellator tessellator, int x, int y, int z, int u, int v, int width, int height){
		drawTextureRect(tessellator, x, y, z, u, v, width, height, width, height);
	}

	/**
	 * Same as {@link #drawTextureRect} but lets the quad be a different size to the sampled region, so a
	 * 32x32 atlas glyph can be drawn as a small speck without stretching the UVs.
	 */
	public static void drawTextureRect(Tessellator tessellator, int x, int y, int z, int u, int v, int uWidth, int vHeight, int width, int height){
		final float f = 0.00390625F;
		final float f1 = 0.00390625F;
		tessellator.startDrawingQuads();
		tessellator.addVertexWithUV(x, y + height, z, (u * f), (v + vHeight) * f1);
		tessellator.addVertexWithUV(x + width, y + height, z, (u + uWidth) * f, (v + vHeight) * f1);
		tessellator.addVertexWithUV(x + width, y, z, (u + uWidth) * f, v * f1);
		tessellator.addVertexWithUV(x, y, z, (u * f), v * f1);
		tessellator.draw();
	}
	protected ForgeDirection getRotationAxis(DimensionProperties properties, int posX, int posZ) { return ForgeDirection.EAST; }
	protected ResourceLocation getTextureForPlanet(DimensionProperties properties) {
		return properties.getPlanetIcon();
	}
	protected float getSkyRotationAmount() {return celestialAngle;}
	protected Vector3F<Float> getRotateAxis() { return axis;}
	protected void rotateAroundAxis() {
		Vector3F<Float> axis = getRotateAxis();
		GL11.glRotatef(getSkyRotationAmount() * 360.0F, axis.x, axis.y, axis.z);
	}

	/**
	 * Where in the simulated universe the camera is, in sim block coordinates.  On a planet that is the body
	 * itself; subclasses standing somewhere else (in space, on a station) override this.
	 */
	public Vector3F<Double> getViewpoint(float partialTicks, EntityPlayer player){
		SimUniverse.SimBody body = SimUniverse.getInstance().getBodyForDim(mc.theWorld.provider.dimensionId);
		if(body == null) return null;
		return new Vector3F<>(body.x, body.y, body.z);
	}
	@Override
	public void render(float partialTicks, @NotNull WorldClient world, @NotNull Minecraft mc) {

		//TODO: properly handle this
		float atmosphere;
		int solarOrbitalDistance, planetOrbitalDistance = 0;
		double myPhi = 0, myTheta = 0, myPrevOrbitalTheta = 0, myRotationalPhi = 0;
		boolean hasAtmosphere = false, isMoon;
		float[] parentAtmColor = new float[]{1f,1f,1f};
		float[] ringColor = {0f,0f,0f};
		float[] parentRingColor = {0f,0f,0f};
		float sunSize = 1.0f;
		float starSeperation = 0f;
		boolean isWarp = false;
		boolean isGasGiant = false;
		boolean hasRings = false;
		boolean parentHasRings = false;
		DimensionProperties parentProperties;
		DimensionProperties properties;
		@Nullable ForgeDirection travelDirection = null;
		ResourceLocation parentPlanetIcon = null;
		List<DimensionProperties> children;
		List<StellarBody> subStars = new LinkedList<>();
		StellarBody primaryStar;
		celestialAngle = mc.theWorld.getCelestialAngle(partialTicks);

		Vec3 sunColor;
		setupDimProperties:{
			if (mc.theWorld.provider instanceof IPlanetaryProvider) {
				IPlanetaryProvider planetaryProvider = (IPlanetaryProvider) mc.theWorld.provider;

				properties = (DimensionProperties) planetaryProvider.getDimensionProperties((int) mc.thePlayer.posX, (int) mc.thePlayer.posZ);

				atmosphere = planetaryProvider.getAtmosphereDensityFromHeight(mc.renderViewEntity.posY, (int) mc.thePlayer.posX, (int) mc.thePlayer.posZ);

				ForgeDirection dir = getRotationAxis(properties, (int) mc.thePlayer.posX, (int) mc.thePlayer.posZ);
				axis.x = (float) dir.offsetX;
				axis.y = (float) dir.offsetY;
				axis.z = (float) dir.offsetZ;

				myPhi = properties.orbitalPhi;
				myTheta = properties.orbitTheta;
				myRotationalPhi = properties.rotationalPhi;
				myPrevOrbitalTheta = properties.prevOrbitalTheta;
				hasRings = properties.hasRings();
				ringColor = properties.ringColor;

				children = new LinkedList<>();
				for (Integer i : properties.getChildPlanets()) {
					children.add(DimensionManager.getInstance().getDimensionProperties(i));
				}

				solarOrbitalDistance = properties.getSolarOrbitalDistance();

				isMoon = properties.isMoon();
				if (isMoon) {
					parentProperties = properties.getParentProperties();
					isGasGiant = parentProperties.isGasGiant();
					hasAtmosphere = parentProperties.hasAtmosphere();
					planetOrbitalDistance = properties.getParentOrbitalDistance();
					parentAtmColor = parentProperties.skyColor;
					parentPlanetIcon = getTextureForPlanet(parentProperties);
					parentHasRings = parentProperties.hasRings;
					parentRingColor = parentProperties.ringColor;

				}

				sunColor = planetaryProvider.getSunColor((int) mc.thePlayer.posX, (int) mc.thePlayer.posZ);
				primaryStar = properties.getStar();
				if (primaryStar != null) {
					sunSize = primaryStar.getSize();
					subStars = primaryStar.getSubStars();
				} else primaryStar = DimensionManager.getInstance().getStar(0);

				if (world.provider.dimensionId == Configuration.stationDimId) {
					isWarp = properties.getParentPlanet() == SpaceObjectManager.WARPDIMID;
					if (isWarp) {
						SpaceObject station = (SpaceObject) SpaceObjectManager.getSpaceManager().getSpaceStationFromBlockCoords((int) mc.thePlayer.posX, (int) mc.thePlayer.posZ);
						travelDirection = station.getForwardDirection();
					}
				}
			} else if (DimensionManager.getInstance().isDimensionCreated(mc.theWorld.provider.dimensionId)) {

				properties = DimensionManager.getInstance().getDimensionProperties(mc.theWorld.provider.dimensionId);

				atmosphere = properties.getAtmosphereDensityAtHeight(mc.renderViewEntity.posY);//planetaryProvider.getAtmosphereDensityFromHeight(mc.getRenderViewEntity().posY, mc.player.getPosition());
				ForgeDirection dir = getRotationAxis(properties, (int) mc.thePlayer.posX, (int) mc.thePlayer.posZ);
				axis.x = (float) dir.offsetX;
				axis.y = (float) dir.offsetY;
				axis.z = (float) dir.offsetZ;

				myPhi = properties.orbitalPhi;
				myTheta = properties.orbitTheta;
				myRotationalPhi = properties.rotationalPhi;
				myPrevOrbitalTheta = properties.prevOrbitalTheta;
				hasRings = properties.hasRings();
				ringColor = properties.ringColor;

				children = new LinkedList<>();
				for (Integer i : properties.getChildPlanets()) {
					children.add(DimensionManager.getInstance().getDimensionProperties(i));
				}

				solarOrbitalDistance = properties.getSolarOrbitalDistance();

				isMoon = properties.isMoon();
				if (isMoon) {
					parentProperties = properties.getParentProperties();
					isGasGiant = parentProperties.isGasGiant();
					hasAtmosphere = parentProperties.hasAtmosphere();
					planetOrbitalDistance = properties.getParentOrbitalDistance();
					parentAtmColor = parentProperties.skyColor;
					parentPlanetIcon = getTextureForPlanet(parentProperties);
					parentHasRings = parentProperties.hasRings;
					parentRingColor = parentProperties.ringColor;
				}

				float[] sunColorFloat = properties.getSunColor();

				sunColor = Vec3.createVectorHelper(sunColorFloat[0], sunColorFloat[1], sunColorFloat[2]);//planetaryProvider.getSunColor(mc.player.getPosition());

				primaryStar = properties.getStar();
				if (primaryStar != null) {
					sunSize = primaryStar.getSize();
					subStars = primaryStar.getSubStars();
				} else
					primaryStar = DimensionManager.getInstance().getStar(0);

			} else {
				children = new LinkedList<>();
				isMoon = false;
				hasAtmosphere = DimensionManager.overworldProperties.hasAtmosphere();
				atmosphere = DimensionManager.overworldProperties.getAtmosphereDensityAtHeight(mc.renderViewEntity.posY);
				solarOrbitalDistance = DimensionManager.overworldProperties.orbitalDist;
				primaryStar = DimensionManager.overworldProperties.getStar();
				sunColor = Vec3.createVectorHelper(1, 1, 1);
				properties = DimensionManager.overworldProperties;
			}
		}

		GL11.glDisable(GL11.GL_TEXTURE_2D);
		Vec3 vec3 = Minecraft.getMinecraft().theWorld.getSkyColor(this.mc.renderViewEntity, partialTicks);
		float f1 = (float)vec3.xCoord;
		float f2 = (float)vec3.yCoord;
		float f3 = (float)vec3.zCoord;
		float f6;

		//3D Effect
		if (this.mc.gameSettings.anaglyph) {
			float f4 = (f1 * 30.0F + f2 * 59.0F + f3 * 11.0F) / 100.0F;
			float f5 = (f1 * 30.0F + f2 * 70.0F) / 100.0F;
			f6 = (f1 * 30.0F + f3 * 70.0F) / 100.0F;
			f1 = f4;
			f2 = f5;
			f3 = f6;
		}

		//Simulate atmospheric thickness
		f1 *= atmosphere;
		f2 *= atmosphere;
		f3 *= atmosphere;

		Tessellator tessellator1 = Tessellator.instance;
		GL11.glDepthMask(false);

		GL11.glEnable(GL11.GL_FOG);
		GL11.glColor3f(f1, f2, f3);
		GL11.glCallList(this.glSkyList);
		GL11.glDisable(GL11.GL_FOG);
		GL11.glDisable(GL11.GL_ALPHA_TEST);
		GL11.glEnable(GL11.GL_BLEND);
		OpenGlHelper.glBlendFunc(770, 771, 1, 0);
		RenderHelper.disableStandardItemLighting();
		float[] sunriseSunsetColors = mc.theWorld.provider.calcSunriseSunsetColors(celestialAngle, partialTicks);
		float f7;
		float f8;
		float f9;
		float f10;


		drawSunRiseSetColor: if (sunriseSunsetColors != null) {
			GL11.glDisable(GL11.GL_TEXTURE_2D);
			GL11.glShadeModel(GL11.GL_SMOOTH);
			GL11.glPushMatrix();
			GL11.glRotatef(90.0F, 1.0F, 0.0F, 0.0F);
			GL11.glRotatef(MathHelper.sin(mc.theWorld.getCelestialAngleRadians(partialTicks)) < 0.0F ? 180.0F : 0.0F, 0.0F, 0.0F, 1.0F);
			GL11.glRotatef(90.0F, 0.0F, 0.0F, 1.0F);

			//Sim atmospheric thickness
			f6 = sunriseSunsetColors[0];
			f7 = sunriseSunsetColors[1];
			f8 = sunriseSunsetColors[2];
			float f11;

			//3D Effect
			if (this.mc.gameSettings.anaglyph) {
				f9 = (f6 * 30.0F + f7 * 59.0F + f8 * 11.0F) / 100.0F;
				f10 = (f6 * 30.0F + f7 * 70.0F) / 100.0F;
				f11 = (f6 * 30.0F + f8 * 70.0F) / 100.0F;
				f6 = f9;
				f7 = f10;
				f8 = f11;
			}

			tessellator1.startDrawing(6);
			tessellator1.setColorRGBA_F(f6, f7, f8, sunriseSunsetColors[3] * atmosphere);
			tessellator1.addVertex(0.0D, 100.0D, 0.0D);
			byte b0 = 16;
			tessellator1.setColorRGBA_F(sunriseSunsetColors[0], sunriseSunsetColors[1], sunriseSunsetColors[2], 0.0F);

			for (int j = 0; j <= b0; ++j)
			{
				f11 = (float)j * (float)Math.PI * 2.0F / (float)b0;
				float f12 = MathHelper.sin(f11);
				float f13 = MathHelper.cos(f11);
				tessellator1.addVertex(f12 * 120.0F, f13 * 120.0F, -f13 * 40.0F * sunriseSunsetColors[3]);
			}

			tessellator1.draw();
			GL11.glPopMatrix();
			GL11.glShadeModel(GL11.GL_FLAT);
		}

		float atmosphereRainAlpha = 1.0F;
		if (atmosphere > 0) atmosphereRainAlpha = 1.0F - (mc.theWorld.getRainStrength(partialTicks) * (atmosphere / 100f));

        GL11.glDisable(GL11.GL_TEXTURE_2D);
		float starBrightness = mc.theWorld.getStarBrightness(partialTicks) * atmosphereRainAlpha * (atmosphere) + (1 - atmosphere);
		if (mc.theWorld.isRaining()) starBrightness *= 1 - mc.theWorld.getRainStrength(partialTicks);

		drawRandomStars: {
			if (starBrightness > 0.0F) {
                GL11.glColor4f(1.0F, 1.0F, 1.0F, starBrightness);
				GL11.glPushMatrix();
				GL11.glRotatef(-90.0F, 0.0F, 1.0F, 0.0F);
				if(!isWarp) rotateAroundAxis();
				if (isWarp) {
					for (int i = -3; i < 5; i++) {
						GL11.glPushMatrix();
						double magnitude = i * -100 + (((System.currentTimeMillis()) + 50) % 2000) / 20f;
						GL11.glTranslated(-travelDirection.offsetZ * magnitude, 0, travelDirection.offsetX * magnitude);
						GL11.glCallList(this.starGLCallList);
						GL11.glPopMatrix();
					}
					//GL11.glTranslated(((System.currentTimeMillis()/10) + 50) % 100, 0, 0);
				} else {
					GL11.glColor4f(1.0F, 1.0F, 1.0F, starBrightness);
					GL11.glCallList(this.starGLCallList);
					GL11.glRotatef(-90,1,0,0);
					GL11.glCallList(this.starGLCallList);
				}
				GL11.glPopMatrix();
			}
			GL11.glEnable(GL11.GL_TEXTURE_2D);
		}

		EntityPlayer player = mc.thePlayer;

		GL11.glDisable(GL11.GL_FOG);
		GL11.glDisable(GL11.GL_ALPHA_TEST);
		GL11.glEnable(GL11.GL_BLEND);
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

		GL11.glEnable(GL11.GL_DEPTH_TEST);
		GL11.glDepthMask(true);
		final Vector3F<Double> viewpoint = getViewpoint(partialTicks, player);

		Tessellator tessellator = Tessellator.instance;
		GL11.glPushMatrix();
		if(!isWarp) rotateAroundAxis();

		//A null viewpoint means we are somewhere the simulation does not know about, so there is nothing to
		//draw the sky relative to; the random star field above still gives a backdrop
		drawSimulatedBodies: if(viewpoint != null) {
			//Reused between frames: this runs every frame and the body count is fixed
			sortedBodies.clear();
			sortedBodies.addAll(SimUniverse.getInstance().getAllBodies());

			//Farthest first, so nearer bodies paint over them
			Collections.sort(sortedBodies, (b1, b2) -> Double.compare(b2.distanceSqTo(viewpoint.x, viewpoint.y, viewpoint.z),
                    b1.distanceSqTo(viewpoint.x, viewpoint.y, viewpoint.z)));

			for (SimUniverse.SimBody body : sortedBodies) {
				double dx = body.x - viewpoint.x;
				double dy = body.y - viewpoint.y;
				double dz = body.z - viewpoint.z;
				double dist = Math.sqrt(dx * dx + dy * dy + dz * dz);

				//The body we are standing on
				if(dist <= 0.1) continue;

				//Nearby bodies stay lit even in full daylight; distant ones fade with the star field
				float stellarBright = (float) Math.max(starBrightness, 1F - dist/500F);
				if(stellarBright < 0.01F) continue;

				//Everything is painted onto a shell around the camera.  Farther bodies get pushed slightly
				//further out so a planet can never be drawn in front of the star it orbits.
				double depthOffset = Math.min(dist / 100.0, 75);
				double shell = 25F + depthOffset;
				double scale = shell / dist;
				double renderX = dx * scale;
				double renderY = dy * scale;
				double renderZ = dz * scale;

				double renderSize = Math.min(shell * MAX_APPARENT_SHARE,
						Math.max(body.getCaptureRadius() * scale * BODY_APPARENT_SIZE, 0.01f));

				GL11.glColor4f(1.0F, 1.0F, 1.0F, stellarBright);

				if(body.isStar()) {
					StellarBody stellar = DimensionManager.getInstance().getStar(body.getConfig().getStarId());
					if(stellar == null) continue;

					float[] colorArray = stellar.getColor();
					Vec3 stellarColor = Vec3.createVectorHelper(colorArray[0], colorArray[1], colorArray[2]);

					if(renderSize > .05F) {
						drawStar(tessellator, (float)renderX, (float)renderY, (float)renderZ, solarOrbitalDistance, (float)(renderSize / STAR_MODEL_SCALE), stellarColor, partialTicks, properties, stellar, stellarBright);
					} else {
						//Too small for the model to be worth it - a coloured speck reads the same
						GL11.glColor4f((float) Math.min(1.0F, stellarColor.xCoord*1.2F), (float)Math.min(1.0F, stellarColor.yCoord*1.2F), (float)Math.min(1.0F, stellarColor.zCoord*1.2F), stellarBright);
						mc.renderEngine.bindTexture(TextureResources.locationSunLODFar);
						SkyBillboard.drawFacing(tessellator, renderX, renderY, renderZ, renderSize);
					}
				}
				else {
					//getPropertiesId, not getLandingDimId: a gas giant has an icon to draw even though there is
					//nowhere on it to land
					int dimId = body.getConfig().getPropertiesId();
					if(dimId == SimUniverse.NO_DIMENSION) continue;

					mc.renderEngine.bindTexture(DimensionManager.getInstance().getDimensionProperties(dimId).getPlanetIcon());
					SkyBillboard.drawFacing(tessellator, renderX, renderY, renderZ, renderSize);
				}
			}
		}


		GL11.glPopMatrix();
		GL11.glEnable(GL11.GL_ALPHA_TEST);
		GL11.glDisable(GL11.GL_BLEND);


		GL11.glEnable(GL11.GL_TEXTURE_2D);
		OpenGlHelper.glBlendFunc(770, 1, 1, 0);
		GL11.glPushMatrix();


		f7 = 0.0F;
		f8 = 0.0F;
		f9 = 0.0F;
		GL11.glColor4f(1.0F, 1.0F, 1.0F, starBrightness);
		GL11.glTranslatef(f7, f8, f9);
		GL11.glRotatef(-90.0F, 0.0F, 1.0F, 0.0F);

		GL11.glRotatef((float)myRotationalPhi, 0f, 1f, 0f);

		float multiplier = (2-atmosphere)/2.2f;//atmosphere > 1 ? (2-atmosphere) : 1f;
		multiplier *= 1-mc.theWorld.getRainStrength(partialTicks);

		drawRings: if(hasRings) {
			GL11.glPushMatrix();
			GL11.glRotatef(90f, 0f, 1f, 0f);

			f10 = 100;
			double ringDist = 0;
			mc.renderEngine.bindTexture(DimensionProperties.planetRings);

			GL11.glRotated(80, 1, 0, 0);
			GL11.glTranslated(0, -10, 50);
			GL11.glRotated(0.00036*(System.currentTimeMillis()%1000000),0,1,0);

			GL11.glColor4f(ringColor[0], ringColor[1], ringColor[2],multiplier);
			tessellator1.startDrawing(GL11.GL_QUADS);
			tessellator1.addVertexWithUV(f10, ringDist, -f10,1.0D, 0.0D);
			tessellator1.addVertexWithUV(-f10, ringDist, -f10, 0.0D, 0.0D);
			tessellator1.addVertexWithUV(-f10, ringDist, f10, 0.0D, 1.0D);
			tessellator1.addVertexWithUV(f10, ringDist, f10, 1.0D, 1.0D);
			tessellator1.draw();
			GL11.glPopMatrix();

			GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
			GL11.glPushMatrix();

			GL11.glRotatef(90f, 0f, 1f, 0f);
			GL11.glRotated(70, 1, 0, 0);
			GL11.glTranslated(0, -10, 50);

			mc.renderEngine.bindTexture(DimensionProperties.planetRingShadow);
			GL11.glColor4f(0f, 0f, 0f,1);
			tessellator1.startDrawing(GL11.GL_QUADS);
			tessellator1.addVertexWithUV(f10, ringDist, -f10,1.0D, 0.0D);
			tessellator1.addVertexWithUV(-f10, ringDist, -f10, 0.0D, 0.0D);
			tessellator1.addVertexWithUV(-f10, ringDist, f10, 0.0D, 1.0D);
			tessellator1.addVertexWithUV(f10, ringDist, f10, 1.0D, 1.0D);
			tessellator1.draw();
			GL11.glPopMatrix();

			OpenGlHelper.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE, 1, 0);
		}

		if(!isWarp) rotateAroundAxis();

		GL11.glPopMatrix();

		drawExtra(tessellator1, properties, multiplier, sunColor);
		GL11.glEnable(GL11.GL_FOG);
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		GL11.glDisable(GL11.GL_BLEND);
		GL11.glEnable(GL11.GL_ALPHA_TEST);

		GL11.glDisable(GL11.GL_TEXTURE_2D);
		GL11.glColor3f(0.0F, 0.0F, 0.0F);

		if (mc.theWorld.provider.isSkyColored())
		{
			GL11.glColor3f(f1 * 0.2F + 0.04F, f2 * 0.2F + 0.04F, f3 * 0.6F + 0.1F);
		}
		else
		{
			GL11.glColor3f(f1, f2, f3);
		}

		//Blackness @ bottom of world
		/*GL11.glPushMatrix();
		GL11.glTranslatef(0.0F, -((float)(d0 - 16.0D)), 0.0F);
		GL11.glCallList(this.glSkyList2);
		GL11.glPopMatrix();*/

		GL11.glEnable(GL11.GL_TEXTURE_2D);
		//Vanilla clears depth, draws the sky, then draws terrain.  The bodies above are drawn with depth writes
		//on so the 3D star model self-occludes, which would then cull any terrain further away than the shell
		//they sit on - so hand the terrain pass a clean buffer.
		GL11.glClear(GL11.GL_DEPTH_BUFFER_BIT);
		GL11.glDepthMask(true);

		RocketEventHandler.onPostWorldRender(partialTicks);
	}
	protected void drawStar(Tessellator buffer, float x, float y, float z, int solarOrbitalDistance, float sunSize, Vec3 sunColor, float partialTicks, DimensionProperties properties, @Nullable StellarBody sun, float multiplier) {
		if(sun == null)return;
		GL11.glPushMatrix();
		GL11.glTranslated(x,y,z);
		GL11.glEnable(GL11.GL_BLEND);

		float f10 = sunSize*15f;
		float i1= f10 * 0.6F;
        if(sun.dysonSphere != null)sun.dysonSphere.draw(0,0, i1,0,90,i1/500F,0.8F,(System.currentTimeMillis() % 36000) / 100F / (sun.dysonSphere.size+1));

		//Set sun color
		GL11.glColor4f((float)sunColor.xCoord, (float)sunColor.yCoord , (float)sunColor.zCoord , multiplier);
		GL11.glColor4f((float) Math.min(1.0F, sunColor.xCoord*1.2F), (float)Math.min(1.0F, sunColor.yCoord*1.2F) , (float)Math.min(1.0F, sunColor.zCoord*1.2F) , (float) Math.min(0.9,multiplier));
		GL11.glDepthMask(false);
		boolean enable3DSun = true;
		if(enable3DSun) {
			GL11.glPushMatrix();
			mc.getTextureManager().bindTexture(sunTexture);
			GL11.glRotated(90, 0, 0, 1);

			GL11.glRotated(-(System.currentTimeMillis() % 360000) / 1000F, 0, 1, 0);
			GL11.glScalef(f10 * 0.3F, f10 * 0.3F, f10 * 0.3F);
			GL11.glCallList(sunList);

			GL11.glPopMatrix();
		}
		else {
			mc.renderEngine.bindTexture(TextureResources.locationSunNew);
			//Set sun color and distance
			GL11.glColor4f((float)sunColor.xCoord, (float)sunColor.yCoord , (float)sunColor.zCoord , (float) Math.min(0.9,multiplier));
			SkyBillboard.drawFacing(buffer,x *.9F,y *.9F,z *.9F,sunSize*32);
		}
		if(sun.dysonCloud != null)sun.dysonCloud.draw(0,0, i1,0,90,i1/500F,1.2F,(System.currentTimeMillis() % 36000) / 100F);

		GL11.glPopMatrix();
		GL11.glPushMatrix();
		GL11.glColor4f((float) Math.min(1.0F, sunColor.xCoord*1.2F), (float)Math.min(1.0F, sunColor.yCoord*1.2F) , (float)Math.min(1.0F, sunColor.zCoord*1.2F) , (float) (multiplier* 1.2F));

		mc.getTextureManager().bindTexture(TextureResources.locationStarLight);
		SkyBillboard.drawFacing(buffer,x *.9F,y *.9F,z *.9F,sunSize*8);

		GL11.glDepthMask(true);



		GL11.glPopMatrix();

	}

	protected void drawExtra(Tessellator buffer, DimensionProperties properties, float alphaMultiplier, Vec3 sunColor){

	}
	protected void renderPlanet(Tessellator buffer, ResourceLocation icon, float planetOrbitalDistance, float alphaMultiplier, double shadowAngle, boolean hasAtmosphere, float[] skyColor, float[] ringColor, boolean gasGiant, boolean hasRing, Vec3 sunColor) {
		renderPlanet2(buffer, icon, 0, 0, -100, 10f*AstronomicalBodyHelper.getBodySizeMultiplier(planetOrbitalDistance), alphaMultiplier, shadowAngle, hasAtmosphere, skyColor, ringColor, gasGiant, hasRing, sunColor);
	}

	protected void renderPlanet2(Tessellator buffer, ResourceLocation icon, int locationX, int locationY, double zLevel, float size, float alphaMultiplier, double shadowAngle, boolean hasAtmosphere, float[] skyColor, float[] ringColor, boolean gasGiant, boolean hasRing, Vec3 sunColor) {
		renderPlanetPubHelper(buffer, icon, locationX, locationY, zLevel, size, alphaMultiplier, shadowAngle, hasAtmosphere, skyColor, ringColor, gasGiant, hasRing);
	}
	public static void renderPlanetPubHelper(@NotNull Tessellator tessellator1, ResourceLocation icon, int locationX, int locationY, double zLevel, float size, float alphaMultiplier, double shadowAngle, boolean hasAtmosphere, float[] skyColor, float[] ringColor, boolean gasGiant, boolean hasRing) {
		GL11.glEnable(GL11.GL_BLEND);
		//Set planet Orbiting distance; size

		GL11.glPushMatrix();
		GL11.glTranslated(locationX, zLevel, locationY);

		//ATM Glow
		GL11.glPushMatrix();
		GL11.glRotated(90-shadowAngle* 180/Math.PI, 0, 1, 0);

		//Rings
		if(hasRing) {
			GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
			GL11.glColor4f(ringColor[0], ringColor[1], ringColor[2], alphaMultiplier*0.4f);
			float ringSize = size *1.4f;

			Minecraft.getMinecraft().renderEngine.bindTexture(DimensionProperties.planetRings);
			tessellator1.startDrawing(GL11.GL_QUADS);

			tessellator1.addVertexWithUV(-ringSize, zLevel-0.01f, ringSize, 0f, 1f);
			tessellator1.addVertexWithUV(ringSize, zLevel-0.01f, ringSize, 1f, 1f);
			tessellator1.addVertexWithUV(ringSize, zLevel-0.01f, -ringSize, 1f, 0f);
			tessellator1.addVertexWithUV(-ringSize, zLevel-0.01f, -ringSize, 0f, 0f);
			tessellator1.draw();


			GL11.glColor4f(0f, 0f, 0f, alphaMultiplier);
			Minecraft.getMinecraft().renderEngine.bindTexture(DimensionProperties.planetRingShadow);
			tessellator1.startDrawing(GL11.GL_QUADS);
			tessellator1.addVertexWithUV(-ringSize, zLevel-0.01f, ringSize, 0f, 1f);
			tessellator1.addVertexWithUV(ringSize, zLevel-0.01f, ringSize, 1f, 1f);
			tessellator1.addVertexWithUV(ringSize, zLevel-0.01f, -ringSize, 1f, 0f);
			tessellator1.addVertexWithUV(-ringSize, zLevel-0.01f, -ringSize, 0f, 0f);
			tessellator1.draw();
		}

		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);
		tessellator1.startDrawing(GL11.GL_QUADS);
		Minecraft.getMinecraft().renderEngine.bindTexture(DimensionProperties.atmGlow);

		GL11.glColor4f(1f, 1f, 1f, alphaMultiplier);
		tessellator1.addVertexWithUV(-size, zLevel+0.01f, size, 0f, 1f);
		tessellator1.addVertexWithUV(size, zLevel+0.01f, size, 1f, 1f);
		tessellator1.addVertexWithUV(size, zLevel+0.01f, -size, 1f, 0f);
		tessellator1.addVertexWithUV(-size, zLevel+0.01f, -size, 0f, 0f);
		tessellator1.draw();
		GL11.glPopMatrix();

		//End ATM glow

		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		Minecraft.getMinecraft().renderEngine.bindTexture(icon);
		//TODO: draw sky planets

		tessellator1.startDrawingQuads();

		tessellator1.setColorRGBA_F(1f, 1f, 1f, alphaMultiplier);

		tessellator1.addVertexWithUV(-size, zLevel, size, 0f, 1f);
		tessellator1.addVertexWithUV(size, zLevel, size, 1f, 1f);
		tessellator1.addVertexWithUV(size, zLevel, -size, 1f, 0f);
		tessellator1.addVertexWithUV(-size, zLevel, -size, 0f, 0f);

		tessellator1.draw();
		GL11.glEnable(GL11.GL_BLEND);

		//GL11.glPopAttrib();

		//Draw atmosphere if applicable
		if(hasAtmosphere) {
			GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);

			tessellator1.startDrawingQuads();
			Minecraft.getMinecraft().renderEngine.bindTexture(DimensionProperties.getAtmosphereResource());
			tessellator1.setColorRGBA_F(skyColor[0], skyColor[1], skyColor[2], alphaMultiplier);

			tessellator1.addVertexWithUV(-size, zLevel, size, 0f, 1f);
			tessellator1.addVertexWithUV(size, zLevel, size, 1f, 1f);
			tessellator1.addVertexWithUV(size, zLevel, -size, 1f, 0f);
			tessellator1.addVertexWithUV(-size, zLevel, -size, 0f, 0f);
			tessellator1.draw();
		}

		GL11.glRotated(90-shadowAngle* 180/Math.PI, 0, 1, 0);

		//Draw Shadow
		Minecraft.getMinecraft().renderEngine.bindTexture(DimensionProperties.getShadowResource());
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		GL11.glColor4f(1f, 1f, 1f, alphaMultiplier);

		tessellator1.startDrawing(GL11.GL_QUADS);
		tessellator1.addVertexWithUV(-size, zLevel-0.01f, size, 0f, 1f);
		tessellator1.addVertexWithUV(size, zLevel-0.01f, size, 1f, 1f);
		tessellator1.addVertexWithUV(size, zLevel-0.01f, -size, 1f, 0f);
		tessellator1.addVertexWithUV(-size, zLevel-0.01f, -size, 0f, 0f);
		tessellator1.draw();

		GL11.glPopMatrix();

		tessellator1.setColorRGBA_F(1f, 1f, 1f, 1f);
	}

}
