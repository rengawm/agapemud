package jw.commands;
import java.util.*;

import jw.core.*;
import jw.data.*;
import static jw.core.MudMain.*;

/**
	The RoomCommands class contains commands primarily used for moving through,
	exploring, or viewing rooms.
*/
public class RoomCommands
{
	/**
	Display the room's information and any characters/objects in the room to the user.
	
	@param c The user who entered this command.
	@param args A string containing any arguments to the command.
	*/
	public static void doLook(UserCon c, String args)
	{
		if (c.ch.position.equals("sleeping"))
		{
			c.sendln("You can't do that when you're "+c.ch.position+".");
			return;
		}
		
		if (!Combat.canSee(c.ch, c.ch))
		{
			if (c.ch.currentRoom.isDark())
				c.sendln("It's too dark. You can't see anything!");
			else
				c.sendln("You can't see anything!");
			return;
		}
		
		if (args.length() > 0)
		{
			String arg1 = CommandHandler.getArg(args, 1).toLowerCase();
			String arg2 = CommandHandler.getArg(args, 2).toLowerCase();
			
			if (arg1.equalsIgnoreCase("in"))
			{
				if (arg2.length() == 0)
				{
					c.sendln("Look inside what?");
					return;
				}
				
				ObjData target[] = Combat.findObj(c.ch, ObjData.allVisibleObjects(c.ch), arg2);
				if (target.length == 0)
				{
					c.sendln("There's nothing here that matches that description.");
					return;
				}
				
				if (!target[0].type.equals("container"))
				{
					c.sendln(Fmt.cap(Fmt.seeName(c.ch, target[0]))+" is not a container.");
					return;
				}
				if (target[0].typeFlags.get("closed"))
				{
					c.sendln(Fmt.cap(Fmt.seeName(c.ch, target[0]))+" is closed.");
					return;
				}
				if (target[0].objects.size() == 0)
				{
					c.sendln("There's nothing in "+Fmt.seeName(c.ch, target[0])+".");
					return;
				}
				if (target[0].objects.size() == 1)
					c.sendln("There is 1 item in "+Fmt.seeName(c.ch, target[0])+":");
				else
					c.sendln("There are "+target[0].objects.size()+" items in "+Fmt.seeName(c.ch, target[0])+":");

				String objList[] = ObjData.listContents(c.ch, target[0].objects.toArray(new ObjData[0]), false);
				for (String s : objList)
					c.sendln(s);
				return;
			}
			
			for (String k : c.ch.currentRoom.eds.keySet())
				for (String s : k.split(" "))
					if (s.startsWith(arg1))
					{
						c.sendln(c.ch.currentRoom.eds.get(k));
						return;
					}
			for (CharData ch : allChars())
				if (ch.currentRoom == c.ch.currentRoom)
					for (String k : ch.eds.keySet())
						for (String s : k.split(" "))
							if (s.startsWith(arg1))
							{
								c.sendln(ch.eds.get(k));
								return;
							}
			for (ObjData o : ObjData.allVisibleObjects(c.ch))
				for (String k : o.eds.keySet())
					for (String s : k.split(" "))
						if (s.startsWith(arg1))
						{
							c.sendln(o.eds.get(k));
							return;
						}
			
			CharData targetChar = Combat.findChar(c.ch, null, arg1, false);
			if (targetChar != null)
			{
				if (targetChar.description.length() == 0)
					c.sendln("There's nothing particularly interesting about "+Fmt.seeName(c.ch, targetChar)+".");
				else
					c.sendln(targetChar.description);
				
				c.sendln(Fmt.cap(Fmt.seeName(c.ch, targetChar))+" has "+targetChar.hp+"/"+targetChar.maxHp()+" hp.");
				c.sendln(Fmt.cap(Fmt.seeName(c.ch, targetChar))+" is using:");
				boolean printed = false;
				for (String t : Flags.wearlocs)
				{
					ObjData targetObj = targetChar.getWearloc(t);
					if (targetObj != null)
					{
						if (Combat.canSee(c.ch, targetObj))
						{
							printed = true;
							String tempString = "}M<}m"+targetObj.wearlocName()+"}M>";
							c.sendln(Fmt.fit(tempString, 18)+": }N"+Fmt.getLookFlags(targetObj, c)+"}N"+Fmt.seeName(c.ch, targetObj)+"{x");
						}
						else
						{
							printed = true;
							String tempString = "}M<}m"+targetObj.wearlocName()+"}M>";
							c.sendln(Fmt.fit(tempString, 18)+": }NSomething{x");
						}
					}
				}
				if (!printed)
					c.sendln("    Nothing.");
				
				targetChar.checkTrigger("look", c.ch, null, "", 0);
				return;
			}

			ObjData target[] = Combat.findObj(c.ch, ObjData.allVisibleObjects(c.ch), arg1);
			if (target.length == 0)
			{
				c.sendln("There's nothing here that matches that description.");
				return;
			}
			if (target[0].description.length() == 0)
				c.sendln("There's nothing particularly interesting about "+Fmt.seeName(c.ch, target[0])+".");
			else
				c.sendln(target[0].description);

			target[0].checkTrigger("look", c.ch, null, "", 0);
			return;
		}
		
		if (c.ch.usingJog.length() == 0)
		{
			c.sendln("}s"+c.ch.currentRoom.name);
			if (!c.prefs.get("brief") || c.delay == 0)
				c.sendln("}S"+c.ch.currentRoom.description);
			if (!c.prefs.get("compact") && c.ch.currentRoom.description.length() > 0)
				c.sendln("");
			String exitLine = "";
			for (Exit e : c.ch.currentRoom.exits)
				if (e.flags.get("hidden") && !(e.flags.get("door") && !e.flags.get("closed")))
					continue;
				else if (e.flags.get("door"))
					if (e.flags.get("locked"))
						exitLine = exitLine+" ^{"+e.direction+"^}";
					else if (e.flags.get("closed"))
						exitLine = exitLine+" ["+e.direction+"]";
					else
						exitLine = exitLine+" -"+e.direction+"-";
				else
					exitLine = exitLine+" "+e.direction;
			if (exitLine.length() == 0)
				exitLine = " none";
			c.sendln("}s  Exits:}t"+exitLine+"{x");
		}
		else
		{
			c.sendln("}M[}mJogging}M]  }s"+c.ch.currentRoom.name+"{x");
		}
		c.ch.currentRoom.checkTrigger("look", c.ch, null, "", 0);
		
		boolean printedLine = false;

		for (Effect e : c.ch.currentRoom.effects)
		{
			if (e.name.equals("blizzard"))
				c.sendln("      {CAn isolated, vicious blizzard coats everything in ice.{x");
			else if (e.name.equals("firestorm"))
				c.sendln("      {RA column of raging flames consumes the room in blistering heat.{x");
			printedLine = true;
		}

		String objList[] = ObjData.listContents(c.ch, c.ch.currentRoom.objects.toArray(new ObjData[0]), true);
		for (String s : objList)
		{
			printedLine = true;
			c.sendln("}T "+s+"{x");
		}

		String temp = "";
		for (CharData ch : allChars())
			if (ch.currentRoom == c.ch.currentRoom && ch != c.ch)
			{
				String flags = "";
				if (!Combat.canSee(c.ch, ch))
					continue;
				for (Effect e : c.ch.effects)
				{
					if(e.name.equals("detect alignment"))
					{
						if(ch.align >= 0)
							flags = flags+ " {Y({yGolden Aura{Y)";
						else
							flags = flags+ " {R({rRed Aura{R)";
					}
				}
				if (ch.conn.realCh != null)
				{
					if (!ch.conn.realCh.conn.isDummy)
					{
						flags = flags+" {w({mSwitched{w)";
						temp = " {W"+Fmt.cap(Fmt.seeName(c.ch, ch))+"{x";
						if (!ch.longName.startsWith(".") && !ch.longName.startsWith(","))
							temp = temp+" ";
						temp = temp+ch.longName;
					}
					else
						temp = " "+ch.longName;
				}
				else if (!ch.conn.isDummy)
				{
					temp = " {W"+Fmt.cap(Fmt.seeName(c.ch, ch))+"{x";
					if (!ch.longName.startsWith(".") && !ch.longName.startsWith(","))
						temp = temp+" ";
					temp = temp+ch.longName;
					if(ch.conn.prefs.get("pvp"))
					{
						flags = flags + " {r({RPVP{r)";
					}
				}
				else
					temp = " "+ch.longName;

				if (!ch.position.equals("standing") || ch.positionTarget != null)
					if (ch.positionTarget == null)
						temp = " "+Fmt.cap(Fmt.seeName(c.ch, ch))+" is "+ch.position+" here.";
					else
						temp = " "+Fmt.cap(Fmt.seeName(c.ch, ch))+" is "+ch.position+" on "+Fmt.seeName(c.ch, ch.positionTarget)+"{x.";

				if (ch.fighting != null)
					if (ch.fighting == c.ch)
						temp = " "+Fmt.cap(Fmt.seeName(c.ch, ch))+" is here, fighting you!{x";
					else
						temp = " "+Fmt.cap(Fmt.seeName(c.ch, ch))+", fighting "+Fmt.seeName(c.ch, ch.fighting)+".{x";

				if (!printedLine)
				{
					c.sendln("");
					printedLine = true;
				}
				flags = flags+" "+Fmt.getLookFlags(ch.conn, c);
				flags = " "+flags.trim()+"{x";
				c.sendln(flags+Fmt.cap(temp));
			}
	}
	
	public static void doScan(UserCon c, String args)
	{
		if (c.ch.position.equals("sleeping"))
		{
			c.sendln("You can't do that when you're "+c.ch.position+".");
			return;
		}

		if (!Combat.canSee(c.ch, c.ch))
		{
			if (c.ch.currentRoom.isDark())
				c.sendln("It's too dark. You can't see anything!");
			else
				c.sendln("You can't see anything!");
			return;
		}

		boolean printed = false;

		ArrayList<Exit> validExits = new ArrayList<Exit>();
		for (Exit e : c.ch.currentRoom.exits)
			if (!e.flags.get("hidden") || (e.flags.get("door") && !e.flags.get("closed")))
				validExits.add(e);
		
		if (args.length() == 0)
		{
			if (validExits.size() == 0)
			{
				c.sendln("There are no obvious exits from this room.");
				return;
			}
			
			c.sendln("In the surrounding rooms, you see...");
			for (CharData ch : allChars())
				if (ch.currentRoom == c.ch.currentRoom && ch != c.ch)
				{
					if (Combat.canSee(c.ch, ch))
					{
						c.sendln(Fmt.cap(Fmt.seeName(c.ch, ch))+", right here.");
						printed = true;
					}
				}
	
			for (Exit e : c.ch.currentRoom.exits)
				if (!e.flags.get("closed"))
					for (CharData ch : allChars())
						if (ch.currentRoom == e.to)
						{
							if (Combat.canSee(c.ch, ch))
							{
								c.sendln(Fmt.cap(Fmt.seeName(c.ch, ch))+", through the "+e.direction+" exit.");
								printed = true;
							}
						}
			if (printed == false)
				c.sendln("Nothing.");
			return;
		}

		int distanceMax = 3;
		
		args = args.toLowerCase();
		if ("north".startsWith(args))
			args = "north";
		else if ("east".startsWith(args))
			args = "east";
		else if ("south".startsWith(args))
			args = "south";
		else if ("west".startsWith(args))
			args = "west";
		else if ("up".startsWith(args))
			args = "up";
		else if ("down".startsWith(args))
			args = "down";
		else if (distanceMax > 1)
			distanceMax = 1;
		
		Room start = c.ch.currentRoom;
		for (int ctr = 1; ctr <= distanceMax; ctr++)
		{
			Exit scanExit = null;
			validExits = new ArrayList<Exit>();
			for (Exit e : start.exits)
				if (e.direction.equalsIgnoreCase(args))
					scanExit = e;
			if (scanExit == null)
			{
				if (ctr == 1)
				{
					c.sendln("That's not a valid exit from this room.");
					return;
				}
				break;
			}
			if (scanExit.flags.get("hidden") && ctr > 1)
				scanExit = null;
			if (scanExit.flags.get("closed"))
			{
				if (ctr == 1)
				{
					c.sendln("Your view is blocked by a door.");
					return;
				}
				break;
			}
			start = scanExit.to;
			
			for (CharData ch : allChars())
				if (ch.currentRoom == start)
				{
					if (Combat.canSee(c.ch, ch))
					{
						if (!printed)
							c.sendln("You look through the "+args+" exit and see...");
						if (ctr == 1)
							c.sendln(Fmt.cap(Fmt.seeName(c.ch, ch))+", one room away.");
						else
							c.sendln(Fmt.cap(Fmt.seeName(c.ch, ch))+", "+ctr+" rooms away.");
						printed = true;
					}
				}
		}
		if (!printed)
			c.sendln("Nothing.");
	}
	
	public static void doExits(UserCon c, String args)
	{
		if (c.ch.position.equals("sleeping"))
		{
			c.sendln("You can't do that when you're "+c.ch.position+".");
			return;
		}

		if (!Combat.canSee(c.ch, c.ch))
		{
			if (c.ch.currentRoom.isDark())
				c.sendln("It's too dark. You can't see anything!");
			else
				c.sendln("You can't see anything!");
			return;
		}

		ArrayList<Exit> validExits = new ArrayList<Exit>();
		for (Exit e : c.ch.currentRoom.exits)
			if (!e.flags.get("hidden"))
				validExits.add(e);

		if (validExits.size() == 0)
		{
			c.sendln("There are no obvious exits from this room.");
			return;
		}
		c.sendln("Obvious exits from this room:");
		for (Exit e : validExits)
			c.sendln("}m"+Fmt.cap(Fmt.fit(e.direction, 12))+" }M- }N"+e.to.name+"{x");
	}
	
	/**
	A standardized alias to moving north in a room.
	
	@param c The user who entered this command.
	@param args A string containing any arguments to the command.
	*/
	public static void doNorth(UserCon c, String args)
	{
		if (!c.ch.currentRoom.takeExit(c, "north"))
			c.sendln("There's no exit in that direction.");
	}

	/**
	A standardized alias to moving east in a room.
	
	@param c The user who entered this command.
	@param args A string containing any arguments to the command.
	*/
	public static void doEast(UserCon c, String args)
	{
		if (!c.ch.currentRoom.takeExit(c, "east"))
			c.sendln("There's no exit in that direction.");
	}

	/**
	A standardized alias to moving south in a room.
	
	@param c The user who entered this command.
	@param args A string containing any arguments to the command.
	*/
	public static void doSouth(UserCon c, String args)
	{
		if (!c.ch.currentRoom.takeExit(c, "south"))
			c.sendln("There's no exit in that direction.");
	}

	/**
	A standardized alias to moving west in a room.
	
	@param c The user who entered this command.
	@param args A string containing any arguments to the command.
	*/
	public static void doWest(UserCon c, String args)
	{
		if (!c.ch.currentRoom.takeExit(c, "west"))
			c.sendln("There's no exit in that direction.");
	}

	/**
	A standardized alias to moving up in a room.
	
	@param c The user who entered this command.
	@param args A string containing any arguments to the command.
	*/
	public static void doUp(UserCon c, String args)
	{
		if (!c.ch.currentRoom.takeExit(c, "up"))
			c.sendln("There's no exit in that direction.");
	}

	/**
	A standardized alias to moving down in a room.
	
	@param c The user who entered this command.
	@param args A string containing any arguments to the command.
	*/
	public static void doDown(UserCon c, String args)
	{
		if (!c.ch.currentRoom.takeExit(c, "down"))
			c.sendln("There's no exit in that direction.");
	}
	
	public static void doJog(UserCon c, String args)
	{
		if (args.length() == 0)
		{
			if (c.ch.usingJog.length() == 0)
			{
				c.sendln("Jog where?");
				return;
			}
			c.sendln("Jog aborted.");
			c.ch.usingJog = "";
			return;
		}
		
		c.sendln("You begin jogging...");
		c.ch.usingJog = args;
	}
	
	/**
	Open a given exit from the user's current room, if a door is there.
	
	@param c The user who entered this command.
	@param args A string containing any arguments to the command.
	*/
	public static void doOpen(UserCon c, String args)
	{
		if (c.ch.position.equals("sleeping"))
		{
			c.sendln("You can't do that when you're "+c.ch.position+".");
			return;
		}

		if (args.length() == 0)
		{
			c.sendln("Open what?");
			return;
		}
		
		Exit ex = c.ch.currentRoom.matchExit(args);
		
		if (ex == null)
		{
			ObjData target[] = Combat.findObj(c.ch, ObjData.allVisibleObjects(c.ch), args);
			if (target.length == 0)
			{
				c.sendln("There's no exit like that here to open.");
				return;
			}
			if (!target[0].type.equals("container"))
			{
				c.sendln(Fmt.cap(Fmt.seeName(c.ch, target[0]))+" is not a container.");
				return;
			}
			if (!target[0].typeFlags.get("closed"))
			{
				c.sendln(Fmt.cap(Fmt.seeName(c.ch, target[0]))+" is not closed.");
				return;
			}
			if (target[0].typeFlags.get("locked"))
			{
				c.sendln(Fmt.cap(Fmt.seeName(c.ch, target[0]))+" is locked.");
				return;
			}
			target[0].typeFlags.put("closed", false);
			c.sendln("You open "+Fmt.seeName(c.ch, target[0])+".");
			for (UserCon cs : conns)
				if (cs.ch.currentRoom == c.ch.currentRoom && cs != c)
					if (!cs.ch.position.equals("sleeping"))
						cs.sendln(Fmt.cap(Fmt.seeName(cs.ch, c.ch))+" opens "+Fmt.seeName(cs.ch, target[0])+".");
			return;
		}
		
		if (!ex.flags.get("door"))
		{
			c.sendln("There's no door in that direction.");
			return;
		}
		
		if (ex.flags.get("locked"))
		{
			c.sendln("It's locked.");
			return;
		}
		
		if (!ex.flags.get("closed"))
		{
			c.sendln("It's already open.");
			return;
		}
		
		if (c.ch.currentRoom.checkTrigger("dooropen", c.ch, null, ex.direction, 0) == 0)
			return;
		if (ex.to != c.ch.currentRoom)
			if (ex.to.checkTrigger("dooropen", c.ch, null, ex.direction, 0) == 0)
				return;
		for (CharData ch : mobs)
			if (ch.currentRoom == ex.to || ch.currentRoom == c.ch.currentRoom && ch != c.ch)
				if (ch.checkTrigger("dooropen", c.ch, null, ex.direction, 0) == 0)
					return;
		for (ObjData o : ObjData.allObjects())
			if (o.getCurrentRoom() == ex.to || o.getCurrentRoom() == c.ch.currentRoom)
				if (o.checkTrigger("dooropen", c.ch, null, ex.direction, 0) == 0)
					return;

		if (ex.timer > 0)
			updates.add(new Update(ex.timer, "closeDoor", ex.id));
		else if (ex.timer == 0)
			updates.add(new Update(600, "closeDoor", ex.id));
		
		String tempDir = "";
		if (ex.direction.equals("north") || ex.direction.equals("south") ||
			ex.direction.equals("west") || ex.direction.equals("east") ||
			ex.direction.equals("up") || ex.direction.equals("down"))
			tempDir = " door";
		if (ex.doorName.length() > 0)
			tempDir = " "+ex.doorName;

		ex.flags.put("closed", false);
		c.sendln("You open the "+ex.direction+tempDir+".");
		for (UserCon cs : conns)
			if (cs.ch.currentRoom == c.ch.currentRoom && cs != c)
				if (!cs.ch.position.equals("sleeping"))
					cs.sendln(Fmt.cap(Fmt.seeName(cs.ch, c.ch))+" opens the "+ex.direction+tempDir+".");
		
		Database.saveExit(c.ch.currentRoom, ex);
		Exit ex2 = ex.to.oppExit(c.ch.currentRoom, ex.direction);
		if (ex2 != null)
		{
			if (ex2.flags.get("door") && ex2.flags.get("closed") && !ex2.flags.get("locked"))
			{
				ex2.flags.put("closed", false);
				for (UserCon cs : conns)
					if (cs.ch.currentRoom == ex.to)
						cs.sendln("The "+ex2.direction+tempDir+" opens from the other side.");
				Database.saveExit(ex.to, ex2);
			}
		}
	}

	public static void doUnlock(UserCon c, String args)
	{
		if (c.ch.position.equals("sleeping"))
		{
			c.sendln("You can't do that when you're "+c.ch.position+".");
			return;
		}

		if (args.length() == 0)
		{
			c.sendln("Unlock what?");
			return;
		}
		
		Exit ex = c.ch.currentRoom.matchExit(args);
		
		if (ex == null)
		{
			ObjData target[] = Combat.findObj(c.ch, ObjData.allVisibleObjects(c.ch), args);
			if (target.length == 0)
			{
				c.sendln("There's no exit like that here to unlock.");
				return;
			}
			if (!target[0].type.equals("container"))
			{
				c.sendln(Fmt.cap(Fmt.seeName(c.ch, target[0]))+" is not a container.");
				return;
			}
			if (!target[0].typeFlags.get("closed"))
			{
				c.sendln(Fmt.cap(Fmt.seeName(c.ch, target[0]))+" is not closed.");
				return;
			}
			if (!target[0].typeFlags.get("locked"))
			{
				c.sendln(Fmt.cap(Fmt.seeName(c.ch, target[0]))+" is not locked.");
				return;
			}
			boolean keyCheck = false;
			for (ObjData o : c.ch.objects)
			{
				if (!o.type.equals("key") || o.op == null)
					continue;
				if (o.op.id == Fmt.getInt(target[0].value2))
				{
					if (o.checkTrigger("use", c.ch, null, "", 0) == 0)
						return;
					keyCheck = true;
					if (o.typeFlags.get("destroyonuse"))
					{
						c.sendln(Fmt.cap(Fmt.seeName(c.ch, o))+" dissolves in the lock.");
						c.ch.objects.remove(o);
					}
					break;
				}
			}
			if (keyCheck || c.hasPermission("staff") || c.hasPermission("builder"))
			{
				target[0].typeFlags.put("locked", false);
				c.sendln("You unlock "+Fmt.seeName(c.ch, target[0])+".");
				for (UserCon cs : conns)
					if (cs.ch.currentRoom == c.ch.currentRoom && cs != c)
						if (!cs.ch.position.equals("sleeping"))
							cs.sendln(Fmt.cap(Fmt.seeName(cs.ch, c.ch))+" unlocks "+Fmt.seeName(cs.ch, target[0])+".");
				return;
			}
			c.sendln("You don't have a key which unlocks that container.");
			return;
		}
		
		if (!ex.flags.get("door"))
		{
			c.sendln("There's no door in that direction.");
			return;
		}
		
		if (!ex.flags.get("locked"))
		{
			c.sendln("It's not locked.");
			return;
		}
		
		if (!ex.flags.get("closed"))
		{
			c.sendln("It's already open.");
			return;
		}
		
		boolean keyCheck = false;
		for (ObjData o : c.ch.objects)
		{
			if (!o.type.equals("key") || o.op == null)
				continue;
			if (o.op.id == ex.key)
			{
				if (o.checkTrigger("use", c.ch, null, "", 0) == 0)
					return;
				keyCheck = true;
				if (o.typeFlags.get("destroyonuse"))
				{
					c.sendln(Fmt.cap(Fmt.seeName(c.ch, o))+" dissolves in the lock.");
					c.ch.objects.remove(o);
				}
				break;
			}
		}
		
		int tempInt = 0;
		if (!keyCheck || ex.key == 0)
			do {
				if ((tempInt = c.ch.currentRoom.checkTrigger("doorunlock", c.ch, null, ex.direction, 0)) == 0)
					return;
				if (tempInt == 1)
				{
					keyCheck = true;
					break;
				}
				if (ex.to != c.ch.currentRoom)
				{
					if ((tempInt = ex.to.checkTrigger("doorunlock", c.ch, null, ex.direction, 0)) == 0)
						return;
					if (tempInt == 1)
					{
						keyCheck = true;
						break;
					}
				}
	
				for (CharData ch : mobs)
					if (ch.currentRoom == ex.to || ch.currentRoom == c.ch.currentRoom && ch != c.ch)
					{
						if ((tempInt = ch.checkTrigger("doorunlock", c.ch, null, ex.direction, 0)) == 0)
							return;
						if (tempInt == 1)
						{
							keyCheck = true;
							break;
						}
					}
				if (tempInt == 1)
					break;
					
				for (ObjData o : ObjData.allObjects())
					if (o.getCurrentRoom() == ex.to || o.getCurrentRoom() == c.ch.currentRoom)
					{
						if ((tempInt = o.checkTrigger("doorunlock", c.ch, null, ex.direction, 0)) == 0)
							return;
						if (tempInt == 1)
						{
							keyCheck = true;
							break;
						}
					}
			} while (false);

		if (keyCheck || c.hasPermission("staff") || c.hasPermission("builder"))
		{
			if (ex.timer > 0)
				updates.add(new Update(ex.timer, "closeDoor", ex.id));
			else if (ex.timer == 0)
				updates.add(new Update(600, "closeDoor", ex.id));
			
			ex.flags.put("locked", false);
			String tempDir = "";
			if (ex.direction.equals("north") || ex.direction.equals("south") ||
				ex.direction.equals("west") || ex.direction.equals("east") ||
				ex.direction.equals("up") || ex.direction.equals("down"))
				tempDir = " door";
			if (ex.doorName.length() > 0)
				tempDir = " "+ex.doorName;
			
			c.sendln("You unlock the "+ex.direction+tempDir+".");
			for (UserCon cs : conns)
				if (cs.ch.currentRoom == c.ch.currentRoom && cs != c)
					if (!cs.ch.position.equals("sleeping"))
						cs.sendln(Fmt.cap(Fmt.seeName(cs.ch, c.ch))+" unlocks the "+ex.direction+tempDir+".");
			
			Database.saveExit(c.ch.currentRoom, ex);
			Exit ex2 = ex.to.oppExit(c.ch.currentRoom, ex.direction);
			if (ex2 != null)
			{
				if (ex2.flags.get("door") && ex2.flags.get("closed") && ex2.flags.get("locked"))
				{
					ex2.flags.put("locked", false);
					for (UserCon cs : conns)
						if (cs.ch.currentRoom == ex.to)
							cs.sendln("The "+ex2.direction+tempDir+" is unlocked from the other side.");
					Database.saveExit(ex.to, ex2);
				}
			}
			return;
		}
		c.sendln("You don't have a key which unlocks that exit.");
		return;
	}

	public static void doLock(UserCon c, String args)
	{
		if (c.ch.position.equals("sleeping"))
		{
			c.sendln("You can't do that when you're "+c.ch.position+".");
			return;
		}

		if (args.length() == 0)
		{
			c.sendln("Lock what?");
			return;
		}
		
		Exit ex = c.ch.currentRoom.matchExit(args);
		
		if (ex == null)
		{
			ObjData target[] = Combat.findObj(c.ch, ObjData.allVisibleObjects(c.ch), args);
			if (target.length == 0)
			{
				c.sendln("There's no exit like that here to lock.");
				return;
			}
			if (!target[0].type.equals("container"))
			{
				c.sendln(Fmt.cap(Fmt.seeName(c.ch, target[0]))+" is not a container.");
				return;
			}
			if (!target[0].typeFlags.get("closed"))
			{
				c.sendln(Fmt.cap(Fmt.seeName(c.ch, target[0]))+" is not closed.");
				return;
			}
			if (target[0].typeFlags.get("locked"))
			{
				c.sendln(Fmt.cap(Fmt.seeName(c.ch, target[0]))+" is already locked.");
				return;
			}
			boolean keyCheck = false;
			for (ObjData o : c.ch.objects)
			{
				if (!o.type.equals("key") || o.op == null)
					continue;
				if (o.op.id == Fmt.getInt(target[0].value2))
				{
					if (o.checkTrigger("use", c.ch, null, "", 0) == 0)
						return;
					keyCheck = true;
					break;
				}
			}
			
			if (keyCheck || c.hasPermission("staff") || c.hasPermission("builder"))
			{
				target[0].typeFlags.put("locked", true);
				c.sendln("You lock "+Fmt.seeName(c.ch, target[0])+".");
				for (UserCon cs : conns)
					if (cs.ch.currentRoom == c.ch.currentRoom && cs != c)
						if (!cs.ch.position.equals("sleeping"))
							cs.sendln(Fmt.cap(Fmt.seeName(cs.ch, c.ch))+" locks "+Fmt.seeName(cs.ch, target[0])+".");
				return;
			}
			c.sendln("You don't have a key which locks that container.");
			return;
		}
		
		if (!ex.flags.get("door"))
		{
			c.sendln("There's no door in that direction.");
			return;
		}
		
		if (ex.flags.get("locked"))
		{
			c.sendln("It's already locked.");
			return;
		}
		
		if (!ex.flags.get("closed"))
		{
			c.sendln("It's not closed.");
			return;
		}
		
		boolean keyCheck = false;
		for (ObjData o : c.ch.objects)
		{
			if (!o.type.equals("key") || o.op == null)
				continue;
			if (o.op.id == ex.key || c.hasPermission("staff") || c.hasPermission("builder"))
			{
				if (o.checkTrigger("use", c.ch, null, "", 0) == 0)
					return;
				keyCheck = true;
				break;
			}
		}
		
		int tempInt = 0;
		if (!keyCheck || ex.key == 0)
			do {
				if ((tempInt = c.ch.currentRoom.checkTrigger("doorlock", c.ch, null, ex.direction, 0)) == 0)
					return;
				if (tempInt == 1)
				{
					keyCheck = true;
					break;
				}
				if (ex.to != c.ch.currentRoom)
				{
					if ((tempInt = ex.to.checkTrigger("doorlock", c.ch, null, ex.direction, 0)) == 0)
						return;
					if (tempInt == 1)
					{
						keyCheck = true;
						break;
					}
				}
	
				for (CharData ch : mobs)
					if (ch.currentRoom == ex.to || ch.currentRoom == c.ch.currentRoom && ch != c.ch)
					{
						if ((tempInt = ch.checkTrigger("doorlock", c.ch, null, ex.direction, 0)) == 0)
							return;
						if (tempInt == 1)
						{
							keyCheck = true;
							break;
						}
					}
				if (tempInt == 1)
					break;
					
				for (ObjData o : ObjData.allObjects())
					if (o.getCurrentRoom() == ex.to || o.getCurrentRoom() == c.ch.currentRoom)
					{
						if ((tempInt = o.checkTrigger("doorlock", c.ch, null, ex.direction, 0)) == 0)
							return;
						if (tempInt == 1)
						{
							keyCheck = true;
							break;
						}
					}
			} while (false);

		if (keyCheck || c.hasPermission("staff") || c.hasPermission("builder"))
		{
			ex.flags.put("locked", true);
			String tempDir = "";
			if (ex.direction.equals("north") || ex.direction.equals("south") ||
				ex.direction.equals("west") || ex.direction.equals("east") ||
				ex.direction.equals("up") || ex.direction.equals("down"))
				tempDir = " door";
			if (ex.doorName.length() > 0)
				tempDir = " "+ex.doorName;

			c.sendln("You lock the "+ex.direction+tempDir+".");
			for (UserCon cs : conns)
				if (cs.ch.currentRoom == c.ch.currentRoom && cs != c)
					if (!cs.ch.position.equals("sleeping"))
						cs.sendln(Fmt.cap(Fmt.seeName(cs.ch, c.ch))+" locks the "+ex.direction+tempDir+".");
			
			Database.saveExit(c.ch.currentRoom, ex);
			Exit ex2 = ex.to.oppExit(c.ch.currentRoom, ex.direction);
			if (ex2 != null)
			{
				if (ex2.flags.get("door") && ex2.flags.get("closed") && !ex2.flags.get("locked"))
				{
					ex2.flags.put("locked", true);
					for (UserCon cs : conns)
						if (cs.ch.currentRoom == ex.to)
							cs.sendln("The "+ex2.direction+tempDir+" is locked from the other side.");
					Database.saveExit(ex.to, ex2);
				}
			}
			return;
		}
		c.sendln("You don't have a key which locks that exit.");
		return;
	}
	
	/**
	Close a given exit from the user's current room, if a door is there.
	
	@param c The user who entered this command.
	@param args A string containing any arguments to the command.
	*/
	public static void doClose(UserCon c, String args)
	{
		if (c.ch.position.equals("sleeping"))
		{
			c.sendln("You can't do that when you're "+c.ch.position+".");
			return;
		}

		if (args.length() == 0)
		{
			c.sendln("Close what?");
			return;
		}
		
		Exit ex = c.ch.currentRoom.matchExit(args);
		
		if (ex == null)
		{
			ObjData target[] = Combat.findObj(c.ch, ObjData.allVisibleObjects(c.ch), args);
			if (target.length == 0)
			{
				c.sendln("There's no exit like that here to close.");
				return;
			}
			if (!target[0].type.equals("container"))
			{
				c.sendln(Fmt.cap(Fmt.seeName(c.ch, target[0]))+" is not a container.");
				return;
			}
			if (target[0].typeFlags.get("closed"))
			{
				c.sendln(Fmt.cap(Fmt.seeName(c.ch, target[0]))+" is already closed.");
				return;
			}
			if (!target[0].typeFlags.get("closeable"))
			{
				c.sendln(Fmt.cap(Fmt.seeName(c.ch, target[0]))+" can't be closed.");
				return;
			}
			target[0].typeFlags.put("closed", true);
			c.sendln("You close "+Fmt.seeName(c.ch, target[0])+".");
			for (UserCon cs : conns)
				if (cs.ch.currentRoom == c.ch.currentRoom && cs != c)
					if (!cs.ch.position.equals("sleeping"))
						cs.sendln(Fmt.cap(Fmt.seeName(cs.ch, c.ch))+" closes "+Fmt.seeName(cs.ch, target[0])+".");
			return;
		}
		
		
		if (!ex.flags.get("door"))
		{
			c.sendln("There's no door in that direction.");
			return;
		}
		
		if (ex.flags.get("closed"))
		{
			c.sendln("It's already closed.");
			return;
		}

		if (c.ch.currentRoom.checkTrigger("doorclose", c.ch, null, ex.direction, 0) == 0)
			return;
		if (ex.to != c.ch.currentRoom)
			if (ex.to.checkTrigger("doorclose", c.ch, null, ex.direction, 0) == 0)
				return;
		for (CharData ch : mobs)
			if (ch.currentRoom == ex.to || ch.currentRoom == c.ch.currentRoom && ch != c.ch)
				if (ch.checkTrigger("doorclose", c.ch, null, ex.direction, 0) == 0)
					return;
		for (ObjData o : ObjData.allObjects())
			if (o.getCurrentRoom() == ex.to || o.getCurrentRoom() == c.ch.currentRoom)
				if (o.checkTrigger("doorclose", c.ch, null, ex.direction, 0) == 0)
					return;
		
		ex.flags.put("closed", true);
		String tempDir = "";
		if (ex.direction.equals("north") || ex.direction.equals("south") ||
			ex.direction.equals("west") || ex.direction.equals("east") ||
			ex.direction.equals("up") || ex.direction.equals("down"))
			tempDir = " door";
		if (ex.doorName.length() > 0)
			tempDir = " "+ex.doorName;

		c.sendln("You close the "+ex.direction+tempDir+".");
		for (UserCon cs : conns)
			if (cs.ch.currentRoom == c.ch.currentRoom && cs != c)
				if (!cs.ch.position.equals("sleeping"))
					cs.sendln(Fmt.cap(Fmt.seeName(cs.ch, c.ch))+" closes the "+ex.direction+tempDir+".");
		
		Database.saveExit(c.ch.currentRoom, ex);
		Exit ex2 = ex.to.oppExit(c.ch.currentRoom, ex.direction);
		if (ex2 != null)
		{
			if (ex2.flags.get("door") && !ex2.flags.get("closed"))
			{
				ex2.flags.put("closed", true);
				for (UserCon cs : conns)
					if (cs.ch.currentRoom == ex.to)
						cs.sendln("The "+ex2.direction+tempDir+" closes from the other side.");
				Database.saveExit(ex.to, ex2);
			}
		}
	}
	
	/**
	Show information about the user's current area, as well as any other players
	in the area.
	
	@param c The user who entered this command.
	@param args A string containing any arguments to the command.
	*/
	public static void doWhere(UserCon c, String args)
	{
		if (c.ch.position.equals("sleeping"))
		{
			c.sendln("You can't do that when you're "+c.ch.position+".");
			return;
		}

		c.sendln(Fmt.heading("")+"{x");
		c.sendln("}m     Current Room}M:}n "+c.ch.currentRoom.name);
		if (c.ch.currentArea() != null)
		{
			c.sendln("}m     Current Area}M:}n "+c.ch.currentArea().name);
			if (c.ch.currentArea().minLevel == Flags.minLevel && c.ch.currentArea().maxLevel == Flags.maxPlayableLevel)
				c.sendln("}m      Level Range}M:}n This area is suitable for all levels.");
			else
				c.sendln("}m      Level Range}M:}n "+c.ch.currentArea().minLevel+" - "+c.ch.currentArea().maxLevel);
			c.sendln("}m Area Description}M:}N");
			c.sendln(c.ch.currentArea().description);
			int otherInh = 0;
			for (CharData ch : allChars())
				if (ch.currentArea() == c.ch.currentArea() && c.ch != ch)
					if (!ch.currentRoom.flags.get("nowhere"))
						otherInh++;
			c.sendln("}mOther Inhabitants}M:}n "+otherInh);
			for (UserCon cs : conns)
				if (cs != c && cs.ch.currentArea() == c.ch.currentArea())
					if (!cs.ch.currentRoom.flags.get("nowhere"))
						if (Combat.canSee(c.ch, cs.ch))
							c.sendln(" }M- }n"+Fmt.seeName(c.ch, cs.ch)+"}M: }n"+cs.ch.currentRoom.name);
		}
		c.sendln(Fmt.heading("")+"{x");
	}
	
	public static void doList(UserCon c, String args)
	{
		for (CharData ch : mobs)
			if (ch.currentRoom == c.ch.currentRoom)
				if (ch.flags.get("shopkeeper") && Combat.canSee(c.ch, ch))
				{
					if (!Combat.canSee(ch, c.ch))
					{
						c.sendln(Fmt.cap(Fmt.seeName(c.ch, ch))+" can't see you. Go 'visible' before shopping here.");
						return;
					}
					ArrayList<ObjData> list = new ArrayList<ObjData>();
					for (ObjData o : ch.objects)
						if (!list.contains(o) && Combat.canSee(c.ch, o) && o.cost > 0)
						{
							if (o.type.equals("training"))
							{
								if (o.typeFlags.get("autouse"))
								{
									Skill chSkill = Skill.lookup(o.value1);
									if (chSkill == null)
										continue;
									if (chSkill.availAt(c.ch.charClass) == 0)
										continue;
								}
							}
							boolean listed = false;
							for (ObjData o2 : list)
								if (o2.shortName.equals(o.shortName) && o2.op == o.op)
								{
									listed = true;
									break;
								}
							if (!listed)
								list.add(o);
						}
					
					if (list.size() == 0)
					{
						c.sendln("There's nothing for sale here.");
						return;
					}

					if (args.length() > 0)
					{
						int tempInt = Fmt.getInt(args);
						if (tempInt < 1 || tempInt > list.size())
						{
							c.sendln("There aren't that many items for sale here.");
							return;
						}
						c.sendln(list.get(tempInt-1).showInfo());
						return;
					}
					
					c.sendln(Fmt.cap(Fmt.seeName(c.ch, ch))+" is selling...");
					c.sendln("}m(#)  Lvl  Price  Stocked?  Name");
					c.sendln(Fmt.heading(""));
					int ctr = 0;
					for (ObjData o : list)
					{
						ctr++;
						String stocked = "No";
						for (Lootgroup l : ch.sells)
							if (l.contents.get(o.op) != null)
								stocked = "Yes";
						String cc = "}n";
						int tempLevel = o.level;
						if (o.type.equals("training"))
							tempLevel = Skill.lookup(o.value1).availAt(c.ch.charClass);
						if (tempLevel > c.ch.level)
							cc = "{R";
						String firstColumns = cc+Fmt.rfit(""+ctr, 2)+" }M|"+cc+Fmt.rfit(""+tempLevel, 3)+" }M|"+cc+Fmt.rfit(""+o.cost, 7)+"}M | "+cc+Fmt.center(stocked, 4)+"}M ";
						if (o.type.equals("training"))
						{
							Skill targetSkill = Skill.lookup(o.value1);
							if (targetSkill != null)
							{
								if (c.ch.skillPercent(targetSkill) > 0)
									firstColumns = "   }M|  {RAlready Learned  }M ";
							}
						}
						c.sendln(firstColumns+"| }N"+Fmt.seeName(c.ch, o)+"{x");
					}
					return;
				}
		c.sendln("There's no shop here.");
		return;
	}
	
	public static void doBuy(UserCon c, String args)
	{
		for (CharData ch : mobs)
			if (ch.currentRoom == c.ch.currentRoom)
				if (ch.flags.get("shopkeeper") && Combat.canSee(c.ch, ch))
				{
					if (!Combat.canSee(ch, c.ch))
					{
						c.sendln(Fmt.cap(Fmt.seeName(c.ch, ch))+" can't see you. Go 'visible' before shopping here.");
						return;
					}
					if (ch.objects.size() == 0)
					{
						c.sendln("There's nothing for sale here.");
						return;
					}
					ArrayList<ObjData> list = new ArrayList<ObjData>();
					for (ObjData o : ch.objects)
						if (!list.contains(o) && Combat.canSee(c.ch, o) && o.cost > 0)
						{
							if (o.type.equals("training"))
							{
								if (o.typeFlags.get("autouse"))
								{
									Skill chSkill = Skill.lookup(o.value1);
									if (chSkill == null)
										continue;
									if (chSkill.availAt(c.ch.charClass) == 0)
										continue;
								}
							}
							boolean listed = false;
							for (ObjData o2 : list)
								if (o2.shortName.equals(o.shortName) && o2.op == o.op)
								{
									listed = true;
									break;
								}
							if (!listed)
								list.add(o);
						}
					
					int quantity = 0;
					String targetName = "";
					String quantCheck[] = args.split("\\*");
					if (Fmt.getInt(quantCheck[0]) > 0 && quantCheck.length == 2)
					{
						quantity = Fmt.getInt(quantCheck[0]);
						targetName = quantCheck[1];
					}
					else if ((quantity = Fmt.getInt(CommandHandler.getArg(args, 1))) > 0)
						targetName = CommandHandler.getLastArg(args, 2);
					else
						targetName = CommandHandler.getLastArg(args, 1);
					
					ObjData target = null;
					if (targetName.startsWith("#"))
					{
						int tempInt = Fmt.getInt(targetName.substring(1));
						if (tempInt < 1 || tempInt > list.size())
						{
							c.sendln("There aren't that many items for sale here.");
							return;
						}
						target = list.get(tempInt-1);
					}
					else
					{
						ObjData[] targets = Combat.findObj(c.ch, list, targetName);
						if (targets.length == 0)
						{
							c.sendln("Nothing in this shop matches that description.");
							return;
						}
						target = targets[0];
					}

					boolean stocked = false;
					for (Lootgroup l : ch.sells)
						if (l.contents.get(target.op) != null)
							stocked = true;

					if (quantity > 0)
					{
						if (!stocked)
						{
							c.sendln("You can't buy multiples of non-stocked (special) items.");
							return;
						}
						if (target.type.equals("training"))
						{
							c.sendln("You can't buy multiples of ability training.");
							return;
						}
						if (target.cost*quantity > c.ch.gold)
						{
							c.sendln("You can't afford that many of "+Fmt.seeName(c.ch, target)+".");
							c.sendln("It would cost "+(target.cost*quantity)+" gold, and you have "+c.ch.gold+".");
							return;
						}
						for (int ctr = 0; ctr < quantity; ctr++)
						{
							ObjData tempObj = new ObjData(target.op);
							tempObj.toChar(c.ch);
						}
						ch.gold += (target.cost*quantity)/5;
						c.ch.gold -= (target.cost*quantity);
						c.sendln("You buy "+quantity+" of "+Fmt.seeName(c.ch, target)+" for "+(target.cost*quantity)+" gold.");
						Fmt.actAround(c.ch, null, target, "$n buys "+quantity+" of $o.");
						return;
					}
					if (target.type.equals("training"))
					{
						if (target.typeFlags.get("autouse"))
						{
							Skill targetSkill = Skill.lookup(target.value1);
							if (targetSkill != null)
								if (targetSkill.availAt(c.ch.charClass) == 0
									|| targetSkill.availAt(c.ch.charClass) > c.ch.level)
								{
									c.sendln("You can't learn that "+targetSkill.type+" until level "+targetSkill.availAt(c.ch.charClass)+".");
									return;
								}
								if (c.ch.skillPercent(targetSkill) > 0)
								{
									c.sendln("You already know that skill.");
									return;
								}
						}
					}
					if (target.cost > c.ch.gold)
					{
						c.sendln("You can't afford "+Fmt.seeName(c.ch, target)+".");
						c.sendln("It would cost "+target.cost+" gold, and you have "+c.ch.gold+".");
						return;
					}
					ch.gold += target.cost/5;
					c.ch.gold -= target.cost;
					if (stocked)
					{
						ObjData tempObj = new ObjData(target.op);
						tempObj.toChar(c.ch);
					}
					else
					{
						target.toChar(c.ch);
					}
					c.sendln("You buy "+Fmt.seeName(c.ch, target)+" for "+target.cost+" gold.");
					Fmt.actAround(c.ch, null, target, "$n buys $o.");
					return;
				}
		c.sendln("There's no shop here.");
		return;
	}
	
	public static void doSell(UserCon c, String args)
	{
		for (CharData ch : mobs)
			if (ch.currentRoom == c.ch.currentRoom)
				if (ch.flags.get("shopkeeper") && Combat.canSee(c.ch, ch))
				{
					if (!Combat.canSee(ch, c.ch))
					{
						c.sendln(Fmt.cap(Fmt.seeName(c.ch, ch))+" can't see you. Go 'visible' before shopping here.");
						return;
					}
					
					ArrayList<ObjData> inv = new ArrayList<ObjData>();
					for (ObjData o : c.ch.objects)
						if (o.wearloc.equals("none"))
							inv.add(o);
					
					ObjData selling[] = Combat.findObj(c.ch, inv, args);
					
					if (selling.length == 0)
					{
						c.sendln("You don't have any objects matching that description.");
						return;
					}
					
					ArrayList<ObjData> soldObjects = new ArrayList<ObjData>();
					int startGold = c.ch.gold;
					for (ObjData o : selling)
					{
						if (o.cost < 2)
						{
							c.sendln(Fmt.cap(Fmt.seeName(c.ch, o))+" isn't valuable enough to sell.");
							continue;
						}
						if (o.decay > 0)
						{
							c.sendln("You can't sell objects with running decay timers.");
							continue;
						}
						
						boolean stocked = false;
						for (Lootgroup l : ch.sells)
							if (l.contents.get(o.op) != null)
								stocked = true;
						if (stocked || ch.objects.size() > 50)
							o.clearObjects();
						else
							o.toChar(ch);
						c.ch.gold += o.cost/2;
						if (ch.gold > o.cost/2)
							ch.gold -= o.cost/2;
						soldObjects.add(o);
					}

					for (UserCon cs : conns)
						if (cs.ch.currentRoom == c.ch.currentRoom)
							if (!cs.ch.position.equals("sleeping"))
							{
								String dropList[] = ObjData.listContents(cs.ch, soldObjects.toArray(new ObjData[0]), false);
								for (String s : dropList)
								{
									s = s.trim();
									if (cs == c)
										cs.sendln("You sell "+s+".");
									else
										cs.sendln(Fmt.seeName(cs.ch, c.ch)+" sells "+s+".");
								}
							}
					c.sendln("Total profit: {Y"+(c.ch.gold-startGold)+" gold{x");
					return;
				}
		c.sendln("There's no shop here.");
		return;
	}
	
	/*public static void doHunt(UserCon c, String args)
	{
		int targetInt = Fmt.getInt(args);
		Room targetRoom = Room.lookup(targetInt);
		if (targetRoom == null)
		{
			c.sendln("That room ID was not found.");
			return;
		}
		if (targetRoom == c.ch.currentRoom)
		{
			c.sendln("You're already here.");
			return;
		}
		String result = Room.pathTo(c.ch.currentRoom, targetRoom, false);
		if (result.length() == 0)
		{
			c.sendln("You can't get there from here.");
			return;
		}
		c.sendln(result);
	}*/
}