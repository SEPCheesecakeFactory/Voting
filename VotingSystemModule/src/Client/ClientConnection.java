package Client;
//Socket 2 - Michael
import Common.Poll;
import Common.PollResult;
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

  public void setModel(Model model)
  {
    this.model = model;
  }

  public ClientConnection(Socket socket) throws IOException
  {
    outToServer = new ObjectOutputStream(socket.getOutputStream());
    inFromServer = new ObjectInputStream(socket.getInputStream());
  }

  @Override public void run()
  {
    try
    {
      Profile profile = (Profile) inFromServer.readObject();
      model.setProfile(profile);

      String message = (String) inFromServer.readObject();
      model.setMessage(message);
      while (true)
      {

        Poll poll = (Poll) inFromServer.readObject();
        //logic of displaying the poll info and choosing options should be here
        // TODO: Should be changed for protocol 2.0, choices can't be gotten right after receiving the poll
        // TODO: Should be actually changed for protocol 3.0
        if (model != null)
        {
          model.setPoll(poll); // push to model
        }
        //        Logger.log("Message received: " + message);
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
    outToServer.reset();
    outToServer.writeObject(vote);
  }

  public void sendLoginOrRegister(Profile profile) throws IOException
  {
    outToServer.reset();
    outToServer.writeObject(profile);
  }

  public void sendChangeUsername(Profile profile) throws IOException
  {
    outToServer.reset();
    outToServer.writeObject(profile);
  }

  /*public void sendFinalResults(Poll poll) throws IOException
  {
    outToServer.reset();
    outToServer.writeObject(poll);
  }*/
  public void sendClosePollRequest(int pollId) throws IOException
  {
    String message = "close_poll:" + pollId;
    outToServer.reset();
    outToServer.writeObject(message);
  }

  public void getPollResult() throws IOException, ClassNotFoundException
  {
    inFromServer.reset();
    PollResult pollResult = (PollResult) inFromServer.readObject();
    model.getResult(pollResult);
  }

  public void sendFinalResults(Poll poll) throws IOException
  {
    outToServer.reset();
    outToServer.writeObject(poll);
  }

  public void sendCreatePoll(Poll poll) throws IOException
  {
    outToServer.reset();
    outToServer.writeObject(poll);
  }

  public void sendPollResultRequest(int pollID) throws IOException
  {
    String request = "result_request:" + pollID;
    outToServer.reset();
    outToServer.writeObject(request);
  }
}