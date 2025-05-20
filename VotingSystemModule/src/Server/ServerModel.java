package Server;

import Common.*;
import Utils.JsonUtil;
import Utils.Logger;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.lang.reflect.Type;
import java.sql.SQLException;
import java.util.List;
import java.util.Set;

public class ServerModel implements ServerModelService {
  private final DatabaseConnector db;
  private final ConnectionPool connectionPool;
  private Profile currentProfile; // Track logged-in user
  private ServerConnection connection; // For sending direct messages

  public ServerModel(DatabaseConnector db, ConnectionPool connectionPool) {
    this.db = db;
    this.connectionPool = connectionPool;
  }

  public void setCurrentProfile(Profile profile) {
    this.currentProfile = profile;
  }

  public Profile getCurrentProfile() {
    return currentProfile;
  }

  public DatabaseConnector getDb() {
    return db;
  }

  public void storeVote(Vote vote) {
    db.storeVote(vote);
  }

  public void closePoll(int pollId, int clientConnectionIndex) {
    db.closePollAndSaveResults(pollId);
    Message message = new Message(MessageType.ClosePoll);
    message.addParam("pollClosed", pollId);
    message.addParam("clientConnectionIndex", clientConnectionIndex);
    connectionPool.changeToMap(message, connection);
    try
    {
      connectionPool.sendDirectMessage(message);
    }
    catch (IOException e)
    {
      Logger.log(e.getMessage());
    }
  }

  public PollResult retrievePollResult(int pollID) {
    return db.retrievePollResults(pollID);
  }

  // Optionally inject connection to send direct messages to the client
  public void setConnection(ServerConnection connection) {
    this.connection = connection;
  }

  public void sendMessageToUser(String message) {
    try {
      if (connection != null) {
          connection.send(message);
      } else {
        Logger.log("Cannot send message to user: no connection attached.");
      }
    } catch (IOException e) {
      Logger.log("Failed to send message to user: " + e.getMessage());
    }
  }

  public boolean checkPollAccess(int pollId) {
    if (currentProfile == null) {
      sendMessageToUser("Not logged in.");
      return false;
    }

    try
    {
      if (!db.userHasAccessToPoll(currentProfile.getId(), pollId)) {
        sendMessageToUser("You do not have access to this poll.");
        return false;
      }
    }
    catch (SQLException e)
    {
      Logger.log(e.getMessage());
    }

    return true;
  }

  public void sendPollResultsToUser(PollResult pollResult, int clientConnectionIndex){
    try {
      Message message = new Message(MessageType.SendResultResults);
      message.addParam("pollResult", pollResult);
      message.addParam("clientConnectionIndex", clientConnectionIndex);
      connectionPool.changeToMap(message, connection);
      connectionPool.sendDirectMessage(message);
      Logger.log("ServerModel: Results sent");
    } catch (IOException e) {
      Logger.log("Failed to send pollResult to user: " + e.getMessage());
    }
  }
  public void sendLookupUserResults(Profile profile, int clientConnectionIndex)
  {
    try
    {
      Message message = new Message(MessageType.SendLookupUserResult);
      message.addParam("profile", profile);
      message.addParam("clientConnectionIndex", clientConnectionIndex);
      connectionPool.changeToMap(message, connection);
      connectionPool.sendDirectMessage(message);
    }
    catch (IOException e)
    {
      Logger.log("Failed to send LookupUserResults to user: " + e.getMessage());    }

  }
  public void sendUpdatedProfile(Profile profile, int clientConnectionIndex){
    try {
      Logger.log("ServerModel: Profile send");
      Message message = new Message(MessageType.SendProfileBack);
      message.addParam("UpdatedProfile", profile);
      message.addParam("clientConnectionIndex", clientConnectionIndex);
      connectionPool.changeToMap(message, connection);
      connectionPool.sendDirectMessage(message);
    } catch (IOException e) {
      Logger.log("Failed to send the Updated profile to user: " + e.getMessage());
    }
  }

  public void storePoll(Poll poll, Profile profile, int clientConnectionIndex)
  {
    try
    {
      poll.setId(db.storePoll(poll, profile).getId());
      Message message = new Message(MessageType.SendCreatedPoll);
      message.addParam("poll", poll);
      message.addParam("clientConnectionIndex", clientConnectionIndex);
      connectionPool.changeToMap(message, connection);
      connectionPool.sendDirectMessage(message);
    }
    catch (IOException e)
    {
      Logger.log("ServerModel: Failed to store poll: " + e.getMessage());
    }

  }

  public void sendPoll(int id, int clientConnectionIndex)
  {
    try
    {
      Poll poll = db.retrievePoll(id);
      Message message = new Message(MessageType.SendPoll);
      message.addParam("poll",poll);
      message.addParam("clientConnectionIndex", clientConnectionIndex);
      connectionPool.changeToMap(message, connection);
      connectionPool.sendDirectMessage(message);
    }
    catch (IOException e)
    {
      Logger.log("ServerModel: Failed to send the Poll: " + e.getMessage());
    }
  }

  public void storeUserGroup(UserGroup userGroup, int creatorId) {

    int groupId = db.createUserGroup(userGroup.getGroupName(), creatorId);
    userGroup.setId(groupId);


    for (Profile profile : userGroup.getMembers()) {
      db.addUserToGroup(profile.getId(), groupId);
    }
  }
  public void grantPollAccessToUsers(int pollId, Set<Profile> users, int userId)
  {
    for (Profile user : users) {
      db.grantPollAccessToUser(pollId, user.getId(), userId);
    }
  }
  public void grantPollAccessToGroups(int pollId, Set<UserGroup> groups, int userId)
  {
    for (UserGroup group : groups) {
      db.grantPollAccessToGroup(pollId, group.getGroupName(), userId);
    }
  }

  public void sendLookupGroupResults(UserGroup group, int clientConnectionIndex)
  {
    try
    {
      Message message = new Message(MessageType.SendLookupGroupResult);
      message.addParam("userGroup", group);
      message.addParam("clientConnectionIndex", clientConnectionIndex);
      connectionPool.changeToMap(message, connection);
      connectionPool.sendDirectMessage(message);
    }
    catch (IOException e)
    {
      Logger.log("Failed to send sendLookupGroupResults to user: " + e.getMessage());    }
  }

  public List<UserGroup> getGroupsCreatedByUser(int userId)
  {
    List<UserGroup> groups = db.getGroupsCreatedByUser(userId);
    return groups;
  }

  public void sendUserGroups(List<UserGroup> groups, int clientConnectionIndex)
  {

    try{
      Message message = new Message(MessageType.SendUserGroups);
      message.addParam("userGroups",groups);
      message.addParam("clientConnectionIndex", clientConnectionIndex);
      connectionPool.changeToMap(message, connection);
      connectionPool.sendDirectMessage(message);
    }
    catch (IOException e)
    {
      Logger.log("Failed to send sendUserGroups to user: " + e.getMessage());
    }
  }
  public void handle(Object incoming) {
    try {
      if (incoming instanceof Vote vote) {
        storeVote(vote);
        Logger.log("Vote handled by ServerProxy: " + vote);
      }
      else if (incoming instanceof String message && message.startsWith("close_poll:")) {
        int pollId = Integer.parseInt(message.split(":")[1]);

        int userId = getCurrentProfile().getId(); // assumes you store profile in ServerModel

        if (!getDb().isOwner(userId, pollId)) {
          Logger.log("Unauthorized close attempt by user " + userId + " on poll " + pollId);
          sendMessageToUser("You are not authorized to close this poll.");
          return;
        }

        //model.closePoll(pollId, clientConnectionIndex);
        Logger.log("Poll close request handled for ID: " + pollId + " by user " + userId);
      }
      //TODO: Send the Poll Results ot the client *** *** *** *** *** *** *** *** *** *** *** *** *** *** *** *** *** ***
      else if (incoming instanceof String message && message.startsWith("result_request:")){
        int pollId = Integer.parseInt(message.split(":")[1]);
        PollResult pollResult = retrievePollResult(pollId);
        Logger.log("Poll Results handled for: " +pollId);
      }
      else {
        Logger.log("Unknown object type received in ServerProxy: " + incoming);
      }
    }
    catch (Exception e) {
      Logger.log("Error in ServerProxy: " + e.getMessage());
      e.printStackTrace();
    }
  }


  public void process(String message)
  {
    Message messageObject = JsonUtil.deserialize(message, Message.class);
    int clientConnectionIndex = messageObject.getParam("clientConnectionIndex", int.class);
    try {
      int pollId;
      Profile profile;
      switch (messageObject.getType()) {
        case MessageType.SendPollRequest:
          pollId = messageObject.getParam("pollId", int.class);
          sendPoll(pollId, clientConnectionIndex);
          break;
        case MessageType.DisplayPollRequest:
          pollId = messageObject.getParam("pollId", int.class);
          sendPoll(pollId, clientConnectionIndex);
          Logger.log("Poll display request handled for ID: " + pollId);
          break;
        case MessageType.GetAvailablePolls:
          int userId=messageObject.getParam("userId",int.class);
          List<Poll> availablePolls = getDb().getAllAvailablePolls(userId);
          Message sendMsg = new Message(MessageType.SendAvailablePolls);
          sendMsg.addParam("polls", availablePolls);
          sendMessageToUser(JsonUtil.serialize(sendMsg));
          Logger.log("Sent available polls to client.");
          break;
        case MessageType.SendVote:
          Vote vote = messageObject.getParam("vote", Vote.class);
          storeVote(vote);
          break;
        case MessageType.ClosePoll:
          pollId = messageObject.getParam("pollId", int.class);

          userId = messageObject.getParam("userId", int.class);

          if (!getDb().isOwner(userId, pollId)) {
            Logger.log("Unauthorized close attempt by user " + userId + " on poll " + pollId);
            sendMessageToUser("You are not authorized to close this poll.");
            return;
          }

          closePoll(pollId, clientConnectionIndex);
          Logger.log("Poll close request handled for ID: " + pollId + " by user " + userId);
          break;

        case MessageType.SendResultRequest:
          pollId = messageObject.getParam("pollId", int.class);
          PollResult pollResult = retrievePollResult(pollId);
          Logger.log("Poll Results handled for: " +pollId);
          sendPollResultsToUser(pollResult, clientConnectionIndex);
          break;

        case MessageType.CreatePoll:
          Poll poll = messageObject.getParam("poll", Poll.class);
          profile = messageObject.getParam("profile",Profile.class);
          storePoll(poll, profile, clientConnectionIndex);
          Logger.log("Poll successfully created for: " + poll.getId());
          break;
        case MessageType.SendLoginOrRegister:
          profile = messageObject.getParam("profile", Profile.class);
          int id=getDb().loginOrRegisterAProfile(profile);
          Logger.log("Profile logged or registered with id: " + id);
          profile.setId(id);
          sendUpdatedProfile(profile, clientConnectionIndex);
          break;
        case MessageType.SendChangeUsername:
          try {
            profile = messageObject.getParam("username", Profile.class);
            getDb().changeUsername(profile);
            Logger.log("Username successfully changed for ID: " + profile.getId());

            Message response = new Message(MessageType.SendChangeUsername);
            response.addParam("status", "Username successfully changed");
            sendMessageToUser(JsonUtil.serialize(response));
          } catch (Exception e) {
            Message response = new Message(MessageType.SendChangeUsername);
            response.addParam("status", "Username already used");
            sendMessageToUser(JsonUtil.serialize(response));
          }
          break;

        case MessageType.SendCreateVoteGroupRequest:
          UserGroup userGroup = messageObject.getParam("voteGroup", UserGroup.class);
          clientConnectionIndex = messageObject.getParam("clientConnectionIndex",int.class);
          storeUserGroup(userGroup, clientConnectionIndex);
          break;
        case MessageType.SendUserGroupsRequest:
          clientConnectionIndex = messageObject.getParam("clientConnectionIndex",int.class);
          List<UserGroup> userGroups=getGroupsCreatedByUser(clientConnectionIndex);
          sendUserGroups(userGroups, clientConnectionIndex);
          break;

        case MessageType.SendPollAccess:
          pollId = messageObject.getParam("pollId", int.class);

          Type userSetType = new TypeToken<Set<Profile>>() {}.getType();
          Set<Profile> users = messageObject.getParam("users", userSetType);

          Type groupSetType = new TypeToken<Set<UserGroup>>() {}.getType();
          Set<UserGroup> groups = messageObject.getParam("groups", groupSetType);
          userId=messageObject.getParam("userId",int.class);

          if (users!=null)
          {
            grantPollAccessToUsers(pollId, users, userId);
          }
          if(groups!=null)
          {
            grantPollAccessToGroups(pollId, groups, userId);
          }


          break;
        case MessageType.LookupUser:
          profile = messageObject.getParam("profile", Profile.class);
          Profile fullProfile = getDb().getProfileByUsername(profile.getUsername());

          if (fullProfile == null) {
            fullProfile = new Profile(profile.getUsername());
            fullProfile.setId(-1); // signal "not found"
          }

          sendLookupUserResults(fullProfile, clientConnectionIndex);
          break;
        case MessageType.LookupGroup:
          String groupName = messageObject.getParam("groupName", String.class);
          UserGroup group = getDb().getGroupByUsername(groupName);

          if (group == null) {
            group = new UserGroup(groupName);
            group.setId(-1);
          }

          // Send back the full group (or the dummy with id -1)
          sendLookupGroupResults(group, clientConnectionIndex);
          break;
        default:
          Logger.log("Received an unknown message type: " + messageObject.getType());
          break;
      }
    } catch (Exception e) {
      Logger.log("Error in ServerProxy: " + e.getMessage());
      throw new RuntimeException(e);
    }
  }
}
