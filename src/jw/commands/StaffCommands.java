package jw.commands;
import java.util.*;

import jw.core.*;
import jw.data.*;
import static jw.core.MudMain.*;

/**
	The StaffCommands class contains general purpose staff commands.
*/
public class StaffCommands
{
	/**
	Add permissions or individual command grants to a user.
	
	@param c The user who entered this command.
	@param args A string containing any arguments to the command.
	*/
	public static void doGrant(UserCon c, String args)
	{
		args = args.toLowerCase();
		String arg1 = CommandHandler.getArg(args, 1);
		String arg2 = CommandHandler.getArg(args, 2);
		String arg3 = CommandHandler.getArg(args, 3);
		
		if (arg1.length() == 0)
		{
			InfoCommands.doHelp(c, "grant");
			return;
		}

		for (UserCon cs : conns)
			if (cs.ch.shortName.toLowerCase().startsWith(arg1))
			{
				if (arg2.length() == 0 || arg3.length() == 0)
				{
					c.sendln("Current permission groups granted to "+cs.ch.shortName+":");
					for (String s : cs.permissions)
						c.sendln("  "+s);
					if (cs.permissions.size() == 0)
						c.sendln("  "+cs.ch.shortName+" has no permission groups set.");
					c.sendln("Current commands granted to "+cs.ch.shortName+":");
					for (String s : cs.granted)
						c.sendln("  "+s);
					if (cs.granted.size() == 0)
						c.sendln("  "+cs.ch.shortName+" has no command grants set.");
					return;
				}
				if (cs == c)
				{
					c.sendln("You can't grant or revoke your own permissions.");
					return;
				}
				if ("permission".startsWith(arg2))
				{
					for (String s : Flags.permissionTypes)
						if (s.equals(arg3) && (c.hasPermission(s) || c.hasPermission("admin")))
						{
							if (cs.hasPermission(s))
							{
								for (int ctr = 0; ctr < cs.permissions.size(); ctr++)
									if (cs.permissions.get(ctr).equals(s))
									{
										cs.permissions.remove(ctr);
										c.sendln("'"+s+"' revoked from "+cs.ch.shortName+".");
										cs.sendln("Your '"+s+"' permissions have been revoked by "+Fmt.seeName(cs.ch, c.ch)+".");
										break;
									}
							}
							else
							{
								cs.permissions.add(s);
								c.sendln("'"+s+"' granted to "+cs.ch.shortName+".");
								cs.sendln("You have been granted '"+s+"' permissions by "+Fmt.seeName(cs.ch, c.ch)+".");
							}
							Database.saveAccount(cs);
							return;
						}
					c.sendln("'"+arg3+"' is not a valid permission setting.");
					c.send("Valid permissions are:");
					for (String p : Flags.permissionTypes)
						c.send(" "+p);
					c.sendln("");
					return;
				}
				if ("command".startsWith(arg2))
				{
					for (Command cmd : commands)
						if (cmd.fullName.equals(arg3) && cmd.allowCheck(c))
						{
							for (int ctr = 0; ctr < cs.granted.size(); ctr++)
								if (cs.granted.get(ctr).equals(cmd.fullName))
								{
									cs.granted.remove(ctr);
									c.sendln("'"+cmd.fullName+"' revoked from "+cs.ch.shortName+".");
									cs.sendln("Your '"+cmd.fullName+"' command has been revoked by "+Fmt.seeName(cs.ch, c.ch)+".");
									Database.saveAccount(cs);
									return;
								}
							cs.granted.add(cmd.fullName);
							c.sendln("'"+cmd.fullName+"' granted to "+cs.ch.shortName+".");
							cs.sendln("You have been granted access to '"+cmd.fullName+"' by "+Fmt.seeName(cs.ch, c.ch)+".");
							Database.saveAccount(cs);
							return;
						}
						
					c.sendln("'"+arg3+"' is not a valid command name.");
					c.sendln("You must use the full command name instead of any shortened versions. Try 'commands'.");
					return;
				}

				InfoCommands.doHelp(c, "grant");
				return;
			}

		c.sendln("No character by that name was found.");
	}
	
	public static void doRole(UserCon c, String args)
	{
		String arg1 = CommandHandler.getArg(args, 1);
		String arg2 = CommandHandler.getLastArg(args, 2);
		
		if (arg1.length() == 0)
		{
			c.sendln("Set whose role?");
			return;
		}
		
		CharData target = Combat.findChar(c.ch, null, arg1, true);
		
		if (target == null)
		{
			c.sendln("No character by that name was found.");
			return;
		}
		
		if (target.conn.isDummy)
		{
			c.sendln("You can't set roles on mobs.");
			return;
		}
		
		target.conn.role = arg2;
		c.sendln(Fmt.seeNameGlobal(c.ch, target)+"'s role has been set.");
		target.sendln(Fmt.cap(Fmt.seeNameGlobal(target, c.ch))+" has set your role to '"+arg2+"{x'.");
		Database.saveAccount(target.conn);
	}
	
	public static void doMultok(UserCon c, String args)
	{
		if (args.length() == 0)
		{
			c.sendln(Fmt.heading("Existing Mult-OKs"));
			for (Moban m : mobans)
				if (m.category.equals("multok"))
				{
					if (m.end == 0)
						c.sendln("}M[}n"+m.type.substring(0, 2)+"}M] }m"+Fmt.fit(m.host, 35)+" }M: }NForever");
					else
						c.sendln("}M[}n"+m.type.substring(0, 2)+"}M] }m"+Fmt.fit(m.host, 35)+" }M: }N"+frmt.format(m.end*1000));
					c.sendln("}M    -- }N"+m.description+"{x");
				}
			return;
		}
		Moban.runCommand(c, "multok", args);
	}

	public static void doBan(UserCon c, String args)
	{
		if (args.length() == 0)
		{
			c.sendln(Fmt.heading("Existing Bans"));
			for (Moban m : mobans)
				if (m.category.equals("ban"))
				{
					if (m.end == 0)
						c.sendln("}M[}n"+m.type.substring(0, 2)+"}M] }m"+Fmt.fit(m.host, 35)+" }M: }NForever");
					else
						c.sendln("}M[}n"+m.type.substring(0, 2)+"}M] }m"+Fmt.fit(m.host, 35)+" }M: }N"+frmt.format(m.end*1000));
					c.sendln("}M    -- }N"+m.description+"{x");
				}
			return;
		}
		Moban.runCommand(c, "ban", args);
	}
	
	public static void doTrouble(UserCon c, String args)
	{
		if (args.length() == 0)
		{
			c.sendln("Trouble who?");
			return;
		}
		
		CharData target = Combat.findChar(c.ch, null, args, true);
		
		if (target == null)
		{
			c.sendln("No character by that name was found.");
			return;
		}
		
		if (target.conn.isDummy)
		{
			c.sendln("You can't trouble mobs.");
			return;
		}
		
		if (target.conn.hasPermission("staff"))
		{
			c.sendln("You can't trouble staff members.");
			return;
		}
		
		if (target.conn.troubled)
		{
			c.sendln(Fmt.cap(Fmt.seeNameGlobal(c.ch, target))+" is no longer troubled.");
			target.sendln("Your troublemaker status has been lifted.");
			target.conn.troubled = false;
			Database.saveAccount(target.conn);
			return;
		}
		
		c.sendln(Fmt.cap(Fmt.seeNameGlobal(c.ch, target))+" is now troubled.");
		target.sendln("You have been identified as a troublemaker by the game administration.");
		target.sendln("");
		target.sendln("This means that you can't communicate with other players outside of your");
		target.sendln("current room. You can use the 'tt' channel to communicate. Contact a staff");
		target.sendln("member with the 'admin' channel if you would like to ask about or appeal");
		target.sendln("this action.");
		target.conn.troubled = true;
		Database.saveAccount(target.conn);
	}
	
	public static void doJail(UserCon c, String args)
	{
		if (args.length() == 0)
		{
			c.sendln("Jail who?");
			return;
		}
		
		CharData target = Combat.findChar(c.ch, null, args, true);
		
		if (target == null)
		{
			c.sendln("No character by that name was found.");
			return;
		}
		
		if (target.conn.isDummy)
		{
			c.sendln("You can't jail mobs.");
			return;
		}
		
		if (target.conn.hasPermission("staff"))
		{
			c.sendln("You can't jail staff members.");
			return;
		}
		
		if (target.currentRoom.id == Flags.jailId)
		{
			if (Room.lookup(Flags.recallId) == null)
			{
				sysLog("bugs", "No recall room at "+Flags.recallId+".");
				return;
			}
			target.currentRoom = Room.lookup(Flags.recallId);
			target.sendln("You have been released from jail.");
			c.sendln(Fmt.cap(Fmt.seeNameGlobal(c.ch, target))+" has been released from jail.");
			return;
		}
		
		if (Room.lookup(Flags.jailId) == null)
		{
			sysLog("bugs", "No jail room at "+Flags.jailId+".");
			return;
		}
		target.currentRoom = Room.lookup(Flags.jailId);
		target.sendln("You have been put in jail.");
		target.sendln("");
		target.sendln("While in jail, you can't play the game. Unless you have also been troubled,");
		target.sendln("you can still communicate with other players. Contact a staff member with the");
		target.sendln("'admin' channel if you would like to ask about or appeal this action.");
		c.sendln(Fmt.cap(Fmt.seeNameGlobal(c.ch, target))+" has been jailed. Note that this doesn't block global channels.");
		c.sendln("Use trouble in conjunction with jail for the full effect.");
	}
	
	public static void doRevoke(UserCon c, String args)
	{
		String arg1 = CommandHandler.getArg(args, 1).toLowerCase();
		String arg2 = CommandHandler.getArg(args, 2).toLowerCase();

		if (arg1.length() == 0)
		{
			c.sendln("Revoke a command from whom?");
			return;
		}
		
		CharData target = Combat.findChar(c.ch, null, arg1, true);
		
		if (target == null)
		{
			c.sendln("No character by that name was found.");
			return;
		}
		
		if (target.conn.isDummy)
		{
			c.sendln("You can't revoke commands from mobs.");
			return;
		}
		
		if (target.conn.hasPermission("staff"))
		{
			c.sendln("You can't revoke commands from staff members.");
			return;
		}
		
		if (arg2.length() == 0)
		{
			String temp = Fmt.cap(Fmt.seeNameGlobal(c.ch, target))+"'s Revoked Commands:";
			for (String s : target.conn.revoked)
				temp = temp+" "+s;
			if (target.conn.revoked.size() == 0)
				temp = temp+" none";
			c.sendln(temp);
			return;
		}
		
		for (Command cm : commands)
			if (cm.fullName.equalsIgnoreCase(arg2))
			{
				if (target.conn.revoked.contains(cm.fullName))
				{
					target.conn.revoked.remove(cm.fullName);
					c.sendln("'"+Fmt.cap(cm.fullName)+"' removed from "+Fmt.seeNameGlobal(c.ch, target)+"'s revoke list.");
					target.sendln("Your access to the '"+cm.fullName+"' command has been restored."); 
					Database.saveAccount(target.conn);
					return;
				}
				target.conn.revoked.add(cm.fullName);
				c.sendln("'"+Fmt.cap(cm.fullName)+"' added to "+Fmt.seeNameGlobal(c.ch, target)+"'s revoke list.");
				target.sendln("Your access to the '"+cm.fullName+"' command has been revoked."); 
				Database.saveAccount(target.conn);
				return;
			}
		
		c.sendln("That command was not found. The full command name (not an alias) must be used.");
	}
	
	public static void doSnoop(UserCon c, String args)
	{
		if (c.snoop != null)
		{
			c.snoop = null;
			c.sendln("You stop snooping.");
			return;
		}
		
		if (args.length() == 0)
		{
			c.sendln("Snoop whom?");
			return;
		}
		
		CharData target = Combat.findChar(c.ch, null, args, true);
		
		if (target == null)
		{
			c.sendln("No character by that name was found.");
			return;
		}
		
		if (target.conn.isDummy)
		{
			c.sendln("You can't snoop mobs.");
			return;
		}
		
		if (target.conn.hasPermission("staff"))
		{
			c.sendln("You can't snoop staff members.");
			return;
		}
		
		c.snoop = target.conn;
		c.sendln("You are now snooping "+target.shortName+".");
	}
	
	public static void doLog(UserCon c, String args)
	{
		if (args.length() == 0)
		{
			c.sendln("Log whom?");
			return;
		}
		
		CharData target = Combat.findChar(c.ch, null, args, true);
		
		if (target == null)
		{
			c.sendln("No character by that name was found.");
			return;
		}
		
		if (target.conn.isDummy)
		{
			c.sendln("You can't log mobs.");
			return;
		}
		
		if (target.conn.hasPermission("staff"))
		{
			c.sendln("You can't log staff members.");
			return;
		}
		
		if (target.conn.logged)
		{
			target.conn.logged = false;
			Database.saveAccount(target.conn);
			c.sendln(Fmt.cap(Fmt.seeName(c.ch, target))+" is no longer being logged.");
			return;
		}
		target.conn.logged = true;
		Database.saveAccount(target.conn);
		c.sendln(Fmt.cap(Fmt.seeName(c.ch, target))+" is now being logged.");
	}
    
    

	/**
	Instantly kill a target. This uses Combat.damage().
	
	@param c The user who entered this command.
	@param args A string containing any arguments to the command.
	*/
	public static void doSlay(UserCon c, String args)
	{
		if (c.ch.position.equals("sleeping"))
		{
			c.sendln("You can't do that when you're "+c.ch.position+".");
			return;
		}

		if (args.trim().length() == 0)
		{
			c.sendln("Slay who?");
			return;
		}
			
		CharData target;
		
		if ((target = Combat.findChar(c.ch, null, args, false)) == null)
		{
			c.sendln("There isn't anyone here by that name.");
			return;
		}
		if (target == c.ch)
		{
			c.sendln("You can't slay yourself.");
			return;
		}
		
		for (UserCon cs : conns)
			if (cs.ch.currentRoom == c.ch.currentRoom && cs != c)
				if (!cs.ch.position.equals("sleeping"))
					cs.sendln("{1"+Fmt.cap(Fmt.seeName(cs.ch, c.ch))+" slays "+Fmt.seeName(cs.ch, target)+"!{x");
		c.sendln("{1You slay "+Fmt.seeName(c.ch, target)+"!{x");
		
		target.hp = 0;
		Combat.checkForDeath(c.ch, target);
	}
	
	/**
	Rename a player to the given string.
	
	@param c The user who entered this command.
	@param args A string containing any arguments to the command.
	*/
	public static void doRename(UserCon c, String args)
	{
		String arg1 = CommandHandler.getArg(args, 1);
		String arg2 = CommandHandler.getArg(args, 2);
		
		if (arg1.length() == 0 || arg2.length() == 0)
		{
			c.sendln("Syntax: }H'}hrename }H<}htarget}H> <}hnew name}H>'{w");
			return;
		}

		CharData target;
		
		if ((target = Combat.findChar(c.ch, null, arg1, true)) == null)
		{
			c.sendln("No character by that name was found.");
			return;
		}
		if (target.conn.isDummy)
		{
			c.sendln("You can only rename players, not mobs.");
			return;
		}

		if (Database.nameTaken(arg2))
		{
			c.sendln("That name is already in use.");
			return;
		}
		
		for (int ctr = 0; ctr < arg2.length(); ctr++)
		{
			char cr = arg2.charAt(ctr);
			if ((cr < 'a' && cr > 'z') && (cr < 'A' && cr > 'Z'))
			{
				c.sendln("The new name must contain only alphabetic characters (a-z, A-Z).");
				return;
			}
		}
		arg2 = arg2.substring(0, 1).toUpperCase()+arg2.substring(1);
		
		c.sendln("'"+target.shortName+"' has been renamed to '"+arg2+"'.");
		target.shortName = arg2;
		target.name = arg2;
		if (target != c.ch)
			target.sendln(Fmt.cap(Fmt.seeName(target, c.ch))+" has renamed you to '"+arg2+"'.");
		target.save();
	}
	
	/**
	Move to the specified room ID or name.
	
	@param c The user who entered this command.
	@param args A string containing any arguments to the command.
	*/
	public static void doGoto(UserCon c, String args)
	{
		if (c.ch.position.equals("sleeping"))
		{
			c.sendln("You can't do that when you're "+c.ch.position+".");
			return;
		}

		if (args.trim().length() == 0)
		{
			c.sendln("Syntax: }H'}hgoto }H<}iroom id}H|}hcharacter name}H>'{x");
			return;
		}
		
		Room targetRoom = Room.lookup(Fmt.getInt(args));
		if (targetRoom == null)
		{
			CharData targetChar = Combat.findChar(c.ch, null, args, true);
			if (targetChar == null)
			{
				c.sendln("That room ID or character was not found.");
				return;
			}
			targetRoom = targetChar.currentRoom;
		}
		
		if (targetRoom == c.ch.currentRoom)
		{
			c.sendln("You're already here!");
			return;
		}
		
		for (UserCon cs : conns)
			if (cs.ch.currentRoom == c.ch.currentRoom && cs != c)
				if (!cs.ch.position.equals("sleeping"))
					if (Combat.canSee(cs.ch, c.ch))
						if (c.poofout.length() == 0)
							cs.sendln(Fmt.cap(Fmt.seeName(cs.ch, c.ch))+" looks upward and suddenly shoots into the air.");
						else
							cs.sendln(c.poofout+"{x");
		
		c.ch.currentRoom = targetRoom;
		RoomCommands.doLook(c, "");
		
		for (UserCon cs : conns)
			if (cs.ch.currentRoom == c.ch.currentRoom && cs != c)
				if (!cs.ch.position.equals("sleeping"))
					if (Combat.canSee(cs.ch, c.ch))
						if (c.poofin.length() == 0)
							cs.sendln(Fmt.cap(Fmt.seeName(cs.ch, c.ch))+" sails in on a breeze.");
						else
							cs.sendln(c.poofin+"{x");

		c.ch.save();
	}
	
	public static void doPoofin(UserCon c, String args)
	{
		if (args.length() == 0)
		{
			c.poofin = "";
			c.sendln("Poofin cleared.");
		}
		else
		{
			if (!UserCon.stripCodes(args).toLowerCase().contains(c.ch.shortName.toLowerCase()))
			{
				c.sendln("Your poofin must contain your name.");
				return;
			}
			c.poofin = args;
			c.sendln("Poofin set.");
		}
		Database.saveAccount(c);
	}

	public static void doPoofout(UserCon c, String args)
	{
		if (args.length() == 0)
		{
			c.poofout = "";
			c.sendln("Poofout cleared.");
		}
		else
		{
			if (!UserCon.stripCodes(args).toLowerCase().contains(c.ch.shortName.toLowerCase()))
			{
				c.sendln("Your poofout must contain your name.");
				return;
			}
			c.poofout = args;
			c.sendln("Poofout set.");
		}
		Database.saveAccount(c);
	}
	
	/**
	Perform a command at the location of a given room ID or name.
	
	@param c The user who entered this command.
	@param args A string containing any arguments to the command.
	*/
	public static void doAt(UserCon c, String args)
	{
		if (c.ch.position.equals("sleeping"))
		{
			c.sendln("You can't do that when you're "+c.ch.position+".");
			return;
		}

		String arg1 = CommandHandler.getArg(args, 1);
		String arg2 = CommandHandler.getLastArg(args, 2);
		
		if (arg1.length() == 0 || arg2.length() == 0)
		{
			c.sendln("Syntax: }H'}hat }H<}iroom id}H|}hcharacter name}H> <}hcommand}H>'{x");
			return;
		}
		
		Room previousRoom = c.ch.currentRoom;
		
		Room targetRoom = Room.lookup(Fmt.getInt(arg1));
		if (targetRoom == null)
		{
			CharData targetChar = Combat.findChar(c.ch, null, arg1, true);
			if (targetChar == null)
			{
				c.sendln("That room ID or character was not found.");
				return;
			}
			targetRoom = targetChar.currentRoom;
		}
		
		if (arg2.startsWith("!"))
		{
			c.sendln("Using ! in the at command can have disastrous consequences!");
			return;
		}
		c.ch.currentRoom = targetRoom;
		c.commandQueue.add(arg2);
		c.processCommand();

		c.lastCommand = "at "+args;

		c.ch.currentRoom = previousRoom;
	}
	
	public static void doEcho(UserCon c, String args)
	{	
		if (args.length() == 0)
		{
			c.sendln("Syntax: }H'}hecho }H<}iwhat to echo}H>{x");
			return;
		}
		
		for (UserCon cs : conns)
		{
			if (cs.ch.currentRoom == c.ch.currentRoom)
			{
				if (cs.hasPermission("staff") && !c.isDummy)
					cs.sendln("Echo by "+Fmt.seeNameGlobal(cs.ch, c.ch)+": "+args);
				else
					cs.sendln(args);
			}
		}
	}
	
	public static void doEchoat(UserCon c, String args)
	{
		String arg1 = CommandHandler.getArg(args, 1);
		String arg2 = CommandHandler.getLastArg(args, 2);
		
		if (arg1.length() == 0 || arg2.length() == 0)
		{
			c.sendln("Syntax: }H'}hechoat }H<}icharacter name}H> <}iwhat to echo}H>'{x");
			return;
		}

		CharData target = Combat.findChar(c.ch, null, arg1, true);
		if (target == null)
		{
			c.sendln("That character was not found.");
			return;
		}
		
		if (c.ch != target)
			c.sendln("Echoat sent.");
		if (target.conn.hasPermission("staff") && !c.isDummy)
			target.sendln("Echoat by "+Fmt.seeNameGlobal(target, c.ch)+": "+arg2);
		else
			target.sendln(arg2);
	}

	public static void doEchoaround(UserCon c, String args)
	{
		String arg1 = CommandHandler.getArg(args, 1);
		String arg2 = CommandHandler.getLastArg(args, 2);
		
		if (arg1.length() == 0 || arg2.length() == 0)
		{
			c.sendln("Syntax: }H'}hechoaround }H<}icharacter name}H> <}iwhat to echo}H>'{x");
			return;
		}

		CharData target = Combat.findChar(c.ch, null, arg1, true);
		if (target == null)
		{
			c.sendln("That character was not found.");
			return;
		}
		
		if (c.ch == target)
			c.sendln("Echoaround sent.");
		
		for (UserCon cs : conns)
		{
			if (cs.ch.currentRoom == target.currentRoom && cs.ch != target)
			{
				if (cs.hasPermission("staff") && !c.isDummy)
					cs.sendln("Echoaround by "+Fmt.seeNameGlobal(cs.ch, c.ch)+": "+arg2);
				else
					cs.sendln(arg2);
			}
		}
	}
	
	public static void doGecho(UserCon c, String args)
	{
		if (args.length() == 0)
		{
			c.sendln("Syntax: }H'}hgecho }H<}iwhat to echo}H>'{x");
			return;
		}
		
		for (UserCon cs : conns)
		{
			if (cs.hasPermission("staff") && !c.isDummy)
				cs.sendln("Gecho by "+Fmt.seeNameGlobal(cs.ch, c.ch)+": "+args);
			else
				cs.sendln(args);
		}
	}
	
	public static void doAecho(UserCon c, String args)
	{
		if (args.length() == 0)
		{
			c.sendln("Syntax: }H'}haecho }H<}iwhat to echo}H>'{x");
			return;
		}
		
		for (UserCon cs : conns)
		{
			if (cs.ch.currentArea() == c.ch.currentArea())
			{
				if (cs.hasPermission("staff") && !c.isDummy)
					cs.sendln("Aecho by "+Fmt.seeNameGlobal(cs.ch, c.ch)+": "+args);
				else
					cs.sendln(args);
			}
		}
	}
	
	public static void doPeace(UserCon c, String args)
	{
		for (CharData cd : allChars())
		{
			if (cd.currentRoom == c.ch.currentRoom && cd.fighting != null)
			{
				cd.fighting = null;
				cd.attacksAccrued = 0.0;
				cd.combatQueue.clear();
				cd.targetQueue.clear();
				cd.hating.clear();
				cd.damagers.clear();
			}
		}
		Fmt.actRoom(c.ch.currentRoom, null, null, null, "A peaceful wave ensues.");
	}
	
	/**
	Transfer a character/player to the location of a given room ID or name.
	
	@param c The user who entered this command.
	@param args A string containing any arguments to the command.
	*/
	public static void doTransfer(UserCon c, String args)
	{
		String arg1 = CommandHandler.getArg(args, 1);
		String arg2 = CommandHandler.getArg(args, 2);
		
		if (arg1.length() == 0)
		{
			c.sendln("Syntax: }H'}htransfer }H<}hcharacter name}H> <}iroom id}H|}hcharacter name}H>'{x");
			return;
		}
		
		CharData targetChar = Combat.findChar(c.ch, null, arg1, true);
		if (targetChar == null && !arg1.equalsIgnoreCase("all"))
		{
			c.sendln("No character by that name was found.");
			return;
		}
		
		Room targetRoom;
		if (arg2.length() == 0)
			targetRoom = c.ch.currentRoom;
		else
		{
			targetRoom = Room.lookup(Fmt.getInt(arg2));
			if (targetRoom == null)
			{
				CharData destChar = Combat.findChar(c.ch, null, arg2, true);
				if (destChar == null)
				{
					c.sendln("That room ID or character was not found.");
					return;
				}
				targetRoom = destChar.currentRoom;
			}
		}
		
		if (arg1.equalsIgnoreCase("all"))
		{
			for (UserCon cs : conns)
				if (cs != c)
					doTransfer(c, cs.ch.shortName+" "+arg2);
			return;
		}

		if (targetChar.currentRoom == targetRoom)
		{
			c.sendln("They're already there!");
			return;
		}
		
		for (UserCon cs : conns)
			if (cs.ch.currentRoom == targetChar.currentRoom && cs.ch != targetChar)
				cs.sendln(Fmt.cap(Fmt.seeName(cs.ch, targetChar))+" is blown away by a gust of wind.");
		
		targetChar.currentRoom = targetRoom;
		targetChar.sendln(Fmt.cap(Fmt.seeName(targetChar, c.ch))+" has transferred you!^/");
		c.sendln(targetChar.shortName+" has been transferred to "+targetRoom.name+".");
		
		if (targetChar.conn != null)
		{
			RoomCommands.doLook(targetChar.conn, "");
			targetChar.save();
		}
		
		for (UserCon cs : conns)
			if (cs.ch.currentRoom == targetChar.currentRoom && cs.ch != targetChar)
				cs.sendln(Fmt.cap(Fmt.seeName(cs.ch, targetChar))+" falls out of the sky.");

		targetRoom.checkTrigger("greet", c.ch, null, "transfer", 0);
		for (CharData ch : mobs)
			if (ch.currentRoom == targetRoom)
				ch.checkTrigger("greet", c.ch, null, "transfer", 0);
		for (ObjData o : ObjData.allObjects())
			if (o.getCurrentRoom() == targetRoom)
				o.checkTrigger("greet", c.ch, null, "transfer", 0);
	}
	
	/**
	Force the target to perform a command.
	
	@param c The user who entered this command.
	@param args A string containing any arguments to the command.
	*/
	public static void doForce(UserCon c, String args)
	{
		String arg1 = CommandHandler.getArg(args, 1);
		String arg2 = CommandHandler.getLastArg(args, 2);
		String cmd = CommandHandler.getArg(arg2, 1).toLowerCase();
		
		if (arg1.length() == 0 || arg2.length() == 0)
		{
			c.sendln("Syntax: }H'}hforce }H<}icharacter name}H> <}icommand}H>'{x");
			return;
		}
		
		if (arg1.equalsIgnoreCase("all"))
		{
			for (UserCon cs : conns)
				if (cs != c)
					doForce(c, cs.ch.shortName+" "+arg2);
			return;
		}
		
		CharData targetChar = Combat.findChar(c.ch, null, arg1, true);
		if (targetChar == null)
		{
			c.sendln("No character by that name was found.");
			return;
		}
		
		// Find the appropriate command method and run it.
		for (Command cm : commands)
			for (String as : cm.alias)
				if (as.startsWith(cmd))
					if (cm.allowCheck(c))
					{
						if (targetChar.conn != null)
						{
							if (!cm.allowCheck(targetChar.conn))
							{
								c.sendln("Your target does not have access to that command - force failed.");
								return;
							}
							targetChar.sendln(Fmt.cap(Fmt.seeName(targetChar, c.ch))+" has forced you to '"+arg2+"'.^/");
							targetChar.conn.commandQueue.add(arg2);
							targetChar.conn.processCommand();
						}
						else
							targetChar.mobCommand(arg2);
						c.sendln("Command forced.");
						return;
					}
		targetChar.sendln(Fmt.cap(Fmt.seeName(targetChar, c.ch))+" has forced you to '"+arg2+"'.^/");
		targetChar.conn.commandQueue.add(arg2);
		targetChar.conn.processCommand();
		c.sendln("Command forced.");
	}
	
	public static void doInvisible(UserCon c, String args)
	{
		if (!c.invis)
		{
			c.invis = true;
			c.sendln("You are now invisible to players.");
		}
		else
		{
			c.invis = false;
			c.sendln("You are no longer invisible to players.");
		}
	}

	public static void doIncognito(UserCon c, String args)
	{
		if (!c.incog)
		{
			c.incog = true;
			c.sendln("You are now invisible to players outside your room.");
		}
		else
		{
			c.incog = false;
			c.sendln("You are no longer invisible to players outside your room.");
		}
	}
	
	public static void doVisible(UserCon c, String args)
	{
		c.incog = false;
		c.invis = false;
		for (Effect e : c.ch.effects)
			if (e.name.equals("invisible"))
			{
				c.ch.effects.remove(e);
				break;
			}
		
		c.sendln("Any invisibility effects have been removed.");
		return;
	}
	
	public static void doSwitch(UserCon c, String args)
	{
		if (args.length() == 0)
		{
			c.sendln("Syntax: }H'}hswitch }H<}icharacter name}H>'{x");
			return;
		}
		
		CharData targetChar = Combat.findChar(c.ch, null, args, true);
		if (targetChar == null)
		{
			c.sendln("No character by that name was found.");
			return;
		}
		
		if (!targetChar.conn.isDummy)
		{
			c.sendln("You can't switch with players.");
			return;
		}
		
		if (targetChar.conn.realCh != null)
		{
			c.sendln("You can't switch with a switched player.");
			return;
		}
		
		if (c.realCh != null)
			doReturn(c, "");
		
		UserCon tempCon = c;
		CharData tempChar = c.ch;
		
		mobs.add(c.ch);
		mobs.remove(targetChar);

		c.realCh = c.ch;
		targetChar.conn.realCh = targetChar;

		targetChar.conn.ch = c.ch;
		c.ch.conn = targetChar.conn;

		c.ch = targetChar;
		targetChar.conn = c;
		
		c.sendln("You are now switched into "+c.ch.shortName+".");
	}
	
	public static void doReturn(UserCon c, String args)
	{
		if (c.realCh == null)
		{
			c.sendln("You're not switched with anyone.");
			return;
		}
		
		CharData targetChar = c.realCh;

		UserCon tempCon = c;
		CharData tempChar = c.ch;
		
		mobs.add(c.ch);
		mobs.remove(targetChar);

		c.realCh = null;
		targetChar.conn.realCh = null;

		targetChar.conn.ch = c.ch;
		c.ch.conn = targetChar.conn;

		c.ch = targetChar;
		targetChar.conn = c;
		
		c.sendln("You return to your original body.");
	}
	
	public static void doLocate(UserCon c, String args)
	{
		int staffCount = 0;
		int playerCount = 0;
		
		int totalHpPct = 0;
		int totalMnPct = 0;
		int totalStPct = 0;
		
		boolean printedHeading = false;
		for (UserCon cs : conns)
		{
			if (cs.role.length() > 0)
			{
				if (!printedHeading)
				{
					c.sendln(Fmt.heading("Staff Members"));
					c.sendln(Fmt.center("}mName           Invis  Incog   }M[}mRoom #}M] }N"+Fmt.fit("", 20)));
					c.sendln(Fmt.heading(""));
					printedHeading = true;
				}
				String tempInv = " Off ";
				if (cs.invis)
					tempInv = " On  ";
				String tempInc = " Off ";
				if (cs.incog)
					tempInc = " On  ";
				
				c.sendln(Fmt.center("}n"+Fmt.fit(cs.ch.shortName, 15)+tempInv+"  "+tempInc+"   }M[}n"+Fmt.rfit(""+cs.ch.currentRoom.id, 6)+"}M] }N"+Fmt.fit(cs.ch.currentRoom.name, 20)));
				staffCount++;
			}
		}
		
		printedHeading = false;
		for (UserCon cs : conns)
		{
			if (cs.role.length() == 0)
			{
				if (!printedHeading)
				{
					if (staffCount > 0)
						c.sendln("");
					c.sendln(Fmt.heading("Players"));
					c.sendln(Fmt.center("}mName           Lvl Pos  %hp  %mn  %st }M[}mRoom #}M] }N"+Fmt.fit("", 20)));
					c.sendln(Fmt.heading(""));
					printedHeading = true;
				}
				String tempInv = " Off ";
				if (cs.invis)
					tempInv = " On  ";
				String tempInc = " Off ";
				if (cs.incog)
					tempInc = " On  ";
				
				int hpPct = (cs.ch.hp*100)/cs.ch.maxHp();
				int mnPct = (cs.ch.mana*100)/cs.ch.maxMana();
				int stPct = (cs.ch.energy*100)/cs.ch.maxEnergy();

				totalHpPct += hpPct;
				totalMnPct += mnPct;
				totalStPct += stPct;
				
				c.sendln(Fmt.center("}n"+Fmt.fit(cs.ch.shortName, 15)+Fmt.rfit(""+cs.ch.level, 3)+" "+Fmt.cap(cs.ch.position.substring(0, 3))+Fmt.rfit(hpPct+"%", 5)+Fmt.rfit(mnPct+"%", 5)+Fmt.rfit(stPct+"%", 5)+ " }M[}n"+Fmt.rfit(""+cs.ch.currentRoom.id, 6)+"}M] }N"+Fmt.fit(cs.ch.currentRoom.name, 20)));
				playerCount++;
			}
		}
		
		c.sendln(Fmt.heading(""));
		if (playerCount > 0)
		{
			c.sendln(Fmt.center("}mAvg hp}M: }n"+totalHpPct/playerCount+"%   }mAvg mn}M: }n"+totalMnPct/playerCount+"%   }mAvg st}M: }n"+totalStPct/playerCount+"%"));
		}
		if (playerCount == 1 && staffCount == 1)
			c.sendln(Fmt.center("}NThere are 1 player and 1 staff member online.{x"));
		else if (playerCount == 1)
			c.sendln(Fmt.center("}NThere are 1 player and "+staffCount+" staff members online.{x"));
		else if (staffCount == 1)
			c.sendln(Fmt.center("}NThere are "+playerCount+" players and 1 staff member online.{x"));
		else
			c.sendln(Fmt.center("}NThere are "+playerCount+" players and "+staffCount+" staff members online.{x"));
	}
	
	public static void doAward(UserCon c, String args)
	{
		String arg1 = CommandHandler.getArg(args, 1);
		int arg2 = Fmt.getInt(CommandHandler.getArg(args, 2));
		String arg3 = CommandHandler.getArg(args, 3).toLowerCase();
		
		CharData target = Combat.findChar(c.ch, null, arg1, true);
		if (target == null)
		{
			c.sendln("No character by that name was found.");
			return;
		}
		
		if (arg2 <= 0)
		{
			c.sendln("The amount must be greater than zero.");
			return;
		}
		
		if ("gold".startsWith(arg3))
		{
			target.gold += arg2;
			target.save();
			target.sendln(Fmt.seeNameGlobal(target, c.ch)+" has awarded you {W"+arg2+" gold{x!");
			c.sendln(Fmt.seeNameGlobal(c.ch, target)+" has been awarded with "+arg2+" gold.");
			return;
		}
		
		if ("experience".startsWith(arg3))
		{
			target.sendln(Fmt.seeNameGlobal(target, c.ch)+" has awarded you with {W"+arg2+" experience points{x!");
			target.awardExp(arg2);
			c.sendln(Fmt.seeNameGlobal(c.ch, target)+" has been awarded with "+arg2+" experience points.");
			return;
		}
		
		c.sendln("'"+arg3+"' is not a valid award type.");
	}
	
	public static void doExtract(UserCon c, String args)
	{
		String arg1 = CommandHandler.getArg(args, 1);
		String arg2 = CommandHandler.getArg(args, 2);
		String arg3 = CommandHandler.getArg(args, 3).toLowerCase();
		
		CharData target = Combat.findChar(c.ch, null, arg1, true);
		if (target == null)
		{
			c.sendln("That character was not found.");
			return;
		}
		
		ObjData targetObjs[] = Combat.findObj(c.ch, ObjData.allCarriedObjects(c.ch), arg2);
		if (targetObjs.length == 0)
		{
			c.sendln("The target doesn't have any matching objects.");
			return;
		}
		
		if ("delete".startsWith(arg3))
		{
			for (ObjData o : targetObjs)
			{
				c.sendln(Fmt.seeName(c.ch, o)+" has been deleted from "+Fmt.seeName(c.ch, target)+".");
				target.sendln(Fmt.seeName(target, c.ch)+" has deleted your "+Fmt.seeName(target, o));
				o.clearObjects();
			}
		}
		else
		{
			for (ObjData o : targetObjs)
			{
				c.sendln(Fmt.seeName(c.ch, o)+" has been extracted from "+Fmt.seeName(c.ch, target)+".");
				target.sendln(Fmt.seeName(target, c.ch)+" has extracted your "+Fmt.seeName(target, o));
				o.toChar(c.ch);
				c.ch.save();
				target.save();
			}
		}
	}
	
	public static void doSet(UserCon c, String args)
	{
		String arg1 = CommandHandler.getArg(args, 1).toLowerCase();
		String arg2 = CommandHandler.getArg(args, 2).toLowerCase();
		String arg3 = CommandHandler.getArg(args, 3).toLowerCase();
		String arg4 = CommandHandler.getLastArg(args, 4);
		
		if (arg1.length() == 0)
		{
			c.sendln("Set what? (character/object)");
			return;
		}
		if ("character".startsWith(arg1))
		{
			if (arg2.length() == 0)
			{
				c.sendln("Set the values of what character?");
				return;
			}
			
			CharData target = Combat.findChar(c.ch, null, arg2, true);
			if (target == null)
			{
				c.sendln("No character by that name was found.");
				return;
			}
			
			if (arg3.length() == 0)
			{
				c.sendln("Set what value on that character?");
				return;
			}
			
			// Values that can take an empty string argument below here.
			if ("short".startsWith(arg3))
			{
				if (target.conn != null)
				{
					c.sendln("To rename players, use the 'rename' command.");
					return;
				}
				target.shortName = arg4;
				c.sendln("Character short name set.");
				target.save();
				return;
			}
			if ("long".startsWith(arg3) || "title".startsWith(arg3))
			{
				target.longName = arg4;
				target.save();
				if (target.conn != null)
				{
					c.sendln("Character title set.");
					target.sendln(Fmt.cap(Fmt.seeName(target, c.ch))+" has set your title to "+arg3+".");
				}	
				else
				{
					c.sendln("Character long name set.");
				}
				return;
			}
			
			// Values that take a string argument below here.
			if (arg4.length() == 0)
			{
				c.sendln("Set that value to what?");
				return;
			}
			if ("name".startsWith(arg3))
			{
				if (target.conn != null)
				{
					c.sendln("To rename players, use the 'rename' command.");
					return;
				}
				target.name = arg4;
				target.save();
				c.sendln("Character keywords set.");
				return;
			}
			if ("sex".startsWith(arg3))
			{
				arg4 = arg4.toLowerCase();
				if ("male".startsWith(arg4))
					target.sex = "m";
				else if ("female".startsWith(arg4))
					target.sex = "f";
				else
				{
					c.sendln("That's not a valid sex. (male/female)");
					return;
				}
				target.save();
				c.sendln("Character sex set.");
				if (target.sex.equals("m"))
					target.sendln(Fmt.cap(Fmt.seeName(target, c.ch))+" has set your sex to 'male'.");
				else
					target.sendln(Fmt.cap(Fmt.seeName(target, c.ch))+" has set your sex to 'female'.");
				return;
			}
			if ("race".startsWith(arg3))
			{
				Race tempRace = Race.lookup(arg4);
				if (tempRace != null)
				{
					target.charRace = tempRace;
					target.save();
					c.sendln("Character race set.");
					target.sendln(Fmt.cap(Fmt.seeName(target, c.ch))+" has set your race to '"+target.charRace.name+"'.");
					return;
				}
				c.sendln("That's not a valid race ID or name.^/"+
						"Try }H'}hrcedit list}H'{x to view all races.");
				return;
			}

			if ("class".startsWith(arg3))
			{
				CharClass tempClass = CharClass.lookup(arg4);
				if (tempClass != null)
				{
					target.charClass = tempClass;
					target.save();
					c.sendln("Character class set.");
					target.sendln(Fmt.cap(Fmt.seeName(target, c.ch))+" has set your class to '"+target.charClass.name+"'.");
					return;
				}
				c.sendln("That's not a valid class ID or name.^/"+
						"Try }H'}hmedit }H<}iid}H> }hclass}H'{x to view all classes.");
				return;
			}
			
			// Values that require an integer below here.
			int intArg = Fmt.getInt(arg4);
			if (intArg == 0 && !arg4.equals("0"))
			{
				c.sendln("That's not a valid integer value.");
				return;
			}
			if ("align".startsWith(arg3))
			{
				target.align = intArg;
				c.sendln("Character align set.");
				target.sendln(Fmt.cap(Fmt.seeName(target, c.ch))+" has set your align to '"+intArg+"'.");
				target.save();
				return;
			}		
			if ("gold".startsWith(arg3))
			{
				target.gold = intArg;
				c.sendln("Character gold set.");
				target.sendln(Fmt.cap(Fmt.seeName(target, c.ch))+" has set your gold to '"+intArg+"'.");
				target.save();
				return;
			}
			if ("bank".startsWith(arg3))
			{
				target.bank = intArg;
				c.sendln("Character bank gold set.");
				target.sendln(Fmt.cap(Fmt.seeName(target, c.ch))+" has set your banked gold to '"+intArg+"'.");
				target.save();
				return;
			}
			if ("hp".startsWith(arg3))
			{
				target.baseMaxHp = intArg;
				c.sendln("Character base hp set.");
				target.sendln(Fmt.cap(Fmt.seeName(target, c.ch))+" has set your base hp to '"+intArg+"'.");
				target.save();
				return;
			}
			if ("mana".startsWith(arg3))
			{
				target.baseMaxMana = intArg;
				c.sendln("Character base mana set.");
				target.sendln(Fmt.cap(Fmt.seeName(target, c.ch))+" has set your base mana to '"+intArg+"'.");
				target.save();
				return;
			}
			if ("energy".startsWith(arg3))
			{
				target.baseMaxEnergy = intArg;
				c.sendln("Character base energy set.");
				target.sendln(Fmt.cap(Fmt.seeName(target, c.ch))+" has set your base energy to '"+intArg+"'.");
				target.save();
				return;
			}
			if ("str".startsWith(arg3))
			{
				target.baseStr = intArg;
				c.sendln("Character base strength set.");
				target.sendln(Fmt.cap(Fmt.seeName(target, c.ch))+" has set your base strength to '"+intArg+"'.");
				target.save();
				return;
			}
			if ("dex".startsWith(arg3))
			{
				target.baseDex = intArg;
				c.sendln("Character base dexterity set.");
				target.sendln(Fmt.cap(Fmt.seeName(target, c.ch))+" has set your base dexterity to '"+intArg+"'.");
				target.save();
				return;
			}
			if ("con".startsWith(arg3))
			{
				target.baseCon = intArg;
				c.sendln("Character base constitution set.");
				target.sendln(Fmt.cap(Fmt.seeName(target, c.ch))+" has set your base constitution to '"+intArg+"'.");
				target.save();
				return;
			}
			if ("int".startsWith(arg3))
			{
				target.baseInt = intArg;
				c.sendln("Character base intelligence set.");
				target.sendln(Fmt.cap(Fmt.seeName(target, c.ch))+" has set your base intelligence to '"+intArg+"'.");
				target.save();
				return;
			}
			if ("cha".startsWith(arg3))
			{
				target.baseCha = intArg;
				c.sendln("Character base charisma set.");
				target.sendln(Fmt.cap(Fmt.seeName(target, c.ch))+" has set your base charisma to '"+intArg+"'.");
				target.save();
				return;
			}
			if ("slash".startsWith(arg3))
			{
				target.baseArmSlash = intArg;
				c.sendln("Character base slash armor set.");
				target.sendln(Fmt.cap(Fmt.seeName(target, c.ch))+" has set your base slash armor to '"+intArg+"'.");
				target.save();
				return;
			}
			if ("bash".startsWith(arg3))
			{
				target.baseArmBash = intArg;
				c.sendln("Character base bash armor set.");
				target.sendln(Fmt.cap(Fmt.seeName(target, c.ch))+" has set your base bash armor to '"+intArg+"'.");
				target.save();
				return;
			}
			if ("pierce".startsWith(arg3))
			{
				target.baseArmPierce = intArg;
				c.sendln("Character base pierce armor set.");
				target.sendln(Fmt.cap(Fmt.seeName(target, c.ch))+" has set your base pierce armor to '"+intArg+"'.");
				target.save();
				return;
			}
			if ("frost".startsWith(arg3))
			{
				target.baseResFrost = intArg;
				c.sendln("Character base frost resistance set.");
				target.sendln(Fmt.cap(Fmt.seeName(target, c.ch))+" has set your base frost resistance to '"+intArg+"'.");
				target.save();
				return;
			}
			if ("fire".startsWith(arg3))
			{
				target.baseResFire = intArg;
				c.sendln("Character base fire resistance set.");
				target.sendln(Fmt.cap(Fmt.seeName(target, c.ch))+" has set your base fire resistance to '"+intArg+"'.");
				target.save();
				return;
			}
			if ("lightning".startsWith(arg3))
			{
				target.baseResLightning = intArg;
				c.sendln("Character base lightning resistance set.");
				target.sendln(Fmt.cap(Fmt.seeName(target, c.ch))+" has set your base lightning resistance to '"+intArg+"'.");
				target.save();
				return;
			}
			if ("acid".startsWith(arg3))
			{
				target.baseResAcid = intArg;
				c.sendln("Character base acid resistance set.");
				target.sendln(Fmt.cap(Fmt.seeName(target, c.ch))+" has set your base acid resistance to '"+intArg+"'.");
				target.save();
				return;
			}
			if ("good".startsWith(arg3))
			{
				target.baseResGood = intArg;
				c.sendln("Character base good resistance set.");
				target.sendln(Fmt.cap(Fmt.seeName(target, c.ch))+" has set your base good resistance to '"+intArg+"'.");
				target.save();
				return;
			}
			if ("evil".startsWith(arg3))
			{
				target.baseResEvil = intArg;
				c.sendln("Character base evil resistance set.");
				target.sendln(Fmt.cap(Fmt.seeName(target, c.ch))+" has set your base evil resistance to '"+intArg+"'.");
				target.save();
				return;
			}
			c.sendln("That's not a valid settable field.");
			return;
		}
		else if ("object".startsWith(arg1))
		{
			if (arg2.length() == 0)
			{
				c.sendln("Set the values of what object?");
				return;
			}
			
			ObjData target[] = Combat.findObj(c.ch, ObjData.allVisibleObjects(c.ch), arg2);
			if (target.length == 0)
			{
				c.sendln("There's nothing here that matches that description.");
				return;
			}

			if (arg3.length() == 0)
			{
				c.sendln("Set what value on that object?");
				return;
			}

			// Values that can take an empty string argument below here.
			if ("short".startsWith(arg3))
			{
				target[0].shortName = arg4;
				c.sendln("Object short name set.");
				return;
			}
			if ("long".startsWith(arg3) || "title".startsWith(arg3))
			{
				target[0].longName = arg4;
				c.sendln("Object long name set.");
				return;
			}
			
			// Values that take a string argument below here.
			if (arg4.length() == 0)
			{
				c.sendln("Set that value to what?");
				return;
			}
			if ("name".startsWith(arg3))
			{
				target[0].name = arg4;
				c.sendln("Object keywords set.");
				return;
			}
			if ("type".startsWith(arg3))
			{
				for (String t : Flags.objTypes.keySet())
				{
					if (t.equals(arg3))
					{
						target[0].type = t;
						c.sendln("Object type set.");
						return;
					}
				}
				c.sendln("That's not a valid object type.");
				return;
			}
			if ("decay".startsWith(arg3))
			{
				target[0].decay = Fmt.getInt(arg4);
				c.sendln("Object decay timer set.");
				return;
			}
			if ("level".startsWith(arg3))
			{
				int tempInt = Fmt.getInt(arg4);
				if (tempInt < Flags.minLevel || tempInt > Flags.maxLevel)
				{
					c.sendln("That's not a valid level.");
					return;
				}
				target[0].level = tempInt;
				c.sendln("Object level set.");
				return;
			}
			if ("cost".startsWith(arg3))
			{
				int tempInt = Fmt.getInt(arg4);
				if (tempInt < 0)
				{
					c.sendln("That's not a valid cost.");
					return;
				}
				target[0].cost = tempInt;
				c.sendln("Object cost set.");
				return;
			}
			if ("material".startsWith(arg3))
			{
				target[0].material = arg4;
				c.sendln("Object material set.");
				return;
			}
			
			if ("addoprog".startsWith(arg3))
			{
				arg4 = CommandHandler.getArg(args, 4);
				String arg5 = CommandHandler.getArg(args, 5);
				String arg6 = CommandHandler.getArg(args, 5);
				String arg7 = CommandHandler.getLastArg(args, 6);
				
				ObjProg targetOprog = ObjProg.lookup(Fmt.getInt(arg4));
				if (targetOprog == null)
				{
					c.sendln("That oprog ID was not found.");
					return;
				}
				
				for (String s : Flags.oprogTriggerTypes)
					if (s.equalsIgnoreCase(arg5))
					{
						Trigger newTrigger = new Trigger();
						newTrigger.type = s;
						newTrigger.arg = arg7;
						newTrigger.numArg = Fmt.getInt(arg6);
						newTrigger.oprog = targetOprog;
						target[0].triggers.add(newTrigger);
						c.sendln("Oprog '"+targetOprog.name+"' added to this object.");
						return;
					}
				
				String temp = "";
				for (String s : Flags.oprogTriggerTypes)
					temp = temp+s+" ";
				c.sendln("That's not a valid oprog trigger.^/Valid triggers are: "+temp.trim());
				return;
			}
			
			if ("deloprog".startsWith(arg3))
			{
				int targetNr = Fmt.getInt(arg4);
				if (targetNr < 1 || targetNr > target[0].triggers.size())
				{
					if (ObjProg.lookup(targetNr) != null)
					{
						for (Trigger t : target[0].triggers)
						{
							if (t.oprog == ObjProg.lookup(targetNr))
							{
								target[0].triggers.remove(t);
								c.sendln("Oprog trigger deleted.");
								return;
							}
						}
						c.sendln("That object doesn't have a trigger for that prog.");
						return;
					}
					c.sendln("There aren't that many oprog triggers on this object prototype.");
					return;
				}
				target[0].triggers.remove(targetNr-1);
				c.sendln("Oprog trigger deleted.");
				return;
			}

			if ("flags".startsWith(arg3))
			{
				if (arg4.length() == 0)
				{
					c.sendln("Set what flags? See 'help objflags' for a list.");
					return;
				}
				String[] flags = arg4.split(" ");
				for (String s : flags)
				{
					Boolean flagFound = false;
					s = s.trim().toLowerCase();
					if (s.length() == 0)
						continue;
					for (String fs : Flags.objFlags)
					{
						if (fs.startsWith(s))
						{
							if (target[0].flags.get(fs))
							{
								target[0].flags.put(fs, false);
								c.sendln("'"+fs+"' removed.");
							}
							else
							{
								target[0].flags.put(fs, true);
								c.sendln("'"+fs+"' set.");
							}
							flagFound = true;
							break;
						}
					}
					if (!flagFound)
					{
						c.sendln("'"+s+"' is not a valid object flag.");
						continue;
					}
				}
				return;
			}
	
			c.sendln("That's not a valid settable field.");
			return;
		}
		c.sendln("You can't set that. Enter 'character' or 'object' as the first argument.");
		return;
	}
	
	public static void doGset(UserCon c, String args)
	{
		String arg1 = CommandHandler.getArg(args, 1).toLowerCase();
		String arg2 = CommandHandler.getLastArg(args, 2);

		if (arg1.length() == 0)
		{
			c.sendln("Global variables:");
			for (String s : globals.keySet())
				c.sendln(Fmt.fit(s, 25)+" : "+globals.get(s));
			return;
		}
		
		globals.remove(arg1);

		if (arg2.length() > 0)
		{
			arg2 = Script.evalMath(arg2);
			globals.put(arg1, arg2);
			c.sendln("Global variable '"+arg1+"' set to: "+arg2);
		}
		else
		{
			c.sendln("Global variable '"+arg1+"' cleared.");
		}

		Database.saveGlobals();
	}
	
	public static void doCset(UserCon c, String args)
	{
		String arg1 = CommandHandler.getArg(args, 1);
		String arg2 = CommandHandler.getArg(args, 2).toLowerCase();
		String arg3 = CommandHandler.getLastArg(args, 3);

		CharData target = Combat.findChar(c.ch, null, arg1, true);
		if (target == null)
		{
			c.sendln("That character was not found.");
			return;
		}
		
		if (arg2.length() == 0)
		{
			if (target.variables.size() == 0)
			{
				c.sendln(Fmt.seeNameGlobal(c.ch, target)+" has no variables set.");
				return;
			}
			c.sendln("Variables on "+Fmt.seeNameGlobal(c.ch, target)+":");
			for (String s : target.variables.keySet())
				c.sendln(Fmt.fit(s, 25)+" : "+target.variables.get(s));
			return;
		}
		
		target.variables.remove(arg2);
		if (arg3.trim().length() > 0)
		{
			arg3 = Script.evalMath(arg3);
			target.variables.put(arg2, arg3);
			c.sendln(Fmt.seeNameGlobal(c.ch, target)+"'s '"+arg2+"' variable set to: "+arg3);
		}
		else
		{
			c.sendln(Fmt.seeNameGlobal(c.ch, target)+"'s '"+arg2+"' variable cleared.");
		}
		
		target.save();
	}

	public static void doOset(UserCon c, String args)
	{
		String arg1 = CommandHandler.getArg(args, 1);
		String arg2 = CommandHandler.getArg(args, 2).toLowerCase();
		String arg3 = CommandHandler.getLastArg(args, 3);

		ObjData[] targets = Combat.findObj(c.ch, ObjData.allObjects(), arg1);
		if (targets.length == 0)
		{
			c.sendln("That object was not found.");
			return;
		}
		
		ObjData target = targets[0];
		
		if (arg2.length() == 0)
		{
			if (target.variables.size() == 0)
			{
				c.sendln(Fmt.seeName(c.ch, target)+" has no variables set.");
				return;
			}
			c.sendln("Variables on "+Fmt.seeName(c.ch, target)+":");
			for (String s : target.variables.keySet())
				c.sendln(Fmt.fit(s, 25)+" : "+target.variables.get(s));
			return;
		}
		
		target.variables.remove(arg2);
		if (arg3.trim().length() > 0)
		{
			arg3 = Script.evalMath(arg3);
			target.variables.put(arg2, arg3);
			c.sendln(Fmt.seeName(c.ch, target)+"'s '"+arg2+"' variable set to: "+arg3);
		}
		else
		{
			c.sendln(Fmt.seeName(c.ch, target)+"'s '"+arg2+"' variable cleared.");
		}
	}
	
	public static void doAdvance(UserCon c, String args)
	{
		String arg1 = CommandHandler.getArg(args, 1);
		String arg2 = CommandHandler.getArg(args, 2);
		
		if (arg1.length() == 0)
		{
			c.sendln("Advance who to what level?");
			return;
		}
		
		CharData target = Combat.findChar(c.ch, null, arg1, true);
		if (target == null)
		{
			c.sendln("No character by that name was found.");
			return;
		}
		
		if (arg2.length() == 0)
		{
			c.sendln("Advance that character to what level?");
			return;
		}
		
		int newLvl = Fmt.getInt(arg2);
		if (newLvl < 0 || newLvl > Flags.maxLevel)
		{
			c.sendln("That level is outside of the valid level range (0 - "+Flags.maxLevel+")");
			return;
		}
		if (!target.conn.isDummy && newLvl > Flags.maxPlayableLevel)
		{
			c.sendln("That level is too high for players.");
			return;
		}
		if (newLvl == target.level)
		{
			c.sendln("Your target is already at that level.");
			return;
		}
		if (newLvl > target.level)
		{
			while (target.level < newLvl)
				target.awardExp(target.tnl);
			c.sendln(Fmt.cap(target.shortName)+" advanced to level "+newLvl+".");
			if (target != c.ch)
				target.sendln(Fmt.cap(Fmt.seeNameGlobal(target, c.ch))+" has advanced you to level "+newLvl+".");
			return;
		}
		else
		{
			target.initializeValues();
			while (target.level < newLvl)
				target.awardExp(target.tnl);
			c.sendln(Fmt.cap(target.shortName)+" reduced to level "+newLvl+".");
			if (target != c.ch)
				target.sendln(Fmt.cap(Fmt.seeNameGlobal(target, c.ch))+" has reduced your level to "+newLvl+".");
			return;
		}
	}
	
	public static void doRestore(UserCon c, String args)
	{
		if (args.length() == 0)
		{
			c.sendln("Restore who?");
			return;
		}
		
		if (args.equalsIgnoreCase("all"))
		{
			for (UserCon cs : conns)
			{
				cs.ch.restore();
				cs.sendln("{W"+Fmt.cap(Fmt.seeNameGlobal(cs.ch, c.ch))+" has restored you.{x");
			}
			c.sendln("{WAll players restored.{x");
			return;
		}
		
		CharData target = Combat.findChar(c.ch, null, args, true);
		if (target == null)
		{
			c.sendln("No character by that name was found.");
			return;
		}
		
		target.restore();
		if (target != c.ch)
			target.sendln("{W"+Fmt.cap(Fmt.seeNameGlobal(target, c.ch))+" has restored you.{x");
		c.sendln("{W"+Fmt.cap(Fmt.seeNameGlobal(c.ch, target))+" restored.{x");
	}
	
	public static void doOload(UserCon c, String args)
	{
		String arg1 = CommandHandler.getArg(args, 1);
		String arg2 = CommandHandler.getLastArg(args, 2).toLowerCase();
		int targetId = Fmt.getInt(arg1);
		ObjProto op = ObjProto.lookup(targetId);
		
		if (op == null)
		{
			c.sendln("That's not a valid object prototype ID.");
			return;
		}
		
		ObjData newObj = new ObjData(op);
		
		if (arg2.length() == 0)
		{
			c.sendln("You create "+newObj.shortName+". It's now in your inventory.");
			newObj.toChar(c.ch);
		}
		else if ("room".equalsIgnoreCase(arg2))
		{
			c.sendln("You create "+newObj.shortName+". It's now in the room.");
			newObj.toRoom(c.ch.currentRoom);
		}
		else if (Combat.findChar(c.ch, null, arg2, true) != null)
		{
			CharData target = Combat.findChar(c.ch, null, arg2, true);
			
			c.sendln("You create "+newObj.shortName+". It's now in "+target.shortName+"'s inventory.");
			newObj.toChar(target);
		}
		else
		{
			c.sendln("That character could not be found.");
			return;
		}

		newObj.checkTrigger("load", null, null, "", 0);
	}
	
	public static void doLload(UserCon c, String args)
	{
		String arg1 = CommandHandler.getArg(args, 1);
		String arg2 = CommandHandler.getLastArg(args, 2).toLowerCase();
		int targetId = Fmt.getInt(arg1);
		Lootgroup lg = Lootgroup.lookup(targetId);
		
		if (lg == null)
		{
			c.sendln("That's not a valid lootgroup ID.");
			return;
		}
		
		ObjData newObj = lg.getObject();
		
		if (arg2.length() == 0)
		{
			c.sendln("You create "+newObj.shortName+". It's now in your inventory.");
			newObj.toChar(c.ch);
		}
		else if ("room".equalsIgnoreCase(arg2))
		{
			c.sendln("You create "+newObj.shortName+". It's now in the room.");
			newObj.toRoom(c.ch.currentRoom);
		}
		else if (Combat.findChar(c.ch, null, arg2, true) != null)
		{
			CharData target = Combat.findChar(c.ch, null, arg2, true);
			
			c.sendln("You create "+newObj.shortName+". It's now in "+target.shortName+"'s inventory.");
			newObj.toChar(target);
		}
		else
		{
			c.sendln("That character could not be found.");
			return;
		}

		newObj.checkTrigger("load", null, null, "", 0);
	}

	public static void doMload(UserCon c, String args)
	{
		String arg1 = CommandHandler.getArg(args, 1);
		int targetId = Fmt.getInt(arg1);
		CharProto cp = CharProto.lookup(targetId);
		
		if (cp == null)
		{
			c.sendln("That's not a valid mob prototype ID.");
			return;
		}
		
		CharData newChar = new CharData(cp);
		newChar.currentRoom = c.ch.currentRoom;
		mobs.add(newChar);
		newChar.checkTrigger("load", null, null, "", 0);
		c.sendln("You create "+cp.shortName+". It's now in your room.");
	}

	public static void doPurge(UserCon c, String args)
	{
		if (args.length() == 0)
		{
			for (int ctr = c.ch.currentRoom.objects.size()-1; ctr >= 0; ctr--)
				c.ch.currentRoom.objects.remove(ctr);
			
			ArrayList<CharData> chars = new ArrayList<CharData>();
			for (CharData ch : mobs)
				chars.add(ch);
			for (CharData ch : chars)
				if (ch.currentRoom == c.ch.currentRoom)
				{
					mobs.remove(ch);
					ch.conn.cleanup();
				}
			c.sendln("The contents of this room have been purged.");
			return;
		}

		CharData targetChar = Combat.findChar(c.ch, null, args, false);
		if (targetChar != null)
		{
			if (!targetChar.conn.isDummy)
			{
				c.sendln("You can't purge players.");
				return;
			}
			mobs.remove(targetChar);
			targetChar.conn.cleanup();
			c.sendln("Target ("+targetChar.shortName+") purged.");
			return;
		}

		ObjData target[] = Combat.findObj(c.ch, ObjData.allVisibleObjects(c.ch), args);
		if (target.length == 0)
		{
			c.sendln("There's nothing here that matches that description.");
			return;
		}
		target[0].clearObjects();
		c.sendln("Target ("+target[0].shortName+") purged.");
		return;
	}
	
	/**
	Set system information - commands, aliases, permissions, etc.
	
	@param c The user who entered this command.
	@param args A string containing any arguments to the command.
	*/
	public static void doConfigure(UserCon c, String args)
	{
		String arg1 = CommandHandler.getArg(args, 1);
		String arg2 = CommandHandler.getArg(args, 2);
		String arg3 = CommandHandler.getArg(args, 3);
		
		if (arg1.length() == 0)
		{
			InfoCommands.doHelp(c, "configure");
			return;
		}
		
		if ("alias".startsWith(arg1))
		{
			if (arg2.length() == 0)
			{
				InfoCommands.doHelp(c, "configure alias");
				return;
			}
			
			// "configure alias <command>" - show existing aliases for that command.
			if (arg3.length() == 0)
			{
				for (Command cmd : commands)
				{
					if (cmd.fullName.equals(arg2))
					{
						if (cmd.alias.size() == 1)
							c.sendln("There are no aliases for '"+arg2+"'.");
						else
						{
							c.send("Current aliases for '"+arg2+"':");
							for (String als : cmd.alias)
								if (!als.equals(cmd.fullName))
									c.send(" "+als);
							c.sendln("");
						}
						return;
					}
				}
				c.sendln("Command not found: '"+arg2+"'.");
				return;
			}
			// "configure alias <command> <alias>" - add an alias for a command.
			else
			{
				boolean aliasFound = false;
				// Check for an existing alias.
				for (Command cmd : commands)
				{
					for (int ctr = 0; ctr < cmd.alias.size(); ctr++)
					{
						if (cmd.alias.get(ctr).equals(arg3) && !cmd.alias.get(ctr).equals(cmd.fullName))
						{
							c.sendln("Alias '"+arg3+"' removed from '"+cmd.fullName+"'.");
							cmd.alias.remove(ctr);
							Database.saveCommand(cmd);
							if (cmd.fullName.equals(arg2))
							{
								Collections.sort(commands);
								return;
							}
							else
							{
								aliasFound = true;
								break;
							}
						}
					}
					if (aliasFound)
						break;
				}
				for (Command cmd : commands)
				{
					if (cmd.fullName.equals(arg2))
					{
						cmd.alias.add(arg3);
						Collections.sort(cmd.alias);
						Database.saveCommand(cmd);
						c.sendln("Alias '"+arg3+"' added to '"+arg2+"'.");
						Collections.sort(commands);
						return;
					}
				}
				c.sendln("Command not found: '"+arg2+"'.");
				return;
			}
		}

		if ("permissions".startsWith(arg1))
		{
			if (arg2.length() == 0)
			{
				InfoCommands.doHelp(c, "configure permissions");
				return;
			}
			
			// "configure permissions <command>" - show existing permission requirements for that command.
			if (arg3.length() == 0)
			{
				for (Command cmd : commands)
				{
					if (cmd.fullName.equals(arg2))
					{
						if (cmd.permissions.size() == 0)
							c.sendln("'"+arg2+"' is currently available to all users. (No permissions set.)");
						else
						{
							c.send("'"+arg2+"' is available to:");
							for (String p : cmd.permissions)
								c.send(" "+p);
							c.sendln("");
						}
						return;
					}
				}
				c.sendln("Command not found: '"+arg2+"'.");
				return;
			}
			// "configure permissions <command> <permission>" - toggle a permission entry for a command.
			else
			{
				boolean validPermission = false;
				for (String p : Flags.permissionTypes)
				{
					if (p.equals(arg3))
						validPermission = true;
				}
				if (!validPermission)
				{
					c.sendln("'"+arg3+"' is not a valid permission setting.");
					c.send("Valid permissions are:");
					for (String p : Flags.permissionTypes)
						c.send(" "+p);
					c.sendln("");
					return;
				}
				
				for (Command cmd : commands)
				{
					if (cmd.fullName.equals(arg2))
					{
						for (int ctr = 0; ctr < cmd.permissions.size(); ctr++)
						{
							if (cmd.permissions.get(ctr).equals(arg3))
							{
								cmd.permissions.remove(ctr);
								Database.saveCommand(cmd);
								c.sendln("Access to '"+cmd.fullName+"' removed for '"+arg3+"'.");
								if (cmd.permissions.size() == 0)
									c.sendln("{R'"+cmd.fullName+"' is now available to all users.{x");
								else
								{
									c.send("'"+arg2+"' is now available to:");
									for (String p : cmd.permissions)
										c.send(" "+p);
									c.sendln("");
								}
								return;
							}
						}
						cmd.permissions.add(arg3);
						Database.saveCommand(cmd);
						c.send("'"+arg2+"' is now available to:");
						for (String p : cmd.permissions)
							c.send(" "+p);
						c.sendln("");
						return;
					}
				}
				c.sendln("Command not found: '"+arg2+"'.");
				return;
			}
		}
		
		if ("log".startsWith(arg1))
		{
			if (arg2.length() == 0)
			{
				String temp = "";
				for (Command cmd : commands)
					if (cmd.log)
						temp = temp+" "+cmd.fullName;
				if (temp.length() == 0)
					temp = " No commands are being logged.";
				c.sendln("Logged commands:"+temp);
				return;
			}
			
			for (Command cmd : commands)
				if (cmd.fullName.equalsIgnoreCase(arg2))
				{
					if (cmd.log)
					{
						cmd.log = false;
						c.sendln("The "+cmd.fullName+" command is no longer being logged.");
						Database.saveCommand(cmd);
						return;
					}
					else
					{
						cmd.log = true;
						c.sendln("The "+cmd.fullName+" command is now being logged.");
						Database.saveCommand(cmd);
						return;
					}
				}
			c.sendln("Command not found: '"+arg2+"'.");
			return;
		}
		InfoCommands.doHelp(c, "configure");
	}
	
	public static void doLogall(UserCon c, String args)
	{
		if (logAll)
		{
			c.sendln("All commands no longer being logged.");
			logAll = false;
		}
		else
		{
			c.sendln("All commands will now be logged.");
			logAll = true;
		}
	}
	
	public static void doStafflog(UserCon c, String args)
	{
		if (args.length() == 0)
		{
			for (String s : Flags.staffLogs)
				if (c.staffLogs.contains(s))
					c.sendln("{x"+Fmt.rfit(Fmt.cap(s), 12)+": {Gon{x");
				else
					c.sendln("{x"+Fmt.rfit(Fmt.cap(s), 12)+": {Roff{x");
			return;
		}
		
		for (String s : Flags.staffLogs)
			if (s.startsWith(args.toLowerCase()))
			{
				if (c.staffLogs.contains(s))
				{
					c.staffLogs.remove(s);
					c.sendln("You will no longer see the "+s+" log.");
					Database.saveAccount(c);
					return;
				}
				else
				{
					c.staffLogs.add(s);
					c.sendln("You will now see the "+s+" log.");
					Database.saveAccount(c);
					return;
				}
			}
		
		c.sendln("That's not a valid staff log name. Try 'stafflog' to see a list.");
		return;
	}
	
	/**
	Show system updates which are scheduled to run.
	
	@param c The user who entered this command.
	@param args A string containing any arguments to the command.
	*/
	public static void doUpdates(UserCon c, String args)
	{
		c.sendln("Showing scheduled updates:^/Current game cycle: "+updateCycles+"^/");
		for (Update u : updates)
			c.sendln("}m  At }n"+u.runAt+"}M: }m"+u.type+" #}n"+u.target+"{x");
		
		if (updates.size() == 0)
			c.sendln("  There are no updates scheduled.");
	}
	
	/**
	Shut down the game after saving all players, accounts, and loaded mobs.
	
	@param c The user who entered this command.
	@param args A string containing any arguments to the command.
	*/
	public static void doShutdown(UserCon c, String args)
	{
		sysLog("system", "Shutdown initiated by "+c.ch.shortName+".");
		for (UserCon cs : conns)
			cs.sendln("MUD shutting down for maintenance. Please check back soon.");
		
		sysLog("system", "Cleaning updates...");
		for (Update u : updates)
			if (u.runAt >= updateCycles)
				u.run();
			
		sysLog("system", "Saving players...");
		for (UserCon cs : conns)
		{
			if (cs.realCh != null)
				StaffCommands.doReturn(cs, "");
			
			cs.ch.save();
			Database.saveAccount(cs);
		}
		
		CharData.saveLoadedMobs();
		sysLog("system", "Saving loaded mobs... "+mobs.size()+" mobs saved.");
		
		Database.saveRoomObjects();
		sysLog("system", "Saving room objects...");
		
		sysLog("system", "Terminating program.");
		Runtime.getRuntime().halt(0);
	}
}