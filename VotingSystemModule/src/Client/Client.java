package Client;

import Common.*;
import Utils.JsonUtil;
import Utils.Logger;

import java.io.IOException;
import java.net.Socket;

public class Client implements MessageListener
{
  private ClientConnection clientConnection;
  private Model model;
  private String host;
  private int port;

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
      clientConnection.registerMessageListener(this);
      WindowManager.getInstance().setModel(new Model(this));
      this.model = WindowManager.getInstance().getModel();
      var thread = new Thread(clientConnection);
      thread.setDaemon(true);
      thread.start();
    }
    catch (IOException e)
    {
      Logger.log("Could not establish a connection with the server. Shutting down...");
      Logger.log("Exception",e.getMessage());
    }
  }

  public boolean send(Message message)
  {
    try
    {
      clientConnection.send(JsonUtil.serialize(message));
      return true;
    }
    catch (IOException e)
    {
      Logger.log("Client - Exception",e.getMessage());
      return false;
    }
  }

  @Override public void receiveMessage(Message message)
  {
    // TODO: handle messages
    Logger.log(String.format("handling message of type %s", message.getType()));
    switch (message.getType())
    {
      case SendProfileBack -> model.setProfile(message.getParam("UpdatedProfile",
          Profile.class));
      case SendResultResults -> model.getResult(message.getParam("pollResult",
          PollResult.class));
      case SendPoll -> model.setPoll(message.getParam("poll",
         Poll.class));
      case SendLookupUserResult -> model.handleUserLookupResult(message.getParam("profile", Profile.class));
      case SendLookupGroupResult -> model.handleUserGroupLookupResult(message.getParam("userGroup", UserGroup.class));

      default -> Logger.log(String.format("Could not handle message of type %s", message.getType()));
    }
  }
}