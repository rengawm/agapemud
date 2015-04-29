package jw.core;
import java.util.*;

import jw.core.*;
import static jw.core.MudMain.*;

/**
	The ObjProto class represents a single object prototype, with all associated fields.
*/
public class ObjProto implements Comparable<ObjProto>
{
	/** The object prototype's ID number. */
	public int id;
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
	/** The material of this object. */
	public String material = "";
	/** The type of this object. */
	public String type = "none";
	/** Flags. */
	public HashMap<String, Boolean> flags = new HashMap<String, Boolean>();
	/** Effects. */
	public HashMap<String, Boolean> effects = new HashMap<String, Boolean>();
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
	/** The quests this object can offer to players. */
	public ArrayList<Quest> offers = new ArrayList<Quest>();
	
	/**
	Allocate an object prototype and assign a pre-defined ID to it.
	*/
	public ObjProto(int newId)
	{
		id = newId;
		for (String s : Flags.objFlags)
			flags.put(s, false);
		for (String s : Flags.objEffects)
			effects.put(s, false);
	}

	/**
	Search for a object prototype which has the ID {@code targetId}.
	<p>
	This runs through the {@link MudMain#charProtos objProtos} global ArrayList
	and returns the object prototype which matches {@code targetId}.
	@param targetId The object prototype ID to search for.
	@return The object prototype which matches {@code targetId}, or {@code null}
		if none exists.
	*/
	public static ObjProto lookup(int targetId)
	{
		for (ObjProto p : objProtos)
			if (p.id == targetId)
				return p;
		return null;
	}

	public int compareTo(ObjProto other)
	{
		return (this.id-other.id);
	}
	
	public void setTypeFlags(String typeFlagString)
	{
		for (String s : ObjData.getTypeFlags(type))
			if (typeFlagString.indexOf(";"+s+";") > -1 || typeFlagString.startsWith(s+";"))
				typeFlags.put(s, true);
			else
				typeFlags.put(s, false);
	}
}