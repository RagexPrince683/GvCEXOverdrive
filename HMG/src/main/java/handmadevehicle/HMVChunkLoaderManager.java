package handmadevehicle;

import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.ListMultimap;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import handmadeguns.entity.bullets.HMGEntityBulletBase;
import handmadevehicle.entity.parts.Hasmode;
import handmadevehicle.entity.parts.IDriver;
import handmadevehicle.entity.parts.IVehicle;
import handmadevehicle.entity.parts.Modes;
import handmadevehicle.entity.parts.logics.BaseLogic;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.ChunkCoordIntPair;
import net.minecraft.world.EnumDifficulty;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeChunkManager;
import net.minecraftforge.event.entity.EntityEvent;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public class HMVChunkLoaderManager  implements ForgeChunkManager.LoadingCallback, ForgeChunkManager.OrderedLoadingCallback, ForgeChunkManager.PlayerOrderedLoadingCallback {
	public static final HMVChunkLoaderManager INSTANCE = new HMVChunkLoaderManager();

	private HMVChunkLoaderManager(){}

	@SubscribeEvent
	public void entityEnteredChunk(EntityEvent.EnteringChunk event)
	{

		if(event.entity instanceof IDriver && event.entity instanceof EntityLiving && event.entity.worldObj.difficultySetting != EnumDifficulty.PEACEFUL){
			if(((EntityLiving) event.entity).getAttackTarget() != null && ((IDriver) event.entity).getLinkedVehicle() != null)
			{
//			System.out.println("debug" + event.newChunkX + " , " + event.newChunkZ);
				BaseLogic loader = ((IDriver) event.entity).getLinkedVehicle();

				if(loader != null)loader.forceChunkLoading(event.newChunkX, event.newChunkZ);
			}
			if(event.entity instanceof Hasmode && ((Hasmode) event.entity).getMobMode() != Modes.Wait &&
					((Hasmode) event.entity).getMoveToPos() != null && event.entity.getDistanceSq(
					((Hasmode) event.entity).getMoveToPos().x,((Hasmode) event.entity).getMoveToPos().y,((Hasmode) event.entity).getMoveToPos().z) > 2500)
			{
				BaseLogic loader = ((IDriver) event.entity).getLinkedVehicle();

				if(loader != null)loader.forceChunkLoading(event.newChunkX, event.newChunkZ);
			}
		}
		if(event.entity instanceof HMGEntityBulletBase && ((HMGEntityBulletBase) event.entity).chunkLoaderBullet){
//			System.out.println("debug" + event.newChunkX + " , " + event.newChunkZ);

			((HMGEntityBulletBase) event.entity).forceChunkLoading(event.newChunkX, event.newChunkZ);
		}
	}

	/**指定範囲のChunkCoordIntPairを新たに取得*/
	public void getChunksAround(Set<ChunkCoordIntPair> set, int xChunk, int zChunk, int radius)
	{
		set.clear();
		for(int xx = xChunk - radius; xx <= xChunk + radius; xx++)
		{
			for(int zz = zChunk - radius; zz <= zChunk + radius; zz++)
			{
				set.add(new ChunkCoordIntPair(xx, zz));
			}
		}
	}

	public ForgeChunkManager.Ticket getNewTicket(World world, ForgeChunkManager.Type type)
	{
		return ForgeChunkManager.requestTicket(HMVehicle.INSTANCE, world, type);
	}

	@Override
	public List<ForgeChunkManager.Ticket> ticketsLoaded(List<ForgeChunkManager.Ticket> tickets, World world, int maxTicketCount)
	{
		Set set = new HashSet();
		for(ForgeChunkManager.Ticket ticket : tickets)
		{
			if(ticket.getEntity() instanceof IVehicle)
			{
				set.add(ticket);
				continue;
			}
			NBTTagCompound nbt = ticket.getModData();

			if(nbt.hasKey("TYPE"))
			{
				set.add(ticket);
				continue;
			}
		}
		List ticketList = new LinkedList();
		ticketList.addAll(set);
		return ticketList;
	}

	@Override
	public void ticketsLoaded(List<ForgeChunkManager.Ticket> tickets, World world)
	{

	}

	@Override
	public ListMultimap<String, ForgeChunkManager.Ticket> playerTicketsLoaded(ListMultimap<String, ForgeChunkManager.Ticket> tickets, World world)
	{
		return LinkedListMultimap.create();
	}

	public static void writeData(ForgeChunkManager.Ticket ticket, TileEntity tile)
	{
		NBTTagCompound nbt = ticket.getModData();
		nbt.setString("TYPE", "TileEntity");
		nbt.setInteger("BlockX", tile.xCoord);
		nbt.setInteger("BlockY", tile.yCoord);
		nbt.setInteger("BlockZ", tile.zCoord);
	}
}