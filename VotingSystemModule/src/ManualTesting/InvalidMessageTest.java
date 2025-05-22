package ManualTesting;

import Client.ClientConnection;
import Common.Message;
import Common.MessageListener;
import Utils.JsonUtil;
import Utils.Logger;

import java.io.IOException;
import java.net.Socket;

public class InvalidMessageTest  implements MessageListener
{
  public ClientConnection clientConnection;
  private static final String host = "localhost";
  private static final int port = 2910;
  public int myListenerIndex;

  public static void main(String[] args)
      throws IOException, InterruptedException
  {
    InvalidMessageTest test = new InvalidMessageTest();
    Socket socket = new Socket(host, port);
    test.clientConnection = new ClientConnection(socket);
    System.out.println(socket.getLocalPort());
    test.myListenerIndex = test.clientConnection.registerMessageListener(test);

    var thread = new Thread(test.clientConnection);
    thread.start();
    Thread.sleep(500);
    Logger.log("Sending Invalid JSON string");
    test.clientConnection.send("invalid json string");
    Logger.log("Sent Invalid JSON string");
  }

  @Override public void receiveMessage(Message message)
  {
    Logger.log("Received: " + JsonUtil.serialize(message));
  }
}
