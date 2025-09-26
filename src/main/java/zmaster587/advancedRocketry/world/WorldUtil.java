package zmaster587.advancedRocketry.world;

import com.bioxx.tfc.api.TFCBlocks;
import gregapi.data.MT;
import gregapi.data.OP;
import gregapi.util.OM;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.world.WorldProvider;

import java.util.ArrayList;
import java.util.List;

public class WorldUtil {
	public WorldProvider getProviderForName(String name) {
		return null;
	}


	protected static final List<ItemStack> allValidCobbleStones = new ArrayList<>();

	public static int getAllValidCobbleStonesAmount() {
		return allValidCobbleStonesAmount;
	}

	protected static int allValidCobbleStonesAmount;
	public static List<ItemStack> getAllValidCobbleStones(){
		if(allValidCobbleStones.isEmpty()){
			allValidCobbleStones.add(new ItemStack(Blocks.cobblestone));
			allValidCobbleStones.add(new ItemStack(TFCBlocks.stoneMMCobble, 1, 0));
			allValidCobbleStones.add(new ItemStack(TFCBlocks.stoneMMCobble, 1, 1));
			allValidCobbleStones.add(new ItemStack(TFCBlocks.stoneMMCobble, 1, 2));
			allValidCobbleStones.add(new ItemStack(TFCBlocks.stoneMMCobble, 1, 3));
			allValidCobbleStones.add(new ItemStack(TFCBlocks.stoneMMCobble, 1, 4));
			allValidCobbleStones.add(new ItemStack(TFCBlocks.stoneMMCobble, 1, 5));
			allValidCobbleStones.add(new ItemStack(TFCBlocks.stoneSedCobble, 1, 0));
			allValidCobbleStones.add(new ItemStack(TFCBlocks.stoneSedCobble, 1, 1));
			allValidCobbleStones.add(new ItemStack(TFCBlocks.stoneSedCobble, 1, 2));
			allValidCobbleStones.add(new ItemStack(TFCBlocks.stoneSedCobble, 1, 3));
			allValidCobbleStones.add(new ItemStack(TFCBlocks.stoneSedCobble, 1, 4));
			allValidCobbleStones.add(new ItemStack(TFCBlocks.stoneSedCobble, 1, 5));
			allValidCobbleStones.add(new ItemStack(TFCBlocks.stoneSedCobble, 1, 6));
			allValidCobbleStones.add(new ItemStack(TFCBlocks.stoneSedCobble, 1, 7));
			allValidCobbleStones.add(new ItemStack(TFCBlocks.stoneIgExCobble, 1, 0));
			allValidCobbleStones.add(new ItemStack(TFCBlocks.stoneIgExCobble, 1, 1));
			allValidCobbleStones.add(new ItemStack(TFCBlocks.stoneIgExCobble, 1, 2));
			allValidCobbleStones.add(new ItemStack(TFCBlocks.stoneIgExCobble, 1, 3));
			allValidCobbleStones.add(new ItemStack(TFCBlocks.stoneIgInCobble, 1, 0));
			allValidCobbleStones.add(new ItemStack(TFCBlocks.stoneIgInCobble, 1, 1));
			allValidCobbleStones.add(new ItemStack(TFCBlocks.stoneIgInCobble, 1, 2));
			if(OM.get(OP.stoneCobble, MT.STONES.SpaceRock    , 1)!=null)allValidCobbleStones.add(OM.get(OP.stoneCobble, MT.STONES.SpaceRock    , 1));
			if(OM.get(OP.stoneCobble, MT.STONES.MoonRock     , 1)!=null)allValidCobbleStones.add(OM.get(OP.stoneCobble, MT.STONES.MoonRock     , 1));
			if(OM.get(OP.stoneCobble, MT.STONES.MoonTurf     , 1)!=null)allValidCobbleStones.add(OM.get(OP.stoneCobble, MT.STONES.MoonTurf     , 1));
			if(OM.get(OP.stoneCobble, MT.STONES.MarsRock     , 1)!=null)allValidCobbleStones.add(OM.get(OP.stoneCobble, MT.STONES.MarsRock     , 1));
			if(OM.get(OP.stoneCobble, MT.STONES.MarsSand     , 1)!=null)allValidCobbleStones.add(OM.get(OP.stoneCobble, MT.STONES.MarsSand     , 1));
			if(OM.get(OP.stoneCobble, MT.STONES.SkyStone     , 1)!=null)allValidCobbleStones.add(OM.get(OP.stoneCobble, MT.STONES.SkyStone     , 1));
			if(OM.get(OP.stoneCobble, MT.STONES.Holystone    , 1)!=null)allValidCobbleStones.add(OM.get(OP.stoneCobble, MT.STONES.Holystone    , 1));
			if(OM.get(OP.stoneCobble, MT.STONES.Livingrock   , 1)!=null)allValidCobbleStones.add(OM.get(OP.stoneCobble, MT.STONES.Livingrock   , 1));
			if(OM.get(OP.stoneCobble, MT.STONES.Deadrock     , 1)!=null)allValidCobbleStones.add(OM.get(OP.stoneCobble, MT.STONES.Deadrock     , 1));
			if(OM.get(OP.stoneCobble, MT.STONES.Betweenstone , 1)!=null)allValidCobbleStones.add(OM.get(OP.stoneCobble, MT.STONES.Betweenstone , 1));
			if(OM.get(OP.stoneCobble, MT.STONES.Pitstone     , 1)!=null)allValidCobbleStones.add(OM.get(OP.stoneCobble, MT.STONES.Pitstone     , 1));
			if(OM.get(OP.stoneCobble, MT.STONES.Cragrock     , 1)!=null)allValidCobbleStones.add(OM.get(OP.stoneCobble, MT.STONES.Cragrock     , 1));
			if(OM.get(OP.stoneCobble, MT.STONES.Templerock   , 1)!=null)allValidCobbleStones.add(OM.get(OP.stoneCobble, MT.STONES.Templerock   , 1));
			if(OM.get(OP.stoneCobble, MT.STONES.Mazestone    , 1)!=null)allValidCobbleStones.add(OM.get(OP.stoneCobble, MT.STONES.Mazestone    , 1));
			if(OM.get(OP.stoneCobble, MT.STONES.Castlerock   , 1)!=null)allValidCobbleStones.add(OM.get(OP.stoneCobble, MT.STONES.Castlerock   , 1));
			if(OM.get(OP.stoneCobble, MT.STONES.Umber        , 1)!=null)allValidCobbleStones.add(OM.get(OP.stoneCobble, MT.STONES.Umber        , 1));
			if(OM.get(OP.stoneCobble, MT.STONES.Shale        , 1)!=null)allValidCobbleStones.add(OM.get(OP.stoneCobble, MT.STONES.Shale        , 1));
			if(OM.get(OP.stoneCobble, MT.STONES.Redrock      , 1)!=null)allValidCobbleStones.add(OM.get(OP.stoneCobble, MT.STONES.Redrock      , 1));
			if(OM.get(OP.stoneCobble, MT.STONES.Komatiite    , 1)!=null)allValidCobbleStones.add(OM.get(OP.stoneCobble, MT.STONES.Komatiite    , 1));
			if(OM.get(OP.stoneCobble, MT.STONES.Pumice       , 1)!=null)allValidCobbleStones.add(OM.get(OP.stoneCobble, MT.STONES.Pumice       , 1));
			if(OM.get(OP.stoneCobble, MT.STONES.Gabbro       , 1)!=null)allValidCobbleStones.add(OM.get(OP.stoneCobble, MT.STONES.Gabbro       , 1));
			if(OM.get(OP.stoneCobble, MT.STONES.Basalt       , 1)!=null)allValidCobbleStones.add(OM.get(OP.stoneCobble, MT.STONES.Basalt       , 1));
			if(OM.get(OP.stoneCobble, MT.STONES.Marble       , 1)!=null)allValidCobbleStones.add(OM.get(OP.stoneCobble, MT.STONES.Marble       , 1));
			if(OM.get(OP.stoneCobble, MT.STONES.Limestone    , 1)!=null)allValidCobbleStones.add(OM.get(OP.stoneCobble, MT.STONES.Limestone    , 1));
			if(OM.get(OP.stoneCobble, MT.STONES.Greenschist  , 1)!=null)allValidCobbleStones.add(OM.get(OP.stoneCobble, MT.STONES.Greenschist  , 1));
			if(OM.get(OP.stoneCobble, MT.STONES.Blueschist   , 1)!=null)allValidCobbleStones.add(OM.get(OP.stoneCobble, MT.STONES.Blueschist   , 1));
			if(OM.get(OP.stoneCobble, MT.STONES.Grayschist   , 1)!=null)allValidCobbleStones.add(OM.get(OP.stoneCobble, MT.STONES.Grayschist   , 1));
			if(OM.get(OP.stoneCobble, MT.STONES.Pinkschist   , 1)!=null)allValidCobbleStones.add(OM.get(OP.stoneCobble, MT.STONES.Pinkschist   , 1));
			if(OM.get(OP.stoneCobble, MT.STONES.Gneiss       , 1)!=null)allValidCobbleStones.add(OM.get(OP.stoneCobble, MT.STONES.Gneiss       , 1));
			if(OM.get(OP.stoneCobble, MT.STONES.Kimberlite   , 1)!=null)allValidCobbleStones.add(OM.get(OP.stoneCobble, MT.STONES.Kimberlite   , 1));
			if(OM.get(OP.stoneCobble, MT.STONES.Quartzite    , 1)!=null)allValidCobbleStones.add(OM.get(OP.stoneCobble, MT.STONES.Quartzite    , 1));
			if(OM.get(OP.stoneCobble, MT.STONES.GraniteRed   , 1)!=null)allValidCobbleStones.add(OM.get(OP.stoneCobble, MT.STONES.GraniteRed   , 1));
			if(OM.get(OP.stoneCobble, MT.STONES.GraniteBlack , 1)!=null)allValidCobbleStones.add(OM.get(OP.stoneCobble, MT.STONES.GraniteBlack , 1));
			if(OM.get(OP.stoneCobble, MT.STONES.Granite      , 1)!=null)allValidCobbleStones.add(OM.get(OP.stoneCobble, MT.STONES.Granite      , 1));
			if(OM.get(OP.stoneCobble, MT.STONES.Andesite     , 1)!=null)allValidCobbleStones.add(OM.get(OP.stoneCobble, MT.STONES.Andesite     , 1));
			if(OM.get(OP.stoneCobble, MT.STONES.Diorite      , 1)!=null)allValidCobbleStones.add(OM.get(OP.stoneCobble, MT.STONES.Diorite      , 1));
			if(OM.get(OP.stoneCobble, MT.STONES.Blackstone   , 1)!=null)allValidCobbleStones.add(OM.get(OP.stoneCobble, MT.STONES.Blackstone   , 1));
			if(OM.get(OP.stoneCobble, MT.STONES.Greywacke    , 1)!=null)allValidCobbleStones.add(OM.get(OP.stoneCobble, MT.STONES.Greywacke    , 1));
			if(OM.get(OP.stoneCobble, MT.STONES.Siltstone    , 1)!=null)allValidCobbleStones.add(OM.get(OP.stoneCobble, MT.STONES.Siltstone    , 1));
			if(OM.get(OP.stoneCobble, MT.STONES.Rhyolite     , 1)!=null)allValidCobbleStones.add(OM.get(OP.stoneCobble, MT.STONES.Rhyolite     , 1));
			if(OM.get(OP.stoneCobble, MT.STONES.Migmatite    , 1)!=null)allValidCobbleStones.add(OM.get(OP.stoneCobble, MT.STONES.Migmatite    , 1));
			if(OM.get(OP.stoneCobble, MT.STONES.Chert        , 1)!=null)allValidCobbleStones.add(OM.get(OP.stoneCobble, MT.STONES.Chert        , 1));
			if(OM.get(OP.stoneCobble, MT.STONES.Dacite       , 1)!=null)allValidCobbleStones.add(OM.get(OP.stoneCobble, MT.STONES.Dacite       , 1));
			if(OM.get(OP.stoneCobble, MT.STONES.Slate        , 1)!=null)allValidCobbleStones.add(OM.get(OP.stoneCobble, MT.STONES.Slate        , 1));
			if(OM.get(OP.stoneCobble, MT.STONES.Deepslate    , 1)!=null)allValidCobbleStones.add(OM.get(OP.stoneCobble, MT.STONES.Deepslate    , 1));
			if(OM.get(OP.stoneCobble, MT.STONES.Eclogite     , 1)!=null)allValidCobbleStones.add(OM.get(OP.stoneCobble, MT.STONES.Eclogite     , 1));
			if(OM.get(OP.stoneCobble, MT.STONES.PhobosRock   , 1)!=null)allValidCobbleStones.add(OM.get(OP.stoneCobble, MT.STONES.PhobosRock   , 1));
			if(OM.get(OP.stoneCobble, MT.STONES.DeimosRock   , 1)!=null)allValidCobbleStones.add(OM.get(OP.stoneCobble, MT.STONES.DeimosRock   , 1));
			if(OM.get(OP.stoneCobble, MT.STONES.VenusRock    , 1)!=null)allValidCobbleStones.add(OM.get(OP.stoneCobble, MT.STONES.VenusRock    , 1));
			if(OM.get(OP.stoneCobble, MT.STONES.MercuryRock  , 1)!=null)allValidCobbleStones.add(OM.get(OP.stoneCobble, MT.STONES.MercuryRock  , 1));
			if(OM.get(OP.stoneCobble, MT.STONES.CeresRock    , 1)!=null)allValidCobbleStones.add(OM.get(OP.stoneCobble, MT.STONES.CeresRock    , 1));
			if(OM.get(OP.stoneCobble, MT.STONES.JupiterRock  , 1)!=null)allValidCobbleStones.add(OM.get(OP.stoneCobble, MT.STONES.JupiterRock  , 1));
			if(OM.get(OP.stoneCobble, MT.STONES.IoRock       , 1)!=null)allValidCobbleStones.add(OM.get(OP.stoneCobble, MT.STONES.IoRock       , 1));
			if(OM.get(OP.stoneCobble, MT.STONES.EuropaRock   , 1)!=null)allValidCobbleStones.add(OM.get(OP.stoneCobble, MT.STONES.EuropaRock   , 1));
			if(OM.get(OP.stoneCobble, MT.STONES.GanymedeRock , 1)!=null)allValidCobbleStones.add(OM.get(OP.stoneCobble, MT.STONES.GanymedeRock , 1));
			if(OM.get(OP.stoneCobble, MT.STONES.CallistoRock , 1)!=null)allValidCobbleStones.add(OM.get(OP.stoneCobble, MT.STONES.CallistoRock , 1));
			if(OM.get(OP.stoneCobble, MT.STONES.SaturnRock   , 1)!=null)allValidCobbleStones.add(OM.get(OP.stoneCobble, MT.STONES.SaturnRock   , 1));
			if(OM.get(OP.stoneCobble, MT.STONES.RheaRock     , 1)!=null)allValidCobbleStones.add(OM.get(OP.stoneCobble, MT.STONES.RheaRock     , 1));
			if(OM.get(OP.stoneCobble, MT.STONES.TitanRock    , 1)!=null)allValidCobbleStones.add(OM.get(OP.stoneCobble, MT.STONES.TitanRock    , 1));
			if(OM.get(OP.stoneCobble, MT.STONES.OberonRock   , 1)!=null)allValidCobbleStones.add(OM.get(OP.stoneCobble, MT.STONES.OberonRock   , 1));
			if(OM.get(OP.stoneCobble, MT.STONES.IapetusRock  , 1)!=null)allValidCobbleStones.add(OM.get(OP.stoneCobble, MT.STONES.IapetusRock  , 1));
			if(OM.get(OP.stoneCobble, MT.STONES.UranusRock   , 1)!=null)allValidCobbleStones.add(OM.get(OP.stoneCobble, MT.STONES.UranusRock   , 1));
			if(OM.get(OP.stoneCobble, MT.STONES.TitaniaRock  , 1)!=null)allValidCobbleStones.add(OM.get(OP.stoneCobble, MT.STONES.TitaniaRock  , 1));
			if(OM.get(OP.stoneCobble, MT.STONES.NeptuneRock  , 1)!=null)allValidCobbleStones.add(OM.get(OP.stoneCobble, MT.STONES.NeptuneRock  , 1));
			if(OM.get(OP.stoneCobble, MT.STONES.TritonRock   , 1)!=null)allValidCobbleStones.add(OM.get(OP.stoneCobble, MT.STONES.TritonRock   , 1));
			allValidCobbleStonesAmount = allValidCobbleStones.size();
		}
		return allValidCobbleStones;
	}
}
