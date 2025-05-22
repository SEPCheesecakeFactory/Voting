import Client.ClientConnection;
import Common.Message;
import Common.MessageListener;
import Utils.Logger;

import java.io.IOException;
import java.net.Socket;

public class StressTest  implements MessageListener
{
  public ClientConnection clientConnection;
  private static final String host = "localhost";
  private static final int port = 2910;
  public int myListenerIndex;
  private static final int FLOOD_COUNT = 100;
  private static final int FLOODS_COUNT = 10;

  public static void main(String[] args)
      throws IOException, InterruptedException
  {
    StressTest test = new StressTest();
    Socket socket = new Socket(host, port);
    test.clientConnection = new ClientConnection(socket);
    System.out.println(socket.getLocalPort());
    test.myListenerIndex = test.clientConnection.registerMessageListener(test);

    int id = 12;

    var thread = new Thread(test.clientConnection);
    thread.start();
    Logger.log("Test","Logging in...");
    Thread.sleep(500);
    test.clientConnection.send("{\"type\":\"SendLoginOrRegister\",\"params\":{\"profile\":{\"username\":\"stress_test\",\"id\":11}}}");
    Logger.log("Test","Flooding begins...");
    Thread.sleep(1000);
    for(int i = 0; i < FLOODS_COUNT; i++)
    {
      (new Thread(()->{
        for (int j = 0; j < FLOOD_COUNT; j++)
        {
          try
          {
            test.clientConnection.send("{\"type\":\"SendChangeUsername\",\"params\":{\"username\":{\"username\":\"v\",\"id\":11,\"clientConnectionIndex\":11}}}");
            test.clientConnection.send("{\"type\":\"SendChangeUsername\",\"params\":{\"username\":{\"username\":\"e\",\"id\":11,\"clientConnectionIndex\":11}}}");
          }
          catch (IOException e)
          {
            throw new RuntimeException(e);
          }
          Logger.log("Flood", "Flooding cycle "+j);
        }
      })).start();
    }
  }

  @Override public void receiveMessage(Message message)
  {

  }
}
