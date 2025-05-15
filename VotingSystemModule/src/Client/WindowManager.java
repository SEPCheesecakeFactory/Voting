package Client;

import Client.ChangeUsername.ChangeUsernameView;
import Client.ChangeUsername.ChangeUsernameViewModel;
import Client.ClosePoll.ClosePollView;
import Client.ClosePoll.ClosePollViewModel;
import Client.CreatePoll.CreatePollView;
import Client.CreatePoll.CreatePollViewModel;
import Client.CreateVoteGroup.CreateVoteGroupView;
import Client.CreateVoteGroup.CreateVoteGroupViewModel;
import Client.DisplayPoll.DisplayPollView;
import Client.DisplayPoll.DisplayPollViewModel;
import Client.GUITest.GUITestView;
import Client.Login.LoginView;
import Client.Login.LoginViewController;
import Client.Login.LoginViewModel;
import Client.Menu.MenuView;
import Client.Menu.MenuViewModel;
import Client.PollResult.PollResultView;
import Client.PollResult.PollResultViewModel;
import Client.Test.TestView;
import Client.Test.TestViewModel;
import Utils.Logger;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.IOException;

public class WindowManager
{
  private volatile static WindowManager instance;
  private Model model;
  private Stage primaryStage;

  private WindowManager()
  {
  }

  public static WindowManager getInstance()
  {
    if (instance == null)
    {
      instance = new WindowManager();
    }
    return instance;
  }

  public Stage getPrimaryStage()
  {
    return primaryStage;
  }

  public void setPrimaryStage(Stage primaryStage)
  {
    this.primaryStage = primaryStage;
  }

  // TODO: removing the old one could be implemented
  public void showView(ViewType type)
  {
    switch (type)
    {
      case ViewType.Menu:
        MenuViewModel menuVM = new MenuViewModel();
        MenuView menuV = new MenuView(menuVM);
        (new Thread(()->{showView(ViewType.Menu);})).start();
        break;
      case ViewType.PollResult:
        PollResultViewModel pollResultVM = new PollResultViewModel(getModel());
        PollResultView pollResultV = new PollResultView(pollResultVM);
        break;
      case ViewType.CreatePoll:
        CreatePollViewModel createPollViewModel = new CreatePollViewModel(
            getModel());
        CreatePollView createPollView = new CreatePollView(createPollViewModel);
        createPollView.render();
        break;
      case ViewType.DisplayPoll:
        openJavaFXWindow(getDisplayPollScene());
        break;
      case Login:
        openJavaFXWindow(getLoginScene());
        break;
      case ChangeUsername:
        ChangeUsernameViewModel changeUsernameVM = new ChangeUsernameViewModel(
            getModel());
        ChangeUsernameView changeUsernameV = new ChangeUsernameView(
            changeUsernameVM);
        break;
      case CreateGroup:
        CreateVoteGroupViewModel voteGroupVM = new CreateVoteGroupViewModel(
            getModel());
        CreateVoteGroupView voteGroupV = new CreateVoteGroupView(
            voteGroupVM);
        break;
      case ClosePoll:
        ClosePollViewModel closePollVM = new ClosePollViewModel(getModel());
        ClosePollView closePollV = new ClosePollView(closePollVM);
        closePollV.render();
        break;
      case Test:
        TestViewModel testVM = new TestViewModel();
        TestView testV = new TestView(testVM);
        testV.render();
        break;
      case GUITest:
        openJavaFXWindow(getGUITestScene());
        break;
      case HomeScreen:
        openJavaFXWindow(getHomeScreenScene());
        break;
    }
    // WindowManager.getInstance().showView(ViewType.Menu);
  }

  public void setModel(Model model)
  {
    this.model = model;
  }

  public Model getModel()
  {
    if (model == null)
    {
      model = new Model(new Client());
      Logger.log("Warning", "New Model created with Default Client...");
    }

    return model;
  }

  public void showErrorPopup(String errorText)
  {
    // Ensuring JavaFX Thread
    Platform.runLater(() -> {
      Alert alert = new Alert(Alert.AlertType.ERROR);
      alert.setTitle("Error");
      alert.setHeaderText("An error has occurred");
      alert.setContentText(errorText);
      alert.showAndWait();
    });
  }

  private void openJavaFXWindow(Scene scene)
  {
    try
    {
      showScene(scene);
    }
    catch (Exception e)
    {
      showErrorPopup("Could not open a new scene!");
      e.printStackTrace();
    }
  }

  private Scene getLoginScene()
  {
    FXMLLoader loader = new FXMLLoader(
        getClass().getResource("/Client/Login/LoginView.fxml"));
    Parent root = null;
    try
    {
      root = loader.load();
    }
    catch (IOException e)
    {
      return null;
    }

    LoginViewController controller = loader.getController();
    LoginViewModel viewModel = new LoginViewModel(getModel());
    controller.init(viewModel);

    return new Scene(root);
  }

  private Scene getDisplayPollScene() {
    FXMLLoader loader = new FXMLLoader(getClass().getResource("/Client/DisplayPoll/VoteScreen.fxml")); // or whatever your path is
    Parent root;
    try {
      root = loader.load();
    } catch (IOException e) {
      e.printStackTrace();
      return null;
    }

    DisplayPollView controller = loader.getController();
    controller.init(new DisplayPollViewModel(getModel()));
    return new Scene(root);
  }


  public void openJavaFXScene(String fxmlPath)
  {
    showScene(getScene(fxmlPath));
  }

  private Scene getScene(String fxmlPath)
  {
    FXMLLoader loader = new FXMLLoader(
        getClass().getResource(fxmlPath));
    Parent root = null;
    try
    {
      root = loader.load();
    }
    catch (IOException e)
    {
      return null;
    }

    return new Scene(root);
  }

  private Scene getHomeScreenScene()
  {
    return getScene("/Client/Menu/HomeScreen.fxml");
  }

  private Scene getGUITestScene()
  {
    // create a ViewModel if needed later
    FXMLLoader fxmlLoader = new FXMLLoader(
        getClass().getResource("/Client/GUITest/GUITest.fxml"));
    fxmlLoader.setControllerFactory(controllerClass -> new GUITestView());
    Scene scene = null;
    try
    {
      scene = new Scene(fxmlLoader.load());
    }
    catch (IOException e)
    {
      return null;
    }
    scene.setFill(Color.TRANSPARENT);
    return scene;
  }

  private void showScene(Scene scene)
  {
    Platform.runLater(()->{
      getPrimaryStage().setTitle("Voting System");
      // getPrimaryStage().initStyle(StageStyle.TRANSPARENT);
      getPrimaryStage().setScene(scene);
      getPrimaryStage().show();
    });
  }
}
