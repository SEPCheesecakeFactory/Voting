package Client;

import Common.*;
import Utils.Logger;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.IOException;

public class Model implements PropertyChangeSubject, PollResultRequestService,
    CreateVoteGroupService, CreatePollService
{
  private final PropertyChangeSupport support;
  private Poll currentPoll;
  private Profile currentProfile;
  private Client client;

  public Model(Client client)
  {
    this.client = client;
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

  public Client getClient()
  {
    return client;
  }

  public void sendLoginOrRegister(Profile profile)
  {
    Logger.log("Debugging - sendLoginOrRegister");
    try
    {
      // connection.sendLoginOrRegister(profile);
    }
    catch (Exception e)
    {
      Logger.log("Failed to login or register: " + e.getMessage());
    }
  }

  public void sendChangeUsername(String username)
  {
    Logger.log("Debugging - sendChangeUsername");
    try
    {
      currentProfile.changeUsername(username);

      // connection.sendChangeUsername(currentProfile);
    }
    catch (Exception e)
    {
      Logger.log("Failed to change username: " + e.getMessage());
    }
  }

  public void sendVote(int userId, int[] choices)
  {
    Logger.log("Debugging - sendVote");
    try
    {
      Vote vote = new Vote(userId, choices);
      // connection.sendVote(vote);
    }
    catch (Exception e)
    {
      Logger.log("Failed to send vote: " + e.getMessage());
    }
  }

  public void sendPollCloseRequest(int pollId)
  {
    Logger.log("Debugging - addPropertyChangeListener");
    /*try
    {
      connection.sendClosePollRequest(pollId);
    }
    catch (IOException e)
    {
      Logger.log("Failed to send poll close request: " + e.getMessage());
    }*/
  }

  @Override public void getResult(PollResult pollResult)
  {
    Logger.log("Debugging - getResult");
    support.firePropertyChange("PollResult", null, pollResult);
  }

  @Override public void sendResultRequest(int pollID)
  {
    Logger.log("Debugging - sendResultRequest");
    /*try
    {
      connection.sendPollResultRequest(pollID);
    }
    catch (IOException e)
    {
      Logger.log("Failed to send poll results request: " + e.getMessage());
    }*/
  }

  @Override public void sendVoteGroup(UserGroup userGroup)
  {
    Logger.log("Debugging - sendVoteGroup");
    /*try
    {
      connection.sendVoteGroup(userGroup);
    }
    catch (IOException e)
    {
      throw new RuntimeException(e);
    }*/
  }

  @Override public void createPoll(Poll poll)
  {
    Logger.log("Debugging - createPoll");
    /*try
    {
      connection.sendCreatePoll(poll);
    }
    catch (IOException e)
    {
      Logger.log("Failed to create the poll: " + e.getMessage());
    }*/
  }

  @Override public void addPropertyChangeListener(
      PropertyChangeListener listener)
  {
    Logger.log("Debugging - addPropertyChangeListener");
    support.addPropertyChangeListener(listener);
  }

  @Override public void addPropertyChangeListener(String name,
      PropertyChangeListener listener)
  {
    Logger.log("Debugging - addPropertyChangeListener");
    support.addPropertyChangeListener(name, listener);
  }

  @Override public void removePropertyChangeListener(
      PropertyChangeListener listener)
  {
    Logger.log("Debugging - removePropertyChangeListener");
    support.removePropertyChangeListener(listener);
  }

  @Override public void removePropertyChangeListener(String name,
      PropertyChangeListener listener)
  {
    Logger.log("Debugging - removePropertyChangeListener");
    support.removePropertyChangeListener(name, listener);
  }

  @Override public void propertyChange(PropertyChangeEvent evt)
  {
    Logger.log("Debugging - propertyChange");
  }
}
