package Client;

import Common.Poll;
import Common.Vote;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

public class ClientViewModel implements PropertyChangeListener {
  private final Model model;
  private final ClientConnection connection; //can this be like this or should this reference be in the model?
  private ClientView view;

  public ClientViewModel(Model model, ClientConnection connection) {
    this.model = model;
    this.connection = connection;
    this.model.addPropertyChangeListener("PollUpdated", this);
  }

  //i know this is against the rules but this is going to be fixed when we start using javaFX and binding
  public void setView(ClientView view) {
    this.view = view;
  }

  //should the logic of this method be in the model?
  public void sendVote(int userId, int[] choices) {
    try {
      Vote vote = new Vote(userId, choices);
      connection.sendVote(vote);
    } catch (Exception e) {
      System.out.println("Failed to send vote: " + e.getMessage());
    }
  }

  @Override
  public void propertyChange(PropertyChangeEvent evt) {
    if ("PollUpdated".equals(evt.getPropertyName()) && view != null) {
      Poll updatedPoll = (Poll) evt.getNewValue();
      view.displayPoll(updatedPoll);
    }
  }
}
