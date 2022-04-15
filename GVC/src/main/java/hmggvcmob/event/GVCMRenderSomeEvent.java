package hmggvcmob.event;

import cpw.mods.fml.common.event.FMLServerStoppedEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import hmggvcmob.entity.*;
import handmadevehicle.CLProxy;
import hmggvcmob.tile.TileEntityFlag;
import net.minecraft.client.gui.Gui;
import net.minecraft.world.World;
import net.minecraftforge.client.model.AdvancedModelLoader;
import net.minecraftforge.client.model.IModelCustom;
import net.minecraftforge.event.entity.PlaySoundAtEntityEvent;

import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static handmadevehicle.HMVehicle.HMV_Proxy;
import static hmggvcmob.GVCMobPlus.forPlayer;
import static java.lang.Math.sqrt;

public class GVCMRenderSomeEvent {

	@SideOnly(Side.CLIENT)
	@SubscribeEvent
	public void tickEvent(TickEvent.WorldTickEvent event) {
		try {
			if (event.phase == TickEvent.Phase.START && !GVCMXEntityEvent.soundedentity.isEmpty()) {
				ArrayList tempremove = new ArrayList();
				for (int i = 0; i < GVCMXEntityEvent.soundedentity.size(); i++) {
					if (GVCMXEntityEvent.soundedentity.get(i) != null) {
						GVCMXEntityEvent.soundedentity.get(i).getEntityData().setFloat("GunshotLevel", GVCMXEntityEvent.soundedentity.get(i).getEntityData().getFloat("GunshotLevel") * 0.95f);
						if (GVCMXEntityEvent.soundedentity.get(i).getEntityData().getFloat("GunshotLevel") < 0.1) {
							tempremove.add(GVCMXEntityEvent.soundedentity.get(i));
						}
					}
				}
				GVCMXEntityEvent.soundedentity.removeAll(tempremove);
			}
		}catch (Exception e){
			e.printStackTrace();
		}
	}
	@SubscribeEvent
	public void soundevent(PlaySoundAtEntityEvent event) {
		if(event.entity.getEntityData().getFloat("GunshotLevel")<event.volume) {
			event.entity.getEntityData().setFloat("GunshotLevel", event.volume);
		}
		GVCMXEntityEvent.soundedentity.add(event.entity);
	}
	public static World clientWorld;
	public static TileEntityFlag nearestCamp;
	public static double nearestCampDist;

	public static ExecutorService nearestCampFindEvent;
	public GVCMRenderSomeEvent(){
		nearestCampFindEvent = Executors.newCachedThreadPool();
		nearestCampFindEvent.execute(() -> {
			while(true) {
				try
				{
					Minecraft minecraft = FMLClientHandler.instance().getClient();
					EntityPlayer entityplayer = minecraft.thePlayer;
					if(clientWorld != null && entityplayer != null) {
						double dist = 16384;//

						TileEntityFlag closestFlag = null;
						for (Object obj : clientWorld.loadedTileEntityList) {
							if (obj instanceof TileEntityFlag) {
								TileEntityFlag tileEntity = (TileEntityFlag) obj;
								if (!tileEntity.isInvalid() && tileEntity.flagHeight >= tileEntity.campObj.maxFlagHeight / 2) {
									double tempDist = tileEntity.getDistanceFrom(entityplayer.posX, entityplayer.posY, entityplayer.posZ);
									if (tempDist < dist || dist == -1) {
										dist = tempDist;
										closestFlag = tileEntity;
									}
								}
							}
						}
						nearestCampDist = sqrt(dist);
						nearestCamp = closestFlag;
					}
					Thread.sleep(1000);
				}
				catch (Throwable e)
				{
					e.printStackTrace();
				}
			}
		});
	}

	@SideOnly(Side.CLIENT)
	@SubscribeEvent
	public void renderover(RenderGameOverlayEvent.Pre event) {
		Minecraft minecraft = FMLClientHandler.instance().getClient();
		EntityPlayer entityplayer = minecraft.thePlayer;
		
		ScaledResolution scaledresolution = new ScaledResolution(minecraft, minecraft.displayWidth,
				                                                        minecraft.displayHeight);
		int i = scaledresolution.getScaledWidth();
		int j = scaledresolution.getScaledHeight();
		clientWorld = entityplayer.worldObj;
		FontRenderer fontrenderer = minecraft.fontRenderer;
		{
			String beaconStateMessage = "lost connection";
			int color = 0x808080;
			if (nearestCamp != null) {

				if (nearestCamp.campObj.playerIsFriend) {
					color = 0x80FF80;
					beaconStateMessage = "connected to Respawn-Beacon";
				} else {
					color = 0xFF8080;
					beaconStateMessage = "detected an Enemy beacon";
				}
				beaconStateMessage = beaconStateMessage + "  " + (int)nearestCampDist + "m";
			}

			fontrenderer.drawStringWithShadow(beaconStateMessage, 0, 0, color);
		}

		if (entityplayer.ridingEntity instanceof EntityMGAX55) {
			EntityMGAX55 gear = (EntityMGAX55)entityplayer.ridingEntity;
			String weaponmode;
			int color = 0xFFFFFF;
			switch (gear.weaponMode) {
				case 0:
					if (gear.railGunChargecnt > 0) {
						color = 0xFF0000;
						weaponmode = "Rail Gun : CHRG..." + gear.railGunChargecnt;
						
					} else if (gear.railGunCoolcnt > 0) {
						color = 0xFFFF00;
						weaponmode = "Rail Gun : LD..." + gear.railGunCoolcnt;
					} else {
						weaponmode = "Rail Gun : RDY :EN " + gear.railGunMagazine;
					}
					break;
				case 1:
					if (gear.rocketMagazine <= 0) color = 0xFF0000;
					weaponmode = "AT Missile : NUM " + gear.rocketMagazine;
					break;
				case 2:
					if (gear.normalGunHeat < EntityMGAX55.normalGunHeat_Max)
						weaponmode = "Machine Gun : RDY :HEAT" + gear.normalGunHeat;
					else {
						color = 0xFF0000;
						weaponmode = "Machine Gun : OH COOLING... HEAT" + gear.normalGunHeat;
					}
					break;
				default:
					weaponmode = "Error";
			}
			fontrenderer.drawStringWithShadow("Weapon Mode : " + weaponmode, i - 300, j - 20 - 10, color);
			color = 0x00FF00;
			if (gear.health < 100) {
				color = 0xFF0000;
			}
			fontrenderer.drawStringWithShadow("Armor : " + gear.health, i - 300, j - 40 - 10, color);
		}

		GL11.glColor4f(1, 1, 1, 1);
		minecraft.getTextureManager().bindTexture(Gui.icons);
	}
}
