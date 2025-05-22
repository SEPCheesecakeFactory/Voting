package Client.Login;

import Client.Model;
import Common.Profile;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

public class LoginViewModel {
  private final Model model;
  private final PropertyChangeSupport support;

  public LoginViewModel(Model model) {
    this.model = model;
    this.support = new PropertyChangeSupport(this);

    this.model.addPropertyChangeListener(evt -> {
      String evtName = evt.getPropertyName();
      if (evtName.equals("loginSuccess") || evtName.equals("loginFailure") ||
          evtName.equals("registerSuccess") || evtName.equals("registerFailure")) {
        support.firePropertyChange(evtName, null, evt.getNewValue());
      }
    });
  }

  public void login(String username) {
    Profile profile = new Profile(username);
    model.sendLogin(profile);
  }

  public void register(String username) {
    Profile profile = new Profile(username);
    model.sendRegister(profile);
  }

  public void addPropertyChangeListener(PropertyChangeListener listener) {
    support.addPropertyChangeListener(listener);
  }

  public void removePropertyChangeListener(PropertyChangeListener listener) {
    support.removePropertyChangeListener(listener);
  }
}
