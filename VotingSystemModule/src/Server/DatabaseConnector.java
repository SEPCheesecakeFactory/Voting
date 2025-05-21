package Server;

import Common.*;

import java.sql.SQLException;
import java.util.List;

// TODO: Needs rethinking - should be split into several depending on usage? (poll, vote or get/post)
public interface DatabaseConnector
{
  public boolean storeVote(Vote vote);
  public void editVote(Vote vote);
  public Poll retrievePoll(int id);
  public PollResult retrievePollResults(int id);
  public int loginOrRegisterAProfile(Profile profile);
  public void changeUsername(Profile profile);
  public Poll storePoll(Poll poll, Profile profile);
  public boolean userHasAccessToPoll(int userId, int pollId) throws
      SQLException;
  public void closePollAndSaveResults(int pollId);
  boolean isOwner(int userId, int pollId);
  void addUserToGroup(int userId, int groupId);
  int createUserGroup(String groupName, int creatorId);
//  void addUserToPoll(int userId, int pollId);
//  void addGroupToPoll(int groupId, int pollId); grantPollAccess methods does the same in a better way
  Profile getProfileByUsername(String username);
  UserGroup getGroupByUsername(String username);
  void grantPollAccessToUser(int pollId, int userId, int clientId);
  void grantPollAccessToGroup(int pollId, String groupName, int clientId);
  List<Poll> getAllAvailablePolls(int userId);
  List<UserGroup> getGroupsCreatedByUser(int userId);
}
