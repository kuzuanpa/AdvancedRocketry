package zmaster587.advancedRocketry.util;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fluids.IFluidBlock;
import org.jetbrains.annotations.NotNull;
import zmaster587.advancedRocketry.api.AdvancedRocketryBlocks;
import zmaster587.advancedRocketry.api.atmosphere.IAtmosphereSealHandler;
import zmaster587.libVulpes.util.BlockPosition;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

/**
 * Handler for checking if blocks can be used to deal a room.
 * <p/>
 * Created by Dark(DarkGuardsman, Robert) on 1/6/2016.
 */
public final class SealableBlockHandler implements IAtmosphereSealHandler
{
    /** List of blocks not allowed. */
    private final List<Block> blockBanList = new ArrayList<>();
    /** List of blocks that are allowed regardless of properties. */
    private final List<Block> blockAllowList = new ArrayList<>();
    /** List of block materials not allowed. */
    private final List<Material> materialBanList = new ArrayList<>();
    /** List of block materials that are allowed regardless of properties. */
    private final List<Material> materialAllowList = new ArrayList<>();
    
    private final @NotNull HashSet<BlockPosition> doorPositions = new HashSet<>();
    //TODO add meta support
    //TODO add complex logic support threw API interface
    //TODO add complex logic handler for integration support

    /** INSTANCE */
    public static final SealableBlockHandler INSTANCE = new SealableBlockHandler();

    private SealableBlockHandler() {}

    @Override
    public boolean isBlockSealed(World world, int x, int y, int z)
    {
        return isBlockSealed(world, new BlockPosition(x, y, z));
    }

    /**
     * Checks to see if the block at the location can be sealed
     * @param world
     * @param pos
     * @return
     */
    public boolean isBlockSealed(World world, BlockPosition pos)
    {
        //Ensure we are not checking outside of the map
        if(pos.y >= 0 && pos.y <= 256)
        {
            //Prevents orphan chunk loading - DarkGuardsman
            if(world instanceof WorldServer && !((WorldServer) world).theChunkProviderServer.chunkExists(pos.x >> 4, pos.z >> 4))
            {
                return false;
            }

            Block block = world.getBlock(pos.x, pos.y, pos.z);
            int meta = world.getBlockMetadata(pos.x, pos.y, pos.z);
            Material material = block.getMaterial();

            //Always allow list
            if (blockAllowList.contains(block) || materialAllowList.contains(material))
            {
                return true;
            }
            //Always block list
            else if (blockBanList.contains(block) || materialBanList.contains(material))
            {
                return false;
            }
            else if (material.isLiquid() || !material.isSolid())
            {
                return false;
            }
            else if (world.isAirBlock(pos.x, pos.y, pos.z) || block instanceof IFluidBlock)
            {
                return false;
            }
            //TODO replace with seal logic handler
            else if (block == AdvancedRocketryBlocks.blockAirLock)
            {
				
				if(doorPositions.contains(pos))
					return true;
				doorPositions.add(pos);
				
				boolean doorIsSealed = checkDoorIsSealed(world, pos, meta);
				doorPositions.remove(pos);
				return doorIsSealed;
            }
            //TODO add is side solid check, which will require forge direction or side check. Eg more complex logic...
            return isFulBlock(world, pos);
        }
        return false;
    }

    @Override
    public void addUnsealableBlock(Block block)
    {
        if (!blockBanList.contains(block))
        {
            blockBanList.add(block);
        }
        blockAllowList.remove(block);
    }

    @Override
    public void addSealableBlock(Block block)
    {
        if (!blockAllowList.contains(block))
        {
            blockAllowList.add(block);
        }
        blockBanList.remove(block);
    }

    /**
     * Checks if a block is full sized based off of block bounds. This
     * is not a perfect check as mods may have a full size. However,
     * have a 3D model that doesn't look a full block in size. There
     * is no way around this other than to make a black list check.
     *
     * @param world - world
     * @param pos   - location
     * @return true if full block
     */
    public static boolean isFulBlock(World world, @NotNull BlockPosition pos)
    {
        return isFulBlock(world, world.getBlock(pos.x, pos.y, pos.z), pos);
    }

    /**
     * Checks if a block is full sized based off of collision bounds. This
     * is not a perfect check as some mods may have blocks that can be walked through.
     * But should be air-tight like forcefields
     *
     * @param block - block to compare
     * @return true if full block
     */
    public static boolean isFulBlock(World world, Block block, @NotNull BlockPosition pos)
    {
    	AxisAlignedBB bb = block.getCollisionBoundingBoxFromPool(world, pos.x, pos.y, pos.z);
    	
    	if(bb == null)
    		return false;
    	
        //size * 100 to correct rounding errors
        int minX = (int) ((bb.minX - pos.x) * 100);
        int minY = (int) ((bb.minY - pos.y) * 100);
        int minZ = (int) ((bb.minZ - pos.z) * 100);
        int maxX = (int) ((bb.maxX - pos.x) * 100);
        int maxY = (int) ((bb.maxY - pos.y) * 100);
        int maxZ = (int) ((bb.maxZ - pos.z) * 100);

        return minX == 0 && minY == 0 && minZ == 0 && maxX == 100 && maxY == 100 && maxZ == 100;
    }

    //TODO unit test, document, cleanup
    private boolean checkDoorIsSealed(World world, BlockPosition pos, int meta)
    {
        //TODO: door corners
        return ((meta & 8) == 8
                ||
                ((meta & 4) >> 2 == (meta & 1) && checkDoorSeal(world, pos.getPositionAtOffset(0, 0, 1), meta)
                        && checkDoorSeal(world, pos.getPositionAtOffset(0, 0, -1), meta))
                ||
                (meta & 4) >> 2 != (meta & 1) && checkDoorSeal(world, pos.getPositionAtOffset(1, 0, 0), meta)
                        && checkDoorSeal(world, pos.getPositionAtOffset(-1, 0, 0), meta));
    }

    //TODO unit test, document, cleanup
    private boolean checkDoorSeal(World world, @NotNull BlockPosition pos, int meta)
    {
        Block otherBlock = world.getBlock(pos.x, pos.y, pos.z);
        int otherMeta = world.getBlockMetadata(pos.x, pos.y, pos.z);

        return (otherBlock == AdvancedRocketryBlocks.blockAirLock && (otherMeta & 1) == (meta & 1)) ||
                (otherBlock != AdvancedRocketryBlocks.blockAirLock && isBlockSealed(world, pos));
    }

    /**
     * Checks if the block is banned from being a seal
     *
     * @param block - checked block
     * @return true if it is banned, based on ban list only.
     */
    public boolean isBlockBanned(Block block)
    {
        return blockBanList.contains(block);
    }

    /**
     * Checks if the material is banned from being a seal
     *
     * @param mat - material being checked
     * @return true if it is banned, based on ban list only.
     */
    public boolean isMaterialBanned(Material mat)
    {
        return materialBanList.contains(mat);
    }

    /**
     * Loads defaults..
     */
    public void loadDefaultData()
    {
        materialBanList.add(Material.air);
        materialBanList.add(Material.cactus);
        materialBanList.add(Material.craftedSnow);
        materialBanList.add(Material.fire);
        materialBanList.add(Material.leaves);
        materialBanList.add(Material.portal);
        materialBanList.add(Material.vine);
        materialBanList.add(Material.plants);
        materialBanList.add(Material.coral);
        materialBanList.add(Material.web);
        materialBanList.add(Material.sponge);
        materialBanList.add(Material.sand);

        //TODO check each vanilla block
    }
}
