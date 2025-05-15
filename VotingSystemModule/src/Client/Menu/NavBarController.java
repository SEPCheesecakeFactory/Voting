package Client.Menu;

import Client.WindowManager;
import javafx.fxml.FXML;
import javafx.scene.control.Button;

public class NavBarController
{

  @FXML private Button homeNavButton;
  @FXML private Button createPollNavButton;
  @FXML private Button availablePollsNavButton;

  @FXML public void initialize()
  {
    homeNavButton.setOnAction(e -> navigate("/Client/Menu/HomeScreen.fxml"));
    createPollNavButton.setOnAction(
        e -> navigate("/Client/CreatePoll/CreatePollScreen.fxml"));
    availablePollsNavButton.setOnAction(
        e -> navigate("/Client/DisplayPoll/AvailablePolls.fxml"));
  }

  private void navigate(String fxmlPath)
  {
    WindowManager.getInstance().openJavaFXScene(fxmlPath);
  }
}