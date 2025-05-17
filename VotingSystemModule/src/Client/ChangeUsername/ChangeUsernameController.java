package Client.ChangeUsername;

import Client.Model;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

import java.net.URL;
import java.util.ResourceBundle;

public class ChangeUsernameController implements Initializable {

  private ChangeUsernameViewModel viewModel;

  @FXML
  private TextField currentUsernameField;

  @FXML
  private TextField newUsernameField;

  @FXML
  private Button saveProfileButton;

  @FXML
  private Label statusLabel;

  // Setter to inject the ViewModel
  public void setViewModel(ChangeUsernameViewModel viewModel) {
    this.viewModel = viewModel;

    // Initialize the current username field if profile is present
    if (viewModel != null) {
      currentUsernameField.setText(viewModel.getCurrentUsername());
    }

    // Listen for successful username change events
    viewModel.addPropertyChangeListener("UsernameChanged", evt -> {
      String newUsername = (String) evt.getNewValue();
      Platform.runLater(() -> {
        currentUsernameField.setText(newUsername);
        statusLabel.setText("Username changed successfully.");
        statusLabel.setStyle("-fx-text-fill: green;");
        newUsernameField.clear();
      });
    });

    // Listen for username change failure events
    viewModel.addPropertyChangeListener("UsernameChangeFailed", evt -> {
      String error = (String) evt.getNewValue();
      Platform.runLater(() -> {
        statusLabel.setText("Failed to change username: " + error);
        statusLabel.setStyle("-fx-text-fill: red;");
      });
    });
  }

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    currentUsernameField.setEditable(false);

    saveProfileButton.setOnAction(event -> {
      String newUsername = newUsernameField.getText().trim();
      if (newUsername.isEmpty()) {
        statusLabel.setText("New username cannot be empty");
        statusLabel.setStyle("-fx-text-fill: red;");
        return;
      }
      if (viewModel == null) {
        statusLabel.setText("ViewModel is not set!");
        statusLabel.setStyle("-fx-text-fill: red;");
        return;
      }
      viewModel.changeUserName(newUsername);
      statusLabel.setText("Username change requested");
      statusLabel.setStyle("-fx-text-fill: black;");
    });
  }
}
