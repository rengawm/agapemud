package jw.commands;
import java.util.*;

import jw.core.*;
import static jw.core.MudMain.*;

/**
	The CombatCommands class contains commands which are primarily used for combat.
*/
public class CombatCommands
{
	/**
	Initiate a fight between the player and the target, if the target is found in
	the room.
	<p>
	The method will first use {@link Combat#findChar(CharData, Room, String, boolean)
	Combat.findChar} to obtain a target. If a target was found, combat is initiated
	and the player may perform one free attack on the target. If the player was already
	in combat, this will instead switch their attack target to the specified one.
	
	@param c The user who entered this command.
	@param args A string containing any arguments to the command.
	*/
	public static void doAttack(UserCon c, String args)
	{
		if (!c.ch.position.equals("standing"))
		{
			c.sendln("You can't do that when you're "+c.ch.position+".");
			return;
		}
		if (args.trim().length() == 0)
		{
			c.sendln("Attack who?");
			return;
		}
			
		CharData target = Combat.findChar(c.ch, null, args, false);
		
		if (target == null)
		{
			c.sendln("There isn't anyone here by that name.");
			return;
		}
		if (target == c.ch)
		{
			c.sendln("You can't fight yourself.");
			return;
		}
		if (target == c.ch.fighting)
		{
			c.sendln("You're already fighting "+Fmt.seeName(c.ch, target)+".");
			return;
		}
		
		boolean newFight = false;
		if (c.ch.fighting == null)
			newFight = true;
		
		c.ch.fighting = target;
		if (target.fighting == null)
			target.fighting = c.ch;
		if (!target.position.equals("sleeping"))
			target.sendln(Fmt.cap(Fmt.seeName(target, c.ch))+" rushes toward you and attacks!");
		
		if (newFight)
		{
			Combat.combatRound(c.ch);
			c.delay = 15;
		}
		else
			c.sendln("You shift your attention to "+Fmt.seeName(c.ch, target)+".");
	}
	
	/**
	Show the player a message describing the difference between their level and the
	level/difficulty of the target.
	
	@param c The user who entered this command.
	@param args A string containing any arguments to the command.
	*/
	public static void doConsider(UserCon c, String args)
	{
		if (c.ch.position.equals("sleeping"))
		{
			c.sendln("You can't do that when you're "+c.ch.position+".");
			return;
		}
		if (args.length() == 0)
		{
			c.sendln("Consider attacking who?");
			return;
		}
			
		CharData target = Combat.findChar(c.ch, null, args, false);
		
		if (target == null)
		{
			c.sendln("There isn't anyone here by that name.");
			return;
		}
		if (target == c.ch)
		{
			c.sendln("You can't attack yourself.");
			return;
		}
		
		int compLevel = target.level;
		if (target.difficulty > 0)
			compLevel = (int)(target.level*(1.0+(target.difficulty/10.0)*2));
		if (target.difficulty < 0)
			compLevel = (int)(target.level*(1.0+(target.difficulty/10.0)/2));
		
		if (compLevel > c.ch.level+15)
			c.sendln(Fmt.cap(Fmt.seeName(c.ch, target))+" could probably kill you in a few seconds.");
		else if (compLevel > c.ch.level+10)
			c.sendln(Fmt.cap(Fmt.seeName(c.ch, target))+" would be extremely hard to beat.");
		else if (compLevel > c.ch.level+6)
			c.sendln(Fmt.cap(Fmt.seeName(c.ch, target))+" looks like a moderately difficult target.");
		else if (compLevel > c.ch.level+3)
			c.sendln("Fighting "+Fmt.seeName(c.ch, target)+" would be could be a bit challenging.");
		else if (compLevel < c.ch.level-3)
			c.sendln("You have a slight advantage against "+Fmt.seeName(c.ch, target)+".");
		else if (compLevel < c.ch.level-6)
			c.sendln(Fmt.cap(Fmt.seeName(c.ch, target))+" wouldn't put up too much of a fight.");
		else if (compLevel < c.ch.level-10)
			c.sendln(Fmt.cap(Fmt.seeName(c.ch, target))+" isn't really worth your effort.");
		else if (compLevel < c.ch.level-15)
			c.sendln(Fmt.cap(Fmt.seeName(c.ch, target))+" wouldn't take more than a few seconds to subdue.");
		else
			c.sendln("It would be a fairly even fight.");
	}
	
	public static void doCast(UserCon c, String args)
	{
		if (!c.ch.position.equals("standing"))
		{
			c.sendln("You can't do that when you're "+c.ch.position+".");
			return;
		}

		String arg1 = CommandHandler.getArg(args, 1);
		String arg2 = CommandHandler.getLastArg(args, 2);
		
		if (arg1.length() == 0)
		{
			c.sendln("Cast what?");
			return;
		}
		
		// Find a spell match and run it.
		for (Skill s : MudMain.skills)
			if (s.type.equals("spell")
				&& s.name.startsWith(arg1)
				&& c.ch.skillPercent(s) > 0)
			{
				if (s.flags.get("passive"))
				{
					c.sendln(Fmt.cap(s.name)+" is a passive spell and is activated automatically.");
					return;
				}
				if (c.ch.mana < s.cost)
				{
					c.sendln("You don't have enough mana to cast that spell.");
					return;
				}
				if (!Combat.checkTarget(c.ch, s, arg2))
					return;
				c.ch.combatQueue.add("dsp#"+s.useDelay);
				c.ch.combatQueue.add("\""+s.name+"\" "+arg2);
				c.showPrompt = false;
				return;
			}
		
		c.sendln("You don't know any spells of that name.");
	}
	
	public static void doQueuedump(UserCon c, String args)
	{
		if (c.ch.combatQueue.size() == 0)
		{
			c.sendln("You don't have any abilities queued for use.");
			return;
		}
		String temp = c.ch.combatQueue.get(0);
		c.ch.combatQueue = new ArrayList<String>();
		c.ch.targetQueue = new ArrayList<Object>();
		if (temp.startsWith("dsk#"))
			c.ch.combatQueue.add(temp);
		c.sendln("Combat queue emptied.");
	}
	
	public static void doEffects(UserCon c, String args)
	{
		if (c.ch.effects.size() == 0)
		{
			c.sendln("There are no spells or enhancements affecting you.");
			return;
		}
		c.sendln("The following spells and enhancements are affecting you:");
		c.sendln(Fmt.heading(""));
		c.sendln("}m  Level   Remaining   Effect");
		
		String[] ccs = {"}N", "{G", "{R"};
		for (String cctr : ccs)
		{
			for (Effect e : c.ch.effects)
			{
				String cc = "}N";
				for (String s : Flags.badEffects)
					if (s.equals(e.name))
						cc = "{R";
				for (String s : Flags.goodEffects)
					if (s.equals(e.name))
						cc = "{G";
				
				if (cc.equals(cctr))
				{
					if (e.duration == -1)
						c.sendln(" }M[ }n"+Fmt.fit(""+e.level, 4)+"}M] [ }ninfinite }M] "+cc+e.name);
					else if (e.duration/600 >= 1)
						c.sendln(" }M[ }n"+Fmt.fit(""+e.level, 4)+"}M] [ }n"+Fmt.fit(e.duration/600+" min", 9)+"}M] "+cc+e.name);
					else
						c.sendln(" }M[ }n"+Fmt.fit(""+e.level, 4)+"}M] [ }n"+Fmt.fit(e.duration/10+" sec", 9)+"}M] "+cc+e.name);
					for (String s : e.statMods.keySet())
						c.sendln("}N                      - affects }n"+Flags.fullStatName(s)+" }Nby }n"+e.statMods.get(s)+"}N.");
				}
			}
		}
		c.sendln(Fmt.heading("")+"{x");
	}
	
	public static void doCooldowns(UserCon c, String args)
	{
		if (c.ch.cooldowns.size() == 0)
		{
			c.sendln("You have no abilities on cooldown.");
			return;
		}
		
		c.sendln(Fmt.heading("Ability Cooldowns"));
		for (Skill s : c.ch.cooldowns.keySet())
			c.sendln("}m"+Fmt.rfit(s.name, 20)+"}M: }n"+c.ch.cooldowns.get(s)+" seconds{x");
		c.sendln(Fmt.heading(""));
	}
	
	public static void doFlee(UserCon c, String args)
	{
		if (c.ch.fighting == null)
		{
			c.sendln("You can't flee if you aren't fighting anyone.");
			return;
		}

		ArrayList<Exit> validExits = new ArrayList<Exit>();
		for (Exit ex : c.ch.currentRoom.exits)
			if (!ex.flags.get("closed")
				&& !ex.flags.get("hidden")
				)
				validExits.add(ex);
		if (validExits.size() == 0)
		{
			c.sendln("There aren't any readily usable exits from this room.");
			return;
		}
		for (CharData ch : allChars())
			if (ch.fighting == c.ch)
			{
				if (!ch.hating.contains(c.ch) && ch.conn.isDummy)
					ch.hating.add(c.ch);
				ch.fighting = null;
			}

		c.ch.fighting = null;
		c.ch.fleeing = true;

		c.sendln("You flee from combat!");
		Fmt.actAround(c.ch, null, null, "$n flees from combat!");
		
		c.ch.currentRoom.takeExit(c, validExits.get(gen.nextInt(validExits.size())).direction);
	}
	
	public static void doRecall(UserCon c, String args)
	{
		for (Effect e : c.ch.effects)
			if (e.name.equals("curse"))
			{
				c.sendln("You can't recall when you're cursed.");
				return;
			}
		if (c.ch.currentRoom.flags.get("norecall"))
		{
			c.sendln("You can't recall from this room.");
			return;
		}
		if (c.ch.fighting != null)
		{
			c.sendln("You can't recall while in combat. Try fleeing first.");
			return;
		}
		if (!c.ch.position.equals("standing"))
		{
			c.sendln("You can't do that when you're "+c.ch.position+".");
			return;
		}
		
		if (c.ch.currentRoom.id == Flags.recallId)
		{
			c.sendln("You're already at your chosen recall location.");
			return;
		}
		
		if (Room.lookup(Flags.recallId) == null)
		{
			sysLog("bugs", "No recall room found at ID "+Flags.recallId);
			return;
		}
		
		Fmt.actAround(c.ch, null, null, "$n prays for transportation...");
		Fmt.actAround(c.ch, null, null, "$n disappears!");
		c.ch.currentRoom = Room.lookup(Flags.recallId);
		RoomCommands.doLook(c, "");
		Fmt.actAround(c.ch, null, null, "$n appears in the room.");
	}
	
	public static void doZap(UserCon c, String args)
	{
		String arg1 = CommandHandler.getArg(args, 1);
		String arg2 = CommandHandler.getArg(args, 2);
		
		if (c.ch.skillPercent(Skill.lookup("wands")) == 0)
		{
			c.sendln("You don't know how to use wands.");
			return;
		}
		
		if (!c.ch.position.equals("standing"))
		{
			c.sendln("You can't do that when you're "+c.ch.position+".");
			return;
		}

		ObjData using = null;
		if (arg2.length() == 0)
		{
			for (ObjData o : c.ch.objects)
				if (o.type.equals("wand"))
				{
					using = o;
					break;
				}
			
			if (using == null)
			{
				c.sendln("You're not carrying a wand in your inventory.");
				return;
			}
		}
		else
		{
			ObjData objArr[] = Combat.findObj(c.ch, c.ch.objects, arg2);
			if (objArr.length > 0)
				using = objArr[0];
		}
		
		if (using == null)
		{
			c.sendln("You don't have a wand matching that description.");
			return;
		}
		if (!using.type.equals("wand"))
		{
			c.sendln(Fmt.cap(Fmt.seeName(c.ch, using))+"{x is not a wand.");
			return;
		}
		
		Skill usingSk = Skill.lookup(using.value2);
		if (usingSk == null)
		{
			c.sendln("That wand isn't linked to a valid spell.");
			return;
		}
		
		int usingLv = Fmt.getInt(using.value1);
		if (usingLv < Flags.minLevel || usingLv > Flags.maxLevel)
		{
			c.sendln("That wand's effect level is out of range.");
			return;
		}

		if (using.level > c.ch.level)
		{
			c.sendln("You must be level "+using.level+" to use that wand.");
			return;
		}
		
		int usingRem = Fmt.getInt(using.value4);
		if (usingRem < 1)
		{
			c.sendln("That wand has no remaining charges.");
			return;
		}
		
		if (c.ch.queueDelay > 0)
		{
			c.sendln("You can't use wands while you have an active combat queue.");
			return;
		}
		
		if (!Combat.checkTarget(c.ch, usingSk, arg1))
			return;
		
		using.value4 = ""+(usingRem-1);
		c.ch.queueDelay = (int)(usingSk.useDelay*Formulas.castSpeedMod(c.ch));
		
		if (gen.nextInt(50)+51 > c.ch.skillPercent(Skill.lookup("wands")))
		{
			c.sendln("The wand shivers in your hand as the spell fails.");
			Fmt.actAround(c.ch, null, using, "$n tries to use $o, but fails.");
			Skill.lookup("wands").checkFailure(c.ch);
			return;
		}
		
		usingSk.runAsObject(c, usingLv, arg1, c.ch.targetQueue.get(0));
		c.ch.targetQueue.remove(0);
		Skill.lookup("wands").checkGain(c.ch);
	}
	
	public static void doRecite(UserCon c, String args)
	{
		String arg1 = CommandHandler.getArg(args, 1);
		String arg2 = CommandHandler.getArg(args, 2);
		
		if (c.ch.skillPercent(Skill.lookup("scrolls")) == 0)
		{
			c.sendln("You don't know how to use scrolls.");
			return;
		}
		
		if (!c.ch.position.equals("standing"))
		{
			c.sendln("You can't do that when you're "+c.ch.position+".");
			return;
		}

		ObjData using = null;
		if (arg1.length() == 0)
		{
			for (ObjData o : c.ch.objects)
				if (o.type.equals("scroll"))
				{
					using = o;
					break;
				}
			
			if (using == null)
			{
				c.sendln("You're not carrying a scroll in your inventory.");
				return;
			}
		}
		else
		{
			ObjData objArr[] = Combat.findObj(c.ch, c.ch.objects, arg1);
			if (objArr.length > 0)
				using = objArr[0];
		}
		
		if (using == null)
		{
			c.sendln("You don't have a scroll matching that description.");
			return;
		}
		if (!using.type.equals("scroll"))
		{
			c.sendln(Fmt.cap(Fmt.seeName(c.ch, using))+"{x is not a scroll.");
			return;
		}
		
		ArrayList<Skill> usingSk = new ArrayList<Skill>();
		Skill usingSk1 = Skill.lookup(using.value2);
		if (usingSk1 != null)
			usingSk.add(usingSk1);
		Skill usingSk2 = Skill.lookup(using.value3);
		if (usingSk2 != null)
			usingSk.add(usingSk2);
		Skill usingSk3 = Skill.lookup(using.value4);
		if (usingSk3 != null)
			usingSk.add(usingSk3);
		Skill usingSk4 = Skill.lookup(using.value5);
		if (usingSk4 != null)
			usingSk.add(usingSk4);

		if (usingSk.size() == 0)
		{
			c.sendln("That scroll isn't linked to a valid spell.");
			return;
		}
		
		int usingLv = Fmt.getInt(using.value1);
		if (usingLv < Flags.minLevel || usingLv > Flags.maxLevel)
		{
			c.sendln("That scroll's effect level is out of range.");
			return;
		}

		if (using.level > c.ch.level)
		{
			c.sendln("You must be level "+using.level+" to use that scroll.");
			return;
		}
		
		if (c.ch.queueDelay > 0)
		{
			c.sendln("You can't use scrolls while you have an active combat queue.");
			return;
		}
		
		int maxDelay = 0;
		boolean foundTarget = false;
		
		ArrayList<Skill> clearedSk = new ArrayList<Skill>();
		for (Skill sk : usingSk)
		{
			if (sk.useDelay > maxDelay)
				maxDelay = sk.useDelay;
			if (!Combat.checkTarget(c.ch, sk, arg2))
				continue;
			clearedSk.add(sk);
			foundTarget = true;
		}
		
		if (foundTarget)
		{
			using.clearObjects();
			if (gen.nextInt(50)+51 > c.ch.skillPercent(Skill.lookup("scrolls")))
			{
				c.sendln("The incantation fails as you mispronounce part of it.");
				Fmt.actAround(c.ch, null, using, "$n tries to use $o, but fails.");
				Skill.lookup("scrolls").checkFailure(c.ch);
				return;
			}
			Skill.lookup("scrolls").checkGain(c.ch);
			c.sendln("You recite "+Fmt.seeName(c.ch, using)+".");
			Fmt.actAround(c.ch, null, using, "$n recites $o.");
		}
		
		for (Skill sk : clearedSk)
		{
			sk.runAsObject(c, usingLv, arg2, c.ch.targetQueue.get(0));
			c.ch.targetQueue.remove(0);
		}
		
		c.ch.queueDelay = maxDelay;
	}
	
	public static void doQuaff(UserCon c, String args)
	{
		if (!c.ch.position.equals("standing"))
		{
			c.sendln("You can't do that when you're "+c.ch.position+".");
			return;
		}

		ObjData using = null;
		if (args.length() == 0)
		{
			for (ObjData o : c.ch.objects)
				if (o.type.equals("potion"))
				{
					using = o;
					break;
				}
			
			if (using == null)
			{
				c.sendln("You're not carrying a potion in your inventory.");
				return;
			}
		}
		else
		{
			ObjData objArr[] = Combat.findObj(c.ch, c.ch.objects, args);
			if (objArr.length > 0)
				using = objArr[0];
		}
		
		if (using == null)
		{
			c.sendln("You don't have a potion matching that description.");
			return;
		}
		if (!using.type.equals("potion"))
		{
			c.sendln(Fmt.cap(Fmt.seeName(c.ch, using))+"{x is not a potion.");
			return;
		}
		
		ArrayList<Skill> usingSk = new ArrayList<Skill>();
		Skill usingSk1 = Skill.lookup(using.value2);
		if (usingSk1 != null)
			usingSk.add(usingSk1);
		Skill usingSk2 = Skill.lookup(using.value3);
		if (usingSk2 != null)
			usingSk.add(usingSk2);
		Skill usingSk3 = Skill.lookup(using.value4);
		if (usingSk3 != null)
			usingSk.add(usingSk3);
		Skill usingSk4 = Skill.lookup(using.value5);
		if (usingSk4 != null)
			usingSk.add(usingSk4);

		if (usingSk.size() == 0)
		{
			c.sendln("That potion isn't linked to a valid spell.");
			return;
		}
		
		int usingLv = Fmt.getInt(using.value1);
		if (usingLv < Flags.minLevel || usingLv > Flags.maxLevel)
		{
			c.sendln("That potion's effect level is out of range.");
			return;
		}
		
		if (using.level > c.ch.level)
		{
			c.sendln("You must be level "+using.level+" to use that potion.");
			return;
		}
		
		ArrayList<Skill> clearedSk = new ArrayList<Skill>();
		for (Skill sk : usingSk)
		{
			if (!Combat.checkTarget(c.ch, sk, "self"))
				continue;
			clearedSk.add(sk);
		}
		
		using.clearObjects();
		c.sendln("You quaff "+Fmt.seeName(c.ch, using)+".");
		Fmt.actAround(c.ch, null, using, "$n quaffs $o.");
		
		for (Skill sk : clearedSk)
		{
			sk.runAsObject(c, usingLv, "self", c.ch.targetQueue.get(0));
			c.ch.targetQueue.remove(0);
		}
	}
	
	public static void doCancel(UserCon c, String args)
	{
		if (args.length() == 0)
		{
			c.sendln("Cancel which beneficial effect?");
			return;
		}
		
		args = args.toLowerCase();
		for (Effect e : c.ch.effects)
			if (e.name.toLowerCase().startsWith(args))
			{
				for (String s : Flags.goodEffects)
					if (e.name.equals(s))
					{
						e.duration = 0;
						c.sendln("'"+Fmt.cap(e.name)+"' cancelled.");
						return;
					}
				c.sendln("'"+Fmt.cap(e.name)+"' is not marked as a beneficial effect, and cannot be cancelled.");
				return;
			}
		c.sendln("That effect was not found.");
	}
	
	public static void doSpar(UserCon c, String args)
	{
		if (args.length() == 0)
		{
			if (c.ch.sparring != null)
			{
				if (c.ch.sparring.sparring == c.ch)
					c.sendln("You're currently sparring with "+Fmt.seeName(c.ch, c.ch.sparring)+".");
				else
					c.sendln("You have challenged "+Fmt.seeName(c.ch, c.ch.sparring)+" {xto spar.");
				return;
			}

			boolean printed = false;
			for (UserCon cs : conns)
			{
				if (cs.ch.sparring == c.ch)
				{
					if (!printed)
					{
						c.sendln("{xThe following characters have challenged you to a sparring match.");
						c.sendln("Use }H'}hspar }H<}icharacter}H>' {xto accept a challenge.");
						printed = true;
					}
					c.sendln("    }m"+Fmt.seeNameGlobal(c.ch, cs.ch)+"{x");
				}
			}
			if (!printed)
			{
				c.sendln("{xNobody has challenged you to spar.");
				c.sendln("Use }H'}hspar }H<}icharacter}H>' {xto challenge someone else.");
			}
			return;
		}
		
		args = args.toLowerCase();
		
		if ("decline".startsWith(args))
		{
			if (c.ch.sparring != null)
			{
				c.sendln("You can't decline if you're already involved in a sparring match.");
				c.sendln("Try }H'}hspar cancel}H'{x.");
				return;
			}
			for (UserCon cs : conns)
				if (cs.ch.sparring == c.ch)
				{
					cs.sendln(Fmt.seeNameGlobal(cs.ch, c.ch)+" has declined your challenge to spar.");
					c.sendln("You have declined "+Fmt.seeName(c.ch, cs.ch)+"'s challenge to spar.");
					return;
				}
			c.sendln("Nobody has challenged you to spar.");
			return;
		}
		
		if ("cancel".startsWith(args) || "withdraw".startsWith(args))
		{
			if (c.ch.sparring != null)
			{
				if (c.ch.sparring.sparring == c.ch)
				{
					if (c.ch.fighting == c.ch.sparring || c.ch.sparring.fighting == c.ch)
					{
						c.sendln("You can't cancel the sparring match once it's begun.");
						return;
					}
					c.sendln("You have cancelled the sparring match.");
					c.ch.sparring.sendln(Fmt.seeNameGlobal(c.ch.sparring, c.ch)+" has withdrawn from the sparring match.");
					c.ch.sparring.sparring = null;
					c.ch.sparring = null;
					return;
				}
				c.ch.sparring.sendln(Fmt.seeNameGlobal(c.ch.sparring, c.ch)+" has withdrawn their sparring challenge.");
				c.ch.sparring = null;
				c.sendln("You have withdrawn your sparring challenge.");
				return;
			}
			c.sendln("You haven't challenged anyone to spar.");
			return;
		}
		
		CharData target = Combat.findChar(c.ch, null, args, true);

		if (target == null)
		{
			c.sendln("No character by that name was found.");
			return;
		}
		
		if (!Combat.canSeeGlobal(target, c.ch) || !Combat.canSeeGlobal(c.ch, target))
		{
			c.sendln("You can't spar with someone if they can't see you.");
			return;
		}
		
		if (c.ch.sparring != null)
		{
			if (c.ch.sparring == target)
			{
				c.sendln("You've already challenged "+Fmt.seeNameGlobal(c.ch, target)+" to spar.");
				return;
			}
			c.sendln("You've already challenged "+Fmt.seeNameGlobal(c.ch, c.ch.sparring)+" to spar.");
			c.sendln("You must use 'spar cancel' before you challenge someone else.");
			return;
		}
		
		if (target.conn.isDummy)
		{
			c.sendln("You can't spar with mobs.");
			return;
		}
		
		if (target.sparring != null)
		{
			if (target.sparring == c.ch)
			{
				target.sendln(Fmt.seeNameGlobal(target, c.ch)+" has accepted your sparring challenge.");
				c.sendln("You are now sparring with "+Fmt.seeNameGlobal(c.ch, target)+".");
				Fmt.actAround(c.ch, target, null, "A sparring match between $n and $N has begun.", target);
				if (c.ch.currentRoom != target.currentRoom)
					Fmt.actAround(target, c.ch, null, "A sparring match between $N and $n has begun.", c.ch);
				c.ch.sparring = target;
				return;
			}
			c.sendln(Fmt.seeNameGlobal(c.ch, target)+" is already sparring with someone else.");
			return;
		}
		
		c.ch.sparring = target;
		c.sendln("You have challenged "+Fmt.seeNameGlobal(c.ch, target)+" to a sparring match.");
		target.sendln(Fmt.seeNameGlobal(target, c.ch)+" has challenged you to a sparring match.");
		target.sendln("Use }H'}hspar "+Fmt.seeNameGlobal(target, c.ch)+"}H' {xto accept the challenge.");
	}
}