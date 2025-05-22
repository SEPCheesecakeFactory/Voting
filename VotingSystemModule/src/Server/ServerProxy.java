package Server;

import Common.*;

import java.util.List;
import java.util.Set;

public class ServerProxy implements ServerModelService
{
  private final ServerModel realService;

  public ServerProxy(ServerModel realService) {
    this.realService = realService;
  }

  @Override public void setCurrentProfile(Profile profile)
  {
    realService.setCurrentProfile(profile);
  }

  @Override public Profile getCurrentProfile()
  {
    return realService.getCurrentProfile();
  }

  @Override public DatabaseConnector getDb()
  {
    return realService.getDb();
  }

  @Override public void storeVote(Vote vote)
  {
    realService.storeVote(vote);
  }

  @Override public void closePoll(int pollId, int clientConnectionIndex)
  {
    realService.closePoll(pollId,clientConnectionIndex);
  }

  @Override public PollResult retrievePollResult(int pollID)
  {
    return realService.retrievePollResult(pollID);
  }

  @Override public void setConnection(ServerConnection connection)
  {
    realService.setConnection(connection);
  }

  @Override public void sendMessageToUser(Message message)
  {
    realService.sendMessageToUser(message);
  }

  @Override public boolean checkPollAccess(int pollId)
  {
    return realService.checkPollAccess(pollId);
  }

  @Override public void sendPollResultsToUser(PollResult pollResult,
      int clientConnectionIndex)
  {
    realService.sendPollResultsToUser(pollResult,clientConnectionIndex);
  }

  @Override public void sendLookupUserResults(Profile profile,
      int clientConnectionIndex)
  {
    realService.sendLookupUserResults(profile, clientConnectionIndex);
  }

  @Override public void sendUpdatedProfile(Profile profile,
      int clientConnectionIndex)
  {
    realService.sendUpdatedProfile(profile, clientConnectionIndex);
  }

  @Override public void storePoll(Poll poll, Profile profile,
      int clientConnectionIndex)
  {
    realService.storePoll(poll, profile, clientConnectionIndex);
  }

  @Override public void sendPoll(int id, int clientConnectionIndex)
  {
    realService.sendPoll(id, clientConnectionIndex);
  }

  @Override public void storeUserGroup(UserGroup userGroup, int creatorId)
  {
    realService.storeUserGroup(userGroup,creatorId);
  }

  @Override public void grantPollAccessToUsers(int pollId, Set<Profile> users,
      int userId)
  {
    realService.grantPollAccessToUsers(pollId, users,userId);
  }

  @Override public void grantPollAccessToGroups(int pollId,
      Set<UserGroup> groups, int userId)
  {
    realService.grantPollAccessToGroups(pollId, groups, userId);
  }

  @Override public void sendLookupGroupResults1(UserGroup group,
      int clientConnectionIndex)
  {
    realService.sendLookupGroupResults1(group, clientConnectionIndex);
  }
  @Override public void sendLookupGroupResults2(UserGroup group,
      int clientConnectionIndex)
  {
    realService.sendLookupGroupResults2(group, clientConnectionIndex);
  }

  @Override public List<UserGroup> getGroupsCreatedByUser(int userId)
  {
    return realService.getGroupsCreatedByUser(userId);
  }

  @Override public void sendUserGroups(List<UserGroup> groups,
      int clientConnectionIndex)
  {
    realService.sendUserGroups(groups, clientConnectionIndex);
  }

  @Override public void handle(Object incoming)
  {
    realService.handle(incoming);
  }

  @Override public void process(String message,
      ServerConnection serverConnection)
  {
    realService.process(message, serverConnection);
  }

  @Override public void sendAvailablePolls(Message message,
      int clientConnectionIndex)
  {
    realService.sendAvailablePolls(message, clientConnectionIndex);
  }

}
