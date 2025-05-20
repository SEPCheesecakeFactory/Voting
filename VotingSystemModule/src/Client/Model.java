package Client;

import Client.AddUsers.AddUsersService;
import Client.CreatePoll.CreatePollService;
import Client.CreateVoteGroup.CreateVoteGroupService;
import Client.DisplayPoll.DisplayPollService;
import Client.PollResult.PollResultRequestService;
import Common.*;
import Utils.Logger;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class Model implements PropertyChangeSubject, PollResultRequestService,
    CreateVoteGroupService, CreatePollService, DisplayPollService,
    AddUsersService
{
  private final PropertyChangeSupport support;
  private Poll currentPoll;
  private Profile currentProfile;
  private Client client;
  private List<Poll> availablePolls = new ArrayList<>();

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
    if(!success) WindowManager.getInstance().showErrorPopup("Could not login or register!");
  }

  public void sendChangeUsername(String username) {
    currentProfile.changeUsername(username);
    var message = new Message(MessageType.SendChangeUsername);
    message.addParam("username", currentProfile);
    boolean success = client.send(message);
    if(!success) WindowManager.getInstance().showErrorPopup("Could not change the username!");
  }

  public void fireUsernameChanged() {
    support.firePropertyChange("UsernameChanged", null, currentProfile.getUsername());
  }

  public void fireUsernameChangeFailed(String reason) {
    support.firePropertyChange("UsernameChangeFailed", null, reason);
  }

  public void sendVote(int userId, int[] choices)
  {
    Logger.log("Debugging - sendVote");
    var message = new Message(MessageType.SendVote);
    Vote vote = new Vote(userId, choices);
    message.addParam("vote", vote);
    boolean success = client.send(message);
    if(!success) WindowManager.getInstance().showErrorPopup("Could not send the vote!");
  }

  public void sendPollCloseRequest(int pollId)
  {
    Logger.log("Debugging - sendPollCloseRequest");
    var message = new Message(MessageType.ClosePoll);
    message.addParam("pollId", pollId);
    boolean success = client.send(message);
    if(!success) WindowManager.getInstance().showErrorPopup("Could not close the poll!");
  }

  public void sendDisplayPollRequest(int pollId) {
    Logger.log("Debugging - sendDisplayPollRequest");
    Message message = new Message(MessageType.DisplayPollRequest);
    message.addParam("pollId", pollId);
    boolean success = client.send(message);
    if (!success) {
      Logger.log("Failed to send DisplayPollRequest for poll ID: " + pollId);
      if(!success) WindowManager.getInstance().showErrorPopup("Can't display the poll currently!");
    }
  }

  public void requestAvailablePolls() {
    Logger.log("Debugging - requestAvailablePolls");
    Message message = new Message(MessageType.GetAvailablePolls);
    boolean success;
    if(getProfile() != null)
    {
      message.addParam("userId",getProfile().getId());
      success = client.send(message);
    }
    else
      success = false;
    if(!success) WindowManager.getInstance().showErrorPopup("Request for available polls failed!");
  }

  public void handleAvailablePolls(List<Poll> polls) {
    this.availablePolls = polls;
    support.firePropertyChange("AvailablePolls", null, polls); // notify listeners
  }

  public List<Poll> getAvailablePolls() {
    return availablePolls;
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
    if(!success) WindowManager.getInstance().showErrorPopup("Request for the poll results failed!");
  }

  @Override public void sendVoteGroup(UserGroup userGroup)
  {
    Logger.log("Debugging - sendVoteGroup");
    var message = new Message(MessageType.SendCreateVoteGroupRequest);
    message.addParam("voteGroup", userGroup);
    message.addParam("userId", getProfile().getId());
    boolean success = client.send(message);
    if(!success) WindowManager.getInstance().showErrorPopup("Sending a vote group failed!");
  }

  @Override public void requestUserLookup(String username) {
    Logger.log("Debugging - requestUserLookup");
    Message message = new Message(MessageType.LookupUser);
    Profile temp = new Profile(username);
    message.addParam("profile", temp);
    boolean success=client.send(message);
    if(!success) WindowManager.getInstance().showErrorPopup("Request for user lookup failed!");
  }

  @Override public void handleUserLookupResult(Profile profile)
  {
   Logger.log("Debugging - handleUserLookupResult");
    support.firePropertyChange("LookupUserResults", null, profile);
  }

  @Override public void requestUserGroups()
  {
    Logger.log("Debugging - requestUserGroups");
    Message message = new Message(MessageType.SendUserGroupsRequest);
    boolean success;
    if(getProfile() != null)
    {
      message.addParam("userId",getProfile().getId());
      success = client.send(message);
    }
    else
      success = false;
    if(!success) WindowManager.getInstance().showErrorPopup("Request for user groups failed!");
  }

  @Override public void receiveUserGroups(List<UserGroup> groups)
  {
    Logger.log("Debugging - receiveUserGroups");
    support.firePropertyChange("receiveUserGroups",null,groups);
  }

  @Override public void handleUserGroupLookupResult(UserGroup userGroup)
  {
    Logger.log("Debugging - handleUserGroupLookupResult");
    support.firePropertyChange("LookupGroupResults", null, userGroup);
  }

  @Override public void sendPollAccess(int pollId, Set<Profile> users,
      Set<UserGroup> groups)
  {
    Logger.log("Debugging - sendPollAccess");
    var message = new Message(MessageType.SendPollAccess);
    message.addParam("pollId",pollId);
    message.addParam("users",users);
    message.addParam("groups",groups);
    message.addParam("userId",getProfile().getId());
    boolean success = client.send(message);
    if(!success) WindowManager.getInstance().showErrorPopup("Sending poll access failed!");
  }

  @Override public void requestGroupLookup(String groupName)
  {
    Logger.log("Debugging - requestGroupLookup");
    var message = new Message(MessageType.LookupGroup);
    message.addParam("groupName", groupName);
    boolean success = client.send(message);
    if(!success) WindowManager.getInstance().showErrorPopup("Request for group lookup failed!");
  }



  @Override public void createPoll(Poll poll)
  {
    Logger.log("Debugging - createPoll");
    var message = new Message(MessageType.CreatePoll);
    message.addParam("poll", poll);
    message.addParam("profile",currentProfile);
    boolean success = client.send(message);
    if(!success) WindowManager.getInstance().showErrorPopup("Creating a poll failed!");
  }
  @Override public void sendPollRequest(int pollID)
  {
    Logger.log("Debugging - sendPollRequest");
    var message = new Message(MessageType.SendPollRequest);
    message.addParam("pollId",pollID);
    boolean success = client.send(message);
    if(!success) WindowManager.getInstance().showErrorPopup("Request for poll failed!");
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
