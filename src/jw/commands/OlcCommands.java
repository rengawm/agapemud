package jw.commands;
import java.util.*;

import jw.core.*;
import jw.data.*;
import static jw.core.MudMain.*;

/**
	The OlcCommand class contains all commands used to modify objects in the game.
*/
public class OlcCommands
{
	/**
	Create/edit/delete areas.
	
	@param c The user who entered this command.
	@param args A string containing any arguments to the command.
	*/
	public static void doAedit(UserCon c, String args)
	{
		String arg1 = CommandHandler.getArg(args, 1).toLowerCase();
		String arg2 = CommandHandler.getArg(args, 2).toLowerCase();
		String arg3 = CommandHandler.getLastArg(args, 3);
		
		// No argument - toggle OLC mode.
		if (arg1.length() == 0)
		{
			c.olcMode = "aedit "+c.ch.currentArea().id;
			c.sendln("OLC mode set: aedit");
			return;
		}
		
		// Create a new area.
		if ("create".startsWith(arg1) || (c.olcMode.length() > 0 && "create".equals(arg2)))
		{
			Database.newArea();
			c.sendln("New area created: 'A New Area'. ID: "+areas.get(areas.size()-1).id);
			return;
		}
		
		Area targetArea = Area.lookup(Fmt.getInt(arg1));
		if (Fmt.getInt(arg1) == 0 && !arg1.equals("0"))
		{
			targetArea = c.ch.currentArea();
			arg2 = CommandHandler.getArg(args, 1);
			arg3 = CommandHandler.getLastArg(args, 2);
		}
		
		if (targetArea == null)
		{
			c.sendln("That area ID was not found. Try }H'}hareas}H'{x to show all areas.");
			return;
		}
		if (arg2.length() == 0)
		{
			c.olcMode = "aedit "+targetArea.id;
			c.sendln("OLC mode set: aedit");
			return;
		}
		
		if ("info".startsWith(arg2))
		{
			String fString = "";
			for (String f : Flags.areaFlags)
				if (targetArea.flags.get(f))
					fString = fString+" "+f;
			if (fString.length() == 0)
				fString = " none";

			c.sendln(Fmt.heading(""));
			c.sendln("}m       Area ID}M:}n "+targetArea.id);
			c.sendln("}m          Name}M:}n "+targetArea.name);
			c.sendln("}m Minimum Level}M:}n "+targetArea.minLevel);
			c.sendln("}m Maximum Level}M:}n "+targetArea.maxLevel);
			c.sendln("}m         Flags}M:}n"+fString);
			c.sendln("}m    Room Start}M:}n "+targetArea.start);
			c.sendln("}m      Room End}M:}n "+targetArea.end);
			c.sendln("}m       Climate}M:}n "+targetArea.climate);
			c.sendln(Fmt.heading("")+"{x");
			c.sendln("}mDescription}M:^/}N"+targetArea.description);
			c.sendln(Fmt.heading("")+"{x");
			return;
		}

		// Set the name of an area.
		if ("name".startsWith(arg2))
		{
			if (arg3.length() > 0)
			{
				targetArea.name = arg3;
				Database.saveArea(targetArea);
				c.sendln("Area name set.");
				return;
			}
			c.sendln("The new area name can't be blank.");
			return;
		}
		
		// Set the description of an area.
		if ("description".startsWith(arg2))
		{
			c.sendln("Now editing description for: "+targetArea.name);
			c.editMode("AeditDescription", ""+targetArea.id, targetArea.description);
			return;
		}
		
		if ("flags".startsWith(arg2))
		{
			if (arg3.length() == 0)
			{
				c.sendln("Set what flags? See 'help areaflags' for a list.");
				return;
			}
			String[] flags = arg3.split(" ");
			for (String s : flags)
			{
				Boolean flagFound = false;
				s = s.trim().toLowerCase();
				if (s.length() == 0)
					continue;
				for (String fs : Flags.areaFlags)
				{
					if (fs.startsWith(s))
					{
						if (targetArea.flags.get(fs))
						{
							targetArea.flags.put(fs, false);
							c.sendln("'"+fs+"' removed.");
						}
						else
						{
							targetArea.flags.put(fs, true);
							c.sendln("'"+fs+"' set.");
						}
						flagFound = true;
						break;
					}
				}
				if (!flagFound)
				{
					c.sendln("'"+s+"' is not a valid area flag.");
					continue;
				}
			}
			Database.saveArea(targetArea);
			return;
		}

		if ("climate".startsWith(arg2))
		{
			if (arg3.length() > 0)
			{
				arg3 = arg3.toLowerCase();
				for (String t : Flags.areaClimates)
				{
					if (t.startsWith(arg3))
					{
						targetArea.climate = t;
						Database.saveArea(targetArea);
						c.sendln("Area climate set.");
						return;
					}
				}
				c.sendln("That's not a valid area climate. See 'help climates' for a list.");
				return;
			}
			c.sendln("Set the climate to what? See 'help climates' for a list.");
			return;
		}
		
		if ("minimum".startsWith(arg2))
		{
			int newNr = Fmt.getInt(arg3.trim());
			if (newNr < Flags.minLevel || newNr > Flags.maxPlayableLevel)
			{
				c.sendln("That's not a valid minimum level. ("+Flags.minLevel+" - "+Flags.maxPlayableLevel+")");
				return;
			}
			if (newNr > targetArea.maxLevel)
			{
				targetArea.maxLevel = newNr;
				c.sendln("The max level was lower than the new min level and has been adjusted.");
			}
			targetArea.minLevel = newNr;
			Database.saveArea(targetArea);
			c.sendln("New minimum level set.");
			return;
		}

		if ("maximum".startsWith(arg2))
		{
			int newNr = Fmt.getInt(arg3.trim());
			if (newNr < Flags.minLevel || newNr > Flags.maxPlayableLevel)
			{
				c.sendln("That's not a valid maximum level. ("+Flags.minLevel+" - "+Flags.maxPlayableLevel+")");
				return;
			}
			if (newNr < targetArea.minLevel)
			{
				targetArea.minLevel = newNr;
				c.sendln("The min level was higher than the new max level and has been adjusted.");
			}
			targetArea.maxLevel = newNr;
			Database.saveArea(targetArea);
			c.sendln("New maximum level set.");
			return;
		}

		if ("start".startsWith(arg2))
		{
			int newId = Fmt.getInt(arg3.trim());
			if (newId < 1)
			{
				c.sendln("That's not a valid room ID.");
				return;
			}
			for (Area a : areas)
				if (a != targetArea && a.start <= newId && a.end >= newId)
				{
					c.sendln("Another area includes that room ID already ("+a.name+").");
					return;
				}
			if (newId > targetArea.end)
			{
				targetArea.end = newId;
				c.sendln("The area's end ID was less than the new start ID, and has been adjusted.");
			}
			targetArea.start = newId;
			Database.saveArea(targetArea);
			c.sendln("New start ID set.");
			return;
		}

		if ("end".startsWith(arg2))
		{
			int newId = Fmt.getInt(arg3.trim());
			if (newId < 1)
			{
				c.sendln("That's not a valid room ID.");
				return;
			}
			for (Area a : areas)
				if (a != targetArea && a.start <= newId && a.end >= newId)
				{
					c.sendln("Another area includes that room ID already ("+a.name+").");
					return;
				}
			if (newId < targetArea.start)
			{
				c.sendln("The new end ID must be greater than the current start ID ("+targetArea.start+").");
				return;
			}
			targetArea.end = newId;
			Database.saveArea(targetArea);
			c.sendln("New end ID set.");
			return;
		}
		
		if (c.olcMode.startsWith("aedit"))
			c.olcMatched = false;
		else
			InfoCommands.doHelp(c, "aedit");
	}
	/**
	Receive text from the prompt and give it to the area the user was editing.
	
	@param c The user who is in edit mode.
	@param finishedText The entire contents of the finished editor.
	*/
	public static void prAeditDescription(UserCon c, String finishedText)
	{
		Area targetArea = Area.lookup(Fmt.getInt(c.promptTarget));

		if (targetArea != null)
		{
			targetArea.description = finishedText;
			Database.saveArea(targetArea);
			c.sendln("Area description saved.");
		}

		c.clearEditMode();
	}
	
	
	
	/**
	Create/edit/delete rooms.
	
	@param c The user who entered this command.
	@param args A string containing any arguments to the command.
	*/
	public static void doRedit(UserCon c, String args)
	{
		String arg1 = CommandHandler.getArg(args, 1).toLowerCase();
		String arg2 = CommandHandler.getArg(args, 2).toLowerCase();
		String arg3 = CommandHandler.getArg(args, 3).toLowerCase();
		
		// No argument - toggle OLC mode.
		if (arg1.length() == 0)
		{
			c.olcMode = "redit";
			c.sendln("OLC mode set: redit");
			return;
		}
		
		if ("info".startsWith(arg1))
		{
			c.sendln(Fmt.heading(""));
			c.sendln("}m   Room ID}M:}n "+c.ch.currentRoom.id);
			if (c.ch.currentArea() != null)
				c.sendln("}m      Area}M:}n "+c.ch.currentArea().name+" }M(}m#}n"+c.ch.currentArea().id+"}M)");
			c.sendln("}m      Name}M:}n "+c.ch.currentRoom.name);
			c.sendln("}m    Sector}M:}n "+c.ch.currentRoom.sector);
			c.sendln("}mOcc. Limit}M:}n "+c.ch.currentRoom.occLimit);

			String fString = "";
			for (String s : Flags.roomFlags)
				if (c.ch.currentRoom.flags.get(s) != null)
					if (c.ch.currentRoom.flags.get(s))
						fString = fString+s+" ";
			if (fString.length() == 0)
				fString = "none";
			else
				fString = fString.trim();
				
			c.sendln("}m     Flags}M:}n "+fString);

			c.sendln("^/}mEffects}M:");
			if (c.ch.currentRoom.effects.size() == 0)
				c.sendln("}m  None");
			for (Effect e : c.ch.currentRoom.effects)
			{
				if (e.duration == -1)
					c.sendln(" }M[ }n"+Fmt.fit(""+e.level, 4)+"}M] [ }ninfinite }M] }N"+e.name);
				else if (e.duration/600 >= 1)
					c.sendln(" }M[ }n"+Fmt.fit(""+e.level, 4)+"}M] [ }n"+Fmt.fit(e.duration/600+" min", 9)+"}M] }N"+e.name);
				else
					c.sendln(" }M[ }n"+Fmt.fit(""+e.level, 4)+"}M] [ }n"+Fmt.fit(e.duration/10+" sec", 9)+"}M] }N"+e.name);
				for (String s : e.statMods.keySet())
					c.sendln("                      - affects }n"+Flags.fullStatName(s)+" }Nby }n"+e.statMods.get(s)+"}N.");
			}

			c.sendln("^/}mExits}M:");
			if (c.ch.currentRoom.exits.size() == 0)
				c.sendln("}m  None");
			for (Exit e : c.ch.currentRoom.exits)
			{
				String flags = "";
				for (String s : Flags.exitFlags)
					if (e.flags.get(s))
						flags = flags+" "+s;
				flags = flags.trim();
				if (flags.length() == 0)
					flags = "none";
				c.sendln("}m  "+e.direction+" }Nto}n "+e.to.id+" }M- }mFlags: }M[}n"+flags+"}M] - }mKey: }M[}n"+e.key+"}M]");
				if (e.timer != 0)
					c.sendln("}m      Timer}M: }n"+e.timer+"{x");
				if (e.doorName.length() > 0)
					c.sendln("}m      Door Name}M: }n"+e.doorName+"{x");
			}
			
			c.sendln("^/}mRprogs}M:");
			if (c.ch.currentRoom.triggers.size() == 0)
				c.sendln("}m  None");
			int ctr = 0;
			for (Trigger t : c.ch.currentRoom.triggers)
			{
				ctr++;
				c.sendln("}m #}n"+ctr+"}M: }nRprog }m#}n"+t.rprog.id+" }M- }NTrigger: }n"+t.type+" }M- }NArgs: }n"+t.numArg+"}M/}n"+t.arg);
			}

			String edTemp = "";
			for (String s : c.ch.currentRoom.eds.keySet())
				edTemp = edTemp+" }M[}N"+s+"}M]";
			if (edTemp.length() == 0)
				edTemp = " none";
			c.sendln("^/ }mExtra Descs}M:}n"+edTemp);

			c.sendln(Fmt.heading(""));
			c.sendln("}mDescription}M:^/}N"+c.ch.currentRoom.description);
			c.sendln(Fmt.heading("")+"{x");
			return;
		}

		// Create a new room.
		if ("create".startsWith(arg1))
		{
			int newId = Fmt.getInt(arg2);
			if (newId < 1)
			{
				c.sendln("That's not a valid room ID.");
				return;
			}
			if (!Database.newRoom(newId))
			{
				c.sendln("A room with that ID already exists.");
				return;
			}
			if (c.olcMode.length() == 0)
			{
				c.olcMode = "redit";
				c.sendln("OLC mode set: redit");
			}

			Collections.sort(rooms);
			for (Area a : areas)
				if (a.start <= newId && a.end >= newId)
				{
					c.sendln("New room in '"+a.name+"' created: 'A New Room'.");
					return;
				}
			c.sendln("New room created: 'A New Room'.^/{RWARNING: {wThe room ID does not fit in any existing areas. It currently has no area.{x");
			return;
		}
		
		if ("addrprog".startsWith(arg1))
		{
			String arg4 = CommandHandler.getArg(args, 4);
			String arg5 = CommandHandler.getLastArg(args, 5);
			RoomProg targetRprog = RoomProg.lookup(Fmt.getInt(arg2));
			if (targetRprog == null)
			{
				c.sendln("That rprog ID was not found.");
				return;
			}
			
			for (String s : Flags.rprogTriggerTypes)
				if (s.equalsIgnoreCase(arg3))
				{
					Trigger newTrigger = new Trigger();
					newTrigger.type = s;
					newTrigger.arg = arg5;
					newTrigger.numArg = Fmt.getInt(arg4);
					newTrigger.rprog = targetRprog;
					c.ch.currentRoom.triggers.add(newTrigger);
					Database.saveRoom(c.ch.currentRoom);
					c.sendln("Rprog '"+targetRprog.name+"' added to this room.");
					return;
				}
			
			String temp = "";
			for (String s : Flags.rprogTriggerTypes)
				temp = temp+s+" ";
			c.sendln("That's not a valid rprog trigger.^/Valid triggers are: "+temp.trim());
			return;
		}
		
		if ("delrprog".startsWith(arg1))
		{
			int targetNr = Fmt.getInt(arg2);
			if (targetNr < 1 || targetNr > c.ch.currentRoom.triggers.size())
			{
				if (RoomProg.lookup(targetNr) != null)
				{
					for (Trigger t : c.ch.currentRoom.triggers)
					{
						if (t.rprog == RoomProg.lookup(targetNr))
						{
							c.ch.currentRoom.triggers.remove(t);
							c.sendln("Rprog trigger deleted.");
							Database.saveRoom(c.ch.currentRoom);
							return;
						}
					}
					c.sendln("That room doesn't have a trigger for that prog.");
					return;
				}
				c.sendln("There aren't that many rprog triggers in this room.");
				return;
			}
			c.ch.currentRoom.triggers.remove(targetNr-1);
			Database.saveRoom(c.ch.currentRoom);
			c.sendln("Rprog trigger deleted.");
			return;
		}
		
		if ("delete".startsWith(arg1))
		{
			Room targetRoom = Room.lookup(Fmt.getInt(arg2));
			if (targetRoom != null)
			{
				Database.deleteRoom(targetRoom);
				c.sendln("Room '"+targetRoom.name+"' deleted.{x");
				return;
			}
			c.sendln("That room ID was not found.");
			return;
		}
		
		if ("flags".startsWith(arg1))
		{
			arg2 = CommandHandler.getLastArg(args, 2);
			if (arg2.length() == 0)
			{
				c.sendln("Set what flags? See 'help roomflags' for a list.");
				return;
			}
			String[] flags = arg2.split(" ");
			for (String s : flags)
			{
				Boolean flagFound = false;
				s = s.trim().toLowerCase();
				if (s.length() == 0)
					continue;
				for (String fs : Flags.roomFlags)
				{
					if (fs.startsWith(s))
					{
						if (c.ch.currentRoom.flags.get(fs))
						{
							c.ch.currentRoom.flags.put(fs, false);
							c.sendln("'"+fs+"' removed from this room.");
						}
						else
						{
							c.ch.currentRoom.flags.put(fs, true);
							c.sendln("'"+fs+"' set in this room.");
						}
						flagFound = true;
						break;
					}
				}
				if (!flagFound)
				{
					c.sendln("'"+s+"' is not a valid room flag.");
					continue;
				}
			}
			Database.saveRoom(c.ch.currentRoom);
			return;
		}
		
		if ("effects".startsWith(arg1) || "affects".startsWith(arg1))
		{
			String arg4 = CommandHandler.getArg(args, 4).toLowerCase();
			if ("add".startsWith(arg2))
			{
				if (arg3.length() == 0)
				{
					c.sendln("Add what effect? See 'help roomeffects' for a list.");
					return;
				}
				if (arg4.length() == 0)
				{
					c.sendln("Add that effect at what level?");
					return;
				}
				String tempName = "";
				for (String fs : Flags.roomEffects)
					if (fs.startsWith(arg3.toLowerCase()))
					{
						tempName = fs;
						break;
					}
				if (tempName.length() == 0)
				{
					c.sendln("'"+arg3+"' is not a valid room effect.");
					return;
				}
				
				int tempLevel = Fmt.getInt(arg4);
				if (tempLevel < Flags.minLevel || tempLevel > Flags.maxLevel)
				{
					c.sendln("That's not a valid effect level. ("+Flags.minLevel+" - "+Flags.maxLevel+")");
					return;
				}
				for (Effect e : c.ch.currentRoom.effects)
				{
					if (e.name.equals(tempName))
					{
						c.sendln("'"+tempName+"' is already active in this room. Delete it first.");
						return;
					}
				}
				Effect newE = new Effect(tempName, tempLevel, -1);
				c.ch.currentRoom.effects.add(newE);
				c.sendln("'"+tempName+"' added.");
				Database.saveRoom(c.ch.currentRoom);
				return;
			}
			else if ("remove".startsWith(arg2) || "delete".startsWith(arg2))
			{
				if (arg3.length() == 0)
				{
					c.sendln("Remove what effect? See 'help roomeffects' for a list.");
					return;
				}
				String tempName = "";
				for (String fs : Flags.roomEffects)
					if (fs.startsWith(arg3.toLowerCase()))
					{
						tempName = fs;
						break;
					}
				if (tempName.length() == 0)
				{
					c.sendln("'"+arg3+"' is not a valid room effect.");
					return;
				}
				for (Effect e : c.ch.currentRoom.effects)
				{
					if (e.name.equals(tempName))
					{
						c.ch.currentRoom.effects.remove(e);
						c.sendln("'"+tempName+"' removed.");
						Database.saveRoom(c.ch.currentRoom);
						return;
					}
				}
			}
			InfoCommands.doHelp(c, "redit");
		}

		if ("link".startsWith(arg1) || "exit".startsWith(arg1))
		{
			String arg4[] = CommandHandler.getLastArg(args, 4).toLowerCase().split(" ");
			
			if (arg2.length() == 0)
			{
				InfoCommands.doHelp(c, "redit");
				return;
			}
			if (arg3.length() == 0)
			{
				InfoCommands.doHelp(c, "redit");
				return;
			}
			
			arg2 = Fmt.resolveDir(arg2);

			if ("flags".startsWith(arg3))
			{
				Exit targetExit = c.ch.currentRoom.matchExit(arg2);
				if (targetExit != null)
				{
					if (arg4.length == 0)
					{
						InfoCommands.doHelp(c, "redit exit flags");
						return;
					}
					c.sendln("Setting flags on this exit:");
					for (String flag : arg4)
						targetExit.toggleFlag(c, flag);
					Database.saveExit(c.ch.currentRoom, targetExit);
					Exit targetExit2 = targetExit.to.oppExit(c.ch.currentRoom, targetExit.direction);
					if ("link".startsWith(arg1) && targetExit2 != null)
					{
						c.sendln("Setting flags on the opposite exit:");
						for (String flag : arg4)
							targetExit2.toggleFlag(c, flag);
						Database.saveExit(targetExit.to, targetExit2);
					}
					return;
				}
				c.sendln("There is no exit by that name from this room.");
				return;
			}
			
			if ("key".startsWith(arg3))
			{
				if (arg4.length == 0)
				{
					c.sendln("Set the key to what object ID?");
					return;
				}
				Exit targetExit = c.ch.currentRoom.matchExit(arg2);
				if (targetExit != null)
				{
					int tempInt = Fmt.getInt(arg4[0]);
					ObjProto tempObj = ObjProto.lookup(tempInt);
					if ((tempObj != null && tempObj.type.equals("key")) || tempInt == 0)
					{
						targetExit.key = tempInt;
						c.sendln("Exit key set.");
						Database.saveExit(c.ch.currentRoom, targetExit);
						Exit targetExit2 = targetExit.to.oppExit(c.ch.currentRoom, targetExit.direction);
						if ("link".startsWith(arg1) && targetExit2 != null)
						{
							c.sendln("Key set on the opposite exit.");
							targetExit2.key = tempInt;
							Database.saveExit(targetExit.to, targetExit2);
						}
						return;
					}
					c.sendln("The exit key value must be a valid key object number, or 0 for no key.");
					return;
				}
				c.sendln("There is no exit by that name from this room.");
				return;
			}

			if ("timer".startsWith(arg3))
			{
				if (arg4.length == 0)
				{
					c.sendln("Set the timer to how many seconds? (-1 for never, 0 for default)");
					return;
				}
				Exit targetExit = c.ch.currentRoom.matchExit(arg2);
				if (targetExit != null)
				{
					int tempInt = Fmt.getInt(arg4[0]);
					if (tempInt >= -1)
					{
						targetExit.timer = tempInt;
						c.sendln("Exit timer set.");
						Database.saveExit(c.ch.currentRoom, targetExit);
						Exit targetExit2 = targetExit.to.oppExit(c.ch.currentRoom, targetExit.direction);
						if ("link".startsWith(arg1) && targetExit2 != null)
						{
							c.sendln("Timer set on the opposite exit.");
							targetExit2.timer = tempInt;
							Database.saveExit(targetExit.to, targetExit2);
						}
						return;
					}
					c.sendln("The timer must be a -1, 0, or a positive number.");
					return;
				}
				c.sendln("There is no exit by that name from this room.");
				return;
			}

			if ("doorname".startsWith(arg3))
			{
				Exit targetExit = c.ch.currentRoom.matchExit(arg2);
				if (targetExit != null)
				{
					if (arg4.length == 0)
					{
						targetExit.doorName = "";
						c.sendln("Exit door name cleared.");
					}
					else
					{
						targetExit.doorName = arg4[0].toLowerCase();
						c.sendln("Exit door name set.");
					}
					Database.saveExit(c.ch.currentRoom, targetExit);
					Exit targetExit2 = targetExit.to.oppExit(c.ch.currentRoom, targetExit.direction);
					if ("link".startsWith(arg1) && targetExit2 != null)
					{
						if (arg4.length == 0)
						{
							targetExit2.doorName = "";
							c.sendln("Door name cleared on the opposite exit.");
						}
						else
						{
							targetExit2.doorName = arg4[0].toLowerCase();
							c.sendln("Door name set on the opposite exit.");
						}
						Database.saveExit(targetExit.to, targetExit2);
					}
					return;
				}
				c.sendln("There is no exit by that name from this room.");
				return;
			}
			
			if ("delete".startsWith(arg3))
			{
				Exit targetExit = c.ch.currentRoom.matchExit(arg2);
				if (targetExit != null)
				{
					Exit targetExit2 = targetExit.to.oppExit(c.ch.currentRoom, targetExit.direction);
					if ("link".startsWith(arg1) && targetExit2 != null)
					{
						Database.deleteExit(targetExit2);
						targetExit.to.exits.remove(targetExit2);
						c.sendln("'"+targetExit2.direction+"' exit of previously linked room deleted.");
					}
					c.sendln("'"+targetExit.direction+"' exit deleted.");
					Database.deleteExit(targetExit);
					c.ch.currentRoom.exits.remove(targetExit);
					return;
				}
				c.sendln("There is no exit by that name from this room.");
				return;
			}
			else
			{
				Room targetRoom = Room.lookup(Fmt.getInt(arg3));
				if (targetRoom != null)
				{
					if (c.ch.currentRoom.matchExit(arg2) != null)
						doRedit(c, arg1+" "+arg2+" delete");

					Exit newExit = Database.newExit();
					newExit.to = targetRoom;
					newExit.from = c.ch.currentRoom;
					newExit.direction = arg2;
					c.ch.currentRoom.exits.add(newExit);
					Database.saveExit(c.ch.currentRoom, newExit);
					if ("link".startsWith(arg1))
					{
						if (targetRoom.oppExit(null, arg2) != null)
						{
							c.sendln("'"+targetRoom.oppExit(null, arg2).direction+"' exit of new linked room was set - it has been replaced with this one.");
							Database.deleteExit(targetRoom.oppExit(null, arg2));
							targetRoom.exits.remove(targetRoom.oppExit(null, arg2));
						}
						
						Exit newExit2 = Database.newExit();
						newExit2.to = c.ch.currentRoom;
						String oppExit = arg2;
						
						if ("north".startsWith(oppExit))
							oppExit = "south";
						else if ("east".startsWith(oppExit))
							oppExit = "west";
						else if ("south".startsWith(oppExit))
							oppExit = "north";
						else if ("west".startsWith(oppExit))
							oppExit = "east";
						else if ("up".startsWith(oppExit))
							oppExit = "down";
						else if ("down".startsWith(oppExit))
							oppExit = "up";
						newExit2.direction = oppExit;
						targetRoom.exits.add(newExit2);
						Database.saveExit(targetRoom, newExit2);
						c.sendln("'"+arg2+"' two-way exit set.");
					}
					else
						c.sendln("'"+arg2+"' one-way exit set.");
					return;
				}
				c.sendln("That room ID was not found.");
				return;
			}
		}
		
		// Convenience alias for create and link.
		if ("dig".startsWith(arg1))
		{
			if (arg2.length() == 0)
			{
				c.sendln("Syntax: }H'}hredit dig }H<}hdirection}H> [<}iroom id}H>]'{x");
				return;
			}
			int newId = Fmt.getInt(arg3);
			
			if (arg3.length() == 0)
			{
				if (c.ch.currentArea() == null)
				{
					c.sendln("You must specify a room ID when using dig if you're not in an area.");
					return;
				}
				for (int start = c.ch.currentArea().start; ; start++)
				{
					if (start > c.ch.currentArea().end)
					{
						c.sendln("The area you're editing is full. Try expanding its vnum range first.");
						return;
					}
					if (Room.lookup(start) == null)
					{
						newId = start;
						break;
					}
				}
			}
			if (newId < 1)
			{
				c.sendln("That's not a valid room ID.");
				return;
			}
			doRedit(c, "create "+newId);
			doRedit(c, "link "+arg2+" "+newId);
			return;
		}
		
		// List the ID and title of all rooms in the current area target.
		if ("list".startsWith(arg1))
		{
			ArrayList<String> ast = new ArrayList<String>();
			if (c.ch.currentArea() == null)
			{
				c.sendln("You're not currently in an area. Listing all rooms outside of any area's range:");
				boolean printRoom = true;
				for (Room r : rooms)
				{
					printRoom = true;
					for (Area a : areas)
					{
						if (a.start <= r.id && a.end >= r.id)
						{
							printRoom = false;
							break;
						}
						if (printRoom)
							ast.add("#}n"+r.id+"}M: }n"+r.name);
					}
				}
				c.sendln(Fmt.defaultTextColumns(ast.toArray(new String[0])));
				return;
			}
			
			c.sendln("Showing all rooms in "+c.ch.currentArea().name+":");
			for (Room r : rooms)
				if (c.ch.currentArea().start <= r.id && c.ch.currentArea().end >= r.id)
					ast.add("#}n"+r.id+"}M: }n"+r.name);
			c.sendln(Fmt.defaultTextColumns(ast.toArray(new String[0])));
			return;
		}

		// Set the name of a room.
		if ("name".startsWith(arg1))
		{
			arg2 = CommandHandler.getLastArg(args, 2);
			if (arg2.length() > 0)
			{
				c.ch.currentRoom.name = arg2;
				Database.saveRoom(c.ch.currentRoom);
				c.sendln("Room name set.");
				return;
			}
			c.sendln("The new room name can't be blank.");
			return;
		}

		// Set the name of a room.
		if ("sector".startsWith(arg1))
		{
			if (arg2.length() == 0)
			{
				c.sendln("Set the room sector to what? See 'help roomsectors' for a list.");
				return;
			}

			arg2 = arg2.toLowerCase();
			for (String s : Flags.roomSectors)
			{
				if (s.startsWith(arg2))
				{
					c.ch.currentRoom.sector = s;
					Database.saveRoom(c.ch.currentRoom);
					c.sendln("Room sector set.");
					return;
				}
			}
			c.sendln("That's not a valid sector type. See 'help roomsectors' for a list.");
			return;
		}

		if ("occlimit".startsWith(arg1))
		{
			int newLimit = Fmt.getInt(arg2);
			if (newLimit > 0 || (newLimit == 0 && arg2.equals("0")))
			{
				c.ch.currentRoom.occLimit = newLimit;
				Database.saveRoom(c.ch.currentRoom);
				c.sendln("Occupancy limit set.");
				return;
			}
			c.sendln("The occupancy limit must be 0 or greater.");
			return;
		}
		
		if ("ed".startsWith(arg1))
		{
			arg2 = CommandHandler.getArg(args, 2).toLowerCase();
			arg3 = CommandHandler.getLastArg(args, 3).toLowerCase();
			
			if (arg2.length() == 0)
			{
				c.sendln("Add/remove/edit an extra description?");
				return;
			}
			if (arg3.length() == 0)
			{
				c.sendln("Add/remove/edit what extra description?");
				return;
			}
			if ("remove".startsWith(arg2) || "delete".startsWith(arg2))
			{
				for (String s : c.ch.currentRoom.eds.keySet())
					if (s.startsWith(arg3))
					{
						c.ch.currentRoom.eds.remove(s);
						c.sendln("Extra description removed.");
						Database.saveRoom(c.ch.currentRoom);
						return;
					}
				c.sendln("That extra description was not found.");
				return;
			}
			if ("add".startsWith(arg2))
			{
				for (String s : c.ch.currentRoom.eds.keySet())
					if (s.equals(arg3))
					{
						c.sendln("That extra description is already added.");
						return;
					}
				c.sendln("Now editing extra description '"+arg3+"' for: "+c.ch.currentRoom.name);
				c.editMode("ReditExtradesc", c.ch.currentRoom.id+"/"+arg3, "");
				return;
			}
			if ("edit".startsWith(arg2))
			{
				for (String s : c.ch.currentRoom.eds.keySet())
					if (s.startsWith(arg3))
					{
						c.sendln("Now editing extra description '"+arg3+"' for: "+c.ch.currentRoom.name);
						c.editMode("OeditExtradesc", c.ch.currentRoom.id+"/"+arg3, c.ch.currentRoom.eds.get(s));
						return;
					}
				c.sendln("There's no matching extra description on this room.");
				return;
			}
			InfoCommands.doHelp(c, "redit");
			return;
		}

		// Set the description of a room.
		if ("description".startsWith(arg1))
		{
			c.sendln("Now editing description for: "+c.ch.currentRoom.name);
			c.editMode("ReditDescription", ""+c.ch.currentRoom.id, c.ch.currentRoom.description);
			return;
		}
		
		if (c.olcMode.equals("redit"))
			c.olcMatched = false;
		else
			InfoCommands.doHelp(c, "redit");
	}
	/**
	Receive text from the prompt and give it to the room the user was editing.
	
	@param c The user who is in edit mode.
	@param finishedText The entire contents of the finished editor.
	*/
	public static void prReditDescription(UserCon c, String finishedText)
	{
		Room targetRoom = Room.lookup(Fmt.getInt(c.promptTarget));

		if (targetRoom != null)
		{
			targetRoom.description = finishedText;
			Database.saveRoom(targetRoom);
			c.sendln("Room description saved.");
		}

		c.clearEditMode();
	}
	public static void prReditExtradesc(UserCon c, String finishedText)
	{
		String id = c.promptTarget.split("\\/", 2)[0];
		String edName = c.promptTarget.split("\\/", 2)[1];
		Room targetRoom = Room.lookup(Fmt.getInt(id));

		if (targetRoom != null)
		{
			targetRoom.eds.put(edName, finishedText);
			Database.saveRoom(targetRoom);
			c.sendln("Room extra description saved.");
		}

		c.clearEditMode();
	}
	
	
	
	/**
	Create/edit/delete help files.
	
	@param c The user who entered this command.
	@param args A string containing any arguments to the command.
	*/
	public static void doHedit(UserCon c, String args)
	{
		String arg1 = CommandHandler.getArg(args, 1).toLowerCase();
		String arg2 = CommandHandler.getArg(args, 2).toLowerCase();
		String arg3 = CommandHandler.getLastArg(args, 3);
		
		// No argument.
		if (arg1.length() == 0)
		{
			InfoCommands.doHelp(c, "hedit");
			return;
		}
		
		// Create a new help file after checking for identical titles.
		if ("create".startsWith(arg1) || (c.olcMode.length() > 0 && "create".equals(arg2)))
		{
			arg2 = CommandHandler.getLastArg(args, 2);
			if (c.olcMode.startsWith("hedit"))
				arg2 = arg3;
			
			if (arg2.length() == 0)
			{
				c.sendln("The name of the new help file can't be blank.");
				return;
			}
			
			for (Help h : helps)
				if (h.title.equals(arg2))
				{
					c.sendln("A help file with that title already exists.");
					return;
				}

			Help newHelp = new Help(Database.newHelp());
			newHelp.title = arg2;
			newHelp.text = "";
			helps.add(newHelp);
			Database.saveHelp(newHelp);
			c.sendln("New help file created: '"+arg2+"'. ID: "+newHelp.id);
			return;
		}
		
		// List the ID and title of all help files.
		if ("list".startsWith(arg1) || (c.olcMode.length() > 0 && "list".equals(arg2)))
		{
			ArrayList<String> ast = new ArrayList<String>();
			c.sendln("Showing all help files:");
			for (Help h : helps)
				ast.add("#}n"+h.id+"}M: }n"+h.title);
			c.sendln(Fmt.defaultTextColumns(ast.toArray(new String[0])));
			return;
		}
		
		Help targetHelp = null;
		targetHelp = Help.lookup(Fmt.getInt(arg1));
		
		if (targetHelp == null)
		{
			ArrayList<Help> showHelp = new ArrayList<Help>();
			int foundType = 0;
			// Look for an exact title match.
			for (Help h : helps)
				if (h.title.toLowerCase().equals(arg1) ||
					h.title.toLowerCase().startsWith(arg1+" ") ||
					h.title.toLowerCase().endsWith(" "+arg1) ||
					h.title.toLowerCase().contains(" "+arg1+" "))
					if (h.allowCheck(c))
					{
						showHelp.add(h);
						foundType = 1;
						break;
					}
	
			// If no exact title matches were found, look for similar titles.
			if (showHelp.size() == 0)
				for (Help h : helps)
					if (h.title.toLowerCase().contains(arg1))
						if (h.allowCheck(c))
						{
							showHelp.add(h);
							foundType = 2;
						}
			
			if (showHelp.size() > 1)
			{
				c.sendln("}IThere were multiple results for '"+arg1+"'.{x");
				c.sendln(Fmt.heading("")+"{x");
				for (Help h : showHelp)
					c.sendln(" }m#}n"+h.id+"}M: }I"+h.title+"{x");
				c.sendln(Fmt.heading("")+"{x");
				return;
			}
			else if (showHelp.size() == 1)
			{
				targetHelp = showHelp.get(0);
			}
		}
		
		
		if (targetHelp == null)
		{
			c.sendln("That help file was not found. Try }H'}hhedit list}H'{x to show all help files,^/or }H'}hhelp }H<}htitle}H>'{x to view the ID of an individual file.");
			return;
		}
		if (!targetHelp.allowCheck(c))
		{
			c.sendln("You don't have access to that help file.");
			return;
		}
		
		if (arg2.length() == 0)
		{
			InfoCommands.doHelp(c, "hedit");
			return;
		}
		
		if ("permissions".startsWith(arg2))
		{
			// "hedit <id> permissions" - show existing permission requirements for that help file.
			if (arg3.length() == 0)
			{
				if (targetHelp.permissions.size() == 0)
					c.sendln("'"+targetHelp.title+"' is currently available to all users. (No permissions set.)");
				else
				{
					c.send("'"+targetHelp.title+"' is available to:");
					for (String p : targetHelp.permissions)
						c.send(" "+p);
					c.sendln("");
				}
				return;
			}
			// "hedit <id> permissions <permission>" - toggle a permission entry on a help file.
			else
			{
				boolean validPermission = false;
				for (String p : Flags.permissionTypes)
					if (p.equals(arg3))
						validPermission = true;
				if (!validPermission)
				{
					c.sendln("'"+arg3+"' is not a valid permission setting.");
					c.send("Valid permissions are:");
					for (String p : Flags.permissionTypes)
						c.send(" "+p);
					c.sendln("");
					return;
				}

				for (String perm : targetHelp.permissions)
				{
					if (perm.equals(arg3))
					{
						targetHelp.permissions.remove(perm);
						Database.saveHelp(targetHelp);
						c.sendln("Access to '"+targetHelp.title+"' removed for '"+arg3+"'.");
						if (targetHelp.permissions.size() == 0)
							c.sendln("{R'"+targetHelp.title+"' is now available to all users.{x");
						else
						{
							c.send("'"+arg2+"' is now available to:");
							for (String p : targetHelp.permissions)
								c.send(" "+p);
							c.sendln("");
						}
						return;
					}
				}
				targetHelp.permissions.add(arg3);
				Database.saveHelp(targetHelp);
				c.send("'"+targetHelp.title+"' is now available to:");
				for (String p : targetHelp.permissions)
					c.send(" "+p);
				c.sendln("");
				return;
			}
		}
		// Edit title - go to heditTitle prompt.
		if ("title".startsWith(arg2))
		{
			for (Help h : helps)
			{
				if (h.title.equals(arg3) && h != targetHelp)
				{
					c.sendln("Error: A help file with that title already exists.");
					c.clearPrompt();
					return;
				}
			}
			String oldTitle = targetHelp.title;
			targetHelp.title = arg3;
			Database.saveHelp(targetHelp);
			c.sendln("The '"+oldTitle+"' help file is now titled: '"+arg3+"'.");
			return;
		}
		// Edit text - start a text editor.
		if ("text".startsWith(arg2))
		{
			c.sendln("Now editing text for: "+targetHelp.title);
			c.editMode("HeditText", ""+targetHelp.id, targetHelp.text);
			return;
		}
		// Delete - go to heditDelete prompt.
		if ("delete".startsWith(arg2))
		{
			c.prompt("HeditDelete", ""+targetHelp.id, "Are you sure you want to delete '"+targetHelp.title+"'? (y/n):");
			return;
		}

		InfoCommands.doHelp(c, "hedit");
	}
	/**
	Receive text from the prompt and give it to the help file the user was editing.
	
	@param c The user who is in edit mode.
	@param finishedText The entire contents of the finished editor.
	*/
	public static void prHeditText(UserCon c, String finishedText)
	{
		Help targetHelp = Help.lookup(Fmt.getInt(c.promptTarget));
		
		if (targetHelp != null)
		{
			targetHelp.text = finishedText;
			Database.saveHelp(targetHelp);
			c.sendln("Help text saved.");
		}

		c.clearEditMode();
	}
	/**
	Receive confirmation of a help file deletion - abort or go through with the deletion.
	
	@param c The user who is in edit mode.
	@param args The user's response to the prompt.
	*/
	public static void prHeditDelete(UserCon c, String args)
	{
		if (!args.toLowerCase().startsWith("y"))
		{
			c.sendln("Deletion cancelled.");
			return;
		}

		Help targetHelp = Help.lookup(Fmt.getInt(c.promptTarget));
		if (targetHelp != null)
		{
			Database.deleteHelp(targetHelp);
			helps.remove(targetHelp);
			c.sendln("Help file deleted.");
		}

		c.clearPrompt();
	}
	
	
	
	/**
	Create/edit/delete socials.
	
	@param c The user who entered this command.
	@param args A string containing any arguments to the command.
	*/
	public static void doSedit(UserCon c, String args)
	{
		String arg1 = CommandHandler.getArg(args, 1).toLowerCase();
		String arg2 = CommandHandler.getArg(args, 2).toLowerCase();
		String arg3 = CommandHandler.getLastArg(args, 3);
		
		// No argument - toggle OLC mode.
		if (arg1.length() == 0)
		{
			InfoCommands.doHelp(c, "sedit");
			return;
		}
		
		// Create a new social.
		if ("create".startsWith(arg1) || (c.olcMode.length() > 0 && "create".equals(arg2)))
		{
			if (arg2.length() == 0)
			{
				c.sendln("Create what social?");
				return;
			}

			arg2 = arg2.toLowerCase();
			for (Social s : socials)
				if (s.name.equals(arg2))
				{
					c.sendln("That social already exists.");
					return;
				}
			
			Database.newSocial(arg2);
			Collections.sort(socials);
			c.sendln("New social created: '"+arg2+"'.");
			return;
		}
		
		Social targetSocial = Social.lookup(arg1);
		if (targetSocial == null)
		{
			c.sendln("That social was not found.");
			return;
		}
		if (arg2.length() == 0)
		{
			c.olcMode = "sedit "+targetSocial.name;
			c.sendln("OLC mode set: sedit");
			return;
		}
		
		if ("info".startsWith(arg2))
		{
			c.sendln(Fmt.heading(""));
			c.sendln("}m   Name}M:}N "+targetSocial.name);
			c.sendln("^/}m CNoArg}M:}N "+targetSocial.cNoArg);
			c.sendln("}m ONoArg}M:}N "+targetSocial.oNoArg);
			c.sendln("^/}m CFound}M:}N "+targetSocial.cFound);
			c.sendln("}m OFound}M:}N "+targetSocial.oFound);
			c.sendln("}m VFound}M:}N "+targetSocial.vFound);
			c.sendln("^/}m  CSelf}M:}N "+targetSocial.cSelf);
			c.sendln("}m  OSelf}M:}N "+targetSocial.oSelf);
			c.sendln(Fmt.heading("")+"{x");
			return;
		}
		
		// Set the name of a social.
		if ("name".startsWith(arg2))
		{
			if (arg3.length() > 0)
			{
				if (Social.lookup(arg3) != null && !targetSocial.name.equals(arg3))
				{
					c.sendln("Another social is already using that name.");
					return;
				}
				targetSocial.name = arg3;
				Database.saveSocial(targetSocial);
				Collections.sort(socials);
				c.sendln("Social name set.");
				return;
			}
			c.sendln("The new social name can't be blank.");
			return;
		}

		if ("cnoarg".startsWith(arg2))
		{
			if (arg3.length() > 0)
			{
				targetSocial.cNoArg = arg3;
				Database.saveSocial(targetSocial);
				c.sendln("New cnoarg string set.");
				return;
			}
			c.sendln("The new cnoarg string can't be blank.");
			return;
		}

		if ("onoarg".startsWith(arg2))
		{
			if (arg3.length() > 0)
			{
				targetSocial.oNoArg = arg3;
				Database.saveSocial(targetSocial);
				c.sendln("New onoarg string set.");
				return;
			}
			c.sendln("The new onoarg string can't be blank.");
			return;
		}

		if ("cfound".startsWith(arg2))
		{
			if (arg3.length() > 0)
			{
				targetSocial.cFound = arg3;
				Database.saveSocial(targetSocial);
				c.sendln("New cfound string set.");
				return;
			}
			c.sendln("The new cfound string can't be blank.");
			return;
		}

		if ("ofound".startsWith(arg2))
		{
			if (arg3.length() > 0)
			{
				targetSocial.oFound = arg3;
				Database.saveSocial(targetSocial);
				c.sendln("New ofound string set.");
				return;
			}
			c.sendln("The new ofound string can't be blank.");
			return;
		}

		if ("vfound".startsWith(arg2))
		{
			if (arg3.length() > 0)
			{
				targetSocial.vFound = arg3;
				Database.saveSocial(targetSocial);
				c.sendln("New vfound string set.");
				return;
			}
			c.sendln("The new vfound string can't be blank.");
			return;
		}

		if ("cself".startsWith(arg2))
		{
			if (arg3.length() > 0)
			{
				targetSocial.cSelf = arg3;
				Database.saveSocial(targetSocial);
				c.sendln("New cself string set.");
				return;
			}
			c.sendln("The new cself string can't be blank.");
			return;
		}

		if ("oself".startsWith(arg2))
		{
			if (arg3.length() > 0)
			{
				targetSocial.oSelf = arg3;
				Database.saveSocial(targetSocial);
				c.sendln("New oself string set.");
				return;
			}
			c.sendln("The new oself string can't be blank.");
			return;
		}
		
		if (c.olcMode.startsWith("sedit"))
			c.olcMatched = false;
		else
			InfoCommands.doHelp(c, "sedit");
	}



	public static void doSkedit(UserCon c, String args)
	{
		String arg1 = CommandHandler.getArg(args, 1).toLowerCase();
		String arg2 = CommandHandler.getArg(args, 2).toLowerCase();
		String arg3 = CommandHandler.getArg(args, 3);
		
		if (arg1.length() == 0)
		{
			InfoCommands.doHelp(c, "skedit");
			return;
		}
		
		Skill targetSkill = null;
		for (Skill s : skills)
			if (s.name.startsWith(arg1))
			{
				targetSkill = s;
				break;
			}
		
		if (targetSkill == null)
		{
			c.sendln("That's not a valid skill or spell name.");
			return;
		}
		
		if (arg2.length() == 0 || "info".startsWith(arg2))
		{
			String fString = "";
			for (String fs : Flags.skillFlags)
				if (targetSkill.flags.get(fs))
					fString = fString+" "+fs;
			if (fString.length() == 0)
				fString = "none";
			else
				fString = fString.trim();

			c.sendln(Fmt.heading(""));
			c.sendln("}m        Name}M:}n "+targetSkill.name);
			c.sendln("}m        Type}M:}n "+targetSkill.type);
			c.sendln("}m   Base Cost}M:}n "+targetSkill.cost);
			c.sendln("}m   Use Delay}M:}n "+(targetSkill.useDelay/10.0)+" sec");
			c.sendln("}m    Cooldown}M:}n "+targetSkill.cooldown+" sec");
			c.sendln("}m Target Type}M:}n "+targetSkill.targetType);
			c.sendln("}mTarget Flags}M:}n "+fString);
			c.sendln(Fmt.heading("")+"{x");
			return;
		}
		
		if ("cost".startsWith(arg2))
		{
			targetSkill.cost = Fmt.getInt(arg3);
			Database.saveSkill(targetSkill);
			c.sendln("Skill base cost set.");
			return;
		}

		if ("delay".startsWith(arg2))
		{
			targetSkill.useDelay = (int)(Fmt.getDouble(arg3)*10);
			Database.saveSkill(targetSkill);
			c.sendln("Skill use delay set.");
			return;
		}

		if ("cooldown".startsWith(arg2))
		{
			targetSkill.cooldown = Fmt.getInt(arg3);
			Database.saveSkill(targetSkill);
			c.sendln("Skill cooldown set.");
			return;
		}
		
		if ("flags".startsWith(arg2))
		{
			String setFlags[] = arg3.split(" ");
			if (setFlags.length == 0)
			{
				c.sendln("Set what flags on that skill? (See 'help skillflags' for a list)");
				return;
			}
			for (String s : setFlags)
			{
				Boolean flagFound = false;
				s = s.trim().toLowerCase();
				if (s.length() == 0)
					continue;
				for (String fs : Flags.skillFlags)
				{
					if (fs.startsWith(s))
					{
						if (targetSkill.flags.get(fs))
						{
							targetSkill.flags.put(fs, false);
							c.sendln("'"+fs+"' removed.");
						}
						else
						{
							targetSkill.flags.put(fs, true);
							c.sendln("'"+fs+"' set.");
						}
						flagFound = true;
						break;
					}
				}
				if (!flagFound)
				{
					c.sendln("'"+s+"' is not a valid skill flag. (See 'help skillflags' for a list)");
					continue;
				}
				Database.saveSkill(targetSkill);
				return;
			}
		}

		if ("type".startsWith(arg2))
		{
			if (arg3.length() == 0)
			{
				c.sendln("Set the skill type to what? (See 'help skilltypes' for a list)");
				return;
			}
			
			arg3 = arg3.toLowerCase();
			for (String fs : Flags.skillTypes)
				if (fs.startsWith(arg3))
				{
					targetSkill.targetType = fs;
					c.sendln("Skill type set to '"+fs+"'.");
					Database.saveSkill(targetSkill);
					return;
				}

			c.sendln("'"+arg3+"' is not a valid skill type. (See 'help skilltypes' for a list)");
			return;
		}
		
		if ("avail".startsWith(arg2))
		{
			String arg4 = CommandHandler.getArg(args, 4);
			if (arg3.length() == 0 || arg4.length() == 0)
			{
				c.sendln("Make that ability available to what class at what level?");
				c.sendln("Syntax: }H'}hskedit }H<}iskill name}H> }havail }H<}iclass}H> <}ilevel}H>'{x");
				return;
			}
			
			CharClass targetClass = CharClass.lookup(Fmt.getInt(arg3));
			if (targetClass == null)
				targetClass = CharClass.lookup(arg3);
			if (targetClass == null)
			{
				c.sendln("That's not a valid class name or ID.");
				return;
			}
			
			int tempInt = Fmt.getInt(arg4);
			if ((tempInt == 0 && !arg4.equals("0")) || tempInt > Flags.maxPlayableLevel || tempInt < 0)
			{
				c.sendln("That's not a valid level.");
				return;
			}
			
			if (tempInt == 0)
			{
				targetSkill.avail.remove(targetClass);
				c.sendln(Fmt.cap(targetSkill.name)+" is no longer available to the "+Fmt.cap(targetClass.name)+" class.");
			}
			else
			{
				targetSkill.avail.put(targetClass, tempInt);
				c.sendln(Fmt.cap(targetSkill.name)+" is now available to the "+Fmt.cap(targetClass.name)+" class at level "+tempInt+".");
			}
			Database.saveSkill(targetSkill);
			return;
		}
		
		InfoCommands.doHelp(c, "skedit");
	}
	
	
	
	/**
	Create/edit/delete races.
	
	@param c The user who entered this command.
	@param args A string containing any arguments to the command.
	*/
	public static void doRcedit(UserCon c, String args)
	{
		String arg1 = CommandHandler.getArg(args, 1).toLowerCase();
		String arg2 = CommandHandler.getArg(args, 2).toLowerCase();
		String arg3 = CommandHandler.getLastArg(args, 3);
		
		if (arg1.length() == 0)
		{
			InfoCommands.doHelp(c, "rcedit");
			return;
		}
		
		// Create a new race.
		if ("create".startsWith(arg1) || (c.olcMode.length() > 0 && "create".equals(arg2)))
		{
			Database.newRace();
			c.sendln("New race created: 'NewRace'. ID: "+races.get(races.size()-1).id);
			return;
		}
		
		if ("list".startsWith(arg1) || (c.olcMode.length() > 0 && "list".equals(arg2)))
		{
			c.sendln("Existing races:");
			ArrayList<String> rcs = new ArrayList<String>();
			for (Race r : races)
				rcs.add("#}n"+r.id+"}M: }n"+r.name);
			c.sendln(Fmt.defaultTextColumns(rcs.toArray(new String[0])));
			return;
		}
		
		Race targetRace = Race.lookup(Fmt.getInt(arg1));
		if (targetRace == null)
		{
			c.sendln("That race ID was not found.");
			return;
		}
		if (arg2.length() == 0)
		{
			c.olcMode = "rcedit "+targetRace.id;
			c.sendln("OLC mode set: rcedit");
			return;
		}
		
		if ("info".startsWith(arg2))
		{
			c.sendln(Fmt.heading(""));
			c.sendln("}m        Name}M:}n "+targetRace.name);
			c.sendln("}m      Plural}M:}n "+targetRace.plural);
			c.sendln("}m   Racegroup}M:}n "+targetRace.racegroup);
			if (targetRace.movement.length() > 0)
				c.sendln("}m    Movement}M:}n "+targetRace.movement);
			if (targetRace.hitname.length() > 0)
				c.sendln("}m    Hit Name}M:}n "+targetRace.hitname);
			c.sendln("^/}M"+c.repeat("-", 31)+" }mBase Stats }M"+c.repeat("-", 32));
			c.sendln("}m                  Strength}M:}n "+Fmt.fit(""+targetRace.baseStr, 10)+"}m     Frost Resist}M:}n "+targetRace.baseFrost);
			c.sendln("}m                 Dexterity}M:}n "+Fmt.fit(""+targetRace.baseDex, 10)+"}m      Fire Resist}M:}n "+targetRace.baseFire);
			c.sendln("}m              Constitution}M:}n "+Fmt.fit(""+targetRace.baseCon, 10)+"}m Lightning Resist}M:}n "+targetRace.baseLightning);
			c.sendln("}m              Intelligence}M:}n "+Fmt.fit(""+targetRace.baseInt, 10)+"}m      Acid Resist}M:}n "+targetRace.baseAcid);
			c.sendln("}m                  Charisma}M:}n "+Fmt.fit(""+targetRace.baseCha, 10)+"}m      Good Resist}M:}n "+targetRace.baseGood);
			c.sendln("}m               Slash Armor}M:}n "+Fmt.fit(""+targetRace.baseSlash, 10)+"}m      Evil Resist}M:}n "+targetRace.baseEvil);
			c.sendln("}m                Bash Armor}M:}n "+targetRace.baseBash);
			c.sendln("}m              Pierce Armor}M:}n "+targetRace.basePierce);
			c.sendln(Fmt.heading(""));
			c.sendln("}mDescription}M:^/}N"+targetRace.description);
			c.sendln(Fmt.heading("")+"{x");
			return;
		}
		
		// Set the name of a race.
		if ("name".startsWith(arg2))
		{
			if (arg3.length() > 0)
			{
				targetRace.name = arg3;
				Database.saveRace(targetRace);
				c.sendln("Race name set.");
				return;
			}
			c.sendln("The new race name can't be blank.");
			return;
		}

		// Set the plural name of a race.
		if ("plural".startsWith(arg2))
		{
			if (arg3.length() > 0)
			{
				targetRace.plural = arg3;
				Database.saveRace(targetRace);
				c.sendln("Race plural name set.");
				return;
			}
			c.sendln("The new race plural name can't be blank.");
			return;
		}

		// Set the racegroup name of a race. This also makes the race playable.
		if ("racegroup".startsWith(arg2))
		{
			if (arg3.length() > 0)
			{
				targetRace.racegroup = arg3;
				Database.saveRace(targetRace);
				c.sendln("Racegroup name set. (Now available to new characters.)");
				return;
			}
			targetRace.racegroup = "";
			Database.saveRace(targetRace);
			c.sendln("Racegroup name cleared. (No longer available to new characters.)");
			return;
		}
		
		// Set the custom movement verb of a race.
		if ("movement".startsWith(arg2))
		{
			if (arg3.length() > 0)
			{
				boolean found = false;
				for (String m : Flags.charMovements)
					if (m.startsWith(arg3.toLowerCase()))
					{
						found = true;
						arg3 = m;
					}
				
				if (!found)
				{
					c.sendln("That's not a valid movement verb. See 'help movementverbs'.");
					return;
				}
				
				targetRace.movement = arg3;
				Database.saveRace(targetRace);
				c.sendln("Race movement verb set.");
				return;
			}
			targetRace.movement = "";
			Database.saveRace(targetRace);
			c.sendln("Custom movement verb cleared. The race will use the default movement verb.");
			return;
		}

		// Set the custom unarmed hit name of a race.
		if ("hitname".startsWith(arg2))
		{
			if (arg3.length() > 0)
			{
				boolean found = false;
				for (String m : Flags.hitTypes)
					if (m.startsWith(arg3.toLowerCase()))
					{
						found = true;
						arg3 = m;
					}
				
				if (!found)
				{
					c.sendln("That's not a valid hitname. See 'help hittypes'.");
					return;
				}
				
				targetRace.hitname = arg3;
				Database.saveRace(targetRace);
				c.sendln("Race hit name set.");
				return;
			}
			targetRace.hitname = "";
			Database.saveRace(targetRace);
			c.sendln("Custom hit name cleared. The race will use the default hit name.");
			return;
		}
		
		if ("strength".startsWith(arg2))
		{
			if (arg3.length() == 0)
			{
				c.sendln("Set the race base strength to what?");
				return;
			}
			int stat = Fmt.getInt(arg3);
			if (stat < 0 || (stat == 0 && !arg3.equals("0")))
			{
				c.sendln("That's not a valid base strength value.");
				return;
			}
			targetRace.baseStr = stat;
			Database.saveRace(targetRace);
			c.sendln("Race base strength set.");
			return;
		}

		if ("dexterity".startsWith(arg2))
		{
			if (arg3.length() == 0)
			{
				c.sendln("Set the race base dexterity to what?");
				return;
			}
			int stat = Fmt.getInt(arg3);
			if (stat < 0 || (stat == 0 && !arg3.equals("0")))
			{
				c.sendln("That's not a valid base dexterity value.");
				return;
			}
			targetRace.baseDex = stat;
			Database.saveRace(targetRace);
			c.sendln("Race base dexterity set.");
			return;
		}

		if ("constitution".startsWith(arg2))
		{
			if (arg3.length() == 0)
			{
				c.sendln("Set the race base constitution to what?");
				return;
			}
			int stat = Fmt.getInt(arg3);
			if (stat < 0 || (stat == 0 && !arg3.equals("0")))
			{
				c.sendln("That's not a valid base constitution value.");
				return;
			}
			targetRace.baseCon = stat;
			Database.saveRace(targetRace);
			c.sendln("Race base constitution set.");
			return;
		}

		if ("intelligence".startsWith(arg2))
		{
			if (arg3.length() == 0)
			{
				c.sendln("Set the race base intelligence to what?");
				return;
			}
			int stat = Fmt.getInt(arg3);
			if (stat < 0 || (stat == 0 && !arg3.equals("0")))
			{
				c.sendln("That's not a valid base intelligence value.");
				return;
			}
			targetRace.baseInt = stat;
			Database.saveRace(targetRace);
			c.sendln("Race base intelligence set.");
			return;
		}

		if ("charisma".startsWith(arg2))
		{
			if (arg3.length() == 0)
			{
				c.sendln("Set the race base charisma to what?");
				return;
			}
			int stat = Fmt.getInt(arg3);
			if (stat < 0 || (stat == 0 && !arg3.equals("0")))
			{
				c.sendln("That's not a valid base charisma value.");
				return;
			}
			targetRace.baseCha = stat;
			Database.saveRace(targetRace);
			c.sendln("Race base charisma set.");
			return;
		}

		if ("slash".startsWith(arg2))
		{
			if (arg3.length() == 0)
			{
				c.sendln("Set the race base slash armor to what?");
				return;
			}
			int stat = Fmt.getInt(arg3);
			if (stat < 0 || (stat == 0 && !arg3.equals("0")))
			{
				c.sendln("That's not a valid base slash armor value.");
				return;
			}
			targetRace.baseSlash = stat;
			Database.saveRace(targetRace);
			c.sendln("Race base slash armor set.");
			return;
		}

		if ("bash".startsWith(arg2))
		{
			if (arg3.length() == 0)
			{
				c.sendln("Set the race base bash armor to what?");
				return;
			}
			int stat = Fmt.getInt(arg3);
			if (stat < 0 || (stat == 0 && !arg3.equals("0")))
			{
				c.sendln("That's not a valid base bash armor value.");
				return;
			}
			targetRace.baseBash = stat;
			Database.saveRace(targetRace);
			c.sendln("Race base bash armor set.");
			return;
		}

		if ("pierce".startsWith(arg2))
		{
			if (arg3.length() == 0)
			{
				c.sendln("Set the race base pierce armor to what?");
				return;
			}
			int stat = Fmt.getInt(arg3);
			if (stat < 0 || (stat == 0 && !arg3.equals("0")))
			{
				c.sendln("That's not a valid base pierce armor value.");
				return;
			}
			targetRace.basePierce = stat;
			Database.saveRace(targetRace);
			c.sendln("Race base pierce armor set.");
			return;
		}

		if ("frost".startsWith(arg2))
		{
			if (arg3.length() == 0)
			{
				c.sendln("Set the race base frost resistance to what?");
				return;
			}
			int stat = Fmt.getInt(arg3);
			if (stat < 0 || (stat == 0 && !arg3.equals("0")))
			{
				c.sendln("That's not a valid base frost resistance value.");
				return;
			}
			targetRace.baseFrost = stat;
			Database.saveRace(targetRace);
			c.sendln("Race base frost resistance set.");
			return;
		}

		if ("fire".startsWith(arg2))
		{
			if (arg3.length() == 0)
			{
				c.sendln("Set the race base fire resistance to what?");
				return;
			}
			int stat = Fmt.getInt(arg3);
			if (stat < 0 || (stat == 0 && !arg3.equals("0")))
			{
				c.sendln("That's not a valid base fire resistance value.");
				return;
			}
			targetRace.baseFire = stat;
			Database.saveRace(targetRace);
			c.sendln("Race base fire resistance set.");
			return;
		}

		if ("lightning".startsWith(arg2))
		{
			if (arg3.length() == 0)
			{
				c.sendln("Set the race base lightning resistance to what?");
				return;
			}
			int stat = Fmt.getInt(arg3);
			if (stat < 0 || (stat == 0 && !arg3.equals("0")))
			{
				c.sendln("That's not a valid base lightning resistance value.");
				return;
			}
			targetRace.baseLightning = stat;
			Database.saveRace(targetRace);
			c.sendln("Race base lightning resistance set.");
			return;
		}

		if ("acid".startsWith(arg2))
		{
			if (arg3.length() == 0)
			{
				c.sendln("Set the race base acid resistance to what?");
				return;
			}
			int stat = Fmt.getInt(arg3);
			if (stat < 0 || (stat == 0 && !arg3.equals("0")))
			{
				c.sendln("That's not a valid base acid resistance value.");
				return;
			}
			targetRace.baseAcid = stat;
			Database.saveRace(targetRace);
			c.sendln("Race base acid resistance set.");
			return;
		}

		if ("good".startsWith(arg2))
		{
			if (arg3.length() == 0)
			{
				c.sendln("Set the race base good resistance to what?");
				return;
			}
			int stat = Fmt.getInt(arg3);
			if (stat < 0 || (stat == 0 && !arg3.equals("0")))
			{
				c.sendln("That's not a valid base good resistance value.");
				return;
			}
			targetRace.baseGood = stat;
			Database.saveRace(targetRace);
			c.sendln("Race base good resistance set.");
			return;
		}

		if ("evil".startsWith(arg2))
		{
			if (arg3.length() == 0)
			{
				c.sendln("Set the race base evil resistance to what?");
				return;
			}
			int stat = Fmt.getInt(arg3);
			if (stat < 0 || (stat == 0 && !arg3.equals("0")))
			{
				c.sendln("That's not a valid base evil resistance value.");
				return;
			}
			targetRace.baseEvil = stat;
			Database.saveRace(targetRace);
			c.sendln("Race base evil resistance set.");
			return;
		}
		
		// Set the description of a race.
		if ("description".startsWith(arg2))
		{
			c.sendln("Now editing description for: "+targetRace.name);
			c.editMode("RceditDescription", ""+targetRace.id, targetRace.description);
			return;
		}
		
		if (c.olcMode.startsWith("rcedit"))
			c.olcMatched = false;
		else
			InfoCommands.doHelp(c, "rcedit");
	}
	/**
	Receive text from the prompt and give it to the race the user was editing.
	
	@param c The user who is in edit mode.
	@param finishedText The entire contents of the finished editor.
	*/
	public static void prRceditDescription(UserCon c, String finishedText)
	{
		Race targetRace = Race.lookup(Fmt.getInt(c.promptTarget));

		if (targetRace != null)
		{
			targetRace.description = finishedText;
			Database.saveRace(targetRace);
			c.sendln("Race description saved.");
		}

		c.clearEditMode();
	}



	public static void doLedit(UserCon c, String args)
	{
		String arg1 = CommandHandler.getArg(args, 1).toLowerCase();
		String arg2 = CommandHandler.getArg(args, 2).toLowerCase();
		String arg3 = CommandHandler.getLastArg(args, 3);
		
		if (arg1.length() == 0)
		{
			InfoCommands.doHelp(c, "ledit");
			return;
		}

		// Create a new lootgroup.
		if ("create".startsWith(arg1) || (c.olcMode.length() > 0 && "create".equals(arg2)))
		{
			int newId = Fmt.getInt(arg2);
			if (c.olcMode.length() > 0)
				newId = Fmt.getInt(arg3);
			if (newId < 1)
			{
				c.sendln("That's not a valid lootgroup ID.");
				return;
			}
			if (!Database.newLootgroup(newId))
			{
				c.sendln("A lootgroup with that ID already exists.");
				return;
			}
			c.olcMode = "ledit "+newId;
			c.sendln("OLC mode set: ledit");
			for (Area a : areas)
				if (a.start <= newId && a.end >= newId)
				{
					c.sendln("New lootgroup in '"+a.name+"' created: 'A New Lootgroup'.");
					return;
				}
			c.sendln("New lootgroup created: 'A New Lootgroup'.^/{RWARNING: {wThe lootgroup ID does not fit in any existing areas. It currently has no area.{x");
			return;
		}

		// List the ID and title of all lootgroups in the current area.
		if ("list".startsWith(arg1) || (c.olcMode.length() > 0 && "list".equals(arg2)))
		{
			ArrayList<String> ast = new ArrayList<String>();
			if (c.ch.currentArea() == null)
			{
				c.sendln("You're not currently in an area.^/"+
						"Listing all lootgroups outside of any area's range:");
				boolean printLg = true;
				for (Lootgroup l : lootgroups)
				{
					printLg = true;
					for (Area a : areas)
					{
						if (a.start <= l.id && a.end >= l.id)
						{
							printLg = false;
							break;
						}
						if (printLg)
							ast.add("#}n"+l.id+"}M: }n"+l.name);
					}
				}
				c.sendln(Fmt.defaultTextColumns(ast.toArray(new String[0])));
				return;
			}
			
			c.sendln("Showing all lootgroups in "+c.ch.currentArea().name+":");
			for (Lootgroup l : lootgroups)
				if (c.ch.currentArea().start <= l.id && c.ch.currentArea().end >= l.id)
					ast.add("#}n"+l.id+"}M: }n"+l.name);
			c.sendln(Fmt.defaultTextColumns(ast.toArray(new String[0])));
			return;
		}
		
		Lootgroup targetLg = Lootgroup.lookup(Fmt.getInt(arg1));
		if (targetLg == null)
		{
			c.sendln("That lootgroup ID was not found.");
			return;
		}
		if (arg2.length() == 0)
		{
			c.olcMode = "ledit "+targetLg.id;
			c.sendln("OLC mode set: ledit");
			return;
		}
		
		if ("info".startsWith(arg2))
		{
			c.sendln(Fmt.heading(""));
			c.sendln("}m Name}M:}n "+targetLg.name);
			c.sendln("}m Contains }n"+targetLg.contents.size()+"}m objects}M:");

			int totalNum = 0;
			for (ObjProto k : targetLg.contents.keySet())
				totalNum += targetLg.contents.get(k);
			
			c.sendln("   }nID#  }M| }nLoad Prob. }M(}n %}M) | }NName");
			for (ObjProto op : targetLg.contents.keySet())
				c.sendln(" }n"+Fmt.rfit(""+op.id, 6)+"}M | }n"+Fmt.rfit(""+targetLg.contents.get(op), 6)+" }M(}n"+Fmt.rhfit(""+(targetLg.contents.get(op)*100.0/totalNum), 5)+"%}M) :}N "+op.shortName);
			c.sendln(Fmt.heading("")+"{x");
			return;
		}
		
		if ("name".startsWith(arg2))
		{
			if (arg3.length() > 0)
			{
				targetLg.name = arg3;
				Database.saveLootgroup(targetLg);
				c.sendln("Lootgroup name set.");
				return;
			}
			c.sendln("The new lootgroup name can't be blank.");
			return;
		}
		
		if ("add".startsWith(arg2))
		{
			arg3 = CommandHandler.getArg(args, 3);
			String arg4 = CommandHandler.getArg(args, 4);
			
			if (arg3.length() > 0)
			{
				ObjProto tempOp = ObjProto.lookup(Fmt.getInt(arg3));
				if (tempOp != null)
				{
					if (targetLg.contents.get(tempOp) != null)
						c.sendln("The lootgroup already contains that object - previous load probability cleared.");
					
					int prob = Fmt.getInt(arg4);
					if (prob < 1)
						prob = 100;

					targetLg.contents.put(tempOp, prob);
					Database.saveLootgroup(targetLg);
					c.sendln("Object added.");
					return;
				}
				c.sendln("That's not a valid object ID.");
				return;
			}
			c.sendln("Add what object ID?");
			return;
		}

		if ("remove".startsWith(arg2))
		{
			if (arg3.length() > 0)
			{
				ObjProto tempOp = ObjProto.lookup(Fmt.getInt(arg3));
				if (tempOp != null)
				{
					if (targetLg.contents.get(tempOp) == null)
					{
						c.sendln("The lootgroup doesn't contain that object.");
						return;
					}
					targetLg.contents.remove(tempOp);
					Database.saveLootgroup(targetLg);
					c.sendln("Object removed.");
					return;
				}
				c.sendln("That's not a valid object ID.");
				return;
			}
			c.sendln("Remove what object ID?");
			return;
		}
		
		if (c.olcMode.startsWith("ledit"))
			c.olcMatched = false;
		else
			InfoCommands.doHelp(c, "ledit");
	}
	
	
	
	/**
	Create/edit/delete mob prototypes.
	
	@param c The user who entered this command.
	@param args A string containing any arguments to the command.
	*/
	public static void doMedit(UserCon c, String args)
	{
		String arg1 = CommandHandler.getArg(args, 1).toLowerCase();
		String arg2 = CommandHandler.getArg(args, 2).toLowerCase();
		String arg3 = CommandHandler.getLastArg(args, 3);
		
		if (arg1.length() == 0)
		{
			InfoCommands.doHelp(c, "medit");
			return;
		}

		// Create a new mob prototype.
		if ("create".startsWith(arg1) || (c.olcMode.length() > 0 && "create".equals(arg2)))
		{
			int newId = Fmt.getInt(arg2);
			if (c.olcMode.length() > 0)
				newId = Fmt.getInt(arg3);
			if (newId < 1)
			{
				c.sendln("That's not a valid mob prototype ID.");
				return;
			}
			if (!Database.newCharProto(newId))
			{
				c.sendln("A mob prototype with that ID already exists.");
				return;
			}
			c.olcMode = "medit "+newId;
			c.sendln("OLC mode set: medit");
			for (Area a : areas)
				if (a.start <= newId && a.end >= newId)
				{
					c.sendln("New mob in '"+a.name+"' created: 'A New Mob'.");
					return;
				}
			c.sendln("New mob created: 'A New Mob'.^/{RWARNING: {wThe mob ID does not fit in any existing areas. It currently has no area.{x");
			return;
		}

		// List the ID and title of all mobs in the current area.
		if ("list".startsWith(arg1) || (c.olcMode.length() > 0 && "list".equals(arg2)))
		{
			ArrayList<String> ast = new ArrayList<String>();
			if (c.ch.currentArea() == null)
			{
				c.sendln("You're not currently in an area.^/"+
						"Listing all mob prototypes outside of any area's range:");
				boolean printMob = true;
				for (CharProto p : charProtos)
				{
					printMob = true;
					for (Area a : areas)
					{
						if (a.start <= p.id && a.end >= p.id)
						{
							printMob = false;
							break;
						}
						if (printMob)
							ast.add("#}n"+p.id+"}M: }n"+p.name);
					}
				}
				c.sendln(Fmt.defaultTextColumns(ast.toArray(new String[0])));
				return;
			}
			
			c.sendln("Showing all mob prototypes in "+c.ch.currentArea().name+":");
			for (CharProto p : charProtos)
				if (c.ch.currentArea().start <= p.id && c.ch.currentArea().end >= p.id)
					ast.add("#}n"+p.id+"}M: }n"+p.name);
			c.sendln(Fmt.defaultTextColumns(ast.toArray(new String[0])));
			return;
		}
		
		CharProto targetCp = CharProto.lookup(Fmt.getInt(arg1));
		if (targetCp == null)
		{
			c.sendln("That mob ID was not found.");
			return;
		}
		if (arg2.length() == 0)
		{
			c.olcMode = "medit "+targetCp.id;
			c.sendln("OLC mode set: medit");
			return;
		}
		
		if ("info".startsWith(arg2))
		{
			c.sendln(Fmt.heading(""));
			c.sendln("}m        Name}M:}n "+targetCp.name);
			c.sendln("}m  Short Name}M:}n "+targetCp.shortName);
			c.sendln("}m   Long Name}M:}n "+targetCp.longName);
			if (targetCp.sex.equals("m"))
				c.sendln("}m         Sex}M:}n Male");
			else if (targetCp.sex.equals("f"))
				c.sendln("}m         Sex}M:}n Female");
			else
				c.sendln("}m         Sex}M:}n Random");
			c.sendln("");
			c.sendln("}m       Level}M:}n "+targetCp.level);
			c.sendln("}m       Align}M:}n "+targetCp.align);
			c.sendln("}m  Difficulty}M:}n "+targetCp.difficulty);

			String fString = "";
			for (String s : Flags.charFlags)
				if (targetCp.flags.get(s) != null)
					if (targetCp.flags.get(s))
						fString = fString+s+" ";
			if (fString.length() == 0)
				fString = "none";
			else
				fString = fString.trim();

			String eString = "";
			for (String s : Flags.charEffects)
				if (targetCp.effects.get(s) != null)
					if (targetCp.effects.get(s))
						eString = eString+s+" ";
			if (eString.length() == 0)
				eString = "none";
			else
				eString = eString.trim();
				
			c.sendln("}m       Flags}M:}n "+fString);
			c.sendln("}m     Effects}M:}n "+eString);
			c.sendln("}m    Position}M:}n "+targetCp.position);
			if (targetCp.movement.length() > 0)
				c.sendln("}m    Movement}M:}n "+targetCp.movement);
			if (targetCp.hitname.length() > 0)
				c.sendln("}m    Hit Name}M:}n "+targetCp.hitname);
			c.sendln("}m        Race}M:}n "+targetCp.charRace.name);
			c.sendln("}m       Class}M:}n "+targetCp.charClass.name);

			String edTemp = "";
			for (String s : targetCp.eds.keySet())
				edTemp = edTemp+" }M[}N"+s+"}M]";
			if (edTemp.length() == 0)
				edTemp = " none";
			c.sendln(" }mExtra Descs}M:}n"+edTemp);

			c.sendln("^/}mMprogs}M:");
			if (targetCp.triggers.size() == 0)
				c.sendln("}m  None");
			int ctr = 0;
			for (Trigger t : targetCp.triggers)
			{
				ctr++;
				c.sendln("}m #}n"+ctr+"}M: }nMprog }m#}n"+t.mprog.id+" }M- }NTrigger: }n"+t.type+" }M- }NArgs: }n"+t.numArg+"}M/}n"+t.arg);
			}

			c.sendln(Fmt.heading(""));
			c.sendln("}mDescription}M:^/}N"+targetCp.description);
			if (targetCp.flags.get("shopkeeper"))
			{
				c.sendln(Fmt.heading("Shopkeeper Data"));
				for (Lootgroup l : targetCp.sells)
					c.sendln("}mSells from}M: }n#"+l.id+" }M(}N"+l.name+"}M){x");
			}
			c.sendln(Fmt.heading("")+"{x");
			return;
		}
		
		if ("forceupdate".startsWith(arg2))
		{
			int updated = 0;
			for (int ctr = 0; ctr < mobs.size(); ctr++)
			{
				CharData tempCh = mobs.get(ctr);
				if (tempCh.cp == targetCp)
				{
					CharData newCh = new CharData(targetCp);
					newCh.objects = tempCh.objects;
					newCh.currentRoom = tempCh.currentRoom;
					newCh.id = tempCh.id;
					newCh.resetFilled = tempCh.resetFilled;
					mobs.remove(ctr);
					mobs.add(ctr, newCh);
					updated++;
				}
			}
			
			c.sendln(updated+" "+(updated != 1 ? "mobs have" : "mob has")+" been updated.");
			return;
		}
		
		if ("addmprog".startsWith(arg2))
		{
			arg3 = CommandHandler.getArg(args, 3);
			String arg4 = CommandHandler.getArg(args, 4);
			String arg5 = CommandHandler.getArg(args, 5);
			String arg6 = CommandHandler.getLastArg(args, 6);
			
			MobProg targetMprog = MobProg.lookup(Fmt.getInt(arg3));
			if (targetMprog == null)
			{
				c.sendln("That mprog ID was not found.");
				return;
			}
			
			for (String s : Flags.mprogTriggerTypes)
				if (s.equalsIgnoreCase(arg4))
				{
					Trigger newTrigger = new Trigger();
					newTrigger.type = s;
					newTrigger.arg = arg6;
					newTrigger.numArg = Fmt.getInt(arg5);
					newTrigger.mprog = targetMprog;
					targetCp.triggers.add(newTrigger);
					Database.saveCharProto(targetCp);
					c.sendln("Mprog '"+targetMprog.name+"' added to this mob.");
					return;
				}
			
			String temp = "";
			for (String s : Flags.mprogTriggerTypes)
				temp = temp+s+" ";
			c.sendln("That's not a valid mprog trigger.^/Valid triggers are: "+temp.trim());
			return;
		}
		
		if ("delmprog".startsWith(arg2))
		{
			int targetNr = Fmt.getInt(arg3);
			if (targetNr < 1 || targetNr > targetCp.triggers.size())
			{
				if (MobProg.lookup(targetNr) != null)
				{
					for (Trigger t : targetCp.triggers)
					{
						if (t.mprog == MobProg.lookup(targetNr))
						{
							targetCp.triggers.remove(t);
							c.sendln("Mprog trigger deleted.");
							Database.saveCharProto(targetCp);
							return;
						}
					}
					c.sendln("That character doesn't have a trigger for that prog.");
					return;
				}
				c.sendln("There aren't that many mprog triggers on this mob prototype.");
				return;
			}
			targetCp.triggers.remove(targetNr-1);
			Database.saveCharProto(targetCp);
			c.sendln("Mprog trigger deleted.");
			return;
		}
		
		if ("name".startsWith(arg2))
		{
			if (arg3.length() > 0)
			{
				targetCp.name = arg3;
				Database.saveCharProto(targetCp);
				c.sendln("Mob name (keywords) set.");
				return;
			}
			c.sendln("The new mob name can't be blank.");
			return;
		}

		if ("short".startsWith(arg2))
		{
			if (arg3.length() > 0)
			{
				targetCp.shortName = arg3;
				Database.saveCharProto(targetCp);
				c.sendln("Mob short name set.");
				return;
			}
			c.sendln("The new mob short name can't be blank.");
			return;
		}

		if ("long".startsWith(arg2))
		{
			if (arg3.length() > 0)
			{
				targetCp.longName = arg3;
				Database.saveCharProto(targetCp);
				c.sendln("Mob long name set.");
				return;
			}
			c.sendln("The new mob long name can't be blank.");
			return;
		}

		if ("flags".startsWith(arg2))
		{
			if (arg3.length() == 0)
			{
				c.sendln("Set what flags? See 'help mobflags' for a list.");
				return;
			}
			String[] flags = arg3.split(" ");
			for (String s : flags)
			{
				Boolean flagFound = false;
				s = s.trim().toLowerCase();
				if (s.length() == 0)
					continue;
				for (String fs : Flags.charFlags)
				{
					if (fs.startsWith(s))
					{
						if (targetCp.flags.get(fs))
						{
							targetCp.flags.put(fs, false);
							c.sendln("'"+fs+"' removed.");
						}
						else
						{
							targetCp.flags.put(fs, true);
							c.sendln("'"+fs+"' set.");
						}
						flagFound = true;
						break;
					}
				}
				if (!flagFound)
				{
					c.sendln("'"+s+"' is not a valid mob flag.");
					continue;
				}
			}
			Database.saveCharProto(targetCp);
			return;
		}

		if ("effects".startsWith(arg2) || "affects".startsWith(arg2))
		{
			if (arg3.length() == 0)
			{
				c.sendln("Set what effects? See 'help mobeffects' for a list.");
				return;
			}
			String[] flags = arg3.split(" ");
			for (String s : flags)
			{
				Boolean flagFound = false;
				s = s.trim().toLowerCase();
				if (s.length() == 0)
					continue;
				for (String fs : Flags.charEffects)
				{
					if (fs.startsWith(s))
					{
						if (targetCp.effects.get(fs))
						{
							targetCp.effects.put(fs, false);
							c.sendln("'"+fs+"' removed.");
						}
						else
						{
							targetCp.effects.put(fs, true);
							c.sendln("'"+fs+"' set.");
						}
						flagFound = true;
						break;
					}
				}
				if (!flagFound)
				{
					c.sendln("'"+s+"' is not a valid mob effect.");
					continue;
				}
			}
			Database.saveCharProto(targetCp);
			return;
		}

		if ("position".startsWith(arg2))
		{
			if (arg3.length() == 0)
			{
				c.sendln("Set the mob's default position to what?");
				return;
			}
			for (String s : Flags.charPositions)
			{
				if (s.startsWith(arg3))
				{
					targetCp.position = s;
					Database.saveCharProto(targetCp);
					c.sendln("Mob default position set.");
					return;
				}
			}
			c.sendln("That's not a valid position. (sitting, standing, resting, sleeping)");
			return;
		}

		if ("sex".startsWith(arg2))
		{
			arg3 = arg3.toLowerCase();
			if (arg3.length() > 0)
			{
				boolean valid = false;
				if ("male".startsWith(arg3))
				{
					targetCp.sex = "m";
					valid = true;
				}
				else if ("female".startsWith(arg3))
				{
					targetCp.sex = "f";
					valid = true;
				}
				else if ("random".startsWith(arg3))
				{
					targetCp.sex = "r";
					valid = true;
				}
				if (valid)
				{
					Database.saveCharProto(targetCp);
					c.sendln("Mob sex set.");
					return;
				}
			}
			c.sendln("Invalid mob sex setting.^/"+
					"Valid settings: male female random");
			return;
		}
		
		if ("race".startsWith(arg2))
		{
			if (arg3.length() == 0)
			{
				doRcedit(c, "list");
				return;
			}
			Race tempRace = Race.lookup(arg3);
			if (tempRace != null)
			{
				targetCp.charRace = tempRace;
				Database.saveCharProto(targetCp);
				c.sendln("Mob race set.");
				return;
			}
			c.sendln("That's not a valid race ID or name.^/"+
					"Try }H'}hrcedit list}H'{x to view all races.");
			return;
		}

		if ("class".startsWith(arg2))
		{
			if (arg3.length() == 0)
			{
				ArrayList<String> ast = new ArrayList<String>();
				for (CharClass cl : classes)
					ast.add("#}n"+cl.id+"}M: }n"+cl.name);
				c.sendln("Existing classes:^/"+Fmt.defaultTextColumns(ast.toArray(new String[0])));
				return;
			}
			CharClass tempClass = CharClass.lookup(arg3);
			if (tempClass != null)
			{
				targetCp.charClass = tempClass;
				Database.saveCharProto(targetCp);
				c.sendln("Mob class set.");
				return;
			}
			c.sendln("That's not a valid class ID or name.^/"+
					"Try }H'}hmedit }H<}iid}H> }hclass}H'{x to view all classes.");
			return;
		}
		
		if ("level".startsWith(arg2))
		{
			int level = Fmt.getInt(arg3);
			if (level >= Flags.minLevel && level <= Flags.maxLevel)
			{
				targetCp.level = level;
				Database.saveCharProto(targetCp);
				c.sendln("Mob level set.");
				return;
			}
			c.sendln("That's not a valid level. Mob level must be in the range "+Flags.minLevel+" - "+Flags.maxLevel+".");
			return;
		}
		
		if ("align".startsWith(arg2))
		{
			int align = Fmt.getInt(arg3);
			if (align >= Flags.minAlign && align <= Flags.maxAlign)
			{
				targetCp.align = align;
				Database.saveCharProto(targetCp);
				c.sendln("Mob align set.");
				return;
			}
			c.sendln("That's not a valid alignment. Mob alignment must be in the range "+Flags.minAlign+" - "+Flags.maxAlign+".");
			return;
		}
		
		if ("difficulty".startsWith(arg2))
		{
			int diff = Fmt.getInt(arg3);
			if (diff >= 1 && diff <= 5)
			{
				targetCp.difficulty = diff;
				Database.saveCharProto(targetCp);
				c.sendln("Mob difficulty set.");
				return;
			}
			c.sendln("That's not a valid difficulty. Mob difficulty must be in the range 1 - 5.");
			return;
		}
		
		if ("sells".startsWith(arg2))
		{
			if (arg3.length() == 0)
			{
				c.sendln("Add/remove which lootgroup to the sell list?");
				return;
			}
			Lootgroup targetLg = Lootgroup.lookup(Fmt.getInt(arg3));
			if (targetLg == null)
			{
				c.sendln("That's not a valid lootgroup number.");
				return;
			}
			if (targetCp.sells.contains(targetLg))
			{
				targetCp.sells.remove(targetLg);
				c.sendln("Lootgroup #"+targetLg.id+" removed from sell list.");
				Database.saveCharProto(targetCp);
				return;
			}
			targetCp.sells.add(targetLg);
			c.sendln("Lootgroup #"+targetLg.id+" added to the sell list.");
			Database.saveCharProto(targetCp);
			return;
		}

		if ("ed".startsWith(arg2))
		{
			arg3 = CommandHandler.getArg(args, 3).toLowerCase();
			String arg4 = CommandHandler.getLastArg(args, 4).toLowerCase();
			
			if (arg3.length() == 0)
			{
				c.sendln("Add/remove/edit an extra description?");
				return;
			}
			if (arg4.length() == 0)
			{
				c.sendln("Add/remove/edit what extra description?");
				return;
			}
			if ("remove".startsWith(arg3) || "delete".startsWith(arg3))
			{
				for (String s : targetCp.eds.keySet())
					if (s.startsWith(arg4))
					{
						targetCp.eds.remove(s);
						c.sendln("Extra description removed.");
						Database.saveCharProto(targetCp);
						return;
					}
				c.sendln("That extra description was not found.");
				return;
			}
			if ("add".startsWith(arg3))
			{
				for (String s : targetCp.eds.keySet())
					if (s.equals(arg4))
					{
						c.sendln("That extra description is already added.");
						return;
					}
				c.sendln("Now editing extra description '"+arg4+"' for: "+targetCp.name);
				c.editMode("MeditExtradesc", targetCp.id+"/"+arg4, "");
				return;
			}
			if ("edit".startsWith(arg3))
			{
				for (String s : targetCp.eds.keySet())
					if (s.startsWith(arg4))
					{
						c.sendln("Now editing extra description '"+arg4+"' for: "+targetCp.name);
						c.editMode("MeditExtradesc", targetCp.id+"/"+arg4, targetCp.eds.get(s));
						return;
					}
				c.sendln("There's no matching extra description on this mob.");
				return;
			}
			InfoCommands.doHelp(c, "medit");
			return;
		}

		// Set the custom movement verb of a character.
		if ("movement".startsWith(arg2))
		{
			if (arg3.length() > 0)
			{
				boolean found = false;
				for (String m : Flags.charMovements)
					if (m.startsWith(arg3.toLowerCase()))
					{
						found = true;
						arg3 = m;
					}
				
				if (!found)
				{
					c.sendln("That's not a valid movement verb. See 'help movementverbs'.");
					return;
				}
				
				targetCp.movement = arg3;
				Database.saveCharProto(targetCp);
				c.sendln("Character movement verb set.");
				return;
			}
			targetCp.movement = "";
			Database.saveCharProto(targetCp);
			c.sendln("Custom movement verb cleared.");
			return;
		}

		// Set the custom unarmed hit name of a character.
		if ("hitname".startsWith(arg2))
		{
			if (arg3.length() > 0)
			{
				boolean found = false;
				for (String m : Flags.hitTypes)
					if (m.startsWith(arg3.toLowerCase()))
					{
						found = true;
						arg3 = m;
					}
				
				if (!found)
				{
					c.sendln("That's not a valid hit name. See 'help hittypes'.");
					return;
				}
				
				targetCp.hitname = arg3;
				Database.saveCharProto(targetCp);
				c.sendln("Character hit name set.");
				return;
			}
			targetCp.hitname = "";
			Database.saveCharProto(targetCp);
			c.sendln("Custom hit name cleared.");
			return;
		}

		// Set the description of a mob prototype.
		if ("description".startsWith(arg2))
		{
			c.sendln("Now editing description for: "+targetCp.name);
			c.editMode("MeditDescription", ""+targetCp.id, targetCp.description);
			return;
		}
		
		if (c.olcMode.startsWith("medit"))
			c.olcMatched = false;
		else
			InfoCommands.doHelp(c, "medit");
	}
	/**
	Receive text from the prompt and give it to the mob prototype the user was editing.
	
	@param c The user who is in edit mode.
	@param finishedText The entire contents of the finished editor.
	*/
	public static void prMeditDescription(UserCon c, String finishedText)
	{
		CharProto targetCp = CharProto.lookup(Fmt.getInt(c.promptTarget));

		if (targetCp != null)
		{
			targetCp.description = finishedText;
			Database.saveCharProto(targetCp);
			c.sendln("Mob description saved.");
		}

		c.clearEditMode();
	}
	public static void prMeditExtradesc(UserCon c, String finishedText)
	{
		String id = c.promptTarget.split("\\/", 2)[0];
		String edName = c.promptTarget.split("\\/", 2)[1];
		CharProto targetCp = CharProto.lookup(Fmt.getInt(id));

		if (targetCp != null)
		{
			targetCp.eds.put(edName, finishedText);
			Database.saveCharProto(targetCp);
			c.sendln("Mob extra description saved.");
		}

		c.clearEditMode();
	}
	
	
	
	/**
	Create/edit/delete object prototypes.
	
	@param c The user who entered this command.
	@param args A string containing any arguments to the command.
	*/
	public static void doOedit(UserCon c, String args)
	{
		String arg1 = CommandHandler.getArg(args, 1).toLowerCase();
		String arg2 = CommandHandler.getArg(args, 2).toLowerCase();
		String arg3 = CommandHandler.getLastArg(args, 3);
		
		if (arg1.length() == 0)
		{
			InfoCommands.doHelp(c, "oedit");
			return;
		}

		// Create a new object prototype.
		if ("create".startsWith(arg1) || (c.olcMode.length() > 0 && "create".equals(arg2)))
		{
			int newId = Fmt.getInt(arg2);
			if (c.olcMode.length() > 0)
				newId = Fmt.getInt(arg3);
			if (newId < 1)
			{
				c.sendln("That's not a valid object prototype ID.");
				return;
			}
			if (!Database.newObjProto(newId))
			{
				c.sendln("An object prototype with that ID already exists.");
				return;
			}
			c.olcMode = "oedit "+newId;
			c.sendln("OLC mode set: oedit");
			for (Area a : areas)
				if (a.start <= newId && a.end >= newId)
				{
					c.sendln("New object in '"+a.name+"' created: 'A New Object'.");
					return;
				}
			c.sendln("New object created: 'A New Object'.^/{RWARNING: {wThe object ID does not fit in any existing areas. It currently has no area.{x");
			return;
		}

		// List the ID and title of all objects in the current area.
		if ("list".startsWith(arg1) || (c.olcMode.length() > 0 && "list".equals(arg2)))
		{
			ArrayList<String> ast = new ArrayList<String>();
			if (c.ch.currentArea() == null)
			{
				c.sendln("You're not currently in an area.^/"+
						"Listing all object prototypes outside of any area's range:");
				boolean printObj = true;
				for (ObjProto p : objProtos)
				{
					printObj = true;
					for (Area a : areas)
					{
						if (a.start <= p.id && a.end >= p.id)
						{
							printObj = false;
							break;
						}
						if (printObj)
							ast.add("#}n"+p.id+"}M: }n"+p.name);
					}
				}
				c.sendln(Fmt.defaultTextColumns(ast.toArray(new String[0])));
				return;
			}
			
			c.sendln("Showing all object prototypes in "+c.ch.currentArea().name+":");
			for (ObjProto p : objProtos)
				if (c.ch.currentArea().start <= p.id && c.ch.currentArea().end >= p.id)
					ast.add("#}n"+p.id+"}M: }n"+p.name);
			c.sendln(Fmt.defaultTextColumns(ast.toArray(new String[0])));
			return;
		}
		
		ObjProto targetOp = ObjProto.lookup(Fmt.getInt(arg1));
		if (targetOp == null)
		{
			c.sendln("That object ID was not found.");
			return;
		}
		if (arg2.length() == 0)
		{
			c.olcMode = "oedit "+targetOp.id;
			c.sendln("OLC mode set: oedit");
			return;
		}
		
		if ("info".startsWith(arg2))
		{
			c.sendln(Fmt.heading(""));
			c.sendln("}m        Name}M:}n "+targetOp.name);
			c.sendln("}m  Short Name}M:}n "+targetOp.shortName);
			c.sendln("}m   Long Name}M:}n "+targetOp.longName);
			c.sendln("}m       Level}M:}n "+targetOp.level);
			c.sendln("}m        Cost}M:}n "+targetOp.cost);
			c.sendln("}m Decay Timer}M:}n "+targetOp.decay+" (seconds)");

			String fString = "";
			for (String s : Flags.objFlags)
				if (targetOp.flags.get(s) != null)
					if (targetOp.flags.get(s))
						fString = fString+s+" ";
			if (fString.length() == 0)
				fString = "none";
			else
				fString = fString.trim();

			String eString = "";
			for (String s : Flags.objEffects)
				if (targetOp.effects.get(s) != null)
					if (targetOp.effects.get(s))
						eString = eString+s+" ";
			if (eString.length() == 0)
				eString = "none";
			else
				eString = eString.trim();

			c.sendln("}m       Flags}M:}n "+fString);
			c.sendln("}m     Effects}M:}n "+eString);

			c.sendln("}m    Material}M:}n "+targetOp.material);
			c.sendln("}m        Type}M:}n "+targetOp.type);

			if (ObjData.getTypeFlags(targetOp.type).length > 0)
			{
				String tfString = "";
				for (String s : targetOp.typeFlags.keySet())
					if (targetOp.typeFlags.get(s))
						tfString = tfString+s+" ";
				if (tfString.length() == 0)
					tfString = "none";
				else
					tfString = tfString.trim();
				c.sendln("}m  Type Flags}M:}n "+tfString);
			}

			String temp = Flags.objTypes.get(targetOp.type)[0];
			if (temp.length() > 0)
			{
				c.send("^/}M[}mV1}M]}m "+Fmt.rfit(temp, 21)+"}M:}n "+targetOp.value1);
				if (targetOp.type.equals("portal"))
				{
					Room tempRoom = Room.lookup(Fmt.getInt(targetOp.value1));
					if (tempRoom != null)
						c.send(" }M(}n"+tempRoom.name+"}M)");
				}
				c.sendln("");
			}
			temp = Flags.objTypes.get(targetOp.type)[1];
			if (temp.length() > 0)
			{
				c.send("}M[}mV2}M]}m "+Fmt.rfit(temp, 21)+"}M:}n "+targetOp.value2);
				if (targetOp.type.equals("container"))
				{
					ObjProto tempObj = ObjProto.lookup(Fmt.getInt(targetOp.value2));
					if (tempObj != null)
						c.send(" }M(}n"+tempObj.shortName+"}M)");
				}
				c.sendln("");
			}
			temp = Flags.objTypes.get(targetOp.type)[2];
			if (temp.length() > 0)
				c.sendln("}M[}mV3}M]}m "+Fmt.rfit(temp, 21)+"}M:}n "+targetOp.value3);
			temp = Flags.objTypes.get(targetOp.type)[3];
			if (temp.length() > 0)
				c.sendln("}M[}mV4}M]}m "+Fmt.rfit(temp, 21)+"}M:}n "+targetOp.value4);
			temp = Flags.objTypes.get(targetOp.type)[4];
			if (temp.length() > 0)
				c.sendln("}M[}mV5}M]}m "+Fmt.rfit(temp, 21)+"}M:}n "+targetOp.value5);

			if (targetOp.statMods.size() > 0)
				c.sendln("");
			for (String s : targetOp.statMods.keySet())
				c.sendln("}m     Affects}M: }n"+Flags.fullStatName(s)+" }Nby }n"+targetOp.statMods.get(s));

			String edTemp = "";
			for (String s : targetOp.eds.keySet())
				edTemp = edTemp+" }M[}N"+s+"}M]";
			if (edTemp.length() == 0)
				edTemp = " none";
			c.sendln(" }mExtra Descs}M:}n"+edTemp);
			
			c.sendln("^/}mOprogs}M:");
			if (targetOp.triggers.size() == 0)
				c.sendln("}m  None");
			int ctr = 0;
			for (Trigger t : targetOp.triggers)
			{
				ctr++;
				c.sendln("}m #}n"+ctr+"}M: }nMprog }m#}n"+t.oprog.id+" }M- }NTrigger: }n"+t.type+" }M- }NArgs: }n"+t.numArg+"}M/}n"+t.arg);
			}

			c.sendln(Fmt.heading(""));
			c.sendln("}mDescription}M:^/}N"+targetOp.description);
			c.sendln(Fmt.heading("")+"{x");
			return;
		}

		if ("forceupdate".startsWith(arg2))
		{
			ObjData ob = new ObjData(targetOp);
			int updated = Database.forceUpdateObject(ob, targetOp.id);
			int liveUpdated = 0;
			
			for (ObjData o : ObjData.allObjects())
				if (o.op == targetOp)
				{
					o.copyFrom(targetOp);
					liveUpdated++;
				}
			
			c.sendln(updated+" saved "+(updated != 1 ? "objects have" : "object has")+" been updated. ("+liveUpdated+" live.)");
			return;
		}
		
		if ("addoprog".startsWith(arg2))
		{
			arg3 = CommandHandler.getArg(args, 3);
			String arg4 = CommandHandler.getArg(args, 4);
			String arg5 = CommandHandler.getArg(args, 5);
			String arg6 = CommandHandler.getLastArg(args, 6);
			
			ObjProg targetOprog = ObjProg.lookup(Fmt.getInt(arg3));
			if (targetOprog == null)
			{
				c.sendln("That oprog ID was not found.");
				return;
			}
			
			for (String s : Flags.oprogTriggerTypes)
				if (s.equalsIgnoreCase(arg4))
				{
					Trigger newTrigger = new Trigger();
					newTrigger.type = s;
					newTrigger.arg = arg6;
					newTrigger.numArg = Fmt.getInt(arg5);
					newTrigger.oprog = targetOprog;
					targetOp.triggers.add(newTrigger);
					Database.saveObjProto(targetOp);
					c.sendln("Oprog '"+targetOprog.name+"' added to this object.");
					return;
				}
			
			String temp = "";
			for (String s : Flags.oprogTriggerTypes)
				temp = temp+s+" ";
			c.sendln("That's not a valid oprog trigger.^/Valid triggers are: "+temp.trim());
			return;
		}
		
		if ("deloprog".startsWith(arg2))
		{
			int targetNr = Fmt.getInt(arg3);
			if (targetNr < 1 || targetNr > targetOp.triggers.size())
			{
				if (ObjProg.lookup(targetNr) != null)
				{
					for (Trigger t : targetOp.triggers)
					{
						if (t.oprog == ObjProg.lookup(targetNr))
						{
							targetOp.triggers.remove(t);
							c.sendln("Oprog trigger deleted.");
							Database.saveObjProto(targetOp);
							return;
						}
					}
					c.sendln("That object doesn't have a trigger for that prog.");
					return;
				}
				c.sendln("There aren't that many oprog triggers on this object prototype.");
				return;
			}
			targetOp.triggers.remove(targetNr-1);
			Database.saveObjProto(targetOp);
			c.sendln("Oprog trigger deleted.");
			return;
		}

		if ("name".startsWith(arg2))
		{
			if (arg3.length() > 0)
			{
				targetOp.name = arg3;
				Database.saveObjProto(targetOp);
				c.sendln("Object name (keywords) set.");
				return;
			}
			c.sendln("The new object name can't be blank.");
			return;
		}

		if ("short".startsWith(arg2))
		{
			if (arg3.length() > 0)
			{
				targetOp.shortName = arg3;
				Database.saveObjProto(targetOp);
				c.sendln("Object short name set.");
				return;
			}
			c.sendln("The new object short name can't be blank.");
			return;
		}

		if ("long".startsWith(arg2))
		{
			if (arg3.length() > 0)
			{
				targetOp.longName = arg3;
				Database.saveObjProto(targetOp);
				c.sendln("Object long name set.");
				return;
			}
			c.sendln("The new object long name can't be blank.");
			return;
		}
		
		if ("level".startsWith(arg2))
		{
			int level = Fmt.getInt(arg3);
			if (level >= Flags.minLevel && level <= Flags.maxLevel)
			{
				targetOp.level = level;
				Database.saveObjProto(targetOp);
				c.sendln("Object level set.");
				return;
			}
			c.sendln("That's not a valid level. Object level must be in the range "+Flags.minLevel+" - "+Flags.maxLevel+".");
			return;
		}

		if ("cost".startsWith(arg2))
		{
			int cost = Fmt.getInt(arg3);
			if (cost >= 0)
			{
				targetOp.cost = cost;
				Database.saveObjProto(targetOp);
				c.sendln("Object cost set.");
				return;
			}
			c.sendln("That's not a valid cost. Object cost must be at least 0.");
			return;
		}

		if ("decay".startsWith(arg2))
		{
			int decay = Fmt.getInt(arg3);
			if (decay >= 0)
			{
				targetOp.decay = decay;
				Database.saveObjProto(targetOp);
				c.sendln("Object decay set.");
				return;
			}
			c.sendln("That's not a valid decay timer. Valid values are 0 for never or a positive number.");
			return;
		}

		if ("flags".startsWith(arg2))
		{
			if (arg3.length() == 0)
			{
				c.sendln("Set what flags? See 'help objflags' for a list.");
				return;
			}
			String[] flags = arg3.split(" ");
			for (String s : flags)
			{
				Boolean flagFound = false;
				s = s.trim().toLowerCase();
				if (s.length() == 0)
					continue;
				for (String fs : Flags.objFlags)
				{
					if (fs.startsWith(s))
					{
						if (targetOp.flags.get(fs))
						{
							targetOp.flags.put(fs, false);
							c.sendln("'"+fs+"' removed.");
						}
						else
						{
							targetOp.flags.put(fs, true);
							c.sendln("'"+fs+"' set.");
						}
						flagFound = true;
						break;
					}
				}
				if (!flagFound)
				{
					c.sendln("'"+s+"' is not a valid object flag.");
					continue;
				}
			}
			Database.saveObjProto(targetOp);
			return;
		}

		if ("effects".startsWith(arg2) || "affects".startsWith(arg2))
		{
			if (arg3.length() == 0)
			{
				c.sendln("Set what effects? See 'help objeffects' for a list.");
				return;
			}
			String[] flags = arg3.split(" ");
			for (String s : flags)
			{
				Boolean flagFound = false;
				s = s.trim().toLowerCase();
				if (s.length() == 0)
					continue;
				for (String fs : Flags.objEffects)
				{
					if (fs.startsWith(s))
					{
						if (targetOp.effects.get(fs))
						{
							targetOp.effects.put(fs, false);
							c.sendln("'"+fs+"' removed.");
						}
						else
						{
							targetOp.effects.put(fs, true);
							c.sendln("'"+fs+"' set.");
						}
						flagFound = true;
						break;
					}
				}
				if (!flagFound)
				{
					c.sendln("'"+s+"' is not a valid object effect.");
					continue;
				}
			}
			Database.saveObjProto(targetOp);
			return;
		}

		if ("material".startsWith(arg2))
		{
			if (arg3.length() > 0)
			{
				arg3 = arg3.toLowerCase();
				for (String t : Flags.objMaterials)
				{
					if (t.startsWith(arg3))
					{
						targetOp.material = t;
						Database.saveObjProto(targetOp);
						c.sendln("Object material set.");
						return;
					}
				}
				c.sendln("That's not a valid object material. See 'help objmaterials' for a list.");
				return;
			}
			c.sendln("Set the material to what? See 'help objmaterials' for a list.");
			return;
		}

		if ("type".startsWith(arg2))
		{
			if (arg3.length() > 0)
			{
				arg3 = arg3.toLowerCase();
				for (String t : Flags.objTypes.keySet())
				{
					if (t.startsWith(arg3))
					{
						targetOp.type = t;
						targetOp.setTypeFlags("");
						targetOp.value1 = ObjData.defaultTypeValues(targetOp.type)[0];
						targetOp.value2 = ObjData.defaultTypeValues(targetOp.type)[1];
						targetOp.value3 = ObjData.defaultTypeValues(targetOp.type)[2];
						targetOp.value4 = ObjData.defaultTypeValues(targetOp.type)[3];
						targetOp.value5 = ObjData.defaultTypeValues(targetOp.type)[4];
						Database.saveObjProto(targetOp);
						c.sendln("Object type set.");
						return;
					}
				}
				c.sendln("That's not a valid object type. See 'help objtypes' for a list.");
				return;
			}
			c.sendln("Set the type to what? See 'help objtypes' for a list.");
			return;
		}
		
		if ("typeflags".startsWith(arg2))
		{
			String[] validFlags = ObjData.getTypeFlags(targetOp.type);
			if (validFlags.length == 0)
			{
				c.sendln("There are no type flags for the '"+targetOp.type+"' type.");
				return;
			}
			if (arg3.length() == 0)
			{
				c.sendln("Set what type flags? See 'help "+targetOp.type+"flags' for a list.");
				return;
			}
			String[] flags = arg3.split(" ");
			for (String s : flags)
			{
				Boolean flagFound = false;
				s = s.trim().toLowerCase();
				if (s.length() == 0)
					continue;
				for (String fs : validFlags)
				{
					if (fs.startsWith(s))
					{
						if (targetOp.typeFlags.get(fs))
						{
							targetOp.typeFlags.put(fs, false);
							c.sendln("'"+fs+"' removed.");
						}
						else
						{
							targetOp.typeFlags.put(fs, true);
							c.sendln("'"+fs+"' set.");
						}
						flagFound = true;
						break;
					}
				}
				if (!flagFound)
				{
					c.sendln("'"+s+"' is not a valid type flag. See 'help "+targetOp.type+"flags' for a list.");
					continue;
				}
			}
			Database.saveObjProto(targetOp);
			return;
		}

		if ("value1".startsWith(arg2) || "v1".startsWith(arg2))
		{
			String temp = Flags.objTypes.get(targetOp.type)[0];
			if (temp.length() == 0)
			{
				c.sendln("This object type does not use the value1 field.");
				return;
			}
			if (temp.equals("(S) Armor Type"))
			{
				if (arg3.length() > 0)
					for (String v : Flags.armorTypes)
						if (v.startsWith(arg3.toLowerCase()))
						{
							targetOp.value1 = v;
							c.sendln("Armor type set.");
							Database.saveObjProto(targetOp);
							return;
						}
				c.sendln("That's not a valid armor type. See 'help armortypes' for a list.");
				return;
			}
			if (targetOp.type.equals("weapon"))
			{
				if (arg3.length() > 0)
					for (String v : Flags.weaponTypes)
						if (v.startsWith(arg3.toLowerCase()))
						{
							targetOp.value1 = v;
							c.sendln("Weapon type set.");
							Database.saveObjProto(targetOp);
							return;
						}
				c.sendln("That's not a valid weapon type. See 'help weapontypes' for a list.");
				return;
			}
			if (targetOp.type.equals("shield"))
			{
				int tempInt = Fmt.getInt(arg3);
				if (tempInt >= 0 && tempInt <= 100)
				{
					targetOp.value1 = ""+tempInt;
					c.sendln("Maximum block percent set.");
					Database.saveObjProto(targetOp);
					return;
				}
				c.sendln("That's not a valid block percent value. (0-100)");
				return;
			}
			if (targetOp.type.equals("container"))
			{
				int tempInt = Fmt.getInt(arg3);
				if (tempInt >= 1)
				{
					targetOp.value1 = ""+tempInt;
					c.sendln("Container capacity set.");
					Database.saveObjProto(targetOp);
					return;
				}
				c.sendln("That's not a valid container capacity value. (1+)");
				return;
			}
			if (temp.equals("(#) Effect Level"))
			{
				int tempInt = Fmt.getInt(arg3);
				if (tempInt >= Flags.minLevel && tempInt <= Flags.maxLevel)
				{
					targetOp.value1 = ""+tempInt;
					c.sendln("Effect level set.");
					Database.saveObjProto(targetOp);
					return;
				}
				c.sendln("That's not a valid effect level. ("+Flags.minLevel+"-"+Flags.maxLevel+")");
				return;
			}
			if (targetOp.type.equals("furniture"))
			{
				int tempInt = Fmt.getInt(arg3);
				if (tempInt >= 0)
				{
					targetOp.value1 = ""+tempInt;
					c.sendln("Furniture capacity set.");
					Database.saveObjProto(targetOp);
					return;
				}
				c.sendln("That's not a valid furniture capacity value. (0+)");
				return;
			}
			if (targetOp.type.equals("drink"))
			{
				if (arg3.length() > 0)
				{
					targetOp.value1 = arg3;
					c.sendln("Drink type set.");
					Database.saveObjProto(targetOp);
					return;
				}
				c.sendln("Set the drink type to what?");
				return;
			}
			if (targetOp.type.equals("portal"))
			{
				if (Room.lookup(Fmt.getInt(arg3)) != null || arg3.equals("0"))
				{
					targetOp.value1 = arg3;
					c.sendln("Portal destination set.");
					Database.saveObjProto(targetOp);
					return;
				}
				c.sendln("The portal destination value must be a valid room number or 0 for a random room.");
				return;
			}
			if (targetOp.type.equals("reagent"))
			{
				if (arg3.length() > 0)
					for (String v : Flags.reagentTypes)
						if (v.startsWith(arg3.toLowerCase()))
						{
							targetOp.value1 = v;
							c.sendln("Reagent type set.");
							Database.saveObjProto(targetOp);
							return;
						}
				c.sendln("That's not a valid reagent type. See 'help reagenttypes' for a list.");
				return;
			}
			if (targetOp.type.equals("training"))
			{
				Skill targetSkill = Skill.lookup(arg3);
				if (targetSkill == null)
				{
					c.sendln("That skill was not found. You must use the full skill name.");
					return;
				}
				targetOp.value1 = targetSkill.name;
				c.sendln("Training skill set.");
				Database.saveObjProto(targetOp);
				return;
			}
			sysLog("bugs", "Unmatched value/type: "+targetOp.id+" value1");
			return;
		}
		if ("value2".startsWith(arg2) || "v2".startsWith(arg2))
		{
			String temp = Flags.objTypes.get(targetOp.type)[1];
			if (temp.length() == 0)
			{
				c.sendln("This object type does not use the value2 field.");
				return;
			}
			if (targetOp.type.equals("weapon"))
			{
				int tempInt = Fmt.getInt(arg3);
				if (tempInt >= 0)
				{
					int tempMax = Fmt.getInt(targetOp.value3);
					targetOp.value2 = ""+tempInt;
					c.sendln("Minimum base damage set.");
					if (tempMax < tempInt)
					{
						targetOp.value3 = ""+tempInt;
						c.sendln("Old maximum base damage was below new maximum - value 3 adjusted.");
					}
					Database.saveObjProto(targetOp);
					return;
				}
				c.sendln("That's not a valid base damage value. (0+)");
				return;
			}
			if (targetOp.type.equals("container"))
			{
				ObjProto tempObj = ObjProto.lookup(Fmt.getInt(arg3));
				if ((tempObj != null && tempObj.type.equals("key")) || arg3.equals("0"))
				{
					targetOp.value2 = arg3;
					c.sendln("Container key set.");
					Database.saveObjProto(targetOp);
					return;
				}
				c.sendln("The container key value must be a valid key object number, or 0 for no key.");
				return;
			}
			if (temp.startsWith("(S) Spell"))
			{
				if (arg3.length() == 0 || arg3.equals("none"))
				{
					targetOp.value2 = "";
					c.sendln("Spell cleared.");
					Database.saveObjProto(targetOp);
					return;
				}
				for (Skill s : MudMain.skills)
					if (s.type.equals("spell") && s.name.startsWith(arg3))
					{
						targetOp.value2 = s.name;
						c.sendln("Spell set to "+s.name+".");
						Database.saveObjProto(targetOp);
						return;
					}
				c.sendln("No spell by that name was found.");
				return;
			}
			if (targetOp.type.equals("furniture"))
			{
				int tempInt = Fmt.getInt(arg3);
				if ((tempInt >= -100 && tempInt <= 1000 && tempInt != 0) || arg3.equals("0"))
				{
					targetOp.value2 = ""+tempInt;
					c.sendln("Health regeneration bonus set.");
					Database.saveObjProto(targetOp);
					return;
				}
				c.sendln("The regen bonus must be a number between -100 and 1000.");
				return;
			}
			sysLog("bugs", "Unmatched value/type: "+targetOp.id+" value2");
			return;
		}
		if ("value3".startsWith(arg2) || "v3".startsWith(arg2))
		{
			String temp = Flags.objTypes.get(targetOp.type)[2];
			if (temp.length() == 0)
			{
				c.sendln("This object type does not use the value3 field.");
				return;
			}
			if (targetOp.type.equals("weapon"))
			{
				int tempInt = Fmt.getInt(arg3);
				if (tempInt >= 0)
				{
					int tempMin = Fmt.getInt(targetOp.value2);
					targetOp.value3 = ""+tempInt;
					c.sendln("Maximum base damage set.");
					if (tempMin > tempInt)
					{
						targetOp.value2 = ""+tempInt;
						c.sendln("Old minimum base damage was above new maximum - value 2 adjusted.");
					}
					Database.saveObjProto(targetOp);
					return;
				}
				c.sendln("That's not a valid base damage value. (0+)");
				return;
			}
			if (temp.startsWith("(S) Spell"))
			{
				if (arg3.length() == 0 || arg3.equals("none"))
				{
					targetOp.value3 = "";
					c.sendln("Spell cleared.");
					Database.saveObjProto(targetOp);
					return;
				}
				for (Skill s : MudMain.skills)
					if (s.type.equals("spell") && s.name.startsWith(arg3))
					{
						targetOp.value3 = s.name;
						c.sendln("Spell set to "+s.name+".");
						Database.saveObjProto(targetOp);
						return;
					}
				c.sendln("No spell by that name was found.");
				return;
			}
			if (targetOp.type.equals("wand"))
			{
				int tempInt = Fmt.getInt(arg3);
				if (tempInt > 0 || arg3.equals("0"))
				{
					targetOp.value3 = ""+tempInt;
					c.sendln("Maximum charges set.");
					Database.saveObjProto(targetOp);
					return;
				}
				c.sendln("The maximum charges must be a non-negative integer. (0+)");
				return;
			}
			if (targetOp.type.equals("furniture"))
			{
				int tempInt = Fmt.getInt(arg3);
				if ((tempInt >= -100 && tempInt <= 1000 && tempInt != 0) || arg3.equals("0"))
				{
					targetOp.value3 = ""+tempInt;
					c.sendln("Mana regeneration bonus set.");
					Database.saveObjProto(targetOp);
					return;
				}
				c.sendln("The regen bonus must be a number between -100 and 1000.");
				return;
			}
			sysLog("bugs", "Unmatched value/type: "+targetOp.id+" value3");
			return;
		}
		if ("value4".startsWith(arg2) || "v4".startsWith(arg2))
		{
			String temp = Flags.objTypes.get(targetOp.type)[3];
			if (temp.length() == 0)
			{
				c.sendln("This object type does not use the value4 field.");
				return;
			}
			if (targetOp.type.equals("weapon"))
			{
				if (arg3.length() > 0)
					for (String v : Flags.hitTypes)
						if (v.startsWith(arg3.toLowerCase()))
						{
							targetOp.value4 = v;
							c.sendln("Hit type set.");
							Database.saveObjProto(targetOp);
							return;
						}
				c.sendln("That's not a valid hit type. See 'help hittypes' for a list.");
				return;
			}
			if (temp.startsWith("(S) Spell"))
			{
				if (arg3.length() == 0 || arg3.equals("none"))
				{
					targetOp.value4 = "";
					c.sendln("Spell cleared.");
					Database.saveObjProto(targetOp);
					return;
				}
				for (Skill s : MudMain.skills)
					if (s.type.equals("spell") && s.name.startsWith(arg3))
					{
						targetOp.value4 = s.name;
						c.sendln("Spell set to "+s.name+".");
						Database.saveObjProto(targetOp);
						return;
					}
				c.sendln("No spell by that name was found.");
				return;
			}
			if (targetOp.type.equals("wand"))
			{
				int tempInt = Fmt.getInt(arg3);
				if (tempInt > 0 || arg3.equals("0"))
				{
					targetOp.value4 = ""+tempInt;
					c.sendln("Remaining charges set.");
					Database.saveObjProto(targetOp);
					return;
				}
				c.sendln("The remaining charges must be a non-negative integer. (0+)");
				return;
			}
			if (targetOp.type.equals("furniture"))
			{
				int tempInt = Fmt.getInt(arg3);
				if ((tempInt >= -100 && tempInt <= 1000 && tempInt != 0) || arg3.equals("0"))
				{
					targetOp.value4 = ""+tempInt;
					c.sendln("Energy regeneration bonus set.");
					Database.saveObjProto(targetOp);
					return;
				}
				c.sendln("The regen bonus must be a number between -100 and 1000.");
				return;
			}
			sysLog("bugs", "Unmatched value/type: "+targetOp.id+" value4");
			return;
		}
		if ("value5".startsWith(arg2) || "v5".startsWith(arg2))
		{
			String temp = Flags.objTypes.get(targetOp.type)[4];
			if (temp.length() == 0)
			{
				c.sendln("This object type does not use the value5 field.");
				return;
			}
			if (temp.startsWith("(S) Spell"))
			{
				if (arg3.length() == 0 || arg3.equals("none"))
				{
					targetOp.value5 = "";
					c.sendln("Spell cleared.");
					Database.saveObjProto(targetOp);
					return;
				}
				for (Skill s : MudMain.skills)
					if (s.type.equals("spell") && s.name.startsWith(arg3))
					{
						targetOp.value5 = s.name;
						c.sendln("Spell set to "+s.name+".");
						Database.saveObjProto(targetOp);
						return;
					}
				c.sendln("No spell by that name was found.");
				return;
			}
			sysLog("bugs", "Unmatched value/type: "+targetOp.id+" value5");
			return;
		}
		if ("statmod".startsWith(arg2))
		{
			arg3 = CommandHandler.getArg(args, 3).toLowerCase();
			String arg4 = CommandHandler.getArg(args, 4);
			if (arg3.length() == 0)
			{
				c.sendln("Set what stat bonus? See 'help statnames' for a list.");
				return;
			}
			
			for (String s : Flags.statNames)
			{
				if (s.startsWith(arg3))
				{
					if (arg4.length() == 0)
					{
						c.sendln("Set the object's '"+s+"' modifier to what?");
						return;
					}
					int statMod = Fmt.getInt(arg4);
					if (statMod == 0 && !arg4.equals("0"))
					{
						c.sendln("That's not a valid stat modifier value.");
						return;
					}
					if (statMod == 0)
					{
						targetOp.statMods.remove(s);
						Database.saveObjProto(targetOp);
						c.sendln("The object's '"+s+"' bonus has been cleared.");
						return;
					}
					else
					{
						targetOp.statMods.put(s, statMod);
						Database.saveObjProto(targetOp);
						c.sendln("The object's '"+s+"' bonus has been set to "+statMod+".");
						return;
					}
				}
			}
			c.sendln("That's not a valid stat name. See 'help statnames' for a list.");
			return;
		}
		
		if ("ed".startsWith(arg2))
		{
			arg3 = CommandHandler.getArg(args, 3).toLowerCase();
			String arg4 = CommandHandler.getLastArg(args, 4).toLowerCase();
			
			if (arg3.length() == 0)
			{
				c.sendln("Add/remove/edit an extra description?");
				return;
			}
			if (arg4.length() == 0)
			{
				c.sendln("Add/remove/edit what extra description?");
				return;
			}
			if ("remove".startsWith(arg3) || "delete".startsWith(arg3))
			{
				for (String s : targetOp.eds.keySet())
					if (s.startsWith(arg4))
					{
						targetOp.eds.remove(s);
						c.sendln("Extra description removed.");
						Database.saveObjProto(targetOp);
						return;
					}
				c.sendln("That extra description was not found.");
				return;
			}
			if ("add".startsWith(arg3))
			{
				for (String s : targetOp.eds.keySet())
					if (s.equals(arg4))
					{
						c.sendln("That extra description is already added.");
						return;
					}
				c.sendln("Now editing extra description '"+arg4+"' for: "+targetOp.name);
				c.editMode("OeditExtradesc", targetOp.id+"/"+arg4, "");
				return;
			}
			if ("edit".startsWith(arg3))
			{
				for (String s : targetOp.eds.keySet())
					if (s.startsWith(arg4))
					{
						c.sendln("Now editing extra description '"+arg4+"' for: "+targetOp.name);
						c.editMode("OeditExtradesc", targetOp.id+"/"+arg4, targetOp.eds.get(s));
						return;
					}
				c.sendln("There's no matching extra description on this object.");
				return;
			}
			InfoCommands.doHelp(c, "oedit");
			return;
		}
		
		// Set the description of an object prototype.
		if ("description".startsWith(arg2))
		{
			c.sendln("Now editing description for: "+targetOp.name);
			c.editMode("OeditDescription", ""+targetOp.id, targetOp.description);
			return;
		}
		
		if (c.olcMode.startsWith("oedit"))
			c.olcMatched = false;
		else
			InfoCommands.doHelp(c, "oedit");
	}
	/**
	Receive text from the prompt and give it to the object prototype the user was editing.
	
	@param c The user who is in edit mode.
	@param finishedText The entire contents of the finished editor.
	*/
	public static void prOeditDescription(UserCon c, String finishedText)
	{
		ObjProto targetOp = ObjProto.lookup(Fmt.getInt(c.promptTarget));

		if (targetOp != null)
		{
			targetOp.description = finishedText;
			Database.saveObjProto(targetOp);
			c.sendln("Object description saved.");
		}

		c.clearEditMode();
	}
	public static void prOeditExtradesc(UserCon c, String finishedText)
	{
		String id = c.promptTarget.split("\\/", 2)[0];
		String edName = c.promptTarget.split("\\/", 2)[1];
		ObjProto targetOp = ObjProto.lookup(Fmt.getInt(id));

		if (targetOp != null)
		{
			targetOp.eds.put(edName, finishedText);
			Database.saveObjProto(targetOp);
			c.sendln("Object extra description saved.");
		}

		c.clearEditMode();
	}



	/**
	Create/delete resets.
	
	@param c The user who entered this command.
	@param args A string containing any arguments to the command.
	*/
	public static void doReset(UserCon c, String args)
	{
		String arg1 = CommandHandler.getArg(args, 1).toLowerCase();
		String arg2 = CommandHandler.getArg(args, 2).toLowerCase();
		String arg3 = CommandHandler.getArg(args, 3).toLowerCase();
		String arg4 = CommandHandler.getArg(args, 4).toLowerCase();
		
		if ("list".startsWith(arg1) || arg1.length() == 0)
		{
			c.sendln("Resets in your current room:");
			if (c.ch.currentRoom.resets.size() == 0)
			{
				c.sendln("   No resets in this room.");
				return;
			}
			c.sendln(Fmt.heading(""));
			String objLocation = "in the room";
			int ctr = 1;
			c.sendln("}m No.  Type  Count  Target ID  Name");
			for (Reset r : c.ch.currentRoom.resets)
			{
				String nameString = "";
				if (r.type.equals("mob"))
					nameString = CharProto.lookup(r.subject).shortName;
				else if (r.type.equals("object") || r.type.equals("inside"))
					nameString = ObjProto.lookup(r.subject).shortName;
				else if (r.type.equals("lootgroup"))
					nameString = Lootgroup.lookup(r.subject).name;
				
				c.sendln("}M[}n"+Fmt.rfit(""+ctr, 2)+"}M] "
						+" [ }n"+r.type.toUpperCase().charAt(0)+"}M] "
						+" [}n"+Fmt.rfit(""+r.count, 3)+"}M] "
						+" [}n"+Fmt.rfit(""+r.subject, 7)+"}M] "
						+" }N"+nameString);
				ctr++;
			}
			c.sendln(Fmt.heading("")+"{x");
			return;
		}
		
		if ("now".startsWith(arg1))
		{
			boolean mobOnly = false;
			for (Reset rs : c.ch.currentRoom.resets)
			{
				if ((rs.type.equals("object") || rs.type.equals("lootgroup")) && !mobOnly)
					rs.fillReset();
				else if (rs.type.equals("mob"))
				{
					rs.fillReset();
					mobOnly = true;
				}
			}
			c.sendln("Room reset.");
			return;
		}
		
		int resetNr = Fmt.getInt(arg1)-1;
		if (arg1.equals("add"))
			resetNr = c.ch.currentRoom.resets.size();
		if (resetNr > c.ch.currentRoom.resets.size())
			resetNr = c.ch.currentRoom.resets.size();
		if (resetNr < 0)
		{
			c.sendln("That's not a valid reset number. Use 'reset' to view existing resets.");
			return;
		}
		
		if (arg2.length() == 0)
		{
			InfoCommands.doHelp(c, "reset");
			return;
		}
		
		if ("mob".startsWith(arg2))
		{
			if (arg3.length() == 0)
			{
				c.sendln("Add what mob?");
				return;
			}
			int mobId = Fmt.getInt(arg3);
			if (CharProto.lookup(mobId) == null)
			{
				c.sendln("That's not a valid mob ID.");
				return;
			}
			int count = Fmt.getInt(arg4);
			if (count < 1 || arg4.length() == 0)
				count = 1;
			
			Reset newReset = new Reset(0);
			newReset.type = "mob";
			newReset.location = c.ch.currentRoom;
			newReset.subject = mobId;
			newReset.count = count;
			Database.newReset(newReset);
			c.ch.currentRoom.resets.add(resetNr, newReset);
			for (Reset rs : c.ch.currentRoom.resets)
				Database.saveReset(rs);
			c.sendln("Reset added.");
			return;
		}
		
		if ("lootgroup".startsWith(arg2))
		{
			if (arg3.length() == 0)
			{
				c.sendln("Add what lootgroup?");
				return;
			}
			int lootId = Fmt.getInt(arg3);
			if (Lootgroup.lookup(lootId) == null)
			{
				c.sendln("That's not a valid lootgroup ID.");
				return;
			}
			int count = Fmt.getInt(arg4);
			if (count < 1 || arg4.length() == 0)
				count = 1;
			
			Reset newReset = new Reset(0);
			newReset.type = "lootgroup";
			newReset.location = c.ch.currentRoom;
			newReset.subject = lootId;
			newReset.count = count;
			Database.newReset(newReset);
			c.ch.currentRoom.resets.add(resetNr, newReset);
			for (Reset rs : c.ch.currentRoom.resets)
				Database.saveReset(rs);
			c.sendln("Reset added.");
			return;
		}
		
		if ("object".startsWith(arg2))
		{
			if (arg3.length() == 0)
			{
				c.sendln("Add what object?");
				return;
			}
			int objId = Fmt.getInt(arg3);
			if (ObjProto.lookup(objId) == null)
			{
				c.sendln("That's not a valid object ID.");
				return;
			}
			
			Reset newReset = new Reset(0);
			newReset.type = "object";
			newReset.location = c.ch.currentRoom;
			newReset.subject = objId;
			newReset.count = 1;
			Database.newReset(newReset);
			c.ch.currentRoom.resets.add(resetNr, newReset);
			for (Reset rs : c.ch.currentRoom.resets)
				Database.saveReset(rs);
			c.sendln("Reset added.");
			return;
		}
		
		if ("inside".startsWith(arg2))
		{
			if (arg3.length() == 0)
			{
				c.sendln("Add what object?");
				return;
			}
			int objId = Fmt.getInt(arg3);
			if (resetNr == 0)
			{
				c.sendln("An 'inside' reset can't be the first reset in the room.");
				return;
			}

			ObjProto tempOp = ObjProto.lookup(c.ch.currentRoom.resets.get(resetNr-1).subject);
			if (tempOp.type.equals("container")
				|| c.ch.currentRoom.resets.get(resetNr-1).type.equals("inside"))
			{
				Reset newReset = new Reset(0);
				newReset.type = "inside";
				newReset.location = c.ch.currentRoom;
				newReset.subject = objId;
				newReset.count = 1;
				Database.newReset(newReset);
				c.ch.currentRoom.resets.add(resetNr, newReset);
				for (Reset rs : c.ch.currentRoom.resets)
					Database.saveReset(rs);
				c.sendln("Reset added.");
				return;
			}
			c.sendln("You can't add 'inside' resets to objects that aren't containers.");
			return;
		}
		
		if ("delete".startsWith(arg2))
		{
			if (resetNr >= c.ch.currentRoom.resets.size())
			{
				c.sendln("That's not a valid reset number. Use 'reset' to view existing resets.");
				return;
			}
			
			Database.deleteReset(c.ch.currentRoom.resets.get(resetNr));
			c.ch.currentRoom.resets.remove(resetNr);
			for (Reset rs : c.ch.currentRoom.resets)
				Database.saveReset(rs);
			c.sendln("Reset deleted. Make sure you check resets which appeared below this one - they're not deleted.");
			return;
		}
		
		InfoCommands.doHelp(c, "reset");
	}
	
	
	
	/**
	Create/edit/delete room progs.
	
	@param c The user who entered this command.
	@param args A string containing any arguments to the command.
	*/
	public static void doRpedit(UserCon c, String args)
	{
		String arg1 = CommandHandler.getArg(args, 1).toLowerCase();
		String arg2 = CommandHandler.getArg(args, 2).toLowerCase();
		String arg3 = CommandHandler.getLastArg(args, 3);
		
		// Create a new rprog.
		if ("create".startsWith(arg1) || (c.olcMode.length() > 0 && "create".equals(arg2)))
		{
			int newId = Fmt.getInt(arg2);
			if (c.olcMode.length() > 0)
				newId = Fmt.getInt(arg3);
			if (newId < 1)
			{
				c.sendln("That's not a valid rprog ID.");
				return;
			}
			if (!Database.newRprog(newId))
			{
				c.sendln("A rprog with that ID already exists.");
				return;
			}
			c.olcMode = "rpedit "+newId;
			c.sendln("OLC mode set: rpedit "+newId);
			for (Area a : areas)
				if (a.start <= newId && a.end >= newId)
				{
					c.sendln("New rprog in '"+a.name+"' created.");
					return;
				}
			c.sendln("New rprog created.^/{RWARNING: {wThe rprog ID does not fit in any existing areas. It currently has no area.{x");
			return;
		}

		// List the ID and title of all roomprogs in the current area target.
		if ("list".startsWith(arg1) || (c.olcMode.length() > 0 && "list".equals(arg2)))
		{
			ArrayList<String> ast = new ArrayList<String>();
			if (c.ch.currentArea() == null)
			{
				c.sendln("You're not currently in an area. Listing all rprogs outside of any area's range:");
				boolean printRprog = true;
				for (RoomProg rp : rprogs)
				{
					printRprog = true;
					for (Area a : areas)
					{
						if (a.start <= rp.id && a.end >= rp.id)
						{
							printRprog = false;
							break;
						}
						if (printRprog)
							ast.add("#}n"+rp.id+"}M: }n"+rp.name);
					}
				}
				c.sendln(Fmt.defaultTextColumns(ast.toArray(new String[0])));
				return;
			}
			
			c.sendln("Showing all rprogs in "+c.ch.currentArea().name+":");
			for (RoomProg rp : rprogs)
				if (c.ch.currentArea().start <= rp.id && c.ch.currentArea().end >= rp.id)
					ast.add("#}n"+rp.id+"}M: }n"+rp.name);
			c.sendln(Fmt.defaultTextColumns(ast.toArray(new String[0])));
			return;
		}

		
		RoomProg targetRprog = RoomProg.lookup(Fmt.getInt(arg1));

		if (targetRprog == null)
		{
			c.sendln("That rprog ID was not found.");
			return;
		}
		if (arg2.length() == 0)
		{
			c.olcMode = "rpedit "+targetRprog.id;
			c.sendln("OLC mode set: rpedit");
			return;
		}

		if ("info".startsWith(arg2))
		{
			c.sendln(Fmt.heading(""));
			c.sendln("}m    Prog ID}M:}n "+targetRprog.id);
			c.sendln("}m  Prog Name}M:}n "+targetRprog.name);
			c.sendln("}mDescription}M:^/}N"+targetRprog.description);
			c.sendln(Fmt.heading("")+"{x");
			return;
		}
		if ("delete".startsWith(arg2))
		{
			if (c.olcMode.startsWith("rpedit "+targetRprog.id))
				c.olcMode = "";
			Database.deleteRprog(targetRprog);
			c.sendln("Rprog deleted.");
			return;
		}
		
		// Set the name of a rprog.
		if ("name".startsWith(arg2))
		{
			if (arg3.length() > 0)
			{
				targetRprog.name = arg3;
				Database.saveRprog(targetRprog);
				c.sendln("Rprog name set.");
				return;
			}
			c.sendln("The new rprog name can't be blank.");
			return;
		}

		// Set the code of a roomprog.
		if ("code".startsWith(arg2))
		{
			String codeString = "";
			for (String s : targetRprog.code)
				codeString = codeString+"^/"+s;
			if (codeString.length() > 2)
				codeString = codeString.substring(2);
			
			c.sendln("Now editing code for: "+targetRprog.id);
			c.editMode("RpeditCode", ""+targetRprog.id, codeString);
			return;
		}
		
		// Set the description of a roomprog.
		if ("description".startsWith(arg2))
		{
			c.sendln("Now editing description for: "+targetRprog.id);
			c.editMode("RpeditDescription", ""+targetRprog.id, targetRprog.description);
			return;
		}
		
		if (c.olcMode.startsWith("rpedit "))
			c.olcMatched = false;
		else
			InfoCommands.doHelp(c, "rpedit");
	}
	/**
	Receive text from the prompt and give it to the roomprog the user was editing.
	
	@param c The user who is in edit mode.
	@param finishedText The entire contents of the finished editor.
	*/
	public static void prRpeditCode(UserCon c, String finishedText)
	{
		RoomProg targetRprog = RoomProg.lookup(Fmt.getInt(c.promptTarget));

		if (targetRprog != null)
		{
			targetRprog.code = new ArrayList<String>();
			String lines[] = finishedText.split("\\^/");
			for (String line : lines)
				targetRprog.code.add(line);
			targetRprog.code = Script.formatCode(c, targetRprog.code);
			Database.saveRprog(targetRprog);
			c.sendln("Rprog code saved.");
		}

		c.clearEditMode();
	}
	/**
	Receive text from the prompt and give it to the roomprog the user was editing.
	
	@param c The user who is in edit mode.
	@param finishedText The entire contents of the finished editor.
	*/
	public static void prRpeditDescription(UserCon c, String finishedText)
	{
		RoomProg targetRprog = RoomProg.lookup(Fmt.getInt(c.promptTarget));

		if (targetRprog != null)
		{
			targetRprog.description = finishedText;
			Database.saveRprog(targetRprog);
			c.sendln("Rprog description saved.");
		}

		c.clearEditMode();
	}
	
	
	
	/**
	Create/edit/delete mob progs.
	
	@param c The user who entered this command.
	@param args A string containing any arguments to the command.
	*/
	public static void doMpedit(UserCon c, String args)
	{
		String arg1 = CommandHandler.getArg(args, 1).toLowerCase();
		String arg2 = CommandHandler.getArg(args, 2).toLowerCase();
		String arg3 = CommandHandler.getLastArg(args, 3);
		
		// Create a new mprog.
		if ("create".startsWith(arg1) || (c.olcMode.length() > 0 && "create".equals(arg2)))
		{
			int newId = Fmt.getInt(arg2);
			if (c.olcMode.length() > 0)
				newId = Fmt.getInt(arg3);
			if (newId < 1)
			{
				c.sendln("That's not a valid mprog ID.");
				return;
			}
			if (!Database.newMprog(newId))
			{
				c.sendln("An mprog with that ID already exists.");
				return;
			}
			c.olcMode = "mpedit "+newId;
			c.sendln("OLC mode set: mpedit "+newId);
			for (Area a : areas)
				if (a.start <= newId && a.end >= newId)
				{
					c.sendln("New mprog in '"+a.name+"' created.");
					return;
				}
			c.sendln("New mprog created.^/{RWARNING: {wThe mprog ID does not fit in any existing areas. It currently has no area.{x");
			return;
		}

		// List the ID and title of all mobprogs in the current area target.
		if ("list".startsWith(arg1) || (c.olcMode.length() > 0 && "list".equals(arg2)))
		{
			ArrayList<String> ast = new ArrayList<String>();
			if (c.ch.currentArea() == null)
			{
				c.sendln("You're not currently in an area. Listing all mprogs outside of any area's range:");
				boolean printMprog = true;
				for (MobProg mp : mprogs)
				{
					printMprog = true;
					for (Area a : areas)
					{
						if (a.start <= mp.id && a.end >= mp.id)
						{
							printMprog = false;
							break;
						}
						if (printMprog)
							ast.add("#}n"+mp.id+"}M: }n"+mp.name);
					}
				}
				c.sendln(Fmt.defaultTextColumns(ast.toArray(new String[0])));
				return;
			}
			
			c.sendln("Showing all mprogs in "+c.ch.currentArea().name+":");
			for (MobProg mp : mprogs)
				if (c.ch.currentArea().start <= mp.id && c.ch.currentArea().end >= mp.id)
					ast.add("#}n"+mp.id+"}M: }n"+mp.name);
			c.sendln(Fmt.defaultTextColumns(ast.toArray(new String[0])));
			return;
		}

		
		MobProg targetMprog = MobProg.lookup(Fmt.getInt(arg1));

		if (targetMprog == null)
		{
			c.sendln("That mprog ID was not found.");
			return;
		}
		if (arg2.length() == 0)
		{
			c.olcMode = "mpedit "+targetMprog.id;
			c.sendln("OLC mode set: mpedit");
			return;
		}

		if ("info".startsWith(arg2))
		{
			c.sendln(Fmt.heading(""));
			c.sendln("}m    Prog ID}M:}n "+targetMprog.id);
			c.sendln("}m  Prog Name}M:}n "+targetMprog.name);
			c.sendln("}mDescription}M:^/}N"+targetMprog.description);
			c.sendln(Fmt.heading("")+"{x");
			return;
		}
		if ("delete".startsWith(arg2))
		{
			if (c.olcMode.startsWith("mpedit "+targetMprog.id))
				c.olcMode = "";
			Database.deleteMprog(targetMprog);
			c.sendln("Mprog deleted.");
			return;
		}
		
		// Set the name of a mprog.
		if ("name".startsWith(arg2))
		{
			if (arg3.length() > 0)
			{
				targetMprog.name = arg3;
				Database.saveMprog(targetMprog);
				c.sendln("Mprog name set.");
				return;
			}
			c.sendln("The new mprog name can't be blank.");
			return;
		}

		// Set the code of a mprog.
		if ("code".startsWith(arg2))
		{
			String codeString = "";
			for (String s : targetMprog.code)
				codeString = codeString+"^/"+s;
			if (codeString.length() > 2)
				codeString = codeString.substring(2);
			
			c.sendln("Now editing code for: "+targetMprog.id);
			c.editMode("MpeditCode", ""+targetMprog.id, codeString);
			return;
		}
		
		// Set the description of a mprog.
		if ("description".startsWith(arg2))
		{
			c.sendln("Now editing description for: "+targetMprog.id);
			c.editMode("MpeditDescription", ""+targetMprog.id, targetMprog.description);
			return;
		}
		
		if (c.olcMode.startsWith("mpedit "))
			c.olcMatched = false;
		else
			InfoCommands.doHelp(c, "mpedit");
	}
	/**
	Receive text from the prompt and give it to the mobprog the user was editing.
	
	@param c The user who is in edit mode.
	@param finishedText The entire contents of the finished editor.
	*/
	public static void prMpeditCode(UserCon c, String finishedText)
	{
		MobProg targetMprog = MobProg.lookup(Fmt.getInt(c.promptTarget));

		if (targetMprog != null)
		{
			targetMprog.code = new ArrayList<String>();
			String lines[] = finishedText.split("\\^/");
			for (String line : lines)
				targetMprog.code.add(line);
			targetMprog.code = Script.formatCode(c, targetMprog.code);
			Database.saveMprog(targetMprog);
			c.sendln("Mprog code saved.");
		}

		c.clearEditMode();
	}
	/**
	Receive text from the prompt and give it to the mobprog the user was editing.
	
	@param c The user who is in edit mode.
	@param finishedText The entire contents of the finished editor.
	*/
	public static void prMpeditDescription(UserCon c, String finishedText)
	{
		MobProg targetMprog = MobProg.lookup(Fmt.getInt(c.promptTarget));

		if (targetMprog != null)
		{
			targetMprog.description = finishedText;
			Database.saveMprog(targetMprog);
			c.sendln("Mprog description saved.");
		}

		c.clearEditMode();
	}




	public static void doOpedit(UserCon c, String args)
	{
		String arg1 = CommandHandler.getArg(args, 1).toLowerCase();
		String arg2 = CommandHandler.getArg(args, 2).toLowerCase();
		String arg3 = CommandHandler.getLastArg(args, 3);
		
		// Create a new oprog.
		if ("create".startsWith(arg1) || (c.olcMode.length() > 0 && "create".equals(arg2)))
		{
			int newId = Fmt.getInt(arg2);
			if (c.olcMode.length() > 0)
				newId = Fmt.getInt(arg3);
			if (newId < 1)
			{
				c.sendln("That's not a valid oprog ID.");
				return;
			}
			if (!Database.newOprog(newId))
			{
				c.sendln("An oprog with that ID already exists.");
				return;
			}
			c.olcMode = "opedit "+newId;
			c.sendln("OLC mode set: opedit "+newId);
			for (Area a : areas)
				if (a.start <= newId && a.end >= newId)
				{
					c.sendln("New oprog in '"+a.name+"' created.");
					return;
				}
			c.sendln("New oprog created.^/{RWARNING: {wThe oprog ID does not fit in any existing areas. It currently has no area.{x");
			return;
		}

		// List the ID and title of all mobprogs in the current area target.
		if ("list".startsWith(arg1) || (c.olcMode.length() > 0 && "list".equals(arg2)))
		{
			ArrayList<String> ast = new ArrayList<String>();
			if (c.ch.currentArea() == null)
			{
				c.sendln("You're not currently in an area. Listing all oprogs outside of any area's range:");
				boolean printOprog = true;
				for (ObjProg op : oprogs)
				{
					printOprog = true;
					for (Area a : areas)
					{
						if (a.start <= op.id && a.end >= op.id)
						{
							printOprog = false;
							break;
						}
						if (printOprog)
							ast.add("#}n"+op.id+"}M: }n"+op.name);
					}
				}
				c.sendln(Fmt.defaultTextColumns(ast.toArray(new String[0])));
				return;
			}
			
			c.sendln("Showing all oprogs in "+c.ch.currentArea().name+":");
			for (ObjProg op : oprogs)
				if (c.ch.currentArea().start <= op.id && c.ch.currentArea().end >= op.id)
					ast.add("#}n"+op.id+"}M: }n"+op.name);
			c.sendln(Fmt.defaultTextColumns(ast.toArray(new String[0])));
			return;
		}

		
		ObjProg targetOprog = ObjProg.lookup(Fmt.getInt(arg1));

		if (targetOprog == null)
		{
			c.sendln("That oprog ID was not found.");
			return;
		}
		if (arg2.length() == 0)
		{
			c.olcMode = "opedit "+targetOprog.id;
			c.sendln("OLC mode set: opedit");
			return;
		}

		if ("info".startsWith(arg2))
		{
			c.sendln(Fmt.heading(""));
			c.sendln("}m    Prog ID}M:}n "+targetOprog.id);
			c.sendln("}m  Prog Name}M:}n "+targetOprog.name);
			c.sendln("}mDescription}M:^/}N"+targetOprog.description);
			c.sendln(Fmt.heading("")+"{x");
			return;
		}
		if ("delete".startsWith(arg2))
		{
			if (c.olcMode.startsWith("opedit "+targetOprog.id))
				c.olcMode = "";
			Database.deleteOprog(targetOprog);
			c.sendln("Oprog deleted.");
			return;
		}
		
		// Set the name of a oprog.
		if ("name".startsWith(arg2))
		{
			if (arg3.length() > 0)
			{
				targetOprog.name = arg3;
				Database.saveOprog(targetOprog);
				c.sendln("Oprog name set.");
				return;
			}
			c.sendln("The new oprog name can't be blank.");
			return;
		}

		// Set the code of a oprog.
		if ("code".startsWith(arg2))
		{
			String codeString = "";
			for (String s : targetOprog.code)
				codeString = codeString+"^/"+s;
			if (codeString.length() > 2)
				codeString = codeString.substring(2);
			
			c.sendln("Now editing code for: "+targetOprog.id);
			c.editMode("OpeditCode", ""+targetOprog.id, codeString);
			return;
		}
		
		// Set the description of a oprog.
		if ("description".startsWith(arg2))
		{
			c.sendln("Now editing description for: "+targetOprog.id);
			c.editMode("OpeditDescription", ""+targetOprog.id, targetOprog.description);
			return;
		}
		
		if (c.olcMode.startsWith("opedit "))
			c.olcMatched = false;
		else
			InfoCommands.doHelp(c, "opedit");
	}
	/**
	Receive text from the prompt and give it to the objprog the user was editing.
	
	@param c The user who is in edit mode.
	@param finishedText The entire contents of the finished editor.
	*/
	public static void prOpeditCode(UserCon c, String finishedText)
	{
		ObjProg targetOprog = ObjProg.lookup(Fmt.getInt(c.promptTarget));

		if (targetOprog != null)
		{
			targetOprog.code = new ArrayList<String>();
			String lines[] = finishedText.split("\\^/");
			for (String line : lines)
				targetOprog.code.add(line);
			targetOprog.code = Script.formatCode(c, targetOprog.code);
			Database.saveOprog(targetOprog);
			c.sendln("Oprog code saved.");
		}

		c.clearEditMode();
	}
	/**
	Receive text from the prompt and give it to the objprog the user was editing.
	
	@param c The user who is in edit mode.
	@param finishedText The entire contents of the finished editor.
	*/
	public static void prOpeditDescription(UserCon c, String finishedText)
	{
		ObjProg targetOprog = ObjProg.lookup(Fmt.getInt(c.promptTarget));

		if (targetOprog != null)
		{
			targetOprog.description = finishedText;
			Database.saveOprog(targetOprog);
			c.sendln("Oprog description saved.");
		}

		c.clearEditMode();
	}
	
	
	
	public static void doQedit(UserCon c, String args)
	{
		String arg1 = CommandHandler.getArg(args, 1).toLowerCase();
		String arg2 = CommandHandler.getArg(args, 2).toLowerCase();
		String arg3 = CommandHandler.getLastArg(args, 3);
		
		// Create a new quest.
		if ("create".startsWith(arg1) || (c.olcMode.length() > 0 && "create".equals(arg2)))
		{
			int newId = Fmt.getInt(arg2);
			if (c.olcMode.length() > 0)
				newId = Fmt.getInt(arg3);
			if (newId < 1)
			{
				c.sendln("That's not a valid quest ID.");
				return;
			}
			if (!Database.newQuest(newId))
			{
				c.sendln("A quest with that ID already exists.");
				return;
			}
			c.olcMode = "qedit "+newId;
			c.sendln("OLC mode set: qedit "+newId);
			for (Area a : areas)
				if (a.start <= newId && a.end >= newId)
				{
					c.sendln("New quest in '"+a.name+"' created.");
					return;
				}
			c.sendln("New quest created.^/{RWARNING: {wThe quest ID does not fit in any existing areas. It currently has no area.{x");
			return;
		}

		// List the ID and title of all quests in the current area target.
		if ("list".startsWith(arg1) || (c.olcMode.length() > 0 && "list".equals(arg2)))
		{
			ArrayList<String> ast = new ArrayList<String>();
			if (c.ch.currentArea() == null)
			{
				c.sendln("You're not currently in an area. Listing all quests outside of any area's range:");
				boolean printQuest = true;
				for (Quest q : quests)
				{
					printQuest = true;
					for (Area a : areas)
					{
						if (a.start <= q.id && a.end >= q.id)
						{
							printQuest = false;
							break;
						}
						if (printQuest)
							ast.add("#}n"+q.id+"}M: }n"+q.name);
					}
				}
				c.sendln(Fmt.defaultTextColumns(ast.toArray(new String[0])));
				return;
			}
			
			c.sendln("Showing all quests in "+c.ch.currentArea().name+":");
			for (Quest q : quests)
				if (c.ch.currentArea().start <= q.id && c.ch.currentArea().end >= q.id)
					ast.add("#}n"+q.id+"}M: }n"+q.name);
			c.sendln(Fmt.defaultTextColumns(ast.toArray(new String[0])));
			return;
		}

		
		Quest targetQuest = Quest.lookup(Fmt.getInt(arg1));

		if (targetQuest == null)
		{
			c.sendln("That quest ID was not found.");
			return;
		}
		if (arg2.length() == 0)
		{
			c.olcMode = "qedit "+targetQuest.id;
			c.sendln("OLC mode set: qedit");
			return;
		}

		if ("info".startsWith(arg2))
		{
			c.sendln(Fmt.heading(""));
			c.sendln("}m  Quest ID}M:}n "+targetQuest.id);
			c.sendln("}m      Name}M:}n "+targetQuest.name);
			c.sendln("}mMin. Level}M:}n "+targetQuest.minLevel);
			c.sendln("}mMax. Level}M:}n "+targetQuest.maxLevel);
			c.sendln("}mDifficulty}M:}n "+targetQuest.difficulty);
			
			String fString = "";
			for (String s : Flags.questFlags)
				if (targetQuest.flags.get(s) != null)
					if (targetQuest.flags.get(s))
						fString = fString+s+" ";
			if (fString.length() == 0)
				fString = "none";
			else
				fString = fString.trim();

			c.sendln("}m     Flags}M:}n "+fString);
			c.sendln(Fmt.heading("Description"));
			c.sendln("}N"+targetQuest.description);
			c.sendln(Fmt.heading("")+"{x");
			return;
		}
		if ("delete".startsWith(arg2))
		{
			if (c.olcMode.startsWith("qedit "+targetQuest.id))
				c.olcMode = "";
			Database.deleteQuest(targetQuest);
			c.sendln("Quest deleted.");
			return;
		}
		
		if ("minlevel".startsWith(arg2))
		{
			int level = Fmt.getInt(arg3);
			if (level >= Flags.minLevel && level <= Flags.maxLevel)
			{
				targetQuest.minLevel = level;
				Database.saveQuest(targetQuest);
				c.sendln("Quest minimum level set.");
				return;
			}
			c.sendln("That's not a valid level. Minimum level must be in the range "+Flags.minLevel+" - "+Flags.maxLevel+".");
			return;
		}
		
		if ("maxlevel".startsWith(arg2))
		{
			int level = Fmt.getInt(arg3);
			if (level >= Flags.minLevel && level <= Flags.maxLevel)
			{
				targetQuest.maxLevel = level;
				Database.saveQuest(targetQuest);
				c.sendln("Quest maximum level set.");
				return;
			}
			c.sendln("That's not a valid level. Maximum level must be in the range "+Flags.minLevel+" - "+Flags.maxLevel+".");
			return;
		}
		
		if ("prereq".startsWith(arg2))
		{
			int id = Fmt.getInt(arg3);
			if (Quest.lookup(id) == null)
			{
				c.sendln("That's not a valid quest ID.");
				return;
			}
			if (targetQuest.prereqs.remove((Object)id))
			{
				Database.saveQuest(targetQuest);
				c.sendln("Prerequisite quest #"+id+" removed.");
				return;
			}
			targetQuest.prereqs.add(id);
			Database.saveQuest(targetQuest);
			c.sendln("Prerequisite quest #"+id+" added.");
			return;
		}
		
		if ("flags".startsWith(arg2))
		{
			if (arg3.length() == 0)
			{
				c.sendln("Set what flags? See 'help questflags' for a list.");
				return;
			}
			String[] flags = arg3.split(" ");
			for (String s : flags)
			{
				Boolean flagFound = false;
				s = s.trim().toLowerCase();
				if (s.length() == 0)
					continue;
				for (String fs : Flags.questFlags)
				{
					if (fs.startsWith(s))
					{
						if (targetQuest.flags.get(fs))
						{
							targetQuest.flags.put(fs, false);
							c.sendln("'"+fs+"' removed.");
						}
						else
						{
							targetQuest.flags.put(fs, true);
							c.sendln("'"+fs+"' set.");
						}
						flagFound = true;
						break;
					}
				}
				if (!flagFound)
				{
					c.sendln("'"+s+"' is not a valid quest flag.");
					continue;
				}
			}
			Database.saveQuest(targetQuest);
			return;
		}

		// Set the name of a quest.
		if ("name".startsWith(arg2))
		{
			if (arg3.length() > 0)
			{
				targetQuest.name = arg3;
				Database.saveQuest(targetQuest);
				c.sendln("Quest name set.");
				return;
			}
			c.sendln("The new quest name can't be blank.");
			return;
		}

		// Set the check code of the quest.
		if ("check".startsWith(arg2))
		{
			String codeString = "";
			for (String s : targetQuest.check.code)
				codeString = codeString+"^/"+s;
			if (codeString.length() > 2)
				codeString = codeString.substring(2);
			
			c.sendln("Now editing check code for: "+targetQuest.id);
			c.editMode("QeditCheck", ""+targetQuest.id, codeString);
			return;
		}

		// Set the offer code of the quest.
		if ("offer".startsWith(arg2))
		{
			String codeString = "";
			for (String s : targetQuest.offer.code)
				codeString = codeString+"^/"+s;
			if (codeString.length() > 2)
				codeString = codeString.substring(2);
			
			c.sendln("Now editing offer code for: "+targetQuest.id);
			c.editMode("QeditOffer", ""+targetQuest.id, codeString);
			return;
		}

		// Set the accept code of the quest.
		if ("accept".startsWith(arg2))
		{
			String codeString = "";
			for (String s : targetQuest.accept.code)
				codeString = codeString+"^/"+s;
			if (codeString.length() > 2)
				codeString = codeString.substring(2);
			
			c.sendln("Now editing accept code for: "+targetQuest.id);
			c.editMode("QeditAccept", ""+targetQuest.id, codeString);
			return;
		}

		// Set the complete code of the quest.
		if ("complete".startsWith(arg2))
		{
			String codeString = "";
			for (String s : targetQuest.complete.code)
				codeString = codeString+"^/"+s;
			if (codeString.length() > 2)
				codeString = codeString.substring(2);
			
			c.sendln("Now editing complete code for: "+targetQuest.id);
			c.editMode("QeditComplete", ""+targetQuest.id, codeString);
			return;
		}
		
		// Set the description of a quest.
		if ("description".startsWith(arg2))
		{
			c.sendln("Now editing description for: "+targetQuest.id);
			c.editMode("QeditDescription", ""+targetQuest.id, targetQuest.description);
			return;
		}
		
		if (c.olcMode.startsWith("qedit "))
			c.olcMatched = false;
		else
			InfoCommands.doHelp(c, "qedit");
	}
	public static void prQeditCheck(UserCon c, String finishedText)
	{
		Quest targetQuest = Quest.lookup(Fmt.getInt(c.promptTarget));

		if (targetQuest != null)
		{
			targetQuest.check.code = new ArrayList<String>();
			String lines[] = finishedText.split("\\^/");
			for (String line : lines)
				targetQuest.check.code.add(line);
			targetQuest.check.code = Script.formatCode(c, targetQuest.check.code);
			Database.saveQuest(targetQuest);
			c.sendln("Quest check code saved.");
		}

		c.clearEditMode();
	}
	public static void prQeditOffer(UserCon c, String finishedText)
	{
		Quest targetQuest = Quest.lookup(Fmt.getInt(c.promptTarget));

		if (targetQuest != null)
		{
			targetQuest.offer.code = new ArrayList<String>();
			String lines[] = finishedText.split("\\^/");
			for (String line : lines)
				targetQuest.offer.code.add(line);
			targetQuest.offer.code = Script.formatCode(c, targetQuest.offer.code);
			Database.saveQuest(targetQuest);
			c.sendln("Quest offer code saved.");
		}

		c.clearEditMode();
	}
	public static void prQeditAccept(UserCon c, String finishedText)
	{
		Quest targetQuest = Quest.lookup(Fmt.getInt(c.promptTarget));

		if (targetQuest != null)
		{
			targetQuest.accept.code = new ArrayList<String>();
			String lines[] = finishedText.split("\\^/");
			for (String line : lines)
				targetQuest.accept.code.add(line);
			targetQuest.accept.code = Script.formatCode(c, targetQuest.accept.code);
			Database.saveQuest(targetQuest);
			c.sendln("Quest accept code saved.");
		}

		c.clearEditMode();
	}
	public static void prQeditComplete(UserCon c, String finishedText)
	{
		Quest targetQuest = Quest.lookup(Fmt.getInt(c.promptTarget));

		if (targetQuest != null)
		{
			targetQuest.complete.code = new ArrayList<String>();
			String lines[] = finishedText.split("\\^/");
			for (String line : lines)
				targetQuest.complete.code.add(line);
			targetQuest.complete.code = Script.formatCode(c, targetQuest.complete.code);
			Database.saveQuest(targetQuest);
			c.sendln("Quest complete code saved.");
		}

		c.clearEditMode();
	}
	public static void prQeditDescription(UserCon c, String finishedText)
	{
		Quest targetQuest = Quest.lookup(Fmt.getInt(c.promptTarget));

		if (targetQuest != null)
		{
			targetQuest.description = finishedText;
			Database.saveQuest(targetQuest);
			c.sendln("Quest description saved.");
		}

		c.clearEditMode();
	}




	public static void doQoedit(UserCon c, String args)
	{
		String arg1 = CommandHandler.getArg(args, 1).toLowerCase();
		String arg2 = CommandHandler.getArg(args, 2).toLowerCase();
		String arg3 = CommandHandler.getLastArg(args, 3);
		
		// Create a new quest objective.
		if ("create".startsWith(arg1) || (c.olcMode.length() > 0 && "create".equals(arg2)))
		{
			String ids[] = arg2.split("\\.");
			if (c.olcMode.length() > 0)
				ids = arg3.split("\\.");
			if (ids.length < 2)
			{
				c.sendln("Invalid quest objective ID format. Use }H<}iquest ID}H>}h.}H<objective #}H>{x.");
				return;
			}
			int questId = Fmt.getInt(ids[0]);
			int objId = Fmt.getInt(ids[1]);
			
			Quest targetQuest = Quest.lookup(questId);
			if (targetQuest == null)
			{
				c.sendln("There is no quest by that ID.");
				return;
			}
			if (objId <= 0 || objId > targetQuest.objectives.size()+1)
			{
				c.sendln("The new objective ID must be between 1 and (objective count)+1.");
				return;
			}
			
			Database.newQuestObjective(targetQuest, objId);

			c.olcMode = "qoedit "+questId+"."+objId;
			c.sendln("OLC mode set: qoedit "+questId+"."+objId);
			c.sendln("New quest objective added to quest #"+questId);
			return;
		}
		
		String ids[] = arg1.split("\\.");
		if (ids.length < 2)
		{
			c.sendln("Invalid quest objective ID format. Use }H<}iquest ID}H>}h.}H<objective #}H>{x.");
			return;
		}
		int questId = Fmt.getInt(ids[0]);
		int objId = Fmt.getInt(ids[1]);

		Quest targetQuest = Quest.lookup(Fmt.getInt(arg1));
		if (targetQuest == null)
		{
			c.sendln("That quest ID was not found.");
			return;
		}

		if (objId <= 0 || objId > targetQuest.objectives.size())
		{
			c.sendln("That objective number was not found for that quest.");
			return;
		}
		QuestObjective targetQo = targetQuest.objectives.get(objId);

		if (arg2.length() == 0)
		{
			c.olcMode = "qoedit "+questId+"."+objId;
			c.sendln("OLC mode set: qoedit");
			return;
		}

		if ("info".startsWith(arg2))
		{
			c.sendln(Fmt.heading(""));
			c.sendln("}mObjective ID}M:}n "+questId+"."+objId);
			c.sendln("}m        Name}M:}n "+targetQo.name);
			
			String fString = "";
			for (String s : Flags.questObjectiveFlags)
				if (targetQo.flags.get(s) != null)
					if (targetQo.flags.get(s))
						fString = fString+s+" ";
			if (fString.length() == 0)
				fString = "none";
			else
				fString = fString.trim();

			c.sendln("}m       Flags}M:}n "+fString);
			if (targetQo.objType.size() == 0)
				c.sendln("}m       Tasks}M:}n none");
			else
			{
				c.sendln("}m       Tasks}M:}n }M[       }mType       }M] / [ }mID }M] / [ }mCount}M ]");
				for (int ctr = 0; ctr < targetQo.objType.size(); ctr++)
					c.sendln("         "+Fmt.rfit("}m#}n"+(ctr+1), 3)+"}M:}n "+Fmt.fit(targetQo.objType.get(ctr), 20)+" }M/}n "+Fmt.fit(""+targetQo.objId.get(ctr), 6)+" }M/}n "+Fmt.fit(""+targetQo.objCount.get(ctr), 4)+"{x");
			}
			c.sendln("^/}mAdded Triggers}M:");
			if (targetQo.triggers.size() == 0)
				c.sendln("}m  None");
			int ctr = 0;
			for (Trigger t : targetQo.triggers)
			{
				ctr++;
				c.sendln("}m #}n"+ctr+"}M: }NTrigger: }n"+t.type+" }M- }NArgs: }n"+t.numArg+"}M/}n"+t.arg);
			}
			c.sendln(Fmt.heading("Description"));
			c.sendln("}N"+targetQo.description);
			c.sendln(Fmt.heading("")+"{x");
			return;
		}
		if ("delete".startsWith(arg2))
		{
			if (c.olcMode.startsWith("qoedit "+questId+"."+objId))
				c.olcMode = "";
			Database.deleteQuestObjective(targetQuest, targetQo);
			c.sendln("Quest objective deleted.");
			return;
		}
		
		if ("move".startsWith(arg2))
		{
			int newPos = Fmt.getInt(arg3);
			if (newPos <= 0)
			{
				c.sendln("That objective number is invalid.");
				return;
			}
			if (newPos > targetQuest.objectives.size())
				newPos = targetQuest.objectives.size();
			
			targetQuest.objectives.remove(targetQo);
			targetQuest.objectives.add(newPos, targetQo);
			
			Database.moveQuestObjective(targetQuest, targetQo, newPos);
			c.sendln("Objective moved to #"+newPos+".");
			return;
		}
				
		if ("flags".startsWith(arg2))
		{
			if (arg3.length() == 0)
			{
				c.sendln("Set what flags? See 'help qoflags' for a list.");
				return;
			}
			String[] flags = arg3.split(" ");
			for (String s : flags)
			{
				Boolean flagFound = false;
				s = s.trim().toLowerCase();
				if (s.length() == 0)
					continue;
				for (String fs : Flags.questObjectiveFlags)
				{
					if (fs.startsWith(s))
					{
						if (targetQo.flags.get(fs))
						{
							targetQo.flags.put(fs, false);
							c.sendln("'"+fs+"' removed.");
						}
						else
						{
							targetQo.flags.put(fs, true);
							c.sendln("'"+fs+"' set.");
						}
						flagFound = true;
						break;
					}
				}
				if (!flagFound)
				{
					c.sendln("'"+s+"' is not a valid quest objective flag.");
					continue;
				}
			}
			Database.saveQuestObjective(targetQo);
			return;
		}
		
		if ("addtask".startsWith(arg2))
		{
			arg3 = CommandHandler.getArg(args, 3);
			String arg4 = CommandHandler.getArg(args, 4);
			String arg5 = CommandHandler.getArg(args, 5);
			
			
		}
		
		if ("deltask".startsWith(arg2))
		{
			int targetNr = Fmt.getInt(arg3);
			if (targetNr < 1 || targetNr > targetQo.objType.size())
			{
				c.sendln("There aren't that many tasks on this quest objective.");
				return;
			}
			targetQo.objType.remove(targetNr-1);
			targetQo.objId.remove(targetNr-1);
			targetQo.objCount.remove(targetNr-1);
			Database.saveQuestObjective(targetQo);
			c.sendln("Task deleted from this objective.");
			return;
		}
		
		if ("addtrigger".startsWith(arg2))
		{
			arg3 = CommandHandler.getArg(args, 3);
			String arg4 = CommandHandler.getArg(args, 4);
			String arg5 = CommandHandler.getLastArg(args, 5);
			
			for (String s : Flags.mprogTriggerTypes)
				if (s.equalsIgnoreCase(arg3))
				{
					Trigger newTrigger = new Trigger();
					newTrigger.type = s;
					newTrigger.arg = arg5;
					newTrigger.numArg = Fmt.getInt(arg4);
					newTrigger.qo = targetQo;
					targetQo.triggers.add(newTrigger);
					Database.saveQuestObjective(targetQo);
					c.sendln("Trigger added to this objective.");
					return;
				}
			
			String temp = "";
			for (String s : Flags.mprogTriggerTypes)
				temp = temp+s+" ";
			c.sendln("That's not a valid character trigger.^/Valid triggers are: "+temp.trim());
			return;
		}
		
		if ("deltrigger".startsWith(arg2))
		{
			int targetNr = Fmt.getInt(arg3);
			if (targetNr < 1 || targetNr > targetQo.triggers.size())
			{
				c.sendln("There aren't that many triggers on this quest objective.");
				return;
			}
			targetQo.triggers.remove(targetNr-1);
			Database.saveQuestObjective(targetQo);
			c.sendln("Trigger deleted from this objective.");
			return;
		}

		// Set the name of a quest objective.
		if ("name".startsWith(arg2))
		{
			if (arg3.length() > 0)
			{
				targetQo.name = arg3;
				Database.saveQuestObjective(targetQo);
				c.sendln("Quest objective name set.");
				return;
			}
			c.sendln("The new quest objective name can't be blank.");
			return;
		}

		// Set the start code of the objective.
		if ("start".startsWith(arg2))
		{
			String codeString = "";
			for (String s : targetQo.start.code)
				codeString = codeString+"^/"+s;
			if (codeString.length() > 2)
				codeString = codeString.substring(2);
			
			c.sendln("Now editing start code for: "+questId+"."+objId);
			c.editMode("QoeditStart", ""+targetQo.id, codeString);
			return;
		}

		// Set the end code of the objective.
		if ("end".startsWith(arg2))
		{
			String codeString = "";
			for (String s : targetQo.end.code)
				codeString = codeString+"^/"+s;
			if (codeString.length() > 2)
				codeString = codeString.substring(2);
			
			c.sendln("Now editing end code for: "+questId+"."+objId);
			c.editMode("QoeditEnd", ""+targetQo.id, codeString);
			return;
		}

		// Set the script code of the objective.
		if ("script".startsWith(arg2))
		{
			String codeString = "";
			for (String s : targetQo.script.code)
				codeString = codeString+"^/"+s;
			if (codeString.length() > 2)
				codeString = codeString.substring(2);
			
			c.sendln("Now editing script code for: "+questId+"."+objId);
			c.editMode("QoeditScript", ""+targetQo.id, codeString);
			return;
		}

		// Set the check code of the objective.
		if ("check".startsWith(arg2))
		{
			String codeString = "";
			for (String s : targetQo.check.code)
				codeString = codeString+"^/"+s;
			if (codeString.length() > 2)
				codeString = codeString.substring(2);
			
			c.sendln("Now editing check code for: "+questId+"."+objId);
			c.editMode("QoeditCheck", ""+targetQo.id, codeString);
			return;
		}
		
		// Set the description of a quest.
		if ("description".startsWith(arg2))
		{
			c.sendln("Now editing description for: "+questId+"."+objId);
			c.editMode("QoeditDescription", ""+targetQo.id, targetQo.description);
			return;
		}
		
		if (c.olcMode.startsWith("qoedit "))
			c.olcMatched = false;
		else
			InfoCommands.doHelp(c, "qoedit");
	}
	public static void prQoeditStart(UserCon c, String finishedText)
	{
		QuestObjective targetQo = QuestObjective.lookup(Fmt.getInt(c.promptTarget));

		if (targetQo != null)
		{
			targetQo.start.code = new ArrayList<String>();
			String lines[] = finishedText.split("\\^/");
			for (String line : lines)
				targetQo.start.code.add(line);
			targetQo.start.code = Script.formatCode(c, targetQo.start.code);
			Database.saveQuestObjective(targetQo);
			c.sendln("Objective start code saved.");
		}

		c.clearEditMode();
	}
	public static void prQoeditEnd(UserCon c, String finishedText)
	{
		QuestObjective targetQo = QuestObjective.lookup(Fmt.getInt(c.promptTarget));

		if (targetQo != null)
		{
			targetQo.end.code = new ArrayList<String>();
			String lines[] = finishedText.split("\\^/");
			for (String line : lines)
				targetQo.end.code.add(line);
			targetQo.end.code = Script.formatCode(c, targetQo.end.code);
			Database.saveQuestObjective(targetQo);
			c.sendln("Objective end code saved.");
		}

		c.clearEditMode();
	}
	public static void prQoeditScript(UserCon c, String finishedText)
	{
		QuestObjective targetQo = QuestObjective.lookup(Fmt.getInt(c.promptTarget));

		if (targetQo != null)
		{
			targetQo.script.code = new ArrayList<String>();
			String lines[] = finishedText.split("\\^/");
			for (String line : lines)
				targetQo.script.code.add(line);
			targetQo.script.code = Script.formatCode(c, targetQo.script.code);
			Database.saveQuestObjective(targetQo);
			c.sendln("Objective script code saved.");
		}

		c.clearEditMode();
	}
	public static void prQoeditCheck(UserCon c, String finishedText)
	{
		QuestObjective targetQo = QuestObjective.lookup(Fmt.getInt(c.promptTarget));

		if (targetQo != null)
		{
			targetQo.check.code = new ArrayList<String>();
			String lines[] = finishedText.split("\\^/");
			for (String line : lines)
				targetQo.check.code.add(line);
			targetQo.check.code = Script.formatCode(c, targetQo.check.code);
			Database.saveQuestObjective(targetQo);
			c.sendln("Objective check code saved.");
		}

		c.clearEditMode();
	}
	public static void prQoeditDescription(UserCon c, String finishedText)
	{
		Quest targetQuest = Quest.lookup(Fmt.getInt(c.promptTarget));

		if (targetQuest != null)
		{
			targetQuest.description = finishedText;
			Database.saveQuest(targetQuest);
			c.sendln("Quest description saved.");
		}

		c.clearEditMode();
	}

	
	
	/**
	Clear any OLC mode the user is in.
	
	@param c The user who entered this command.
	@param args A string containing any arguments to the command.
	*/
	public static void doDone(UserCon c, String args)
	{
		if (c.olcMode.length() > 0)
		{
			c.olcMode = "";
			c.sendln("OLC mode cleared.");
		}
		else
			c.sendln("You're not in OLC mode.");
	}
}