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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ClientConnection implements Runnable
{

  private final ObjectOutputStream outToServer;
  private final ObjectInputStream inFromServer;
  private final List<MessageListener> messageListeners;
  private final Map<Integer,MessageListener> identifiedMessageListeners;
  private int userId = 0;

  public ClientConnection(Socket socket) throws IOException
  {
    outToServer = new ObjectOutputStream(socket.getOutputStream());
    inFromServer = new ObjectInputStream(socket.getInputStream());
    messageListeners = new ArrayList<>();
    identifiedMessageListeners = new HashMap<>();
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
    outToServer.flush();
  }

  /**
   * Registers a listener and returns its index.
   */
  public int registerMessageListener(MessageListener listener) {
    messageListeners.add(listener);
    return messageListeners.size() - 1;
  }

  public void identifyClientConnection(int newUserId, MessageListener client){
    setUserId(newUserId);
    identifiedMessageListeners.put(userId, client);
  }

  public int getUserId(){
    return userId;
  }
  public void setUserId(int newUserId){
    this.userId = newUserId;
  }

  public int getIndex(){
    return messageListeners.indexOf(this);
  }
}