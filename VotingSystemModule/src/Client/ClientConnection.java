package Client;
//Socket 2 - Michael
import Common.*;
import Utils.JsonUtil;
import Utils.Logger;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class ClientConnection implements Runnable
{

  private final ObjectOutputStream outToServer;
  private final ObjectInputStream inFromServer;

  public void setModel(Model model)
  {

  }

  public ClientConnection(Socket socket) throws IOException
  {
    outToServer = new ObjectOutputStream(socket.getOutputStream());
    inFromServer = new ObjectInputStream(socket.getInputStream());
  }

  @Override public void run()
  {
    try
    {
      String message;
      while(true)
      {
        message = (String) inFromServer.readObject();
        Logger.log("Message received: " + message);
        // TODO: handle it
      }
    }
    catch (Exception e)
    {
      throw new RuntimeException(e);
    }
  }

  public void send(String message) throws IOException
  {
    outToServer.writeObject(message);
  }
}