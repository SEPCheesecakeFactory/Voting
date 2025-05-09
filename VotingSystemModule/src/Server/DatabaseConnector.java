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
  public void storePoll(Poll poll);
  public boolean userHasAccessToPoll(int userId, int pollId) throws
      SQLException;
}
