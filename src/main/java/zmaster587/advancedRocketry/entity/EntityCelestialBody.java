package zmaster587.advancedRocketry.entity;

import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.common.registry.IEntityAdditionalSpawnData;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import zmaster587.advancedRocketry.dimension.sim.SimUniverse;
import zmaster587.advancedRocketry.util.TeleportHelper;

import java.util.List;

import static zmaster587.advancedRocketry.dimension.sim.AdvanceRocketrySimulateUniverseCompact.debugMode;

public class EntityCelestialBody extends Entity implements IEntityAdditionalSpawnData {
    private String bodyID;
    public SimUniverse.SimBody data;

    public EntityCelestialBody(World world) {
        super(world);
        this.ignoreFrustumCheck = true;
        this.isImmuneToFire = true;
        this.field_70135_K = true;
    }

    public EntityCelestialBody(World world, SimUniverse.SimBody data) {
        this(world);
        this.bodyID = data.getConfig().getID();
        this.data = data;
        this.updateFromSim();
    }

    @Override
    public void writeSpawnData(ByteBuf buffer) {
        ByteBufUtils.writeUTF8String(buffer, this.bodyID);
    }

    @Override
    public void readSpawnData(ByteBuf additionalData) {
        this.bodyID = ByteBufUtils.readUTF8String(additionalData);

        this.data = SimUniverse.getInstance().getBody(this.bodyID);
    }
    @Override
    public void onUpdate() {
        if (data == null) {
            data = SimUniverse.getInstance().getBody(bodyID);
        }

        if(worldObj.isRemote)return;
        if (data != null) {
            updateFromSim();
            checkPlayerProximity();
        } else {
            this.setDead();
        }
    }

    private void checkPlayerProximity() {
        double physicalRadius = data.getConfig().getSize();
        double triggerRadius = physicalRadius * 2.0;

        List<EntityPlayer> players = worldObj.getEntitiesWithinAABB(
                EntityPlayer.class,
                this.boundingBox.expand(triggerRadius, triggerRadius, triggerRadius)
        );

        for (EntityPlayer player : players) {
            double distSq = this.getDistanceSqToEntity(player);

            if (distSq < triggerRadius * triggerRadius) {
                onPlayerEnterOrbit(player, distSq, physicalRadius);
            }
        }
    }

    private void onPlayerEnterOrbit(EntityPlayer player, double distSq, double radius) {
        double dist = Math.sqrt(distSq);

        if (dist > radius) {
            double pullStrength = 1;
            player.motionX += (this.posX - player.posX) / dist * pullStrength;
            player.motionY += (this.posY - player.posY) / dist * pullStrength;
            player.motionZ += (this.posZ - player.posZ) / dist * pullStrength;
        }

        if (dist <= radius + 2.0) {
            handleLanding(player);
        }
    }

    private void handleLanding(EntityPlayer player) {
        TeleportHelper.teleportPlayerWithRiding((EntityPlayerMP) player,Integer.parseInt(data.getConfig().getID()), rand.nextDouble()* 1000F, rand.nextDouble()* 1000F, rand.nextDouble()* 1000F);

    }
    private void updateFromSim() {
        if(debugMode)this.setPosition(data.x/10.0F, data.y/10.0F - 1f, data.z/10.0F );
        else this.setPosition(data.x , data.y- 1f, data.z );
    }

    @Override
    protected void entityInit() {}
    @Override
    protected void readEntityFromNBT(NBTTagCompound nbt) { this.bodyID = nbt.getString("bodyID"); }
    @Override
    protected void writeEntityToNBT(NBTTagCompound nbt) { nbt.setString("bodyID", bodyID); }
}