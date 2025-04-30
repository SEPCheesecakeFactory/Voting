package Client;

import Common.Poll;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

public class Model implements PropertyChangeSubject {
  private final PropertyChangeSupport support;
  private Poll currentPoll;

  public Model() {
    support = new PropertyChangeSupport(this);
  }

  public void setPoll(Poll poll) {
    Poll oldPoll = this.currentPoll;
    this.currentPoll = poll;
    support.firePropertyChange("PollUpdated", oldPoll, currentPoll);
  }

  public Poll getPoll() {
    return currentPoll;
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
