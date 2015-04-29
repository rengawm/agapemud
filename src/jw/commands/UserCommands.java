package jw.commands;
import java.util.*;

import jw.core.*;
import jw.data.*;
import static jw.core.MudMain.*;

/**
	The UserCommands class contains all user-related commands, including preferences.
*/
public class UserCommands
{
	/**
	Change a user's password after going through several prompts.
	
	@param c The user who entered this command.
	@param args A string containing any arguments to the command.
	*/
	public static void doPassword(UserCon c, String args)
	{
		c.prompt("Password1", "", "Enter your current password:");
	}
	/**
	Receive the user's current password, check it, and ask for a new one.
	
	@param c The user who entered this command.
	@param oldPass A string containing the old password.
	*/
	public static void prPassword1(UserCon c, String oldPass)
	{
		if (!Database.checkLogin(c.ch.name, oldPass))
		{
			c.sendln("Invalid password.");
			c.clearPrompt();
			return;
		}
		c.prompt("Password2", oldPass, "Enter a new password:");
	}
	/**
	Receive the first new password and ask for a confirmation.
	
	@param c The user who entered this command.
	@param newPass1 The first entry of the new password.
	*/
	public static void prPassword2(UserCon c, String newPass1)
	{
		String oldPass = c.promptTarget;
		if (oldPass.equals(newPass1))
		{
			c.sendln("That's already your password.");
			c.clearPrompt();
			return;
		}
		c.prompt("Password3", newPass1, "Repeat the new password:");
	}
	/**
	Receive the second new password and set the user's password.
	
	@param c The user who entered this command.
	@param newPass2 The second entry of the new password.
	*/
	public static void prPassword3(UserCon c, String newPass2)
	{
		String newPass1 = c.promptTarget;
		c.clearPrompt();
		if (!newPass1.equals(newPass2))
		{
			c.sendln("New passwords don't match. Please try again.");
			return;
		}
		if (newPass1.length() < 5)
		{
			c.sendln("Passwords must be at least 5 characters long.");
			return;
		}
		Database.setPassword(c, newPass1);
		c.sendln("Password changed.");
	}
	
	/**
	Set user-controlled options and display preferences.
	
	@param c The user who entered this command.
	@param args A string containing any arguments to the command.
	*/
	public static void doPreference(UserCon c, String args)
	{
		String arg1 = CommandHandler.getArg(args, 1).toLowerCase();
		String arg2 = CommandHandler.getArg(args, 2);
		String arg3 = CommandHandler.getArg(args, 3);
		
		if (args.length() == 0)
		{
			ArrayList<String> prefs = new ArrayList<String>();
			for (String s : Flags.userPrefs)
				if (c.prefs.get(s))
					prefs.add(Fmt.fit(Fmt.cap(s), 15)+"}M: {Gon");
				else
					prefs.add(Fmt.fit(Fmt.cap(s), 15)+"}M: {Roff");
			
			c.sendln(Fmt.heading("Your Preferences and Custom Settings"));
			c.sendln("}m      Real Name}M: }n"+c.realname);
			c.sendln("}m E-mail Address}M: }n"+c.email);
			c.sendln("}m    Page Length}M: }n"+c.pageLength+"{x");
			c.sendln("}mTime Adjustment}M: }n"+c.timeAdj+"{x");
			c.sendln(Fmt.heading(""));
			c.sendln(Fmt.defaultTextColumns(prefs.toArray(new String[0])));
			return;
		}

		if ("nofollow".startsWith(arg1))
		{
			if (c.prefs.get("nofollow"))
			{
				c.prefs.put("nofollow", false);
				c.sendln("Other players will now be able to follow you.");
			}
			else
			{
				c.prefs.put("nofollow", true);
				c.sendln("Other players can no longer follow you.");
				for (CharData ch : allChars())
					if (ch.following == c.ch)
					{
						ch.sendln("You stop following "+Fmt.seeName(ch, c.ch)+".");
						c.sendln(Fmt.cap(Fmt.seeName(c.ch, ch))+" stops following you.");
					}
			}
			Database.saveAccount(c);
			return;
		}

		if ("noloot".startsWith(arg1))
		{
			if (c.prefs.get("noloot"))
			{
				c.prefs.put("noloot", false);
				c.sendln("Other players will now be able to loot or retrieve your pile.");
			}
			else
			{
				c.prefs.put("noloot", true);
				c.sendln("Other players can no longer loot or retrieve your pile.");
			}
			Database.saveAccount(c);
			return;
		}

		if ("autoloot".startsWith(arg1))
		{
			if (c.prefs.get("autoloot"))
			{
				c.prefs.put("autoloot", false);
				c.sendln("You will no longer automatically loot items from mobs you have killed.");
			}
			else
			{
				c.prefs.put("autoloot", true);
				c.sendln("You will now automatically loot items from mobs you have killed.");
			}
			Database.saveAccount(c);
			return;
		}

		if ("autoassist".startsWith(arg1))
		{
			if (c.prefs.get("autoassist"))
			{
				c.prefs.put("autoassist", false);
				c.sendln("You will no longer automatically assist group members in combat.");
			}
			else
			{
				c.prefs.put("autoassist", true);
				c.sendln("You will now automatically assist group members in combat.");
			}
			Database.saveAccount(c);
			return;
		}
		
		if ("brief".startsWith(arg1))
		{
			if (c.prefs.get("brief"))
			{
				c.prefs.put("brief", false);
				c.sendln("You will now see descriptions when moving around.");
			}
			else
			{
				c.prefs.put("brief", true);
				c.sendln("You will no longer see room descriptions after moving.");
			}
			Database.saveAccount(c);
			return;
		}

		if ("compact".startsWith(arg1))
		{
			if (c.prefs.get("compact"))
			{
				c.prefs.put("compact", false);
				c.sendln("Compact prompt mode off.");
			}
			else
			{
				c.prefs.put("compact", true);
				c.sendln("Compact prompt mode on.");
			}
			Database.saveAccount(c);
			return;
		}

		if ("witness".startsWith(arg1))
		{
			if (c.prefs.get("witness"))
			{
				c.prefs.put("witness", false);
				c.sendln("You can no longer communicate with troubled players.");
			}
			else
			{
				c.prefs.put("witness", true);
				c.sendln("You can now communicate with troubled players.");
			}
			Database.saveAccount(c);
			return;
		}

		if ("prompt".startsWith(arg1))
		{
			if (c.prefs.get("prompt"))
			{
				c.prefs.put("prompt", false);
				c.sendln("Your prompt has been turned off.");
			}
			else
			{
				c.prefs.put("prompt", true);
				c.sendln("Your prompt has been turned on.");
			}
			Database.saveAccount(c);
			return;
		}

		if ("ansi".startsWith(arg1))
		{
			if (c.prefs.get("ansi"))
			{
				c.prefs.put("ansi", false);
				c.sendln("{xANSI colors off.");
			}
			else
			{
				c.prefs.put("ansi", true);
				c.sendln("{RA{YN{CS{GI {xcolors on.");
			}
			Database.saveAccount(c);
			return;
		}

		if ("showemail".startsWith(arg1))
		{
			if (c.prefs.get("showemail"))
			{
				c.prefs.put("showemail", false);
				c.sendln("Your e-mail address is now hidden.");
			}
			else
			{
				c.prefs.put("showemail", true);
				c.sendln("Your e-mail address will now be displayed.");
			}
			Database.saveAccount(c);
			return;
		}

		if ("savetells".startsWith(arg1))
		{
			if (c.prefs.get("savetells"))
			{
				c.prefs.put("savetells", false);
				c.sendln("Your received tells will no longer be saved.");
			}
			else
			{
				c.prefs.put("savetells", true);
				c.sendln("All tells you receive will now be saved in your buffer.");
			}
			Database.saveAccount(c);
			return;
		}
		
		if ("pvp".startsWith(arg1))
		{
			if (c.prefs.get("pvp"))
			{
				boolean effected = false;
				for (Effect e : c.ch.effects)
				{
					if(e.name.equals("pvp lock"))
					{
						c.sendln("You cannot do that right now.");
						effected = true;
					}
				}
				if(!effected)
				{
					c.prefs.put("pvp", false);
					c.sendln("You will no longer be flagged for PVP.");
				}
			}
			else
			{
				c.prefs.put("pvp", true);
				c.sendln("You will now be flagged for PVP");
			}
			Database.saveAccount(c);
			return;
		}

		if ("channels".startsWith(arg1))
		{
			if (arg2.length() == 0)
			{
				c.sendln("Syntax: }H'}hpreference channel }H<}hchannel name}H>'{x");
				return;
			}
			arg2 = arg2.toLowerCase();
			String tc = "";
			if ("fellowship".startsWith(arg2))
				tc = "fellowship";
			else if ("qa".startsWith(arg2))
				tc = "qa";
			else if ("discussion".startsWith(arg2))
				tc = "discussion";
			else if ("quote".startsWith(arg2))
				tc = "quote";
			else if ("grats".startsWith(arg2))
				tc = "grats";
			else if ("ministry".startsWith(arg2))
				tc = "ministry";
			else if ("stafftalk".startsWith(arg2))
				tc = "stafftalk";
			else if ("gemote".startsWith(arg2))
				tc = "gemote";
			else if ("gocial".startsWith(arg2))
				tc = "gocial";
			else if ("vulgar".startsWith(arg2))
				tc = "vulgar";
			else
			{
				c.sendln("That's not a valid channel name.");
				c.sendln("Channels: fellowship qa discussion quote grats ministry stafftalk gemote gocial vulgar");
				return;
			}
			if (c.channelOff(tc))
			{
				for (int ctr = 0; ctr < c.chansOff.size(); ctr++)
					if (c.chansOff.get(ctr).equals(tc))
					{
						c.chansOff.remove(ctr);
						break;
					}
				c.sendln("'"+tc+"' channel turned on.");
			}
			else
			{
				c.chansOff.add(tc);
				c.sendln("'"+tc+"' channel turned off.");
			}
			Database.saveAccount(c);
			return;
		}
		
		if ("color".startsWith(arg1))
		{
			if (arg2.length() == 0)
			{
				c.sendln("Syntax: }H'{hpreference color }H<}hcolor type}H> <}hcolor name}H>'{x");
				return;
			}
			String tc = "";
			String cc = "";
			if (arg2.equalsIgnoreCase("auction")) tc = "{a";
			else if (arg2.equalsIgnoreCase("auction_text")) tc = "{A";
			else if (arg2.equalsIgnoreCase("gtell")) tc = "{e";
			else if (arg2.equalsIgnoreCase("gtell_text")) tc = "{E";
			else if (arg2.equalsIgnoreCase("fellow")) tc = "{f";
			else if (arg2.equalsIgnoreCase("fellow_text")) tc = "{F";
			else if (arg2.equalsIgnoreCase("quote")) tc = "{h";
			else if (arg2.equalsIgnoreCase("quote_text")) tc = "{H";
			else if (arg2.equalsIgnoreCase("stafftalk")) tc = "{i";
			else if (arg2.equalsIgnoreCase("stafftalk_text")) tc = "{I";
			else if (arg2.equalsIgnoreCase("gocial")) tc = "{j";
			else if (arg2.equalsIgnoreCase("gocial_text")) tc = "{J";
			else if (arg2.equalsIgnoreCase("yell")) tc = "{k";
			else if (arg2.equalsIgnoreCase("yell_text")) tc = "{K";
			else if (arg2.equalsIgnoreCase("log")) tc = "{l";
			else if (arg2.equalsIgnoreCase("log_text")) tc = "{L";
			else if (arg2.equalsIgnoreCase("admin")) tc = "{n";
			else if (arg2.equalsIgnoreCase("admin_text")) tc = "{N";
			else if (arg2.equalsIgnoreCase("discuss")) tc = "{o";
			else if (arg2.equalsIgnoreCase("discuss_text")) tc = "{O";
			else if (arg2.equalsIgnoreCase("ministry")) tc = "{p";
			else if (arg2.equalsIgnoreCase("ministry_text")) tc = "{P";
			else if (arg2.equalsIgnoreCase("qa")) tc = "{q";
			else if (arg2.equalsIgnoreCase("qa_text")) tc = "{Q";
			else if (arg2.equalsIgnoreCase("say")) tc = "{s";
			else if (arg2.equalsIgnoreCase("say_text")) tc = "{S";
			else if (arg2.equalsIgnoreCase("tell")) tc = "{t";
			else if (arg2.equalsIgnoreCase("tell_text")) tc = "{T";
			else if (arg2.equalsIgnoreCase("clan")) tc = "{u";
			else if (arg2.equalsIgnoreCase("clan_text")) tc = "{U";
			else if (arg2.equalsIgnoreCase("vulgar")) tc = "{v";
			else if (arg2.equalsIgnoreCase("vulgar_text")) tc = "{V";
			else if (arg2.equalsIgnoreCase("grats")) tc = "{z";
			else if (arg2.equalsIgnoreCase("grats_text")) tc = "{Z";
			else if (arg2.equalsIgnoreCase("help_text")) tc = "}h";
			else if (arg2.equalsIgnoreCase("help_pun")) tc = "}H";
			else if (arg2.equalsIgnoreCase("help_num")) tc = "}i";
			else if (arg2.equalsIgnoreCase("help_oth")) tc = "}I";
			else if (arg2.equalsIgnoreCase("menu_text")) tc = "}m";
			else if (arg2.equalsIgnoreCase("menu_pun")) tc = "}M";
			else if (arg2.equalsIgnoreCase("menu_num")) tc = "}n";
			else if (arg2.equalsIgnoreCase("menu_oth")) tc = "}N";
			else if (arg2.equalsIgnoreCase("room_title")) tc = "}s";
			else if (arg2.equalsIgnoreCase("room_text")) tc = "}S";
			else if (arg2.equalsIgnoreCase("room_exits")) tc = "}t";
			else if (arg2.equalsIgnoreCase("room_things")) tc = "}T";
			else if (arg2.equalsIgnoreCase("fight_death")) tc = "{1";
			else if (arg2.equalsIgnoreCase("fight_yhit")) tc = "{2";
			else if (arg2.equalsIgnoreCase("fight_ohit")) tc = "{3";
			else if (arg2.equalsIgnoreCase("fight_thit")) tc = "{4";
			else if (arg2.equalsIgnoreCase("fight_skill")) tc = "{5";
			else
			{
				c.sendln("That's not a valid color type. Try }H'}hhelp custom colors}H' {xfor a list of options.");
				return;
			}
			
			if (arg3.equalsIgnoreCase("red")) cc = "{r";
			else if (arg3.equalsIgnoreCase("hi-red")) cc = "{R";
			else if (arg3.equalsIgnoreCase("green")) cc = "{g";
			else if (arg3.equalsIgnoreCase("hi-green")) cc = "{G";
			else if (arg3.equalsIgnoreCase("blue")) cc = "{b";
			else if (arg3.equalsIgnoreCase("hi-blue")) cc = "{B";
			else if (arg3.equalsIgnoreCase("yellow")) cc = "{y";
			else if (arg3.equalsIgnoreCase("hi-yellow")) cc = "{Y";
			else if (arg3.equalsIgnoreCase("magenta")) cc = "{m";
			else if (arg3.equalsIgnoreCase("hi-magenta")) cc = "{M";
			else if (arg3.equalsIgnoreCase("cyan")) cc = "{c";
			else if (arg3.equalsIgnoreCase("hi-cyan")) cc = "{C";
			else if (arg3.equalsIgnoreCase("white")) cc = "{w";
			else if (arg3.equalsIgnoreCase("hi-white")) cc = "{W";
			else if (arg3.equalsIgnoreCase("dark")) cc = "{d";
			else if (arg3.equalsIgnoreCase("hi-dark")) cc = "{D";
			else
			{
				c.sendln("That's not a valid color name. Try }H'}hhelp colors}H' {xfor a list of options.");
				return;
			}
			
			for (int ctr = 0; ctr < c.colors.size(); ctr++)
				if (c.colors.get(ctr).startsWith(tc))
				{
					c.colors.remove(ctr);
					break;
				}
			c.colors.add(tc+"="+cc);
			c.sendln("Color for '"+arg2.toLowerCase()+"' set to '"+cc+arg3.toLowerCase()+"{x.");
			Database.saveAccount(c);
			return;
		}
		
		if ("email".startsWith(arg1))
		{
			c.email = arg2.trim();
			if (c.email.length() > 0)
				c.sendln("Your e-mail address is now set to '"+c.email+"'.");
			else
				c.sendln("Your e-mail address is now blank.");
			Database.saveAccount(c);
			return;
		}
		
		if ("pagelength".startsWith(arg1))
		{
			int newPageLength = Fmt.getInt(arg2);
			if (newPageLength == 0)
			{
				c.sendln("You must enter the page length as a number greater than 0.");
				return;
			}
			if (newPageLength < 10)
			{
				c.sendln("The page length must be at least 10 lines.");
				return;
			}

			c.pageLength = newPageLength;
			c.sendln("You will now see page breaks after "+c.pageLength+" lines.");
			Database.saveAccount(c);
			return;
		}
		
		if ("time".startsWith(arg1))
		{
			arg2 = arg2.replace("+", "");
			int newTimeAdj = Fmt.getInt(arg2);
			if (newTimeAdj < -6 || newTimeAdj > 20 || (newTimeAdj == 0 && !arg2.equals("0")))
			{
				c.sendln("That's not a valid time zone adjustment. The adjustment can be from -6 to +20.");
				return;
			}
			
			c.timeAdj = newTimeAdj;
			c.sendln("Time zone adjustment set.");
			Database.saveAccount(c);
			return;
		}
		
		if ("realname".startsWith(arg1))
		{
			c.realname = arg2.trim();
			if (c.realname.length() > 0)
				c.sendln("Your real name is now set to "+c.realname+".");
			else
				c.sendln("Your real name address is now blank.");
			Database.saveAccount(c);
			return;
		}

		InfoCommands.doHelp(c, "preference");
	}
	
	public static void doBeep(UserCon c, String args)
	{
		if (args.length() == 0)
		{
			if (c.beeps.size() == 0)
			{
				c.sendln("You haven't set any beep triggers.");
				return;
			}
			c.sendln("The following strings will cause a beep:");
			int ctr = 0;
			for (String s : c.beeps)
			{
				ctr++;
				c.send("}m"+Fmt.rfit(""+ctr, 2)+"}M: }N");
				c.sendPlain(s+"\n\r");
			}
			return;
		}
		
		String arg1 = CommandHandler.getArg(args, 1).toLowerCase();
		String arg2 = CommandHandler.getLastArg(args, 2);
		
		if ("delete".startsWith(arg1) || "remove".startsWith(arg1))
		{
			int temp = Fmt.getInt(arg2);
			if (temp < 1 || temp > c.beeps.size())
			{
				c.sendln("That's not a valid beep trigger number. Use 'beep' to list your triggers.");
				return;
			}
			c.send("Beep trigger removed: ");
			c.sendPlain(c.beeps.get(temp-1)+"\n\r");
			c.beeps.remove(temp-1);
			Database.saveAccount(c);
			return;
		}
		
		if ("add".startsWith(arg1))
		{
			c.beeps.add(arg2);
			c.send("Beep trigger added: ");
			c.sendPlain(arg2+"\n\r");
			Database.saveAccount(c);
			return;
		}
		
		InfoCommands.doHelp(c, "beep");
	}
	
	/**
	Set custom aliases for the user.
	
	@param c The user who entered this command.
	@param args A string containing any arguments to the command.
	*/
	public static void doAlias(UserCon c, String args)
	{
		String alias = CommandHandler.getArg(args, 1).toLowerCase();
		String command = CommandHandler.getLastArg(args, 2);
		
		if (args.length() == 0)
		{
			c.sendln("Your current aliases:");
			if (c.aliases.size() == 0)
				c.sendln("  You have no aliases set.");
			for (int ctr = 0; ctr < c.aliases.size(); ctr += 2)
				c.sendln("  "+c.aliases.get(ctr)+"}M:{x "+c.aliases.get(ctr+1));
			return;
		}
		if (alias.length() == 0)
		{
			InfoCommands.doHelp(c, "alias");
			return;
		}
		if (command.length() == 0)
		{
			for (int ctr = 0; ctr < c.aliases.size(); ctr += 2)
				if (c.aliases.get(ctr).equals(alias))
				{
					c.aliases.remove(ctr+1);
					c.aliases.remove(ctr);
					c.sendln("Alias '"+alias+"' removed.");
					Database.saveAccount(c);
					return;
				}
			InfoCommands.doHelp(c, "alias");
			return;
		}
		if (command.toLowerCase().startsWith(alias+" "))
		{
			c.sendln("You cannot create an alias which points to itself.");
			return;
		}
		c.aliases.add(alias);
		c.aliases.add(command);
		c.sendln("'"+alias+"' added as an alias to '"+command+"'.");
		Database.saveAccount(c);
	}
	
	public static void doIgnore(UserCon c, String args)
	{
		if (args.length() == 0)
		{
			String temp = "";
			for (String s : c.ignoring)
				temp = temp+" "+Fmt.cap(s);
			if (c.ignoring.size() == 0)
				temp = " nobody";
			c.sendln("You're currently ignoring:"+temp);
			return;
		}
		
		if (c.ignoring.contains(args.toLowerCase()))
		{
			c.ignoring.remove(args.toLowerCase());
			c.sendln("You are no longer ignoring "+Fmt.cap(args.toLowerCase())+".");
			Database.saveAccount(c);
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
			c.sendln("You can't ignore mobs.");
			return;
		}
		if (target.conn.hasPermission("staff"))
		{
			c.sendln("You can't ignore staff members.");
			return;
		}
		if (target == c.ch)
		{
			c.sendln("You can't ignore yourself!");
			return;
		}
		
		c.ignoring.add(target.shortName.toLowerCase());
		c.sendln("You are now ignoring "+Fmt.seeNameGlobal(c.ch, target)+".");
		Database.saveAccount(c);
	}
	
	/**
	Set the user's title.
	
	@param c The user who entered this command.
	@param args A string containing any arguments to the command.
	*/
	public static void doTitle(UserCon c, String args)
	{
		if (args.length() == 0)
		{
			c.ch.longName = "";
			c.sendln("Your title has been cleared.");
		}
		else
		{
			if (args.indexOf("^/") > -1)
			{
				c.sendln("Your title cannot contain the newline symbol. (^^/)");
				return;
			}
			if (args.length() >= 100)
			{
				c.sendln("That title is too long. Please keep your title under 100 characters long.");
				return;
			}
			c.ch.longName = args;
			c.sendln("Your title has been set to '"+args+"{x'.");
		}
		c.ch.save();
	}

	/**
	Set the user's prompt.
	
	@param c The user who entered this command.
	@param args A string containing any arguments to the command.
	*/
	public static void doPrompt(UserCon c, String args)
	{
		if (args.length() == 0 || "show".startsWith(args.toLowerCase()) || "display".startsWith(args.toLowerCase()))
		{
			c.sendln("Your current prompt is:{x");
			c.sendln(c.prompt);
			c.sendln("{xWith color codes:");
			c.sendPlain(c.prompt+"{x\n\r");
		}
		else
		{
			c.prompt = args;
			c.sendln("Prompt set.");
		}
		Database.saveAccount(c);
	}

	/**
	Set the user's battle prompt.
	
	@param c The user who entered this command.
	@param args A string containing any arguments to the command.
	*/
	public static void doBprompt(UserCon c, String args)
	{
		if (args.length() == 0 || "show".startsWith(args.toLowerCase()) || "display".startsWith(args.toLowerCase()))
		{
			c.sendln("Your current battle prompt is:{x");
			c.sendln(c.bprompt);
			c.sendln("{xWith color codes:");
			c.sendPlain(c.bprompt+"{x\n\r");
		}
		else
		{
			c.bprompt = args;
			c.sendln("Battle prompt set.");
		}
		Database.saveAccount(c);
	}
	
	public static void doDescription(UserCon c, String args)
	{
		c.sendln("Editing your character's description:");
		c.editMode("UserDescription", "", c.ch.description);
		return;
	}
	public static void prUserDescription(UserCon c, String finishedText)
	{
		c.ch.description = finishedText;
		c.sendln("Your description has been set.");
		c.clearEditMode();
	}
	
	public static void doTrain(UserCon c, String args)
	{
		if (args.length() == 0)
		{
			if (c.ch.trains == 1)
				c.sendln("You have 1 training point to use.");
			else
				c.sendln("You have "+c.ch.trains+" training points to use.");
			c.sendln("Syntax: }H'}htrain str}H'");
			c.sendln("        }H'}htrain dex}H'");
			c.sendln("        }H'}htrain con}H'");
			c.sendln("        }H'}htrain int}H'");
			c.sendln("        }H'}htrain cha}H'{x");
			return;
		}
		if (c.ch.trains == 0)
		{
			c.sendln("You don't have any training points.");
			return;
		}

		args = args.toLowerCase();
		if ("strength".startsWith(args))
		{
			if (c.ch.baseStr >= c.ch.charRace.baseStr+10)
			{
				c.sendln("You can't increase an attribute more than 10 points above your race's base stats.");
				return;
			}
			c.sendln("{5Your {Wstrength {5has increased.{x");
			c.ch.baseStr++;
			c.ch.trains--;
			c.ch.save();
			return;
		}
		if ("dexterity".startsWith(args))
		{
			if (c.ch.baseDex >= c.ch.charRace.baseDex+10)
			{
				c.sendln("You can't increase an attribute more than 10 points above your race's base stats.");
				return;
			}
			c.sendln("{5Your {Wdexterity {5has increased.{x");
			c.ch.baseDex++;
			c.ch.trains--;
			c.ch.save();
			return;
		}
		if ("constitution".startsWith(args))
		{
			if (c.ch.baseCon >= c.ch.charRace.baseCon+10)
			{
				c.sendln("You can't increase an attribute more than 10 points above your race's base stats.");
				return;
			}
			c.sendln("{5Your {Wconstitution {5has increased.{x");
			c.ch.baseCon++;
			c.ch.trains--;
			c.ch.save();
			return;
		}
		if ("intelligence".startsWith(args))
		{
			if (c.ch.baseInt >= c.ch.charRace.baseInt+10)
			{
				c.sendln("You can't increase an attribute more than 10 points above your race's base stats.");
				return;
			}
			c.sendln("{5Your {Wintelligence {5has increased.{x");
			c.ch.baseInt++;
			c.ch.trains--;
			c.ch.save();
			return;
		}
		if ("charisma".startsWith(args))
		{
			if (c.ch.baseCha >= c.ch.charRace.baseCha+10)
			{
				c.sendln("You can't increase an attribute more than 10 points above your race's base stats.");
				return;
			}
			c.sendln("{5Your {Wcharisma {5has increased.{x");
			c.ch.baseCha++;
			c.ch.trains--;
			c.ch.save();
			return;
		}
		c.sendln("That's not a valid attribute name.");
		c.sendln("Syntax: }H'}htrain str}H'");
		c.sendln("        }H'}htrain dex}H'");
		c.sendln("        }H'}htrain con}H'");
		c.sendln("        }H'}htrain int}H'");
		c.sendln("        }H'}htrain cha}H'{x");
	}

	public static void doReclass(UserCon c, String args)
	{
		if (c.ch.level < Flags.maxPlayableLevel)
		{
			c.sendln("You can't reclass until you've reached level "+Flags.maxPlayableLevel+".");
			return;
		}
		
		ArrayList<CharClass> valid = new ArrayList<CharClass>();
		for (CharClass cc : classes)
			if (cc.parent == c.ch.charClass.id)
				valid.add(cc);
		
		if (valid.size() == 0)
		{
			c.sendln("You've already reached a top-level class and can reclass no further.");
			return;
		}
		
		if (args.length() == 0)
		{
			ArrayList<String> cst = new ArrayList<String>();
			String classPrompt = "^/The following classes are available to you:^/^/";
			for (CharClass cc : valid)
				cst.add(cc.name);
			Collections.sort(cst);
			classPrompt = classPrompt+Fmt.defaultTextColumns(cst.toArray(new String[0]))+"^/^/See }H'}hhelp }H<}hclass}H>'{x for more information.";
			c.sendln(classPrompt);
			c.sendln("Syntax: }H'}hreclass }H<}ipassword}H> <}ifull class name}H>'{x");
			return;
		}
		
		String arg1 = CommandHandler.getArg(args, 1);
		String arg2 = CommandHandler.getLastArg(args, 2);
		
		if (!Database.checkLogin(c.ch.name, arg1))
		{
			c.sendln("That's not your password.");
			c.sendln("Syntax: }H'}hreclass }H<}ipassword}H> <}ifull class name}H>'{x");
			return;
		}
		
		for (CharClass cc : valid)
			if (cc.name.equalsIgnoreCase(arg2))
			{
				c.ch.charClass = cc;
				c.ch.initializeValues();
				c.sendln("You have reclassed to a {W"+cc.name+"{x!");
				c.sendln("Your character has been reset to level 1. New and powerful abilities await!");
				sysLog("system", "Reclass: "+c.ch.shortName+" -> "+cc.name);
				return;
			}
			else if (cc.name.toLowerCase().startsWith(arg2.toLowerCase()))
			{
				c.sendln("You must enter the full name of the desired class (using quotes if necessary).");
				c.sendln("Syntax: }H'}hreclass }H<}ipassword}H> <}ifull class name}H>'{x");
				return;
			}
		
		c.sendln("You can't reclass to that class. Type 'reclass' to see your options.");
		c.sendln("Syntax: }H'}hreclass }H<}ipassword}H> <}ifull class name}H>'{x");
	}
	
	public static void doAfk(UserCon c, String args)
	{
		if (c.afk)
		{
			c.afk = false;
			c.sendln("You are no longer AFK. Use 'replay' to view missed tells.");
			return;
		}
		c.afk = true;
		c.sendln("You are now AFK. Any tells sent to you will be saved for you to 'replay'.");
		return;
	}

	/**
	Quit the game after saving a user and the character.
	
	@param c The user who entered this command.
	@param args A string containing any arguments to the command.
	*/
	public static void doQuit(UserCon c, String args)
	{
		for (CharData ch : allChars())
			if (ch.fighting == c.ch || c.ch.fighting != null)
			{
				c.sendln("You can't quit while you're in combat.");
				return;
			}
		
		for (UserCon cs : conns)
			if (cs != c && Combat.canSeeGlobal(cs.ch, c.ch))
				cs.sendln(Fmt.cap(Fmt.seeName(cs.ch, c.ch))+" has left the game.");

		c.sendln("{YThanks for playing!{x\n\n\r");
		sysLog("connections", "Closing socket normally: "+c.ch.shortName);
		c.closeSocket();
		c.lastInput = 3000;
		c.lastOutput = 3000;
	}
	
	public static void doDelete(UserCon c, String args)
	{
		if (!Database.checkLogin(c.ch.name, args))
		{
			c.sendln("That's not your password.");
			c.sendln("Syntax: }H'}hdelete }H<}ipassword}H>'{x");
			return;
		}
		c.sendln("{RWARNING: {wThis command will {RPERMANENTLY {wdelete your character and equipment.");
		c.sendln("Your character cannot be restored after deletion.");
		c.prompt("Delete", "", "If you really want to delete your character, enter your password again.^/Enter anything else to abort the deletion process.");
	}
	public static void prDelete(UserCon c, String args)
	{
		if (!Database.checkLogin(c.ch.name, args))
		{
			c.sendln("Invalid password. Deletion aborted.");
			c.clearPrompt();
			return;
		}
		c.saveable = false;
		Database.deleteAccount(c);
		c.sendln("Your character has been deleted.");
		sysLog("connections", "Character deleted: "+c.ch.shortName);
		c.closeSocket();
		c.lastInput = 3000;
		c.lastOutput = 3000;
		return;
	}
}