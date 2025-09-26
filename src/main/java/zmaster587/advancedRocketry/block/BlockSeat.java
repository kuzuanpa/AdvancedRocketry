package zmaster587.advancedRocketry.block;

import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import zmaster587.advancedRocketry.api.AdvancedRocketryBlocks;
import zmaster587.advancedRocketry.entity.EntityDummy;
import zmaster587.libVulpes.block.BlockRotatableModel;

import java.util.List;

public class BlockSeat extends BlockRotatableModel {

	public BlockSeat(Material par2Material, int modelId) {
		super(par2Material, modelId);
	}
	@Override
	public int onBlockPlaced(World world, int x, int y, int z, int side, float p_149660_6_, float p_149660_7_, float p_149660_8_, int p_149660_9_) {
		super.onBlockPlaced(world, x, y, z, side, p_149660_6_, p_149660_7_, p_149660_8_, p_149660_9_);
		world.setBlock(x,y+1,z, AdvancedRocketryBlocks.blockGenericSeatUp);
		return p_149660_9_;
	}

	@Override
	public AxisAlignedBB getCollisionBoundingBoxFromPool(World world, int x, int y, int z) {
		return AxisAlignedBB.getBoundingBox(x + this.minX, y + this.minY, z + this.minZ, x + this.maxX, y + 0.1F, z + this.maxZ);
	}

	//If the block is destroyed remove any mounting associated with it
	@Override
	public void onBlockPreDestroy(World world, int x,
			int y, int z, int meta) {
		super.onBlockPreDestroy(world, x, y, z,	meta);
		
		List<Entity> list = world.getEntitiesWithinAABB(EntityDummy.class,AxisAlignedBB.getBoundingBox(x, y, z, x+1, y+1, z+1));

		//We only expect one but just be sure
		for(Entity e : list) {
			if(e instanceof EntityDummy) {
				e.setDead();
			}
		}
		if(world.getBlock(x,y+1,z) instanceof BlockSeatUp)world.setBlockToAir(x,y+1,z);
	}

	@Override
	public boolean onBlockActivated(@NotNull World world, int x, int y, int z, @NotNull EntityPlayer player, int a, float b, float c, float d) {
		if(!world.isRemote) {
			List<Entity> list = world.getEntitiesWithinAABBExcludingEntity(player, AxisAlignedBB.getBoundingBox(x, y, z, x+1, y+1, z+1));

			//Try to mount player to dummy entity in the block
			for(Entity e : list) {
				if(e instanceof EntityDummy) {
                    if (e.riddenByEntity == null) {
                        //Ensure that the entity is in the correct position
                        e.setPosition(x + 0.5d, y + 0.2d, z + 0.5d);
                        player.mountEntity(e);
                    }
                    return true;
                }
			}
			EntityDummy entity = new EntityDummy(world, x + 0.5d,y + 0.2d, z + 0.5d);
			world.spawnEntityInWorld(entity);
			player.mountEntity(entity);
		}

		return true;
	}
}
