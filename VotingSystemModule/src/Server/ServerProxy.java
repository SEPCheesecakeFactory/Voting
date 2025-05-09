package Server;

import Common.Vote;
import Utils.Logger;

public class ServerProxy implements ServerConnectionInterface
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
}
