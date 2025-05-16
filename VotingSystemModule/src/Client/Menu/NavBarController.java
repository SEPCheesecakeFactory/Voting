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
  @FXML private Button pollResultsNavButton;
  @FXML private Button myUserGroupsNavButton;

  @FXML public void initialize()
  {
    homeNavButton.setOnAction(e -> navigateToView(ViewType.HomeScreen));
    createPollNavButton.setOnAction(e -> navigateToView(ViewType.CreatePoll));
    availablePollsNavButton.setOnAction(
        e -> navigateToView(ViewType.AvailablePolls));
    pollResultsNavButton.setOnAction(
        e -> navigateToView(ViewType.PollResult));
    myUserGroupsNavButton.setOnAction(
        e -> navigateToView(ViewType.CreateGroup));
  }

  private void navigateToView(ViewType viewType)
  {
    WindowManager.getInstance().showView(viewType);
  }
}