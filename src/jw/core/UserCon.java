package jw.core;
import java.io.*;
import java.nio.*;
import java.nio.charset.*;
import java.net.*;
import java.util.*;

import jw.commands.*;
import jw.data.*;
import static jw.core.MudMain.*;


/**
	The UserCon class stores information about each connection to the MUD, and
	contains all lower-level I/O functions.
*/
public class UserCon implements Comparable<UserCon>
{
	// Connection Information.
	/** Input buffer from client. */
	private BufferedReader inFromClient;
	/** Output stream for this connection. */
	private OutputStream outToClient;
	/** The socket used by this connection. */
	private Socket connSocket;
	/** For initial connection - dump bogus chars on the first read. */
	public boolean hasReadOnce = false;
	/** For dummy connections - for NPCs. */
	public boolean isDummy = false;
	/** Real character - for switching. */
	public CharData realCh = null;
	/** Snoop target. */
	public UserCon snoop = null;
	/** AFK? */
	public boolean afk = false;
	/** In quiet mode? */
	public boolean quiet = false;
	
	// Buffers and I/O Information.
	/** "Buffered" commands received. */
	public ArrayList<String> commandQueue = new ArrayList<String>();
	/** Last command received (for "!"). */
	public String lastCommand;
	/** Prompt text for special prompts. */
	public String tempPrompt = "";
	/** Information for the prompt method. */
	public String promptTarget = "";
	/** The method to return to after the prompt. */
	public String promptType = "";
	/** The contents of the user's text editor. */
	public ArrayList<String> editorContents;
	/** The line currently being edited. */
	public int insertAt = -1;
	/** Show prompt during this refresh? */
	public boolean showPrompt = false;
	/** Has the user received text during this refresh? */
	public boolean hasPrinted = false;
	/** Is the user in page mode? */
	public boolean pageMode = false;
	/** The number of lines printed in a row (for paging). */
	public int linesPrinted = 0;
	/** The contents of the paging buffer. */
	public String pageBuffer = "";
	/** A prefix command to add to input if doing OLC. */
	public String olcMode = "";
	/** Did the user's prefixed OLC command do something? */
	public boolean olcMatched = false;
	/** Has the user logged in successfully? */
	public boolean saveable = false;
	
	// Other Information.
	/** The user's current connection state. */
	public ConnState cs;
	/** The previous connection state (for editing) */
	public ConnState lastCs;
	/** The user's ID in the database. */
	public int id;
	/** The character data associated with this connection. */
	public CharData ch;
	/** Permissions available to the user. */
	public ArrayList<String> permissions = new ArrayList<String>();
	/** Specific commands granted to the user. */
	public ArrayList<String> granted = new ArrayList<String>();
	/** Specific commands revoked from the user. */
	public ArrayList<String> revoked = new ArrayList<String>();
	/** Other players this user is ignoring. */
	public ArrayList<String> ignoring = new ArrayList<String>();
	/** The user's role (for staff) */
	public String role = "";
	/** Custom aliases. Even indices are aliases, odd indices are their commands. */
	public ArrayList<String> aliases = new ArrayList<String>();
	/** Custom color settings for the user. */
	public ArrayList<String> colors = new ArrayList<String>();
	/** The list of channels which the user has turned off. */
	public ArrayList<String> chansOff = new ArrayList<String>();
	/** Total time played in milliseconds. */
	public long timePlayed;
	/** The Unix timecode of the last connection. */
	public long lastConnected = System.currentTimeMillis()/1000;
	/** The Unix timecode of when the account was created. */
	public long joined = System.currentTimeMillis()/1000;
	/** The list of times the user last read various boards. */
	public HashMap<Board, Long> lastRead = new HashMap<Board, Long>();
	/** The user's reply list. */
	public ArrayList<CharData> replyList = new ArrayList<CharData>();
	/** The user's saved tells. */
	public ArrayList<String> savedTells = new ArrayList<String>();
	/** The number of updates performed since the user entered a command. */
	public int lastInput = 0;
	/** The number of updates performed since the user received input. */
	public int lastOutput = 0;
	/** The lag before the next command can be accepted from the user. */
	public int delay = 0;
	/** The number of chats the user sent over the threshold. */
	public int chatCount = 0;
	/** The user's delay on chatting. */
	public int chatDelay = 0;
	/** The time in updates since the user received a prompt. */
	public int lastPrompt = 5;
	/** A message in progress. */
	public BoardMessage writing;
	/** Staff invisibility. */
	public boolean invis = false;
	/** Staff incognito. */
	public boolean incog = false;
	/** Most recent hostname. */
	public String host1 = "";
	/** Previous hostname. */
	public String host2 = "";
	/** First hostname. */
	public String host3 = "";

	// User Preferences.
	/** The user's "previous mudding experience" setting. */
	public int prevExp = 0;
	/** The user's custom prompt string. */
	public String prompt;
	/** The user's custom battle prompt string. */
	public String bprompt;
	/** The user's real name. */
	public String realname;
	/** The user's e-mail address. */
	public String email;
	/** The length of the user's page (lines until pagemode turns on). */
	public int pageLength;
	/** The time adjustment from CST. */
	public int timeAdj = 0;
	/** The 'troubled' setting for troublemakers. */
	public boolean troubled = false;
	/** Is the user being logged? */
	public boolean logged = false;
	/** Boolean-style preferences. */
	public HashMap<String, Boolean> prefs = new HashMap<String, Boolean>();
	/** Logs the user is listening to. */
	public ArrayList<String> staffLogs = new ArrayList<String>();
	/** Text which will trigger a beep when sent to the user. */
	public ArrayList<String> beeps = new ArrayList<String>();
	/** Custom poof-in message. */
	public String poofin = "";
	/** Custom poof-out message. */
	public String poofout = "";
	
	/**
	Create a "dummy" UserCon for mobs to execute commands.
	*/
	public UserCon()
	{
		for (String s : Flags.userPrefs)
			prefs.put(s, false);
		cs = ConnState.PLAYING;
		isDummy = true;
		ch = null;
	}
	
	/**
	Set up a new connection given a new socket.
	<p>
	This also handles new account creation and initial setting of options.
	
	@param newSocket The socket used to establish this connection.
	*/
	public UserCon(Socket newSocket)
	{
		connSocket = newSocket;
		try {
			inFromClient = new BufferedReader(new InputStreamReader(connSocket.getInputStream()));
			outToClient = connSocket.getOutputStream();
			cs = ConnState.LOGIN;
			pageLength = 20;
			prompt = "{W[{r%h{R/{r%H {Rhp{W] [{c%m{C/{c%M {Cmn{W] [{g%s{G/{g%S {Gst{W] [{cEx{C: {c%e{W] %l{x^/";
			bprompt = "{W[{r%h{R/{r%H {Rhp{W] [{c%m{C/{c%M {Cmn{W] [{g%s{G/{g%S {Gst{W] [{cEx{C: {c%e{W] %l^/{wFighting %O{W: {R%P%% {C%N%% {G%I%% {w--> {x";
			for (String s : Flags.userPrefs)
				prefs.put(s, false);
			prefs.put("prompt", true);
			for (Board b : boards)
				lastRead.put(b, (long)0);
			String tempName = "";
			boolean askForName = false;
			boolean createdNewChar = false;

			sendln("^/"
				+"   AAAAAA  AAAA  GGG      GGG    AAAAAA  AAAA     PPPPPPPPPPP     EEEEEEEEE^/"
				+" AAA   AAAAAA    GGGG     GGGG  AAA   AAAAAA     PPPPPPPPPPPPP   E EEEEE  EEEE^/" 
				+"AAA     AAAA        GG,  GGGG  AAA     AAAA         PP   PPP        EEE    EEE^/"
				+"AAA      AAA        GGG  GGG   AAA      AAA         PP   PP         EEE    EEE^/"
				+"AAA      AAAA        GG  GG    AAA      AAAA        PP   PP         EEE    EEE^/"
				+"AAA      AAAA         G GG     AAA      AAAA        PP   PP         EEE    EEE^/"
				+"AAA      AAAA         GGGG     AAA      AAAA       PPP   PPP        EEE    EEE^/"
				+" AAA    AAAAAAAA      GGG       AAA    AAAAAAAA    PP    PPP.P      EEE    EEE^/"
				+"  AAAAAAA  AAAAA      GG         AAAAAAA  AAAAA  .PP      PPP       EEE    EEE^/"
				+"                      GG                                                    EE^/"
				+"                      GG                       Agape:                       EE^/"
				+"                     GGG                  The New Covenant                  EEE^/"
				+"^/");
			do {
				askForName = false;
				tempName = prompt("Enter your name, or type 'new' to create a new character.");
				if (tempName.trim().length() < 1)
				{
					askForName = true;
					continue;
				}
				
				if (tempName.equalsIgnoreCase("new") || !Database.nameTaken(tempName))
				{
					if (Moban.hasMatch("ban", "", connSocket.getInetAddress().getHostName()))
					{
						sendln("This location has been banned from connecting to Agape. If you have");
						sendln("never connected from this location before, please visit our website");
						sendln("at www.agapemud.org and <insert instructions here>.");
						sysLog("justice", "Connection from banned site: "+connSocket.getInetAddress().getHostName());
						closeSocket();
						return;
					}
					for (UserCon cs : conns)
					{
						if (cs.hasPermission("staff"))
							continue;
						if (cs.host1.equals(connSocket.getInetAddress().getHostName())
							&& !Moban.hasMatch("multok", cs.ch.name, cs.host1)
							&& !cs.hasPermission("staff"))
						{
							sendln("Another character is already logged in from the same connection you're using.");
							sendln("If you were playing on a character but were disconnected or did not quit using");
							sendln("the 'quit' command, please log back on to that character and 'quit' before");
							sendln("logging on with a different character.");
							sendln("");
							sendln("Note that multiplaying (using multiple characters at the same time) is not");
							sendln("permitted on Agape. If there is more than one person at your connection who");
							sendln("would like the play the game, please speak with a staff member or send an");
							sendln("e-mail to staff@agapemud.org including your character's name.");
							closeSocket();
							return;
						}
					}
	
					// Ask for the character's new name until they enter a valid one.
					chansOff.add("ministry");
					chansOff.add("vulgar");
					String newName = "";
					Race newCharRace = null;
					CharClass newCharClass = null;
					String newSex = "";
					boolean validName = false;
					
					if (!tempName.equalsIgnoreCase("new"))
					{
						String makeNewChar = "";
						do {
							if (makeNewChar.length() > 0)
								sendln("Please enter 'y' or 'n'.");
		
							makeNewChar = prompt("^/That name isn't familiar. Would you like to create a new character? (y/n)").toLowerCase();
						} while (!makeNewChar.equals("y") && !makeNewChar.equals("n"));
						if (makeNewChar.equals("n"))
						{
							askForName = true;
							continue;
						}
	
						if (!validateName(tempName))
							validName = false;
						else
						{
							newName = tempName;
							validName = true;
						}
					}
					while (!validName)
					{
						newName = prompt("Please choose a name for your new character:").trim();
	
						if (newName.length() == 0)
							continue;
						
						if (!validateName(newName))
							continue;
						break;
					}
					
					newName = newName.substring(0, 1).toUpperCase()+newName.substring(1);
					sysLog("connections", "New character: "+newName+" ("+connSocket.getInetAddress().getHostName()+")");
					sendln("^/^/^/ Welcome to the game, "+newName+"! Before you can begin playing,^/"+
						   " you need to set up your character and basic preferences.^/^/");
	
					// Ask for a password until a valid one is entered.
					String newPassword = "";
					while (true)
					{
						do {
							if (newPassword.length() > 0 && newPassword.length() < 5)
							{
								sendln("Your password must be at least five characters long.");
								sendln("");
							}
							newPassword = prompt("Please choose a password: ", false);
						} while (newPassword.length() < 5);
						String newPassword2 = prompt("Please enter the password again to confirm it: ", false);
						if (newPassword.equals(newPassword2))
							break;
						
						sendln("^/^/The passwords don't match. Note that the password is case-sensitive.^/");
					}
					sendln("^/^/ Your password has been set. No staff member will ever ask for your password.^/"+
							"      Please help us keep the game secure - never share your password!");
					prompt("^/^/Push enter to continue...");
	
					// Ask for a Y/N answer to color mode.
					prefs.put("ansi", true);
					String newAnsi = "";
					sendln("^/           {W**** {GTesting {Bcolor {Cmode{W! {W****{x");
					sendln("  If the above line displays correctly (with colors),"+
						   "^/         your terminal supports color mode.");
					do {
						if (newAnsi.length() > 0)
							sendln("Please enter 'y' or 'n'.");
	
						newAnsi = prompt("^/Would you like to turn color mode on? (y/n)").toLowerCase();
					} while (!newAnsi.equals("y") && !newAnsi.equals("n"));
					if (newAnsi.equals("n"))
						prefs.put("ansi", false);
					
					// Ask for the user's previous MUDding experience.
					boolean askedForPrevExp = false;
					prevExp = 0;
					do {
						if (askedForPrevExp)
							sendln("That's not a valid selection. Please enter }H'}h1}H'{x, }H'}h2}H'{x, }H'}h3}H'{x, or }H'}h4}H'{x.");
						sendln("");
						sendln("Please choose the option that best describes your previous gaming experience.");
						sendln("This selection will determine what kind of tutorials and helpful information");
						sendln("is displayed to you as you learn about the game.");
						sendln("");
						sendln("}H( }h1}H) }ILittle or no experience with role playing games. (World of Warcraft, etc.)");
						sendln("}H( }h2}H) }IFamiliar with RPGs, but little or no experience playing a MUD.");
						sendln("}H( }h3}H) }IFamiliar with MUDs, but new to Agape.");
						sendln("}H( }h4}H) }IFamiliar with Agape.{x");
						prevExp = Fmt.getInt(prompt(""));
						if (prevExp < 1 || prevExp > 4)
							prevExp = 0;
						askedForPrevExp = true;
					} while (prevExp == 0);
					
					// Ask for a sex until a valid one is entered.
					do {
						if (newSex.length() > 0)
							sendln("Please enter }H'}hm}H'{x or }H'}hf}H'{x.");
						sendln("");
						newSex = prompt("Is your new character male or female? }H(}hm}H/}hf}H){x");
					} while (!newSex.equalsIgnoreCase("m") && !newSex.equalsIgnoreCase("f"));
					if (newSex.equalsIgnoreCase("m"))
						newSex = "m";
					else
						newSex = "f";
	
					// Build the class prompt.
					String classPrompt = "^/The following classes are available to you:^/^/";
					classPrompt = classPrompt+" }mCleric }M: }NMany healing spells, solid offensive spells. Some melee skills.^/";
					classPrompt = classPrompt+" }mMage   }M: }NPowerful offensive spellcaster. Very poor melee ability.^/";
					classPrompt = classPrompt+" }mFighter}M: }NA durable, straightforward, and versatile melee combat class.^/";
					classPrompt = classPrompt+" }mRogue  }M: }NLight melee class with many combat moves. Can sneak and use poisons.^/{x";
					classPrompt = classPrompt+"^/Enter the name of your desired class, or }H'}hhelp }H<}iclass}H>'{x for more information.";
					
					sendln("^/^/^/^/^/");
					sendln("While there are many classes in Agape, you'll start by selecting one of");
					sendln("four base classes. This will determine the fundamental set of skills which");
					sendln("your character can use, as well as which higher-level classes you'll have");
					sendln("access to later in the game.");
					sendln("");
					sendln("{W** {RNOTE{x: Each class provides a very different gameplay experience. If you're");
					sendln("         new to MUDs or role playing games, you may want to take a few minutes");
					sendln("         to read about our classes on our website: }hwww.agapemud.org/classes{x");
					// Ask for a class until a valid one is entered.
					while (true)
					{
						String newClass = prompt(classPrompt).toLowerCase().trim();
						
						if (newClass.length() == 0)
							continue;
						if (newClass.startsWith("help") && newClass.split(" ", 2).length > 1)
						{
							InfoCommands.doHelp(this, newClass.split(" ", 2)[1]);
							continue;
						}
						for (CharClass cl : MudMain.classes)
							if (cl.name.toLowerCase().startsWith(newClass) && cl.parent == 0)
								if (prompt("You have selected '"+cl.name+"'. Is this correct? (Y/N)").toLowerCase().startsWith("y"))
								{
									newCharClass = cl;
									break;
								}
								else
									continue;
	
						if (newCharClass != null)
							break;
						sendln("That's not a valid class.");
					}

					// Build the racegroup prompt text.
					String raceGroupPrompt = "";
					String raceGroupsAdded = " ";
					ArrayList<String> rgst = new ArrayList<String>();
					for (Race r : MudMain.races)
						if (r.racegroup.length() > 0 && !rgst.contains(r.racegroup))
							rgst.add(r.racegroup);
					Collections.sort(rgst);
					raceGroupPrompt = "^/The following race groups are available to you:^/^/"+Fmt.defaultTextColumns(rgst.toArray(new String[0]))+"^/^/Enter the name of your desired race group, or }H'}hhelp }H<}irace group}H>'{x for^/more information.";
					
					sendln("^/^/^/^/^/");
					sendln("Agape offers many different races, but these are divided into six groups of");
					sendln("races. You'll choose your race group first, and then choose the specific");
					sendln("race you wish to play as. Your character's race will determine how well you");
					sendln("can use different kinds of abilities. Some races also receive special, unique");
					sendln("abilities beyond those offered by your class.");
					sendln("");
					sendln("{W** {RNOTE{x: It is important to pick a race with traits that match your class well.");
					sendln("         Check the class recommendations section of race help files to find out");
					sendln("         if a race would fit your class well.");
					sendln("         This information is also available at: }hwww.agapemud.org/races{x");
					// Ask for a racegroup until a valid one is entered.
					while (true)
					{
						String newRaceGroup = prompt(raceGroupPrompt).toLowerCase().trim();
						if (newRaceGroup.length() == 0)
							continue;
	
						// Allow the user to view help files from this prompt.
						if (newRaceGroup.startsWith("help") && newRaceGroup.split(" ", 2).length > 1)
						{
							InfoCommands.doHelp(this, newRaceGroup.split(" ", 2)[1]);
							continue;
						}
	
						// Look for a racegroup match. Found one? Then ask for a race.
						String newRace = "";
						for (Race r : MudMain.races)
						{
							if (r.racegroup.toLowerCase().startsWith(newRaceGroup) && rgst.contains(r.racegroup))
							{
								// Build the race prompt for the chosen racegroup.
								ArrayList<String> rst = new ArrayList<String>();
								String racePrompt = "^/The following races are available within the "+r.racegroup+" group:^/^/";
								for (Race r2 : MudMain.races)
									if (r2.racegroup.equals(r.racegroup))
										rst.add(r2.name);
								Collections.sort(rst);
								racePrompt = racePrompt+Fmt.defaultTextColumns(rst.toArray(new String[0]))+"^/^/You may enter }H'}hback}H'{x to choose another racegroup.^/Enter the name of your desired race, or }H'}hhelp }H<}irace}H>'{x for more information.";
	
								while (true)
								{
									newRace = prompt(racePrompt).toLowerCase().trim();
				
									if (newRace.length() == 0)
										continue;
									if (newRace.startsWith("help") && newRace.split(" ", 2).length > 1)
									{
										InfoCommands.doHelp(this, newRace.split(" ", 2)[1]);
										continue;
									}
									if (newRace.equals("back"))
										break;
									for (Race r2 : MudMain.races)
										if (r2.name.toLowerCase().startsWith(newRace) && rst.contains(r2.name))
											if (prompt("You have selected '"+r2.name+"'. Is this correct? (Y/N)").toLowerCase().startsWith("y"))
											{
												newCharRace = r2;
												break;
											}
											else
												continue;
									if (newCharRace != null || newRace.equals("back"))
										break;
									sendln("That's not a valid race.");
								}
							}
							if (newCharRace != null || newRace.equals("back"))
								break;
						}
						if (newCharRace != null)
							break;
						sendln("That's not a valid race group.");
					}
					
					// Get the user's real name.
					realname = prompt("^/What is your real name? To leave it blank, just push enter.");
	
					// Ask for the user's e-mail address.
					email = prompt("^/Your e-mail address can be hidden from other players,^/"+
								   "   and we will not share it with other entities.^/^/"+
								   "What is your e-mail address? To leave it blank, just push enter.");
	
					// Ask for a Y/N answer to e-mail sharing.
					String newShowEmail = "";
					do {
						if (newShowEmail.length() > 0)
							sendln("Please enter }H'}hy}H'{x or }H'}hn}H'{x.");
						newShowEmail = prompt("^/Would you like your e-mail address to be visible to other players? }H(}hy}H/}hn}H){x").toLowerCase();
					} while (!newShowEmail.equals("y") && !newShowEmail.equals("n"));
					if (newShowEmail.equals("y"))
						prefs.put("showemail", true);
					
					prompt("^/To change any of these settings, type }H'}hhelp preference}H'{x.^/Push enter to continue...");
	
					// Create the character, save the account, and reload both to obtain IDs.
					id = Database.newAccount(newPassword);
					host3 = connSocket.getInetAddress().getHostName();
					ch = new CharData(this, true);
					ch.name = newName;
					ch.shortName = newName;
					ch.charRace = newCharRace;
					ch.charClass = newCharClass;
					ch.sex = newSex;
					ch.currentRoom = rooms.get(0);
					ch.initializeValues();
					ch.gold = 100;
					ch.bank = 250;
					ch.save();
					ch = new CharData(this, true);
	
					Database.saveAccount(this);
	
					createdNewChar = true;
				} else
				{
					if (Database.checkLogin(tempName, prompt("Enter your password: ", false)))
					{
						if (Moban.hasMatch("ban", "", connSocket.getInetAddress().getHostName()))
						{
							sendln("This location has been banned from connecting to Agape. If you have");
							sendln("never connected from this location before, please visit our website");
							sendln("at www.agapemud.org and <insert instructions here>.");
							sysLog("connections", "Connection from banned site: "+tempName+" at "+connSocket.getInetAddress().getHostName());
							closeSocket();
							return;
						}
						if (Moban.hasMatch("ban", tempName, ""))
						{
							sendln("Your character has been banned from connecting to Agape. If you would");
							sendln("like to appeal this ban, please visit our website at www.agapemud.org");
							sendln("and <insert instructions here>.");
							sysLog("connections", "Connection from banned character: "+tempName+" at "+connSocket.getInetAddress().getHostName());
							closeSocket();
							return;
						}
	
						Database.loadAccount(this, tempName);
						if (checkDuplicates(tempName))
							return;
					}
					else
					{
						sysLog("connections", "Invalid password: "+tempName+" ("+connSocket.getInetAddress().getHostName()+")");
						sendln("^/Invalid password.^/");
						closeSocket();
						return;
					}
				}
			} while (askForName);
			
			if (ch == null || createdNewChar)
			{
				if (ch == null)
					ch = new CharData(this, true);

				writing = new BoardMessage(ch.shortName);
				sysLog("connections", "Connected: "+ch.shortName+" ("+connSocket.getInetAddress().getHostName()+")");

				sendln("^/");
				InfoCommands.doHelp(this, "motd");
				prompt("^/Push enter to continue...");
				
				InfoCommands.doBoards(this, "");
				prompt("^/Push enter to continue...");
				
				lastPrompt = -40;
				conns.add(this);
				RoomCommands.doLook(this, "");
				if (createdNewChar)
				{
					sendToAllExcept(ch.name+", a new player, has connected.");
					sendln("You enter the game as "+ch.name+", a young adventurer!");
				} else {
					sendToAllExcept(ch.name+" has connected.");
					sendln("^/{gWelcome to Agape, {Y"+ch.name+"{g!{x");
				}
				lastPrompt = 2;
			}
			else
			{
				sysLog("connections", "Reconnected: "+ch.shortName+" ("+connSocket.getInetAddress().getHostName()+")");
				sendln("Reconnecting...");
			}

			ch.save();
			cs = ConnState.PLAYING;
			saveable = true;

			if (!conns.contains(this))
				conns.add(this);

			if (checkDuplicates(tempName))
			{
				conns.remove(this);
				return;
			}

			if (!host1.equals(connSocket.getInetAddress().getHostName()))
			{
				host2 = host1;
				host1 = connSocket.getInetAddress().getHostName();
				Database.saveAccount(this);
			}
		} catch (Exception e) {}
	}
	
	public boolean validateName(String newName)
	{
		if (Database.nameTaken(newName))
		{
			sendln("That name is already in use. Please choose another name.");
			sendln("If you have forgotten your password, contact a staff member.");
			return false;
		}
		if (newName.length() < 3)
		{
			sendln("Your character's name must be at least three letters long.");
			return false;
		}
		if (newName.length() > 18)
		{
			sendln("Your character's name must be less than eighteen letters long.");
			return false;
		}
		
		boolean needNewName = false;
		for (int ctr = 0; ctr < newName.length(); ctr++)
		{
			char c = newName.charAt(ctr);
			if ((c < 'a' || c > 'z') && (c < 'A' || c > 'Z'))
			{
				sendln("Your character's name must contain only alphabetic characters (a-z, A-Z).");
				needNewName = true;
				break;
			}
		}
		for (String in : Flags.illegalNames)
			if (newName.toLowerCase().contains(in))
			{
				sendln("Your character's name may not include the text '"+in+"'.");
				needNewName = true;
				break;
			}
		
		if (needNewName)
			return false;
		return true;
	}
	
	public boolean checkDuplicates(String tempName)
	{
		// Check for duplicate names or hostnames.
		if (!hasPermission("staff")
			&& !Moban.hasMatch("multok", tempName, connSocket.getInetAddress().getHostName()))
			for (UserCon cs : conns)
			{
				if (cs.hasPermission("staff"))
					continue;
				if (cs.host1.equals(connSocket.getInetAddress().getHostName())
					&& !cs.ch.name.equalsIgnoreCase(tempName)
					&& !Moban.hasMatch("multok", cs.ch.name, cs.host1)
					&& !cs.hasPermission("staff"))
				{
					sendln("Another character is already logged in from the same connection you're using.");
					sendln("If you were playing on a character but were disconnected or did not quit using");
					sendln("the 'quit' command, please log back on to that character and 'quit' before");
					sendln("logging on with a different character.");
					sendln("");
					sendln("Note that multiplaying (using multiple characters at the same time) is not");
					sendln("permitted on Agape. If there is more than one person at your connection who");
					sendln("would like the play the game, please speak with a staff member.");
					closeSocket();
					return true;
				}
			}
		for (UserCon cs : conns)
			if (cs.ch.name.equalsIgnoreCase(tempName) && cs != this)
			{
				conns.remove(cs);
				writing = cs.writing;
				ch = cs.ch;
				ch.conn = this;
				cs.sendln("This character has been logged on from a different connection.");
				cs.sendln("If this message appears after no action on your part, you may");
				cs.sendln("wish to change your password to prevent hacking attempts.");
				sysLog("connections", "New connection for "+tempName+": Booting old connection.");
				cs.closeSocket();
				break;
			}
		return false;
	}

	/**
	Is the socket closed?
	
	@return {@code true} if the socket has been closed; {@code false} otherwise.
	*/
	public boolean isClosed()
	{
		return connSocket.isClosed();
	}
	/**
	Force the socket to close from the server-side.
	*/
	public void closeSocket()
	{
		try
		{
			for (CharData chs : replyList)
			{
				chs.conn.replyList.remove(ch);
				chs.sendln("Your reply list has changed.");
			}
			
			if (saveable)
			{
				if (realCh != null)
					StaffCommands.doReturn(this, "");
				
				ch.save();
				Database.saveAccount(this);
			}
			
			connSocket.close();
		} catch (Exception e) {
			sysLog("bugs", "Error in closeSocket: "+e.getMessage());
			logException(e);
		}
	}
	
	public void cleanup()
	{
		for (CharData chs : allChars())
		{
			if (chs.sparring == ch)
			{
				chs.sendln(Fmt.seeNameGlobal(chs, ch)+" has withdrawn from the sparring match.");
				chs.sparring = null;
			}
			if (chs.fighting == ch)
				chs.fighting = null;
			if (chs.hating.contains(ch))
				chs.hating.remove(ch);
		}
		for (Group g : groups)
		{
			if (g.invites.contains(ch))
				g.invites.remove(ch);
			if (g.members.contains(ch))
			{
				g.members.remove(ch);
				for (CharData chs : g.members)
					chs.sendln(Fmt.cap(Fmt.seeNameGlobal(chs, ch))+" has left the group.");
				if (g.leader == ch && g.members.size() > 0)
				{
					g.leader = g.members.get(0);
					g.leader.sendln("You are now the group leader.");
					for (CharData chs : g.members)
						if (chs != g.leader)
							chs.sendln(Fmt.cap(Fmt.seeNameGlobal(chs, g.leader))+" is now the group leader.");
				}
			}
		}
		if (lastOutput != lastInput)
			sysLog("connections", "Dead link closed: "+ch.shortName);
		ch.save();
		conns.remove(this);
	}
	
	/**
	Fetch the next command from the user's command queue and pass it to
	{@link CommandHandler#doCommand(UserCon, String) CommandHandler.doCommand}.
	*/
	public void processCommand()
	{
		if (delay > 0 && !isDummy)
		{
			delay--;
			return;
		}
		delay = 0;
		
		try
		{
			if (commandQueue.size() == 0)
				return;
			
			
			String commandLine = commandQueue.get(0);
			commandQueue.remove(0);
			if (logged && commandLine.length() > 0)
				sysLog("justice", ch.shortName+" @ "+ch.currentRoom.id+": "+commandLine);
			CommandHandler.doCommand(this, commandLine);

		} catch (Exception e) {
			e.printStackTrace();
			if (ch.shortName.length() > 0)
				sysLog("connections", "Socket closed unexpectedly: "+ch.shortName);
			else
				sysLog("connections", "Socket closed unexpectedly: No character.");
			this.closeSocket();
		}
	}
	
	/**
	Input method: Waits for a line of input to be read from the client.
	<p>
	This method is invoked by the thread waiting for input from each socket.
	
	@return The string received from the client.
	*/
	public String read()
	{
		try
		{
			int startAt = 0;
			String tempRead = inFromClient.readLine();
			
			// Clear out any nonalphabetic characters from the input buffer if
			// the user has just connected.
			if (!hasReadOnce)
			{
				for (int ctr = 0; ctr < tempRead.length(); ctr++)
				{
					if ((tempRead.charAt(ctr) >= 'a' && tempRead.charAt(ctr) <= 'z')
						|| (tempRead.charAt(ctr) >= 'A' && tempRead.charAt(ctr) <= 'Z'))
					{
						startAt = ctr;
						break;
					}
				}
				hasReadOnce = true;
			}

			hasPrinted = true;
			tempRead = tempRead.substring(startAt, tempRead.length());
			
			// Never return nonprintable characters. To support windows telnet, go back one
			// character for each backspace (chr #8) encountered.
			String newRead = "";
			for (int ctr = 0; ctr < tempRead.length(); ctr++)
			{
				if (tempRead.charAt(ctr) > 31)
					newRead = newRead+tempRead.charAt(ctr);
				if (tempRead.charAt(ctr) == 8 && newRead.length() > 0)
					newRead = newRead.substring(0, newRead.length()-1);
			}

			return newRead;

		} catch (NullPointerException e) {
			this.closeSocket();
			if (ch.shortName.length() > 0)
				sysLog("connections", "Socket closed unexpectedly: "+ch.shortName);
			else
				sysLog("connections", "Socket closed unexpectedly: No character.");
			return "";
		} catch (SocketException e) {
			return "";
		} catch (Exception e) {
			if (ch.shortName.length() > 0)
				sysLog("connections", "Socket closed unexpectedly: "+ch.shortName);
			else
				sysLog("connections", "Socket closed unexpectedly: No character.");
			return "";
		}
	}
	
	/**
	Basic version of prompt - only to be used in the UserCon constructor,
	or it will put the MUD on pause until input is received.
	
	@param newPrompt The prompt to display to the user.
	@param newLine Should a newline character be printed after the prompt?
	@return The text received by the prompt.
	*/
	public String prompt(String newPrompt, boolean newLine)
	{
		tempPrompt = newPrompt;
		if (newLine)
			sendln(tempPrompt);
		else
			send(tempPrompt);
		String commandLine = read();
		tempPrompt = "";
		return commandLine;
	}
	/**
	Convenience method which defaults to a newLine using {@link #prompt(String, boolean)
	prompt(String, boolean)}.
	
	@param newPrompt The prompt to display to the user.
	@return The text received by the prompt.
	*/
	public String prompt(String newPrompt)
	{
		return prompt(newPrompt, true);
	}

	/**
	Prompt for mid-game requests.
	<p>
	Rather than waiting for input, this version marks the user's next command to
	trigger the appropriate function.
	
	@param newType The name of the prompt method to return to.
	@param newTarget The target (ID, text, etc) to pass to the prompt method.
	@param newPrompt The text to display the user instead of their usual prompt.
	*/
	public void prompt(String newType, String newTarget, String newPrompt)
	{
		promptTarget = newTarget;
		promptType = newType;
		tempPrompt = newPrompt;
		showPrompt = true;
	}
	/**
	Clear any prompt information previously set.
	*/
	public void clearPrompt()
	{
		promptTarget = "";
		promptType = "";
		tempPrompt = "";
		showPrompt = true;
	}

	/**
	Text editor variant of the prompt system. Only a few tweaks here -
	it sets the connstate and sets up the text contents variable.
	See CommandHandler.java to add more text editor links to other methods.
	
	@param newType The name of the prompt method to return to.
	@param newTarget The target (ID, text, etc) to pass to the prompt method.
	@param oldContent The existing text of what the user is editing to be put
		in the buffer.
	*/
	public void editMode(String newType, String newTarget, String oldContent)
	{
		if (cs == ConnState.EDITING)
		{
			sendln("You're already editing. Exit your current buffer first.");
			return;
		}
		promptTarget = newTarget;
		promptType = newType;
		lastCs = cs;
		cs = ConnState.EDITING;
		sendln("{G /s - Save and Close   /q - Close Without Saving  /h - View Help & Commands");
		sendln("{G---------------------------------------------------------------------------");
		editorContents = new ArrayList<String>();
		if (oldContent.length() > 0)
		{
			String oldContents[] = oldContent.split("\\^/", -1);
			for (int ctr = 0; ctr < oldContents.length; ctr++)
				editorContents.add(oldContents[ctr]);
		}
	}
	/**
	Clear any edit mode information previously set.
	*/
	public void clearEditMode()
	{
		promptTarget = "";
		promptType = "";
		cs = lastCs;
	}
	
	/**
	Show a prompt to users who have something other than the prompt as the
	last thing on their screen.
	*/
	public void displayPrompt()
	{
		if (ch.usingJog.length() > 0)
			return;
			
		if (showPrompt)
		{
			// Show the pageMode prompt if necessary.
			if (pageMode)
			{
				pageMode = false;
				sendln("");
				sendln("{Y[== {CPAGE MODE: {wPush enter to continue. See {W'{chelp pagemode{W' {wfor details. {Y==]{x");
				showPrompt = false;
				pageMode = true;
				return;
			}
			
			// Show the editor prompt if necessary.
			if (cs == ConnState.EDITING)
			{
				send("{G > {x");
				showPrompt = false;
				return;
			}

			send("{x");
			if (!prefs.get("compact"))
				sendln("");
			if (tempPrompt.length() > 0)
				sendln(tempPrompt);
			else
			{
				String pr = prompt;
				if (ch.fighting != null)
					pr = bprompt;
				if (!prefs.get("prompt"))
					pr = "";
				
				String exitLine = "";
				for (Exit e : ch.currentRoom.exits)
					if (e.flags.get("hidden") && !(e.flags.get("door") && !e.flags.get("closed")))
						continue;
					else if (e.flags.get("door") && e.flags.get("closed"))
						exitLine = exitLine+e.direction.substring(0, 1).toLowerCase();
					else
						exitLine = exitLine+e.direction.substring(0, 1).toUpperCase();
				
				String lightString = "";
				int hour = Fmt.getHour();
				int season = Fmt.getSeason();
				int sunrise = Fmt.getSunrise(season);
				int sunset = Fmt.getSunset(season);
				if (hour < sunrise || hour > sunset)
					lightString = "{D(){x";
				else if (hour >= sunrise && hour < sunrise+2)
					lightString = "{D({Y){x";
				else if (hour <= sunset && hour > sunset-2)
					lightString = "{Y({D){x";
				else
					lightString = "{Y(){x";

				pr = pr.replace("%%", "SPEC_PERCENT");

				pr = pr.replace("%h", ""+ch.hp);
				pr = pr.replace("%H", ""+ch.maxHp());
				pr = pr.replace("%p", ""+((ch.hp*100)/ch.maxHp()));
				pr = pr.replace("%m", ""+ch.mana);
				pr = pr.replace("%M", ""+ch.maxMana());
				pr = pr.replace("%n", ""+((ch.mana*100)/ch.maxMana()));
				pr = pr.replace("%s", ""+ch.energy);
				pr = pr.replace("%S", ""+ch.maxEnergy());
				pr = pr.replace("%i", ""+((ch.energy*100)/ch.maxEnergy()));
				
				pr = pr.replace("%x", ""+ch.tnl);
				pr = pr.replace("%g", ""+ch.gold);
				pr = pr.replace("%G", ""+ch.bank);
				pr = pr.replace("%c", ""+ch.combatQueue.size()/2);

				pr = pr.replace("%a", ch.currentArea().name);
				pr = pr.replace("%r", ch.currentRoom.name);
				pr = pr.replace("%e", ""+exitLine);
				pr = pr.replace("%t", ""+savedTells.size());
				pr = pr.replace("%T", ""+timeFrmt.format(System.currentTimeMillis()));
				pr = pr.replace("%d", ""+dateFrmt.format(System.currentTimeMillis()));
				pr = pr.replace("%D", ""+longDateFrmt.format(System.currentTimeMillis()));
				pr = pr.replace("%l", lightString);
				
				pr = pr.replace("%A", ""+ch.currentArea().id);
				pr = pr.replace("%R", ""+ch.currentRoom.id);
				pr = pr.replace("%o", olcMode);

				if (ch.fighting != null)
				{
					pr = pr.replace("%O", Fmt.seeName(ch, ch.fighting));
					pr = pr.replace("%P", ""+((ch.fighting.hp*100)/ch.fighting.maxHp()));
					pr = pr.replace("%N", ""+((ch.fighting.mana*100)/ch.fighting.maxMana()));
					pr = pr.replace("%I", ""+((ch.fighting.energy*100)/ch.fighting.maxEnergy()));
				}

				pr = pr.replace("SPEC_PERCENT", "%");
				
				send(pr+"{x");
			}
			showPrompt = false;
		}
	}

	/**
	Send text to the user after converting from UNICODE. Lowest-level output function.
	
	@param msg The text to send to the user.
	*/
	public void encodeAndSend(String msg)
	{
		if (isClosed())
			return;
		try
		{
			Charset charset = Charset.forName("ISO-8859-1");
			CharsetEncoder encoder = charset.newEncoder();
			ByteBuffer bbuf = encoder.encode(CharBuffer.wrap(msg));
			outToClient.write(bbuf.array());
			outToClient.flush();
			lastOutput = 0;
			
			// Copy output to anyone snooping this connection.
			for (UserCon c : conns)
				if (c.snoop == this)
					c.encodeAndSend("} "+msg);
		}
		catch (Exception e)
		{
			if (!e.getMessage().equals("Broken pipe"))
			{
				sysLog("bugs", "Error in encodeAndSend: "+e.getMessage());
				logException(e);
			}
			else
				closeSocket();
		}
	}
	
	/**
	Go through pageMode checks and to {@link #encodeAndSend(String) encodeAndSend}
	without processing formatting characters.
	
	@param msg The text to send to the user.
	*/
	public void sendPlain(String msg)
	{
		// Send nothing to the dummy UserCon.
		if (isDummy || this == progDummyCon)
			return;

		try
		{
			// Set this to show a prompt to anyone who received output during this
			// user check round.
			if (!pageMode)
			{
				showPrompt = true;
			
				// Put a space between the prompt and the output for those who haven't
				// entered any commands. This just makes things look a little more tidy.
				if (!hasPrinted)
				{
					encodeAndSend("\n\r");
					hasPrinted = true;
				}
			}

			// Print until the page length is reached, then add the remaining msg to
			// the output paging buffer.
			while (linesPrinted < pageLength && msg.length() > 0 && !pageMode)
			{
				// Separate the first line from the rest.
				String lines[] = msg.split("\n\r", 2);
				
				// If a newline existed in the message, include that in the output.
				if (lines.length > 1)
				{
					encodeAndSend(lines[0]+"\n\r");
					if (cs != ConnState.LOGIN && cs != ConnState.EDITING && saveable)
						linesPrinted++;
				}
				else
				{
					encodeAndSend(lines[0]);
				}

				// If there was more text, run the loop again against that text.
				if (lines.length > 1)
					msg = lines[1];
				else
				{
					msg = "";
					break;
				}
			}

			// If we couldn't print all of the text, add it to the pageBuffer.			
			if (msg.length() > 0)
			{
				pageMode = true;
				pageBuffer = pageBuffer+msg;
			}

		} catch (Exception e) {
			sysLog("bugs", "Error in sendPlain: "+e.getMessage());
			logException(e);
		}
	}

	/**
	Default output function. Send output after running it through the code
	processing method.
	
	@param msg The text to send to the user.
	*/
	public void send(String msg)
	{
		sendPlain(codeFilter(msg));
	}
	
	/**
	Appends \n\r to a string and sends it to the client. This method
	exists to standardize the practice of sending full lines.
	
	@param msg The text to send to the user.
	*/
	public void sendln(String msg)
	{
		sendPlain(codeFilter(msg+"\n\r"));
	}
	
	/**
	Send a message to all players but this one.
	
	@param args The text to send to users.
	*/
	public void sendToAllExcept(String args)
	{
		for (int ctr = 0; ctr < conns.size(); ctr++)
			if (conns.get(ctr) != this)
				conns.get(ctr).sendln(args);
	}
	
	/**
	Substitute formatting codes with ANSI codes or complex characters.
	
	@param msg The text to process.
	@return The processed string with ANSI codes instead of color codes.
	*/
	public String codeFilter(String msg)
	{
		char ESC = (char)0x1B;
		int CLEAR = 0;			/* {x */
		int BOLD = 1;			/* Uppercase version of any of the following include bold. */
		int BLACK = 30;			/* {d */
		int RED = 31;			/* {r */
		int GREEN = 32;			/* {g */
		int YELLOW = 33;		/* {y */
		int BLUE = 34;			/* {b */
		int MAGENTA = 35;		/* {m */
		int CYAN = 36;			/* {c */
		int WHITE = 37;			/* {w */
		int BG_BLACK = 40;		/*  */
		int BG_RED = 41;		/*  */
		int BG_GREEN = 42;		/*  */
		int BG_YELLOW = 43;		/*  */
		int BG_BLUE = 44;		/*  */
		int BG_MAGENTA = 45;	/*  */
		int BG_CYAN = 46;		/*  */
		int BG_WHITE = 47;		/*  */
		
		for (String s : beeps)
			msg = msg.replace(s, "{*"+s);
		
		msg = msg.replace("^^", "SPEC_DOUBLE_CARROTS");
		msg = msg.replace("^{", "SPEC_BRACE_LEFT");
		msg = msg.replace("^}", "SPEC_BRACE_RIGHT");

		for (String codes : colors)
		{
			if (codes.split("=", 2).length == 2)
			{
				String code1 = codes.split("=", 2)[0];
				String code2 = codes.split("=", 2)[1];
				if (code1.length() > 0 && code2.length() > 0)
					msg = msg.replace(code1, code2);
			}
		}
		for (String codes : colorSubs)
		{
			if (codes.split("=", 2).length == 2)
			{
				String code1 = codes.split("=", 2)[0];
				String code2 = codes.split("=", 2)[1];
				if (code1.length() > 0 && code2.length() > 0)
					msg = msg.replace(code1, code2);
			}
		}
		
		if (prefs.get("ansi"))
		{
			msg = msg.replace("{b", ESC+"["+CLEAR+"m"+ESC+"["+BLUE+"m");
			msg = msg.replace("{B", ESC+"["+BOLD+"m"+ESC+"["+BLUE+"m");
			msg = msg.replace("{c", ESC+"["+CLEAR+"m"+ESC+"["+CYAN+"m");
			msg = msg.replace("{C", ESC+"["+BOLD+"m"+ESC+"["+CYAN+"m");
			// Real black turned off for accessibility purposes.
			//msg = msg.replace("{d", ESC+"["+CLEAR+"m"+ESC+"["+BLACK+"m");
			msg = msg.replace("{D", ESC+"["+BOLD+"m"+ESC+"["+BLACK+"m");
			msg = msg.replace("{g", ESC+"["+CLEAR+"m"+ESC+"["+GREEN+"m");
			msg = msg.replace("{G", ESC+"["+BOLD+"m"+ESC+"["+GREEN+"m");
			msg = msg.replace("{m", ESC+"["+CLEAR+"m"+ESC+"["+MAGENTA+"m");
			msg = msg.replace("{M", ESC+"["+BOLD+"m"+ESC+"["+MAGENTA+"m");
			msg = msg.replace("{r", ESC+"["+CLEAR+"m"+ESC+"["+RED+"m");
			msg = msg.replace("{R", ESC+"["+BOLD+"m"+ESC+"["+RED+"m");
			msg = msg.replace("{w", ESC+"["+CLEAR+"m"+ESC+"["+WHITE+"m");
			msg = msg.replace("{W", ESC+"["+BOLD+"m"+ESC+"["+WHITE+"m");
			msg = msg.replace("{y", ESC+"["+CLEAR+"m"+ESC+"["+YELLOW+"m");
			msg = msg.replace("{Y", ESC+"["+BOLD+"m"+ESC+"["+YELLOW+"m");
			msg = msg.replace("{x", ESC+"["+CLEAR+"m");
		}
		else
		{
			msg = msg.replace("{b", "");
			msg = msg.replace("{B", "");
			msg = msg.replace("{c", "");
			msg = msg.replace("{C", "");
			msg = msg.replace("{d", "");
			msg = msg.replace("{D", "");
			msg = msg.replace("{g", "");
			msg = msg.replace("{G", "");
			msg = msg.replace("{m", "");
			msg = msg.replace("{M", "");
			msg = msg.replace("{r", "");
			msg = msg.replace("{R", "");
			msg = msg.replace("{w", "");
			msg = msg.replace("{W", "");
			msg = msg.replace("{y", "");
			msg = msg.replace("{Y", "");
			msg = msg.replace("{x", "");
		}
		msg = msg.replace("{*", ""+(char)7);

		msg = msg.replace("^/", "\n\r");
		msg = msg.replace("^\\", "");
		msg = msg.replace("^-", "~");
		msg = msg.replace("^i", ""+(char)161);
		msg = msg.replace("^$", ""+(char)162);
		msg = msg.replace("^&", ""+(char)163);
		msg = msg.replace("^*", ""+(char)164);
		msg = msg.replace("^V", ""+(char)165);
		msg = msg.replace("^|", ""+(char)166);
		msg = msg.replace("^S", ""+(char)167);
		msg = msg.replace("^#", ""+(char)168);
		msg = msg.replace("^@", ""+(char)169);
		msg = msg.replace("^a", ""+(char)170);
		msg = msg.replace("^<", ""+(char)171);
		msg = msg.replace("^]", ""+(char)172);
		msg = msg.replace("^%", ""+(char)174);
		msg = msg.replace("^_", ""+(char)175);
		msg = msg.replace("^o", ""+(char)176);
		msg = msg.replace("^+", ""+(char)177);
		msg = msg.replace("^2", ""+(char)178);
		msg = msg.replace("^3", ""+(char)179);
		msg = msg.replace("^`", ""+(char)180);
		msg = msg.replace("^u", ""+(char)181);
		msg = msg.replace("^P", ""+(char)182);
		msg = msg.replace("^.", ""+(char)183);
		msg = msg.replace("^,", ""+(char)184);
		msg = msg.replace("^1", ""+(char)185);
		msg = msg.replace("^0", ""+(char)186);
		msg = msg.replace("^>", ""+(char)187);
		msg = msg.replace("^4", ""+(char)188);
		msg = msg.replace("^5", ""+(char)189);
		msg = msg.replace("^6", ""+(char)190);
		msg = msg.replace("^?", ""+(char)191);
		msg = msg.replace("^M", ""+(char)192);
		msg = msg.replace("^Q", ""+(char)193);
		msg = msg.replace("^7", ""+(char)195);
		msg = msg.replace("^z", ""+(char)196);
		msg = msg.replace("^A", ""+(char)197);
		msg = msg.replace("^8", ""+(char)198);
		msg = msg.replace("^C", ""+(char)199);
		msg = msg.replace("^R", ""+(char)200);
		msg = msg.replace("^W", ""+(char)201);
		msg = msg.replace("^E", ""+(char)202);
		msg = msg.replace("^H", ""+(char)203);
		msg = msg.replace("^Z", ""+(char)204);
		msg = msg.replace("^k", ""+(char)205);
		msg = msg.replace("^L", ""+(char)206);
		msg = msg.replace("^I", ""+(char)207);
		msg = msg.replace("^D", ""+(char)208);
		msg = msg.replace("^N", ""+(char)209);
		msg = msg.replace("^l", ""+(char)210);
		msg = msg.replace("^m", ""+(char)211);
		msg = msg.replace("^O", ""+(char)212);
		msg = msg.replace("^G", ""+(char)213);
		msg = msg.replace("^J", ""+(char)214);
		msg = msg.replace("^X", ""+(char)215);
		msg = msg.replace("^!", ""+(char)216);
		msg = msg.replace("^n", ""+(char)217);
		msg = msg.replace("^q", ""+(char)218);
		msg = msg.replace("^:", ""+(char)219);
		msg = msg.replace("^K", ""+(char)220);
		msg = msg.replace("^Y", ""+(char)221);
		msg = msg.replace("^p", ""+(char)222);
		msg = msg.replace("^B", ""+(char)223);
		msg = msg.replace("^r", ""+(char)224);
		msg = msg.replace("^s", ""+(char)225);
		msg = msg.replace("^j", ""+(char)226);
		msg = msg.replace("^b", ""+(char)227);
		msg = msg.replace("^d", ""+(char)228);
		msg = msg.replace("^F", ""+(char)229);
		msg = msg.replace("^9", ""+(char)230);
		msg = msg.replace("^c", ""+(char)231);
		msg = msg.replace("^t", ""+(char)232);
		msg = msg.replace("^v", ""+(char)233);
		msg = msg.replace("^g", ""+(char)234);
		msg = msg.replace("^e", ""+(char)235);
		msg = msg.replace("^w", ""+(char)236);
		msg = msg.replace("^x", ""+(char)237);
		msg = msg.replace("^h", ""+(char)238);
		msg = msg.replace("^f", ""+(char)239);
		msg = msg.replace("^T", ""+(char)240);
		msg = msg.replace("^(", ""+(char)241);
		msg = msg.replace("^\"", ""+(char)245);
		msg = msg.replace("^=", ""+(char)247);
		msg = msg.replace("^[", ""+(char)248);
		msg = msg.replace("^)", ""+(char)251);
		msg = msg.replace("^U", ""+(char)252);
		msg = msg.replace("^y", ""+(char)253);
		msg = msg.replace("^;", ""+(char)254);
		
		msg = msg.replace("SPEC_DOUBLE_CARROTS", "^");
		msg = msg.replace("SPEC_BRACE_LEFT", "{");
		msg = msg.replace("SPEC_BRACE_RIGHT", "}");
		
		return msg;
	}
	
	/**
	Calculate the length of a string if formatting codes were taken out.
	<p>
	This isn't always precisely accurate. It doesn't support some special
	characters. It will generally work if the only special codes are in
	the first part of the string.
	
	@param msg The text to calculate the length of.
	@return The length of the string with no formatting codes.
	*/
	public static int codelessLength(String msg)
	{
		int length = 0;
		int msglen = msg.length();
		for (int ctr = 0; ctr < msglen; ctr++)
		{
			char cr = msg.charAt(ctr);
			if (cr == '{' || cr == '}')
				length--;
			else if (cr == '^')
				continue;
			else
				length++;
		}
		return length;
	}
	
	public static String stripCodes(String msg)
	{
		String newMsg = "";
		int msglen = msg.length();
		boolean skip = false;
		for (int ctr = 0; ctr < msglen; ctr++)
		{
			if (skip)
			{
				skip = false;
				continue;
			}
			char cr = msg.charAt(ctr);
			if (cr == '^' || cr == '{' || cr == '}')
				skip = true;
			else
				newMsg = newMsg+cr;
		}
		
		return newMsg;
	}

	/**
	Repeat the string {@code s count} times.
	
	@param s The string to repeat.
	@param count The number of times to repeat the string.
	@return The finished string.
	*/
	public String repeat(String s, int count)
	{
		String result = "";
		for (int ctr = 0; ctr < count; ctr++)
			result = result+s;
			
		return result;
	}
	
	/**
	Check the user's permissions ArrayList for the given permission.
	
	@param perm The name of the permission to check for.
	@return {code true} if the user has this permission; {code false} otherwise.
	*/
	public boolean hasPermission(String perm)
	{
		for (String p : permissions)
			if (p.equals(perm))
				return true;
		return false;
	}
	
	/**
	Check the user's list of turned-off channels for the given channel name.
	
	@param chname The name of the channel to check for.
	@return {code true} if the user has this channel turned off; {code false}
		otherwise.
	*/
	public boolean channelOff(String chname)
	{
		for (String cn : chansOff)
			if (cn.equals(chname))
				return true;
		return false;
	}
	
	public boolean spamCheck()
	{
		if (isDummy)
			return false;
		if (hasPermission("staff"))
			return false;
		
		if (chatDelay > 0)
		{
			sendln("Your channels are still disabled for "+chatDelay/10+" seconds.");
			return true;
		}

		chatCount++;
		if (chatCount > 10)
		{
			chatDelay = 300;
			sysLog("justice", "Spam alert: "+ch.shortName);
			sendln("{RYou've tripped our spam trigger!{x");
			sendln("Your channels have been disabled for 30 seconds.");
			sendln("To prevent spamming, we limit the number of communications you can send to an");
			sendln("average of one per 5 seconds.");
			return true;
		}
		return false;
	}
	
	public int compareTo(UserCon other)
	{
		return ch.level-other.ch.level;
	}
}
