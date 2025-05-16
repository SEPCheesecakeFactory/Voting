package Client.CreateVoteGroup;

import Client.Model;
import Client.PropertyChangeSubject;
import Common.Profile;
import Common.UserGroup;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

public class CreateVoteGroupViewModelGUI  implements PropertyChangeListener,
    PropertyChangeSubject
{
  private PropertyChangeSupport support;
  private Model model;
  private UserGroup currentGroup;
  public CreateVoteGroupViewModelGUI(Model model)
  {
    this.model=model;
    this.model.addPropertyChangeListener("LookupUserResults", this);
    support = new PropertyChangeSupport(this);
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
    support.firePropertyChange("LookupUserResults", null,profile);
  }

  @Override public void propertyChange(PropertyChangeEvent evt)
  {
    handleLookupResult((Profile)evt.getNewValue());
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
}
