package zmaster587.advancedRocketry.block;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fluids.BlockFluidClassic;
import net.minecraftforge.fluids.Fluid;
import org.jetbrains.annotations.NotNull;

public class BlockFluid extends BlockFluidClassic {

    @SideOnly(Side.CLIENT)
    protected IIcon stillIcon;
    @SideOnly(Side.CLIENT)
    protected IIcon flowingIcon;
    
    final Fluid myFluid;
	
	public BlockFluid(Fluid fluid, Material material) {
		super(fluid, material);
		myFluid = fluid;
	}
    @Override
    public IIcon getIcon(int side, int meta) {
            return (side == 0 || side == 1)? stillIcon : flowingIcon;
    }
   
    @SideOnly(Side.CLIENT)
    @Override
    public void registerBlockIcons(@NotNull IIconRegister register) {
            stillIcon = register.registerIcon("advancedrocketry:fluid/oxygen_still");
            flowingIcon = register.registerIcon("advancedrocketry:fluid/oxygen_flow");
            myFluid.setIcons(stillIcon, flowingIcon);
    }
   
    @Override
    public int getBlockColor() {
    	return myFluid.getColor();
    }
    
    
    
    @Override
    public int getRenderColor(int p_149741_1_) {
    	return getBlockColor();
    }
	
	@Override
	public int colorMultiplier(IBlockAccess access, int x, int y, int z) {
		return getBlockColor();
	}
    
    @Override
    public boolean canDisplace(IBlockAccess world, int x, int y, int z) {
            if (world.getBlock(x,  y,  z).getMaterial().isLiquid()) return false;
            return super.canDisplace(world, x, y, z);
    }
   
    @Override
    public boolean displaceIfPossible(World world, int x, int y, int z) {
            if (world.getBlock(x,  y,  z).getMaterial().isLiquid()) return false;
            return super.displaceIfPossible(world, x, y, z);
    }
}
