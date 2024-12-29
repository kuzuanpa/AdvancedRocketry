package zmaster587.advancedRocketry.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.settings.KeyBinding;

import org.jetbrains.annotations.NotNull;
import org.lwjgl.input.Keyboard;

import zmaster587.advancedRocketry.api.Constants;
import zmaster587.advancedRocketry.api.EntityRocketBase;
import zmaster587.advancedRocketry.entity.EntityRocket;
import zmaster587.libVulpes.LibVulpes;
import zmaster587.libVulpes.interfaces.INetworkEntity;
import zmaster587.libVulpes.network.PacketChangeKeyState;
import zmaster587.libVulpes.network.PacketEntity;
import zmaster587.libVulpes.network.PacketHandler;
import zmaster587.libVulpes.util.InputSyncHandler;
import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.client.registry.ClientRegistry;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.InputEvent;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class KeyBindings {

	boolean prevState;
		@SubscribeEvent
		public void onKeyInput(InputEvent.KeyInputEvent event) {
			final Minecraft minecraft = FMLClientHandler.instance().getClient();
			final EntityPlayerSP player = minecraft.thePlayer;


			//Prevent control when a GUI is open
			if(Minecraft.getMinecraft().currentScreen != null)// && Minecraft.getMinecraft().currentScreen instanceof GuiChat)
				return;
			
			
			//EntityRocket rocket;
			//If the space bar is pressed then send a packet to the server and launch the rocket
			/*if(/*launch.isPressed()* / false && player.ridingEntity instanceof EntityRocket && !(rocket = (EntityRocket)player.ridingEntity).isInFlight()) {
				PacketHandler.sendToServer(new PacketEntity(rocket, (byte)EntityRocket.PacketType.LAUNCH.ordinal()));
				rocket.launch();
			}*/
			
			if(player.ridingEntity instanceof EntityRocket) {
				EntityRocket rocket = (EntityRocket)player.ridingEntity;
				if(!rocket.isInFlight() && Keyboard.isKeyDown(Keyboard.KEY_SPACE)) {
					if(Minecraft.getMinecraft().inGameHasFocus && player.equals(Minecraft.getMinecraft().thePlayer)) {
						rocket.prepareLaunch();
					}
				}
				
			}
			
			if(toggleJetpack.isPressed()) {
				if(player.isSneaking())
					PacketHandler.sendToServer(new PacketChangeKeyState(1, false));
				else
					PacketHandler.sendToServer(new PacketChangeKeyState(0, false));
			}
			
			if(openRocketUI.isPressed()) {
				if(player.ridingEntity instanceof EntityRocketBase) {
					PacketHandler.sendToServer(new PacketEntity((INetworkEntity) player.ridingEntity, (byte)EntityRocket.PacketType.OPEN_GUI.ordinal()));
				}
			}
			
			if(Keyboard.isKeyDown(Keyboard.KEY_SPACE) != prevState) {
				prevState = Keyboard.isKeyDown(Keyboard.KEY_SPACE);
				InputSyncHandler.updateKeyPress(player, Keyboard.KEY_SPACE, prevState);
				PacketHandler.sendToServer(new PacketChangeKeyState(Keyboard.KEY_SPACE, prevState));
			}
		}
		
		//static KeyBinding launch = new KeyBinding("Launch", Keyboard.KEY_SPACE, "key.controls." + Constants.modId);
		static final KeyBinding toggleJetpack = new KeyBinding(LibVulpes.proxy.getLocalizedString("key.toggleJetpack"), Keyboard.KEY_X, LibVulpes.proxy.getLocalizedString("key.controls." + Constants.modId));
		static final @NotNull KeyBinding openRocketUI	= new KeyBinding(LibVulpes.proxy.getLocalizedString("key.openRocketUI"), Keyboard.KEY_C, LibVulpes.proxy.getLocalizedString("key.controls." + Constants.modId));
		
		public static void init() {
			//ClientRegistry.registerKeyBinding(launch);
			ClientRegistry.registerKeyBinding(toggleJetpack);
			ClientRegistry.registerKeyBinding(openRocketUI);
		}
	}