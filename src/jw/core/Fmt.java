package jw.core;
import java.util.*;

import jw.core.*;
import static jw.core.MudMain.*;

/**
The Fmt class contains formatting and output processing methods.
*/
public class Fmt
{
	/**
	Convert a string to an int, or 0 if it's not valid.
	
	@param args The string to convert.
	@return The integer representation of {@code args}, or 0 if it could not be
		converted.
	*/
	public static int getInt(String args)
	{
		args = args.trim();

		int newInt = 0;
		try
		{
			newInt = Integer.parseInt(args);
		} catch (Exception e)
		{
			newInt = 0;
		}
		
		return newInt;
	}

	public static double getDouble(String args)
	{
		args = args.trim();

		double newDouble = 0.0;
		try
		{
			newDouble = Double.parseDouble(args);
		} catch (Exception e)
		{
			newDouble = 0;
		}
		
		return newDouble;
	}
	
	/**
	Translate special characters in "text" using information from "ch" and "victim".
	
	@param ch The "actor" of the text (who initiated the command).
	@param victim The "target" of the text/command.
	@param text The text to process, containing special $ tags.
	@return The processed string with all pronouns/names filled in.
	*/
	public static String actString(CharData to, CharData ch, CharData victim, ObjData ob, String text, boolean global)
	{
		String n = "null";
		String r = "null";
		String p = "null";
		String e = "null";
		String m = "null";
		String s = "null";
		String N = "null";
		String R = "null";
		String P = "null";
		String E = "null";
		String M = "null";
		String S = "null";
		String o = "null";
		
		if (ch != null)
		{
			if (global)
				n = seeNameGlobal(to, ch);
			else
				n = seeName(to, ch);
			r = ch.charRace.name;
			p = ch.charRace.plural;
			if ((!Combat.canSee(to, ch) && !global) || (!Combat.canSeeGlobal(to, ch) && global))
			{
				r = "unknown";
				p = "unknowns";
				e = "it";
				m = "it";
				s = "its";
			}
			else if (ch.sex.equals("m"))
			{
				e = "he";
				m = "him";
				s = "his";
			}
			else
			{
				e = "she";
				m = "her";
				s = "her";
			}
			if (to == ch)
			{
				n = "you";
				e = "you";
				m = "you";
				s = "your";
			}
		}
		if (victim != null)
		{
			if (global)
				N = seeNameGlobal(to, victim);
			else
				N = seeName(to, victim);
			R = victim.charRace.name;
			P = victim.charRace.plural;
			if ((!Combat.canSee(to, victim) && !global) || (!Combat.canSeeGlobal(to, victim) && global))
			{
				R = "unknown";
				P = "unknowns";
				E = "it";
				M = "it";
				S = "its";
			}
			else if (victim.sex.equals("m"))
			{
				E = "he";
				M = "him";
				S = "his";
			}
			else
			{
				E = "she";
				M = "her";
				S = "her";
			}
			if (to == victim)
			{
				N = "you";
				E = "you";
				M = "you";
				S = "your";
			}
		}
		if (ob != null)
		{
			o = Fmt.seeName(to, ob);
		}
		
		text = text.replace("$n", n);
		text = text.replace("$r", r);
		text = text.replace("$p", p);
		text = text.replace("$e", e);
		text = text.replace("$m", m);
		text = text.replace("$s", s);
		text = text.replace("$N", N);
		text = text.replace("$R", R);
		text = text.replace("$P", P);
		text = text.replace("$E", E);
		text = text.replace("$M", M);
		text = text.replace("$S", S);
		text = text.replace("$o", o);
		
		return text;
	}
	
	public static void actAround(CharData target, CharData victim, ObjData o, String text)
	{
		actAround(target, victim, o, text, null);
	}

	public static void actAround(CharData target, CharData victim, ObjData o, String text, CharData skip)
	{
		for (UserCon c : conns)
			if (c.ch.currentRoom == target.currentRoom && c.ch != target && c.ch != skip)
				if (!c.ch.position.equals("sleeping"))
					c.ch.sendln(actString(c.ch, target, victim, o, text, false));
	}
	
	public static void actRoom(Room targetRm, CharData target, CharData victim, ObjData o, String text)
	{
		for (UserCon c : conns)
			if (c.ch.currentRoom == targetRm)
				if (!c.ch.position.equals("sleeping"))
					c.ch.sendln(actString(c.ch, target, victim, o, text, false));
	}
	
	/**
	Display text in a multi-column layout with a number of formatting options.
	
	@param items A String array containing individual items to display.
	@param cols The number of columns of output to create.
	@param width The total width of the desired output, in characters.
	@param lDiv The string which will appear to the left of each item as a separator.
	@param rDiv The string which will appear to the right of each item as a separator.
	@return A full string, including '^/' where necessary, containing the formatted
		output.
	*/
	public static String textColumns(String[] items, int cols, int width, String lDiv, String rDiv)
	{
		if (items.length == 0)
			return "";
		String result = "";
		int colWidth = (width/cols)-UserCon.codelessLength(lDiv+rDiv);
		for (int ctr = 0; ctr < items.length; ctr += cols)
		{
			for (int lineCtr = 0; lineCtr < cols && (lineCtr+ctr) < items.length; lineCtr++)
				result = result+lDiv+fit(items[ctr+lineCtr], colWidth)+rDiv;
			result = result+"^/";
		}
		return result.substring(0, result.length()-2);
	}
	
	/**
	A shortcut for a default set of arguments to textColumns.
	
	@param items A String array containing individual items to display.
	@return The formatted string from textColumns.
	*/
	public static String defaultTextColumns(String[] items)
	{
		return textColumns(items, 3, 75, " }M[}m ", " }M]{x");
	}
	
	/**
	Fit the given string into exactly "width" characters, adding spaces to fit.
	
	@param item The text to fit.
	@param width The number of characters to occupy with the text.
	@return Exactly {@code width} characters containing as much of {@code item} as will
		fit.
	*/
	public static String fit(String item, int width)
	{
		int clength = UserCon.codelessLength(item);
		while (clength < width)
		{
			item = item+" ";
			clength++;
		}
		if (clength > width)
			item = item.substring(0, (width+(item.length()-clength))-2)+"..";
		return item;
	}

	public static String rfit(String item, int width)
	{
		int clength = UserCon.codelessLength(item);
		while (clength < width)
		{
			item = " "+item;
			clength++;
		}
		if (clength > width)
			item = item.substring(0, (width+(item.length()-clength))-2)+"..";
		return item;
	}

	public static String rhfit(String item, int width)
	{
		int clength = UserCon.codelessLength(item);
		while (clength < width)
		{
			item = " "+item;
			clength++;
		}
		if (clength > width)
			item = item.substring(0, width);
		return item;
	}
	
	public static ArrayList<String> formatString(ArrayList<String> contents, int width, boolean indent)
	{
		ArrayList<String> newContents = new ArrayList<String>();
		try {
			String all = "";
			for (String s : contents)
				all = all+" "+s;
			
			int maxWidth = width;
			boolean addBreak = all.contains("^\\");
			for (String s : all.split("\\^\\\\"))
			{
				if (indent)
					s = "   "+s;

				if (UserCon.codelessLength(s) > 75)
				{
					while (s.contains("  "))
						s = s.replace("  ", " ");
				}
				
				if (indent)
					s = "   "+s;
				else
					s = s.substring(1);

				width = maxWidth;
				if (s.trim().length() == 0)
				{
					if (addBreak)
						newContents.add("^\\");
					else
						newContents.add("");
					continue;
				}
				while (s.length() > 0)
				{
					width = 0;
					int ctr = 0;
					int offset = 0;
					while (ctr < s.length() && width < maxWidth)
					{
						if (s.charAt(ctr)	== '{' || s.charAt(ctr) == '}')
							offset += 2;
						else if (s.charAt(ctr) == '^')
							offset++;
						else
							width++;
						ctr++;
					}
					
					width = width+offset;
					if (s.length() <= width)
					{
						newContents.add(s);
						break;
					}
					
					int chop = width;
					while (s.charAt(chop) != ' ')
						chop--;
					if (chop <= width-15)
					{
						newContents.add(s.substring(0, width-1)+"-");
						s = s.substring(width-1).trim();
					}
					else
					{
						newContents.add(s.substring(0, chop));
						s = s.substring(chop).trim();
					}
				}
				if (addBreak)
				{
					String temp = newContents.get(newContents.size()-1);
					temp = temp+"^\\";
					newContents.remove(newContents.size()-1);
					newContents.add(temp);
				}
			}
		} catch (Exception e) {
			sysLog("bugs", "Error in formatString: "+e.getMessage());
			logException(e);
		}
		return newContents;
	}
	
	public static String cap(String arg)
	{
		if (arg.length() < 2)
			return arg.toUpperCase();
		return arg.substring(0, 1).toUpperCase()+arg.substring(1);
	}
	
	public static String heading(String text)
	{
		String finText = "";
		int add = 1;
		if (text.length() > 0)
			finText = " }m"+text+"}M ";
		int maxLength = 75+(finText.length()-UserCon.codelessLength(finText));
		while (finText.length() < maxLength)
		{
			if (add == 1)
				finText = finText+"-";
			else
				finText = "-"+finText;
			add = add*-1;
		}
		
		finText = "}M"+finText+"{x";
		return finText;
	}
	
	public static String center(String text)
	{
		return center(text, 75);
	}
	public static String center(String text, int width)
	{
		String finText = text;
		int add = 1;
		int maxLength = width+(finText.length()-UserCon.codelessLength(finText));
		while (finText.length() < maxLength)
		{
			if (add == 1)
				finText = finText+" ";
			else
				finText = " "+finText;
			add = add*-1;
		}
		return finText;
	}
	
	public static String hearName(CharData ch, CharData target)
	{
		if (Combat.canSee(ch, target))
			return target.shortName;
		else
			if (target.conn.hasPermission("staff"))
				return "a staff member";
			else
				return "an invisible voice";
	}
	
	public static String seeName(CharData ch, CharData target)
	{
		if (Combat.canSee(ch, target))
			return target.shortName;
		else
			if (target.conn.hasPermission("staff"))
				return "a staff member";
			else
				return "someone";
	}
	
	public static String seeName(CharData ch, ObjData target)
	{
		if (Combat.canSee(ch, target))
			return target.shortName;
		else
			return "an invisible object";
	}

	public static String seeNameGlobal(CharData ch, CharData target)
	{
		if (Combat.canSeeGlobal(ch, target))
			return target.shortName;
		else
			if (target.conn.hasPermission("staff"))
				return "a staff member";
			else
				return "someone";
	}
	
	public static String getWhoFlags(UserCon c, UserCon viewing)
	{
		String flags = "";
		if (viewing.ignoring.contains(c.ch.shortName.toLowerCase()))
			flags = flags+"{R^{{rIgnored{R^} ";
		if (c.afk)
			flags = flags+"{w-{cAFK{w- ";
		if (c.quiet)
			flags = flags+"{w^{{cQ{w^} ";
		if (c.troubled)
			flags = flags+"{W[{RT{W] ";
		if (c.ch.currentRoom.id == Flags.jailId && !c.hasPermission("staff"))
			flags = flags+"{W[{RJ{W] ";
		if (c.invis)
			flags = flags+"{w({yI{w) ";
		if (c.incog)
			flags = flags+"{w({yC{w) ";
		flags = flags+"{x";
		return flags;
	}

	public static String getLookFlags(UserCon c, UserCon viewing)
	{
		String flags = "";
		if (viewing.ignoring.contains(c.ch.shortName.toLowerCase()))
			flags = flags+"{R^{{rIgnored{R^} ";
		if (c.afk)
			flags = flags+"{w-{cAFK{w- ";
		if (c.invis)
			flags = flags+"{w({yInvis{w) ";
		if (c.incog)
			flags = flags+"{w({yIncog{w) ";
		for (Effect e : c.ch.effects)
		{
			if (e.name.equals("invisible"))
				flags = flags+"{W({DInvis{W) ";
		}

		return flags;
	}

	public static String getLookFlags(ObjData o, UserCon viewing)
	{
		String flags = "";
		for (Effect e : o.effects)
		{
			if (e.name.equals("invisible"))
				flags = flags+"{W({DInvis{W) ";
			if (e.name.equals("flaming"))
				flags = flags+"{r({yFlaming{r) ";
		}

		return flags;
	}

	// Year = 13 months = 2695680 seconds
	// Season = 78 days = 673920 seconds
	// Month = 4 weeks = 207360 seconds
	// Week = 6 days = 51840 seconds
	// Day = 24 hours = 8640 seconds
	// Hour = 360 seconds
	
	public static int getYear()
	{
		long startTime = System.currentTimeMillis()/1000;
		startTime = startTime-timeOffset;
		
		return (int)(startTime/2695680)+1;
	}
	
	public static int getSeason()
	{
		long startTime = System.currentTimeMillis()/1000;
		startTime = startTime-timeOffset;

		return (int)((startTime/673920) % 4)+1;
	}
	
	public static int getMonth()
	{
		long startTime = System.currentTimeMillis()/1000;
		startTime = startTime-timeOffset;

		return (int)((startTime/207360) % 13)+1;
	}
	
	public static int getWeek()
	{
		long startTime = System.currentTimeMillis()/1000;
		startTime = startTime-timeOffset;
		
		return (int)((startTime/51840) % 4)+1;
	}
	
	public static int getDay()
	{
		long startTime = System.currentTimeMillis()/1000;
		startTime = startTime-timeOffset;
		
		return (int)((startTime/8640) % 24)+1;
	}

	public static int getHour()
	{
		long startTime = System.currentTimeMillis()/1000;
		startTime = startTime-timeOffset;
		
		return (int)((startTime/360) % 24);
	}
	
	public static String getMonthName(int month)
	{
		switch (month)
		{
			case 1:
				return "January";
			case 2:
				return "February";
			case 3:
				return "March";
			case 4:
				return "April";
			case 5:
				return "May";
			case 6:
				return "June";
			case 7:
				return "July";
			case 8:
				return "August";
			case 9:
				return "September";
			case 10:
				return "October";
			case 11:
				return "November";
			case 12:
				return "December";
			case 13:
				return "Lastmonth";
		}
		return ""+month;
	}
	
	public static String getSeasonName(int season)
	{
		switch (season)
		{
			case 1:
				return "winter";
			case 2:
				return "spring";
			case 3:
				return "summer";
			case 4:
				return "fall";
		}
		return ""+season;
	}

	public static int getSunset(int season)
	{
		switch (season)
		{
			case 1:
				return 18;
			case 3:
				return 21;
			default:
				return 19;
		}
	}

	public static int getSunrise(int season)
	{
		switch (season)
		{
			case 1:
				return 9;
			case 3:
				return 6;
			default:
				return 7;
		}
	}
	
	public static String nth(int nr)
	{
		if (nr > 10 && nr < 20)
			return nr+"th";
		if (nr % 10 == 1)
			return nr+"st";
		if (nr % 10 == 2)
			return nr+"nd";
		if (nr % 10 == 3)
			return nr+"rd";
		return nr+"th";
	}
	
	public static String intToStr(int nr)
	{
		String pr = "";
		if (nr < 0)
		{
			pr = "negative ";
			nr = Math.abs(nr);
		}
		
		String[] nrs = {
			"zero", "one", "two", "three", "four", "five", "six",
			"seven", "eight", "nine", "ten", "eleven", "twelve", "thirteen",
			"fourteen", "fifteen", "sixteen", "seventeen", "eighteen", "nineteen",
			"twenty"};
		
		if (nr <= 20)
			return pr+nrs[nr];
		
		String[] tens = {"", "", "twenty", "thirty", "forty", "fifty", "sixty",
			"seventy", "eighty", "ninety"};
		
		if (nr <= 99)
		{
			if (nr % 10 == 0)
				return pr+tens[nr/10];
			return pr+tens[nr/10]+"-"+intToStr(nr % 10);
		}
		
		if (nr < 999)
		{
			if (nr % 100 == 0)
				return pr+intToStr(nr/100)+" hundred";
			return pr+intToStr(nr/100)+" hundred "+intToStr(nr % 100);
		}
		
		if (nr < 999999)
		{
			if (nr % 1000 == 0)
				return pr+intToStr(nr/1000)+" thousand";
			if (nr % 1000 < 100)
				return pr+intToStr(nr/1000)+" thousand "+intToStr(nr % 1000);
			return pr+intToStr(nr/1000)+" thousand, "+intToStr(nr % 1000);
		}

		if (nr < 999999999)
		{
			if (nr % 1000000 == 0)
				return pr+intToStr(nr/1000000)+" million";
			if (nr % 1000000 < 100)
				return pr+intToStr(nr/1000000)+" million "+intToStr(nr % 1000000);
			return pr+intToStr(nr/1000000)+" million, "+intToStr(nr % 1000000);
		}

		if (nr % 1000000000 == 0)
			return pr+intToStr(nr/1000000000)+" billion";
		if (nr % 1000000000 < 100)
			return pr+intToStr(nr/1000000000)+" billion "+intToStr(nr % 1000000000);
		return pr+intToStr(nr/1000000000)+" billion, "+intToStr(nr % 1000000000);
	}
	
	public static String resolveDir(String direction)
	{
		if ("north".startsWith(direction))
			return "north";
		if ("east".startsWith(direction))
			return "east";
		if ("south".startsWith(direction))
			return "south";
		if ("west".startsWith(direction))
			return "west";
		if ("up".startsWith(direction))
			return "up";
		if ("down".startsWith(direction))
			return "down";
		return direction;
	}
}