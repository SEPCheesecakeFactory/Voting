package Client.DisplayPoll;

import Client.Model;
import Client.PropertyChangeSubject;
import Client.WindowManager;
import Common.Poll;
import Utils.Logger;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

public class DisplayPollViewModel  implements PropertyChangeListener,
    PropertyChangeSubject
{
  private Model model;
  private PropertyChangeSupport support = new PropertyChangeSupport(this);

  public DisplayPollViewModel(Model model) {
    this.model = model;
    this.model.addPropertyChangeListener("PollUpdated", this);
  }

  public Model getModel()
  {
    return model;
  }
  public void sendVote(int userId, int[] choices)
  {
    Poll currentPoll = model.getPoll();

    if (currentPoll != null && currentPoll.isClosed())
      WindowManager.getInstance().showErrorPopup("Cannot vote: Poll is closed.");

    model.sendVote(userId, choices);
  }
  public void sendPollRequest(int pollId)
  {
    model.sendPollRequest(pollId);
  }

  public void sendDisplayPollRequest(int pollId) {
    model.sendDisplayPollRequest(pollId);
  }

  public void setPoll(Poll poll) {
    propertyChange(new PropertyChangeEvent(this, "PollUpdated", null, poll));
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
    System.out.println("?");
    support.firePropertyChange(evt);
  }
}