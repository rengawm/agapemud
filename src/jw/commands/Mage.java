package jw.commands;
import java.util.*;

import jw.core.*;
import static jw.core.MudMain.*;

/**
	The Mage class contains skills/spells for the mage in-game class.
*/
public class Mage
{
	public static void spCreate_Food(UserCon c, String args, Object target)
	{
		if (ObjProto.lookup(Flags.makeFoodId) == null)
		{
			sysLog("bugs", "No create food object at id "+Flags.makeFoodId+".");
		}
		else
		{
			for (int ctr = 0; ctr < 5; ctr++)
			{
				ObjData food = new ObjData(ObjProto.lookup(Flags.makeFoodId));
				food.level = c.ch.level;
				food.toRoom(c.ch.currentRoom);
			}
			Fmt.actRoom(c.ch.currentRoom, null, null, null, "A small stack of biscuits appears in the room.");
		}
	}

	public static void spCreate_Light(UserCon c, String args, Object target)
	{
		if (ObjProto.lookup(Flags.makeLightId) == null)
		{
			sysLog("bugs", "No create light object at id "+Flags.makeLightId+".");
		}
		else
		{
			ObjData light = new ObjData(ObjProto.lookup(Flags.makeLightId));
			light.toRoom(c.ch.currentRoom);
			Fmt.actRoom(c.ch.currentRoom, null, null, null, "A sphere of light appears in the room.");
		}
	}

	public static void spCreate_Water(UserCon c, String args, Object target)
	{
		if (ObjProto.lookup(Flags.makeWaterId) == null)
		{
			sysLog("bugs", "No create water object at id "+Flags.makeWaterId+".");
		}
		else
		{
			for (int ctr = 0; ctr < 5; ctr++)
			{
				ObjData water = new ObjData(ObjProto.lookup(Flags.makeWaterId));
				water.level = c.ch.level;
				water.toRoom(c.ch.currentRoom);
			}
			Fmt.actRoom(c.ch.currentRoom, null, null, null, "Five orbs of fresh water form in the room.");
		}
	}
	
	public static void spFlame(UserCon c, String args, Object target)
	{
		CharData victim = (CharData) target;
		int dmg = (int)(c.ch.level+8);
		Formulas.spellDamage(c.ch, victim, "flame", "fire", dmg);
	}

	public static void spFreeze(UserCon c, String args, Object target)
	{
		CharData victim = (CharData) target;
		int dmg = (int)(c.ch.level*0.75+6);
		Formulas.spellDamage(c.ch, victim, "freeze", "frost", dmg);

		Effect newE = new Effect("frozen", c.ch.level, 150);
		newE.autoMods(c.ch);

		if (Effect.addEffect(victim.effects, newE, false))
		{
			Fmt.actAround(victim, null, null, "$n's skin turns blue and $s movement slows.");
			victim.sendln("Your movements are slowed as you are frozen.");
		}
		else
			c.sendln("A more powerful spell is already affecting the target.");
	}

	public static void spHaste(UserCon c, String args, Object target)
	{
		CharData chTarget = (CharData) target;

		Effect newE = new Effect("haste", c.ch.level, 6000);
		newE.autoMods(c.ch);

		if (Effect.addEffect(chTarget.effects, newE, false))
		{
			Fmt.actAround(chTarget, null, null, "$n's movements speed up.");
			chTarget.sendln("Your surroundings seem to slow down as your movement speeds.");
		}
		else
			c.sendln("A more powerful spell is already affecting the target.");
	}
	
	public static void spInflame_Weapon(UserCon c, String args, Object target)
	{
		ObjData oTarget = (ObjData) target;
		
		if (!oTarget.type.equals("weapon"))
		{
			c.sendln("You can only cast that spell on a weapon.");
			return;
		}
		
		Effect newE = new Effect("flaming", c.ch.level, 12000);

		if (Effect.addEffect(oTarget.effects, newE, false))
			Fmt.actRoom(c.ch.currentRoom, null, null, oTarget, "$o starts to glow softly, then erupts in bright flames.");
		else
			c.sendln("A more powerful spell is already affecting the target.");
	}

	public static void spIntelligence(UserCon c, String args, Object target)
	{
		CharData chTarget = (CharData) target;

		Effect newE = new Effect("intelligence", c.ch.level, 6000);
		newE.autoMods(c.ch);

		if (Effect.addEffect(chTarget.effects, newE, false))
		{
			Fmt.actAround(chTarget, null, null, "$n's looks more intelligent.");
			chTarget.sendln("Your mind feels empowered and your intelligence increases.");
		}
	}

	public static void spMagic_Missile(UserCon c, String args, Object target)
	{
		CharData victim = (CharData) target;
		int dmg = (int)(c.ch.level*1.5+14);
		Formulas.spellDamage(c.ch, victim, "magic missile", "magic", dmg);
	}

	public static void spMagic_Shield(UserCon c, String args, Object target)
	{
		CharData chTarget = (CharData) target;

		Effect newE = new Effect("magic shield", c.ch.level, 9000);
		newE.autoMods(c.ch);

		if (Effect.addEffect(chTarget.effects, newE, false))
		{
			Fmt.actAround(chTarget, null, null, "$n is surrounded by a glittery aura.");
			chTarget.sendln("A protective magical field surrounds you.");
		}
		else
			c.sendln("A more powerful spell is already affecting the target.");
	}
	
	public static void spResist_Fire(UserCon c, String args, Object target)
	{
		CharData chTarget = (CharData) target;

		Effect newE = new Effect("resist fire", c.ch.level, 12000);
		newE.autoMods(c.ch);

		if (Effect.addEffect(chTarget.effects, newE, false))
		{
			Fmt.actAround(chTarget, null, null, "An faded red and orange field surrounds $n.");
			chTarget.sendln("A faded red and orange field surrounds you, warding off fire.");
		}
		else
			c.sendln("A more powerful spell is already affecting the target.");
	}
	
	public static void spSlow(UserCon c, String args, Object target)
	{
		CharData victim = (CharData) target;

		Effect newE = new Effect("slow", c.ch.level, 600);
		newE.autoMods(c.ch);

		if (Effect.addEffect(victim.effects, newE, false))
		{
			Fmt.actAround(victim, null, null, "$n starts to move in slow motion.");
			victim.sendln("You feel your movements slow to a crawl.");
		}
		else
			c.sendln("A more powerful spell is already affecting the target.");
	}
	
	public static void spDetect_Invisible(UserCon c, String args, Object obtarget)
	{
		CharData target = (CharData) obtarget;
		if (Effect.addEffect(target.effects, new Effect("detect invisible", c.ch.level, 9000), false))
		{
			Fmt.actAround(target, null, null, "$n's eyes glow with blue light for a moment.");
			target.sendln("Your eyes tingle as the realm of the invisible comes into view.");
		}
		else
			c.sendln("A more powerful spell is already affecting the target.");
	}
	
	public static void spInvisibility(UserCon c, String args, Object target)
	{
		Effect newE = new Effect("invisible", c.ch.level, 9000);
		if (Combat.targetClass(target).equals("object"))
		{
			ObjData oTarget = (ObjData) target;
			if (Effect.addEffect(oTarget.effects, newE, false))
			{
				oTarget.effects.remove(newE);
				c.sendln(Fmt.cap(Fmt.seeName(c.ch, oTarget))+" fades into the realm of the invisible.");
				Fmt.actAround(c.ch, null, oTarget, "$o fades into the realm of the invisible.");
				oTarget.effects.add(newE);
				return;
			}
			else
			{
				c.sendln("A more powerful spell is already affecting the target.");
				return;
			}
		}
		else if (Combat.targetClass(target).equals("character"))
		{
			CharData chTarget = (CharData) target;
			if (Effect.addEffect(chTarget.effects, newE, false))
			{
				chTarget.effects.remove(newE);
				chTarget.sendln("You enter the shifting, shadowy realm of the invisible.");
				Fmt.actAround(chTarget, null, null, "$n fades into the realm of the invisible.");
				chTarget.effects.add(newE);
				return;
			}
			else
			{
				c.sendln("A more powerful spell is already affecting the target.");
				return;
			}
		}
	}
	
	public static void spFireball(UserCon c, String args, Object target)
	{
		CharData victim = (CharData) target;
		int dmg = (int)(c.ch.level*3);
		Formulas.spellDamage(c.ch, victim, "fireball", "fire", dmg);
	}
}