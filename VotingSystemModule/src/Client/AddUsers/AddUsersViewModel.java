package Client.AddUsers;

import Client.Model;
import Common.Profile;
import Common.UserGroup;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.HashSet;
import java.util.Set;

public class AddUsersViewModel implements PropertyChangeListener {
  private final Model model;
  private int pollId;

  // Store only confirmed users and group names
  private final Set<Profile> confirmedUsers = new HashSet<>();
  private final Set<UserGroup> confirmedGroups = new HashSet<>();

  public AddUsersViewModel(Model model) {
    this.model = model;
    this.model.addPropertyChangeListener("LookupUserResults", this);
    this.model.addPropertyChangeListener("LookupGroupResults", this);
  }

  public void setPollId(int pollId) {
    this.pollId = pollId;
  }

  // Initiate user lookup
  public void addUser(String username) {
    model.requestUserLookup(username);
  }

  // Initiate group lookup
  public void addGroup(String groupName) {
    model.requestGroupLookup1(groupName);
  }

  private void handleUserLookupResult(Profile profile) {
    if (profile.getId() == -1) {
      System.out.println("User '" + profile.getUsername() + "' not found. Cannot add.");
    } else if (confirmedUsers.add(profile)) {
      System.out.println("User '" + profile.getUsername() + "' added for poll access.");
    } else {
      System.out.println("User '" + profile.getUsername() + "' is already queued.");
    }
  }

  private void handleGroupLookupResult(UserGroup userGroup) {
    if (userGroup == null) {
      System.out.println("Group not found. Cannot add.");
    } else if (confirmedGroups.add(userGroup)) {
      System.out.println("Group '" + userGroup + "' added for poll access.");
    } else {
      System.out.println("Group '" + userGroup + "' is already queued.");
    }
  }

  @Override
  public void propertyChange(PropertyChangeEvent evt) {
    switch (evt.getPropertyName()) {
      case "LookupUserResults":
        handleUserLookupResult((Profile) evt.getNewValue());
        break;
      case "LookupGroupResults":
        handleGroupLookupResult((UserGroup) evt.getNewValue());
        break;
    }
  }

  public void saveAccess() {
    model.sendPollAccess(pollId, confirmedUsers, confirmedGroups);
  }
}
