package jw.core;
import java.util.*;

import jw.data.*;
import static jw.core.MudMain.*;

/**
	The Update class represents a single update to be performed by the game timer.
*/
public class Update
{
	/** The game update cycle when the game will run this update. */
	public long runAt;
	/** The type of update to be performed. */
	public String type;
	/** The ID of the update object to be performed (typically a reset). */
	public int target = 0;
	
	/** For progs: The room prog to run. */
	public RoomProg rprog = null;
	/** For progs: The mob prog to run. */
	public MobProg mprog = null;
	/** For progs: The obj prog to run. */
	public ObjProg oprog = null;
	/** For progs: The trigger associated with the prog. */
	public Trigger tby = null;
	/** For progs: The argument passed to the prog. */
	public String tArg = "";
	/** For progs: The location where the prog was triggered. */
	public Room loc = null;
	/** For progs: The character where the prog was triggered. */
	public CharData mob = null;
	/** For progs: The object where the prog was triggered. */
	public ObjData obj = null;
	/** For progs: The character triggering the prog. */
	public CharData actor = null;
	/** For progs: The character targeted by the actor. */
	public CharData victim = null;
	/** For progs: The line number to resume execution at. */
	public int startAt = 0;
	/** For progs: If/else level to resume execution at. */
	public int ifLevel = 0;
	/** For progs: If/else status to resume execution at. */
	public int skippingFrom = 0;
	/** For progs: Variables in use by the specific program. */
	HashMap<String, String> variables = null;
	
	/**
	General-purpose constructor.
	<p>
	This sets all three values of the update.
	
	@param delay The delay (in seconds) before the update will be performed.
	@param newType The type of the update.
	@param newTarget The target ID to perform the update on.
	*/
	public Update(float delay, String newType, int newTarget)
	{
		if (newType.equals("closeDoor"))
			for (Update u : updates)
				if (u.target == newTarget)
					if (u.type.equals(newType))
						u.runAt = updateCycles-1;

		runAt = updateCycles+((int)(delay*10));
		type = newType;
		target = newTarget;
	}
	
	public Update(int delay, RoomProg newRprog, MobProg newMprog, ObjProg newOprog,
					Trigger newTby, String newTArg, Room newLoc, CharData newMob,
					ObjData newObj, CharData newActor, CharData newVictim,
					int newStartAt, int newIfLevel, int newSkippingFrom,
					HashMap<String, String> newVariables)
	{
		runAt = updateCycles+delay;
		type = "prog";
		rprog = newRprog;
		mprog = newMprog;
		oprog = newOprog;
		tby = newTby;
		tArg = newTArg;
		loc = newLoc;
		mob = newMob;
		obj = newObj;
		actor = newActor;
		victim = newVictim;
		startAt = newStartAt;
		ifLevel = newIfLevel;
		skippingFrom = newSkippingFrom;
		variables = newVariables;
	}
	
	/**
	Run the update, checking the type and performing any appropriate actions.
	*/
	public void run()
	{
		if (type.equals("closeDoor"))
		{
			Exit targetExit = Exit.lookup(target);
			Room targetRoom = Exit.lookupRoom(target);
			
			if (targetExit == null || targetRoom == null)
				return;

			if (targetExit.flags.get("door") && !targetExit.flags.get("closed"))
				targetExit.flags.put("closed", true);
			if (targetExit.key != 0)
				targetExit.flags.put("locked", true);
			Database.saveExit(targetRoom, targetExit);
			
			Exit oppExit = targetExit.to.oppExit(targetRoom, targetExit.direction);
			if (oppExit != null)
			{
				if (oppExit.flags.get("door") && !oppExit.flags.get("closed"))
					oppExit.flags.put("closed", true);
				if (oppExit.key != 0)
					oppExit.flags.put("locked", true);
				Database.saveExit(targetExit.to, oppExit);
			}
			return;
		}
		if (type.equals("mobReset"))
		{
			Reset targetReset = Reset.lookup(target);
			if (targetReset == null)
				return;
			targetReset.fillReset();
			return;
		}
		if (type.equals("prog"))
		{
			if (rprog != null)
				rprog.run(tby, tArg, loc, mob, obj, actor, victim, startAt, ifLevel, skippingFrom, variables);
			if (mprog != null)
				mprog.run(tby, tArg, loc, mob, obj, actor, victim, startAt, ifLevel, skippingFrom, variables);
			if (oprog != null)
				oprog.run(tby, tArg, loc, mob, obj, actor, victim, startAt, ifLevel, skippingFrom, variables);
			return;
		}
	}
}