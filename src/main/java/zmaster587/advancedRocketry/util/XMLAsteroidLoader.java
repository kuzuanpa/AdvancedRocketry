package zmaster587.advancedRocketry.util;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;
import zmaster587.advancedRocketry.AdvancedRocketry;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

public class XMLAsteroidLoader {

	Document doc;

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

	public XMLAsteroidLoader() {
		doc = null;
	}

	/**
	 * Load the propery file looking for combinations of temp and pressure
	 * @param propertyFile
	 * @return  list of singleEntry (order MUST be preserved)
	 */
	public List<AsteroidSmall> loadPropertyFile() {
		Node childNode = doc.getFirstChild().getFirstChild();
		List<AsteroidSmall> mapping = new LinkedList<>();

		while(childNode != null) {

			if(childNode.getNodeType() != Node.ELEMENT_NODE || !childNode.getNodeName().equalsIgnoreCase("asteroid")) { 
				childNode = childNode.getNextSibling();
				continue;
			}

			AsteroidSmall asteroid = new AsteroidSmall();

			if(childNode.hasAttributes()) {
				NamedNodeMap att = childNode.getAttributes();

				Node node = att.getNamedItem("name");

				if(node != null) {
					asteroid.ID = node.getTextContent();
				}

				node = att.getNamedItem("distance");
				if(node != null) {
					try {
						asteroid.distance = Integer.parseInt(node.getTextContent());
					} catch(NumberFormatException e) {
                        AdvancedRocketry.logger.warn("Asteroid {} has invalid distance value", asteroid.ID);
					}
				}

				node = att.getNamedItem("mass");
				if(node != null) {
					try {
						asteroid.mass = Integer.parseInt(node.getTextContent());
					} catch(NumberFormatException e) {
                        AdvancedRocketry.logger.warn("Asteroid {} has invalid mass value", asteroid.ID);
					}
				}

				node = att.getNamedItem("minLevel");
				if(node != null) {
					try {
						asteroid.minLevel = Integer.parseInt(node.getTextContent());
					} catch(NumberFormatException e) {
                        AdvancedRocketry.logger.warn("Asteroid {} has invalid minLevel value", asteroid.ID);
					}
				}

				node = att.getNamedItem("massVariability");
				if(node != null) {
					try {
						asteroid.massVariability = Float.parseFloat(node.getTextContent());
					} catch(NumberFormatException e) {
                        AdvancedRocketry.logger.warn("Asteroid {} has invalid massVariability value", asteroid.ID);
					}
				}

				node = att.getNamedItem("richness");
				if(node != null) {
					try {
						asteroid.richness = Float.parseFloat(node.getTextContent());
					} catch(NumberFormatException e) {
                        AdvancedRocketry.logger.warn("Asteroid {} has invalid richness value", asteroid.ID);
					}
				}

				node = att.getNamedItem("richnessVariability");
				if(node != null) {
					try {
						asteroid.richnessVariability = Float.parseFloat(node.getTextContent());
					} catch(NumberFormatException e) {
                        AdvancedRocketry.logger.warn("Asteroid {} has invalid richnessVariability value", asteroid.ID);
					}
				}

				node = att.getNamedItem("probability");
				if(node != null) {
					try {
						asteroid.probability = Float.parseFloat(node.getTextContent());
					} catch(NumberFormatException e) {
                        AdvancedRocketry.logger.warn("Asteroid {} has invalid probability value", asteroid.ID);
					}
				}
				
				node = att.getNamedItem("timeMultiplier");
				if(node != null) {
					try {
						asteroid.timeMultiplier = Float.parseFloat(node.getTextContent());
					} catch(NumberFormatException e) {
                        AdvancedRocketry.logger.warn("Asteroid {} has invalid timeMultiplier value", asteroid.ID);
					}
				}
				else
					asteroid.timeMultiplier = 1f;
			}
			Node asteroidNode = childNode.getFirstChild();

			while(asteroidNode != null) {
				if(asteroidNode.getNodeType() != Node.ELEMENT_NODE || !asteroidNode.getNodeName().equalsIgnoreCase("ore")) { 
					asteroidNode = asteroidNode.getNextSibling();
					continue;
				}

					NamedNodeMap att = asteroidNode.getAttributes();

					//Add itemStacks
					Node node = att.getNamedItem("itemStack");
					if(node != null) {
						ItemStack stack = getStack(node.getTextContent());
						if(stack != null)
							asteroid.itemStacks.add(stack);
						else {
                            AdvancedRocketry.logger.warn("Asteroid {} has invalid ore", asteroid.ID);
							break;
						}
					}

					node = att.getNamedItem("chance");

					if(node != null) {

						try {
							asteroid.stackProbabilites.add(Float.parseFloat(node.getTextContent()));
						} catch (NumberFormatException e) {
                            AdvancedRocketry.logger.warn("Asteroid {} has invalid ore", asteroid.ID);
							break;
						}
					}


				asteroidNode = asteroidNode.getNextSibling();
			}

			mapping.add(asteroid);

			childNode = childNode.getNextSibling();
		}

		return mapping;
	}

	public static ItemStack getStack(@NotNull String text) {
		String[] splitStr = text.split(" ");
		int meta = 0;
		int size = 1;
		//format: "name meta size"
		if(splitStr.length > 1) {
			try {
				meta = Integer.parseInt(splitStr[1]);
			} catch( NumberFormatException e) {}
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
