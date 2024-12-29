package zmaster587.advancedRocketry.satellite;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraftforge.oredict.OreDictionary;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import zmaster587.advancedRocketry.AdvancedRocketry;
import zmaster587.advancedRocketry.api.Configuration;
import zmaster587.advancedRocketry.api.satellite.SatelliteBase;
import zmaster587.libVulpes.util.ZUtils;

public class SatelliteLaserNoDrill extends SatelliteBase {
	protected boolean  jammed;
	protected final IInventory boundChest;
	World world;
	private static List<ItemStack> ores;
	final Random random;

	public SatelliteLaserNoDrill(IInventory boundChest) {
		this.boundChest = boundChest;
		random = new Random(System.currentTimeMillis());

		//isEmpty check because <init> is called in post init to register for holo projector
		if(ores == null && !Configuration.standardLaserDrillOres.isEmpty()) {
			ores = new LinkedList<>();
			for(int i = 0; i < Configuration.standardLaserDrillOres.size(); i++) {
				String oreDictName = Configuration.standardLaserDrillOres.get(i);
				
				String[] args = oreDictName.split(":");
				
				List<ItemStack> ores2 = OreDictionary.getOres(args[0]);

				if(ores2 != null && !ores2.isEmpty()) {
					int amt = 5;
					if(args.length > 1)
					{
						try {
							amt = Integer.parseInt(args[1]);
						} catch (NumberFormatException e) {}
					}
					ores.add(new ItemStack(ores2.get(0).getItem(), amt, ores2.get(0).getItemDamage()));
				}
				else
				{
					String[] splitStr = oreDictName.split(":");
					String name;
					try {
						name = splitStr[0] + ":" + splitStr[1];
					}
					catch(IndexOutOfBoundsException e) {
                        AdvancedRocketry.logger.warn("Unexpected ore name: \"{}\" during laser drill harvesting", oreDictName);
						continue;
					}
					
					int meta = 0;
					int size = 1;
					//format: "name meta size"
					if(splitStr.length > 2) {
						try {
							meta = Integer.parseInt(splitStr[2]);
						} catch( NumberFormatException e) {}
					}
					if(splitStr.length > 3) {
						try {
							size= Integer.parseInt(splitStr[3]);
						} catch (NumberFormatException e) {}
					}

					ItemStack stack = null;
					Block block = Block.getBlockFromName(name);
					if(block == null) {

						//Try getting item by name first
						Item item = (Item) Item.itemRegistry.getObject(splitStr[0]);

						if(item != null)
							stack = new ItemStack(item, size, meta);
						else {
							try {

								item = Item.getItemById(Integer.parseInt(splitStr[0]));
								if(item != null)
									stack = new ItemStack(item, size, meta);
							} catch (NumberFormatException e) { continue; }

						}
					}
					else
						stack = new ItemStack(block, size, meta);
					
					if(stack != null)
						ores.add(stack);
				}
			}
		}
	}

	public boolean isAlive() {
		return world != null;
	}

	public boolean isFinished() {
		return false;
	}

	public boolean getJammed() { return jammed; }

	public void setJammed(boolean newJam) { jammed = newJam; }

	public void deactivateLaser() {
		this.world = null;
	}

	/**
	 * creates the laser and begins mining.  This can
	 * fail if the chunk cannot be force loaded
	 * @param world world to spawn the laser into
	 * @param x x coord
	 * @param z z coord
	 * @return whether creating the laser is successful
	 */
	public boolean activateLaser(World world, int x, int z) {
		this.world = world;
		return true;
	}

	
	public void performOperation() {

		ArrayList<ItemStack> items = new ArrayList<>();
		if(random.nextInt(10) == 0) {
			ItemStack item = ores.get(random.nextInt(ores.size()));
			ItemStack newStack = item.copy();
			items.add(newStack);
		}
		else
			items.add(new ItemStack(Blocks.cobblestone, 5));
		
		
		//TODO: generate Items

		if(boundChest != null){
			ItemStack[] stacks = new ItemStack[items.size()];

			stacks = items.toArray(stacks);

			ZUtils.mergeInventory(stacks, boundChest);

			if(!ZUtils.isInvEmpty(stacks)) {
				//TODO: drop extra items
				this.deactivateLaser();
				this.jammed = true;
            }
		}
	}

	@Override
	public @Nullable String getInfo(World world) {
		return null;
	}

	@Override
	public @NotNull String getName() {
		return "Laser";
	}

	@Override
	public boolean performAction(EntityPlayer player, World world, int x,
			int y, int z) {
		performOperation();
		return false;
	}

	@Override
	public double failureChance() {
		return 0;
	}

	@Override
	public void writeToNBT(@NotNull NBTTagCompound nbt) {
		nbt.setBoolean("jammed", jammed);
	}

	@Override
	public void readFromNBT(@NotNull NBTTagCompound nbt) {
		jammed = nbt.getBoolean("jammed");
	}

}