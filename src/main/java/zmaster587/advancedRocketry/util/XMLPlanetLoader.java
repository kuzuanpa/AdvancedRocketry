package zmaster587.advancedRocketry.util;

import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTException;
import net.minecraftforge.common.BiomeManager.BiomeEntry;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import org.jetbrains.annotations.NotNull;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import zmaster587.advancedRocketry.AdvancedRocketry;
import zmaster587.advancedRocketry.api.AdvancedRocketryBiomes;
import zmaster587.advancedRocketry.api.Configuration;
import zmaster587.advancedRocketry.api.dimension.IDimensionProperties;
import zmaster587.advancedRocketry.api.dimension.solar.IGalaxy;
import zmaster587.advancedRocketry.api.dimension.solar.StellarBody;
import zmaster587.advancedRocketry.dimension.DimensionManager;
import zmaster587.advancedRocketry.dimension.DimensionProperties;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.util.*;

public class XMLPlanetLoader {

	Document doc;
	NodeList currentList;
	final int currentNodeIndex;
	final int starId;
	int offset;

	final HashMap<StellarBody, Integer> maxPlanetNumber = new HashMap<>();
	final HashMap<StellarBody, Integer> maxGasPlanetNumber = new HashMap<>();

	public boolean loadFile(File xmlFile) throws IOException {
		DocumentBuilder docBuilder;
		doc = null;
		try {
			docBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
		} catch (ParserConfigurationException e) {
			return false;
		}

		try {
			doc = docBuilder.parse(xmlFile);
		} catch (SAXException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}
	public XMLPlanetLoader() {
		doc = null;
		currentNodeIndex = -1;
		starId=0;
	}

	public boolean isValid() {
		return doc != null;
	}

	public int getMaxNumPlanets(StellarBody body) {
		return maxPlanetNumber.get(body);
	}


	public int getMaxNumGasGiants(StellarBody body) {
		return maxGasPlanetNumber.get(body);
	}

	private List<DimensionProperties> readPlanetFromNode(Node planetNode, StellarBody star) {
		List<DimensionProperties> list = new ArrayList<>();
		Node planetPropertyNode = planetNode.getFirstChild();


		DimensionProperties properties = new DimensionProperties(DimensionManager.getInstance().getNextFreeDim(offset));

        list.add(properties);
		offset++;//Increment for dealing with child planets


		//Set name for dimension if exists
		if(planetNode.hasAttributes()) {
			Node nameNode = planetNode.getAttributes().getNamedItem("name");
			if(nameNode != null && !nameNode.getNodeValue().isEmpty()) {
				properties.setName(nameNode.getNodeValue());
			}

			nameNode = planetNode.getAttributes().getNamedItem("DIMID");
			if(nameNode != null && !nameNode.getNodeValue().isEmpty()) {
				try {
					if(nameNode.getTextContent().isEmpty()) throw new NumberFormatException();
					properties.setId(Integer.parseInt(nameNode.getTextContent()));
					//We're not using the offset so decrement to prepare for next planet
					offset--;
				} catch (NumberFormatException e) {
                    AdvancedRocketry.logger.warn("Invalid DIMID specified for planet {}", properties.getName()); //TODO: more detailed error msg
					list.remove(properties);
					offset--;
					return list;
				}
			}

			nameNode = planetNode.getAttributes().getNamedItem("dimMapping");
			if(nameNode != null) {
				properties.isNativeDimension = false;
			}

			nameNode = planetNode.getAttributes().getNamedItem("customIcon");
			if(nameNode != null) {
				properties.customIcon = nameNode.getTextContent();
			}
		}

		while(planetPropertyNode != null) {
			if(planetPropertyNode.getNodeName().equalsIgnoreCase("fogcolor")) {
				String[] colors = planetPropertyNode.getTextContent().split(",");
				try {
					if(colors.length >= 3) {
						float[] rgb = new float[3];


						for(int j = 0; j < 3; j++)
							rgb[j] = Float.parseFloat(colors[j]);
						properties.fogColor = rgb;

					}
					else if(colors.length == 1) {
						int cols = Integer.parseUnsignedInt(colors[0].substring(2), 16);
						float[] rgb = new float[3];

						rgb[0] = ((cols >>> 16) & 0xff) / 255f;
						rgb[1] = ((cols >>> 8) & 0xff) / 255f;
						rgb[2] = (cols & 0xff) / 255f;

						properties.fogColor = rgb;
					}
					else
						AdvancedRocketry.logger.warn("Invalid number of floats specified for fog color (Required 3, comma sperated)"); //TODO: more detailed error msg
				} catch (NumberFormatException e) {
					AdvancedRocketry.logger.warn("Invalid fog color specified"); //TODO: more detailed error msg
				}
			}
			else if(planetPropertyNode.getNodeName().equalsIgnoreCase("gas")) {
				Fluid f = FluidRegistry.getFluid(planetPropertyNode.getTextContent());
				
				if(f == null)
                    AdvancedRocketry.logger.warn("\"{}\" is not a valid fluid", planetPropertyNode.getTextContent()); //TODO: more detailed error msg
				else {
					properties.getHarvestableGasses().add(f);
				}
			}
			else if(planetPropertyNode.getNodeName().equalsIgnoreCase("oceanBlock")) {
				String blockName = planetPropertyNode.getTextContent();
				Block block = (Block) Block.blockRegistry.getObject(blockName);
				
				if(block == Blocks.air || block == null)
                    AdvancedRocketry.logger.warn("Invalid ocean block: {}", blockName); //TODO: more detailed error msg
				
				properties.setOceanBlock(block);
			}
			else if(planetPropertyNode.getNodeName().equalsIgnoreCase("fillerBlock")) {
				String blockName = planetPropertyNode.getTextContent();
				Block block = Block.getBlockFromName(blockName);
				
				if(block == Blocks.air || block == null)
				{
                    AdvancedRocketry.logger.warn("Invalid filler block: {}", blockName); //TODO: more detailed error msg
					block = null;
				}
				
				properties.setStoneBlock(block);
			}
			else if(planetPropertyNode.getNodeName().equalsIgnoreCase("skycolor")) {
				String[] colors = planetPropertyNode.getTextContent().split(",");
				try {

					if(colors.length >= 3) {
						float[] rgb = new float[3];

						for(int j = 0; j < 3; j++)
							rgb[j] = Float.parseFloat(colors[j]);
						properties.skyColor = rgb;

					}
					else if(colors.length == 1) {
						int cols = Integer.parseUnsignedInt(colors[0].substring(2), 16);
						float[] rgb = new float[3];

						rgb[0] = ((cols >>> 16) & 0xff) / 255f;
						rgb[1] = ((cols >>> 8) & 0xff) / 255f;
						rgb[2] = (cols & 0xff) / 255f;

						properties.skyColor = rgb;
					}
					else
						AdvancedRocketry.logger.warn("Invalid number of floats specified for sky color (Required 3, comma sperated)"); //TODO: more detailed error msg

				} catch (NumberFormatException e) {
					AdvancedRocketry.logger.warn("Invalid sky color specified"); //TODO: more detailed error msg
				}
			}
			else if(planetPropertyNode.getNodeName().equalsIgnoreCase("atmosphereDensity")) {

				try {
					properties.setAtmosphereDensityDirect(Math.min(Math.max(Integer.parseInt(planetPropertyNode.getTextContent()), DimensionProperties.MIN_ATM_PRESSURE), DimensionProperties.MAX_ATM_PRESSURE));
				} catch (NumberFormatException e) {
					AdvancedRocketry.logger.warn("Invalid atmosphereDensity specified"); //TODO: more detailed error msg
				}
			}
			else if(planetPropertyNode.getNodeName().equalsIgnoreCase("gravitationalmultiplier")) {

				try {
					properties.gravitationalMultiplier = Math.min(Math.max(Integer.parseInt(planetPropertyNode.getTextContent()), DimensionProperties.MIN_GRAVITY), DimensionProperties.MAX_GRAVITY)/100f;
				} catch (NumberFormatException e) {
					AdvancedRocketry.logger.warn("Invalid gravitationalMultiplier specified"); //TODO: more detailed error msg
				}
			}
			else if(planetPropertyNode.getNodeName().equalsIgnoreCase("orbitaldistance")) {

				try {
					properties.orbitalDist = Math.min(Math.max(Integer.parseInt(planetPropertyNode.getTextContent()), DimensionProperties.MIN_DISTANCE), DimensionProperties.MAX_DISTANCE);
				} catch (NumberFormatException e) {
					AdvancedRocketry.logger.warn("Invalid orbitalDist specified"); //TODO: more detailed error msg
				}
			}
			else if(planetPropertyNode.getNodeName().equalsIgnoreCase("orbitaltheta")) {

				try {
					properties.baseOrbitTheta = (Integer.parseInt(planetPropertyNode.getTextContent()) % 360) * Math.PI/180f;
				} catch (NumberFormatException e) {
					AdvancedRocketry.logger.warn("Invalid orbitalTheta specified"); //TODO: more detailed error msg
				}
			}
			else if(planetPropertyNode.getNodeName().equalsIgnoreCase("rotationalperiod")) {
				try {
					int rotationalPeriod =  Integer.parseInt(planetPropertyNode.getTextContent());
					if(properties.rotationalPeriod > 0)
						properties.rotationalPeriod = rotationalPeriod;
					else
						AdvancedRocketry.logger.warn("rotational Period must be greater than 0"); //TODO: more detailed error msg
				} catch (NumberFormatException e) {
					AdvancedRocketry.logger.warn("Invalid rotational period specified"); //TODO: more detailed error msg
				}
			}
			else if(planetPropertyNode.getNodeName().equalsIgnoreCase("seaLevel")) {
				try {
					properties.setSeaLevel(Integer.parseInt(planetPropertyNode.getTextContent()));
				} catch (NumberFormatException e) {
					AdvancedRocketry.logger.warn("Invalid sealevel specified"); //TODO: more detailed error msg
				}
			}
			else if(planetPropertyNode.getNodeName().equalsIgnoreCase("biomeids")) {

				String[] biomeList = planetPropertyNode.getTextContent().split(",");
                for (String s : biomeList) {
                    try {
                        int biome = Integer.parseInt(s);

                        if (!properties.addBiome(biome))
                            AdvancedRocketry.logger.warn("{} is not a valid biome id", s); //TODO: more detailed error msg
                    } catch (NumberFormatException e) {
                        AdvancedRocketry.logger.warn("{} is not a valid biome id", s); //TODO: more detailed error msg
                    }
                }
			}
			else if(planetPropertyNode.getNodeName().equalsIgnoreCase("artifact")) {
				ItemStack stack = XMLPlanetLoader.getStack(planetPropertyNode.getTextContent());

				if(stack != null)
					properties.getRequiredArtifacts().add(stack);
			}
			else if(planetPropertyNode.getNodeName().equalsIgnoreCase("planet")) {
				List<DimensionProperties> childList = readPlanetFromNode(planetPropertyNode, star);
				if(!childList.isEmpty()) {
					DimensionProperties child = childList.get(childList.size()-1); // Last entry in the list is the child planet
					properties.addChildPlanet(child);
					list.addAll(childList);
				}
			}
			else if(planetPropertyNode.getNodeName().equalsIgnoreCase("orbitalPhi")) {
				try {
					properties.orbitalPhi = (Integer.parseInt(planetPropertyNode.getTextContent()) % 360);
				} catch (NumberFormatException e) {
					AdvancedRocketry.logger.warn("Invalid orbitalPhi specified"); //TODO: more detailed error msg
				}
			}
			else if(planetPropertyNode.getNodeName().equalsIgnoreCase("oreGen")) {
				properties.oreProperties = XMLOreLoader.loadOre(planetPropertyNode);
			}
			else if(planetPropertyNode.getNodeName().equalsIgnoreCase("genType")) {
				try {
					properties.setGenType(Integer.parseInt(planetPropertyNode.getTextContent()));
				} catch (NumberFormatException e) {
					AdvancedRocketry.logger.warn("Invalid generator type specified"); //TODO: more detailed error msg
				}
			}
			else if(planetPropertyNode.getNodeName().equalsIgnoreCase("hasRings"))
				properties.hasRings = Boolean.parseBoolean(planetPropertyNode.getTextContent());
			else if(planetPropertyNode.getNodeName().equalsIgnoreCase("ringColor")) {
				String[] colors = planetPropertyNode.getTextContent().split(",");
				try {

					if(colors.length >= 3) {
						float[] rgb = new float[3];

						for(int j = 0; j < 3; j++)
							rgb[j] = Float.parseFloat(colors[j]);
						properties.ringColor = rgb;

					}
					else if(colors.length == 1) {
						int cols = Integer.parseUnsignedInt(colors[0].substring(2), 16);
						float[] rgb = new float[3];

						rgb[0] = ((cols >>> 16) & 0xff) / 255f;
						rgb[1] = ((cols >>> 8) & 0xff) / 255f;
						rgb[2] = (cols & 0xff) / 255f;

						properties.ringColor = rgb;
					}
					else
						AdvancedRocketry.logger.warn("Invalid number of floats specified for ring color (Required 3, comma sperated)"); //TODO: more detailed error msg

				} catch (NumberFormatException e) {
					AdvancedRocketry.logger.warn("Invalid sky color specified"); //TODO: more detailed error msg
				}
			}
			else if(planetPropertyNode.getNodeName().equalsIgnoreCase("hasOxygen")) {
				String text = planetPropertyNode.getTextContent();
				if(text != null && text.equalsIgnoreCase("false"))
					properties.hasOxygen = false;
			}
			else if(planetPropertyNode.getNodeName().equalsIgnoreCase("GasGiant")) {
				String text = planetPropertyNode.getTextContent();
				if(text != null && text.equalsIgnoreCase("true"))
					properties.setGasGiant(true);
			}else if(planetPropertyNode.getNodeName().equalsIgnoreCase("spawnable")) {
				int weight = 100;
				int groupMin = 1, groupMax = 1;
				String nbtString = "";
				Node weightNode = planetPropertyNode.getAttributes().getNamedItem("weight");
				Node groupMinNode = planetPropertyNode.getAttributes().getNamedItem("groupMin");
				Node groupMaxNode = planetPropertyNode.getAttributes().getNamedItem("groupMax");
				Node nbtNode = planetPropertyNode.getAttributes().getNamedItem("nbt");

				//Get spawn properties
				if(weightNode != null) {
					try {
						weight = Integer.parseInt(weightNode.getTextContent());
						weight = Math.max(1, weight);
					} catch(NumberFormatException ignored) {
					}
				}
				if(groupMinNode != null) {
					try {
						groupMin = Integer.parseInt(groupMinNode.getTextContent());
						groupMin = Math.max(1, groupMin);
					} catch(NumberFormatException ignored) {
					}
				}
				if(groupMaxNode != null) {
					try {
						groupMax = Integer.parseInt(groupMaxNode.getTextContent());
						groupMax = Math.max(1, groupMax);
					} catch(NumberFormatException ignored) {
					}
				}

				if(nbtNode != null) {
					nbtString = nbtNode.getTextContent();
				}

				if (groupMax < groupMin) {
					groupMax = groupMin;
				}

				Class clazz= (Class) EntityList.stringToClassMapping.get(planetPropertyNode.getTextContent());
				//If not using string name maybe it's a class name?
				if(clazz == null) {
					try {
						clazz = Class.forName(planetPropertyNode.getTextContent());
						if(!Entity.class.isAssignableFrom(clazz))
							clazz = null;
					} catch (Exception ignored) {}
				}

				if(clazz != null) {
					SpawnListEntryNBT entry = new SpawnListEntryNBT(clazz, weight, groupMin, groupMax);
					if(!nbtString.isEmpty())
						try {
							entry.setNbt(nbtString);
						} catch (DOMException e) {
                            AdvancedRocketry.logger.fatal("===== Configuration Error!  Please check your save's planetDefs.xml config file =====\n{}\nThe following is not valid JSON:\n{}", e.getLocalizedMessage(), nbtString);
						} catch (NBTException e) {
                            AdvancedRocketry.logger.fatal("===== Configuration Error!  Please check your save's planetDefs.xml config file =====\n{}\nThe following is not valid NBT data:\n{}", e.getLocalizedMessage(), nbtString);
						}

					properties.getSpawnListEntries().add(entry);
				} else
                    AdvancedRocketry.logger.warn("Cannot find {} while registering entity for planet spawn", planetPropertyNode.getTextContent());

			} else if(planetPropertyNode.getNodeName().equalsIgnoreCase("isKnown")) {
				String text = planetPropertyNode.getTextContent();
				if(text != null && text.equalsIgnoreCase("true")) {
					Configuration.initiallyKnownPlanets.add(properties.getId());
				}
			}

			planetPropertyNode = planetPropertyNode.getNextSibling();
		}

		//Star may not be registered at this time, use ID version instead
		properties.setStar(star.getId());

		//Set peak insolation multiplier
		//Assumes that a 16 atmosphere is 16x the partial pressure but not thicker, because I don't want to deal with that and this is fairly simple right now
		//Get what it would be relative to LEO, this gives ~0.76 for Earth at the surface
		double insolationRelativeToLEO = AstronomicalBodyHelper.getStellarBrightness(star, properties.getSolarOrbitalDistance()) * Math.pow(Math.E, -(0.0026899d * properties.getAtmosphereDensity()));
		//Multiply by Earth LEO/Earth Surface for ratio relative to Earth surface (1360/1040)
		properties.peakInsolationMultiplier = insolationRelativeToLEO * 1.308d;

		//Set temperature
		properties.averageTemperature = AstronomicalBodyHelper.getAverageTemperature(star, properties.getSolarOrbitalDistance(), properties.getAtmosphereDensity());

		//If no biomes are specified add some!
		if(properties.getBiomes().isEmpty())
			properties.addBiomes(properties.getViableBiomes());

		return list;
	}


	public StellarBody readStar(@NotNull Node planetNode) {
		StellarBody star = readSubStar(planetNode);
		if(planetNode.hasAttributes()) {
			Node nameNode;

			nameNode = planetNode.getAttributes().getNamedItem("x");

			if(nameNode != null && !nameNode.getNodeValue().isEmpty()) {
				try {
					star.setPosX(Integer.parseInt(nameNode.getNodeValue()));
				} catch (NumberFormatException e) {
                    AdvancedRocketry.logger.warn("Error Reading star {}", star.getName());
				}
			}

			nameNode = planetNode.getAttributes().getNamedItem("y");

			if(nameNode != null && !nameNode.getNodeValue().isEmpty()) {
				try {
					star.setPosZ(Integer.parseInt(nameNode.getNodeValue()));
				} catch (NumberFormatException e) {
                    AdvancedRocketry.logger.warn("Error Reading star {}", star.getName());
				}
			}

			nameNode = planetNode.getAttributes().getNamedItem("numPlanets");

			try {
				maxPlanetNumber.put(star ,Integer.parseInt(nameNode.getNodeValue()));
			} catch (Exception e) {
				AdvancedRocketry.logger.warn("Invalid number of planets specified in xml config!");
			}

			nameNode = planetNode.getAttributes().getNamedItem("numGasGiants");
			try {
				maxGasPlanetNumber.put(star ,Integer.parseInt(nameNode.getNodeValue()));
			} catch (Exception e) {
				AdvancedRocketry.logger.warn("Invalid number of planets specified in xml config!");
			}
		}
		return star;
	}
	public DimensionProperties registerStarDims(Node planetNode,StellarBody star){
		DimensionProperties properties = new DimensionProperties(DimensionManager.getInstance().getNextFreeDim(offset));
		offset++;//Increment for dealing with child planets
		//Set name for dimension if exists
		if(planetNode.hasAttributes()) {
			Node nameNode = planetNode.getAttributes().getNamedItem("name");
			if(nameNode != null && !nameNode.getNodeValue().isEmpty()) {
				properties.setName(nameNode.getNodeValue());
			}

			nameNode = planetNode.getAttributes().getNamedItem("id");
			if(nameNode != null && !nameNode.getNodeValue().isEmpty()) {
				try {
					if(nameNode.getTextContent().isEmpty()) throw new NumberFormatException();
					properties.setId(Integer.parseInt(nameNode.getTextContent()));
					//We're not using the offset so decrement to prepare for next planet
					offset--;
				} catch (NumberFormatException e) {
                    AdvancedRocketry.logger.warn("Invalid DIMID specified for star {}", properties.getName()); //TODO: more detailed error msg
					offset--;
					return null;
				}
			}
		}
		properties.setStar(star.getId());
		properties.addBiomes(Collections.singletonList(AdvancedRocketryBiomes.spaceBiome));
		properties.setSun(true);
		return properties;
	}
	
	public StellarBody readSubStar(Node planetNode) {
		StellarBody star = new StellarBody();
		if(planetNode.hasAttributes()) {
			Node nameNode = planetNode.getAttributes().getNamedItem("name");
			if(nameNode != null && !nameNode.getNodeValue().isEmpty()) {
				star.setName(nameNode.getNodeValue());
			}
			nameNode = planetNode.getAttributes().getNamedItem("id");
			if(nameNode != null && !nameNode.getNodeValue().isEmpty()) {
				try {
					star.setId(Integer.parseInt(nameNode.getNodeValue()));
				} catch (NumberFormatException e) {
                    AdvancedRocketry.logger.warn("Error Reading star {}", star.getName());
				}
			}
			nameNode = planetNode.getAttributes().getNamedItem("temp");

			if(nameNode != null && !nameNode.getNodeValue().isEmpty()) {
				try {
					star.setTemperature(Integer.parseInt(nameNode.getNodeValue()));
				} catch (NumberFormatException e) {
                    AdvancedRocketry.logger.warn("Error Reading star {}", star.getName());
				}
			}
			
			nameNode = planetNode.getAttributes().getNamedItem("size");
			if(nameNode != null && !nameNode.getNodeValue().isEmpty()) {
				try {
					star.setSize(Float.parseFloat(nameNode.getNodeValue()));
				} catch (NumberFormatException e) {
                    AdvancedRocketry.logger.warn("Error Reading star {}", star.getName());
				}
			}
			
			nameNode = planetNode.getAttributes().getNamedItem("seperation");
			if(nameNode != null && !nameNode.getNodeValue().isEmpty()) {
				try {
					star.setStarSeperation(Float.parseFloat(nameNode.getNodeValue()));
				} catch (NumberFormatException e) {
                    AdvancedRocketry.logger.warn("Error Reading star {}", star.getName());
				}
			}
		}
		return star;
	}

	public DimensionPropertyCoupling readAllPlanets() {
		DimensionPropertyCoupling coupling = new DimensionPropertyCoupling();

		Node masterNode = doc.getElementsByTagName("galaxy").item(0).getFirstChild();

		//readPlanetFromNode changes value
		//Yes it's hacky but that's another reason why it's private

		offset = DimensionManager.dimOffset;
		while(masterNode != null) {
			if(!masterNode.getNodeName().equals("star")) {
				masterNode = masterNode.getNextSibling();
				continue;
			}

			StellarBody star = readStar(masterNode);
			coupling.dims.add(registerStarDims(masterNode,star));
			coupling.stars.add(star);

			@NotNull NodeList planetNodeList = masterNode.getChildNodes();

			Node planetNode = planetNodeList.item(0);

			while(planetNode != null) {
				if(planetNode.getNodeName().equalsIgnoreCase("planet")) {
					coupling.dims.addAll(readPlanetFromNode(planetNode, star));
				}
				if(planetNode.getNodeName().equalsIgnoreCase("star")) {
					StellarBody star2 = readSubStar(planetNode);
					coupling.dims.add(registerStarDims(planetNode,star));
					star.addSubStar(star2);
				}
				planetNode = planetNode.getNextSibling();
			}

			masterNode = masterNode.getNextSibling();
		}
		return coupling;
	}

	public static @NotNull String writeXML(IGalaxy galaxy) {
		//galaxy.
		StringBuilder outputString = new StringBuilder("<galaxy>\n");

		Collection<StellarBody> stars = galaxy.getStars();

		for(StellarBody star : stars) {
			outputString.append("\t<star name=\"").append(star.getName()).append("\" id=\"").append(star.getId()).append("\" temp=\"").append(star.getTemperature()).append("\" x=\"").append(star.getPosX()).append("\" y=\"").append(star.getPosZ()).append("\" size=\"").append(star.getSize()).append("\" numPlanets=\"0\" numGasGiants=\"0\">\n");

			for(StellarBody star2 : star.getSubStars()) {
				outputString.append("\t\t<star temp=\"").append(star2.getTemperature()).append("\" size=\"").append(star2.getSize()).append("\" seperation=\"").append(star2.getStarSeperation()).append("\" />\n");

			}
			
			for(IDimensionProperties properties : star.getPlanets()) {
				if(!properties.isMoon()&&!properties.isSun()&&!(properties.getId()==star.getId()))
					outputString.append(writePlanet((DimensionProperties) properties, 2));
			}

			outputString.append("\t</star>\n");
		}

		outputString.append("</galaxy>");

		return outputString.toString();
	}

	private static String writePlanet(@NotNull DimensionProperties properties, int numTabs) {
		StringBuilder outputString;
		StringBuilder tabLen = new StringBuilder();

		for(int i = 0; i < numTabs; i++) {
			tabLen.append("\t");
		}

		outputString = new StringBuilder(tabLen + "<planet name=\"" + properties.getName() + "\" DIMID=\"" + properties.getId() + "\"" +
                (properties.isNativeDimension ? "" : " dimMapping=\"\"") +
                (properties.customIcon.isEmpty() ? "" : " customIcon=\"" + properties.customIcon + "\"") + ">\n");


		outputString.append(tabLen).append("\t<isKnown>").append(Configuration.initiallyKnownPlanets.contains(properties.getId())).append("</isKnown>\n");
		if(properties.hasRings) {
			outputString.append(tabLen).append("\t<hasRings>true</hasRings>\n");
			outputString.append(tabLen).append("\t<ringColor>").append(properties.ringColor[0]).append(",").append(properties.ringColor[1]).append(",").append(properties.ringColor[2]).append("</ringColor>\n");
		}

		if(!properties.hasOxygen)
		{
			outputString.append(tabLen).append("\t<hasOxygen>false</hasOxygen>\n");
		}

		if(properties.isGasGiant())
		{
			outputString.append(tabLen).append("\t<GasGiant>true</GasGiant>\n");
			if(!properties.getHarvestableGasses().isEmpty())
			{
				for(Fluid f : properties.getHarvestableGasses())
				{
					outputString.append(tabLen).append("\t<gas>").append(f.getName()).append("</gas>\n");
				}
				
			}
		}

		outputString.append(tabLen).append("\t<fogColor>").append(properties.fogColor[0]).append(",").append(properties.fogColor[1]).append(",").append(properties.fogColor[2]).append("</fogColor>\n");
		outputString.append(tabLen).append("\t<skyColor>").append(properties.skyColor[0]).append(",").append(properties.skyColor[1]).append(",").append(properties.skyColor[2]).append("</skyColor>\n");
		outputString.append(tabLen).append("\t<gravitationalMultiplier>").append((int) (properties.getGravitationalMultiplier() * 100f)).append("</gravitationalMultiplier>\n");
		outputString.append(tabLen).append("\t<orbitalDistance>").append(properties.getOrbitalDist()).append("</orbitalDistance>\n");
		outputString.append(tabLen).append("\t<orbitalTheta>").append((int) (properties.baseOrbitTheta * 180d / Math.PI)).append("</orbitalTheta>\n");
		outputString.append(tabLen).append("\t<solarInsolationMult>").append(properties.peakInsolationMultiplier).append("</solarInsolationMult>\n");
		outputString.append(tabLen).append("\t<avgTemperature>").append(properties.averageTemperature).append("</avgTemperature>\n");
		outputString.append(tabLen).append("\t<orbitalPhi>").append((int) (properties.orbitalPhi)).append("</orbitalPhi>\n");
		outputString.append(tabLen).append("\t<rotationalPeriod>").append(properties.rotationalPeriod).append("</rotationalPeriod>\n");
		outputString.append(tabLen).append("\t<atmosphereDensity>").append(properties.getAtmosphereDensity()).append("</atmosphereDensity>\n");
		
		if(properties.getSeaLevel() != 63)
			outputString.append(tabLen).append("\t<seaLevel>").append(properties.getSeaLevel()).append("</seaLevel>\n");
		
		if(properties.getGenType() != 0)
			outputString.append(tabLen).append("\t<genType>").append(properties.getGenType()).append("</genType>\n");
		
		if(properties.oreProperties != null) {
			outputString.append(tabLen).append("\t<oreGen>\n");
			outputString.append(XMLOreLoader.writeOreEntryXML(properties.oreProperties, numTabs + 2));
			outputString.append(tabLen).append("\t</oreGen>\n");
		}
		
		if(properties.isNativeDimension && !properties.isGasGiant()) {
			StringBuilder biomeIds = new StringBuilder();
			for(BiomeEntry biome : properties.getBiomes()) {
				biomeIds.append(",").append(biome.biome.biomeID);
			}
			if(biomeIds.length() > 0)
				biomeIds = new StringBuilder(biomeIds.substring(1));
			else
                AdvancedRocketry.logger.warn("Dim {} has no biomes to save!", properties.getId());
			
			outputString.append(tabLen).append("\t<biomeIds>").append(biomeIds).append("</biomeIds>\n");
		}

		for(ItemStack stack : properties.getRequiredArtifacts()) {
			outputString.append(tabLen).append("\t<artifact>").append(Item.itemRegistry.getNameForObject(stack.getItem())).append(";").append(stack.getItemDamage()).append(";").append(stack.stackSize).append("</artifact>\n");
		}
		
		for(Integer properties2 : properties.getChildPlanets()) {
			outputString.append(writePlanet(DimensionManager.getInstance().getDimensionProperties(properties2), numTabs + 1));
		}

		if(properties.getOceanBlock() != null) {
			outputString.append(tabLen).append("\t<oceanBlock>").append(Block.blockRegistry.getNameForObject(properties.getOceanBlock())).append("</oceanBlock>\n");
		}
		
		if(properties.getStoneBlock() != null) {
			outputString.append(tabLen).append("\t<fillerBlock>").append(Block.blockRegistry.getNameForObject(properties.getStoneBlock())).append("</fillerBlock>\n");
		}
		
		outputString.append(tabLen).append("</planet>\n");
		return outputString.toString();
	}

	public static class DimensionPropertyCoupling {

		public final List<StellarBody> stars = new LinkedList<>();
		public final @NotNull List<DimensionProperties> dims = new LinkedList<>();


	}

	
	public static ItemStack getStack(@NotNull String text) {
		String[] splitStr = text.split(";");
		int meta = 0;
		int size = 1;
		//format: "name;meta;size"
		if(splitStr.length > 1) {
			try {
				meta = Integer.parseInt(splitStr[1]);
			} catch( NumberFormatException e) {}
			
			if(splitStr.length > 2)
			{
				try {
					size = Integer.parseInt(splitStr[2]);
				} catch( NumberFormatException e) {}
			}
		}

		ItemStack stack = null;
		Block block = Block.getBlockFromName(splitStr[0]);
		if(block == null) {

			//Try getting item by name first
			Item item = (Item) Item.itemRegistry.getObject(splitStr[0]);

			if(item != null)
				stack = new ItemStack(item, size, meta);
			else {
				try {

					item = Item.getItemById(Integer.parseInt(splitStr[0]));
					if(item != null)
						stack = new ItemStack(item, size, meta);
				} catch (NumberFormatException e) { return null;}

			}
		}
		else
			stack = new ItemStack(block, size, meta);
	
		return stack;
	}
}
