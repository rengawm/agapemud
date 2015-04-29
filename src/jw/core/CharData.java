package jw.core;
import java.sql.*;
import java.util.*;

import jw.commands.*;
import jw.core.*;
import jw.data.*;
import static jw.core.MudMain.*;

/**
	The CharData class represents a single character/mob, with all associated fields.
*/
public class CharData
{
	/** The ID of a player-character, used for saving to the database. */
	public int id = 0;
	/** The connection associated with this character (if it's a player). */
	public UserCon conn;
	/** The mob prototype used to generate this character (if it's an NPC). */
	public CharProto cp;
	/** The character's keyword(s). */
	public String name = "";
	/** The short name of the character (used in action phrases, "you hit
		<shortname>", etc). */
	public String shortName = "";
	/** The long name of the character, used when seen in a room. If this is set for 
		a player's character, it is their title. */
	public String longName = "";
	/** The multi-line description of the character, used when a player looks at it. */
	public String description = "";
	/** Extra descriptions. */
	public HashMap<String, String> eds = new HashMap<String, String>();
	/** A list of custom variables set on this character. */
	public HashMap<String, String> variables = new HashMap<String, String>();
	/** All mob progs active on this character. */
	public ArrayList<Trigger> triggers = new ArrayList<Trigger>();
	/** The sex of a character - 'm' for male, 'f' for female, 'r' for randomly
		generated. */
	public String sex = "r";
	/** The character's current room. */
	public Room currentRoom;
	/** The race of this character. */
	public Race charRace;
	/** The class (CharClass) of this character. */
	public CharClass charClass;
	/** The level of the character. */
	public int level = 1;
	/** How much gold this player has. */
	public int gold = 0;
	/** How much gold this player has in the bank. */
	public int bank = 0;
	/** The number of experience points needed to progress to the next level. */
	public int tnl = 0;
	/** The number of training points this mob has available. */
	public int trains = 0;
	/** The difficulty setting of this mob, on a scale of 1-4. */
	public int difficulty = 1;
	/** Flags. */
	public HashMap<String, Boolean> flags = new HashMap<String, Boolean>();
	/** Effects. */
	public ArrayList<Effect> effects = new ArrayList<Effect>();
	/** Character's current position. */
	public String position = "standing";
	/** The object the character is <position>ing on. */
	public ObjData positionTarget;
	/** The position the character was in before eating/drinking. */
	public String lastPosition = "";
	/** The current health of the character. */
	public int hp = 0;
	/** The base maximum health of the character. */
	public int baseMaxHp = 0;
	/** The current mana of the character. */
	public int mana = 0;
	/** The base maximum mana of the character. */
	public int baseMaxMana = 0;
	/** The current energy of the character. */
	public int energy = 0;
	/** The base maximum energy of this character. */
	public int baseMaxEnergy = 0;
	/** The base strength of this character. */
	public int baseStr = 0;
	/** The base dexterity of this character. */
	public int baseDex = 0;
	/** The base constitution of this character. */
	public int baseCon = 0;
	/** The base intelligence of this character. */
	public int baseInt = 0;
	/** The base charisma of this character. */
	public int baseCha = 0;
	/** The base slash armor of this character. */
	public int baseArmSlash = 0;
	/** The base bash armor of this character. */
	public int baseArmBash = 0;
	/** The base pierce armor of this character. */
	public int baseArmPierce = 0;
	/** The base frost resistance of this character. */
	public int baseResFrost = 0;
	/** The base fire resistance of this character. */
	public int baseResFire = 0;
	/** The base lightning resistance of this character. */
	public int baseResLightning = 0;
	/** The base acid resistance of this character. */
	public int baseResAcid = 0;
	/** The base good resistance of this character. */
	public int baseResGood = 0;
	/** The base evil resistance of this character. */
	public int baseResEvil = 0;
	/** The movement verb of this character. */
	public String movement = "";
	/** The hit name for this character's unarmed combat. */
	public String hitname = "";
	/** Loot groups used by this mob, and number of items to pull from that group. */
	public HashMap<Lootgroup, Integer> lootgroups = new HashMap<Lootgroup, Integer>();
	/** The list of objects held/used by this character. */
	public ArrayList<ObjData> objects = new ArrayList<ObjData>();
	/** The lootgroups this shop sells from, if it's a shopkeeper. */
	public ArrayList<Lootgroup> sells = new ArrayList<Lootgroup>();
	/** The skills known by this character. */
	public HashMap<Skill, Integer> learned = new HashMap<Skill, Integer>();
	/** Time until the character can use certain abilities. */
	public HashMap<Skill, Integer> cooldowns = new HashMap<Skill, Integer>();
	/** The reset ID which this character is filling (if it's an NPC). */
	public int resetFilled = 0;
	/** The character which this character is fighting, or null if the character is
		not currently fighting anyone. */
	public CharData fighting;
	/** Attacks per round not yet used. */
	public double attacksAccrued = 0.0;
	/** The time (in updates) since this character was last saved. */
	public int lastSaved = 0;
	/** The time (in updates) before the next combat queue command will be executed. */
	public int queueDelay = 0;
	/** The list of combat-related commands to perform. */
	public ArrayList<String> combatQueue = new ArrayList<String>();
	/** The list of spell targets for queued spells. */
	public ArrayList<Object> targetQueue = new ArrayList<Object>();
	/** The list of people this character will attack on sight. */
	public ArrayList<CharData> hating = new ArrayList<CharData>();
	/** The list of people who have damaged this mob recently. */
	public HashMap<CharData, Integer> damagers = new HashMap<CharData, Integer>();
	/** The character this character is sparring with. */
	public CharData sparring = null;
	/** Who this character is following. */
	public CharData following = null;
	/** Jog path, if jogging. */
	public String usingJog = "";
	/** Is the character fleeing? */
	public boolean fleeing = false;
	/** The character's alignment */
	public int align = 0;
	/** The quests this character has completed. */
	public ArrayList<Quest> completed = new ArrayList<Quest>();
	/** The objectives this character is currently working on. */
	public HashMap<QuestObjective, ArrayList<Integer>> progress = new HashMap<QuestObjective, ArrayList<Integer>>();
	/** The quests this character can offer to players. */
	public ArrayList<Quest> offers = new ArrayList<Quest>();
	
	/**
	Player-character constructor: Use a connection to create a character.
	<p>
	This will set the character's {@code conn} variable to the given UserCon object,
	and mark the {@code cp} variable as {@code null}, since this character is not set
	from a CharProto object.
	
	@param newConn The connection to assign this character to.
	*/
	public CharData(UserCon newConn, boolean loadObjects)
	{
		conn = newConn;
		conn.cs = ConnState.PLAYING;
		conn.ch = this;
		cp = null;
		
		if (conn != progDummyCon)
		{
			try
			{
				ResultSet dbResult = Database.dbQuery.executeQuery("SELECT * FROM characters"+
						" WHERE character_user = "+conn.id);
				if (dbResult.next())
				{
					id = dbResult.getInt("character_id");
					load(loadObjects);
				}
				else
				{
					create();
				}
			} catch (Exception e) {
				sysLog("bugs", "Error in CharData(UserCon): "+e.getMessage());
				logException(e);
			}
		}
	}
	
	public void initializeValues()
	{
		level = 1;
		trains = 0;
		baseStr = charRace.baseStr;
		if (baseStr < 10)
			baseStr += 10+gen.nextInt(5);
		baseDex = charRace.baseDex;
		if (baseDex < 10)
			baseDex += 10+gen.nextInt(5);
		baseCon = charRace.baseCon;
		if (baseCon < 10)
			baseCon += 10+gen.nextInt(5);
		baseInt = charRace.baseInt;
		if (baseInt < 10)
			baseInt += 10+gen.nextInt(5);
		baseCha = charRace.baseCha;
		if (baseCha < 10)
			baseCha += 10+gen.nextInt(5);

		baseMaxHp = 30+(baseCon/2);
		if (!conn.isDummy)
			baseMaxHp += 100;
		else if (level < 10)
			baseMaxHp -= baseMaxHp/(level/2+1);
		baseMaxMana = 30+(baseInt/2);
		baseMaxEnergy = 100;
		hp = baseMaxHp;
		mana = baseMaxMana;
		energy = baseMaxEnergy;

		tnl = Formulas.tnl(this);

		learned = new HashMap<Skill, Integer>();
		for (Skill s : skills)
			if (s.availAt(charClass) == 1)
				learned.put(s, 75);
		
		restore();
	}
	
	/**
	Saved mobs constructor: Create a blank NPC to be set by another method.
	<p>
	This constructor is only used by the {@link Database#loadSavedMobs()
	Database.loadSavedMobs} method on MUD startup. Since they are loaded from a database,
	any values that they have should override the values of their CharProto, even
	though that will still be set.
	*/
	public CharData(int newId)
	{
		id = newId;
		conn = new UserCon();
		conn.ch = this;
		conn.permissions.add("admin");
		load(true);
	}
	
	/**
	Standard NPC constructor: Create an NPC and copy the values of the given mob prototype.
	<p>
	This constructor will set all values from the values of {@code newCp}.
	
	@param newCp The CharProto object to copy values from.
	*/
	public CharData(CharProto newCp)
	{
		conn = new UserCon();
		conn.ch = this;
		conn.permissions.add("admin");
		cp = newCp;
		name = newCp.name;
		shortName = newCp.shortName;
		longName = newCp.longName;
		description = newCp.description;
		
		for (String k : newCp.eds.keySet())
			eds.put(k, newCp.eds.get(k));
		
		for (Trigger t : newCp.triggers)
			triggers.add(t);

		sex = newCp.sex;
		if (sex.equals("r"))
		{
			if (gen.nextInt(2) == 0)
				sex = "m";
			else
				sex = "f";
		}
		currentRoom = null;
		charRace = newCp.charRace;
		charClass = newCp.charClass;
		difficulty = newCp.difficulty;

		for (String s : Flags.charFlags)
			if (newCp.flags.get(s))
				flags.put(s, true);
			else
				flags.put(s, false);
		
		position = newCp.position;
		
		baseArmSlash = 100;
		if (cp.baseArmSlash > 0)
			baseArmSlash = cp.baseArmSlash;

		baseArmBash = 100;
		if (cp.baseArmBash > 0)
			baseArmBash = cp.baseArmBash;

		baseArmPierce = 100;
		if (cp.baseArmPierce > 0)
			baseArmPierce = cp.baseArmPierce;

		baseResFrost = 0;
		if (cp.baseResFrost > 0)
			baseResFrost = cp.baseResFrost;

		baseResFire = 0;
		if (cp.baseResFire > 0)
			baseResFire = cp.baseResFire;

		baseResLightning = 0;
		if (cp.baseResLightning > 0)
			baseResLightning = cp.baseResLightning;

		baseResAcid = 0;
		if (cp.baseResAcid > 0)
			baseResAcid = cp.baseResAcid;

		baseResGood = 0;
		if (cp.baseResGood > 0)
			baseResGood = cp.baseResGood;

		baseResEvil = 0;
		if (cp.baseResEvil > 0)
			baseResEvil = cp.baseResEvil;
		
		movement = cp.movement;
		hitname = cp.hitname;
		
		align = 0;
		if (cp.align > 0)
			align = cp.align;
		
		for (Lootgroup l : cp.sells)
			sells.add(l);
		if (flags.get("shopkeeper"))
			for (Lootgroup l : sells)
				for (ObjProto op : l.contents.keySet())
				{
					boolean found = false;
					for (ObjData o : objects)
						if (o.op == op)
						{
							found = true;
							break;
						}
					if (!found)
					{
						ObjData tempOb = (new ObjData(op));
						tempOb.toChar(this);
					}
				}
		

		initializeValues();
		// For any mob above level 10 that isn't elite, randomly adjust the level for variety.
		int targetLevel = newCp.level;
		if (targetLevel > 10 && difficulty < 5)
			targetLevel += (gen.nextInt(5)-2);
		StaffCommands.doAdvance(conn, "self "+targetLevel);
			
		if (difficulty == 2)
			baseMaxHp += (int)(baseMaxHp*0.7);
		else if (difficulty == 3)
			baseMaxHp += (int)(baseMaxHp*1.5);
		else if (difficulty == 4)
			baseMaxHp += (int)(baseMaxHp*4);
		else if (difficulty == 5)
			baseMaxHp += (int)(baseMaxHp*14);
		
		baseMaxMana = baseMaxMana*difficulty*(difficulty/3);

		baseMaxEnergy = level+100;

		hp = maxHp();
		mana = maxMana();
		energy = maxEnergy();
		
		for (Skill s : skills)
			if (s.availAt(charClass) > 0 && s.availAt(charClass) <= level)
				learned.put(s, 75+difficulty*5);
		if (level < 10)
			learned.put(Skill.lookup("hand to hand"), 10+level*6);
		else
			learned.put(Skill.lookup("hand to hand"), 75+difficulty*5);

		for (String e : Flags.charEffects)
			if (newCp.effects.get(e))
			{
				Effect newE = new Effect(e, level, -1);
				newE.autoMods(this);
				effects.add(newE);
			}
	}
	
	public boolean create()
	{
		id = 0;
		try
		{
			Database.dbQuery.executeUpdate("INSERT INTO characters (character_user) VALUES ("+conn.id+")");
			ResultSet dbResult = Database.dbQuery.executeQuery("SELECT MAX(character_id) AS new_id FROM characters");
			dbResult.next();
			id = dbResult.getInt("new_id");
		} catch (Exception e) {
			sysLog("bugs", "Error in createCharacter: "+e.getMessage());
			logException(e);
		}
		return true;
	}

	public void load(boolean loadObjects)
	{
		try
		{
			ResultSet dbResult = Database.dbQuery.executeQuery(""+
					"SELECT * FROM characters "+
					"WHERE character_id = "+id);
			if (dbResult.next())
			{
				cp = CharProto.lookup(dbResult.getInt("character_proto"));
				name = dbResult.getString("character_name");
				shortName = dbResult.getString("character_short");
				longName = dbResult.getString("character_long");
				description = dbResult.getString("character_description");
				
				String dString = dbResult.getString("character_extradescs");
				String tempDesc = "";
				for (String s : dString.split(";"))
				{
					if (tempDesc.length() == 0)
						tempDesc = s.replace("SPECSEMICOLON", ";");
					else
					{
						eds.put(tempDesc, s.replace("SPECSEMICOLON", ";"));
						tempDesc = "";
					}
				}

				String tempVariables = dbResult.getString("character_variables");
				for (String s : tempVariables.split(";"))
				{
					String tempVar[] = s.replace("SPECSEMICOLON", ";").split("=", 2);
					if (tempVar.length > 1)
						variables.put(tempVar[0], tempVar[1]);
				}

				String tempTriggers = dbResult.getString("character_triggers");
				int ctr = 0;
				Trigger tempTrigger = new Trigger();
				for (String s : tempTriggers.split(";", -1))
				{
					ctr++;
					switch (ctr)
					{
						case 1:
							tempTrigger = new Trigger();
							tempTrigger.type = s;
							break;
						case 2:
							tempTrigger.numArg = Fmt.getInt(s);
							break;
						case 3:
							tempTrigger.arg = s.replace("SPECSEMICOLON", ";");
							break;
						case 4:
							tempTrigger.mprog = MobProg.lookup(Fmt.getInt(s));
							if (tempTrigger.mprog != null)
								triggers.add(tempTrigger);
							ctr = 0;
							break;
					}
				}
				
				if (dbResult.getString("character_sex").equals("m"))
					sex = "m";
				else
					sex = "f";
					
				currentRoom = Room.lookup(dbResult.getInt("character_room"));
				if (currentRoom == null)
					currentRoom = rooms.get(0);
				charRace = Race.lookup(dbResult.getInt("character_race"));
				if (charRace == null)
					charRace = races.get(0);
				charClass = CharClass.lookup(dbResult.getInt("character_class"));
				if (charClass == null)
					charClass = classes.get(0);

				level = dbResult.getInt("character_level");
				gold = dbResult.getInt("character_gold");
				bank = dbResult.getInt("character_bank");
				tnl = dbResult.getInt("character_tnl");
				trains = dbResult.getInt("character_trains");
				
				for (String s : Flags.charFlags)
					flags.put(s, false);
				String fString = dbResult.getString("character_flags");
				for (String s : Flags.charFlags)
					if (fString.indexOf(";"+s+";") > -1 || fString.startsWith(s+";"))
						flags.put(s, true);
				
				String eString[] = dbResult.getString("character_effects").split(";");
				for (String ef : eString)
				{
					String part[] = ef.split("\\|");
					if (part.length < 3)
						continue;
					Effect newE = new Effect(part[0], Fmt.getInt(part[1]), Fmt.getInt(part[2]));
					for (ctr = 3; part.length > ctr; ctr++)
					{
						String subpart[] = part[ctr].split("=");
						if (subpart.length == 2)
							newE.statMods.put(subpart[0], Fmt.getInt(subpart[1]));
					}
					effects.add(newE);
				}
				
				position = dbResult.getString("character_position");
				int tempPosTarget = dbResult.getInt("character_position_target");

				hp = dbResult.getInt("character_hp");
				baseMaxHp = dbResult.getInt("character_base_max_hp");
				if (baseMaxHp < 1)
					baseMaxHp = 1;
				mana = dbResult.getInt("character_mana");
				baseMaxMana = dbResult.getInt("character_base_max_mana");
				if (baseMaxMana < 1)
					baseMaxMana = 1;
				energy = dbResult.getInt("character_energy");
				baseMaxEnergy = dbResult.getInt("character_base_max_energy");
				if (baseMaxEnergy < 1)
					baseMaxEnergy = 1;
				baseStr = dbResult.getInt("character_base_str");
				baseDex = dbResult.getInt("character_base_dex");
				baseCon = dbResult.getInt("character_base_con");
				baseInt = dbResult.getInt("character_base_int");
				baseCha = dbResult.getInt("character_base_cha");
				baseArmSlash = dbResult.getInt("character_base_arm_slash");
				baseArmBash = dbResult.getInt("character_base_arm_bash");
				baseArmPierce = dbResult.getInt("character_base_arm_pierce");
				baseResFrost = dbResult.getInt("character_base_res_frost");
				baseResFire = dbResult.getInt("character_base_res_fire");
				baseResLightning = dbResult.getInt("character_base_res_lightning");
				baseResAcid = dbResult.getInt("character_base_res_acid");
				baseResGood = dbResult.getInt("character_base_res_good");
				baseResEvil = dbResult.getInt("character_base_res_evil");
				
				movement = dbResult.getString("character_movement");
				hitname = dbResult.getString("character_hitname");
				align = dbResult.getInt("character_align");

				for (String s : dbResult.getString("character_sells").split(";"))
				{
					Lootgroup tempLg = Lootgroup.lookup(Fmt.getInt(s));
					if (tempLg != null)
						sells.add(tempLg);
				}

				for (String s : dbResult.getString("character_learned").split(";"))
				{
					if (s.split(":").length == 2)
					{
						String left = s.split(":")[0];
						String right = s.split(":")[1];
						Skill tempSkill = Skill.lookup(left);
						if (tempSkill != null)
							learned.put(tempSkill, Fmt.getInt(right));
					}
				}

				for (String s : dbResult.getString("character_cooldowns").split(";"))
				{
					if (s.split(":").length == 2)
					{
						String left = s.split(":")[0];
						String right = s.split(":")[1];
						Skill tempSkill = Skill.lookup(left);
						if (tempSkill != null)
							cooldowns.put(tempSkill, Fmt.getInt(right));
					}
				}
				
				// Add all skills for staff testing.
				if (conn.hasPermission("staff"))
					for (Skill sk : skills)
						learned.put(sk, 100);

				resetFilled = dbResult.getInt("character_reset");
				
				if (loadObjects)
				{
					Database.loadCharObjects(this);
					
					for (ObjData o : currentRoom.objects)
						if (o.id == tempPosTarget)
							positionTarget = o;
					for (ObjData o : objects)
						if (o.id == tempPosTarget)
							positionTarget = o;
					if (positionTarget == null && tempPosTarget != 0)
						position = "standing";
				}
			}
		} catch (Exception e) {
			sysLog("bugs", "Error in CharData.Character(UserCon): "+e.getMessage());
			logException(e);
		}
	}

	public void save()
	{
		if (conn.isDummy)
			return;

		String dString = "";
		for (String d : eds.keySet())
			dString = dString+d.replace(";", "SPECSEMICOLON")+";"+eds.get(d).replace(";", "SPECSEMICOLON")+";";

		String vString = "";
		for (String s : variables.keySet())
			vString = vString+s.replace(";", "SPECSEMICOLON")+"="+variables.get(s).replace(";", "SPECSEMICOLON")+";";
		
		String tString = "";
		for (Trigger t : triggers)
			tString = tString+t.type+";"+t.numArg+";"+t.arg.replace(";", "SPECSEMICOLON")+";"+t.mprog.id+";";

		String fString = "";
		for (String s : Flags.charFlags)
			if (flags.get(s) != null)
				if (flags.get(s))
					fString = fString+s+";";
		
		String eString = "";
		for (Effect e : effects)
		{
			eString = eString+e.name+"|"+e.level+"|"+e.duration;
			for (String s : e.statMods.keySet())
				eString = eString+"|"+s+"="+e.statMods.get(s);
			eString = eString+";";
		}
		
		String ptString = "0";
		if (positionTarget != null)
			ptString = ""+positionTarget.id;

		String lString = "";
		for (Lootgroup l : sells)
			lString = lString+l.id+";";
		
		String lrString = "";
		for (Skill s : learned.keySet())
			lrString = lrString+s.name+":"+learned.get(s)+";";

		String cString = "";
		for (Skill s : cooldowns.keySet())
			cString = cString+s.name+":"+cooldowns.get(s)+";";

		try
		{
			Database.dbQuery.executeUpdate("UPDATE characters SET character_room = "+currentRoom.id+
					", character_name = '"+Database.dbSafe(name)+"'"+
					", character_short = '"+Database.dbSafe(shortName)+"'"+
					", character_long = '"+Database.dbSafe(longName)+"'"+
					", character_description = '"+Database.dbSafe(description)+"'"+
					", character_extradescs = '"+Database.dbSafe(dString)+"'"+
					", character_triggers = '"+Database.dbSafe(tString)+"'"+
					", character_variables = '"+Database.dbSafe(vString)+"'"+
					", character_sex = '"+Database.dbSafe(sex)+"'"+
					", character_race = "+charRace.id+
					", character_class = "+charClass.id+
					", character_level = "+level+
					", character_gold = "+gold+
					", character_bank = "+bank+
					", character_tnl = "+tnl+
					", character_trains = "+trains+
					", character_difficulty = "+difficulty+
					", character_flags = '"+Database.dbSafe(fString)+"'"+
					", character_effects = '"+Database.dbSafe(eString)+"'"+
					", character_position = '"+Database.dbSafe(position)+"'"+
					", character_position_target = "+ptString+
					", character_hp = "+hp+
					", character_base_max_hp = "+baseMaxHp+
					", character_mana = "+mana+
					", character_base_max_mana = "+baseMaxMana+
					", character_energy = "+energy+
					", character_base_max_energy = "+baseMaxEnergy+
					", character_base_str = "+baseStr+
					", character_base_dex = "+baseDex+
					", character_base_con = "+baseCon+
					", character_base_int = "+baseInt+
					", character_base_cha = "+baseCha+
					", character_base_arm_slash = "+baseArmSlash+
					", character_base_arm_bash = "+baseArmBash+
					", character_base_arm_pierce = "+baseArmPierce+
					", character_base_res_frost = "+baseResFrost+
					", character_base_res_fire = "+baseResFire+
					", character_base_res_lightning = "+baseResLightning+
					", character_base_res_acid = "+baseResAcid+
					", character_base_res_good = "+baseResGood+
					", character_base_res_evil = "+baseResEvil+
					", character_movement = '"+Database.dbSafe(movement)+"'"+
					", character_hitname = '"+Database.dbSafe(hitname)+"'"+
					", character_sells = '"+Database.dbSafe(lString)+"'"+
					", character_learned = '"+Database.dbSafe(lrString)+"'"+
					", character_cooldowns = '"+Database.dbSafe(cString)+"'"+
					", character_reset = "+resetFilled+
					", character_align = "+align+
					" WHERE character_id = "+id);
			
			Database.saveCharObjects(this);

			if (!conn.isDummy)
				Database.dbQuery.executeUpdate("UPDATE characters SET character_user = "+conn.id+" WHERE character_id = "+id);
			if (cp != null)
				Database.dbQuery.executeUpdate("UPDATE characters SET character_proto = "+cp.id+" WHERE character_id = "+id);
		} catch (Exception e) {
			sysLog("bugs", "Error in CharData.save: "+e.getMessage());
			logException(e);
		}
	}
	
	/**
	Load all saved mobs from the database and create characters for each of them.
	<p>
	These entries are created when the MUD shuts down gracefully in order to preserve
	mob status. Any values in the database override values defined by the CharProto
	associated with the mob.
	*/
	public static void loadSavedMobs()
	{
		ArrayList<Integer> loadIds = new ArrayList<Integer>();
		try
		{
			ResultSet dbResult = Database.dbQuery.executeQuery("SELECT * FROM characters WHERE character_user = 0");
			while (dbResult.next())
			{
				loadIds.add(dbResult.getInt("character_id"));
			}
			for (Integer i : loadIds)
			{
				CharData newChar = new CharData(i);
				mobs.add(newChar);
			}
			Database.dbQuery.executeUpdate("DELETE FROM characters WHERE character_user = 0");
			
			for (CharData ch : mobs)
				Database.loadCharObjects(ch);
		} catch (Exception e) {
			sysLog("bugs", "Error in loadSavedMobs: "+e.getMessage());
			logException(e);
		}
	}
	
	/**
	Save all loaded mobs to the database to be loaded next time the MUD starts up.
	*/
	public static void saveLoadedMobs()
	{
		try
		{
			for (CharData ch : mobs)
			{
				boolean fixId = false;
				String idString = "NULL";
				if (ch.id > 0)
					idString = ""+ch.id;
				else
					fixId = true;
		
				String dString = "";
				for (String d : ch.eds.keySet())
					dString = dString+d.replace(";", "SPECSEMICOLON")+";"+ch.eds.get(d).replace(";", "SPECSEMICOLON")+";";
		
				String vString = "";
				for (String s : ch.variables.keySet())
					vString = vString+s.replace(";", "SPECSEMICOLON")+"="+ch.variables.get(s).replace(";", "SPECSEMICOLON")+";";

				String tString = "";
				for (Trigger t : ch.triggers)
					tString = tString+t.type+";"+t.numArg+";"+t.arg.replace(";", "SPECSEMICOLON")+";"+t.mprog.id+";";
		
				String fString = "";
				for (String s : Flags.charFlags)
					if (ch.flags.get(s) != null)
						if (ch.flags.get(s))
							fString = fString+s+";";
				
				String eString = "";
				for (Effect e : ch.effects)
				{
					eString = eString+e.name+"|"+e.level+"|"+e.duration;
					for (String s : e.statMods.keySet())
						eString = eString+"|"+s+"="+e.statMods.get(s);
					eString = eString+";";
				}

				String ptString = "0";
				if (ch.positionTarget != null)
					ptString = ""+ch.positionTarget.id;

				String lString = "";
				for (Lootgroup l : ch.sells)
					lString = lString+l.id+";";
				
				String lrString = "";
				for (Skill s : ch.learned.keySet())
					lrString = lrString+s.name+":"+ch.learned.get(s)+";";

				String cString = "";
				for (Skill s : ch.cooldowns.keySet())
					cString = cString+s.name+":"+ch.cooldowns.get(s)+";";
		
				Database.dbQuery.executeUpdate("INSERT INTO characters VALUES ("+idString+", "+
						"0, "+
						ch.cp.id+", "+
						"'"+Database.dbSafe(ch.name)+"',"+
						"'"+Database.dbSafe(ch.shortName)+"', "+
						"'"+Database.dbSafe(ch.longName)+"', "+
						"'"+Database.dbSafe(ch.description)+"', "+
						"'"+Database.dbSafe(dString)+"', "+
						"'"+Database.dbSafe(vString)+"', "+
						"'"+Database.dbSafe(tString)+"', "+
						"'"+Database.dbSafe(ch.sex)+"', "+
						ch.currentRoom.id+", "+
						ch.charRace.id+", "+
						ch.charClass.id+", "+
						ch.level+", "+
						ch.gold+", "+
						ch.bank+", "+
						"0, "+
						ch.trains+", "+
						ch.difficulty+", "+
						"'"+Database.dbSafe(fString)+"', "+
						"'"+Database.dbSafe(eString)+"', "+
						"'"+Database.dbSafe(ch.position)+"', "+
						ptString+", "+
						ch.hp+", "+
						ch.baseMaxHp+", "+
						ch.mana+", "+
						ch.baseMaxMana+", "+
						ch.energy+", "+
						ch.baseMaxEnergy+", "+
						ch.baseStr+", "+
						ch.baseDex+", "+
						ch.baseCon+", "+
						ch.baseInt+", "+
						ch.baseCha+", "+
						ch.baseArmSlash+", "+
						ch.baseArmBash+", "+
						ch.baseArmPierce+", "+
						ch.baseResFrost+", "+
						ch.baseResFire+", "+
						ch.baseResLightning+", "+
						ch.baseResAcid+", "+
						ch.baseResGood+", "+
						ch.baseResEvil+", "+
						"'"+Database.dbSafe(ch.movement)+"', "+
						"'"+Database.dbSafe(ch.hitname)+"', "+
						"'"+Database.dbSafe(lString)+"', "+
						"'"+Database.dbSafe(lrString)+"', "+
						"'"+Database.dbSafe(cString)+"', "+
						ch.resetFilled+", "+
						ch.align+")");

				if (fixId)
				{
					ResultSet dbResult = Database.dbQuery.executeQuery("SELECT MAX(character_id) AS max_id FROM characters");
					if (dbResult.next())
						ch.id = dbResult.getInt("max_id");
				}

				Database.saveCharObjects(ch);
			}
		} catch (Exception e) {
			sysLog("bugs", "Error in saveLoadedMobs: "+e.getMessage());
			logException(e);
		}
	}

	/**
	Run automatic update tasks for this mob.
	<p>
	This method is called by the {@link MudMain.MudRunner#run() MudRunner} method at
	regular intervals. It will check for any periodic action to be performed by the mob,
	such as random wandering movement.
	*/
	public void update()
	{
		if (conn.realCh != null)
			return;
		
		if (flags.get("janitor"))
		{
			ArrayList<ObjData> toDelete = new ArrayList<ObjData>();
			for (ObjData o : currentRoom.objects)
				if (o.lastTouched > 3600)
					if (!o.flags.get("notake"))
						toDelete.add(o);
			for (ObjData o : toDelete)
			{
				Fmt.actAround(this, null, o, "$n disposes of $o.");
				o.clearObjects();
			}
		}
		
		if (flags.get("shopkeeper"))
		{
			for (ObjData o : objects)
			{
				if (o.lastTouched < 14400)
					continue;
				
				boolean stocked = false;
				for (Lootgroup l : sells)
					for (ObjProto op : l.contents.keySet())
						if (op == o.op)
							stocked = true;
				
				if (!stocked)
					o.clearObjects();
			}
		}
		
		if (gen.nextInt(8) == 0)
		{
			ArrayList<Exit> validExits = new ArrayList<Exit>();
			for (Exit ex : currentRoom.exits)
				if (!ex.flags.get("closed")
					&& !ex.flags.get("hidden")
					&& !flags.get("stayroom")
					&& !(flags.get("staysector") && !currentRoom.sector.equals(ex.to.sector))
					&& !(flags.get("stayarea") && currentArea() != ex.to.getArea())
					&& !ex.to.flags.get("nomob")
					)
					validExits.add(ex);
			if (validExits.size() > 0)
				mobCommand(validExits.get(gen.nextInt(validExits.size())).direction);
		}
	}
	
	public void regen(boolean regenEnergy)
	{
		int tempMaxHp = maxHp();
		int tempHpGain = 0;
		if (fighting == null)
			tempHpGain += Math.ceil(0.001666*tempMaxHp);
		tempHpGain += hpRegen();
		if (positionTarget != null)
			if (Fmt.getInt(positionTarget.value2) > 0)
				tempHpGain = (int)(tempHpGain*(1.0+Fmt.getInt(positionTarget.value2)/100.0));
		hp += tempHpGain;
		if (hp > tempMaxHp)
			hp = tempMaxHp;
		
		int tempMaxMana = maxMana();
		int tempManaGain = 0;
		tempManaGain += Math.ceil(0.001666*tempMaxMana);
		tempManaGain += manaRegen();
		if (positionTarget != null)
			if (Fmt.getInt(positionTarget.value3) > 0)
				tempManaGain = (int)(tempManaGain*(1.0+Fmt.getInt(positionTarget.value3)/100.0));
		mana += tempManaGain;
		if (mana > tempMaxMana)
			mana = tempMaxMana;

		int tempMaxEnergy = maxEnergy();
		int tempEnergyGain = 0;
		tempEnergyGain += energyRegen();
		if (regenEnergy)
			tempEnergyGain += tempMaxEnergy*0.1;
		if (positionTarget != null)
			if (Fmt.getInt(positionTarget.value4) > 0)
				tempEnergyGain = (int)(tempEnergyGain*(1.0+Fmt.getInt(positionTarget.value4)/100.0));
		energy += tempEnergyGain;
		if (energy > tempMaxEnergy)
			energy = tempMaxEnergy;
	}
	
	public void updateEffects()
	{
		boolean checkPos = false;
		for (int ctr = 0; ctr < effects.size(); ctr++)
		{
			Effect e = effects.get(ctr);
			if (e.duration == -1)
				continue;
			e.duration -= 10;
			if (e.duration <= 0)
			{
				if (e.name.equals("eating"))
				{
					sendln("You finish eating.");
					checkPos = true;
				}
				else if (e.name.equals("drinking"))
				{
					checkPos = true;
					sendln("You finish drinking.");
				}
				else
				{
					sendln("Your '"+e.name+"' effect has worn off.");
					Fmt.actAround(this, null, null, "$n's '"+e.name+"' effect has worn off.");
				}
				effects.remove(e);
				ctr--;
			}
			if (e.duration % 30 == 0)
			{
				e.tick(this);
			}
		}
		
		if (checkPos)
		{
			for (Effect e : effects)
				if (e.name.equals("eating") || e.name.equals("drinking"))
				{
					checkPos = false;
					break;
				}
			
			if (checkPos && lastPosition.length() > 0)
			{
				if (lastPosition.equals("standing") && position != lastPosition)
					ObjectCommands.doStand(conn, "");
				else if (lastPosition.equals("sitting") && position != lastPosition)
					ObjectCommands.doSit(conn, "");
				else if (lastPosition.equals("resting") && position != lastPosition)
					ObjectCommands.doRest(conn, "");
				else if (lastPosition.equals("sleeping") && position != lastPosition)
					ObjectCommands.doSleep(conn, "");
				lastPosition = "";
			}
		}

		for (Skill s : cooldowns.keySet())
		{
			cooldowns.put(s, cooldowns.get(s)-1);
			if (cooldowns.get(s) <= 0)
			{
				cooldowns.remove(s);
				sendln("You can now use the '"+s.name+"' "+s.type+" again.");
			}
		}
	}
	
	/**
	Run a command as a mob as if it were a player.
	<p>
	This method will cause the mob to assign the {@link MudMain#dummyCon dummyCon} to
	itself, and then execute {@code commandLine} as if it were a player entering the
	same command.
	
	@param commandLine The command to run.
	*/
	public void mobCommand(String commandLine, UserCon dummy)
	{
		String tempName = name;
		name = shortName;
		dummy.ch = this;
		dummy.commandQueue.add(commandLine);
		dummy.processCommand();
		name = tempName;
		if (dummy != conn)
			dummy.ch = null;
	}
	
	public void mobCommand(String commandLine)
	{
		mobCommand(commandLine, conn);
	}
	
	/**
	A convenience method to send {@code msg} to the connection associated with this
	character.
	<p>
	It will check to make sure there is an active connection associated with the
	character before it redirects to the {@link UserCon#send(String) UserCon.send}
	method.
	
	@param msg The message to send to the character's connection.
	*/
	public void send(String msg)
	{
		if (conn.isDummy)
			return;
		conn.send(msg);
	}
	
	/**
	A convenience method to send {@code msg} to the connection associated with this
	character.
	<p>
	It will check to make sure there is an active connection associated with the
	character before it redirects to the {@link UserCon#sendln(String) UserCon.sendln}
	method.
	
	@param msg The message to send to the character's connection.
	*/
	public void sendln(String msg)
	{
		if (conn.isDummy)
			return;
		conn.sendln(msg);
	}
	
	/**
	Award experience to this character for killing {@code victim}, using formulas.
	<p>
	This uses the {@link Formulas#expGain(CharData, CharData) Formulas.expGain} method
	to calculate the amount of experience to award to this character. If this is an
	NPC, no experience is gained.
	<p>
	This also checks the character's {@code tnl} value after the experience is awarded
	and performs any necessary level gain actions.
	*/
	public void awardExp(int gain)
	{
		if (level >= Flags.maxPlayableLevel && !conn.isDummy)
			return;

		if (gain == 0)
			return;
		
		tnl -= gain;
		sendln("{5You gain {W"+gain+" {5xp.{x");

		while (tnl < 1 && (level < Flags.maxPlayableLevel || conn.isDummy))
		{
			level++;
			int hpGain = Formulas.hpGain(level, maxCon());
			baseMaxHp += hpGain;
			hpGain += (int)(baseCon/2.0);
			int manaGain = Formulas.manaGain(level, maxInt());
			baseMaxMana += manaGain;
			manaGain += (int)(baseInt/2.0);
			int energyGain = 1;
			if (level == Flags.maxPlayableLevel)
				energyGain = 2;
			baseMaxEnergy += energyGain;

			if (level >= Flags.maxPlayableLevel && !conn.isDummy)
				tnl = 0;
			else
				tnl += Formulas.tnl(this);

			sendln("{5You have progressed to level "+level+"!{x");
			sendln("{5Your base stats have increased by {W"+hpGain+"{5 hp, {W"+manaGain+"{5 mana, and {W"+energyGain+"{5 energy.{x");
			if (level % 5 == 0)
			{
				if (conn.isDummy)
				{
					String statName = "";
					int totalBase = charRace.baseStr+charRace.baseDex+charRace.baseCon+charRace.baseInt+charRace.baseCha;
					if (totalBase == 0)
						switch (gen.nextInt(5))
						{
							case 0:
								statName = "strength";
								baseStr++;
								break;
							case 1:
								statName = "dexterity";
								baseDex++;
								break;
							case 2:
								statName = "constitution";
								baseCon++;
								break;
							case 3:
								statName = "intelligence";
								baseCon++;
								break;
							default:
								statName = "charisma";
								baseCha++;
								break;
						}
					while (statName.length() == 0)
					{
						int newStat = gen.nextInt(totalBase)+1;
						if (newStat <= charRace.baseStr && baseStr < 75)
						{
							statName = "strength";
							baseStr++;
							break;
						}
						else
							newStat -= charRace.baseStr;
						if (newStat <= charRace.baseDex && baseDex < 75)
						{
							statName = "dexterity";
							baseDex++;
							break;
						}
						else
							newStat -= charRace.baseDex;
						if (newStat <= charRace.baseCon && baseCon < 75)
						{
							statName = "constitution";
							baseCon++;
							break;
						}
						else
							newStat -= charRace.baseCon;
						if (newStat <= charRace.baseInt && baseInt < 75)
						{
							statName = "intelligence";
							baseInt++;
							break;
						}
						else
							newStat -= charRace.baseInt;
						if (newStat <= charRace.baseCha && baseCha < 75)
						{
							statName = "charisma";
							baseCha++;
							break;
						}
					}
					sendln("{5Your {W"+statName+"{5 has increased.{x");
				}
				else
				{
					trains++;
					sendln("{5You have gained one training point. Use }H'}htrain }H<}iattribute}H>' {5to apply it.{x");
				}
			}
		}
		
		save();
	}
	
	public void restore()
	{
		hp = maxHp();
		mana = maxMana();
		energy = maxEnergy();
		
		for (int ctr = effects.size()-1; ctr >= 0; ctr--)
		{
			for (String s : Flags.badEffects)
				if (effects.get(ctr).name.equals(s))
				{
					effects.remove(ctr);
					break;
				}
		}
	}
	
	/**
	Use the character's {@code currentRoom} to find their current area.
	<p>
	This searches through the {@link MudMain#areas areas} global ArrayList to find an
	area which includes the ID of the character's {@code currentRoom}.
	
	@return The character's current area, or {@code null} if their room does not fit in
		any area's range.
	*/
	public Area currentArea()
	{
		for (Area a : areas)
			if (a.start <= currentRoom.id && a.end >= currentRoom.id)
				return a;
		return null;
	}
	
	public ObjData getWearloc(String wearloc)
	{
		for (ObjData o : objects)
			for (String t : Flags.objTypes.keySet())
				if (o.wearloc.equals(wearloc))
					return o;
		return null;
	}
	
	public boolean nameMatches(String arg)
	{
		String argSplit[] = arg.split(" ");
		
		for (String key : argSplit)
			if (name.toLowerCase().indexOf(" "+key) == -1
				&& !name.toLowerCase().startsWith(key))
					return false;
		return true;
	}
	
	public int maxHp()
	{
		int conMod = (int)(maxCon()*(level/1.5));
		return baseMaxHp+getStatMod("hp")+conMod;
	}

	public int maxMana()
	{
		int intMod = (int)(maxInt()*(level/1.5));
		return baseMaxMana+getStatMod("mana")+intMod;
	}

	public int maxEnergy()
	{
		return baseMaxEnergy+getStatMod("energy");
	}
	
	public int maxStr()
	{
		return Math.min(baseStr+getStatMod("strength"), 100);
	}
	
	public int maxDex()
	{
		return Math.min(baseDex+getStatMod("dexterity"), 100);
	}

	public int maxCon()
	{
		return Math.min(baseCon+getStatMod("constitution"), 100);
	}

	public int maxInt()
	{
		return Math.min(baseInt+getStatMod("intelligence"), 100);
	}

	public int maxCha()
	{
		return Math.min(baseCha+getStatMod("charisma"), 100);
	}

	public int maxArmSlash()
	{
		return baseArmSlash+getStatMod("armslash");
	}

	public int maxArmBash()
	{
		return baseArmBash+getStatMod("armbash");
	}

	public int maxArmPierce()
	{
		return baseArmPierce+getStatMod("armpierce");
	}

	public int maxResFrost()
	{
		return baseResFrost+getStatMod("resfrost");
	}

	public int maxResFire()
	{
		return baseResFire+getStatMod("resfire");
	}

	public int maxResLightning()
	{
		return baseResLightning+getStatMod("reslightning");
	}

	public int maxResAcid()
	{
		return baseResAcid+getStatMod("resacid");
	}

	public int maxResGood()
	{
		return baseResGood+getStatMod("resgood");
	}

	public int maxResEvil()
	{
		return baseResEvil+getStatMod("resevil");
	}
	
	public int hpRegen()
	{
		if (position.equals("standing") || position.equals("sleeping"))
			for (Effect e : effects)
				if (e.name.equals("eating"))
				{
					effects.remove(e);
					sendln("Your eating has been interrupted by "+position+".");
					break;
				}
		int temp = getStatMod("hp_regen");
		return temp;
	}

	public int manaRegen()
	{
		if (position.equals("standing") || position.equals("sleeping"))
			for (Effect e : effects)
				if (e.name.equals("drinking"))
				{
					effects.remove(e);
					sendln("Your drinking has been interrupted by "+position+".");
					break;
				}
		int temp = getStatMod("mn_regen");
		
		return temp;
	}

	public int energyRegen()
	{
		int temp = getStatMod("en_regen");
		
		return temp;
	}
	
	public int getStatMod(String stat)
	{
		int temp = 0;
		for (Effect e : effects)
			if (e.statMods.containsKey(stat))
				temp += e.statMods.get(stat);

		for (ObjData o : objects)
			if (!o.wearloc.equals("none"))
			{
				if (o.statMods.containsKey(stat))
					temp += o.statMods.get(stat);
				for (Effect e : o.effects)
					if (e.statMods.containsKey(stat))
						temp += e.statMods.get(stat);
			}
		return temp;
	}
	
	public void doJog()
	{
		if (usingJog.length() == 0)
			return;
		
		if (fighting != null)
		{
			sendln("Jog interrupted.");
			usingJog = "";
			return;
		}
		
		String nr = "";
		while (usingJog.charAt(0) >= '0' && usingJog.charAt(0) <= '9')
		{
			nr = nr+usingJog.charAt(0);
			usingJog = usingJog.substring(1);
			if (usingJog.length() == 0)
			{
				sendln("Error in jog format - a number must always be followed by a direction.");
				return;
			}
		}
		
		char toJog = usingJog.charAt(0);
		if (nr.length() > 0 && Fmt.getInt(nr) > 1)
			usingJog = (Fmt.getInt(nr)-1)+usingJog;
		else
			usingJog = usingJog.substring(1);
		if (usingJog.length() == 0)
			usingJog = "/";
		
		String dir = "";
		switch (toJog)
		{
			case 'n':
				dir = "north";
				break;
			case 'e':
				dir = "east";
				break;
			case 's':
				dir = "south";
				break;
			case 'w':
				dir = "west";
				break;
			case 'u':
				dir = "up";
				break;
			case 'd':
				dir = "down";
				break;
			case 'p':
				dir = "portal";
				break;
			default:
				sendln("Invalid character found in jog: '"+toJog+"'");
				usingJog = "";
				return;
		}
		
		if (!currentRoom.takeExit(conn, dir+" forjog"))
		{
			if (dir.equals("portal"))
			{
				for (ObjData o : currentRoom.objects)
					if (o.type.equals("portal") && Combat.canSee(this, o))
					{
						o.use(conn);
						return;
					}
			}
			else
			{
				sendln("You can't go "+dir+" from here.");
				usingJog = "";
			}
			return;
		}
		if (usingJog.equals("/"))
		{
			usingJog = "";
			sendln("You have finished jogging.");
			return;
		}
	}
	
	public int skillPercent(Skill s)
	{
		if (s == null)
			return 0;
		if (learned.get(s) == null)
			return 0;
		return learned.get(s);
	}
	
	public String getMovementVerb()
	{
		if (fleeing)
			return "flee";
		if (Effect.findEffect(effects, "levitation") != null)
			return "float";
		if (usingJog.length() > 0)
			return "jog";
		if (movement.length() > 0)
			return movement;
		if (charRace != null)
			if (charRace.movement.length() > 0)
				return charRace.movement;
		return "walk";
	}

	public int checkTrigger(String type, CharData actor, CharData victim, String args, int numArg)
	{
		return checkTrigger(type, actor, victim, args, numArg, null);
	}
	public int checkTrigger(String type, CharData actor, CharData victim, String args, int numArg, HashMap<String, String> variables)
	{
		int retVal = -1;
		for (Trigger t : triggers)
			if (t.type.equals(type) && retVal == -1)
				if (t.validate(args, numArg))
					retVal = t.mprog.run(t, args, null, this, null, actor, victim, 0, 0, 0, variables);
		return retVal;
	}
	
	public void clearQueueOf(CharData target)
	{
		int cleared = 0;
		int offset = 0;
		int ctr = 0;
		
		if (combatQueue.size() <= 1)
			return;
		
		if (combatQueue.size() % 2 == 1)
			offset = 1;
		
		if (combatQueue.size() < targetQueue.size()*2)
			ctr = 1;
		
		for (; ctr < targetQueue.size(); ctr++)
		{
			if (target == targetQueue.get(ctr))
			{
				cleared++;
				combatQueue.remove(ctr*2+offset);
				combatQueue.remove(ctr*2+offset);
				targetQueue.remove(ctr);
				ctr--;
			}
		}
		
		sendln(cleared+" abilities have been removed from your combat queue.");
	}
}