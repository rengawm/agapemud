package jw.commands;
import java.util.*;

import jw.core.*;
import static jw.core.MudMain.*;

/**
	The ChatCommands class contains commands which are primarily used for communicating
	with other players. All channels should be in this class.
*/
public class ChatCommands
{
	/**
	Show a list of users who are currently online.
	
	@param c The user who entered this command.
	@param args A string containing any arguments to the command.
	*/
	public static void doWho(UserCon c, String args)
	{
		int playerCount = 0;
		int volunteerCount = 0;
		int staffCount = 0;
		Collections.sort(conns);
		for (UserCon uc : conns)
		{
			if (!Combat.canSeeGlobal(c.ch, uc.ch))
				continue;
			if (uc.role.length() > 0 || uc.hasPermission("staff") || uc.hasPermission("builder"))
				continue;
			if (playerCount == 0)
				c.sendln(Fmt.heading("Players"));
			String tempRace = Fmt.cap(Fmt.fit(uc.ch.charRace.name.split(" ")[0], 8).substring(0, 6));
			
			String tempClass = Fmt.cap(Fmt.fit(uc.ch.charClass.name, 5).substring(0, 3));
			if (uc.ch.charClass.name.equals("Dark Priest"))
				tempClass = "DPr";
			else if (uc.ch.charClass.name.equals("Dark Knight"))
				tempClass = "DKn";
			else if (uc.ch.charClass.name.equals("Master Thief"))
				tempClass = "MTh";
			else if (uc.ch.charClass.name.equals("Privateer"))
				tempClass = "Prv";
				
			String tempWho = String.format("}n%3d %-6s %-3s", uc.ch.level, tempRace, tempClass);
			tempWho = tempWho+" }M| }N";
			String nameTitle = uc.ch.shortName;
			if (!uc.ch.longName.startsWith(".") && !uc.ch.longName.startsWith(","))
				nameTitle = nameTitle+" ";
			nameTitle = nameTitle+uc.ch.longName;
			nameTitle = Fmt.getWhoFlags(uc, c)+nameTitle;
			ArrayList<String> tempName = new ArrayList<String>();
			tempName.add(nameTitle);
			tempName = Fmt.formatString(tempName, 58, false);
			c.sendln(tempWho+tempName.get(0));
			for (int ctr = 1; ctr < tempName.size(); ctr++)
				c.sendln(Fmt.fit("", 17)+tempName.get(ctr));
			playerCount++;
		}

		for (UserCon uc : conns)
		{
			if (!Combat.canSeeGlobal(c.ch, uc.ch))
				continue;
			if (!uc.hasPermission("builder") || uc.hasPermission("staff"))
				continue;
			if (volunteerCount == 0)
				c.sendln(Fmt.heading("Volunteers"));
			String tempWho = Fmt.center(uc.role, 15)+"}M| }N";
			String nameTitle = uc.ch.shortName;
			if (!uc.ch.longName.startsWith(".") && !uc.ch.longName.startsWith(","))
				nameTitle = nameTitle+" ";
			nameTitle = nameTitle+uc.ch.longName;
			nameTitle = Fmt.getWhoFlags(uc, c)+nameTitle;
			ArrayList<String> tempName = new ArrayList<String>();
			tempName.add(nameTitle);
			tempName = Fmt.formatString(tempName, 58, false);
			c.sendln(tempWho+tempName.get(0));
			for (int ctr = 1; ctr < tempName.size(); ctr++)
				c.sendln(Fmt.fit("", 17)+tempName.get(ctr));
			volunteerCount++;
		}

		for (UserCon uc : conns)
		{
			if (!Combat.canSeeGlobal(c.ch, uc.ch))
				continue;
			if (!uc.hasPermission("staff"))
				continue;
			if (staffCount == 0)
				c.sendln(Fmt.heading("Staff Members"));
			String tempWho = Fmt.center(uc.role, 15)+"}M| }N";
			String nameTitle = uc.ch.shortName;
			if (!uc.ch.longName.startsWith(".") && !uc.ch.longName.startsWith(","))
				nameTitle = nameTitle+" ";
			nameTitle = nameTitle+uc.ch.longName;
			nameTitle = Fmt.getWhoFlags(uc, c)+nameTitle;
			ArrayList<String> tempName = new ArrayList<String>();
			tempName.add(nameTitle);
			tempName = Fmt.formatString(tempName, 58, false);
			c.sendln(tempWho+tempName.get(0));
			for (int ctr = 1; ctr < tempName.size(); ctr++)
				c.sendln(Fmt.fit("", 17)+tempName.get(ctr));
			staffCount++;
		}

		c.sendln(Fmt.heading(""));
		String playerText = playerCount+" players";
		if (playerCount == 1)
			playerText = "1 player";
		String volunteerText = volunteerCount+" volunteers";
		if (volunteerCount == 1)
			volunteerText = "1 volunteer";
		String staffText = staffCount+" staff members";
		if (staffCount == 1)
			staffText = "1 staff member";

		c.sendln("}NThere are "+playerText+", "+volunteerText+", and "+staffText+" online. ("+(playerCount+volunteerCount+staffCount)+" total){x");
	}
	
	/**
	Broadcast a message from a user using the fellowship channel.
	
	@param c The user who entered this command.
	@param args A string containing any arguments to the command.
	*/
	public static void doFellowship(UserCon c, String args)
	{
		if (c.spamCheck())
			return;
		if (c.quiet)
		{
			c.sendln("You're in quiet mode. Turn quiet mode off first.");
			return;
		}
		if (c.troubled)
		{
			c.sendln("You can't use that channel when you're troubled.");
			return;
		}
		
		if (c.channelOff("fellowship"))
		{
			c.sendln("You have turned the fellowship channel off.");
			c.sendln("To turn it on, use }H'}hpreference channel fellowship}H'{w.");
			return;
		}
		if (args.length() == 0)
		{
			c.sendln("Fellowship what?");
			return;
		}

		String chatText = pullSocial(args)[0];
		String socialText = pullSocial(args)[1];

		if (socialText.length() > 0)
			doGocial(c, socialText);
		if (chatText.length() > 0)
		{
			c.sendln("{fYou fellowship '{F"+chatText+"{f'{x");
			for (UserCon cs : conns)
				if (cs != c && !cs.channelOff("fellowship") && !cs.ignoring.contains(c.ch.shortName.toLowerCase()) && !cs.quiet)
					cs.sendln("{f"+Fmt.seeNameGlobal(cs.ch, c.ch)+" fellowships '{F"+chatText+"{f'{x");
		}
	}

	/**
	Broadcast a message from a user using the Q/A channel.
	
	@param c The user who entered this command.
	@param args A string containing any arguments to the command.
	*/
	public static void doQa(UserCon c, String args)
	{
		if (c.spamCheck())
			return;
		if (c.quiet)
		{
			c.sendln("You're in quiet mode. Turn quiet mode off first.");
			return;
		}
		if (c.troubled)
		{
			c.sendln("You can't use that channel when you're troubled.");
			return;
		}
		
		if (c.channelOff("qa"))
		{
			c.sendln("You have turned the Q/A channel off.");
			c.sendln("To turn it on, use }H'}hpreference channel qa}H'{w.");
			return;
		}
		if (args.length() == 0)
		{
			c.sendln("Q/A what?");
			return;
		}

		String chatText = pullSocial(args)[0];
		String socialText = pullSocial(args)[1];
		Social usingSocial = null;
		String socialCommand = "";
		String socialTarget = "";
		if (socialText.length() > 0)
		{
			socialCommand = CommandHandler.getArg(socialText, 1).toLowerCase();
			socialTarget = CommandHandler.getArg(socialText, 2);
			
			for (Social s : socials)
				if (s.name.startsWith(socialCommand))
				{
					usingSocial = s;
					break;
				}
			
			if (usingSocial == null)
				c.sendln("That isn't a valid social name. Try }H'}hsocials}H'{x to list all socials.");
			if (Combat.findChar(c.ch, null, socialTarget, true) == null && socialTarget.length() > 0)
			{
				c.sendln("No character by that name was found.");
				usingSocial = null;
			}
		}

		for (UserCon cs : conns)
			if (!cs.channelOff("qa") && !cs.ignoring.contains(c.ch.shortName.toLowerCase()) && !cs.quiet)
			{
				if (usingSocial != null)
					cs.sendln("{q^{Q/A^} "+Fmt.seeNameGlobal(cs.ch, c.ch)+": {Q"+usingSocial.globalSocial(c, socialTarget, cs));
				if (chatText.length() > 0)
					cs.sendln("{q^{Q/A^} "+Fmt.seeNameGlobal(cs.ch, c.ch)+": {Q"+chatText+"{x");
			}
	}
	
	/**
	Broadcast a message from a user using the discussion channel.
	
	@param c The user who entered this command.
	@param args A string containing any arguments to the command.
	*/
	public static void doDiscussion(UserCon c, String args)
	{
		if (c.spamCheck())
			return;
		if (c.quiet)
		{
			c.sendln("You're in quiet mode. Turn quiet mode off first.");
			return;
		}
		if (c.troubled)
		{
			c.sendln("You can't use that channel when you're troubled.");
			return;
		}
		
		if (c.channelOff("discussion"))
		{
			c.sendln("You have turned the discussion channel off.");
			c.sendln("To turn it on, use }H'}hpreference channel discussion}H'{w.");
			return;
		}
		if (args.length() == 0)
		{
			c.sendln("Discuss what?");
			return;
		}
		
		String chatText = pullSocial(args)[0];
		String socialText = pullSocial(args)[1];
		Social usingSocial = null;
		String socialCommand = "";
		String socialTarget = "";
		if (socialText.length() > 0)
		{
			socialCommand = CommandHandler.getArg(socialText, 1).toLowerCase();
			socialTarget = CommandHandler.getArg(socialText, 2);
			
			for (Social s : socials)
				if (s.name.startsWith(socialCommand))
				{
					usingSocial = s;
					break;
				}
			
			if (usingSocial == null)
				c.sendln("That isn't a valid social name. Try }H'}hsocials}H'{x to list all socials.");
			if (Combat.findChar(c.ch, null, socialTarget, true) == null && socialTarget.length() > 0)
			{
				c.sendln("No character by that name was found.");
				usingSocial = null;
			}
		}

		for (UserCon cs : conns)
			if (!cs.channelOff("discussion") && !cs.ignoring.contains(c.ch.shortName.toLowerCase()) && !cs.quiet)
			{
				if (usingSocial != null)
					cs.sendln("{o^{Discussion^} "+Fmt.seeNameGlobal(cs.ch, c.ch)+": {O"+usingSocial.globalSocial(c, socialTarget, cs));
				if (chatText.length() > 0)
					cs.sendln("{o^{Discussion^} "+Fmt.seeNameGlobal(cs.ch, c.ch)+": {O"+chatText+"{x");
			}
	}
	
	/**
	Broadcast a message from a user using the quote channel.
	
	@param c The user who entered this command.
	@param args A string containing any arguments to the command.
	*/
	public static void doQuote(UserCon c, String args)
	{
		if (c.spamCheck())
			return;
		if (c.quiet)
		{
			c.sendln("You're in quiet mode. Turn quiet mode off first.");
			return;
		}
		if (c.troubled)
		{
			c.sendln("You can't use that channel when you're troubled.");
			return;
		}
		
		if (c.channelOff("quote"))
		{
			c.sendln("You have turned the quote channel off.");
			c.sendln("To turn it on, use }H'}hpreference channel quote}H'{w.");
			return;
		}
		if (args.length() == 0)
		{
			c.sendln("Quote what?");
			return;
		}
		
		String chatText = pullSocial(args)[0];
		String socialText = pullSocial(args)[1];
		Social usingSocial = null;
		String socialCommand = "";
		String socialTarget = "";
		if (socialText.length() > 0)
		{
			socialCommand = CommandHandler.getArg(socialText, 1).toLowerCase();
			socialTarget = CommandHandler.getArg(socialText, 2);
			
			for (Social s : socials)
				if (s.name.startsWith(socialCommand))
				{
					usingSocial = s;
					break;
				}
			
			if (usingSocial == null)
				c.sendln("That isn't a valid social name. Try }H'}hsocials}H'{x to list all socials.");
			if (Combat.findChar(c.ch, null, socialTarget, true) == null && socialTarget.length() > 0)
			{
				c.sendln("No character by that name was found.");
				usingSocial = null;
			}
		}

		if (usingSocial != null)
			c.sendln("{hYou quote '{H"+usingSocial.globalSocial(c, socialTarget, c)+"{h'{x");
		if (chatText.length() > 0)
			c.sendln("{hYou quote '{H"+chatText+"{h'{x");

		for (UserCon cs : conns)
			if (cs != c && !cs.channelOff("quote") && !cs.ignoring.contains(c.ch.shortName.toLowerCase()) && !cs.quiet)
			{
				if (usingSocial != null)
					cs.sendln("{h"+Fmt.seeNameGlobal(cs.ch, c.ch)+" quotes '{H"+usingSocial.globalSocial(c, socialTarget, cs)+"{h'{x");
				if (chatText.length() > 0)
					cs.sendln("{h"+Fmt.seeNameGlobal(cs.ch, c.ch)+" quotes '{H"+chatText+"{h'{x");
			}
	}
	
	/**
	Broadcast a message from a user using the grats channel.
	
	@param c The user who entered this command.
	@param args A string containing any arguments to the command.
	*/
	public static void doGrats(UserCon c, String args)
	{
		if (c.spamCheck())
			return;
		if (c.quiet)
		{
			c.sendln("You're in quiet mode. Turn quiet mode off first.");
			return;
		}
		if (c.troubled)
		{
			c.sendln("You can't use that channel when you're troubled.");
			return;
		}
		
		if (c.channelOff("grats"))
		{
			c.sendln("You have turned the grats channel off.");
			c.sendln("To turn it on, use }H'}hpreference channel grats}H'{w.");
			return;
		}
		if (args.length() == 0)
		{
			c.sendln("Grats what?");
			return;
		}
		
		String chatText = pullSocial(args)[0];
		String socialText = pullSocial(args)[1];
		Social usingSocial = null;
		String socialCommand = "";
		String socialTarget = "";
		if (socialText.length() > 0)
		{
			socialCommand = CommandHandler.getArg(socialText, 1).toLowerCase();
			socialTarget = CommandHandler.getArg(socialText, 2);
			
			for (Social s : socials)
				if (s.name.startsWith(socialCommand))
				{
					usingSocial = s;
					break;
				}
			
			if (usingSocial == null)
				c.sendln("That isn't a valid social name. Try }H'}hsocials}H'{x to list all socials.");
			if (Combat.findChar(c.ch, null, socialTarget, true) == null && socialTarget.length() > 0)
			{
				c.sendln("No character by that name was found.");
				usingSocial = null;
			}
		}

		for (UserCon cs : conns)
			if (!cs.channelOff("grats") && !cs.ignoring.contains(c.ch.shortName.toLowerCase()) && !cs.quiet)
			{
				if (usingSocial != null)
					cs.sendln("{z^{Grats^} "+Fmt.seeNameGlobal(cs.ch, c.ch)+": {Z"+usingSocial.globalSocial(c, socialTarget, cs)+"{x");
				if (chatText.length() > 0)
					cs.sendln("{z^{Grats^} "+Fmt.seeNameGlobal(cs.ch, c.ch)+": {Z"+chatText+"{x");
			}
	}

	public static void doTt(UserCon c, String args)
	{
		if (c.spamCheck())
			return;
		if (c.quiet)
		{
			c.sendln("You're in quiet mode. Turn quiet mode off first.");
			return;
		}
		
		if (c.channelOff("troubletalk"))
		{
			c.sendln("You have turned the troubletalk channel off.");
			c.sendln("To turn it on, use }H'}hpreference channel troubletalk}H'{w.");
			return;
		}
		if (!c.prefs.get("witness") && !c.troubled && !c.hasPermission("staff"))
		{
			c.sendln("You must enable witness mode to communicate on the troubled channel.");
			c.sendln("To turn it on, use }H'}hpreference witness}H'{w.{x");
		}
		if (args.length() == 0)
		{
			c.sendln("Troubletalk what?");
			return;
		}
		
		String chatText = pullSocial(args)[0];
		String socialText = pullSocial(args)[1];
		Social usingSocial = null;
		String socialCommand = "";
		String socialTarget = "";
		if (socialText.length() > 0)
		{
			socialCommand = CommandHandler.getArg(socialText, 1).toLowerCase();
			socialTarget = CommandHandler.getArg(socialText, 2);
			
			for (Social s : socials)
				if (s.name.startsWith(socialCommand))
				{
					usingSocial = s;
					break;
				}
			
			if (usingSocial == null)
				c.sendln("That isn't a valid social name. Try }H'}hsocials}H'{x to list all socials.");
			if (Combat.findChar(c.ch, null, socialTarget, true) == null && socialTarget.length() > 0)
			{
				c.sendln("No character by that name was found.");
				usingSocial = null;
			}
		}

		for (UserCon cs : conns)
			if (!cs.channelOff("troubletalk") && !cs.ignoring.contains(c.ch.shortName.toLowerCase()) && (cs.troubled || cs.prefs.get("witness") || cs.hasPermission("staff")) && !cs.quiet)
			{
				if (usingSocial != null)
					cs.sendln("{W[{RT{W] ({y"+Fmt.seeNameGlobal(cs.ch, c.ch)+"{W) : {R'{w"+usingSocial.globalSocial(c, socialTarget, cs)+"{R'{x");
				if (chatText.length() > 0)
					cs.sendln("{W[{RT{W] ({y"+Fmt.seeNameGlobal(cs.ch, c.ch)+"{W) : {R'{w"+chatText+"{R'{x");
			}
	}

	/**
	Broadcast a message from a user using the ministry channel.
	
	@param c The user who entered this command.
	@param args A string containing any arguments to the command.
	*/
	public static void doMinistry(UserCon c, String args)
	{
		if (c.spamCheck())
			return;
		if (c.quiet)
		{
			c.sendln("You're in quiet mode. Turn quiet mode off first.");
			return;
		}
		if (c.troubled)
		{
			c.sendln("You can't use that channel when you're troubled.");
			return;
		}
		
		if (c.channelOff("ministry"))
		{
			c.sendln("You have turned the ministry channel off.");
			c.sendln("To turn it on, use }H'}hpreference channel ministry}H'{w.");
			return;
		}
		if (args.length() == 0)
		{
			c.sendln("Ministry what?");
			return;
		}
		
		String chatText = pullSocial(args)[0];
		String socialText = pullSocial(args)[1];
		Social usingSocial = null;
		String socialCommand = "";
		String socialTarget = "";
		if (socialText.length() > 0)
		{
			socialCommand = CommandHandler.getArg(socialText, 1).toLowerCase();
			socialTarget = CommandHandler.getArg(socialText, 2);
			
			for (Social s : socials)
				if (s.name.startsWith(socialCommand))
				{
					usingSocial = s;
					break;
				}
			
			if (usingSocial == null)
				c.sendln("That isn't a valid social name. Try }H'}hsocials}H'{x to list all socials.");
			if (Combat.findChar(c.ch, null, socialTarget, true) == null && socialTarget.length() > 0)
			{
				c.sendln("No character by that name was found.");
				usingSocial = null;
			}
		}

		for (UserCon cs : conns)
			if (!cs.channelOff("ministry") && !cs.ignoring.contains(c.ch.shortName.toLowerCase()) && !cs.quiet)
			{
				if (usingSocial != null)
					cs.sendln("{W+ {pMinistry {W+ {p"+Fmt.seeNameGlobal(cs.ch, c.ch)+": {P"+usingSocial.globalSocial(c, socialTarget, cs)+"{x");
				if (chatText.length() > 0)
					cs.sendln("{W+ {pMinistry {W+ {p"+Fmt.seeNameGlobal(cs.ch, c.ch)+": {P"+chatText+"{x");
			}
	}

	public static void doVulgar(UserCon c, String args)
	{
		if (c.spamCheck())
			return;
		if (c.quiet)
		{
			c.sendln("You're in quiet mode. Turn quiet mode off first.");
			return;
		}
		if (c.troubled)
		{
			c.sendln("You can't use that channel when you're troubled.");
			return;
		}
		
		if (c.channelOff("vulgar"))
		{
			c.sendln("You have turned the vulgar channel off.");
			c.sendln("To turn it on, use }H'}hpreference channel vulgar}H'{w.");
			return;
		}
		if (args.length() == 0)
		{
			c.sendln("Vulgar what?");
			return;
		}
		
		String chatText = pullSocial(args)[0];
		String socialText = pullSocial(args)[1];
		Social usingSocial = null;
		String socialCommand = "";
		String socialTarget = "";
		if (socialText.length() > 0)
		{
			socialCommand = CommandHandler.getArg(socialText, 1).toLowerCase();
			socialTarget = CommandHandler.getArg(socialText, 2);
			
			for (Social s : socials)
				if (s.name.startsWith(socialCommand))
				{
					usingSocial = s;
					break;
				}
			
			if (usingSocial == null)
				c.sendln("That isn't a valid social name. Try }H'}hsocials}H'{x to list all socials.");
			if (Combat.findChar(c.ch, null, socialTarget, true) == null && socialTarget.length() > 0)
			{
				c.sendln("No character by that name was found.");
				usingSocial = null;
			}
		}

		for (UserCon cs : conns)
			if (!cs.channelOff("vulgar") && !cs.ignoring.contains(c.ch.shortName.toLowerCase()) && !cs.quiet)
			{
				if (usingSocial != null)
					cs.sendln("{v(Vulgar) "+Fmt.seeNameGlobal(cs.ch, c.ch)+": {V"+usingSocial.globalSocial(c, socialTarget, cs)+"{x");
				if (chatText.length() > 0)
					cs.sendln("{v(Vulgar) "+Fmt.seeNameGlobal(cs.ch, c.ch)+": {V"+chatText+"{x");
			}
	}
	
	/**
	Send a message from one staff member to all staff members.
	
	@param c The user who entered this command.
	@param args A string containing any arguments to the command.
	*/
	public static void doStafftalk(UserCon c, String args)
	{
		if (c.spamCheck())
			return;
		if (c.quiet)
		{
			c.sendln("You're in quiet mode. Turn quiet mode off first.");
			return;
		}
		if (c.troubled)
		{
			c.sendln("You can't use that channel when you're troubled.");
			return;
		}
		
		if (c.channelOff("stafftalk"))
		{
			c.sendln("You have turned the stafftalk channel off.");
			c.sendln("To turn it on, use }H'}hpreference channel stafftalk}H'{w.");
			return;
		}
		if (args.length() == 0)
		{
			c.sendln("Stafftalk what?");
			return;
		}
		
		String chatText = pullSocial(args)[0];
		String socialText = pullSocial(args)[1];
		Social usingSocial = null;
		String socialCommand = "";
		String socialTarget = "";
		if (socialText.length() > 0)
		{
			socialCommand = CommandHandler.getArg(socialText, 1).toLowerCase();
			socialTarget = CommandHandler.getArg(socialText, 2);
			
			for (Social s : socials)
				if (s.name.startsWith(socialCommand))
				{
					usingSocial = s;
					break;
				}
			
			if (usingSocial == null)
				c.sendln("That isn't a valid social name. Try }H'}hsocials}H'{x to list all socials.");
			if (Combat.findChar(c.ch, null, socialTarget, true) == null && socialTarget.length() > 0)
			{
				c.sendln("No character by that name was found.");
				usingSocial = null;
			}
		}

		Command targetCommand = Command.lookup("stafftalk");
		for (UserCon cs : conns)
			if (targetCommand.allowCheck(cs) && !cs.channelOff("stafftalk") && !cs.quiet)
			{
				if (usingSocial != null)
					cs.sendln("{i[Staff] "+c.ch.shortName+": {I"+usingSocial.globalSocial(c, socialTarget, cs)+"{x");
				if (chatText.length() > 0)
					cs.sendln("{i[Staff] "+c.ch.shortName+": {I"+chatText+"{x");
			}
	}
	
	/**
	Send a message from any user to all staff members.
	
	@param c The user who entered this command.
	@param args A string containing any arguments to the command.
	*/
	public static void doAdmin(UserCon c, String args)
	{
		if (c.spamCheck())
			return;
		
		if (args.length() == 0)
		{
			c.sendln("Admin what?");
			return;
		}
		
		c.sendln("{n[Admin] "+c.ch.shortName+": {N"+args+"{x");
		c.sendln("Thanks. A staff member should contact you shortly.");
		
		Command targetCommand = Command.lookup("stafftalk");
		for (UserCon cs : conns)
			if (cs != c && targetCommand.allowCheck(cs))
				cs.sendln("{n[Admin] "+c.ch.shortName+": {N"+args+"{x");
	}
	
	public static void doGtell(UserCon c, String args)
	{
		if (c.spamCheck())
			return;
		
		if (args.length() == 0)
		{
			c.sendln("Gtell what?");
			return;
		}
		
		Group userGroup = null;
		for (Group g : groups)
			if (g.members.contains(c.ch))
			{
				userGroup = g;
				break;
			}
		
		if (userGroup == null)
		{
			c.sendln("You're not in a group.");
			return;
		}
		
		for (CharData ch : userGroup.members)
			ch.sendln("{W[{eGroup{W] {e"+Fmt.seeNameGlobal(ch, c.ch)+": {E"+args+"{x");
	}
	
	/**
	Show a message to all characters in the same room as the user.
	
	@param c The user who entered this command.
	@param args A string containing any arguments to the command.
	*/
	public static void doSay(UserCon c, String args)
	{
		if (c.spamCheck())
			return;
		if (c.ch.currentRoom.flags.get("silent"))
		{
			c.sendln("You can't talk in this room.");
			return;
		}
		
		if (c.ch.position.equals("sleeping"))
		{
			c.sendln("You can't do that when you're "+c.ch.position+".");
			return;
		}

		if (args.length() == 0)
		{
			c.sendln("Say what?");
			return;
		}

		c.sendln("{sYou say '{S"+args+"{s'{x");
		for (UserCon cs : conns)
			if (cs.ch.currentRoom == c.ch.currentRoom && c != cs)
				if (!cs.ch.position.equals("sleeping"))
					cs.sendln("{s"+Fmt.hearName(cs.ch, c.ch)+" says '{S"+args+"{s'{x");

		if (c.ch.currentRoom.checkTrigger("speech", c.ch, null, args, 0) != -1)
			return;
		for (CharData ch : allChars())
			if (ch.currentRoom == c.ch.currentRoom && ch != c.ch)
				if (ch.checkTrigger("speech", c.ch, null, args, 0) != -1)
					return;
		for (ObjData o : c.ch.currentRoom.objects)
			if (o.checkTrigger("speech", c.ch, null, args, 0) != -1)
				return;
		for (ObjData o : c.ch.objects)
			if (o.checkTrigger("speech", c.ch, null, args, 0) != -1)
				return;
	}

	public static void doIsay(UserCon c, String args)
	{
		if (c.spamCheck())
			return;
		if (c.ch.currentRoom.flags.get("silent"))
		{
			c.sendln("You can't talk in this room.");
			return;
		}
		
		if (c.ch.position.equals("sleeping"))
		{
			c.sendln("You can't do that when you're "+c.ch.position+".");
			return;
		}

		if (args.length() == 0)
		{
			c.sendln("Say what?");
			return;
		}

		c.sendln("{sYou say {S(I{sn {SC{sharacter{S){s '{S"+args+"{s'{x");
		for (UserCon cs : conns)
			if (cs.ch.currentRoom == c.ch.currentRoom && c != cs)
				if (!cs.ch.position.equals("sleeping"))
					cs.sendln("{s"+Fmt.hearName(cs.ch, c.ch)+" says {S(I{sn {SC{sharacter{S){s '{S"+args+"{s'{x");

		if (c.ch.currentRoom.checkTrigger("speech", c.ch, null, args, 0) != -1)
			return;
		for (CharData ch : allChars())
			if (ch.currentRoom == c.ch.currentRoom && ch != c.ch)
				if (ch.checkTrigger("speech", c.ch, null, args, 0) != -1)
					return;
		for (ObjData o : c.ch.currentRoom.objects)
			if (o.checkTrigger("speech", c.ch, null, args, 0) != -1)
				return;
		for (ObjData o : c.ch.objects)
			if (o.checkTrigger("speech", c.ch, null, args, 0) != -1)
				return;
	}

	/**
	Show a message to all characters in the same area as the user.
	
	@param c The user who entered this command.
	@param args A string containing any arguments to the command.
	*/
	public static void doYell(UserCon c, String args)
	{
		if (c.spamCheck())
			return;
		if (c.troubled)
		{
			c.sendln("You can't use that channel when you're troubled.");
			return;
		}
		if (c.ch.currentRoom.flags.get("silent"))
		{
			c.sendln("You can't yell in this room.");
			return;
		}
		
		if (c.ch.position.equals("sleeping"))
		{
			c.sendln("You can't do that when you're "+c.ch.position+".");
			return;
		}
		
		if (args.length() == 0)
		{
			c.sendln("Yell what?");
			return;
		}

		c.sendln("{kYou yell '{K"+args+"{k'{x");
		for (UserCon cs : conns)
			if (cs.ch.currentArea() == c.ch.currentArea() && c != cs && !cs.ignoring.contains(c.ch.shortName.toLowerCase()))
				cs.sendln("{k"+Fmt.hearName(cs.ch, c.ch)+" yells '{K"+args+"{k'{x");

		if (c.ch.currentRoom.checkTrigger("speech", c.ch, null, args, 0) != -1)
			return;
		for (CharData ch : allChars())
			if (ch.currentRoom == c.ch.currentRoom && ch != c.ch)
				if (ch.checkTrigger("speech", c.ch, null, args, 0) != -1)
					return;
		for (ObjData o : c.ch.currentRoom.objects)
			if (o.checkTrigger("speech", c.ch, null, args, 0) != -1)
				return;
		for (ObjData o : c.ch.objects)
			if (o.checkTrigger("speech", c.ch, null, args, 0) != -1)
				return;
	}
	
	/**
	Show a custom social message to all characters in the same room as the user.
	
	@param c The user who entered this command.
	@param args A string containing any arguments to the command.
	*/
	public static void doEmote(UserCon c, String args)
	{
		if (c.spamCheck())
			return;
		if (c.ch.currentRoom.flags.get("silent"))
		{
			c.sendln("You can't emote in this room.");
			return;
		}
		
		if (c.ch.position.equals("sleeping"))
		{
			c.sendln("You can't do that when you're "+c.ch.position+".");
			return;
		}

		if (args.length() == 0)
		{
			c.sendln("Emote what?");
			return;
		}
		
		for (UserCon cs : conns)
			if (cs.ch.currentRoom == c.ch.currentRoom)
				if (!cs.ch.position.equals("sleeping"))
					cs.sendln(Fmt.seeName(cs.ch, c.ch)+" "+args);
	}
	
	/**
	Broadcast a global custom social message from the user.
	
	@param c The user who entered this command.
	@param args A string containing any arguments to the command.
	*/
	public static void doGemote(UserCon c, String args)
	{
		if (c.spamCheck())
			return;
		if (c.quiet)
		{
			c.sendln("You're in quiet mode. Turn quiet mode off first.");
			return;
		}
		if (c.troubled)
		{
			c.sendln("You can't use that channel when you're troubled.");
			return;
		}
		
		if (c.channelOff("gemote"))
		{
			c.sendln("You have turned the gemote channel off.");
			c.sendln("To turn it on, use }H'}hpreference channel gemote}H'{w.");
			return;
		}
		if (args.length() == 0)
		{
			c.sendln("Gemote what?");
			return;
		}
		
		for (UserCon cs : conns)
			if (!cs.channelOff("gemote") && !cs.ignoring.contains(c.ch.shortName.toLowerCase()) && !cs.quiet)
				cs.sendln("{jGemote: {J"+Fmt.seeNameGlobal(cs.ch, c.ch)+" "+args+"{x");
	}

	/**
	Perform a social and display it globally.
	
	@param c The user who entered this command.
	@param args A string containing any arguments to the command.
	*/
	public static void doGocial(UserCon c, String args)
	{
		if (c.spamCheck())
			return;
		if (c.quiet)
		{
			c.sendln("You're in quiet mode. Turn quiet mode off first.");
			return;
		}
		if (c.troubled)
		{
			c.sendln("You can't use that channel when you're troubled.");
			return;
		}
		
		if (c.channelOff("gocial"))
		{
			c.sendln("You have turned the gocial channel off.");
			c.sendln("To turn it on, use }H'}hpreference channel gocial}H'{w.");
			return;
		}
		if (args.length() == 0)
		{
			c.sendln("Gocial what?");
			return;
		}

		Social usingSocial = null;
		String socialCommand = CommandHandler.getArg(args, 1).toLowerCase();
		String socialTarget = CommandHandler.getArg(args, 2);

		for (Social s : socials)
			if (s.name.startsWith(socialCommand))
			{
				usingSocial = s;
				break;
			}
		
		if (usingSocial == null)
		{
			c.sendln("That isn't a valid social name. Try }H'}hsocials}H'{x to list all socials.");
			return;
		}

		if (Combat.findChar(c.ch, null, socialTarget, true) == null && socialTarget.length() > 0)
		{
			c.sendln("No character by that name was found.");
			return;
		}

		for (UserCon cs : conns)
			if (!cs.channelOff("gocial") && !cs.ignoring.contains(c.ch.shortName.toLowerCase()) && !cs.quiet)
				cs.sendln("{jGocial: {J"+usingSocial.globalSocial(c, socialTarget, cs));
	}
	
	/**
	Send a private message from one user to another.
	
	@param c The user who entered this command.
	@param args A string containing any arguments to the command.
	*/
	public static void doTell(UserCon c, String args)
	{
		if (c.spamCheck())
			return;
		
		String arg1 = CommandHandler.getArg(args, 1);
		String arg2 = CommandHandler.getLastArg(args, 2);
		
		if (arg1.length() == 0)
		{
			c.sendln("Tell who what?");
			return;
		}
		
		UserCon cs = null;
		for (UserCon rc : conns)
			if (rc.ch.shortName.toLowerCase().startsWith(arg1.toLowerCase()))
				if (Combat.canSeeGlobal(c.ch, rc.ch))
				{
					cs = rc;
					break;
				}
		if (arg1.startsWith("RLIST#="))
		{
			int checkNr = Fmt.getInt(arg1.substring(7));
			if (checkNr >= 1 && checkNr <= c.replyList.size())
				cs = c.replyList.get(checkNr-1).conn;
		}
		
		if (cs == null)
		{
			c.sendln("There is no user by that name online.");
			return;
		}
		
		if (!cs.hasPermission("staff") && !cs.prefs.get("witness") && c.troubled)
		{
			c.sendln("When you're troubled, you can only send tells to staff members and");
			c.sendln("other players who have opted to communicate with troubled players.");
			return;
		}
		
		if (cs.troubled && !c.hasPermission("staff") && !c.prefs.get("witness"))
		{
			c.sendln("You can't send tells to troubled players.");
			c.sendln("To change this setting, use 'preference witness'.");
			return;
		}
		
		if (cs.ignoring.contains(c.ch.shortName.toLowerCase()))
		{
			c.sendln("That player is ignoring you.");
			return;
		}
		
		if (c.ignoring.contains(cs.ch.shortName.toLowerCase()))
		{
			c.sendln("You're ignoring that player. Remove them from your ignore list first.");
			return;
		}
		
		if (arg2.length() == 0)
		{
			c.sendln("Tell "+Fmt.seeNameGlobal(c.ch, cs.ch)+" what?");
			return;
		}
		if (cs == c)
		{
			c.sendln("Talking to yourself again?");
			return;
		}
		
		int replyNumber = cs.replyList.size()+1;
		for (int ctr = 0; ctr < cs.replyList.size(); ctr++)
			if (cs.replyList.get(ctr) == c.ch)
				replyNumber = ctr+1;
		if (replyNumber > cs.replyList.size())
			cs.replyList.add(c.ch);
		
		int ctr = 1;
		String outgoingNr = "";
		for (CharData ch : c.replyList)
			if (ch == cs.ch)
				outgoingNr = "}M[}n"+ctr+"}M] ";
			else
				ctr++;
		if (outgoingNr.length() == 0)
		{
			c.replyList.add(cs.ch);
			outgoingNr = "}M[}n"+ctr+"}M] ";
		}
		
		String chatText = pullSocial(arg2)[0];
		String socialText = pullSocial(arg2)[1];
		Social usingSocial = null;
		String socialCommand = "";
		String socialTarget = "";
		if (socialText.length() > 0)
		{
			socialCommand = CommandHandler.getArg(socialText, 1).toLowerCase();
			socialTarget = CommandHandler.getArg(socialText, 2);
			
			for (Social s : socials)
				if (s.name.startsWith(socialCommand))
				{
					usingSocial = s;
					break;
				}
			
			if (usingSocial == null)
				c.sendln("That isn't a valid social name. Try }H'}hsocials}H'{x to list all socials.");
			if (Combat.findChar(c.ch, null, socialTarget, true) == null && socialTarget.length() > 0)
			{
				c.sendln("No character by that name was found.");
				usingSocial = null;
			}
		}

		if (usingSocial != null)
		{
			cs.sendln("}M[}n"+replyNumber+"}M] {t"+Fmt.seeNameGlobal(cs.ch, c.ch)+" tells you '{T"+usingSocial.globalSocial(c, socialTarget, cs)+"{t'{x");
			c.sendln(outgoingNr+"{tYou tell "+Fmt.seeNameGlobal(c.ch, cs.ch)+" '{T"+usingSocial.globalSocial(c, socialTarget, c)+"{t'{x");
			if (cs.prefs.get("savetells") || cs.afk)
				cs.savedTells.add("{t"+Fmt.seeNameGlobal(cs.ch, c.ch)+"{t tells you '{T"+usingSocial.globalSocial(c, socialTarget, cs)+"{t'{x");
		}
		if (chatText.length() > 0)
		{
			cs.sendln("}M[}n"+replyNumber+"}M] {t"+Fmt.seeNameGlobal(cs.ch, c.ch)+" tells you '{T"+chatText+"{t'{x");
			c.sendln(outgoingNr+"{tYou tell "+Fmt.seeNameGlobal(c.ch, cs.ch)+" '{T"+chatText+"{t'{x");
			if (cs.prefs.get("savetells") || cs.afk)
				cs.savedTells.add("{t"+Fmt.seeNameGlobal(cs.ch, c.ch)+"{t tells you '{T"+chatText+"{t'{x");
		}
	}	
		
	public static void doReport(UserCon c, String args)
	{
		for (UserCon cs : conns)
			if (cs.ch.currentRoom == c.ch.currentRoom)
			cs.sendln(Fmt.cap(Fmt.seeName(cs.ch, c.ch))+" reports: " + c.ch.hp + " / " + c.ch.maxHp() + " hp, " +
				c.ch.mana + " / " + c.ch.maxMana() + " mana, " + c.ch.energy + " / " + c.ch.maxEnergy() + " energy.");
	}
	
	public static void doTnl(UserCon c, String args)
	{
		for (UserCon cs : conns)
			if (cs.ch.currentRoom == c.ch.currentRoom)
			cs.sendln(Fmt.cap(Fmt.seeName(cs.ch, c.ch))+" reports: " + c.ch.tnl + " experience until next level.");
	}
		
	/**
	Reply from a user to someone who already sent them a tell, using their replylist.
	
	@param c The user who entered this command.
	@param args A string containing any arguments to the command.
	*/
	public static void doReply(UserCon c, String args)
	{
		if (c.replyList.size() == 0)
		{
			c.sendln("You have nobody to reply to.");
			return;
		}
		
		int replyCount = Fmt.getInt(CommandHandler.getArg(args, 1));
		if (replyCount == 0)
		{
			doTell(c, "RLIST#=1 "+args);
			return;
		}
		if (c.replyList.size() < replyCount)
		{
			c.sendln("There aren't that many people on your reply list. To view it, try 'replylist'.");
			return;
		}
		doTell(c, "RLIST#="+replyCount+" "+CommandHandler.getLastArg(args, 2));
	}
	
	/**
	Show a list of people who have recently sent tells to the user.
	
	@param c The user who entered this command.
	@param args A string containing any arguments to the command.
	*/
	public static void doReplylist(UserCon c, String args)
	{
		if (args.length() == 0)
		{
			if (c.replyList.size() == 0)
			{
				c.sendln("You have nobody to reply to.");
				return;
			}
			for (int ctr = 0; ctr < c.replyList.size(); ctr++)
				c.sendln((ctr+1)+" : "+Fmt.seeNameGlobal(c.ch, c.replyList.get(ctr)));
			return;
		}
		
		String arg1 = CommandHandler.getArg(args, 1).toLowerCase();
		String arg2 = CommandHandler.getArg(args, 2);
		
		if ("delete".startsWith(arg1))
		{
			if (arg2.length() == 0)
			{
				c.sendln("Delete which reply number?");
				return;
			}
			int delNr = Fmt.getInt(arg2);
			if (delNr <= 0 || delNr > c.replyList.size())
			{
				c.sendln("That's not a valid reply number to delete.");
				return;
			}
			c.replyList.get(delNr-1).conn.replyList.remove(c.ch);
			c.replyList.get(delNr-1).sendln("Your reply list has changed.");
			c.replyList.remove(delNr-1);
			c.sendln("Your reply list has changed.");
			return;
		}
		
		InfoCommands.doHelp(c, "replylist");
	}
	
	/**
	Show any tells/replies which have been saved by the user, and clear their buffer.
	
	@param c The user who entered this command.
	@param args A string containing any arguments to the command.
	*/
	public static void doReplay(UserCon c, String args)
	{
		if (c.savedTells.size() == 0)
		{
			c.sendln("You have no saved tells to display.");
			return;
		}
		for (String s : c.savedTells)
			c.sendln(s);
		
		c.savedTells.clear();
	}
	
	public static String[] pullSocial(String args)
	{
		String socialText = "";
		String chatText = args;
		if (args.charAt(0) == '*')
		{
			chatText = "";
			String argSplit[] = args.split(" ", 2);
			if (argSplit[0].endsWith("*") || argSplit.length == 1)
			{
				socialText = argSplit[0];
				if (argSplit.length > 1)
					chatText = argSplit[1];
			}
			else
			{
				argSplit = args.split(" ", 3);
				if (argSplit.length > 1)
				{
					socialText = argSplit[0]+" "+argSplit[1];
					if (argSplit.length > 2)
						chatText = argSplit[2];
				}
			}
		}
		socialText = socialText.trim().replace("*", "");
		chatText = chatText.trim();
		
		String result[] = {chatText, socialText};
		return result;
	}
	
	public static void doGroup(UserCon c, String args)
	{
		String arg1 = CommandHandler.getArg(args, 1).toLowerCase();
		String arg2 = CommandHandler.getArg(args, 2);
		
		Group userGroup = null;
		for (Group g : groups)
			if (g.members.contains(c.ch))
				userGroup = g;
		
		if (arg1.length() == 0 || "list".startsWith(arg1))
		{
			if (userGroup == null)
			{
				c.sendln("You're not in a group.");
				return;
			}
			else
			{
				c.sendln("}m    Name               Lvl  %hp  %mn  %st  Location");
				for (CharData ch : userGroup.members)
				{
					int hpPct = (ch.hp*100)/ch.maxHp();
					int mnPct = (ch.mana*100)/ch.maxMana();
					int stPct = (ch.energy*100)/ch.maxEnergy();

					String lead = "}N    ";
					if (userGroup.leader == ch)
						lead = "}M({WL}M) }N";
					String location = "(R) "+ch.currentRoom.name;
					if (ch.currentArea() != null && ch.currentArea() != c.ch.currentArea())
						location = "(A) "+ch.currentArea().name;
						
					c.sendln(lead+Fmt.fit(Fmt.cap(Fmt.seeNameGlobal(c.ch, ch)), 18)+" }n"+Fmt.rfit(""+ch.level, 3)+" "+Fmt.rfit(hpPct+"%", 5)+Fmt.rfit(mnPct+"%", 5)+Fmt.rfit(stPct+"%", 5)+"}N "+Fmt.fit(location, 30));
				}
			}
			return;
		}
		
		if ("invite".startsWith(arg1))
		{
			if (userGroup != null)
			{
				if (userGroup.leader != c.ch)
				{
					c.sendln("You're not the leader of your group.");
					return;
				}
			}
			if (arg2.length() == 0)
			{
				c.sendln("Invite whom to the group?");
				return;
			}
			CharData target = Combat.findChar(c.ch, null, arg2, true);
			if (target == null)
			{
				c.sendln("No character by that name was found.");
				return;
			}
			if (target == c.ch)
			{
				c.sendln("You can't invite yourself to a group.");
				return;
			}
			if (userGroup == null)
			{
				userGroup = new Group(c.ch);
				userGroup.invite(target);
				if (userGroup.invites.size() > 0)
					groups.add(userGroup);
			}
			else
			{
				userGroup.invite(target);
			}
			return;
		}
		
		if ("accept".startsWith(arg1))
		{
			for (Group g : groups)
			{
				if (g.invites.contains(c.ch))
				{
					g.invites.remove(c.ch);
					for (CharData ch : g.members)
						ch.sendln(Fmt.cap(Fmt.seeNameGlobal(ch, c.ch))+" has joined the group.");
					g.members.add(c.ch);
					c.sendln("You have joined "+Fmt.seeNameGlobal(c.ch, g.leader)+"'s group.");
					return;
				}
			}
			c.sendln("You haven't been invited to any groups.");
			return;
		}
		
		if ("decline".startsWith(arg1))
		{
			for (Group g : groups)
			{
				if (g.invites.contains(c.ch))
				{
					g.invites.remove(c.ch);
					g.leader.sendln(Fmt.cap(Fmt.seeNameGlobal(g.leader, c.ch))+" has declined your group invitation.");
					c.sendln("You have declined "+Fmt.seeNameGlobal(c.ch, g.leader)+"'s group invitation.");
					return;
				}
			}
			c.sendln("You haven't been invited to any groups.");
			return;
		}
		
		if ("leave".startsWith(arg1))
		{
			if (userGroup == null)
			{
				c.sendln("You're not in a group.");
				return;
			}
			c.sendln("You leave the group.");
			userGroup.members.remove(c.ch);
			for (CharData chs : userGroup.members)
				chs.sendln(Fmt.cap(Fmt.seeNameGlobal(chs, c.ch))+" has left the group.");
			if (userGroup.leader == c.ch && userGroup.members.size() > 0)
			{
				userGroup.leader = userGroup.members.get(0);
				userGroup.leader.sendln("You are now the group leader.");
				for (CharData chs : userGroup.members)
					if (chs != userGroup.leader)
						chs.sendln(Fmt.cap(Fmt.seeNameGlobal(chs, userGroup.leader))+" is now the group leader.");
			}
			return;
		}
		
		if ("kick".startsWith(arg1))
		{
			if (userGroup == null)
			{
				c.sendln("You're not in a group.");
				return;
			}
			if (userGroup.leader != c.ch)
			{
				c.sendln("Only the group leader can kick group members.");
				return;
			}
			
			CharData target = Combat.findChar(c.ch, null, arg2, true);
			if (target == null)
			{
				c.sendln("No character by that name was found.");
				return;
			}
			if (target == c.ch)
			{
				c.sendln("You can't kick yourself from a group.");
				return;
			}
			if (!userGroup.members.contains(target))
			{
				c.sendln("That person isn't in your group.");
				return;
			}
			target.sendln(Fmt.cap(Fmt.seeNameGlobal(target, c.ch))+" has kicked you from the group.");
			userGroup.members.remove(target);
			for (CharData chs : userGroup.members)
				if (chs == c.ch)
					chs.sendln(Fmt.cap(Fmt.seeNameGlobal(chs, target))+" has been kicked from the group.");
				else
					chs.sendln(Fmt.cap(Fmt.seeNameGlobal(chs, target))+" has been kicked from the group by "+Fmt.seeNameGlobal(chs, c.ch)+".");
			return;
		}
		
		if ("leader".startsWith(arg1))
		{
			if (userGroup == null)
			{
				c.sendln("You're not in a group.");
				return;
			}
			if (userGroup.leader != c.ch)
			{
				c.sendln("Only the group leader can appoint a new group leader.");
				return;
			}
			
			CharData target = Combat.findChar(c.ch, null, arg2, true);
			if (target == null)
			{
				c.sendln("No character by that name was found.");
				return;
			}
			if (target == c.ch)
			{
				c.sendln("You're already the leader.");
				return;
			}
			if (!userGroup.members.contains(target))
			{
				c.sendln("That person isn't in your group.");
				return;
			}
			target.sendln(Fmt.cap(Fmt.seeNameGlobal(target, c.ch))+" has made you the group leader.");
			userGroup.leader = target;
			for (CharData chs : userGroup.members)
				if (chs != target)
					chs.sendln(Fmt.cap(Fmt.seeNameGlobal(chs, target))+" is now the group leader.");
			return;
		}
		
		if ("disband".startsWith(arg1))
		{
			if (userGroup == null)
			{
				c.sendln("You're not in a group.");
				return;
			}
			if (userGroup.leader != c.ch)
			{
				c.sendln("Only the group leader can disband the group.");
				c.sendln("To leave the group, use 'group leave'.");
				return;
			}
			for (CharData ch : userGroup.members)
				ch.sendln("Your group has been disbanded.");
			groups.remove(userGroup);
		}
		
		InfoCommands.doHelp(c, "group");
		return;
	}
	
	public static void doFollow(UserCon c, String args)
	{
		String arg1 = CommandHandler.getArg(args, 1).toLowerCase();
		
		if (arg1.length() == 0)
		{
			if (c.ch.following == null)
			{
				c.sendln("You're not following anyone.");
				return;
			}
			c.sendln("You stop following "+Fmt.seeName(c.ch, c.ch.following)+".");
			c.ch.following.sendln(Fmt.cap(Fmt.seeName(c.ch.following, c.ch))+" stops following you.");
			c.ch.following = null;
			return;
		}
		
		CharData target = Combat.findChar(c.ch, null, arg1, false);
		
		if (target == null)
		{
			c.sendln("There isn't anyone here by that name.");
			return;
		}
		if (target == c.ch)
		{
			if (c.ch.following != null)
			{
				c.sendln("You stop following "+Fmt.seeName(c.ch, c.ch.following)+".");
				c.ch.following.sendln(Fmt.cap(Fmt.seeName(c.ch.following, c.ch))+" stops following you.");
				c.ch.following = null;
				return;
			}
			c.sendln("You can't follow yourself.");
			return;
		}
		
		if (target == c.ch.following)
		{
			c.sendln("You're already following "+Fmt.seeName(c.ch, target)+".");
			return;
		}
		
		if (target.conn.prefs.get("nofollow"))
		{
			c.sendln(Fmt.cap(Fmt.seeName(c.ch, target))+" does not wish to be followed.");
			return;
		}
		
		if (c.ch.following != null)
		{
			c.sendln("You stop following "+Fmt.seeName(c.ch, c.ch.following)+".");
			c.ch.following.sendln(Fmt.cap(Fmt.seeName(c.ch.following, c.ch))+" stops following you.");
		}
		
		c.ch.following = target;
		target.sendln(Fmt.cap(Fmt.seeName(target, c.ch))+" is now following you.");
		c.ch.sendln("You are now following "+Fmt.seeName(c.ch, target)+".");
	}
	
	public static void doQuiet(UserCon c, String args)
	{
		if (c.quiet)
		{
			c.quiet = false;
			c.sendln("You are no longer in quiet mode.");
			return;
		}
		c.quiet = true;
		c.sendln("You are now in quiet mode. All global channels have been muted.");
		return;
	}
	
	public static void doChannels(UserCon c, String args)
	{
		c.sendln(Fmt.heading("Channel Status"));
		c.sendln("}m            Fellowship: "+(c.channelOff("fellowship") ? "{ROff" : "{GOn ")+" }M/ {fSomeone fellowships '{FSomething{f'");
		c.sendln("}m                   Q/A: "+(c.channelOff("qa") ? "{ROff" : "{GOn ")+" }M/ {q^{Q/A^} Someone: {QSomething{x");
		c.sendln("}m            Discussion: "+(c.channelOff("discussion") ? "{ROff" : "{GOn ")+" }M/ {o^{Discussion^} Someone: {OSomething{x");
		c.sendln("}m                 Quote: "+(c.channelOff("quote") ? "{ROff" : "{GOn ")+" }M/ {hSomeone quotes '{HSomething{h'");
		c.sendln("}m           Troubletalk: "+(c.channelOff("troubletalk") ? "{ROff" : "{GOn ")+" }M/ {W[{RT{W] ({ySomeone{W) : {R'{wSomething{R'");
		c.sendln("}m                Vulgar: "+(c.channelOff("vulgar") ? "{ROff" : "{GOn ")+" }M/ {v(Vulgar) Someone: {VSomething{x");
		c.sendln("}m              Ministry: "+(c.channelOff("ministry") ? "{ROff" : "{GOn ")+" }M/ {W+ {pMinistry {W+ {pSomeone: {PSomething{x");
		c.sendln("}m                Gemote: "+(c.channelOff("gemote") ? "{ROff" : "{GOn ")+" }M/ {jGemote: {JSomeone does something.{x");
		c.sendln("}m                Gocial: "+(c.channelOff("gocial") ? "{ROff" : "{GOn ")+" }M/ {jGocial: {JSomeone does something.{x");
		Command targetCommand = Command.lookup("stafftalk");
		if (targetCommand.allowCheck(c))
			c.sendln("}m             Stafftalk: "+(c.channelOff("stafftalk") ? "{ROff" : "{GOn ")+" }M/ {i[Staff] Someone: {ISomething{x");
		c.sendln(Fmt.heading(""));
	}
}