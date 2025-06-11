package Server;

import Common.*;
import Utils.Logger;

import java.util.List;
import java.util.Set;

public class ServerProxy implements ServerModelService
{
  private final ServerModel realService;

  public ServerProxy(ServerModel realService)
  {
    this.realService = realService;
  }

  @Override public void setCurrentProfile(Profile profile)
  {
    Logger.log("setCurrentProfile");
    realService.setCurrentProfile(profile);
  }

  @Override public Profile getCurrentProfile()
  {
    Logger.log("getCurrentProfile");
    return realService.getCurrentProfile();
  }

  @Override public DatabaseConnector getDb()
  {
    Logger.log("getDb");
    return realService.getDb();
  }

  @Override public void storeVote(Vote vote)
  {
    Logger.log("storeVote");
    realService.storeVote(vote);
  }

  @Override public void closePoll(int pollId, int clientConnectionIndex)
  {
    Logger.log("closePoll");
    realService.closePoll(pollId, clientConnectionIndex);
  }

  @Override public PollResult retrievePollResult(int pollID)
  {
    Logger.log("retrievePollResult");
    return realService.retrievePollResult(pollID);
  }

  @Override public void setConnection(ServerConnection connection)
  {
    Logger.log("setConnection");
    realService.setConnection(connection);
  }

  @Override public void sendMessageToUser(Message message)
  {
    Logger.log("sendMessageToUser");
    realService.sendMessageToUser(message);
  }

  @Override public boolean checkPollAccess(int pollId)
  {
    Logger.log("checkPollAccess");
    return realService.checkPollAccess(pollId);
  }

  @Override public void sendPollResultsToUser(PollResult pollResult,
      int clientConnectionIndex)
  {
    Logger.log("sendPollResultsToUser");
    realService.sendPollResultsToUser(pollResult, clientConnectionIndex);
  }

  @Override public void sendLookupUserResults(Profile profile,
      int clientConnectionIndex)
  {
    Logger.log("sendLookupUserResults");
    realService.sendLookupUserResults(profile, clientConnectionIndex);
  }

  @Override public void sendUpdatedProfile(Profile profile,
      int clientConnectionIndex)
  {
    Logger.log("sendUpdatedProfile");
    realService.sendUpdatedProfile(profile, clientConnectionIndex);
  }

  @Override public void storePoll(Poll poll, Profile profile,
      int clientConnectionIndex)
  {
    Logger.log("storePoll");
    realService.storePoll(poll, profile, clientConnectionIndex);
  }

  @Override public void sendPoll(int id, int clientConnectionIndex)
  {
    Logger.log("sendPoll");
    realService.sendPoll(id, clientConnectionIndex);
  }

  @Override public void storeUserGroup(UserGroup userGroup, int creatorId)
  {
    Logger.log("storeUserGroup");
    realService.storeUserGroup(userGroup, creatorId);
  }

  @Override public void grantPollAccessToUsers(int pollId, Set<Profile> users,
      int userId)
  {
    Logger.log("grantPollAccessToUsers");
    realService.grantPollAccessToUsers(pollId, users, userId);
  }

  @Override public void grantPollAccessToGroups(int pollId,
      Set<UserGroup> groups, int userId)
  {
    Logger.log("grantPollAccessToGroups");
    realService.grantPollAccessToGroups(pollId, groups, userId);
  }

  @Override public void sendLookupGroupResults1(UserGroup group,
      int clientConnectionIndex)
  {
    Logger.log("sendLookupGroupResults1");
    realService.sendLookupGroupResults1(group, clientConnectionIndex);
  }

  @Override public void sendLookupGroupResults2(UserGroup group,
      int clientConnectionIndex)
  {
    Logger.log("sendLookupGroupResults2");
    realService.sendLookupGroupResults2(group, clientConnectionIndex);
  }

  @Override public List<UserGroup> getGroupsCreatedByUser(int userId)
  {
    Logger.log("getGroupsCreatedByUser");
    return realService.getGroupsCreatedByUser(userId);
  }

  @Override public void sendUserGroups(List<UserGroup> groups,
      int clientConnectionIndex)
  {
    Logger.log("sendUserGroups");
    realService.sendUserGroups(groups, clientConnectionIndex);
  }

  @Override public void handle(Object incoming)
  {
    Logger.log("handle");
    realService.handle(incoming);
  }

  @Override public void process(String message,
      ServerConnection serverConnection)
  {
    Logger.log("process");
    realService.process(message, serverConnection);
  }

  @Override public void sendAvailablePolls(Message message,
      int clientConnectionIndex)
  {
    Logger.log("sendAvailablePolls");
    realService.sendAvailablePolls(message, clientConnectionIndex);
  }
}
