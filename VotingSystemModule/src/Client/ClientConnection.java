package Client;
//Socket 2 - Michael
import Common.*;
import Utils.JsonUtil;
import Utils.Logger;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class ClientConnection implements Runnable
{

  private final ObjectOutputStream outToServer;
  private final ObjectInputStream inFromServer;
  private final List<MessageListener> messageListeners;

  public ClientConnection(Socket socket) throws IOException
  {
    outToServer = new ObjectOutputStream(socket.getOutputStream());
    inFromServer = new ObjectInputStream(socket.getInputStream());
    messageListeners = new ArrayList<>();
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
        Message messageObject = JsonUtil.deserialize(message, Message.class);
        if (messageObject.getType() == MessageType.SendProfileBack){

        }
        sendMessageToListeners(messageObject);
      }
    }
    catch (Exception e)
    {
      throw new RuntimeException(e);
    }
  }

  private void sendMessageToListeners(Message message)
  {
    for(var listener : messageListeners)
      listener.receiveMessage(message);
  }

  public void send(String message) throws IOException
  {
    outToServer.writeObject(message);
  }

  public void registerMessageListener(MessageListener listener)
  {
    messageListeners.add(listener);
  }
}