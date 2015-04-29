package jw.core;
import java.util.*;
import jw.commands.*;
import static jw.core.MudMain.*;

/**
	The CommandHandler class contains methods central to executing commands and managing
	prompts. It also includes many string formatting and argument processing methods.
*/
public class CommandHandler
{
	/**
	Find a command that matches what a user just entered and run it, based on several
	status variables.
	<p>
	This method goes through a number of steps:
	<br>1: Is the user in pageMode? If so, just dump more saved output and ignore input.
	<br>2: Is the user in editMode? If so, redirect to the {@link
		CommandHandler#editor(UserCon, String) editor} method.
	<br>3: Parse {@code commandLine} and pull out the command word and arguments.
	<br>4: Check for an alias match or a shorthand match and run the alternative
		command instead.
	<br>5: If the user is in OLC mode, use their {@link UserCon#olcMode olcMode} string
		as a command prefix.
	<br>6: Look for an exit that matches the command in the user's current room.
	<br>7: Look for a standard command that matches the command.
	<br>8: Look for a social that matches the command.
	
	@param c The user who entered the command.
	@param commandLine The entire line of text entered by the user.
	*/
	public static void doCommand(UserCon c, String commandLine)
	{
		// User is in pageMode. Dump more output.
		if (c.pageMode)
		{
			// Copy the pageBuffer and clear it. This is done because the sendPlain
			// method will fill it up again if necessary - this way, nothing is ever
			// printed twice.
			String tempPage = c.pageBuffer;
			c.pageBuffer = "";
			c.pageMode = false;
			
			// Run through the pageBuffer once.
			c.sendPlain(tempPage);
			
			return;
		}

		// User is editing - move to the edit function.		
		if (c.cs == ConnState.EDITING)
		{
			if (commandLine.startsWith("/*"))
			{
				commandLine = commandLine.substring(2);
			}
			else
			{
				editor(c, commandLine);
				return;
			}
		}

		// Check for "!" (repeat last command) or update the lastCommand variable.
		if (!c.olcMatched)
		{
			if (commandLine.startsWith("!"))
			{
				commandLine = c.lastCommand;
			}
			else
			{
				c.lastCommand = commandLine;
			}
		}
		
		// Isolate the single command word from the args; clean both up.
		commandLine = commandLine.trim();
		String cmd = CommandHandler.getArg(commandLine, 1).trim().toLowerCase();
		String args = CommandHandler.getLastArg(commandLine, 2);

		// Check for a prompt leading back to a previous command.
		if (c.tempPrompt.length() > 0)
		{
			if (c.promptType.length() == 0) c.clearPrompt();

			// Find the appropriate prompt method and run it.
			for (Command ps : prompts)
				if (ps.alias.get(0).equalsIgnoreCase(c.promptType))
				{
					ps.run(c, commandLine);
					return;
				}

			c.sendln("Prompt Error: "+c.promptType+" has no prompt handler match.");
			c.clearPrompt();
			return;
		}

		c.showPrompt = true;
		
		// Check for a custom alias match.
		for (int ctr = 0; ctr < c.aliases.size()-1; ctr += 2)
			if (c.aliases.get(ctr).equals(cmd))
			{
				doCommand(c, c.aliases.get(ctr+1)+" "+args);
				return;
			}

		// Special command prefixes.
		if (commandLine.startsWith("/"))
		{
			doCommand(c, commandLine.replaceFirst("\\/", "recall "));
			return;
		}
		else if (commandLine.startsWith(","))
		{
			doCommand(c, commandLine.replaceFirst("\\,", "emote "));
			return;
		}
		else if (commandLine.startsWith("="))
		{
			doCommand(c, commandLine.replaceFirst("\\=", "gtell "));
			return;
		}
		else if (commandLine.startsWith("."))
		{
			doCommand(c, commandLine.replaceFirst("\\.", "fellowship "));
			return;
		}
		else if (commandLine.startsWith("'"))
		{
			doCommand(c, commandLine.replaceFirst("\\'", "say "));
			return;
		}
		else if (commandLine.startsWith("?"))
		{
			doCommand(c, commandLine.replaceFirst("\\?", "qa "));
			return;
		}
		else if (commandLine.startsWith("-"))
		{
			doCommand(c, commandLine.replaceFirst("\\-", "replay "));
			return;
		}
		else if (commandLine.startsWith("`"))
		{
			doCommand(c, commandLine.replaceFirst("\\`", "reply "));
			return;
		}
		else if (commandLine.startsWith(":"))
		{
			doCommand(c, commandLine.replaceFirst("\\:", "stafftalk "));
			return;
		}
		// If the user is on OLC mode, attempt to use their OLC mode as a command
		// prefix. If the command meant something to the OLC method, exit this method.
		// Otherwise, continue looking for a command.
		else if (c.olcMode.length() > 0 && !c.olcMatched && cmd.length() != 1)
		{
			c.olcMatched = true;
			if (cmd.length() == 0)
				commandLine = "info";
			doCommand(c, c.olcMode+" "+commandLine);
			if (c.olcMatched)
			{
				c.olcMatched = false;
				return;
			}
			c.olcMatched = false;
		}

		// User just hit enter? Just return - prompt will be printed.
		if (cmd.length() == 0)
			return;
		

		// Check for a valid exit match.
		if (c.ch.currentRoom.takeExit(c, cmd))
			return;
		
		// Find the appropriate command method and run it.
		String cmdTemp = "";
		for (Command cm : commands)
			for (String as : cm.alias)
				if (as.startsWith(cmd))
					if (cm.allowCheck(c))
					{
						if (as.compareTo(cmdTemp) < 0 || cmdTemp.length() == 0)
							cmdTemp = as;
					}
		
		if (cmdTemp.length() > 0)
			for (Command cm : commands)
				for (String as : cm.alias)
					if (as.equals(cmdTemp))
					{
						if (cm.log)
							sysLog("commands", c.ch.shortName+": "+cm.fullName+" "+args);
						cm.run(c, args);
						return;
					}
				

		// Find a skill match and run it.
		for (Skill s : skills)
			if (s.type.equals("skill") && s.name.startsWith(cmd) && c.ch.skillPercent(s) > 0)
			{
				if (s.flags.get("passive"))
				{
					c.sendln(Fmt.cap(s.name)+" is a passive spell and is activated automatically.");
					return;
				}
				if (!c.ch.position.equals("standing"))
				{
					c.sendln("You can't do that when you're "+c.ch.position+".");
					return;
				}
				if (c.ch.energy < s.cost)
				{
					c.sendln("You don't have enough energy to use that ability.");
					return;
				}
				if (!Combat.checkTarget(c.ch, s, args))
					return;
				c.ch.combatQueue.add("\""+s.name+"\" "+args);
				c.ch.combatQueue.add("dsk#"+s.useDelay);
				c.showPrompt = false;
				return;
			}

		// Find a social match and run it.
		for (Social s : socials)
			if (s.name.startsWith(cmd))
			{
				if (c.ch.position.equals("sleeping"))
				{
					c.sendln("You can't do that when you're "+c.ch.position+".");
					return;
				}
				s.localSocial(c, args);
				return;
			}

		c.sendln("Unknown command. For assistance, type }H'}hhelp}H'{x.");
	}
	
	/**
	Run the user's input through the text editor and check for editor command matches.
	<p>
	The method will first look for a matching /command. If the user just entered a line
	of text, it will simply be added to their editorContents. This method includes all
	editor command processing and editor content manipulation.
	
	@param c The user who entered the input.
	@param commandLine The entire line of text entered by the user.
	*/
	public static void editor(UserCon c, String commandLine)
	{
		c.showPrompt = true;
		String arg1 = getArg(commandLine, 1);
		String arg2 = getArg(commandLine, 2);
		String arg3 = getArg(commandLine, 3);

		if (commandLine.length() < 2)
		{
			c.editorContents.add(commandLine);
		} else if (arg1.equalsIgnoreCase("/c"))
		{
			c.editorContents = new ArrayList<String>();
			c.sendln("Text cleared.");
		} else if (arg1.equalsIgnoreCase("/d") && arg2.length() > 0)
		{
			int lineNr = Fmt.getInt(arg2);
			
			if (lineNr < 1 || lineNr > c.editorContents.size())
			{
				c.sendln("Line number is too high or too low. Use }H'}h/l}H'{x to view line numbers.");
				return;
			}
			c.editorContents.remove(lineNr-1);
			c.sendln("{wLine "+lineNr+" deleted.");
		} else if (arg1.equalsIgnoreCase("/f"))
		{
			if (c.promptType.contains("Code"))
				c.editorContents = Script.formatCode(c, c.editorContents);
			else
				c.editorContents = Fmt.formatString(c.editorContents, 75, true);
			c.sendln("Editor contents formatted.");
		} else if (arg1.equalsIgnoreCase("/i") && arg2.length() > 0)
		{
			arg3 = getLastArg(commandLine, 3);
			int lineNr = Fmt.getInt(arg2);
			
			if (lineNr < 1 || lineNr > c.editorContents.size())
			{
				c.sendln("Line number is too high or too low. Use }H'}h/l}H'{x to view line numbers.");
				return;
			}
			c.editorContents.add(lineNr-1, arg3);
			c.sendln("Line "+lineNr+" inserted.");
		} else if (arg1.equalsIgnoreCase("/v"))
		{
			c.sendln("Current text:");
			for (int ctr = 0; ctr < c.editorContents.size(); ctr++)
				c.sendln(Fmt.fit(""+(ctr+1), 2)+": "+c.editorContents.get(ctr).replace("^\\", "^P"));
		} else if (arg1.equalsIgnoreCase("/vc"))
		{
			c.sendln("Current text with codes:");
			for (int ctr = 0; ctr < c.editorContents.size(); ctr++)
				c.sendPlain(c.editorContents.get(ctr)+"\n\r");
		} else if (arg1.equalsIgnoreCase("/q") || arg1.equalsIgnoreCase("/a"))
		{
			c.clearEditMode();
			c.sendln("Editing aborted: Text was not saved.");
		} else if (arg1.equalsIgnoreCase("/r"))
		{
			for (int ctr = 0; ctr < c.editorContents.size(); ctr++)
			{
				String temp = c.editorContents.get(ctr).replace(arg2, arg3);
				c.editorContents.remove(ctr);
				c.editorContents.add(ctr, temp);
			}
			c.sendln("All occurrences of '"+arg2+"' replaced with '"+arg3+"'.");
		} else if (arg1.equalsIgnoreCase("/lr") && arg2.length() > 0 && arg3.length() > 0)
		{
			arg3 = getLastArg(commandLine, 3);
			int lineNr = Fmt.getInt(arg2);
			
			if (lineNr < 1 || lineNr > c.editorContents.size())
			{
				c.sendln("Line number is too high or too low. Use }H'}h/l}H'{x to view line numbers.");
				return;
			}
			
			if (arg3.length() < 1)
			{
				c.sendln("Syntax is: }H/lr <}hline number to delete}H> <}htext to insert}H>");
				return;
			}

			c.editorContents.remove(lineNr-1);
			c.sendln("{wLine "+lineNr+" deleted.");
			c.editorContents.add(lineNr-1, arg3);
			c.sendln("Line "+lineNr+" inserted.");
		} else if (arg1.equalsIgnoreCase("/s"))
		{
			String finishedText = "";
			for (int ctr = 0; ctr < c.editorContents.size(); ctr++)
			{
				if (ctr > 0)
					finishedText = finishedText+"^/";
				finishedText = finishedText+c.editorContents.get(ctr);
			}
			
			// Find the originating method and return the contents of the editor to it.
			for (Command ps : prompts)
				if (ps.alias.get(0).equalsIgnoreCase(c.promptType))
				{
					ps.run(c, finishedText);
					return;
				}
			c.sendln("Prompt Error: "+c.promptType+" has no editor prompt handler match.");
			c.clearEditMode();
			return;
		} else if (arg1.equalsIgnoreCase("/h") || arg1.equalsIgnoreCase("/"))
		{
			InfoCommands.doHelp(c, "editor commands");
		} 
		else
		{
			if (c.promptType.contains("Code"))
			{
				c.editorContents.add(commandLine);
			}
			else if (UserCon.codelessLength(commandLine) <= 75)
			{
				commandLine = commandLine.replace("^^", "SPEC_DOUBLE_CARROT");
				ArrayList<String> addText = new ArrayList<String>();
				for (String s : commandLine.split("\\^/", -1))
				{
					s = s.replace("SPEC_DOUBLE_CARROT", "^^");
					c.editorContents.add(s);
				}
			}
			else
			{
				c.sendln("Long line automatically formatted.");
				commandLine = commandLine.replace("^^", "SPEC_DOUBLE_CARROT");
				ArrayList<String> addText = new ArrayList<String>();
				for (String s : commandLine.split("\\^/", -1))
				{
					s = s.replace("SPEC_DOUBLE_CARROT", "^^");
					addText.add(s);
				}
				boolean indent = true;
				for (String s : Fmt.formatString(addText, 75, indent))
				{
					c.editorContents.add(s);
					indent = false;
				}
			}
		}
	}
	
	/**
	Use {@link CommandHandler#isolateFirst(String) isolateFirst} to pull arguments
	off and return a single-phrase argument.
	
	@param args The full argument given to the command.
	@param count The number of the argument to isolate. (2 = second argument, etc.)
	@return A string containing only the specified argument.
	*/
	public static String getArg(String args, int count)
	{
		String remain = args;
		String result = "";
		for (int ctr = 1; ctr <= count; ctr++)
		{
			result = isolateFirst(remain)[0];
			remain = isolateFirst(remain)[1];
		}
		return result;
	}
	
	/**
	Use {@link CommandHandler#isolateFirst(String) isolateFirst} to pull arguments
	off and return the remainder of {@code args}.
	
	@param args The full argument given to the command.
	@param count The number of the argument to isolate. (2 = second argument, etc.)
	@return A string containing all text after {@code count-1} arguments have been
		removed.
	*/
	public static String getLastArg(String args, int count)
	{
		String remain = args;
		String result = "";
		for (int ctr = 1; ctr < count; ctr++)
		{
			result = isolateFirst(remain)[0];
			remain = isolateFirst(remain)[1];
		}
		return remain;
	}
	
	/**
	Separate the first argument and the rest of the arguments.
	<p>
	This supports quotes in arguments.
	@param args The string to pull an argument from.
	@return An array such that the first element is the first argument and the second
		element is the remainder of the original {@code args} string after the
		first argument has been removed.
	*/
	public static String[] isolateFirst(String args)
	{
		String result[] = new String[2];
		result[0] = "";
		result[1] = "";
		
		if (args.length() == 0)
			return result;
		
		if (args.charAt(0) == '"')
		{
			int nextQuote = args.indexOf("\"", 1);
			if (nextQuote == -1)
			{
				result[0] = args.split(" ", 2)[0];
				if (args.split(" ", 2).length > 1)
					result[1] = args.split(" ", 2)[1].trim();
			}
			else
			{
				result[0] = args.substring(1, nextQuote);
				if (args.length() > nextQuote)
					result[1] = args.substring(nextQuote+1).trim();
				else
					result[1] = "";
			}
		}
		else if (args.charAt(0) == '\'')
		{
			int nextQuote = args.indexOf("'", 1);
			if (nextQuote == -1)
			{
				result[0] = args.split(" ", 2)[0];
				if (args.split(" ", 2).length > 1)
					result[1] = args.split(" ", 2)[1].trim();
			}
			else
			{
				result[0] = args.substring(1, nextQuote);
				if (args.length() > nextQuote)
					result[1] = args.substring(nextQuote+1).trim();
				else
					result[1] = "";
			}
		}
		else
		{
			result[0] = args.split(" ", 2)[0];
			if (args.split(" ", 2).length > 1)
				result[1] = args.split(" ", 2)[1].trim();
		}

		return result;
	}
}