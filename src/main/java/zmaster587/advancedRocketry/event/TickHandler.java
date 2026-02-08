package zmaster587.advancedRocketry.event;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import cpw.mods.fml.common.gameevent.TickEvent.Phase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.event.world.BlockEvent.BreakEvent;
import net.minecraftforge.event.world.ChunkEvent;
import zmaster587.advancedRocketry.AdvancedRocketry;
import zmaster587.advancedRocketry.cable.NetworkRegistry;
import zmaster587.advancedRocketry.dimension.sim.AdvanceRocketrySimulateUniverseCompact;
import zmaster587.advancedRocketry.dimension.sim.SimUniverse;
import zmaster587.advancedRocketry.entity.EntityCelestialBody;
import zmaster587.advancedRocketry.tile.cables.TilePipe;

import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

public class TickHandler {

	@SubscribeEvent
	public void onTick(TickEvent.ServerTickEvent tick) {
		try {
			if(tick.phase == Phase.END) {
				NetworkRegistry.dataNetwork.tickAllNetworks();
				NetworkRegistry.energyNetwork.tickAllNetworks();
				NetworkRegistry.liquidNetwork.tickAllNetworks();
			}
			if(tick.phase == Phase.START){
				AdvanceRocketrySimulateUniverseCompact.tick();
			}
		} catch (ConcurrentModificationException e) {
			AdvancedRocketry.logger.error(e);
		}
	}

	private static final double SPAWN_RANGE = 1000.0D;
	private static final double DESPAWN_RANGE = 1200.0D;

	@SubscribeEvent
	public void onWorldTick(TickEvent.WorldTickEvent event) {
		if (event.phase != TickEvent.Phase.START || event.world.isRemote) return;

		if (event.world.provider.dimensionId != -3) return;
		if (event.world.playerEntities.isEmpty()) return;

		SimUniverse universe = SimUniverse.getInstance();

		for (SimUniverse.SimBody body : universe.getAllBodies()) {
			if (body.hasEntity()) {
				EntityCelestialBody existing = body.getEntity();
				if (existing.isDead || existing.worldObj != event.world) {
					body.setEntity(null);
				}
			}

			boolean playerNearby = isPlayerNearby(event.world, body);

			if (playerNearby && !body.hasEntity()) {
				int cx = (int)Math.floor(body.x) >> 4;
				int cz = (int)Math.floor(body.z) >> 4;
				if (!event.world.getChunkProvider().chunkExists(cx, cz)) {
					event.world.getChunkProvider().loadChunk(cx, cz);
				}

				EntityCelestialBody entity = new EntityCelestialBody(event.world, body);
				event.world.spawnEntityInWorld(entity);
				body.setEntity(entity);
			}
			else if (!playerNearby && body.hasEntity()) {
				body.getEntity().setDead();
				body.setEntity(null);
			}
		}
	}

	private boolean isPlayerNearby(World world, SimUniverse.SimBody body) {
		for (Object obj : world.playerEntities) {
			EntityPlayer player = (EntityPlayer) obj;
			double distSq = player.getDistanceSq(body.x, body.y, body.z);
			if (distSq < SPAWN_RANGE * SPAWN_RANGE) return true;
		}
		return false;
	}

	@SubscribeEvent
	public void chunkLoadedEvent(ChunkEvent.Load event) {

		Map<?,?> map = event.getChunk().chunkTileEntityMap;
		Iterator<? extends Entry<?, ?>> iter = map.entrySet().iterator();

		try {
			while(iter.hasNext()) {
				Object obj = iter.next().getValue();

				if(obj instanceof TilePipe) {
					((TilePipe)obj).markForUpdate();
				}
			}
		} catch ( ConcurrentModificationException e) {
			AdvancedRocketry.logger.warn("You have been visited by the rare pepe.. I mean error of pipes not loading, this is not good, some pipe systems may not work right away.  But it's better than a corrupt world");
		}

	}

	@SubscribeEvent
	public void onBlockBroken(BreakEvent event) {

		if(event.block.hasTileEntity(event.blockMetadata)) {

			TileEntity homeTile = event.world.getTileEntity(event.x , event.y, event.z);

			if(homeTile instanceof TilePipe) {

				//removed in favor of pipecount
				//boolean lastInNetwork =true;

				((TilePipe)homeTile).setDestroyed();
				((TilePipe)homeTile).setInvalid();

				int pipecount=0;

				for(ForgeDirection dir : ForgeDirection.VALID_DIRECTIONS) {
					TileEntity tile = event.world.getTileEntity(event.x + dir.offsetX, event.y + dir.offsetY, event.z + dir.offsetZ);
					if(tile instanceof TilePipe) 
						pipecount++;
				}
				//TODO: delete check if sinks/sources need removal
				if(pipecount > 1) {
					for(ForgeDirection dir : ForgeDirection.VALID_DIRECTIONS) {
						TileEntity tile = event.world.getTileEntity(event.x + dir.offsetX, event.y + dir.offsetY, event.z + dir.offsetZ);

						if(tile instanceof TilePipe) {
							((TilePipe) tile).getNetworkHandler().removeNetworkByID(((TilePipe) tile).getNetworkID());
							((TilePipe) tile).setInvalid();
							//lastInNetwork = false;
						}
						//HandlerCableNetwork.removeFromAllTypes((TilePipe)tile,event.world.getTileEntity(event.x, event.y, event.z));
					}
				}
				if(pipecount == 0) //lastInNetwork
					((TilePipe)homeTile).getNetworkHandler().removeNetworkByID(((TilePipe)homeTile).getNetworkID());
				(homeTile).markDirty();
			}
			else if(homeTile != null) {
				for(ForgeDirection dir : ForgeDirection.VALID_DIRECTIONS) {
					TileEntity tile = event.world.getTileEntity(event.x + dir.offsetX, event.y + dir.offsetY, event.z + dir.offsetZ);

					if(tile instanceof TilePipe) {
						((TilePipe)tile).getNetworkHandler().removeFromAllTypes((TilePipe)tile, homeTile);
					}
				}
			}
		}
	}
}
