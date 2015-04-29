package jw.core;
import static jw.core.MudMain.*;

/**
	The Social class represents a social in the game, and all associated values.
*/
public class Social implements Comparable<Social>
{
	/** The ID of this social in the database. */
	public int id;
	/** The name of this social. */
	public String name;
	/** The string sent to the actor when there is no argument. */
	public String cNoArg;
	/** The string sent to others when there is no argument. */
	public String oNoArg;
	/** The string sent to the actor when there is a target. */
	public String cFound;
	/** The string sent to others when there is a target. */
	public String oFound;
	/** The string sent to the target. */
	public String vFound;
	/** The string sent to the actor when the actor is the target. */
	public String cSelf;
	/** The string sent to others when the actor is the target. */
	public String oSelf;
	
	/**
	Allocate a new social and assign it the given ID.
	
	@param newId The ID to associate with this social.
	*/
	public Social(int newId)
	{
		id = newId;
	}
	
	/**
	Execute a social in the characer's local room.

	@param c The user who entered this command.
	@param args A string containing any arguments to the command.
	*/
	public void localSocial(UserCon c, String args)
	{
		if (c.ch.position.equals("sleeping"))
		{
			c.sendln("You can't do that when you're "+c.ch.position+".");
			return;
		}
		
		if (args.length() == 0)
		{
			c.sendln(Fmt.actString(c.ch, c.ch, null, null, getSocialText(c.ch, null, "cNoArg"), false));
			Fmt.actAround(c.ch, null, null, getSocialText(c.ch, null, "oNoArg"));
			return;
		}
		
		CharData target = Combat.findChar(c.ch, null, args, false);
		
		if (target == null)
		{
			c.sendln("There isn't anyone here by that name.");
			return;
		}
		
		if (target == c.ch)
		{
			c.sendln(Fmt.actString(c.ch, c.ch, target, null, getSocialText(c.ch, target, "cSelf"), false));
			Fmt.actAround(c.ch, target, null, getSocialText(c.ch, target, "oSelf"));
			return;
		}
		
		c.sendln(Fmt.actString(c.ch, c.ch, target, null, getSocialText(c.ch, target, "cFound"), false));
		if (!target.position.equals("sleeping"))
			target.sendln(Fmt.actString(target, c.ch, target, null, getSocialText(c.ch, target, "vFound"), false));
		Fmt.actAround(c.ch, target, null, getSocialText(c.ch, target, "oFound"), target);
	}

	/**
	Get text for a social sent across a global channel.

	@param c The user who entered this command.
	@param args A string containing any arguments to the command.
	*/
	public String globalSocial(UserCon c, String args, UserCon at)
	{
		if (args.length() == 0)
		{
			if (c == at)
				return (Fmt.actString(at.ch, c.ch, null, null, getSocialText(c.ch, null, "cNoArg")+"{x", true));
			else
				return (Fmt.actString(at.ch, c.ch, null, null, getSocialText(c.ch, null, "oNoArg")+"{x", true));
		}
		
		CharData target = Combat.findChar(c.ch, null, args, true);
		
		if (target == null)
		{
			return "";
		}
		
		if (target == c.ch)
		{
			if (c == at)
				return (Fmt.actString(at.ch, c.ch, target, null, getSocialText(c.ch, target, "cSelf")+"{x", true));
			else
				return (Fmt.actString(at.ch, c.ch, target, null, getSocialText(c.ch, target, "oSelf")+"{x", true));
		}
		
		
		if (c == at)
			return (Fmt.actString(at.ch, c.ch, target, null, getSocialText(c.ch, target, "cFound")+"{x", true));
		else if (at.ch == target)
			return (Fmt.actString(at.ch, c.ch, target, null, getSocialText(c.ch, target, "vFound")+"{x", true));
		else
			return (Fmt.actString(at.ch, c.ch, target, null, getSocialText(c.ch, target, "oFound")+"{x", true));
	}
	
	/**
	Use {@link CommandHandler#actString(CharData, CharData, String)
	CommandHandler.actString} to substitute $ fields in the string with appropriate
	pronouns/names.

	@param ch The "actor" who initiated the social.
	@param target The target of the social, if there is any.
	@param type The type of string to send.
	@return The finished string.
	*/
	public String getSocialText(CharData ch, CharData target, String type)
	{
		String result = "";
		if (type.equals("cNoArg"))
			result = cNoArg;
		else if (type.equals("oNoArg"))
			result = oNoArg;
		else if (type.equals("cFound"))
			result = cFound;
		else if (type.equals("oFound"))
			result = oFound;
		else if (type.equals("vFound"))
			result = vFound;
		else if (type.equals("cSelf"))
			result = cSelf;
		else if (type.equals("oSelf"))
			result = oSelf;
		
		return result;
	}
	
	/**
	Search for a social which has the ID {@code targetId}.
	<p>
	This runs through the {@link MudMain#socials socials} global ArrayList and returns
	the social which matches {@code targetId}.
	
	@param targetId The social ID to search for.
	@return The social which matches {@code targetId}, or {@code null} if none exists.
	*/
	public static Social lookup(int targetId)
	{
		for (Social s : socials)
			if (s.id == targetId)
				return s;
		return null;
	}
	
	/**
	Search for a social named {@code target}.
	<p>
	This runs through the {@link MudMain#socials socials} global ArrayList and returns
	the social named {@code target}.
	
	@param target The social name to search for.
	@return The social named {@code target}, or {@code null} if none exists.
	*/
	public static Social lookup(String target)
	{
		for (Social s : socials)
			if (s.name.equals(target))
				return s;
		return null;
	}
	
	/**
	Compares two socials by name for use with Collections.sort().
	*/
	public int compareTo(Social other)
	{
		return (this.name.compareTo(other.name));
	}
}