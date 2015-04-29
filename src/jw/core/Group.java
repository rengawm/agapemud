package jw.core;
import java.util.*;
import java.lang.reflect.Method;

import static jw.core.MudMain.*;

/**
The Group class is used when players start/join groups with each other.
*/
public class Group
{
	/** The leader of this group. */
	public CharData leader = null;
	/** All members of this group (including leader). */
	public ArrayList<CharData> members = new ArrayList<CharData>();
	/** Pending invitations to the group. */
	public ArrayList<CharData> invites = new ArrayList<CharData>();
	
	public Group(CharData newLeader)
	{
		leader = newLeader;
		members.add(newLeader);
	}
	
	public void invite(CharData target)
	{
		if (target == leader)
		{
			leader.sendln("You can't invite yourself to a group.");
			return;
		}
		if (members.contains(target))
		{
			leader.sendln("That character is already in your group.");
			return;
		}
		if (invites.contains(target))
		{
			leader.sendln("That character has already been invited to your group.");
			leader.sendln("They must 'group accept' to join.");
			return;
		}
		for (Group g : groups)
		{
			if (g.members.contains(target))
			{
				target.sendln(Fmt.cap(Fmt.seeNameGlobal(target, leader))+" has invited you to a group, but you're already in one.");
				target.sendln("You can use 'group leave' to leave your current group and ask for another invitation.");
				
				leader.sendln("That character is already in another group.");
				leader.sendln("They must 'group leave' first to join your group.");
				return;
			}
			if (g.invites.contains(target))
			{
				target.sendln(Fmt.cap(Fmt.seeNameGlobal(target, leader))+" has invited you to a group, but you're already invited to one.");
				target.sendln("You can use 'group decline' to decline the conflicting invitation and ask for another invitation.");

				leader.sendln("That character has already been invited to another group.");
				leader.sendln("They must 'group decline' first to join your group.");
				return;
			}
		}
		invites.add(target);
		leader.sendln(Fmt.cap(Fmt.seeNameGlobal(leader, target))+" has been invited to the group.");
		target.sendln(Fmt.cap(Fmt.seeNameGlobal(target, leader))+" has invited you to a group.");
		target.sendln("Use 'group accept' to join or 'group decline' to decline.");
		return;
	}
}