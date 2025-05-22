package Server;

import Common.*;

import java.util.List;
import java.util.Set;

public interface ServerModelService
{
  void setCurrentProfile(Profile profile);
  Profile getCurrentProfile();
  DatabaseConnector getDb();
  void storeVote(Vote vote);
  void closePoll(int pollId, int clientConnectionIndex);
  PollResult retrievePollResult(int pollID);
  void setConnection(ServerConnection connection);
  void sendMessageToUser(Message message);
  boolean checkPollAccess(int pollId);
  void sendPollResultsToUser(PollResult pollResult, int clientConnectionIndex);
  void sendLookupUserResults(Profile profile, int clientConnectionIndex);
  void sendUpdatedProfile(Profile profile, int clientConnectionIndex);
  void storePoll(Poll poll, Profile profile, int clientConnectionIndex);
  void sendPoll(int id, int clientConnectionIndex);
  void storeUserGroup(UserGroup userGroup, int creatorId);
  void grantPollAccessToUsers(int pollId, Set<Profile> users, int userId);
  void grantPollAccessToGroups(int pollId, Set<UserGroup> groups, int userId);
  void sendLookupGroupResults1(UserGroup group, int clientConnectionIndex);
  void sendLookupGroupResults2(UserGroup group, int clientConnectionIndex);
  List<UserGroup> getGroupsCreatedByUser(int userId);
  void sendUserGroups(List<UserGroup> groups, int clientConnectionIndex);
  void handle(Object incoming);
  void process(String message, ServerConnection serverConnection);
  void sendAvailablePolls(Message message, int clientConnectionIndex);

}
