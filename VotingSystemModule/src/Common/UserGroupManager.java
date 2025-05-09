package Common;

import java.util.ArrayList;
import java.util.List;

public class UserGroupManager
{
  public List<UserGroup> groups;

  public UserGroupManager()
  {
    groups = new ArrayList<>();
  }

  public boolean createGroup(String groupName)
  {
    if (getGroup(groupName) != null)
    {
      return false;
    }
    groups.add(new UserGroup(groupName));
    return true;
  }

  public UserGroup getGroup(String groupName)
  {
    for (UserGroup group : groups)
    {
      if (group.getGroupName().equals(groupName))
      {
        return group;
      }
    }
    return null;
  }

  public List<UserGroup> getAllGroups()
  {
    return new ArrayList<>(groups);
  }

  public boolean isUserInGroup(String groupName, Profile user)
  {
    UserGroup group = getGroup(groupName);
    return group != null && group.getMembers().contains(user);
  }
}