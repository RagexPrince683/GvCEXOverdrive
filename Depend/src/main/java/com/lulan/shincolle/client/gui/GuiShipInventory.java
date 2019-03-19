package com.lulan.shincolle.client.gui;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import com.lulan.shincolle.client.gui.inventory.ContainerShipInventory;
import com.lulan.shincolle.entity.BasicEntityShip;
import com.lulan.shincolle.entity.BasicEntityShipCV;
import com.lulan.shincolle.entity.IShipInvisible;
import com.lulan.shincolle.entity.destroyer.EntityDestroyerIkazuchi;
import com.lulan.shincolle.entity.destroyer.EntityDestroyerInazuma;
import com.lulan.shincolle.handler.ConfigHandler;
import com.lulan.shincolle.network.C2SGUIPackets;
import com.lulan.shincolle.proxy.CommonProxy;
import com.lulan.shincolle.reference.ID;
import com.lulan.shincolle.reference.Reference;
import com.lulan.shincolle.reference.Values;
import com.lulan.shincolle.utility.CalcHelper;
import com.lulan.shincolle.utility.GuiHelper;
import com.lulan.shincolle.utility.LogHelper;
import com.lulan.shincolle.utility.ParticleHelper;

/** ship inventory gui
 * 
 */
public class GuiShipInventory extends GuiContainer {

	public BasicEntityShip entity;
	public InventoryPlayer player;
	private float xMouse, yMouse;
	private int xClick, yClick;
	private static final ResourceLocation TEXTURE_BG = new ResourceLocation(Reference.TEXTURES_GUI+"GuiShipInventory.png");
	private static final ResourceLocation TEXTURE_ICON = new ResourceLocation(Reference.TEXTURES_GUI+"GuiNameIcon.png");
	
	//draw string
	private List mouseoverList;
	private String titlename, shiplevel, lvMark, hpMark, canMelee, canLATK, canHATK, canALATK, canAHATK, 
	               strATK, strAATK, strLATK, strHATK, strALATK, strAHATK, strDEF, strSPD, strMOV, strHIT, 
	               Kills, Exp, Grudge, Owner, AmmoLight, AmmoHeavy, AirLight, AirHeavy, TarAI,
	               overText, marriage, followMin, followMax, fleeHP, followMinValue, followMaxValue,
	               fleeHPValue, barPosValue, auraEffect, strOnSight, strPVP, strAA, strASM, Formation,
	               strAttrAtk1, strAttrAtk2, strAttrDEF, strAttrSPD, strAttrMOV, strAttrHIT, strMiKills,
	               strMiExp, strMiAirL, strMiAirH, strMiAmmoL, strMiAmmoH, strMiGrudge, strAttrCri,
	               strAttrDHIT, strAttrTHIT, strAttrAA, strAttrASM, strAttrMiss, strAttrMissA, strAttrMissR,
	               strAttrDodge, strAttrFPos, strAttrFormat, strAttrWedding, strAttrWedTrue, strAttrWedFalse,
	               strTimeKeep, strM0, strM1, strM2, strM3, strM4, strPick, strWpStay, strWpStayValue;
	private int hpCurrent, hpMax, color, showPage, showPageAI, pageIndicator, pageIndicatorAI, showAttack,
				fMinPos, fMaxPos, fleeHPPos, barPos, mousePressBar, shipType, shipClass, showPageInv,
				wpStayPos;
	private boolean switchMelee, switchLight, switchHeavy, switchAirLight, switchAirHeavy,
					switchTarAI, mousePress, switchAura, switchOnSight, switchPVP, switchAA, switchASM,
					switchTimeKeep, switchPick;
	private int[][] iconXY;  //icon array:  [ship type, ship name][file,x,y]
	

	public GuiShipInventory(InventoryPlayer invPlayer, BasicEntityShip entity1)
	{
		super(new ContainerShipInventory(invPlayer, entity1));
		
		this.mouseoverList = new ArrayList();			
		this.entity = entity1;
		this.player = invPlayer;
		this.xSize = 256;
		this.ySize = 214;
		this.showPage = 1;			//show page 1
		this.showPageAI = 1;		//show AI control page 1
		this.showPageInv = 0;		//get inventory number
		this.showAttack = 1;		//show attack 1
		this.mousePress = false;	//no key clicked
		this.mousePressBar = -1;	//no bar pressed
		
		if (this.entity != null)
		{
			this.shipType = this.entity.getShipType();
			this.shipClass = this.entity.getShipClass();
			
			//special name icon
			if (this.entity.ridingEntity instanceof EntityDestroyerInazuma ||
				this.entity.riddenByEntity instanceof EntityDestroyerIkazuchi)
			{
				this.shipType = ID.ShipType.HEAVY_CRUISER;
				this.shipClass = ID.Ship.Raiden;
			}
			
			this.iconXY = new int[2][3];
			this.iconXY[0] = Values.ShipTypeIconMap.get((byte)this.shipType);
			this.iconXY[1] = Values.ShipNameIconMap.get((short)this.shipClass);
		}
		
		//general string
		lvMark = I18n.format("gui.shincolle:level");
		hpMark = I18n.format("gui.shincolle:hp");
		strM0 = I18n.format("gui.shincolle:morale0");
		strM1 = I18n.format("gui.shincolle:morale1");
		strM2 = I18n.format("gui.shincolle:morale2");
		strM3 = I18n.format("gui.shincolle:morale3");
		strM4 = I18n.format("gui.shincolle:morale4");
		
		//attrs string
		strAttrAtk1 = I18n.format("gui.shincolle:firepower1");
		strAttrAtk2 = I18n.format("gui.shincolle:firepower2");
		strAttrDEF = I18n.format("gui.shincolle:armor");
		strAttrSPD = I18n.format("gui.shincolle:attackspeed");
		strAttrMOV = I18n.format("gui.shincolle:movespeed");
		strAttrHIT = I18n.format("gui.shincolle:range");
		strAttrCri = I18n.format("gui.shincolle:critical");
		strAttrDHIT = I18n.format("gui.shincolle:doublehit");
		strAttrTHIT = I18n.format("gui.shincolle:triplehit");
		strAttrAA = I18n.format("gui.shincolle:antiair");
		strAttrASM = I18n.format("gui.shincolle:antiss");
		strAttrMiss = I18n.format("gui.shincolle:missrate");
		strAttrMissA = I18n.format("gui.shincolle:missrateair");
		strAttrMissR = I18n.format("gui.shincolle:missreduce");
		strAttrDodge = I18n.format("gui.shincolle:dodge");
		strAttrFPos = I18n.format("gui.shincolle:formation.position");
		strAttrFormat = I18n.format("gui.shincolle:formation.formation");
		strAttrWedding = I18n.format("gui.shincolle:marriage");
		strAttrWedTrue = I18n.format("gui.shincolle:married");
		strAttrWedFalse = I18n.format("gui.shincolle:unmarried");
		
		//minor string
		strMiKills = I18n.format("gui.shincolle:kills");
		strMiExp = I18n.format("gui.shincolle:exp");
		strMiAmmoL = I18n.format("gui.shincolle:ammolight");
		strMiAmmoH = I18n.format("gui.shincolle:ammoheavy");
		strMiGrudge = I18n.format("gui.shincolle:grudge");
		strMiAirL = I18n.format("gui.shincolle:airplanelight");
		strMiAirH = I18n.format("gui.shincolle:airplaneheavy");
		
		//formation
		I18n.format("gui.shincolle:formation.position");
		
		//AI string
		canMelee = I18n.format("gui.shincolle:canmelee");
		canLATK = I18n.format("gui.shincolle:canlightattack");
		canHATK = I18n.format("gui.shincolle:canheavyattack");
		canALATK = I18n.format("gui.shincolle:canairlightattack");
		canAHATK = I18n.format("gui.shincolle:canairheavyattack");
		auraEffect = I18n.format("gui.shincolle:auraeffect");
		followMin = I18n.format("gui.shincolle:followmin");
		followMax = I18n.format("gui.shincolle:followmax");
		fleeHP = I18n.format("gui.shincolle:fleehp");
		TarAI = I18n.format("gui.shincolle:targetAI");
		strOnSight = I18n.format("gui.shincolle:onsightAI");
		strPVP = I18n.format("gui.shincolle:ai.pvp");
		strAA = I18n.format("gui.shincolle:ai.aa");
		strASM = I18n.format("gui.shincolle:ai.asm");
		strTimeKeep = I18n.format("gui.shincolle:ai.timekeeper");
		strPick = I18n.format("gui.shincolle:ai.pickitem");
		strWpStay = I18n.format("gui.shincolle:ai.wpstay");
		
	}
	
	//GUI前景: 文字 
	@Override
	protected void drawGuiContainerForegroundLayer(int i, int j) {
		//取得gui顯示名稱
		titlename = entity.getCustomNameTag();	//get type name from nbt
		
		//畫出字串 parm: string, x, y, color, (是否dropShadow)
		//draw entity name (title) 
		this.fontRendererObj.drawString(titlename, 8, 6, 0);

		drawAttributes();	
		
		handleHoveringText();
	}

	//GUI背景: 背景圖片
	@Override
	protected void drawGuiContainerBackgroundLayer(float par1,int par2, int par3) {
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);	//RGBA
		
		//draw background
        Minecraft.getMinecraft().getTextureManager().bindTexture(TEXTURE_BG);
        drawTexturedModalRect(guiLeft, guiTop, 0, 0, xSize, ySize);
        
        //draw banned inventory icon
        switch(this.entity.getInventoryPageSize()) {
        case 2:
        	break;
        case 1:
        	drawTexturedModalRect(guiLeft+62, guiTop+90, 80, 214, 6, 34);
        	break;
    	default:
    		drawTexturedModalRect(guiLeft+62, guiTop+54, 80, 214, 6, 34);
    		drawTexturedModalRect(guiLeft+62, guiTop+90, 80, 214, 6, 34);
    		break;
        }
        
        //draw inventory page indicator
        this.showPageInv = this.entity.getExtProps().getInventoryPage();
        switch(this.showPageInv) {
        case 1:  //page 1
        	this.pageIndicator = 54;
        	break;
        case 2:  //page 2
        	this.pageIndicator = 90;
        	break;
        default: //page 0
        	this.pageIndicator = 18;
        	break;
        }
        drawTexturedModalRect(guiLeft+62, guiTop+this.pageIndicator, 74, 214, 6, 34);
        
        //draw page indicator
        switch(this.showPage) {
        case 1:	//page 1
        	this.pageIndicator = 18;
        	break;
        case 2:	//page 2
        	this.pageIndicator = 54;
        	break;
        case 3:	//page 3
        	this.pageIndicator = 90;
        	break;
        }
        drawTexturedModalRect(guiLeft+135, guiTop+this.pageIndicator, 74, 214, 6, 34);
        
        //draw AI page indicator
        switch(this.showPageAI) {
        case 1:	{	//page 1
        	this.pageIndicator = 239;
        	this.pageIndicatorAI = 131;
        	
        	//get button value
            this.switchMelee = this.entity.getStateFlag(ID.F.UseMelee);
        	this.switchLight = this.entity.getStateFlag(ID.F.UseAmmoLight);
            this.switchHeavy = this.entity.getStateFlag(ID.F.UseAmmoHeavy);
            this.switchAirLight = this.entity.getStateFlag(ID.F.UseAirLight);
            this.switchAirHeavy = this.entity.getStateFlag(ID.F.UseAirHeavy);
            this.switchAura = this.entity.getStateFlag(ID.F.UseRingEffect);
            
            //draw button
            if(this.switchMelee) {
            	drawTexturedModalRect(guiLeft+174, guiTop+132, 0, 214, 11, 11);
            }
            else {
            	drawTexturedModalRect(guiLeft+174, guiTop+132, 11, 214, 11, 11);
            }
            
            if(entity.getAttackType(ID.F.AtkType_Light)) {
	            if(this.switchLight) {
	            	drawTexturedModalRect(guiLeft+174, guiTop+144, 0, 214, 11, 11);
	            }
	            else {
	            	drawTexturedModalRect(guiLeft+174, guiTop+144, 11, 214, 11, 11);
	            }
            }
            
            if(entity.getAttackType(ID.F.AtkType_Heavy)) {
	            if(this.switchHeavy) {
	            	drawTexturedModalRect(guiLeft+174, guiTop+156, 0, 214, 11, 11);
	            }
	            else {
	            	drawTexturedModalRect(guiLeft+174, guiTop+156, 11, 214, 11, 11);
	            }
            }
            
            if(entity.getAttackType(ID.F.AtkType_AirLight)) {
	            if(this.switchAirLight) {
	            	drawTexturedModalRect(guiLeft+174, guiTop+168, 0, 214, 11, 11);
	            }
	            else {
	            	drawTexturedModalRect(guiLeft+174, guiTop+168, 11, 214, 11, 11);
	            }
            }
            
            if(entity.getAttackType(ID.F.AtkType_AirHeavy)) {
	            if(this.switchAirHeavy) {
	            	drawTexturedModalRect(guiLeft+174, guiTop+180, 0, 214, 11, 11);
	            }
	            else {
	            	drawTexturedModalRect(guiLeft+174, guiTop+180, 11, 214, 11, 11);
	            }
            }
            
            if(entity.getAttackType(ID.F.HaveRingEffect)) {
	            if(this.switchAura) {
	            	drawTexturedModalRect(guiLeft+174, guiTop+192, 0, 214, 11, 11);
	            }
	            else {
	            	drawTexturedModalRect(guiLeft+174, guiTop+192, 11, 214, 11, 11);
	            }
            }
            
            break;
        	}	
        case 2: {	//page 2
        	this.pageIndicator = 239;
        	this.pageIndicatorAI = 157;
        	
        	//get button value
        	fMinPos = (int)(((entity.getStateMinor(ID.M.FollowMin) - 1) / 30F) * 42F);
        	fMaxPos = (int)(((entity.getStateMinor(ID.M.FollowMax) - 2) / 30F) * 42F);
        	fleeHPPos = (int)((entity.getStateMinor(ID.M.FleeHP) / 100F) * 42F);
        	
        	//draw range bar
        	drawTexturedModalRect(guiLeft+191, guiTop+148, 31, 214, 43, 3);
        	drawTexturedModalRect(guiLeft+191, guiTop+172, 31, 214, 43, 3);
        	drawTexturedModalRect(guiLeft+191, guiTop+196, 31, 214, 43, 3);
        	
        	//draw range indicator
        	if(this.mousePressBar == 0) {
        		drawTexturedModalRect(guiLeft+187+barPos, guiTop+145, 22, 214, 9, 9);
        	}
        	else {
        		drawTexturedModalRect(guiLeft+187+fMinPos, guiTop+145, 22, 214, 9, 9);
        	}
        	
        	if(this.mousePressBar == 1) {
        		drawTexturedModalRect(guiLeft+187+barPos, guiTop+169, 22, 214, 9, 9);
        	}
        	else {
        		drawTexturedModalRect(guiLeft+187+fMaxPos, guiTop+169, 22, 214, 9, 9);
        	}
        	
        	if(this.mousePressBar == 2) {
        		drawTexturedModalRect(guiLeft+187+barPos, guiTop+193, 22, 214, 9, 9);
        	}
        	else {
        		drawTexturedModalRect(guiLeft+187+fleeHPPos, guiTop+193, 22, 214, 9, 9);
        	}
        	
        	break;
        	}
        case 3:	{	//page 3
        	this.pageIndicator = 239;
    		this.pageIndicatorAI = 183;
    		
    		//get button value
            this.switchTarAI = this.entity.getStateFlag(ID.F.PassiveAI);
    		this.switchOnSight = this.entity.getStateFlag(ID.F.OnSightChase);
    		this.switchPVP = this.entity.getStateFlag(ID.F.PVPFirst);
    		this.switchAA = this.entity.getStateFlag(ID.F.AntiAir);
    		this.switchASM = this.entity.getStateFlag(ID.F.AntiSS);
    		this.switchTimeKeep = this.entity.getStateFlag(ID.F.TimeKeeper);
    		
    		//draw button
            if(this.switchTarAI) {
            	drawTexturedModalRect(guiLeft+174, guiTop+132, 0, 214, 11, 11);
            }
            else {
            	drawTexturedModalRect(guiLeft+174, guiTop+132, 11, 214, 11, 11);
            }
            if(this.switchOnSight) {
            	drawTexturedModalRect(guiLeft+174, guiTop+144, 0, 214, 11, 11);
            }
            else {
            	drawTexturedModalRect(guiLeft+174, guiTop+144, 11, 214, 11, 11);
            }
            if(this.switchPVP) {
            	drawTexturedModalRect(guiLeft+174, guiTop+156, 0, 214, 11, 11);
            }
            else {
            	drawTexturedModalRect(guiLeft+174, guiTop+156, 11, 214, 11, 11);
            }
            if(this.switchAA) {
            	drawTexturedModalRect(guiLeft+174, guiTop+168, 0, 214, 11, 11);
            }
            else {
            	drawTexturedModalRect(guiLeft+174, guiTop+168, 11, 214, 11, 11);
            }
            if(this.switchASM) {
            	drawTexturedModalRect(guiLeft+174, guiTop+180, 0, 214, 11, 11);
            }
            else {
            	drawTexturedModalRect(guiLeft+174, guiTop+180, 11, 214, 11, 11);
            }
            if(this.switchTimeKeep) {
            	drawTexturedModalRect(guiLeft+174, guiTop+192, 0, 214, 11, 11);
            }
            else {
            	drawTexturedModalRect(guiLeft+174, guiTop+192, 11, 214, 11, 11);
            }
  
    		break;
    		}
        case 4:	{	//page 4
        	this.pageIndicator = 246;
    		this.pageIndicatorAI = 131;
    		
            //draw button
            if (this.entity.getStateFlag(ID.F.CanPickItem))
            {
            	this.switchPick = this.entity.getStateFlag(ID.F.PickItem);
            	if (this.switchPick)
                {
                	drawTexturedModalRect(guiLeft+174, guiTop+132, 0, 214, 11, 11);
                }
                else
                {
                	drawTexturedModalRect(guiLeft+174, guiTop+132, 11, 214, 11, 11);
                }
            }
            
            
    		break;
        	}
        case 5: {	//page 5
        	this.pageIndicator = 246;
        	this.pageIndicatorAI = 157;
        	
        	//get button value
        	wpStayPos = (int)(entity.getStateMinor(ID.M.WpStay) * 0.0625F * 42F);
//        	LogHelper.info("AAAAAAAAAAAA "+wpStayPos);
        	//draw range bar
        	drawTexturedModalRect(guiLeft+191, guiTop+148, 31, 214, 43, 3);
        	
        	//draw range indicator
        	if(this.mousePressBar == 3) {
        		drawTexturedModalRect(guiLeft+187+barPos, guiTop+145, 22, 214, 9, 9);
        	}
        	else {
        		drawTexturedModalRect(guiLeft+187+wpStayPos, guiTop+145, 22, 214, 9, 9);
        	}
        	
        	break;
        	}
        case 6:	{	//page 6
        	this.pageIndicator = 246;
    		this.pageIndicatorAI = 183;
    		break;
        	}
        }//end AI page switch
        
        //draw AI page indicator
        drawTexturedModalRect(guiLeft + this.pageIndicator, guiTop + this.pageIndicatorAI, 74, 214, 6, 24);
        
        //draw level, ship type/name icon
        Minecraft.getMinecraft().getTextureManager().bindTexture(TEXTURE_ICON);
        
        if(entity.getStateMinor(ID.M.ShipLevel) > 99) {
        	//draw level background
        	drawTexturedModalRect(guiLeft+165, guiTop+18, 0, 0, 40, 42);
        	
        	try{
        		//draw ship type icon
        		drawTexturedModalRect(guiLeft+167, guiTop+22, this.iconXY[0][0], this.iconXY[0][1], 28, 28);

        		//use name icon file 0
        		if(iconXY[1][0] == 0) {
        			//draw ship name icon
        			drawTexturedModalRect(guiLeft+176, guiTop+63, this.iconXY[1][1], this.iconXY[1][2], 11, 59);
        		}
        	}
        	catch(Exception e) {
//        		LogHelper.info("Exception : get name icon fail "+e);
        	}
        }
        else {
        	//draw level background
        	drawTexturedModalRect(guiLeft+165, guiTop+18, 0, 43, 30, 30);
        	
        	try {
        		//draw ship type icon
        		drawTexturedModalRect(guiLeft+165, guiTop+18, this.iconXY[0][0], this.iconXY[0][1], 28, 28);
        		
        		//use name icon file 0
        		if(iconXY[1][0] == 0) {
        			//draw ship name icon
        			drawTexturedModalRect(guiLeft+176, guiTop+63, this.iconXY[1][1], this.iconXY[1][2], 11, 59);
        		}
        	}
        	catch(Exception e) {
//        		LogHelper.info("Exception : get name icon fail "+e);
        	}
        }
        
        //draw ship morale
        drawIconMorale();
        
        //draw entity model                                            guiLeft + 200 - xMouse  guiTop + 50 - yMouse
        drawEntityModel(guiLeft+218, guiTop+100, entity.getModelPos(), guiLeft+215-xMouse, guiTop+60-yMouse, this.entity);
        
	}
	
	//draw ship morale
	private void drawIconMorale() {
		int ix = 44;
		
		switch(this.entity.getMoraleLevel()) {
		case ID.Morale.Excited:
			ix = 0;
			break;
		case ID.Morale.Happy:
			ix = 11;
			break;
		case ID.Morale.Normal:
			ix = 22;
			break;
		case ID.Morale.Tired:
			ix = 33;
			break;
		}
        
        drawTexturedModalRect(guiLeft+239, guiTop+18, ix, 240, 11, 11);
	}
	
	//draw tooltip
	private void handleHoveringText() {
		String str, str2;
		int temp, strWidth;
		
		//reset text
		mouseoverList.clear();
		
		//draw morale string
		if(xMouse > 238+guiLeft && xMouse < 251+guiLeft && yMouse > 17+guiTop && yMouse < 30+guiTop) {
			mouseoverList.clear();
			
			switch(this.entity.getMoraleLevel()) {
			case ID.Morale.Excited:
				mouseoverList.add(strM0);
				break;
			case ID.Morale.Happy:
				mouseoverList.add(strM1);
				break;
			case ID.Morale.Normal:
				mouseoverList.add(strM2);
				break;
			case ID.Morale.Tired:
				mouseoverList.add(strM3);
				break;
			default:
				mouseoverList.add(strM4);
				break;
			}
			
			this.drawHoveringText(mouseoverList, 200, 45, this.fontRendererObj);
		}
		
		//draw states value
		if(xMouse > 73+guiLeft && xMouse < 134+guiLeft) {
			//show text at ATTACK
			if(showPage == 2 && yMouse > 18+guiTop && yMouse < 40+guiTop) {
//					LogHelper.info("DEBUg : get tag "+this.entity.getEffectEquip(ID.EF_CRI));
				//draw attack text
				mouseoverList.add(EnumChatFormatting.RED + strAttrAtk1);
				temp = this.fontRendererObj.getStringWidth(strAttrAtk1);
				strWidth = temp;
				
				mouseoverList.add(EnumChatFormatting.RED + strAttrAtk2);
				temp = this.fontRendererObj.getStringWidth(strAttrAtk2);
				if(temp > strWidth) strWidth = temp;
				
				mouseoverList.add(EnumChatFormatting.AQUA + strAttrCri);
				temp = this.fontRendererObj.getStringWidth(strAttrCri);
				if(temp > strWidth) strWidth = temp;
				
				mouseoverList.add(EnumChatFormatting.YELLOW + strAttrDHIT);
				temp = this.fontRendererObj.getStringWidth(strAttrDHIT);
				if(temp > strWidth) strWidth = temp;
				
				mouseoverList.add(EnumChatFormatting.GOLD + strAttrTHIT);
				temp = this.fontRendererObj.getStringWidth(strAttrTHIT);
				if(temp > strWidth) strWidth = temp;
				
				mouseoverList.add(EnumChatFormatting.YELLOW + strAttrAA);
				temp = this.fontRendererObj.getStringWidth(strAttrAA);
				if(temp > strWidth) strWidth = temp;
				
				mouseoverList.add(EnumChatFormatting.AQUA + strAttrASM);
				temp = this.fontRendererObj.getStringWidth(strAttrASM);
				if(temp > strWidth) strWidth = temp;
				
				this.drawHoveringText(mouseoverList, 55, 143, this.fontRendererObj);
				
				
				//draw attack value
				mouseoverList.clear();
				
				mouseoverList.add(strATK);
				
				mouseoverList.add(strAATK);
				
				overText = String.valueOf((int)(this.entity.getEffectEquip(ID.EF_CRI) * 100F))+ " %";
				mouseoverList.add(overText);
				
				overText = String.valueOf((int)(this.entity.getEffectEquip(ID.EF_DHIT) * 100F)) + " %";
				mouseoverList.add(overText);
				
				overText = String.valueOf((int)(this.entity.getEffectEquip(ID.EF_THIT) * 100F)) + " %";
				mouseoverList.add(overText);
				
				overText = String.valueOf((int)(this.entity.getEffectEquip(ID.EF_AA)));
				mouseoverList.add(overText);
				
				overText = String.valueOf((int)(this.entity.getEffectEquip(ID.EF_ASM)));
				mouseoverList.add(overText);
				
				this.drawHoveringText(mouseoverList, 61+strWidth, 143, this.fontRendererObj);
			}
			//show text at RANGE
			else if(showPage == 2 && yMouse > 104+guiTop && yMouse < 126+guiTop) {
				//draw text
				mouseoverList.add(EnumChatFormatting.RED + strAttrMiss);
				temp = this.fontRendererObj.getStringWidth(strAttrMiss);
				strWidth = temp;
				
				mouseoverList.add(EnumChatFormatting.AQUA + strAttrMissA);
				temp = this.fontRendererObj.getStringWidth(strAttrMissA);
				if(temp > strWidth) strWidth = temp;
				
				mouseoverList.add(EnumChatFormatting.GOLD + strAttrDodge);
				temp = this.fontRendererObj.getStringWidth(strAttrDodge);
				if(temp > strWidth) strWidth = temp;
				
				this.drawHoveringText(mouseoverList, 55, 143, this.fontRendererObj);
				
				
				//draw value
				mouseoverList.clear();
				
				//calc miss
				temp = (int) ((0.2F - this.entity.getEffectEquip(ID.EF_MISS) - 0.001F * this.entity.getStateMinor(ID.M.ShipLevel)) * 100F);
				if(temp < 0) temp = 0;
				if(temp > 35) temp = 35;
				str = String.valueOf(temp);
				
				temp = (int) ((0.35F - this.entity.getEffectEquip(ID.EF_MISS) - 0.001F * this.entity.getStateMinor(ID.M.ShipLevel)) * 100F);
				if(temp < 0) temp = 0;
				if(temp > 35) temp = 35;
				str2 = String.valueOf(temp);
				
				overText = str + " ~ " + str2 + " %";
				mouseoverList.add(overText);
				
				//calc air miss
				temp = (int) ((0.25F - this.entity.getEffectEquip(ID.EF_MISS) - 0.001F * this.entity.getStateMinor(ID.M.ShipLevel)) * 100F);
				if(temp < 0) temp = 0;
				if(temp > 35) temp = 35;
				
				overText = String.valueOf(temp) + " %";
				mouseoverList.add(overText);
				
				//calc dodge
				if(this.entity instanceof IShipInvisible) {
					temp = (int) (this.entity.getEffectEquip(ID.EF_DODGE) +
							   			((IShipInvisible)this.entity).getInvisibleLevel());
					if(temp > ConfigHandler.limitShipEffect[6]) temp = (int) ConfigHandler.limitShipEffect[6];
					overText = String.valueOf(temp) + " %";
				}
				else {
					overText = String.valueOf((int)this.entity.getEffectEquip(ID.EF_DODGE)) + " %";
				}
				
				mouseoverList.add(overText);
				
				this.drawHoveringText(mouseoverList, 61+strWidth, 143, this.fontRendererObj);
			}
			//show text at FORMATION
			else if(showPage == 3 && yMouse > 40+guiTop && yMouse < 62+guiTop && this.entity.getStateMinor(ID.M.FormatType) >= 1) {
				overText = EnumChatFormatting.LIGHT_PURPLE + strAttrFPos;
				mouseoverList.add(overText);
				temp = this.fontRendererObj.getStringWidth(overText);
				strWidth = temp;
				
				overText = EnumChatFormatting.RED + strAttrAtk1;
				mouseoverList.add(overText);
				temp = this.fontRendererObj.getStringWidth(overText);
				if(temp > strWidth) strWidth = temp;
				
				overText = EnumChatFormatting.RED + strAttrAtk2;
				mouseoverList.add(overText);
				temp = this.fontRendererObj.getStringWidth(overText);
				if(temp > strWidth) strWidth = temp;
				
				overText = EnumChatFormatting.WHITE + strAttrDEF;
				mouseoverList.add(overText);
				temp = this.fontRendererObj.getStringWidth(overText);
				if(temp > strWidth) strWidth = temp;
				
				overText = EnumChatFormatting.GOLD + strAttrDodge;
				mouseoverList.add(overText);
				temp = this.fontRendererObj.getStringWidth(overText);
				if(temp > strWidth) strWidth = temp;
				
				overText = EnumChatFormatting.RED + strAttrMissR;
				mouseoverList.add(overText);
				temp = this.fontRendererObj.getStringWidth(overText);
				if(temp > strWidth) strWidth = temp;
				
				overText = EnumChatFormatting.AQUA + strAttrCri;
				mouseoverList.add(overText);
				temp = this.fontRendererObj.getStringWidth(overText);
				if(temp > strWidth) strWidth = temp;
				
				overText = EnumChatFormatting.YELLOW + strAttrDHIT;
				mouseoverList.add(overText);
				temp = this.fontRendererObj.getStringWidth(overText);
				if(temp > strWidth) strWidth = temp;
				
				overText = EnumChatFormatting.GOLD + strAttrTHIT;
				mouseoverList.add(overText);
				temp = this.fontRendererObj.getStringWidth(overText);
				if(temp > strWidth) strWidth = temp;
				
				overText = EnumChatFormatting.YELLOW + strAttrAA;
				mouseoverList.add(overText);
				temp = this.fontRendererObj.getStringWidth(overText);
				if(temp > strWidth) strWidth = temp;
				
				overText = EnumChatFormatting.AQUA + strAttrASM;
				mouseoverList.add(overText);
				temp = this.fontRendererObj.getStringWidth(overText);
				if(temp > strWidth) strWidth = temp;
				
				overText = EnumChatFormatting.GRAY + strAttrMOV;
				mouseoverList.add(overText);
				temp = this.fontRendererObj.getStringWidth(overText);
				if(temp > strWidth) strWidth = temp;
				
				this.drawHoveringText(mouseoverList, 55, 78, this.fontRendererObj);
				
				
				//draw value
				mouseoverList.clear();
				
				overText = String.valueOf(this.entity.getStateMinor(ID.M.FormatPos)+1);
				mouseoverList.add(overText);
				
				str = String.valueOf((int)this.entity.getEffectFormation(ID.Formation.ATK_L) + 100);
				str2 = String.valueOf((int)this.entity.getEffectFormation(ID.Formation.ATK_H) + 100);
				overText = str + " / " + str2 + " %";
				mouseoverList.add(overText);
				
				str = String.valueOf((int)this.entity.getEffectFormation(ID.Formation.ATK_AL) + 100);
				str2 = String.valueOf((int)this.entity.getEffectFormation(ID.Formation.ATK_AH) + 100);
				overText = str + " / " + str2 + " %";
				mouseoverList.add(overText);
				
				overText = String.valueOf((int)this.entity.getEffectFormation(ID.Formation.DEF) + 100) + " %";
				mouseoverList.add(overText);
				
				overText = String.valueOf((int)this.entity.getEffectFormation(ID.Formation.DODGE) + 100) + " %";
				mouseoverList.add(overText);
				
				overText = String.valueOf((int)this.entity.getEffectFormation(ID.Formation.MISS) + 100) + " %";
				mouseoverList.add(overText);
				
				overText = String.valueOf((int)this.entity.getEffectFormation(ID.Formation.CRI) + 100) + " %";
				mouseoverList.add(overText);
				
				overText = String.valueOf((int)this.entity.getEffectFormation(ID.Formation.DHIT) + 100) + " %";
				mouseoverList.add(overText);
				
				overText = String.valueOf((int)this.entity.getEffectFormation(ID.Formation.THIT) + 100) + " %";
				mouseoverList.add(overText);
				
				overText = String.valueOf((int)this.entity.getEffectFormation(ID.Formation.AA) + 100) + " %";
				mouseoverList.add(overText);
				
				overText = String.valueOf((int)this.entity.getEffectFormation(ID.Formation.ASM) + 100) + " %";
				mouseoverList.add(overText);
				
				overText = String.format("%.2f", this.entity.getEffectFormation(ID.Formation.MOV));
				mouseoverList.add(overText);
				
				this.drawHoveringText(mouseoverList, 61+strWidth, 78, this.fontRendererObj);
			}
		}
			
	}
	
	//get new mouseX,Y and draw gui
	@Override
	public void drawScreen(int mouseX, int mouseY, float f) {
		super.drawScreen(mouseX, mouseY, f);
		
		xMouse = mouseX;
		yMouse = mouseY;
	}
	
	//draw entity model, copy from player inventory class
	public static void drawEntityModel(int x, int y, float[] modelPos, float yaw, float pitch, BasicEntityShip entity) {		
		//set basic position and rotation
		GL11.glEnable(GL11.GL_COLOR_MATERIAL);
		GL11.glPushMatrix();
		GL11.glTranslatef(x + modelPos[0], y + modelPos[1], 50.0F + modelPos[2]);
		GL11.glScalef(-modelPos[3], modelPos[3], modelPos[3]);
		GL11.glRotatef(180.0F, 0.0F, 0.0F, 1.0F);
		float f2 = entity.renderYawOffset;
		float f3 = entity.rotationYaw;
		float f4 = entity.rotationPitch;
		float f5 = entity.prevRotationYawHead;
		float f6 = entity.rotationYawHead;
		
		//set the light of model (face to player)
		GL11.glRotatef(135.0F, 0.0F, 1.0F, 0.0F);
		RenderHelper.enableStandardItemLighting();
		GL11.glRotatef(-135.0F, 0.0F, 1.0F, 0.0F);
		
		//set head look angle
		GL11.glRotatef(-((float) Math.atan(pitch / 40.0F)) * 20.0F, 1.0F, 0.0F, 0.0F);
		
		entity.renderYawOffset = (float) Math.atan(yaw / 40.0F) * 20.0F;
		entity.rotationYaw = (float) Math.atan(yaw / 40.0F) * 40.0F;
		entity.rotationPitch = -((float) Math.atan(pitch / 40.0F)) * 20.0F;
		entity.rotationYawHead = entity.rotationYaw;
		entity.prevRotationYawHead = entity.rotationYaw;
		
		//get mount or rider
		BasicEntityShip shipMount = null;
		BasicEntityShip shipRider = null;
		
		if (entity.ridingEntity instanceof EntityDestroyerInazuma)
		{
			shipMount = (BasicEntityShip) entity.ridingEntity;
			shipMount.renderYawOffset = entity.renderYawOffset;
			shipMount.rotationYaw = entity.rotationYaw;
			shipMount.rotationPitch = entity.rotationPitch;
			shipMount.rotationYawHead = entity.rotationYawHead;
			shipMount.prevRotationYawHead = entity.prevRotationYawHead;
			
			if (shipMount.isSitting()) shipMount.setSitting(false);
		}
		else if (entity.riddenByEntity instanceof EntityDestroyerIkazuchi)
		{
			shipRider = (BasicEntityShip) entity.riddenByEntity;
			shipRider.renderYawOffset = entity.renderYawOffset;
			shipRider.rotationYaw = entity.rotationYaw;
			shipRider.rotationPitch = entity.rotationPitch;
			shipRider.rotationYawHead = entity.rotationYawHead;
			shipRider.prevRotationYawHead = entity.prevRotationYawHead;
			
			if (entity.isSitting()) entity.setSitting(false);
		}
		
		GL11.glTranslatef(0.0F, entity.yOffset, 0.0F);
		RenderManager.instance.playerViewY = 180.0F;
		
		//draw mount or rider
		if (shipRider != null)
		{
			//ship必須先畫才畫mounts
			float[] partPos = ParticleHelper.rotateXZByAxis(-0.2F, 0F, (entity.renderYawOffset % 360) / 57.2957F, 1F);
			GL11.glTranslatef(partPos[1], (float)(entity.getMountedYOffset()), partPos[0]);
			RenderManager.instance.renderEntityWithPosYaw(shipRider, 0D, 0D, 0D, 0.0F, 1.0F);
			GL11.glTranslatef(-partPos[1], -(float)(entity.getMountedYOffset()), -partPos[0]);
			RenderManager.instance.renderEntityWithPosYaw(entity, 0D, 0D, 0D, 0.0F, 1.0F);
		}
		else if (shipMount != null)
		{
			//ship必須先畫才畫mounts
			float[] partPos = ParticleHelper.rotateXZByAxis(-0.2F, 0F, (entity.renderYawOffset % 360) / 57.2957F, 1F);
			GL11.glTranslatef(partPos[1], (float)(shipMount.getMountedYOffset()), partPos[0]);
			RenderManager.instance.renderEntityWithPosYaw(entity, 0.0D, 0.0D, 0.0D, 0.0F, 1.0F);
			GL11.glTranslatef(-partPos[1], -(float)(shipMount.getMountedYOffset()), -partPos[0]);
			RenderManager.instance.renderEntityWithPosYaw(shipMount, 0.0D, 0.0D, 0.0D, 0.0F, 1.0F);
		}
		else
		{
			RenderManager.instance.renderEntityWithPosYaw(entity, 0.0D, 0.0D, 0.0D, 0.0F, 1.0F);
		}
		
//		entity.renderYawOffset = f2;
//		entity.rotationYaw = f3;
//		entity.rotationPitch = f4;
//		entity.prevRotationYawHead = f5;
//		entity.rotationYawHead = f6;
		GL11.glPopMatrix();
		RenderHelper.disableStandardItemLighting();
		GL11.glDisable(GL12.GL_RESCALE_NORMAL);
		OpenGlHelper.setActiveTexture(OpenGlHelper.lightmapTexUnit);
		GL11.glDisable(GL11.GL_TEXTURE_2D);
		OpenGlHelper.setActiveTexture(OpenGlHelper.defaultTexUnit);
	}

	//draw level,hp,atk,def...
	private void drawAttributes() {
		//draw hp, level
		shiplevel = String.valueOf(entity.getStateMinor(ID.M.ShipLevel));
		hpCurrent = MathHelper.ceiling_float_int(entity.getHealth());
		hpMax = MathHelper.ceiling_float_int(entity.getMaxHealth());
		color = 0;

		//draw lv/hp name
		this.fontRendererObj.drawStringWithShadow(lvMark, 231-this.fontRendererObj.getStringWidth(lvMark), 6, 65535);
		this.fontRendererObj.drawStringWithShadow(hpMark, 145-this.fontRendererObj.getStringWidth(hpMark), 6, 65535);
		
		//draw level: 150->gold other->white
		if(entity.getStateMinor(ID.M.ShipLevel) < 150) {
			color = 16777215;  //white
		}
		else {
			color = 16766720;  //gold	
		}
		this.fontRendererObj.drawStringWithShadow(shiplevel, xSize-6-this.fontRendererObj.getStringWidth(shiplevel), 6, color);

		//draw hp/maxhp, if currHP < maxHP, use darker color
		color = GuiHelper.pickColor(entity.getBonusPoint(ID.HP));
		this.fontRendererObj.drawStringWithShadow("/"+String.valueOf(hpMax), 148 + this.fontRendererObj.getStringWidth(String.valueOf(hpCurrent)), 6, color);
		if(hpCurrent < hpMax) {
			switch(entity.getBonusPoint(ID.HP)) {
			case 0:
				color = 16119285;	//gray
				break;
			case 1:
				color = 13421568;	//dark yellow
				break;
			case 2:
				color = 16747520;	//dark orange
				break;
			default:
				color = 13107200;	//dark red
				break;
			}
		}
		this.fontRendererObj.drawStringWithShadow(String.valueOf(hpCurrent), 147, 6, color);	
				
		//draw string in different page
		switch(this.showPage) {
		case 2: {	//page 2: attribute page
			strLATK = String.format("%.1f", this.entity.getStateFinal(ID.ATK));
			strHATK = String.format("%.1f", this.entity.getStateFinal(ID.ATK_H));
			strATK = strLATK + "/" + strHATK;
			strALATK = String.format("%.1f", this.entity.getStateFinal(ID.ATK_AL));
			strAHATK = String.format("%.1f", this.entity.getStateFinal(ID.ATK_AH));
			strAATK = strALATK + "/" + strAHATK;
			strDEF = String.format("%.2f", this.entity.getStateFinal(ID.DEF))+"%";
			strSPD = String.format("%.2f", this.entity.getStateFinal(ID.SPD));
			strMOV = String.format("%.2f", this.entity.getStateFinal(ID.MOV));
			strHIT = String.format("%.2f", this.entity.getStateFinal(ID.HIT));
			
			//draw firepower
			if(this.showAttack == 1) {	//show cannon attack
				this.fontRendererObj.drawString(strAttrAtk1, 75, 20, 0);
				color = GuiHelper.pickColor(entity.getBonusPoint(ID.ATK));
				this.fontRendererObj.drawStringWithShadow(strATK, 133-this.fontRendererObj.getStringWidth(strATK), 30, color);
			}
			else {						//show aircraft attack
				this.fontRendererObj.drawString(strAttrAtk2, 75, 20, 0);
				color = GuiHelper.pickColor(entity.getBonusPoint(ID.ATK));
				this.fontRendererObj.drawStringWithShadow(strAATK, 133-this.fontRendererObj.getStringWidth(strAATK), 30, color);
			}
			this.fontRendererObj.drawString(strAttrDEF, 75, 41, 0);
			this.fontRendererObj.drawString(strAttrSPD, 75, 62, 0);
			this.fontRendererObj.drawString(strAttrMOV, 75, 83, 0);
			this.fontRendererObj.drawString(strAttrHIT, 75, 104, 0);
			
			//draw armor
			color = GuiHelper.pickColor(entity.getBonusPoint(ID.DEF));
			this.fontRendererObj.drawStringWithShadow(strDEF, 133-this.fontRendererObj.getStringWidth(strDEF), 51, color);
			
			//draw attack speed
			color = GuiHelper.pickColor(entity.getBonusPoint(ID.SPD));
			this.fontRendererObj.drawStringWithShadow(strSPD, 133-this.fontRendererObj.getStringWidth(strSPD), 72, color);
			
			//draw movement speed
			color = GuiHelper.pickColor(entity.getBonusPoint(ID.MOV));
			this.fontRendererObj.drawStringWithShadow(strMOV, 133-this.fontRendererObj.getStringWidth(strMOV), 93, color);
					
			//draw range
			color = GuiHelper.pickColor(entity.getBonusPoint(ID.HIT));
			this.fontRendererObj.drawStringWithShadow(strHIT, 133-this.fontRendererObj.getStringWidth(strHIT), 114, color);
			break;
			}
		case 1:	{	//page 1: exp, kills, L&H ammo, fuel
			//draw string
			this.fontRendererObj.drawString(strMiKills, 75, 20, 0);
			this.fontRendererObj.drawString(strMiExp, 75, 41, 0);
			this.fontRendererObj.drawString(strMiAmmoL, 75, 62, 0);
			this.fontRendererObj.drawString(strMiAmmoH, 75, 83, 0);
			this.fontRendererObj.drawString(strMiGrudge, 75, 104, 0);
			//draw value
			entity.setExpNext();  //update exp value
			Exp = String.valueOf(this.entity.getStateMinor(ID.M.ExpCurrent))+"/"+String.valueOf(this.entity.getStateMinor(ID.M.ExpNext));
			Kills = String.valueOf(this.entity.getStateMinor(ID.M.Kills));
			AmmoLight = String.valueOf(this.entity.getStateMinor(ID.M.NumAmmoLight));
			AmmoHeavy = String.valueOf(this.entity.getStateMinor(ID.M.NumAmmoHeavy));
			Grudge = String.valueOf(this.entity.getStateMinor(ID.M.NumGrudge));
				
			this.fontRendererObj.drawStringWithShadow(Kills, 133-this.fontRendererObj.getStringWidth(Kills), 30, Values.Color.WHITE);
			this.fontRendererObj.drawStringWithShadow(Exp, 133-this.fontRendererObj.getStringWidth(Exp), 51, Values.Color.WHITE);
			this.fontRendererObj.drawStringWithShadow(AmmoLight, 133-this.fontRendererObj.getStringWidth(AmmoLight), 72, Values.Color.WHITE);
			this.fontRendererObj.drawStringWithShadow(AmmoHeavy, 133-this.fontRendererObj.getStringWidth(AmmoHeavy), 93, Values.Color.WHITE);
			this.fontRendererObj.drawStringWithShadow(Grudge, 133-this.fontRendererObj.getStringWidth(Grudge), 114, Values.Color.WHITE);
						
			break;
			}
		case 3: {	//page 3: light/heavy airplane, marriage
			//draw string
			this.fontRendererObj.drawString(strAttrWedding, 75, 20, 0);
			this.fontRendererObj.drawString(strAttrFormat, 75, 41, 0);
			
			//draw value
			//draw marriage
			if(this.entity.getStateFlag(ID.F.IsMarried)) {
				marriage = strAttrWedTrue;
			}
			else {
				marriage = strAttrWedFalse;
			}
			
			//draw formation
			int ftype = this.entity.getStateMinor(ID.M.FormatType);
			this.Formation = I18n.format("gui.shincolle:formation.format"+ftype);
			this.fontRendererObj.drawStringWithShadow(Formation, 133-this.fontRendererObj.getStringWidth(Formation), 51, Values.Color.WHITE);
			
			//大型艦, 顯示艦載機數量
			if(this.entity instanceof BasicEntityShipCV) {
				this.fontRendererObj.drawString(strMiAirL, 75, 83, 0);
				this.fontRendererObj.drawString(strMiAirH, 75, 104, 0);
				AirLight = String.valueOf(((BasicEntityShipCV)this.entity).getNumAircraftLight());
				AirHeavy = String.valueOf(((BasicEntityShipCV)this.entity).getNumAircraftHeavy());
				this.fontRendererObj.drawStringWithShadow(AirLight, 133-this.fontRendererObj.getStringWidth(AirLight), 93, Values.Color.YELLOW);
				this.fontRendererObj.drawStringWithShadow(AirHeavy, 133-this.fontRendererObj.getStringWidth(AirHeavy), 114, Values.Color.YELLOW);	
			}
			
			this.fontRendererObj.drawStringWithShadow(marriage, 133-this.fontRendererObj.getStringWidth(marriage), 30, Values.Color.YELLOW);
			
			break;
			}//end case 3
		}//end page switch
		
		//draw AI page
		switch(this.showPageAI) {
		case 1:	{	//AI page 1
				//draw string
				this.fontRendererObj.drawString(canMelee, 187, 134, 0);
				if(entity.getAttackType(ID.F.AtkType_Light))
				this.fontRendererObj.drawString(canLATK, 187, 146, 0);
				if(entity.getAttackType(ID.F.AtkType_Heavy))
				this.fontRendererObj.drawString(canHATK, 187, 158, 0);
				if(entity.getAttackType(ID.F.AtkType_AirLight))
				this.fontRendererObj.drawString(canALATK, 187, 170, 0);
				if(entity.getAttackType(ID.F.AtkType_AirHeavy))
				this.fontRendererObj.drawString(canAHATK, 187, 182, 0);
				if(entity.getAttackType(ID.F.HaveRingEffect))
				this.fontRendererObj.drawString(auraEffect, 187, 194, 0);
			}
			break;
		case 2:	{	//AI page 2
				//draw string
				this.fontRendererObj.drawString(followMin, 174, 134, 0);
				this.fontRendererObj.drawString(followMax, 174, 158, 0);
				this.fontRendererObj.drawString(fleeHP, 174, 182, 0);
				
				//draw value
				followMinValue = String.valueOf(entity.getStateMinor(ID.M.FollowMin));
				followMaxValue = String.valueOf(entity.getStateMinor(ID.M.FollowMax));
				fleeHPValue = String.valueOf(entity.getStateMinor(ID.M.FleeHP));
				
				if(this.mousePressBar == 0) {
					barPosValue = String.valueOf((int)(barPos / 42F * 30F + 1F));
					this.fontRendererObj.drawStringWithShadow(barPosValue, 174, 145, Values.Color.RED);
				}
				else {
					this.fontRendererObj.drawStringWithShadow(followMinValue, 174, 145, Values.Color.YELLOW);
				}
				
				if(this.mousePressBar == 1) {
					barPosValue = String.valueOf((int)(barPos / 42F * 30F + 2F));
					this.fontRendererObj.drawStringWithShadow(barPosValue, 174, 169, Values.Color.RED);		
				}
				else {
					this.fontRendererObj.drawStringWithShadow(followMaxValue, 174, 169, Values.Color.YELLOW);
				}
				
				if(this.mousePressBar == 2) {
					barPosValue = String.valueOf((int)(barPos / 42F * 100F));
					this.fontRendererObj.drawStringWithShadow(barPosValue, 174, 193, Values.Color.RED);
				}
				else {
					this.fontRendererObj.drawStringWithShadow(fleeHPValue, 174, 193, Values.Color.YELLOW);
				}	
			}
			break;
		case 3: {	//AI page 3
			//draw string
			this.fontRendererObj.drawString(TarAI, 187, 134, 0);
			this.fontRendererObj.drawString(strOnSight, 187, 146, 0);
			this.fontRendererObj.drawString(strPVP, 187, 158, 0);
			this.fontRendererObj.drawString(strAA, 187, 170, 0);
			this.fontRendererObj.drawString(strASM, 187, 182, 0);
			this.fontRendererObj.drawString(strTimeKeep, 187, 194, 0);
			}
			break;
		case 4:		//AI page 4
		{
			if(this.entity.getStateFlag(ID.F.CanPickItem)) this.fontRendererObj.drawString(strPick, 187, 134, 0);
		}
		break;
		case 5:		//AI page 5
		{
			//draw string
			this.fontRendererObj.drawString(strWpStay, 174, 134, 0);
			
			//draw value TODO id to time string
			strWpStayValue = CalcHelper.tick2SecOrMin(entity.wpStayTime2Ticks(entity.getStateMinor(ID.M.WpStay)));
			
			if (this.mousePressBar == 3)
			{
				barPosValue = CalcHelper.tick2SecOrMin(entity.wpStayTime2Ticks((int)(barPos / (42F * 0.0625F))));
				this.fontRendererObj.drawStringWithShadow(barPosValue, 174, 145, Values.Color.RED);
			}
			else
			{
				this.fontRendererObj.drawStringWithShadow(strWpStayValue, 174, 145, Values.Color.YELLOW);
			}
		}
		break;
		}//end AI page switch
	}
	
	//mouse press + move
    @Override
	protected void mouseClickMove(int posX, int posY, int mouseKey, long pressTime) {
    	super.mouseClickMove(posX, posY, mouseKey, pressTime);

    	//get click position
        xClick = posX - this.guiLeft;
        yClick = posY - this.guiTop;
        
        barPos = xClick - 191;
        if(barPos > 42) barPos = 42;
        if(barPos < 0)  barPos = 0;

    }
    
    //state: -1:move 0:left up 1:right up 2:...
    @Override
	protected void mouseMovedOrUp(int posX, int posY, int state) {
    	super.mouseMovedOrUp(posX, posY, state);

    	//get click position
        xClick = posX - this.guiLeft;
        yClick = posY - this.guiTop;
        
    	//get cliuck button
        int barvalue = 0;
        
    	switch(mousePressBar) {
    	case 0:	//bar0: follow min
    		barvalue = (int)(barPos / 42F * 30F + 1F);
    		CommonProxy.channelG.sendToServer(new C2SGUIPackets(this.entity, ID.B.ShipInv_FollowMin, barvalue));
    		break;
    	case 1:	//bar1: follow max
    		barvalue = (int)(barPos / 42F * 30F + 2F);
    		CommonProxy.channelG.sendToServer(new C2SGUIPackets(this.entity, ID.B.ShipInv_FollowMax, barvalue));
    		break;
    	case 2:	//bar2: flee hp
    		barvalue = (int)(barPos / 42F * 100F);
    		CommonProxy.channelG.sendToServer(new C2SGUIPackets(this.entity, ID.B.ShipInv_FleeHP, barvalue));
    		break;
    	case 3:	//bar3: wp stay time
    		barvalue = (int)(barPos / (42F * 0.0625F));
    		CommonProxy.channelG.sendToServer(new C2SGUIPackets(this.entity, ID.B.ShipInv_WpStay, barvalue));
    		break;
    	}
    	
    	//reset flag
    	mousePress = false;
    	mousePressBar = -1;
    }
	
	//handle mouse click, @parm posX, posY, mouseKey (0:left 1:right 2:middle 3:...etc)
	@Override
	protected void mouseClicked(int posX, int posY, int mouseKey)
	{
        super.mouseClicked(posX, posY, mouseKey);
        
        //get click position
        xClick = posX - this.guiLeft;
        yClick = posY - this.guiTop;
        mousePress = true;
        
        //check press bar
        if (this.showPageAI == 2)
        {
        	switch(GuiHelper.getButton(ID.G.SHIPINVENTORY, 2, xClick, yClick))
        	{
        	case 0:
        		mousePressBar = 0;
        		break;
        	case 1:
        		mousePressBar = 1;
        		break;
        	case 2:
        		mousePressBar = 2;
        		break;
    		default:
    			mousePressBar = -1;
        		break;
        	}
        }
        else if (this.showPageAI == 5)
        {
        	switch(GuiHelper.getButton(ID.G.SHIPINVENTORY, 2, xClick, yClick))
        	{
        	case 0:
        		mousePressBar = 3;
        		break;
    		default:
    			mousePressBar = -1;
        		break;
        	}
        }
        else
        {
        	mousePressBar = -1;
        }
        
        //check button
        switch(GuiHelper.getButton(ID.G.SHIPINVENTORY, 0, xClick, yClick))
        {
        case 0:	//page 1 button
        	this.showPage = 1;
        	break;
        case 1:	//page 2 button
        	this.showPage = 2;
        	break;
        case 2:	//page 3 button
        	this.showPage = 3;
        	break;
        case 3:	//AI operation 0 
        	if (this.showPageAI == 1)
        	{	//page 1: can melee button
        		this.switchMelee = this.entity.getStateFlag(ID.F.UseMelee);
        		CommonProxy.channelG.sendToServer(new C2SGUIPackets(this.entity, ID.B.ShipInv_Melee, getInverseInt(this.switchMelee)));
        	}
        	else if (this.showPageAI == 3)
        	{	//page 3: change target AI
        		this.switchTarAI = this.entity.getStateFlag(ID.F.PassiveAI);
        		CommonProxy.channelG.sendToServer(new C2SGUIPackets(this.entity, ID.B.ShipInv_TarAI, getInverseInt(this.switchTarAI)));
        	}
        	else if (this.showPageAI == 4)
        	{	//page 4: pick item AI
        		this.switchPick = this.entity.getStateFlag(ID.F.PickItem);
        		CommonProxy.channelG.sendToServer(new C2SGUIPackets(this.entity, ID.B.ShipInv_PickitemAI, getInverseInt(this.switchPick)));
        	}
        	break;
        case 4:	//AI operation 1 
        	if (this.showPageAI == 1)
        	{	//page 1: use ammo light button
        		this.switchLight = this.entity.getStateFlag(ID.F.UseAmmoLight);
        		CommonProxy.channelG.sendToServer(new C2SGUIPackets(this.entity, ID.B.ShipInv_AmmoLight, getInverseInt(this.switchLight)));
        	}
        	else if (this.showPageAI == 3)
        	{	//page 3: change onsight AI
        		this.switchOnSight = this.entity.getStateFlag(ID.F.OnSightChase);
        		CommonProxy.channelG.sendToServer(new C2SGUIPackets(this.entity, ID.B.ShipInv_OnSightAI, getInverseInt(this.switchOnSight)));
        	}
        	break;
        case 5:	//AI operation 2
        	if (this.showPageAI == 1)
        	{	//page 1: use ammo heavy button
        		this.switchHeavy = this.entity.getStateFlag(ID.F.UseAmmoHeavy);
        		CommonProxy.channelG.sendToServer(new C2SGUIPackets(this.entity, ID.B.ShipInv_AmmoHeavy, getInverseInt(this.switchHeavy)));
        	}
        	else if (this.showPageAI == 3)
        	{	//page 3: change PVP first AI
        		this.switchPVP = this.entity.getStateFlag(ID.F.PVPFirst);
        		CommonProxy.channelG.sendToServer(new C2SGUIPackets(this.entity, ID.B.ShipInv_PVPAI, getInverseInt(this.switchPVP)));
        	}
        	break;
        case 6:	//AI operation 3
        	if (this.showPageAI == 1)
        	{	//page 1: use air light button
        		this.switchAirLight = this.entity.getStateFlag(ID.F.UseAirLight);
        		CommonProxy.channelG.sendToServer(new C2SGUIPackets(this.entity, ID.B.ShipInv_AirLight, getInverseInt(this.switchAirLight)));
        	}
        	else if (this.showPageAI == 3)
        	{	//page 3: change onsight AI
        		this.switchAA = this.entity.getStateFlag(ID.F.AntiAir);
        		CommonProxy.channelG.sendToServer(new C2SGUIPackets(this.entity, ID.B.ShipInv_AAAI, getInverseInt(this.switchAA)));
        	}
        	break;
        case 7:	//AI operation 4
        	if (this.showPageAI == 1)
        	{	//page 1: use air heavy button
        		this.switchAirHeavy = this.entity.getStateFlag(ID.F.UseAirHeavy);
        		CommonProxy.channelG.sendToServer(new C2SGUIPackets(this.entity, ID.B.ShipInv_AirHeavy, getInverseInt(this.switchAirHeavy)));
        	}
        	else if (this.showPageAI == 3)
        	{	//page 3: change onsight AI
        		this.switchASM = this.entity.getStateFlag(ID.F.AntiSS);
        		CommonProxy.channelG.sendToServer(new C2SGUIPackets(this.entity, ID.B.ShipInv_ASMAI, getInverseInt(this.switchASM)));
        	}
        	break;
        case 8:	//AI operation 5
        	if (this.showPageAI == 1)
        	{	//page 1: apply aura effect
        		this.switchAura = this.entity.getStateFlag(ID.F.UseRingEffect);
        		CommonProxy.channelG.sendToServer(new C2SGUIPackets(this.entity, ID.B.ShipInv_AuraEffect, getInverseInt(this.switchAura)));
        	}
        	else if (this.showPageAI == 3)
        	{	//page 3: timekeeper AI
        		this.switchTimeKeep = this.entity.getStateFlag(ID.F.TimeKeeper);
        		CommonProxy.channelG.sendToServer(new C2SGUIPackets(this.entity, ID.B.ShipInv_TIMEKEEPAI, getInverseInt(this.switchTimeKeep)));
        	}
        	break;
        case 9:		//AI page 1
        	this.showPageAI = 1;
        	break;
        case 10:	//AI page 2
        	this.showPageAI = 2;
        	break;
        case 11:	//AI page 3
        	this.showPageAI = 3;
        	break;
        case 12:	//AI page 4
        	this.showPageAI = 4;
        	break;
        case 13:	//AI page 5
        	this.showPageAI = 5;
        	break;
        case 14:	//AI page 6
        	this.showPageAI = 6;
        	break;
        case 15:	//inventory page 0
        	CommonProxy.channelG.sendToServer(new C2SGUIPackets(this.entity, ID.B.ShipInv_InvPage, 0));
        	break;
        case 16:	//inventory page 1
        	if (this.entity.getInventoryPageSize() > 0)
        	{
        		CommonProxy.channelG.sendToServer(new C2SGUIPackets(this.entity, ID.B.ShipInv_InvPage, 1));
        	}
        	break;
        case 17:	//inventory page 2
        	if (this.entity.getInventoryPageSize() > 1)
        	{
        		CommonProxy.channelG.sendToServer(new C2SGUIPackets(this.entity, ID.B.ShipInv_InvPage, 2));
        	}
        	break;
    	}//end all page switch
        
        if (this.showPage == 2)
        {	//page 2: damage display switch
        	switch (GuiHelper.getButton(ID.G.SHIPINVENTORY, 1, xClick, yClick))
        	{
        	case 0:
        		if (this.showAttack == 1)
        		{
        			this.showAttack = 2;
        		}
        		else
        		{
        			this.showAttack = 1;
        		}
        		break;
        	}
        }

	}
	
	//return 0 if par1 = true
	private int getInverseInt(boolean par1)
	{
		return par1 ? 0 : 1;
	}
	
	//close gui if entity dead or too far away
	@Override
	public void updateScreen()
	{
		super.updateScreen();
		
		if (this.entity == null || !this.entity.isEntityAlive() ||
			this.entity.getDistanceToEntity(this.mc.thePlayer) > ConfigHandler.closeGUIDist)
		{
            this.mc.thePlayer.closeScreen();
        }
	}


}
