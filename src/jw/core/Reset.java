package jw.core;
import static jw.core.MudMain.*;

/**
	The Reset class represents a reset in the game, and all associated fields.
*/
public class Reset
{
	/** The ID associated with this reset. */
	public int id = 0;
	/** The type of reset this is. */
	public String type;
	/** The location this reset points to. */
	public Room location;
	/** The id of the subject created by this reset. */
	public int subject;
	/** The maximum number of subjects which can be filling this reset at the same
		time. */
	public int count;
	
	/**
	Allocate a reset and associate it with the given ID.
	
	@param newId The ID to assign this reset.
	*/
	public Reset(int newId)
	{
		id = newId;
	}
	
	/**
	Search for a reset which has the ID {@code targetId}.
	<p>
	This runs through the {@link MudMain#roomResets roomResets} and {@link
	MudMain#resetResets resetResets} global ArrayLists and returns the reset which
	matches {@code targetId}.
	
	@param targetId The reset ID to search for.
	@return The reset which matches {@code targetId}, or {@code null} if none exists.
	*/
	public static Reset lookup(int targetId)
	{
		for (Room r : rooms)
			for (Reset rs : r.resets)
				if (rs.id == targetId)
					return rs;
		return null;
	}
	
	/**
	Fill this reset, depending on the reset type.
	<p>
	For most reset types, this will count the number of objects already in existence
	which are filling this reset. If that number is less than the maximum allowed count,
	another will be created.
	*/
	public void fillReset()
	{
		if (type.equals("mob"))
		{
			int existCount = 0;
			for (CharData ch : mobs)
				if (ch.resetFilled == id)
					existCount++;
			
			if (existCount < count)
			{
				CharProto tempCp = CharProto.lookup(subject);
				if (tempCp != null)
				{
					CharData newChar = new CharData(tempCp);
					newChar.currentRoom = location;
					newChar.resetFilled = id;
					mobs.add(newChar);
					newChar.checkTrigger("load", null, null, "", 0);
					int thisReset = location.resets.lastIndexOf(this);
					for (int ctr = thisReset+1; ctr < location.resets.size(); ctr++)
					{
						Reset tempRs = location.resets.get(ctr);
						if (tempRs.type.equals("mob"))
							break;
						if (tempRs.type.equals("object"))
						{
							ObjProto tempOp = ObjProto.lookup(tempRs.subject);
							if (tempOp != null)
							{
								ObjData newObj = new ObjData(tempOp);
								newObj.resetFilled = tempRs.id;
								newObj.toChar(newChar);
								newObj.checkTrigger("load", null, null, "", 0);
								if (newObj.type.equals("container"))
								{
									for (int ctr2 = ctr+1; ctr2 < location.resets.size(); ctr2++)
									{
										Reset tempRs2 = location.resets.get(ctr2);
										if (tempRs2.type.equals("inside"))
										{
											ObjProto tempOp2 = ObjProto.lookup(tempRs2.subject);
											if (tempOp2 != null)
											{
												ObjData newObj2 = new ObjData(tempOp2);
												newObj2.resetFilled = tempRs2.id;
												newObj2.toObject(newObj);
												newObj2.checkTrigger("load", null, null, "", 0);
											}
										}
										else
										{
											break;
										}
									}
								}
							}
						}
						if (tempRs.type.equals("lootgroup"))
						{
							Lootgroup tempLg = Lootgroup.lookup(tempRs.subject);
							if (tempLg != null)
							{
								ObjData newObj = tempLg.getObject();
								newObj.resetFilled = tempRs.id;
								newObj.toChar(newChar);
								newObj.checkTrigger("load", null, null, "", 0);
							}
						}
					}
					newChar.mobCommand("wear all");
					newChar.hp = newChar.maxHp();
					newChar.mana = newChar.maxMana();
					newChar.energy = newChar.maxEnergy();
				}
			}
		}
		else if (type.equals("object"))
		{
			for (ObjData o : location.objects)
				if (o.resetFilled == id)
				{
					if (o.type.equals("container"))
					{
						int thisReset = location.resets.lastIndexOf(this);
						for (int ctr = thisReset+1; ctr < location.resets.size(); ctr++)
						{
							Reset tempRs2 = location.resets.get(ctr);
							if (tempRs2.type.equals("inside"))
							{
								boolean found = false;
								for (ObjData o2 : o.objects)
									if (o2.resetFilled == tempRs2.id)
									{
										found = true;
										break;
									}
								
								if (found)
									continue;

								ObjProto tempOp2 = ObjProto.lookup(tempRs2.subject);
								if (tempOp2 != null)
								{
									ObjData newObj2 = new ObjData(tempOp2);
									newObj2.resetFilled = tempRs2.id;
									newObj2.toObject(o);
									newObj2.checkTrigger("load", null, null, "", 0);
								}
							}
							else
							{
								break;
							}
						}
					}
					return;
				}
			ObjProto tempOp = ObjProto.lookup(subject);
			if (tempOp != null)
			{
				ObjData newObj = new ObjData(tempOp);
				newObj.resetFilled = id;
				newObj.toRoom(location);
				newObj.checkTrigger("load", null, null, "", 0);
				if (newObj.type.equals("container"))
				{
					int thisReset = location.resets.lastIndexOf(this);
					for (int ctr = thisReset+1; ctr < location.resets.size(); ctr++)
					{
						Reset tempRs2 = location.resets.get(ctr);
						if (tempRs2.type.equals("inside"))
						{
							ObjProto tempOp2 = ObjProto.lookup(tempRs2.subject);
							if (tempOp2 != null)
							{
								ObjData newObj2 = new ObjData(tempOp2);
								newObj2.resetFilled = tempRs2.id;
								newObj2.toObject(newObj);
								newObj2.checkTrigger("load", null, null, "", 0);
							}
						}
						else
						{
							break;
						}
					}
				}
			}
		}
		else if (type.equals("lootgroup"))
		{
			for (ObjData o : location.objects)
				if (o.resetFilled == id)
					return;
			Lootgroup tempLg = Lootgroup.lookup(subject);
			if (tempLg != null)
			{
				ObjData newObj = tempLg.getObject();
				newObj.resetFilled = id;
				newObj.toRoom(location);
				newObj.checkTrigger("load", null, null, "", 0);
			}
		}
	}
}