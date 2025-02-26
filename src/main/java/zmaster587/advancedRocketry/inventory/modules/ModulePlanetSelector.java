package zmaster587.advancedRocketry.inventory.modules;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ICrafting;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.opengl.GL11;
import zmaster587.advancedRocketry.api.Configuration;
import zmaster587.advancedRocketry.api.dimension.IDimensionProperties;
import zmaster587.advancedRocketry.api.dimension.solar.IGalaxy;
import zmaster587.advancedRocketry.api.dimension.solar.StellarBody;
import zmaster587.advancedRocketry.dimension.DimensionManager;
import zmaster587.advancedRocketry.dimension.DimensionProperties;
import zmaster587.advancedRocketry.inventory.IPlanetDefiner;
import zmaster587.advancedRocketry.inventory.TextureResources;
import zmaster587.libVulpes.LibVulpes;
import zmaster587.libVulpes.inventory.GuiModular;
import zmaster587.libVulpes.inventory.modules.*;
import zmaster587.libVulpes.render.RenderHelper;

import java.util.*;
import java.util.stream.Collectors;

public class ModulePlanetSelector extends ModuleContainerPan implements IButtonInventory {
	//Closest thing i can get to a struct :/
	private static class PlanetRenderProperties {
		int radius;
		int posX;
		int posY;

		public PlanetRenderProperties() {}

		public PlanetRenderProperties(int radius, int posX, int posY) {
			this.radius = radius;
			this.posX = posX;
			this.posY = posY;
		}
	}

	private static final int size = 2000;
	final ISelectionNotify hostTile;
	private int currentSystem, selectedSystem;
	private double zoom,zoomToGo;
	private boolean currentSystemChanged = false;
	private final List<ModuleButton> planetList;
	int topLevel;
	private boolean stellarView;
	private ModuleContainerPan clickablePlanetList;

	private final HashMap<Integer, PlanetRenderProperties> renderPropertiesMap;
	PlanetRenderProperties currentlySelectedPlanet;
	final IPlanetDefiner planetDefiner;

	public ModulePlanetSelector(int planetId, ResourceLocation backdrop, ISelectionNotify tile, boolean star) {
		this(planetId, backdrop, tile, null, star);
	}

	public ModulePlanetSelector(int planetId, ResourceLocation backdrop, ISelectionNotify tile, IPlanetDefiner definer, boolean star) {
		super(0, 0, null, null, backdrop, 0, 0, 0, 0, size,size);
		this.planetDefiner = definer;
		hostTile = tile;
		int center = size/2;
		zoom = 1.0;
		zoomToGo=1.0;

		planetList = new ArrayList<>();
		moduleList = new ArrayList<>();
		staticModuleList = new ArrayList<>();
		renderPropertiesMap = new HashMap<>();
		currentlySelectedPlanet = new PlanetRenderProperties();
		currentSystem = 10000;
		selectedSystem = -1;
		stellarView = false;

		staticModuleList.add(new ModuleButton(0, 0, -1, "<<"+LibVulpes.proxy.getLocalizedString("gui.button.up"), this, zmaster587.libVulpes.inventory.TextureResources.buttonBuild));
		staticModuleList.add(new ModuleButton(0, 18, -2, LibVulpes.proxy.getLocalizedString("gui.button.select"), this, zmaster587.libVulpes.inventory.TextureResources.buttonBuild));
		staticModuleList.add(new ModuleButton(0, 36, -3, LibVulpes.proxy.getLocalizedString("gui.button.list"), this, zmaster587.libVulpes.inventory.TextureResources.buttonBuild));

		ModuleDualProgressBar progressBar;
		staticModuleList.add(progressBar = new ModuleDualProgressBar(100, 0, 0, TextureResources.atmIndicator, (IProgressBar)tile, "%b -> %a"+LibVulpes.proxy.getLocalizedString("gui.tooltip.earth_atmospheric_pressure")));
		progressBar.setTooltipValueMultiplier(.16f);

		staticModuleList.add(progressBar = new ModuleDualProgressBar(200, 0, 2, TextureResources.massIndicator, (IProgressBar)tile, "%b -> %a"+LibVulpes.proxy.getLocalizedString("gui.tooltip.earth_mass")));
		progressBar.setTooltipValueMultiplier(.02f);

		staticModuleList.add(progressBar = new ModuleDualProgressBar(300, 0, 1, TextureResources.distanceIndicator, (IProgressBar)tile, "%b -> %a"+LibVulpes.proxy.getLocalizedString("gui.tooltip.relative_distance")));
		progressBar.setTooltipValueMultiplier(.16f);

		//renderPlanetarySystem(properties, center, center, 3f);
		if(FMLCommonHandler.instance().getSide().isClient()) {
			
			//bgTexture = new ModuleImage(0, 54, zmaster587.libVulpes.inventory.TextureResources.buttonScan[0], 128,256);
			
			//staticModuleList.add(bgTexture);
			
			if(star) {
				topLevel = -1;
				currentSystem = planetId;
				StellarBody stellar = DimensionManager.getInstance().getStarFromPlanet(planetId);
                if (stellar != null) renderStarSystem(stellar, center, center, 1f, 0.5f);
                else renderStarSystem(DimensionManager.getFallbackStar(), center, center, 1f, 0.5f);
            }
			else {
				currentSystem = planetId;
				topLevel = -1;
				renderPlanetarySystem(DimensionManager.getInstance().getDimensionProperties(planetId), center, center, 1f, 3f);
			}
			refreshSideBar(true, currentSystem);
		}
		
		
	}

	@Override
	public void onScroll(int dwheel) {
		//redrawSystem();

		if(clickablePlanetList != null&&clickablePlanetList.isEnabled()) clickablePlanetList.onScroll(dwheel);
		else zoomToGo = Math.max(zoomToGo + dwheel/4000.0, 0.36);

	}

	public int getSelectedSystem() {
		return selectedSystem;
	}

	public void setSelectedSystem(int id) {
		selectedSystem = id;
	}

	@SideOnly(Side.CLIENT)
	private void renderGalaxyMap(IGalaxy galaxy, int posX, int posY, float distanceZoomMultiplier, float planetSizeMultiplier) {
		Collection<StellarBody> stars = galaxy.getStars();

		for(StellarBody star : stars) {

			if(planetDefiner != null && !planetDefiner.isStarKnown(star))
				continue;

			float displaySize = (planetSizeMultiplier*star.getDisplayRadius());
			int offsetX = (int) (star.getPosX() + posX - displaySize/2);
			int offsetY = (int) (star.getPosZ() + posY - displaySize/2);
			ModuleButton button;
			planetList.add(button = new ModuleButton(offsetX, offsetY, star.getId(), "", this, new ResourceLocation[] { TextureResources.locationSunNew }, LibVulpes.proxy.getLocalizedString("gui.tooltip.name")+": "+star.getName()+"\n"+LibVulpes.proxy.getLocalizedString("gui.tooltip.num_of_planets")+": "+star.getNumPlanets(), (int)displaySize, (int)displaySize));

			button.setSound("buttonBlipA");
			button.setBGColor(star.getColorRGB8());

			renderPropertiesMap.put(star.getId(), new PlanetRenderProperties((int) displaySize, offsetX, offsetY));
			//prevMultiplier *= 0.25f;

		}

		moduleList.addAll(planetList);
	}

	@SideOnly(Side.CLIENT)
	private void renderStarSystem(StellarBody star, int posX, int posY, float distanceZoomMultiplier, float planetSizeMultiplier) {

		int displaySize = (int)(planetSizeMultiplier*star.getDisplayRadius());

		int offsetX = posX - displaySize/2; 
		int offsetY = posY - displaySize/2; 

		ModuleButton button;

		if(star.getSubStars() != null && !star.getSubStars().isEmpty()) {
			float phaseInc = 360f/star.getSubStars().size();
			float phase = 0;
			for(StellarBody star2 : star.getSubStars()) {
				displaySize = (int)(planetSizeMultiplier*star2.getDisplayRadius());

				int deltaX, deltaY;
				deltaX = (int)(star2.getStarSeperation()*MathHelper.cos(phase)*0.5);
				deltaY = (int)(star2.getStarSeperation()*MathHelper.sin(phase)*0.5);

				planetList.add(button = new ModuleButton(offsetX + deltaX, offsetY + deltaY, star.getId(), "", this, new ResourceLocation[] { TextureResources.locationSunNew }, LibVulpes.proxy.getLocalizedString("gui.tooltip.name")+star.getName()+"\n"+LibVulpes.proxy.getLocalizedString("gui.tooltip.num_of_planets")+star.getNumPlanets(), displaySize, displaySize));
				button.setSound("buttonBlipA");
				button.setBGColor(star2.getColorRGB8());
				phase += phaseInc;
			}
		}
		displaySize = (int)(planetSizeMultiplier*star.getDisplayRadius());
		offsetX = posX - displaySize/2; 
		offsetY = posY - displaySize/2; 

		planetList.add(button = new ModuleButton(offsetX, offsetY, star.getId(), "", this, new ResourceLocation[] { TextureResources.locationSunNew }, LibVulpes.proxy.getLocalizedString("gui.tooltip.name")+": "+star.getName()+"\n"+LibVulpes.proxy.getLocalizedString("gui.tooltip.num_of_planets")+": "+star.getNumPlanets(), displaySize, displaySize));
		button.setSound("buttonBlipA");
		button.setBGColor(star.getColorRGB8());
		renderPropertiesMap.put(star.getId(), new PlanetRenderProperties(displaySize, offsetX, offsetY));


		//prevMultiplier *= 0.25f;
		displaySize = (int)(planetSizeMultiplier*100);
		offsetX = posX - displaySize/2; 
		offsetY = posY - displaySize/2;

		for(IDimensionProperties properties : star.getPlanets()) {

			if(planetDefiner != null && !planetDefiner.isPlanetKnown(properties))
				continue;

			if(!properties.isMoon()&&!properties.isSun())
				renderPlanets((DimensionProperties)properties, offsetX + displaySize/2, offsetY + displaySize/2, displaySize, distanceZoomMultiplier,planetSizeMultiplier);
		}

		moduleList.addAll(planetList);
	}

	@SideOnly(Side.CLIENT)
	private void renderPlanetarySystem(DimensionProperties planet, int posX, int posY, float distanceZoomMultiplier, float planetSizeMultiplier) {

		int displaySize = (int) (distanceZoomMultiplier*Math.max((int)(planetSizeMultiplier*planet.gravitationalMultiplier/.02f), 7));

		int offsetX = posX - displaySize/2;
		int offsetY = posY - displaySize/2;

		ModuleButton button;
		planetList.add(button = new ModuleButtonPlanet(offsetX, offsetY, planet.getId(), "", this, planet, planet.getName(), displaySize, displaySize));
		button.setSound("buttonBlipA");

		renderPropertiesMap.put(planet.getId(), new PlanetRenderProperties(displaySize, offsetX, offsetY));

		//prevMultiplier *= 0.25f;

		for(Integer childId : planet.getChildPlanets()) {
			DimensionProperties properties = DimensionManager.getInstance().getDimensionProperties(childId);
			
			if(planetDefiner != null && (!planetDefiner.isPlanetKnown(properties)))
				continue;
			
			renderPlanets(properties, offsetX + displaySize/2, offsetY + displaySize/2, displaySize, distanceZoomMultiplier, distanceZoomMultiplier*planetSizeMultiplier/2);
		}

		moduleList.addAll(planetList);
	}

	@SideOnly(Side.CLIENT)
	private void renderPlanets(DimensionProperties planet, int parentOffsetX, int parentOffsetY, int parentRadius, float distanceMultiplier, float planetSizeMultiplier) {

		int displaySize = Math.max((int)(planetSizeMultiplier*planet.gravitationalMultiplier/.02f),7);
		int offsetX = parentOffsetX + (int)(Math.cos(planet.orbitTheta)*((planet.orbitalDist*distanceMultiplier))) - displaySize/2;
		int offsetY = parentOffsetY + (int)(Math.sin(planet.orbitTheta)*((planet.orbitalDist*distanceMultiplier))) - displaySize/2;
		ModuleButton button;

		planetList.add(button = new ModuleButtonPlanet(offsetX, offsetY, planet.getId(), "", this, planet, planet.getName() + "\n"+LibVulpes.proxy.getLocalizedString("gui.tooltip.moons")+": " + planet.getChildPlanets().size(), displaySize, displaySize));
		button.setSound("buttonBlipA");

		renderPropertiesMap.put(planet.getId(), new PlanetRenderProperties(displaySize, offsetX, offsetY));

	}


	@SideOnly(Side.CLIENT)
	public void setPlanetAsKnown(int id) {
		for(ModuleBase module : moduleList) {
			if(module instanceof ModuleButton && ((ModuleButton)module).buttonId == id) {
				((ModuleButton)module).setImage( new ResourceLocation[] {DimensionManager.getInstance().getDimensionProperties(id).getPlanetIcon()});
			}
		}
	}

	@Override
	@SideOnly(Side.CLIENT)
	public List<GuiButton> addButtons(int x, int y) {

		this.screenSizeX = Minecraft.getMinecraft().displayWidth;
		this.screenSizeY = Minecraft.getMinecraft().displayHeight;

		setOffset2(internalOffsetX - Minecraft.getMinecraft().displayWidth/4, internalOffsetY - Minecraft.getMinecraft().displayHeight /4);

		List <GuiButton> list = super.addButtons(x, y);

		if(clickablePlanetList != null)
			list.addAll(clickablePlanetList.addButtons(x, y));

		return list;
	}

	@SideOnly(Side.CLIENT)
	private void redrawSystem() {
		zoom += (zoomToGo-zoom)/8;

		int offsetX = -currentPosX;
		int offsetY = -currentPosY;
		setOffset2(0,0);
		for(int i = 0; i< planetList.size(); i++) {
			ModuleButton module = planetList.get(i);
			if(planetList.contains(module))
				this.buttonList.remove(module.button);
		}

		this.moduleList.removeAll(planetList);

		planetList.clear();
		if(!stellarView) {
			StellarBody star = DimensionManager.getInstance().getStar(currentSystem);
			if(star == null) {
				DimensionProperties properties = DimensionManager.getInstance().getDimensionProperties(currentSystem);
				renderPlanetarySystem(properties, size/2, size/2, (float) zoom,properties.getPathLengthToStar());
			}
			else
				renderStarSystem(star, size/2, size/2, (float) zoom, (float)zoom*.2f);
		}
		else
			renderGalaxyMap(DimensionManager.getInstance(), size/2, size/2, (float) zoom, (float)zoom*.25f);

		int x = currentPosX - size/2, y = currentPosY - size/2;

		this.screenSizeX = Minecraft.getMinecraft().displayWidth;
		this.screenSizeY = Minecraft.getMinecraft().displayHeight;
		for(ModuleBase module : this.planetList) {
			for(GuiButton module2 : module.addButtons(currentPosX, currentPosY)) {
				if(module2.xPosition > 128 + offsetX || clickablePlanetList == null || !clickablePlanetList.isEnabled())
					buttonList.add( module2 );
			}
		}

		setOffset2(offsetX, offsetY);
	}

	public boolean isStar(int id){
		return DimensionManager.getInstance().getStar(id) != null;
	}
	@Override
	@SideOnly(Side.CLIENT)
	public void onMouseClicked(@NotNull GuiModular gui, int x, int y, int button) {

		if(clickablePlanetList != null) clickablePlanetList.onMouseClicked(gui, x, y, button);

		super.onMouseClicked(gui, x, y, button);

		//CME workaround
		if(currentSystemChanged) {
			currentPosX = 0;
			currentPosY = 0;
			zoom = 1;
			zoomToGo = 1;
			redrawSystem();
			setOffset2(internalOffsetX - Minecraft.getMinecraft().displayWidth/4 , internalOffsetY - Minecraft.getMinecraft().displayHeight /4);
			//redrawSystem();

			//selectedSystem = -1;

			currentSystemChanged = false;

			hostTile.onSystemFocusChanged(this);
			refreshSideBar(true, selectedSystem);
		}
	}

	@Override
	public void renderForeground(int guiOffsetX, int guiOffsetY, int mouseX,
								 int mouseY, float zLevel, @NotNull GuiContainer gui, FontRenderer font) {
		super.renderForeground(guiOffsetX, guiOffsetY, mouseX, mouseY, zLevel, gui,
				font);
	}

	@Override
	protected void moveContainerInterior(int deltaX, int deltaY) {
		super.moveContainerInterior(deltaX, deltaY);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void renderBackground(GuiContainer gui, int x, int y, int mouseX,
			int mouseY, FontRenderer font) {
		
		if(!stellarView && Minecraft.getSystemTime() % 5 == 0)
			redrawSystem();
		super.renderBackground(gui, x, y, mouseX, mouseY, font);

		int center = size/2;
		int numSegments = 50;

		float theta = (float) (2 * Math.PI / (float)(numSegments));
		float cos = (float) Math.cos(theta);
		float sin = (float) Math.sin(theta);

		GL11.glPushMatrix();

		//GL11.glTranslated(-this.currentPosX/4, -this.currentPosY/4, 0);
		//GL11.glScaled(zoom, zoom, 1.0);
		//GL11.glTranslated(this.currentPosX/4, this.currentPosY/4, 0);

		//Render orbits
		if(!stellarView) {
			StellarBody star = DimensionManager.getInstance().getStar(currentSystem);
			@NotNull Collection<Integer> set = star != null ? star.getPlanets().stream().filter(p -> !p.isSun()).map(IDimensionProperties::getOrbitalDist).collect(Collectors.toList()) :  DimensionManager.getInstance().getDimensionProperties(currentSystem).getChildPlanets().stream().map(dim->DimensionManager.getInstance().getDimensionProperties(dim).getOrbitalDist()).collect(Collectors.toList());
			for(Integer ii : set) {
				float radius = (float) (ii*zoom);
				float y2 = 0;
				float t;
				GL11.glPushMatrix();
				GL11.glTranslatef(center + currentPosX, center + currentPosY, 0);
				GL11.glDisable(GL11.GL_TEXTURE_2D);
				GL11.glEnable(GL11.GL_BLEND);
				GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
				GL11.glColor4f(0.8f, .8f, 1f, .2f);
				GL11.glEnable(GL11.GL_LINE_STIPPLE);
				GL11.glLineStipple(5, (short)0x5555);

				Tessellator.instance.startDrawing(GL11.GL_LINE_LOOP);
				for(int i = 0; i < numSegments; i++)	{
					Tessellator.instance.addVertex(radius, y2, 0);
					t = radius;
					radius = cos*radius - sin*y2;
					y2 = sin*t + cos*y2;
				}
				Tessellator.instance.draw();
				//Reset GL info
				GL11.glEnable(GL11.GL_TEXTURE_2D);
				GL11.glDisable(GL11.GL_LINE_STIPPLE);
				GL11.glColor4f(1f, 1f, 1f, 1f);
				GL11.glDisable(GL11.GL_BLEND);
				GL11.glPopMatrix();

			}
		}
		
		//Render Selection
		if(selectedSystem != -1) {

			Minecraft.getMinecraft().getTextureManager().bindTexture(TextureResources.selectionCircle);
			GL11.glPushMatrix();
			GL11.glEnable(GL11.GL_BLEND);
			GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
			float radius = currentlySelectedPlanet.radius/2f;

			if(renderPropertiesMap.containsKey(selectedSystem)) {
				PlanetRenderProperties base = renderPropertiesMap.get(selectedSystem);
				GL11.glTranslatef(base.posX + currentPosX + base.radius/2f, base.posY + currentPosY + base.radius/2f, 0);
			}
			else 
				GL11.glTranslatef(currentlySelectedPlanet.posX + currentPosX + radius, currentlySelectedPlanet.posY  + currentPosY + radius, 0);

			double progress = System.currentTimeMillis() % 20000 / 50f;

			GL11.glPushMatrix();
			GL11.glRotated(progress, 0, 0, 1);
			Tessellator.instance.startDrawingQuads();
			RenderHelper.renderNorthFaceWithUV(Tessellator.instance, 1, -radius, -radius, radius, radius, 0, 1, 0, 1);
			Tessellator.instance.draw();
			GL11.glPopMatrix();

			GL11.glPushMatrix();
			//GL11.glRotatef(-Minecraft.getMinecraft().theWorld.getTotalWorldTime(), 0, 0, 1);
			radius *= (float) (1.2 + 0.1*Math.sin(progress/10f));
			Tessellator.instance.startDrawingQuads();
			RenderHelper.renderNorthFaceWithUV(Tessellator.instance, 1, -radius, -radius, radius, radius, 0, 1, 0, 1);
			Tessellator.instance.draw();
			GL11.glPopMatrix();
			GL11.glDisable(GL11.GL_BLEND);

			GL11.glPopMatrix();
		}
		GL11.glPopMatrix();
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void onInventoryButtonPressed(int buttonId) {
		//Go Up a level
		if(buttonId == -1) {
			DimensionProperties properties =  DimensionManager.getInstance().getDimensionProperties(currentSystem);

			if(topLevel == -1 || currentSystem != topLevel) {
				if(!isStar(currentSystem) && properties.isMoon())
					currentSystem = properties.getParentPlanet();
				else {
					if(isStar(currentSystem)) {
						//if the star was the current system then go to stellar view
						stellarView = true;
					}
					currentSystem = properties.getStar().getId();
				}

				currentSystemChanged=true;

				selectedSystem = -1;
			}
		}
		//Confirm selection
		else if(buttonId == -2) {
			if(!isStar(selectedSystem)) {
				hostTile.onSelectionConfirmed(this);
				Minecraft.getMinecraft().thePlayer.closeScreen();
			}
		}
		else if(buttonId == -3) {
			if(clickablePlanetList != null) {
				boolean flag = !clickablePlanetList.isEnabled();
				clickablePlanetList.setEnabled(flag);
				//bgTexture.setEnabled(flag);
			}
		}
		else {
			//Zoom into selected system
			if(selectedSystem == buttonId) {
				if(buttonId==currentSystem)return;
				currentSystem = buttonId;
				currentSystemChanged=true;
				//Go back to planetary mapping
				stellarView = false;
				//selectedSystem = -1;
			}
			else {
				//Make clicked planet selected
				selectedSystem = buttonId;
				currentlySelectedPlanet = renderPropertiesMap.get(buttonId);
				hostTile.onSelected(this);
				refreshSideBar(currentSystemChanged, selectedSystem);
			}
		}
	}

	@SideOnly(Side.CLIENT)
	private void refreshSideBar(boolean planetChanged, int selectedPlanet) {
		List<ModuleBase> list2 = new LinkedList<>();

		if(!stellarView) {
			StellarBody star = DimensionManager.getInstance().getStar(currentSystem);
			if(star == null) {
				DimensionProperties parent = DimensionManager.getInstance().getDimensionProperties(currentSystem);

				List<Integer> propertyList = new LinkedList<>(parent.getChildPlanets());
				propertyList.add(parent.getId());
				int i = 0;
				for( int childId :  propertyList) 
				{
					DimensionProperties properties = DimensionManager.getInstance().getDimensionProperties(childId);

					if(planetDefiner != null && !planetDefiner.isPlanetKnown(properties)) continue;

					ModuleButton button = new ModuleButton(0, i*18, properties.getId(), properties.getName(), this, zmaster587.advancedRocketry.inventory.TextureResources.buttonGeneric, 128, 18);
					list2.add(button);

					if(properties.getId() == selectedPlanet) button.setColor(0xFFFF2222);
					
					i++;
				}
			}
			//Get planets around a star
			else {
				int i = 0;
				for( IDimensionProperties properties : star.getPlanets() )
				{

					if(planetDefiner != null && !planetDefiner.isPlanetKnown(properties))
						continue;

					if(!properties.isMoon() &&!properties.isSun()&& properties.getId() != Configuration.spaceDimId) {
						ModuleButton button = new ModuleButton(0, i*18, properties.getId(), properties.getName(), this, zmaster587.advancedRocketry.inventory.TextureResources.buttonGeneric, 128, 18);
						list2.add(button);

						if(properties.getId() == selectedPlanet)
							button.setColor(0xFFFF2222);
					}
					i++;
				}
			}
		}
		else {
			int i = 0;
			for( StellarBody properties : DimensionManager.getInstance().getStars() ) 
			{

				if(planetDefiner != null && !planetDefiner.isStarKnown(properties))
					continue;

				ModuleButton button = new ModuleButton(0, i*18, properties.getId(), properties.getName(), this, zmaster587.advancedRocketry.inventory.TextureResources.buttonGeneric, 128, 18);
				list2.add(button);

				if(properties.getId() == selectedPlanet)
					button.setColor(0xFFFF2222);
				i++;
			}
		}

		boolean enabled = clickablePlanetList != null && clickablePlanetList.isEnabled();

		int offX = 0, offY = 0;
		
		if(clickablePlanetList != null) {
			staticModuleList.remove(clickablePlanetList);
			offX = clickablePlanetList.getScrollX();
			offY = clickablePlanetList.getScrollY();
		}

		clickablePlanetList = new ModuleContainerPan(0, 128, list2, new LinkedList<>(), null, 512, 256, 0, 0, 258, 256);
		staticModuleList.add(clickablePlanetList);
		clickablePlanetList.addButtons(0, 0);
		
		//Hacky fix for bug in containerPan
		if(!planetChanged)
			clickablePlanetList.setOffset2(-offX, -offY);
		else
			clickablePlanetList.setOffset2(0, 64);
		
		clickablePlanetList.setEnabled(enabled);
		//bgTexture.setEnabled(enabled);
	}

	@Override
	public boolean needsUpdate(int localId) {
		for(ModuleBase module : staticModuleList) {
			if(localId >= 0 && localId < module.numberOfChangesToSend())
				return module.needsUpdate(localId);

			localId -= module.numberOfChangesToSend();
		}
		return false;
	}

	@Override
	public void sendChanges(Container container, ICrafting crafter,
			int variableId, int localId) {
		for(ModuleBase module : staticModuleList) {
			if(localId >= 0 && localId < module.numberOfChangesToSend()) {
				module.sendChanges(container, crafter, variableId, localId);
				return;
			}

			localId -= module.numberOfChangesToSend();
		}
	}

	@Override
	public void onChangeRecieved(int slot, int value) {
		for(ModuleBase module : staticModuleList) {
			if(slot >= 0 && slot < module.numberOfChangesToSend()) {
				module.onChangeRecieved(slot, value);
				return;
			}

			slot -= module.numberOfChangesToSend();
		}
	}

	@Override
	public int numberOfChangesToSend() {
		int numChanges = 0;
		for(ModuleBase module : staticModuleList) {
			numChanges += module.numberOfChangesToSend();
		}

		return numChanges;
	}
}
