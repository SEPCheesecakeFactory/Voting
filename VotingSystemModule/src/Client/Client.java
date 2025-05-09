package Client;
//Socket 2 - Michael
import java.io.IOException;
import java.net.Socket;
import java.util.Scanner;

public class Client
{
  private ClientConnection clientConnection;
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

  public static void main(String[] args)
  {
    Client client = new Client();
    client.run();
  }

  public void run()
  {
    try
    {
      Socket socket = new Socket(host, port);
      clientConnection = new ClientConnection(socket);
      WindowManager.getInstance().setClientConnection(clientConnection);
      WindowManager.getInstance().showView(ViewType.Menu);
      clientConnection.setModel(WindowManager.getInstance().getModel());
      new Thread(clientConnection).start();
    }
    catch (IOException e)
    {
      throw new RuntimeException(e);
    }
  }
}