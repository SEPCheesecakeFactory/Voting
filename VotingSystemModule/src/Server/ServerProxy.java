package Server;

import Common.*;
import Utils.JsonUtil;
import Utils.Logger;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.lang.reflect.Type;
import java.sql.SQLException;
import java.util.*;

public class ServerProxy
{
  private final ServerModel model;

  public ServerProxy(ServerModel model) {
    this.model = model;
  }

  public void handle(Object incoming) {
    try {
      if (incoming instanceof Vote vote) {
        model.storeVote(vote);
        Logger.log("Vote handled by ServerProxy: " + vote);
      }
      else if (incoming instanceof String message && message.startsWith("close_poll:")) {
        int pollId = Integer.parseInt(message.split(":")[1]);

        int userId = model.getCurrentProfile().getId(); // assumes you store profile in ServerModel

        if (!model.getDb().isOwner(userId, pollId)) {
          Logger.log("Unauthorized close attempt by user " + userId + " on poll " + pollId);
          model.sendMessageToUser("You are not authorized to close this poll.");
          return;
        }

        model.closePoll(pollId);
        Logger.log("Poll close request handled for ID: " + pollId + " by user " + userId);
      }
      //TODO: Send the Poll Results ot the client *** *** *** *** *** *** *** *** *** *** *** *** *** *** *** *** *** ***
      else if (incoming instanceof String message && message.startsWith("result_request:")){
        int pollId = Integer.parseInt(message.split(":")[1]);
        PollResult pollResult = model.retrievePollResult(pollId);
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
  public ServerModel getModel() {
    return model;
  }

  public void process(String message)
  {
    Message messageObject = JsonUtil.deserialize(message, Message.class);

    try {
      int pollId;
      Profile profile;
      switch (messageObject.getType()) {
        case MessageType.SendPollRequest:
          pollId = messageObject.getParam("pollId", int.class);
          model.sendPoll(pollId);
          break;
        case MessageType.DisplayPollRequest:
          pollId = messageObject.getParam("pollId", int.class);
          model.sendPoll(pollId);
          Logger.log("Poll display request handled for ID: " + pollId);
          break;
        case MessageType.GetAvailablePolls:
          List<Poll> availablePolls = model.getDb().getAllAvailablePolls();  // 🔧 You'll write this method next
          Message sendMsg = new Message(MessageType.SendAvailablePolls);
          sendMsg.addParam("polls", availablePolls);
          model.sendMessageToUser(JsonUtil.serialize(sendMsg));
          Logger.log("Sent available polls to client.");
          break;
        case MessageType.SendVote:
          Vote vote = messageObject.getParam("vote", Vote.class);
          model.storeVote(vote);
          break;
        case MessageType.ClosePoll:
          pollId = messageObject.getParam("pollId", int.class);

          int userId = messageObject.getParam("userId", int.class); // assumes you store profile in ServerModel

          if (!model.getDb().isOwner(userId, pollId)) {
            Logger.log("Unauthorized close attempt by user " + userId + " on poll " + pollId);
            model.sendMessageToUser("You are not authorized to close this poll.");
            return;
          }

          try
          {
            model.closePoll(pollId);
          }
          catch (IOException e)
          {
            throw new RuntimeException(e);
          }
          Logger.log("Poll close request handled for ID: " + pollId + " by user " + userId);
          break;

        case MessageType.SendResultRequest:
          pollId = messageObject.getParam("pollId", int.class);
          PollResult pollResult = model.retrievePollResult(pollId);
          Logger.log("Poll Results handled for: " +pollId);
          model.sendPollResultsToUser(pollResult);
          break;

        case MessageType.CreatePoll:
          Poll poll = messageObject.getParam("poll", Poll.class);
          profile = messageObject.getParam("profile",Profile.class);
          model.storePoll(poll, profile);
          Logger.log("Poll successfully created for: " + poll.getId());
          break;
        case MessageType.SendLoginOrRegister:
          profile = messageObject.getParam("profile", Profile.class);
          int id=model.getDb().loginOrRegisterAProfile(profile);
          Logger.log("Profile logged or registered with id: " +id);
          profile.setId(id);
          model.sendUpdatedProfile(profile);
          break;
        case MessageType.SendChangeUsername:
          profile = messageObject.getParam("username", Profile.class);
          model.getDb().changeUsername(profile);
          Logger.log("Username changed for the profile with id: " +profile.getId());
          // model.sendMessageToUser("Username changed");
          Message response = new Message(MessageType.SendChangeUsername);
          response.addParam("status", "Username successfully changed");
          model.sendMessageToUser(JsonUtil.serialize(response));

          break;
        case MessageType.SendCreateVoteGroupRequest:
          UserGroup userGroup = messageObject.getParam("voteGroup", UserGroup.class);
          userId = messageObject.getParam("userId",int.class);
          model.storeUserGroup(userGroup, userId);
          break;
        case MessageType.SendUserGroupsRequest:
          userId = messageObject.getParam("userId",int.class);
          List<UserGroup> userGroups=model.getGroupsCreatedByUser(userId);





          model.sendUserGroups(userGroups);
          break;

        case MessageType.SendPollAccess:
          pollId = messageObject.getParam("pollId", int.class);

          Type userSetType = new TypeToken<Set<Profile>>() {}.getType();
          Set<Profile> users = messageObject.getParam("users", userSetType);

          Type groupSetType = new TypeToken<Set<UserGroup>>() {}.getType();
          Set<UserGroup> groups = messageObject.getParam("groups", groupSetType);

          if (users!=null)
          {
            model.grantPollAccessToUsers(pollId, users);
          }
          if(groups!=null)
          {
            model.grantPollAccessToGroups(pollId, groups);
          }


          break;
        case MessageType.LookupUser:
          profile = messageObject.getParam("profile", Profile.class);
          Profile fullProfile = model.getDb().getProfileByUsername(profile.getUsername());

          if (fullProfile == null) {
            fullProfile = new Profile(profile.getUsername());
            fullProfile.setId(-1); // signal "not found"
          }

          model.sendLookupUserResults(fullProfile);
          break;
        case MessageType.LookupGroup:
          String groupName = messageObject.getParam("groupName", String.class);
          UserGroup group = model.getDb().getGroupByUsername(groupName);

          if (group == null) {
            group = new UserGroup(groupName);
            group.setId(-1);
          }

          // Send back the full group (or the dummy with id -1)
          model.sendLookupGroupResults(group);
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
