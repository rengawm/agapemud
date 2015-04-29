package jw.commands;
import java.util.*;

import jw.core.*;
import static jw.core.MudMain.*;

/**
	The Fighter class contains skills/spells for the fighter in-game class.
*/
public class Fighter
{
	public static void skKick(UserCon c, String args, Object target)
	{
		CharData victim = (CharData) target;
		int dmg = c.ch.level;
		Formulas.meleeDamage(c.ch, victim, "kick", dmg, null, "kick");
	}

	public static void skEnrage(UserCon c, String args, Object target)
	{
		CharData chTarget = (CharData) target;

		Effect newE = new Effect("enrage", c.ch.level, 1800);
		newE.autoMods(c.ch);

		if (Effect.addEffect(chTarget.effects, newE, false))
		{
			Fmt.actAround(chTarget, null, null, "$n becomes visibly enraged.");
			chTarget.sendln("Your heart races and your blood boils as you become enraged.");
		}
		else
			c.sendln("A more powerful spell is already affecting the target.");
	}
	
	public static void skHunt(UserCon c, String args, Object target)
	{
		CharData chTarget = (CharData) target;
		
		if (chTarget == null)
		{
			c.sendln("No character by that name was found.");
			return;
		}
		
		String dir = Room.pathTo(c.ch.currentRoom, chTarget.currentRoom, false, true);
		
		if (chTarget.currentRoom == c.ch.currentRoom)
		{
			c.sendln(Fmt.seeNameGlobal(c.ch, chTarget)+" is right here!");
			return;
		}
		
		if (dir.length() == 0)
		{
			c.sendln("You couldn't detect any signs of "+Fmt.seeNameGlobal(c.ch, chTarget)+".");
			return;
		}
		else if (dir.length() > c.ch.maxInt()/5+c.ch.skillPercent(Skill.lookup("hunt"))/5)
		{
			c.sendln("Any signs of "+Fmt.seeNameGlobal(c.ch, chTarget)+" are too faint for you to detect.");
			return;
		}
		
		c.sendln("Your surroundings indicate that "+Fmt.seeNameGlobal(c.ch, chTarget)+" is "+Fmt.resolveDir(dir.substring(0, 1))+" from here.");
	}
}