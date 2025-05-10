package Server;

import Common.Message;
import Common.MessageType;
import Common.PollResult;
import Common.Vote;
import Utils.JsonUtil;
import Utils.Logger;

import java.io.IOException;
import java.sql.SQLException;

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
      switch (messageObject.getType()) {
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
        case MessageType.RequestPollResult:
          pollId = messageObject.getParam("pollId", int.class);
          PollResult pollResult = model.retrievePollResult(pollId);
          Logger.log("Poll Results handled for: " +pollId);
          //TODO: Send pollResult to Client ***********************************
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
