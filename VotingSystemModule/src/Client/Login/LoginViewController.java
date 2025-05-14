package Client.Login;

import javafx.fxml.FXML;
import javafx.scene.control.TextField;

public class LoginViewController {
  private LoginViewModel viewModel;

  @FXML
  private TextField usernameField;

  public void init(LoginViewModel viewModel) {
    this.viewModel = viewModel;
  }

  @FXML
  private void onLoginClicked() {
    String username = usernameField.getText();
    viewModel.loginOrRegister(username);
  }

  @FXML
  private void onRegisterClicked() {

  }
}