package zmaster587.advancedRocketry.world.gen;

import java.util.Random;

import org.jetbrains.annotations.NotNull;
import zmaster587.advancedRocketry.api.AdvancedRocketryBlocks;
import net.minecraft.block.Block;
import net.minecraft.block.BlockSapling;
import net.minecraft.init.Blocks;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.WorldGenAbstractTree;
import net.minecraftforge.common.util.ForgeDirection;

public class WorldGenAlienTree extends WorldGenAbstractTree {

	public WorldGenAlienTree(boolean p_i45461_1_)
	{
		super(p_i45461_1_);
	}
	
	
	// CanGrowInto
	@Override
	protected boolean func_150523_a(Block blockType) {
		return super.func_150523_a(blockType) || blockType == AdvancedRocketryBlocks.blockAlienSapling || blockType == AdvancedRocketryBlocks.blockAlienWood || blockType == AdvancedRocketryBlocks.blockAlienLeaves;
	}



	public boolean generate(@NotNull World world, Random random, int x, int y, int z)
	{
		int treeHeight = random.nextInt(10) + 20;
		boolean flag = true;

		//Make sure tree can generate
		if (y >= 1 && y + treeHeight + 1 <= 256)
		{
			int j1;
			int k1;

			for (int treeHeightIterator = y; treeHeightIterator <= y + 1 + treeHeight; ++treeHeightIterator)
			{
				byte xzIterator = 3;

				if (treeHeightIterator == y)
				{
					xzIterator = 0;
				}

				if (treeHeightIterator >= y + 1 + treeHeight - 2)
				{
					xzIterator = 3;
				}

				for (j1 = x - xzIterator; j1 <= x + xzIterator && flag; ++j1)
				{
					for (k1 = z - xzIterator; k1 <= z + xzIterator && flag; ++k1)
					{
						if (treeHeightIterator >= 0 && treeHeightIterator < 256)
						{
							Block block = world.getBlock(j1, treeHeightIterator, k1);

							if (!this.isReplaceable(world, j1, treeHeightIterator, k1))
							{
								flag = false;
							}
						}
						else
						{
							flag = false;
						}
					}
				}
			}

			if (!flag)
			{
				return false;
			}
			else //Actually generate tree
			{
				Block block2 = world.getBlock(x, y - 1, z);

				boolean isSoil = block2.canSustainPlant(world, x, y - 1, z, ForgeDirection.UP, (BlockSapling)Blocks.sapling);
				if (isSoil && y < 256 - treeHeight - 1)
				{
					//Throw events
					onPlantGrow(world, x,     y - 1, z,     x, y, z);
					onPlantGrow(world, x + 1, y - 1, z,     x, y, z);
					onPlantGrow(world, x + 1, y - 1, z + 1, x, y, z);
					onPlantGrow(world, x,     y - 1, z + 1, x, y, z);
					int j3 = random.nextInt(4);
                    random.nextInt(4);
                    int i2 = 0;
					int j2;
					int trunkY;

					for (j2 = 0; j2 < treeHeight; ++j2)
					{
						trunkY = y + j2;

						Block block1 = world.getBlock(x, trunkY, z);

						if (block1.isAir(world, x, trunkY, z) || block1.isLeaves(world, x, trunkY, z))
						{
							this.setBlockAndNotifyAdequately(world, x, trunkY, z, AdvancedRocketryBlocks.blockAlienWood, 1);
							this.setBlockAndNotifyAdequately(world, x + 1, trunkY, z, AdvancedRocketryBlocks.blockAlienWood, 1);
							this.setBlockAndNotifyAdequately(world, x, trunkY, z + 1, AdvancedRocketryBlocks.blockAlienWood, 1);
							this.setBlockAndNotifyAdequately(world, x + 1, trunkY, z + 1, AdvancedRocketryBlocks.blockAlienWood, 1);
							i2 = trunkY;
						}
					}

					//Genthe root
					this.setBlockAndNotifyAdequately(world, x - 1, y, z, AdvancedRocketryBlocks.blockAlienWood, 1);
					this.setBlockAndNotifyAdequately(world, x + 2, y, z, AdvancedRocketryBlocks.blockAlienWood, 1);
					this.setBlockAndNotifyAdequately(world, x + 2, y, z + 1, AdvancedRocketryBlocks.blockAlienWood, 1);
					this.setBlockAndNotifyAdequately(world, x - 1, y, z + 1, AdvancedRocketryBlocks.blockAlienWood, 1);
					
					this.setBlockAndNotifyAdequately(world, x, y, z - 1, AdvancedRocketryBlocks.blockAlienWood, 1);
					this.setBlockAndNotifyAdequately(world, x + 1, y, z - 1, AdvancedRocketryBlocks.blockAlienWood, 1);
					this.setBlockAndNotifyAdequately(world, x + 1, y, z + 2, AdvancedRocketryBlocks.blockAlienWood, 1);
					this.setBlockAndNotifyAdequately(world, x, y, z + 2, AdvancedRocketryBlocks.blockAlienWood, 1);
					
					generatePod(world, random, 6, x + 1, random.nextInt(10) + y + treeHeight / 6, z, 1, 1);
					generatePod(world, random, 6, x, random.nextInt(10) + y + treeHeight / 6, z + 1, -1, -1);
					generatePod(world, random, 6, x, random.nextInt(10) + y + treeHeight / 6, z + 1, -1, 1);
					generatePod(world, random, 6, x + 1, random.nextInt(10) + y + treeHeight / 6, z, 1, -1);
					
					generatePod(world, random, 6, x + 1, random.nextInt(10) + y + treeHeight / 6, z, 1, 0);
					generatePod(world, random, 6, x, random.nextInt(10) + y + treeHeight / 6, z + 1, -1, 0);
					generatePod(world, random, 6, x, random.nextInt(10) + y + treeHeight / 6, z + 1, 0, 1);
					generatePod(world, random, 6, x + 1, random.nextInt(10) + y + treeHeight / 6, z, 0, -1);
					
					generatePod(world, random, 3, x + 1, random.nextInt(5) + y + treeHeight-(treeHeight / 3), z, 1, 1);
					generatePod(world, random, 3, x, random.nextInt(5) + y + treeHeight-(treeHeight / 3), z + 1, -1, -1);
					generatePod(world, random, 3, x, random.nextInt(5) + y + treeHeight-(treeHeight / 3), z + 1, -1, 1);
					generatePod(world, random, 3, x + 1, random.nextInt(5) + y + treeHeight-(treeHeight / 3), z, 1, -1);
					
					generatePod(world, random, 3, x + 1, random.nextInt(5) + y + treeHeight-(treeHeight / 3), z, 1, 0);
					generatePod(world, random, 3, x, random.nextInt(5) + y + treeHeight- (treeHeight / 3), z + 1, -1, 0);
					generatePod(world, random, 3, x, random.nextInt(5) + y + treeHeight - (treeHeight / 3), z + 1, 0, 1);
					generatePod(world, random, 3, x + 1, random.nextInt(5) + y + treeHeight - (treeHeight / 3), z, 0, -1);

					for (j2 = -3; j2 <= 3; ++j2)
					{
						for (trunkY = -3; trunkY <= 1; ++trunkY)
						{
							byte b1 = -1;

							for(int c = 0; c < treeHeight - 4; c++) {
								int radius = Math.abs(x + j2) + Math.abs(z + trunkY);
								if( (c < treeHeight/3 && radius < 3 ) || ((c >= treeHeight/3) && radius < 4)){
									this.replaceAirWithLeaves(world, x + j2, i2 + b1 - c, z + trunkY);
									this.replaceAirWithLeaves(world, 1 + x - j2, i2 + b1 - c, z + trunkY);
									this.replaceAirWithLeaves(world, x + j2, i2 + b1 - c, 1 + z - trunkY);
									this.replaceAirWithLeaves(world, 1 + x - j2, i2 + b1 - c, 1 + z - trunkY);
								}
							}


							if ((j2 > -2 || trunkY > -1) && (j2 != -1 || trunkY != -2))
							{
								byte b2 = 1;
								this.replaceAirWithLeaves(world, x + j2, i2 + b2, z + trunkY);
								this.replaceAirWithLeaves(world, 1 + x - j2, i2 + b2, z + trunkY);
								this.replaceAirWithLeaves(world, x + j2, i2 + b2, 1 + z - trunkY);
								this.replaceAirWithLeaves(world, 1 + x - j2, i2 + b2, 1 + z - trunkY);
							}
						}
					}

					if (random.nextBoolean())
					{
						this.replaceAirWithLeaves(world, x, i2 + 2, z);
						this.replaceAirWithLeaves(world, x + 1, i2 + 2, z);
						this.replaceAirWithLeaves(world, x + 1, i2 + 2, z + 1);
						this.replaceAirWithLeaves(world, x, i2 + 2, z + 1);
					}

					for (j2 = -3; j2 <= 4; ++j2)
					{
						for (trunkY = -3; trunkY <= 4; ++trunkY)
						{
							if ((j2 != -3 || trunkY != -3) && (j2 != -3 || trunkY != 4) && (j2 != 4 || trunkY != -3) && (j2 != 4 || trunkY != 4) && (Math.abs(j2) < 3 || Math.abs(trunkY) < 3))
							{
								this.replaceAirWithLeaves(world, x + j2, i2, z + trunkY);
							}
						}
					}

					return true;
				}
				else
				{
					return false;
				}
			}
		}
		else
		{
			return false;
		}
	}

	private void generatePod(World world, Random random, int intitalDist, int x, int y, int z, int dirX, int dirZ) {
		int branchLength = random.nextInt(5) + intitalDist;

		int direction = (dirX != 0 && dirZ != 0) ? Math.abs(dirX)*4 : Math.abs(dirX)*4 + Math.abs(dirZ)*8;
		
		boolean flag = true;
		
		for(int l = 0; l < branchLength && flag; l++) {
			int newX = x + (dirX*l);
			int newY = l >= branchLength/2 ? y + 2 : y;
			int newZ = z + (dirZ*l);
			
				flag = this.replaceBlockWithWood(world, newX, newY, newZ, direction);
				flag = flag && this.replaceBlockWithWood(world, newX, newY - 1, newZ, direction);
				flag = flag && this.replaceBlockWithWood(world, newX + dirZ, newY, newZ + dirX, direction);
				flag = flag && this.replaceBlockWithWood(world, newX + dirZ, newY - 1, newZ + dirX, direction);
		}

		int radius = 4;

		for(int offX = -radius; offX < radius; offX++) {
			for(int offY = -radius; offY < radius; offY++) {
				for(int offZ = -radius; offZ < radius; offZ++) {
					if(offX*offX + offY*offY + offZ*offZ < radius*radius + 1) replaceAirWithLeaves(world, x + offX + (dirX*branchLength), y + offY + 1, z + offZ + (dirZ*branchLength));
				}
			}
		}

	}

	private boolean replaceBlockWithWood(World world, int x, int y, int z, int direction) {
		Block block = world.getBlock(x, y, z);
		
		if( block.isReplaceable(world, x, y, z) || block.isLeaves(world, x, y, z) || block == AdvancedRocketryBlocks.blockAlienWood) {
			this.setBlockAndNotifyAdequately(world, x, y, z, AdvancedRocketryBlocks.blockAlienWood, direction);
			return true;
		}
		else
			return false;
	}
	
	private void replaceAirWithLeaves(@NotNull World p_150526_1_, int p_150526_2_, int p_150526_3_, int p_150526_4_)
	{
		Block block = p_150526_1_.getBlock(p_150526_2_, p_150526_3_, p_150526_4_);

		if (block.isAir(p_150526_1_, p_150526_2_, p_150526_3_, p_150526_4_))
		{
			this.setBlockAndNotifyAdequately(p_150526_1_, p_150526_2_, p_150526_3_, p_150526_4_, AdvancedRocketryBlocks.blockAlienLeaves, 0);
		}
	}

	//Just a helper macro
	private void onPlantGrow(World world, int x, int y, int z, int sourceX, int sourceY, int sourceZ)
	{
		world.getBlock(x, y, z).onPlantGrow(world, x, y, z, sourceX, sourceY, sourceZ);
	}

}
