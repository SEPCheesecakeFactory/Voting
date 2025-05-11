package Client.Login;

import Client.Model;
import Common.Profile;

public class LoginViewModel {
  private Model model;

  public LoginViewModel(Model model) {
    this.model = model;
  }

  public void loginOrRegister(String username)
  {
    Profile profile = new Profile(username);
    model.sendLoginOrRegister(profile);  // Send profile to the server
  }
}