package Client;

import Common.Poll;
import Common.Profile;
import Common.Vote;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

public class ClientViewModel implements PropertyChangeListener {
  private final Model model;
  private ClientView view;

  public ClientViewModel(Model model) {
    this.model = model;
    this.model.addPropertyChangeListener("PollUpdated", this);
  }

  //i know this is against the rules but this is going to be fixed when we start using javaFX and binding
  public void setView(ClientView view) {
    this.view = view;
  }

  public void sendVote(int userId, int[] choices) {
  model.sendVote(userId, choices);
  }
  public void loginOrRegister(String username) {
    Profile profile = new Profile(username);
    model.sendLoginOrRegister(profile);  // Send profile to the server
  }
  public Model getModel()
  {
    return model;
  }

  @Override
  public void propertyChange(PropertyChangeEvent evt) {
    if ("PollUpdated".equals(evt.getPropertyName()) && view != null) {
      Poll updatedPoll = (Poll) evt.getNewValue();
      view.displayPoll(updatedPoll);
    }
  }
}
