package Server;
//Sockets 2 class
import Utils.Logger;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.SQLException;

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
        DatabaseConnectionProxy databaseConnectionProxy = new DatabaseConnectionProxy();
//        MockDatabaseConnection databaseConnectionProxy = new MockDatabaseConnection();
        ServerConnection serverConnection = new ServerConnection(socket, connectionPool, databaseConnectionProxy);
        connectionPool.add(serverConnection);
        Logger.log("Client connected");
        new Thread(serverConnection).start();
      }
    }
    catch (IOException e)
    {
      throw new RuntimeException(e);
    }
    catch (SQLException e)
    {
      throw new RuntimeException(e);
    }
  }
}