package zmaster587.advancedRocketry.cable;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.ForgeDirection;
import zmaster587.advancedRocketry.tile.cables.TilePipe;
import zmaster587.libVulpes.util.SingleEntry;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

public class CableNetwork {

	int networkID;
	protected int numCables = 0;

	protected static HashSet<Integer> usedIds = new HashSet<>();

	CopyOnWriteArraySet<Entry<TileEntity, ForgeDirection>> sources;

	CopyOnWriteArraySet<Entry<TileEntity, ForgeDirection>> sinks;

	protected CableNetwork() {

		sources = new CopyOnWriteArraySet<>();
		sinks = new CopyOnWriteArraySet<>();
	}

	public Set<Entry<TileEntity, ForgeDirection>> getSources() {
		return sources;
	}

	public Set<Entry<TileEntity, ForgeDirection>> getSinks() {
		return sinks;
	}

	public void addSource(TileEntity tile, ForgeDirection dir) {

        for (Entry<TileEntity, ForgeDirection> entry : sources) {
            TileEntity tile2 = entry.getKey();
            if (tile2.equals(tile)) {
                return;
            }
            if (tile2.xCoord == tile.xCoord && tile2.yCoord == tile.yCoord && tile2.zCoord == tile.zCoord) {
                sources.remove(entry);
                break;
            }
        }

		sources.add(new SingleEntry<>(tile, dir));
	}

	public void addSink(TileEntity tile, ForgeDirection dir) {

        for (Entry<TileEntity, ForgeDirection> entry : sinks) {
            TileEntity tile2 = entry.getKey();
            if (tile2.equals(tile)) {
                return;
            }
            if (tile2.xCoord == tile.xCoord && tile2.yCoord == tile.yCoord && tile2.zCoord == tile.zCoord) {
                sinks.remove(entry);
                break;
            }
        }

		sinks.add(new SingleEntry<>(tile, dir));
	}

	public void writeToNBT(NBTTagCompound nbt) {

	}


	public void readFromNBT(NBTTagCompound nbt) {

	}

	public static CableNetwork initWithID(int id) {
		CableNetwork net = new CableNetwork();
		net.networkID = id;

		return net;
	}

	public static CableNetwork initNetwork() {
		Random random = new Random(System.currentTimeMillis());

		int id = random.nextInt();

		while(usedIds.contains(id)){ id = random.nextInt(); }

        CableNetwork net = new CableNetwork();

		usedIds.add(id);
		net.networkID = id;

		return net;
	}

	public int getNetworkID() {	return networkID; }

	public void removeFromAll(TileEntity tile) {
		Iterator<Entry<TileEntity, ForgeDirection>> iter = sources.iterator();

		while(iter.hasNext()) {
			Entry<TileEntity, ForgeDirection> entry = iter.next();
			TileEntity tile2 = entry.getKey();
			if(tile2.xCoord == tile.xCoord && tile2.yCoord == tile.yCoord && tile2.zCoord == tile.zCoord) {
				sources.remove(entry);
				break;
			}
		}

		iter = sinks.iterator();

		while(iter.hasNext()) {
			Entry<TileEntity, ForgeDirection> entry = iter.next();
			TileEntity tile2 = entry.getKey();
			if(tile2.xCoord == tile.xCoord && tile2.yCoord == tile.yCoord && tile2.zCoord == tile.zCoord) {
				sinks.remove(entry);
				break;
			}
		}

	}

	@Override 
	public String toString() {
		StringBuilder output = new StringBuilder("Sources: ");
		for(Entry<TileEntity, ForgeDirection> obj : sources) {
			TileEntity tile = obj.getKey();
			output.append(tile.xCoord).append(",").append(tile.yCoord).append(",").append(tile.zCoord).append(" ");
		}

		output.append("    Sinks: ");
		for(Entry<TileEntity, ForgeDirection> obj : sinks) {
			TileEntity tile = obj.getKey();
			output.append(tile.xCoord).append(",").append(tile.yCoord).append(",").append(tile.zCoord).append(" ");
		}
		return output.toString();
	}

	/**
	 * Merges this network with the one specified.  Normally the specified one is removed
	 * @param cableNetwork
	 */
	public boolean merge(CableNetwork cableNetwork) {
		sinks.addAll(cableNetwork.getSinks());

		for(Entry<TileEntity, ForgeDirection> obj : cableNetwork.getSinks()) {
			for(Entry<TileEntity, ForgeDirection> obj2 : sinks) {
				if(obj.getKey().xCoord == obj2.getKey().xCoord && obj.getKey().yCoord == obj2.getKey().yCoord && obj.getKey().zCoord == obj2.getKey().zCoord && obj.getValue() == obj2.getValue()) {
					return false;
				}
			}
			sinks.add(obj);
        }

		for(Entry<TileEntity, ForgeDirection> obj : cableNetwork.getSources()) {
			for(Entry<TileEntity, ForgeDirection> obj2 : sources) {
				if(obj.getKey().xCoord == obj2.getKey().xCoord && obj.getKey().yCoord == obj2.getKey().yCoord && obj.getKey().zCoord == obj2.getKey().zCoord && obj.getValue() == obj2.getValue()) {
					return false;
				}
			}
			sources.add(obj);
		}
		return true;
	}

	public void tick() {
	}

	public void removePipeFromNetwork(TilePipe tilePipe) {
		numCables--;
	}

	public void addPipeToNetwork(TilePipe tilePipe) {
		numCables++;
	}
}
