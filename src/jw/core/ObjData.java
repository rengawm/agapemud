package jw.core;
import java.lang.reflect.Field;
import java.util.*;

import jw.data.*;
import jw.commands.*;
import static jw.core.MudMain.*;

/**
	The ObjData class represents a single object, with all associated fields.
*/
public class ObjData
{
	/** The object's ID in the database. */
	public int id;
	/** The object prototype used to generate this object. */
	public ObjProto op;
	/** The object's keyword(s). */
	public String name = "";
	/** The short name of the object (used in action phrases, "you get
		<shortname>", etc). */
	public String shortName = "";
	/** The long name of the object, used when seen in a room. */
	public String longName = "";
	/** The multi-line description of the object, used when a player looks at it. */
	public String description = "";
	/** Extra descriptions. */
	public HashMap<String, String> eds = new HashMap<String, String>();
	/** All obj progs active on this object. */
	public ArrayList<Trigger> triggers = new ArrayList<Trigger>();
	/** The level required to use this object. */
	public int level = 0;
	/** The value (in gold) of this object. */
	public int cost = 0;
	/** The seconds before this object decays. */
	public int decay = 0;
	/** The seconds since this object was last touched by a player. */
	public int lastTouched = 0;
	/** The material of this object. */
	public String material = "";
	/** The type of this object. */
	public String type = "none";
	/** Flags. */
	public HashMap<String, Boolean> flags = new HashMap<String, Boolean>();
	/** Effects. */
	public ArrayList<Effect> effects = new ArrayList<Effect>();
	/** Type-specific Flags. */
	public HashMap<String, Boolean> typeFlags = new HashMap<String, Boolean>();
	/** The first extra value on this object. */
	public String value1 = "";
	/** The second extra value on this object. */
	public String value2 = "";
	/** The third extra value on this object. */
	public String value3 = "";
	/** The fourth extra value on this object. */
	public String value4 = "";
	/** The fifth extra value on this object. */
	public String value5 = "";
	/** The stat modifiers given by this object. */
	public HashMap<String, Integer> statMods = new HashMap<String, Integer>();
	/** A list of custom variables set on this object. */
	public HashMap<String, String> variables = new HashMap<String, String>();
	/** The object's current room, if applicable. */
	public Room currentRoom = null;
	/** The character holding/using this object, if applicable. */
	public CharData currentChar = null;
	/** The object containing this object, if applicable. */
	public ObjData currentObj = null;
	/** The wearloc this object is filling, if applicable. */
	public String wearloc = "none";
	/** The list of objects contained by this object. */
	public ArrayList<ObjData> objects = new ArrayList<ObjData>();
	/** The quests this object can offer to players. */
	public ArrayList<Quest> offers = new ArrayList<Quest>();
	/** The reset ID which this object is filling. */
	public int resetFilled = 0;

	public ObjData()
	{
	}
	
	/**
	Saved objects constructor: Create a blank object to be set by another method.
	<p>
	This constructor is only used by the {@link Database#loadSavedObjects()
	Database.loadSavedObjects} method on MUD startup. Since they are loaded from a database,
	any values that they have should override the values of their ObjProto, even
	though that will still be set.
	*/
	public ObjData(int newId)
	{
		id = newId;
		for (String s : Flags.objFlags)
			flags.put(s, false);
	}
	
	/**
	Standard object constructor: Create an object and copy the values of the given
	object prototype.
	<p>
	This constructor will set all values from the values of {@code newOp}.
	
	@param newOp The ObjProto object to copy values from.
	*/
	public ObjData(ObjProto newOp)
	{
		id = 0;
		op = newOp;
		name = newOp.name;
		shortName = newOp.shortName;
		longName = newOp.longName;
		description = newOp.description;

		for (String k : newOp.eds.keySet())
			eds.put(k, newOp.eds.get(k));
		
		for (Trigger t : newOp.triggers)
			triggers.add(t);

		level = newOp.level;
		cost = newOp.cost;
		decay = newOp.decay;
		material = newOp.material;
		type = newOp.type;
		
		for (String s : newOp.typeFlags.keySet())
			typeFlags.put(s, newOp.typeFlags.get(s));

		for (String s : Flags.objFlags)
			flags.put(s, newOp.flags.get(s));

		value1 = newOp.value1;
		value2 = newOp.value2;
		value3 = newOp.value3;
		value4 = newOp.value4;
		value5 = newOp.value5;

		for (String s : newOp.statMods.keySet())
			statMods.put(s, newOp.statMods.get(s));
		
		for (String e : Flags.objEffects)
			if (newOp.effects.get(e))
			{
				Effect newE = new Effect(e, level, -1);
				effects.add(newE);
			}
	}
	
	// This uses an ObjProto to update all values to be equal to the ObjProto's values.
	public void copyFrom(ObjProto newOp)
	{
		name = newOp.name;
		shortName = newOp.shortName;
		longName = newOp.longName;
		description = newOp.description;

		eds.clear();
		for (String k : newOp.eds.keySet())
			eds.put(k, newOp.eds.get(k));
		
		triggers.clear();
		for (Trigger t : newOp.triggers)
			triggers.add(t);

		level = newOp.level;
		cost = newOp.cost;
		material = newOp.material;
		type = newOp.type;
		
		typeFlags.clear();
		for (String s : newOp.typeFlags.keySet())
			typeFlags.put(s, newOp.typeFlags.get(s));
		
		flags.clear();
		for (String s : Flags.objFlags)
			flags.put(s, newOp.flags.get(s));

		value1 = newOp.value1;
		value2 = newOp.value2;
		value3 = newOp.value3;
		value4 = newOp.value4;
		value5 = newOp.value5;

		statMods.clear();
		for (String s : newOp.statMods.keySet())
			statMods.put(s, newOp.statMods.get(s));
		
		effects.clear();
		for (String e : Flags.objEffects)
			if (newOp.effects.get(e))
			{
				Effect newE = new Effect(e, level, -1);
				effects.add(newE);
			}
	}
	
	/**
	Run automatic update tasks for this object.
	<p>
	This method is called by the {@link MudMain.MudRunner#run() MudRunner} method at
	regular intervals. It will check for any periodic action to be performed by the object.
	*/
	public void update()
	{
		for (int ctr = 0; ctr < effects.size(); ctr++)
		{
			Effect e = effects.get(ctr);
			if (e.duration == -1)
				continue;
			e.duration -= 100;
			if (e.duration <= 0)
			{
				ObjData tempObj = this;
				while (tempObj.currentObj != null)
					tempObj = currentObj;
				if (tempObj.currentChar != null)
					tempObj.currentChar.sendln(Fmt.cap(Fmt.seeName(tempObj.currentChar, this))+" has lost its '"+e.name+"' effect.");
				else if (tempObj.currentRoom != null)
					for (UserCon cs : conns)
						if (cs.ch.currentRoom == tempObj.currentRoom)
							cs.sendln(Fmt.cap(Fmt.seeName(cs.ch, this))+" has lost its '"+e.name+"' effect.");
				effects.remove(e);
				ctr--;
			}
		}
		
		if (decay > 0)
		{
			decay -= 10;
			if (decay <= 0)
			{
				String decayMsg = " decays and is gone.";
				if (type.equals("drink"))
					decayMsg = " has dried up.";
				
				ObjData tempObj = this;
				while (tempObj.currentObj != null)
					tempObj = currentObj;
				if (tempObj.currentChar != null)
					tempObj.currentChar.sendln(Fmt.cap(Fmt.seeName(tempObj.currentChar, this))+decayMsg);
				else if (tempObj.currentRoom != null)
					for (UserCon cs : conns)
						if (cs.ch.currentRoom == tempObj.currentRoom)
							cs.sendln(Fmt.cap(Fmt.seeName(cs.ch, this))+decayMsg);
				clearObjects();
				return;
			}
		}
		lastTouched += 10;
	}
	
	public void toRoom(Room r)
	{
		clearObjects();
		r.objects.add(this);
		currentRoom = r;
		lastTouched = 0;
	}
	
	public void toChar(CharData ch)
	{
		clearObjects();
		if (type.equals("money"))
		{
			ch.gold += cost;
			return;
		}
		if (type.equals("training"))
			if (typeFlags.get("autouse") && !ch.conn.isDummy)
			{
				use(ch.conn);
				return;
			}
		ch.objects.add(this);
		currentChar = ch;
		lastTouched = 0;
	}
	
	public void toObject(ObjData o)
	{
		clearObjects();
		o.objects.add(this);
		currentObj = o;
		lastTouched = 0;
	}
	
	public void clearObjects()
	{
		wearloc = "none";
		if (currentChar != null)
			currentChar.objects.remove(this);
		if (currentObj != null)
			currentObj.objects.remove(this);
		if (currentRoom != null)
			currentRoom.objects.remove(this);
		currentChar = null;
		currentObj = null;
		currentRoom = null;
	}
	
	public boolean nameMatches(String arg)
	{
		if (op != null && arg.equals(""+op.id))
			return true;
		
		String argSplit[] = arg.split(" ");
		
		for (String key : argSplit)
			if (name.toLowerCase().indexOf(" "+key) == -1
				&& !name.toLowerCase().startsWith(key))
					return false;
		return true;
	}
	
	public static String[] listContents(CharData ch, ObjData[] os, boolean useLong)
	{
		HashMap<String, Integer> contents = new HashMap<String, Integer>();
		
		for (ObjData o : os)
		{
			if (!Combat.canSee(ch, o))
				continue;
			if (useLong)
			{
				String temp = Fmt.getLookFlags(o, ch.conn)+"}T"+o.longName;
				if (!contents.containsKey(temp))
					contents.put(temp, 1);
				else
					contents.put(temp, contents.get(temp)+1);
			}
			else
			{
				String temp = Fmt.getLookFlags(o, ch.conn)+"{x"+o.shortName;
				if (!contents.containsKey(temp))
					contents.put(temp, 1);
				else
					contents.put(temp, contents.get(temp)+1);
			}
		}
		
		ArrayList<String> result = new ArrayList<String>();
		
		for (String s : contents.keySet())
		{
			if (contents.get(s) == 1)
				result.add("     "+s+"{x");
			else
				result.add(String.format("%-5s", "("+String.format("%2d", contents.get(s))+")")+s+"{x");
		}
		
		return result.toArray(new String[0]);
	}
	
	public static ArrayList<ObjData> allVisibleObjects(CharData ch)
	{
		ArrayList<ObjData> visible = new ArrayList<ObjData>();
		
		for (ObjData o : ch.objects)
			visible.add(o);
		for (ObjData o : ch.currentRoom.objects)
			visible.add(o);
		
		return visible;
	}
	
	public static ArrayList<ObjData> allCarriedObjects(CharData ch)
	{
		ArrayList<ObjData> carried = new ArrayList<ObjData>();
		
		for (ObjData o : ch.objects)
			carried.add(o);
		
		for (int ctr = 0; ctr < carried.size(); ctr++)
			for (ObjData o : carried.get(ctr).objects)
				carried.add(o);
		
		return carried;
	}

	public static ArrayList<ObjData> allCarriedObjects(ObjData obj)
	{
		ArrayList<ObjData> carried = new ArrayList<ObjData>();
		
		for (ObjData o : obj.objects)
			carried.add(o);
		
		for (int ctr = 0; ctr < carried.size(); ctr++)
			for (ObjData o : carried.get(ctr).objects)
				carried.add(o);
		
		return carried;
	}
	
	public static ArrayList<ObjData> allObjects()
	{
		ArrayList<ObjData> all = new ArrayList<ObjData>();
		
		for (Room r : rooms)
			for (ObjData o : r.objects)
				all.add(o);
		for (CharData ch : allChars())
			for (ObjData o : ch.objects)
				all.add(o);
		
		for (int ctr = 0; ctr < all.size(); ctr++)
			for (ObjData o : all.get(ctr).objects)
				all.add(o);
		
		return all;
	}
	
	public String wearlocName()
	{
		if (currentChar == null)
			return "none";

		if (wearloc.startsWith("finger"))
			return "Finger";
		if (wearloc.startsWith("trinket"))
			return "Trinket";
		if (wearloc.equals("wield1"))
			if (currentChar.getWearloc("wield2") == null)
				return "Wielded";
			else
				return "Main Hand";
		if (wearloc.equals("wield2"))
			return "Off-Hand";

		return Fmt.cap(wearloc);
	}
	
	public static double capCount(ObjData[] obs)
	{
		double totalCount = 0;
		for (ObjData o : obs)
		{
			if (o.type.equals("money"))
				continue;
			else if (o.type.equals("scroll")
				|| o.type.equals("pill")
				|| o.type.equals("potion")
				)
				totalCount += 0.05;
			else
				totalCount++;
		}
		return totalCount;
	}
	
	public static boolean capCheck(CharData ch, ObjData newObj)
	{
		ObjData newObjArr[] = new ObjData[1];
		newObjArr[0] = newObj;

		double currentFill = capCount(ch.objects.toArray(new ObjData[0]));
		currentFill += capCount(newObjArr);

		if (Flags.inventoryCapacity*20 < Math.round(currentFill*20) && !ch.conn.hasPermission("staff"))
			return false;
		return true;
	}
	
	public static String[] getTypeFlags(String type)
	{
		try
		{
			Field f = Flags.class.getDeclaredField(type+"Flags");
			String[] tempFlags = (String[])f.get(null);
			return tempFlags;
		}
		catch (Exception e)
		{}
		String[] noFlags = {};
		return noFlags;
	}
	
	public static String[] defaultTypeValues(String type)
	{
		String[] values = {"", "", "", "", ""};
		
		for (int ctr = 0; ctr < 5; ctr++)
		{
			String temp = Flags.objTypes.get(type)[ctr];
			if (temp.startsWith("(%)") || temp.startsWith("(#)"))
				values[ctr] = "0";
			else if (temp.equals("(S) Armor Type"))
				values[ctr] = Flags.armorTypes[0];
			else if (temp.equals("(S) Weapon Type"))
				values[ctr] = Flags.weaponTypes[0];
			else if (temp.equals("(S) Hit Type"))
				values[ctr] = Flags.hitTypes[0];
			else if (temp.equals("(S) Reagent Type"))
				values[ctr] = Flags.reagentTypes[0];
		}
		return values;
	}
	
	public void setTypeFlags(String typeFlagString)
	{
		for (String s : ObjData.getTypeFlags(type))
			if (typeFlagString.indexOf(";"+s+";") > -1 || typeFlagString.startsWith(s+";"))
				typeFlags.put(s, true);
			else
				typeFlags.put(s, false);
	}
	
	public static ObjData makeCoins(int coinCount)
	{
		ObjProto tempOp = ObjProto.lookup(Flags.coinsId);
		if (tempOp == null)
		{
			sysLog("bugs", "No coins object at id "+Flags.coinsId+".");
			return null;
		}
		ObjData coins = new ObjData(tempOp);
		coins.cost = coinCount;
		if (coinCount == 1)
		{
			coins.name = "gold coin";
			coins.shortName = "a gold coin";
			coins.longName = "A single gold coin has been left here.";
		}
		else if (coinCount < 20)
		{
			coins.name = "stack pile coins gold";
			coins.shortName = coinCount+" gold coins";
			coins.longName = "A stack of "+coinCount+" gold coins has been left here.";
		}
		else
		{
			coins.shortName = coinCount+" gold coins";
			coins.longName = "A pile of "+coinCount+" gold coins has been left here.";
		}
		return coins;
	}
	
	public String showInfo()
	{
		String output = Fmt.heading(shortName)+"^/";
		output = output+"}m      Level}M: }n"+level+"^/"+
						"}m       Type}M: }n"+type+"^/"+
						"}m   Material}M: }n"+material+"^/";
		if (type.equals("weapon"))
		{
			output = output+"}mWeapon Type}M:}n";
			for (String s : value1.split("\\_"))
				output = output+" "+Fmt.cap(s);
			output = output+"^/";
		}
		else if (Flags.objTypes.get(type)[0].equals("(S) Armor Type"))
		{
			output = output+"}m Armor Type}M: }n"+value1+"^/";
		}
		
		if (statMods.size() > 0)
		{
			output = output+"^/"+Fmt.heading("Bonuses")+"^/";
			for (String s : statMods.keySet())
				output = output+"Affects }n"+Flags.fullStatName(s)+" }Nby }n"+statMods.get(s)+"}N."+"^/";
		}
		
		if (effects.size() > 0)
		{
			output = output+"^/"+Fmt.heading("Effects")+"^/";
			output = output+"}m  Level   Remaining   Effect"+"^/";
			for (Effect e : effects)
			{
				if (e.duration == -1)
					output = output+" }M[ }n"+Fmt.fit(""+e.level, 4)+"}M] [ }ninfinite }M] }N"+e.name+"^/";
				else if (e.duration/600 >= 1)
					output = output+" }M[ }n"+Fmt.fit(""+e.level, 4)+"}M] [ }n"+Fmt.fit(e.duration/600+" min", 9)+"}M] }N"+e.name+"^/";
				else
					output = output+" }M[ }n"+Fmt.fit(""+e.level, 4)+"}M] [ }n"+Fmt.fit(e.duration/10+" sec", 9)+"}M] }N"+e.name+"^/";
				for (String s : e.statMods.keySet())
					output = output+"                      - affects }n"+Flags.fullStatName(s)+" }Nby }n"+e.statMods.get(s)+"}N."+"^/";
			}
		}
		
		output = output+Fmt.heading("");
		return output+"{x";
	}
	
	public void use(UserCon c)
	{
		if (checkTrigger("use", c.ch, null, "", 0) == 0)
			return;
		
		lastTouched = 0;
		if (type.equals("training"))
		{
			Skill targetSkill = Skill.lookup(value1);
			if (targetSkill == null)
			{
				c.sendln("You can't learn what that teaches.");
				return;
			}
			if (c.ch.skillPercent(targetSkill) > 0)
			{
				c.sendln("You already know that ability.");
				return;
			}
			int avail = targetSkill.availAt(c.ch.charClass);
			if (avail == 0)
			{
				c.sendln("Your class can't learn that.");
				return;
			}
			if (avail > c.ch.level)
			{
				c.sendln("You can't learn that until level "+avail+".");
				return;
			}
			c.ch.learned.put(targetSkill, 50);
			c.sendln("You now know the '"+targetSkill.name+"' "+targetSkill.type+".");
			clearObjects();
			return;
		}
		if (type.equals("portal"))
		{
			if (c.ch.currentRoom.flags.get("noteleport"))
			{
				c.sendln("You can't leave this room with that.");
				return;
			}
			Room targetRoom = Room.lookup(Fmt.getInt(value1));
			if (targetRoom == null)
			{
				if (value1.equals("0"))
				{
					do {
					targetRoom = rooms.get(gen.nextInt(rooms.size()));
					} while (c.ch.currentRoom != targetRoom && targetRoom.flags.get("noteleport"));
				}
				else
				{
					c.sendln("That portal doesn't lead anywhere.");
					return;
				}
			}
			if (targetRoom == c.ch.currentRoom)
			{
				c.sendln("You're already where that portal leads to.");
				return;
			}
			Fmt.actAround(c.ch, null, this, "$n enters $o and disappears.");
			c.sendln("You step through "+Fmt.seeName(c.ch, this)+"...");
			c.ch.currentRoom = targetRoom;
			RoomCommands.doLook(c, "");
			Fmt.actAround(c.ch, null, this, "A misty image of $o appears momentarily, and $n steps through it.");
			if (typeFlags.get("destroyonuse"))
			{
				clearObjects();
				c.sendln(Fmt.cap(Fmt.seeName(c.ch, this))+" disappears!");
			}
			return;
		}
		if (type.equals("food"))
		{
			if (c.ch.fighting != null)
			{
				c.sendln("You can't eat while you're fighting.");
				return;
			}
			String tempPosition = c.ch.position;
			if (c.ch.position.equals("standing"))
				ObjectCommands.doSit(c, "");
			if (c.ch.position.equals("sleeping")
				|| c.ch.position.equals("standing"))
			{
				c.sendln("You can't eat while you're "+c.ch.position+".");
				return;
			}
			
			if (c.ch.lastPosition.length() == 0)
				c.ch.lastPosition = tempPosition;

			int tempLevel = level;
			if (typeFlags.get("flexlevel"))
				tempLevel = c.ch.level;
			Effect newE = new Effect("eating", tempLevel, 300);
			newE.statMods.put("hp_regen", tempLevel+5);
	
			if (Effect.addEffect(c.ch.effects, newE, false))
			{
				Fmt.actAround(c.ch, null, this, "$n's starts eating $o.");
				c.sendln("You start eating "+Fmt.seeName(c.ch, this)+".");
			}
			else
				c.sendln("You're already eating something better.");
			
			if (!typeFlags.get("infinite"))
				clearObjects();
			return;
		}
		if (type.equals("drink"))
		{
			if (c.ch.fighting != null)
			{
				c.sendln("You can't drink while you're fighting.");
				return;
			}
			String tempPosition = c.ch.position;
			if (c.ch.position.equals("standing"))
				ObjectCommands.doSit(c, "");
			if (c.ch.position.equals("sleeping")
				|| c.ch.position.equals("standing"))
			{
				c.sendln("You can't drink while you're "+c.ch.position+".");
				return;
			}
			
			if (c.ch.lastPosition.length() == 0)
				c.ch.lastPosition = tempPosition;
			
			int tempLevel = level;
			if (typeFlags.get("flexlevel"))
				tempLevel = c.ch.level;
			Effect newE = new Effect("drinking", tempLevel, 300);
			newE.statMods.put("mn_regen", tempLevel+5);
	
			if (Effect.addEffect(c.ch.effects, newE, false))
			{
				Fmt.actAround(c.ch, null, this, "$n's starts drinking from $o.");
				c.sendln("You start drinking from "+Fmt.seeName(c.ch, this)+".");
			}
			else
				c.sendln("You're already drinking something better.");
			
			if (!typeFlags.get("infinite"))
				clearObjects();
			return;
		}
		c.sendln("You can't use that.");
		return;
	}

	public int checkTrigger(String type, CharData actor, CharData victim, String args, int numArg)
	{
		return checkTrigger(type, actor, victim, args, numArg, null);
	}
	public int checkTrigger(String type, CharData actor, CharData victim, String args, int numArg, HashMap<String, String> variables)
	{
		int retVal = -1;
		for (Trigger t : triggers)
			if (t.type.equals(type) && retVal == -1)
				if (t.validate(args, numArg))
					retVal = t.oprog.run(t, args, null, null, this, actor, victim, 0, 0, 0, variables);
		return retVal;
	}
	
	public Room getCurrentRoom()
	{
		if (currentRoom != null)
			return currentRoom;
		if (currentChar != null)
			return currentChar.currentRoom;
		if (currentObj != null)
			return currentObj.getCurrentRoom();
		return null;
	}
	
	public CharData getCurrentChar()
	{
		if (currentRoom != null)
			return null;
		if (currentChar != null)
			return currentChar;
		if (currentObj != null)
			return currentObj.getCurrentChar();
		return null;
	}
}