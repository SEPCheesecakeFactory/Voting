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

  @FXML public void initialize()
  {
    homeNavButton.setOnAction(e -> System.out.println("edit NavBarController"));
    createPollNavButton.setOnAction(e -> navigateToCreatePoll());
    availablePollsNavButton.setOnAction(
        e -> System.out.println("edit NavBarController"));
  }

  private void navigate(String fxmlPath)
  {
    WindowManager.getInstance().openJavaFXScene(fxmlPath);
  }
  private  void navigateToCreatePoll()
  {
    WindowManager.getInstance().showView(ViewType.CreatePoll);
  }
}