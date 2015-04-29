package jw.core;
import java.sql.*;
import java.util.*;

import jw.commands.*;
import jw.data.*;
import static jw.core.MudMain.*;

/**
	The Board class represents a single message board.
*/
public class Board
{
	/** The board's ID number. */
	public int id;
	/** The name of this board. */
	public String name = "";
	/** The description of this board. */
	public String description = "";
	/** The time until posts on this board are archived, in days. */
	public long timeToDelete = -1;

	public Board(int newId)
	{
		id = newId;
	}

	public static void loadAll()
	{
		try
		{
			ResultSet dbResult = Database.dbQuery.executeQuery(""+
					"SELECT * FROM boards "+
					"ORDER BY board_id");
			while (dbResult.next())
			{
				Board b = new Board(dbResult.getInt("board_id"));
				b.name = dbResult.getString("board_name");
				b.description = dbResult.getString("board_description");
				b.timeToDelete = dbResult.getInt("board_ttd");
				boards.add(b);
			}
		} catch (Exception e) {
			sysLog("bugs", "Error in Board.loadAll: "+e.getMessage());
			logException(e);
		}
	}
	
	public static Board lookup(int targetId)
	{
		for (Board b : boards)
			if (b.id == targetId)
				return b;
		return null;
	}
	
	public ArrayList<BoardMessage> readable(UserCon c, boolean unreadOnly)
	{
		ArrayList<BoardMessage> result = new ArrayList<BoardMessage>();
		for (BoardMessage m : boardMessages)
			if (m.postedTo == this)
				if (m.canRead(c) &&
					(m.postedTime > c.lastRead.get(this) || !unreadOnly))
					result.add(m);
		return result;
	}
	
	public void print(UserCon c)
	{
		ArrayList<BoardMessage> getNotes = readable(c, false);
		if (getNotes.size() == 0)
		{
			c.sendln("There are no messages on the "+name+" board.");
			return;
		}
		int ctr = 1;
		for (BoardMessage m : getNotes)
		{
			if (m.to.contains(" "+c.ch.shortName.toLowerCase())
				|| m.to.contains(c.ch.shortName.toLowerCase()+" ")
				|| m.to.equalsIgnoreCase(c.ch.shortName))
				c.send("{R>> ");
			else
				c.send("   ");
			c.sendln("}M[}n"+Fmt.rfit(""+ctr, 4)+"}M] }m"+Fmt.fit(m.author, 15)+"}M: }N"+m.title+"{x");
			ctr++;
		}
	}
	
	public BoardMessage noteNr(UserCon c, int nr)
	{
		ArrayList<BoardMessage> getNotes = readable(c, false);
		if (getNotes.size() == 0)
		{
			c.sendln("There are no messages on the "+name+" board.");
			return null;
		}
		nr--;
		if (nr < 0 || nr >= getNotes.size())
		{
			c.sendln("That number is out of range. (1-"+getNotes.size()+")");
			return null;
		}
		return getNotes.get(nr);
	}
	
	public static void boardCommand(UserCon c, String args, String boardName)
	{
		Board getBoard = null;
		for (Board b : boards)
			if (b.name.equals(boardName))
				getBoard = b;
		if (getBoard == null)
		{
			sysLog("bugs", "No "+boardName+" board loaded.");
			return;
		}
		
		if (args.length() == 0)
		{
			ArrayList<BoardMessage> readable = getBoard.readable(c, true);
			if (readable.size() == 0)
			{
				c.sendln("You have no unread messages on the "+boardName+" board.");
				return;
			}
			readable.get(0).print(c);
			return;
		}

		String origArgs = args;
		String arg1 = CommandHandler.getArg(args, 1).toLowerCase();
		args = args.toLowerCase();
		
		if ("to".startsWith(arg1)
			|| "subject".startsWith(arg1)
			|| "title".startsWith(arg1)
			|| "text".startsWith(arg1)
			|| "write".startsWith(arg1)
			|| "show".startsWith(arg1))
		{
			InfoCommands.doMessage(c, origArgs);
			return;
		}
		if ("post".startsWith(arg1))
		{
			InfoCommands.doMessage(c, "post "+getBoard.name);
			return;
		}

		if ("list".startsWith(args))
		{
			getBoard.print(c);
			return;
		}

		if ("catchup".startsWith(args))
		{
			c.lastRead.put(getBoard, System.currentTimeMillis()/1000);
			c.sendln("All messages on the "+boardName+" board marked as read.");
			return;
		}
		
		String arg2 = CommandHandler.getArg(args, 2).toLowerCase();
		if ("read".startsWith(arg1))
			arg1 = arg2;
		int readNr = Fmt.getInt(arg1);
		if (readNr == 0)
		{
			c.sendln("That's not a valid message number.");
			return;
		}
		BoardMessage targetNote = getBoard.noteNr(c, readNr);
		if (targetNote == null)
			return;
		
		if (arg2.length() == 0)
		{
			targetNote.print(c);
			return;
		}
		if ("forward".startsWith(arg2) || "copy".startsWith(arg2))
		{
			c.writing.title = "Fwd: "+targetNote.title;
			c.writing.text = targetNote.text;
			c.sendln("Message title and text copied to your message editor.");
			return;
		}
		if ("delete".startsWith(arg2))
		{
			if (c.hasPermission("staff") || targetNote.author.equalsIgnoreCase(c.ch.shortName))
			{
				if (!c.hasPermission("staff") && targetNote.postedTime < (System.currentTimeMillis()/1000)+1800)
				{
					c.sendln("You can only delete messages up to 30 minutes after they're posted.");
					return;
				}
				targetNote.delete();
				c.sendln("Message deleted.");
				return;
			}
			else
			{
				c.sendln("You can't delete that message.");
				return;
			}
		}
		targetNote.print(c);
	}
}