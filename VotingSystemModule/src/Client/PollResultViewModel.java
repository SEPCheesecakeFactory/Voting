package Client;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

public class PollResultViewModel implements PropertyChangeListener, PropertyChangeSubject {
  private final PollResultRequestService pollResultRequestService;
  private final PropertyChangeSupport support;

  public PollResultViewModel(PollResultRequestService pollResultRequestService) {
    this.pollResultRequestService = pollResultRequestService;
    support = new PropertyChangeSupport(this);
    this.pollResultRequestService.addPropertyChangeListener("PollResult", this);
  }

  public void requestPollResult(int pollID) {
    pollResultRequestService.sendResultRequest(pollID);
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

  @Override
  public void propertyChange(PropertyChangeEvent evt) {
    support.firePropertyChange(evt);
  }
}
