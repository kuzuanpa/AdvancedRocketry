package zmaster587.advancedRocketry.thread;

import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.tileentity.TileEntity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import zmaster587.advancedRocketry.AdvancedRocketry;
import zmaster587.advancedRocketry.block.rocket.ILeveledPartsDivider;
import zmaster587.advancedRocketry.tile.TileGuidanceComputer;
import zmaster587.advancedRocketry.util.StageLayout;
import zmaster587.advancedRocketry.util.StorageChunk;
import zmaster587.libVulpes.util.BlockPosition;

import java.util.ArrayDeque;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Computes which rocket stage each block of a rocket belongs to, off the main thread.
 *
 * The result is plain data ({@link StageLayout}) rather than {@link StorageChunk} copies: building a
 * StorageChunk constructs a WorldDummy, which grabs the main thread's Profiler and mutates the shared
 * static ProviderDummy, and calls writeToNBT/createAndLoadEntity on arbitrary mod tiles. None of that is
 * safe off-thread. Splitting the chunk itself happens on the main thread at the moment a stage separates.
 *
 * {@link #computeLayout} is a pure function of the block array, so the main thread can call it directly
 * when a result is needed before the worker gets to it. The thread is an optimization, not a correctness
 * requirement.
 */
public class RocketStructureThread extends Thread{
    @Override
    public synchronized void start() {
        this.setDaemon(true);
        super.start();
    }
    @Override
    public void run() {
        while (true){
            @Nullable Map.Entry<UUID, StorageChunk> task = nextTask();

            if(task == null) {
                try {
                    sleep(1);
                } catch (InterruptedException e) {
                    return;
                }
                continue;
            }

            boolean outputLog = false;
            if(outputLog)System.out.print("Calculating Rocket Structure for: "+task.getKey()+"\n");
            long i = System.nanoTime();
            //A malformed rocket must never take the worker down with it, it is a daemon shared by every rocket
            try {
                results.put(task.getKey(), computeLayout(task.getValue()));
            } catch (Throwable e) {
                AdvancedRocketry.logger.warn("Failed to compute rocket stage layout, treating the rocket as single stage", e);
                results.put(task.getKey(), StageLayout.EMPTY);
            } finally {
                tasks.remove(task.getKey());
            }
            if(outputLog)System.out.print("Calculation Complete, takes: "+(System.nanoTime()-i)+"ns\n");
        }
    }

    private final ConcurrentHashMap<UUID,StorageChunk> tasks = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<UUID,StageLayout> results = new ConcurrentHashMap<>();

    public RocketStructureThread(String name) {
        super(name);
    }

    private @Nullable Map.Entry<UUID, StorageChunk> nextTask() {
        for (Map.Entry<UUID, StorageChunk> entry : tasks.entrySet()) {
            return entry;
        }
        return null;
    }

    public void addATask(UUID id, StorageChunk entireRocket){
        results.remove(id);
        tasks.put(id,entireRocket);
    }
    public boolean isTaskCompleted(UUID id){
        return results.containsKey(id);
    }
    public @Nullable StageLayout getResult(UUID id){
        return results.get(id);
    }
    public @Nullable StageLayout getResultAndRemove(UUID id){
        return results.remove(id);
    }
    /**Drops any pending work for a rocket that no longer exists*/
    public void cancelTask(UUID id){
        tasks.remove(id);
        results.remove(id);
    }

    /**
     * Assigns a stage level to every block of the rocket. Level 0 is the group containing the guidance
     * computer; crossing a divider block increments the level, so higher levels sit further from the
     * computer and are discarded first. A divider belongs to the group that reached it.
     *
     * Pure function of the chunk's block array - safe to call from any thread.
     *
     * @return the layout, or {@link StageLayout#EMPTY} if the rocket has no guidance computer to start from
     */
    public static @NotNull StageLayout computeLayout(@NotNull StorageChunk entireRocket){
        List<TileEntity> computers = entireRocket.getTileEntityList().stream().filter(tile -> tile instanceof TileGuidanceComputer).collect(Collectors.toList());
        //Without a start point there is nothing to divide, this is the normal case for debris and satellite rockets
        if (computers.isEmpty()) return StageLayout.EMPTY;

        int sizeX = entireRocket.getSizeX(), sizeY = entireRocket.getSizeY(), sizeZ = entireRocket.getSizeZ();
        byte[] levels = new byte[sizeX * sizeY * sizeZ];

        TileEntity computer = computers.get(0);
        int maxLevel = findGroups(levels, entireRocket, computer.xCoord, computer.yCoord, computer.zCoord, false, 0);

        return new StageLayout(levels, Math.max(0, maxLevel), sizeX, sizeY, sizeZ);
    }

    /**
     * Marks the group reachable from the given position, then recurses across each divider it found.
     * Recursion depth is bounded by the number of dividers; the flood fill within a group is iterative.
     *
     * @return the highest level that actually contains blocks
     */
    private static int findGroups(byte[] levels, @NotNull StorageChunk entireRocket, int x, int y, int z, boolean startFromDivide, int level){
        //Insertion ordered: when a region is reachable from two dividers, whichever the fill met first claims
        //it. Hash order would make that depend on coordinates, and client and server must agree.
        final Set<BlockPosition> dividersFound = new LinkedHashSet<>();
        int assigned = findConnectedParts(levels, dividersFound, entireRocket, x, y, z, startFromDivide, level);

        //An empty group means the divider had nothing below it, so this level does not exist
        int maxLevel = assigned > 0 ? level : level - 1;

        //Recurse even when this group was empty, otherwise adjacent dividers silently drop everything below them
        for (BlockPosition pos : dividersFound) {
            maxLevel = Math.max(maxLevel, findGroups(levels, entireRocket, pos.x, pos.y, pos.z, true, level + 1));
        }
        return maxLevel;
    }

    /**
     * Iterative flood fill over one stage, stopping at divider blocks and recording them for the caller
     * to continue from. Iterative rather than recursive: a large rocket is tens of thousands of blocks and
     * per-block recursion overflows the stack.
     *
     * The levels array doubles as the visited set - a block already carries a non-zero level exactly when
     * some earlier fill claimed it.
     *
     * @return how many blocks were assigned to this level
     */
    private static int findConnectedParts(byte[] levels, Set<BlockPosition> dividersFound, @NotNull StorageChunk entireRocket, int x, int y, int z, boolean startFromDivide, int level){
        ArrayDeque<BlockPosition> queue = new ArrayDeque<>();
        queue.add(new BlockPosition(x, y, z));
        //When crossing a divider the start position is the divider itself, which already belongs to the group above
        boolean skipSelf = startFromDivide;
        int assigned = 0;

        while (!queue.isEmpty()) {
            BlockPosition pos = queue.poll();
            int idx = index(entireRocket, pos.x, pos.y, pos.z);

            if(!skipSelf) {
                if(levels[idx] != 0) continue;

                levels[idx] = (byte) (level + 1);
                assigned++;

                //A divider terminates this stage, the blocks beyond it form the next one
                if(entireRocket.getBlock(pos.x, pos.y, pos.z) instanceof ILeveledPartsDivider) {
                    dividersFound.add(pos);
                    continue;
                }
            }
            skipSelf = false;

            enqueueIfSolid(queue, entireRocket, pos.x + 1, pos.y, pos.z);
            enqueueIfSolid(queue, entireRocket, pos.x - 1, pos.y, pos.z);
            enqueueIfSolid(queue, entireRocket, pos.x, pos.y + 1, pos.z);
            enqueueIfSolid(queue, entireRocket, pos.x, pos.y - 1, pos.z);
            enqueueIfSolid(queue, entireRocket, pos.x, pos.y, pos.z + 1);
            enqueueIfSolid(queue, entireRocket, pos.x, pos.y, pos.z - 1);
        }
        return assigned;
    }

    /**getBlock returns air outside the chunk, so this doubles as the bounds check*/
    private static void enqueueIfSolid(ArrayDeque<BlockPosition> queue, @NotNull StorageChunk entireRocket, int x, int y, int z){
        Block block = entireRocket.getBlock(x, y, z);
        if(block != Blocks.air) queue.add(new BlockPosition(x, y, z));
    }

    /**Same addressing as {@link StorageChunk#writeToNBT}*/
    private static int index(@NotNull StorageChunk chunk, int x, int y, int z){
        return z + (chunk.getSizeZ() * y) + (chunk.getSizeZ() * chunk.getSizeY() * x);
    }
}
