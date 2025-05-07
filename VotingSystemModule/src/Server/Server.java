package Server;
//Sockets 2 class
import Utils.Logger;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Server
{
  public static void main(String[] args)
  {
    Logger.log("Starting the server...");
    try
    {
      ServerSocket welcomeSocket = new ServerSocket(2910);
      ConnectionPool connectionPool = new ConnectionPool();

      while (true)
      {
        Socket socket = welcomeSocket.accept();
        ServerConnection serverConnection = new ServerConnection(socket, connectionPool);
        connectionPool.add(serverConnection);
        Logger.log("Client connected");
        new Thread(serverConnection).start();
      }
    }
    catch (IOException e)
    {
      throw new RuntimeException(e);
    }
  }
}