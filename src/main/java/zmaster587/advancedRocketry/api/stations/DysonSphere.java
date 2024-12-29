package zmaster587.advancedRocketry.api.stations;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;
import zmaster587.advancedRocketry.AdvancedRocketry;
import zmaster587.advancedRocketry.api.Constants;

public class DysonSphere implements IDysonSphere{
    public final static boolean T=true;
    public final static boolean F=false;
    public final static byte[] lengthXFromSize = {16,24,32,40,48};
    public final static byte[] lengthYFromSize = {8,12,16,20,24};
    public static int[] eachNodeRequiredPart = {620,1420,4800,12400,48600};
    //get the minimum level of the dyson sphere node on [size][x][y]
    protected final static byte[][][] minimumDysonSphereNodeLevel
    = new byte[][][] {
            {
                    {-1, -1, -1, -1,  4,  4,  4,  4,  4,  4,  4,  4, -1, -1, -1, -1},
                    {-1, -1,  4,  4,  4,  4,  4,  4,  4,  4,  4,  4,  4,  4, -1, -1},
                    {-1,  3,  3,  3,  3,  3,  3,  3,  3,  3,  3,  3,  3,  3,  3, -1},
                    { 2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2},
                    { 2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2},
                    {-1,  3,  3,  3,  3,  3,  3,  3,  3,  3,  3,  3,  3,  3,  3, -1},
                    {-1, -1,  4,  4,  4,  4,  4,  4,  4,  4,  4,  4,  4,  4, -1, -1},
                    {-1, -1, -1, -1,  4,  4,  4,  4,  4,  4,  4,  4, -1, -1, -1, -1}
            },
            {
                    {-1, -1, -1, -1, -1, -1, -1, -1,  4,  4,  4,  4, 4,  4,  4,  4, -1, -1, -1, -1, -1, -1, -1, -1},
                    {-1, -1, -1, -1,  3,  3,  3,  3,  3,  3,  3,  3, 3,  3,  3,  3,  3,  3,  3,  3, -1, -1, -1, -1},
                    {-1, -1,  3,  3,  3,  3,  3,  3,  3,  3,  3,  3, 3,  3,  3,  3,  3,  3,  3,  3,  3,  3, -1, -1},
                    {-1,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2, 2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2, -1},
                    { 2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2, 2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2},
                    { 1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1, 1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1},
                    { 1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1, 1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1},
                    { 2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2, 2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2},
                    {-1,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2, 2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2, -1},
                    {-1, -1,  3,  3,  3,  3,  3,  3,  3,  3,  3,  3, 3,  3,  3,  3,  3,  3,  3,  3,  3,  3, -1, -1},
                    {-1, -1, -1, -1,  3,  3,  3,  3,  3,  3,  3,  3, 3,  3,  3,  3,  3,  3,  3,  3, -1, -1, -1, -1},
                    {-1, -1, -1, -1, -1, -1, -1, -1,  4,  4,  4,  4, 4,  4,  4,  4, -1, -1, -1, -1, -1, -1, -1, -1}
            },
            {
                    {-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,  4,  4,  4,  4,  4,  4,  4,  4, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
                    {-1, -1, -1, -1, -1, -1, -1, -1,  3,  3,  3,  3,  3,  3,  3,  3,  3,  3,  3,  3,  3,  3,  3,  3, -1, -1, -1, -1, -1, -1, -1, -1},
                    {-1, -1, -1, -1,  3,  3,  3,  3,  3,  3,  3,  3,  3,  3,  3,  3,  3,  3,  3,  3,  3,  3,  3,  3,  3,  3,  3,  3, -1, -1, -1, -1},
                    {-1, -1, -1, -1,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2, -1, -1, -1, -1},
                    {-1, -1,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2, -1, -1},
                    {-1,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2, -1},
                    { 1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1},
                    { 1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1},
                    { 1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1},
                    { 1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1},
                    {-1,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2, -1},
                    {-1, -1,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2, -1, -1},
                    {-1, -1, -1, -1,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2, -1, -1, -1, -1},
                    {-1, -1, -1, -1,  3,  3,  3,  3,  3,  3,  3,  3,  3,  3,  3,  3,  3,  3,  3,  3,  3,  3,  3,  3,  3,  3,  3,  3, -1, -1, -1, -1},
                    {-1, -1, -1, -1, -1, -1, -1, -1,  3,  3,  3,  3,  3,  3,  3,  3,  3,  3,  3,  3,  3,  3,  3,  3, -1, -1, -1, -1, -1, -1, -1, -1},
                    {-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,  4,  4,  4,  4,  4,  4,  4,  4, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
            },
            {
                    {-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,  4,  4,  4,  4,  4,  4,  4,  4,  4,  4,  4,  4, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
                    {-1, -1, -1, -1, -1, -1, -1, -1, -1, -1,  4,  4,  4,  4,  4,  4,  4,  4,  4,  4,  4,  4,  4,  4,  4,  4,  4,  4,  4,  4, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
                    {-1, -1, -1, -1, -1, -1, -1, -1,  4,  4,  4,  4,  4,  4,  4,  4,  4,  4,  4,  4,  4,  4,  4,  4,  4,  4,  4,  4,  4,  4,  4,  4, -1, -1, -1, -1, -1, -1, -1, -1},
                    {-1, -1, -1, -1, -1, -1,  3,  3,  3,  3,  3,  3,  3,  3,  3,  3,  3,  3,  3,  3,  3,  3,  3,  3,  3,  3,  3,  3,  3,  3,  3,  3,  3,  3, -1, -1, -1, -1, -1, -1},
                    {-1, -1, -1, -1,  3,  3,  3,  3,  3,  3,  3,  3,  3,  3,  3,  3,  3,  3,  3,  3,  3,  3,  3,  3,  3,  3,  3,  3,  3,  3,  3,  3,  3,  3,  3,  3, -1, -1, -1, -1},
                    {-1, -1,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2, -1, -1},
                    {-1,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2, -1},
                    { 2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2},
                    { 1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1},
                    { 1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1},
                    { 1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1},
                    { 1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1},
                    { 2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2},
                    {-1,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2, -1},
                    {-1, -1,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2, -1, -1},
                    {-1, -1, -1, -1,  3,  3,  3,  3,  3,  3,  3,  3,  3,  3,  3,  3,  3,  3,  3,  3,  3,  3,  3,  3,  3,  3,  3,  3,  3,  3,  3,  3,  3,  3,  3,  3, -1, -1, -1, -1},
                    {-1, -1, -1, -1, -1, -1,  3,  3,  3,  3,  3,  3,  3,  3,  3,  3,  3,  3,  3,  3,  3,  3,  3,  3,  3,  3,  3,  3,  3,  3,  3,  3,  3,  3, -1, -1, -1, -1, -1, -1},
                    {-1, -1, -1, -1, -1, -1, -1, -1,  4,  4,  4,  4,  4,  4,  4,  4,  4,  4,  4,  4,  4,  4,  4,  4,  4,  4,  4,  4,  4,  4,  4,  4, -1, -1, -1, -1, -1, -1, -1, -1},
                    {-1, -1, -1, -1, -1, -1, -1, -1, -1, -1,  4,  4,  4,  4,  4,  4,  4,  4,  4,  4,  4,  4,  4,  4,  4,  4,  4,  4,  4,  4, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
                    {-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,  4,  4,  4,  4,  4,  4,  4,  4,  4,  4,  4,  4, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
            },
            {
                    {-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,  4,  4,  4,  4,  4,  4,  4,  4,  4,  4,  4,  4,  4,  4,  4,  4, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
                    {-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,  4,  4,  4,  4,  4,  4,  4,  4,  4,  4,  4,  4,  4,  4,  4,  4,  4,  4,  4,  4,  4,  4,  4,  4, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
                    {-1, -1, -1, -1, -1, -1, -1, -1,  4,  4,  4,  4,  4,  4,  4,  4,  4,  4,  4,  4,  4,  4,  4,  4,  4,  4,  4,  4,  4,  4,  4,  4,  4,  4,  4,  4,  4,  4,  4,  4, -1, -1, -1, -1, -1, -1, -1, -1},
                    {-1, -1, -1, -1, -1, -1,  3,  3,  3,  3,  3,  3,  3,  3,  3,  3,  3,  3,  3,  3,  3,  3,  3,  3,  3,  3,  3,  3,  3,  3,  3,  3,  3,  3,  3,  3,  3,  3,  3,  3,  3,  3, -1, -1, -1, -1, -1, -1},
                    {-1, -1, -1, -1,  3,  3,  3,  3,  3,  3,  3,  3,  3,  3,  3,  3,  3,  3,  3,  3,  3,  3,  3,  3,  3,  3,  3,  3,  3,  3,  3,  3,  3,  3,  3,  3,  3,  3,  3,  3,  3,  3,  3,  3, -1, -1, -1, -1},
                    {-1, -1, -1, -1,  3,  3,  3,  3,  3,  3,  3,  3,  3,  3,  3,  3,  3,  3,  3,  3,  3,  3,  3,  3,  3,  3,  3,  3,  3,  3,  3,  3,  3,  3,  3,  3,  3,  3,  3,  3,  3,  3,  3,  3, -1, -1, -1, -1},
                    {-1, -1,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2, -1, -1},
                    {-1, -1,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2, -1},
                    { 2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2},
                    { 1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1},
                    { 1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1},
                    { 1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1},
                    { 1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1},
                    { 1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1},
                    { 1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1},
                    { 2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2},
                    {-1, -1,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2, -1},
                    {-1, -1,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2, -1, -1},
                    {-1, -1, -1, -1,  3,  3,  3,  3,  3,  3,  3,  3,  3,  3,  3,  3,  3,  3,  3,  3,  3,  3,  3,  3,  3,  3,  3,  3,  3,  3,  3,  3,  3,  3,  3,  3,  3,  3,  3,  3,  3,  3,  3,  3, -1, -1, -1, -1},
                    {-1, -1, -1, -1,  3,  3,  3,  3,  3,  3,  3,  3,  3,  3,  3,  3,  3,  3,  3,  3,  3,  3,  3,  3,  3,  3,  3,  3,  3,  3,  3,  3,  3,  3,  3,  3,  3,  3,  3,  3,  3,  3,  3,  3, -1, -1, -1, -1},
                    {-1, -1, -1, -1, -1, -1,  3,  3,  3,  3,  3,  3,  3,  3,  3,  3,  3,  3,  3,  3,  3,  3,  3,  3,  3,  3,  3,  3,  3,  3,  3,  3,  3,  3,  3,  3,  3,  3,  3,  3,  3,  3, -1, -1, -1, -1, -1, -1},
                    {-1, -1, -1, -1, -1, -1, -1, -1,  4,  4,  4,  4,  4,  4,  4,  4,  4,  4,  4,  4,  4,  4,  4,  4,  4,  4,  4,  4,  4,  4,  4,  4,  4,  4,  4,  4,  4,  4,  4,  4, -1, -1, -1, -1, -1, -1, -1, -1},
                    {-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,  4,  4,  4,  4,  4,  4,  4,  4,  4,  4,  4,  4,  4,  4,  4,  4,  4,  4,  4,  4,  4,  4,  4,  4, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
                    {-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,  4,  4,  4,  4,  4,  4,  4,  4,  4,  4,  4,  4,  4,  4,  4,  4, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
            }                
    };
    public static byte getMinimumDysonSphereNodeLevel(byte size,int nodeX,int nodeY){return minimumDysonSphereNodeLevel[size][nodeY][nodeX];}
    public NBTTagCompound writeToNBT(){
        NBTTagCompound nbt = new NBTTagCompound();
        nbt.setInteger("size",size);
        int lengthX = lengthXFromSize[size];
        int lengthY = lengthYFromSize[size];
        int[] nodesBuildingProgressNBT = new int[lengthX*lengthY];
        byte[] nodesLevelNBT = new byte[lengthX*lengthY];
        byte[] nodesTypeNBT = new byte[lengthX*lengthY];
        for (byte y = 0; y < lengthY; y++)for (byte x = 0; x < lengthX; x++)  {
             nodesBuildingProgressNBT[y * lengthY + x] = nodesBuildingProgress[y][x] ;
             nodesLevelNBT[y * lengthY + x] = nodesLevel[y][x] ;
             nodesTypeNBT[y * lengthY + x] = nodesType[y][x] ;
        }
        nbt.setIntArray("nodesBuildingProgress",nodesBuildingProgressNBT);
        nbt.setByteArray("nodesLevel",nodesLevelNBT);
        nbt.setByteArray("nodesType",nodesTypeNBT);
        return nbt;
    }

    public DysonSphere readFromNBT(NBTTagCompound nbt){
        try {
            byte size = nbt.getByte("size");
            this.size=size;
            int lengthX = lengthXFromSize[size];
            int lengthY = lengthYFromSize[size];
            int[][] nodesBuildingProgress = new int[lengthY][lengthX];
            byte[][] nodesLevel = new byte[lengthY][lengthX];
            byte[][] nodesType = new byte[lengthY][lengthX];
            int[] progressNBTArray = nbt.getIntArray("nodesBuildingProgress");
            byte[] LevelNBTArray = nbt.getByteArray("nodesLevel");
            byte[] typeNBTArray = nbt.getByteArray("nodesType");
            for (byte y = 0; y < lengthY; y++) for (byte x = 0; x < lengthX; x++) {
                nodesBuildingProgress[y][x] = progressNBTArray[y * lengthY + x];
                nodesLevel[y][x] = LevelNBTArray[y * lengthY + x];
                nodesType[y][x] = typeNBTArray[y * lengthY + x];
            }
            this.nodesBuildingProgress = nodesBuildingProgress;
            this.nodesLevel = nodesLevel;
            this.nodesType = nodesType;
        }catch (Exception e){
            AdvancedRocketry.logger.error(e);
            return null;
        }

        return this;
    }
    public int[][] nodesBuildingProgress;
    public byte[][] nodesLevel;
    public byte[][] nodesType;
    public byte size=-1;
    public DysonSphere(){
    }
    public DysonSphere(byte size){
        this.init(size);
    }
    public static int getDisplayNodeX(byte size,int nodeX,int nodeY){
        int minusCount=0;
        for (int i = 0; i < lengthXFromSize[size]; i++) {
            if(DysonSphere.minimumDysonSphereNodeLevel[size][nodeY][i]<0)minusCount++;
            if(DysonSphere.minimumDysonSphereNodeLevel[size][nodeY][i]>0)break;
        }
        return nodeX-minusCount;
    }

    public static int getValidNodesInARow(byte size,int Y){
        int num=0;
        for (int i = 0; i < lengthXFromSize[size]; i++) {
            if(DysonSphere.minimumDysonSphereNodeLevel[size][Y][i]>0)num++;
        }
        return num;
    }

    public void updateNode(int nodeX,int nodeY,int nodeLevel,int nodeType,int buildingProcess){
        this.nodesLevel[nodeY][nodeX]= (byte) nodeLevel;
        this.nodesType[nodeY][nodeX]= (byte) nodeType;
        this.nodesBuildingProgress[nodeY][nodeX]= (byte) buildingProcess;
    }
    public void updateNodeLevel(int nodeX,int nodeY,int nodeLevel){
        this.nodesLevel[nodeY][nodeX]= (byte) nodeLevel;
    }
    public void updateNodeType(int nodeX,int nodeY,int nodeType){
        this.nodesType[nodeY][nodeX]= (byte) nodeType;
    }
    public void getNodeBuildingProgress(int nodeX,int nodeY,int buildingProcess){
        this.nodesBuildingProgress[nodeY][nodeX]=buildingProcess;
    }

    public byte getNodeLevel(int nodeX,int nodeY){
        return this.nodesLevel[nodeY][nodeX];
    }
    public byte getNodeType(int nodeX,int nodeY){
        return this.nodesType[nodeY][nodeX];
    }
    public int getNodeBuildingProgress(int nodeX,int nodeY){
        return this.nodesBuildingProgress[nodeY][nodeX];
    }
    public void drawBehindLayer(int x,int y,int distanceFromStarBase,int z,int offsetRotateZ,float scale,float distanceFromStarMultiplier,float rotate) {
        for(int nodeY=0;nodeY<DysonSphere.lengthYFromSize[this.size];nodeY++)for(int nodeX=0;nodeX<DysonSphere.getValidNodesInARow(this.size, nodeY);nodeX++) {
            GL11.glPushMatrix();
            GL11.glTranslatef(x, y, z);
            GL11.glRotatef(offsetRotateZ, 0, 0, 1);
            GL11.glRotatef(-rotate, 0, 1, 0);

            float angle=(360.0F / DysonSphere.getValidNodesInARow(this.size, nodeY)) * nodeX;
            if(angle>360){
                GL11.glPopMatrix();
                continue;
            }
            GL11.glRotatef(angle, 0, 1, 0);
            float f1=(distanceFromStarBase+(distanceFromStarMultiplier*this.size * distanceFromStarBase/2F));
            GL11.glTranslatef(0,(f1 *((float)(nodeY) / DysonSphere.lengthYFromSize[this.size]))-f1/2F, f1*0.4F*(float) Math.sin(3.14F* (nodeY+0.5F)/ (DysonSphere.lengthYFromSize[this.size])));
            GL11.glRotatef(-90*((nodeY+1-DysonSphere.lengthYFromSize[this.size]/2F) / DysonSphere.lengthYFromSize[this.size]),1,0,0);
            GL11.glScalef(scale,scale,1);
            GL11.glTranslatef(-16, -16, f1/2+0.01F);
            //if(drawNodesCoord)fontRendererObj.drawString(nodeX+","+nodeY, 2,3,0x44aaff);
            GL11.glColor4f(1,1,1,0.5F);
            Minecraft.getMinecraft().getTextureManager().bindTexture(new ResourceLocation(Constants.modId, "textures/gui/DysonSphere/node.png"));
            Tessellator tessellator = Tessellator.instance;
            tessellator.startDrawingQuads();
            tessellator.addVertexWithUV(0, 0, 0, 0, 0);
            tessellator.addVertexWithUV(0, 32, 0, 0, 0);
            tessellator.addVertexWithUV(32, 32, 0, 0, 0);
            tessellator.addVertexWithUV(32, 0, 0, 0, 0);
            tessellator.draw();
            GL11.glPopMatrix();

        }
    }
    public void drawFrontLayer(int x,int y,int distanceFromStarBase,int z,int offsetRotateZ,float scale,float distanceFromStarMultiplier,float rotate) {
        for(int nodeY=0;nodeY<DysonSphere.lengthYFromSize[this.size];nodeY++)for(int nodeX=0;nodeX<DysonSphere.getValidNodesInARow(this.size, nodeY);nodeX++) {
            GL11.glPushMatrix();
            GL11.glTranslatef(x, y, z);
            GL11.glRotatef(offsetRotateZ, 0, 0, 1);
            GL11.glRotatef(-180-rotate, 0, 1, 0);
            float angle=(360.0F / DysonSphere.getValidNodesInARow(this.size, nodeY)) * nodeX;
            if(angle>360){
                GL11.glPopMatrix();
                continue;
            }
            GL11.glRotatef(angle, 0, 1, 0);
            float f1=(distanceFromStarBase+distanceFromStarMultiplier*this.size * distanceFromStarBase/2F);
            GL11.glTranslatef(0,(f1 *((float)(nodeY) / DysonSphere.lengthYFromSize[this.size]))-f1/2F, -f1*0.4F*(float) Math.sin(3.14F* (nodeY+0.5F)/ (DysonSphere.lengthYFromSize[this.size])));
            GL11.glRotatef(90*((nodeY+1-DysonSphere.lengthYFromSize[this.size]/2F) / DysonSphere.lengthYFromSize[this.size]),1,0,0);
            GL11.glScalef(scale,scale,1);
            GL11.glTranslatef(-16, -16, -f1/2);
            //if(drawNodesCoord)fontRendererObj.drawString(nodeX+","+nodeY, 2,3,0x44aaff);
            GL11.glColor4f(1,1,1,0.5F);
            Minecraft.getMinecraft().getTextureManager().bindTexture(new ResourceLocation(Constants.modId, "textures/gui/DysonSphere/node.png"));
            Tessellator tessellator = Tessellator.instance;
            tessellator.startDrawingQuads();
            tessellator.addVertexWithUV(0, 0, 0, 0, 0);
            tessellator.addVertexWithUV(0, 32, 0, 0, 0);
            tessellator.addVertexWithUV(32, 32, 0, 0, 0);
            tessellator.addVertexWithUV(32, 0, 0, 0, 0);
            tessellator.draw();
            GL11.glPopMatrix();

        }
    }

    public DysonSphere init(byte size){
        this.size=size;
        int lengthX = lengthXFromSize[size];
        int lengthY = lengthYFromSize[size];
        int[][] nodesBuildingProgress = new int[lengthY][lengthX];
        byte[][] nodesLevel = new byte[lengthY][lengthX];
        byte[][] nodesType = new byte[lengthY][lengthX];
        for (byte y = 0; y < lengthY; y++) for (byte x = 0; x < lengthX; x++) {
            nodesBuildingProgress[y][x] = 0;
            nodesLevel[y][x] = 0;
            nodesType[y][x] = 0;
        }
        this.nodesBuildingProgress = nodesBuildingProgress;
        this.nodesLevel = nodesLevel;
        this.nodesType = nodesType;
        return this;
    }
}
