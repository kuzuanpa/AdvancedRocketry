package zmaster587.advancedRocketry.client.render;

import cpw.mods.fml.common.FMLLog;
import gregapi.block.multitileentity.MultiTileEntityBlock;
import gregapi.data.LH;
import gregapi.tileentity.base.TileEntityBase04MultiTileEntities;
import gregapi.tileentity.base.TileEntityBase09FacingSingle;
import gregapi.tileentity.notick.TileEntityBase03MultiTileEntities;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.MovingObjectPosition;
import net.minecraftforge.common.util.ForgeDirection;
import org.apache.logging.log4j.Level;
import org.lwjgl.opengl.GL11;
import zmaster587.libVulpes.block.RotatableBlock;
import zmaster587.libVulpes.render.RenderHelper;
import zmaster587.libVulpes.tile.TileSchematic;
import zmaster587.libVulpes.tile.multiblock.TileMultiBlock;
import zmaster587.libVulpes.tile.multiblock.TilePlaceholder;

import static gregapi.block.multitileentity.MultiTileEntityRegistry.getRegistry;
import static gregapi.data.CS.OPOS;
import static org.lwjgl.opengl.GL11.*;

public class RendererPhantomBlock extends TileEntitySpecialRenderer {

	private static final RenderBlocks renderBlocks = RenderBlocks.getInstance();

	@Override
	public void renderTileEntityAt(TileEntity ti, double x,
			double y, double z, float t) {
		if(ti.isInvalid())return;

		try {
			TilePlaceholder tileGhost = (TilePlaceholder) ti;
			Block block = tileGhost.getReplacedBlock();

			if (tileGhost.getReplacedTileEntity() != null && !(tileGhost.getReplacedTileEntity() instanceof TileMultiBlock) && TileEntityRendererDispatcher.instance.hasSpecialRenderer(tileGhost.getReplacedTileEntity())) {
				GL11.glEnable(GL11.GL_BLEND);
				glBlendFunc(GL11.GL_ONE_MINUS_SRC_COLOR, GL_SRC_ALPHA);
				GL11.glColor4f(1f, 1f, 1f, 0.7f);
				TileEntityRendererDispatcher.instance.renderTileEntityAt(tileGhost.getReplacedTileEntity(), x, y, z, t);
				GL11.glDisable(GL11.GL_BLEND);
			} else if (block != null) {
				GL11.glPushMatrix();

				GL11.glTranslated(x, y, z);
				GL11.glColor4f(1, 1, 1, 1);
				if (block instanceof RotatableBlock) {
					ForgeDirection direction = ForgeDirection.getOrientation(tileGhost.getReplacedBlockMeta());
					GL11.glTranslated(.5f, .5f, .5f);
					if (direction.offsetX != 0) {
						GL11.glRotatef(-90, 0, direction.offsetX, 0);
					} else if (direction.offsetZ == 1) {
						GL11.glRotatef(180, direction.offsetZ, 0, 0);
						GL11.glRotatef(180, 0, 0, 1);
					}
					//GL11.glScalef(-1, -1, -1);
					GL11.glTranslated(-.5f, -.5f, -.5f);
				}

				//Render Each block
				Minecraft.getMinecraft().getTextureManager().bindTexture(TextureMap.locationBlocksTexture);
				if (tileGhost instanceof TileSchematic && block instanceof MultiTileEntityBlock && ((TileSchematic) tileGhost).getReplacedGTTile() != null) {
					TileEntity tileEntity = ((TileSchematic) tileGhost).getReplacedGTTile();
					if (!tileEntity.hasWorldObj()) {
						tileEntity.setWorldObj(tileGhost.getWorldObj());
						tileEntity.xCoord = tileGhost.xCoord;
						tileEntity.yCoord = tileGhost.yCoord;
						tileEntity.zCoord = tileGhost.zCoord;
					}
					if (tileEntity instanceof TileEntityBase09FacingSingle)
						((TileEntityBase09FacingSingle) tileEntity).mFacing = OPOS[(byte) tileGhost.getReplacedBlockMeta()];
					((MultiTileEntityBlock) block).overrideTileEntity = tileEntity;
				}
				renderBlocks.blockAccess = tileGhost.getWorldObj();
				renderBlocks.renderAllFaces = true;


				GL11.glEnable(GL11.GL_BLEND);
				net.minecraft.client.renderer.RenderHelper.enableStandardItemLighting();
				GL11.glBlendFunc(GL_ONE_MINUS_SRC_COLOR, GL_SRC_ALPHA);
				Tessellator.instance.startDrawingQuads();

				if (block.getRenderType() == 0) {
					block.setBlockBoundsBasedOnState(ti.getWorldObj(), ti.xCoord, ti.yCoord, ti.zCoord);
					renderBlocks.setRenderBoundsFromBlock(block);
					RenderHelper.renderStandardBlockWithColorMultiplierAndBrightness(block, 0, 0, 0, 1, 1, 1, 0.2f, (ti.getWorldObj().getLightBrightnessForSkyBlocks(ti.xCoord, ti.yCoord, ti.zCoord, 0) / 2) + 4);
				} else {
					net.minecraft.client.renderer.RenderHelper.enableGUIStandardItemLighting();
					renderBlocks.renderBlockByRenderType(block, 0, 0, 0);
				}
				Tessellator.instance.draw();
				GL11.glDisable(GL11.GL_BLEND);
				GL11.glPopMatrix();
			}

			if (block != null) {
				//If the player is mousing over this block
				MovingObjectPosition movingObjPos = Minecraft.getMinecraft().objectMouseOver;
				if (Minecraft.getMinecraft().objectMouseOver != null && movingObjPos.blockX == ti.xCoord && movingObjPos.blockY == ti.yCoord && movingObjPos.blockZ == ti.zCoord) {
					String displayName = "";
					if (tileGhost instanceof TileSchematic && !((TileSchematic) tileGhost).getReplacedBlockOverrideName().isEmpty())
						displayName = ((TileSchematic) tileGhost).getReplacedBlockOverrideName();
					else if (tileGhost.getReplacedBlock() instanceof MultiTileEntityBlock && ((MultiTileEntityBlock) tileGhost.getReplacedBlock()).overrideTileEntity != null) {
						TileEntity til = ((MultiTileEntityBlock) tileGhost.getReplacedBlock()).overrideTileEntity;
						if (til instanceof TileEntityBase03MultiTileEntities && getRegistry(((TileEntityBase03MultiTileEntities) til).getMultiTileEntityRegistryID()) == null) {
							FMLLog.log(Level.FATAL, "MultiTileEntity Reg = Null, That should not happen! Checks if your mod list is completed equal to server!");
							FMLLog.log(Level.FATAL, "MultiTileEntity RegId =" + ((TileEntityBase03MultiTileEntities) til).getMultiTileEntityRegistryID());

						} else if (til instanceof TileEntityBase04MultiTileEntities && getRegistry(((TileEntityBase04MultiTileEntities) til).getMultiTileEntityRegistryID()) == null) {
							FMLLog.log(Level.FATAL, "MultiTileEntity Reg = Null, That should not happen! Checks if your mod list is completed equal to server!");
							FMLLog.log(Level.FATAL, "MultiTileEntity RegId =" + ((TileEntityBase04MultiTileEntities) til).getMultiTileEntityRegistryID());
						} else {
							if (til instanceof TileEntityBase03MultiTileEntities)
								displayName = LH.get(getRegistry(((TileEntityBase03MultiTileEntities) til).getMultiTileEntityRegistryID()).mNameInternal + "." + ((TileEntityBase03MultiTileEntities) til).getMultiTileEntityID());
							if (til instanceof TileEntityBase04MultiTileEntities)
								displayName = LH.get(getRegistry(((TileEntityBase04MultiTileEntities) til).getMultiTileEntityRegistryID()).mNameInternal + "." + ((TileEntityBase04MultiTileEntities) til).getMultiTileEntityID());
						}
					} else {
						ItemStack stack = ti.getWorldObj().getBlock(ti.xCoord, ti.yCoord, ti.zCoord).getPickBlock(movingObjPos, Minecraft.getMinecraft().theWorld, movingObjPos.blockX, movingObjPos.blockY, movingObjPos.blockZ, Minecraft.getMinecraft().thePlayer);
						if (stack != null) displayName = stack.getDisplayName();
					}
					if (displayName != null && !displayName.isEmpty())
						RenderHelper.renderTag(Minecraft.getMinecraft().thePlayer.getDistanceSq(movingObjPos.blockX, movingObjPos.blockY, movingObjPos.blockZ), displayName, x, y, z, 10);
				}
				if (tileGhost instanceof TileSchematic && block instanceof MultiTileEntityBlock && ((TileSchematic) tileGhost).getReplacedGTTile() != null)
					((MultiTileEntityBlock) block).overrideTileEntity = null;
			}
		}catch (Exception exception){
			exception.printStackTrace();
		}
}}
