package zmaster587.advancedRocketry.tile.oxygen;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import org.jetbrains.annotations.NotNull;
import zmaster587.advancedRocketry.api.*;
import zmaster587.advancedRocketry.api.util.IBlobHandler;
import zmaster587.advancedRocketry.atmosphere.AtmosphereHandler;
import zmaster587.advancedRocketry.atmosphere.AtmosphereTypes;
import zmaster587.advancedRocketry.dimension.DimensionManager;
import zmaster587.advancedRocketry.inventory.TextureResources;
import zmaster587.libVulpes.LibVulpes;
import zmaster587.libVulpes.api.IToggleableMachine;
import zmaster587.libVulpes.client.RepeatingSound;
import zmaster587.libVulpes.inventory.modules.IButtonInventory;
import zmaster587.libVulpes.inventory.modules.IModularInventory;
import zmaster587.libVulpes.inventory.modules.IToggleButton;
import zmaster587.libVulpes.inventory.modules.ModuleBase;
import zmaster587.libVulpes.inventory.modules.ModuleLiquidIndicator;
import zmaster587.libVulpes.inventory.modules.ModulePower;
import zmaster587.libVulpes.inventory.modules.ModuleRedstoneOutputButton;
import zmaster587.libVulpes.inventory.modules.ModuleToggleSwitch;
import zmaster587.libVulpes.network.PacketHandler;
import zmaster587.libVulpes.network.PacketMachine;
import zmaster587.libVulpes.tile.TileInventoriedRFConsumerTank;
import zmaster587.libVulpes.util.BlockPosition;
import zmaster587.libVulpes.util.IAdjBlockUpdate;
import zmaster587.libVulpes.util.INetworkMachine;
import zmaster587.libVulpes.util.ZUtils.RedstoneState;
import io.netty.buffer.ByteBuf;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import cpw.mods.fml.relauncher.Side;

public class TileOxygenVent extends TileInventoriedRFConsumerTank implements IBlobHandler, IModularInventory, INetworkMachine, IAdjBlockUpdate, IToggleableMachine, IButtonInventory, IToggleButton {


	boolean isSealed;
	boolean firstRun;
	boolean hasFluid;
	boolean soundInit;
	boolean allowTrace;
	int numScrubbers;
	final List<TileCO2Scrubber> scrubbers;
	int radius = 0;
	
	final static byte PACKET_REDSTONE_ID = 2;
	final static byte PACKET_TRACE_ID = 3;
	
	RedstoneState state;
	final ModuleRedstoneOutputButton redstoneControl;
	final ModuleToggleSwitch traceToggle;
	
	public TileOxygenVent() {
		super(1000,2, 1000);
		isSealed = true;
		firstRun = true;
		hasFluid = true;
		soundInit = false;
		allowTrace = false;
		numScrubbers = 0;
		scrubbers = new LinkedList<>();
		
		state = RedstoneState.ON;
		redstoneControl = new ModuleRedstoneOutputButton(174, 4, PACKET_REDSTONE_ID, "", this);
		traceToggle = new ModuleToggleSwitch(80, 20, PACKET_TRACE_ID, LibVulpes.proxy.getLocalizedString("msg.vent.trace"), this, TextureResources.buttonGeneric, 80, 18, false);
	}

	public TileOxygenVent(int energy, int invSize, int tankSize) {
		super(energy, invSize, tankSize);
		isSealed = false;
		firstRun = false;
		hasFluid = true;
		soundInit = false;
		allowTrace = false;
		scrubbers = new LinkedList<>();
		
		state = RedstoneState.ON;
		redstoneControl = new ModuleRedstoneOutputButton(174, 4, 0, "", this);
		traceToggle = new ModuleToggleSwitch(80, 20, 5, LibVulpes.proxy.getLocalizedString("msg.vent.trace"), this, TextureResources.buttonGeneric, 80, 18, false);
	}

	@Override
	public boolean canPerformFunction() {
		return AtmosphereHandler.hasAtmosphereHandler(this.worldObj.provider.dimensionId);
	}

	@Override
	public void updateEntity() {

		if(canPerformFunction()) {
			if(hasEnoughEnergy(getPowerPerOperation())) {
				performFunction();
				if(!worldObj.isRemote && isSealed) this.energy.extractEnergy(getPowerPerOperation(), false);
			}
			else
				notEnoughEnergyForFunction();
		}
		
		if(!soundInit && worldObj.isRemote) {
			LibVulpes.proxy.playSound(new RepeatingSound(TextureResources.sndHiss, this));
		}
		soundInit = true;
	}

	@Override
	public Packet getDescriptionPacket() {
		NBTTagCompound nbt = new NBTTagCompound();
		nbt.setBoolean("sealed", isSealed);
		return new S35PacketUpdateTileEntity(xCoord, yCoord, zCoord, 0, nbt);
	}
	
	@Override
	public void onDataPacket(NetworkManager net, S35PacketUpdateTileEntity pkt) {
		isSealed = pkt.func_148857_g().getBoolean("sealed");
	}
	
	@Override
	public World getWorld() {
		return getWorldObj();
	}

	@Override
	public void onAdjacentBlockUpdated() {
		if(isSealed) 
			activateAdjblocks();

		scrubbers.clear();
		TileEntity[] tiles = new TileEntity[4];
		tiles[0] = worldObj.getTileEntity(this.xCoord + 1, this.yCoord, this.zCoord);
		tiles[1] = worldObj.getTileEntity(this.xCoord - 1, this.yCoord, this.zCoord);
		tiles[2] = worldObj.getTileEntity(this.xCoord, this.yCoord, this.zCoord + 1);
		tiles[3] = worldObj.getTileEntity(this.xCoord, this.yCoord, this.zCoord - 1);

		for(TileEntity tile : tiles) {
			if(tile instanceof TileCO2Scrubber && worldObj.getBlock(tile.xCoord, tile.yCoord, tile.zCoord) == AdvancedRocketryBlocks.blockOxygenScrubber)
				scrubbers.add((TileCO2Scrubber)tile);
		}


	}

	private void activateAdjblocks() {
		numScrubbers = 0;
		numScrubbers = toggleAdjBlock(this.xCoord + 1, this.yCoord, this.zCoord, true) ? numScrubbers + 1 : numScrubbers;
		numScrubbers = toggleAdjBlock(this.xCoord - 1, this.yCoord, this.zCoord, true) ? numScrubbers + 1 : numScrubbers;
		numScrubbers = toggleAdjBlock(this.xCoord, this.yCoord, this.zCoord + 1, true) ? numScrubbers + 1 : numScrubbers;
		numScrubbers = toggleAdjBlock(this.xCoord, this.yCoord, this.zCoord - 1, true) ? numScrubbers + 1 : numScrubbers;
		
		markDirty();
		worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
	}

	private void deactivateAdjblocks() {
		toggleAdjBlock(this.xCoord + 1, this.yCoord, this.zCoord, false);
		toggleAdjBlock(this.xCoord - 1, this.yCoord, this.zCoord, false);
		toggleAdjBlock(this.xCoord, this.yCoord, this.zCoord + 1, false);
		toggleAdjBlock(this.xCoord, this.yCoord, this.zCoord - 1, false);
		
		markDirty();
		worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
	}

	private boolean toggleAdjBlock(int x, int y, int z, boolean on) {
		Block block = this.worldObj.getBlock(x,y,z);
		if(block == AdvancedRocketryBlocks.blockOxygenScrubber) {
			int meta = worldObj.getBlockMetadata(x,y,z);
			if(on && (meta & 8) == 0)
				worldObj.setBlockMetadataWithNotify(x, y, z, 8, 2);
			else if(!on && (meta & 8) == 8)
				worldObj.setBlockMetadataWithNotify(x, y, z, 0, 2);

			return true;
		}
		return false;
	}

	@Override
	public void invalidate() {
		super.invalidate();

		AtmosphereHandler handler = AtmosphereHandler.getOxygenHandler(this.worldObj.provider.dimensionId);
		if(handler != null)
			AtmosphereHandler.getOxygenHandler(this.worldObj.provider.dimensionId).unregisterBlob(this);
		deactivateAdjblocks();
	}

	@Override
	public int getPowerPerOperation() {
		return (int)((numScrubbers*10 + 1)*Configuration.oxygenVentPowerMultiplier);
	}

	@Override
	public boolean canFill(ForgeDirection from, Fluid fluid) {
		return fluid.getID() == AdvancedRocketryFluids.fluidOxygen.getID() && super.canFill(from, fluid);
	}
	
	public boolean getEquivilentPower() {
		if(state == RedstoneState.OFF)
			return true;

		
		boolean state2 = worldObj.isBlockIndirectlyGettingPowered(xCoord, yCoord, zCoord);

		if(state == RedstoneState.INVERTED)
			state2 = !state2;
		return state2;
	}

	@Override
	public void performFunction() {

		/*NB: canPerformFunction returns false and must return true for perform function to execute
		 *  if there is no O2 handler, this is why we can safely call AtmosphereHandler.getOxygenHandler
		 * And not have to worry about an NPE being thrown
		 */

		//IF first tick then register the blob and check for scrubbers


		if(!worldObj.isRemote) {
			if(firstRun) {
                AtmosphereHandler.getOxygenHandler(this.worldObj.provider.dimensionId).registerBlob(this, xCoord, yCoord, zCoord);

                onAdjacentBlockUpdated();
                //isSealed starts as true so we can accurately check for scrubbers, we now set it to false to force the tile to check for a seal on first run
                isSealed = false;

                firstRun = false;
			}
			
			if(isSealed && AtmosphereHandler.getOxygenHandler(this.worldObj.provider.dimensionId).getBlobSize(this) == 0) {
				deactivateAdjblocks();
				setSealed(false);
			}

			if(isSealed && !getEquivilentPower() ) {
				AtmosphereHandler.getOxygenHandler(this.worldObj.provider.dimensionId).clearBlob(this);

				deactivateAdjblocks();

				isSealed = false;
			}
			else if(!isSealed && getEquivilentPower() ) {
				
				setSealed(AtmosphereHandler.getOxygenHandler(this.worldObj.provider.dimensionId).addBlock(this, new BlockPosition(this.xCoord, this.yCoord, this.zCoord)));


				if(isSealed) {
					activateAdjblocks();
				}
				else if(worldObj.getTotalWorldTime() % 10 == 0 && allowTrace) {
					radius++;
					if(radius > 128)
						radius = 0;
				}
			}

			if(isSealed) {
				//if(worldObj.getTotalWorldTime() % 30 == 0)
					//worldObj.playSoundEffect(xCoord, yCoord, zCoord, "advancedrocketry:airHissLoop", 0.3f,  0.975f + worldObj.rand.nextFloat()*0.05f);

				//If scrubbers exist and the config allows then use the cartridge
				if(Configuration.scrubberRequiresCartrige){
					//TODO: could be optimized
					if(worldObj.getTotalWorldTime() % 200 == 0) {
						numScrubbers = 0;
						for(TileCO2Scrubber scrubber : scrubbers) {
							numScrubbers =  scrubber.useCharge() ? numScrubbers + 1 : numScrubbers;
						}
					}

				}

				int amtToDrain = (int) (AtmosphereHandler.getOxygenHandler(this.worldObj.provider.dimensionId).getBlobSize(this)*getGasUsageMultiplier());
				FluidStack drainedFluid = this.drain(ForgeDirection.UNKNOWN, amtToDrain, false);

				if( (drainedFluid != null && drainedFluid.amount >= amtToDrain) || amtToDrain == 0) {
					this.drain(ForgeDirection.UNKNOWN, amtToDrain, true);
					if(!hasFluid) {
						hasFluid = true;

						activateAdjblocks();

						AtmosphereHandler.getOxygenHandler(this.worldObj.provider.dimensionId).setAtmosphereType(this, AtmosphereTypes.PRESSURIZEDAIR);
					}
				}
				else if(hasFluid){
					AtmosphereHandler.getOxygenHandler(this.worldObj.provider.dimensionId).setAtmosphereType(this, DimensionManager.getInstance().getDimensionProperties(this.worldObj.provider.dimensionId).getAtmosphere());

					deactivateAdjblocks();

					hasFluid = false;
				}
			}
		}
	}
	
	@Override
	public int getTraceDistance() {
		return allowTrace ? radius : -1;
	}

	
	private void setSealed(boolean sealed) {
		boolean prevSealed = isSealed;
		if((prevSealed != sealed)) {
			markDirty();
			worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
			
			if(isSealed)
				radius = -1;
		}
		isSealed = sealed;
	}

	public float getGasUsageMultiplier() {
		return (float) (Math.max(0.01f - numScrubbers*0.005f,0)*Configuration.oxygenVentConsumptionMult);
	}

	@Override
	public void notEnoughEnergyForFunction() {
		if(!worldObj.isRemote) {
			AtmosphereHandler handler = AtmosphereHandler.getOxygenHandler(this.worldObj.provider.dimensionId);
			if(handler != null)
				handler.clearBlob(this);

			deactivateAdjblocks();

			isSealed = false;
		}
	}

	@Override
	public int[] getAccessibleSlotsFromSide(int p_94128_1_) {
		return new int[]{};
	}

	@Override
	public boolean isItemValidForSlot(int slot, ItemStack itemStack) {
		return false;
	}

	@Override
	public boolean canBlobsOverlap(BlockPosition blockPosition, AreaBlob blob) {
		return false;
	}

	@Override
	public int getMaxBlobRadius() {
		return Configuration.oxygenVentSize;
	}

	@Override
	public BlockPosition getRootPosition() {
		return new BlockPosition(this.xCoord, this.yCoord, this.zCoord);
	}

	@Override
	public List<ModuleBase> getModules(int ID, EntityPlayer player) {
		ArrayList<ModuleBase> modules = new ArrayList<>();

		modules.add(new ModulePower(18, 20, this));
		modules.add(new ModuleLiquidIndicator(32, 20, this));
		modules.add(redstoneControl);
		modules.add(traceToggle);
		//modules.add(toggleSwitch = new ModuleToggleSwitch(160, 5, 0, "", this, TextureResources.buttonToggleImage, 11, 26, getMachineEnabled()));
		//TODO add itemStack slots for liqiuid
		return modules;
	}

	@Override
	public @NotNull String getModularInventoryName() {
		return "OxygenVent";
	}

	@Override
	public boolean canInteractWithContainer(EntityPlayer entity) {
		return true;
	}

	@Override
	public boolean canFormBlob() {
		return getEquivilentPower() ;
	}

	@Override
	public boolean isRunning() {
		return isSealed;
	}
	
	@Override
	public void onInventoryButtonPressed(int buttonId) {
		if(buttonId == PACKET_REDSTONE_ID) {
			state = redstoneControl.getState();
			PacketHandler.sendToServer(new PacketMachine(this, PACKET_REDSTONE_ID));
		}
		if(buttonId == PACKET_TRACE_ID) {
			allowTrace = traceToggle.getState();
			PacketHandler.sendToServer(new PacketMachine(this, PACKET_TRACE_ID));
		}
	}
	
	@Override
	public void writeDataToNetwork(ByteBuf out, byte id) {
		if(id == PACKET_REDSTONE_ID)
			out.writeByte(state.ordinal());
		else if(id == PACKET_TRACE_ID)
			out.writeBoolean(allowTrace);
	}

	@Override
	public void readDataFromNetwork(ByteBuf in, byte packetId,
			NBTTagCompound nbt) {
		if(packetId == PACKET_REDSTONE_ID)
			nbt.setByte("state", in.readByte());
		else if(packetId == PACKET_TRACE_ID)
			nbt.setBoolean("trace", in.readBoolean());
	}

	@Override
	public void useNetworkData(EntityPlayer player, Side side, byte id,
			NBTTagCompound nbt) {
		if(id == PACKET_REDSTONE_ID)
			state = RedstoneState.values()[nbt.getByte("state")];
		else if(id == PACKET_TRACE_ID) {
			allowTrace = nbt.getBoolean("trace");
			if(!allowTrace)
				radius = -1;
		}
	}
	
	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		super.readFromNBT(nbt);
		
		state = RedstoneState.values()[nbt.getByte("redstoneState")];
		redstoneControl.setRedstoneState(state);
		allowTrace = nbt.getBoolean("allowtrace");

	}
	
	@Override
	public void writeToNBT(@NotNull NBTTagCompound nbt) {
		super.writeToNBT(nbt);
		nbt.setByte("redstoneState", (byte) state.ordinal());
		nbt.setBoolean("allowtrace", allowTrace);
	}

	@Override
	public void stateUpdated(ModuleBase module) {
		if(module.equals(traceToggle)) {
			allowTrace = ((ModuleToggleSwitch)module).getState();
			PacketHandler.sendToServer(new PacketMachine(this, PACKET_TRACE_ID));
		}
	}
}