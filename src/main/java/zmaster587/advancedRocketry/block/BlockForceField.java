package zmaster587.advancedRocketry.block;

import org.jetbrains.annotations.NotNull;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.common.util.ForgeDirection;

public class BlockForceField extends Block {

	public BlockForceField(@NotNull Material materialIn) {
		super(materialIn);
	}
	
    /**
     * Used to determine ambient occlusion and culling when rebuilding chunks for render
     */
	@Override
	public boolean isOpaqueCube() {
		return false;
	}
   
	@Override
	public boolean isBlockNormalCube() {
		return false;
	}
    
	@Override
	@SideOnly(Side.CLIENT)
	public int getRenderBlockPass() {
		return 1;
	}
	
	
	@Override
	@SideOnly(Side.CLIENT)
	public boolean shouldSideBeRendered(@NotNull IBlockAccess world,
                                        int x, int y, int z, int dir) {
		ForgeDirection dir2 = ForgeDirection.getOrientation(dir).getOpposite();
		return world.getBlock(x, y, z) != this && super.shouldSideBeRendered(world, x, y, z, dir);
	}  

}
