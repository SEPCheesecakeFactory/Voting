package Client;

import Common.Poll;
import Common.Profile;
import Common.Vote;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.security.InvalidParameterException;

public class ClientViewModel
    implements PropertyChangeListener, PropertyChangeSubject
{
  private final Model model;
  private final PropertyChangeSupport support;

  public ClientViewModel(Model model)
  {
    support = new PropertyChangeSupport(this);
    this.model = model;
    this.model.addPropertyChangeListener("PollUpdated", this);
    this.model.addPropertyChangeListener("NewMessage", this);
    this.model.addPropertyChangeListener("ProfileSet", this);
  }

  public void sendVote(int userId, int[] choices)
  {
    model.sendVote(userId, choices);
  }

  public void loginOrRegister(String username)
  {
    Profile profile = new Profile(username);
    model.sendLoginOrRegister(profile);  // Send profile to the server
  }

  public void changeUserName(String username)
  {
    model.sendChangeUsername(username);
  }

  public Model getModel()
  {
    return model;
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
    support.firePropertyChange(evt);
  }
}
