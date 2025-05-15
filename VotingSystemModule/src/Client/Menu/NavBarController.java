package Client.Menu;

import Client.WindowManager;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;

public class NavBarController {

  @FXML private Button homeNavButton;
  @FXML private Button createPollNavButton;
  @FXML private Button availablePollsNavButton;

  @FXML
  public void initialize() {
    homeNavButton.setOnAction(e -> navigate("/Client/Menu/HomeScreen.fxml"));
    createPollNavButton.setOnAction(e -> navigate("/Client/CreatePoll/createPollScreen.fxml"));
    availablePollsNavButton.setOnAction(e -> navigate("/Client/DisplayPoll/availablePolls.fxml"));
  }

  private void navigate(String fxmlPath) {
    WindowManager.getInstance().openJavaFXScene(fxmlPath);
  }
}