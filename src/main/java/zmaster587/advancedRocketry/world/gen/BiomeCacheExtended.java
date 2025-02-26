package zmaster587.advancedRocketry.world.gen;

import net.minecraft.server.MinecraftServer;
import net.minecraft.util.LongHashMap;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.biome.WorldChunkManager;
import org.jetbrains.annotations.NotNull;
import zmaster587.advancedRocketry.api.Configuration;

import java.util.ArrayList;
import java.util.List;

public class BiomeCacheExtended {
    /** Reference to the WorldChunkManager */
    private final WorldChunkManager chunkManager;
    /** The last time this BiomeCache was cleaned, in milliseconds. */
    private long lastCleanupTime;
    /** The map of keys to BiomeCacheBlocks. Keys are based on the chunk x, z coordinates as (x | z << 32). */
    private final @NotNull LongHashMap cacheMap = new LongHashMap();
    /** The list of cached BiomeCacheBlocks */
    private final List<BiomeCacheExtended.Block> cache = new ArrayList<>();
    private static final String __OBFID = "CL_00000162";

    public BiomeCacheExtended(WorldChunkManager p_i1973_1_)
    {
        this.chunkManager = p_i1973_1_;
    }

    /**
     * Returns a biome cache block at location specified.
     */
    public BiomeCacheExtended.Block getBiomeCacheBlock(int p_76840_1_, int p_76840_2_)
    {
        p_76840_1_ >>= 4;
        p_76840_2_ >>= 4;
        long k = (long)p_76840_1_ & 4294967295L | ((long)p_76840_2_ & 4294967295L) << 32;
        BiomeCacheExtended.Block block = (BiomeCacheExtended.Block)this.cacheMap.getValueByKey(k);

        if (block == null)
        {
            block = new BiomeCacheExtended.Block(p_76840_1_, p_76840_2_);
            this.cacheMap.add(k, block);
            this.cache.add(block);
        }

        block.lastAccessTime = MinecraftServer.getSystemTimeMillis();
        return block;
    }

    /**
     * Returns the BiomeGenBase related to the x, z position from the cache.
     */
    public BiomeGenBase getBiomeGenAt(int p_76837_1_, int p_76837_2_)
    {
        return this.getBiomeCacheBlock(p_76837_1_, p_76837_2_).getBiomeGenAt(p_76837_1_, p_76837_2_);
    }

    /**
     * Removes BiomeCacheBlocks from this cache that haven't been accessed in at least 30 seconds.
     */
    public void cleanupCache()
    {
        long i = MinecraftServer.getSystemTimeMillis();
        long j = i - this.lastCleanupTime;

        if (j > 7500L || j < 0L)
        {
            this.lastCleanupTime = i;

            for (int k = 0; k < this.cache.size(); ++k)
            {
            	BiomeCacheExtended.Block block = this.cache.get(k);
                long l = i - block.lastAccessTime;

                if (l > 30000L || l < 0L)
                {
                    this.cache.remove(k--);
                    long i1 = (long)block.xPosition & 4294967295L | ((long)block.zPosition & 4294967295L) << 32;
                    this.cacheMap.remove(i1);
                }
            }
        }
    }

    /**
     * Returns the array of cached biome types in the BiomeCacheBlock at the given location.
     */
    public BiomeGenBase[] getCachedBiomes(int p_76839_1_, int p_76839_2_)
    {
        return this.getBiomeCacheBlock(p_76839_1_, p_76839_2_).biomes;
    }

    public class Block
    {
        /** An array of chunk rainfall values saved by this cache. */
        public final float[] rainfallValues = new float[Configuration.maxBiomes];
        /** The array of biome types stored in this BiomeCacheBlock. */
        public final BiomeGenBase[] biomes = new BiomeGenBase[Configuration.maxBiomes];
        /** The x coordinate of the BiomeCacheBlock. */
        public final int xPosition;
        /** The z coordinate of the BiomeCacheBlock. */
        public final int zPosition;
        /** The last time this BiomeCacheBlock was accessed, in milliseconds. */
        public long lastAccessTime;
        private static final String __OBFID = "CL_00000163";

        public Block(int p_i1972_2_, int p_i1972_3_)
        {
            this.xPosition = p_i1972_2_;
            this.zPosition = p_i1972_3_;
            BiomeCacheExtended.this.chunkManager.getRainfall(this.rainfallValues, p_i1972_2_ << 4, p_i1972_3_ << 4, 16, 16);
            BiomeCacheExtended.this.chunkManager.getBiomeGenAt(this.biomes, p_i1972_2_ << 4, p_i1972_3_ << 4, 16, 16, false);
        }
        
        /**
         * Returns the BiomeGenBase related to the x, z position from the cache block.
         */
        public BiomeGenBase getBiomeGenAt(int p_76885_1_, int p_76885_2_)
        {
            return this.biomes[p_76885_1_ & 15 | (p_76885_2_ & 15) << 4];
        }
    }
}
