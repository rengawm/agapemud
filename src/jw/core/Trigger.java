package jw.core;

import static jw.core.MudMain.*;

/**
	The Trigger class represents a trigger set on a room/mob/object linking to
	a prog.
*/
public class Trigger
{
	public String type = "";
	public int numArg = 0;
	public String arg = "";
	public RoomProg rprog = null;
	public MobProg mprog = null;
	public ObjProg oprog = null;
	public Quest q = null;
	public QuestObjective qo = null;
	
	public boolean validate(String tText, int tNum)
	{
		if (type.equals("greet") || type.equals("leave") || 
			type.equals("dooropen") || type.equals("doorclose") ||
			type.equals("doorunlock") || type.equals("doorlock") ||
			type.equals("entry"))
		{
			if (!tText.equalsIgnoreCase(arg) && arg.length() > 0)
				return false;
			if (numArg > 0 && gen.nextInt(100)+1 > numArg)
				return false;
		}
		if (type.equals("random"))
		{
			if (gen.nextInt(100)+1 > numArg)
				return false;
		}
		if (type.equals("command"))
		{
			if (numArg > 0 && gen.nextInt(100)+1 > numArg)
				return false;
			
			String tCmd = CommandHandler.getArg(tText, 1);
			boolean found = false;
			String cCmd = "";
			int ctr = 1;
			while ((cCmd = CommandHandler.getArg(arg, ctr)).length() > 0)
			{
				if (cCmd.equalsIgnoreCase(tCmd))
					found = true;
				ctr++;
			}
			if (!found)
				return false;
		}
		if (type.equals("speech"))
		{
			if (numArg > 0 && gen.nextInt(100)+1 > numArg)
				return false;
			
			if (!UserCon.stripCodes(tText.toLowerCase()).contains(arg.toLowerCase()))
				return false;
		}
		if (type.equals("drop") || type.equals("put"))
		{
			if (numArg > 0 && numArg != tNum)
				return false;
		}
		if (type.equals("skill"))
		{
			if (numArg > 0 && gen.nextInt(100)+1 > numArg)
				return false;
			
			if (!tText.equals(arg.toLowerCase()))
				return false;
		}
		if (type.equals("time"))
		{
			if (numArg > 0 && numArg != tNum)
				return false;
		}
		if (type.equals("bribe"))
		{
			if (tNum < numArg)
				return false;
		}
		if (type.equals("hitpercent"))
		{
			if (tNum > numArg)
				return false;
		}
		if (type.equals("vischeck") || type.equals("look") ||
			type.equals("load") || type.equals("death") ||
			type.equals("kill") || type.equals("encounter") ||
			type.equals("combat") || type.equals("use") ||
			type.equals("wear") || type.equals("remove") ||
			type.equals("give") || type.equals("get"))
		{
			if (numArg > 0 && gen.nextInt(100)+1 > numArg)
				return false;
		}
		
		return true;
	}
}