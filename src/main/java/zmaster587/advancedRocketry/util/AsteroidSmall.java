package zmaster587.advancedRocketry.util;

import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import org.jetbrains.annotations.NotNull;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public class AsteroidSmall {
	public String ID;
	public int distance;					//distance from the star, impacts fuelcost
	public int mass;						//factor of the amount of material total
	public int minLevel;					//Minimum level of the telescope required
	public float massVariability;			//variability of mass
	public float richness;					//factor of the ratio of ore to stone
	public float richnessVariability;		//variability of richness
	public float probability;				//probability of the asteroid spawning
	public float timeMultiplier;
	public final List<Float> stackProbabilites;	//
	public final List<ItemStack> itemStacks;
	public static final Random rand = new Random();
	private static final int precision = 1000;
	
	
	public AsteroidSmall() {
		stackProbabilites = new LinkedList<>();
		itemStacks = new LinkedList<>();
	}
	
	public String getName() {return ID;}
	public int getDistance() { return distance; }
	public int getMass() {return mass;}
	public int getMinLevel() {return minLevel;}
	public float getProbability() {return probability;}

	public List<StackEntry> getHarvest(long seed) {
		return getHarvest(seed, 1);
	}
	
	
	/**
	 * @param seed to use in RNG
	 * @param uncertainty how uncertain the outcome should be, 1 is default settings, 0 is 100% known
	 * @return
	 */
	public List<StackEntry> getHarvest(long seed, float uncertainty) {
		
		@NotNull List<StackEntry> entries = new LinkedList<>();
		rand.setSeed(seed);
		
		int myMass = (int)(mass + ((rand.nextFloat()*massVariability)*mass) - massVariability*mass/2f);
		int numOres = (int) (myMass*(richness + rand.nextFloat()*richnessVariability - richnessVariability/2f));
		
		StackEntry entry = new StackEntry();
		entry.stack = new ItemStack(Blocks.cobblestone, myMass - numOres);
		entry.variablility = (int)(uncertainty*entry.stack.stackSize);
		entry.midpoint =  (int)(entry.variablility*rand.nextFloat() - uncertainty*entry.variablility/2f);
		
		if(entry.midpoint + myMass - numOres < entry.variablility)
			entry.midpoint = entry.variablility;
		else
			entry.midpoint += myMass - numOres;
		entries.add(entry);
		
		int[] ores = new int[itemStacks.size()];
		
		float normFactor = 0;
		for(Float prob : stackProbabilites)
			normFactor += prob;
		 //normFactor /= stackProbabilites.size(); 
		
		for(int i = 0; i < numOres; i++) {
			
			float probability = rand.nextFloat();
			
			float currentLocation = 0;
			int counter = 0;
			Item item = null;
			
			for(Float prob : stackProbabilites) {
				if(probability <= currentLocation + prob/normFactor && currentLocation <= probability) {
                    itemStacks.get(counter).getItem();
                    break;
				}
				
				counter++;
				currentLocation += prob/normFactor;
			}
			
			if(counter < itemStacks.size())
				ores[counter]++;
		}
		
		for(int i = 0; i < itemStacks.size(); i++) {
			
			int num = ores[i];
			
			if(num == 0)
				continue;
			
			ItemStack stack = new ItemStack(itemStacks.get(i).getItem(), num, itemStacks.get(i).getItemDamage());
			entry = new StackEntry();
			
			entry.stack = stack;
			entry.variablility = (int) (uncertainty*rand.nextFloat()*num);
			
			entry.midpoint =  (int)(entry.variablility*rand.nextFloat() - entry.variablility/2f);
			
			if(entry.midpoint + num < entry.variablility)
				entry.midpoint = entry.variablility;
			else
				entry.midpoint += num;
			
			entries.add(entry);
		}
		
		return entries;
	}
	
	
	public static class StackEntry {
		public ItemStack stack;
		public int variablility;
		public int midpoint;
		public boolean isKnown;
	}
}
