package Client;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

public class PollResultViewModel implements PropertyChangeListener, PropertyChangeSubject
{
  private PollResultRequestService pollResultRequestService;
  private final PropertyChangeSupport support;
  public PollResultViewModel(PollResultRequestService pollResultRequestService)
  {
    this.pollResultRequestService=pollResultRequestService;
    support = new PropertyChangeSupport(this);
    this.pollResultRequestService.addPropertyChangeListener("PollResult",this);
  }

  @Override public void addPropertyChangeListener(
      PropertyChangeListener listener)
  {

  }

  @Override public void addPropertyChangeListener(String name,
      PropertyChangeListener listener)
  {

  }

  @Override public void removePropertyChangeListener(
      PropertyChangeListener listener)
  {

  }

  @Override public void removePropertyChangeListener(String name,
      PropertyChangeListener listener)
  {

  }

  @Override public void propertyChange(PropertyChangeEvent evt)
  {

  }
}
