package Client.CreateVoteGroup;

import Common.Profile;
import Common.UserGroup;

public interface CreateVoteGroupService {
  void sendVoteGroup(UserGroup userGroup);
  void requestUserLookup(String username);
  void handleUserLookupResult(Profile profile);
}