package jw.core;
import java.io.*;
import java.lang.reflect.Method;
import java.net.*;
import java.sql.*;
import java.text.*;
import java.util.*;

import jw.commands.*;
import jw.data.*;
import jw.core.*;

/**
	The MudMain class contains all central methods responsible for loading and operating
	the MUD.
*/
public class MudMain
{
	/** The list of classes to search for commands by method name. */
	public static Class commandClasses[] = {ChatCommands.class,
											CombatCommands.class,
											InfoCommands.class,
											ObjectCommands.class,
											OlcCommands.class,
											RoomCommands.class,
											StaffCommands.class,
											UserCommands.class};
	/** The list of classes to search for skills by method name. */
	public static Class skillClasses[] = {	Cleric.class,
											Fighter.class,
											Generic.class,
											Mage.class};
	/** All commands loaded in the game. */
	public static ArrayList<Command> commands = new ArrayList<Command>();
	/** All prompts loaded in the game. */
	public static ArrayList<Command> prompts = new ArrayList<Command>();
	/** All script commands loaded in the game. */
	public static ArrayList<Command> scriptCommands = new ArrayList<Command>();
	/** All mobans loaded in the game. */
	public static ArrayList<Moban> mobans = new ArrayList<Moban>();
	/** All help files loaded in the game. */
	public static ArrayList<Help> helps = new ArrayList<Help>();
	/** All socials loaded in the game. */
	public static ArrayList<Social> socials = new ArrayList<Social>();
	/** All areas loaded in the game. */
	public static ArrayList<Area> areas = new ArrayList<Area>();
	/** All roomprogs loaded in the game. */
	public static ArrayList<RoomProg> rprogs = new ArrayList<RoomProg>();
	/** All mobprogs loaded in the game. */
	public static ArrayList<MobProg> mprogs = new ArrayList<MobProg>();
	/** All objprogs loaded in the game. */
	public static ArrayList<ObjProg> oprogs = new ArrayList<ObjProg>();
	/** All quests loaded in the game. */
	public static ArrayList<Quest> quests = new ArrayList<Quest>();
	/** All rooms loaded in the game. */
	public static ArrayList<Room> rooms = new ArrayList<Room>();
	/** All skills loaded in the game. */
	public static ArrayList<Skill> skills = new ArrayList<Skill>();
	/** All races loaded in the game. */
	public static ArrayList<Race> races = new ArrayList<Race>();
	/** All classes loaded in the game. */
	public static ArrayList<CharClass> classes = new ArrayList<CharClass>();
	/** All character prototypes loaded in the game. */
	public static ArrayList<CharProto> charProtos = new ArrayList<CharProto>();
	/** All mobs loaded in the game. */
	public static ArrayList<CharData> mobs = new ArrayList<CharData>();
	/** All lootgroups loaded in the game. */
	public static ArrayList<Lootgroup> lootgroups = new ArrayList<Lootgroup>();
	/** All object prototypes loaded in the game. */
	public static ArrayList<ObjProto> objProtos = new ArrayList<ObjProto>();
	/** All custom variables in the game. */
	public static HashMap<String, String> globals = new HashMap<String, String>();
	/** All message boards in the game. */
	public static ArrayList<Board> boards = new ArrayList<Board>();
	/** All messages in the game. */
	public static ArrayList<BoardMessage> boardMessages = new ArrayList<BoardMessage>();
	/** The list of scheduled updates to perform. */
	public static ArrayList<Update> updates = new ArrayList<Update>();
	/** All players connected to the game. */
	public static ArrayList<UserCon> conns = new ArrayList<UserCon>();
	/** All player groups. */
	public static ArrayList<Group> groups = new ArrayList<Group>();
	
	/** The available connection status types. */
	public enum ConnState {LOGIN, CHATTING, PLAYING, EDITING};
	/** The list of standard color substitutions. */
	public static String colorSubs[] = { 
		"{a={Y", /* auction */		"{A={W", /* auction_text */
		"{e={r", /* gtell */		"{E={g", /* gtell_text */
		"{f={r", /* fellow */		"{F={w", /* fellow_text */
		"{h={g", /* quote */		"{H={y", /* quote_text */
		"{i={Y", /* stafftalk */	"{I={c", /* stafftalk_text */
		"{j={r", /* gocial */		"{J={w", /* gocial_text */
		"{k={b", /* yell */			"{K={c", /* yell_text */
		"{l={g", /* log */			"{L={m", /* log_text */
		"{n={c", /* admin */		"{N={R", /* admin_text */
		"{o={C", /* discuss */		"{O={c", /* discuss_text */
		"{p={W", /* ministry */		"{P={r", /* ministry_text */
		"{q={Y", /* qa */			"{Q={W", /* qa_text */
		"{s={g", /* say */			"{S={G", /* say_text */
		"{t={g", /* tell */			"{T={G", /* tell_text */
		"{u={m", /* clan */			"{U={c", /* clan_text */
		"{v={y", /* vulgar */		"{V={w", /* vulgar_text */
		"{z={w", /* grats */		"{Z={m", /* grats_text */

		"}h={C", /* help_text */	"}H={W", /* help_pun */
		"}i={c", /* help_num */		"}I={w", /* help_oth */
		"}m={C", /* menu_text */	"}M={B", /* menu_pun */
		"}n={W", /* menu_num */		"}N={w", /* menu_oth */
		"}s={c", /* room_title */	"}S={w", /* room_text */
		"}t={g", /* room_exits */	"}T={c", /* room_things */

		"{1={r", /* fight_death */	"{2={g", /* fight_yhit */
		"{3={y", /* fight_ohit */	"{4={r", /* fight_thit */
		"{5={w"}; /* fight_skill */

	/** Logging of all commands. */
	public static boolean logAll = false;
	/** Global random number generator. */
	public static Random gen = new Random();
	/** Global short time format. */
	public static SimpleDateFormat timeFrmt = new SimpleDateFormat("h:mm:ss a");
	/** Global date-only format. */
	public static SimpleDateFormat dateFrmt = new SimpleDateFormat("MM/dd/yyyy");
	/** Global long date-only format. */
	public static SimpleDateFormat longDateFrmt = new SimpleDateFormat("EEEE, MMM. d yyyy");
	/** Global short date/time format. */
	public static SimpleDateFormat frmt = new SimpleDateFormat("MM/dd/yyyy h:mm:ss a");
	/** Global long date/time format. */
	public static SimpleDateFormat longFrmt = new SimpleDateFormat("EEEE, MMM. d yyyy h:mm a");

	/** Offset for converting real time to game time. */
	public static int timeOffset = 1255567890;

	/** The number of updates performed since startup. */
	public static long updateCycles = 0;
	/** The port being used for the game. */
	public static int portNr = 1335;
	/** The "dummy" user connection used by progs executing commands. */
	public static UserCon progDummyCon = new UserCon();
	/** The "dummy" character is used by progs executing commands. */
	public static CharData dummyChar = new CharData(progDummyCon, true);
	/** The server socket used to listen for new connections. */
	public static ServerSocket welcomeSocket;

	/**
	Main method: Invoked by running the game.
	
	@param argv The array of arguments presented to the game. Currently used only for
		the port number.
	*/
	public static void main(String argv[])
	{
		if (argv.length > 0)
		{
			try
			{
				portNr = Integer.parseInt(argv[0]);
			}
			catch (Exception e)
			{
				System.out.println("Invalid port number.");
				return;
			}
			if (portNr < 1 || portNr > 65535)
			{
				System.out.println("Port number must be between 1 and 65535.");
				return;
			}
		}
		
		try
		{
			sysLog("system", "");
			sysLog("system", "      *           *           *           *");
			sysLog("system", "    *   *       *   *       *   *       *  ");
			sysLog("system", "  *       *   *       *   *       *   *    ");
			sysLog("system", "*           *           *           *      ");
			sysLog("system", "");
			sysLog("system", "Initializing...");
			System.out.println("Initializing...");
			Database.initDb();
			Flags.setObjTypes();
			Flags.setWeatherTypes();
			
			// Generate the arrayList of commands.
			for (int ctr = 0; ctr < commandClasses.length; ctr++)
			{
				Method[] classMethods = commandClasses[ctr].getDeclaredMethods();
				for (Method m : classMethods)
				{
					// Find all methods that meet the standard command method syntax.
					// (doCapitalizedCommandName).
					String tempName = m.getName();
					if (tempName.length() > 2)
						if (tempName.startsWith("do") && tempName.charAt(2) >= 'A' && tempName.charAt(2) <= 'Z')
						{
							Command newC = new Command(m);
							Database.loadCommand(newC);
							commands.add(newC);
						}
				}
			}
			Collections.sort(commands);
			sysLog("system", "Loading commands... "+commands.size()+" commands loaded.");
	
			// Generate the arrayList of prompts.
			for (int ctr = 0; ctr < commandClasses.length; ctr++)
			{
				Method[] classMethods = commandClasses[ctr].getDeclaredMethods();
				for (Method m : classMethods)
				{
					// Find all methods that meet the standard prompt method syntax.
					// (prCapitalizedPromptName).
					String tempName = m.getName();
					if (tempName.length() > 2)
						if (tempName.startsWith("pr") && tempName.charAt(2) >= 'A' && tempName.charAt(2) <= 'Z')
						{
							Command newP = new Command(m);
							prompts.add(newP);
						}
				}
			}
			Collections.sort(prompts);
			sysLog("system", "Loading prompts... "+prompts.size()+" prompts loaded.");

			// Load mobans from database.
			Moban.loadMobans();
			sysLog("system", "Loading mobans... "+mobans.size()+" mobans loaded.");
			
			// Load help files from database.
			Database.loadHelps();
			sysLog("system", "Loading helps... "+helps.size()+" help files loaded.");
	
			// Load socials from database.
			Database.loadSocials();
			Collections.sort(socials);
			sysLog("system", "Loading socials... "+socials.size()+" socials loaded.");

			// Load classes from database.
			Database.loadClasses();
			sysLog("system", "Loading classes... "+classes.size()+" classes loaded.");
	
			// Generate the arrayList of skills.
			for (int ctr = 0; ctr < skillClasses.length; ctr++)
			{
				Method[] classMethods = skillClasses[ctr].getDeclaredMethods();
				for (Method m : classMethods)
				{
					String tempName = m.getName();
					if (tempName.length() > 2)
						if ((tempName.startsWith("sp") || tempName.startsWith("sk")) && tempName.charAt(2) >= 'A' && tempName.charAt(2) <= 'Z')
						{
							Skill newSkill = new Skill(m);
							Database.loadSkill(newSkill);
							skills.add(newSkill);
						}
				}
			}
			Collections.sort(skills);
			sysLog("system", "Loading skills... "+skills.size()+" skills loaded.");

			// Load races from database.
			Database.loadRaces();
			sysLog("system", "Loading races... "+races.size()+" races loaded.");

			// Load progs from database.	
			sysLog("system", "Loading progs...");
			Database.loadProgs();

			// Load object prototypes from database.
			Database.loadObjProtos();
			sysLog("system", "Loading object prototypes... "+objProtos.size()+" objects loaded.");

			// Load lootgroups from database.
			Database.loadLootgroups();
			sysLog("system", "Loading lootgroups... "+lootgroups.size()+" lootgroups loaded.");

			// Load character prototypes from database.
			Database.loadCharProtos();
			sysLog("system", "Loading character prototypes... "+charProtos.size()+" mobs loaded.");
	
			// Load areas from database.
			Database.loadAreas();
			sysLog("system", "Loading areas... "+areas.size()+" areas loaded.");
	
			// Load rooms from database.
			Database.loadRooms();
			Database.loadExits();
			sysLog("system", "Loading rooms... "+rooms.size()+" rooms loaded.");

			// Load saved mobs from last shutdown.
			CharData.loadSavedMobs();
			sysLog("system", "Loading saved mobs... "+mobs.size()+" mobs loaded.");
			progDummyCon.permissions.add("staff");
			progDummyCon.permissions.add("builder");
			dummyChar.shortName = "a mysterious force";
			
			// Load resets from the database and fill them.
			sysLog("system", "Loading resets... ");
			Database.loadResets();

			// Load global variables from the database.
			sysLog("system", "Loading global variables...");
			Database.loadGlobals();
			
			// Load boards from database.
			Board.loadAll();
			sysLog("system", "Loading boards... "+boards.size()+" boards loaded.");

			// Load messages from database.
			BoardMessage.loadAll();
			sysLog("system", "Loading messages... "+boardMessages.size()+" messages loaded.");

			// Start the connection handling thread.
			welcomeSocket = new ServerSocket(portNr);
			sysLog("system", "Listening for connections on port "+portNr+".");
			new Thread(new GetConnections()).start();
	
			// Start MUD update thread.
			sysLog("system", "Starting MUD Update Thread.");
			Timer runTask = new Timer();
			runTask.scheduleAtFixedRate(new MudRunner(), 0, 100);
			
			sysLog("system", "All systems are go on port "+portNr+".");
			System.out.println("All systems are go on port "+portNr+".");
			System.out.println("System messages are being logged to logs/system.txt.");
		}
		catch (SQLException e)
		{
			System.out.println("There was an error while connecting to the database:");
			System.out.println(e.getMessage());
		}
		catch (BindException e)
		{
			System.out.println("There was an error while initializing the server port:");
			System.out.println(e.getMessage());
		}
		catch (Exception e)
		{
			System.out.println("Unexpected error in MudMain: "+e.getMessage());
			sysLog("bugs", "Unexpected error in MudMain: "+e.getMessage());
			logException(e);
		}
	}
	
	// HandleIO class:
	//	Fetches input from all connections;
	//	Finds and removes closed connections.
	static class HandleIO implements Runnable
	{
		private UserCon c;
		private Socket sock;
		
		public HandleIO(Socket newSock)
		{
			sock = newSock;
		}
		
		public void run()
		{
			String commandLine;
			c = new UserCon(sock);

			while(true)
			{
				// Dump closed connections.
				if (c.isClosed())
				{
					return;
				}
				commandLine = c.read();
				if (!c.isClosed())
					c.lastInput = 0;
				c.commandQueue.add(commandLine);
			}
		}
	}
	
	// GetConnections class:
	//	Listens and accepts new connections;
	//	Adds to conns ArrayList.
	static class GetConnections implements Runnable
	{
		public void run()
		{
			try
			{
				while (true)
				{
					Socket newConn = welcomeSocket.accept();
					sysLog("connections", "New connection: "+newConn.getInetAddress().getHostName());
					// Start the command processing thread.
					new Thread(new HandleIO(newConn)).start();
				}
			} catch (Exception e) {
				System.out.println("There was an error listening on "+portNr+":");
				System.out.println(e.getMessage());
			}
		}
	}
	
	// MudRunner class:
	//  Runs the MUD, from handling user commands to managing combat.
	//  Almost all output should be indirectly called through this
	//  method (usually through processCommand()) in order to prevent
	//  lines from one method from appearing between lines from other
	//  methods.
	static class MudRunner extends TimerTask
	{
		public void run()
		{
			try
			{
				// Each update cycle is 0.1 seconds. 30 = Every 3 seconds, etc.
				updateCycles++;
				
				if (updateCycles % 5 == 0)
					for (CharData ch : allChars())
						ch.doJog();
				
				if (updateCycles % 10 == 0)
				{
					boolean checkEnergy = (updateCycles % 30 == 0);
					for (CharData ch : allChars())
					{
						ch.updateEffects();
						ch.regen(checkEnergy);
					}
					
					Script.checkTimed();
				}
				
				if (updateCycles % 30 == 0)
				{
					Combat.updateCombat();
					for (CharData ch : allChars())
						Combat.checkAutoAttacks(ch.conn);
					for (int ctr = groups.size()-1; ctr >= 0; ctr--)
					{
						if (groups.get(ctr).members.size() <= 1 && groups.get(ctr).invites.size() == 0)
						{
							for (CharData chs : groups.get(ctr).members)
								chs.sendln("Your group has automatically been disbanded.");
							groups.remove(ctr);
						}
					}
				}
				
				if (updateCycles % 50 == 0)
					for (UserCon cs : conns)
						if (cs.chatCount > 0)
							cs.chatCount--;
				
				if (updateCycles % 100 == 0)
				{
					Combat.checkPursuits();
					for (ObjData o : ObjData.allObjects())
						o.update();
					for (Room r : rooms)
						r.update();
				}
				
				if (updateCycles % 150 == 0)
				{
					for (Room r : rooms)
						r.checkTrigger("random", null, null, "", 0);
					for (CharData ch : mobs)
						ch.checkTrigger("random", null, null, "", 0);
					for (ObjData o : ObjData.allObjects())
						o.checkTrigger("random", null, null, "", 0);
				}
				
				if (updateCycles % 200 == 0)
					for (CharData ch : mobs)
						ch.update();
				
				if (updateCycles % 600 == 0)
				{
					// Keep the database from timing out.
					Database.refreshDb();

					// Clear out old MultOKs / Bans:
					for (Moban m : mobans)
						if (m.end > (System.currentTimeMillis()/1000) && m.end > 0)
						{
							sysLog("justice", "MO/Ban expired: "+m.type+" "+m.host);
							m.delete();
							break;
						}
				}
					
				if (updateCycles % 3600 == 0)
				{
					for (Area a : areas)
						a.update();
					Area.weatherStatus();
				}
				
				if (updateCycles % 12000 == 0)
					for (Room r : rooms)
					{
						boolean mobOnly = false;
						for (Reset rs : r.resets)
						{
							if ((rs.type.equals("object") || rs.type.equals("lootgroup")) && !mobOnly)
								rs.fillReset();
							else if (rs.type.equals("mob"))
							{
								rs.fillReset();
								mobOnly = true;
							}
						}
					}
				
				// Handle the skill/spell queue.
				Combat.processQueue();
				
				// Look for dynamic updates to run.
				for (int ctr = 0; ctr < updates.size(); ctr++)
					if (updates.get(ctr).runAt == updateCycles)
						updates.get(ctr).run();
				
				// Continuously clean up old updates.
				for (Update u : updates)
					if (u.runAt < updateCycles)
					{
						updates.remove(u);
						break;
					}
				
				// User check loop one: Process a command from each user,
				// where applicable, and update time-related information.
				for (int ctr = 0; ctr < conns.size(); ctr++)
				{
					conns.get(ctr).lastConnected = (System.currentTimeMillis()/1000);
					conns.get(ctr).timePlayed += 100;
					conns.get(ctr).lastInput++;
					conns.get(ctr).lastOutput++;

					if (conns.get(ctr).lastOutput >= 6000)
						conns.get(ctr).encodeAndSend("Time passes...\n\r");
					
					if (conns.get(ctr).lastInput == 6000 && !conns.get(ctr).afk)
					{
						conns.get(ctr).afk = true;
						conns.get(ctr).sendln("You are now AFK. Any tells sent to you will be saved for you to 'replay'.");
					}
					if (conns.get(ctr).lastInput >= 12000 && !conns.get(ctr).isClosed() && !conns.get(ctr).hasPermission("staff") && !conns.get(ctr).hasPermission("builder"))
					{
						conns.get(ctr).ch.save();
						Database.saveAccount(conns.get(ctr));
						conns.get(ctr).sendln("Since you have been inactive for more than 20 minutes, your connection^/"+
											  "has been closed. When you come back, just log back in to pick up where^/"+
											  "you left off.");
						sysLog("system", "Idle connection closed: "+conns.get(ctr).ch.name);
						conns.get(ctr).closeSocket();
						conns.get(ctr).lastOutput = conns.get(ctr).lastInput;
					}
					
					if (conns.get(ctr).isClosed() && conns.get(ctr).lastOutput >= 3000 && conns.get(ctr).lastInput >= 3000)
					{
						conns.get(ctr).cleanup();
						ctr--;
						continue;
					}
					conns.get(ctr).processCommand();
				}
				// User check loop two: Finish up output with a prompt, and deal with
				// paging counters.
				for (int ctr = 0; ctr < conns.size(); ctr++)
				{
					UserCon c = conns.get(ctr);
					if (!c.isClosed())
					{
						if (c.chatDelay > 0)
						{
							c.chatDelay--;
							if (c.chatDelay == 0)
								c.sendln("Your channels have been enabled.");
						}
						c.lastPrompt++;
						if (c.lastPrompt > 3)
						{
							c.lastPrompt = 0;
							c.linesPrinted = 0;
							c.displayPrompt();
							c.hasPrinted = false;
							
							// Do this again to prevent the prompt from counting in paging.
							c.linesPrinted = 0;
						}
					}
				}
			} catch (Exception e)
			{
				sysLog("bugs", "Error in MudRunner: "+e.getMessage());
				logException(e);
			}
		}
	}
	
	/**
	Creates an array of all characters in the game.
	
	@return An array of {@code CharData} containing all characters loaded in the game.
	*/
	public static CharData[] allChars()
	{
		ArrayList<CharData> chars = new ArrayList<CharData>();
		
		for (CharData ch : mobs)
			chars.add(ch);
		for (UserCon c : conns)
			chars.add(c.ch);
		
		return chars.toArray(new CharData[0]);
	}
	
	/**
	Record log messages and display them to staff.
	
	@param msg The message to be logged.
	*/
	public static void sysLog(String type, String msg)
	{
		for (UserCon c : conns)
			if (!c.isClosed())
				if (c.staffLogs.contains(type))
					c.sendln("{l"+Fmt.cap(type)+" Log: {L"+msg+"{x");
		
		if (!type.equals("exceptions"))
		{
			FileOutputStream out;
			PrintStream p;
			try
			{
				out = new FileOutputStream("logs/system.txt", true);
				p = new PrintStream(out);
				p.println(frmt.format(System.currentTimeMillis())+" "+type+" / "+msg);
				p.close();
			} catch (Exception e) {
				System.out.println("Error writing to log file: "+e.getMessage());
			}
		}
	}
	
	public static void logException(Exception e)
	{
		for (StackTraceElement ste : e.getStackTrace())
		{
			try
			{
				FileOutputStream out;
				PrintStream p;
				out = new FileOutputStream("logs/exceptions.txt", true);
				p = new PrintStream(out);
				p.println(frmt.format(System.currentTimeMillis())+" / "+ste);
				p.close();
			} catch (Exception e2) {
				System.out.println("Error writing to log file: "+e2.getMessage());
			}
			sysLog("exceptions", ste.toString());
		}
	}
}
