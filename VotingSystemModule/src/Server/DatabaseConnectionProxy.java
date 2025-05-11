package Server;

import Common.Poll;
import Common.PollResult;
import Common.Profile;
import Common.Vote;
import Utils.Logger;

import java.sql.SQLException;

public class DatabaseConnectionProxy implements DatabaseConnector
{
  private DatabaseConnection databaseConnection;
  public DatabaseConnectionProxy() throws SQLException
  {
    databaseConnection = new DatabaseConnection();
  }

  @Override public void storeVote(Vote vote)
  {
    databaseConnection.storeVote(vote);
    Logger.log("Vote sent to the database. {"+vote+"}");
  }

  @Override public void editVote(Vote vote)
  {
    databaseConnection.editVote(vote);
    Logger.log("Vote sent to the database. {"+vote+"}");
  }

  @Override public Poll retrievePoll(int id)
  {
    Poll poll = databaseConnection.retrievePoll(id);
    Logger.log("Poll retrieved. " + poll);
    return poll;
  }

  @Override public PollResult retrievePollResults(int id)
  {
    return databaseConnection.retrievePollResults(id);

    // maybe this instead of null?
    // PollResult pollResult = databaseConnection.retrievePollResults(id);
    // Logger.log("Poll results retrieved. " + pollResult);
    // return pollResult;
  }

  @Override public int loginOrRegisterAProfile(Profile profile)
  {
    Logger.log("user "+profile.getUsername()+" logged or registered");
    return databaseConnection.loginOrRegisterAProfile(profile);


  }

  @Override public void changeUsername(Profile profile)
  {
    databaseConnection.changeUsername(profile);
    Logger.log("Username changed");
  }

  @Override public void storePoll(Poll poll)
  {
    databaseConnection.storePoll(poll);
    Logger.log("Poll stored in the database.");
  }

  public boolean userHasAccessToPoll(int userId, int pollId) {
    boolean hasAccess = databaseConnection.userHasAccessToPoll(userId, pollId);
    Logger.log("User " + userId + (hasAccess ? " has access to " : " does not have access to ") + "poll " + pollId);
    return hasAccess;
  }

  @Override
  public void closePollAndSaveResults(int pollId)
  {
    databaseConnection.closePollAndSaveResults(pollId);
    Logger.log("Poll with ID " + pollId + " marked as closed in database.");
  }

  public boolean isOwner(int userId, int pollId) {
    return databaseConnection.isOwner(userId, pollId); // delegates to real DB
  }
}
