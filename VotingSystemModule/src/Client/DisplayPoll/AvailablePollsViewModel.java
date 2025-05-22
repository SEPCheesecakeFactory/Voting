package Client.DisplayPoll;

import Client.Model;
import Client.PropertyChangeSubject;
import Client.WindowManager;
import Common.Poll;
import Common.Profile;
import Common.UserGroup;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class AvailablePollsViewModel implements PropertyChangeSubject
{

  private final Model model;
  private final ObservableList<Poll> availablePolls = FXCollections.observableArrayList();
  private final StringProperty searchText = new SimpleStringProperty("");

  private final Set<Profile> confirmedUsers = new HashSet<>();
  private final Set<UserGroup> confirmedGroups = new HashSet<>();
  private final PropertyChangeSupport support = new PropertyChangeSupport(this);

  public AvailablePollsViewModel(Model model) {
    this.model = model;

    model.addPropertyChangeListener("AvailablePolls", evt -> {
      List<Poll> polls = (List<Poll>) evt.getNewValue();
      Platform.runLater(() -> {
        availablePolls.setAll(polls);
        support.firePropertyChange(evt);
      });
    });

    model.addPropertyChangeListener("LookupUserResults", this::handleUserLookupEvent);
    model.addPropertyChangeListener("LookupGroupResults", this::handleGroupLookupEvent);

    model.requestAvailablePolls();
  }

  public ObservableList<Poll> getAvailablePolls() {
    return availablePolls;
  }

  public StringProperty searchTextProperty() {
    return searchText;
  }

  public void requestVote(Poll poll) {
    model.sendDisplayPollRequest(poll.getId());
  }

  public void requestResults(Poll poll) {
    model.sendResultRequest(poll.getId());
  }

  public void closePoll(Poll poll) {
    boolean success = model.sendPollCloseRequest(poll.getId());
    if(!success) WindowManager.getInstance().showErrorPopup("Could not close the poll!");
    else
      WindowManager.getInstance().showInfoPopup("Poll closed!");
  }

  public int getLoggedInUserId() {
    return model.getProfile().getId();
  }

  public void validateUsername(String username) {
    model.requestUserLookup(username);
  }

  public void validateGroupName(String groupName) {
    model.requestGroupLookup(groupName);
  }

  private void handleUserLookupEvent(PropertyChangeEvent evt) {
    Profile profile = (Profile) evt.getNewValue();
    boolean success = profile.getId() != -1;

    if (success) {
      confirmedUsers.add(profile);
    }

    support.firePropertyChange("UserValidated", null, new ValidationResult(profile.getUsername(), success));
  }

  private void handleGroupLookupEvent(PropertyChangeEvent evt) {
    UserGroup group = (UserGroup) evt.getNewValue();
    boolean success = group.getId() != -1;

    if (success) {
      confirmedGroups.add(group);
    }

    support.firePropertyChange("GroupValidated", null, new ValidationResult(group.getGroupName(), success));
  }

  @Override
  public void addPropertyChangeListener(PropertyChangeListener listener) {
    support.addPropertyChangeListener(listener);
  }

  @Override public void addPropertyChangeListener(String name,
      PropertyChangeListener listener)
  {
    support.addPropertyChangeListener(name, listener);
  }

  public void removePropertyChangeListener(PropertyChangeListener listener) {
    support.removePropertyChangeListener(listener);
  }

  @Override public void removePropertyChangeListener(String name,
      PropertyChangeListener listener)
  {
    support.removePropertyChangeListener(name, listener);
  }

  public void refreshAvailablePolls()
  {
    model.requestAvailablePolls();

  }

  public static class ValidationResult {
    private final String name;
    private final boolean valid;

    public ValidationResult(String name, boolean valid) {
      this.name = name;
      this.valid = valid;
    }

    public String getName() {
      return name;
    }

    public boolean isValid() {
      return valid;
    }
  }
  public void saveAccessToUsers(Poll poll) {
    model.sendPollAccess(poll.getId(), confirmedUsers, null);
  }
  public void saveAccessToGroups(Poll poll) {
    model.sendPollAccess(poll.getId(), null, confirmedGroups);
  }
}
