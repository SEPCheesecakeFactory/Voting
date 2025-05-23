package Server;
//Sockets 2 michael
import Common.*;
import Utils.Logger;
import jdk.jshell.spi.ExecutionControl;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.sql.SQLException;

public class ServerConnection implements Runnable
{
  private final ObjectInputStream inFromClient;
  private final ObjectOutputStream outToClient;
  private final ServerModelService serverModelService;

  public ServerConnection(Socket connectionSocket, ServerModelService serverModelService) throws IOException {
    outToClient = new ObjectOutputStream(connectionSocket.getOutputStream());
    outToClient.flush();

    inFromClient = new ObjectInputStream(connectionSocket.getInputStream());

    this.serverModelService = serverModelService;
    serverModelService.setConnection(this);
  }

  @Override public void run()
  {
    try
    {
      while (true)
      {
        Logger.log("Ready to receive an object...");
        Object incoming = inFromClient.readObject();
        String message = (String) incoming;
        Logger.log("Received a message: " + message);
        serverModelService.process(message, this);
      }
    }
    catch (IOException | ClassNotFoundException e)
    {
      Logger.log("Server - Exception",
          "Server connection error: " + e.getMessage());
    }
  }

  public void send(String message) throws IOException
  {
    outToClient.reset();
    outToClient.writeObject(message);
  }
}