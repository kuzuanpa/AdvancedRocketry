package zmaster587.advancedRocketry.tile;

import io.netty.buffer.ByteBuf;

import java.util.LinkedList;
import java.util.List;

import cpw.mods.fml.relauncher.Side;
import org.jetbrains.annotations.NotNull;
import zmaster587.libVulpes.inventory.modules.IModularInventory;
import zmaster587.libVulpes.inventory.modules.IToggleButton;
import zmaster587.libVulpes.inventory.modules.ModuleBase;
import zmaster587.libVulpes.inventory.modules.ModuleToggleSwitch;
import zmaster587.libVulpes.network.PacketHandler;
import zmaster587.libVulpes.network.PacketMachine;
import zmaster587.libVulpes.util.INetworkMachine;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;

public class TileDrill extends TileEntity implements IModularInventory, IToggleButton, INetworkMachine {

	private float distanceExtended;
	private boolean extended;
	private ModuleToggleSwitch toggleSwitch;

	public TileDrill() {
		distanceExtended = 0;
	}

	public float getDistanceExtended() {
		return distanceExtended;
	}

	public void setDistanceExtended(float distance) {
		this.distanceExtended = distance;
	}

	@Override
	public boolean canUpdate() {
		return false;//super.canUpdate();
	}

	public boolean drillExtended() {
		return extended;
	}

	public void setDrillExtended(boolean value) {
		extended = value;
		distanceExtended = value ? 1f : 0f;
	}

	@Override
	public List<ModuleBase> getModules(int id, EntityPlayer player) {
		List<ModuleBase> modules = new LinkedList<>();

		modules.add(toggleSwitch = new ModuleToggleSwitch(160, 5, 0, "", this,  zmaster587.libVulpes.inventory.TextureResources.buttonToggleImage, 11, 26, drillExtended()));

		return modules;
	}

	@Override
	public String getModularInventoryName() {
		return "tile.drill.name";
	}

	@Override
	public boolean canInteractWithContainer(EntityPlayer entity) {
		return true;
	}

	@Override
	public void onInventoryButtonPressed(int buttonId) {

		if(buttonId == 0) {
			this.setDrillExtended(toggleSwitch.getState());
			PacketHandler.sendToServer(new PacketMachine(this,(byte)0));
		}
	}

	@Override
	public void stateUpdated(ModuleBase module) {
		if(module == toggleSwitch)
			setDrillExtended(toggleSwitch.getState());
	}

	@Override
	public void writeDataToNetwork(ByteBuf out, byte id) {
		out.writeBoolean(extended);

	}

	@Override
	public void readDataFromNetwork(ByteBuf in, byte packetId,
									@NotNull NBTTagCompound nbt) {
		nbt.setBoolean("enabled", in.readBoolean());
	}

	@Override
	public void useNetworkData(EntityPlayer player, Side side, byte id,
			NBTTagCompound nbt) {
		setDrillExtended(nbt.getBoolean("enabled"));
		toggleSwitch.setToggleState(drillExtended());

		//Last ditch effort to update the toggle switch when it's flipped
		if(!worldObj.isRemote)
			PacketHandler.sendToNearby(new PacketMachine(this, (byte)0), worldObj.provider.dimensionId, xCoord, yCoord, zCoord, 64);

	}
	
	@Override
	public void writeToNBT(@NotNull NBTTagCompound nbt) {
		super.writeToNBT(nbt);
		
		nbt.setFloat("extendAmt", distanceExtended);
		nbt.setBoolean("extended", extended);
	}
	
	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		super.readFromNBT(nbt);
		
		distanceExtended = nbt.getFloat("extendAmt");
		extended = nbt.getBoolean("extended");
		
	}

}
