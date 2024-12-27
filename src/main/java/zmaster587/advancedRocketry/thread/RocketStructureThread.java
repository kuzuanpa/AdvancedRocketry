package zmaster587.advancedRocketry.thread;

import net.minecraft.init.Blocks;
import net.minecraft.tileentity.TileEntity;
import org.jetbrains.annotations.NotNull;
import zmaster587.advancedRocketry.block.rocket.ILeveledPartsDivider;
import zmaster587.advancedRocketry.entity.LeveledRocketPart;
import zmaster587.advancedRocketry.tile.TileGuidanceComputer;
import zmaster587.advancedRocketry.util.StorageChunk;
import zmaster587.libVulpes.util.BlockPosition;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class RocketStructureThread extends Thread{
    @Override
    public synchronized void start() {
        this.setDaemon(true);
        super.start();
    }
    @Override
    public void run() {
        while (true){
            if(tasks.isEmpty()) {
                try {
                    sleep(1);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }else {
                currentTaskID=tasks.keySet().iterator().next();
                if(outputLog)System.out.print("Calculating Rocket Structure for: "+currentTaskID+"\n");
                long i = System.nanoTime();
                calculateLeveledRocketParts(tasks.get(currentTaskID));
                if(outputLog)System.out.print("Calculation Complete, takes: "+(System.nanoTime()-i)+"ns\n");
            }
        }
    }

    private final boolean outputLog=false;
    private final HashMap<UUID,StorageChunk> tasks = new HashMap<>();
    private final HashMap<UUID,ArrayList<LeveledRocketPart>> results = new HashMap<>();
    private UUID currentTaskID;
    private final ArrayList<BlockPosition> searchedParts = new ArrayList<>();

    public RocketStructureThread(String name) {
        super(name);
    }

    public void addATask(UUID id, StorageChunk entireRocket){
        tasks.put(id,entireRocket);
    }
    public boolean isTaskCompleted(UUID id){
        return tasks.get(id) == null && results.get(id) != null;
    }
    public ArrayList<LeveledRocketPart> getResult(UUID id){
        return results.get(id);
    }
    public ArrayList<LeveledRocketPart> getResultAndRemove(UUID id){
        results.remove(id);
        return results.get(id);
    }
    public void calculateLeveledRocketParts(StorageChunk entireRocket){
        //search for control computer
        List<TileEntity> computers = entireRocket.getTileEntityList().stream().filter(tile -> tile instanceof TileGuidanceComputer).collect(Collectors.toList());
        if (computers.size()>1)throw new IllegalArgumentException("More than one control computer found in Rocket!");
        ArrayList<LeveledRocketPart> partList= new ArrayList<>();
        findGroups(partList,entireRocket,computers.get(0).xCoord,computers.get(0).yCoord,computers.get(0).zCoord,false);
        results.put(currentTaskID,partList);
        tasks.remove(currentTaskID);
    }

    private void findGroups(ArrayList<LeveledRocketPart> partList, @NotNull StorageChunk entireRocket, int x, int y, int z, boolean startFromDivide){
        final ArrayList<BlockPosition> list = new ArrayList<>();
        findConnectedParts(list,entireRocket, x,y,z,startFromDivide);
        //if we can't find any new parts, return
        if(list.size()==0||list.stream().allMatch(pos->entireRocket.getBlock(pos.x,pos.y,pos.z) instanceof ILeveledPartsDivider))return;

        partList.add(new LeveledRocketPart(StorageChunk.divideStorage(entireRocket, list), 0, false, 1));

        //This is a recursion to collect all possible groups
        list.stream().filter(pos->entireRocket.getBlock(pos.x,pos.y,pos.z) instanceof ILeveledPartsDivider).forEach(pos-> findGroups(partList,entireRocket, pos.x,pos.y,pos.z,true));
    }

    private void findConnectedParts(@NotNull ArrayList<BlockPosition> blockList, StorageChunk entireRocket, int x, int y, int z, boolean startFromDivide){
        if(searchedParts.contains(new BlockPosition(x, y, z))) return;
        if(!startFromDivide)blockList.add(new BlockPosition(x, y, z));
        if(!startFromDivide&&entireRocket.getBlock(x,y,z) instanceof ILeveledPartsDivider) return;

        searchedParts.add(new BlockPosition(x, y, z));
        if (entireRocket.getBlock(x + 1, y, z) != Blocks.air) findConnectedParts(blockList,entireRocket, x + 1, y, z,false);
        if (entireRocket.getBlock(x - 1, y, z) != Blocks.air) findConnectedParts(blockList,entireRocket, x - 1, y, z,false);
        if (entireRocket.getBlock(x, y + 1, z) != Blocks.air) findConnectedParts(blockList,entireRocket, x, y + 1, z,false);
        if (entireRocket.getBlock(x, y - 1, z) != Blocks.air) findConnectedParts(blockList,entireRocket, x, y - 1, z,false);
        if (entireRocket.getBlock(x, y, z + 1) != Blocks.air) findConnectedParts(blockList,entireRocket, x, y, z + 1,false);
        if (entireRocket.getBlock(x, y, z - 1) != Blocks.air) findConnectedParts(blockList,entireRocket, x, y, z - 1,false);
    }
}
