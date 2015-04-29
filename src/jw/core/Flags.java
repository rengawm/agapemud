package jw.core;
import java.util.*;

import jw.commands.*;
import jw.data.*;
import jw.core.*;

/**
	The Flags class contains definitions of valid flags as well as methods for
	manipulating and displaying flags.
*/
public class Flags
{
	public static int minLevel = 1;
	public static int easyDeathLevel = 20;
	public static int maxPlayableLevel = 100;
	public static int maxLevel = 110;
	public static int inventoryCapacity = 20;
	public static int minAlign = -1000;
	public static int maxAlign = 1000;

	public static int corpseId = 100000;
	public static int coinsId = 100001;
	public static int makeFoodId = 100002;
	public static int makeLightId = 100003;
	public static int makeWaterId = 100004;
	public static int springId = 100005;
	public static int mannaId = 100006;

	public static int recallId = 1;
	public static int infirmaryId = 100000;
	public static int jailId = 100001;
	
	public static ObjData hands = new ObjData();
	
	/** The list of permission groups which can be assigned to commands/players. */
	public static String[] permissionTypes = {"staff", "builder", "coder", "admin"};
	
	public static String[] userPrefs = {
			"brief",			"compact",			"noloot",
			"ansi",				"savetells",		"showemail",
			"witness",			"prompt",			"autoloot",
			"nofollow",			"autoassist",		"pvp"};

	public static String[] staffLogs = {
			"bugs",				"commands",			"system",
			"connections",		"justice",			"progs",
			"exceptions"};

	public static String[] charPositions = {
			"standing",			"sitting",			"sleeping",
			"resting"};
	
	public static String[] charMovements = {
			"amble",
			"charge",
			"crawl",
			"creep",
			"dart",
			"flutter",
			"gallop",
			"glide",
			"limp",
			"move",
			"pound",
			"prowl",
			"rumble",
			"run",
			"shamble",
			"skitter",
			"slink",
			"slither",
			"sneak",
			"soar",
			"stomp",
			"swim",
			"tromp",
			"walk",
			"wriggle"
			};
			
	public static String[] skillFlags = {
			"character",		"object",			"global_character",
			"passive"};

	public static String[] skillTypes = {
			"none",				"self",				"defensive",
			"offensive",		"targetless"};

	public static String[] badEffects = {
			// Cleric
			"curse",			"mark of regret",	"disease",
			"feebleness",		
			
			// Mage
			"frozen",			"slow"};

	public static String[] goodEffects = {
			// Cleric
			"bless",			"renew",			"holy shield",
			"holy armor",		"righteousness",	"true sight",
			"resist evil",		"resist holy",		"alertness",
			"levitation",		"judgement",		"empower",
			"detect alignment",	"courage",			"passage",
			"revival",			"fortify",			"greater blessing",
			"beacon of light",	"divine strength",	"desparate prayer",
			"zeal",
			
			// Fighter
			"enrage",
			
			// Mage
			"intelligence",		"invisible",		"haste",
			"magic shield",		"resist fire",		"detect invisible"};

	public static String[] areaFlags = {
			"closed",			"hidden",			"city"};

	public static String[] roomFlags = {
			"dark",				"light",			"nomob",
			"nocombat",			"norecall",			"noteleport",
			"noassistance",		"nopets",			"neverpvp",
			"alwayspvp",		"nowhere",			"silent"};
			
	public static String[] roomSectors = {
			"indoors",			"city",				"path",
			"field",			"hills",			"mountain",
			"forest",			"desert",			"air",
			"cavern",			"water",			"underwater",
			"deepwater"};

	public static String[] exitFlags = {
			"door",				"closed",			"locked",
			"hidden",			"nopick",			"nobash",
			"nopass"};
			
	public static String[] charFlags = {
			"stayroom",			"staysector",		"stayarea",
			"attackgood",		"attackevil",		"attackenemy",
			"attackall",		"fleealways",		"fleewounded",
			"noattack",			"janitor",			"shopkeeper",
			"pursues",			"banker"};
	
	public static String[] damageTypes = {
			"ranged",			"melee",			"magic",
			"slowmelee",		"fastmelee",		"handcombat",
			"frost",			"fire",				"lightning",
			"acid",				"good",				"evil",
			"stun",				"control",			"summon"};
	public static String[] wearlocs = {
			"head",				"neck",				"chest",
			"back",				"arms",				"wrist",
			"hands",			"finger1",			"finger2",
			"waist",			"legs",				"feet",
			"wield1",			"wield2",			"shield",
			"light",			"trinket1",			"trinket2"};
	public static String[] objMaterials = {
			"cloth",			"leather",			"iron",
			"steel",			"bronze",			"copper",
			"silver",			"gold",				"platinum",
			"mithril",			"wood",				"stone",
			"bone",				"glass",			"paper",
			"liquid",			"magic",			"organic"};
	public static String[] objFlags = {
			"nodrop",			"nolocate",			"notake",
			"noput"};

	public static HashMap<String, String[]> objTypes = new HashMap<String, String[]>();
	public static void setObjTypes()
	{
		String[] head = {"(S) Armor Type", "", "", "", ""};
		objTypes.put("head", head);
		String[] neck = {"", "", "", "", ""};
		objTypes.put("neck", neck);
		String[] chest = {"(S) Armor Type", "", "", "", ""};
		objTypes.put("chest", chest);
		String[] back = {"", "", "", "", ""};
		objTypes.put("back", back);
		String[] arms = {"(S) Armor Type", "", "", "", ""};
		objTypes.put("arms", arms);
		String[] wrist = {"", "", "", "", ""};
		objTypes.put("wrist", wrist);
		String[] hands = {"(S) Armor Type", "", "", "", ""};
		objTypes.put("hands", hands);
		String[] ring = {"", "", "", "", ""};
		objTypes.put("ring", ring);
		String[] waist = {"(S) Armor Type", "", "", "", ""};
		objTypes.put("waist", waist);
		String[] legs = {"(S) Armor Type", "", "", "", ""};
		objTypes.put("legs", legs);
		String[] feet = {"(S) Armor Type", "", "", "", ""};
		objTypes.put("feet", feet);
		String[] weapon = {"(S) Weapon Type", "(#) Min. Base Damage", "(#) Max. Base Damage", "(S) Hit Type", ""};
		objTypes.put("weapon", weapon);
		String[] shield = {"(%) Maximum Block", "", "", "", ""};
		objTypes.put("shield", shield);
		String[] light = {"", "", "", "", ""};
		objTypes.put("light", light);
		String[] trinket = {"", "", "", "", ""};
		objTypes.put("trinket", trinket);
		String[] container = {"(#) Capacity", "(#) Key Object", "", "", ""};
		objTypes.put("container", container);
		String[] scroll = {"(#) Effect Level", "(S) Spell 1", "(S) Spell 2", "(S) Spell 3", "(S) Spell 4"};
		objTypes.put("scroll", scroll);
		String[] potion = {"(#) Effect Level", "(S) Spell 1", "(S) Spell 2", "(S) Spell 3", "(S) Spell 4"};
		objTypes.put("potion", potion);
		String[] pill = {"(#) Effect Level", "(S) Spell 1", "(S) Spell 2", "(S) Spell 3", "(S) Spell 4"};
		objTypes.put("pill", pill);
		String[] wand = {"(#) Effect Level", "(S) Spell", "(#) Maximum Charges", "(#) Remaining Charges", ""};
		objTypes.put("wand", wand);
		String[] furniture = {"(#) Maximum People", "(%) + Health Regen.", "(%) + Mana Regen.", "(%) + Energy Regen.", ""};
		objTypes.put("furniture", furniture);
		String[] drink = {"(S) Drink Type", "", "", "", ""};
		objTypes.put("drink", drink);
		String[] food = {"", "", "", "", ""};
		objTypes.put("food", food);
		String[] key = {"", "", "", "", ""};
		objTypes.put("key", key);
		String[] money = {"", "", "", "", ""};
		objTypes.put("money", money);
		String[] portal = {"(#) Destination", "", "", "", ""};
		objTypes.put("portal", portal);
		String[] reagent = {"(S) Reagent Type", "", "", "", ""};
		objTypes.put("reagent", reagent);
		String[] training = {"(S) Skill", "", "", "", ""};
		objTypes.put("training", training);
		String[] trash = {"", "", "", "", ""};
		objTypes.put("trash", trash);
		String[] none = {"", "", "", "", ""};
		objTypes.put("none", none);
	}

	public static String[] armorTypes = {
			"cloth",			"leather",			"mail",
			"plate",			"magic"};
	public static String[] weaponTypes = {
			"sword",			"heavy_sword",		"dagger",
			"mace",				"heavy_mace",		"flail",
			"axe",				"heavy_axe",		"polearm",
			"whip",				"staff",			"hand_weapon",
			"exotic"};
	public static String[] hitTypes = {
			"hit",				"slice",			"stab",
			"slash",			"whip",				"claw",
			"blast",			"pound",			"crush",
			"pierce",			"beating",			"digestion",
			"charge",			"slap",				"punch",
			"wrath",			"magic",			"divine power",
			"cleave",			"scratch",			"peck",
			"chop",				"sting",			"smash",
			"shocking bite",	"flaming bite",		"freezing bite",
			"acidic bite",		"chomp",			"life drain",
			"slime",			"shock",			"thwack",
			"flame",			"chill",			"hoof",
			"quill",			"talon",			"antler",
			"jab",				"gnaw",				"bite",
			"strike",			"skewer"};
	public static String[] shieldFlags = damageTypes;
	public static String[] containerFlags = {
			"closeable",		"closed",			"nopick",
			"locked"};
	public static String[] furnitureFlags = {
			"stand",			"sit",				"rest",
			"sleep"};
	public static String[] drinkFlags = {
			"infinite",			"flexlevel"};
	public static String[] foodFlags = {
			"infinite",			"flexlevel"};
	public static String[] keyFlags = {
			"destroyonuse"};
	public static String[] trainingFlags = {
			"autouse"};
	public static String[] portalFlags = {
			"destroyonuse"};
	public static String[] reagentTypes = {
			"warpstone"};
	
	public static String[] roomEffects = {
			"blizzard",			"firestorm",		"windstorm"};
	public static String[] charEffects = {
			"invisible",		"bless",			"intelligence",
			"enrage"};
	public static String[] objEffects = {
			"sharpened",		"vampiric",			"frost",
			"flaming",			"shocking",			"poison",
			"invisible"};
	
	public static String[] statNames = {
			"hp",				"mana",				"energy",
			"strength",			"dexterity",		"constitution",
			"intelligence",		"charisma",			"armslash",
			"armbash",			"armpierce",		"resfrost",
			"resfire",			"reslightning",		"resacid",
			"resgood",			"resevil",			"meleedmg",
			"rangeddmg",		"spelldmg",			"healing",
			"hp_regen",			"mn_regen",			"st_regen",
			"castspeed",		"meleespeed",		"meleeaccuracy",
			"castaccuracy"};
	
	public static String fullStatName(String stat)
	{
		if (stat.equals("armslash"))
			return "slash armor";
		if (stat.equals("armbash"))
			return "bash armor";
		if (stat.equals("armpierce"))
			return "pierce armor";
		if (stat.equals("resfrost"))
			return "frost resistance";
		if (stat.equals("resfire"))
			return "fire resistance";
		if (stat.equals("reslightning"))
			return "lightning resistance";
		if (stat.equals("resacid"))
			return "acid resistance";
		if (stat.equals("resgood"))
			return "good resistance";
		if (stat.equals("resevil"))
			return "evil resistance";
		if (stat.equals("meleedmg"))
			return "melee damage";
		if (stat.equals("rangeddmg"))
			return "ranged damage";
		if (stat.equals("spelldmg"))
			return "spell damage";
		if (stat.equals("hp_regen"))
			return "health regen/second";
		if (stat.equals("mn_regen"))
			return "mana regen/second";
		if (stat.equals("st_regen"))
			return "energy regen/second";
		if (stat.equals("castspeed"))
			return "casting speed";
		if (stat.equals("meleespeed"))
			return "melee attack speed";
		if (stat.equals("meleeaccuracy"))
			return "melee accuracy";
		if (stat.equals("castaccuracy"))
			return "casting accuracy";
		return stat;
	}

	/** The list of valid rprog triggers. */
	public static String rprogTriggerTypes[] = {
			"greet",			"leave",			"random",
			"command",			"speech",			"drop",
			"skill",			"time",				"dooropen",
			"doorclose",		"doorunlock",		"doorlock",
			"look"};
	/** The list of valid mprog triggers. */
	public static String mprogTriggerTypes[] = {
			"greet",			"leave",			"random",
			"command",			"speech",			"drop",
			"skill",			"time",				"dooropen",
			"doorclose",		"doorunlock",		"doorlock",
			"vischeck",			"look",				"load",
			"death",			"kill",				"encounter",
			"entry",			"give",				"combat",
			"hitpercent",		"bribe"};
	/** The list of valid oprog triggers. */
	public static String oprogTriggerTypes[] = {
			"greet",			"leave",			"random",
			"command",			"speech",			"drop",
			"skill",			"time",				"dooropen",
			"doorclose",		"doorunlock",		"doorlock",
			"vischeck",			"look",				"load",
			"use",				"wear",				"remove",
			"give",				"get",				"put"};

	public static String questFlags[] = {};
	
	public static String questObjectiveFlags[] = {};
	
	public static String questObjectiveTasks[] = {
			"findroom",			"findobj",			"findchar",
			"gatherobj",		"killchar"};

	public static String[] areaClimates = {
			"temperate",		"desert",			"jungle",
			"mountains",		"tundra",			"arctic"};
			
	public static HashMap<String, Integer[]> weatherTypes = new HashMap<String, Integer[]>();
	public static void setWeatherTypes()
	{
		Integer[] clear = {6, 0, 0, 0};
		weatherTypes.put("clear", clear);
		Integer[] windy = {-2, 0, 0, 4};
		weatherTypes.put("windy", windy);
		Integer[] pc = {4, 1, 0, 1};
		weatherTypes.put("partly cloudy", pc);
		Integer[] c = {-2, 2, 0, 2};
		weatherTypes.put("cloudy", c);
		Integer[] ss = {-2, 3, 1, 2};
		weatherTypes.put("scattered showers", ss);
		Integer[] st = {-2, 4, 3, 3};
		weatherTypes.put("scattered thunderstorms", st);
		Integer[] wf = {14, 2, 0, 3};
		weatherTypes.put("warm front", wf);
		Integer[] wfr = {10, 3, 2, 3};
		weatherTypes.put("warm front with rain", wfr);
		Integer[] cf = {-10, 2, 0, 3};
		weatherTypes.put("cold front", cf);
		Integer[] cfr = {-10, 3, 2, 3};
		weatherTypes.put("cold front with rain", cfr);
		Integer[] cfs = {-16, 4, 4, 4};
		weatherTypes.put("cold front with storms", cfs);
		Integer[] cfhs = {-16, 5, 5, 5};
		weatherTypes.put("cold front with heavy storms", cfhs);
		Integer[] lss = {-2, 2, 1, 0};
		weatherTypes.put("light snow showers", lss);
		Integer[] hss = {-2, 2, 3, 1};
		weatherTypes.put("heavy snow showers", hss);
		Integer[] bl = {-6, 5, 5, 5};
		weatherTypes.put("blizzard", bl);
	}
























/*		VULGAR STUFF BELOW HERE... CONSIDER YOURSELF WARNED.		*/















































	/** The list of strings which are not permitted in player-created names. */
	public static String[] illegalNames = {"admin", "agape", "allah", "anonymous", "anus", "anybody", "anyone", "ass", "baal", "bastard", "beelzebub", "bitch", "builder", "cancel", "catholic", "christ", "clitoris", "coder", "cunt", "devil", "diablo", "dick", "everybody", "everyone", "everything", "fag", "fuck", "fuk", "gay", "god", "holy", "lesbian", "lucifer", "jehovah", "jesus", "kcuf", "messiah", "mohamed", "mohammad", "mohammed", "muhammad", "nobody", "penis", "pope", "pussy", "room", "satan", "secks", "seks", "self", "sex", "shit", "shoggoth", "slut", "somebody", "someone", "something", "staff", "suck", "susej", "the", "twat", "vagina", "you"};
}
