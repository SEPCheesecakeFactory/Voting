package Client;

import Common.Poll;
import Common.Profile;
import Common.Vote;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.security.InvalidParameterException;

public class ClientViewModel implements PropertyChangeListener {
  private final Model model;
  private ClientView view;

  public ClientViewModel(Model model) {
    this.model = model;
    this.model.addPropertyChangeListener("PollUpdated", this);
    this.model.addPropertyChangeListener("NewMessage", this);
    this.model.addPropertyChangeListener("ProfileSet", this);
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
  public void changeUserName(String username)
  {
    model.sendChangeUsername(username);
  }
  public Model getModel()
  {
    return model;
  }

  @Override
  public void propertyChange(PropertyChangeEvent evt)
  {
    if (view == null) return;
    switch (evt.getPropertyName()) {
      case "PollUpdated":
        view.displayPoll((Poll) evt.getNewValue());
        break;
      case "NewMessage":
        view.displayMessage((String) evt.getNewValue());
        break;
      case "ProfileSet":
        view.displayChangeUsername();
        break;
      default:
        throw new InvalidParameterException(String.format("Event %s does not exist in the current context.", evt.getPropertyName()));
    }
  }
}
