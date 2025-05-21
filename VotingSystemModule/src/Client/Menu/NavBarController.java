package Client.Menu;

import Client.ViewType;
import Client.WindowManager;
import javafx.fxml.FXML;
import javafx.scene.control.Button;

public class NavBarController
{

  @FXML private Button homeNavButton;
  @FXML private Button createPollNavButton;
  @FXML private Button availablePollsNavButton;
  @FXML private Button myUserGroupsNavButton;
  @FXML private Button userSettingsNavButton;


  @FXML public void initialize()
  {
    homeNavButton.setOnAction(e -> navigateToView(ViewType.HomeScreen));
    createPollNavButton.setOnAction(e -> navigateToView(ViewType.CreatePoll));
    availablePollsNavButton.setOnAction(
        e -> navigateToView(ViewType.AvailablePolls));
    myUserGroupsNavButton.setOnAction(
        e -> navigateToView(ViewType.CreateGroup));
    userSettingsNavButton.setOnAction(
        e -> navigateToView(ViewType.ChangeUsername));
  }

  private void navigateToView(ViewType viewType)
  {
    WindowManager.getInstance().showView(viewType);
  }
}