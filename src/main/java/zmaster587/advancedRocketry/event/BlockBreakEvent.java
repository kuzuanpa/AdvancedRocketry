package zmaster587.advancedRocketry.event;

import cpw.mods.fml.common.eventhandler.Cancelable;
import cpw.mods.fml.common.eventhandler.Event;


@Cancelable
public class BlockBreakEvent extends Event {
	
	final int posX;
    final int posY;
    final int posZ;
	
	public BlockBreakEvent(int x, int y, int z) {
		posX = x;
		posY = y;
		posZ = z;
	}
	
	
	public static class LaserBreakEvent extends BlockBreakEvent{
		public LaserBreakEvent(int x, int y, int z) {
			super(x,y,z);
		}
		
		public int getX() {return posX;}

        public int getY() {return posY;}

        public int getZ() {return posZ;}
    }
}
