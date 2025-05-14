package Client.Login;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.text.Text;

public class LoginViewController {

  @FXML
  private TextField usernameField;

  @FXML
  private Button loginButton;

  @FXML
  private Button registerButton;

  @FXML
  private Text messageText;

  private LoginViewModel viewModel;

  public void init(LoginViewModel viewModel) {
    this.viewModel = viewModel;
    // Optional: bind or listen to model changes if needed
  }

  @FXML
  private void onLoginClicked() {
    String username = usernameField.getText().trim();
    if (!username.isEmpty()) {
      viewModel.loginOrRegister(username);
    } else {
      messageText.setText("Username cannot be empty.");
    }
  }

  @FXML
  private void onRegisterClicked() {
    String username = usernameField.getText().trim();
    if (!username.isEmpty()) {
      viewModel.loginOrRegister(username);
    } else {
      messageText.setText("Username cannot be empty.");
    }
  }
}