package jw.core;

import java.util.*;
import static jw.core.MudMain.*;

/**
	The Formulas class contains all formulas for generating stats, xp gains, mob hp, and
	other game numbers.
*/
public class Formulas
{
	public static int hpGain(int level, int con)
	{
		double gain = (con/100.0)*1.4+0.6;
		gain = gain*(level);
		if (gain < 5)
			return 5;
		return (int)gain;
	}

	public static int manaGain(int level, int charInt)
	{
		double gain = (charInt/100.0)*1.4+0.6;
		gain = gain*(level);
		if (gain < 5)
			return 5;
		return (int)gain;
	}
	
	/*
		Calculate the xp value of victim and distribute it to everyone involved in
		killing the target.
	*/
	public static void distribExp(CharData victim)
	{
		if (victim.damagers.size() == 0)
			return;
		
		CharData maxLevel = null;
		
		int totalLevels = 0;

		ArrayList<CharData> tempDamagers = new ArrayList<CharData>();
		for (CharData ch : victim.damagers.keySet())
			tempDamagers.add(ch);
		
		for (CharData ch : tempDamagers)
		{
			if (maxLevel == null)
				maxLevel = ch;

			// Figure out the max level of anyone who did damage.
			if (ch.level > maxLevel.level)
				maxLevel = ch;
			totalLevels += ch.level;
			
			// Add any group members here but not doing damage.
			for (Group g : groups)
				if (g.members.contains(ch))
					for (CharData gch : g.members)
						if (gch.currentRoom == victim.currentRoom
							&& victim.damagers.get(gch) == null)
						{
							victim.damagers.put(gch, 1);
							totalLevels += gch.level;
							if (gch.level > maxLevel.level)
								maxLevel = gch;
						}
		}
		
		int xpForMax = expGain(maxLevel, victim);
		switch (victim.damagers.size())
		{
			case 1:
				break;
			case 2: xpForMax += xpForMax*0.25;
				break;
			case 3: xpForMax += xpForMax*0.50;
				break;
			case 4: xpForMax += xpForMax*0.75;
				break;
			case 5: xpForMax += xpForMax;
				break;
			case 6: xpForMax += xpForMax*1.25;
				break;
			case 7: xpForMax += xpForMax*1.50;
				break;
			case 8: xpForMax += xpForMax*1.75;
				break;
			default: xpForMax += xpForMax*2;
		}
		
		if (xpForMax > 0)
			for (CharData ch : victim.damagers.keySet())
				ch.awardExp((int)(xpForMax*(ch.level/(totalLevels*1.0))));
		else
			for (CharData ch : victim.damagers.keySet())
				if (ch.level < maxLevel.level)
					ch.awardExp((int)(expGain(ch, victim)/20.0));
	}
	
	/* Calculate the full xp value of a victim to a character. */
	public static int expGain(CharData ch, CharData victim)
	{
		if (ch == victim || !victim.conn.isDummy)
			return 0;
		
		// Start with the base mob xp for the victim's level.
		double xpGain = mxp(victim.level);
		
		// Compute the xp multiplier based on level.
		int levelDiff = victim.level-ch.level;
		
		if (levelDiff > 0)
		{
			if (levelDiff > 8)
				levelDiff = 8;
			xpGain += (int)(xpGain*levelDiff*0.075);
		}
		else if (levelDiff < 0)
		{
			int zeroDiff = 5+ch.level/10;
			xpGain += (int)(xpGain*(levelDiff/(zeroDiff*1.0)));
		}
		
		// Compute the xp multiplier based on mob difficulty.
		if (victim.difficulty == 2)
			xpGain += xpGain/2;
		else if (victim.difficulty == 3)
			xpGain += xpGain;
		else if (victim.difficulty == 4)
			xpGain += xpGain*3;
		else if (victim.difficulty == 5)
			xpGain += xpGain*9;
		
		// Toss in a little randomness for variety.
		if (xpGain > 10)
			xpGain = gen.nextInt((int)(xpGain/4))+xpGain*7/8;
		
		if (xpGain < 1)
			return 0;

		return (int)Math.round(xpGain);
	}
	
	public static int tnl(CharData ch)
	{
		return mpl(ch.level)*mxp(ch.level);
	}
	
	// The base XP value of a mob at a given level.
	public static int mxp(int level)
	{
		return 50+10*level;
	}
	
	// The number of mobs needed to level at a given level.
	public static int mpl(int level)
	{
		int count = 3*level+2;
		if (level < 25)
			return count;
		return count+(level-25)*2;
	}
	
	public static boolean checkMeleeHit(CharData ch, CharData victim, String hitName, ObjData using, String skUsing)
	{
		if (!Combat.fightCheck(ch, victim))
			return false;
		
		// Start with ch's dexterity.
		int toHit = ch.maxDex();

		// Look up the weapon skill in use and add the appropriate number.
		String checkSkill = "";
		boolean skipGain = false;
		if (Skill.lookup(skUsing) != null)
		{
			checkSkill = skUsing;
			skipGain = Skill.lookup(skUsing).flags.get("passive");
		}
		else if (using == Flags.hands)
			checkSkill = "hand to hand";
		else if (using.value1.equals("sword"))
			checkSkill = "swords";
		else if (using.value1.equals("heavy_sword"))
			checkSkill = "heavy swords";
		else if (using.value1.equals("dagger"))
			checkSkill = "daggers";
		else if (using.value1.equals("mace"))
			checkSkill = "maces";
		else if (using.value1.equals("heavy_mace"))
			checkSkill = "heavy maces";
		else if (using.value1.equals("flail"))
			checkSkill = "flails";
		else if (using.value1.equals("axe"))
			checkSkill = "axes";
		else if (using.value1.equals("heavy_axe"))
			checkSkill = "heavy axes";
		else if (using.value1.equals("polearm"))
			checkSkill = "polearms";
		else if (using.value1.equals("whip"))
			checkSkill = "whips";
		else if (using.value1.equals("staff"))
			checkSkill = "staves";
		else if (using.value1.equals("hand_weapon"))
			checkSkill = "hand to hand";
		
		if (ch.skillPercent(Skill.lookup(checkSkill)) > 0)
			toHit += ch.skillPercent(Skill.lookup(checkSkill))/5;
		
		// Penalty for dual wield, more so if they don't know dual wield well.
		if (using != null)
			if (using.wearloc.equals("wield2"))
				toHit -= 110-ch.skillPercent(Skill.lookup("dual wield"));

		// Add a 5% miss chance and modify it by level.
		int deltaLevel = victim.level-ch.level;
		
		if (deltaLevel < -3)
			toHit -= (int)(4.4+(deltaLevel+3)*2);
		else if (deltaLevel <= 3)
			toHit -= (int)(5+deltaLevel*0.2);
		else
			toHit -= (int)(5.6+(deltaLevel-3)*2);
		
		/* += hitroll, which comes from spells/equip. */
		toHit += ch.getStatMod("meleeaccuracy")*100/(ch.level*5);

		/* Add assorted bonuses and penalties here - darkness, blindness, etc. */
		
		// If the target is not also fighting ch, add 15% bonus.
		if (victim.fighting != ch)
			toHit += 15;

		if (toHit < 3)
			toHit = 3;
		
		if (gen.nextInt(100)+1 <= toHit)
		{
			if (!skipGain)
			{
				if (ch.skillPercent(Skill.lookup(checkSkill)) > 0)
					Skill.lookup(checkSkill).checkGain(ch);
				if (using != null)
					if (using.wearloc.equals("wield2"))
						Skill.lookup("dual wield").checkGain(ch);
			}
			return true;
		}

		ch.sendln("{2Your "+hitName+" {wmisses {2"+Fmt.seeName(ch, victim)+"{2.{x");
		if (ch != victim)
			victim.sendln("{4"+Fmt.cap(Fmt.seeName(victim, ch))+"'s {4"+hitName+" {wmisses {4you.{x");
		
		if (!skipGain)
		{
			if (ch.skillPercent(Skill.lookup(checkSkill)) > 0)
				Skill.lookup(checkSkill).checkFailure(ch);
			if (using != null)
				if (using.wearloc.equals("wield2"))
					Skill.lookup("dual wield").checkFailure(ch);
		}
		
		return false;
	}
	
	public static boolean checkDodge(CharData ch, CharData victim, String hitName)
	{
		int dodgePct = victim.skillPercent(Skill.lookup("dodge"));
		if (dodgePct == 0)
			return false;
		
		int dodgeChance = dodgePct/10;
		// Add bonus/penalty from effects/gear.
		
		if (gen.nextInt(100)+1 > dodgeChance)
		{
			Skill.lookup("dodge").checkFailure(victim);
			return false;
		}
		
		ch.sendln("{4"+Fmt.cap(Fmt.seeName(ch, victim))+"{4 {Wdodges {4your "+hitName+"{4!{x");
		if (ch != victim)
			victim.sendln("{2You {Wdodge {2"+Fmt.seeName(victim, ch)+"'s {2"+hitName+"{2!{x");
		Skill.lookup("dodge").checkGain(victim);
		return true;
	}

	public static boolean checkParry(CharData ch, CharData victim, String hitName)
	{
		int parryPct = victim.skillPercent(Skill.lookup("parry"));
		if (parryPct == 0)
			return false;
		
		if (victim.getWearloc("wield1") == null)
			return false;
			
		int parryChance = parryPct/10;
		// Add bonus/penalty from effects/gear.
		
		if (gen.nextInt(100)+1 > parryChance)
		{
			Skill.lookup("parry").checkFailure(victim);
			return false;
		}
		
		ch.sendln("{4"+Fmt.cap(Fmt.seeName(ch, victim))+"{4 {Wparries {4your "+hitName+"{4!{x");
		if (ch != victim)
			victim.sendln("{2You {Wparry {2"+Fmt.seeName(victim, ch)+"'s {2"+hitName+"{2!{x");
		Skill.lookup("parry").checkGain(victim);
		return true;
	}
	
	public static void meleeDamage(CharData ch, CharData victim, String hitName, int base, ObjData using, String skUsing)
	{
		if (hitName.length() == 0)
			hitName = "hit";

		if (!checkMeleeHit(ch, victim, hitName, using, skUsing))
			return;
		if (checkDodge(ch, victim, hitName))
			return;
		if (checkParry(ch, victim, hitName))
			return;

		int dmg = base;
		if (using != null)
		{
			if (using == Flags.hands)
			{
				dmg += ch.level+10;
				if (ch.skillPercent(Skill.lookup("hand to hand")) > 0)
					dmg += (ch.level+5);
			}
			else
			{
				int min = Fmt.getInt(using.value2);
				int max = Fmt.getInt(using.value3);
				dmg += min;
				if (max > min)
					dmg += gen.nextInt(max-min);
			}
		}

		dmg += ch.getStatMod("meleedmg");
		
		int pctBonus = 0;

		// All classes gain damage from strength.
		pctBonus += ch.maxStr();
		
		// Depending on the class type, increase by another attribute.
		if (ch.charClass.type.startsWith("melee"))
		{
			if (ch.charClass.type.endsWith("str"))
				pctBonus += ch.maxStr();
			if (ch.charClass.type.endsWith("dex"))
				pctBonus += ch.maxDex();
			if (ch.charClass.type.endsWith("int"))
				pctBonus += ch.maxInt();
			if (ch.charClass.type.endsWith("con"))
				pctBonus += ch.maxCon();
		}
		
		dmg += (int)(dmg*(pctBonus/100.0));
		
		// Give newbies a break.
		if (ch.conn.isDummy && ch.level < 10)
			dmg = (dmg*7)/(20-ch.level);
		
		if (dmg > 8)
			dmg = gen.nextInt((int)(dmg/4))+dmg*7/8;
		
		if (dmg < 0)
			dmg = 0;
			
		Combat.damage(ch, victim, dmg, hitName);
		
		if (dmg > 0 && using != null)
		{
			for (Effect e : using.effects)
			{
				if (e.name.equals("flaming"))
					if (gen.nextInt(10) > 5)
						spellDamage(ch, victim, "flaming weapon", "fire", (int)(e.level*0.7+6));
			}
		}
	}


	public static boolean checkSpellHit(CharData ch, CharData victim, String hitName, String type)
	{
		if (!Combat.fightCheck(ch, victim))
			return false;

		int toHit = 80;
		
		if (type.equals("frost"))
			toHit -= (victim.maxResFrost()/(victim.level*5));
		else if (type.equals("fire"))
			toHit -= (victim.maxResFire()/(victim.level*5));
		else if (type.equals("lightning"))
			toHit -= (victim.maxResLightning()/(victim.level*5));
		else if (type.equals("acid"))
			toHit -= (victim.maxResAcid()/(victim.level*5));
		else if (type.equals("good"))
			toHit -= (victim.maxResGood()/(victim.level*5));
		else if (type.equals("evil"))
			toHit -= (victim.maxResEvil()/(victim.level*5));

		// Add a 5% miss chance and modify it by level.
		int deltaLevel = victim.level-ch.level;
		
		if (deltaLevel < -3)
			toHit -= (int)(4.4+(deltaLevel+3)*2);
		else if (deltaLevel <= 3)
			toHit -= (int)(5+deltaLevel*0.2);
		else
			toHit -= (int)(5.6+(deltaLevel-3)*2);
		
		/* Add assorted bonuses and penalties here - darkness, blindness, etc. */
		toHit += ch.getStatMod("castaccuracy")*100/(ch.level*5);
		
		if (toHit < 3)
			toHit = 3;
		
		//sysLog("bugs", ""+toHit);
		if (gen.nextInt(100)+1 <= toHit)
			return true;

		ch.sendln("{2Your "+hitName+" {wis resisted by {2"+Fmt.seeName(ch, victim)+"{2.{x");
		if (ch != victim)
			victim.sendln("{4"+Fmt.cap(Fmt.seeName(victim, ch))+"'s {4"+hitName+" {wis resisted by {4you.{x");
		return false;
	}

	public static void spellDamage(CharData ch, CharData victim, String hitName, String type, int base)
	{
		if (hitName.length() == 0)
			hitName = "spell";

		if (!type.equals("healing"))
			if (!checkSpellHit(ch, victim, hitName, type))
				return;

		int dmg = base;

		if (type.equals("frost"))
			dmg -= victim.maxResFrost();
		else if (type.equals("fire"))
			dmg -= victim.maxResFire();
		else if (type.equals("lightning"))
			dmg -= victim.maxResLightning();
		else if (type.equals("acid"))
			dmg -= victim.maxResAcid();
		else if (type.equals("good"))
			dmg -= victim.maxResGood();
		else if (type.equals("evil"))
			dmg -= victim.maxResEvil();

		if (type.equals("healing"))
			dmg += ch.getStatMod("healing");
		else
			dmg += ch.getStatMod("spelldmg");

		// All classes gain damage from intelligence.
		dmg += (int)(dmg*(ch.maxInt()/100.0));
		
		// Add bonus based on alignment for alignment classes.
		
		
		if (dmg > 8)
			dmg = gen.nextInt((int)(dmg/4))+dmg*7/8;

		if (type.equals("healing"))
			dmg = dmg*(-1);
		else if (dmg < 0)
			dmg = 1;

		Combat.damage(ch, victim, dmg, hitName);
	}
	
	/* This function returns the number of attacks per round (3 seconds) gained by
	   a character. The "meleespeed" stat affects this. */
	public static double attacksPerRound(CharData ch)
	{
		double base = 1.0;
		double ratingPerLevel = 5+(ch.level/2.0);
		
		base += ch.getStatMod("meleespeed")/ratingPerLevel;
		
		if (base > 10)
			base = 10;
		if (base < 0.5)
			base = 0.5;
			
		return base;
	}
	
	/* This function returns a double modifier which is multiplied with a spell's
	   base cast speed to reduce casting speed. */
	public static double castSpeedMod(CharData ch)
	{
		double base = 1.0;
		double ratingPerTenPct = 5+(ch.level/2.0);
		
		base -= (ch.getStatMod("castspeed")/ratingPerTenPct)/10;
		
		if (base < 0.1)
			base = 0.1;
		if (base > 10)
			base = 10;
			
		return base;
	}
}