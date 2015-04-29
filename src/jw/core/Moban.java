package jw.core;
import java.sql.*;
import java.util.*;

import jw.data.*;
import static jw.core.MudMain.*;

/**
	The Moban class represents a single mult-ok, ban, or allow.
*/
public class Moban
{
	public int id = 0;
	public String category = "";
	public String type = "";
	public String host = "";
	public long start = 0;
	public long end = 0;
	public String description = "";
	
	public Moban(int newId)
	{
		id = newId;
		load();
	}
	
	public Moban()
	{
		id = create();
		if (id > 0)
			mobans.add(this);
	}
	
	public int create()
	{
		try
		{
			Database.dbQuery.executeUpdate("INSERT INTO mobans VALUES (NULL, '', '', '', 0, 0, '')");
			ResultSet dbResult = Database.dbQuery.executeQuery("SELECT MAX(moban_id) AS max_id FROM mobans");
			dbResult.next();
			return dbResult.getInt("max_id");
		} catch (Exception e) {
			sysLog("bugs", "Error in Moban.create: "+e.getMessage());
			logException(e);
		}
		return 0;
	}
	
	public void save()
	{
		try
		{
			Database.dbQuery.executeUpdate("UPDATE mobans SET "+
					"moban_category = '"+Database.dbSafe(category)+"', "+
					"moban_type = '"+Database.dbSafe(type)+"', "+
					"moban_host = '"+Database.dbSafe(host)+"', "+
					"moban_start = "+start+", "+
					"moban_end = "+end+", "+
					"moban_description = '"+Database.dbSafe(description)+"' "+
					"WHERE moban_id = "+id);
		} catch (Exception e) {
			sysLog("bugs", "Error in Moban.save: "+e.getMessage());
			logException(e);
		}
	}
	
	public void delete()
	{
		try
		{
			Database.dbQuery.executeUpdate("DELETE FROM mobans WHERE moban_id = "+id);
		} catch (Exception e) {
			sysLog("bugs", "Error in Moban.delete: "+e.getMessage());
			logException(e);
		}
		mobans.remove(this);
	}
	
	public void load()
	{
		try
		{
			ResultSet dbResult = Database.dbQuery.executeQuery("SELECT * FROM mobans WHERE moban_id = "+id);
			if (dbResult.next())
			{
				category = dbResult.getString("moban_category");
				type = dbResult.getString("moban_type");
				host = dbResult.getString("moban_host");
				start = dbResult.getLong("moban_start");
				end = dbResult.getLong("moban_end");
				description = dbResult.getString("moban_description");
			}
		} catch (Exception e) {
			sysLog("bugs", "Error in Moban.load: "+e.getMessage());
			logException(e);
		}
	}
	
	public static void loadMobans()
	{
		ArrayList<Integer> loadIds = new ArrayList<Integer>();
		try
		{
			ResultSet dbResult = Database.dbQuery.executeQuery("SELECT * FROM mobans");
			while (dbResult.next())
			{
				loadIds.add(dbResult.getInt("moban_id"));
			}
			for (Integer i : loadIds)
			{
				Moban newMoban = new Moban(i);
				mobans.add(newMoban);
			}
		} catch (Exception e) {
			sysLog("bugs", "Error in loadMobans: "+e.getMessage());
			logException(e);
		}
	}
	
	public static void runCommand(UserCon c, String newCategory, String args)
	{
		String arg1 = CommandHandler.getArg(args, 1).toLowerCase();
		String arg2 = CommandHandler.getArg(args, 2).toLowerCase();
		String arg3 = CommandHandler.getArg(args, 3).toLowerCase();
		String arg4 = CommandHandler.getArg(args, 4).toLowerCase();
		String arg5 = CommandHandler.getLastArg(args, 5);
		
		String newType = "";
		if ("character".startsWith(arg1))
			newType = "character";
		else if ("site".startsWith(arg1))
			newType = "site";
		else if ("cancel".startsWith(arg1) || "delete".startsWith(arg1))
		{
			if (arg2.length() == 0)
			{
				c.sendln("Cancel which "+newCategory+"? (Must match existing "+newCategory+" exactly.)");
				return;
			}
			for (Moban m : mobans)
				if (m.host.equalsIgnoreCase(arg2) && m.category.equals(newCategory))
				{
					m.delete();
					c.sendln(Fmt.cap(newCategory)+" deleted.");
					return;
				}
			c.sendln("No "+newCategory+" matching that string were found.");
			return;
		}
		else
		{
			c.sendln(Fmt.cap(newCategory)+" a character or site?");
			return;
		}
		
		if (arg2.length() == 0)
		{
			c.sendln(Fmt.cap(newCategory)+" what "+newType+"?");
			return;
		}
		
		String newHost = arg2;
		
		if (arg3.length() == 0)
		{
			c.sendln(Fmt.cap(newCategory)+" that "+newType+" for how long?");
			return;
		}
		
		long newEnd = System.currentTimeMillis()/1000;
		if ("forever".startsWith(arg3))
		{
			newEnd = 0;
			arg5 = CommandHandler.getLastArg(args, 4);
		}
		else
		{
			int quant = Fmt.getInt(arg3);
			if (quant <= 0)
			{
				c.sendln("That's not a valid duration number.");
				c.sendln("Enter the duration in the format '# <period>', as in '2 weeks'.");
				return;
			}
			if ("hours".startsWith(arg4))
				newEnd = newEnd+(quant*3600);
			else if ("days".startsWith(arg4))
				newEnd = newEnd+(quant*86400);
			else if ("weeks".startsWith(arg4))
				newEnd = newEnd+(quant*604800);
			else if ("months".startsWith(arg4))
				newEnd = newEnd+(quant*2628000);
			else if ("years".startsWith(arg4))
				newEnd = newEnd+(quant*31536000);
			else
			{
				c.sendln("That's not a valid duration name. (hours, days, weeks, months, years)");
				return;
			}
		}
		
		if (arg5.length() == 0)
		{
			c.sendln("You need to add a description on the end of each "+newCategory+".");
			return;
		}
		
		Moban newMoban = new Moban();
		newMoban.category = newCategory;
		newMoban.type = newType;
		newMoban.host = newHost;
		newMoban.start = System.currentTimeMillis()/1000;
		newMoban.end = newEnd;
		newMoban.description = arg5;
		newMoban.save();

		if (newEnd == 0)
			c.sendln(Fmt.cap(newCategory)+" set on "+newHost+" permanently.");
		else
			c.sendln(Fmt.cap(newCategory)+" set on "+newHost+" until "+longFrmt.format(newEnd*1000)+".");
	}
	
	public static boolean hasMatch(String chCategory, String chName, String chHost)
	{
		for (Moban m : mobans)
			if (m.category.equals(chCategory))
			{
				if (m.type.equals("character") && m.host.equalsIgnoreCase(chName))
					return true;
				else if (m.type.equals("site"))
				{
					if (m.host.equalsIgnoreCase(chHost))
						return true;
					else if (m.host.startsWith("*"))
					{
						if (m.host.endsWith("*") && chHost.contains(m.host.substring(1, m.host.length()-1)))
							return true;
						else if (chHost.endsWith(m.host.substring(1)))
							return true;
					}
					else if (m.host.endsWith("*") && chHost.startsWith(m.host.substring(0, m.host.length()-1)))
						return true;
				}
			}
		return false;
	}
}