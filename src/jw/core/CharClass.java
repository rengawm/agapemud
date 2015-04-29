package jw.core;
import static jw.core.MudMain.*;

/**
	The CharClass class represents a single character class, with all associated fields.
*/
public class CharClass
{
	/** The ID of this class. */
	public int id;
	/** The name of this class. */
	public String name = "";
	/** The parent class (for reclass) */
	public int parent;
	/** The class's attribute valuation type. */
	public String type = "";
	
	/**
	Allocate a class and assign a pre-defined ID to it. All other fields are left blank.
	*/
	public CharClass(int newId)
	{
		id = newId;
	}
	
	/**
	Search for a class which has the ID {@code targetId}.
	<p>
	This runs through the {@link MudMain#classes classes} global ArrayList and returns
	the class which matches {@code targetId}.
	@param targetId The class ID to search for.
	@return The class which matches {@code targetId}, or {@code null} if none exists.
	*/
	public static CharClass lookup(int targetId)
	{
		for (CharClass c : classes)
			if (c.id == targetId)
				return c;
		return null;
	}
	
	/**
	Search for a class named {@code target}.
	<p>
	This runs through the {@link MudMain#classes classes} global ArrayList and returns
	the class named {@code target}.
	@param target The class name to search for.
	@return The class named {@code target}, or {@code null} if none exists.
	*/
	public static CharClass lookup(String target)
	{
		CharClass lookById = lookup(Fmt.getInt(target));
		if (lookById != null)
			return lookById;
		
		for (CharClass c : classes)
			if (c.name.equalsIgnoreCase(target))
				return c;
		return null;
	}
}