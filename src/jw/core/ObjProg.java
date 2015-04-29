package jw.core;
import java.util.*;

import static jw.core.MudMain.*;

/**
	The ObjProg class represents a single object program.
*/
public class ObjProg
{
	public int id;
	public String name = "";
	public String description = "";
	public ArrayList<String> code = new ArrayList<String>();
	
	public ObjProg(int newId)
	{
		id = newId;
	}
	
	public int run(Trigger tby, String args, Room loc, CharData mob, ObjData obj, CharData actor, CharData victim, int startAt, int ifLevel, int skippingFrom, HashMap<String, String> variables)
	{
		if (variables == null)
			variables = new HashMap<String, String>();
		int lineCtr = 0;
		if (loc == null)
			loc = obj.getCurrentRoom();
		Room defaultLoc = loc; // For at.
		
		for (String line : code)
		{
			line = line.trim();
			loc = defaultLoc; // For at.
			CharData target = null;
			lineCtr++;
			String debug = "oprog #"+id+" at obj #"+obj.op.id+", line "+lineCtr;
			
			if (lineCtr < startAt)
				continue;
			
			if (line.trim().equalsIgnoreCase("else"))
			{
				if (skippingFrom <= ifLevel)
				{
					if (skippingFrom > 0)
						skippingFrom = 0;
					else
						skippingFrom = ifLevel;
				}
				continue;
			}
			if (line.trim().equalsIgnoreCase("endif"))
			{
				if (skippingFrom == ifLevel)
					skippingFrom = 0;
				ifLevel--;
				continue;
			}
			
			if (skippingFrom > 0)
			{
				if (line.toLowerCase().trim().startsWith("if "))
					ifLevel++;
				continue;
			}
			
			// Translate % keywords to text.
			line = Script.resolveString(line, tby, args, variables, actor, victim, loc, mob, obj);

			// Pull out commands/arguments.
			String command = CommandHandler.getArg(line, 1).toLowerCase();
			String fullArg = CommandHandler.getLastArg(line, 2);
			String arg1 = CommandHandler.getArg(fullArg, 1);
			String arg2 = CommandHandler.getLastArg(fullArg, 2);
			
			// Program flow control commands go here.
			if (command.equals("at"))
			{
				loc = Room.lookup(Fmt.getInt(arg1));
				if (loc == null)
				{
					target = Combat.findChar(null, defaultLoc, arg1, true);
					if (target != null)
						loc = target.currentRoom;
				}
				if (loc == null)
				{
					sysLog("progs", "Warning: Invalid at target in "+debug+".");
					continue;
				}
				
				// Pull out commands/arguments.
				command = CommandHandler.getArg(arg2, 1).toLowerCase();
				fullArg = CommandHandler.getLastArg(arg2, 2);
				arg1 = CommandHandler.getArg(fullArg, 1);
				arg2 = CommandHandler.getLastArg(fullArg, 2);
			}
			if (command.equals("if"))
			{
				ifLevel++;
				if (!Script.evalIf(fullArg, debug))
					skippingFrom = ifLevel;
				continue;
			}
			if (command.equals("return"))
			{
				if (arg1.length() == 0)
					return -1;
				else
					return Fmt.getInt(Script.evalMath(arg1, debug));
			}
			if (command.equals("wait"))
			{
				updates.add(new Update(Fmt.getInt(Script.evalMath(arg1, debug)), null, null, this, tby,
							args, loc, mob, obj, actor, victim, lineCtr+1, ifLevel, skippingFrom, variables));
				return -1;
			}
			if (command.equals("lset"))
			{
				if (arg1.trim().length() == 0)
					continue;
				
				variables.remove(arg1);

				if (arg2.length() > 0)
				{
					arg2 = Script.evalMath(arg2);
					variables.put(arg1, arg2);
				}
				continue;
			}

			dummyChar.currentRoom = loc;
			dummyChar.mobCommand(command+" "+fullArg, progDummyCon);
			dummyChar.currentRoom = null;
		}
		
		return -1;
	}

	public static ObjProg lookup(int targetId)
	{
		for (ObjProg op : oprogs)
			if (op.id == targetId)
				return op;
		return null;
	}
}