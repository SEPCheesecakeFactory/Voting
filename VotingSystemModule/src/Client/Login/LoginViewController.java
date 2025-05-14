package Client.Login;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.io.IOException;

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
      // Switch to HomeScreen.fxml
      try {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("../Menu/HomeScreen.fxml"));
        Parent root = loader.load();

        // Optional: pass data to the HomeScreen controller if needed
        // HomeScreenController controller = loader.getController();
        // controller.init(...);

        Stage stage = (Stage) loginButton.getScene().getWindow();
        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
      } catch (IOException e) {
        e.printStackTrace();
        messageText.setText("Failed to load home screen.");
      }
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