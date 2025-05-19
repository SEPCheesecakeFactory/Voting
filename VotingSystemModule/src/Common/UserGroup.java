package Common;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class UserGroup implements Serializable
{
  private String groupName;
  private List<Profile> members;
  private int id;

  public UserGroup(String groupName)
  {
    this.groupName = groupName;
    this.members = new ArrayList<>();
  }

  public String getGroupName()
  {
    return groupName;
  }

  public void setId(int id)
  {
    this.id = id;
  }

  public int getId()
  {
    return id;
  }

  public List<Profile> getMembers()
  {
    return members;
  }

  public boolean addMember(Profile profile)
  {
    for (Profile member : members)
    {
      if (member.getUsername().equals(profile.getUsername()))
      {
        return false;
      }
    }
    members.add(profile);
    return true;
  }

  public boolean removeMember(Profile profile)
  {
    return members.remove(profile);
  }

}
