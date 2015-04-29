package jw.core;
import java.util.*;

import jw.commands.*;
import static jw.core.MudMain.*;

/**
The Room class represents a single room, with all associated fields and exit data.
*/
public class Room implements Comparable<Room>
{
	/** The room's ID number. */
	public int id;
	/** The name of this room. */
	public String name = "";
	/** Multi-line room description. */
	public String description = "";
	/** Extra descriptions. */
	public HashMap<String, String> eds = new HashMap<String, String>();
	/** Sector type. */
	public String sector = "";
	/** Occupancy limit. */
	public int occLimit = 0;
	/** Flags. */
	public HashMap<String, Boolean> flags = new HashMap<String, Boolean>();
	/** Effects. */
	public ArrayList<Effect> effects = new ArrayList<Effect>();
	/** All exits available from this room. */
	public ArrayList<Exit> exits = new ArrayList<Exit>();
	/** All room progs active in this room. */
	public ArrayList<Trigger> triggers = new ArrayList<Trigger>();
	/** The list of objects located in this room. */
	public ArrayList<ObjData> objects = new ArrayList<ObjData>();
	/** All resets active in this room. */
	public ArrayList<Reset> resets = new ArrayList<Reset>();
	/** The quests this room can offer to players. */
	public ArrayList<Quest> offers = new ArrayList<Quest>();
	
	/**
	Allocate a room and assign a pre-defined ID to it. All other fields are left blank.
	*/
	public Room(int newId)
	{
		id = newId;
		for (String s : Flags.roomFlags)
			flags.put(s, false);
	}
	
	/**
	Search for a room which has the ID {@code targetId}.
	<p>
	This runs through the {@link MudMain#rooms rooms} global ArrayList and returns
	the room which matches {@code targetId}.
	
	@param targetId The room ID to search for.
	@return The room which matches {@code targetId}, or {@code null} if none exists.
	*/
	public static Room lookup(int targetId)
	{
		for (Room r : rooms)
			if (r.id == targetId)
				return r;
		return null;
	}

	public static Room lookup(String targetAddr)
	{
		int id = Fmt.getInt(targetAddr.substring(14));
		return lookup(id);
	}
	
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
				for (UserCon cs : conns)
					if (cs.ch.currentRoom == this)
						cs.sendln("The '"+e.name+"' effect in the room has worn off.");
				effects.remove(e);
				ctr--;
			}
		}
	}

	public Area getArea()
	{
		for (Area a : areas)
			if (a.start <= id && a.end >= id)
				return a;
		return null;
	}
	
	/**
	Search for the given {@code direction} in the {@link Room#exits exits} ArrayList
	for this room.
	<p>
	The method will properly translate shortened versions of the six basic direcions
	(north, east, south, west, up, down) before searching.
	
	@param direction The direction to search for.
	@return The exit which leads in this direction, or {@code null} if none exists.
	*/
	public Exit matchExit(String direction)
	{
		direction = Fmt.resolveDir(direction.toLowerCase().trim());

		for (Exit e : exits)
			if (e.direction.equalsIgnoreCase(direction))
				return e;
		
		for (Exit e : exits)
			if (e.doorName.startsWith(direction.toLowerCase()))
				return e;
		return null;
	}
	
	/**
	Search for the exit opposite the {@code direction} exit from Room {@code to}.
	<p>
	The method will automatically reverse the six basic directions (north, east, south,
	west, up, down). All custom directions are assumed to have the same name from both
	rooms involved.
	
	@param to The room which must be on the other end of the exit from this room.
	@param direction The direction to search for as it exists in the opposite room.
		("north" if searching for a "south" exit from this room, etc.)
	@return The exit which leads to Room {@code to} in the opposite of {@code direction},
		or {@code null} if none exists.
	*/
	public Exit oppExit(Room to, String direction)
	{
		direction = direction.toLowerCase().trim();

		if ("north".startsWith(direction))
			direction = "south";
		else if ("east".startsWith(direction))
			direction = "west";
		else if ("south".startsWith(direction))
			direction = "north";
		else if ("west".startsWith(direction))
			direction = "east";
		else if ("up".startsWith(direction))
			direction = "down";
		else if ("down".startsWith(direction))
			direction = "up";
		
		for (Exit e : exits)
			if (e.direction.equalsIgnoreCase(direction) && (e.to == to || to == null))
				return e;
		return null;
	}
	
	/**
	Attempt to move user {@code c} from this room through the {@code direction} exit.
	<p>
	The method first looks for an exit which matches {@code direction} from this room.
	If none is found, it returns {@code false} and does nothing. If one is found, it will
	run various checks (user can't be fighting, there can't be a closed door in the way,
	etc.). If all of these checks pass, it will move the user through the exit.
	
	@param c The user to move.
	@param direction The direction in which the user is attempting to move.
	@return {@code false} if no exit matches the specified {@code direction}; {@code
		true} otherwise, even if the user could not be moved.
	*/
	public boolean takeExit(UserCon c, String direction)
	{
		String arg1 = CommandHandler.getArg(direction, 1);
		String arg2 = CommandHandler.getArg(direction, 2);
		if (c.ch.usingJog.length() > 0)
		{
			if (!arg2.equals("forjog"))
			{
				c.sendln("Jog interrupted.");
				c.ch.usingJog = "";
			}
			else
				direction = arg1;
		}
		
		Exit dest = matchExit(direction);
		
		if (dest == null)
			return false;
		
		if (c.afk)
		{
			c.afk = false;
			c.sendln("You are no longer AFK. Use 'replay' to view missed tells.");
		}
		
		if (c.ch.energy < 5)
		{
			c.sendln("You're too exhausted to move. Try resting for a moment.");
			return true;
		}

		if (c.ch.fighting != null)
		{
			c.sendln("You're too busy fighting to move.");
			return true;
		}
		
		if (!c.ch.position.equals("standing"))
		{
			c.sendln("You can't move around when you're "+c.ch.position+". Try standing up first.");
			return true;
		}
		
		boolean closeAfter = false;
		if (dest.flags.get("closed"))
		{
			if (arg2.equals("forjog"))
			{
				RoomCommands.doOpen(c, dest.direction);
				closeAfter = true;
			}
			if (dest.flags.get("closed"))
			{
				for(Effect e : c.ch.effects)
				{
					if(e.name.equalsIgnoreCase("passage"))
					{
						if(doTakeExit(c, direction, closeAfter));
							return true;
					}
				}
				c.sendln("The door is closed.");
				return true;
			}
		}
		
		if(doTakeExit(c, direction, closeAfter))
			return true;
		
		return false;
	}
	
	public boolean doTakeExit(UserCon c, String direction, boolean closeAfter)
	{
		String arg1 = CommandHandler.getArg(direction, 1);
		String arg2 = CommandHandler.getArg(direction, 2);
		
		Exit dest = matchExit(direction);
		
		String action = "through the "+dest.direction+".";
		String revAction = "into the room.";
		if (dest.direction.equals("north") || dest.direction.equals("east")
			|| dest.direction.equals("south") || dest.direction.equals("west"))
			action = "to the "+dest.direction+".";
		if (dest.direction.equals("up") || dest.direction.equals("down"))
			action = dest.direction+".";
			
		Exit oppExit = dest.to.oppExit(c.ch.currentRoom, dest.direction);
		
		if (checkTrigger("leave", c.ch, null, dest.direction, 0) == 0)
			return true;
		for (CharData ch : mobs)
			if (ch.currentRoom == this)
				if (ch.checkTrigger("leave", c.ch, null, dest.direction, 0) == 0)
					return true;
		for (ObjData o : ObjData.allObjects())
			if (o.getCurrentRoom() == this)
				if (o.checkTrigger("leave", c.ch, null, dest.direction, 0) == 0)
					return true;

		if (oppExit != null)
		{
			revAction = "in through the "+oppExit.direction;
			if (oppExit.direction.equals("north") || oppExit.direction.equals("east")
				|| oppExit.direction.equals("south") || oppExit.direction.equals("west"))
				revAction = "in from the "+oppExit.direction+".";
			if (oppExit.direction.equals("up"))
				revAction = "in from above.";
			if (oppExit.direction.equals("down"))
				revAction = "in from below.";
	
			if (dest.to.checkTrigger("greet", c.ch, null, oppExit.direction, 0) == 0)
				return true;
			for (CharData ch : mobs)
				if (ch.currentRoom == dest.to)
					if (ch.checkTrigger("greet", c.ch, null, oppExit.direction, 0) == 0)
						return true;
			for (ObjData o : ObjData.allObjects())
				if (o.getCurrentRoom() == dest.to)
					if (o.checkTrigger("greet", c.ch, null, oppExit.direction, 0) == 0)
						return true;
		}
		else
		{
			if (dest.to.checkTrigger("greet", c.ch, null, "null", 0) == 0)
				return true;
			for (CharData ch : mobs)
				if (ch.currentRoom == dest.to)
					if (ch.checkTrigger("greet", c.ch, null, "null", 0) == 0)
						return true;
			for (ObjData o : ObjData.allObjects())
				if (o.getCurrentRoom() == dest.to)
					if (o.checkTrigger("greet", c.ch, null, "null", 0) == 0)
						return true;
		}

		Fmt.actAround(c.ch, null, null, "$n "+c.ch.getMovementVerb()+"s "+action);

		c.ch.currentRoom = dest.to;

		c.ch.positionTarget = null;
		if (c.ch.combatQueue.size() > 0)
			CombatCommands.doQueuedump(c, "");

		Fmt.actAround(c.ch, null, null, "$n "+c.ch.getMovementVerb()+"s "+revAction);

		if (Effect.findEffect(c.ch.effects, "levitation") != null)
			c.ch.energy -= 1;
		else
			c.ch.energy -= 5;
		
		if (c.ch.usingJog.length() == 0)
			c.delay = 5;
		c.sendln("You "+c.ch.getMovementVerb()+" "+action);
		RoomCommands.doLook(c, "");
		
		c.ch.fleeing = false;
		
		// Any following characters should also take this exit.
		for (CharData chs : allChars())
			if (chs.following == c.ch)
			{
				if (chs.currentRoom == this)
					takeExit(chs.conn, direction);
				else
					ChatCommands.doFollow(chs.conn, "");
			}
		
		if (closeAfter)
			RoomCommands.doClose(c, oppExit.direction);
		
		c.ch.checkTrigger("entry", null, null, dest.direction, 0);

		// Check for automatic mob attacks.
		Combat.checkAutoAttacks(c);
		return true;
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
					retVal = t.rprog.run(t, args, this, null, null, actor, victim, 0, 0, 0, variables);
		return retVal;
	}
	
	public boolean isDark()
	{
		for (CharData ch : allChars())
			if (ch.currentRoom == this)
				if (ch.getWearloc("light") != null)
					return false;

		if (sector.equals("indoors")
			|| sector.equals("city")
			|| sector.equals("path"))
			return false;
		
		int hour = Fmt.getHour();
		int season = Fmt.getSeason();
		int sunrise = Fmt.getSunrise(season);
		int sunset = Fmt.getSunset(season);
		int riseMod = 0;
		int setMod = 0;
		
		if (sector.equals("cavern"))
			return true;
		
		if (sector.equals("forest")
			|| sector.equals("underwater"))
		{
			riseMod = 2;
			setMod = -2;
		}
		
		sunrise = sunrise+riseMod;
		sunset = sunset+setMod;
		
		if (hour >= sunrise && hour <= sunset)
			return false;
		
		return true;
	}
	
	// Uses Dijsktra's Algorithm to find a path from one room to another.
	public static String pathTo(Room from, Room to, boolean oneDir, boolean noCollapse)
	{
		if (from == null || to == null)
			return "";
		if (from == to)
			return "";

		Set<Room> connected = new HashSet<Room>();
		Set<Room> examined = new HashSet<Room>();
		ArrayList<Room> examineQueue = new ArrayList<Room>();
		examineQueue.add(from);
		
		// Make sure the target room is connected to this one before finding a path.
		while (examineQueue.size() > 0)
		{
			Room temp = examineQueue.get(0);
			examineQueue.remove(0);
			examined.add(temp);
			
			for (Exit e : temp.exits)
			{
				// Ignore nonstandard exits.
				if (!e.direction.equals("north")
					&& !e.direction.equals("east")
					&& !e.direction.equals("south")
					&& !e.direction.equals("west")
					&& !e.direction.equals("up")
					&& !e.direction.equals("down"))
					continue;

				if (!examined.contains(e.to))
					examineQueue.add(e.to);
				if (!connected.contains(e.to))
					connected.add(e.to);
			}
		}
		
		if (!connected.contains(to))
			return "";

		final int maxLen = connected.size();
		Set<Room> settled = new HashSet<Room>();
		final HashMap<Room, Integer> minLen = new HashMap<Room, Integer>();
		HashMap<Room, Exit> prev = new HashMap<Room, Exit>();

		Comparator<Room> shortestDistanceComparator = new Comparator<Room>()
		{	public int compare(Room left, Room right)	{
			Integer shortestDistanceLeft = minLen.get(left);
			if (shortestDistanceLeft == null)
				shortestDistanceLeft = maxLen;
			Integer shortestDistanceRight = minLen.get(right);
			if (shortestDistanceRight == null)
				shortestDistanceRight = maxLen;

			if (shortestDistanceLeft > shortestDistanceRight)
				return 1;
			else if (shortestDistanceLeft < shortestDistanceRight)
				return -1;
			else
				return left.compareTo(right);
		}	};

		PriorityQueue<Room> unsettled = new PriorityQueue<Room>(1, shortestDistanceComparator);

		unsettled.add(from);
		minLen.put(from, 0);
		
		while (unsettled.size() > 0)
		{
			Room temp = unsettled.poll();
			settled.add(temp);
			if (settled == to)
				break;
			int tempLen = minLen.get(temp)+1;

			for (Exit e : temp.exits)
			{
				// Ignore nonstandard exits.
				if (!e.direction.equals("north")
					&& !e.direction.equals("east")
					&& !e.direction.equals("south")
					&& !e.direction.equals("west")
					&& !e.direction.equals("up")
					&& !e.direction.equals("down"))
					continue;

				if (!settled.contains(e.to))
				{
					if (minLen.get(e.to) != null)
					{
						if (minLen.get(e.to) > tempLen)
						{
							minLen.put(e.to, tempLen);
							prev.put(e.to, e);
							unsettled.add(e.to);
						}
					}
					else
					{
						minLen.put(e.to, tempLen);
						prev.put(e.to, e);
						unsettled.add(e.to);
					}
				}
			}
		}
		
		String dirs = "";
		
		Exit tempExit = prev.get(to);
		while (tempExit != null)
		{
			dirs = tempExit.direction.substring(0, 1)+dirs;
			tempExit = prev.get(tempExit.from);
		}
		
		if (oneDir)
			return dirs.substring(0, 1);
		if (noCollapse)
			return dirs;
			
		char collapseExits[] = {'n', 'e', 's', 'w', 'u', 'd'};
		for (char ch : collapseExits)
		{
			String newDirs = "";
			int repeat = 0;
			boolean repeating = false;
			for (int ctr = 0; ctr < dirs.length(); ctr++)
			{
				if (dirs.charAt(ctr) == ch)
				{
					if (repeating)
					{
						repeat++;
					}
					else
					{
						repeating = true;
						repeat = 1;
					}
				}
				else
				{
					if (repeating)
					{
						if (repeat == 1)
							newDirs = newDirs+ch;
						else
							newDirs = newDirs+repeat+ch;
						repeating = false;
					}
					newDirs = newDirs+dirs.charAt(ctr);
				}
			}
			if (repeating)
			{
				if (repeat == 1)
					newDirs = newDirs+ch;
				else
					newDirs = newDirs+repeat+ch;
			}
			dirs = newDirs;
		}
		
		return dirs;
	}
	
	
	/**
	Allows rooms to be sorted using Collections.sort() in order of their ID.
	*/
	public int compareTo(Room other)
	{
		return (this.id-other.id);
	}
}