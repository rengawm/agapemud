package jw.core;
import java.util.*;
import java.lang.reflect.Method;

import static jw.core.MudMain.*;

/**
	The Command class represents a command which can be run on the MUD. It is assigned
	a method from one of the command class files, and uses that method whenever the
	command is invoked.
*/
public class Command implements Comparable<Command>
{
	/** The method to invoke when this command is run. */
	private Method m;
	/** The "official" full name of this command, equal to its method name minus the
		"do" or "pr" prefix. */
	public String fullName;
	/** Any system-wide aliases which have been added to this command. */
	public ArrayList<String> alias = new ArrayList<String>();
	/** The list of permission groups with access to this method. When this ArrayList
		is empty, all users can access the command. */
	public ArrayList<String> permissions = new ArrayList<String>();
	/** Is this command being logged? */
	public boolean log = false;
	
	/**
	Allocate a new command and assign the method {@code newM} to it.
	<p>
	This also pulls the command's full name from the method name and sets it.
	
	@param newM The method around which to form this command.
	*/
	public Command(Method newM)
	{
		m = newM;
		alias.add(m.getName().substring(2).toLowerCase());
		fullName = alias.get(0);
	}
	
	/**
	Invoke the method associated with this command.
	
	@param c The user who entered this command.
	@param args A string containing any arguments to the command.
	*/
	public void run(UserCon c, String args)
	{
		if (logAll)
			sysLog("bugs", "Command used: "+fullName+" by "+c.ch.shortName+" at "+c.ch.currentRoom.id+".");
		try
		{
			if (c.ch.currentRoom.checkTrigger("command", c.ch, null, fullName+" "+args, 0) != -1)
				return;
			for (CharData ch : allChars())
				if (ch.currentRoom == c.ch.currentRoom)
					if (ch.checkTrigger("command", c.ch, null, fullName+" "+args, 0) != -1)
						return;
			for (ObjData o : c.ch.currentRoom.objects)
				if (o.checkTrigger("command", c.ch, null, fullName+" "+args, 0) != -1)
					return;
			for (ObjData o : c.ch.objects)
				if (o.checkTrigger("command", c.ch, null, fullName+" "+args, 0) != -1)
					return;

			m.invoke(null, c, args);
		} catch (Exception e) {
			sysLog("bugs", "Error in "+fullName+".run: "+e.getMessage());
			logException(e);
		}
	}
	
	/**
	Compares two commands by name for use with Collections.sort().
	*/
	public int compareTo(Command other)
	{
		return this.alias.get(0).compareTo(other.alias.get(0));
	}
	
	/**
	Compare the permissions of a given user with this command's requirements and return
	whether they can use the command.
	<p>
	In addition to checking for matching permission groups, this will also look for
	individual commands granted to the user.
	
	@param c The user whose permissions are being checked.
	@return {@code true} if the user can use the command; {@code false} otherwise.
	*/
	public boolean allowCheck(UserCon c)
	{
		for (String r : c.revoked)
			if (r.equals(fullName))
				return false;
		if (permissions.size() == 0)
			return true;
		for (String cPerm : c.permissions)
		{
			if (cPerm.equals("admin"))
				return true;
			for (String aPerm : permissions)
				if (aPerm.equals(cPerm))
					return true;
		}
		for (String cGrant : c.granted)
			if (cGrant.equals(fullName))
				return true;
		return false;
	}
	
	/**
	Search for a command named {@code target}.
	<p>
	This runs through the {@link MudMain#commands commands} global ArrayList and returns
	the command named {@code target}.
	@param target The command name to search for.
	@return The command named {@code target}, or {@code null} if none exists.
	*/
	public static Command lookup(String target)
	{
		for (Command c : commands)
			if (c.fullName.equals(target))
				return c;
		return null;
	}
}