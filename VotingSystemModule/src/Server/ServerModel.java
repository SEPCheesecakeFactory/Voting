package Server;

import Common.*;
import Utils.Logger;

import java.io.IOException;
import java.sql.SQLException;

public class ServerModel {
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

  public void storeVote(Vote vote) throws SQLException {
    db.storeVote(vote);
  }

  public void closePoll(int pollId) throws SQLException, IOException {
    db.closePollAndSaveResults(pollId);
    Message message = new Message(MessageType.ClosePoll);
    message.addParam("pollClosed", pollId);
    connectionPool.broadcast(message);
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

  public boolean checkPollAccess(int pollId) throws SQLException {
    if (currentProfile == null) {
      sendMessageToUser("Not logged in.");
      return false;
    }

    if (!db.userHasAccessToPoll(currentProfile.getId(), pollId)) {
      sendMessageToUser("You do not have access to this poll.");
      return false;
    }

    return true;
  }

  public void sendPollResultsToUser(PollResult pollResult){
    try {
      Message message = new Message(MessageType.SendResultResults);
      message.addParam("pollResult", pollResult);
      connectionPool.broadcast(message);
      Logger.log("ServerModel: Results sent");
    } catch (IOException e) {
      Logger.log("Failed to send pollResult to user: " + e.getMessage());
    }
  }
  public void sendUpdatedProfile(Profile profile){
    try {
      Logger.log("ServerModel: Profile send");
      Message message = new Message(MessageType.SendProfileBack);
      message.addParam("UpdatedProfile", profile);
      connectionPool.broadcast(message);
    } catch (IOException e) {
      Logger.log("Failed to send the Updated profile to user: " + e.getMessage());
    }
  }

  public void storePoll(Poll poll)
  {
    try
    {
      poll = db.storePoll(poll);
      Message message = new Message(MessageType.SendCreatedPoll);
      message.addParam("poll", poll);
      connectionPool.broadcast(message);
    }
    catch (IOException e)
    {
      Logger.log("ServerModel: Failed to store poll: " + e.getMessage());
    }

  }
}
