package jw.core;
import java.sql.*;
import java.util.*;

import jw.data.*;
import static jw.core.MudMain.*;

/**
	The Boardmessage class represents a single message to be posted on a board.
*/
public class BoardMessage
{
	/** The message's ID number. */
	public int id = 0;
	/** The board this post is added to. */
	public Board postedTo;
	/** The time this post was added, in Unix timecode */
	public long postedTime = 0;
	/** The author of this post. */
	public String author = "";
	/** The recipient of this post. */
	public String to = "";
	/** The title of this post. */
	public String title = "";
	/** The text of the post. */
	public String text = "";
	
	public BoardMessage(int newId)
	{
		id = newId;
	}

	public BoardMessage(String newAuthor)
	{
		author = newAuthor;
	}
	
	public static void loadAll()
	{
		try
		{
			ResultSet dbResult = Database.dbQuery.executeQuery(""+
					"SELECT * FROM board_msgs "+
					"ORDER BY board_msg_id");
			while (dbResult.next())
			{
				BoardMessage m = new BoardMessage(dbResult.getInt("board_msg_id"));
				m.postedTime = dbResult.getLong("board_msg_posted");
				m.author = dbResult.getString("board_msg_author");
				m.to = dbResult.getString("board_msg_to");
				m.title = dbResult.getString("board_msg_title");
				m.text = dbResult.getString("board_msg_text");
				Board tempBoard = Board.lookup(dbResult.getInt("board_msg_board"));
				if (tempBoard != null)
				{
					m.postedTo = tempBoard;
					boardMessages.add(m);
				}
			}
		} catch (Exception e) {
			sysLog("bugs", "Error in BoardMessage.loadAll: "+e.getMessage());
			logException(e);
		}
	}
	
	public void save()
	{
		postedTime = System.currentTimeMillis()/1000;
		try
		{
			Database.dbQuery.executeUpdate(""+
					"INSERT INTO board_msgs VALUES ("+
					"NULL, "+
					postedTo.id+", "+
					postedTime+", "+
					"'"+Database.dbSafe(author)+"', "+
					"'"+Database.dbSafe(to)+"', "+
					"'"+Database.dbSafe(title)+"', "+
					"'"+Database.dbSafe(text)+"')");
			ResultSet dbResult = Database.dbQuery.executeQuery(""+
					"SELECT MAX(board_msg_id) AS max_id FROM board_msgs");
			dbResult.next();
			id = dbResult.getInt("max_id");
		} catch (Exception e) {
			sysLog("bugs", "Error in BoardMessage.save: "+e.getMessage());
			logException(e);
		}
	}
	
	public void delete()
	{
		boardMessages.remove(this);
		try
		{
			Database.dbQuery.executeUpdate(""+
					"DELETE FROM board_msgs WHERE board_msg_id = "+id);
		} catch (Exception e) {
			sysLog("bugs", "Error in BoardMessage.delete: "+e.getMessage());
			logException(e);
		}
	}
	
	public boolean canRead(UserCon c)
	{
		if (author.equalsIgnoreCase(c.ch.shortName))
			return true;
		for (String s : to.split(" "))
		{
			if (s.equalsIgnoreCase("all"))
				return true;
			if (s.equalsIgnoreCase("staff") && c.hasPermission("staff"))
				return true;
			if ((s.equalsIgnoreCase("builder") || s.equalsIgnoreCase("builders")) && c.hasPermission("builder"))
				return true;
			if (s.equalsIgnoreCase(c.ch.shortName))
				return true;
		}
		return false;
	}
	
	public void print(UserCon c)
	{
		c.sendln("}mAuthor}M: }n"+author);
		c.sendln("}m    To}M: }n"+to);
		c.sendln("}m Title}M: }N"+title);
		if (postedTime > 0)
			c.sendln("}mPosted}M: }n"+longFrmt.format(postedTime*1000+c.timeAdj*3600000));
		c.sendln(Fmt.heading(""));
		c.sendln("{x"+text);
		c.sendln(Fmt.heading(""));
		if (postedTo != null)
			if (c.lastRead.get(postedTo) < postedTime)
				c.lastRead.put(postedTo, postedTime);
	}
}