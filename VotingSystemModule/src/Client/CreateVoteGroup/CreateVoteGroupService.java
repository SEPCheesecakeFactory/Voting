package Client.CreateVoteGroup;

import Common.Profile;
import Common.UserGroup;

import java.util.List;

public interface CreateVoteGroupService {
  void sendVoteGroup(UserGroup userGroup);
  void sendEditedVoteGroup(UserGroup userGroup);
  void requestUserLookup(String username);
  void handleUserLookupResult(Profile profile);
  void requestUserGroups();
  void receiveUserGroups(List<UserGroup> groups);
  void requestRemoveGroup(String groupName);
  void handleUserGroupLookupResult2(UserGroup userGroup);
  void requestGroupLookup2(String groupName);
}