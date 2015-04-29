package jw.core;
import java.util.*;

import static jw.core.MudMain.*;
import jw.data.*;

/**
	The Area class represents a single area, with all associated fields.
*/
public class Area implements Comparable<Area>
{
	/** The area's ID number. */
	public int id;
	/** The name of this area. */
	public String name = "";
	/** Multi-line area description. */
	public String description = "";
	/** The climate in this area. */
	public String climate = "temperate";
	/** Lowest recommended level. */
	public int minLevel = 1;
	/** Highest recommended level. */
	public int maxLevel = 100;
	/** Flags. */
	public HashMap<String, Boolean> flags = new HashMap<String, Boolean>();
	/** The first ID in this area's range. */
	public int start = 0;
	/** The last ID in this area's range. */
	public int end = 0;

	/** The current weather cycle in the area. */
	public String weatherCycle = "clear";
	/** The last weather cycle in the area (for transition phrasing). */
	public String weatherLastCycle = "clear";
	/** The timer before the weather cycle changes. */
	public int weatherTimer = 0;

	/** Overall temperature of the area. */
	public int weatherTemp = 0;
	/** Amount of cloud cover in the area. (0 = none, 5 = dark clouds) */
	public int weatherCloud = 0;
	/** Amount of precipitation in the area. (0 = none, 5 = worst) */
	public int weatherPrecip = 0;
	/** Amount of wind in the area. (0 = none, 5 = most) */
	public int weatherWind = 0;

	
	/**
	Allocate an area and assign a pre-defined ID to it. All other fields are left blank.
	*/
	public Area(int newId)
	{
		id = newId;
		for (String s : Flags.areaFlags)
			flags.put(s, false);
	}
	
	/**
	Search for an area which has the ID {@code targetId}.
	<p>
	This runs through the {@link MudMain#areas areas} global ArrayList and returns
	the area which matches {@code targetId}.
	@param targetId The area ID to search for.
	@return The area which matches {@code targetId}, or {@code null} if none exists.
	*/
	public static Area lookup(int targetId)
	{
		for (Area a : areas)
			if (a.id == targetId)
				return a;
		return null;
	}
	
	public void update()
	{
		if (Flags.weatherTypes.get(weatherCycle) == null)
			weatherCycle = "clear";
		weatherTimer--;
		if (weatherTimer <= 0)
		{
			weatherTemp += Flags.weatherTypes.get(weatherCycle)[0]/2;
			weatherCycle = getNextWeather();
			weatherTemp += Flags.weatherTypes.get(weatherCycle)[0]/2;
			weatherCloud = Flags.weatherTypes.get(weatherCycle)[1];
			weatherPrecip = Flags.weatherTypes.get(weatherCycle)[2];
			weatherWind = Flags.weatherTypes.get(weatherCycle)[3];

			if (weatherCycle.indexOf("front") > -1)
				weatherTimer = 3;
			else
				weatherTimer = 6;
		}
		Database.saveArea(this);
	}
	
	public String getNextWeather()
	{
		int season = Fmt.getSeason();
		ArrayList<String> pickFrom = new ArrayList<String>();
		
		// Start with some temperature "target" adjustments for certain climates.
		int tempMod = 0;
		if (climate.equals("desert"))
		{
			tempMod = 30;
			if (season == 1)
				tempMod = 50;
		}
		if (climate.equals("jungle"))
		{
			tempMod = 20;
			if (season == 1)
				tempMod = 35;
		}
		if (climate.equals("mountains"))
		{
			tempMod = -10;
			if (season == 3)
				tempMod = -25;
		}
		if (climate.equals("tundra"))
		{
			tempMod = -20;
			if (season == 3)
				tempMod = -40;
		}
		if (climate.equals("arctic"))
		{
			tempMod = -30;
			if (season == 3)
				tempMod = -50;
		}

		// Generate a list of acceptible weather based on season and temperature only.
		pickFrom.add("clear");
		pickFrom.add("clear");
		pickFrom.add("clear");
		pickFrom.add("windy");
		pickFrom.add("partly cloudy");
		pickFrom.add("cloudy");
		switch (season)
		{
			case 1:
				if (weatherTemp < -15+tempMod)
				{
					weatherTemp += 2;
					pickFrom.remove("windy");
					pickFrom.remove("cloudy");
				}
				if (weatherTemp > 30+tempMod)
				{
					weatherTemp -= 2;
					pickFrom.remove("clear");
				}
				break;
			case 3:
				if (weatherTemp > 100+tempMod)
					weatherTemp -= 2;
				if (weatherTemp > 85+tempMod && weatherTemp > 40)
				{
					pickFrom.add("cold front with storms");
					pickFrom.add("cold front with heavy storms");
				}
				if (weatherTemp > 70+tempMod)
				{
					pickFrom.add("cold front");
					if (weatherTemp > 40)
						pickFrom.add("cold front with rain");
				}
				if (weatherTemp < 65+tempMod)
				{
					pickFrom.add("warm front");
					if (weatherTemp > 30)
						pickFrom.add("warm front with rain");
				}
				if (weatherTemp < 55+tempMod)
					weatherTemp += 2;
				break;
			case 2:
			case 4:
				if (weatherTemp > 70+tempMod)
				{
					weatherTemp -= 2;
				}
				if (weatherTemp > 50+tempMod)
				{
					pickFrom.add("cold front");
					if (weatherTemp > 40)
						pickFrom.add("cold front with rain");
				}
				if (weatherTemp < 40+tempMod)
				{
					pickFrom.add("warm front");
					if (weatherTemp > 30)
						pickFrom.add("warm front with rain");
				}
				if (weatherTemp < 30+tempMod)
					weatherTemp += 2;
				break;
		}
		
		// Temperature-based precipitation.
		if (weatherTemp < 30 && weatherTemp > 0)
		{
			pickFrom.add("light snow showers");
			pickFrom.add("heavy snow showers");
			pickFrom.add("blizzard");
		}
		if (weatherTemp > 30)
		{
			pickFrom.add("scattered showers");
		}
		if (weatherTemp > 70)
		{
			pickFrom.add("scattered thunderstorms");
		}

		// Pick out or add specific weather patterns per climate.
		if (climate.equals("desert"))
		{
			pickFrom.remove("light snow showers");
			pickFrom.remove("heavy snow showers");
			pickFrom.remove("blizzard");
		}
		if (climate.equals("jungle"))
		{
			pickFrom.remove("light snow showers");
			pickFrom.remove("heavy snow showers");
			pickFrom.remove("blizzard");
			pickFrom.remove("windy");
		}
		if (climate.equals("mountains"))
		{
			pickFrom.remove("cold front with storms");
			pickFrom.remove("cold front with heavy storms");
		}
		
		if (pickFrom.size() == 0)
			return "clear";
		
		// Finished product - return the name of the new weather.
		return pickFrom.get(gen.nextInt(pickFrom.size()));
	}
	
	public static void weatherStatus()
	{
		for (UserCon cs : conns)
		{
			if (cs.ch.position.equals("sleeping"))
				continue;
			Area tA = cs.ch.currentArea();
			if (tA == null)
				continue;
			if (cs.ch.currentRoom.sector.equals("indoors")
				|| cs.ch.currentRoom.sector.equals("cavern")
				|| cs.ch.currentRoom.sector.equals("underwater"))
				continue;
			
			if (tA.weatherCycle.equals("clear"))
			{
				cs.sendln("The weather is now: "+tA.weatherCycle);
			}
			else if (tA.weatherCycle.equals("windy"))
			{
				cs.sendln("The weather is now: "+tA.weatherCycle);
			}
			else if (tA.weatherCycle.equals("partly cloudy"))
			{
				cs.sendln("The weather is now: "+tA.weatherCycle);
			}
			else if (tA.weatherCycle.equals("cloudy"))
			{
				cs.sendln("The weather is now: "+tA.weatherCycle);
			}
			else if (tA.weatherCycle.equals("scattered showers"))
			{
				cs.sendln("The weather is now: "+tA.weatherCycle);
			}
			else if (tA.weatherCycle.equals("scattered thunderstorms"))
			{
				cs.sendln("The weather is now: "+tA.weatherCycle);
			}
			else if (tA.weatherCycle.equals("warm front"))
			{
				cs.sendln("The weather is now: "+tA.weatherCycle);
			}
			else if (tA.weatherCycle.equals("warm front with rain"))
			{
				cs.sendln("The weather is now: "+tA.weatherCycle);
			}
			else if (tA.weatherCycle.equals("cold front"))
			{
				cs.sendln("The weather is now: "+tA.weatherCycle);
			}
			else if (tA.weatherCycle.equals("cold front with rain"))
			{
				cs.sendln("The weather is now: "+tA.weatherCycle);
			}
			else if (tA.weatherCycle.equals("cold front with storms"))
			{
				cs.sendln("The weather is now: "+tA.weatherCycle);
			}
			else if (tA.weatherCycle.equals("cold front with heavy storms"))
			{
				cs.sendln("The weather is now: "+tA.weatherCycle);
			}
			else if (tA.weatherCycle.equals("light snow showers"))
			{
				cs.sendln("The weather is now: "+tA.weatherCycle);
			}
			else if (tA.weatherCycle.equals("heavy snow showers"))
			{
				cs.sendln("The weather is now: "+tA.weatherCycle);
			}
			else if (tA.weatherCycle.equals("blizzard"))
			{
				cs.sendln("The weather is now: "+tA.weatherCycle);
			}
			else
				sysLog("bugs", "Unknown weather in area #"+tA.id+": "+tA.weatherCycle);
		}
	}
	
	public int compareTo(Area other)
	{
		if (minLevel == other.minLevel && maxLevel == other.maxLevel)
			return 0;
		if (minLevel == Flags.minLevel && maxLevel == Flags.maxPlayableLevel)
			return -1;
		if (other.minLevel == Flags.minLevel && other.maxLevel == Flags.maxPlayableLevel)
			return 1;
		if (minLevel < other.minLevel)
			return -1;
		if (minLevel > other.minLevel)
			return 1;
		if (maxLevel < other.maxLevel)
			return -1;
		if (maxLevel > other.maxLevel)
			return 1;
		return 0;
	}
}