package jw.core;
import java.util.*;

import jw.core.*;
import static jw.core.MudMain.*;

/**
The Exit class represents a single exit, with all associated fields.
*/
public class Exit implements Comparable<Exit>
{
	/** The ID of this exit in the database. */
	public int id;
	/** The room which this exit leads to. */
	public Room to;
	/** The room which this exit comes from. */
	public Room from;
	/** The direction this exit occupies. */
	public String direction = "";
	/** The alternative name of the door. */
	public String doorName = "";
	/** Reset timer. */
	public int timer = 0;
	/** Flags. */
	public HashMap<String, Boolean> flags = new HashMap<String, Boolean>();
	/** The ID of the object which unlocks this exit, or 0 if it can't be locked. */
	public int key = 0;
	
	/**
	Allocate a new exit and use {@code newId} as its ID. Also set {@code to} to null.
	
	@param newId The ID to associate with the new exit.
	*/
	public Exit(int newId)
	{
		id = newId;
		to = null;
		for (String s : Flags.exitFlags)
			flags.put(s, false);
	}
	
	/**
	Search for an exit which has the ID {@code targetId}.
	<p>
	This runs through the {@link MudMain#rooms rooms} global ArrayList and checks all
	exits from each room to return the exit which matches {@code targetId}.

	@param targetId The exit ID to search for.
	@return The exit which matches {@code targetId}, or {@code null} if none exists.
	*/
	public static Exit lookup(int targetId)
	{
		for (Room r : rooms)
			for (Exit e : r.exits)
				if (e.id == targetId)
					return e;
		return null;
	}
	
	/**
	Search for an exit which has the ID {@code targetId} and return the room it comes
	from.
	<p>
	This runs through the {@link MudMain#rooms rooms} global ArrayList and checks all
	exits from each room to return the exit which matches {@code targetId}.

	@param targetId The exit ID to search for.
	@return The room containing the exit which matches {@code targetId}, or {@code
		null} if none exists.
	*/
	public static Room lookupRoom(int targetId)
	{
		for (Room r : rooms)
			for (Exit e : r.exits)
				if (e.id == targetId)
					return r;
		return null;
	}

	/**
	Toggle the given {@code flag} on this exit.
	
	@param c The user who initiated the command.
	@param flag The name of the flag to toggle.
	*/
	public void toggleFlag(UserCon c, String newFlag)
	{
		String flag = "";
		for (String s : Flags.exitFlags)
			if (s.startsWith(newFlag))
				flag = s;

		if (flag.length() == 0)
		{
			c.sendln("'"+flag+"' is an unknown exit flag.");
			return;
		}
		
		if (flag.equals("nopick") || flag.equals("nobash") || flag.equals("nopass"))
		{
			if (flags.get(flag))
			{
				flags.put(flag, false);
				c.sendln("'"+flag+"' flag removed on exit.");
				return;
			}
			flags.put(flag, true);
			c.sendln("'"+flag+"' flag set on exit.");
			return;
		}
		
		if (flag.equals("door"))
		{
			if (flags.get("door"))
			{
				flags.put("door", false);
				flags.put("closed", false);
				flags.put("locked", false);
				c.sendln("Door removed from exit.");
			}
			else
			{
				flags.put("door", true);
				c.sendln("Door added to exit.");
			}
		}
		else if (flag.equals("closed"))
		{
			if (!flags.get("door"))
			{
				c.sendln("There's no door to close on that exit.");
			}
			else if (flags.get("closed"))
			{
				flags.put("closed", false);
				flags.put("locked", false);
				c.sendln("Door opened on exit.");
			}
			else
			{
				flags.put("closed", true);
				c.sendln("Door closed on exit.");
			}
		}
		else if (flag.equals("locked"))
		{
			if (!flags.get("door"))
			{
				c.sendln("There's no door to lock on that exit.");
			}
			else if (flags.get("locked"))
			{
				flags.put("locked", false);
				c.sendln("Door unlocked on exit.");
			}
			else
			{
				flags.put("closed", true);
				flags.put("locked", true);
				c.sendln("Door locked on exit.");
			}
		}
		else if (flag.equals("hidden"))
		{
			if (flags.get("hidden"))
			{
				flags.put("hidden", false);
				c.sendln("Exit no longer hidden.");
			}
			else
			{
				flags.put("hidden", true);
				c.sendln("Exit is now hidden.");
			}
		}
	}

	/**
	Allows exits to be sorted using Collections.sort() in order of their name.
	*/
	public int compareTo(Exit other)
	{
		return direction.compareTo(other.direction);
	}
}