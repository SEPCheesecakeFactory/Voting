package Client;

import Common.Poll;
import Common.PollResult;
import Common.Profile;
import Common.Vote;
import Utils.Logger;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.IOException;

public class Model implements PropertyChangeSubject, PollResultRequestService
{
  private final PropertyChangeSupport support;
  private final ClientConnection connection;
  private Poll currentPoll;
  private Profile currentProfile;


  public Model(ClientConnection connection)
  {
    this.connection = connection;
    support = new PropertyChangeSupport(this);
  }

  public void setPoll(Poll poll)
  {
    Poll oldPoll = this.currentPoll;
    this.currentPoll = poll;
    support.firePropertyChange("PollUpdated", oldPoll, currentPoll);
  }

  public void setMessage(String message)
  {
    support.firePropertyChange("NewMessage", null, message);
  }

  public void setProfile(Profile profile)
  {
    this.currentProfile = profile;
    support.firePropertyChange("ProfileSet", null, null);

  }

  public Profile getProfile()
  {
    return currentProfile;
  }

  public Poll getPoll()
  {
    return currentPoll;
  }

  public void sendLoginOrRegister(Profile profile)
  {
    try
    {
      connection.sendLoginOrRegister(profile);

    }
    catch (Exception e)
    {
      Logger.log("Failed to login or register: " + e.getMessage());
    }
  }

  public void sendChangeUsername(String username)
  {
    try
    {
      currentProfile.changeUsername(username);

      connection.sendChangeUsername(currentProfile);
    }
    catch (Exception e)
    {
      Logger.log("Failed to change username: " + e.getMessage());
    }
  }

  public void sendVote(int userId, int[] choices)
  {
    try
    {
      Vote vote = new Vote(userId, choices);
      connection.sendVote(vote);
    }
    catch (Exception e)
    {
      Logger.log("Failed to send vote: " + e.getMessage());
    }
  }

  /*public void sendFinalResult(Poll poll)
  {
    try
    {
      connection.sendFinalResults(poll);
    }
    catch (IOException e)
    {
      Logger.log("Failed to send final poll results: " + e.getMessage());
    }
  }*/

  public void sendPollCloseRequest(int pollId) {
    try {
      connection.sendClosePollRequest(pollId);
    } catch (IOException e) {
      Logger.log("Failed to send poll close request: " + e.getMessage());
    }
  }

  @Override public void addPropertyChangeListener(
      PropertyChangeListener listener)
  {
    support.addPropertyChangeListener(listener);
  }

  @Override public void addPropertyChangeListener(String name,
      PropertyChangeListener listener)
  {
    support.addPropertyChangeListener(name, listener);
  }

  @Override public void removePropertyChangeListener(
      PropertyChangeListener listener)
  {
    support.removePropertyChangeListener(listener);
  }

  @Override public void removePropertyChangeListener(String name,
      PropertyChangeListener listener)
  {
    support.removePropertyChangeListener(name, listener);
  }

  @Override public void propertyChange(PropertyChangeEvent evt)
  {

  }

  @Override public void getResult(PollResult pollResult)
  {
    support.firePropertyChange("PollResult",null, pollResult );
  }
  public void sendFinalResult(Poll poll)
  {
    try
    {
      connection.sendFinalResults(poll);
    }
    catch (IOException e)
    {
      Logger.log("Failed to send final poll results: " + e.getMessage());
    }
  }

  @Override public void sendResultRequest(int pollID)
  {

      try
      {
        connection.sendPollResultRequest(pollID);
      }
      catch (IOException e)
      {
        throw new RuntimeException(e);
      }
    }

}
