package jw.core;
import java.util.*;
import java.lang.reflect.Method;

import jw.commands.*;
import static jw.core.MudMain.*;

/**
The Skill class represents a single skill.
*/
public class Skill implements Comparable<Skill>
{
	/** The method to invoke when this command is run. */
	private Method m;
	/** The name of this skill. */
	public String name = "";
	/** The type of this skill. (skill or spell) */
	public String type = "";
	/** The base cost of using this skill. */
	public int cost;
	/** Targeting flags for this skill. */
	public HashMap<String, Boolean> flags = new HashMap<String, Boolean>();
	/** The targeting type of this skill (offensive, defensive, etc) */
	public String targetType = "none";
	/** Availability to certain classes at certain levels. */
	public HashMap<CharClass, Integer> avail = new HashMap<CharClass, Integer>();
	/** The delay (in updates) caused by using this ability. */
	public int useDelay = 30;
	/** The cooldown between uses of this ability. */
	public int cooldown = 0;
	
	public Skill(Method newM)
	{
		m = newM;
		name = m.getName().substring(2).toLowerCase().replace("_", " ");
		if (m.getName().startsWith("sp"))
			type = "spell";
		else
			type = "skill";
	}
	
	public void run(UserCon c, String args, Object target)
	{
		try
		{
			if (c.ch.cooldowns.get(this) != null)
			{
				c.sendln("You can't use '"+name+"' for "+c.ch.cooldowns.get(this)+" more seconds.");
				if (type.equals("skill"))
					c.ch.combatQueue.remove(0);
				return;
			}

			if (type.equals("skill"))
			{
				if (c.ch.energy < cost)
				{
					c.sendln("You don't have enough energy to use that ability.");
					c.ch.combatQueue.remove(0);
					CombatCommands.doQueuedump(c, "");
					return;
				}
				c.ch.energy -= cost;
			}
			else
			{
				if (c.ch.mana < cost)
				{
					c.sendln("You don't have enough mana to cast that spell.");
					CombatCommands.doQueuedump(c, "");
					return;
				}
				c.ch.mana -= cost;
				
				if (gen.nextInt(c.ch.maxInt())+40 > c.ch.skillPercent(this))
				{
					c.sendln("You lost your concentration, and the "+name+" spell fizzles.");
					checkFailure(c.ch);
					return;
				}
			}
			
			if (cooldown > 0)
				c.ch.cooldowns.put(this, cooldown);

			HashMap<String, String> tempVar = new HashMap<String, String>();
			if (Combat.targetClass(target).equals("object"))
				tempVar.put("target", Script.objToStr((ObjData)target));
			else if (Combat.targetClass(target).equals("character"))
				tempVar.put("target", Script.chToStr((CharData)target));
			
			if (c.ch.currentRoom.checkTrigger("skill", c.ch, null, name+" "+args, 0, tempVar) != -1)
				return;
			for (CharData ch : allChars())
				if (ch.currentRoom == c.ch.currentRoom)
					if (ch.checkTrigger("skill", c.ch, null, name+" "+args, 0, tempVar) != -1)
						return;
			for (ObjData o : c.ch.currentRoom.objects)
				if (o.checkTrigger("skill", c.ch, null, name+" "+args, 0, tempVar) != -1)
					return;
			for (ObjData o : c.ch.objects)
				if (o.checkTrigger("skill", c.ch, null, name+" "+args, 0, tempVar) != -1)
					return;

			if (Combat.targetClass(target).equals("character"))
				fightCheck(c.ch, (CharData) target);

			m.invoke(null, c, args, target);

			if (!flags.get("passive"))
				checkGain(c.ch);
		} catch (Exception e) {
			sysLog("bugs", "Error in "+name+".run: "+e.getMessage());
			logException(e);
		}
	}
	
	public void runAsObject(UserCon c, int level, String args, Object target)
	{
		try
		{
			int origBMH = c.ch.baseMaxHp;
			int origMaxHp = c.ch.maxHp();
			int origBMM = c.ch.baseMaxMana;
			int origMaxMana = c.ch.maxMana();
			int origBME = c.ch.baseMaxEnergy;
			int origMaxEnergy = c.ch.maxEnergy();
			int tempLevel = c.ch.level;
			int tempPercent = c.ch.skillPercent(this);
			
			c.ch.level = level;
			c.ch.learned.put(this, 100);
			c.ch.baseMaxHp = origBMH+(origMaxHp-c.ch.maxHp());
			c.ch.baseMaxMana = origBMM+(origMaxMana-c.ch.maxMana());
			c.ch.baseMaxEnergy = origBME+(origMaxEnergy-c.ch.maxEnergy());
			
			m.invoke(null, c, args, target);
			
			if (Combat.targetClass(target).equals("character"))
				fightCheck(c.ch, (CharData) target);
			
			c.ch.level = tempLevel;
			c.ch.baseMaxHp = origBMH;
			c.ch.baseMaxMana = origBMM;
			c.ch.baseMaxEnergy = origBME;
			if (tempPercent == 0)
				c.ch.learned.remove(this);
			else
				c.ch.learned.put(this, tempPercent);
		} catch (Exception e) {
			sysLog("bugs", "Error in "+name+".run: "+e.getMessage());
			logException(e);
		}
	}
	
	public void fightCheck(CharData ch, CharData victim)
	{
		if (ch == victim)
			return;
		
		if (!targetType.equals("offensive"))
			return;
		
		if (ch.currentRoom != victim.currentRoom)
			return;
		
		if (ch.fighting == null)
			ch.fighting = victim;
		
		if (victim.fighting == null)
			victim.fighting = ch;
	}

	public int availAt(CharClass cc)
	{
		if (avail.get(cc) != null)	
			return (avail.get(cc));
		return 0;
	}
	
	public void checkGain(CharData ch)
	{
		if (ch.learned.get(this) == null)
		{
			sysLog("bugs", "Checking gain for "+name+", which is not learned by "+ch.shortName+".");
			return;
		}
		if (ch.learned.get(this) == 100)
			return;
		if (gen.nextInt(70)+31 >= ch.learned.get(this))
		{
			ch.learned.put(this, ch.learned.get(this)+1);
			if (ch.learned.get(this) == 100)
				ch.sendln("{WYou have mastered the "+name+" "+type+".{x");
			else
				ch.sendln("{5You have become better at the "+name+" "+type+".{x");
		}
	}
	public void checkFailure(CharData ch)
	{
		if (ch.learned.get(this) == null)
		{
			sysLog("bugs", "Checking gain for "+name+", which is not learned by "+ch.shortName+".");
			return;
		}
		if (ch.learned.get(this) > 75)
			return;
		if (gen.nextInt(80) >= ch.learned.get(this))
		{
			ch.learned.put(this, ch.learned.get(this)+1);
			ch.sendln("{5You learn from your mistakes, and your "+name+" "+type+" improves.{x");
		}
	}
	
	public int compareTo(Skill other)
	{
		return (this.name.compareTo(other.name));
	}
	
	public static Skill lookup(String target)
	{
		for (Skill s : skills)
			if (s.name.equalsIgnoreCase(target))
				return s;
		return null;
	}
}