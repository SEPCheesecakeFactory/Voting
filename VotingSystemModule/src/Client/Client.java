package Client;

import Common.Message;
import Utils.JsonUtil;
import Utils.Logger;

import java.io.IOException;
import java.net.Socket;

public class Client
{
  private ClientConnection clientConnection;
  private Model model;
  private String host;
  private int port;

  public Client()
  {
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
      WindowManager.getInstance().setModel(new Model(this));
      WindowManager.getInstance().showView(ViewType.Menu);
      this.model = WindowManager.getInstance().getModel();
      new Thread(clientConnection).start();
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
      Logger.log(e.getMessage());
      return false;
    }
  }
}