package zmaster587.advancedRocketry.tile.multiblock.machine;

import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import zmaster587.advancedRocketry.inventory.TextureResources;
import zmaster587.libVulpes.api.LibVulpesBlocks;
import zmaster587.libVulpes.api.material.AllowedProducts;
import zmaster587.libVulpes.api.material.MaterialRegistry;
import zmaster587.libVulpes.interfaces.IRecipe;
import zmaster587.libVulpes.inventory.modules.IModularInventory;
import zmaster587.libVulpes.inventory.modules.ModuleBase;
import zmaster587.libVulpes.inventory.modules.ModuleProgress;
import zmaster587.libVulpes.recipe.RecipesMachine;
import zmaster587.libVulpes.tile.multiblock.TileMultiblockMachine;

public class TileLathe extends TileMultiblockMachine implements IModularInventory {

	public static final Object[][][] structure = { 
		{{'I', LibVulpesBlocks.motors, Blocks.air, 'c'}},
		{{'O', LibVulpesBlocks.blockStructureBlock, LibVulpesBlocks.blockStructureBlock, 'P'}},
	};
	
	@Override
	public void registerRecipes() {
		RecipesMachine.getInstance().addRecipe(TileLathe.class, MaterialRegistry.getItemStackFromMaterialAndType("Iron", AllowedProducts.getProductByName("STICK"), 2), 300, 100, "ingotIron");
	}
	
	@Override
	public Object[][][] getStructure() {
		return structure;
	}

	@Override
	public boolean shouldHideBlock(World world, int x, int y, int z, Block tile) {
		return true;
	}
	
	@Override
	protected float getTimeMultiplierForRecipe(IRecipe recipe) {
		return super.getTimeMultiplierForRecipe(recipe);
	}
	
	@Override
	public AxisAlignedBB getRenderBoundingBox() {
		return AxisAlignedBB.getBoundingBox(xCoord -3,yCoord -2, zCoord -3, xCoord + 3, yCoord + 2, zCoord + 3);
	}
	
	@Override
	public ResourceLocation getSound() {
		return TextureResources.sndLathe;
	}

	@Override
	public int getSoundDuration() {
		return 30;
	}

	@Override
	public @NotNull List<ModuleBase> getModules(int ID, EntityPlayer player) {
		List<ModuleBase> modules = super.getModules(ID, player);

		modules.add(new ModuleProgress(100, 40, 0, TextureResources.latheProgressBar, this));
		return modules;
	}

	@Override
	public @NotNull String getMachineName() {
		return "tile.lathe.name";
	}
}
