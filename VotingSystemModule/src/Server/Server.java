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
      DatabaseConnection databaseConnection = new DatabaseConnection();
      DatabaseConnectionProxy databaseConnectionProxy = new DatabaseConnectionProxy(databaseConnection);

      DatabaseConnector databaseConnector = databaseConnectionProxy;

      ServerModel modelService = new ServerModel(databaseConnector, connectionPool);
      ServerProxy serverProxy = new ServerProxy(modelService);

      ServerModelService serverModelService = serverProxy;

      while (true)
      {
        Socket socket = welcomeSocket.accept();
        System.out.println(socket);
        ServerConnection serverConnection = new ServerConnection(socket, serverModelService);
        connectionPool.add(serverConnection);
        Logger.log("Client connected");
        new Thread(serverConnection).start();
      }
    }
    catch (IOException e)
    {
      System.out.println(e.getMessage());
    }
    catch (SQLException e)
    {
      throw new RuntimeException(e);
    }
  }
}