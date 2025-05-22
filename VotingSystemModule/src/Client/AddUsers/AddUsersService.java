package Client.AddUsers;

import Common.Profile;
import Common.UserGroup;

import java.util.Set;

public interface AddUsersService
{
  void requestUserLookup(String username);
  void handleUserLookupResult(Profile profile);
  void sendPollAccess(int pollId, Set<Profile> users, Set<UserGroup> groups);
  void requestGroupLookup1(String groupName);
  void handleUserGroupLookupResult1(UserGroup userGroup);

}
