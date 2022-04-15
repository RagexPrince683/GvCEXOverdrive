package handmadeguns.Handler;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import handmadeguns.entity.PlacedGunEntity;
import handmadeguns.network.PacketSendPlacedGunStack;
import net.minecraft.entity.Entity;
import net.minecraft.world.World;

import static handmadeguns.HandmadeGunsCore.HMG_proxy;

public class MessageCatcher_PlacedGunStack implements IMessageHandler<PacketSendPlacedGunStack, IMessage> {
	@Override
	public IMessage onMessage(PacketSendPlacedGunStack message, MessageContext ctx) {

		World world;
//        System.out.println("debug");
		if(ctx.side.isServer()) {
			world = ctx.getServerHandler().playerEntity.worldObj;
		}else{
			world = HMG_proxy.getCilentWorld();
		}
		try {
			if(world != null){
				Entity tgt = world.getEntityByID(message.shooterID);
				if(tgt instanceof PlacedGunEntity){
					((PlacedGunEntity) tgt).gunStack = message.stack;
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