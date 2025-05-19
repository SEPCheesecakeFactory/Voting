package Client.ChangeUsername;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;

import java.net.URL;
import java.util.ResourceBundle;

public class ChangeUsernameController implements Initializable {

  private ChangeUsernameViewModel viewModel;

  @FXML private TextField currentUsernameField;
  @FXML private TextField newUsernameField;
  @FXML private Button saveProfileButton;
  @FXML private Label statusLabel;

  public void setViewModel(ChangeUsernameViewModel viewModel) {
    this.viewModel = viewModel;
    currentUsernameField.setText(viewModel.getCurrentUsername());

    viewModel.addPropertyChangeListener("UsernameChanged", evt -> {
      String newUsername = (String) evt.getNewValue();
      Platform.runLater(() -> {
        currentUsernameField.setText(newUsername);
        statusLabel.setText("Username changed successfully.");
        statusLabel.setStyle("-fx-text-fill: green;");
        newUsernameField.clear();
      });
    });

    viewModel.addPropertyChangeListener("UsernameChangeFailed", evt -> {
      String error = (String) evt.getNewValue();
      Platform.runLater(() -> {
        statusLabel.setText("Error: " + error);
        statusLabel.setStyle("-fx-text-fill: red;");
        newUsernameField.clear();
      });
    });
  }

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    currentUsernameField.setEditable(false);

    saveProfileButton.setOnAction(event -> {
      String newUsername = newUsernameField.getText().trim();
      if (newUsername.isEmpty()) {
        statusLabel.setText("New username cannot be empty.");
        statusLabel.setStyle("-fx-text-fill: red;");
        return;
      }

      statusLabel.setText("Username change requested...");
      statusLabel.setStyle("-fx-text-fill: black;");
      viewModel.changeUserName(newUsername);
    });
  }
}
