package jw.core;
import java.util.*;

import static jw.core.MudMain.*;

/**
	The Help class represents a single help file, along with all of its values.
*/
public class Help
{
	/** The ID of this help file. */
	public int id;
	/** The title of the help file. */
	public String title;
	/** The full text of the help file. */
	public String text;
	/** The list of permission groups with access to this help file. When this
		ArrayList is empty, all users can access the help file. */
	public ArrayList<String> permissions = new ArrayList<String>();
	
	/**
	Allocate a new help file and associate it with the given ID.
	
	@param newId The ID to associate with this help file.
	*/
	public Help(int newId)
	{
		id = newId;
	}

	/**
	Compare the permissions of a given user with this help file's requirements and
	return whether they can view the help file.
	<p>
	In addition to checking for matching permission groups, this will also look for
	individual commands granted to the user. If the user has been granted access to
	a command whose name matches the help file exactly, the user can access the file
	regardless of their permission groups.
	
	@param c The user whose permissions are being checked.
	@return {@code true} if the user can view the help file; {@code false} otherwise.
	*/
	public boolean allowCheck(UserCon c)
	{
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
		
		// Has the user been granted access to a specific command that matches this
		// file's title?
		for (String gr : c.granted)
			if (gr.equals(title))
				return true;

		return false;
	}
	
	/**
	Search for a help file which has the ID {@code targetId}.
	<p>
	This runs through the {@link MudMain#helps helps} global ArrayList and returns
	the help file which matches {@code targetId}.

	@param targetId The help ID to search for.
	@return The help file which matches {@code targetId}, or {@code null} if none exists.
	*/
	public static Help lookup(int targetId)
	{
		for (Help h : helps)
			if (h.id == targetId)
				return h;
		return null;
	}
}