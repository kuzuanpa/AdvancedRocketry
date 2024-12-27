package zmaster587.advancedRocketry.tile;

import io.netty.buffer.ByteBuf;

import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import cpw.mods.fml.relauncher.Side;
import org.jetbrains.annotations.NotNull;
import zmaster587.advancedRocketry.api.IAtmosphere;
import zmaster587.advancedRocketry.api.atmosphere.AtmosphereRegister;
import zmaster587.advancedRocketry.atmosphere.AtmosphereHandler;
import zmaster587.advancedRocketry.atmosphere.AtmosphereTypes;
import zmaster587.libVulpes.LibVulpes;
import zmaster587.libVulpes.inventory.modules.IButtonInventory;
import zmaster587.libVulpes.inventory.modules.IModularInventory;
import zmaster587.libVulpes.inventory.modules.ModuleBase;
import zmaster587.libVulpes.inventory.modules.ModuleButton;
import zmaster587.libVulpes.inventory.modules.ModuleContainerPan;
import zmaster587.libVulpes.network.PacketHandler;
import zmaster587.libVulpes.network.PacketMachine;
import zmaster587.libVulpes.util.INetworkMachine;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.ForgeDirection;

public class TileAtmosphereDetector extends TileEntity implements IModularInventory, IButtonInventory, INetworkMachine {

	IAtmosphere atmosphereToDetect;

	public TileAtmosphereDetector() {
		atmosphereToDetect = AtmosphereTypes.AIR;
	}

	@Override
	public boolean canUpdate() {
		return true;
	}

	@Override
	public void updateEntity() {
		if(!worldObj.isRemote && worldObj.getWorldTime() % 10 == 0) {
			int meta = worldObj.getBlockMetadata(xCoord, yCoord, zCoord);
			boolean detectedAtm = false;

			//TODO: Galacticcraft support
			if(AtmosphereHandler.getOxygenHandler(worldObj.provider.dimensionId) == null) {
				detectedAtm = atmosphereToDetect == AtmosphereTypes.AIR;
			}
			else {
				for(int i = 1; i < ForgeDirection.values().length; i++) {
					ForgeDirection direction = ForgeDirection.getOrientation(i);
					detectedAtm = (!worldObj.getBlock(xCoord + direction.offsetX, yCoord + direction.offsetY, zCoord + direction.offsetZ).isOpaqueCube() && atmosphereToDetect == AtmosphereHandler.getOxygenHandler(worldObj.provider.dimensionId).getAtmosphereType(xCoord + direction.offsetX, yCoord + direction.offsetY, zCoord + direction.offsetZ));
					if(detectedAtm) break;
				}
			}

			if((meta == 1) != detectedAtm) {
				worldObj.setBlockMetadataWithNotify(xCoord, yCoord, zCoord, detectedAtm ? 1 : 0, 3);
			}
		}
	}

	@Override
	public @NotNull List<ModuleBase> getModules(int id, EntityPlayer player) {
		List<ModuleBase> modules = new LinkedList<>();
		List<ModuleBase> btns = new LinkedList<>();

		Iterator<IAtmosphere> atmIter = AtmosphereRegister.getInstance().getAtmosphereList().iterator();

		int i = 0;
		while(atmIter.hasNext()) {
			IAtmosphere atm = atmIter.next();
			btns.add(new ModuleButton(60, 4 + i*24, i, LibVulpes.proxy.getLocalizedString(atm.getUnlocalizedName()), this,  zmaster587.libVulpes.inventory.TextureResources.buttonBuild));
			i++;
		}

		ModuleContainerPan panningContainer = new ModuleContainerPan(5, 20, btns, new LinkedList<>(), zmaster587.libVulpes.inventory.TextureResources.starryBG, 165, 120, 0, 500);
		modules.add(panningContainer);
		return modules;
	}

	@Override
	public @NotNull String getModularInventoryName() {
		return "atmosphereDetector";
	}

	@Override
	public boolean canInteractWithContainer(EntityPlayer entity) {
		return true;
	}

	@Override
	public void onInventoryButtonPressed(int buttonId) {
		atmosphereToDetect = AtmosphereRegister.getInstance().getAtmosphereList().get(buttonId);
		PacketHandler.sendToServer(new PacketMachine(this, (byte)0));
	}

	@Override
	public void writeDataToNetwork(ByteBuf out, byte id) {
		//Send the unlocalized name over the net to reduce chances of foulup due to client/server inconsistencies
		if(id == 0) {
			PacketBuffer buf = new PacketBuffer(out);
			try {
				buf.writeShort(atmosphereToDetect.getUnlocalizedName().length());
				buf.writeStringToBuffer(atmosphereToDetect.getUnlocalizedName());
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public void readDataFromNetwork(ByteBuf in, byte packetId,
			NBTTagCompound nbt) {
		if(packetId == 0) {
			PacketBuffer buf = new PacketBuffer(in);
			try {

				nbt.setString("uName", buf.readStringFromBuffer(buf.readShort()));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public void useNetworkData(EntityPlayer player, Side side, byte id,
							   @NotNull NBTTagCompound nbt) {
		if(id == 0) {
			String name = nbt.getString("uName");
			atmosphereToDetect = AtmosphereRegister.getInstance().getAtmosphere(name);
		}
	}

	@Override
	public void writeToNBT(@NotNull NBTTagCompound nbt) {
		super.writeToNBT(nbt);

		nbt.setString("atmName", atmosphereToDetect.getUnlocalizedName());
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		super.readFromNBT(nbt);

		atmosphereToDetect = AtmosphereRegister.getInstance().getAtmosphere(nbt.getString("atmName"));
	}
}
