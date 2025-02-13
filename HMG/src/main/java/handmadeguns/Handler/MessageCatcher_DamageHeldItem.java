package handmadeguns.Handler;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import handmadeguns.network.PacketDamageHeldItem;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.world.World;

import static handmadeguns.HandmadeGunsCore.HMG_proxy;


public class MessageCatcher_DamageHeldItem implements IMessageHandler<PacketDamageHeldItem, IMessage> {
    @Override//IMessageHandlerのメソッド
    public IMessage onMessage(PacketDamageHeldItem message, MessageContext ctx) {
        //クライアントへ送った際に、EntityPlayerインスタンスはこのように取れる。
        //EntityPlayer player = SamplePacketMod.proxy.getEntityPlayerInstance();
        //サーバーへ送った際に、EntityPlayerインスタンス（EntityPlayerMPインスタンス）はこのように取れる。
        //EntityPlayer entityPlayer = ctx.getServerHandler().playerEntity;
        //Do something.
        World world;
//        System.out.println("debug");
        if(ctx.side.isServer()) {
            world = ctx.getServerHandler().playerEntity.worldObj;
        }else{
            world = HMG_proxy.getCilentWorld();
        }
        try {
            if(world != null){
                Entity shooter = world.getEntityByID(message.shooterid);
                if(shooter != null && shooter instanceof EntityLivingBase && ((EntityLivingBase) shooter).getHeldItem() != null) {
                    if(((EntityLivingBase) shooter).getHeldItem().getItem().getMaxDamage() - ((EntityLivingBase) shooter).getHeldItem().getItemDamage() < message.value) {
                        message.value = ((EntityLivingBase) shooter).getHeldItem().getItem().getMaxDamage() - ((EntityLivingBase) shooter).getHeldItem().getItemDamage();
                    }
                    ((EntityLivingBase) shooter).getHeldItem().damageItem(message.value, (EntityLivingBase) shooter);
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
