package zmaster587.advancedRocketry.entity;

import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.common.registry.IEntityAdditionalSpawnData;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;

/**
 * Legacy marker for a celestial body that used to be spawned into the space dimension as a collision proxy.
 *
 * Bodies are hundreds to thousands of blocks apart, far beyond any entity tracking range, so a proxy entity
 * could never stay in sync with the client.  Positions are now recomputed identically on both sides
 * ({@link zmaster587.advancedRocketry.dimension.sim.SimUniverse}) and arrival is a distance test done in
 * {@link zmaster587.advancedRocketry.dimension.sim.SpaceTravelHandler}.
 *
 * The class stays registered only so worlds saved with the old system still load; any instance removes itself.
 */
public class EntityCelestialBody extends Entity implements IEntityAdditionalSpawnData {

    private String bodyID = "";

    public EntityCelestialBody(World world) {
        super(world);
        this.ignoreFrustumCheck = true;
        this.isImmuneToFire = true;
        this.field_70135_K = true;
    }

    @Override
    public void writeSpawnData(ByteBuf buffer) {
        ByteBufUtils.writeUTF8String(buffer, bodyID == null ? "" : bodyID);
    }

    @Override
    public void readSpawnData(ByteBuf additionalData) {
        this.bodyID = ByteBufUtils.readUTF8String(additionalData);
    }

    @Override
    public void onUpdate() {
        if(!worldObj.isRemote) setDead();
    }

    @Override
    protected void entityInit() {}

    @Override
    protected void readEntityFromNBT(NBTTagCompound nbt) { this.bodyID = nbt.getString("bodyID"); }

    @Override
    protected void writeEntityToNBT(NBTTagCompound nbt) {
        //NBTTagString rejects null, which would break the whole region file on save
        nbt.setString("bodyID", bodyID == null ? "" : bodyID);
    }
}
