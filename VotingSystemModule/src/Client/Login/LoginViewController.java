package Client.Login;

import Client.ViewType;
import Client.WindowManager;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.io.IOException;

public class LoginViewController
{

  @FXML private TextField usernameField;

  @FXML private Button loginButton;

  @FXML private Button registerButton;

  @FXML private Text messageText;

  private LoginViewModel viewModel;

  public void init(LoginViewModel viewModel)
  {
    this.viewModel = viewModel;
    // listen to model if changes needed maybe
  }

  @FXML private void onLoginClicked()
  {
    String username = usernameField.getText().trim();
    if (username.isEmpty())
    {
      messageText.setText("Username cannot be empty.");
      return;
    }
    viewModel.loginOrRegister(username);
    // Switch to HomeScreen.fxml
    WindowManager.getInstance().showView(ViewType.HomeScreen);
  }

  @FXML private void onRegisterClicked()
  {
    String username = usernameField.getText().trim();
    if (!username.isEmpty())
    {
      viewModel.loginOrRegister(username);
    }
    else
    {
      messageText.setText("Username cannot be empty.");
    }
  }
}