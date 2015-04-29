package jw.commands;
import java.util.*;

import jw.core.*;
import static jw.core.MudMain.*;

/**
	The ObjectCommands class contains commands primarily used for intaracting
	with objects.
*/
public class ObjectCommands
{
	public static void doDrop(UserCon c, String args)
	{
		if (c.ch.position.equals("sleeping"))
		{
			c.sendln("You can't do that when you're "+c.ch.position+".");
			return;
		}

		if (args.length() == 0)
		{
			c.sendln("Drop what?");
			return;
		}
		
		ObjData targets[] = {};
		String arg1 = CommandHandler.getArg(args, 1);
		String arg2 = CommandHandler.getArg(args, 2).toLowerCase();
		if (("gold".startsWith(arg2) || "coins".startsWith(arg2)) && arg2.length() > 0)
		{
			int coinCount = Fmt.getInt(arg1);
			if (coinCount < 0 || coinCount == 0)
			{
				c.sendln("Drop how many coins?");
				return;
			}
			if (coinCount > c.ch.gold)
			{
				c.sendln("You don't have that much gold.");
				return;
			}

			ObjData coins = ObjData.makeCoins(coinCount);
			c.ch.gold -= coinCount;
			targets = new ObjData[1];
			targets[0] = coins;
		}
		else
		{
			ArrayList<ObjData> inv = new ArrayList<ObjData>();
			for (ObjData o : c.ch.objects)
				if (o.wearloc.equals("none"))
					inv.add(o);
			
			targets = Combat.findObj(c.ch, inv, args);
			
			if (targets.length == 0)
			{
				c.sendln("You don't have an object matching that description.");
				return;
			}
			
			ArrayList<ObjData> validTargets = new ArrayList<ObjData>();
			for (ObjData o : targets)
			{
				if (o.flags.get("nodrop"))
				{
					c.sendln("You can't drop "+Fmt.seeName(c.ch, o)+".");
					continue;
				}

				int tempId = 0;
				if (o.op != null)
					tempId = o.op.id;
				if (c.ch.currentRoom.checkTrigger("drop", c.ch, null, Script.objToStr(o), tempId) != -1)
					continue;
				for (CharData ch : allChars())
					if (ch.currentRoom == c.ch.currentRoom && ch != c.ch)
						if (ch.checkTrigger("drop", c.ch, null, Script.objToStr(o), tempId) != -1)
							continue;
				for (ObjData ob : c.ch.currentRoom.objects)
					if (ob.checkTrigger("drop", c.ch, null, Script.objToStr(o), tempId) != -1)
						continue;
				if (o.checkTrigger("drop", c.ch, null, Script.objToStr(o), tempId) != -1)
					continue;

				validTargets.add(o);
			}
			targets = validTargets.toArray(new ObjData[0]);
		}
		
		for (UserCon cs : conns)
		{
			if (cs.ch.currentRoom == c.ch.currentRoom)
			{
				if (!cs.ch.position.equals("sleeping"))
				{
					String dropList[] = ObjData.listContents(cs.ch, targets, false);
					for (String s : dropList)
					{
						s = s.trim();
						if (cs == c)
							cs.sendln("You drop "+s+".");
						else
							cs.sendln(Fmt.seeName(cs.ch, c.ch)+" drops "+s+".");
					}
				}
			}
		}
		
		for (ObjData o : targets)
			o.toRoom(c.ch.currentRoom);
	}

	public static void doGet(UserCon c, String args)
	{
		if (c.ch.position.equals("sleeping"))
		{
			c.sendln("You can't do that when you're "+c.ch.position+".");
			return;
		}

		String arg1 = CommandHandler.getArg(args, 1);
		String arg2 = CommandHandler.getArg(args, 2);
		String fromString = "";
		
		ObjData usedContainer = null;

		if (arg1.length() == 0)
		{
			c.sendln("Get what?");
			return;
		}
		
		ObjData targets[] = Combat.findObj(c.ch, c.ch.currentRoom.objects, arg1);
		if (arg2.length() > 0)
		{
			ObjData src[] = Combat.findObj(c.ch, ObjData.allVisibleObjects(c.ch), arg2);
			if (src.length == 0)
			{
				c.sendln("There's no container here that matches that description.");
				return;
			}
			if (!src[0].type.equals("container"))
			{
				c.sendln(Fmt.cap(Fmt.seeName(c.ch, src[0]))+" is not a container.");
				return;
			}
			if (src[0].objects.size() == 0)
			{
				c.sendln("There's nothing in "+Fmt.seeName(c.ch, src[0])+".");
				return;
			}
			if (src[0].op.id == Flags.corpseId && src[0].name.startsWith("pile equipment") && !src[0].name.equals("pile equipment "+c.ch.name.toLowerCase()))
			{
				boolean playerFound = false;
				for (UserCon cs : conns)
					if (src[0].name.equals("pile equipment "+cs.ch.name.toLowerCase()))
					{
						playerFound = true;
						if (cs.prefs.get("noloot"))
						{
							c.sendln("You can't take things from that player's pile.");
							return;
						}
					}
				if (!playerFound)
				{
					c.sendln("You can't take things from an offline player's pile.");
					return;
				}
			}
					
			usedContainer = src[0];
			targets = Combat.findObj(c.ch, src[0].objects, arg1);
			
			if (targets.length == 0)
			{
				c.sendln("There's nothing inside "+Fmt.seeName(c.ch, src[0])+" that matches that description.");
				return;
			}
		}
		
		if (targets.length == 0)
		{
			c.sendln("There's nothing here that matches that description.");
			return;
		}
		ArrayList<ObjData> validTargets = new ArrayList<ObjData>();
		for (ObjData o : targets)
		{
			if (o.op.id == Flags.corpseId && !o.name.equals("pile equipment "+c.ch.name.toLowerCase()))
			{
				for (UserCon cs : conns)
					if (o.name.equals("pile equipment "+cs.ch.name.toLowerCase()))
						if (!cs.prefs.get("noloot"))
							validTargets.add(o);
						else
							break;
				if (!validTargets.contains(o))
					c.sendln("You can't get "+Fmt.seeName(c.ch, o)+".");
			}
			else if (o.flags.get("notake"))
				c.sendln("You can't get "+Fmt.seeName(c.ch, o)+".");
			else if (o.checkTrigger("get", c.ch, null, "", 0) != 0)
				validTargets.add(o);
		}
		targets = validTargets.toArray(new ObjData[0]);

		String fullString = "";
		validTargets = new ArrayList<ObjData>();
		for (ObjData o : targets)
		{
			if (!ObjData.capCheck(c.ch, o))
			{
				fullString = "Your inventory is too full to hold "+Fmt.seeName(c.ch, o)+".";
				break;
			}
			o.toChar(c.ch);
			validTargets.add(o);
		}
		targets = validTargets.toArray(new ObjData[0]);
		
		for (UserCon cs : conns)
		{
			if (cs.ch.currentRoom == c.ch.currentRoom)
			{
				if (!cs.ch.position.equals("sleeping"))
				{
					String dropList[] = ObjData.listContents(cs.ch, targets, false);
					for (String s : dropList)
					{
						s = s.trim();
						fromString = "";
						if (usedContainer != null)
							fromString = " from "+Fmt.seeName(cs.ch, usedContainer);
						if (cs == c)
							cs.sendln("You get "+s+fromString+".");
						else
							cs.sendln(Fmt.seeName(cs.ch, c.ch)+" gets "+s+fromString+".");
					}
				}
			}
		}
		
		if (fullString.length() > 0)
			c.sendln(fullString);
		
		if (usedContainer != null)
			if (usedContainer.op.id == Flags.corpseId && usedContainer.objects.size() == 0)
				usedContainer.clearObjects();
	}
	
	public static void doGive(UserCon c, String args)
	{
		if (c.ch.position.equals("sleeping"))
		{
			c.sendln("You can't do that when you're "+c.ch.position+".");
			return;
		}

		String arg1 = CommandHandler.getArg(args, 1);
		String arg2 = CommandHandler.getArg(args, 2).toLowerCase();

		ObjData targets[] = new ObjData[0];
		ArrayList<ObjData> validTargets = new ArrayList<ObjData>();
		
		if (arg1.length() == 0)
		{
			c.sendln("Give what to whom?");
			return;
		}
		if (arg2.length() == 0)
		{
			c.sendln("Give it to whom?");
			return;
		}

		if ("gold".startsWith(arg2) || "coins".startsWith(arg2))
		{
			int coinCount = Fmt.getInt(arg1);
			if (coinCount < 0 || coinCount == 0)
			{
				c.sendln("Give away how many coins?");
				return;
			}
			if (coinCount > c.ch.gold)
			{
				c.sendln("You don't have that much gold.");
				return;
			}

			ObjData coins = ObjData.makeCoins(coinCount);
			c.ch.gold -= coinCount;
			targets = new ObjData[1];
			targets[0] = coins;
			arg2 = CommandHandler.getArg(args, 3);
		}

		CharData recip = Combat.findChar(c.ch, null, arg2, false);
		
		if (recip == c.ch)
		{
			c.sendln("You can't give something to yourself.");
			return;
		}
		if (recip == null)
		{
			c.sendln("There isn't anyone here by that name.");
			return;
		}

		if (targets.length == 0)
		{
			ArrayList<ObjData> inv = new ArrayList<ObjData>();
			for (ObjData o : c.ch.objects)
				if (o.wearloc.equals("none"))
					inv.add(o);
			
			targets = Combat.findObj(c.ch, inv, arg1);
			
			if (targets.length == 0)
			{
				c.sendln("You don't have an object matching that description.");
				return;
			}
			for (ObjData o : targets)
			{
				if (o.flags.get("nodrop"))
					c.sendln("You can't let go of "+Fmt.seeName(c.ch, o)+".");
				else if (!Combat.canSee(recip, o))
					c.sendln(Fmt.cap(Fmt.seeName(c.ch, recip))+" can't see "+Fmt.seeName(c.ch, o)+".");
				else if (o.checkTrigger("give", c.ch, recip, "", 0) != 0)
				{
					if (recip.checkTrigger("give", c.ch, null, Script.objToStr(o), 0) != 0)
						validTargets.add(o);
				}
			}
			targets = validTargets.toArray(new ObjData[0]);
		}
		
		String fullString = "";
		String recipString = "";
		validTargets = new ArrayList<ObjData>();
		for (ObjData o : targets)
		{
			if (!ObjData.capCheck(recip, o))
			{
				fullString = Fmt.cap(Fmt.seeName(c.ch, recip))+" is already holding too much to carry "+Fmt.seeName(c.ch, o)+".";
				recipString = Fmt.cap(Fmt.seeName(recip, c.ch))+" tries to give "+Fmt.seeName(recip, o)+" to you, but your inventory is too full.";
				break;
			}
			o.toChar(recip);
			validTargets.add(o);
			if (o.type.equals("money"))
				recip.checkTrigger("bribe", c.ch, null, "", o.cost);
		}
		targets = validTargets.toArray(new ObjData[0]);

		for (UserCon cs : conns)
		{
			if (cs.ch.currentRoom == c.ch.currentRoom)
			{
				if (!cs.ch.position.equals("sleeping"))
				{
					String dropList[] = ObjData.listContents(cs.ch, targets, false);
					for (String s : dropList)
					{
						s = s.trim();
						if (cs == c)
							cs.sendln("You give "+s+" to "+Fmt.seeName(c.ch, recip)+".");
						else if (cs.ch == recip)
							cs.sendln(Fmt.seeName(cs.ch, c.ch)+" gives "+s+" to you.");
						else
							cs.sendln(Fmt.seeName(cs.ch, c.ch)+" gives "+s+" to "+Fmt.seeName(cs.ch, recip)+".");
					}
				}
			}
		}
		
		if (fullString.length() > 0)
		{
			c.sendln(fullString);
			recip.sendln(recipString);
		}
	}

	public static void doPut(UserCon c, String args)
	{
		if (c.ch.position.equals("sleeping"))
		{
			c.sendln("You can't do that when you're "+c.ch.position+".");
			return;
		}

		String arg1 = CommandHandler.getArg(args, 1);
		String arg2 = CommandHandler.getArg(args, 2).toLowerCase();
		String arg3 = CommandHandler.getArg(args, 3);
		
		ObjData targets[] = new ObjData[0];
		
		if (arg1.length() == 0)
		{
			c.sendln("Put what in where?");
			return;
		}
		if (arg2.length() == 0)
		{
			c.sendln("Put it in what?");
			return;
		}
		
		if ("gold".startsWith(arg2) || "coins".startsWith(arg2))
		{
			int coinCount = Fmt.getInt(arg1);
			if (coinCount < 0 || coinCount == 0)
			{
				c.sendln("Put how many coins inside?");
				return;
			}
			if (coinCount > c.ch.gold)
			{
				c.sendln("You don't have that much gold.");
				return;
			}

			ObjData coins = ObjData.makeCoins(coinCount);
			c.ch.gold -= coinCount;
			targets = new ObjData[1];
			targets[0] = coins;
			arg2 = arg3;
		}
		else
		{
			ArrayList<ObjData> inv = new ArrayList<ObjData>();
			for (ObjData o : c.ch.objects)
				if (o.wearloc.equals("none"))
					inv.add(o);
			
			targets = Combat.findObj(c.ch, inv, arg1);
			if (targets.length == 0)
			{
				c.sendln("You don't have an object matching that description.");
				return;
			}
		}

		ObjData dest[] = Combat.findObj(c.ch, ObjData.allVisibleObjects(c.ch), arg2);
		if (dest.length == 0)
		{
			c.sendln("There's no container here that matches that description.");
			return;
		}
		if (!dest[0].type.equals("container"))
		{
			c.sendln(Fmt.cap(Fmt.seeName(c.ch, dest[0]))+" is not a container.");
			return;
		}
		if (dest[0].typeFlags.get("closed"))
		{
			c.sendln(Fmt.cap(Fmt.seeName(c.ch, dest[0]))+" is closed.");
			return;
		}
		if (dest[0].value1.equals("0"))
		{
			c.sendln("You can't put anything in that container.");
			return;
		}
		
		ObjData targetsAlt[] = new ObjData[targets.length-1];
		boolean replace = false;
		int aCtr = 0;
		for (int ctr = 0; ctr < targets.length; ctr++)
		{
			if (targets[ctr] == dest[0])
			{
				replace = true;
				continue;
			}
			if (aCtr < targetsAlt.length)
			{
				targetsAlt[aCtr] = targets[ctr];
				aCtr++;
			}
		}
		if (replace)
			targets = targetsAlt;
		
		if (targets.length == 0)
		{
			c.sendln("You can't put "+Fmt.seeName(c.ch, dest[0])+" inside itself.");
			return;
		}

		ArrayList<ObjData> validTargets = new ArrayList<ObjData>();
		for (ObjData o : targets)
		{
			if (o.flags.get("nodrop"))
				c.sendln("You can't let go of "+Fmt.seeName(c.ch, o)+".");
			else if (o.type.equals("container"))
				c.sendln("You can't put "+Fmt.seeName(c.ch, o)+" inside another container.");
			else if (o.flags.get("noput"))
				c.sendln("You can't put "+Fmt.seeName(c.ch, o)+" inside a container.");
			else if (o.checkTrigger("put", c.ch, null, Script.objToStr(dest[0]), dest[0].op.id) == 0)
				continue;
			else if (dest[0].checkTrigger("put", c.ch, null, Script.objToStr(o), o.op.id) == 0)
				continue;
			else
				validTargets.add(o);
		}
		targets = validTargets.toArray(new ObjData[0]);
		
		String fullString = "";
		int destCapacity = Fmt.getInt(dest[0].value1);
		validTargets = new ArrayList<ObjData>();
		for (ObjData o : targets)
		{
			o.toObject(dest[0]);
			double currentFill = ObjData.capCount(dest[0].objects.toArray(new ObjData[0]));
			if (destCapacity*20 < Math.round(currentFill*20))
			{
				fullString = Fmt.cap(Fmt.seeName(c.ch, dest[0]))+" is too full to hold "+Fmt.seeName(c.ch, o)+".";
				o.toChar(c.ch);
				break;
			}
			validTargets.add(o);
		}
		targets = validTargets.toArray(new ObjData[0]);
		
		for (UserCon cs : conns)
		{
			if (cs.ch.currentRoom == c.ch.currentRoom)
			{
				if (!cs.ch.position.equals("sleeping"))
				{
					String dropList[] = ObjData.listContents(cs.ch, targets, false);
					for (String s : dropList)
					{
						s = s.trim();
						if (cs == c)
							cs.sendln("You put "+s+" inside "+Fmt.seeName(c.ch, dest[0])+".");
						else
							cs.sendln(Fmt.seeName(cs.ch, c.ch)+" puts "+s+" inside "+Fmt.seeName(cs.ch, dest[0])+".");
					}
				}
			}
		}
		if (fullString.length() > 0)
			c.sendln(fullString);
	}
	
	public static void doDeposit(UserCon c, String args)
	{
		if (c.ch.position.equals("sleeping"))
		{
			c.sendln("You can't do that when you're "+c.ch.position+".");
			return;
		}

		boolean bankerFound = false;
		for (CharData ch : mobs)
			if (ch.currentRoom == c.ch.currentRoom)
				if (ch.flags.get("banker") && Combat.canSee(c.ch, ch))
				{
					if (ch.fighting != null)
					{
						c.sendln("The banker is too busy fighting to help you.");
						return;
					}
					if (!Combat.canSee(ch, c.ch))
					{
						c.sendln("The banker can't see you. Try going 'visible'.");
						return;
					}
					bankerFound = true;
				}
		if (!bankerFound)
		{
			c.sendln("There's no bank here.");
			return;
		}
				
		int amount = Fmt.getInt(args);
		if (amount <= 0)
		{
			c.sendln("Deposit how much?");
			return;
		}
		if (amount > c.ch.gold)
		{
			c.sendln("You don't have that much gold to deposit.");
			return;
		}
		
		c.ch.gold -= amount;
		c.ch.bank += amount;
		
		c.sendln("You deposit "+amount+" gold.");
		c.ch.save();
		return;
	}

	public static void doWithdraw(UserCon c, String args)
	{
		if (c.ch.position.equals("sleeping"))
		{
			c.sendln("You can't do that when you're "+c.ch.position+".");
			return;
		}

		boolean bankerFound = false;
		for (CharData ch : mobs)
			if (ch.currentRoom == c.ch.currentRoom)
				if (ch.flags.get("banker") && Combat.canSee(c.ch, ch))
				{
					if (ch.fighting != null)
					{
						c.sendln("The banker is too busy fighting to help you.");
						return;
					}
					if (!Combat.canSee(ch, c.ch))
					{
						c.sendln("The banker can't see you. Try going 'visible'.");
						return;
					}
					bankerFound = true;
				}
		if (!bankerFound)
		{
			c.sendln("There's no bank here.");
			return;
		}
				
		int amount = Fmt.getInt(args);
		if (amount <= 0)
		{
			c.sendln("Withdraw how much?");
			return;
		}
		if (amount > c.ch.bank)
		{
			c.sendln("You don't have that much gold in your bank.");
			return;
		}
		
		c.ch.bank -= amount;
		c.ch.gold += amount;
		
		c.sendln("You withdraw "+amount+" gold.");
		c.ch.save();
		return;
	}
	
	public static void doInventory(UserCon c, String args)
	{
		ArrayList<ObjData> display = new ArrayList<ObjData>();
		for (ObjData o : c.ch.objects)
			if (o.wearloc.equals("none"))
				display.add(o);

		if (display.size() == 0)
		{
			c.sendln("You aren't carrying anything.");
			return;
		}
		else
		{
			if (display.size() == 1)
				c.sendln("You are carrying "+display.size()+" item:");
			else
				c.sendln("You are carrying "+display.size()+" items:");
		}

		String objList[] = ObjData.listContents(c.ch, display.toArray(new ObjData[0]), false);
		for (String s : objList)
			c.sendln(s);
	}
	
	public static void doEquipment(UserCon c, String args)
	{
		if (args.equalsIgnoreCase("empty"))
			c.sendln("Unfilled slots:");
		else
			c.sendln("You are using:");

		boolean printed = false;
		for (String t : Flags.wearlocs)
		{
			ObjData targetObj = c.ch.getWearloc(t);
			if (targetObj != null && !args.equalsIgnoreCase("empty"))
			{
				printed = true;
				String tempString = "}M<}m"+targetObj.wearlocName()+"}M>";
				c.sendln(Fmt.fit(tempString, 18)+": }N"+Fmt.getLookFlags(targetObj, c)+"}N"+Fmt.seeName(c.ch, targetObj)+"{x");
			}
			else if (targetObj == null && (args.equalsIgnoreCase("empty") || args.equalsIgnoreCase("all")))
			{
				ObjData tempObj = new ObjData(0);
				tempObj.currentChar = c.ch;
				tempObj.wearloc = t;
				printed = true;
				String tempString = "}M<}m"+tempObj.wearlocName()+"}M>";
				c.sendln(Fmt.fit(tempString, 18)+": }N(empty){x");
			}
		}
		
		if (!printed)
			c.sendln("    Nothing.");
	}
	
	public static void doWear(UserCon c, String args)
	{
		if (c.ch.position.equals("sleeping"))
		{
			c.sendln("You can't do that when you're "+c.ch.position+".");
			return;
		}
		if (c.ch.fighting != null)
		{
			c.sendln("You can't equip items when you're fighting.");
			return;
		}

		if (args.length() == 0)
		{
			c.sendln("Wear what?");
			return;
		}
		
		ArrayList<ObjData> inv = new ArrayList<ObjData>();
		for (ObjData o : c.ch.objects)
			if (o.wearloc.equals("none"))
				inv.add(o);
		
		ObjData targets[] = Combat.findObj(c.ch, inv, args);
		ObjData swap = null;
		
		if (targets.length == 0)
		{
			c.sendln("You don't have an object matching that description.");
			return;
		}
		
		for (ObjData o : targets)
		{
			if (o.level > c.ch.level)
			{
				if (!args.equalsIgnoreCase("all"))
					c.sendln("You must be at least level "+o.level+" to use "+Fmt.seeName(c.ch, o)+".");
				continue;
			}
			for (String w : Flags.wearlocs)
				if (o.type.equals(w))
				{
					swap = c.ch.getWearloc(w);
					o.wearloc = w;
				}
			if (o.type.equals("ring"))
			{
				swap = c.ch.getWearloc("finger1");
				if (swap == null)
					o.wearloc = "finger1";
				else
				{
					swap = c.ch.getWearloc("finger2");
					o.wearloc = "finger2";
				}
			}
			if (o.type.equals("trinket"))
			{
				swap = c.ch.getWearloc("trinket1");
				if (swap == null)
					o.wearloc = "trinket1";
				else
				{
					swap = c.ch.getWearloc("trinket2");
					o.wearloc = "trinket2";
				}
			}
			if (o.type.equals("weapon"))
			{
				String checkSkill = "";
				if (o.value1.equals("sword"))
					checkSkill = "swords";
				else if (o.value1.equals("heavy_sword"))
					checkSkill = "heavy swords";
				else if (o.value1.equals("dagger"))
					checkSkill = "daggers";
				else if (o.value1.equals("mace"))
					checkSkill = "maces";
				else if (o.value1.equals("heavy_mace"))
					checkSkill = "heavy maces";
				else if (o.value1.equals("flail"))
					checkSkill = "flails";
				else if (o.value1.equals("axe"))
					checkSkill = "axes";
				else if (o.value1.equals("heavy_axe"))
					checkSkill = "heavy axes";
				else if (o.value1.equals("polearm"))
					checkSkill = "polearms";
				else if (o.value1.equals("whip"))
					checkSkill = "whips";
				else if (o.value1.equals("staff"))
					checkSkill = "staves";
				else if (o.value1.equals("hand_weapon"))
					checkSkill = "hand to hand";
				
				if (Skill.lookup(checkSkill) != null)
					if (c.ch.skillPercent(Skill.lookup(checkSkill)) == 0)
					{
						o.wearloc = "none";
						c.sendln("You don't know how to wield "+Fmt.seeName(c.ch, o)+".");
						continue;
					}

				if (o.value1.equals("heavy_sword")
					|| o.value1.equals("heavy_mace")
					|| o.value1.equals("heavy_axe")
					|| o.value1.equals("polearm")
					|| o.value1.equals("staff"))
				{
					if (c.ch.getWearloc("wield2") != null
						|| c.ch.getWearloc("shield") != null)
					{
						o.wearloc = "none";
						c.sendln("You need to remove "+Fmt.seeName(c.ch, swap)+" before you can wield a two-handed weapon.");
						continue;
					}
				}

				swap = c.ch.getWearloc("wield1");
				if (swap == null)
					o.wearloc = "wield1";
				else
				{
					swap = c.ch.getWearloc("wield2");
					if (swap == null)
						swap = c.ch.getWearloc("shield");
						
					if (c.ch.skillPercent(Skill.lookup("dual wield")) == 0)
					{
						o.wearloc = "none";
						c.sendln("You don't know how to dual wield.");
						continue;
					}
					o.wearloc = "wield2";
				}
			}
			if (o.type.equals("shield"))
			{
				ObjData tempObj = c.ch.getWearloc("wield1");
				if (tempObj != null)
					if (tempObj.value1.equals("heavy_sword")
						|| tempObj.value1.equals("heavy_mace")
						|| tempObj.value1.equals("heavy_axe")
						|| tempObj.value1.equals("polearm")
						|| tempObj.value1.equals("staff"))
					{
						o.wearloc = "none";
						c.sendln("You can't use a shield with a two-handed weapon.");
						continue;
					}
					
				if (c.ch.skillPercent(Skill.lookup("shields")) == 0)
				{
					o.wearloc = "none";
					c.sendln("You don't know how to use shields.");
					continue;
				}

				if (swap == null)
					swap = c.ch.getWearloc("wield2");
			}

			if (o.wearloc.equals("none"))
			{
				if (!args.equalsIgnoreCase("all"))
					c.sendln("You can't wear "+Fmt.seeName(c.ch, o)+"{x.");
				continue;
			}
			
			if (swap != null)
			{
				if (args.equalsIgnoreCase("all"))
				{
					o.wearloc = "none";
					continue;
				}
				swap.wearloc = "none";
				for (UserCon cs : conns)
					if (cs.ch.currentRoom == c.ch.currentRoom)
					{
						if (!cs.ch.position.equals("sleeping"))
						{
							if (cs == c)
								cs.sendln("You remove "+Fmt.seeName(c.ch, swap)+"{x.");
							else
								cs.sendln(Fmt.cap(Fmt.seeName(cs.ch, c.ch))+" removes "+Fmt.seeName(cs.ch, swap)+"{x.");
						}
					}
			}
				
			for (UserCon cs : conns)
				if (cs.ch.currentRoom == c.ch.currentRoom)
				{
					if (!cs.ch.position.equals("sleeping"))
					{
						if (cs == c)
							cs.sendln("You wear "+Fmt.seeName(cs.ch, o)+"{x.");
						else
							cs.sendln(Fmt.cap(Fmt.seeName(cs.ch, c.ch))+" wears "+Fmt.seeName(cs.ch, o)+"{x.");
					}
				}
			o.checkTrigger("wear", c.ch, null, "", 0);
		}
	}
	
	public static void doRemove(UserCon c, String args)
	{
		if (c.ch.position.equals("sleeping"))
		{
			c.sendln("You can't do that when you're "+c.ch.position+".");
			return;
		}

		if (args.length() == 0)
		{
			c.sendln("Remove what?");
			return;
		}
		
		ArrayList<ObjData> equipped = new ArrayList<ObjData>();
		for (ObjData o : c.ch.objects)
			if (!o.wearloc.equals("none"))
				equipped.add(o);
		
		ObjData targets[] = Combat.findObj(c.ch, equipped, args);
		
		if (targets.length == 0)
		{
			c.sendln("You aren't wearing an object matching that description.");
			return;
		}
		
		for (ObjData o : targets)
		{
			o.wearloc = "none";
			for (UserCon cs : conns)
			{
				if (cs.ch.currentRoom == c.ch.currentRoom)
				{
					if (!cs.ch.position.equals("sleeping"))
					{
						if (cs == c)
							cs.sendln("You remove "+Fmt.seeName(c.ch, o)+"{x.");
						else
							cs.sendln(Fmt.cap(Fmt.seeName(cs.ch, c.ch))+" removes "+Fmt.seeName(cs.ch, o)+"{x.");
					}
				}
			}
			o.checkTrigger("remove", c.ch, null, "", 0);
		}
	}
	
	public static void doUse(UserCon c, String args)
	{
		if (args.length() == 0)
		{
			c.sendln("Use what?");
			return;
		}

		ObjData targets[] = Combat.findObj(c.ch, ObjData.allVisibleObjects(c.ch), args);
		if (targets.length == 0)
		{
			c.sendln("You don't have an object matching that description.");
			return;
		}
		ObjData target = targets[0];
		target.use(c);
	}
	
	public static void doEnter(UserCon c, String args)
	{
		if (args.length() == 0)
		{
			c.sendln("Enter what?");
			return;
		}

		ObjData targets[] = Combat.findObj(c.ch, ObjData.allVisibleObjects(c.ch), args);
		if (targets.length == 0)
		{
			c.sendln("There's no object here matching that description.");
			return;
		}
		ObjData target = targets[0];
		if (target.type.equals("portal"))
			target.use(c);
		else
			c.sendln("That's not a portal.");
	}
	
	public static void doEat(UserCon c, String args)
	{
		if (args.length() == 0)
		{
			c.sendln("Eat what?");
			return;
		}

		ObjData targets[] = Combat.findObj(c.ch, ObjData.allVisibleObjects(c.ch), args);
		if (targets.length == 0)
		{
			c.sendln("There's no object here matching that description.");
			return;
		}
		ObjData target = targets[0];
		if (target.type.equals("food"))
			target.use(c);
		else
			c.sendln("That's not edible.");
	}

	public static void doDrink(UserCon c, String args)
	{
		if (args.length() == 0)
		{
			c.sendln("Drink what?");
			return;
		}

		ObjData targets[] = Combat.findObj(c.ch, ObjData.allVisibleObjects(c.ch), args);
		if (targets.length == 0)
		{
			c.sendln("There's no object here matching that description.");
			return;
		}
		ObjData target = targets[0];
		if (target.type.equals("drink"))
			target.use(c);
		else
			c.sendln("You can't drink that.");
	}
	
	public static void doExamine(UserCon c, String args)
	{
		if (args.length() == 0)
		{
			c.sendln("Examine what?");
			return;
		}

		ObjData targets[] = Combat.findObj(c.ch, ObjData.allVisibleObjects(c.ch), args);
		if (targets.length == 0)
		{
			c.sendln("There's no object here matching that description.");
			return;
		}
		ObjData target = targets[0];
		c.sendln(target.showInfo());
		target.checkTrigger("look", c.ch, null, "", 0);
	}
	
	public static void doStand(UserCon c, String args)
	{
		String actionString = "";
		if (args.length() > 0)
		{
			ObjData target[] = Combat.findObj(c.ch, ObjData.allVisibleObjects(c.ch), args);
			if (target.length == 0)
			{
				c.sendln("There's no furniture here matching that description.");
				return;
			}
			if (!target[0].type.equals("furniture"))
			{
				c.sendln("That object isn't furniture.");
				return;
			}
			if (!target[0].typeFlags.get("stand"))
			{
				c.sendln("You can't stand on "+Fmt.seeName(c.ch, target[0])+".");
				return;
			}
			if (target[0] == c.ch.positionTarget && c.ch.position.equals("standing"))
			{
				c.sendln("You're already standing on that.");
				return;
			}
			
			if (!target[0].value1.equals("0"))
			{
				int current = 0;
				for (CharData ch : allChars())
					if (ch.currentRoom == c.ch.currentRoom && ch.positionTarget == target[0] && ch != c.ch)
						current++;
				if (current >= Fmt.getInt(target[0].value1))
				{
					c.sendln(Fmt.cap(Fmt.seeName(c.ch, target[0]))+"{x can't hold anyone else.");
					return;
				}
			}
			
			if (target[0].checkTrigger("use", c.ch, null, "stand", 0) == 0)
				return;
			
			if (c.ch.position.equals("standing") && c.ch.positionTarget != null)
			{
				for (UserCon cs : conns)
					if (cs.ch.currentRoom == c.ch.currentRoom && c != cs)
						if (!cs.ch.position.equals("sleeping"))
							cs.sendln(Fmt.cap(Fmt.seeName(cs.ch, c.ch))+" gets off of "+Fmt.seeName(cs.ch, c.ch.positionTarget)+"{x and stands on "+Fmt.seeName(cs.ch, target[0])+"{x.");
				c.sendln("You get off of "+Fmt.seeName(c.ch, c.ch.positionTarget)+"{x and stand on "+Fmt.seeName(c.ch, target[0])+".");
			}
			else if (c.ch.position.equals("sleeping"))
			{
				for (UserCon cs : conns)
					if (cs.ch.currentRoom == c.ch.currentRoom && c != cs)
						if (!cs.ch.position.equals("sleeping"))
							cs.sendln(Fmt.cap(Fmt.seeName(cs.ch, c.ch))+" wakes up and stands on "+Fmt.seeName(cs.ch, target[0])+"{x.");
				c.sendln("You wake up and stand on "+Fmt.seeName(c.ch, target[0])+".");
			}
			else
			{
				for (UserCon cs : conns)
					if (cs.ch.currentRoom == c.ch.currentRoom && c != cs)
						if (!cs.ch.position.equals("sleeping"))
							cs.sendln(Fmt.cap(Fmt.seeName(cs.ch, c.ch))+" stands on "+Fmt.seeName(cs.ch, target[0])+"{x.");
				c.sendln("You stand on "+Fmt.seeName(c.ch, target[0])+".");
			}
			c.ch.position = "standing";
			c.ch.positionTarget = target[0];
			return;
		}
		if (c.ch.position.equals("standing") && c.ch.positionTarget == null)
		{
			c.sendln("You're already standing.");
			return;
		}

		if (c.ch.position.equals("standing") && c.ch.positionTarget != null)
		{
			for (UserCon cs : conns)
				if (cs.ch.currentRoom == c.ch.currentRoom && c != cs)
					if (!cs.ch.position.equals("sleeping"))
						cs.sendln(Fmt.cap(Fmt.seeName(cs.ch, c.ch))+" gets off of "+Fmt.seeName(cs.ch, c.ch.positionTarget)+"{x.");
			c.sendln("You get off of "+Fmt.seeName(c.ch, c.ch.positionTarget)+"{x.");
		}
		else if (c.ch.position.equals("sleeping"))
		{
			actionString = " wakes and stands up.";
			c.sendln("You wake and stand up.");
		}
		else
		{
			actionString = " stands up.";
			c.sendln("You stand up.");
		}
		c.ch.position = "standing";
		c.ch.positionTarget = null;
		for (UserCon cs : conns)
			if (cs.ch.currentRoom == c.ch.currentRoom && c != cs)
				if (!cs.ch.position.equals("sleeping"))
					cs.sendln(Fmt.cap(Fmt.seeName(cs.ch, c.ch))+actionString);
	}
	
	public static void doSit(UserCon c, String args)
	{
		boolean foundObj = false;
		ObjData target[] = {};

		String actionString = "";
		if (c.ch.fighting != null)
		{
			c.sendln("You can't sit down while you're fighting.");
			return;
		}

		if (args.length() == 0)
			for (ObjData o : c.ch.objects)
				if (o.type.equals("furniture"))
					if (o.typeFlags.get("sit"))
					{
						target = new ObjData[1];
						target[0] = o;
						foundObj = true;
					}

		if (args.length() > 0 || foundObj)
		{
			if (!foundObj)
				target = Combat.findObj(c.ch, ObjData.allVisibleObjects(c.ch), args);
			if (target.length == 0)
			{
				c.sendln("There's no furniture here matching that description.");
				return;
			}
			if (!target[0].type.equals("furniture"))
			{
				c.sendln("That object isn't furniture.");
				return;
			}
			if (!target[0].typeFlags.get("sit"))
			{
				c.sendln("You can't sit on "+Fmt.seeName(c.ch, target[0])+".");
				return;
			}
			if (target[0] == c.ch.positionTarget && c.ch.position.equals("sitting"))
			{
				c.sendln("You're already sitting on that.");
				return;
			}

			if (!target[0].value1.equals("0"))
			{
				int current = 0;
				for (CharData ch : allChars())
					if (ch.currentRoom == c.ch.currentRoom && ch.positionTarget == target[0] && ch != c.ch)
						current++;
				if (current >= Fmt.getInt(target[0].value1))
				{
					c.sendln(Fmt.cap(Fmt.seeName(c.ch, target[0]))+"{x can't hold anyone else.");
					return;
				}
			}
			
			if (target[0].checkTrigger("use", c.ch, null, "sit", 0) == 0)
				return;

			if (c.ch.position.equals("sleeping"))
			{
				actionString = " wakes up and sits on ";
				c.sendln("You wake up and sit on "+Fmt.seeName(c.ch, target[0])+".");
			}
			else
			{
				actionString = " sits on ";
				c.sendln("You sit on "+Fmt.seeName(c.ch, target[0])+".");
			}
			c.ch.position = "sitting";
			c.ch.positionTarget = target[0];
			for (UserCon cs : conns)
				if (cs.ch.currentRoom == c.ch.currentRoom && c != cs)
					if (!cs.ch.position.equals("sleeping"))
						cs.sendln(Fmt.cap(Fmt.seeName(cs.ch, c.ch))+actionString+Fmt.seeName(cs.ch, target[0])+".");
			return;
		}
		if (c.ch.position.equals("sitting") && c.ch.positionTarget == null)
		{
			c.sendln("You're already sitting.");
			return;
		}
		if (c.ch.position.equals("sleeping"))
		{
			actionString = " wakes up and sits down.";
			c.sendln("You wake up and sit down.");
		}
		else
		{
			actionString = " sits down.";
			c.sendln("You sit down.");
		}
		c.ch.position = "sitting";
		c.ch.positionTarget = null;
		for (UserCon cs : conns)
			if (cs.ch.currentRoom == c.ch.currentRoom && c != cs)
				if (!cs.ch.position.equals("sleeping"))
					cs.sendln(Fmt.cap(Fmt.seeName(cs.ch, c.ch))+actionString);
	}

	public static void doRest(UserCon c, String args)
	{
		boolean foundObj = false;
		ObjData target[] = {};

		String actionString = "";
		if (c.ch.fighting != null)
		{
			c.sendln("You can't rest while you're fighting.");
			return;
		}

		if (args.length() == 0)
			for (ObjData o : c.ch.objects)
				if (o.type.equals("furniture"))
					if (o.typeFlags.get("rest"))
					{
						target = new ObjData[1];
						target[0] = o;
						foundObj = true;
					}

		if (args.length() > 0 || foundObj)
		{
			if (!foundObj)
				target = Combat.findObj(c.ch, ObjData.allVisibleObjects(c.ch), args);
			if (target.length == 0)
			{
				c.sendln("There's no furniture here matching that description.");
				return;
			}
			if (!target[0].type.equals("furniture"))
			{
				c.sendln("That object isn't furniture.");
				return;
			}
			if (!target[0].typeFlags.get("rest"))
			{
				c.sendln("You can't rest on "+Fmt.seeName(c.ch, target[0])+".");
				return;
			}
			if (target[0] == c.ch.positionTarget && c.ch.position.equals("resting"))
			{
				c.sendln("You're already resting on that.");
				return;
			}

			if (!target[0].value1.equals("0"))
			{
				int current = 0;
				for (CharData ch : allChars())
					if (ch.currentRoom == c.ch.currentRoom && ch.positionTarget == target[0] && ch != c.ch)
						current++;
				if (current >= Fmt.getInt(target[0].value1))
				{
					c.sendln(Fmt.cap(Fmt.seeName(c.ch, target[0]))+"{x can't hold anyone else.");
					return;
				}
			}
			
			if (target[0].checkTrigger("use", c.ch, null, "rest", 0) == 0)
				return;

			if (c.ch.position.equals("sleeping"))
			{
				actionString = " wakes up and rests on ";
				c.sendln("You wake up and rest on "+Fmt.seeName(c.ch, target[0])+".");
			}
			else
			{
				actionString = " rests on ";
				c.sendln("You rest on "+Fmt.seeName(c.ch, target[0])+".");
			}
			c.ch.position = "resting";
			c.ch.positionTarget = target[0];
			for (UserCon cs : conns)
				if (cs.ch.currentRoom == c.ch.currentRoom && c != cs)
					if (!cs.ch.position.equals("sleeping"))
						cs.sendln(Fmt.cap(Fmt.seeName(cs.ch, c.ch))+actionString+Fmt.seeName(cs.ch, target[0])+".");
			return;
		}
		if (c.ch.position.equals("resting") && c.ch.positionTarget == null)
		{
			c.sendln("You're already resting.");
			return;
		}
		if (c.ch.position.equals("sleeping"))
		{
			actionString = " wakes up and begins resting.";
			c.sendln("You wake up and begin resting.");
		}
		else
		{
			actionString = " begins resting.";
			c.sendln("You begin resting.");
		}
		c.ch.position = "resting";
		c.ch.positionTarget = null;
		for (UserCon cs : conns)
			if (cs.ch.currentRoom == c.ch.currentRoom && c != cs)
				if (!cs.ch.position.equals("sleeping"))
					cs.sendln(Fmt.cap(Fmt.seeName(cs.ch, c.ch))+actionString);
	}

	public static void doSleep(UserCon c, String args)
	{
		boolean foundObj = false;
		ObjData target[] = {};

		String actionString = "";
		if (c.ch.fighting != null)
		{
			c.sendln("You can't sleep while you're fighting.");
			return;
		}

		if (args.length() == 0)
			for (ObjData o : c.ch.objects)
				if (o.type.equals("furniture"))
					if (o.typeFlags.get("sleep"))
					{
						target = new ObjData[1];
						target[0] = o;
						foundObj = true;
					}

		if (args.length() > 0 || foundObj)
		{
			if (!foundObj)
				target = Combat.findObj(c.ch, ObjData.allVisibleObjects(c.ch), args);
			if (target.length == 0)
			{
				c.sendln("There's no furniture here matching that description.");
				return;
			}
			if (!target[0].type.equals("furniture"))
			{
				c.sendln("That object isn't furniture.");
				return;
			}
			if (!target[0].typeFlags.get("sleep"))
			{
				c.sendln("You can't sleep on "+Fmt.seeName(c.ch, target[0])+".");
				return;
			}
			if (target[0] == c.ch.positionTarget && c.ch.position.equals("sleeping"))
			{
				c.sendln("You're already sleeping on that.");
				return;
			}

			if (!target[0].value1.equals("0"))
			{
				int current = 0;
				for (CharData ch : allChars())
					if (ch.currentRoom == c.ch.currentRoom && ch.positionTarget == target[0] && ch != c.ch)
						current++;
				if (current >= Fmt.getInt(target[0].value1) && c.ch.positionTarget != target[0])
				{
					c.sendln(Fmt.cap(Fmt.seeName(c.ch, target[0]))+"{x can't hold anyone else.");
					return;
				}
			}

			if (target[0].checkTrigger("use", c.ch, null, "sleep", 0) == 0)
				return;
			
			if (c.ch.position.equals("sleeping"))
			{
				actionString = " wakes up and goes to sleep on ";
				c.sendln("You wake up and go to sleep on "+Fmt.seeName(c.ch, target[0])+".");
			}
			else
			{
				actionString = " goes to sleep on ";
				c.sendln("You go to sleep on "+Fmt.seeName(c.ch, target[0])+".");
			}
			c.ch.position = "sleeping";
			c.ch.positionTarget = target[0];
			for (UserCon cs : conns)
				if (cs.ch.currentRoom == c.ch.currentRoom && c != cs)
					if (!cs.ch.position.equals("sleeping"))
						cs.sendln(Fmt.cap(Fmt.seeName(cs.ch, c.ch))+actionString+Fmt.seeName(cs.ch, target[0])+".");
			return;
		}
		if (c.ch.position.equals("sleeping") && c.ch.positionTarget == null)
		{
			c.sendln("You're already sleeping.");
			return;
		}
		if (c.ch.position.equals("sleeping"))
		{
			actionString = " wakes up and goes to sleep on the ground.";
			c.sendln("You wake up and go to sleep on the ground.");
		}
		else
		{
			actionString = " goes to sleep.";
			c.sendln("You go to sleep.");
		}
		c.ch.position = "sleeping";
		c.ch.positionTarget = null;
		for (UserCon cs : conns)
			if (cs.ch.currentRoom == c.ch.currentRoom && c != cs)
				if (!cs.ch.position.equals("sleeping"))
					cs.sendln(Fmt.cap(Fmt.seeName(cs.ch, c.ch))+actionString);
	}
}