package jw.core;
import java.util.*;

import static jw.core.MudMain.*;
import jw.data.*;

/**
	The Quest class represents a single quest, with all associated fields.
*/
public class Quest
{
	/** The quest's ID number. */
	public int id;
	/** The name of this quest. */
	public String name = "";
	/** The brief description of the quest seen by players while doing the quest. */
	public String description = "";
	/** The script which checks to see if the person can accept the quest. */
	public MobProg check = new MobProg(0);
	/** The script to be run when the quest is offered. */
	public MobProg offer = new MobProg(0);
	/** The script to be run when the quest is accepted. */
	public MobProg accept = new MobProg(0);
	/** Text given on completing the quest. */
	public MobProg complete = new MobProg(0);
	/** Minimum required level. */
	public int minLevel = 1;
	/** Highest recommended level. */
	public int maxLevel = 100;
	/** The IDs of the quests required to begin this quest. */
	public ArrayList<Integer> prereqs = new ArrayList<Integer>();
	/** The difficulty of the quest, from 1 to 5. */
	public int difficulty = 1;
	/** Flags. */
	public HashMap<String, Boolean> flags = new HashMap<String, Boolean>();

	/** The objectives required to complete this quest. */
	public ArrayList<QuestObjective> objectives = new ArrayList<QuestObjective>();

	public Quest(int newId)
	{
		id = newId;
		for (String s : Flags.questFlags)
			flags.put(s, false);
	}
	
	/**
	Search for an quest which has the given ID.
	*/
	public static Quest lookup(int targetId)
	{
		for (Quest q : quests)
			if (q.id == targetId)
				return q;
		return null;
	}
}