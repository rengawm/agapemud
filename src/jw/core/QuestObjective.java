package jw.core;
import java.util.*;

import static jw.core.MudMain.*;
import jw.data.*;

/**
	The QuestObjective class represents a single quest objective, with all
	associated fields.
*/
public class QuestObjective
{
	/** The quest objective's ID number. */
	public int id;
	/** The name of this objective. */
	public String name = "";
	/** The brief description of the objective shown to players. */
	public String description = "";
	/** The script to be run when a player begins this objective. */
	public MobProg start = null;
	/** The script to be run when the player completes this objective. */
	public MobProg end = null;
	
	/** For standard objectives: The type of objective. */
	public ArrayList<String> objType = new ArrayList<String>();
	/** For standard objectives: The ID of the objective target. */
	public ArrayList<Integer> objId = new ArrayList<Integer>();
	/** For standard objectives: The count of the objective. */
	public ArrayList<Integer> objCount = new ArrayList<Integer>();

	/** The custom triggers to add to the character doing the quest. */
	public ArrayList<Trigger> triggers = new ArrayList<Trigger>();
	/** The script to be run when the custom trigger is fired. */
	public MobProg script = new MobProg(0);
	/** The script to be run to determine if the objective is complete. */
	public MobProg check = new MobProg(0);

	/** Flags. */
	public HashMap<String, Boolean> flags = new HashMap<String, Boolean>();
	
	/**
	Allocate a quest objective and assign a pre-defined ID to it. All other fields are left blank.
	*/
	public QuestObjective(int newId)
	{
		id = newId;
		for (String s : Flags.questObjectiveFlags)
			flags.put(s, false);
	}
	
	public static QuestObjective lookup(int targetId)
	{
		for (Quest q : quests)
			for (QuestObjective qo : q.objectives)
				if (qo.id == targetId)
					return qo;
		return null;
	}
}