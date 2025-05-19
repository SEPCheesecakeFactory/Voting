package Server;

import Common.*;
import Utils.Logger;

import java.sql.SQLException;
import java.util.List;

public class DatabaseConnectionProxy implements DatabaseConnector
{
  protected DatabaseConnection databaseConnection;
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

  @Override public Poll storePoll(Poll poll, Profile profile)
  {
    poll.setId(databaseConnection.storePoll(poll, profile).getId());
    Logger.log("Poll " + poll.getId() + " stored in the database.");
    return poll;
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

  @Override public void addUserToGroup(int userId, int groupId)
  {
    databaseConnection.addUserToGroup(userId,groupId);
  }

  @Override public int createUserGroup(String groupName, int creatorId)
  {
    return databaseConnection.createUserGroup(groupName, creatorId);
  }





  @Override public Profile getProfileByUsername(String username)
  {
    return databaseConnection.getProfileByUsername(username);
  }

  @Override public UserGroup getGroupByUsername(String username)
  {
    return databaseConnection.getGroupByUsername(username);
  }

  @Override public void grantPollAccessToUser(int pollId, int userId, int clientId)
  {
    databaseConnection.grantPollAccessToUser(pollId, userId, clientId);
  }

  @Override public void grantPollAccessToGroup(int pollId, String groupName, int clientId)
  {
    databaseConnection.grantPollAccessToGroup(pollId, groupName, clientId);
  }

  @Override
  public List<Poll> getAllAvailablePolls(int userId) {
    return databaseConnection.getAllAvailablePolls(userId);
  }

  @Override public List<UserGroup> getGroupsCreatedByUser(int userId)
  {
    return databaseConnection.getGroupsCreatedByUser(userId);
  }
}
