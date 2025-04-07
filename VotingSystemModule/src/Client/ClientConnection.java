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
        int[] choices = {1,3};//dummy not logic
        Vote vote = new Vote(1, choices);//dummy not logic
        sendVote(vote);
//        System.out.println("Message received: " + message);
      }
    }
    catch (IOException | ClassNotFoundException e)
    {
      throw new RuntimeException(e);
    }
  }

  public void sendVote(Vote vote) throws IOException
  {
    outToServer.writeObject(vote);
  }
}