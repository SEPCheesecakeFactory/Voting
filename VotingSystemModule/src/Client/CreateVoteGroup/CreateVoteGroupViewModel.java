package Client.CreateVoteGroup;

import Client.Model;
import Common.Profile;
import Common.UserGroup;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

public class CreateVoteGroupViewModel implements PropertyChangeListener
{
  private Model model;
  private UserGroup currentGroup;

  public CreateVoteGroupViewModel(Model model) {
    this.model = model;
    this.model.addPropertyChangeListener("LookupUserResults", this);
  }

  public void createGroup(String groupName) {
    currentGroup = new UserGroup(groupName);
  }

  public boolean addMemberToGroup(Profile profile) {
    return currentGroup != null && currentGroup.addMember(profile);
  }

  public boolean removeMemberFromGroup(Profile profile) {
    return currentGroup != null && currentGroup.removeMember(profile);
  }

  public UserGroup getCurrentGroup() {
    return currentGroup;
  }

  public void sendGroupToServer() {
    if (currentGroup != null) {
      model.sendVoteGroup(currentGroup);
    }
  }

  public void requestUserLookup(String username) {
    model.requestUserLookup(username);
  }

  // This method will be called by the Model when server sends the Profile back
  public void handleLookupResult(Profile profile) {
    if (profile.getId() == -1) {
      System.out.println("User not found.");
    } else if (addMemberToGroup(profile)) {
      System.out.println("User " + profile.getUsername() + " added with ID " + profile.getId());
    } else {
      System.out.println("User " + profile.getUsername() + " is already in the group.");
    }
  }

  @Override public void propertyChange(PropertyChangeEvent evt)
  {
    handleLookupResult((Profile)evt.getNewValue());
  }
}
