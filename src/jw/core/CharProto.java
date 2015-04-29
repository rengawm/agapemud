package jw.core;
import java.util.*;

import static jw.core.MudMain.*;

/**
	The CharProto class represents a single mob prototype, with all associated fields.
*/
public class CharProto implements Comparable<CharProto>
{
	/** The character prototype's ID number. */
	public int id;
	/** The character's keyword(s). */
	public String name = "";
	/** The short name of the character (used in action phrases, "you hit
		<shortname>", etc). */
	public String shortName = "";
	/** The long name of the character, used when seen in a room. */
	public String longName = "";
	/** The multi-line description of the character, used when a player looks at it. */
	public String description = "";
	/** Extra descriptions. */
	public HashMap<String, String> eds = new HashMap<String, String>();
	/** All mob progs active on this character. */
	public ArrayList<Trigger> triggers = new ArrayList<Trigger>();
	/** The sex of a character - 'm' for male, 'f' for female, 'r' for randomly
		generated. */
	public String sex = "r";
	/** The race of this character. */
	public Race charRace;
	/** The class (CharClass) of this character. */
	public CharClass charClass;
	/** The level of the charcter. */
	public int level = 1;
	/** The difficulty setting of this mob, on a scale of 1-4. */
	public int difficulty = 1;
	/** Flags. */
	public HashMap<String, Boolean> flags = new HashMap<String, Boolean>();
	/** Effects. */
	public HashMap<String, Boolean> effects = new HashMap<String, Boolean>();
	/** Character's default position. */
	public String position = "standing";
	/** The base maximum health of the character. */
	public int baseHp = 0;
	/** The base maximum mana of the character. */
	public int baseMana = 0;
	/** The base maximum energy of this character. */
	public int baseEnergy = 0;
	/** The base strength of this character. */
	public int baseStr = 0;
	/** The base dexterity of this character. */
	public int baseDex = 0;
	/** The base constitution of this character. */
	public int baseCon = 0;
	/** The base intelligence of this character. */
	public int baseInt = 0;
	/** The base charisma of this character. */
	public int baseCha = 0;
	/** The base slash armor of this character. */
	public int baseArmSlash = 0;
	/** The base bash armor of this character. */
	public int baseArmBash = 0;
	/** The base pierce armor of this character. */
	public int baseArmPierce = 0;
	/** The base frost resistance of this character. */
	public int baseResFrost = 0;
	/** The base fire resistance of this character. */
	public int baseResFire = 0;
	/** The base lightning resistance of this character. */
	public int baseResLightning = 0;
	/** The base acid resistance of this character. */
	public int baseResAcid = 0;
	/** The base good resistance of this character. */
	public int baseResGood = 0;
	/** The base evil resistance of this character. */
	public int baseResEvil = 0;
	/** The movement verb of this character. */
	public String movement = "";
	/** The hit name for this character's unarmed combat. */
	public String hitname = "";
	/** The alignment of this character. */
	public int align = 0;
	/** The lootgroups this shop sells from, if it's a shopkeeper. */
	public ArrayList<Lootgroup> sells = new ArrayList<Lootgroup>();
	/** The quests this character can offer to players. */
	public ArrayList<Quest> offers = new ArrayList<Quest>();
	
	/**
	Allocate an character prototype and assign a pre-defined ID to it.
	*/
	public CharProto(int newId)
	{
		id = newId;
		for (String s : Flags.charFlags)
			flags.put(s, false);
		for (String s : Flags.charEffects)
			effects.put(s, false);
	}

	/**
	Search for a character prototype which has the ID {@code targetId}.
	<p>
	This runs through the {@link MudMain#charProtos charProtos} global ArrayList
	and returns the character prototype which matches {@code targetId}.
	@param targetId The character prototype ID to search for.
	@return The character prototype which matches {@code targetId}, or {@code null}
		if none exists.
	*/
	public static CharProto lookup(int targetId)
	{
		for (CharProto p : charProtos)
			if (p.id == targetId)
				return p;
		return null;
	}

	public int compareTo(CharProto other)
	{
		return (this.id-other.id);
	}
}