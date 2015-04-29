package jw.commands;
import java.util.*;

import jw.core.*;
import static jw.core.MudMain.*;

public class Cleric
{
	public static void spBless(UserCon c, String args, Object target)
	{
		CharData chTarget = (CharData) target;

		Effect newE = new Effect("bless", c.ch.level, 9000);
		newE.autoMods(c.ch);

		if (Effect.addEffect(chTarget.effects, newE, false))
		{
			Fmt.actAround(chTarget, null, null, "A pale aura emanates from $n as $e is blessed.");
			chTarget.sendln("Your combat abilities are aided by a divine blessing.");
		}
		else
			c.sendln("A more powerful spell is already affecting the target.");
	}

	public static void spCurse(UserCon c, String args, Object target)
	{ 
		CharData chTarget = (CharData) target;

		Effect newE = new Effect("curse", c.ch.level, 3000);
		newE.autoMods(c.ch);

		if (Effect.addEffect(chTarget.effects, newE, false))
		{
			Fmt.actAround(chTarget, null, null, "A dark aura surrounds $n as $e is cursed.");
			chTarget.sendln("A curse surrounds you with a dark shadow.");
		}
		else
			c.sendln("A more powerful spell is already affecting the target.");
	}

	public static void spHarm(UserCon c, String args, Object target)
	{
		CharData victim = (CharData) target;
		int dmg = (int)(c.ch.level*1.5+10);
		Formulas.spellDamage(c.ch, victim, "harm", "good", dmg);
	}
	
	public static void spHoly_Shield(UserCon c, String args, Object target)
	{
		CharData chTarget = (CharData) target;

		Effect newE = new Effect("holy shield", c.ch.level, 1200);
		newE.autoMods(c.ch);

		if (Effect.addEffect(chTarget.effects, newE, false))
		{
			Fmt.actAround(chTarget, null, null, "Golden shimmering light forms a shield around $n.");
			chTarget.sendln("You are protected by a holy shield.");
		}
		else
			c.sendln("A more powerful spell is already affecting the target.");
	}

	public static void spMinor_Heal(UserCon c, String args, Object target)
	{
		CharData victim = (CharData) target;
		int healing = c.ch.level*2+10;
		Formulas.spellDamage(c.ch, victim, "minor heal", "healing", healing);
	}

	public static void spRenew(UserCon c, String args, Object target)
	{
		CharData chTarget = (CharData) target;

		Effect newE = new Effect("renew", c.ch.level, 100);
		newE.autoMods(c.ch);

		if (Effect.addEffect(chTarget.effects, newE, false))
		{
			Fmt.actAround(chTarget, null, null, "$n's wounds begin to mend.");
			chTarget.sendln("Your wounds begin to mend themselves.");
		}
		else
			c.sendln("A more powerful spell is already affecting the target.");
	}

	public static void spSpring(UserCon c, String args, Object target)
	{
		if (ObjProto.lookup(Flags.springId) == null)
		{
			sysLog("bugs", "No spring object at id "+Flags.springId+".");
		}
		else
		{
			ObjData spring = new ObjData(ObjProto.lookup(Flags.springId));
			spring.toRoom(c.ch.currentRoom);
			Fmt.actRoom(c.ch.currentRoom, null, null, null, "A bubbling spring emerges from the ground.");
		}
	}
	
	public static void spCure_Poison(UserCon c, String args, Object target)
	{
		CharData chTarget = (CharData) target;
		
		Effect pois = Effect.findEffect(chTarget.effects, "poison");
		if (pois == null)
		{
			c.sendln("They are not poisoned.");
			return;
		}
		
		if (pois.dispelCheck(c.ch, 50))
		{
			chTarget.effects.remove(pois);
			Fmt.actAround(chTarget, null, null, "$n looks more healthy.");
			chTarget.sendln("Your poison has been cured by "+Fmt.seeName(chTarget, c.ch)+".");
			return;
		}
		else
			c.sendln("You failed to remove the poison.");
	}
	
	public static void spHoly_Armor(UserCon c, String args, Object target)
	{
		CharData chTarget = (CharData) target;

		Effect newE = new Effect("holy armor", c.ch.level, 12000);
		newE.autoMods(c.ch);

		if (Effect.addEffect(chTarget.effects, newE, false))
		{
			Fmt.actAround(chTarget, null, null, "$n shimmers as $e becomes better protected.");
			chTarget.sendln("You feel more protected.");
		}
		else
			c.sendln("A more powerful spell is already affecting the target.");
	}
	
	public static void spRemove_Curse(UserCon c, String args, Object target)
	{
		CharData chTarget = (CharData) target;
		
		Effect curse = Effect.findEffect(chTarget.effects, "curse");
		if (curse == null)
		{
			c.sendln("They are not cursed.");
			return;
		}
		
		if (curse.dispelCheck(c.ch, 50))
		{
			chTarget.effects.remove(curse);
			Fmt.actAround(chTarget, null, null, "The darkness leaves $n.");
			chTarget.sendln(Fmt.cap(Fmt.seeName(chTarget, c.ch))+" has removed your curse.");
			return;
		}
		else
			c.sendln("You failed to remove the curse.");
	}
	
	public static void spMark_Of_Regret(UserCon c, String args, Object target)
	{
		CharData chTarget = (CharData) target;

		Effect newE = new Effect("mark of regret", c.ch.level, 1500);
		newE.autoMods(c.ch);

		if (Effect.addEffect(chTarget.effects, newE, false))
		{
			Fmt.actAround(chTarget, null, null, chTarget.shortName + " looks more lethargic.");
			chTarget.sendln("You feel more lethargic.");
		}
		else
			c.sendln("A more powerful spell is already affecting the target.");
	}
	
	public static void spDetect_Poison(UserCon c, String args, Object target)
	{
		ObjData oTarget = (ObjData) target;
		boolean check = false;
		
		if(c.ch.level >= oTarget.level)
		{
			for(Effect e : oTarget.effects)
			{
				if(e.name.equals("poison"))
				{
					c.sendln(oTarget.shortName + " glows green.");
					check = true;
				}
			}			
			if(!check)
			{
				c.sendln(oTarget.shortName + " doesn't react.");
			}
		}
		else
			c.sendln("You are not powerful enough to sense poison in this item.");
	}
	
	public static void spRighteousness(UserCon c, String args, Object target)
	{
		CharData chTarget = (CharData) target;
		
		Effect newE = new Effect("righteousness", c.ch.level, 1500);
		newE.autoMods(c.ch);
		
		if (Effect.addEffect(chTarget.effects, newE, false))
		{
			Fmt.actAround(chTarget, null, null, chTarget.shortName + " is surrounded by a blue aura.");
			chTarget.sendln("You feel empowered by righteousness.");
		}
		else
			c.sendln("A more powerful spell is already affecting the target.");
	}
	
	public static void spCure_Disease(UserCon c, String args, Object target)
	{
		CharData chTarget = (CharData) target;
		
		Effect dis = Effect.findEffect(chTarget.effects, "disease");
		if (dis == null)
		{
			c.sendln("They are not afflicted by any disease.");
			return;
		}
		
		if (dis.dispelCheck(c.ch, 50))
		{
			chTarget.effects.remove(dis);
			Fmt.actAround(chTarget, null, null, "$n doesn't seem sick anymore.");
			chTarget.sendln(Fmt.cap(Fmt.seeName(chTarget, c.ch))+" has removed your disease.");
			return;
		}
		else
			c.sendln("The disease resisted your attempt to remove it.");
	}
	
	public static void spTrue_Sight(UserCon c, String args, Object target)
	{
		CharData chTarget = (CharData) target;
		
		if (Effect.addEffect(chTarget.effects, new Effect("true sight", c.ch.level, 6000), false))
		{
			Fmt.actAround(chTarget, null, null, chTarget.shortName + "'s eyes glow with yellow light for a moment.");
			chTarget.sendln("Your eyes tingle as the realm of the invisible comes into view.");
		}
		else
			c.sendln("A more powerful spell is already affecting the target.");
	}	
	
	public static void spHeal(UserCon c, String args, Object target)
	{
		CharData victim = (CharData) target;
		int healing = c.ch.level*4+20;
		Formulas.spellDamage(c.ch, victim, "heal", "healing", healing);
	}
	
	public static void spResist_Holy(UserCon c, String args, Object target)
	{
		CharData chTarget = (CharData) target;

		Effect newE = new Effect("resist holy", c.ch.level, 3000);
		newE.autoMods(c.ch);
		
		for (Effect e : chTarget.effects)
		{
			if (e.name.equals("resist evil"))
			{
				c.sendln("They already resist the forces of evil, they cannot also resist good.");
				return;
			}
		}

		if (Effect.addEffect(chTarget.effects, newE, false))
		{
			Fmt.actAround(chTarget, null, null, chTarget.shortName + " eminates a dark aura.");
			chTarget.sendln("You feel more attuned with the forces of evil.");
		}
		else
			c.sendln("A more powerful spell is already affecting the target.");
	}
	
	public static void spResist_Evil(UserCon c, String args, Object target)
	{
		CharData chTarget = (CharData) target;

		Effect newE = new Effect("resist evil", c.ch.level, 3000);
		newE.autoMods(c.ch);

		for (Effect e : chTarget.effects)
		{
			if (e.name.equals("resist holy"))
			{
				c.sendln("They already resist the forces of evil, they cannot also resist good.");
				return;
			}
		}

		if (Effect.addEffect(chTarget.effects, newE, false))
		{
			Fmt.actAround(chTarget, null, null, chTarget.shortName + " eminates a light aura.");
			chTarget.sendln("You feel more attuned with the forces of good.");
		}
		else
			c.sendln("A more powerful spell is already affecting the target.");
	}
	
	public static void spDisease(UserCon c, String args, Object target)
	{
		CharData chTarget = (CharData) target;
		
		Effect newE = new Effect("disease", c.ch.level, 3000);
		newE.creator = c.ch;
		newE.autoMods(c.ch);
		if (Effect.addEffect(chTarget.effects, newE, false))
		{
			Fmt.actAround(chTarget, null, null, "$n looks very sick.");
			chTarget.sendln("You feel ill.");
			Combat.damage(c.ch, chTarget, (c.ch.level*15)/10, "disease");
		}
		else
			c.sendln("A more powerful spell is already affecting the target.");
	}
	
	public static void spManna(UserCon c, String args, Object target)
	{
		if (ObjProto.lookup(Flags.mannaId) == null)
		{
			sysLog("bugs", "No manna object at id "+Flags.mannaId+".");
		}
		else
		{
			ObjData manna = new ObjData(ObjProto.lookup(Flags.mannaId));
			manna.toRoom(c.ch.currentRoom);
			Fmt.actRoom(c.ch.currentRoom, null, null, null, "A large supply of manna appears.");
		}
	}
	
	public static void spAlertness(UserCon c, String args, Object target)
	{
		CharData chTarget = (CharData) target;
		
		if (Effect.addEffect(chTarget.effects, new Effect("alertness", c.ch.level, 6000), false))
		{
			Fmt.actAround(chTarget, null, null, "$n seems to be more alert.");
			chTarget.sendln("You feel more alert.");
		}
		else
			c.sendln("A more powerful spell is already affecting the target.");		
	}
	
	public static void spFear(UserCon c, String args, Object target)
	{
		CharData chTarget = (CharData) target;
		if(chTarget.fighting == null)
		{
			c.sendln("Your target must be in combat for you to cast fear on them.");
			return;
		}

		if (Effect.findEffect(chTarget.effects, "courage") != null)
		{
			c.sendln("Your fear has no effect on them.");
			return;
		}
		
		if (Formulas.checkSpellHit(c.ch, chTarget, "fear", ""))
			CombatCommands.doFlee(chTarget.conn, args);
	}
	
	public static void spLevitation(UserCon c, String args, Object target)
	{
		CharData chTarget = (CharData) target;
		
		if (Effect.addEffect(chTarget.effects, new Effect("levitation", c.ch.level, 12000), false))
		{
			Fmt.actAround(chTarget, null, null, "$n begins to float above ground.");
			chTarget.sendln("You begin to float.");
		}
		else
			c.sendln("A more powerful spell is already affecting the target.");		
	}
	
	public static void spJudgement(UserCon c, String args, Object target)
	{
		CharData chTarget = (CharData) target;
		
		if (Effect.addEffect(chTarget.effects, new Effect("judgement", c.ch.level, 300), false))
		{
			Fmt.actAround(chTarget, null, null, "$n is surround by a red aura.");
			chTarget.sendln("Your judgement is now active.");
		}
		else
			c.sendln("A more powerful spell is already affecting the target.");		
	}
	
	public static void spEmpower(UserCon c, String args, Object target)
	{
		CharData chTarget = (CharData) target;
		
		Effect newE = new Effect("empower", c.ch.level, 3000);
		newE.autoMods(c.ch);
		
		if (Effect.addEffect(chTarget.effects, newE, false))
		{
			Fmt.actAround(chTarget, null, null, "$n looks much stronger.");
			chTarget.sendln("You feel empowered.");
		}
		else
			c.sendln("A more powerful spell is already affecting the target.");
	}
	
	public static void spSmite(UserCon c, String args, Object target)
	{
		CharData victim = (CharData) target;
		int dmg = (int)(c.ch.level*3.0 +10);
		Formulas.spellDamage(c.ch, victim, "smite", "good", dmg);
	}
	
	public static void spDetect_Alignment(UserCon c, String args, Object target)
	{
		CharData chTarget = (CharData) target;
		
		if (Effect.addEffect(chTarget.effects, new Effect("detect alignment", c.ch.level, 6000), false))
		{
			Fmt.actAround(chTarget, null, null, "$n's eyes glow golden.");
			chTarget.sendln("You feel more insightful.");
		}
		else
			c.sendln("A more powerful spell is already affecting the target.");		
	}
	
	public static void spCourage(UserCon c, String args, Object target)
	{
		CharData chTarget = (CharData) target;
		
		if (Effect.addEffect(chTarget.effects, new Effect("courage", c.ch.level, 3000), false))
		{
			Fmt.actAround(chTarget, null, null, "$n lets out a brave shout.");
			chTarget.sendln("You feel fearless.");
		}
		else
			c.sendln("A more powerful spell is already affecting the target.");		
	}
	
	public static void spPassage(UserCon c, String args, Object target)
	{
		CharData chTarget = (CharData) target;
		
		if (Effect.addEffect(chTarget.effects, new Effect("passage", c.ch.level, 300), false))
		{
			Fmt.actAround(chTarget, null, null, "$n is surrounded by a translucent aura.");
			chTarget.sendln("You can now walk through doors.");
		}
		else
			c.sendln("A more powerful spell is already affecting the target.");		
	}
	
	public static void spRevival(UserCon c, String args, Object target)
	{
		CharData chTarget = (CharData) target;

		Effect newE = new Effect("revival", c.ch.level, 300);
		newE.autoMods(c.ch);

		if (Effect.addEffect(chTarget.effects, newE, false))
		{
			Fmt.actAround(chTarget, null, null, "$n's wounds begin to mend at an incredible rate.");
			chTarget.sendln("Your wounds begin to mend themselves and a feeling of health rushes through you.");
		}
		else
			c.sendln("A more powerful spell is already affecting the target.");
	}
	
	public static void spFeebleness(UserCon c, String args, Object target)
	{
		CharData chTarget = (CharData) target;

		Effect newE = new Effect("feebleness", c.ch.level, 1500);
		newE.autoMods(c.ch);

		if (Effect.addEffect(chTarget.effects, newE, false))
		{
			Fmt.actAround(chTarget, null, null, "$n looks very weak.");
			chTarget.sendln("You feel very weak.");
		}
		else
			c.sendln("A more powerful spell is already affecting the target.");
	}
	
	public static void spTurn_Undead(UserCon c, String args, Object target)
	{
		CharData victim = (CharData) target;
		int dmg = (int)(c.ch.level*4.0 +10);
		if(victim.charRace.name.equalsIgnoreCase("undead"))
			Formulas.spellDamage(c.ch, victim, "smite", "good", dmg);
		else
			c.sendln("This spell only affects undead victims.");
	}
	
	public static void spFortify(UserCon c, String args, Object target)
	{
		CharData chTarget = (CharData) target;

		Effect newE = new Effect("fortify", c.ch.level, 3000);
		newE.autoMods(c.ch);

		if (Effect.addEffect(chTarget.effects, newE, false))
		{
			Fmt.actAround(chTarget, null, null, "$n looks hardier.");
			chTarget.sendln("You feel hardier.");
		}
		else
			c.sendln("A more powerful spell is already affecting the target.");
	}
	
	public static void spMajor_Heal(UserCon c, String args, Object target)
	{
		CharData victim = (CharData) target;
		int healing = c.ch.level*8+20;
		Formulas.spellDamage(c.ch, victim, "major heal", "healing", healing);
	}
	
	public static void spDrain_Life(UserCon c, String args, Object target)
	{
		CharData chTarget = (CharData) target;

		int hpDrained = (chTarget.baseMaxHp/10);
		int manaDrained = (chTarget.baseMaxMana/10);
		Formulas.spellDamage(c.ch, chTarget, "drain life", "good", hpDrained);
		chTarget.mana -= manaDrained;
		chTarget.conn.sendln("{r" + c.ch.shortName + "'s drain life {Rdrains {rmana from you. {W[{r " + manaDrained + " {W]{x");
		Formulas.spellDamage(c.ch, c.ch, "drain life", "healing", hpDrained);
		c.ch.sendln("{wYour drain life {Rdrains{w mana from " + chTarget.shortName + ". {W[ {w" + manaDrained + " {W]{x");
		c.ch.mana += manaDrained;
		if (c.ch.mana > c.ch.maxMana())
			c.ch.mana = c.ch.maxMana();
	}
	
	public static void spGreater_Blessing(UserCon c, String args, Object target)
	{		
		CharData chTarget = (CharData) target;

		Effect newE = new Effect("greater blessing", c.ch.level, 9000);
		newE.autoMods(c.ch);
		
		for (Group g : groups)
		{
			if (g.members.contains(chTarget))
			{
				for (CharData memb : g.members)
				{
					if (memb.currentRoom == chTarget.currentRoom)
					{
						if (Effect.addEffect(memb.effects, newE, false))
						{
							if (memb == c.ch)
							{
								Fmt.actAround(memb, null, null, "A bright aura emanates from $n.");
								memb.sendln("Your combat abilities are aided by a very powerful divine blessing.");
							}
							else
							{
								Fmt.actAround(memb, null, null, "A bright aura emanates from $n as $e is blessed.");
								memb.sendln("Your combat abilities are aided by a very powerful divine blessing.");
							}
						}
						else
							c.sendln("A more powerful spell is already affecting the target.");
					}
				}
				return;
			}
		}

		if (Effect.addEffect(chTarget.effects, newE, false))
		{
			Fmt.actAround(chTarget, null, null, "A bright aura emanates from $n as $e is blessed.");
			chTarget.sendln("Your combat abilities are aided by a very powerful divine blessing.");
		}
		else
			c.sendln("A more powerful spell is already affecting the target.");
	}
	
	public static void spBeacon_Of_Light(UserCon c, String args, Object target)
	{
		CharData chTarget = (CharData) target;

		Effect newE = new Effect("beacon of light", c.ch.level, 200);
		newE.autoMods(c.ch);
		
		for (Group g : groups)
		{
			if (g.members.contains(chTarget))
			{
				for (CharData memb : g.members)
				{
					if (memb.currentRoom == chTarget.currentRoom)
					{
						if (Effect.addEffect(memb.effects, newE, false))
						{
							Fmt.actAround(memb, null, null, "$n's wounds begin to mend.");
							memb.sendln("Your wounds begin to mend themselves.");							
						}
						else
							c.sendln("A more powerful spell is already affecting the target.");
					}
				}
				return;
			}
		}

		if (Effect.addEffect(chTarget.effects, newE, false))
		{
			Fmt.actAround(chTarget, null, null, "$n's wounds begin to mend.");
			chTarget.sendln("Your wounds begin to mend themselves.");							
		}
		else
			c.sendln("A more powerful spell is already affecting the target.");
	}
	
	public static void spDivine_Strength(UserCon c, String args, Object target)
	{
		CharData chTarget = (CharData) target;

		Effect newE = new Effect("divine strength", c.ch.level, 6000);
		newE.autoMods(c.ch);

		if (Effect.addEffect(chTarget.effects, newE, false))
		{
			Fmt.actAround(chTarget, null, null, "$n looks a lot stronger.");
			chTarget.sendln("You feel strengthened.");
		}
		else
			c.sendln("A more powerful spell is already affecting the target.");
	}
	
	public static void spSend_Object(UserCon c, String args, Object target)
	{
		String arg1 = CommandHandler.getArg(args, 1).toLowerCase();
		String arg2 = CommandHandler.getArg(args, 2).toLowerCase();
		
		ObjData[] oTargets = Combat.findObj(c.ch, c.ch.objects, arg1);
		if (oTargets.length == 0)
		{
			c.sendln("You don't have one of those to send.");
			return;
		}
		ObjData oTarget = oTargets[0];
		
		if (oTarget.flags.get("nodrop"))
		{
			c.sendln("That object refuses to leave you.");
			return;
		}
		
		if (arg2.length() == 0)
		{
			c.sendln("Send that to who?");
			return;
		}
		
		if (!oTarget.wearloc.equals("none"))
		{
			c.sendln("You are wearing that and cannot send it.");
			return;
		}
		
		CharData chTarget = Combat.findChar(c.ch, null, arg2, true);
		if (chTarget == null)
		{
			c.sendln("That player could not be found.");
			return;
		}
		
		c.sendln("Your "+Fmt.seeName(c.ch, oTarget)+" has been sent to "+Fmt.seeNameGlobal(c.ch, chTarget)+".");
		Fmt.actAround(c.ch, null, oTarget, "$n holds $o in the air, and it suddenly disappears.");
		if (ObjData.capCheck(chTarget, oTarget))
		{
			oTarget.toChar(chTarget);
			chTarget.sendln("You receive "+Fmt.seeName(chTarget, oTarget)+" from "+Fmt.seeNameGlobal(chTarget, c.ch)+".");
			Fmt.actAround(chTarget, null, oTarget, "$o appears about $n in a cloud of glimmering dust.");
		}
		else
		{
			oTarget.toRoom(chTarget.currentRoom);
			chTarget.sendln(Fmt.cap(Fmt.seeNameGlobal(chTarget, c.ch))+" tries to send "+Fmt.seeName(chTarget, oTarget)+" to you, but your inventory is too full.^/It falls to the ground instead.");
			Fmt.actAround(chTarget, null, oTarget, "$o appears about $n, but falls to the ground.");
		}
	}
	
	public static void spDesperate_Prayer(UserCon c, String args, Object target)
	{
		CharData chTarget = (CharData) target;

		Effect newE = new Effect("desperate prayer", c.ch.level, 50);

		if (Effect.addEffect(chTarget.effects, newE, false))
		{
			Fmt.actAround(chTarget, null, null, "$n is surrounded by a white aura.");
			chTarget.sendln("You are temporarily protected from harm.");
		}
		else
			c.sendln("A more powerful spell is already affecting the target.");
	}
	
	public static void spZeal(UserCon c, String args, Object target)
	{
		CharData chTarget = (CharData) target;

		Effect newE = new Effect("zeal", c.ch.level, 100);
		newE.autoMods(c.ch);

		if (Effect.addEffect(chTarget.effects, newE, false))
		{
			Fmt.actAround(chTarget, null, null, "$n begins to move very fast.");
			chTarget.sendln("Your speed is drastically increased by zeal.");
		}
		else
			c.sendln("A more powerful spell is already affecting the target.");
	}
	
	public static void spEmpathy(UserCon c, String args, Object target)
	{
		CharData chTarget = (CharData) target;
		
		Effect newE = new Effect("empathy", c.ch.level, 3000);
		newE.creator = c.ch;
		newE.autoMods(c.ch);
		
		if(c.ch == chTarget)
		{
			c.sendln("You cannot cast that on yourself.");
			return;
		}
		
		if (Effect.addEffect(chTarget.effects, newE, false))
		{
			Fmt.actAround(chTarget, null, null, "A red beam of light appears between $n and " + c.ch.shortName + ".");
			chTarget.sendln("You will now absorb some of " + chTarget.shortName + "'s damage.");
		}
		else
			c.sendln("A more powerful spell is already affecting the target.");
	}
}