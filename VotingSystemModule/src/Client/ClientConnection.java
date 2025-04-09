package Client;
//Socket 2 - Michael
import Common.Poll;
import Common.Vote;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class ClientConnection implements Runnable
{

  private final ObjectOutputStream outToServer;
  private final ObjectInputStream inFromServer;

  public ClientConnection(Socket socket) throws IOException
  {
    outToServer = new ObjectOutputStream(socket.getOutputStream());
    inFromServer = new ObjectInputStream(socket.getInputStream());
  }

  @Override
  public void run()
  {
    try
    {
      while (true)
      {
         Poll poll = (Poll) inFromServer.readObject();
        //logic of displaying the poll info and choosing options should be here
        // TODO: Should be changed for protocol 2.0, choices can't be gotten right after receiving the poll
        // TODO: Should be actually changed for protocol 3.0

//        System.out.println("Message received: " + message);
      }
    }
    catch (Exception e)
    {
      throw new RuntimeException(e);
    }
  }

  public void sendVote(Vote vote) throws IOException
  {
    outToServer.writeObject(vote);
  }
}