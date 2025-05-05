package Client;
//Socket 2 - Michael
import Common.Poll;
import Common.Profile;
import Common.Vote;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class ClientConnection implements Runnable
{

  private final ObjectOutputStream outToServer;
  private final ObjectInputStream inFromServer;
  private Model model;

  public void setModel(Model model) {
    this.model = model;
  }

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
      Profile profile = (Profile) inFromServer.readObject();
      int userId = retrieveID(profile);
      model.getProfile().setId(userId);
      while (true)
      {

         Poll poll = (Poll) inFromServer.readObject();
        //logic of displaying the poll info and choosing options should be here
        // TODO: Should be changed for protocol 2.0, choices can't be gotten right after receiving the poll
        // TODO: Should be actually changed for protocol 3.0
        if (model != null) {
          model.setPoll(poll); // push to model
        }
//        System.out.println("Message received: " + message);
      }
    }
    catch (Exception e)
    {
      throw new RuntimeException(e);
    }
  }

  private int retrieveID(Profile profile)
  {
    return profile.getId();
  }

  public void sendVote(Vote vote) throws IOException
  {
    outToServer.writeObject(vote);
  }
  public void sendLoginOrRegister(Profile profile) throws IOException
  {
    outToServer.writeObject(profile);
  }
}