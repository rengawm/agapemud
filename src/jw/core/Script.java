package jw.core;
import java.util.*;
import javax.script.*;

import jw.data.*;
import static jw.core.MudMain.*;

/**
	The Script class contains functions used in all types of progs
*/
public class Script
{
	public static String resolveString(String arg, Trigger tby, String tArg, HashMap<String, String> variables, CharData actor, CharData victim, Room loc, CharData mob, ObjData obj)
	{
		String oldArg = arg;

		arg = arg.replace("%trigger.type%", tby.type);
		arg = arg.replace("%trigger.arg%", tby.arg);
		arg = arg.replace("%trigger.numarg%", ""+tby.numArg);
		
		arg = arg.replace("%arg%", tArg);
		
		if (mob != null)
		{
			arg = arg.replace("%self%", ""+chToStr(mob));
			arg = arg.replace("%self.", "%"+chToStr(mob)+".");
		}
		if (obj != null)
		{
			arg = arg.replace("%self%", ""+objToStr(obj));
			arg = arg.replace("%self.", "%"+objToStr(obj)+".");
		}
		if (loc != null)
		{
			arg = arg.replace("%self%", ""+roomToStr(loc));
			arg = arg.replace("%self.", "%"+roomToStr(loc)+".");
		}
		
		if (actor != null)
		{
			arg = arg.replace("%actor%", ""+chToStr(actor));
			arg = arg.replace("%actor.", "%"+chToStr(actor)+".");
		}
		if (victim != null)
		{
			arg = arg.replace("%victim%", ""+chToStr(victim));
			arg = arg.replace("%victim.", "%"+chToStr(victim)+".");
		}
		
		int cdStart = arg.indexOf("%jw.core.CharData@");
		while (cdStart > -1)
		{
			int cdEnd = cdStart+18;
			while (arg.charAt(cdEnd) != '%' && arg.charAt(cdEnd) != '.' && arg.length() > cdEnd)
				cdEnd++;
			String chAddr = arg.substring(cdStart+1, cdEnd);
			CharData chRep = charByMemory(arg.substring(cdStart+1, cdEnd));
			if (chRep == null)
			{
				arg = arg.replace(chAddr, "null");
				chAddr = "null";
			}
			else
			{
				int sfStart = arg.indexOf(chAddr+".");
				while (sfStart > -1)
				{
					int sfEnd = sfStart+1+chAddr.length();
					while (arg.charAt(sfEnd) != '%' && arg.charAt(sfEnd) != '.' && arg.length()-1 > sfEnd)
						sfEnd++;
					String sfName = arg.substring(sfStart+chAddr.length()+1, sfEnd);
					if (arg.charAt(sfEnd) == '%')
						arg = arg.replace("%"+chAddr+"."+sfName+"%", chEval(chRep, sfName, tby, tArg));
					else
						arg = arg.replace(chAddr+"."+sfName, chEval(chRep, sfName, tby, tArg));
					sfStart = arg.indexOf(chAddr+".");
				}
			}
			arg = arg.replace("%"+chAddr+"%", chAddr);
			
			cdStart = arg.indexOf("%jw.core.CharData@");
		}

		int odStart = arg.indexOf("%jw.core.ObjData@");
		while (odStart > -1)
		{
			int odEnd = odStart+17;
			while (arg.charAt(odEnd) != '%' && arg.charAt(odEnd) != '.' && arg.length() > odEnd)
				odEnd++;
			String objAddr = arg.substring(odStart+1, odEnd);
			ObjData objRep = objByMemory(arg.substring(odStart+1, odEnd));
			if (objRep == null)
			{
				arg = arg.replace(objAddr, "null");
				objAddr = "null";
			}
			else
			{
				int sfStart = arg.indexOf(objAddr+".");
				while (sfStart > -1)
				{
					int sfEnd = sfStart+1+objAddr.length();
					while (arg.charAt(sfEnd) != '%' && arg.charAt(sfEnd) != '.' && arg.length() > sfEnd)
						sfEnd++;
					String sfName = arg.substring(sfStart+objAddr.length()+1, sfEnd);
					if (arg.charAt(sfEnd) == '%')
						arg = arg.replace("%"+objAddr+"."+sfName+"%", objEval(objRep, sfName, tby, tArg));
					else
						arg = arg.replace(objAddr+"."+sfName, objEval(objRep, sfName, tby, tArg));
					sfStart = arg.indexOf(objAddr+".");
				}
			}
			arg = arg.replace("%"+objAddr+"%", objAddr);
			
			odStart = arg.indexOf("%jw.core.ObjData@");
		}

		int rdStart = arg.indexOf("%jw.core.Room@");
		while (rdStart > -1)
		{
			int rdEnd = rdStart+14;
			while (arg.charAt(rdEnd) != '%' && arg.charAt(rdEnd) != '.' && arg.length() > rdEnd)
				rdEnd++;
			String roomAddr = arg.substring(rdStart+1, rdEnd);
			Room roomRep = Room.lookup(arg.substring(rdStart+1, rdEnd));
			if (roomRep == null)
			{
				arg = arg.replace(roomAddr, "null");
				roomAddr = "null";
			}
			else
			{
				int sfStart = arg.indexOf(roomAddr+".");
				while (sfStart > -1)
				{
					int sfEnd = sfStart+1+roomAddr.length();
					while (arg.charAt(sfEnd) != '%' && arg.charAt(sfEnd) != '.' && arg.length() > sfEnd)
						sfEnd++;
					String sfName = arg.substring(sfStart+roomAddr.length()+1, sfEnd);
					if (arg.charAt(sfEnd) == '%')
						arg = arg.replace("%"+roomAddr+"."+sfName+"%", roomEval(roomRep, sfName, tby, tArg));
					else
						arg = arg.replace(roomAddr+"."+sfName, roomEval(roomRep, sfName, tby, tArg));
					sfStart = arg.indexOf(roomAddr+".");
				}
			}
			arg = arg.replace("%"+roomAddr+"%", roomAddr);
			
			rdStart = arg.indexOf("%jw.core.Room@");
		}

		int rndStart = arg.indexOf("%random(");
		while (rndStart > -1)
		{
			int rndEnd = rndStart+8;
			while (arg.charAt(rndEnd) != ')' && arg.length() > rndEnd)
				rndEnd++;

			String randArg = arg.substring(rndStart+8, rndEnd);
			String randRep = ""+0;
			
			if (Fmt.getInt(randArg) == 0 && !randArg.equals("0"))
				break;
				
			if (Fmt.getInt(randArg) > 0)
				randRep = ""+gen.nextInt(Fmt.getInt(randArg));

			arg = arg.replaceFirst("\\%random\\("+randArg+"\\)\\%", randRep);
			
			rndStart = arg.indexOf("%random(");
		}

		int n2sStart = arg.indexOf("%numtostr(");
		while (n2sStart > -1)
		{
			int n2sEnd = n2sStart+10;
			while (arg.charAt(n2sEnd) != ')' && arg.length() > n2sEnd)
				n2sEnd++;

			String n2sArg = arg.substring(n2sStart+10, n2sEnd);
			String n2sRep = Fmt.intToStr(Fmt.getInt(n2sArg));
			if (n2sRep.equals("zero") && !n2sArg.equals("0"))
				break;

			arg = arg.replace("%numtostr("+n2sArg+")%", n2sRep);
			
			n2sStart = arg.indexOf("%numtostr(");
		}
		
		arg = arg.replace("%time.hour%", ""+Fmt.getHour());
		arg = arg.replace("%time.hour12%", ""+((Fmt.getHour()-1 % 12)+1));
		arg = arg.replace("%time.ampm%", (Fmt.getHour() > 11 && Fmt.getHour() < 23 ? "PM" : "AM"));
		arg = arg.replace("%time.day%", ""+Fmt.getDay());
		arg = arg.replace("%time.month%", ""+Fmt.getMonth());
		arg = arg.replace("%time.season%", ""+Fmt.getSeason());
		arg = arg.replace("%time.sunrise%", ""+Fmt.getSunrise(Fmt.getSeason()));
		arg = arg.replace("%time.sunset%", ""+Fmt.getSunset(Fmt.getSeason()));
		arg = arg.replace("%time.year%", ""+Fmt.getYear());
		
		for (String s : variables.keySet())
			arg = arg.replace("%locals."+s+"%", variables.get(s));

		for (String s : globals.keySet())
			arg = arg.replace("%globals."+s+"%", globals.get(s));

		if (arg.equals(oldArg))
			return arg;
		else
			return resolveString(arg, tby, tArg, variables, actor, victim, loc, mob, obj);
	}
	
	public static CharData charByMemory(String address)
	{
		if (address.charAt(17) == '#')
		{
			int id = Fmt.getInt(address.substring(18));
			for (CharData ch : allChars())
				if (ch.id == id)
					return ch;
		}
		else
			for (CharData ch : allChars())
				if (ch.toString().equalsIgnoreCase(address))
					return ch;
		return null;
	}

	public static ObjData objByMemory(String address)
	{
		if (address.charAt(16) == '#')
		{
			int id = Fmt.getInt(address.substring(17));
			for (ObjData obj : ObjData.allObjects())
				if (obj.id == id)
					return obj;
		}
		else
			for (ObjData obj : ObjData.allObjects())
				if (obj.toString().equalsIgnoreCase(address))
					return obj;
		return null;
	}
	
	public static String chToStr(CharData ch)
	{
		if (ch == null)
			return "null";
		if (ch.id > 0)
			return "jw.core.CharData@#"+ch.id;
		return ""+ch;
	}
	public static String objToStr(ObjData obj)
	{
		if (obj == null)
			return "null";
		if (obj.id > 0)
			return "jw.core.ObjData@#"+obj.id;
		return ""+obj;
	}
	public static String roomToStr(Room r)
	{
		if (r == null)
			return "null";
		return "jw.core.Room@#"+r.id;
	}
	
	public static String chEval(CharData ch, String field, Trigger tby, String tArg)
	{
		String arg = "";
		int argStart = field.indexOf("(");
		if (argStart > -1)
		{
			int argEnd = field.length()-1;
			arg = field.substring(argStart+1, argEnd);
			field = field.substring(0, argStart);
		}

		field = field.toLowerCase();

		arg = arg.replace(",", "");
		String arg1 = CommandHandler.getArg(arg, 1);
		String arg2 = CommandHandler.getArg(arg, 2);
		String arg3 = CommandHandler.getArg(arg, 3);

		if (field.equals("addeffect"))			{	Effect newE = new Effect(arg1, Fmt.getInt(arg2), Fmt.getInt(arg3));
													dummyChar.level = Fmt.getInt(arg2);
													newE.autoMods(dummyChar);
													Effect.addEffect(ch.effects, newE, false);
													return "";
												}
		if (field.equals("align"))				return ""+ch.align;
		if (field.equals("affected"))			return (Effect.findEffect(ch.effects, arg) != null) ? "true" : "false";
		if (field.equals("armbash"))			return ""+ch.maxArmBash();
		if (field.equals("armpierce"))			return ""+ch.maxArmPierce();
		if (field.equals("armslash"))			return ""+ch.maxArmSlash();
		if (field.equals("basearmbash"))		return ""+ch.baseArmBash;
		if (field.equals("basearmpierce"))		return ""+ch.baseArmPierce;
		if (field.equals("basearmslash"))		return ""+ch.baseArmSlash;
		if (field.equals("basecha"))			return ""+ch.baseCha;
		if (field.equals("basecon"))			return ""+ch.baseCon;
		if (field.equals("basedex"))			return ""+ch.baseDex;
		if (field.equals("baseenergy"))			return ""+ch.baseMaxEnergy;
		if (field.equals("baseint"))			return ""+ch.baseInt;
		if (field.equals("basehp"))				return ""+ch.baseMaxHp;
		if (field.equals("basemana"))			return ""+ch.baseMaxMana;
		if (field.equals("baseresacid"))		return ""+ch.baseResAcid;
		if (field.equals("baseresevil"))		return ""+ch.baseResEvil;
		if (field.equals("baseresfire"))		return ""+ch.baseResFire;
		if (field.equals("baseresfrost"))		return ""+ch.baseResFrost;
		if (field.equals("baseresgood"))		return ""+ch.baseResGood;
		if (field.equals("basereslightning"))	return ""+ch.baseResLightning;
		if (field.equals("basestr"))			return ""+ch.baseStr;
		if (field.equals("cansee"))				{ if (Combat.findChar(ch, null, arg, true) == null) return "false";
												return (Combat.canSee(ch, Combat.findChar(ch, null, arg, true))) ? "true" : "false"; }
		if (field.equals("cha"))				return ""+ch.maxCha();
		if (field.equals("class"))				return ch.charClass.name;
		if (field.equals("con"))				return ""+ch.maxCon();
		if (field.equals("dex"))				return ""+ch.maxDex();
		if (field.equals("energy"))				return ""+ch.energy;
		if (field.equals("equip"))				return objToStr(ch.getWearloc(arg));
		if (field.equals("fighting"))			return chToStr(ch.fighting);
		if (field.equals("findobject"))			{ if (Fmt.getInt(arg) > 0)
												{	int id = Fmt.getInt(arg);
													for (ObjData o : ObjData.allCarriedObjects(ch)) if (o.op.id == id) return objToStr(o);
												} if (Combat.findObj(ch, ObjData.allCarriedObjects(ch), arg).length == 0) return "null";
												return objToStr(Combat.findObj(ch, ObjData.allCarriedObjects(ch), arg)[0]);}
		if (field.equals("following"))			return chToStr(ch.following);
		if (field.equals("follower"))			{ for (CharData chs : allChars()) if (chs.following == ch) return chToStr(chs);
												return "null"; }
		if (field.equals("gold"))				return ""+ch.gold;
		if (field.equals("goldall"))			return ""+(ch.gold+ch.bank);
		if (field.equals("goldbank"))			return ""+ch.bank;
		if (field.equals("hasflag"))			return (ch.flags.get(arg) != null && ch.flags.get(arg) ? "true" : "false");
		if (field.equals("hasobject"))			{ if (Fmt.getInt(arg) > 0)
												{	int id = Fmt.getInt(arg);
													for (ObjData o : ObjData.allCarriedObjects(ch)) if (o.op.id == id) return "true"; return "false";
												} return (Combat.findObj(ch, ObjData.allCarriedObjects(ch), arg).length > 0 ? "true" : "false"); }
		if (field.equals("heshe"))				return (ch.sex.equals("m") ? "he" : "she");
		if (field.equals("himher"))				return (ch.sex.equals("m") ? "him" : "her");
		if (field.equals("hisher"))				return (ch.sex.equals("m") ? "his" : "her");
		if (field.equals("hp"))					return ""+ch.hp;
		if (field.equals("int"))				return ""+ch.maxInt();
		if (field.equals("isnpc"))				return isNPC(ch);
		if (field.equals("ispvp"))				return (ch.conn.prefs.get("pvp") ? "true" : "false");
		if (field.equals("isset"))				return (ch.variables.get(arg) != null ? "true" : "false");
		if (field.equals("level"))				return ""+ch.level;
		if (field.equals("mana"))				return ""+ch.mana;
		if (field.equals("maxhp"))				return ""+ch.maxHp();
		if (field.equals("maxmana"))			return ""+ch.maxMana();
		if (field.equals("maxenergy"))			return ""+ch.maxEnergy();
		
		if (field.equals("name"))				return ch.name;
		if (field.equals("pathtoward"))			{ if (Fmt.getInt(arg) > 0)
													return Room.pathTo(ch.currentRoom, Room.lookup(Fmt.getInt(arg)), true, true);
												if (Combat.findChar(ch, null, arg, true) != null)
													return Room.pathTo(ch.currentRoom, Combat.findChar(ch, null, arg, true).currentRoom, true, true);
												return "null";}
		if (field.equals("position"))			return ch.position;
		if (field.equals("postarget"))			return objToStr(ch.positionTarget);
		if (field.equals("prevexp"))			return ""+ch.conn.prevExp;
		if (field.equals("proto"))				return ""+(ch.cp == null ? 0 : ch.cp.id);
		if (field.equals("race"))				return ch.charRace.name;
		if (field.equals("removeeffect"))		{ Effect.removeEffect(ch.effects, arg);
												return ""; }
		if (field.equals("resacid"))			return ""+ch.maxResAcid();
		if (field.equals("resevil"))			return ""+ch.maxResEvil();
		if (field.equals("resfire"))			return ""+ch.maxResFire();
		if (field.equals("resfrost"))			return ""+ch.maxResFrost();
		if (field.equals("resgood"))			return ""+ch.maxResGood();
		if (field.equals("reslightning"))		return ""+ch.maxResLightning();
		if (field.equals("room"))				return roomToStr(ch.currentRoom);
		if (field.equals("sex"))				return ch.sex;
		if (field.equals("skill"))				return ""+ch.skillPercent(Skill.lookup(arg));
		if (field.equals("short"))				return ch.shortName;
		if (field.equals("str"))				return ""+ch.maxStr();
		if (field.equals("tnl"))				return ""+ch.tnl;
		if (field.equals("vartype"))			return "character";
		
		if (ch.variables.get(field) != null)	return ch.variables.get(field);
		
		return "null";
	}

	public static String objEval(ObjData obj, String field, Trigger tby, String tArg)
	{
		String arg = "";
		int argStart = field.indexOf("(");
		if (argStart > -1)
		{
			int argEnd = field.length()-1;
			arg = field.substring(argStart+1, argEnd);
			field = field.substring(0, argStart);
		}
		
		field = field.toLowerCase();

		arg = arg.replace(",", "");
		String arg1 = CommandHandler.getArg(arg, 1);
		String arg2 = CommandHandler.getArg(arg, 2);
		String arg3 = CommandHandler.getArg(arg, 3);

		if (field.equals("addeffect"))			{	Effect newE = new Effect(arg1, Fmt.getInt(arg2), Fmt.getInt(arg3));
													dummyChar.level = Fmt.getInt(arg2);
													newE.autoMods(dummyChar);
													Effect.addEffect(obj.effects, newE, false);
													return "";
												}
		if (field.equals("affected"))			return (Effect.findEffect(obj.effects, arg) != null) ? "true" : "false";
		if (field.equals("carried_by"))			return chToStr(obj.getCurrentChar());
		if (field.equals("contains"))			return ""+obj.objects.size();
		if (field.equals("cost"))				return ""+obj.cost;
		if (field.equals("decay"))				return ""+obj.decay;
		if (field.equals("findobject"))			{ if (Fmt.getInt(arg) > 0)
												{	int id = Fmt.getInt(arg);
													for (ObjData o : ObjData.allCarriedObjects(obj)) if (o.op.id == id) return objToStr(o);
												} if (Combat.findObj(null, ObjData.allCarriedObjects(obj), arg).length == 0) return "null";
												return objToStr(Combat.findObj(null, ObjData.allCarriedObjects(obj), arg)[0]);}
		if (field.equals("hasflag"))			return (obj.flags.get(arg) != null && obj.flags.get(arg) ? "true" : "false");
		if (field.equals("hasobject"))			{ if (Fmt.getInt(arg) > 0)
												{	int id = Fmt.getInt(arg);
													for (ObjData o : ObjData.allCarriedObjects(obj)) if (o.op.id == id) return "true"; return "false";
												} return (Combat.findObj(null, ObjData.allCarriedObjects(obj), arg).length > 0 ? "true" : "false"); }
		if (field.equals("hastypeflag"))		return (obj.typeFlags.get(arg) != null && obj.typeFlags.get(arg) ? "true" : "false");
		if (field.equals("isset"))				return (obj.variables.get(arg) != null ? "true" : "false");
		if (field.equals("level"))				return ""+obj.level;
		if (field.equals("name"))				return obj.name;
		if (field.equals("proto"))				return ""+(obj.op == null ? 0 : obj.op.id);
		if (field.equals("room"))				return roomToStr(obj.getCurrentRoom());
		if (field.equals("short"))				return obj.shortName;
		if (field.equals("type"))				return obj.type;
		if (field.equals("value1"))				return obj.value1;
		if (field.equals("value2"))				return obj.value2;
		if (field.equals("value3"))				return obj.value3;
		if (field.equals("value4"))				return obj.value4;
		if (field.equals("value5"))				return obj.value5;
		if (field.equals("vartype"))			return "object";
		if (field.equals("wearloc"))			return obj.wearloc;
		
		if (obj.variables.get(field) != null)	return obj.variables.get(field);
		
		return "null";
	}

	public static String roomEval(Room rm, String field, Trigger tby, String tArg)
	{
		String arg = "";
		int argStart = field.indexOf("(");
		if (argStart > -1)
		{
			int argEnd = field.length()-1;
			arg = field.substring(argStart+1, argEnd);
			field = field.substring(0, argStart);
		}
		
		field = field.toLowerCase();

		arg = arg.replace(",", "");
		String arg1 = CommandHandler.getArg(arg, 1);
		String arg2 = CommandHandler.getArg(arg, 2);
		String arg3 = CommandHandler.getArg(arg, 3);

													sysLog("bugs", arg);

		if (field.equals("addeffect"))			{	Effect newE = new Effect(arg1, Fmt.getInt(arg2), Fmt.getInt(arg3));
													dummyChar.level = Fmt.getInt(arg2);
													newE.autoMods(dummyChar);
													Effect.addEffect(rm.effects, newE, false);
													return "";
												}
		if (field.equals("affected"))			return (Effect.findEffect(rm.effects, arg) != null) ? "true" : "false";
		if (field.equals("findchar"))			{ if (Fmt.getInt(arg) > 0)
												{	int id = Fmt.getInt(arg);
													for (CharData ch : mobs) if (ch.currentRoom == rm && ch.cp.id == id) return chToStr(ch);
												} return chToStr(Combat.findChar(null, rm, arg, false)); }
		if (field.equals("findobject"))			{ if (Fmt.getInt(arg) > 0)
												{	int id = Fmt.getInt(arg);
													for (ObjData o : rm.objects) if (o.op.id == id) return objToStr(o);
												} if (Combat.findObj(null, rm.objects, arg).length == 0) return "null";
												return objToStr(Combat.findObj(null, rm.objects, arg)[0]);}
		if (field.equals("hasflag"))			return (rm.flags.get(arg) != null && rm.flags.get(arg) ? "true" : "false");
		if (field.equals("haschar"))			{ if (Fmt.getInt(arg) > 0)
												{	int id = Fmt.getInt(arg);
													for (CharData ch : mobs) if (ch.currentRoom == rm && ch.cp.id == id) return "true"; return "false";
												} return (Combat.findChar(null, rm, arg, false) != null ? "true" : "false"); }
		if (field.equals("hasobject"))			{ if (Fmt.getInt(arg) > 0)
												{	int id = Fmt.getInt(arg);
													for (ObjData o : rm.objects) if (o.op.id == id) return "true"; return "false";
												} return (Combat.findObj(null, rm.objects, arg).length > 0 ? "true" : "false"); }
		if (field.equals("id"))					return ""+rm.id;
		if (field.equals("name"))				return rm.name;
		if (field.equals("occupants"))			{ int ctr = 0; for (CharData ch : allChars()) if (ch.currentRoom == rm) ctr++; return ""+ctr;}
		if (field.equals("pathtoward"))			{ if (Fmt.getInt(arg) > 0)
													return Room.pathTo(rm, Room.lookup(Fmt.getInt(arg)), true, true);
												if (Combat.findChar(null, rm, arg, true) != null)
													return Room.pathTo(rm, Combat.findChar(null, rm, arg, true).currentRoom, true, true);
												return "null";}
		if (field.equals("vartype"))			return "room";

		for (Exit e : rm.exits)
		{
			if (field.equals(e.direction))				return roomToStr(e.to);
			if (field.equals(e.direction+".hasflag"))	return (e.flags.get(arg) != null && e.flags.get(arg) ? "true" : "false");
			if (field.equals(e.direction+".id"))		return ""+e.to.id;
			if (field.equals(e.direction+".key"))		return ""+e.key;
		}
		
		return "null";
	}
	
	public static boolean evalIf(String arg, String debug)
	{
		// Deal with parentheses recursively.
		String parenString = "";
		do {
			parenString = maskQuoted(arg);
			int parenStart = parenString.indexOf('(');
			if (parenStart == -1)
				break;
			int parenEnd = -1;
			int parenLevel = 1;
			for (int ctr = parenStart+1; ctr < parenString.length(); ctr++)
			{
				if (parenString.charAt(ctr) == '(')
					parenLevel++;
				if (parenString.charAt(ctr) == ')')
					parenLevel--;
				if (parenLevel == 0)
				{
					parenEnd = ctr;
					break;
				}
			}
				
			if (parenEnd == -1)
			{
				sysLog("progs", "Warning: Parentheses mismatch in "+debug+".");
				return false;
			}
			
			arg = arg.substring(0, parenStart)+evalIf(arg.substring(parenStart+1, parenEnd), debug)+arg.substring(parenEnd+1);
		} while (parenString.indexOf('(') >= 0);
		
		String ors[] = arg.split("\\|\\|");
		for (int oc = 0; oc < ors.length; oc++)
		{
			ors[oc] = ors[oc].trim();
			String ands[] = ors[oc].split("\\&\\&");
			for (int ac = 0; ac < ands.length; ac++)
			{
				ands[ac] = ands[ac].trim();
				String left = CommandHandler.getArg(ands[ac], 1);
				String op = CommandHandler.getArg(ands[ac], 2);
				String right = CommandHandler.getArg(ands[ac], 3);
				String extra = CommandHandler.getArg(ands[ac], 4);
				
				left = evalMath(left, debug);
				right = evalMath(right, debug);
				
				// If this has already been resolved to a boolean, skip it.
				if ((left.equals("true") || left.equals("false"))
					&& op.length() == 0)
					continue;
				if (left.equals("!true") && op.length() == 0)
				{
					ands[ac] = "false";
					continue;
				}
				if (left.equals("!false") && op.length() == 0)
				{
					ands[ac] = "true";
					continue;
				}
				
				// If there are less than or more than three arguments, it's invalid.
				if (left.length() == 0 || op.length() == 0
					|| right.length() == 0 || extra.length() > 0)
				{
					sysLog("progs", "Warning: Invalid if syntax in "+debug+".");
					return false;
				}

				if (op.equals("=="))
				{
					if (left.equalsIgnoreCase(right))
						ands[ac] = "true";
					else
						ands[ac] = "false";
					continue;
				}
				if (op.equals("!="))
				{
					if (left.equalsIgnoreCase(right))
						ands[ac] = "false";
					else
						ands[ac] = "true";
					continue;
				}
				if (op.equals("<") || op.equals("<=") || op.equals(">") || op.equals(">="))
				{
					int intLeft = Fmt.getInt(left);
					int intRight = Fmt.getInt(right);
					if ((intLeft == 0 && !left.equals("0"))
						|| (intRight == 0 && !right.equals("0")))
					{
						sysLog("progs", "Warning: Invalid comparison of non-integers with "+op+" in "+debug+".");
						return false;
					}
					if (op.equals("<") && intLeft < intRight)
						ands[ac] = "true";
					else if (op.equals("<=") && intLeft <= intRight)
						ands[ac] = "true";
					else if (op.equals(">") && intLeft > intRight)
						ands[ac] = "true";
					else if (op.equals(">=") && intLeft >= intRight)
						ands[ac] = "true";
					else
						ands[ac] = "false";
					
					continue;
				}
				sysLog("progs", "Warning: Invalid if syntax in "+debug+".");
				return false;
			}
			
			ors[oc] = "true";
			for (String s : ands)
				if (s.equals("false"))
					ors[oc] = "false";
		}
		
		for (String s : ors)
			if (s.equals("true"))
				return true;
		return false;
	}
	
	public static String evalMath(String arg)
	{
		return evalMath(arg, "No Debugging Information Given");
	}
	
	public static String evalMath(String arg, String debug)
	{
		String tempArg = maskQuoted(arg);
		tempArg = tempArg.replace("\"", "_");
		tempArg = tempArg.replace("'", "_");
		while (tempArg.contains("__"))
			tempArg = tempArg.replace("__", "_");
		
		String exps[] = tempArg.split("\\_");
		for (String s : exps)
		{
			s = s.trim();
			if (evalable(s))
				arg = arg.replace(s, ""+evalExp(s, debug));
		}
		return arg;
	}
	
	public static int evalExp(String arg, String debug)
	{
		int result = 0;
		try
		{
			ScriptEngineManager factory = new ScriptEngineManager();
			ScriptEngine engine = factory.getEngineByName("JavaScript");
			engine.eval("result = "+arg+";");
			result = Fmt.getInt(engine.get("result").toString().split("\\.")[0]);
		} catch (Exception e)
		{
			sysLog("progs", "Warning: Invalid math in "+debug+".");
			return 0;
		}
		return result;
	}
	
	public static boolean evalable(String arg)
	{
		if (arg.length() == 0)
			return false;
		char argc[] = arg.toCharArray();
		for (char c : argc)
			if (c != '0' && c != '1' && c != '2' && c != '3' &&
				c != '4' && c != '5' && c != '6' && c != '7' &&
				c != '8' && c != '9' && c != '(' && c != ')' &&
				c != '*' && c != '/' && c != '-' && c != '+' && c != ' ')
				return false;
		return true;
	}
	
	public static String maskQuoted(String arg)
	{
		char masked[] = arg.toCharArray();
		boolean insinq = false;
		boolean indblq = false;
		for (int ctr = 0; ctr < arg.length(); ctr++)
		{
			if (arg.charAt(ctr) == '"' && !insinq)
			{
				if (indblq)
					indblq = false;
				else
					indblq = true;
			}
			if (arg.charAt(ctr) == '\'' && !indblq)
			{
				if (insinq)
					insinq = false;
				else
					insinq = true;
			}
			if (insinq || indblq)
				masked[ctr] = '_';
		}
		return new String(masked);
	}
	
	public static ArrayList<String> formatCode(UserCon c, ArrayList<String> code)
	{
		ArrayList<String> newCode = new ArrayList<String>();
		int inLevel = 0;
		int ctr = 0;
		
		for (String line : code)
		{
			ctr++;
			line = line.trim();
			
			if (line.toLowerCase().startsWith("endif"))
				inLevel--;
			if (line.toLowerCase().startsWith("else"))
				inLevel--;
			
			if (inLevel < 0)
			{
				c.sendln("{RWarning{W: {xAn endif on line "+ctr+" does not match an if - too many endifs.");
				inLevel = 0;
			}

			newCode.add(Fmt.fit("", inLevel*2)+line);

			if (line.toLowerCase().startsWith("if "))
				inLevel++;
			if (line.toLowerCase().startsWith("else"))
				inLevel++;
		}
		
		if (inLevel > 0)
			c.sendln("{RWarning{W: {xUnclosed if or else. All ifs must be matched by a closing endif.");

		return newCode;
	}
	
	public static String isNPC(CharData target)
	{
		if (target.conn.isDummy)
			return "true";
		if (!target.conn.hasReadOnce)
			return "true";
		return "false";
	}
	
	public static void checkTimed()
	{
		long startTime = System.currentTimeMillis()/1000;
		startTime = startTime-timeOffset;

		if (startTime % 360 == 0)
		{
			int tempTime = Fmt.getHour();
			
			for (Room r : rooms)
				r.checkTrigger("time", null, null, "", tempTime);
			for (CharData ch : mobs)
				ch.checkTrigger("time", null, null, "", tempTime);
			for (ObjData o : ObjData.allObjects())
				o.checkTrigger("time", null, null, "", tempTime);
				
		}
	}
}