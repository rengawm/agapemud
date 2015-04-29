package jw.core;
import java.util.*;

import static jw.core.MudMain.*;

/**
	The Effect class represents a single effect by name, its level, and duration.
*/
public class Effect
{
	/** The name of this effect. */
	public String name = "";
	/** The level of this effect. */
	public int level = 0;
	/** The remaining duration (in game updates) */
	public int duration = 0;
	/** The stat modifiers given by this effect. */
	public HashMap<String, Integer> statMods = new HashMap<String, Integer>();
	/** Who created this effect? */
	public CharData creator = null;
	
	public Effect(String newName, int newLevel, int newDuration)
	{
		name = newName;
		level = newLevel;
		duration = newDuration;
	}

	public void tick(CharData affected)
	{
		if (creator == null || affected == null)
			return;
		if (name.equalsIgnoreCase("disease"))
		{
			Combat.damage(creator, affected, (creator.level*15)/10, "disease");
			Fmt.actAround(affected, null, null, "$n suffers from disease.");
			affected.sendln("You suffer from disease.");
		}
	}
	
	public boolean dispelCheck(CharData ch, int bonus)
	{
		int chance = 50;
		chance += bonus;
		chance += ch.maxInt()/5;
		chance += ch.level-level;
		if (chance > 95)
			chance = 95;
		if (chance < 5)
			chance = 5;

		if (gen.nextInt(100)+1 > chance)
			return false;
		return true;
	}
	
	public static boolean addEffect(ArrayList<Effect> effects, Effect e, boolean stacks)
	{
		if (!stacks)
			for (int ctr = 0; ctr < effects.size(); ctr++)
				if (effects.get(ctr).name.equalsIgnoreCase(e.name))
					if (effects.get(ctr).level > e.level || effects.get(ctr).duration == -1)
						return false;
					else
					{
						effects.remove(ctr);
						ctr--;
					}
		
		effects.add(e);
		return true;
	}
	
	public static Effect findEffect(ArrayList<Effect> effects, String checkName)
	{
		for (Effect e : effects)
			if (e.name.equalsIgnoreCase(checkName))
				return e;
		return null;
	}
	
	public static void removeEffect(ArrayList<Effect> effects, String checkName)
	{
		Effect e = null;
		while ((e = findEffect(effects, checkName)) != null)
			effects.remove(e);
	}
	
	public void autoMods(CharData ch)
	{
		// SPELLS IN CLERIC.JAVA
		if (name.equals("bless"))
		{
			statMods.put("meleeaccuracy", (ch.level+2)*2);
			statMods.put("castaccuracy", (ch.level+2)*2);
		}
		else if (name.equals("holy armor"))
		{
			statMods.put("armslash", (ch.level+7)*14);
			statMods.put("armbash", (ch.level+7)*14);
			statMods.put("armpierce", (ch.level+7)*14);
		}
		else if (name.equals("mark of regret"))
		{
			statMods.put("dexterity", -(int)(ch.level/10));
			statMods.put("meleespeed", (-1)-(ch.level/20));
			statMods.put("castspeed", (-2)-(ch.level/10));
		}
		else if (name.equals("curse"))
		{
			statMods.put("meleeaccuracy", (int)((ch.level+2)*(-1.5)));
			statMods.put("castaccuracy", (int)((ch.level+2)*(-1.5)));
		}
		else if (name.equals("holy shield"))
		{
			statMods.put("damage absorbed", 50+ch.level*8);
		}
		else if (name.equals("righteousness"))
		{
			int totalHeal = ch.level*10;
			totalHeal += ch.getStatMod("healing");
			statMods.put("hp_regen", totalHeal/10);
			statMods.put("meleespeed", ch.level/5);
		}
		else if (name.equals("resist holy"))
		{
			statMods.put("resgood", ((ch.level+5)*10));
		}
		else if (name.equals("resist evil"))
		{
			statMods.put("resevil", ((ch.level+5)*10));
		}
		else if (name.equals("renew"))
		{
			int totalHeal = 2+ch.level*15;
			totalHeal += ch.getStatMod("healing");
			statMods.put("hp_regen", totalHeal/10);
		}
		else if (name.equals("disease"))
		{
			statMods.put("strength", -(int)(ch.level/10));
		}
		else if (name.equals("empower"))
		{
			statMods.put("meleeaccuracy", (ch.level/10));
			statMods.put("meleedmg", (ch.level/10));
		}
		else if (name.equals("revival"))
		{
			int totalHeal = 6+ch.level*20;
			totalHeal += ch.getStatMod("healing");
			statMods.put("hp_regen", totalHeal/60);
		}
		else if (name.equals("feebleness"))
		{
			statMods.put("meleedmg", -(ch.level/10));
		}
		else if (name.equals("fortify"))
		{
			statMods.put("hp", (ch.baseMaxHp/10));
		}
		else if (name.equals("greater blessing"))
		{
			statMods.put("meleeaccuracy", (ch.level+4)*3);
			statMods.put("castaccuracy", (ch.level+4)*3);
		}
		else if (name.equals("beacon of light"))
		{
			int totalHeal = 10+ch.level*20;
			totalHeal += ch.getStatMod("healing");
			statMods.put("hp_regen", totalHeal/20);
		}
		else if (name.equals("divine strength"))
		{
			statMods.put("strength", ((ch.level/10) - 2));
		}
		else if (name.equals("zeal"))
		{
			statMods.put("meleespeed", ((ch.level/10) * 4));
		}


		// SPELLS IN FIGHTER.JAVA
		else if (name.equals("enrage"))
		{
			statMods.put("dexterity", (ch.level/2+25)/15);
			statMods.put("strength", (ch.level/2+30)/10);
		}
		
		
		// SPELLS IN MAGE.JAVA
		else if (name.equals("frozen"))
		{
			statMods.put("meleespeed", (-2)-(ch.level/10));
		}
		else if (name.equals("haste"))
		{
			statMods.put("meleespeed", 3+ch.level/4);
			statMods.put("castspeed", 5+ch.level/2);
		}
		else if (name.equals("intelligence"))
		{
			statMods.put("intelligence", (ch.level/2+20)/10);
		}
		else if (name.equals("magic shield"))
		{
			statMods.put("armslash", (ch.level+5)*14);
			statMods.put("armbash", (ch.level+5)*14);
			statMods.put("armpierce", (ch.level+5)*14);
		}
		else if (name.equals("resist fire"))
		{
			statMods.put("resfire", ch.level*2);
		}
		else if (name.equals("slow"))
		{
			statMods.put("meleespeed", (-2)-(ch.level/8));
			statMods.put("dexterity", 0-(ch.level/2+20)/6);
		}
	}
}