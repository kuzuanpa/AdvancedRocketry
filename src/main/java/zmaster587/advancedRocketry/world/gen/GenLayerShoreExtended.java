package zmaster587.advancedRocketry.world.gen;

import zmaster587.advancedRocketry.api.AdvancedRocketryBiomes;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.biome.BiomeGenJungle;
import net.minecraft.world.biome.BiomeGenMesa;
import net.minecraft.world.gen.layer.GenLayer;
import net.minecraft.world.gen.layer.IntCache;
import net.minecraftforge.common.BiomeManager;

public class GenLayerShoreExtended extends GenLayerExtendedCompare {

	private static final String __OBFID = "CL_00000568";

	public GenLayerShoreExtended(long p_i2130_1_, GenLayer p_i2130_3_)
	{
		super(p_i2130_1_);
		this.parent = p_i2130_3_;
	}

	/**
	 * Returns a list of integer values generated by this layer. These may be interpreted as temperatures, rainfall
	 * amounts, or biomeList[] indices based on the particular GenLayer subclass.
	 */
	public int[] getInts(int p_75904_1_, int p_75904_2_, int p_75904_3_, int p_75904_4_)
	{
		int[] aint = this.parent.getInts(p_75904_1_ - 1, p_75904_2_ - 1, p_75904_3_ + 2, p_75904_4_ + 2);
		int[] aint1 = IntCache.getIntCache(p_75904_3_ * p_75904_4_);

		for (int i1 = 0; i1 < p_75904_4_; ++i1)
		{
			for (int j1 = 0; j1 < p_75904_3_; ++j1)
			{
				this.initChunkSeed(j1 + p_75904_1_, i1 + p_75904_2_);
				int k1 = aint[j1 + 1 + (i1 + 1) * (p_75904_3_ + 2)];
				BiomeGenBase biomegenbase = AdvancedRocketryBiomes.instance.getBiomeById(k1);
				int l1;
				int i2;
				int j2;
				int k2;

				if (k1 == BiomeGenBase.mushroomIsland.biomeID)
				{
					l1 = aint[j1 + 1 + (i1 + 1 - 1) * (p_75904_3_ + 2)];
					i2 = aint[j1 + 1 + 1 + (i1 + 1) * (p_75904_3_ + 2)];
					j2 = aint[j1 + 1 - 1 + (i1 + 1) * (p_75904_3_ + 2)];
					k2 = aint[j1 + 1 + (i1 + 1 + 1) * (p_75904_3_ + 2)];

					if (l1 != BiomeGenBase.ocean.biomeID && i2 != BiomeGenBase.ocean.biomeID && j2 != BiomeGenBase.ocean.biomeID && k2 != BiomeGenBase.ocean.biomeID)
					{
						aint1[j1 + i1 * p_75904_3_] = k1;
					}
					else
					{
						aint1[j1 + i1 * p_75904_3_] = BiomeGenBase.mushroomIslandShore.biomeID;
					}
				}
				else if (biomegenbase != null && biomegenbase.getBiomeClass() == BiomeGenJungle.class)
				{
					l1 = aint[j1 + 1 + (i1 + 1 - 1) * (p_75904_3_ + 2)];
					i2 = aint[j1 + 1 + 1 + (i1 + 1) * (p_75904_3_ + 2)];
					j2 = aint[j1 + 1 - 1 + (i1 + 1) * (p_75904_3_ + 2)];
					k2 = aint[j1 + 1 + (i1 + 1 + 1) * (p_75904_3_ + 2)];

					if (this.func_151631_c(l1) && this.func_151631_c(i2) && this.func_151631_c(j2) && this.func_151631_c(k2))
					{
						if (!isBiomeOceanic(l1) && !isBiomeOceanic(i2) && !isBiomeOceanic(j2) && !isBiomeOceanic(k2))
						{
							aint1[j1 + i1 * p_75904_3_] = k1;
						}
						else
						{
							aint1[j1 + i1 * p_75904_3_] = BiomeGenBase.beach.biomeID;
						}
					}
					else
					{
						aint1[j1 + i1 * p_75904_3_] = BiomeGenBase.jungleEdge.biomeID;
					}
				}
				else if (k1 != BiomeGenBase.extremeHills.biomeID && k1 != BiomeGenBase.extremeHillsPlus.biomeID && k1 != BiomeGenBase.extremeHillsEdge.biomeID)
				{
					if (biomegenbase != null && biomegenbase.func_150559_j())
					{
						this.func_151632_a(aint, aint1, j1, i1, p_75904_3_, k1, BiomeGenBase.coldBeach.biomeID);
					}
					else if (k1 != BiomeGenBase.mesa.biomeID && k1 != BiomeGenBase.mesaPlateau_F.biomeID)
					{
						if (k1 != BiomeGenBase.ocean.biomeID && k1 != BiomeGenBase.deepOcean.biomeID && k1 != BiomeGenBase.river.biomeID && k1 != BiomeGenBase.swampland.biomeID)
						{
							l1 = aint[j1 + 1 + (i1 + 1 - 1) * (p_75904_3_ + 2)];
							i2 = aint[j1 + 1 + 1 + (i1 + 1) * (p_75904_3_ + 2)];
							j2 = aint[j1 + 1 - 1 + (i1 + 1) * (p_75904_3_ + 2)];
							k2 = aint[j1 + 1 + (i1 + 1 + 1) * (p_75904_3_ + 2)];

							if (!isBiomeOceanic(l1) && !isBiomeOceanic(i2) && !isBiomeOceanic(j2) && !isBiomeOceanic(k2))
							{
								aint1[j1 + i1 * p_75904_3_] = k1;
							}
							else
							{
								aint1[j1 + i1 * p_75904_3_] = BiomeGenBase.beach.biomeID;
							}
						}
						else
						{
							aint1[j1 + i1 * p_75904_3_] = k1;
						}
					}
					else
					{
						l1 = aint[j1 + 1 + (i1 + 1 - 1) * (p_75904_3_ + 2)];
						i2 = aint[j1 + 1 + 1 + (i1 + 1) * (p_75904_3_ + 2)];
						j2 = aint[j1 + 1 - 1 + (i1 + 1) * (p_75904_3_ + 2)];
						k2 = aint[j1 + 1 + (i1 + 1 + 1) * (p_75904_3_ + 2)];

						if (!isBiomeOceanic(l1) && !isBiomeOceanic(i2) && !isBiomeOceanic(j2) && !isBiomeOceanic(k2))
						{
							if (this.func_151633_d(l1) && this.func_151633_d(i2) && this.func_151633_d(j2) && this.func_151633_d(k2))
							{
								aint1[j1 + i1 * p_75904_3_] = k1;
							}
							else
							{
								aint1[j1 + i1 * p_75904_3_] = BiomeGenBase.desert.biomeID;
							}
						}
						else
						{
							aint1[j1 + i1 * p_75904_3_] = k1;
						}
					}
				}
				else
				{
					this.func_151632_a(aint, aint1, j1, i1, p_75904_3_, k1, BiomeGenBase.stoneBeach.biomeID);
				}
			}
		}

		return aint1;
	}

	private void func_151632_a(int[] p_151632_1_, int[] p_151632_2_, int p_151632_3_, int p_151632_4_, int p_151632_5_, int p_151632_6_, int p_151632_7_)
	{
		if (isBiomeOceanic(p_151632_6_))
		{
			p_151632_2_[p_151632_3_ + p_151632_4_ * p_151632_5_] = p_151632_6_;
		}
		else
		{
			int j1 = p_151632_1_[p_151632_3_ + 1 + (p_151632_4_ + 1 - 1) * (p_151632_5_ + 2)];
			int k1 = p_151632_1_[p_151632_3_ + 1 + 1 + (p_151632_4_ + 1) * (p_151632_5_ + 2)];
			int l1 = p_151632_1_[p_151632_3_ + 1 - 1 + (p_151632_4_ + 1) * (p_151632_5_ + 2)];
			int i2 = p_151632_1_[p_151632_3_ + 1 + (p_151632_4_ + 1 + 1) * (p_151632_5_ + 2)];

			if (!isBiomeOceanic(j1) && !isBiomeOceanic(k1) && !isBiomeOceanic(l1) && !isBiomeOceanic(i2))
			{
				p_151632_2_[p_151632_3_ + p_151632_4_ * p_151632_5_] = p_151632_6_;
			}
			else
			{
				p_151632_2_[p_151632_3_ + p_151632_4_ * p_151632_5_] = p_151632_7_;
			}
		}
	}

	private boolean func_151631_c(int p_151631_1_)
	{
		return AdvancedRocketryBiomes.instance.getBiomeById(p_151631_1_) != null && AdvancedRocketryBiomes.instance.getBiomeById(p_151631_1_).getBiomeClass() == BiomeGenJungle.class || p_151631_1_ == BiomeGenBase.jungleEdge.biomeID || p_151631_1_ == BiomeGenBase.jungle.biomeID || p_151631_1_ == BiomeGenBase.jungleHills.biomeID || p_151631_1_ == BiomeGenBase.forest.biomeID || p_151631_1_ == BiomeGenBase.taiga.biomeID || isBiomeOceanic(p_151631_1_);
	}

	private boolean func_151633_d(int p_151633_1_)
	{
		return AdvancedRocketryBiomes.instance.getBiomeById(p_151633_1_) != null && AdvancedRocketryBiomes.instance.getBiomeById(p_151633_1_) instanceof BiomeGenMesa;
	}
	
    protected static boolean isBiomeOceanic(int p_151618_0_)
    {
        return BiomeManager.oceanBiomes.contains(AdvancedRocketryBiomes.instance.getBiomeById(p_151618_0_));
    }
}
