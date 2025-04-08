package Server;
//Sockets 2 michael
import Common.*;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class ServerConnection implements Runnable
{
  private final ObjectInputStream inFromClient;
  private final ObjectOutputStream outToClient;
  private final ConnectionPool connectionPool;

  public ServerConnection(Socket connectionSocket, ConnectionPool connectionPool) throws IOException
  {
    inFromClient = new ObjectInputStream(connectionSocket.getInputStream());
    outToClient = new ObjectOutputStream(connectionSocket.getOutputStream());
    this.connectionPool = connectionPool;
  }

  @Override
  public void run()
  {
    Poll poll = DummyDataMaker.getDummyPoll(0); // TODO: replace with real poll
    try{
      // Protocol - send poll, receive vote
      sendPoll(poll);
      Vote vote = recieveVote();
      System.out.println(vote);
    }
    catch (IOException | ClassNotFoundException e)
    {
      throw new RuntimeException(e); // lol, what a catch
    }

    //    while(true)
    //    {
    //      try
    //      {
    //        String message = (String) inFromClient.readObject();
    //        System.out.println("Received: " + message);
    //        connectionPool.broadcast(message);
    //      }
    //      catch (IOException | ClassNotFoundException e)
    //      {
    //        throw new RuntimeException(e);
    //      }
    //    }
      }

    //  public void send(String message) throws IOException
    //  {
    //    outToClient.writeObject(message);
    //  }

    // NOTE: Leaving out comments with unused code is considered a bac practice by many when using version control
    // TODO: resolve (remove/implement) the unused code

    public void sendPoll(Poll poll) throws IOException
    {
      outToClient.writeObject(poll);
    }
    private Vote recieveVote() throws IOException, ClassNotFoundException
    {
      return (Vote) inFromClient.readObject();
    }
}