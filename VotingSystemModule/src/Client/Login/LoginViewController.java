package Client.Login;

import Client.ViewType;
import Client.WindowManager;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.text.Text;

public class LoginViewController {

  @FXML private TextField usernameField;
  @FXML private Button loginButton;
  @FXML private Button registerButton;
  @FXML private Text messageText;

  private LoginViewModel viewModel;

  public void init(LoginViewModel viewModel) {
    this.viewModel = viewModel;

    viewModel.addPropertyChangeListener(evt -> {
      Platform.runLater(() -> {
        switch (evt.getPropertyName()) {
          case "ProfileSet":
          case "registerSuccess":
            messageText.setText("");
            WindowManager.getInstance().showView(ViewType.HomeScreen);
            break;

          case "loginFailure":
          case "registerFailure":
            messageText.setText((String) evt.getNewValue());
            break;
        }
      });
    });
  }

  @FXML
  private void onLoginClicked() {
    String username = usernameField.getText().trim();
    if (username.isEmpty()) {
      messageText.setText("Username cannot be empty.");
      return;
    }
    viewModel.login(username);
  }

  @FXML
  private void onRegisterClicked() {
    String username = usernameField.getText().trim();
    if (username.isEmpty()) {
      messageText.setText("Username cannot be empty.");
      return;
    }
    viewModel.register(username);
  }
}
