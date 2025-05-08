package Server;

import Common.Poll;
import Common.Profile;
import Common.Vote;

// TODO: Needs rethinking - should be split into several depending on usage? (poll, vote or get/post)
public interface DatabaseConnector
{
  public void storeVote(Vote vote);
  public Poll retrievePoll(int id);
  public int loginOrRegisterAProfile(Profile profile);
  public void changeUsername(Profile profile);
}
