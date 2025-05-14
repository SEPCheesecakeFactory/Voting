package Client.Menu;

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
    homeNavButton.setOnAction(e -> navigate("../Menu/HomeScreen.fxml"));
    createPollNavButton.setOnAction(e -> navigate("../CreatePoll/createPollScreen.fxml"));
    availablePollsNavButton.setOnAction(e -> navigate("../displayPoll/availablePolls.fxml"));
  }

  private void navigate(String fxmlPath) {
    try {
      
      FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
      Scene scene = new Scene(loader.load());


      Stage stage = (Stage) homeNavButton.getScene().getWindow();


      stage.setScene(scene);


      stage.setTitle(fxmlPath.replace(".fxml", "") + " Screen");

    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}