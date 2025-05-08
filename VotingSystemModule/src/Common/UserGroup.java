package Common;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class UserGroup implements Serializable
{
  private String groupName;
  private List<Profile> members;

  public UserGroup(String groupName)
  {
    this.groupName = groupName;
    this.members = new ArrayList<>();
  }

  public String getGroupName()
  {
    return groupName;
  }

  public List<Profile> getMembers()
  {
    return members;
  }

  public boolean addMember(Profile profile)
  {
    for (Profile member : members)
    {
      if (member.getId() == profile.getId())
      {
        return false;
      }
      members.add(profile);
      return true;
    }
  }
}
