package handmadeguns;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Iterator;

import com.google.common.collect.Multimap;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.relauncher.ReflectionHelper;
import handmadeguns.event.GunSoundEvent;
import handmadeguns.items.GunInfo;
import handmadeguns.network.PacketSpawnParticle;
import handmadeguns.tile.TileMounter;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.IAttribute;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.entity.ai.attributes.ModifiableAttributeInstance;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

import static handmadeguns.HandmadeGunsCore.HMG_proxy;


public class CommonSideProxyHMG {
	public File ProxyFile(){
		return new File(".");
	}

	public void setuprender(){

	}
	public EntityPlayer getEntityPlayerInstance() {return null;}
	
	public void registerClientInfo(){}
	
	public void IGuiHandler(){}
	
    public void registerSomething(){}
	
	public World getCilentWorld(){
		return null;}
	public Minecraft getMCInstance() {
		return null;
	}
	public void playGUISound(String sound,float level){

	}

	public void setRightClickTimer(){
	}

	public void InitRendering() {
	}

	public void playsoundat(String sound, float soundLV, float soundSP, float tempsp, double posX, double posY, double posZ){
	}
	public void playsound_Gun(String sound, float soundLV, float soundSP,float maxdist,Entity attached,
	                          double posX,
	                          double posY,
	                          double posZ){
	}
	public void playsoundatEntity(String sound, float soundLV, float soundSP,Entity attached,boolean repeat,int time){
	
	}
	public void playsoundatEntity_reload(String sound, float soundLV, float soundSP, Entity attached, boolean repeat){
	
	}
	public void playsoundatBullet(String sound, float soundLV, float soundSP,float mindsit,float maxdist,Entity attached,boolean repeat){
	
	}
	public void registerTileEntity() {
		GameRegistry.registerTileEntity(TileMounter.class, "TileItemMounter");
		//GameRegistry.registerTileEntity(GVCTileEntityItemG36.class, "GVCTileEntitysample");
	}
	public boolean seekerOpenClose(){
		return false;
	}
	public boolean seekerOpenClose_NonStop(){
		return false;
	}
	public boolean fixkeydown(){
		return false;
	}

	public boolean upElevationKeyDown(){
		return false;
	}
	public boolean downElevationKeyDown(){
		return false;
	}
	public boolean resetElevationKeyDown(){
		return false;
	}

	public boolean ChangeMagazineTypeClick(){
		return false;
	}
	public boolean ModeKey_isPressed(){
		return false;
	}

	public boolean FClick(){
		return false;
	}
	public boolean FClick_no_stopper(){
		return false;
	}
	public boolean ADSClick(){
		return false;
	}
	public boolean ReloadKey_isPressed(){
		return false;
	}
	public boolean AttachmentKey_isPressed(){
		return false;
	}
	public void force_render_item_position(ItemStack itemStack,int i){
	}
	public void resetRightClickTimer(){
	}
	public void spawnParticles(PacketSpawnParticle message) {
	}
	public boolean rightClick(){
		return false;
	}

	public void playerSounded(Entity entity){
		GunSoundEvent.post(entity);
	}
	
	public String getFixkey(){
		return null;
	}

	public float getFOVModifier(Minecraft mc,float p_78481_1_, boolean p_78481_2_) {
		return 0;
	}

	public ItemStack[] getPrevEquippedItems(EntityLivingBase entityLivingBase){
		return null;
	}
	public void setUpModels(){

	}
	public void AddModel(Object o){
	}
}