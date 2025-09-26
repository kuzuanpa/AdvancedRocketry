package zmaster587.advancedRocketry.client.render.planet;

import cpw.mods.fml.common.FMLLog;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.renderer.GLAllocation;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.TextureUtil;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Vec3;
import net.minecraftforge.client.IRenderHandler;
import net.minecraftforge.client.model.AdvancedModelLoader;
import net.minecraftforge.client.model.IModelCustom;
import net.minecraftforge.common.util.ForgeDirection;
import org.apache.logging.log4j.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.opengl.GL11;
import zmaster587.advancedRocketry.api.Configuration;
import zmaster587.advancedRocketry.api.IPlanetaryProvider;
import zmaster587.advancedRocketry.api.dimension.IDimensionProperties;
import zmaster587.advancedRocketry.api.dimension.solar.StellarBody;
import zmaster587.advancedRocketry.dimension.DimensionManager;
import zmaster587.advancedRocketry.dimension.DimensionProperties;
import zmaster587.advancedRocketry.event.RocketEventHandler;
import zmaster587.advancedRocketry.inventory.TextureResources;
import zmaster587.advancedRocketry.stations.SpaceObject;
import zmaster587.advancedRocketry.stations.SpaceObjectManager;
import zmaster587.advancedRocketry.util.AstronomicalBodyHelper;
import zmaster587.libVulpes.util.Vector3F;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

public class RenderPlanetarySky extends IRenderHandler {

	public static int selectedPlanetID = -1;
	private static int bodyList;
	private static int sunList;

	final int starGLCallList;
	final int glSkyList;
	final int glSkyList2;
	float celestialAngle;
	final Vector3F<Float> axis;

	IModelCustom sunModel = AdvancedModelLoader.loadModel(new ResourceLocation("advancedrocketry:models/star.obj"));
	ResourceLocation sunTexture = new ResourceLocation("advancedrocketry:textures/env/sunLEO.png");
	int sunTextureID = -1;

	final Minecraft mc = Minecraft.getMinecraft();

	public RenderPlanetarySky() {
		axis = new Vector3F<>(1f, 0f, 0f);

		GL11.glNewList(sunList = GL11.glGenLists(1), GL11.GL_COMPILE);
		sunModel.renderPart("Cube");
		GL11.glEndList();
		loadSunTexture();

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
	protected void loadSunTexture() {
		try (InputStream inputstream = Minecraft.getMinecraft().getResourceManager().getResource(sunTexture).getInputStream())
		{
			if (this.sunTextureID != -1) {
				TextureUtil.deleteTexture(this.sunTextureID);
				this.sunTextureID = -1;
			}
			BufferedImage bufferedimage = ImageIO.read(inputstream);
			sunTextureID=TextureUtil.uploadTextureImage(sunTextureID, bufferedimage);
		}catch (IOException ioexception)
		{
			FMLLog.log(Level.WARN,"Failed to load texture: " + sunTexture.toString());
			ioexception.printStackTrace();
		}
	}

	public static void drawTextureRect(Tessellator tessellator, int x, int y, int z, int u, int v, int width, int height){
		final float f = 0.00390625F;
		final float f1 = 0.00390625F;
		tessellator.startDrawingQuads();
		tessellator.addVertexWithUV(x, y + height, z, (u * f), (v + height) * f1);
		tessellator.addVertexWithUV(x + width, y + height, z, (u + width) * f, (v + height) * f1);
		tessellator.addVertexWithUV(x + width, y, z, (u + width) * f, v * f1);
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

		GL11.glColor3f(f1, f2, f3);
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
		drawRandomStars:
		{
			if (atmosphere > 0)
				f6 = 1.0F - (mc.theWorld.getRainStrength(partialTicks) * (atmosphere / 100f));
			else
				f6 = 1f;
			GL11.glDisable(GL11.GL_TEXTURE_2D);
			float f18 = mc.theWorld.getStarBrightness(partialTicks) * f6 * (atmosphere) + (1 - atmosphere);
			if (mc.theWorld.isRaining())
				f18 *= 1 - mc.theWorld.getRainStrength(partialTicks);

			if (f18 > 0.0F) {
				GL11.glColor4f(f18, f18, f18, f18);
				GL11.glPushMatrix();
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
					GL11.glCallList(this.starGLCallList);
					//Extra stars for low ATM
					if (atmosphere < 0.5) {
						GL11.glColor4f(f18, f18, f18, f18 / 2f);
						GL11.glPushMatrix();
						GL11.glRotatef(-90, 0, 1, 0);
						GL11.glCallList(this.starGLCallList);
						GL11.glPopMatrix();
					}
					if (atmosphere < 0.25) {
						GL11.glColor4f(f18, f18, f18, f18 / 4f);
						GL11.glPushMatrix();
						GL11.glRotatef(90, 0, 1, 0);
						GL11.glCallList(this.starGLCallList);
						GL11.glPopMatrix();
					}
					GL11.glColor4f(f18, f18, f18, f18);
				}
				GL11.glPopMatrix();
			}
			GL11.glEnable(GL11.GL_TEXTURE_2D);
		}
		if(primaryStar != null){

			List<IDimensionProperties> moons = new ArrayList<>();
            Queue<IDimensionProperties> moonsQueue = new ArrayDeque<>(primaryStar.getPlanets());
			IDimensionProperties current = DimensionManager.getInstance().getDimensionProperties(Minecraft.getMinecraft().theWorld.provider.dimensionId);

			while (!moonsQueue.isEmpty()){
				IDimensionProperties properties1 = moonsQueue.poll();
				if(properties1.isSun() || properties1.equals(current))continue;
				moons.add(properties1);
				properties1.getChildPlanets().forEach(p-> moonsQueue.add(DimensionManager.getInstance().getDimensionProperties(p)));
			}
			selectedPlanetID = -1;
			for (IDimensionProperties moon : moons) {

				float orbitDistance = moon.getSolarOrbitalDistance();
				if (Minecraft.getMinecraft().theWorld.provider.dimensionId != Configuration.stationDimId && Minecraft.getMinecraft().theWorld.provider.dimensionId != Configuration.spaceDimId) {
					orbitDistance = Math.abs(orbitDistance - current.getSolarOrbitalDistance());
					if(orbitDistance < 4) {
						orbitDistance += 4;
						if (moon.isMoon())orbitDistance += moon.getParentOrbitalDistance() / 10F;
					}
				}
				orbitDistance*=2F;

				GL11.glPushMatrix();
				GL11.glColor4f(1.0F, 1.0F, 1.0F, 1f);

				double rot = (moon.getOrbitTheta() * 180F / Math.PI) + getSkyRotationAmount() * 360.0F;

				GL11.glEnable(GL11.GL_TEXTURE_2D);
				GL11.glEnable(GL11.GL_BLEND);

				float multiplier = (2 - atmosphere) / 2f;//atmosphere > 1 ? (2-atmosphere) : 1f;
				multiplier *= 1 - mc.theWorld.getRainStrength(partialTicks);
				double rotateX = rot % 360;
				double rotateY = (myRotationalPhi - 90F) % 360;

				GL11.glRotated(rotateY, 0f, 1f, 0f);
				GL11.glRotated(rotateX, 1f, 0f, 0f);


				float phiAngle = (float) ((moon.getOrbitPhi()) * Math.PI / 180f);
				double x = -MathHelper.sin(phiAngle) * MathHelper.cos((float) moon.getOrbitTheta());
				double y = MathHelper.sin((float) moon.getOrbitTheta());
				double rotation = -Math.PI / 2f + Math.atan2(x, y) - (moon.getOrbitTheta() - Math.PI) * MathHelper.sin(phiAngle);

				renderPlanet(tessellator1, moon.getPlanetIcon(), orbitDistance, multiplier, rotation, moon.hasAtmosphere(), moon.getSunColor(), moon.getRingColor(), moon.isGasGiant(), moon.hasRing(), sunColor);
				GL11.glPopMatrix();

				double pointerRotateX = -(Minecraft.getMinecraft().thePlayer.rotationPitch - 90) % 360;
				double pointerRotateY = -(Minecraft.getMinecraft().thePlayer.rotationYaw + 180) % 360;

				double checkPointerX = (pointerRotateX + 360) % 360;
				double checkPointerY = (pointerRotateY + 360) % 360;
				double checkX = (rotateX + 360) % 360;
				double checkY = (rotateY + 360) % 360;
				float f = 200F / orbitDistance;
				if (((Math.abs(checkX - checkPointerX) < f && Math.abs(checkY - checkPointerY) < f) || (Math.abs(checkX + checkPointerX - 360) < f && Math.abs(checkY + checkPointerY - 360) < f))) {
					selectedPlanetID = moon.getId();
				}
			}

		}

		if(selectedPlanetID != -1) {
			GL11.glPushMatrix();

			double pointerRotateX = -(Minecraft.getMinecraft().thePlayer.rotationPitch - 90) % 360;
			double pointerRotateY = -(Minecraft.getMinecraft().thePlayer.rotationYaw + 180) % 360;

			GL11.glRotated(pointerRotateY, 0f, 1f, 0f);
			GL11.glRotated(pointerRotateX, 1f, 0f, 0f);

			GL11.glTranslated(0, -100, 0);

			f10 = 50;
			mc.renderEngine.bindTexture(DimensionProperties.planetRings);
			GL11.glColor4f(ringColor[0], ringColor[1], ringColor[2], 0.8F);
			tessellator1.startDrawing(GL11.GL_QUADS);
			tessellator1.addVertexWithUV(f10, 0, -f10, 1.0D, 0.0D);
			tessellator1.addVertexWithUV(-f10, 0, -f10, 0.0D, 0.0D);
			tessellator1.addVertexWithUV(-f10, 0, f10, 0.0D, 1.0D);
			tessellator1.addVertexWithUV(f10, 0, f10, 1.0D, 1.0D);
			tessellator1.draw();


			GL11.glPopMatrix();
		}


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

		GL11.glEnable(GL11.GL_TEXTURE_2D);
		OpenGlHelper.glBlendFunc(770, 1, 1, 0);
		GL11.glPushMatrix();


		f7 = 0.0F;
		f8 = 0.0F;
		f9 = 0.0F;
		GL11.glColor4f(1.0F, 1.0F, 1.0F, f6);
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

		mc.renderEngine.bindTexture(TextureResources.locationSunPng);

		//--------------------------- Draw the suns --------------------
		if(!isWarp) {
			drawStarAndSubStars(tessellator1, partialTicks,primaryStar,subStars,properties,solarOrbitalDistance,sunSize,sunColor,multiplier);

		}

		GL11.glEnable(GL11.GL_FOG);
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		GL11.glDisable(GL11.GL_BLEND);
		GL11.glEnable(GL11.GL_ALPHA_TEST);

		GL11.glPopMatrix();
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
		GL11.glDepthMask(true);

		RocketEventHandler.onPostWorldRender(partialTicks);
	}

	protected void drawStarAndSubStars(Tessellator tessellator1, float partialTicks, StellarBody primaryStar, @Nullable List<StellarBody> subStars, DimensionProperties properties, int solarOrbitalDistance, float sunSize, Vec3 sunColor, float multiplier){
		drawStar(tessellator1, partialTicks, primaryStar,properties,solarOrbitalDistance, sunSize, sunColor, multiplier);

		if(subStars != null && !subStars.isEmpty()) {
			GL11.glPushMatrix();
			float phaseInc = 360f/subStars.size();

			for(StellarBody subStar : subStars) {
				GL11.glRotatef(phaseInc, 0, 1, 0);
				GL11.glPushMatrix();

				GL11.glRotatef(subStar.getStarSeperation()*AstronomicalBodyHelper.getBodySizeMultiplier(solarOrbitalDistance), 1, 0, 0);
				Vec3 color = Vec3.createVectorHelper(subStar.getColor()[0],subStar.getColor()[1],subStar.getColor()[2]);
				drawStar(tessellator1, partialTicks, primaryStar,properties,solarOrbitalDistance, subStar.getSize(), color, multiplier);
				GL11.glPopMatrix();
			}
			GL11.glPopMatrix();
		}
	}

	protected void drawStar(Tessellator buffer, float partialTicks, @Nullable StellarBody sun, DimensionProperties properties, int solarOrbitalDistance, float sunSize, Vec3 sunColor, float multiplier) {
		if(sun == null)return;
		GL11.glPushMatrix();
		GL11.glEnable(GL11.GL_BLEND);
		GL11.glDepthMask(true);
		float f10 = sunSize*15f*AstronomicalBodyHelper.getBodySizeMultiplier(solarOrbitalDistance);
		int i1=  (int) (f10 * 0.4F);
		GL11.glColor4f((float) Math.min(1.0F, sunColor.xCoord*1.5F), (float)Math.min(1.0F, sunColor.yCoord*1.5F) , (float)Math.min(1.0F, sunColor.zCoord*1.5F) , (float) Math.min(0.9,multiplier));
        if(sun.dysonSphere != null)sun.dysonSphere.draw(0,100, i1,0,90,i1/500F,0.8F,(System.currentTimeMillis() % 36000) / 100F / (sun.dysonSphere.size+1));

		//Set sun color
		GL11.glColor4f((float)sunColor.xCoord, (float)sunColor.yCoord , (float)sunColor.zCoord , 1.0F);

		boolean enable3DSun = true;
		GL11.glPushMatrix();
		if(enable3DSun) {
			mc.getTextureManager().bindTexture(new ResourceLocation("advancedrocketry:textures/env/starLight.png"));
			buffer.startDrawingQuads();
			//multiplier = 2;
			buffer.addVertexWithUV(-f10 * 0.5F, 100.0D, -f10 * 0.5F, 0.0D, 0.0D);
			buffer.addVertexWithUV(f10 * 0.5F, 100.0D, -f10 * 0.5F, 1.0D, 0.0D);
			buffer.addVertexWithUV(f10 * 0.5F, 100.0D, f10 * 0.5F, 1.0D, 1.0D);
			buffer.addVertexWithUV(-f10 * 0.5F, 100.0D, f10 * 0.5F, 0.0D, 1.0D);
			buffer.draw();
			GL11.glPopMatrix();


			GL11.glPushMatrix();
			GL11.glBindTexture(GL11.GL_TEXTURE_2D, sunTextureID);
			GL11.glTranslatef(0, 100, 0);
			GL11.glRotated(90, 0, 0, 1);

			GL11.glRotated(-(System.currentTimeMillis() % 360000) / 1000F, 0, 1, 0);
			GL11.glScalef(f10 * 0.3F, f10 * 0.3F, f10 * 0.3F);
			GL11.glCallList(sunList);
		}
		else {
			mc.renderEngine.bindTexture(TextureResources.locationSunNew);
			//Set sun color and distance
			GL11.glColor4f((float)sunColor.xCoord, (float)sunColor.yCoord , (float)sunColor.zCoord , (float) Math.min(0.9,multiplier));
			buffer.startDrawingQuads();
			//multiplier = 2;
			buffer.addVertexWithUV(-f10, 100.0D, -f10, 0.0D, 0.0D);
			buffer.addVertexWithUV(f10, 100.0D, -f10, 1.0D, 0.0D);
			buffer.addVertexWithUV(f10, 100.0D, f10, 1.0D, 1.0D);
			buffer.addVertexWithUV(-f10, 100.0D, f10, 0.0D, 1.0D);
			buffer.draw();
		}
		GL11.glPopMatrix();

		GL11.glColor4f((float) Math.min(1.0F, sunColor.xCoord*1.5F), (float)Math.min(1.0F, sunColor.yCoord*1.5F) , (float)Math.min(1.0F, sunColor.zCoord*1.5F) , (float) Math.min(0.9,multiplier));

		if(sun.dysonCloud != null)sun.dysonCloud.draw(0,100, i1,0,90,i1/500F,0.5F,(System.currentTimeMillis() % 36000) / 100F);

		GL11.glClear(GL11.GL_DEPTH_BUFFER_BIT);
		GL11.glDepthMask(false);

		GL11.glPopMatrix();

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
