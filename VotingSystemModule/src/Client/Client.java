package Client;

import Common.*;
import Utils.JsonUtil;
import Utils.Logger;
import com.google.gson.reflect.TypeToken;
import javafx.application.Platform;

import java.io.IOException;
import java.lang.reflect.Type;
import java.net.Socket;
import java.util.List;

public class Client implements MessageListener
{
  private ClientConnection clientConnection;
  private Model model;
  private String host;
  private int port;
  private int myListenerIndex;

  public Client()
  {
    Logger.log("Warning", "Running the client on default host and port...");
    host = "localhost";
    port = 2910;
  }

  public Client(String host, int port)
  {
    this.host = host;
    this.port = port;
  }

  public void run()
  {
    try
    {
      Socket socket = new Socket(host, port);
      clientConnection = new ClientConnection(socket);
      myListenerIndex = clientConnection.registerMessageListener(this);
      WindowManager.getInstance().setModel(new Model(this));
      this.model = WindowManager.getInstance().getModel();
      var thread = new Thread(clientConnection);
      thread.setDaemon(true);
      thread.start();
    }
    catch (IOException e)
    {
      Logger.log(
          "Could not establish a connection with the server. Shutting down...");
      Logger.log("Exception", e.getMessage());
    }
  }

  public boolean send(Message message)
  {
    try
    {
      message.addParam("clientConnectionIndex", myListenerIndex);
      clientConnection.send(JsonUtil.serialize(message));
      return true;
    }
    catch (IOException e)
    {
      Logger.log("Client - Exception", e.getMessage());
      return false;
    }
  }

  @Override public void receiveMessage(Message message)
  {
    Logger.log(String.format("handling message of type %s", message.getType()));

    switch (message.getType())
    {
      case SendProfileBack ->
      {
        Profile updatedProfile = message.getParam("UpdatedProfile", Profile.class);
        model.setProfile(updatedProfile);
        clientConnection.identifyClientConnection(updatedProfile.getId(), this);
      }

      case SendAvailablePolls ->
      {
        Type listType = new TypeToken<List<Poll>>()
        {
        }.getType();
        List<Poll> polls = message.getParam("polls", listType);
        model.handleAvailablePolls(polls);
      }
      case SendResultResults ->
          model.getResult(message.getParam("pollResult", PollResult.class));

      case SendChangeUsername -> {
        String status = message.getParam("status", String.class);
        Platform.runLater(() -> model.setMessage(status));

        if ("Username successfully changed".equals(status)) {
          model.fireUsernameChanged();
        } else {
          model.fireUsernameChangeFailed(status);
        }
      }


      case SendPoll ->
      {
        Poll poll = message.getParam("poll", Poll.class);
        model.setPoll(poll);
        System.out.println("Switching to DisplayPoll view...");
        model.setPoll(poll);
      }

      case SendLookupUserResult -> model.handleUserLookupResult(
          message.getParam("profile", Profile.class));

      case SendLookupGroupResult -> model.handleUserGroupLookupResult(
          message.getParam("userGroup", UserGroup.class));
      case SendUserGroups ->
      {
        Type userGroupListType = new TypeToken<List<UserGroup>>()
        {
        }.getType();
        model.receiveUserGroups(
            message.getParam("userGroups", userGroupListType));
      }

      default -> Logger.log(String.format("Could not handle message of type %s",
          message.getType()));
    }
  }
}