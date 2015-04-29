package jw.commands;
import java.io.*;
import java.util.*;

import jw.core.*;
import jw.data.*;
import static jw.core.MudMain.*;

/**
	The InfoCommands class contains commands with the primary purpose of providing
	information to the user, including score, area listing, etc.
*/
public class InfoCommands
{
	/**
	List all commands available to the user.

	@param c The user who entered this command.
	@param args A string containing any arguments to the command.
	*/
	public static void doCommands(UserCon c, String args)
	{
		ArrayList<String> cmds = new ArrayList<String>();
		c.sendln("The following commands are available to you:");
		for (Command cmd : commands)
			if (cmd.allowCheck(c))
			{
				String cmdString = "";
				for (String s : cmd.alias)
				{
					if (cmdString.length() > 0)
						cmdString = cmdString+" / ";
					cmdString = cmdString+s;
				}
				cmds.add(cmdString);
			}
		c.sendln(Fmt.defaultTextColumns(cmds.toArray(new String[0])));
	}
	
	/**
	Show information about a user's account.

	@param c The user who entered this command.
	@param args A string containing any arguments to the command.
	*/
	public static void doWhois(UserCon c, String args)
	{
		args = CommandHandler.getArg(args, 1);

		if (args.length() == 0)
			args = c.ch.name;

		for (UserCon con : conns)
		{
			if (con.ch.name.toLowerCase().equalsIgnoreCase(args))
			{
				c.sendln(Fmt.heading(""));
				c.sendln("}m User information for }n"+con.ch.shortName+"{x");
				if (con.realname.length() > 0)
					c.sendln("}m   Real Name}M:}n "+con.realname);
				if (con.email.length() > 0 && con.prefs.get("showemail"))
					c.sendln("}m      E-mail}M:}n "+con.email);
				long totalSecondsPlayed = (int)Math.floor(con.timePlayed/1000);
				long secondsPlayed = (int)totalSecondsPlayed % 60;
				long minutesPlayed = (int)Math.floor(totalSecondsPlayed/60) % 60;
				long hoursPlayed = (int)Math.floor(totalSecondsPlayed/3600) % 24;
				long daysPlayed = (int)Math.floor(totalSecondsPlayed/86400);
				c.sendln("}m Time Played}M:}n "+daysPlayed+" days, "+hoursPlayed+" hours, "+minutesPlayed+" minutes, "+secondsPlayed+" seconds");
				c.sendln("}m   Join Date}M:}n "+longFrmt.format(con.joined*1000+c.timeAdj*3600000));
				c.sendln(Fmt.heading("")+"{x");
				if (c.hasPermission("staff"))
				{
					c.sendln("}m Newest Site}M:}n "+con.host1);
					c.sendln("}m Second Site}M:}n "+con.host2);
					c.sendln("}m Oldest Site}M:}n "+con.host3);
					c.sendln(Fmt.heading("")+"{x");
				}
				return;
			}
		}
		
		c.sendln("There is no user by that name online.");
	}

	public static void doWhowas(UserCon c, String args)
	{
		args = CommandHandler.getArg(args, 1);

		if (args.length() == 0)
			args = c.ch.name;
		
		UserCon con = new UserCon();
		Database.loadAccount(con, args);
		
		if (con.isDummy)
		{
			c.sendln("There is no user by that name. The full name must be used for whowas.");
			return;
		}
		con.ch = new CharData(con, false);

		c.sendln(Fmt.heading(""));
		c.sendln("}m User information for }n"+con.ch.shortName+"{x");
		if (con.realname.length() > 0)
			c.sendln("}m   Real Name}M:}n "+con.realname);
		if (con.email.length() > 0 && con.prefs.get("showemail"))
			c.sendln("}m      E-mail}M:}n "+con.email);
		long totalSecondsPlayed = (int)Math.floor(con.timePlayed/1000);
		long secondsPlayed = (int)totalSecondsPlayed % 60;
		long minutesPlayed = (int)Math.floor(totalSecondsPlayed/60) % 60;
		long hoursPlayed = (int)Math.floor(totalSecondsPlayed/3600) % 24;
		long daysPlayed = (int)Math.floor(totalSecondsPlayed/86400);
		c.sendln("}m Time Played}M:}n "+daysPlayed+" days, "+hoursPlayed+" hours, "+minutesPlayed+" minutes, "+secondsPlayed+" seconds");
		c.sendln("}m   Join Date}M:}n "+longFrmt.format(con.joined*1000+c.timeAdj*3600000));
		c.sendln("}m   Last Seen}M:}n "+longFrmt.format(con.lastConnected*1000+c.timeAdj*3600000));
		c.sendln(Fmt.heading("")+"{x");
		if (c.hasPermission("staff"))
		{
			c.sendln("}m Newest Site}M:}n "+con.host1);
			c.sendln("}m Second Site}M:}n "+con.host2);
			c.sendln("}m Oldest Site}M:}n "+con.host3);
			c.sendln(Fmt.heading("")+"{x");
		}
	}
	
	/**
	Search for and display a help file.
	
	@param c The user who entered this command.
	@param args A string containing any arguments to the command.
	*/
	public static void doHelp(UserCon c, String args)
	{
		// If the user has access to the "hedit" command, show help IDs.
		boolean showIds = false;
		for (Command cmd : commands)
			if (cmd.fullName.equals("hedit") && cmd.allowCheck(c))
			{
				showIds = true;
				break;
			}
		
		args = args.toLowerCase();
		ArrayList<Help> showHelp = new ArrayList<Help>();
		int foundType = 0;
		
		// If there is no argument, show the help index page.
		if (args.length() == 0)
		{
			doHelp(c, "index");
			return;
		}
		
		// Look for an exact title match.
		for (Help h : helps)
			if (h.title.toLowerCase().equals(args) ||
				h.title.toLowerCase().startsWith(args+" ") ||
				h.title.toLowerCase().endsWith(" "+args) ||
				h.title.toLowerCase().contains(" "+args+" "))
				if (h.allowCheck(c))
				{
					showHelp.add(h);
					foundType = 1;
					break;
				}

		// If no exact title matches were found, look for similar titles.
		if (showHelp.size() == 0)
			for (Help h : helps)
				if (h.title.toLowerCase().contains(args))
					if (h.allowCheck(c))
					{
						showHelp.add(h);
						foundType = 2;
					}

		// If no matches in titles were found, search help file text.
		if (showHelp.size() == 0)
			for (Help h : helps)
				if (h.text.toLowerCase().contains(args))
					if (h.allowCheck(c))
					{
						showHelp.add(h);
						foundType = 3;
					}
		
		if (showHelp.size() == 0)
			c.sendln("There were no help files found on '"+args+"'.");
		else
		{
			if (showHelp.size() == 1)
			{
				if (foundType > 1)
				{
					c.sendln("}IThere were no exact matches for '"+args+"'.");
					c.sendln("}IShowing closest title.{x");
				}
				
				c.sendln(Fmt.heading("")+"{x");
				if (showIds)
					c.send(" }M[}m#}n"+showHelp.get(0).id+"}M]{x");
				c.sendln(" }hHelp File: }I"+showHelp.get(0).title+"{x");
				c.sendln(Fmt.heading("")+"{x");
				c.sendln("}I"+showHelp.get(0).text+"{x");
				c.sendln(Fmt.heading("")+"{x");
			}
			else
			{
				c.sendln("}IThere were multiple results for '"+args+"'.{x");
				c.sendln("}IUse }H'}hhelp }H<}htitle}H>'}I to view any of these results.{x");
				c.sendln(Fmt.heading("")+"{x");
				for (Help h : showHelp)
					c.sendln(" }I"+h.title+"{x");
				c.sendln(Fmt.heading("")+"{x");
			}
		}
	}
	
	/**
	Show a user's score sheet.

	@param c The user who entered this command.
	@param args A string containing any arguments to the command.
	*/
	public static void doScore(UserCon c, String args)
	{
		CharData target;

		if (args.length() == 0 || !c.hasPermission("staff"))
			target = c.ch;
		else if ((target = Combat.findChar(c.ch, null, args, true)) == null)
		{
			c.sendln("No character by that name was found.");
			return;
		}
		
		c.sendln("}m Score for }N"+target.shortName+"}M:");
		c.sendln("}M"+c.repeat("-", 70));
		c.sendln("}mRace    }M[}n"+Fmt.rfit(target.charRace.name, 14)+"}M] }mHP      }M[}n"+Fmt.rfit(""+target.hp, 6)+"}M/}n"+Fmt.fit(""+target.maxHp(), 6)+"}M] }mSlash Armor   }M[}n"+Fmt.rfit(""+target.maxArmSlash(), 5)+"}M]");
		c.sendln("}mClass   }M[}n"+Fmt.rfit(target.charClass.name, 14)+"}M] }mMana    }M[}n"+Fmt.rfit(""+target.mana, 6)+"}M/}n"+Fmt.fit(""+target.maxMana(), 6)+"}M] }mBash Armor    }M[}n"+Fmt.rfit(""+target.maxArmBash(), 5)+"}M]");
		c.sendln("}mLevel        }M[      }n"+Fmt.rfit(""+target.level, 3)+"}M] }mEnergy  }M[}n"+Fmt.rfit(""+target.energy, 6)+"}M/}n"+Fmt.fit(""+target.maxEnergy(), 6)+"}M] }mPierce Armor  }M[}n"+Fmt.rfit(""+target.maxArmPierce(), 5)+"}M]");
		c.sendln("}mExp to Level }M[  }n"+(target.level == Flags.maxPlayableLevel ? Fmt.rfit("n/a", 7) : Fmt.rfit(""+target.tnl, 7))+"}M] }mStr     }M[}n"+Fmt.rfit(""+target.maxStr(), 3)+"}M | }n"+Fmt.rfit(""+target.baseStr+" }NBase", 7)+"}M] }mFrost Resist   }M[}n"+Fmt.rfit(""+target.maxResFrost(), 4)+"}M]");
		
		int hoursPlayed = 0;
		if (!target.conn.isDummy)
		{
			long totalSecondsPlayed = (int)Math.floor(target.conn.timePlayed/1000);
			hoursPlayed = (int)Math.floor(totalSecondsPlayed/3600);
		}
		
		c.sendln("}mHours Played }M[   }n"+Fmt.rfit(""+hoursPlayed, 6)+"}M] }mDex     }M[}n"+Fmt.rfit(""+target.maxDex(), 3)+"}M | }n"+Fmt.rfit(""+target.baseDex, 2)+" }NBase}M] }mFire Resist    }M[}n"+Fmt.rfit(""+target.maxResFire(), 4)+"}M]");
		
		double hpl = (1.0*hoursPlayed)/target.level;
		int hplDec = (int)Math.round((hpl-Math.floor(hpl))*100);
		int hplInt = (int)Math.floor(hpl);

		c.sendln("}mHours/Level  }M[  }n"+Fmt.rfit(hplInt+"."+hplDec, 7)+"}M] }mCon     }M[}n"+Fmt.rfit(""+target.maxCon(), 3)+"}M | }n"+Fmt.rfit(""+target.baseCon, 2)+" }NBase}M] }mLightning Res. }M[}n"+Fmt.rfit(""+target.maxResLightning(), 4)+"}M]");
		
		ArrayList<ObjData> inv = new ArrayList<ObjData>();
		for (ObjData o : target.objects)
			if (o.wearloc.equals("none"))
				inv.add(o);
		double invCap = ObjData.capCount(inv.toArray(new ObjData[0]));
		int invDec = (int)Math.round((invCap-Math.floor(invCap))*100);
		int invInt = (int)Math.floor(invCap);
		
		c.sendln("}mInventory    }M[ }n"+Fmt.rfit(invInt+"."+invDec+"/20", 8)+"}M] }mInt     }M[}n"+Fmt.rfit(""+target.maxInt(), 3)+"}M | }n"+Fmt.rfit(""+target.baseInt, 2)+"}N Base}M] }mAcid Resist    }M[}n"+Fmt.rfit(""+target.maxResAcid(), 4)+"}M]");
		c.sendln("}mGold On Hand }M[}n"+Fmt.rfit(""+target.gold, 9)+"}M] }mCha     }M[}n"+Fmt.rfit(""+target.maxCha(), 3)+"}M | }n"+Fmt.rfit(""+target.baseCha, 2)+"}N Base}M] }mGood Resist    }M[}n"+Fmt.rfit(""+target.maxResGood(), 4)+"}M]");
		c.sendln("}mGold In Bank }M[}n"+Fmt.rfit(""+target.bank, 9)+"}M] }mAlign  }M[}n"+Fmt.rfit(""+target.align, 5)+"}M|}n  #####}M)] }mEvil Resist    }M[}n"+Fmt.rfit(""+target.maxResEvil(), 4)+"}M]");
		c.sendln("}M"+c.repeat("-", 70)+"{x");
		c.sendln("}mMelee Speed }M[}n"+Fmt.rfit(""+target.getStatMod("meleespeed"), 3)+" }M(}n"+Fmt.rhfit(""+Formulas.attacksPerRound(target), 4)+"}M/}nround}M)]  }mCast Speed  }M[}n"+Fmt.rfit(""+target.getStatMod("castspeed"), 3)+" }M(}n"+Fmt.rhfit(""+Formulas.castSpeedMod(target)*100, 5)+"% normal time}M)]");
		c.sendln("}mMelee Hit   }M[}n"+Fmt.rfit(""+target.getStatMod("meleeaccuracy"), 3)+" }M(}n"+Fmt.rhfit(""+(target.getStatMod("meleeaccuracy")*100/(target.level*5))+"%", 4)+" }nbonus}M)]  }mCast Hit    }M[}n"+Fmt.rfit(""+target.getStatMod("castaccuracy"), 3)+" }M(}n"+Fmt.rhfit(""+(target.getStatMod("castaccuracy")*100/(target.level*5))+"%", 4)+" bonus}M)        ]");
		c.sendln("}M"+c.repeat("-", 70)+"{x");
	}
	
	public static void doSscore(UserCon c, String args)
	{
		String tempPerms = "";
		for (String s : c.permissions)
			tempPerms = tempPerms+" "+s;
		if (tempPerms.length() == 0)
			tempPerms = " none";

		String tempGrant = "";
		for (String s : c.granted)
			tempGrant = tempGrant+" "+s;
		if (tempGrant.length() == 0)
			tempGrant = " none";

		ArrayList<String> logs = new ArrayList<String>();
		for (String s : Flags.staffLogs)
			if (c.staffLogs.contains(s))
				logs.add(Fmt.fit(Fmt.cap(s), 15)+"}M: {Gon");
			else
				logs.add(Fmt.fit(Fmt.cap(s), 15)+"}M: {Roff");

		c.sendln(Fmt.heading("Staff Preferences and Settings"));
		if (c.poofin.length() > 0)
			c.sendln("}m          Poofin}M: }n"+c.poofin);
		else
			c.sendln("}m          Poofin}M: }nNot configured.");
		if (c.poofout.length() > 0)
			c.sendln("}m         Poofout}M: }n"+c.poofout);
		else
			c.sendln("}m         Poofout}M: }nNot configured.");
		c.sendln("}m      Staff Role}M: }n"+c.role);
		c.sendln("}m     Permissions}M:}n"+tempPerms);
		c.sendln("}mGranted Commands}M:}n"+tempGrant);
		c.sendln(Fmt.heading("Staff Log Channels"));
		c.sendln(Fmt.defaultTextColumns(logs.toArray(new String[0])));
	}
	
	public static void doLevel(UserCon c, String args)
	{
		c.sendln("   }mLevel }M: }mXP for this level    XP needed for level");
		c.sendln(Fmt.heading(""));
		int total = c.ch.tnl;
		for (int ctr = c.ch.level; ctr <= c.ch.level+10 && ctr < Flags.maxPlayableLevel; ctr++)
		{
			c.sendln("     }n"+Fmt.rfit(""+(ctr+1), 3)+" }M: }n"+Fmt.center(""+(Formulas.mxp(ctr)*Formulas.mpl(ctr)), 17)+"    "+Fmt.center(""+total, 19)+"{x");
			total += (Formulas.mxp(ctr)*Formulas.mpl(ctr));
		}
	}
	
	/**
	Display a list of all areas in the game.

	@param c The user who entered this command.
	@param args A string containing any arguments to the command.
	*/
	public static void doAreas(UserCon c, String args)
	{
		ArrayList<String> ast = new ArrayList<String>();
		c.sendln("The following areas have been built in the game:");
		if (!c.hasPermission("builder") && !c.hasPermission("staff"))
			c.sendln("}m Lvl Range  }NArea Name");
		c.sendln(Fmt.heading(""));
		Collections.sort(areas);
		for (Area a : areas)
			if ((!a.flags.get("hidden") && !a.flags.get("closed")) || c.hasPermission("builder") || c.hasPermission("staff"))
			{
				if (c.hasPermission("builder") || c.hasPermission("staff"))
					c.sendln("}m#}n"+Fmt.fit(""+a.id+"}M:}n ", 4)+Fmt.rfit(""+a.minLevel, 3)+" }M- }n"+Fmt.rfit(""+a.maxLevel, 3)+"  }N"+a.name+" }M(}mIDs }n"+a.start+" }M- }n"+a.end+"}M){x");
				else
				{
					if (a.minLevel == Flags.minLevel && a.maxLevel == Flags.maxPlayableLevel)
						c.sendln("}n Any Level  }N"+a.name+"{x");
					else
						c.sendln("}n "+Fmt.rfit(""+a.minLevel, 3)+" }M- }n"+Fmt.rfit(""+a.maxLevel, 3)+"  }N"+a.name+"{x");
				}
			}
		c.sendln(Fmt.heading(""));
	}
	
	/**
	Display a list of all socials in the game.

	@param c The user who entered this command.
	@param args A string containing any arguments to the command.
	*/
	public static void doSocials(UserCon c, String args)
	{
		ArrayList<String> ast = new ArrayList<String>();
		c.sendln("The following socials are available:");
		for (Social s : socials)
			ast.add(s.name);
		c.sendln(Fmt.defaultTextColumns(ast.toArray(new String[0])));
	}

	/**
	Log a bug reported by a user.

	@param c The user who entered this command.
	@param args A string containing any arguments to the command.
	*/
	public static void doBug(UserCon c, String args)
	{
		if (args.length() == 0)
		{
			c.sendln("You must provide a short description when submitting a bug report.");
			c.sendln("Syntax: }H'}hbug }H<}hmessage}H>'{x");
			return;
		}
		
		FileOutputStream out;
		PrintStream p;
		try
		{
			out = new FileOutputStream("logs/bugs.txt");
			p = new PrintStream(out);
			p.println(frmt.format(System.currentTimeMillis())+" [Room "+c.ch.currentRoom.id+"] "+c.ch.shortName+": "+args);
			p.close();
		} catch (Exception e) {
			sysLog("bugs", "Error in doBug: "+e.getMessage());
			logException(e);
		}
		c.sendln("Thank you for reporting a bug. We'll look into it!");
	}

	/**
	Log a typo reported by a user.
	
	@param c The user who entered this command.
	@param args A string containing any arguments to the command.
	*/
	public static void doTypo(UserCon c, String args)
	{
		if (args.length() == 0)
		{
			c.sendln("You must provide a short description when submitting a typo report.");
			c.sendln("Syntax: }H'}htypo }H<}hmessage}H>'{x");
			return;
		}
		
		FileOutputStream out;
		PrintStream p;
		try
		{
			out = new FileOutputStream("logs/typos.txt");
			p = new PrintStream(out);
			p.println(frmt.format(System.currentTimeMillis())+" [Room "+c.ch.currentRoom.id+"] "+c.ch.shortName+": "+args);
			p.close();
		} catch (Exception e) {
			sysLog("bugs", "Error in doTypo: "+e.getMessage());
			logException(e);
		}
		c.sendln("Thank you for reporting a typo. We'll look into it!");
	}
	
	public static void doBoards(UserCon c, String args)
	{
		c.sendln("}M[}m    board name}M] [}munread}M] [}m"+Fmt.fit("Description", 47)+"}M]");
		c.sendln(Fmt.heading(""));
		for (Board b : boards)
		{
			if (b.name.equals("penalties") && !c.hasPermission("staff"))
				continue;
			ArrayList<BoardMessage> readable = b.readable(c, true);
			c.sendln("}M[}m"+Fmt.rfit(b.name, 14)+"}M] [}n"+Fmt.rfit(""+readable.size(), 6)+"}M] [}m"+Fmt.fit(b.description, 47)+"}M]{x");
		}
	}
	
	public static void doMessage(UserCon c, String args)
	{
		String arg1 = CommandHandler.getArg(args, 1).toLowerCase();
		String arg2 = CommandHandler.getLastArg(args, 2);
		
		if (arg1.length() == 0 || "show".startsWith(arg1))
		{
			c.sendln("Your current message:");
			c.writing.print(c);
			return;
		}

		if ("to".startsWith(arg1))
		{
			if (arg2.length() == 0)
			{
				c.sendln("Write a message to who?");
				return;
			}
			arg2 = arg2.replace(",", "");

			String tempTo[] = arg2.split(" ");
			for (int ctr = 0; ctr < tempTo.length; ctr++)
				if (tempTo[ctr].length() > 1)
					tempTo[ctr] = tempTo[ctr].substring(0, 1).toUpperCase()+tempTo[ctr].substring(1).toLowerCase();
				else
					tempTo[ctr] = tempTo[ctr].toUpperCase();

			arg2 = "";
			for (String s : tempTo)
				arg2 = arg2+s+" ";
			arg2 = arg2.trim();

			c.writing.to = arg2;
			c.sendln("Message addressee(s) set.");
			return;
		}
		
		if ("title".startsWith(arg1) || "subject".startsWith(arg1))
		{
			if (arg2.length() == 0)
			{
				c.sendln("Set the message title to what?");
				return;
			}
			c.writing.title = arg2;
			c.sendln("Message title set.");
			return;
		}
		
		if ("text".startsWith(arg1) || "write".startsWith(arg1))
		{
			c.sendln("Editing message text:");
			c.editMode("NoteText", "", c.writing.text);
			return;
		}
		
		if ("post".startsWith(arg1))
		{
			if (c.writing.to.length() == 0)
			{
				c.sendln("Your message isn't addressed to anyone yet. Use 'message to <recipient(s)>'.");
				return;
			}
			if (c.writing.title.length() == 0)
			{
				c.sendln("Your message has no title. Use 'message title <title>'.");
				return;
			}
			if (c.writing.text.length() == 0)
			{
				c.sendln("Your message has no text. Use 'message text' to edit it.");
				return;
			}
			if (arg2.length() == 0)
			{
				c.sendln("Post the message to which board? Use 'boards' to see a list.");
				return;
			}
				
			for (Board b : boards)
			{
				if (b.name.startsWith(arg2.toLowerCase()))
				{
					if (b.name.equals("penalties") && !c.hasPermission("staff"))
						continue;
					if (b.name.equals("news") && !c.hasPermission("staff"))
					{
						c.sendln("Players can't post news messages.");
						return;
					}
					if (b.name.equals("changes") && !c.hasPermission("staff"))
					{
						c.sendln("Players can't post change messages.");
						return;
					}
					boardMessages.add(c.writing);
					c.writing.postedTo = b;
					c.writing.save();
					c.sendln("Message posted to the "+b.name+" board.");
					for (UserCon cs : conns)
					{
						if (cs != c)
							if (c.writing.canRead(cs))
								cs.sendln("}n"+Fmt.cap(Fmt.seeName(cs.ch, c.ch))+" }mhas posted a new message on the }n"+b.name+" }mboard: '}N"+c.writing.title+"}m'{x");
					}
					c.writing = new BoardMessage(c.ch.shortName);
					
					if (b.readable(c, true).size() == 1)
						c.lastRead.put(b, System.currentTimeMillis()/1000);
					return;
				}
			}
			c.sendln("That's not a valid board name. Use 'boards' to see a list.");
			return;
		}
		
		InfoCommands.doHelp(c, "message");
	}
	public static void prNoteText(UserCon c, String finishedText)
	{
		c.writing.text = finishedText;
		c.sendln("Note text set.");
		c.clearEditMode();
	}
	
	public static void doNotes(UserCon c, String args)
	{
		Board.boardCommand(c, args, "notes");
	}

	public static void doSupplications(UserCon c, String args)
	{
		Board.boardCommand(c, args, "supplications");
	}

	public static void doCovenants(UserCon c, String args)
	{
		Board.boardCommand(c, args, "covenants");
	}

	public static void doArts(UserCon c, String args)
	{
		Board.boardCommand(c, args, "arts");
	}

	public static void doRealm(UserCon c, String args)
	{
		Board.boardCommand(c, args, "realm");
	}

	public static void doClan(UserCon c, String args)
	{
		Board.boardCommand(c, args, "clan");
	}

	public static void doNews(UserCon c, String args)
	{
		Board.boardCommand(c, args, "news");
	}

	public static void doChanges(UserCon c, String args)
	{
		Board.boardCommand(c, args, "changes");
	}

	public static void doPenalties(UserCon c, String args)
	{
		Board.boardCommand(c, args, "penalties");
	}
	
	public static void doTime(UserCon c, String args)
	{
		c.sendln(Fmt.heading("Server Time"));
		c.sendln(Fmt.center("}mThe system time is }n"+longFrmt.format(System.currentTimeMillis())+" }M(}mCST}M)"));
		if (c.timeAdj == 0)
			c.sendln(Fmt.center("}mUse }H'}hpreference time }H<}imodifier}H>'}m to set your timezone adjustment."));
		else
			c.sendln(Fmt.center("}mYour local time is }n"+longFrmt.format(System.currentTimeMillis()+c.timeAdj*3600000)));
		c.sendln("^/"+Fmt.heading("Game Time"));
		c.sendln(Fmt.center("}mIt is }n"+Fmt.getSeasonName(Fmt.getSeason())+" }mof the year }n"+Fmt.getYear()+"}m."));
		c.sendln(Fmt.center("}mIt is the }n"+Fmt.nth(Fmt.getDay())+" }mday of the month of }n"+Fmt.getMonthName(Fmt.getMonth())+"}m. }M(}mWeek }n"+Fmt.getWeek()+"}M)"));
		c.sendln(Fmt.center("}mIt is the }n"+Fmt.nth(Fmt.getHour())+" }mhour of the day.{x"));
	}
	
	public static void doWeather(UserCon c, String args)
	{
		if (c.ch.position.equals("sleeping"))
		{
			c.sendln("You can't do that when you're "+c.ch.position+".");
			return;
		}

		if (c.ch.currentArea() == null)
		{
			c.sendln("You're not in an area - you can't check the weather.");
			return;
		}

		if (c.ch.currentRoom.sector.equals("cavern")
			|| c.ch.currentRoom.sector.equals("indoors"))
		{
			c.sendln("You can't see the weather from here.");
			return;
		}

		Area cArea = c.ch.currentArea();
		c.sendln("As you examine the weather, you notice that...");

		if (cArea.weatherTemp < -15)
			c.sendln("{BIt is unbearably cold, and ice is forming on your clothes.");
		else if (cArea.weatherTemp < 0)
			c.sendln("{CIt's very cold. Your breath seems to crystalize instantly.");
		else if (cArea.weatherTemp < 15)
			c.sendln("{CYour surroundings are covered in frost and snow.");
		else if (cArea.weatherTemp < 30)
			c.sendln("{cIt's rather cold.");
		else if (cArea.weatherTemp < 45)
			c.sendln("{cIt seems to be a bit cool.");
		else if (cArea.weatherTemp < 60)
			c.sendln("{gThe temperature feels just right.");
		else if (cArea.weatherTemp < 75)
			c.sendln("{yIt seems to be a bit warm.");
		else if (cArea.weatherTemp < 90)
			c.sendln("{rIt's rather hot.");
		else if (cArea.weatherTemp < 105)
			c.sendln("{RIt is extremely hot.");
		else
			c.sendln("{RThe unbearable heat causes the air around you to shimmer.");
		
		switch (cArea.weatherCloud)
		{
			case 0:
				c.sendln("{wThe sky is completely clear and free of clouds.");
				break;
			case 1:
				c.sendln("{wA few small clouds drift through the sky.");
				break;
			case 2:
				c.sendln("{WA wispy layer of soft clouds covers the sky.");
				break;
			case 3:
				c.sendln("{WThere is a blanket of grey clouds above you.");
				break;
			case 4:
				c.sendln("{wA heavy layer of dark clouds looms above.");
				break;
			case 5:
				c.sendln("{DThe sky churns with black, swirling storm clouds.");
				break;
		}
		
		if (cArea.weatherTemp < 30)
		{
			switch (cArea.weatherPrecip)
			{
				case 0:
					c.sendln("{wIt's cold enough to snow, but there is no precipitation.");
					break;
				case 1:
				case 2:
					c.sendln("{WA light snowfall drifts down from the heavens.");
					break;
				case 3:
				case 4:
					c.sendln("{WCountless heavy snowflakes cover your surroundings in white.");
					break;
				case 5:
					c.sendln("{CThe snow is creating such a blizzard you can barely see.");
					break;
			}
		}
		else
		{
			switch (cArea.weatherPrecip)
			{
				case 0:
					c.sendln("{wThere are no signs of rain.");
					break;
				case 1:
					c.sendln("{gA light, misting rain falls here.");
					break;
				case 2:
					c.sendln("{gA light rain patters on the ground around you.");
					break;
				case 3:
					c.sendln("{GA steady rain falls from above.");
					break;
				case 4:
					c.sendln("{BThe heavy rain soaks everything with large raindrops.");
					break;
				case 5:
					c.sendln("{BThe rain is falling in a torrential downpour.");
					break;
			}
		}

		switch (cArea.weatherWind)
		{
			case 0:
				c.sendln("{wThe air is completely still.{x");
				break;
			case 1:
				c.sendln("{wA slow breeze brushes your skin.{x");
				break;
			case 2:
				c.sendln("{wA steady breeze blows from the west.{x");
				break;
			case 3:
				c.sendln("{WA steady wind blows through.{x");
				break;
			case 4:
				c.sendln("{WGusts of wind rush by from the west.{x");
				break;
			case 5:
				c.sendln("{WA brutally strong wind howls, almost knocking you over.{x");
				break;
		}
	}
	
	public static void doSkills(UserCon c, String args)
	{
		CharClass targetClass = CharClass.lookup(args);
		if (args.length() == 0)
			targetClass = c.ch.charClass;
		if (targetClass == null)
		{
			c.sendln("That's not a valid class name. The full class name must be used.");
			c.sendln("Syntax: }H'}hskills }H<}iclass}H>'{x");
			return;
		}
		
		c.sendln("The following skills are available to the "+targetClass.name+" class:");
		for (int ctr = 1; ctr <= 100; ctr++)
		{
			ArrayList<String> temp = new ArrayList<String>();
			for (Skill s : skills)
				if (s.type.equals("skill"))
					if (s.availAt(targetClass) == ctr)
					{
						if (c.ch.charClass != targetClass || ctr > c.ch.level)
							temp.add(s.name);
						else if (c.ch.skillPercent(s) == 0)
							temp.add("{Y"+s.name);
						else
							temp.add("{G"+s.name);
					}
			if (temp.size() > 0)
			{
				boolean printed = false;
				for (String s : Fmt.textColumns(temp.toArray(new String[0]), 3, 72, " }M[}m ", " }M]{x").split("\\^\\/"))
				{
					String tempOutput = "    ";
					if (!printed)
						tempOutput = "}n"+Fmt.rfit(""+ctr, 3)+"}M:";
					c.sendln(tempOutput+s);
					printed = true;
				}

			}
		}
	}

	public static void doSpells(UserCon c, String args)
	{
		CharClass targetClass = CharClass.lookup(args);
		if (args.length() == 0)
			targetClass = c.ch.charClass;
		if (targetClass == null)
		{
			c.sendln("That's not a valid class name. The full class name must be used.");
			c.sendln("Syntax: }H'}hspells }H<}iclass}H>'{x");
			return;
		}
		
		c.sendln("The following spells are available to the "+targetClass.name+" class:");
		for (int ctr = 1; ctr <= 100; ctr++)
		{
			ArrayList<String> temp = new ArrayList<String>();
			for (Skill s : skills)
				if (s.type.equals("spell"))
					if (s.availAt(targetClass) == ctr)
					{
						if (c.ch.charClass != targetClass || ctr > c.ch.level)
							temp.add(s.name);
						else if (c.ch.skillPercent(s) == 0)
							temp.add("{Y"+s.name);
						else
							temp.add("{G"+s.name);
					}
			if (temp.size() > 0)
			{
				boolean printed = false;
				for (String s : Fmt.textColumns(temp.toArray(new String[0]), 3, 72, " }M[}m ", " }M]{x").split("\\^\\/"))
				{
					String tempOutput = "    ";
					if (!printed)
						tempOutput = "}n"+Fmt.rfit(""+ctr, 3)+"}M:";
					c.sendln(tempOutput+s);
					printed = true;
				}

			}
		}
	}

	public static void doPractice(UserCon c, String args)
	{
		ArrayList<String> temp = new ArrayList<String>();
		int total = 0;
		int mastered = 0;
		c.sendln(Fmt.heading("Skills"));
		for (Skill s : skills)
			if (s.type.equals("skill"))
				if (c.ch.learned.get(s) != null)
				{
					total++;
					String cc = "{R";
					int pct = c.ch.learned.get(s);
					if (pct == 100)
					{
						cc = "{G";
						mastered++;
					}
					else if (pct > 75)
						cc = "{Y";
					temp.add("}N"+Fmt.fit(s.name, 18)+" "+cc+Fmt.rfit(""+c.ch.learned.get(s), 3)+"%");
				}
		c.sendln(Fmt.textColumns(temp.toArray(new String[0]), 3, 75, " ", " "));

		temp = new ArrayList<String>();
		c.sendln(Fmt.heading("Spells"));
		for (Skill s : skills)
			if (s.type.equals("spell"))
				if (c.ch.learned.get(s) != null)
				{
					total++;
					String cc = "{R";
					int pct = c.ch.learned.get(s);
					if (pct == 100)
					{
						cc = "{G";
						mastered++;
					}
					else if (pct > 75)
						cc = "{Y";
					temp.add("}N"+Fmt.fit(s.name, 18)+" "+cc+Fmt.rfit(""+c.ch.learned.get(s), 3)+"%");
				}
		c.sendln(Fmt.textColumns(temp.toArray(new String[0]), 3, 75, " ", " "));
		c.sendln(Fmt.heading(""));
		c.sendln(Fmt.center("}N"+total+" known abilities  /  "+mastered+" mastered{x"));
	}
}