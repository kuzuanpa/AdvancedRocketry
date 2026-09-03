package zmaster587.advancedRocketry.entity;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import io.netty.buffer.ByteBuf;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.Constants.NBT;
import net.minecraftforge.common.util.ForgeDirection;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.opengl.GL11;
import zmaster587.advancedRocketry.AdvancedRocketry;
import zmaster587.advancedRocketry.api.*;
import zmaster587.advancedRocketry.api.RocketEvent.RocketLaunchEvent;
import zmaster587.advancedRocketry.api.RocketEvent.RocketPreLaunchEvent;
import zmaster587.advancedRocketry.api.dimension.IDimensionProperties;
import zmaster587.advancedRocketry.api.dimension.solar.StellarBody;
import zmaster587.advancedRocketry.api.fuel.FuelRegistry.FuelType;
import zmaster587.advancedRocketry.api.satellite.SatelliteBase;
import zmaster587.advancedRocketry.api.stations.ISpaceObject;
import zmaster587.advancedRocketry.api.stations.ISpaceTraveler;
import zmaster587.advancedRocketry.atmosphere.AtmosphereHandler;
import zmaster587.advancedRocketry.client.SoundRocketEngine;
import zmaster587.advancedRocketry.dimension.DimensionManager;
import zmaster587.advancedRocketry.dimension.DimensionProperties;
import zmaster587.advancedRocketry.dimension.sim.SimScale;
import zmaster587.advancedRocketry.dimension.sim.SimUniverse;
import zmaster587.advancedRocketry.event.PlanetEventHandler;
import zmaster587.advancedRocketry.inventory.IPlanetDefiner;
import zmaster587.advancedRocketry.inventory.TextureResources;
import zmaster587.advancedRocketry.inventory.modules.ModulePlanetSelector;
import zmaster587.advancedRocketry.inventory.modules.ModuleStellarBackground;
import zmaster587.advancedRocketry.item.ItemPackedStructure;
import zmaster587.advancedRocketry.item.ItemPlanetIdentificationChip;
import zmaster587.advancedRocketry.item.ItemStationChip;
import zmaster587.advancedRocketry.stations.SpaceObject;
import zmaster587.advancedRocketry.stations.SpaceObjectManager;
import zmaster587.advancedRocketry.thread.RocketStructureThread;
import zmaster587.advancedRocketry.tile.TileGuidanceComputer;
import zmaster587.advancedRocketry.tile.hatch.TileSatelliteHatch;
import zmaster587.advancedRocketry.util.StageLayout;
import zmaster587.advancedRocketry.util.StationLandingLocation;
import zmaster587.advancedRocketry.util.StorageChunk;
import zmaster587.advancedRocketry.util.TeleportHelper;
import zmaster587.libVulpes.LibVulpes;
import zmaster587.libVulpes.api.IDismountHandler;
import zmaster587.libVulpes.client.util.ProgressBarImage;
import zmaster587.libVulpes.gui.CommonResources;
import zmaster587.libVulpes.interfaces.INetworkEntity;
import zmaster587.libVulpes.inventory.GuiHandler;
import zmaster587.libVulpes.inventory.modules.*;
import zmaster587.libVulpes.items.ItemLinker;
import zmaster587.libVulpes.network.PacketEntity;
import zmaster587.libVulpes.network.PacketHandler;
import zmaster587.libVulpes.util.BlockPosition;
import zmaster587.libVulpes.util.IconResource;
import zmaster587.libVulpes.util.Vector3F;

import java.lang.ref.WeakReference;
import java.util.*;

import static zmaster587.advancedRocketry.api.Configuration.spaceDimId;

public class EntityRocket extends EntityRocketBase implements INetworkEntity, IDismountHandler, IModularInventory, IProgressBar, IButtonInventory, ISelectionNotify,IPlanetDefiner, ISpaceTraveler {

	//true if the rocket is on decent
	private boolean isInOrbit;
	//True if the rocket isn't on the ground
	private boolean isInFlight;
	//used in the rare case a player goes to a non-existant space station
	private int lastDimensionFrom = 0;
	
	public StorageChunk storage;
	public ArrayList<LeveledRocketPart> LeveledRocketParts=new ArrayList<>();
	/**Which stage each block belongs to, empty until the structure thread reports back*/
	private StageLayout stageLayout;
	/**The stage currently burning, counts down to 0 as stages separate*/
	private int currentStageLevel;
	/**True while the structure thread still owes us a layout for this rocket*/
	private boolean stagesPending;
	private String errorStr;
	private long lastErrorTime = Long.MIN_VALUE;
	private static final long ERROR_DISPLAY_TIME = 100;
	private static final int DESCENT_TIMER = 500;
	/** Per axis speed cap in blocks a tick, and the raised cap a warp core buys in open space */
	private static final double BASE_MAX_SPEED = 1.0D;
	private static final double WARP_MAX_SPEED = 8.0D;
	/** Fuel per block jumped.  One fuel tank holds 500, so a full tank is worth about 2000 blocks of warp. */
	private static final double WARP_FUEL_PER_BLOCK = 0.25D;
	private static final int BUTTON_ID_OFFSET = 25;
	private static final int STATION_LOC_OFFSET = 50;
	/** Sits below tilebuttonOffset so it cannot collide with a tile's own button */
	private static final int BUTTON_ID_WARP = 2;
	/**
	 * Half angle of the cone used to decide which body the pilot is looking at.  A body is only a few skybox
	 * pixels across - under a degree even for a close planet - so matching its drawn size would make it
	 * impossible to point at.  The bearing readout covers precise aiming.
	 */
	private static final double SIGHT_CONE = Math.toRadians(8.0D);
	private ModuleText landingPadDisplayText;
	protected long lastWorldTickTicked;

	private SatelliteBase satallite;
	protected int destinationDimId;
	//Offset for buttons linking to the tileEntityGrid
	private final int tilebuttonOffset = 3;
	private int autoDescendTimer;
	/** Ticks spent adrift in space with no fuel; drives the rescue in SpaceTravelHandler */
	private int strandedTicks;
	private WeakReference<Entity>[] mountedEntities;
	protected ModulePlanetSelector container;
	public int comeFromDimID = spaceDimId;


	public enum PacketType {
		RECEIVE_NBT,
		SEND_INTERACT,
		REQUEST_NBT,
		FORCE_MOUNT,
		LAUNCH,
		DECONSTRUCT,
		OPEN_GUI,
		CHANGE_WORLD,
		REVERT_WORLD,
		OPEN_PLANET_SELECTION,
		SEND_PLANET_DATA,
		DISCONNECT_INFRASTRUCTURE,
		CONNECT_INFRASTRUCTURE,
		ROCKET_LAND_EVENT,
		MENU_CHANGE,
		UPDATE_ATM,
		UPDATE_ORBIT,
		UPDATE_FLIGHT,
		SHOW_ERROR
	}

	public EntityRocket(World p_i1582_1_) {
		super(p_i1582_1_);
		isInOrbit = false;
		stats = new StatsRocket();
		isInFlight = false;
		connectedInfrastructure = new LinkedList<>();
		infrastructureCoords = new HashSet<>();
		mountedEntities = new WeakReference[stats.getNumPassengerSeats()];

		lastWorldTickTicked = p_i1582_1_.getTotalWorldTime();
		autoDescendTimer = 5000;
		landingPadDisplayText = new ModuleText(256, 16, "", 0x00FF00, 2f);
		landingPadDisplayText.setColor(0x00ff00);
	}
	public EntityRocket(@NotNull World world, @NotNull StorageChunk storage, @NotNull StatsRocket stats, double x, double y, double z) {
		this(world);
	    this.stats = stats;
		this.setPosition(x, y, z);
		this.storage = storage;
		this.storage.setEntity(this);
		//Debris has no guidance computer, so there is nothing to divide and no point queueing work
		if(!world.isRemote && storage.getGuidanceComputer() != null) {
			stagesPending = true;
			AdvancedRocketry.rocketStructureDivider.addATask(this.entityUniqueID, storage);
		}
		initFromBounds();
		isInFlight = false;
		mountedEntities = new WeakReference[stats.getNumPassengerSeats()];
		lastWorldTickTicked = world.getTotalWorldTime();
		autoDescendTimer = 5000;
		landingPadDisplayText = new ModuleText(256, 16, "", 0x00FF00, 2f);
		landingPadDisplayText.setColor(0x00ff00);
	}


	@Override
	public AxisAlignedBB getBoundingBox() {
		if(storage != null) {
			//MobileAABB aabb = new MobileAABB(this.boundingBox);
			//aabb.setStorageChunk(storage);
			//return aabb;
			return this.boundingBox;
		}
		return null;
	}

	/**
	 * @return the amount of fuel stored in the rocket
	 */
	public int getFuelAmount() {
		int amount = dataWatcher.getWatchableObjectInt(17);
		stats.setFuelAmount(FuelType.LIQUID,amount);
		return amount;
	}

	/**
	 * Adds fuel and updates the datawatcher
	 * @param amount amount of fuel to add
	 * @return the amount of fuel added
	 */
	public int addFuelAmount(int amount) {
		int ret = stats.addFuelAmount(FuelType.LIQUID, amount);

		setFuelAmount(stats.getFuelAmount(FuelType.LIQUID));

		return ret;
	}

	public void disconnectInfrastructure(IInfrastructure infrastructure){
		infrastructure.unlinkRocket();
		infrastructureCoords.remove(new BlockPosition(((TileEntity)infrastructure).xCoord, ((TileEntity)infrastructure).yCoord, ((TileEntity)infrastructure).zCoord));

		if(!worldObj.isRemote) {
			int[] pos = {((TileEntity)infrastructure).xCoord, ((TileEntity)infrastructure).yCoord, ((TileEntity)infrastructure).zCoord};

			NBTTagCompound nbt = new NBTTagCompound();
			nbt.setIntArray("pos", pos);
			//PacketHandler.sendToPlayersTrackingEntity(new PacketEntity(this, (byte)PacketType.DISCONNECTINFRASTRUCTURE.ordinal(), nbt), this);
		}
	}

	@Override
	public void linkInfrastructure(IInfrastructure tile) {
		super.linkInfrastructure(tile);
		BlockPosition pos =new BlockPosition(((TileEntity)tile).xCoord, ((TileEntity)tile).yCoord, ((TileEntity)tile).zCoord);
        infrastructureCoords.add(pos);
	}
	
	@Override
	public String getTextOverlay() {

		if(this.worldObj.getTotalWorldTime() < this.lastErrorTime + ERROR_DISPLAY_TIME)
			return errorStr;

		//Out in open space the only thing worth showing is where to steer
		if(this.worldObj.provider.dimensionId == spaceDimId)
			return getNavigationOverlay();

		//Get destination string
		String displayStr = LibVulpes.proxy.getLocalizedString("msg.na");
		if(storage != null) {
			int dimid = storage.getDestinationDimId(this.worldObj.provider.dimensionId, (int)posX, (int)posZ);

			if(dimid == Configuration.stationDimId) {
				Vector3F<Float> vec = storage.getDestinationCoordinates(dimid, false);
				if(vec != null) {

					ISpaceObject obj = SpaceObjectManager.getSpaceManager().getSpaceStationFromBlockCoords((int)((float)vec.x),(int)((float)vec.x));

					if(obj != null) {
						displayStr =  LibVulpes.proxy.getLocalizedString("msg.entity.rocket.station") + obj.getId();
						TileGuidanceComputer computer = storage.getGuidanceComputer();
						StationLandingLocation location = computer == null ? null : computer.getLandingLocation(obj.getId());

						if(location != null) {
							displayStr = displayStr + "\n" + LibVulpes.proxy.getLocalizedString("msg.entity.rocket.pad") + location;
						}
					}
				}
			}
			else if(dimid != -1 && dimid != SpaceObjectManager.WARPDIMID) {
				displayStr = DimensionManager.getInstance().getDimensionProperties(dimid).getName();
			}
		}

		if(isInOrbit() && !isInFlight())
			return LibVulpes.proxy.getLocalizedString("msg.entity.rocket.descend.1") + "\n" + LibVulpes.proxy.getLocalizedString("msg.entity.rocket.descend.2") + ((DESCENT_TIMER - this.ticksExisted)/20);
		else if(!isInFlight())
			return LibVulpes.proxy.getLocalizedString("msg.entity.rocket.ascend.1") + "\n" + LibVulpes.proxy.getLocalizedString("msg.entity.rocket.ascend.2") + displayStr;

		return super.getTextOverlay();
	}

	/**
	 * Name, range and heading of the nearest body, plus whatever the pilot has under the crosshair.  Without this
	 * there is nothing to navigate by in the space dimension - the bodies are painted onto the skybox and give no
	 * parallax to steer with.
	 */
	private String getNavigationOverlay() {
		SimUniverse.SimBody target = SimUniverse.getInstance().findNearest(posX, posY, posZ);
		if(target == null) return LibVulpes.proxy.getLocalizedString("msg.entity.rocket.nonav");

		double dx = target.x - posX;
		double dy = target.y - posY;
		double dz = target.z - posZ;
		double dist = Math.sqrt(dx*dx + dy*dy + dz*dz);

		//How far off the pilot's line of sight the target sits, so "0" means dead ahead
		Vec3 look = this.riddenByEntity != null ? this.riddenByEntity.getLookVec() : Vec3.createVectorHelper(0, 0, 1);
		double dot = (look.xCoord*dx + look.yCoord*dy + look.zCoord*dz) / Math.max(1.0E-4D, dist);
		int offBy = (int) Math.toDegrees(Math.acos(Math.max(-1, Math.min(1, dot))));

		String name = target.getName();
		if(name == null || name.isEmpty()) name = LibVulpes.proxy.getLocalizedString("msg.na");

		String overlay = LibVulpes.proxy.getLocalizedString("msg.entity.rocket.nav") + " " + name
				+ "\n" + LibVulpes.proxy.getLocalizedString("msg.entity.rocket.range") + " " + (int) dist
				+ " / " + LibVulpes.proxy.getLocalizedString("msg.entity.rocket.bearing") + " " + offBy + "°";

		//Name whatever is under the crosshair, which is usually not the nearest body.  Appended rather than
		//prepended: it comes and goes as the pilot turns, and the lines above should not jump around with it.
		SimUniverse.SimBody sighted = SimUniverse.getInstance().findLookingAt(posX, posY, posZ,
				look.xCoord, look.yCoord, look.zCoord, SIGHT_CONE);

		if(sighted != null) {
			String sightedName = sighted.getName();
			if(sightedName == null || sightedName.isEmpty()) sightedName = LibVulpes.proxy.getLocalizedString("msg.na");

			overlay = overlay + "\n" + LibVulpes.proxy.getLocalizedString("msg.entity.rocket.sighted") + " " + sightedName;
		}

		return overlay;
	}

	/**
	 * Shows an error on the pilot's HUD.  errorStr is not a synced field, so a failure raised on the server has
	 * to be pushed to whoever is aboard.
	 */
	private void setError(String error) {
		this.errorStr = error;
		this.lastErrorTime = this.worldObj.getTotalWorldTime();

		if(!worldObj.isRemote && riddenByEntity instanceof EntityPlayer) {
			NBTTagCompound nbt = new NBTTagCompound();
			nbt.setString("error", error);
			PacketHandler.sendToPlayer(new PacketEntity(this, (byte)PacketType.SHOW_ERROR.ordinal(), nbt), (EntityPlayer)riddenByEntity);
		}
	}

	@Override
	public void setPosition(double x, double y,
			double z) {
		super.setPosition(x, y, z);

		if(storage != null) {
			float sizeX = storage.getSizeX()/2.0f;
			float sizeY = storage.getSizeY();
			float sizeZ = storage.getSizeZ()/2.0f;
			this.boundingBox.setBounds(x - sizeX, y - (double)this.yOffset + this.ySize, z - sizeZ, x + sizeX, y + sizeY - (double)this.yOffset + this.ySize, z + sizeZ);
		}
	}

	/**
	 * Updates the data option
	 * @param amt sets the amount of fuel in the rocket
	 */
	public void setFuelAmount(int amt) {
		dataWatcher.updateObject(17, amt);
	}

	/**
	 * @return gets the fuel capacity of the rocket
	 */
	public int getFuelCapacity() {
		return stats.getFuelCapacity(FuelType.LIQUID);
	}

	@Override
	public void setEntityId(int id){
		super.setEntityId(id);
		//Ask server for nbt data
		if(worldObj.isRemote) {
			PacketHandler.sendToServer(new PacketEntity(this, (byte)PacketType.REQUEST_NBT.ordinal()));
		}
	}

	@Override
	public boolean canBeCollidedWith() {
		return true;
	}

	/**is Rocket in space, orbiting any planet or star.**/
	public boolean isInTravel(){
		return false;
	}
	/**
	 * If the rocket is in flight, ie the rocket has taken off and has not touched the ground
	 * @return true if in flight
	 */
	public boolean isInFlight() {
		if(!worldObj.isRemote) return isInFlight;
		return this.dataWatcher.getWatchableObjectByte(16) == 1;
	}

	/**
	 * Sets the status of flight of the rocket and updates the datawatcher
	 * @param inOrbit status of flight
	 */
	public void setInOrbit(boolean inOrbit) {
		this.isInOrbit = inOrbit;
		this.dataWatcher.updateObject(18, isInOrbit ? (byte) 1 : (byte) 0);
	}

	/**
	 * If the rocket is in flight, ie the rocket has taken off and has not touched the ground
	 * @return true if in flight
	 */
	public boolean isInOrbit() {
		if(!worldObj.isRemote)return isInOrbit;
		return this.dataWatcher.getWatchableObjectByte(18) == 1;
	}

	/**
	 * Sets the  status of flight of the rocket and updates the datawatcher
	 * @param inflight status of flight
	 */
	public void setInFlight(boolean inflight) {
		this.isInFlight = inflight;
		this.dataWatcher.updateObject(16, isInFlight ? (byte) 1 : (byte) 0);
	}

	@Override
	protected void entityInit() {
		this.dataWatcher.addObject(16, isInFlight ? (byte) 1 : (byte) 0);
		this.dataWatcher.addObject(17, 0);
		this.dataWatcher.addObject(18, isInOrbit ? (byte) 1 : (byte) 0);
	}

	//Set the size and position of the rocket from storage
	public void initFromBounds() {		
		if(storage != null) {
			this.setSize(Math.max(storage.getSizeX(), storage.getSizeZ()), storage.getSizeY());
			this.setPosition(this.posX, this.posY, this.posZ);
		}
	}

	protected boolean interact(EntityPlayer player) {
		//Actual interact code needs to be moved to a packet receive on the server

		ItemStack heldItem = player.getHeldItem();

		//Handle linkers and right-click with fuel
		if(heldItem == null){
			if(player.isSneaking()) {
				openGui(player);
				return true;
			}

			//If pilot seat is open mount entity there
			if (stats.hasSeat() && this.riddenByEntity == null) {
				if (!worldObj.isRemote) player.mountEntity(this);
				return true;
			}
			/*else if(stats.getNumPassengerSeats() > 0) { //If a passenger seat exists and one is empty, mount the player to it
				for(int i = 0; i < stats.getNumPassengerSeats(); i++) {
					if(this.mountedEntities[i] == null || this.mountedEntities[i].get() == null) {
						player.ridingEntity = this;
						this.mountedEntities[i] = new WeakReference<Entity>(player);
						break;
					}
				}
			}*/
			return false;
		}
		if(heldItem.getItem() instanceof ItemLinker) {
			if (!ItemLinker.isSet(heldItem)) return sendMessage(player, "Nothing to be linked");
			TileEntity tile = this.worldObj.getTileEntity(ItemLinker.getMasterX(heldItem), ItemLinker.getMasterY(heldItem), ItemLinker.getMasterZ(heldItem));
			if (!(tile instanceof IInfrastructure)) return sendMessage(player,"This cannot be linked to a rocket!");

			IInfrastructure infrastructure = (IInfrastructure) tile;

			if (this.getDistance(ItemLinker.getMasterX(heldItem), this.posY, ItemLinker.getMasterZ(heldItem)) >= infrastructure.getMaxLinkDistance() + Math.max(storage.getSizeX(), storage.getSizeZ())) return sendMessage(player, "The object you are trying to link is too far away");
			if (connectedInfrastructure.contains(tile)) return sendMessage(player, "Already linked!");
			linkInfrastructure(infrastructure);

			if (!worldObj.isRemote) player.addChatMessage(new ChatComponentText("Linked Sucessfully"));

			ItemLinker.resetPosition(heldItem);
			return true;
		}
		return true;
	}
	public boolean sendMessage(EntityPlayer player, String string) {
		if (!worldObj.isRemote) player.addChatMessage(new ChatComponentText(string));
		return true;
	}

	/**@return Error message, should be a translation key**/
	public String onLinkerUsed(ItemStack heldItem, EntityPlayer player){
		if(!ItemLinker.isSet(heldItem)) return "msg.linker.empty";

		TileEntity tile = this.worldObj.getTileEntity(ItemLinker.getMasterX(heldItem), ItemLinker.getMasterY(heldItem), ItemLinker.getMasterZ(heldItem));

		if (!(tile instanceof IInfrastructure)) {
			if(tile == null) return "msg.linker.error.invalid";
			else return "msg.linker.error.incompatible";
		}
		if (this.getDistance(ItemLinker.getMasterX(heldItem), this.posY, ItemLinker.getMasterZ(heldItem)) > ((IInfrastructure) tile).getMaxLinkDistance() + Math.max(storage.getSizeX(), storage.getSizeZ())) return "msg.linker.too_far";
		if (connectedInfrastructure.contains(tile)) return "msg.linker.already_linked";

		linkInfrastructure((IInfrastructure) tile);
		player.addChatMessage(new ChatComponentText(I18n.format("msg.linker.success")));

		if(player.isSneaking()){
			ItemLinker.resetPosition(heldItem);
			player.addChatMessage(new ChatComponentText(I18n.format("msg.linker.reset")));
		}
		return null;
	}


	public void openGui(EntityPlayer player) {
		player.openGui(LibVulpes.instance, GuiHandler.guiId.MODULAR.ordinal(), player.worldObj, this.getEntityId(), -1,0);

		//Only handle the bypass on the server
		if(!worldObj.isRemote) PlanetEventHandler.addPlayerToInventoryBypass(player);
	}


	@Override
	public boolean interactFirst(EntityPlayer player) {
		if(worldObj.isRemote) {
			//Due to forge's rigid handling of entities (NetHandlerPlayServer:866) needs to be handled differently for large rockets
			PacketHandler.sendToServer(new PacketEntity(this, (byte)PacketType.SEND_INTERACT.ordinal()));
			return interact(player);
		}
		return true;

	}


	public boolean isBurningFuel() {
		return (getFuelAmount() > 0 || !Configuration.rocketRequireFuel) && (!(this.riddenByEntity instanceof EntityPlayer) || !isInOrbit() || ((EntityPlayer)this.riddenByEntity).moveForward > 0);
	}

	public boolean isDescentPhase() {
		//Nothing to descend to in open space, and bodies there can sit at any altitude
		if(worldObj.provider.dimensionId == spaceDimId) return false;
		return Configuration.automaticRetroRockets && isInOrbit() && this.posY < 300 && (this.motionY < -0.4f || worldObj.isRemote);
	}

	public boolean areEnginesRunning() {
		return (this.motionY > 0 || isDescentPhase());
	}

	/**
	 * Speed cap per axis, in blocks a tick.  A warp core raises it, which is what makes crossing between star
	 * systems a couple of minutes rather than the best part of an hour.
	 */
	public double getMaxSpeed() {
		if(worldObj.provider.dimensionId == spaceDimId && storage != null && storage.hasWarpCore())
			return WARP_MAX_SPEED;
		return BASE_MAX_SPEED;
	}


	@Override
	public void onUpdate() {
		super.onUpdate();
		if(stagesPending && AdvancedRocketry.rocketStructureDivider.isTaskCompleted(entityUniqueID)) {
			stageLayout = AdvancedRocketry.rocketStructureDivider.getResultAndRemove(entityUniqueID);
			stagesPending = false;
		}
		long deltaTime = worldObj.getTotalWorldTime() - lastWorldTickTicked;
		lastWorldTickTicked = worldObj.getTotalWorldTime();

		if(this.ticksExisted == 20) {
			//problems with loading on other world then where the infrastructure was set?
			for (BlockPosition temp : new LinkedList<>(infrastructureCoords)) {
				TileEntity tile = this.worldObj.getTileEntity(temp.x, temp.y, temp.z);
				if (tile instanceof IInfrastructure) {
					this.linkInfrastructure((IInfrastructure) tile);
				}
			}
		}
		
		if(this.ticksExisted == 1 && worldObj.isRemote) {
			LibVulpes.proxy.playSound(new SoundRocketEngine( TextureResources.sndCombustionRocket,this));
		}

		if(this.ticksExisted > DESCENT_TIMER && isInOrbit() && !isInFlight())
			setInFlight(true);

		//Hackish crap to make clients mount entities immediately after server transfer and fire events
		if(!worldObj.isRemote && (this.isInFlight() || this.isInOrbit()) && this.ticksExisted == 20) {
			if(this.riddenByEntity instanceof EntityPlayer) {
				EntityPlayer player = (EntityPlayer)this.riddenByEntity;
				//Deorbiting
				MinecraftForge.EVENT_BUS.post(new RocketEvent.RocketDeOrbitingEvent(this));
				PacketHandler.sendToNearby(new PacketEntity(this, (byte)PacketType.ROCKET_LAND_EVENT.ordinal()), worldObj.provider.dimensionId, (int)posX, (int)posY, (int)posZ, 64);
				PacketHandler.sendToPlayer(new PacketEntity(this,(byte)PacketType.FORCE_MOUNT.ordinal()), player);
			}
		}


		if(isInFlight()) {
			boolean burningFuel = isBurningFuel();

			boolean descentPhase = isDescentPhase();

			//Drop spent stages on the way up only, a rocket coming back down has nothing left to shed
			if(!worldObj.isRemote && !isInOrbit()) separateSpentStages();

			if(burningFuel || descentPhase) {
				//Burn the rocket fuel
				if(!worldObj.isRemote && !descentPhase) setFuelAmount((int) (getFuelAmount() - stats.getFuelRate(FuelType.LIQUID)*(Configuration.gravityAffectsFuel ? DimensionManager.getInstance().getDimensionProperties(worldObj.provider.dimensionId).getGravitationalMultiplier() : 1f)));

				//Spawn in the particle effects for the engines
				if(worldObj.isRemote && Minecraft.getMinecraft().gameSettings.particleSetting < 2 && areEnginesRunning()) {
					int engineNum = 0;
					for(Vector3F<Float> vec : stats.getEngineLocations()) {

						AtmosphereHandler handler;
						//Cycle through engines outputting smoke, increases performance with craft with large number of engines
						if(worldObj.getTotalWorldTime() % 10 == 0 && (engineNum < 8 || ((worldObj.getTotalWorldTime()/10) % Math.max((stats.getEngineLocations().size()/8),1)) == (engineNum/8)) && ( (handler = AtmosphereHandler.getOxygenHandler(worldObj.provider.dimensionId)) == null  || handler.getAtmosphereType(this).allowsCombustion()) )
							AdvancedRocketry.proxy.spawnParticle("rocketSmoke", worldObj, this.posX + vec.x, this.posY + vec.y - 4.75, this.posZ +vec.z,0,0,0);

						AdvancedRocketry.proxy.spawnParticle("rocketFlame", worldObj, this.posX + vec.x, this.posY + vec.y - 0.75, this.posZ +vec.z,(this.rand.nextFloat() - 0.5f)/8f,-.75 ,(this.rand.nextFloat() - 0.5f)/8f);


						engineNum++;
					}
				}
			}

			if(this.riddenByEntity instanceof EntityPlayer) {
				EntityPlayer player = (EntityPlayer)this.riddenByEntity;
				player.fallDistance = 0;
				this.fallDistance = 0;

				//Thrust along the pilot's line of sight.  In space this is the only way to move at all; over a
				//planet it doubles as the retro burn.
				if(isInOrbit() && (burningFuel || descentPhase)) {
					float vel =  player.moveForward/100F;
					Vec3 look = player.getLook(0.01F);
					double precision = 100.0D; // 保留两位小数
					double lookX = Math.round(look.xCoord * precision) / precision;
					double lookY = Math.round(look.yCoord * precision) / precision;
					double lookZ = Math.round(look.zCoord * precision) / precision;

					this.motionX += lookX * vel;
					this.motionY += lookY * vel;
					this.motionZ += lookZ * vel;

					double cap = getMaxSpeed();
					this.motionX = Math.max(-cap, Math.min(this.motionX, cap));
					this.motionY = Math.max(-cap, Math.min(this.motionY, cap));
					this.motionZ = Math.max(-cap, Math.min(this.motionZ, cap));

					this.velocityChanged = true;
				}
				this.isAirBorne = true;
			}
			else if(isInOrbit() && descentPhase) { //For unmanned rockets
				this.motionY -= this.motionY/50f;
				this.velocityChanged = true;
			}
			//If out of fuel or descending then accelerate downwards
			if(isInOrbit() || !burningFuel) {
				if(this.worldObj.provider.dimensionId != spaceDimId) this.motionY = Math.min(this.motionY - 0.1, 1);
			} else
				this.motionY += stats.getAcceleration() * deltaTime;

			if(!worldObj.isRemote) {
				boolean inSpace = this.worldObj.provider.dimensionId == spaceDimId;
				double lastPosY = this.posY;
				double prevMotion = this.motionY;
				this.moveEntity(this.motionX, prevMotion, this.motionZ);

				//Check to see if it's landed.  There is no ground in space, and coasting there must not be
				//mistaken for a touchdown.
				if(!inSpace && (isInOrbit() || !burningFuel) && isInFlight() && lastPosY + prevMotion != this.posY && this.posY < 256) {
					//Did  sending this packet cause problems?
					PacketHandler.sendToPlayersTrackingEntity(new PacketEntity(this, (byte)PacketType.ROCKET_LAND_EVENT.ordinal()), this);
					MinecraftForge.EVENT_BUS.post(new RocketEvent.RocketLandedEvent(this));
					this.setInFlight(false);
					this.setInOrbit(false);
				}
				if(!inSpace && !isInOrbit() && (this.posY > Configuration.orbit)) {
					onOrbitReached();
				}

				if(this.posY < 0 && !inSpace) onRockedFallsOutOfWorld();
			}
			else this.moveEntity(this.motionX, this.motionY, this.motionZ);
		}
	}
	@Override
	@SideOnly(Side.CLIENT)
	public void setVelocity(double x, double y, double z) {super.setVelocity(x, y, z);
	}

	@SideOnly(Side.CLIENT)
	@Override
	public void setPositionAndRotation2(double x, double y, double z, float yaw, float pitch, int p_70056_9_) {
		super.setPositionAndRotation2(x, y, z, yaw, pitch, p_70056_9_);
	}
	public void onRockedFallsOutOfWorld() {
		//If the rocket falls out of the world while in orbit either fall back to earth or die
		int dimId = worldObj.provider.dimensionId;

		if(dimId == Configuration.stationDimId || dimId == spaceDimId) {

			ISpaceObject obj = SpaceObjectManager.getSpaceManager().getSpaceStationFromBlockCoords((int)this.posX, (int)this.posZ);

			if(obj != null) {
				int targetDimID = obj.getOrbitingPlanetId();

				Vector3F<Float> pos = storage.getDestinationCoordinates(targetDimID, true);
				if(pos != null) {
					setInOrbit(true);
					setInFlight(false);
					this.travelToDimension(destinationDimId, pos.x, Configuration.orbit, pos.z);
				}
				else this.setDead();
			}
			else {
				Vector3F<Float> pos = storage.getDestinationCoordinates(lastDimensionFrom, true);
				if(pos != null) {
					setInOrbit(true);
					setInFlight(false);
					this.travelToDimension(lastDimensionFrom, pos.x, Configuration.orbit, pos.z);
				}
				else this.setDead();
			}
		}
		else this.setDead();
	}


	/**
	 * @return a list of satellites stores in this rocket
	 */
	public @NotNull List<SatelliteBase> getSatellites() {
		List<SatelliteBase> satellites = new ArrayList<>();
		for(TileSatelliteHatch tile : storage.getSatelliteHatches()) {
			SatelliteBase satellite = tile.getSatellite();
			if(satellite != null)
				satellites.add(satellite);
		}
		return satellites;
	}

	/**
	 * Called when the rocket reaches orbit
	 */
	public void onOrbitReached() {
		super.onOrbitReached();

		if(!stats.hasSeat()) {
			unpackSatellites();

			Vector3F<Float> targetPos = new Vector3F<>((float) this.posX, (float) this.posY, (float) this.posZ);

			storage.setDestinationCoordinates(targetPos, this.worldObj.provider.dimensionId);

			this.setInOrbit(true);
			this.motionY = -this.motionY;

			//unlink any connected tiles
			Iterator<IInfrastructure> connectedTiles = connectedInfrastructure.iterator();
			while(connectedTiles.hasNext()) {
				connectedTiles.next().unlinkRocket();
				connectedTiles.remove();
			}
			this.setPosition(targetPos.x, Configuration.orbit, targetPos.z);
			return;
		}
		unpackSatellites();
		setInOrbit(true);

		//if coordinates are overridden, make sure we grab them
		Vector3F<Float> destPos = storage.getDestinationCoordinates(destinationDimId, true);
		if(destPos == null) destPos = new Vector3F<>((float) posX, (float) Configuration.orbit, (float) posZ);

		this.motionX = 0;
		this.motionY = 0;
		this.motionZ = 0;
		//Reset override coords
		setOverriddenCoords(-1, 0, 0, 0);

		//A station orbits the planet it is above rather than sitting anywhere in the simulated universe, so
		//going to or from one is still a straight dimension change
		if(destinationDimId == Configuration.stationDimId || worldObj.provider.dimensionId == Configuration.stationDimId) {
			this.travelToDimension(destinationDimId, destPos.x, Configuration.orbit, destPos.z);
			if(this.riddenByEntity != null) setInFlight(false);
			return;
		}

		departToSpace();
	}

	/**
	 * Puts the rocket into the space dimension just outside the body it launched from, so the pilot can fly to
	 * wherever they are going.  Every crewed launch goes through here - there is no direct transfer any more.
	 */
	private void departToSpace() {
		SimUniverse.SimBody body = SimUniverse.getInstance().getBodyForDim(this.worldObj.provider.dimensionId);

		if(body == null) {
			//No simulated body to launch from - a dimension another mod owns, or a universe that failed to
			//build.  Fall back to the old behaviour rather than stranding the pilot.
			AdvancedRocketry.logger.warn("No simulated body for dim {}, falling back to a direct transfer", this.worldObj.provider.dimensionId);
			Vector3F<Float> destPos = storage.getDestinationCoordinates(destinationDimId, true);
			if(destPos == null) destPos = new Vector3F<>((float) posX, (float) Configuration.orbit, (float) posZ);
			this.travelToDimension(destinationDimId, destPos.x, Configuration.orbit, destPos.z);
			if(this.riddenByEntity != null) setInFlight(false);
			return;
		}

		//Head away in whatever direction the pilot is facing, so the launch heading means something.  The sim
		//picks the actual spot: just outside the capture sphere and clear of anything else nearby.
		float yaw = (float) Math.toRadians(this.rotationYaw);
		double[] dest = SimUniverse.getInstance().findClearSpot(body, -Math.sin(yaw), Math.cos(yaw));

		//Inherit the body's orbital motion so the rocket keeps station with it instead of being left behind.
		//Set before the transfer: travelToDimension replaces this entity, and only NBT-backed state survives.
		this.motionX = body.getVelX();
		this.motionY = body.getVelY();
		this.motionZ = body.getVelZ();

		this.travelToDimension(Configuration.spaceDimId, dest[0], dest[1], dest[2]);
	}

	/**
	 * Arrives at a body: drops out of the space dimension onto it, in orbit with the engines off so the pilot
	 * gets the usual descent prompt rather than falling straight out of the sky.
	 *
	 * Reads the landing coordinates before clearing them - the fallback destination is both the pad a landing pad
	 * linked us to and the slot that has to be reset, so the order matters.
	 */
	public void deorbitTo(int dimId) {
		Vector3F<Float> dest = storage == null ? null : storage.getDestinationCoordinates(dimId, true);

		destinationDimId = dimId;
		setInOrbit(true);
		setInFlight(false);
		this.motionX = 0;
		this.motionY = 0;
		this.motionZ = 0;
		setOverriddenCoords(-1, 0, 0, 0);
		strandedTicks = 0;

		//Falling back to our own x/z would use space coordinates, which are the body's galactic position - a
		//couple of thousand blocks from anywhere useful.  Spawn is a far better guess.
		double x, z;
		if(dest != null) {
			x = dest.x;
			z = dest.z;
		}
		else {
			ChunkCoordinates spawn = worldObj.getSpawnPoint();
			x = spawn.posX;
			z = spawn.posZ;
		}

		this.travelToDimension(dimId, x, Configuration.orbit, z);
	}

	public int getLastDimensionFrom() {
		return lastDimensionFrom;
	}

	public int incrementStrandedTimer() {
		return ++strandedTicks;
	}

	public void resetStrandedTimer() {
		strandedTicks = 0;
	}

	/**
	 * Warp jump: crosses the space dimension in one step and arrives just outside the target body, from where the
	 * pilot still has to fly in and descend.  This is what a warp core is for - flying between star systems at
	 * the impulse speed cap would take most of an hour.
	 *
	 * @param dimID dimension of the body to jump to
	 * @param distance range the client thought it was asking for; recomputed here before charging for it
	 */
	@Override
	public void travelTo(int dimID, int distance) {
		if(worldObj.isRemote) return;

		if(worldObj.provider.dimensionId != spaceDimId) {
			setError(LibVulpes.proxy.getLocalizedString("error.rocket.warpNotInSpace"));
			return;
		}

		if(storage == null || !storage.hasWarpCore()) {
			setError(LibVulpes.proxy.getLocalizedString("error.rocket.needWarpCore"));
			return;
		}

		SimUniverse.SimBody target = SimUniverse.getInstance().getBodyForDim(dimID);
		if(target == null) {
			setError(LibVulpes.proxy.getLocalizedString("error.rocket.destinationNotExist"));
			return;
		}

		double range = target.distanceTo(posX, posY, posZ);
		int cost = (int) Math.ceil(range * WARP_FUEL_PER_BLOCK);

		if(Configuration.rocketRequireFuel && getFuelAmount() < cost) {
			setError(LibVulpes.proxy.getLocalizedString("error.rocket.notEnoughFuel") + cost);
			return;
		}

		if(Configuration.rocketRequireFuel) setFuelAmount(getFuelAmount() - cost);

		//Match the target's orbital motion, otherwise it drifts out from under us while we close in
		this.motionX = target.getVelX();
		this.motionY = target.getVelY();
		this.motionZ = target.getVelZ();
		this.velocityChanged = true;

		//Arrive on the side we approached from, outside the capture sphere, so the pilot still makes the approach
		double[] dest = SimUniverse.getInstance().findClearSpot(target, posX - target.x, posZ - target.z);
		//setLocationAndAngles rather than setPosition: prevPos has to move too, or the capture sweep next tick
		//traces a line all the way back from where we jumped and lands us on something en route
		setLocationAndAngles(dest[0], dest[1], dest[2], rotationYaw, rotationPitch);

		//Bring the passengers along; the rocket carries them but the client needs telling where they now are
		if(this.riddenByEntity instanceof EntityPlayerMP)
			((EntityPlayerMP) this.riddenByEntity).playerNetServerHandler.setPlayerLocation(posX, posY, posZ, riddenByEntity.rotationYaw, riddenByEntity.rotationPitch);

		PacketHandler.sendToPlayersTrackingEntity(new PacketEntity(this, (byte)PacketType.ROCKET_LAND_EVENT.ordinal()), this);
	}
	private void unpackSatellites() {
		List<TileSatelliteHatch> satelliteHatches = storage.getSatelliteHatches();

		for(TileSatelliteHatch tile : satelliteHatches) {
			SatelliteBase satellite = tile.getSatellite();
			if(satellite == null) {
				ItemStack stack = tile.getStackInSlot(0);
				if(stack != null && stack.getItem() == AdvancedRocketryItems.itemSpaceStation) {
					StorageChunk storage = ((ItemPackedStructure)stack.getItem()).getStructure(stack);
					ISpaceObject object = SpaceObjectManager.getSpaceManager().getSpaceStation(stack.getItemDamage());
					
					//in case of no NBT data or the like
					if(object == null) {
						tile.setInventorySlotContents(0, null);
						continue;
					}
					
					SpaceObjectManager.getSpaceManager().moveStationToBody(object, 
									DimensionManager.getEffectiveDimId(this.worldObj.provider.dimensionId, (int)posX, (int)posZ).getId() );

					//Vector3F<Integer> spawn = object.getSpawnLocation();

					object.onModuleUnpack(storage);

					tile.setInventorySlotContents(0, null);
				}
			}
			else {
				DimensionProperties properties = DimensionManager.getEffectiveDimId(worldObj, (int)this.posX, (int)this.posZ);
				World world = net.minecraftforge.common.DimensionManager.getWorld(properties.getId());

				if(world != null)
					properties.addSatallite(satellite, world);
				tile.setInventorySlotContents(0, null);
			}
		}
	}

	@Override
	/**
	 * Called immediately before launch
	 */
	public void prepareLaunch() {
		@NotNull RocketPreLaunchEvent event = new RocketEvent.RocketPreLaunchEvent(this);
		MinecraftForge.EVENT_BUS.post(event);

		if(!event.isCanceled()) {
			if(worldObj.isRemote)
				PacketHandler.sendToServer(new PacketEntity(this, (byte)EntityRocket.PacketType.LAUNCH.ordinal()));
			launch();
		}
	}

	@Override
	public void launch() {

		if(isInFlight())
			return;

		if(isInOrbit()) {
			setInFlight(true);
			return;
		}

		//Get destination dimid and lock the computer
		//TODO: lock the computer
		destinationDimId = storage.getDestinationDimId(worldObj.provider.dimensionId, (int)this.posX, (int)this.posZ);

		//TODO: make sure this doesn't break asteroid mining
		if(!(DimensionManager.getInstance().canTravelTo(destinationDimId) || (destinationDimId == -1 && !storage.getSatelliteHatches().isEmpty()))) {
			setError(LibVulpes.proxy.getLocalizedString("error.rocket.cannotGetThere"));
			return;
		}

		int finalDest = destinationDimId;
		if(destinationDimId == Configuration.stationDimId) {
			ISpaceObject obj = null;
			Vector3F<Float> vec = storage.getDestinationCoordinates(destinationDimId,false);
			if(vec != null)
				obj = SpaceObjectManager.getSpaceManager().getSpaceStationFromBlockCoords((int)(float)vec.x, (int)(float)vec.z);

			if(obj != null)
				finalDest = obj.getOrbitingPlanetId();
			else { 
				setError(LibVulpes.proxy.getLocalizedString("error.rocket.destinationNotExist"));
				return;
			}
		}

		//If we're on a space station get the id of the planet, not the station
		int thisDimId = this.worldObj.provider.dimensionId;
		if(this.worldObj.provider.dimensionId == Configuration.stationDimId) {
			ISpaceObject object = SpaceObjectManager.getSpaceManager().getSpaceStationFromBlockCoords((int)this.posX, (int)this.posZ);
			if(object != null)
				thisDimId = object.getProperties().getParentProperties().getId();
		}

		//Check to see if it's possible to reach.  A crewed rocket flies there itself through the space dimension,
		//so the only hard limit is that leaving the home star system needs a warp core; an uncrewed rocket is
		//transferred directly and so is still confined to its own planet/moon system.
		if(finalDest != -1) {
			boolean sameStar = DimensionManager.getInstance().getDimensionProperties(finalDest).getStarId()
					== DimensionManager.getInstance().getDimensionProperties(thisDimId).getStarId();

			if(stats.hasSeat()) {
				if(!sameStar && !storage.hasWarpCore()) {
					setError(LibVulpes.proxy.getLocalizedString("error.rocket.needWarpCore"));
					return;
				}
			}
			else if(!sameStar || !DimensionManager.getInstance().areDimensionsInSamePlanetMoonSystem(finalDest, thisDimId)) {
				setError(LibVulpes.proxy.getLocalizedString("error.rocket.notSameSystem"));
				return;
			}
		}

		//TODO: Clean this logic a bit?
		if(!stats.hasSeat() || ((DimensionManager.getInstance().isDimensionCreated(destinationDimId)) || destinationDimId == Configuration.stationDimId || destinationDimId == 0) ) { //Abort if destination is invalid

			prepareStaging();

			setInFlight(true);
			Iterator<IInfrastructure> connectedTiles = connectedInfrastructure.iterator();

			MinecraftForge.EVENT_BUS.post(new RocketLaunchEvent(this));

			//Disconnect things linked to the rocket on liftoff
			while(connectedTiles.hasNext()) {

				IInfrastructure i = connectedTiles.next();
				if(i.disconnectOnLiftOff()) {
					disconnectInfrastructure(i);
					connectedTiles.remove();
				}
			}
		}
	}

	/**
	 * Works out the per-stage thrust, fuel use and separation points just before liftoff.
	 *
	 * Runs on both sides: getFuelAmount reads the datawatcher, which is already in sync, so client and
	 * server reach the same answer without an extra packet.
	 *
	 * The rocket keeps a single shared fuel pool - splitting it per stage would mean touching the fueling
	 * infrastructure, the GUI gauge and the datawatcher. Instead the pool is notionally handed out to the
	 * stages bottom-up, which gives each one the fuel level at which it runs dry and lets go.
	 */
	private void prepareStaging() {
		LeveledRocketParts.clear();
		currentStageLevel = 0;

		if(stagesPending) {
			AdvancedRocketry.rocketStructureDivider.cancelTask(entityUniqueID);
			stagesPending = false;
		}

		//Both sides compute this from the same blocks, so the flight model agrees without an extra packet.
		//The worker usually has it ready on the server; the client has never been told, and recomputing is
		//cheaper than shipping the layout separately.
		if(stageLayout == null || stageLayout.isEmpty()
				|| !stageLayout.matches(storage.getSizeX(), storage.getSizeY(), storage.getSizeZ()))
			stageLayout = RocketStructureThread.computeLayout(storage);

		//No dividers means a conventional rocket, leave the stats exactly as the assembler measured them
		if(stageLayout.isEmpty() || stageLayout.maxLevel < 1) return;

		int stageCount = stageLayout.maxLevel + 1;
		int[] thrust = new int[stageCount], fuelRate = new int[stageCount],
				fuelCapacity = new int[stageCount], blockCount = new int[stageCount];
		List<List<Vector3F<Float>>> engines = new ArrayList<>();
		for(int i = 0; i < stageCount; i++) engines.add(new ArrayList<>());

		float halfX = storage.getSizeX()/2f, halfZ = storage.getSizeZ()/2f;

		for(int x = 0; x < storage.getSizeX(); x++) {
			for(int y = 0; y < storage.getSizeY(); y++) {
				for(int z = 0; z < storage.getSizeZ(); z++) {
					int level = stageLayout.getLevel(x, y, z);
					if(level < 0 || level >= stageCount) continue;

					Block block = storage.getBlock(x, y, z);
					blockCount[level]++;

					if(block instanceof IRocketEngine) {
						thrust[level] += ((IRocketEngine)block).getThrust(storage.world, x, y, z);
						fuelRate[level] += ((IRocketEngine)block).getFuelConsumptionRate(storage.world, x, y, z);
						//Same framing as TileRocketBuilder.scanRocket: centred horizontally, measured up from the base
						engines.get(level).add(new Vector3F<>(x - halfX, (float)y, z - halfZ));
					}

					if(block instanceof IFuelTank)
						fuelCapacity[level] += ((IFuelTank)block).getMaxFill(storage.world, x, y, z, storage.getBlockMetadata(x, y, z));
				}
			}
		}

		//Hand the fuel out from the bottom stage up, a stage separates once everything below it is spent.
		//Capacity is the raw sum: that is what StatsRocket.addFuelAmount caps refuelling at.
		int remaining = getFuelAmount();
		int[] threshold = new int[stageCount];
		for(int level = stageCount - 1; level >= 0; level--) {
			remaining -= Math.min(remaining, fuelCapacity[level]);
			threshold[level] = remaining;
		}

		for(int level = 0; level < stageCount; level++)
			LeveledRocketParts.add(new LeveledRocketPart(level, thrust[level], fuelRate[level], fuelCapacity[level], blockCount[level], threshold[level]));

		currentStageLevel = stageLayout.maxLevel;
		applyStageStats(currentStageLevel, engines.get(currentStageLevel));
	}

	/**
	 * Points the flight model at one stage's engines. Weight stays the whole rocket, so shedding a stage
	 * raises acceleration by removing mass rather than by adding thrust.
	 */
	private void applyStageStats(int level, List<Vector3F<Float>> engineLocations) {
		LeveledRocketPart part = getStage(level);
		if(part == null) return;

		stats.setThrust(part.thrust);
		stats.setFuelRate(FuelType.LIQUID, part.fuelRate);
		stats.clearEngineLocations();
		if(engineLocations != null)
			for(Vector3F<Float> vec : engineLocations)
				stats.addEngineLocation(vec.x, vec.y, vec.z);
	}

	private LeveledRocketPart getStage(int level) {
		for(LeveledRocketPart part : LeveledRocketParts)
			if(part.level == level) return part;

		return null;
	}

	/**
	 * Drops every stage that has run out of fuel. Each becomes its own rocket entity with no engines and
	 * no guidance, so it simply falls, lands and can be taken apart for its blocks.
	 */
	private void separateSpentStages() {
		if(currentStageLevel < 1 || stageLayout == null || stageLayout.isEmpty() || storage == null) return;

		LeveledRocketPart spent = getStage(currentStageLevel);
		if(spent == null || getFuelAmount() > spent.separationThreshold) return;

		ArrayList<BlockPosition> positions = new ArrayList<>();
		int minX = Integer.MAX_VALUE, minY = Integer.MAX_VALUE, minZ = Integer.MAX_VALUE;

		//Every group at this level goes at once, which is what makes side-mounted boosters work
		for(int x = 0; x < storage.getSizeX(); x++) {
			for(int y = 0; y < storage.getSizeY(); y++) {
				for(int z = 0; z < storage.getSizeZ(); z++) {
					if(stageLayout.getLevel(x, y, z) != currentStageLevel) continue;

					positions.add(new BlockPosition(x, y, z));
					minX = Math.min(minX, x);
					minY = Math.min(minY, y);
					minZ = Math.min(minZ, z);
				}
			}
		}

		int nextLevel = currentStageLevel - 1;

		//An empty group can happen for a divider with nothing below it, just move on to the next level
		if(!positions.isEmpty()) {
			StorageChunk debrisStorage = StorageChunk.divideStorage(storage, positions);

			StatsRocket debrisStats = new StatsRocket();
			debrisStats.setWeight(spent.blockCount);

			//divideStorage rebases the split chunk onto its own bounding box, and the entity origin sits at
			//the horizontal centre of the base, so shift by half of each size
			double debrisX = posX - storage.getSizeX()/2f + minX + debrisStorage.getSizeX()/2f;
			double debrisY = posY + minY;
			double debrisZ = posZ - storage.getSizeZ()/2f + minZ + debrisStorage.getSizeZ()/2f;

			storage.removeBlocks(positions);

			EntityRocket debris = new EntityRocket(worldObj, debrisStorage, debrisStats, debrisX, debrisY, debrisZ);
			debris.setInFlight(true);
			worldObj.spawnEntityInWorld(debris);

			//Same handshake TileRocketBuilder.assembleRocket uses to get the blocks onto the clients
			NBTTagCompound debrisNbt = new NBTTagCompound();
			debris.writeToNBT(debrisNbt);
			PacketHandler.sendToNearby(new PacketEntity(debris, (byte)0, debrisNbt), worldObj.provider.dimensionId, (int)posX, (int)posY, (int)posZ, 64);
		}

		currentStageLevel = nextLevel;
		applyStageStats(currentStageLevel, collectEngineLocations(currentStageLevel));
		stats.setWeight(countRemainingBlocks());

		//The clients need the shortened rocket and the new engine positions
		NBTTagCompound nbt = new NBTTagCompound();
		writeNetworkableNBT(nbt);
		PacketHandler.sendToPlayersTrackingEntity(new PacketEntity(this, (byte)PacketType.RECEIVE_NBT.ordinal(), nbt), this);
	}

	private List<Vector3F<Float>> collectEngineLocations(int level) {
		List<Vector3F<Float>> locations = new ArrayList<>();
		if(stageLayout == null || stageLayout.isEmpty()) return locations;

		float halfX = storage.getSizeX()/2f, halfZ = storage.getSizeZ()/2f;

		for(int x = 0; x < storage.getSizeX(); x++) {
			for(int y = 0; y < storage.getSizeY(); y++) {
				for(int z = 0; z < storage.getSizeZ(); z++) {
					if(stageLayout.getLevel(x, y, z) != level) continue;

					if(storage.getBlock(x, y, z) instanceof IRocketEngine)
						locations.add(new Vector3F<>(x - halfX, (float)y, z - halfZ));
				}
			}
		}
		return locations;
	}

	private int countRemainingBlocks() {
		int count = 0;
		for(int x = 0; x < storage.getSizeX(); x++)
			for(int y = 0; y < storage.getSizeY(); y++)
				for(int z = 0; z < storage.getSizeZ(); z++)
					if(!storage.isAirBlock(x, y, z)) count++;

		return count;
	}

	/**
	 * Called when the rocket is to be deconstructed
	 */
	@Override
	public void deconstructRocket() {
		super.deconstructRocket();

		for(@NotNull IInfrastructure infrastructure : connectedInfrastructure) {
			infrastructure.unlinkRocket();
		}


		//paste the rocket into the world as blocks
		storage.pasteInWorld(this.worldObj, (int)(this.posX - storage.getSizeX()/2f), (int)this.posY, (int)(this.posZ - storage.getSizeZ()/2f));
		this.setDead();
	}

	@Override
	public void setDead() {
		super.setDead();

		//unlink any connected tiles
		Iterator<IInfrastructure> connectedTiles = connectedInfrastructure.iterator();
		while(connectedTiles.hasNext()) {
			connectedTiles.next().unlinkRocket();
			connectedTiles.remove();
		}

		if(!worldObj.isRemote)
			AdvancedRocketry.rocketStructureDivider.cancelTask(entityUniqueID);

		if(worldObj.isRemote && storage != null && storage.world.glListID != -1) {
			GL11.glDeleteLists(storage.world.glListID, 1);
		}
	}


	//Relink connected tiles on load
	@Override
	public void onChunkLoad() {
		super.onChunkLoad();

		//problems with loading on other world then where the infrastructure was set?
        for (BlockPosition temp : new LinkedList<>(infrastructureCoords)) {
            TileEntity tile = this.worldObj.getTileEntity(temp.x, temp.y, temp.z);
            if (tile instanceof IInfrastructure) {
                this.linkInfrastructure((IInfrastructure) tile);
            }
        }
	}


	public void setOverriddenCoords(int dimId, float x, float y, float z) {
		TileGuidanceComputer tile = storage.getGuidanceComputer();
		if(tile != null) {
			tile.setFallbackDestination(dimId, new Vector3F<>(x, y, z));
		}
	}

	@Override
	public void travelToDimension(int newDimId) {
		travelToDimension(newDimId, this.posX, Configuration.orbit, this.posZ);
	}

	public void travelToDimension(int newDimId, double posX, double y, double posZ)
	{
		if (!this.worldObj.isRemote && !this.isDead)
		{

			if(!DimensionManager.getInstance().canTravelTo(newDimId)) {
                AdvancedRocketry.logger.warn("Rocket trying to travel from Dim{} to Dim {}.  target not accessible by rocket from launch dim", this.worldObj.provider.dimensionId, newDimId);
				return;
			}

			//Only record where we came from when leaving a real body, so a rescue from space aims at the last
			//place the rocket could actually land rather than at space itself
			if(this.worldObj.provider.dimensionId != Configuration.spaceDimId)
				lastDimensionFrom = this.worldObj.provider.dimensionId;

			TeleportHelper.teleportEntityWithRiding(this, newDimId, posX,y,posZ);

		}
	}

	protected void readNetworkableNBT(@NotNull NBTTagCompound nbt) {
		//Normal function checks for the existance of the data anyway
		readEntityFromNBT(nbt);
	}

	@Override
	protected void readEntityFromNBT(@NotNull NBTTagCompound nbt) {

		setInOrbit(isInOrbit = nbt.getBoolean("orbit"));
		stats.readFromNBT(nbt);

		this.comeFromDimID = nbt.getInteger("comeFromDimID");

		mountedEntities = new WeakReference[stats.getNumPassengerSeats()];

		setFuelAmount(stats.getFuelAmount(FuelType.LIQUID));

		setInFlight(isInFlight = nbt.getBoolean("flight"));

		readMissionPersistantNBT(nbt);
		if(nbt.hasKey("data"))
		{
			if(storage == null) 
				storage = new StorageChunk();

			storage.readFromNBT(nbt.getCompoundTag("data"));
			storage.setEntity(this);
			this.setSize(Math.max(storage.getSizeX(), storage.getSizeZ()), storage.getSizeY());
		}
		readStagingNBT(nbt);
		if(nbt.hasKey("infrastructure")) {
			NBTTagList tagList = nbt.getTagList("infrastructure", 10);
			for (int i = 0; i < tagList.tagCount(); i++) {
				int[] coords = tagList.getCompoundTagAt(i).getIntArray("loc");
				infrastructureCoords.add(new BlockPosition(coords[0], coords[1], coords[2]));
			}
		}
		destinationDimId = nbt.getInteger("destinationDimId");
		lastDimensionFrom = nbt.getInteger("lastDimensionFrom");

		//Satallite
		if(nbt.hasKey("satallite")) {
			NBTTagCompound satalliteNbt = nbt.getCompoundTag("satallite");
			satallite = SatelliteRegistry.createFromNBT(satalliteNbt);
		}
	}

	protected void writeNetworkableNBT(NBTTagCompound nbt) {
		writeMissionPersistantNBT(nbt);
		nbt.setBoolean("orbit", isInOrbit());
		nbt.setBoolean("flight", isInFlight());
		stats.writeToNBT(nbt);

		nbt.setInteger("comeFromDimID", comeFromDimID);

		if(!infrastructureCoords.isEmpty()) {
			NBTTagList itemList = new NBTTagList();
			for(BlockPosition inf : infrastructureCoords)
			{

				NBTTagCompound tag = new NBTTagCompound();
				tag.setIntArray("loc", new int[] {inf.x, inf.y, inf.z});
				itemList.appendTag(tag);

			}
			nbt.setTag("infrastructure", itemList);
		}

		nbt.setInteger("destinationDimId", destinationDimId);

		writeStagingNBT(nbt);

		//Satallite
		if(satallite != null) {
			NBTTagCompound satalliteNbt = new NBTTagCompound();
			satallite.writeToNBT(satalliteNbt);
			satalliteNbt.setString("DataType",SatelliteRegistry.getKey(satallite.getClass()));

			nbt.setTag("satallite", satalliteNbt);
		}
	}

	/**
	 * Staging data goes through writeNetworkableNBT rather than writeEntityToNBT because that is what
	 * RECEIVE_NBT carries - the client needs the stage list to render the right engine flames.
	 */
	private void writeStagingNBT(@NotNull NBTTagCompound nbt) {
		//Nothing worth saving until the layout arrives, readEntityFromNBT re-queues the work on load
		if(stageLayout == null || stageLayout.isEmpty()) return;

		stageLayout.writeToNBT(nbt);
		nbt.setInteger("currentStageLevel", currentStageLevel);

		NBTTagList stages = new NBTTagList();
		for(LeveledRocketPart part : LeveledRocketParts)
			stages.appendTag(part.writeToNBT());

		nbt.setTag("stages", stages);
	}

	private void readStagingNBT(@NotNull NBTTagCompound nbt) {
		LeveledRocketParts.clear();
		stageLayout = StageLayout.readFromNBT(nbt);
		currentStageLevel = nbt.getInteger("currentStageLevel");

		NBTTagList stages = nbt.getTagList("stages", NBT.TAG_COMPOUND);
		for(int i = 0; i < stages.tagCount(); i++)
			LeveledRocketParts.add(LeveledRocketPart.readFromNBT(stages.getCompoundTagAt(i)));

		//A layout that does not describe the storage we just loaded is worse than none at all
		if(storage != null && !stageLayout.isEmpty() && !stageLayout.matches(storage.getSizeX(), storage.getSizeY(), storage.getSizeZ())) {
			stageLayout = StageLayout.EMPTY;
			LeveledRocketParts.clear();
			currentStageLevel = 0;
		}

		//Recompute in the background if we were saved before the layout was ready
		if(!worldObj.isRemote && stageLayout.isEmpty() && storage != null && storage.getGuidanceComputer() != null) {
			stagesPending = true;
			AdvancedRocketry.rocketStructureDivider.addATask(entityUniqueID, storage);
		}
	}

	public void writeMissionPersistantNBT(NBTTagCompound nbt) {

	}

	public void readMissionPersistantNBT(NBTTagCompound nbt) {

	}

	@Override
	protected void writeEntityToNBT(@NotNull NBTTagCompound nbt) {

		writeNetworkableNBT(nbt);
		if(storage != null) {
			NBTTagCompound blocks = new NBTTagCompound();
			storage.writeToNBT(blocks);
			nbt.setTag("data", blocks);
		}
		nbt.setInteger("lastDimensionFrom", lastDimensionFrom);

		//TODO handle non tile Infrastructure
	}

	@Override
	public void readDataFromNetwork(ByteBuf in, byte packetId,
			NBTTagCompound nbt) {
		if(packetId == PacketType.RECEIVE_NBT.ordinal()) {
			int oldGlListID = -1;
			if(worldObj.isRemote && storage != null)
				oldGlListID = storage.world.glListID;

			storage = new StorageChunk();
			storage.setEntity(this);
			storage.readFromNetwork(in);
			comeFromDimID = in.readInt();

			//Hand the display list to the replacement chunk instead of leaking it - this runs on the netty
			//thread, so the list can only be flagged for recompile here, not deleted
			if(oldGlListID != -1) {
				storage.world.glListID = oldGlListID;
				storage.world.glListDirty = true;
			}
		}
		else if(packetId == PacketType.SEND_PLANET_DATA.ordinal()) {
			nbt.setInteger("selection", in.readInt());
		}
	}

	@Override
	public void writeDataToNetwork(ByteBuf out, byte id) {

		if(id == PacketType.RECEIVE_NBT.ordinal()) {
			storage.writeToNetwork(out);
			out.writeInt(comeFromDimID);
		}
		else if(id == PacketType.SEND_PLANET_DATA.ordinal()) {
			if(worldObj.isRemote)
				out.writeInt(container.getSelectedSystem());
			else {
				TileGuidanceComputer guidanceComputer = storage.getFirstTileEntity(TileGuidanceComputer.class);
				if(guidanceComputer != null) {
					ItemStack stack = guidanceComputer.getStackInSlot(0);
					if(stack != null && stack.getItem() == AdvancedRocketryItems.itemPlanetIdChip) {
						out.writeInt(((ItemPlanetIdentificationChip)AdvancedRocketryItems.itemPlanetIdChip).getDimensionId(stack));
					}
				}
			}
		}
	}

	@Override
	public void useNetworkData2(EntityPlayer player, Side side, byte id,
			NBTTagCompound nbt) {


		if(id == PacketType.RECEIVE_NBT.ordinal()) {
			this.readEntityFromNBT(nbt);
			initFromBounds();
		}
		else if(id == PacketType.DECONSTRUCT.ordinal()) {
			deconstructRocket();
		}
		else if(id == PacketType.SEND_INTERACT.ordinal()) {
			interact(player);
		}
		else if(id == PacketType.OPEN_GUI.ordinal()) { //Used in key handler
			if(player.ridingEntity == this) //Prevent cheating
				openGui(player);
		}
		else if(id == PacketType.REQUEST_NBT.ordinal()) {
			if(storage != null) {
				NBTTagCompound nbtdata = new NBTTagCompound();

				this.writeNetworkableNBT(nbtdata);
				PacketHandler.sendToPlayer(new PacketEntity(this, (byte)PacketType.RECEIVE_NBT.ordinal(), nbtdata), player);

			}
		}
		else if(id == PacketType.FORCE_MOUNT.ordinal()) { //Used for pesky dimension transfers
			//When dimensions are transferred make sure to remount the player on the client
			player.mountEntity(this);
			MinecraftForge.EVENT_BUS.post(new RocketEvent.RocketDeOrbitingEvent(this));
		}
		else if(id == PacketType.LAUNCH.ordinal()) {
			if(player.equals(this.riddenByEntity))
				this.prepareLaunch();
		}
		else if(id == PacketType.CHANGE_WORLD.ordinal()) {
			AdvancedRocketry.proxy.changeClientPlayerWorld(storage.world);
		}
		else if(id == PacketType.REVERT_WORLD.ordinal()) {
			AdvancedRocketry.proxy.changeClientPlayerWorld(this.worldObj);
		}
		else if(id == PacketType.OPEN_PLANET_SELECTION.ordinal()) {
			player.openGui(LibVulpes.instance, GuiHandler.guiId.MODULARFULLSCREEN.ordinal(), player.worldObj, this.getEntityId(), -1,0);
		}
		else if(id == PacketType.SEND_PLANET_DATA.ordinal()) {
			TileGuidanceComputer guidanceComputer = storage.getGuidanceComputer();
			ItemStack stack = guidanceComputer == null ? null : guidanceComputer.getStackInSlot(0);
			if(stack != null && stack.getItem() == AdvancedRocketryItems.itemPlanetIdChip) {
				((ItemPlanetIdentificationChip)AdvancedRocketryItems.itemPlanetIdChip).setDimensionId(stack, nbt.getInteger("selection"));

				//Send data back to sync destination dims
				if(!worldObj.isRemote) {
					PacketHandler.sendToPlayersTrackingEntity(new PacketEntity(this, (byte)PacketType.SEND_PLANET_DATA.ordinal()), this);
				}
			}
		}
		else if(id == PacketType.DISCONNECT_INFRASTRUCTURE.ordinal()) {
			int[] pos = nbt.getIntArray("pos");

			//connectedInfrastructure.remove(new BlockPosition(pos[0], pos[1], pos[2]));

			TileEntity tile = worldObj.getTileEntity(pos[0], pos[1], pos[2]);
			if(tile instanceof IInfrastructure) {
				((IInfrastructure)tile).unlinkRocket();
				connectedInfrastructure.remove(tile);
			}
		}
		else if(id == PacketType.ROCKET_LAND_EVENT.ordinal() && worldObj.isRemote) {
			MinecraftForge.EVENT_BUS.post(new RocketEvent.RocketLandedEvent(this));
		}
		else if(id == PacketType.SHOW_ERROR.ordinal() && worldObj.isRemote) {
			errorStr = nbt.getString("error");
			lastErrorTime = worldObj.getTotalWorldTime();
		}
		else if(id >= STATION_LOC_OFFSET + BUTTON_ID_OFFSET) {
			int id2 = id - (STATION_LOC_OFFSET + BUTTON_ID_OFFSET) - 1;
			setDestLandingPad(id2);

			//propagate change back to the clients
			if(!worldObj.isRemote)
				PacketHandler.sendToPlayersTrackingEntity(new PacketEntity(this, id), this);
		}
		else if(id > BUTTON_ID_OFFSET) {
			TileEntity tile = storage.getGUItiles().get(id - BUTTON_ID_OFFSET - tilebuttonOffset);

			//Welcome to super hack time with packets
			//Due to the fact the client uses the player's current world to open the gui, we have to move the client between worlds for a bit
			PacketHandler.sendToPlayer(new PacketEntity(this, (byte)PacketType.CHANGE_WORLD.ordinal()), player);
			storage.getBlock(tile.xCoord, tile.yCoord, tile.zCoord).onBlockActivated(storage.world, tile.xCoord, tile.yCoord,  tile.zCoord, player, 0, 0, 0, 0);
			PacketHandler.sendToPlayer(new PacketEntity(this, (byte)PacketType.REVERT_WORLD.ordinal()), player);
		}
	}

	private void setDestLandingPad(int padIndex) {
		TileGuidanceComputer guidanceComputer = storage.getGuidanceComputer();
		if(guidanceComputer == null) return;

		ItemStack slot0 = guidanceComputer.getStackInSlot(0);
		int uuid;
		//Station location select
		if( slot0 != null && slot0.getItem() instanceof ItemStationChip && (uuid = (int)ItemStationChip.getUUID(slot0)) != 0) {
			ISpaceObject obj = SpaceObjectManager.getSpaceManager().getSpaceStation(uuid);

			if(obj instanceof SpaceObject) {

				if(padIndex == -1) {
					guidanceComputer.setLandingLocation(uuid, null);
				}
				else {

					StationLandingLocation location = ((SpaceObject) obj).getLandingPads().get(padIndex);
					if(location != null && !location.getOccupied())
						guidanceComputer.setLandingLocation(uuid, location);
				}
			}

			StationLandingLocation location = storage.getGuidanceComputer().getLandingLocation(uuid);
			landingPadDisplayText.setText(location != null ? location.toString() : "None Selected");
		}
	}

	@Override
	public void updateRiderPosition()
	{
		if (this.riddenByEntity != null )
		{
			//Bind player to the seat
			if(this.storage != null) {
				//Conditional b/c for some reason client/server positions do not match
				float xOffset = this.storage.getSizeX() % 2 == 0 ? 0.5f : 0f;
				float zOffset = this.storage.getSizeZ() % 2 == 0 ? 0.5f : 0f;
				this.riddenByEntity.setPosition(this.posX  + stats.getSeatX(), this.posY + stats.getSeatY() + (worldObj.isRemote && this.riddenByEntity.equals(Minecraft.getMinecraft().thePlayer) ? 1.5 : -0.25), this.posZ + stats.getSeatZ() );
			}
			else
				this.riddenByEntity.setPosition(this.posX , this.posY , this.posZ );
		}

		for(int i = 0; i < this.stats.getNumPassengerSeats(); i++) {
			BlockPosition pos = this.stats.getPassengerSeat(i);
			if(mountedEntities[i] != null && mountedEntities[i].get() != null) {
				Entity entities = mountedEntities[i].get();
				System.out.println("Additional: " + entities);
				if(entities != null)entities.setPosition(this.posX + pos.x, this.posY + pos.y, this.posZ + pos.z);
			}
		}
	}

	@Override
	public List<ModuleBase> getModules(int ID, EntityPlayer player) {
		List<ModuleBase> modules;
		//If the rocket is flight don't load the interface
		modules = new LinkedList<>();

		if(ID == GuiHandler.guiId.MODULAR.ordinal()) {
			//Backgrounds
			if(worldObj.isRemote) {
				modules.add(new ModuleImage(173, 0, new IconResource(128, 0, 48, 86, CommonResources.genericBackground)));
				modules.add(new ModuleImage(173, 86, new IconResource(98, 0, 78, 83, CommonResources.genericBackground)));
				modules.add(new ModuleImage(173, 168, new IconResource(98, 168, 78, 3, CommonResources.genericBackground)));
			}

			//Fuel
			modules.add(new ModuleProgress(192, 7, 0, new ProgressBarImage(2, 173, 12, 71, 17, 6, 3, 69, 1, 1, ForgeDirection.UP, TextureResources.rocketHud), this));

			//TODO DEBUG tiles!
			List<TileEntity> tiles = storage.getGUItiles();
			for(int i = 0; i < tiles.size(); i++) {
				TileEntity tile  = tiles.get(i);
				try {
					modules.add(new ModuleSlotButton(8 + 18* (i % 9), 17 + 18*(i/9), i + tilebuttonOffset, this, new ItemStack(storage.getBlock(tile.xCoord, tile.yCoord, tile.zCoord), 1, storage.getBlockMetadata(tile.xCoord, tile.yCoord, tile.zCoord))));
				} catch(NullPointerException e) {
					//Fail silently, seems to happen with odd blocks once in a while, see #207
				}
			}

			//Add buttons
			modules.add(new ModuleButton(180, 140, 0, LibVulpes.proxy.getLocalizedString("msg.entity.rocket.disass"), this, zmaster587.libVulpes.inventory.TextureResources.buttonBuild, 64, 20));

			//modules.add(new ModuleButton(180, 95, 1, "", this, TextureResources.buttonLeft, 10, 16));
			//modules.add(new ModuleButton(202, 95, 2, "", this, TextureResources.buttonRight, 10, 16));

			modules.add(new ModuleButton(180, 114, 1, LibVulpes.proxy.getLocalizedString("msg.entity.rocket.seldst"), this,  zmaster587.libVulpes.inventory.TextureResources.buttonBuild, 64,20));
			//modules.add(new ModuleText(180, 114, "Inventories", 0x404040));

			//Warp is only offered where it can actually be used - adrift in space, with a core aboard
			if(worldObj.provider.dimensionId == spaceDimId && storage.hasWarpCore())
				modules.add(new ModuleButton(180, 88, BUTTON_ID_WARP, LibVulpes.proxy.getLocalizedString("msg.entity.rocket.warp"), this, zmaster587.libVulpes.inventory.TextureResources.buttonBuild, 64, 20));
		}
		else {
			TileGuidanceComputer guidanceComputer = storage.getGuidanceComputer();
			//Debris has no computer and so no destination to select
			if(guidanceComputer == null) return modules;

			ItemStack slot0 = guidanceComputer.getStackInSlot(0);
			int uuid;
			//Station location select
			if( slot0 != null && slot0.getItem() instanceof ItemStationChip && (uuid = (int)ItemStationChip.getUUID(slot0)) != 0) {
				ISpaceObject obj = SpaceObjectManager.getSpaceManager().getSpaceStation(uuid);

				modules.add(new ModuleStellarBackground(0, 0, zmaster587.libVulpes.inventory.TextureResources.starryBG));
				//modules.add(new ModuleImage(0, 0, icon));

				if(obj == null)
					return modules;

				List<ModuleBase> list2 = new LinkedList<>();
				ModuleButton button = new ModuleButton(0, 0, STATION_LOC_OFFSET, LibVulpes.proxy.getLocalizedString("msg.entity.rocket.clear"), this, TextureResources.buttonGeneric, 72, 18);
				list2.add(button);

				int i = 1;
				for( StationLandingLocation pos : ((SpaceObject)obj).getLandingPads()) 
				{
					button = new ModuleButton(0, i*18, i + STATION_LOC_OFFSET, pos.toString(), this, TextureResources.buttonGeneric, 72, 18);
					list2.add(button);

					if(pos.getOccupied())
						button.setColor(0xFF0000);
					
					i++;
				}

				ModuleContainerPan pan = new ModuleContainerPan(25, 25, list2, new LinkedList<>(), null, 256, 256, 0, -48, 258, 256);
				modules.add(pan);

				StationLandingLocation location = guidanceComputer.getLandingLocation(uuid);

				landingPadDisplayText.setText(location != null ? location.toString() : LibVulpes.proxy.getLocalizedString("msg.entity.rocket.none"));
				modules.add(landingPadDisplayText);
			}
			else {
				DimensionProperties properties = getSelectorOrigin();
				while(properties.getParentProperties() != null) properties = properties.getParentProperties();

				if(storage.hasWarpCore())
					container = new ModulePlanetSelector(properties.getStarId(), zmaster587.libVulpes.inventory.TextureResources.starryBG, this, this, true);
				else
					container = new ModulePlanetSelector(properties.getId(), zmaster587.libVulpes.inventory.TextureResources.starryBG, this, false);
				container.setOffset(1000, 1000);
				modules.add(container);
			}
		}
		return modules;
	}

	/**
	 * Which system the destination selector should open on.
	 *
	 * Out in space the rocket is not on any body, so the effective dimension is the featureless space one - the
	 * selector would come up empty.  The nearest body is what the pilot is actually looking at, so use that.
	 */
	private DimensionProperties getSelectorOrigin() {
		if(worldObj.provider.dimensionId == spaceDimId) {
			SimUniverse.SimBody nearest = SimUniverse.getInstance().findNearest(posX, posY, posZ);
			if(nearest != null) {
				int dimId = nearest.getConfig().getPropertiesId();
				if(dimId != SimUniverse.NO_DIMENSION)
					return DimensionManager.getInstance().getDimensionProperties(dimId);

				//A star: open on any of its planets, which is what the star-level selector keys off
				StellarBody star = DimensionManager.getInstance().getStar(nearest.getConfig().getStarId());
				if(star != null && !star.getPlanets().isEmpty())
					return DimensionManager.getInstance().getDimensionProperties(star.getPlanets().get(0).getId());
			}
		}
		return DimensionManager.getEffectiveDimId(worldObj, (int)this.posX, (int)this.posZ);
	}

	@Override
	public String getModularInventoryName() {
		return "Rocket";
	}

	@Override
	public float getNormallizedProgress(int id) {
		if(id == 0)
			return getFuelAmount()/(float)getFuelCapacity();
		return 0;
	}

	@Override
	public void setProgress(int id, int progress) {

	}

	@Override
	public int getProgress(int id) {
		return 0;
	}

	@Override
	public int getTotalProgress(int id) {
		return 0;
	}

	@Override
	public void setTotalProgress(int id, int progress) {}

	@Override
	@SideOnly(Side.CLIENT)
	public void onInventoryButtonPressed(int buttonId) {
		switch(buttonId) {
		case 0:
			PacketHandler.sendToServer(new PacketEntity(this, (byte)EntityRocket.PacketType.DECONSTRUCT.ordinal()));
			break;
		case 1:
			PacketHandler.sendToServer(new PacketEntity(this, (byte)EntityRocket.PacketType.OPEN_PLANET_SELECTION.ordinal()));
			break;
		case BUTTON_ID_WARP:
			requestWarp();
			break;
		default:
			PacketHandler.sendToServer(new PacketEntity(this, (byte)(buttonId + BUTTON_ID_OFFSET)));
			//Minecraft.getMinecraft().thePlayer.closeScreen();

			if(buttonId < STATION_LOC_OFFSET) {
				TileEntity tile = storage.getGUItiles().get(buttonId - tilebuttonOffset);
				storage.getBlock(tile.xCoord, tile.yCoord, tile.zCoord).onBlockActivated(storage.world, tile.xCoord, tile.yCoord,  tile.zCoord, Minecraft.getMinecraft().thePlayer, 0, 0, 0, 0);
			}
		}
	}

	/**
	 * Asks the server to warp to whatever the guidance computer is pointing at.  The server revalidates the
	 * target and charges the fuel, so all this has to get right is which body the player picked.
	 */
	private void requestWarp() {
		TileGuidanceComputer computer = storage == null ? null : storage.getGuidanceComputer();
		int target = computer == null ? -1 : computer.getDestinationDimId(worldObj.provider.dimensionId, (int)posX, (int)posZ);

		if(target == -1) {
			setError(LibVulpes.proxy.getLocalizedString("error.rocket.destinationNotExist"));
			return;
		}

		NBTTagCompound nbt = new NBTTagCompound();
		nbt.setInteger(ISpaceTraveler.NBT_TARGET, target);
		nbt.setInteger(ISpaceTraveler.NBT_DISTANCE, 0);
		PacketHandler.sendToServer(new PacketEntity(this, ISpaceTraveler.PACKET_ID, nbt));
	}

	@Override
	public boolean canInteractWithContainer(EntityPlayer entity) {
		boolean ret = !this.isDead && this.getDistanceToEntity(entity) < 64;
		if(!ret)
			PlanetEventHandler.removePlayerFromInventoryBypass(entity);

		return ret;
	}

	@Override
	public StatsRocket getRocketStats() {
		return stats;
	}

	@Override
	public void handleDismount(Entity entity) {

		if(worldObj.provider.dimensionId == spaceDimId)return;
		//Attempt to dismount passengers first, else dismount pilot
		for(int i = 0; i < mountedEntities.length; i++) {

			if(mountedEntities[i] != null && Objects.equals(mountedEntities[i].get(), entity)) {
				mountedEntities[i] = null;
				break;
			}
		}

		entity.ridingEntity = null;
		this.riddenByEntity = null;
	}

	@Override
	public void onSelected(Object sender) {

	}

	@Override
	public void onSelectionConfirmed(Object sender) {
		PacketHandler.sendToServer(new PacketEntity(this, (byte)PacketType.SEND_PLANET_DATA.ordinal()));
	}

	@Override
	public void onSystemFocusChanged(Object sender) {
		// TODO Auto-generated method stub

	}

	public LinkedList<IInfrastructure> getConnectedInfrastructure() {
		return connectedInfrastructure;
	}

	@Override
	public boolean isPlanetKnown(IDimensionProperties properties) {
		return !Configuration.planetsMustBeDiscovered || DimensionManager.getInstance().knownPlanets.contains(properties.getId());
	}

	@Override
	public boolean isStarKnown(StellarBody body) {
		return true;
	}
}
