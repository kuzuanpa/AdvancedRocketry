package zmaster587.advancedRocketry.util;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import net.minecraft.block.Block;
import net.minecraft.util.MathHelper;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import zmaster587.advancedRocketry.AdvancedRocketry;
import zmaster587.advancedRocketry.dimension.DimensionProperties.AtmosphereTypes;
import zmaster587.advancedRocketry.dimension.DimensionProperties.Temps;
import zmaster587.libVulpes.block.BlockMeta;
import zmaster587.libVulpes.util.BlockPosition;
import zmaster587.advancedRocketry.util.OreGenProperties.OreEntry;
import zmaster587.libVulpes.util.SingleEntry;

public class XMLOreLoader {

	@Nullable
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

	public XMLOreLoader() {
		doc = null;
	}

	/**
	 * Load the propery file looking for combinations of temp and pressure
	 * @param propertyFile
	 * @return  list of singleEntry (order MUST be preserved)
	 */
	public @NotNull List<SingleEntry<BlockPosition, OreGenProperties>> loadPropertyFile() {
		Node childNode = doc.getFirstChild().getFirstChild();
		@NotNull List<SingleEntry<BlockPosition, OreGenProperties>> mapping = new LinkedList<>();
		OreGenProperties properties;

		while(childNode != null) {

			if(childNode.getNodeType() != Node.ELEMENT_NODE || !childNode.getNodeName().equals("oreGen")) { 
				childNode = childNode.getNextSibling();
				continue;
			}

			if(childNode.hasAttributes()) {
				int pressure = -1;
				int temp = -1;
				NamedNodeMap att = childNode.getAttributes();

				Node node = att.getNamedItem("pressure");

				if(node != null) {
					try {
						pressure = MathHelper.clamp_int(Integer.parseInt(node.getTextContent()),0, AtmosphereTypes.values().length);
					} catch( NumberFormatException e ) {
                        AdvancedRocketry.logger.warn("Invalid format for pressure: \"{}\" Only numbers are allowed({})", node.getTextContent(), doc.getDocumentURI());
						childNode = childNode.getNextSibling();
						continue;
					}
				}

				node = att.getNamedItem("temp");

				if(node != null) {
					try {
						temp = MathHelper.clamp_int(Integer.parseInt(node.getTextContent()),0, Temps.values().length);
					} catch( NumberFormatException e ) {
                        AdvancedRocketry.logger.warn("Invalid format for temp: \"{}\" Only numbers are allowed({})", node.getTextContent(), doc.getDocumentURI());
						childNode = childNode.getNextSibling();
						continue;
					}
				}

				if(pressure == -1 && temp == -1) {
                    AdvancedRocketry.logger.warn("Invalid format for temp: \"{}\" Only numbers are allowed({})", node.getTextContent(), doc.getDocumentURI());
					childNode = childNode.getNextSibling();
					continue;
				}

				properties = loadOre(childNode);

				if(properties == null) {
					childNode = childNode.getNextSibling();
					continue;
				}

				if(temp != pressure) {
					if(pressure == -1) {
						mapping.add(new SingleEntry<>(new BlockPosition(-1, temp,0), properties));
					}
					else if(temp == -1) {
						mapping.add(new SingleEntry<>(new BlockPosition(pressure, -1,0), properties));
					}
				}
				else
					mapping.add(new SingleEntry<>(new BlockPosition(pressure, temp,0), properties));
				
				childNode = childNode.getNextSibling();
			}
		}
		
		return mapping;
	}

	public static @Nullable OreGenProperties loadOre(Node rootNode) {
		OreGenProperties oreGen = new OreGenProperties();
		Node childNode = rootNode.getFirstChild();

		while(childNode != null) {
			if(childNode.getNodeType() != Node.ELEMENT_NODE || !childNode.getNodeName().equals("ore"))  {
				childNode = childNode.getNextSibling();
				continue;
			}

			if(childNode.hasAttributes()) {
				String block;
				int minHeight, maxHeight, clumpSize, chancePerChunk, meta = 0;
				NamedNodeMap att = childNode.getAttributes();

				Node node = att.getNamedItem("block");

				if(node == null) {
					AdvancedRocketry.logger.warn("Missing \"block\" attribute from ore node");
					childNode = childNode.getNextSibling();
					continue;
				}
				block = node.getTextContent();

				node = att.getNamedItem("meta");

				if(node != null) {
					try {
						meta = Integer.parseInt(node.getTextContent());
					} catch(NumberFormatException e) {
						AdvancedRocketry.logger.warn("Invalid \"meta\" attribute from ore node");
						childNode = childNode.getNextSibling();
						continue;
					}
				}

				node = att.getNamedItem("minHeight");

				if(node != null) {
					try {
						minHeight = Math.max(Integer.parseInt(node.getTextContent()), 1);
					} catch(NumberFormatException e) {
						AdvancedRocketry.logger.warn("Invalid \"minHeight\" attribute from ore node");
						childNode = childNode.getNextSibling();
						continue;
					}
				}
				else {
					AdvancedRocketry.logger.warn("Missing \"minHeight\" attribute from ore node");
					childNode = childNode.getNextSibling();
					continue;
				}

				node = att.getNamedItem("maxHeight");

				if(node != null) {
					try {
						maxHeight = MathHelper.clamp_int(Integer.parseInt(node.getTextContent()),  minHeight, 0xFF);
					} catch(NumberFormatException e) {
						AdvancedRocketry.logger.warn("Invalid \"maxHeight\" attribute from ore node");
						childNode = childNode.getNextSibling();
						continue;
					}
				}
				else {
					AdvancedRocketry.logger.warn("Missing \"maxHeight\" attribute from ore node");
					childNode = childNode.getNextSibling();
					continue;
				}

				node = att.getNamedItem("clumpSize");

				if(node != null) {
					try {
						clumpSize = MathHelper.clamp_int(Integer.parseInt(node.getTextContent()),  1, 0xFF);
					} catch(NumberFormatException e) {
						AdvancedRocketry.logger.warn("Invalid \"clumpSize\" attribute from ore node");
						childNode = childNode.getNextSibling();
						continue;
					}
				}
				else {
					AdvancedRocketry.logger.warn("Missing \"clumpSize\" attribute from ore node");
					childNode = childNode.getNextSibling();
					continue;
				}

				node = att.getNamedItem("chancePerChunk");

				if(node != null) {
					try {
						chancePerChunk = MathHelper.clamp_int(Integer.parseInt(node.getTextContent()),  1, 0xFF);
					} catch(NumberFormatException e) {
						AdvancedRocketry.logger.warn("Invalid \"chancePerChunk\" attribute from ore node");
						childNode = childNode.getNextSibling();
						continue;
					}
				}
				else {
					AdvancedRocketry.logger.warn("Missing \"chancePerChunk\" attribute from ore node");
					childNode = childNode.getNextSibling();
					continue;
				}

				Block block2 = Block.getBlockFromName(block);

				if(block2 == null) {
                    AdvancedRocketry.logger.warn("{} is not a valid name for ore", block);
					childNode = childNode.getNextSibling();
					continue;
				}

				oreGen.addEntry(new BlockMeta(block2,meta), minHeight, maxHeight, clumpSize, chancePerChunk);
			}

			childNode = childNode.getNextSibling();
		}

		return oreGen.getOreEntries().isEmpty() ? null : oreGen;
	}
	
	public static String writeXML(OreGenProperties gen, int numTabs) {
		
		String outputString;
		
		StringBuilder tabLen = new StringBuilder();
		for(int i = 0; i < numTabs; i++) {
			tabLen.append("\t");
		}
		
		outputString = tabLen + "<oreGen ";
		
		return outputString;
	}
	
	public static String writeOreEntryXML(OreGenProperties gen, int numTabs) {
		
		StringBuilder outputString = new StringBuilder();
		
		StringBuilder tabLen = new StringBuilder();
		for(int i = 0; i < numTabs; i++) {
			tabLen.append("\t");
		}
		
		for(OreEntry ore : gen.getOreEntries()) {
			int meta = ore.getBlockState().getMeta();
			
			outputString.append(tabLen).append("<ore block=\"").append(Block.blockRegistry.getNameForObject(ore.getBlockState().getBlock())).append(meta == 0 ? "" : "\" meta=\"" + meta).append("\" minHeight=\"").append(ore.getMinHeight()).append("\" maxHeight=\"").append(ore.getMaxHeight()).append("\" clumpSize=\"").append(ore.getClumpSize()).append("\"").append(" chancePerChunk=\"").append(ore.getChancePerChunk()).append("\" />\n");
			
		}
		
		return outputString.toString();
	}
}