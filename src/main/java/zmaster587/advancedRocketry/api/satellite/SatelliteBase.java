package zmaster587.advancedRocketry.api.satellite;

import cpw.mods.fml.relauncher.Side;
import io.netty.buffer.ByteBuf;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import zmaster587.advancedRocketry.api.AdvancedRocketryItems;
import zmaster587.advancedRocketry.api.ISatelliteIdItem;
import zmaster587.advancedRocketry.api.SatelliteRegistry;
import zmaster587.advancedRocketry.item.ItemSatellite;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ICrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;

public abstract class SatelliteBase {
	
	protected SatelliteProperties satelliteProperties;
	private int dimId = -1;
	//Will always be of type ItemSatellite
	protected ItemStack satellite;

	private boolean isDead;
	
	public SatelliteBase() {
		satelliteProperties = new SatelliteProperties();
		satelliteProperties.setSatelliteType(SatelliteRegistry.getKey(this.getClass()));
		isDead = false;
	}
	
	public boolean acceptsItemInConstruction(@NotNull ItemStack item) {
		SatelliteProperties satelliteProperties = SatelliteRegistry.getSatelliteProperty(item);
		if(satelliteProperties == null)return false;
		int flag = satelliteProperties.getPropertyFlag();
		return SatelliteProperties.Property.MAIN.isOfType(flag);
	}
	
	/**
	 * Gets status info eg "Moving into position" or "Ready" or "68% damaged"
	 * @return Human-readable Information about the satellite (supports \n for newline)
	 */
	public abstract @Nullable String getInfo(World world);
	
	/**
	 * Gets the human-readable display name of the satellite
	 * @return display name of the satellite
	 */
	public abstract String getName();
	
	/**
	 * Actually does something with the satellite.  Normally called when the player rightclicks the master block
	 * @param Player interacting with the satellite
	 * @return whether the player has successfully interacted with the satellite
	 */
	public abstract boolean performAction(EntityPlayer player, World world,int x, int y, int z);
	
	/**
	 * Note: this is not currently used
	 * @return chance from 0 to 1 of failing this tick
	 */
	public abstract double failureChance();
	
	/**
	 * @return an item that can be used to control the satellite, normally a satellite ID chip but can be something else
	 */
	public ItemStack getContollerItemStack(ItemStack satIdChip, SatelliteProperties properties) {
		ISatelliteIdItem idChipItem = (ISatelliteIdItem)satIdChip.getItem();
		idChipItem.setSatellite(satIdChip, properties);
		return satIdChip;
	}
	
	/**
	 * @param stack stack to check (can be null)
	 * @return true if the item stack is a valid controller for the satellite
	 */
	public boolean isAcceptableControllerItemStack(ItemStack stack) {
		return stack != null && stack.getItem() == AdvancedRocketryItems.itemSatelliteIdChip;
	}
	
	/**
	 * @return true if the satellite can tick
	 */
	public boolean canTick() {
		return false;
	}
	
	/**
	 * called every tick if satellite can tick
	 */
	public void tickEntity() {}
	
	/**
	 * @return the long id of the satellite, used to get a satellite from the main list
	 */
	public long getId() {
		return satelliteProperties.getId();
	}
	
	public void setDead(){
		isDead = true;
	}
	
	public boolean isDead() {
		return isDead;
	}
	
	/**
	 * Does not currently support dimension change
	 * @param world World of which to assign to the satellite
	 */
	public void setDimensionId(@NotNull World world) {
        //TODO: handle dim change
        dimId = world.provider.dimensionId;
	}
	
	public void setDimensionId(int world) {
        //TODO: handle dim change
        dimId = world;
	}
	
	/**
	 * @param satelliteProperties satelliteProperties to assign to this satellite
	 */
	public void setProperties(ItemStack stack) {
		this.satelliteProperties = ((ItemSatellite)stack.getItem()).getSatellite(stack);
		this.satellite = stack;
	}
	
	public ItemStack getItemStackFromSatellite() {
		return satellite;
	}
	
	/**
	 * @return dimensionID of the satellite, -1 if none
	 */
	public int getDimensionId() {
		return dimId;
	}
	
	/**
	 * @param nbt NBT data to store
	 */
	public void writeToNBT(NBTTagCompound nbt) {
		nbt.setString("dataType", SatelliteRegistry.getKey(this.getClass()));
		
		NBTTagCompound properties = new NBTTagCompound();
		satelliteProperties.writeToNBT(properties);
		nbt.setTag("properties", properties);
		nbt.setInteger("dimId", dimId);
		
		NBTTagCompound itemNBT = new NBTTagCompound();
		//Transition
		if(satellite != null)
			satellite.writeToNBT(itemNBT);
		nbt.setTag("item", itemNBT);
		
	}
	
	public void readFromNBT(@NotNull NBTTagCompound nbt) {
		satelliteProperties.readFromNBT(nbt.getCompoundTag("properties"));
		dimId = nbt.getInteger("dimId");
		satellite = ItemStack.loadItemStackFromNBT(nbt.getCompoundTag("item"));
	}
	
	public void writeDataToNetwork(ByteBuf out, byte packetId) {
		
	}
	
	public void readDataToNetwork(byte packetId, ByteBuf in) {
		
	}
	
	public void useNetworkData(EntityPlayer player, Side client, byte packetId,
			NBTTagCompound nbt) {
		
	}
	
	//Server Syncing stuff
	//Used if the satellite needs to sync in a modularGUI
	
	public int numberChangesToSend() {
		return 0;
	}
	
	public void onChangeRecieved(int slot, int value) {

	}
	
	public boolean isUpdateRequired(int localId) {
		return false;
	}
	
	public void sendChanges(Container container, ICrafting crafter, int variableId, int localId) {

	}


}
