package Client;

import Client.CreatePoll.CreatePollService;
import Client.CreateVoteGroup.CreateVoteGroupService;
import Client.PollResult.PollResultRequestService;
import Common.*;
import Utils.Logger;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

public class Model implements PropertyChangeSubject, PollResultRequestService,
    CreateVoteGroupService, CreatePollService
{
  private final PropertyChangeSupport support;
  private Poll currentPoll;
  private Profile currentProfile;
  private Client client;

  public Model(Client client)
  {
    if(client == null)
      throw new IllegalArgumentException("Client cannot be null!");
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
    var message = new Message(MessageType.SendLoginOrRegister);
    message.addParam("profile", profile);
    boolean success = client.send(message);
  }

  public void sendChangeUsername(String username)
  {
    Logger.log("Debugging - sendChangeUsername");
    currentProfile.changeUsername(username);
    var message = new Message(MessageType.SendChangeUsername);
    message.addParam("username", currentProfile);
    boolean success = client.send(message);
  }

  public void sendVote(int userId, int[] choices)
  {
    Logger.log("Debugging - sendVote");
    var message = new Message(MessageType.SendVote);
    message.addParam("userId", userId);
    message.addParam("choices", choices);
    boolean success = client.send(message);
  }

  public void sendPollCloseRequest(int pollId)
  {
    Logger.log("Debugging - sendPollCloseRequest");
    var message = new Message(MessageType.ClosePoll);
    message.addParam("pollId", pollId);
    boolean success = client.send(message);
  }

  @Override public void getResult(PollResult pollResult)
  {
    Logger.log("Debugging - getResult");
    support.firePropertyChange("PollResult", null, pollResult);
    // TODO: how is this supposed to work?
    // NOTE: I think I already know how to but I'll deal with this later - Eduard
    // NOTE: Technically, I guess I don't even have to deal with it, just feels weird I had to do something on each function except this one
  }

  @Override public void sendResultRequest(int pollId)
  {
    Logger.log("Debugging - sendResultRequest");
    var message = new Message(MessageType.SendResultRequest);
    message.addParam("pollId", pollId);
    boolean success = client.send(message);
  }

  @Override public void sendVoteGroup(UserGroup userGroup)
  {
    Logger.log("Debugging - sendVoteGroup");
    var message = new Message(MessageType.SendVoteGroup);
    message.addParam("userGroup", userGroup);
    boolean success = client.send(message);
  }

  @Override public void createPoll(Poll poll)
  {
    Logger.log("Debugging - createPoll");
    var message = new Message(MessageType.CreatePoll);
    message.addParam("poll", poll);
    boolean success = client.send(message);
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
