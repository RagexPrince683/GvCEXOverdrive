package com.lulan.shincolle.utility;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityFlying;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.IEntityOwnable;
import net.minecraft.entity.boss.EntityWither;
import net.minecraft.entity.boss.IBossDisplayData;
import net.minecraft.entity.monster.EntityBlaze;
import net.minecraft.entity.passive.EntityBat;
import net.minecraft.entity.passive.EntityTameable;
import net.minecraft.entity.passive.EntityWaterMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.pathfinding.PathEntity;
import net.minecraft.pathfinding.PathPoint;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.DimensionManager;

import com.lulan.shincolle.ai.path.ShipMoveHelper;
import com.lulan.shincolle.ai.path.ShipPathEntity;
import com.lulan.shincolle.ai.path.ShipPathNavigate;
import com.lulan.shincolle.ai.path.ShipPathPoint;
import com.lulan.shincolle.entity.BasicEntityMount;
import com.lulan.shincolle.entity.BasicEntityShip;
import com.lulan.shincolle.entity.BasicEntityShipHostile;
import com.lulan.shincolle.entity.ExtendPlayerProps;
import com.lulan.shincolle.entity.IShipAttackBase;
import com.lulan.shincolle.entity.IShipAttributes;
import com.lulan.shincolle.entity.IShipFloating;
import com.lulan.shincolle.entity.IShipGuardian;
import com.lulan.shincolle.entity.IShipInvisible;
import com.lulan.shincolle.entity.IShipOwner;
import com.lulan.shincolle.handler.ConfigHandler;
import com.lulan.shincolle.item.PointerItem;
import com.lulan.shincolle.network.C2SInputPackets;
import com.lulan.shincolle.network.S2CEntitySync;
import com.lulan.shincolle.network.S2CSpawnParticle;
import com.lulan.shincolle.proxy.ClientProxy;
import com.lulan.shincolle.proxy.CommonProxy;
import com.lulan.shincolle.proxy.ServerProxy;
import com.lulan.shincolle.reference.ID;
import com.lulan.shincolle.team.TeamData;
import com.lulan.shincolle.tileentity.ITileWaypoint;
import com.lulan.shincolle.tileentity.TileEntityCrane;
import com.lulan.shincolle.tileentity.TileEntityDesk;
import com.lulan.shincolle.tileentity.TileEntitySmallShipyard;
import com.lulan.shincolle.tileentity.TileEntityWaypoint;
import com.lulan.shincolle.tileentity.TileMultiGrudgeHeavy;

import cpw.mods.fml.common.network.NetworkRegistry.TargetPoint;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class EntityHelper {
	
	private static Random rand = new Random();

	
	public EntityHelper() {}

	/**check entity is in (stand on, y+0D) liquid (not air or solid block) */
	public static boolean checkEntityIsInLiquid(Entity entity) {
		Block block = entity.worldObj.getBlock(MathHelper.floor_double(entity.posX), (int)(entity.boundingBox.minY), MathHelper.floor_double(entity.posZ));
		return BlockHelper.checkBlockIsLiquid(block);
	}
	
	/**check entity is free to move (stand in, y+0.5D) the block */
	public static boolean checkEntityIsFree(Entity entity) {
		Block block = entity.worldObj.getBlock(MathHelper.floor_double(entity.posX), (int)(entity.boundingBox.minY + 0.5D), MathHelper.floor_double(entity.posZ));
		return BlockHelper.checkBlockSafe(block);
	}
	
	/**check entity is air or underwater mob, return 0:default 1:air 2:water */
	public static int checkEntityTypeForEquipEffect(Entity entity) {
		if(entity instanceof IShipAttackBase) {
			switch(((IShipAttackBase) entity).getDamageType()) {
			case ID.ShipDmgType.AIRPLANE:
				return 1;
			case ID.ShipDmgType.SUBMARINE:
				return 2;
			default:	//default type
				return 0;
			}
		}
		else if(entity instanceof EntityWaterMob) {
			return 2;
		}
		else if(entity instanceof EntityBlaze || entity instanceof EntityWither ||
				entity instanceof EntityBat || entity instanceof EntityFlying) {
			return 1;
		}
		
		return 0;
	}
	
	/** check is same owner for ship (host's owner == target's owner) */
	public static boolean checkSameOwner(Entity enta, Entity entb) {
		int ida = getPlayerUID(enta);
		int idb = getPlayerUID(entb);

		//ida, idb != 0(other entity) or -1(ownerless ship)
		if((ida > 0 || ida < -1) && (idb > 0 || idb < -1)) {
			return (ida == idb);
		}
		
		return false;
	}
	
	/**replace isInWater, check water block with NO extend AABB */
	public static void checkDepth(IShipFloating entity) {
		Entity entityCD = (Entity) entity;
		Block BlockCheck = checkBlockWithOffset(entityCD, 0);
		double depth = 0;
		
		if(BlockHelper.checkBlockIsLiquid(BlockCheck)) {
			depth = 1;

			for(int i = 1; entityCD.posY + i < 255D; i++) {
				BlockCheck = checkBlockWithOffset(entityCD, i);
				
				if(BlockHelper.checkBlockIsLiquid(BlockCheck)) {
					depth++;
				}
				else {	//最上面碰到空氣類方塊才可以上浮, 否則不上浮
					if(BlockCheck.getMaterial() == Material.air) {
						entity.setStateFlag(ID.F.CanFloatUp, true);
					}
					else {
						entity.setStateFlag(ID.F.CanFloatUp, false);
					}
					break;
				}
			}		
			depth = depth - (entityCD.posY - (int)entityCD.posY);
		}
		else {
			depth = 0;	
			entity.setStateFlag(ID.F.CanFloatUp, false);
		}
		
		entity.setShipDepth(depth);
	}
	
	/**get block from entity position with offset */
	public static Block checkBlockWithOffset(Entity entity, int par1) {
		int blockX = MathHelper.floor_double(entity.posX);
	    int blockY = MathHelper.floor_double(entity.boundingBox.minY);
	    int blockZ = MathHelper.floor_double(entity.posZ);

	    return entity.worldObj.getBlock(blockX, blockY + par1, blockZ);    
	}
	
	/**check entity ID is not same, used in AE damage checking */
    public static boolean checkNotSameEntityID(Entity enta, Entity entb) {
		if(enta != null && entb != null) {
			return !(enta.getEntityId() - entb.getEntityId() == 0);
		}
    	
		return true;
	}

	/**check host's owner is EntityPlayer, for mod interact */
	public static boolean checkOwnerIsPlayer(EntityLivingBase ent) {
		EntityLivingBase getOwner = null;
		
		if(ent != null) {
			if(getPlayerUID(ent) > 0) {
				return true;
			}
			else if(ent instanceof EntityTameable) {
				getOwner = ((EntityTameable)ent).getOwner();
				return (getOwner instanceof EntityPlayer);
			}
		}
		
		return false;
	}
	
	/** check target entity is host's ally, SERVER SIDE ONLY */
	public static boolean checkIsAlly(Entity host, Entity target) {
		if(host != null && target != null) {
			int hostID = getPlayerUID(host);
			int tarID = getPlayerUID(target);
			
			return checkIsAlly(hostID, tarID);
		}
		
		return false;
	}
	
	/** check target entity is host's ally, SERVER SIDE ONLY */
	public static boolean checkIsAlly(int hostPID, int tarPID) {
		//mob vs mob
		if(hostPID < -1 && tarPID < -1) {
			return true;
		}
		
		//player vs mob
		if((hostPID < -1 && tarPID > 0) || (hostPID > 0 && tarPID < -1)) {
			return false;
		}
		
		//player vs player
		if(hostPID > 0 && tarPID > 0) {
			//is same owner
			if(hostPID == tarPID) return true;
			
			//not same owner, check team
			TeamData hostTeam = getTeamDataByUID(hostPID);
			TeamData tarTeam = getTeamDataByUID(tarPID);
			
			//host has team
			if(hostTeam != null && tarTeam != null) {
				List alist = hostTeam.getTeamAllyList();
				return alist.contains(tarTeam.getTeamID());
			}
		}
		
		return false;
	}
	
	/** check target entity is host's ally, SERVER SIDE ONLY */
	public static boolean checkIsBanned(Entity host, Entity target) {
		if(host != null && target != null) {
			int hostID = getPlayerUID(host);
			int tarID = getPlayerUID(target);
			
			return checkIsBanned(hostID, tarID);
		}
		
		return false;
	}
	
	/** check target entity is banned team, SERVER SIDE ONLY */
	public static boolean checkIsBanned(int hostPID, int tarPID) {
		//mob vs mob
		if(hostPID < -1 && tarPID < -1) {
			return false;
		}
		
		//player vs mob
		if((hostPID < -1 && tarPID > 0) || (hostPID > 0 && tarPID < -1)) {
			return true;
		}
		
		//player vs player
		if(hostPID > 0 && tarPID > 0) {
			TeamData hostTeam = getTeamDataByUID(hostPID);
			TeamData tarTeam = getTeamDataByUID(tarPID);
			
			//host has team
			if(hostTeam != null && tarTeam != null) {
				List alist = hostTeam.getTeamBannedList();
				return alist.contains(tarTeam.getTeamID());
			}
		}
		
		return false;
	}
	
	/** check ship is out of combat */
	public static boolean checkShipOutOfCombat(BasicEntityShip ship) {
		if(ship != null && ship.ticksExisted - ship.getCombatTick() > 128) {
			return true;
		}
		return false;
	}
	
	/** check ship is in colled list */
	public static boolean checkShipColled(int classID, ExtendPlayerProps props) {
		if(props != null && props.getColleShipList() != null && props.getColleShipList().contains(classID)) {
			return true;
		}
		return false;
	}
	
	/** check ship is in colled list */
	public static boolean checkEquipColled(int equipID, ExtendPlayerProps props) {
		if(props != null && props.getColleEquipList() != null && props.getColleEquipList().contains(equipID)) {
			return true;
		}
		return false;
	}
	
	/** check player is OP */
	public static boolean checkOP(EntityPlayer player) {
		if(player != null) {
			if(!player.worldObj.isRemote) {
				MinecraftServer server = ServerProxy.getServer();
				return server.getConfigurationManager().func_152596_g(player.getGameProfile());
			}
		}
		
		return false;
	}
	
	/** check player is holding a pointer */
	public static boolean checkInUsePointer(EntityPlayer player) {
		if(player != null) {
			if(player.inventory.getCurrentItem() != null &&
			   player.inventory.getCurrentItem().getItem() instanceof PointerItem) {
				return true;
			}
		}
		
		return false;
	}
	
	/** check target is attackable, SERVER SIDE */
	public static boolean checkAttackable(Entity target) {
		if(target != null) {
			//check unattackable list
			List<String> unatklist = ServerProxy.getUnattackableTargetClassList();
			String tarClass = target.getClass().getSimpleName();
			
			if(unatklist != null) {
				for(String s : unatklist) {
					if(s.equals(tarClass)) {  //target class is in list
						return false;
					}
				}
			}
		}

		return true;
	}
	
	/** check friendly fire (false = no damage) */
	public static boolean doFriendlyFire(IShipOwner attacker, Entity target) {
		if(attacker != null && target != null) {
			int ida = attacker.getPlayerUID();	//attacker's owner id
			int idb = getPlayerUID(target);
			
			//enable friendly fire
			if(ConfigHandler.friendlyFire) {
				//attacker = normal ship
				if(ida > 0) {
					//check is same owner
					if(ida == idb) {
						return false;
					}
				}
			}
			//no friendly fire
			else {
				//hostile vs hostile ship = no damage
				if(ida < 0 && idb < 0) {
					return false;
				}
				
				//friendly ship can't hurt player
				if(ida >= -1 && target instanceof EntityPlayer) {
					return false;
				}
				
				//no damage to ally ship
				if(checkIsAlly(ida, idb)) {
					return false;
				}
			}
		}
		
		//default setting: can damage
		return true;
	}
	
	/** get (loaded) entity by entity ID */
	public static Entity getEntityByID(int entityID, int worldID, boolean isClient) {
		World world;
		
		if(isClient) {
			world = ClientProxy.getClientWorld();
		}
		else {
			world = DimensionManager.getWorld(worldID);
		}
		
		if(world != null && entityID > 0) {
			for(Object obj: world.loadedEntityList) {
				if(((Entity)obj).getEntityId() == entityID) {
					return ((Entity)obj);
				}
			}
//			LogHelper.info("DEBUG : entity not found: eid: "+entityID+" world: "+worldID+" client: "+world.isRemote);
		}
			
		return null;
	}
	
	/** get ship entity by ship UID, server side only */
	public static BasicEntityShip getShipBySID(int sid) {
		if(sid > 0) {
			int[] data = ServerProxy.getShipWorldData(sid);
			
			if(data != null) {
				Entity getEnt = getEntityByID(data[0], data[1], false);
//				LogHelper.info("DEBUG : get ship by SID: "+data);
				if(getEnt instanceof BasicEntityShip) {
					return (BasicEntityShip) getEnt;
				}
			}
		}
		else {	//sid <= 0
			return null;
		}
		
//		LogHelper.info("DEBUG : ship not found: sid: "+sid);
		return null;
	}
	
	/** get (online) player by entity ID */
	public static EntityPlayer getEntityPlayerByID(int entityID, int worldID, boolean isClient) {
		World world;
		
		if(isClient) {
			world = ClientProxy.getClientWorld();
		}
		else {
			world = DimensionManager.getWorld(worldID);
		}
		
		if(world != null && entityID > 0) {
			for(Object obj: world.playerEntities) {
				if(((Entity)obj).getEntityId() == entityID) {
					return ((EntityPlayer)obj);
				}
			}
		}
//		LogHelper.info("DEBUG : player not found: eid: "+entityID+" world: "+worldID+" client: "+world.isRemote);
		return null;
	}
	
	/** get (online) player by player name, SERVER SIDE ONLY */
	public static EntityPlayer getEntityPlayerByName(String name) {
		if(name != null) {
			//get all worlds
			World[] worlds = ServerProxy.getServerWorld();
			
			try {
				//check world list
				for(World w : worlds) {
					//check entity list
					for(Object obj : w.playerEntities) {
						//check player name
						if(obj instanceof EntityPlayer) {
							if(((EntityPlayer) obj).getDisplayName().equals(name)) {
								return (EntityPlayer) obj;
							}
						}
					}
				}
			}
			catch(Exception e) {
				LogHelper.info("DEBUG : get EntityPlayer by name fail: "+e);
			}
		}
		
		return null;
	}
	
	/** get cached player entity by player UID, no world id check, SERVER SIDE ONLY */
	public static EntityPlayer getEntityPlayerByUID(int uid) {
		if(uid > 0) {
			//get all worlds
			World[] worlds = ServerProxy.getServerWorld();
			
			//get player data
			int peid = getPlayerEID(uid);

			//get player entity
			try {
				for(World w : worlds) {  //check all world
					for(Object obj : w.playerEntities) {  //check player entity list
						if(obj instanceof EntityPlayer && ((EntityPlayer)obj).getEntityId() == peid) {
							return (EntityPlayer) obj;
						}
					}
				}
			}
			catch(Exception e) {
				LogHelper.info("DEBUG : get EntityPlayer by name fail: "+e);
			}
		}
		
		return null;
	}
	
	/** get cached player entity id by player UID, SERVER SIDE ONLY */
	public static int getPlayerEID(int uid) {
		if(uid > 0) {
			//從server proxy抓出player uid cache
			int[] pdata = ServerProxy.getPlayerWorldData(uid);
			
			//get data
			if(pdata != null) {
				return pdata[0];
			}
		}
		
		return -1;
	}
	
	/** get player UID by entity */
	public static int getPlayerUID(Entity ent) {
		//player entity
		if(ent instanceof EntityPlayer) {
			ExtendPlayerProps extProps = (ExtendPlayerProps) ent.getExtendedProperties(ExtendPlayerProps.PLAYER_EXTPROP_NAME);
			if(extProps != null) return extProps.getPlayerUID();
		}
		
		//shincolle entity
		if(ent instanceof IShipOwner) {
			return ((IShipOwner) ent).getPlayerUID();
		}
		
		//tameable entity
		if(ent instanceof IEntityOwnable) {
			Entity owner = ((IEntityOwnable) ent).getOwner();
			//get player UID
			if(owner instanceof EntityPlayer) {
				ExtendPlayerProps extProps = (ExtendPlayerProps) owner.getExtendedProperties(ExtendPlayerProps.PLAYER_EXTPROP_NAME);
				if(extProps != null) return extProps.getPlayerUID();
			}
		}
		
		return -1;
	}
	
	/** get player is using GUI, SERVER SIDE ONLY */
	public static List<EntityPlayer> getEntityPlayerUsingGUI() {
		WorldServer[] worlds = ServerProxy.getServerWorld();
		List<EntityPlayer> plist = new ArrayList();
	
		for(World w : worlds) {
			if(w != null) {
				for(Object p : w.playerEntities) {
					ExtendPlayerProps props = EntityHelper.getExtendPlayerProps((EntityPlayer) p);
					
					if(props != null && props.isOpeningGUI()) {
						plist.add((EntityPlayer) p);
					}
				}
			}
		}
		
		return plist;
	}
	
	/** get player uuid */
	public static String getPetPlayerUUID(EntityTameable pet) {
		if(pet != null) {
			return pet.func_152113_b();
		}
		
		return null;
	}
	
	/** get player team data by player UID, SERVER SIDE ONLY */
	public static TeamData getTeamDataByUID(int uid) {
		if(uid > 0) {
			return ServerProxy.getTeamData(uid);
		}
		
		return null;
	}
	
	/** set player UID for pet, SERVER SIDE ONLY */
	public static void setPetPlayerUID(EntityPlayer player, IShipOwner pet) {
		setPetPlayerUID(getPlayerUID(player), pet);
	}
	
	/** set player UID for pet */
	public static void setPetPlayerUID(int pid, IShipOwner pet) {
		if(pet != null && pid > 0) {
			pet.setPlayerUID(pid);
		}
	}
	
	/** set owner uuid for pet by player UID and pet entity, SERVER SIDE ONLY */
	public static void setPetPlayerUUID(int pid, EntityTameable pet) {
		if(pet != null) {
			EntityPlayer owner = getEntityPlayerByUID(pid);
			setPetPlayerUUID(owner, pet);
		}
	}
	
	/** set owner uuid for pet by player entity and pet entity */
	public static void setPetPlayerUUID(EntityPlayer player, EntityTameable pet) {
		if(player != null) {
			setPetPlayerUUID(player.getUniqueID().toString(), pet);
		}
	}
	
	/** set owner uuid for pet by player uuid and pet entity */
	public static void setPetPlayerUUID(String uuid, EntityTameable pet) {
		if(pet != null) {
			pet.func_152115_b(uuid);
		}
	}
	
	/** get player extend props by player eid */
	public static ExtendPlayerProps getExtendPlayerProps(int entityID, int worldID, boolean isClient) {
		EntityPlayer player = getEntityPlayerByID(entityID, worldID, isClient);
		if(player != null) {
			return getExtendPlayerProps(player);
		}
		return null;
	}
	
	/** get player extend props by player UID */
	public static ExtendPlayerProps getExtendPlayerProps(int pid) {
		EntityPlayer player = getEntityPlayerByUID(pid);
		return getExtendPlayerProps(player);
	}
	
	/** get player extend props by player entity */
	public static ExtendPlayerProps getExtendPlayerProps(EntityPlayer player) {
		if(player != null) return (ExtendPlayerProps) player.getExtendedProperties(ExtendPlayerProps.PLAYER_EXTPROP_NAME);
		 return null;
	}
	
	/** add player ship list data */
	public static void addPlayerColledShip(int classID, EntityPlayer player) {
		ExtendPlayerProps extProps = getExtendPlayerProps(player);
		
		if(extProps != null) {
			extProps.setColleShip(classID);
		}
	}
	
	/** add player ship list data */
	public static void addPlayerColledEquip(int equipID, EntityPlayer player) {
		ExtendPlayerProps extProps = getExtendPlayerProps(player);
		
		if(extProps != null) {
			extProps.setColleEquip(equipID);
		}
	}
	
	/**set player extend props data by packets, CLIENT SIDE ONLY */
	public static void setPlayerExtProps(int[] value) {
		EntityPlayer player = ClientProxy.getClientPlayer();
		ExtendPlayerProps extProps = (ExtendPlayerProps) player.getExtendedProperties(ExtendPlayerProps.PLAYER_EXTPROP_NAME);
		
		if(extProps != null) {
			extProps.setRingActiveI(value[0]);
			extProps.setMarriageNum(value[1]);
			extProps.setPlayerUID(value[2]);
			
			//disable fly if non-active
			if(!extProps.isRingActive() && !player.capabilities.isCreativeMode && extProps.isRingFlying()) {
				LogHelper.info("DEBUG : cancel fly by right click");
				player.capabilities.isFlying = false;
				extProps.setRingFlying(false);
			}
		}
	}
	
	/**set player extend props team list by packets, CLIENT SIDE ONLY */
	public static void setPlayerExtProps(int teamid, int[] formatID, int[] teamlist, boolean[] selstate) {
		EntityPlayer player = ClientProxy.getClientPlayer();
		ExtendPlayerProps extProps = (ExtendPlayerProps) player.getExtendedProperties(ExtendPlayerProps.PLAYER_EXTPROP_NAME);
		
		if(extProps != null) {
			//set current team
			extProps.setPointerTeamID(teamid);
			
			//set formation id
			extProps.setFormatID(formatID);
			
			//set team selected
			for(int i = 0; i < 6; i++) {
				//set select state
				extProps.setSelectStateCurrentTeam(i, selstate[i]);
				
				//set ship entity
				if(teamlist[i * 2] <= 0) {
					extProps.addShipEntity(i, null, true);
				}
				else {
					Entity getEnt = getEntityByID(teamlist[i * 2], 0, true);
					
					if(getEnt instanceof BasicEntityShip) {
						extProps.addShipEntity(i, (BasicEntityShip) getEnt, true);
					}
					else {
						extProps.addShipEntity(i, null, true);
					}
				}
				
				//set ship UID
				extProps.setSIDCurrentTeam(i, teamlist[i * 2 + 1]);
				
				/**NOTE:
				 * client端可能接收到不同world的entity, 導致getEntityByID結果為null
				 * 此時會讓teamList存null, 但是sidList有存ship UID
				 * pointer item可以藉此將該slot標記為ship lost
				 * 藉此保留該slot直到切換到相同world為止
				 * 
				 * server端可正確找到entity, 以上只針對client端的狀況說明
				 */
			}//end for loop
		}//end props != null
	}
	
	/** process player sync data */
	public static void setPlayerByGUI(int value, int value2) {
		EntityPlayer player = ClientProxy.getClientPlayer();
		ExtendPlayerProps extProps = (ExtendPlayerProps) player.getExtendedProperties(ExtendPlayerProps.PLAYER_EXTPROP_NAME);
		
		if(extProps != null) {
			switch(value) {
			case 0:
				extProps.setRingActiveI(value2);
				break;
			case 1:
				extProps.setMarriageNum(value2);
				break;
			}
		}
	}
	
	/** process GUI click */
	public static void setEntityByGUI(BasicEntityShip entity, int button, int value) {
		if(entity != null) {
			switch(button) {
			case ID.B.ShipInv_Melee:
				entity.setEntityFlagI(ID.F.UseMelee, value);
				break;
			case ID.B.ShipInv_AmmoLight:
				entity.setEntityFlagI(ID.F.UseAmmoLight, value);
				break;
			case ID.B.ShipInv_AmmoHeavy:
				entity.setEntityFlagI(ID.F.UseAmmoHeavy, value);
				break;
			case ID.B.ShipInv_AirLight:
				entity.setEntityFlagI(ID.F.UseAirLight, value);
				break;
			case ID.B.ShipInv_AirHeavy:
				entity.setEntityFlagI(ID.F.UseAirHeavy, value);
				break;
			case ID.B.ShipInv_FollowMin:
				entity.setStateMinor(ID.M.FollowMin, value);
				
				//if min > max, max = min+1
				if(entity.getStateMinor(ID.M.FollowMin) >= entity.getStateMinor(ID.M.FollowMax)) {
					entity.setStateMinor(ID.M.FollowMax, value+1);
				}
				break;
			case ID.B.ShipInv_FollowMax:
				entity.setStateMinor(ID.M.FollowMax, value);
				
				//if max < min, min = max-1
				if(entity.getStateMinor(ID.M.FollowMax) <= entity.getStateMinor(ID.M.FollowMin)) {
					entity.setStateMinor(ID.M.FollowMin, value-1);
				}
				break;
			case ID.B.ShipInv_FleeHP:
				entity.setStateMinor(ID.M.FleeHP, value);
				break;
			case ID.B.ShipInv_TarAI:
				entity.setEntityFlagI(ID.F.PassiveAI, value);
				break;
			case ID.B.ShipInv_AuraEffect:
				entity.setEntityFlagI(ID.F.UseRingEffect, value);
				break;
			case ID.B.ShipInv_OnSightAI:
				entity.setEntityFlagI(ID.F.OnSightChase, value);
				break;
			case ID.B.ShipInv_PVPAI:
				entity.setEntityFlagI(ID.F.PVPFirst, value);
				break;
			case ID.B.ShipInv_AAAI:
				entity.setEntityFlagI(ID.F.AntiAir, value);
				break;
			case ID.B.ShipInv_ASMAI:
				entity.setEntityFlagI(ID.F.AntiSS, value);
				break;
			case ID.B.ShipInv_TIMEKEEPAI:
				entity.setEntityFlagI(ID.F.TimeKeeper, value);
				break;
			case ID.B.ShipInv_InvPage:
				entity.getExtProps().setInventoryPage(value);
				break;
			case ID.B.ShipInv_PickitemAI:
				entity.setEntityFlagI(ID.F.PickItem, value);
				break;
			case ID.B.ShipInv_WpStay:
				entity.setStateMinor(ID.M.WpStay, value);
				break;
			}
		}
		else {
			LogHelper.info("DEBUG : set entity by GUI fail, entity null");
		}
	}
	
	/**process Shipyard GUI click */
	public static void setTileEntityByGUI(TileEntity tile, int button, int value, int value2) {
		if(tile instanceof TileEntitySmallShipyard) {
			TileEntitySmallShipyard smalltile = (TileEntitySmallShipyard) tile;
			smalltile.setBuildType(value);
			
			//set build record
			if(value == ID.Build.EQUIP_LOOP || value == ID.Build.SHIP_LOOP) {
				int[] getMat = new int[] {0,0,0,0};
				
				for(int i = 0; i < 4; i++) {
					if(smalltile.getStackInSlot(i) != null) {
						getMat[i] = smalltile.getStackInSlot(i).stackSize;
					}
				}
				
				smalltile.setBuildRecord(getMat);
			}
			
			return;
		}
		else if(tile instanceof TileMultiGrudgeHeavy) {
			switch(button) {
			case ID.B.Shipyard_Type:		//build type
				((TileMultiGrudgeHeavy)tile).setBuildType(value);
				break;
			case ID.B.Shipyard_InvMode:		//select inventory mode
				((TileMultiGrudgeHeavy)tile).setInvMode(value);
				break;
			case ID.B.Shipyard_SelectMat:	//select material
				((TileMultiGrudgeHeavy)tile).setSelectMat(value);
				break;
			case ID.B.Shipyard_INCDEC:		//material inc,dec
				setLargeShipyardBuildMats((TileMultiGrudgeHeavy)tile, button, value, value2);
				break;
			}
		}
		else if(tile instanceof TileEntityCrane) {
			switch(button) {
			case ID.B.Crane_Load:
				((TileEntityCrane)tile).enabLoad = value == 0 ? false : true;
				break;
			case ID.B.Crane_Unload:
				((TileEntityCrane)tile).enabUnload = value == 0 ? false : true;
				break;
			case ID.B.Crane_Power:
				((TileEntityCrane)tile).isActive = value == 0 ? false : true;
				
				//power off, clear ship
				if(!((TileEntityCrane)tile).isActive) {
					((TileEntityCrane)tile).ship = null;
				}
				break;
			case ID.B.Crane_Meta:
				((TileEntityCrane)tile).checkMetadata = value == 0 ? false : true;
				break;
			case ID.B.Crane_Dict:
				((TileEntityCrane)tile).checkOredict = value == 0 ? false : true;
				break;
			case ID.B.Crane_Mode:
				((TileEntityCrane)tile).craneMode = value;
				break;
			case ID.B.Crane_Nbt:
				((TileEntityCrane)tile).checkNbt = value == 0 ? false : true;
				break;
			case ID.B.Crane_Red:
				if (value > 2) value = 0;
				((TileEntityCrane)tile).redMode = value;
				break;
			}
		}
		else {
			LogHelper.info("DEBUG : set tile entity by GUI fail: tile: "+tile);
		}	
	}
	
	/**process tile entity GUI click */
	public static void setTileEntityByGUI(TileEntity tile, int value1, int[] value3) {
		if(tile instanceof TileEntityDesk) {  //admiral desk sync
			if(value1 == ID.B.Desk_Sync) {
				((TileEntityDesk)tile).setSyncData(value3);
			}
		}
		else {
			LogHelper.info("DEBUG : set tile entity by GUI fail: tile: "+tile);
		}
	}

	/**增減large shipyard的matBuild[] */
	private static void setLargeShipyardBuildMats(TileMultiGrudgeHeavy tile, int button, int matType, int value) {
		//null check
		if(tile == null) return;
		
		int num = 0;
		int num2 = 0;
		boolean stockToBuild = true;	//false = build -> stock , true = stock -> build
		
		//value2轉換為數量
		switch(value) {
		case 0:
		case 4:
			num = 1000;
			break;
		case 1:
		case 5:
			num = 100;
			break;
		case 2:
		case 6:
			num = 10;
			break;
		case 3:
		case 7:
			num = 1;
			break;	
		}
		
		if(value > 3) stockToBuild = false;
		
		//判定num是否要修改, 再增減MatStock跟MatBuild
		if(stockToBuild) {	//matStock -> matBuild
			//材料不夠指定數量, 則num改為剩餘全部材料數量
			if(num > tile.getMatStock(matType)) num = tile.getMatStock(matType);
			//材料超過製造上限(1000), 則num降為上限數量
			if(num + tile.getMatBuild(matType) > 1000) num = 1000 - tile.getMatBuild(matType);
			
			tile.addMatStock(matType, -num);
			tile.addMatBuild(matType, num);
		}
		else {			//matBuild -> matStock
			//材料不夠指定數量, 則num改為剩餘全部材料數量
			if(num > tile.getMatBuild(matType)) num = tile.getMatBuild(matType);
			
			tile.addMatBuild(matType, -num);
			tile.addMatStock(matType, num);
		}	
	}
	
	/** update ship path navigator */
	public static void updateShipNavigator(IShipAttackBase entity) {
		//null check
		if(entity == null) return;
		
		EntityLiving entity2 = (EntityLiving) entity;
		ShipPathNavigate pathNavi = entity.getShipNavigate();
		ShipMoveHelper moveHelper = entity.getShipMoveHelper();
		
		//若有水空path, 則更新ship navigator
        if(pathNavi != null && moveHelper != null && !pathNavi.noPath()) {
        	//若同時有官方ai的路徑, 則清除官方ai路徑
        	if(!entity2.getNavigator().noPath()) {
        		entity2.getNavigator().clearPathEntity();
        	}

        	//若坐下或綁住, 則清除路徑
        	if(entity.getIsSitting() || entity.getIsLeashed()) {
        		entity.getShipNavigate().clearPathEntity();
        	}
        	else {
//            	LogHelper.info("DEBUG : AI tick: path navi update");
//            	LogHelper.info("DEBUG : AI tick: path length A "+this.getShipNavigate().getPath().getCurrentPathIndex()+" / "+this.getShipNavigate().getPath().getCurrentPathLength());
//            	LogHelper.info("DEBUG : AI tick: path length A "+this.getShipNavigate().getPath().getCurrentPathIndex());
    			
        		//用particle顯示path point
    			if(ConfigHandler.debugMode && entity2.ticksExisted % 20 == 0) {
    				ShipPathEntity pathtemp = pathNavi.getPath();
    				ShipPathPoint pointtemp;
//    				LogHelper.info("DEBUG : AI tick: path length A "+pathtemp.getCurrentPathIndex()+" / "+pathtemp.getCurrentPathLength()+" xyz: "+pathtemp.getPathPointFromIndex(0).xCoord+" "+pathtemp.getPathPointFromIndex(0).yCoord+" "+pathtemp.getPathPointFromIndex(0).zCoord+" ");
    				
    				for(int i = 0; i < pathtemp.getCurrentPathLength(); i++) {
    					pointtemp = pathtemp.getPathPointFromIndex(i);
    					//發射者煙霧特效
    			        TargetPoint point = new TargetPoint(entity2.dimension, entity2.posX, entity2.posY, entity2.posZ, 48D);
    					//路徑點畫紅色, 目標點畫綠色
    					if(i == pathtemp.getCurrentPathIndex()) {
    						CommonProxy.channelP.sendToAllAround(new S2CSpawnParticle(entity2, 32, pointtemp.xCoord +0.5D, pointtemp.yCoord + 0.5D, pointtemp.zCoord +0.5D, 0F, 0F, 0F, false), point);
    					}
    					else {
    						CommonProxy.channelP.sendToAllAround(new S2CSpawnParticle(entity2, 33, pointtemp.xCoord +0.5D, pointtemp.yCoord + 0.5D, pointtemp.zCoord +0.5D, 0F, 0F, 0F, false), point);
    					}
    				}
    			}
        	}

        	entity2.worldObj.theProfiler.startSection("ship navi");
        	pathNavi.onUpdateNavigation();
	        entity2.worldObj.theProfiler.endSection();
	        entity2.worldObj.theProfiler.startSection("ship move");
	        moveHelper.onUpdateMoveHelper();
	        entity2.worldObj.theProfiler.endSection();
		}

        if(!entity2.getNavigator().noPath()) {
//        	LogHelper.info("DEBUG : AI tick: path length B "+this.getNavigator().getPath().getCurrentPathIndex()+" / "+this.getNavigator().getPath().getCurrentPathLength());
			//用particle顯示path point
        	if(ConfigHandler.debugMode && entity2.ticksExisted % 20 == 0) {
				PathEntity pathtemp2 = entity2.getNavigator().getPath();
				PathPoint pointtemp2;
//				LogHelper.info("DEBUG : AI tick: path length B "+pathtemp2.getCurrentPathLength()+" "+pathtemp2.getPathPointFromIndex(0).xCoord+" "+pathtemp2.getPathPointFromIndex(0).yCoord+" "+pathtemp2.getPathPointFromIndex(0).zCoord+" ");
//				LogHelper.info("DEBUG : AI tick: path length B "+pathtemp2.getCurrentPathIndex());
				for(int i = 0; i < pathtemp2.getCurrentPathLength(); i++) {
					pointtemp2 = pathtemp2.getPathPointFromIndex(i);
					//發射者煙霧特效
			        TargetPoint point = new TargetPoint(entity2.dimension, entity2.posX, entity2.posY, entity2.posZ, 48D);
					//路徑點畫紅色, 目標點畫綠色
					if(i == pathtemp2.getCurrentPathIndex()) {
						CommonProxy.channelP.sendToAllAround(new S2CSpawnParticle(entity2, 16, pointtemp2.xCoord +0.5D, pointtemp2.yCoord + 0.5D, pointtemp2.zCoord +0.5D, 0F, 0F, 0F, false), point);
					}
					else {
						CommonProxy.channelP.sendToAllAround(new S2CSpawnParticle(entity2, 17, pointtemp2.xCoord +0.5D, pointtemp2.yCoord + 0.5D, pointtemp2.zCoord +0.5D, 0F, 0F, 0F, false), point);
					}
				}
			}
        }
	}
	
	@SideOnly(Side.CLIENT)
	public static MovingObjectPosition getPlayerMouseOverEntity(double dist, float duringTicks) {
		return getMouseOverEntity(ClientProxy.getMineraft().renderViewEntity, dist, duringTicks);
	}

	/**ray trace for entity: 
	 * 1. get the farest block
	 * 2. create collision box (rectangle, diagonal vertex = player and farest block)
	 * 3. get entity in this box
	 * 4. check entity on player~block sight line
	 * 5. return nearest entity
	 */
	@SideOnly(Side.CLIENT)
	public static MovingObjectPosition getMouseOverEntity(EntityLivingBase viewer, double dist, float duringTicks) {
		MovingObjectPosition lookBlock = null;
		
        if(viewer != null && viewer.worldObj != null) {
            lookBlock = viewer.rayTrace(dist, duringTicks);
            Vec3 vec3 = viewer.getPosition(duringTicks);

            //若有抓到方塊, 則d1改為抓到方塊的距離
            double d1 = dist;
            if(lookBlock != null) {
                d1 = lookBlock.hitVec.distanceTo(vec3);
            }

            Vec3 vec31 = viewer.getLook(duringTicks);
            double vec3x = vec31.xCoord * dist;
            double vec3y = vec31.yCoord * dist;
            double vec3z = vec31.zCoord * dist;
            Vec3 vec32 = vec3.addVector(vec3x, vec3y, vec3z);
            Vec3 vec33 = null;
            MovingObjectPosition lookEntity = null;
            Entity pointedEntity = null;
            
            //從玩家到目標方塊之間, 做出擴展1格的方形collision box, 抓出其中碰到的entity
            double f1 = 1D;
            List list = viewer.worldObj.getEntitiesWithinAABBExcludingEntity(viewer, viewer.boundingBox.addCoord(vec3x, vec3y, vec3z).expand(f1, f1, f1));
            double d2 = d1;

            //檢查抓到的entity, 是否在玩家~目標方塊的視線上
            for(int i = 0; i < list.size(); ++i) {
                Entity entity = (Entity)list.get(i);

                if(entity.canBeCollidedWith()) {
                	//檢查碰到的entity是否在視線上
                    double f2 = entity.getCollisionBorderSize();
                    AxisAlignedBB targetBox = entity.boundingBox.expand(f2, f2, f2);
                    MovingObjectPosition getObj = targetBox.calculateIntercept(vec3, vec32);

                    //若viewer完全塞在目標的box裡面
                    if(targetBox.isVecInside(vec3)) {
                        if(d2 >= 0D) {
                        	pointedEntity = entity;
                        	//抓到位置玩家位置或者目標box位置
                            vec33 = (getObj == null ? vec3 : getObj.hitVec);
                            //抓到距離改為0
                            d2 = 0D;
                        }
                    }
                    //其他有抓到目標的情況
                    else if(getObj != null) {
                        double d3 = vec3.distanceTo(getObj.hitVec);	//抓到距離

                        //若抓到距離在dist之內, 則判定為抓到目標
                        if(d3 < d2 || d2 == 0D) {
                        	//若抓到的是玩家自己的座騎, 且屬於不能互動的座騎
                            if(entity == viewer.ridingEntity && !entity.canRiderInteract()) {
                                //若dist設為0D, 才會抓到自己的座騎, 否則都無視座騎
                            	if(d2 == 0D) {
                                    pointedEntity = entity;
                                    vec33 = getObj.hitVec;
                                }
                            }
                            //其他非座騎entity
                            else {
                                pointedEntity = entity;
                                vec33 = getObj.hitVec;
                                d2 = d3;
                            }
                        }
                    }
                }
            }

            //若有抓到entity, 且抓到距離在視線碰到的最遠方塊之內, 才算有抓到entity
            if(pointedEntity != null && (d2 < d1 || lookBlock == null)) {
            	lookBlock = new MovingObjectPosition(pointedEntity, vec33);
            }
        }
        
        return lookBlock;
    }
	
	/** get mouseover target
	 * 
	 *  client/server both side
	 *  calc the look vector by eye height and pitch, less accuracy
	 *  
	 *  par1: check liquid block
	 *  par2: ignore collide check
	 *  par3: always return the last target hit
	 */
	public static MovingObjectPosition getMouseoverTarget(World world, EntityPlayer player, double dist, boolean par1, boolean par2, boolean par3) {
		float f = 1.0F;
        float f1 = player.prevRotationPitch + (player.rotationPitch - player.prevRotationPitch) * f;
        float f2 = player.prevRotationYaw + (player.rotationYaw - player.prevRotationYaw) * f;
        double d0 = player.prevPosX + (player.posX - player.prevPosX) * (double)f;
        double d1 = player.prevPosY + (player.posY - player.prevPosY) * (double)f + (double)(world.isRemote ? player.getEyeHeight() - player.getDefaultEyeHeight() : player.getEyeHeight()); // isRemote check to revert changes to ray trace position due to adding the eye height clientside and player yOffset differences
        double d2 = player.prevPosZ + (player.posZ - player.prevPosZ) * (double)f;
        float f3 = MathHelper.cos(-f2 * 0.017453292F - (float)Math.PI);
        float f4 = MathHelper.sin(-f2 * 0.017453292F - (float)Math.PI);
        float f5 = -MathHelper.cos(-f1 * 0.017453292F);
        float f6 = MathHelper.sin(-f1 * 0.017453292F);
        float f7 = f4 * f5;
        float f8 = f3 * f5;
        
        Vec3 vec3 = Vec3.createVectorHelper(d0, d1, d2);
        Vec3 vec31 = vec3.addVector((double)f7 * dist, (double)f6 * dist, (double)f8 * dist);
        
        return world.func_147447_a(vec3, vec31, par1, par2, par3);
	}
	
	/** set ship guard, and check guard position is not same
	 * 
	 *  GuardType = 1: guard a block
	 */
	public static void applyShipGuard(BasicEntityShip ship, int x, int y, int z) {
		if(ship != null) {
			int gx = ship.getStateMinor(ID.M.GuardX);
			int gy = ship.getStateMinor(ID.M.GuardY);
			int gz = ship.getStateMinor(ID.M.GuardZ);
			int gd = ship.getStateMinor(ID.M.GuardDim);
			
			//clear attack target
			ship.setAttackTarget(null);
			ship.setEntityTarget(null);
			
			//same guard position, cancel guard mode
			if(gx == x && gy == y && gz == z && gd == ship.worldObj.provider.dimensionId) {
				ship.setGuardedPos(-1, -1, -1, 0, 0);		//reset guard position
				ship.setGuardedEntity(null);
				ship.setStateFlag(ID.F.CanFollow, true);	//set follow
			}
			//apply guard mode
			else {
				ship.setSitting(false);						//stop sitting
				ship.setGuardedEntity(null);				//clear guard target
				ship.setGuardedPos(x, y, z, ship.worldObj.provider.dimensionId, 1);
				ship.setStateFlag(ID.F.CanFollow, false);	//stop follow
				
				if(!ship.getStateFlag(ID.F.NoFuel)) {
					//show emotes
					ship.applyEmotesReaction(5);
					
					if(ship.ridingEntity != null && ship.ridingEntity instanceof BasicEntityMount) {
						((BasicEntityMount)ship.ridingEntity).getShipNavigate().tryMoveToXYZ(x, y, z, 1D);
						((BasicEntityMount)ship.ridingEntity).getLookHelper().setLookPosition(x, y, z, 30F, 40F);
					}
					else {
						ship.getShipNavigate().tryMoveToXYZ(x, y, z, 1D);
						ship.getLookHelper().setLookPosition(x, y, z, 30F, 40F);
					}
				}
			}
		}
	}
	
	/** set ship guard, and check guard position is not same
	 * 
	 *  GuardType = 2: guard an entity
	 */
	public static void applyShipGuardEntity(BasicEntityShip ship, Entity guarded) {
		if(ship != null) {
			Entity getEnt = ship.getGuardedEntity();
			
			//same guard position, cancel guard
			if(getEnt != null && getEnt.getEntityId() == guarded.getEntityId()) {
				ship.setGuardedPos(-1, -1, -1, 0, 0);		//reset guard position
				ship.setGuardedEntity(null);
				ship.setStateFlag(ID.F.CanFollow, true);	//set follow
			}
			//apply guard
			else {
				ship.setSitting(false);						//stop sitting
				ship.setGuardedPos(-1, -1, -1, guarded.worldObj.provider.dimensionId, 2);
				ship.setGuardedEntity(guarded);
				ship.setStateFlag(ID.F.CanFollow, false);	//stop follow
				
				if(!ship.getStateFlag(ID.F.NoFuel)) {
					//show emotes
					ship.applyEmotesReaction(5);
					
					if(ship.ridingEntity != null && ship.ridingEntity instanceof BasicEntityMount) {
						((BasicEntityMount)ship.ridingEntity).getShipNavigate().tryMoveToEntityLiving(guarded, 1D);
					}
					else {
						ship.getShipNavigate().tryMoveToEntityLiving(guarded, 1D);
					}
				}
			}
		}
	}
	
	/** set ship attack target with team list */
	public static void applyTeamAttack(EntityPlayer player, int meta, Entity target) {
		if(target instanceof EntityLivingBase) {
			ExtendPlayerProps props = (ExtendPlayerProps) player.getExtendedProperties(ExtendPlayerProps.PLAYER_EXTPROP_NAME);
			BasicEntityShip[] ships = props.getShipEntityByMode(meta);
			BasicEntityMount mounts = null;
			int worldID = player.worldObj.provider.dimensionId;

			if(props != null) {
				switch(meta) {
				case 0:	//single mode
					if(ships[0] != null && ships[0].worldObj.provider.dimensionId == worldID &&
					   player.getDistanceToEntity(ships[0]) < 64F) {
						//設定ship攻擊目標
						ships[0].setSitting(false);
						ships[0].setEntityTarget(target);
						
						//若該ship有騎乘座騎, 將座騎目標也設定
						if(ships[0].ridingEntity instanceof BasicEntityMount) {
							((BasicEntityMount)ships[0].ridingEntity).setEntityTarget(target);
							//show emotes
							ships[0].applyEmotesReaction(5);
						}
					}
					break;
				case 1:		//group mode
				case 2:		//formation mode
					for(int i = 0; i < ships.length; i++) {
						if(ships[i] != null && ships[i].worldObj.provider.dimensionId == worldID &&
						   player.getDistanceToEntity(ships[i]) < 64F) {
							//設定ship攻擊目標
							ships[i].setSitting(false);
							ships[i].setEntityTarget(target);
							//show emotes
							ships[i].applyEmotesReaction(5);
							
							//若該ship有騎乘座騎, 將座騎目標也設定
							if(ships[i].ridingEntity instanceof BasicEntityMount) {
								((BasicEntityMount)ships[i].ridingEntity).setEntityTarget(target);
							}
						}
					}
					break;
				}//end switch
			}
		}
	}
	
	/** set ship move with team list
	 * 
	 *  parms: type: no use for now
	 */
	public static void applyTeamGuard(EntityPlayer player, Entity guarded, int meta, int type) {
		ExtendPlayerProps props = (ExtendPlayerProps) player.getExtendedProperties(ExtendPlayerProps.PLAYER_EXTPROP_NAME);
		BasicEntityShip[] ships = props.getShipEntityByMode(meta);
		int worldID = player.worldObj.provider.dimensionId;
		
		if(props != null) {
			//get current team formation id
			int formatID = props.getFormatIDCurrentTeam();
			
			switch(meta) {
			case 0:	//single mode
				if(ships[0] != null && ships[0].worldObj.provider.dimensionId == worldID && formatID <= 0 &&
				   player.getDistanceToEntity(ships[0]) < 64F) {
					//設定ship移動地點
					applyShipGuardEntity(ships[0], guarded);
					//sync guard
					CommonProxy.channelE.sendTo(new S2CEntitySync(ships[0], 3), (EntityPlayerMP) player);
				}
				break;
			case 1:		//group mode
				for(int i = 0;i < ships.length; i++) {
					if(ships[i] != null && ships[i].worldObj.provider.dimensionId == worldID && formatID <= 0 &&
					   player.getDistanceToEntity(ships[i]) < 64F) {
						//設定ship移動地點
						applyShipGuardEntity(ships[i], guarded);
						//sync guard
						CommonProxy.channelE.sendTo(new S2CEntitySync(ships[i], 3), (EntityPlayerMP) player);
					}
				}
				break;
			case 2:		//formation mode
				if(props.getNumberOfShip(props.getPointerTeamID()) > 4) {
					for(int i = 0;i < ships.length; i++) {
						if(ships[i] != null && ships[i].worldObj.provider.dimensionId == worldID &&
						   player.getDistanceToEntity(ships[i]) < 64F) {
							//設定ship移動地點
							applyShipGuardEntity(ships[i], guarded);
							//sync guard
							CommonProxy.channelE.sendTo(new S2CEntitySync(ships[i], 3), (EntityPlayerMP) player);
						}
					}
				}
				break;
			}//end switch
		}		
	}

	/** set ship move with team list 
	 *  parms: 0:meta 1:guard type 2:posX 3:posY 4:posZ
	 *  
	 *  1:guard type: no use for now
	 */
	public static void applyTeamMove(EntityPlayer player, int[] parms) {
		ExtendPlayerProps props = (ExtendPlayerProps) player.getExtendedProperties(ExtendPlayerProps.PLAYER_EXTPROP_NAME);
		BasicEntityShip[] ships = props.getShipEntityByMode(parms[0]);
		int worldID = player.worldObj.provider.dimensionId;
		
		if(props != null && ships != null && ships.length > 0) {
			//get current team formation id
			int formatID = props.getFormatIDCurrentTeam();
			
			switch(parms[0]) {
			case 0:	//single mode
				if(ships[0] != null && ships[0].worldObj.provider.dimensionId == worldID && formatID <= 0 &&
				   player.getDistanceToEntity(ships[0]) < 64F) {
					//設定ship移動地點
					applyShipGuard(ships[0], parms[2], parms[3], parms[4]);
					//sync guard
					CommonProxy.channelE.sendTo(new S2CEntitySync(ships[0], 3), (EntityPlayerMP) player);
				}
				break;
			case 1:		//group mode
				for(int i = 0;i < ships.length; i++) {
					if(ships[i] != null && ships[i].worldObj.provider.dimensionId == worldID && formatID <= 0 &&
					   player.getDistanceToEntity(ships[i]) < 64F) {
						//設定ship移動地點
						applyShipGuard(ships[i], parms[2], parms[3], parms[4]);
						//sync guard
						CommonProxy.channelE.sendTo(new S2CEntitySync(ships[i], 3), (EntityPlayerMP) player);
					}
				}
				break;
			case 2:		//formation mode
				if(props.getNumberOfShip(props.getPointerTeamID()) > 4 || formatID == 0) {
					boolean canMove = true;
					
					for(BasicEntityShip s : ships) {
						if(s != null) {
							//check formation id is same, distance < 64, same dimension
							if(s.getStateMinor(ID.M.FormatType) != formatID ||
							   player.getDistanceToEntity(s) > 64F ||
							   player.dimension != s.dimension) {
								canMove = false;
								break;
							}
						}
					}
					
					if(canMove) {
						//set formation position
						FormationHelper.applyFormationMoving(ships, formatID, parms[2], parms[3], parms[4]);
					}
				}
				break;
			}//end switch
		}
	}
	
	/** set ship sitting with team list, only called at server side
	 *  1. 若目標不在隊伍, 則單獨設定目標坐下
	 *  2. 若目標在隊伍, 則依照 pointer類型設定目標坐下
	 */
	public static void applyTeamSit(EntityPlayer player, int meta, int shipUID) {
		ExtendPlayerProps props = (ExtendPlayerProps) player.getExtendedProperties(ExtendPlayerProps.PLAYER_EXTPROP_NAME);
		BasicEntityShip[] ships = props.getShipEntityByMode(meta);
		int worldID = player.worldObj.provider.dimensionId;

		if(props != null) {
			//不在隊伍名單裡面
			if(props.checkIsInCurrentTeam(shipUID) < 0 && meta < 3) {
				BasicEntityShip target = getShipBySID(shipUID);
				
				target.setEntitySit();
			}
			//有在隊伍中, 則依照pointer類型抓目標
			else {
				switch(meta) {
				case 0:	//single mode
					if(ships[0] != null && ships[0].worldObj.provider.dimensionId == worldID) {
						//設定ship sit
						ships[0].setEntitySit();
					}
					break;
				case 1:		//group mode
				case 2:		//formation mode
					for(int i = 0; i < ships.length; i++) {
						if(ships[i] != null && ships[i].worldObj.provider.dimensionId == worldID) {
							//設定ship sit
							ships[i].setEntitySit();
						}
					}
					break;
				}//end switch
			}//end not in team
		}
	}
	
	/** set ship select (focus) with team list, only called at server side
	 */
	public static void applyTeamSelect(EntityPlayer player, int meta, int shipUID) {
		ExtendPlayerProps props = (ExtendPlayerProps) player.getExtendedProperties(ExtendPlayerProps.PLAYER_EXTPROP_NAME);
		
		if(props != null) {
			int i = props.checkIsInCurrentTeam(shipUID);
			
			//check entity is in team
			if(i >= 0) {
				switch(meta) {
				case 0:  //single mode (僅一隻可以focus)
					/**single mode不能取消focus, 一定有一隻會是focus狀態*/
					props.clearSelectStateCurrentTeam();
					props.setSelectStateCurrentTeam(i, true);
					break;
				case 1:  //group mode (不限focus數量)
					props.setSelectStateCurrentTeam(i, !props.getSelectStateCurrentTeam(i));
					break;
				}
			}
			
			//sync team list to client
			props.sendSyncPacket(0);
		}	
	}
	
	/** set emotes to all nearby hostile ships */
	public static void applyShipEmotesAOEHostile(World world, double x, double y, double z, double range, int emotesType) {
		//server side only
		if(!world.isRemote) {
			//get ship entity
            AxisAlignedBB box = AxisAlignedBB.getBoundingBox(x-range, y-range, z-range, x+range, y+range, z+range);
            List<BasicEntityShipHostile> slist = world.getEntitiesWithinAABB(BasicEntityShipHostile.class, box);
            
            if(slist != null) {
                for(BasicEntityShipHostile s : slist) {
                	if(s.isEntityAlive()) s.applyEmotesReaction(emotesType);
                }
            }
		}
	}
	
	/** set emotes to all nearby ships
	 * 
	 *  emotes type:
	 *  0: caress head (owner)
  	 *  1: caress head (other)
  	 *  2: damaged
  	 *  3: attack
  	 *  4: idle
  	 *  5: command
  	 *  6: shock
	 */
	public static void applyShipEmotesAOE(World world, double x, double y, double z, double range, int emotesType) {
		//server side only
		if(!world.isRemote) {
			//get ship entity
            AxisAlignedBB box = AxisAlignedBB.getBoundingBox(x-range, y-range, z-range, x+range, y+range, z+range);
            List<BasicEntityShip> slist = world.getEntitiesWithinAABB(BasicEntityShip.class, box);
            
            if(slist != null) {
                for(BasicEntityShip s : slist) {
                	if(s.isEntityAlive()) s.applyEmotesReaction(emotesType);
                }
            }
		}
	}
	
	/** set emotes to all nearby ships with owner checking
	 * 
	 *  emotes type:
	 *  0: caress head (owner)
  	 *  1: caress head (other)
  	 *  2: damaged
  	 *  3: attack
  	 *  4: idle
  	 *  5: command
  	 *  6: shock
	 */
	public static void applyShipEmotesAOECheckOwner(World world, double x, double y, double z, double range, int emotesType, int ownerUID) {
		//server side only
		if(!world.isRemote) {
			//get ship entity
            AxisAlignedBB box = AxisAlignedBB.getBoundingBox(x-range, y-range, z-range, x+range, y+range, z+range);
            List<BasicEntityShip> slist = world.getEntitiesWithinAABB(BasicEntityShip.class, box);
            
            if(slist != null) {
                for(BasicEntityShip s : slist) {
                	if(s.isEntityAlive() && s.getPlayerUID() == ownerUID) {
                		s.applyEmotesReaction(emotesType);
                	}
                }
            }
		}
	}
	
	/** apply emotes to all entity in list */
	public static void applyEmotesAOE(List entlist, int emotes) {
		if(entlist != null && !entlist.isEmpty()) {
			Entity s = (Entity) entlist.get(0);
			TargetPoint point = new TargetPoint(s.dimension, s.posX, s.posY, s.posZ, 48D);
			
			for(Object o : entlist) {
				if(s instanceof Entity) {
					CommonProxy.channelP.sendToAllAround(new S2CSpawnParticle((Entity) o, 36, ((Entity) o).height * 0.6F, 0, emotes), point);
				}
			}
		}
	}
	
	/** apply waypoint moving
  	 * 
  	 *  1. if guard position = waypoint block, get next waypoint
  	 *  2. if next waypoint = ship's last waypoint, get block's last waypoint (backwards mode)
  	 *  3. if no next/last waypoint, stop
  	 */
	public static boolean updateWaypointMove(IShipGuardian entity)
	{
		boolean updatePos = false;

  		//in guard block mode
  		if (!entity.getStateFlag(ID.F.CanFollow) && entity.getGuardedPos(1) > 0 && !entity.getIsSitting() && !entity.getIsLeashed() && !entity.getIsRiding())
  		{
  			//check distance < 3 blocks
  			float dx = (float) (entity.getGuardedPos(0) + 0.5D - ((Entity)entity).posX);
  			float dy = (float) (entity.getGuardedPos(1) - ((Entity)entity).posY);
			float dz = (float) (entity.getGuardedPos(2) + 0.5D - ((Entity)entity).posZ);
			dx = dx * dx;
			dy = dy * dy;
			dz = dz * dz;
  			double distsq = dx + dy + dz;
  			
  			//get target block
	  		TileEntity tile = ((Entity)entity).worldObj.getTileEntity(entity.getGuardedPos(0), entity.getGuardedPos(1), entity.getGuardedPos(2));
	  		
  			//is waypoint block
  			if (tile instanceof TileEntityCrane)
  			{
  				//ship wait for craning (xz < 2 blocks, y < 5 blocks)
  				if (distsq < 25D)
				{
  					if (entity.getStateMinor(ID.M.CraneState) == 0) {
  						entity.setStateMinor(ID.M.CraneState, 1);
  						
  						//強制騎乘者設為可裝卸狀態
  						if (((Entity)entity).riddenByEntity instanceof IShipGuardian)
  						{
  							((IShipGuardian)((Entity)entity).riddenByEntity).setStateMinor(ID.M.CraneState, 1);
  							((Entity)entity).riddenByEntity.mountEntity(null);
  						}
  					}
				}
  				else
  				{
  					//go to below crane
  	  				entity.getShipNavigate().tryMoveToXYZ(entity.getGuardedPos(0) + 0.5D, entity.getGuardedPos(1) - 2D, entity.getGuardedPos(2) + 0.5D, 1D);
  				}
  			}
  			else
  			{
  				//cancel craning
  				entity.setStateMinor(ID.M.CraneState, 0);
  				
				if (((Entity)entity).riddenByEntity instanceof IShipGuardian)
				{
					((IShipGuardian)((Entity)entity).riddenByEntity).setStateMinor(ID.M.CraneState, 0);
				}
  			}
  			
  			//is waypoint block
  			if (tile instanceof TileEntityWaypoint)
  			{
  	  			if (distsq < 9D)
  	  			{
	  	  			try
  	  				{
  	  					updatePos = applyNextWaypoint((TileEntityWaypoint) tile, entity, true, 16);
	  	  				
	  	  				//set follow dist
	  	  				if (updatePos)
	  	  				{
	  	  					entity.setStateMinor(ID.M.FollowMin, 2);
	  	  					entity.getShipNavigate().tryMoveToXYZ(entity.getGuardedPos(0) + 0.5D, entity.getGuardedPos(1), entity.getGuardedPos(2) + 0.5D, 1D);
	  	  				}
	  	  				
	  	  				return updatePos;
  	  				}
  	  				catch (Exception e)
  	  				{
  	  					e.printStackTrace();
  	  				}
  	  			}
  	  			else
  	  			{
  	  				if (entity.getTickExisted() % 128 == 0)
  	  				{
  	  					entity.getShipNavigate().tryMoveToXYZ(entity.getGuardedPos(0) + 0.5D, entity.getGuardedPos(1), entity.getGuardedPos(2) + 0.5D, 1D);
  	  				}
  	  			}
  				
  			}
  		}//end in guard mode
  		
  		return updatePos;
  	}
	
	/** set next waypoint by checking last and next waypoint, return true if changed */
	public static boolean applyNextWaypoint(ITileWaypoint tile, IShipGuardian entity, boolean checkWpStay, int checkDelay)
	{
		boolean changed = false;
		boolean timeout = !checkWpStay;
		int[] next = tile.getNextWaypoint();
		int[] last = tile.getLastWaypoint();
		int[] shiplast = entity.getLastWaypoint();
		
		//check waypoint stay time
		int wpstay = entity.getWpStayTime();
		
		//stay until timeout
		if (checkWpStay)
		{
			//check wp tile stay time and ship wp stay time, get the longer one
			int staytimemax = Math.max(entity.getWpStayTimeMax(), tile.getWpStayTime());
			
			if(wpstay < staytimemax)
			{
				entity.setWpStayTime(wpstay + checkDelay);
			}
			else
			{
				timeout = true;
			}
		}
		
		//timeout, go to next wp
		if (timeout)
		{
			entity.setWpStayTime(0);
			
			//check next == last
			if (next[1] > 0 && next[0] == shiplast[0] && next[1] == shiplast[1] && next[2] == shiplast[2])
			{
				//if no last waypoint, go to next waypoint
				if (last[1] <= 0)
				{
					//go to next waypoint
					if (next[1] > 0)
					{
						entity.setGuardedPos(next[0], next[1], next[2], ((Entity)entity).dimension, 1);
						changed = true;
						
						if (((Entity)entity).riddenByEntity instanceof IShipGuardian)
						{
							((IShipGuardian)((Entity)entity).riddenByEntity).setGuardedPos(next[0], next[1], next[2], ((Entity)entity).dimension, 1);
						}
					}
				}
				else
				{
					//go to last waypoint (backwards mode)
					entity.setGuardedPos(last[0], last[1], last[2], ((Entity)entity).dimension, 1);
					changed = true;
					
					if (((Entity)entity).riddenByEntity instanceof IShipGuardian)
					{
						((IShipGuardian)((Entity)entity).riddenByEntity).setGuardedPos(last[0], last[1], last[2], ((Entity)entity).dimension, 1);
					}
				}
			}
			else {
				//go to next waypoint
				if (next[1] > 0)
				{
					entity.setGuardedPos(next[0], next[1], next[2], ((Entity)entity).dimension, 1);
					changed = true;
					
					if (((Entity)entity).riddenByEntity instanceof IShipGuardian)
					{
						((IShipGuardian)((Entity)entity).riddenByEntity).setGuardedPos(next[0], next[1], next[2], ((Entity)entity).dimension, 1);
					}
				}
			}
			
			//set last waypoint
			entity.setLastWaypoint(new int[] {((TileEntity)tile).xCoord, ((TileEntity)tile).yCoord, ((TileEntity)tile).zCoord});
			
			if (((Entity)entity).riddenByEntity instanceof IShipGuardian)
			{
				((IShipGuardian)((Entity)entity).riddenByEntity).setLastWaypoint(new int[] {((TileEntity)tile).xCoord, ((TileEntity)tile).yCoord, ((TileEntity)tile).zCoord});
			}
		}
		
		return changed;
	}
	
	public static boolean canDodge(IShipAttributes ent, float dist) {
		if(ent != null && !((Entity)ent).worldObj.isRemote) {
			int dodge = (int) ent.getEffectEquip(ID.EF_DODGE);
			Entity ent2 = (Entity) ent;
			
			if(ent instanceof IShipInvisible && dist > 36F) {  //dist > 6 blocks
				dodge += (int) ((IShipInvisible)ent).getInvisibleLevel();
				//check limit
				if(dodge > (int) ConfigHandler.limitShipEffect[6]) dodge = (int) ConfigHandler.limitShipEffect[6];
			}
			
			//roll dodge
			if(rand.nextInt(101) <= dodge) {
				//spawn miss particle
				TargetPoint point = new TargetPoint(ent2.dimension, ent2.posX, ent2.posY, ent2.posZ, 32D);
				CommonProxy.channelP.sendToAllAround(new S2CSpawnParticle(ent2, 34, false), point);
				return true;
			}
		}
		
		return false;
	}
	
	/** command process: ship change owner
	 * 
	 *    1. (done) check command sender is OP (server)
	 *    2. (done) check owner exists (server)
	 *    3. (done) send sender eid to client (s to c)
	 *    4. check sender mouse over target is ship (client)
	 *    5. send ship eid to server (c to s)
	 *    6. change ship's owner UUID and PlayerUID (server)
	 */
	@SideOnly(Side.CLIENT)
	public static void processShipChangeOwner(int senderEID, int ownerEID) {
		//get sender entity
		EntityPlayer sender = getEntityPlayerByID(senderEID, 0, true);
		
		if(sender != null) {
			//get sender's mouse over target
			MovingObjectPosition hitObj = getPlayerMouseOverEntity(32D, 1F);
			
			if(hitObj != null && hitObj.entityHit instanceof BasicEntityShip) {
				//send change owner packet to server
				sender.addChatMessage(new ChatComponentText("Command: ShipChangeOwner: ship: "+EnumChatFormatting.AQUA+hitObj.entityHit));
				CommonProxy.channelG.sendToServer(new C2SInputPackets(C2SInputPackets.PID.CmdChOwner, ownerEID, hitObj.entityHit.getEntityId(), hitObj.entityHit.worldObj.provider.dimensionId));
			}//end get target ship
			else {
				sender.addChatMessage(new ChatComponentText("Command: ShipChangeOwner: entity is not ship!"));
			}
		}
	}
	
	/** command process: show ship info */
	@SideOnly(Side.CLIENT)
	public static void processShowShipInfo(int senderEID) {
		//get sender entity
		EntityPlayer sender = getEntityPlayerByID(senderEID, 0, true);
		
		if(sender != null) {
			//get sender's mouse over target
			MovingObjectPosition hitObj = getPlayerMouseOverEntity(32D, 1F);
			
			if(hitObj != null && hitObj.entityHit instanceof BasicEntityShip) {
				BasicEntityShip ship = (BasicEntityShip) hitObj.entityHit;
				
				//show ship info
				sender.addChatMessage(new ChatComponentText("Command: ShipInfo: User: "+EnumChatFormatting.LIGHT_PURPLE+
						sender.getDisplayName()+EnumChatFormatting.RESET+
						" UID: "+EnumChatFormatting.AQUA+EntityHelper.getPlayerUID(sender)+EnumChatFormatting.RESET+
						" UUID: "+EnumChatFormatting.GOLD+sender.getUniqueID()));
				sender.addChatMessage(new ChatComponentText("Ship Name: "+EnumChatFormatting.AQUA+ship.getCustomNameTag()));
				sender.addChatMessage(new ChatComponentText("Ship EntityID: "+EnumChatFormatting.GOLD+ship.getEntityId()));
				sender.addChatMessage(new ChatComponentText("Ship UID: "+EnumChatFormatting.GREEN+ship.getShipUID()));
				sender.addChatMessage(new ChatComponentText("Ship Owner UID: "+EnumChatFormatting.RED+ship.getPlayerUID()));
				sender.addChatMessage(new ChatComponentText("Ship Owner UUID: "+EnumChatFormatting.YELLOW+EntityHelper.getPetPlayerUUID(ship)));
				sender.addChatMessage(new ChatComponentText("Morale: "+EnumChatFormatting.YELLOW+ship.getStateMinor(ID.M.Morale)));
			}
		}
	}
	
	/** command process: set ship attrs
	 * 
	 *  cmdData: 0:sender eid, 1:ship level, 2~7:bonus value
	 * 
	 *    1. (done) check command sender is OP (server)
	 *    2. (done) send sender eid to client (s to c)
	 *    3. check sender mouse over target is ship (client)
	 *    4. send ship eid to server (c to s)
	 *    5. change ship's attributes (server)
	 */
	@SideOnly(Side.CLIENT)
	public static void processSetShipAttrs(int[] cmdData) {
		//get sender entity
		EntityPlayer sender = getEntityPlayerByID(cmdData[0], 0, true);
		
		if(sender != null) {
			//get sender's mouse over target
			MovingObjectPosition hitObj = getPlayerMouseOverEntity(16D, 1F);
			
			if(hitObj != null && hitObj.entityHit instanceof BasicEntityShip) {
				BasicEntityShip ship = (BasicEntityShip) hitObj.entityHit;
				
				if(cmdData.length == 8) {
					//show msg
					sender.addChatMessage(new ChatComponentText("Command: ShipAttrs: Set ship value: LV: "+
											EnumChatFormatting.LIGHT_PURPLE+cmdData[1]+EnumChatFormatting.RESET+" BonusValue: "+
											EnumChatFormatting.RED+cmdData[2]+" "+cmdData[3]+" "+cmdData[4]+" "+
											cmdData[5]+" "+cmdData[6]+" "+cmdData[7]));
					sender.addChatMessage(new ChatComponentText("Target Ship: "+EnumChatFormatting.AQUA+ship));
					
					//send packet to server: entity id, world id, level, bonus 1~6
					CommonProxy.channelG.sendToServer(new C2SInputPackets(C2SInputPackets.PID.CmdShipAttr,
							ship.getEntityId(), ship.worldObj.provider.dimensionId, cmdData[1], cmdData[2],
							cmdData[3], cmdData[4], cmdData[5], cmdData[6], cmdData[7]));
				}
				else if(cmdData.length == 2) {
					//show msg
					sender.addChatMessage(new ChatComponentText("Command: ShipAttrs: Set ship value: LV: "+
											EnumChatFormatting.LIGHT_PURPLE+cmdData[1]));
					sender.addChatMessage(new ChatComponentText("Target Ship: "+EnumChatFormatting.AQUA+ship));
					
					//send packet to server: entity id, world id, level, bonus 1~6
					CommonProxy.channelG.sendToServer(new C2SInputPackets(C2SInputPackets.PID.CmdShipAttr,
							ship.getEntityId(), ship.worldObj.provider.dimensionId, cmdData[1]));
				}
				
			}
		}
	}
	
	/** count entity number in the world
	 *  type: 0:boss ship, 1:mob ship, 2:all hostile ship
	 */
	public static int getEntityNumber(int type, World world)
	{
		int count = 0;
		
		if (world != null)
		{
			for (Object obj: world.loadedEntityList)
			{
				switch(type)
				{
				case 1:   //boss ship
					if (obj instanceof IBossDisplayData)
					{
						count++;
					}
					break;
				case 2:   //all hostile ship
					if (obj instanceof BasicEntityShipHostile)
					{
						count++;
					}
					break;
				default:  //small or large mob ship
					if (obj instanceof BasicEntityShipHostile && !(obj instanceof IBossDisplayData))
					{
						count++;
					}
					break;
				}
			}
		}//end world not null
		
		return count;
	}

	/** clear both rider and mount */
  	public static void clearMountSeat(EntityLiving host)
  	{
		//若座位2有人, 要先把座位2的乘客踢掉
  		if (host.ridingEntity != null)
  		{
  			if (host.ridingEntity instanceof BasicEntityMount)
  			{
	  			BasicEntityMount mount = (BasicEntityMount) host.ridingEntity;
	  			if (mount.seat2 != null)
	  			{
	  				mount.seat2.setRiderNull();
	  			}
  			}
  			
			host.ridingEntity.riddenByEntity = null;
			host.ridingEntity = null;
  		}
  		
  		//清空騎乘的人
  		if (host.riddenByEntity != null)
  		{
  			//如果是其他船, 解除騎乘時需重設定位置
  			if (host.riddenByEntity instanceof BasicEntityShip)
  			{
  				host.riddenByEntity.setPosition(host.posX, host.posY, host.posZ);
  			}
  			
			host.riddenByEntity.ridingEntity = null;
			host.riddenByEntity = null;
  		}
  	}
	
  	
}