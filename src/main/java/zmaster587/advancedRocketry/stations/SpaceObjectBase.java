package zmaster587.advancedRocketry.stations;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.util.ForgeDirection;
import zmaster587.advancedRocketry.AdvancedRocketry;
import zmaster587.advancedRocketry.api.Configuration;
import zmaster587.advancedRocketry.api.stations.ISpaceObject;
import zmaster587.advancedRocketry.api.stations.IStorageChunk;
import zmaster587.advancedRocketry.dimension.DimensionProperties;
import zmaster587.advancedRocketry.network.PacketStationUpdate;
import zmaster587.libVulpes.network.PacketHandler;
import zmaster587.libVulpes.util.BlockPosition;

public abstract class SpaceObjectBase implements ISpaceObject {
	private int posX, posY;
	private int altitude;
	private BlockPosition spawnLocation;
	private final double[] rotation;
	private final double[] angularVelocity;
	private long lastTimeModification = 0;
	private DimensionProperties properties;

	public SpaceObjectBase() {
		properties = (DimensionProperties) zmaster587.advancedRocketry.dimension.DimensionManager.defaultSpaceDimensionProperties.clone();
		angularVelocity = new double[3];
		rotation = new double[3];
	}

	public long getExpireTime() { 
		return Long.MAX_VALUE;
	}
	
	public void beginTransition(long time) {
	}

	public long getTransitionTime() {
		return 0;
	}

	/**
	 * @return id of the space object (NOT the DIMID)
	 */
	@Override
	public int getId() {
		return properties.getId();
	}

	/**
	 * @return dimension properties of the object
	 */
	@Override
	public DimensionProperties getProperties() {
		return properties;
	}

	@SideOnly(Side.CLIENT)
	public void setProperties(DimensionProperties properties) {
		this.properties = properties;
	}

	/**
	 * @return the DIMID of the planet the object is currently orbiting, -1 if none
	 */
	@Override
	public int getOrbitingPlanetId() {
		return properties.getParentPlanet();
	}

	/**
	 * Sets the forward Facing direction of the object.  Mostly used for warpships
	 * @param direction
	 */
	public void setForwardDirection(ForgeDirection direction) {
		
	}

	/**
	 * Gets the forward facing direction of the ship.  Direction is not garunteed to be set
	 * @return direction of the ship, or UNKNOWN if none exists
	 */
	public ForgeDirection getForwardDirection() {
			return ForgeDirection.UNKNOWN;
	}
	/**
	 * @return the altitude above the parent DIM the object currently is
	 */
	public int getAltitude() {
		return altitude;
	}
	
	/**
	 * @return rotation of the station in degrees
	 */
	public double getRotation(ForgeDirection dir) {
		return (rotation[getIDFromDir(dir)] + getDeltaRotation(dir)*(getWorldTime() - lastTimeModification)*10) % (3600);
	}
	
	protected int getIDFromDir(ForgeDirection facing){
		if(facing == ForgeDirection.EAST)
			return 0;
		else if(facing == ForgeDirection.UP)
			return 1;
		else
			return 2;
	}
	
	/**
	 * @param rotation rotation of the station in degrees
	 */
	public void setRotation(double rotation, ForgeDirection facing) {
		this.rotation[getIDFromDir(facing)] = rotation;
	}
	
	/**
	 * @return anglarVelocity of the station in degrees per tick
	 */
	public double getDeltaRotation(ForgeDirection facing) {
		return this.angularVelocity[getIDFromDir(facing)];
	}
	
	/**
	 * @param rotation anglarVelocity of the station in degrees per tick
	 */
	public void setDeltaRotation(double rotation, ForgeDirection facing) {
		this.rotation[getIDFromDir(facing)] = getRotation(facing);
		this.lastTimeModification = getWorldTime();
		
		this.angularVelocity[getIDFromDir(facing)] = rotation;
	}
	
	public double getMaxRotationalAcceleration() {
		return 0d;
	}

	private long getWorldTime() {
		return AdvancedRocketry.proxy.getWorldTimeUniversal(Configuration.spaceDimId);
	}
	
	/**
	 * @return the X postion on the graph the object is stored in {@link SpaceObjectManager}
	 */
	public int getPosX() {
		return posX;
	}

	/**
	 * @return the Y postion on the graph the object is stored in {@link SpaceObjectManager}
	 */
	public int getPosY() {
		return posY;
	}

	/**
	 * @return the spawn location of the object
	 */
	public BlockPosition getSpawnLocation() {
		return spawnLocation;
	}


	/**
	 * @param id the space object id of this object (NOT DIMID)
	 */
	@Override
	public void setId(int id) {
		properties.setId(id);
	}

	/**
	 * Sets the coords of the space object on the graph
	 * @param posX
	 * @param posY
	 */
	@Override
	public void setPos(int posX, int posY) {
		this.posX = posX;
		this.posY = posY;
	}

	/**
	 * Sets the spawn location for the space object
	 * @param x
	 * @param y
	 * @param z
	 */
	@Override
	public void setSpawnLocation(int x, int y, int z) {
		spawnLocation = new BlockPosition(x,y,z);
	}

	/**
	 * Sets the orbiting planet for the space object but does NOT register it with the planet
	 * @param id
	 */
	@Override
	public void setOrbitingBody(int id) {
		if(id == this.getOrbitingPlanetId())
			return;

		properties.setParentPlanet(zmaster587.advancedRocketry.dimension.DimensionManager.getInstance().getDimensionProperties(id), false);
	}

	@Override
	public void setDestOrbitingBody(int id) {
		if(FMLCommonHandler.instance().getSide().isServer()) {
			PacketHandler.sendToAll(new PacketStationUpdate(this, PacketStationUpdate.Type.DEST_ORBIT_UPDATE));
		}
	}

	@Override
	public int getDestOrbitingBody() {
		return 0;
	}

	/**
	 * When the space stations are first created they are 'unpacked' from the storage chunk they reside in
	 * @param chunk
	 */
	public void onModuleUnpack(IStorageChunk chunk) {
		World worldObj = DimensionManager.getWorld(Configuration.spaceDimId);
		chunk.pasteInWorld(worldObj, spawnLocation.x - chunk.getSizeX()/2, spawnLocation.y - chunk.getSizeY()/2, spawnLocation.z - chunk.getSizeZ()/2);

	}

	@Override
	public void writeToNbt(NBTTagCompound nbt) {
		properties.writeToNBT(nbt);
		nbt.setInteger("id", getId());
		nbt.setInteger("posX", posX);
		nbt.setInteger("posY", posY);
		nbt.setInteger("alitude", altitude);
		nbt.setInteger("spawnX", spawnLocation.x);
		nbt.setInteger("spawnY", spawnLocation.y);
		nbt.setInteger("spawnZ", spawnLocation.z);
		nbt.setDouble("rotationX", rotation[0]);
		nbt.setDouble("rotationY", rotation[1]);
		nbt.setDouble("rotationZ", rotation[2]);
		nbt.setDouble("deltaRotationX", angularVelocity[0]);
		nbt.setDouble("deltaRotationY", angularVelocity[1]);
		nbt.setDouble("deltaRotationZ", angularVelocity[2]);
	}

	@Override
	public void readFromNbt(NBTTagCompound nbt) {
		properties.readFromNBT(nbt);

		posX = nbt.getInteger("posX");
		posY = nbt.getInteger("posY");
		altitude = nbt.getInteger("altitude");
		spawnLocation = new BlockPosition(nbt.getInteger("spawnX"), nbt.getInteger("spawnY"), nbt.getInteger("spawnZ"));
		properties.setId(nbt.getInteger("id"));
		rotation[0] = nbt.getDouble("rotationX");
		rotation[1] = nbt.getDouble("rotationY");
		rotation[2] = nbt.getDouble("rotationZ");
		angularVelocity[0] = nbt.getDouble("deltaRotationX");
		angularVelocity[1] = nbt.getDouble("deltaRotationY");
		angularVelocity[2] = nbt.getDouble("deltaRotationZ");
	}

	/**
	 * True if the spawn location for this space object is not the default one assigned to it
	 * @return
	 */
	@Override
	public boolean hasCustomSpawnLocation() {
		return false;
	}

	@Override
	public boolean hasFreeLandingPad() {
		return false;
	}

	@Override
	public BlockPosition getNextLandingPad(boolean commit) {
		return null;
	}

	@Override
	public void addLandingPad(int x, int z, String name) {
		
	}

	@Override
	public void removeLandingPad(int x, int z) {
		
	}

	@Override
	public void setPadStatus(int posX, int posZ, boolean full) {
		
	}
	

}
