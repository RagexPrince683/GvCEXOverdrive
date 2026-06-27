package handmadeguns.Handler;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import handmadeguns.entity.PlacedGunEntity;
import handmadeguns.items.guns.HMGItem_Unified_Guns;
import handmadeguns.network.PacketreturnMgazineItem;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

import static handmadeguns.HandmadeGunsCore.HMG_proxy;

public class MessageCatcher_returnMagazineItem implements IMessageHandler<PacketreturnMgazineItem, IMessage> {
    @Override
    public IMessage onMessage(PacketreturnMgazineItem message, MessageContext ctx) {
        World world;
//        System.out.println("debug");
        if(ctx.side.isServer()) {
            world = ctx.getServerHandler().playerEntity.worldObj;
        }else{
            world = HMG_proxy.getCilentWorld();
        }
        try {
            if(world != null){
                Entity shooter = world.getEntityByID(message.entityid);
                if(shooter instanceof EntityPlayer && ((EntityLivingBase) shooter).getHeldItem() != null) {
                    Item gunitem = ((EntityLivingBase) shooter).getHeldItem().getItem();
                    ItemStack itemStack = ((EntityLivingBase) shooter).getHeldItem();
                    if(gunitem instanceof HMGItem_Unified_Guns){
                        HMGItem_Unified_Guns unifiedGun = (HMGItem_Unified_Guns) gunitem;
                        if(unifiedGun.gunInfo.perShellReload){
                            unifiedGun.checkTags(itemStack);
                            if(unifiedGun.remain_Bullet(itemStack) < unifiedGun.max_Bullet(itemStack) && unifiedGun.canreloadBullets(itemStack, world, shooter)){
                                itemStack.getTagCompound().setBoolean("IsReloading", true);
                                itemStack.getTagCompound().setBoolean("WaitReloading", false);
                                itemStack.getTagCompound().setInteger("RloadTime", 0);
                            }
                        }else if(unifiedGun.remain_Bullet(itemStack) > 0){
                            unifiedGun.returnInternalMagazines(itemStack,shooter);
                        }
                    }
                }else if(shooter != null && shooter.ridingEntity instanceof PlacedGunEntity){
                    HMGItem_Unified_Guns gunitem = ((PlacedGunEntity) shooter.ridingEntity).gunItem;
                    ItemStack itemStack = ((PlacedGunEntity) shooter.ridingEntity).gunStack;
                    if(gunitem != null && itemStack != null){
                        if(gunitem.gunInfo.perShellReload){
                            gunitem.checkTags(itemStack);
                            if(gunitem.remain_Bullet(itemStack) < gunitem.max_Bullet(itemStack) && gunitem.canreloadBullets(itemStack, world, shooter)){
                                itemStack.getTagCompound().setBoolean("IsReloading", true);
                                itemStack.getTagCompound().setBoolean("WaitReloading", false);
                                itemStack.getTagCompound().setInteger("RloadTime", 0);
                            }
                        }else if(gunitem.remain_Bullet(itemStack) > 0){
                            gunitem.returnInternalMagazines(itemStack,shooter);
                        }
                    }
                }
            }
//        bullet = message.bullet.setdata(bullet);
//        System.out.println("bullet "+ bullet);
        }catch (ClassCastException e) {
            e.printStackTrace();
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }
}
