package zmaster587.advancedRocketry.util;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.play.server.S1BPacketEntityAttach;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.WorldServer;
import zmaster587.advancedRocketry.event.PlanetEventHandler;
import zmaster587.advancedRocketry.world.util.TeleporterNoPortal;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TeleportHelper {
    public static void teleportEntityWithRiding(Entity entity, int targetDimId, double x, double y, double z) {
        Entity root = getRootEntity(entity);
        List<Entity> chain = new ArrayList<>();
        Entity current = root;
        while (current != null) {
            chain.add(current);
            current = current.riddenByEntity;
        }

        for (Entity e : chain) {
            if (e.riddenByEntity != null) e.riddenByEntity.mountEntity(null);
        }

        //teleport
        Map<Integer, Entity> newChainRefs = new HashMap<>();
        for (int i = 0; i < chain.size(); i++) {
            Entity oldEnt = chain.get(i);
            Entity newEnt = performStrictTeleport(oldEnt, targetDimId, x, y, z);
            if (newEnt != null) {
                newChainRefs.put(i, newEnt);
            }
        }
        //remount
        for (int i = 0; i < chain.size() - 1; i++) {
            Entity parent = newChainRefs.get(i);
            Entity child = newChainRefs.get(i + 1);
            if (parent != null && child != null) {
                //A tick of slack: the client has to have been told about both entities before it can attach them
                PlanetEventHandler.scheduleDelayed(1, ()-> {
                    child.mountEntity(parent);
                    WorldServer targetWorld = MinecraftServer.getServer().worldServerForDimension(targetDimId);
                    //Args are (leash flag, rider, ridden) - matches EntityTrackerEntry's own attach packet.
                    //func_151248_b also delivers to the rider itself, which a teleported player needs.
                    targetWorld.getEntityTracker().func_151248_b(child, new S1BPacketEntityAttach(0, child, parent));
                    targetWorld.resetUpdateEntityTick();
                });
            }
        }
    }

    private static Entity performStrictTeleport(Entity entity, int dim, double x, double y, double z) {
        if (entity.worldObj.isRemote || entity.isDead) return null;

        WorldServer targetWorld = MinecraftServer.getServer().worldServerForDimension(dim);
        if (targetWorld == null) return null;

        if (entity instanceof EntityPlayerMP) {
            EntityPlayerMP player = (EntityPlayerMP) entity;
            player.mcServer.getConfigurationManager().transferPlayerToDimension(player, dim, new TeleporterNoPortal(targetWorld));
            player.playerNetServerHandler.setPlayerLocation(x, y, z, player.rotationYaw, player.rotationPitch);
            return player;
        } else {
            entity.mountEntity(null);

            entity.worldObj.removeEntity(entity);
            entity.isDead = false;
            Entity entityNew = EntityList.createEntityByName(EntityList.getEntityString(entity), targetWorld);

            if (entityNew != null) {
                entityNew.copyDataFrom(entity, true);
                targetWorld.getChunkProvider().loadChunk((int)x >> 4, (int)z >> 4);
                entityNew.addedToChunk = false;
                entityNew.dimension = dim;
                entityNew.forceSpawn = true;
                entityNew.setLocationAndAngles(x, y, z, entity.rotationYaw, entity.rotationPitch);
                targetWorld.spawnEntityInWorld(entityNew);
                entityNew.setWorld(targetWorld);

                targetWorld.getEntityTracker().removeEntityFromAllTrackingPlayers(entity);
                entity.isDead = true;
                return entityNew;
            }
            return entity;

        }
    }

    private static Entity getRootEntity(Entity entity) {
        Entity root = entity;
        while (root.ridingEntity != null) {
            root = root.ridingEntity;
        }
        return root;
    }
}