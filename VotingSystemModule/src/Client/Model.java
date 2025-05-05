package Client;

import Common.Poll;
import Common.Profile;
import Common.Vote;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

public class Model implements PropertyChangeSubject {
  private final PropertyChangeSupport support;
  private final ClientConnection connection;
  private Poll currentPoll;
  private Profile currentProfile;

  public Model(ClientConnection connection) {
    this.connection = connection;
    support = new PropertyChangeSupport(this);
  }

  public void setPoll(Poll poll) {
    Poll oldPoll = this.currentPoll;
    this.currentPoll = poll;
    support.firePropertyChange("PollUpdated", oldPoll, currentPoll);
  }
  public void setProfile(Profile profile) {
    this.currentProfile = profile;
  }

  public Profile getProfile() {
    return currentProfile;
  }


  public Poll getPoll() {
    return currentPoll;
  }

  public void sendLoginOrRegister(Profile profile) {
    try {
      connection.sendLoginOrRegister(profile);
      setProfile(profile);  // Set the profile after receiving the user ID
    } catch (Exception e) {
      System.out.println("Failed to login or register: " + e.getMessage());
    }
  }
  public void sendVote(int userId, int[] choices)
  {
    try {
      Vote vote = new Vote(userId, choices);
      connection.sendVote(vote);
    } catch (Exception e) {
      System.out.println("Failed to send vote: " + e.getMessage());
    }
  }

  @Override
  public void addPropertyChangeListener(PropertyChangeListener listener) {
    support.addPropertyChangeListener(listener);
  }

  @Override
  public void addPropertyChangeListener(String name, PropertyChangeListener listener) {
    support.addPropertyChangeListener(name, listener);
  }

  @Override
  public void removePropertyChangeListener(PropertyChangeListener listener) {
    support.removePropertyChangeListener(listener);
  }

  @Override
  public void removePropertyChangeListener(String name, PropertyChangeListener listener) {
    support.removePropertyChangeListener(name, listener);
  }
}
