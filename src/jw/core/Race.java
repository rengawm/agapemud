package jw.core;
import static jw.core.MudMain.*;

/**
	The Race class represents a race and all associated values.
*/
public class Race
{
	/** The ID associated with this race. */
	public int id;
	/** The name of this race. */
	public String name;
	/** The name of this race when used as a plural noun in scripts. */
	public String plural;
	/** The name of the racegroup this race belongs to. Having a racegroup
		set makes the race available to players on character creation. */
	public String racegroup;
	/** The multi-line description of this race. */
	public String description;
	
	public int baseStr;
	public int baseDex;
	public int baseCon;
	public int baseInt;
	public int baseCha;
	public int baseSlash;
	public int baseBash;
	public int basePierce;
	public int baseFrost;
	public int baseFire;
	public int baseLightning;
	public int baseAcid;
	public int baseGood;
	public int baseEvil;
	
	/** Default movement verb for all members of this race. */
	public String movement = "";
	/** Default unarmed combat damage name for all members of this race. */
	public String hitname = "";
	
	/**
	Allocate a new race and assign it the given ID.
	
	@param newId The ID to associate with this race.
	*/
	public Race(int newId)
	{
		id = newId;
	}
	
	/**
	Search for a race which has the ID {@code targetId}.
	<p>
	This runs through the {@link MudMain#races races} global ArrayList and returns
	the race which matches {@code targetId}.
	
	@param targetId The race ID to search for.
	@return The race which matches {@code targetId}, or {@code null} if none exists.
	*/
	public static Race lookup(int targetId)
	{
		for (Race r : races)
			if (r.id == targetId)
				return r;
		return null;
	}
	
	/**
	Search for a race named {@code target}.
	<p>
	This runs through the {@link MudMain#races races} global ArrayList and returns
	the race named {@code target}.
	
	@param target The race name to search for.
	@return The race named {@code target}, or {@code null} if none exists.
	*/
	public static Race lookup(String target)
	{
		Race lookById = lookup(Fmt.getInt(target));
		if (lookById != null)
			return lookById;
		
		for (Race r : races)
			if (r.name.equalsIgnoreCase(target))
				return r;
		return null;
	}
}