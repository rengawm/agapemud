package jw.core;
import java.util.*;

import static jw.core.MudMain.*;

/**
	The Combat class contains methods for character-level interaction, specifically
	during combat.
*/
public class Combat
{
	/**
	Find a character as {@code ch} whose name matches {@code args}.
	<p>
	If {@code global} is {@code false}, the method will only look for characters in
	{@code loc}. Otherwise, it will search globally. This method also supports the
	"<i>#.[keyword]</i>" syntax by skipping the first #-1 matches, if a number has
	been specified.
	
	@param ch The character who is looking for the target. This is checked for 
		things like visibility.
	@param loc The room to search from (targets here are found first).
	@param args The name to search for, optionally preceeded by <i>#.</i> to obtain
		the #th target match.
	@param global Whether a global search should be done. {@code true} conducts a global
		search, and {@code false} only searches the room occupied by {@code ch}.
	@return The character matched by the search, or {@code null} if none was found.
	*/
	public static CharData findChar(CharData ch, Room loc, String args, boolean global)
	{
		if (args.equalsIgnoreCase("self") && ch != null)
			return ch;
		
		// Search for a match by address/ID.
		if (args.toLowerCase().startsWith("jw.core.chardata@"))
			return Script.charByMemory(args);

		if (loc == null && ch != null)
			loc = ch.currentRoom;
		
		if (loc == null)
			return null;

		int targetCount = 1;
		boolean valid;

		args = args.toLowerCase().trim();
		String argSplit[] = args.split("\\.", 2);
		if (argSplit.length > 1)
			try
			{
				targetCount = Integer.parseInt(argSplit[0]);
				args = argSplit[1];
			}
			catch (Exception e)
			{
				targetCount = 1;
			}
		
		if (args.trim().length() == 0)
			return null;
		
		// Search for a player match.
		for (UserCon c : conns)
			if (global || c.ch.currentRoom == loc)
				if (c.ch.name.toLowerCase().startsWith(args) && (canSee(ch, c.ch) || (canSeeGlobal(ch, c.ch) && global)))
					if (targetCount == 1)
						return c.ch;
					else
						targetCount--;

		// Search for a mob in the current room.
		for (CharData cd : mobs)
		{
			valid = true;
			if (cd.currentRoom != loc)
				valid = false;
			else if (!canSee(ch, cd) && !(canSeeGlobal(ch, cd) && global))
				valid = false;
			else
				valid = cd.nameMatches(args);
			
			if (valid)
			{
				if (targetCount == 1)
					return cd;
				else
					targetCount--;
			}
		}
		
		// Search for mobs in other rooms if global is true.
		if (global)
			for (CharData cd : mobs)
			{
				valid = true;
				if (cd.currentRoom == loc)
					valid = false;
				else if (!canSeeGlobal(ch, cd))
					valid = false;
				else
					valid = cd.nameMatches(args);
				
				if (valid)
				{
					if (targetCount == 1)
						return cd;
					else
						targetCount--;
				}
			}

		return null;
	}
	
	public static boolean canSee(CharData ch, CharData target)
	{
		if (ch == null || target == null)
			return true;
		if (ch == target)
			return true;
		if (target.conn.invis || target.conn.incog && ch.currentRoom != target.currentRoom)
		{
			if (target.conn.hasPermission("staff"))
			{
				if (!ch.conn.hasPermission("staff"))
					return false;
				else
					return true;
			}
			if (target.conn.hasPermission("builder"))
			{
				if (!ch.conn.hasPermission("builder"))
					return false;
				else
					return true;
			}
		}
		if (ch.conn.hasPermission("staff") || ch.conn.hasPermission("builder"))
			return true;
		if (ch.currentRoom.isDark())
			return false;

		Effect targetInvis = Effect.findEffect(target.effects, "invisible");
		if (targetInvis != null)
		{
			Effect chDetectInvis = Effect.findEffect(ch.effects, "detect invisible");
			if (chDetectInvis != null)
				if (chDetectInvis.level >= targetInvis.level)
					return true;
			Effect chTrueSight = Effect.findEffect(ch.effects, "true sight");
			if (chTrueSight != null)
				if (chTrueSight.level >= targetInvis.level)
					return true;
			return false;
		}
		
		if (target.checkTrigger("vischeck", ch, null, "", 0) == 0)
			return false;

		return true;
	}
	
	public static boolean canSee(CharData ch, ObjData target)
	{
		if (ch == null || target == null)
			return true;
		if (ch.conn.hasPermission("staff") || ch.conn.hasPermission("builder"))
			return true;
		if (ch.currentRoom.isDark() && target.currentRoom != null)
			return false;

		Effect targetInvis = Effect.findEffect(target.effects, "invisible");
		if (targetInvis != null)
		{
			Effect chDetectInvis = Effect.findEffect(ch.effects, "detect invisible");
			if (chDetectInvis != null)
				if (chDetectInvis.level >= targetInvis.level)
					return true;
			Effect chTrueSight = Effect.findEffect(ch.effects, "true sight");
			if (chTrueSight != null)
				if (chTrueSight.level >= targetInvis.level)
					return true;
			return false;
		}

		if (target.checkTrigger("vischeck", ch, null, "", 0) == 0)
			return false;

		return true;
	}
	
	public static boolean canSeeGlobal(CharData ch, CharData target)
	{
		if (ch == null || target == null)
			return true;
		if (ch == target)
			return true;
		if (target.conn.invis || target.conn.incog && ch.currentRoom != target.currentRoom)
		{
			if (target.conn.hasPermission("staff"))
			{
				if (!ch.conn.hasPermission("staff"))
					return false;
				else
					return true;
			}
			if (target.conn.hasPermission("builder"))
			{
				if (!ch.conn.hasPermission("builder"))
					return false;
				else
					return true;
			}
		}
		if (ch.conn.hasPermission("staff") || ch.conn.hasPermission("builder"))
			return true;
		return true;
	}
	
	public static ObjData[] findObj(CharData ch, ArrayList<ObjData> search, String args)
	{
		int targetCount = 1;
		int targetMult = 1;
		ArrayList<ObjData> results = new ArrayList<ObjData>();

		args = args.toLowerCase().trim();
		
		// Search for a match by address/ID.
		if (args.startsWith("jw.core.objdata@"))
		{
			if (Script.objByMemory(args) != null)
				results.add(Script.objByMemory(args));
			return results.toArray(new ObjData[0]);
		}

		if (args.equals("all"))
			return search.toArray(new ObjData[0]);

		String argSplit[] = args.split("\\*", 2);
		if (argSplit.length > 1)
			try
			{
				targetMult = Integer.parseInt(argSplit[0]);
				args = argSplit[1];
			}
			catch (Exception e)
			{
				if (argSplit[0].equals("all"))
				{
					targetMult = search.size();
					args = argSplit[1];
				}
				else
					targetMult = 1;
			}

		argSplit = args.split("\\.", 2);
		if (argSplit.length > 1)
			try
			{
				targetCount = Integer.parseInt(argSplit[0]);
				args = argSplit[1];
			}
			catch (Exception e)
			{
				if (argSplit[0].equals("all"))
				{
					targetMult = search.size();
					args = argSplit[1];
				}
				targetCount = 1;
			}
		
		if (args.trim().length() == 0)
			return new ObjData[0];
		
		for (ObjData o : search)
			if (o.nameMatches(args))
				if (canSee(ch, o) || ch == null)
				{
					if (targetCount > 1)
					{
						targetCount--;
					}
					else
					{
						if (targetMult > 0)
								results.add(o);
						targetMult--;
					}
				}
		
		return results.toArray(new ObjData[0]);
	}

	/**
	Perform periodic combat updates for all characters who are currently fighting.
	<p>
	Players are updated first, followed by all mobs.
	*/
	public static void updateCombat()
	{
		for (UserCon c : conns)
			if (c.ch.fighting != null)
				combatRound(c.ch);
		for (int ctr = mobs.size()-1; ctr > 0; ctr--)
			if (mobs.get(ctr).fighting != null)
				combatRound(mobs.get(ctr));
		
		// Clear the "damagers" if this character is no longer in combat.
		for (CharData ch : allChars())
		{
			if (ch.damagers.size() == 0)
				continue;
			boolean inCombat = false;
			for (CharData chs : allChars())
				if (chs.fighting == ch)
				{
					inCombat = true;
					break;
				}
			if (!inCombat)
				ch.damagers.clear();
		}
	}
	
	public static void processQueue()
	{
		for (CharData ch : allChars())
		{
			if (ch.queueDelay > 0)
			{
				ch.queueDelay--;
				continue;
			}
			if (ch.combatQueue.size() > 0)
			{
				String temp = ch.combatQueue.get(0);
				ch.combatQueue.remove(0);
				
				if (temp.startsWith("dsp#"))
				{
					ch.queueDelay = (int)(Fmt.getInt(temp.substring(4))*Formulas.castSpeedMod(ch));
					double numSeconds = ch.queueDelay/10.0;
					if (targetClass(ch.targetQueue.get(0)).equals("character"))
					{
						CharData chTarget = (CharData)ch.targetQueue.get(0);
						if (chTarget == ch)
						{
							ch.sendln("You start casting '{W"+CommandHandler.getArg(ch.combatQueue.get(0), 1)+"{x' on yourself. ("+numSeconds+" sec)");
							Fmt.actAround(ch, chTarget, null, "$n starts casting '{W"+CommandHandler.getArg(ch.combatQueue.get(0), 1)+"{x' on $mself. ("+numSeconds+" sec)");
						}
						else if (chTarget.currentRoom == ch.currentRoom)
						{
							ch.sendln("You start casting '{W"+CommandHandler.getArg(ch.combatQueue.get(0), 1)+"{x' on "+Fmt.seeName(ch, chTarget)+". ("+numSeconds+" sec)");
							Fmt.actAround(ch, chTarget, null, "$n starts casting '{W"+CommandHandler.getArg(ch.combatQueue.get(0), 1)+"{x' on $N. ("+numSeconds+" sec)");
						}
						else
						{
							ch.sendln("You start casting '{W"+CommandHandler.getArg(ch.combatQueue.get(0), 1)+"{x.' ("+numSeconds+" to cast.)");
							Fmt.actAround(ch, null, null, "$n starts casting '{W"+CommandHandler.getArg(ch.combatQueue.get(0), 1)+"{x.' ("+numSeconds+" sec)");
						}
					}
					else if (targetClass(ch.targetQueue.get(0)).equals("object"))
					{
						ObjData oTarget = (ObjData)ch.targetQueue.get(0);
						ch.sendln("You start casting '{W"+CommandHandler.getArg(ch.combatQueue.get(0), 1)+"{x' on "+Fmt.seeName(ch, oTarget)+". ("+numSeconds+" sec)");
						Fmt.actAround(ch, null, oTarget, "$n starts casting '{W"+CommandHandler.getArg(ch.combatQueue.get(0), 1)+"{x' on $o. ("+numSeconds+" sec)");
					}
					else
					{
						ch.sendln("You start casting '{W"+CommandHandler.getArg(ch.combatQueue.get(0), 1)+"{x.' ("+numSeconds+" sec)");
						Fmt.actAround(ch, null, null, "$n starts casting '{W"+CommandHandler.getArg(ch.combatQueue.get(0), 1)+"{x.' ("+numSeconds+" sec)");
					}
					continue;
				}
				if (temp.startsWith("dsk#"))
				{
					ch.queueDelay = Fmt.getInt(temp.substring(4));
					continue;
				}
				
				String skName = CommandHandler.getArg(temp, 1);
				String skArgs = CommandHandler.getLastArg(temp, 2);
				Object skTarget = ch.targetQueue.get(0);
				ch.targetQueue.remove(0);
				boolean matched = false;
				for (Skill s : skills)
					if (s.name.equals(skName))
					{
						matched = true;
						boolean invalid = false;
						if (targetClass(skTarget).equals("character"))
						{
							CharData chTarget = (CharData)skTarget;
							boolean stillExists = false;
							for (CharData chs : allChars())
								if (chs == chTarget)
								{
									stillExists = true;
									break;
								}
							if (!stillExists)
								invalid = true;
							if (s.flags.get("global_character"))
							{
								if (!canSeeGlobal(ch, chTarget))
									invalid = true;
							}
							else if (s.flags.get("character"))
							{
								if (!canSee(ch, chTarget) || ch.currentRoom != chTarget.currentRoom)
									invalid = true;
							}
						}
						else if (targetClass(skTarget).equals("object"))
						{
							boolean stillExists = false;
							for (ObjData o : ObjData.allVisibleObjects(ch))
								if (o == skTarget)
								{
									stillExists = true;
									break;
								}
							if (!stillExists)
								invalid = true;
						}
						if (invalid)
						{
							ch.sendln("Your target has moved or is no longer visible.");
							if (s.type.equals("skill"))
								ch.combatQueue.remove(0);
							break;
						}
						s.run(ch.conn, skArgs, skTarget);
						break;
					}
				if (!matched)
					sysLog("bugs", "Combat queue error: "+ch.shortName+" had "+skName+", an unmatched skill.");
			}
		}
	}
	
	public static Object getTarget(CharData ch, Skill sk, String args)
	{
		if (sk.flags.get("character"))
		{
			if (sk.targetType.equals("self"))
				return (Object) ch;
			if (args.length() == 0)
				if (sk.targetType.equals("defensive"))
					return (Object) ch;
				else if (sk.targetType.equals("offensive"))
					return (Object) ch.fighting;

			CharData chTarget = findChar(ch, null, args, false);
			if (chTarget != null)
				return chTarget;
		}
		
		if (sk.flags.get("global_character"))
		{
			if (sk.targetType.equals("self"))
				return (Object) ch;
			if (args.length() == 0)
				if (sk.targetType.equals("self") || sk.targetType.equals("defensive"))
					return (Object) ch;
				else if (sk.targetType.equals("offensive"))
					return (Object) ch.fighting;

			CharData chTarget = findChar(ch, null, args, true);
			if (chTarget != null)
				return chTarget;
		}
		
		if (sk.flags.get("object"))
		{
			ObjData oTarget[] = findObj(ch, ObjData.allVisibleObjects(ch), args);
			if (oTarget.length > 0)
				return (Object)oTarget[0];
		}

		return null;
	}
	
	public static Boolean checkTarget(CharData ch, Skill sk, String args)
	{
		if (sk.targetType.equals("targetless"))
		{
			ch.targetQueue.add(null);
			return true;
		}
		
		Object skTarget = getTarget(ch, sk, args);
		if (skTarget == null && !sk.targetType.equals("none"))
		{
			if (args.length() == 0)
				ch.sendln("Use that "+sk.type+" on whom/what?");
			else
				ch.sendln("That target was not found.");
			if (ch.combatQueue.size() > 0)
				if (ch.combatQueue.get(0).startsWith("dsk#"))
					ch.combatQueue.remove(0);
			return false;
		}	

		if (targetClass(skTarget).equals("character"))
			if (sk.targetType.equals("offensive"))
				if (!fightCheck(ch, (CharData) skTarget))
					return false;

		ch.targetQueue.add(skTarget);
		return true;
	}
	
	public static String targetClass(Object target)
	{
		if (target == null)
			return "none";
		if (target.getClass() == ObjData.class)
			return "object";
		if (target.getClass() == CharData.class)
			return "character";
		sysLog("bugs", "Error in targetClass: Unanticipated target class ("+target.getClass().getName()+")");
		return "none";
	}
	
	public static void checkPursuits()
	{
		for (CharData ch : mobs)
			if (ch.flags.get("pursues") && ch.fighting == null && ch.position.equals("standing"))
				if (ch.hating.size() > 0)
				{
					CharData target = ch.hating.get(ch.hating.size()-1);
					if (Combat.canSeeGlobal(ch, target))
					{
						String dir = Room.pathTo(ch.currentRoom, target.currentRoom, true, true);
						if (dir.length() > 0)
							ch.mobCommand(dir);
					}
				}
	}
	
	public static void checkAutoAttacks(UserCon c)
	{
		if (c.ch.fighting != null)
			return;
		for (CharData ch : allChars())
		{
			if (ch.currentRoom == c.ch.currentRoom && ch != c.ch)
			{
				// Look for mobs that should attack this character.
				if (ch.fighting == null && Combat.canSee(ch, c.ch))
				{
					if ((ch.flags.get("attackall") && !c.isDummy) || ch.hating.contains(c.ch))
					{
						if (ch.checkTrigger("encounter", c.ch, null, "", 0) == 0)
						{
							ch.hating.remove(c.ch);
							continue;
						}
						c.sendln(Fmt.cap(Fmt.seeName(c.ch, ch))+" rushes toward you and attacks!");
						ch.fighting = c.ch;
						if (c.ch.fighting == null)
							c.ch.fighting = ch;
					}
				}
				// If this is an NPC, see if it should attack anyone.
				if (c.isDummy && Combat.canSee(c.ch, ch))
				{
					if ((c.ch.flags.get("attackall") && !ch.conn.isDummy) || c.ch.hating.contains(ch))
					{
						if (c.ch.checkTrigger("encounter", ch, null, "", 0) == 0)
						{
							c.ch.hating.remove(ch);
							continue;
						}
						if (!ch.position.equals("sleeping"))
							ch.sendln(Fmt.cap(Fmt.seeName(ch, c.ch))+" rushes toward you and attacks!");
						c.ch.fighting = ch;
						if (ch.fighting == null)
							ch.fighting = c.ch;
						return;
					}
				}
			}
		}
	}
	
	/**
	Perform a single combat round from one character.
	<p>
	The target is automatically pulled from the character's {@code fighting} variable.
	This method also exits combat if the target has somehow disappeared.
	
	@param ch The character to perform combat updates for.
	*/
	public static void combatRound(CharData ch)
	{
		if (ch.currentRoom != ch.fighting.currentRoom)
		{
			ch.fighting = null;
			return;
		}
		
		for (Group g : groups)
			if (g.members.contains(ch))
				for (CharData chs : g.members)
					if (chs != ch && chs.currentRoom == ch.currentRoom && chs.fighting == null)
						if (chs.conn.prefs.get("autoassist"))
							if (Combat.canSee(chs, ch.fighting))
							{
								chs.sendln("You assist "+Fmt.seeName(chs, ch)+" in combat!");
								ch.sendln(Fmt.cap(Fmt.seeName(ch, chs))+" assists you in combat!");
								chs.fighting = ch.fighting;
							}
		
		double apr = Formulas.attacksPerRound(ch);
		ch.attacksAccrued += apr;
		while (ch.attacksAccrued >= 1)
		{
			oneHit(ch);
			ch.attacksAccrued -= 1.0;
		}
		
		for (CharData chs : allChars())
			if (chs.currentRoom == ch.currentRoom)
				chs.checkTrigger("combat", ch, ch.fighting, "", 0);
	}
	
	/**
	Perform a single hit from one character on its current target.
	<p>
	The target is automatically pulled from the character's {@code fighting} variable.
	
	@param ch The character performing the hit.
	*/
	public static void oneHit(CharData ch)
	{
		if (ch.fighting == null)
			return;

		ObjData tempWeap = ch.getWearloc("wield1");
		int damage = ch.level;
		if (tempWeap != null)
		{
			Formulas.meleeDamage(ch, ch.fighting, tempWeap.value4, 0, tempWeap, "");

			ObjData tempWeap2 = ch.getWearloc("wield2");
			if (tempWeap2 != null && ch.fighting != null)
				Formulas.meleeDamage(ch, ch.fighting, tempWeap2.value4, 0, tempWeap2, "");
		}
		else
		{
			String hitname = "punch";
			if (ch.hitname.length() > 0)
				hitname = ch.hitname;
			else if (ch.charRace.hitname.length() > 0)
				hitname = ch.charRace.hitname;
			
			Formulas.meleeDamage(ch, ch.fighting, hitname, 0, Flags.hands, "");
		}
	}
	
	/**
	Character {@code ch} does the specified {@code amount} of damage to {@code victom}.
	<p>
	This method shows output for all involved, removes {@code amount} hp from {@code
	victim}, and then checks to see if this hit has killed {@code victim}. If it has,
	deal with any necessary death actions.
	
	@param ch The character causing the damage.
	@param victim The character being inflicted with the damage.
	@param amount The amount of damage, in hitpoints, to apply.
	*/
	public static void damage(CharData ch, CharData victim, int amount, String hitName)
	{
		if (amount > -1)
			if (!fightCheck(ch, victim))
				return;

		if (amount == 0)
			return;
		
		if (hitName.length() == 0)
			hitName = "hit";

		if (amount > 0)
		{
			if (!victim.position.equals("standing"))
				victim.position = "standing";

			if (victim.fighting == null)
				if (ch != victim)
					victim.fighting = ch;
				
			if (ch.fighting == null)
				if (ch != victim)
					ch.fighting = victim;
			
			if (!victim.hating.contains(ch) && victim.conn.isDummy)
				victim.hating.add(ch);
				
			if (ch.currentRoom != victim.currentRoom)
			{
				ch.fighting = null;
				victim.fighting = null;
			}
				
			if (victim.damagers.get(ch) == null)
				victim.damagers.put(ch, amount);
			else
				victim.damagers.put(ch, amount+victim.damagers.get(ch));
			
			for (Effect e : victim.effects)
			{
				if (e.name.equals("holy shield"))
				{
					int absLeft = e.statMods.get("damage absorbed");
					int absorbed = 0;
					if (absLeft > amount)
					{
						absorbed = amount;
						e.statMods.put("damage absorbed", (absLeft-absorbed));
						amount = 0;
					}
					else
					{
						absorbed = absLeft;
						victim.effects.remove(e);
						amount -= absLeft;
					}

					ch.sendln("{2Your "+hitName+" is absorbed by {2"+Fmt.seeName(ch, victim)+"'s {2holy shield. {W[ {2"+absorbed+" {W]{x");
					if (ch != victim)
						victim.sendln("{4"+Fmt.cap(Fmt.seeName(victim, ch))+"'s {4"+hitName+" is absorbed by your holy shield. {W[ {4"+absorbed+" {W]{x");
					
					if (absLeft <= amount)
						victim.sendln("Your holy shield has been exhausted.");
					
					if (amount == 0)
						return;
					break;
				}
				else if (e.name.equals("judgement"))
				{
					int reflectDamage =  (amount / 10) + (e.level/10);
					ch.sendln("{2Your "+hitName+" reacts to {2"+Fmt.seeName(ch, victim)+"'s {2judgement, dealing {W[ {2"+reflectDamage+" {W] {2to you.{x");
					if (ch != victim)
						victim.sendln("{4"+Fmt.cap(Fmt.seeName(victim, ch))+"'s {4"+hitName+" is partially reflected, doing {W[ {4"+reflectDamage+
							" {W] {4damage back to them.{x");
					ch.hp -= reflectDamage;
					checkForDeath(ch, victim);
					amount -= reflectDamage;
				}
				else if (e.name.equals("beacon of light"))
				{
					amount -= ((4*amount)/10) + (e.level/10);
					ch.sendln("{2Your attack is diminished by "+Fmt.seeName(ch, victim)+"'s {2beacon of light.");
				}
				else if (e.name.equals("desperate_prayer"))
				{
					amount = 0;
					ch.sendln(Fmt.seeName(ch, victim)+"'s {2desperate prayer voids your attack.");
					victim.sendln("You are protected by your desperate prayer");
				}
				else if (e.name.equals("empathy"))
				{
					int reflectDamage =  (amount / 10) + (e.level/10);
					ch.sendln("{2Your "+hitName+" reacts to {2"+Fmt.seeName(ch, victim)+"'s {2jempathy, dealing {W[ {2"+reflectDamage+" {W] {2to " +
						Fmt.seeName(ch, e.creator)+"{2.{x");
					if (ch != victim)
						victim.sendln("{4"+Fmt.cap(Fmt.seeName(victim, ch))+"'s {4"+hitName+" is partially reflected, doing damage back to "
							+Fmt.seeName(ch, e.creator)+" {W[ {4"+reflectDamage+" {W]{2.{x");
					e.creator.hp -= reflectDamage;
					e.creator.conn.sendln("{2Your empathy redirects damage from "+ Fmt.seeName(e.creator, victim)+" and deals it to you. " + 
						"{W[ {4"+reflectDamage+" {W]{2.{x");
					checkForDeath(ch, victim);
					amount -= reflectDamage;
				}
			}
		}

		String dmgName = "";
		if (amount < 0)			dmgName = "{Cheals";
		else if (amount == 0)	dmgName = "{wmisses";
		else if (amount < 10)	dmgName = "{gscratches";
		else if (amount < 20)	dmgName = "{Ggrazes";
		else if (amount < 30)	dmgName = "{bhits";
		else if (amount < 40)	dmgName = "{Bhurts";
		else if (amount < 50)	dmgName = "{yinjures";
		else if (amount < 60)	dmgName = "{Ywounds";
		else if (amount < 70)	dmgName = "{cmauls";
		else if (amount < 80)	dmgName = "{Cdecimates";
		else if (amount < 90)	dmgName = "{Wdevastates";
		else if (amount < 100)	dmgName = "{mmaims";
		else if (amount < 120)	dmgName = "{Mlacerates";
		else if (amount < 140)	dmgName = "{rmutilates";
		else if (amount < 160)	dmgName = "{Rmangles";
		else if (amount < 180)	dmgName = "{MM{mA{MS{mS{MA{mC{MR{mE{MS";
		else if (amount < 200)	dmgName = "{RD{rE{RM{rO{RL{rI{RS{rH{RE{rS";
		else if (amount < 220)	dmgName = "{CD{cE{CV{cA{CS{cT{CA{cT{CE{cS";
		else if (amount < 250)	dmgName = "{D== {rOBLITERATES {D==";
		else if (amount < 280)	dmgName = "{Y>> {GANNIHILATES {Y<<";
		else if (amount < 320)	dmgName = "{C<< {WERADICATES {C>>";
		else if (amount < 360)	dmgName = "{r*** {YEVISCERATES {r***";
		else if (amount < 400)	dmgName = "{M*** {WSLAUGHTERS {M***";
		else if (amount < 450)	dmgName = "{W({C={B- {DDESTROYS {B-{C={W)";
		else 					dmgName = "{R={Y][{R= {rNULLIFIES {R={Y][{R=";
		victim.hp -= amount;
		
		String cc = "{4";
		String cc2 = "{2";
		String cc3 = "{3";
		if (amount < 0)
		{
			if (victim.hp > victim.maxHp())
				victim.hp = victim.maxHp();
			cc = "{W";
			cc2 = "{W";
			cc3 = "{c";
			amount = amount*(-1);
		}
		
		if (ch != victim)
		{
			ch.sendln(cc2+"Your "+hitName+" "+dmgName+" "+cc2+Fmt.seeName(ch, victim)+cc2+". {W[ "+cc2+amount+" {W]{x");
			victim.sendln(cc+Fmt.cap(Fmt.seeName(victim, ch))+"'s "+cc+hitName+" "+dmgName+cc+" you. {W[ "+cc+amount+" {W]{x");
		}
		else
		{
			ch.sendln(cc2+"Your "+hitName+" "+dmgName+ cc2+" you. " + "{W[ "+cc2+amount+" {W]{x");
		}


		for (UserCon c : conns)
			if (c.ch.currentRoom == ch.currentRoom && c.ch != ch && c.ch != victim)
				if (!c.ch.position.equals("sleeping"))
					c.sendln(cc3+Fmt.cap(Fmt.seeName(c.ch, ch))+"'s"+cc3+" "+hitName+" "+dmgName+cc3+" "+Fmt.seeName(c.ch, victim)+cc3+".{x");
		
		victim.checkTrigger("hitpercent", ch, null, "", (ch.hp*100)/ch.maxHp());
		checkForDeath(ch, victim);
	}
	
	public static boolean fightCheck(CharData ch, CharData victim)
	{
		if (ch == victim)
			return true;

		if (ch.sparring != null && victim != ch.sparring)
		{
			ch.sendln("You can't attack "+Fmt.seeName(ch, victim)+" while sparring.");
			return false;
		}
		if (victim.sparring != null && victim.sparring != ch)
		{
			victim.sendln("Your sparring status has been cleared by combat with someone else.");
			victim.sparring.sendln(Fmt.seeNameGlobal(victim.sparring, victim)+" has entered combat with someone else, and the sparring match has been cancelled.");
			if (victim.sparring.sparring == victim)
				victim.sparring.sparring = null;
			victim.sparring = null;
		}

		if (ch.conn.isDummy || victim.conn.isDummy)
			return true;
		
		if (ch.sparring == victim && victim.sparring == ch)
			return true;
		
		if (!victim.conn.prefs.get("pvp"))
		{
			ch.sendln("You cannot attack a player who doesn't have PVP enabled.");
			victim.sendln(Fmt.seeName(victim, ch) + " just tried to attack you, but you don't have PVP enabled.");

			if (ch.fighting == victim)
				ch.fighting = null;
			if (victim.fighting == ch)
				victim.fighting = null;

			return false;
		}

		if (!ch.conn.prefs.get("pvp"))
		{
			ch.conn.prefs.put("pvp", true);
			ch.sendln("You will now be flagged for PVP");
		}

		Effect e = new Effect("pvp lock", 100, 3000);
		Effect.addEffect(ch.effects, e, false);
		Effect.addEffect(victim.effects, e, false);
		return true;
	}
	
	public static void checkForDeath(CharData ch, CharData victim)
	{
		// Is the victim dead after this hit?
		if (victim.hp < 1)
		{
			// Check for sparring matches.
			if (ch.sparring == victim && victim.sparring == ch)
			{
				ch.sendln("You have won the sparring match!");
				victim.sendln("You have lost the sparring match!");
				Fmt.actAround(ch, victim, null, "$n has defeated $N in a sparring match!", victim);
				ch.sparring = null;
				victim.sparring = null;
				ch.fighting = null;
				victim.fighting = null;
				ch.restore();
				victim.restore();
				
				ch.clearQueueOf(victim);
				victim.clearQueueOf(ch);
				return;
			}
			
			// If any attackers are also fighting other mobs, switch their "fighting" to one of those.
			for (CharData cha : allChars())
			{
				if (cha.fighting == victim)
				{
					cha.fighting = null;
					for (CharData chs : allChars())
						if (chs.fighting == cha && chs != victim)
							cha.fighting = chs;
				}
				if (cha.hating.contains(victim))
					cha.hating.remove(victim);
				cha.clearQueueOf(victim);
			}

			// Clear fighting status.
			victim.fighting = null;

			if (victim.checkTrigger("death", ch, null, "", 0) == 0)
			{
				if (victim.hp < 1)
					victim.hp = 1;
				return;
			}
			if (ch.checkTrigger("kill", victim, null, "", 0) == 0)
			{
				if (victim.hp < 1)
					victim.hp = 1;
				return;
			}

			for (UserCon c : conns)
				if (c.ch.currentRoom == victim.currentRoom && c.ch != victim)
					if (!c.ch.position.equals("sleeping"))
						c.sendln("{1"+Fmt.cap(Fmt.seeName(c.ch, victim))+" has died!{x");
			
			// Try to remove this CharData from mobs. If it wasn't there, this was a PC.
			if (!mobs.remove(victim))
			{
				victim.sendln("{1You have died!{x");
				victim.hp = 1;
				if (Room.lookup(Flags.infirmaryId) == null)
				{
					sysLog("bugs", "No infirmary room at id "+Flags.infirmaryId+".");
				}
				else
				{
					if (ObjProto.lookup(Flags.corpseId) == null)
					{
						sysLog("bugs", "No corpse object at id "+Flags.corpseId+".");
					}
					else if (victim.level < Flags.easyDeathLevel)
					{
						victim.sendln("You have been transported to the infirmary with all of your items.");
					}
					else
					{
						victim.sendln("You have been transported to the infirmary.");
						victim.sendln("Your items are still where you were fighting.");

						ObjData corpse = new ObjData(ObjProto.lookup(Flags.corpseId));
						corpse.flags.put("notake", false);
						corpse.name = "pile equipment "+victim.name.toLowerCase();
						corpse.shortName = "a pile of "+victim.shortName+"'s equipment";
						corpse.longName = "A pile of "+victim.shortName+"'s equipment is here, strewn about the room.";
						corpse.decay = 604800;
						int dumpGold = victim.gold/2;
						int remGold = victim.gold-dumpGold;
						if (dumpGold > 0)
							ObjData.makeCoins(dumpGold).toObject(corpse);
						victim.gold = remGold;
						while (victim.objects.size() > 0)
							victim.objects.get(0).toObject(corpse);
						if (corpse.objects.size() > 0)
							corpse.toRoom(victim.currentRoom);
					}
					
					if (victim.level < Flags.maxPlayableLevel && ch.conn.isDummy)
					{
						int orig = victim.tnl;
						victim.tnl += Formulas.tnl(victim)/4;
						if (victim.tnl > Formulas.tnl(victim))
							victim.tnl = Formulas.tnl(victim);
						int loss = victim.tnl-orig;
						if (loss > 0)
							victim.sendln("{wYou have lost {W"+loss+"{w experience points!{x");
					}
						
					victim.currentRoom = Room.lookup(Flags.infirmaryId);
				}
			}
			else
			{
				victim.conn.cleanup();
				if (ObjProto.lookup(Flags.corpseId) == null)
				{
					sysLog("bugs", "No corpse object at id "+Flags.corpseId+".");
				}
				else
				{
					ObjData corpse = new ObjData(ObjProto.lookup(Flags.corpseId));
					corpse.name = corpse.name+" "+victim.name;
					corpse.shortName = "the corpse of "+victim.shortName;
					corpse.longName = "The corpse of "+victim.shortName+" is here, rotting away.";
					corpse.decay = 300;
					if (victim.gold > 0)
					{
						if (ch.conn.prefs.get("autoloot"))
						{
							int hereCount = 0;
							for (Group g : groups)
								if (g.members.contains(ch))
								{
									for (CharData chs : g.members)
										if (ch.currentRoom == chs.currentRoom)
											hereCount++;

									if (hereCount == 1)
									{
										hereCount = 0;
										break;
									}

									int splitAmt = victim.gold;
									if (hereCount > 1)
										splitAmt = splitAmt/hereCount;

									for (CharData chs : g.members)
										if (ch.currentRoom == chs.currentRoom)
										{
											chs.sendln("Your split of the loot is "+splitAmt+" gold coins.");
											chs.gold += splitAmt;
										}
									break;
								}
							if (hereCount == 0)
							{
								ch.sendln("You loot "+victim.gold+" gold coins.");
								ch.gold += victim.gold;
							}
						}
						else
							ObjData.makeCoins(victim.gold).toObject(corpse);
					}
					
					if (ch.conn.prefs.get("autoloot"))
					{
						ArrayList<ObjData> validTargets = new ArrayList<ObjData>();
						String fullString = "";
						while (victim.objects.size() > 0)
						{
							ObjData o = victim.objects.get(0);
							ch.objects.add(o);
							double currentFill = ObjData.capCount(ch.objects.toArray(new ObjData[0]));
							if (Flags.inventoryCapacity*20 < Math.round(currentFill*20) && !ch.conn.hasPermission("staff"))
							{
								fullString = "Your inventory is too full to hold "+Fmt.seeName(ch, o)+".";
								ch.objects.remove(o);
								break;
							}
							ch.objects.remove(o);
							o.toChar(ch);
							validTargets.add(o);
						}
						
						if (validTargets.size() > 0)
						{
							String fromString = " from the corpse of "+victim.shortName;
							for (UserCon cs : conns)
								if (cs.ch.currentRoom == ch.currentRoom)
									if (!cs.ch.position.equals("sleeping"))
									{
										String dropList[] = ObjData.listContents(cs.ch, validTargets.toArray(new ObjData[0]), false);
										for (String s : dropList)
										{
											s = s.trim();
											if (cs == ch.conn)
												cs.sendln("You get "+s+fromString+".");
											else
												cs.sendln(Fmt.seeName(cs.ch, ch)+" gets "+s+fromString+".");
										}
									}
						}
						
						if (fullString.length() > 0)
							ch.sendln(fullString);
					}
			
					while (victim.objects.size() > 0)
						if (!victim.objects.get(0).type.equals("training"))
							victim.objects.get(0).toObject(corpse);
						else
							victim.objects.get(0).clearObjects();
					if (corpse.objects.size() > 0)
						corpse.toRoom(victim.currentRoom);
				}
				if (victim.resetFilled > 0)
					updates.add(new Update(360, "mobReset", victim.resetFilled));
			}
			Formulas.distribExp(victim);
		}
	}
}