package Client.ChangeUsername;

import Client.Model;
import Client.PropertyChangeSubject;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

public class ChangeUsernameViewModel implements PropertyChangeListener, PropertyChangeSubject {

  private final Model model;
  private final PropertyChangeSupport support = new PropertyChangeSupport(this);

  public ChangeUsernameViewModel(Model model) {
    this.model = model;
    model.addPropertyChangeListener("UsernameChanged", this);
    model.addPropertyChangeListener("UsernameChangeFailed", this);
  }

  public void changeUserName(String username) {
    model.sendChangeUsername(username);
  }

  public String getCurrentUsername() {
    return model.getProfile() != null ? model.getProfile().getUsername() : "";
  }

  @Override
  public void propertyChange(PropertyChangeEvent evt) {
    support.firePropertyChange(evt.getPropertyName(), null, evt.getNewValue());
  }

  @Override public void addPropertyChangeListener(PropertyChangeListener listener) {
    support.addPropertyChangeListener(listener);
  }

  @Override public void addPropertyChangeListener(String name, PropertyChangeListener listener) {
    support.addPropertyChangeListener(name, listener);
  }

  @Override public void removePropertyChangeListener(PropertyChangeListener listener) {
    support.removePropertyChangeListener(listener);
  }

  @Override public void removePropertyChangeListener(String name, PropertyChangeListener listener) {
    support.removePropertyChangeListener(name, listener);
  }
}
