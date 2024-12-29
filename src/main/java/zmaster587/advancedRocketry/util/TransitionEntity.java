package zmaster587.advancedRocketry.util;

import zmaster587.libVulpes.util.BlockPosition;
import net.minecraft.entity.Entity;

public class TransitionEntity {
	final long time;
	public final Entity entity;
	public final int dimId;
	public final BlockPosition location;
	public final Entity entity2; //the mount
	
	public TransitionEntity(long time, Entity entity, int dimId, BlockPosition location, Entity entity2 ) {
		this.time = time;
		this.entity = entity;
		this.dimId = dimId;
		this.location = location;
		this.entity2 = entity2;
	}
}
