package Server;

import Common.Poll;
import Common.PollResult;
import Common.Profile;
import Common.Vote;

import java.sql.SQLException;

// TODO: Needs rethinking - should be split into several depending on usage? (poll, vote or get/post)
public interface DatabaseConnector
{
  public void storeVote(Vote vote);
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
  int createUserGroup(String groupName);
  void addUserToPoll(int userId, int pollId);
  void addGroupToPoll(int groupId, int pollId);

}
