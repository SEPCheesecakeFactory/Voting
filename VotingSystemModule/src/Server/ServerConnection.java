package Server;
//Sockets 2 michael
import Common.*;
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
    Poll poll = null; // TODO: replace with real poll
    try
    {
      DummyDataMaker dummyDataMaker = new DummyDataMaker();
      poll = dummyDataMaker.getDummyPoll(0);
    }
    catch (SQLException e)
    {
      throw new RuntimeException(e);
    }
    try{
      DatabaseConnectionProxy dbp = new DatabaseConnectionProxy();
      //clientlogin or registerign to the system
      Profile profile =(Profile) inFromClient.readObject();
      int id =dbp.loginOrRegisterAProfile(profile);
      sendProfile(profile, id);

      //client changing username
      profile =(Profile) inFromClient.readObject();
      System.out.println(profile.getUsername());
      dbp.changeUsername(profile);
      send("Username changed");


//      // Protocol - send poll, receive vote
      sendPoll(poll);
//      Vote vote = recieveVote();
//      System.out.println(vote);
      Vote vote = (Vote) inFromClient.readObject();

      dbp.storeVote(vote);
      System.out.println("Vote received " + vote);
    }
    catch (IOException | ClassNotFoundException e)
    {
      throw new RuntimeException(e); // lol, what a catch
    }
    catch (SQLException e)
    {
      throw new RuntimeException(e);
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

  public void sendProfile(Profile profile, int id) throws IOException
  {
    outToClient.reset();
    profile.setId(id);

    outToClient.writeObject(profile);
  }
//  public void sendProfile(Profile profile) throws IOException
//  {
//    outToClient.writeObject(profile);
//  }

  //  public void send(String message) throws IOException
    //  {
    //    outToClient.writeObject(message);
    //  }

    // NOTE: Leaving out comments with unused code is considered a bac practice by many when using version control
    // TODO: resolve (remove/implement) the unused code

    public void sendPoll(Poll poll) throws IOException
    {
      outToClient.reset();
      outToClient.writeObject(poll);
    }
    private Vote recieveVote() throws IOException, ClassNotFoundException
    {
      return (Vote) inFromClient.readObject();
    }
    public void send(String message) throws IOException
    {
      outToClient.reset();
      outToClient.writeObject(message);
    }


}