package Client;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

public class ChangeUsernameViewModel implements PropertyChangeListener, PropertyChangeSubject{
  private Model model;
  private PropertyChangeSupport support = new PropertyChangeSupport(this);

  public ChangeUsernameViewModel(Model model) {
    this.model = model;
    this.model.addPropertyChangeListener("ProfileSet", this);
  }
  public void changeUserName(String username)
  {
    model.sendChangeUsername(username);
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
