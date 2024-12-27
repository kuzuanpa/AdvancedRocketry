/**
 * Yeah, i know, this is litterally a copy of the item renderer, other option was asm the class responsible for render distance
 */

package zmaster587.advancedRocketry.client.render.entity;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import zmaster587.advancedRocketry.entity.EntityItemAbducted;

import java.util.Random;

@SideOnly(Side.CLIENT)
public class RendererItem extends net.minecraft.client.renderer.entity.RenderItem
{
    private static final ResourceLocation RES_ITEM_GLINT = new ResourceLocation("textures/misc/enchanted_item_glint.png");
    private final RenderBlocks renderBlocksRi = new RenderBlocks();
    /** The RNG used in RenderItem (for bobbing itemstacks on the ground) */
    private final Random random = new Random();
    public boolean renderWithColor = true;
    /** Defines the zLevel of rendering of item on GUI. */
    public float zLevel;
    public static boolean renderInFrame;
    private static final String __OBFID = "CL_00001003";

    public RendererItem()
    {
        this.shadowSize = 0.15F;
        this.shadowOpaque = 0.75F;
    }

    /**
     * Actually renders the given argument. This is a synthetic bridge method, always casting down its argument and then
     * handing it off to a worker function which does the actual work. In all probabilty, the class Render is generic
     * (Render<T extends Entity) and this method has signature public void func_76986_a(T entity, double d, double d1,
     * double d2, float f, float f1). But JAD is pre 1.5 so doesn't do that.
     */
    public void doRender(@NotNull EntityItemAbducted p_76986_1_, double p_76986_2_, double p_76986_4_, double p_76986_6_, float p_76986_8_, float p_76986_9_)
    {
        super.doRender(p_76986_1_.getItemEntity(), p_76986_2_, p_76986_4_, p_76986_6_, p_76986_8_, p_76986_9_);
    }
    
    @Override
    public void doRender(Entity p_76986_1_, double p_76986_2_, double p_76986_4_, double p_76986_6_, float p_76986_8_, float p_76986_9_)
    {
        this.doRender((EntityItemAbducted)p_76986_1_, p_76986_2_, p_76986_4_, p_76986_6_, p_76986_8_, p_76986_9_);
    }
}