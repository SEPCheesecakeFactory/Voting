package Client;

import Client.AddUsers.AddUsersView;
import Client.AddUsers.AddUsersViewModel;
import Client.ChangeUsername.ChangeUsernameController;
import Client.ChangeUsername.ChangeUsernameView;
import Client.ChangeUsername.ChangeUsernameViewModel;
import Client.ClosePoll.ClosePollView;
import Client.ClosePoll.ClosePollViewModel;
import Client.CreatePoll.CreatePollGUIView;
import Client.CreatePoll.CreatePollGUIViewModel;
import Client.CreatePoll.CreatePollView;
import Client.CreatePoll.CreatePollViewModel;
import Client.CreateVoteGroup.CreateVoteGroupView;
import Client.CreateVoteGroup.CreateVoteGroupViewController;
import Client.CreateVoteGroup.CreateVoteGroupViewModel;
import Client.CreateVoteGroup.CreateVoteGroupViewModelGUI;
import Client.DisplayPoll.*;
import Client.GUITest.GUITestView;
import Client.Login.LoginView;
import Client.Login.LoginViewController;
import Client.Login.LoginViewModel;
import Client.Menu.MenuView;
import Client.Menu.MenuViewModel;
import Client.PollResult.PollResultViewController;
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
import javafx.scene.control.ButtonType;
import javafx.scene.control.DialogPane;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.IOException;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;

public class WindowManager
{
  private volatile static WindowManager instance;
  private Model model;
  private Stage primaryStage;
  private Scene mainScene;

  private static final String GENERAL_CSS = Objects.requireNonNull(
      WindowManager.class.getResource("/general.css")).toExternalForm();

  private WindowManager()
  {
  }

  public static WindowManager getInstance()
  {
    if (instance == null)
    {
      synchronized (WindowManager.class)
      {
        if (instance == null)
        {
          instance = new WindowManager();
        }
      }
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
    Image icon = new Image(
        getClass().getResourceAsStream("/icons/vote_icon2.png"));
    primaryStage.getIcons().add(icon);
  }

  // TODO: removing the old one could be implemented
  public void showView(ViewType type)
  {
    switch (type)
    {
      case ViewType.Menu:
        MenuViewModel menuVM = new MenuViewModel();
        MenuView menuV = new MenuView(menuVM);
        var thread = new Thread(() -> {
          showView(ViewType.Menu);
        });
        thread.setDaemon(true);
        thread.start();
        break;
      case ViewType.PollResult:
        openJavaFXWindow(getPollResultScene());
        break;
      case ViewType.CreatePoll:
        openJavaFXWindow(getCreatePollScene());
        break;
      case ViewType.DisplayPoll:
        openJavaFXWindow(getDisplayPollScene());
        break;
      case Login:
        openJavaFXWindow(getLoginScene());
        break;
      case ChangeUsername:
        //        ChangeUsernameViewModel changeUsernameVM = new ChangeUsernameViewModel(
        //            getModel());
        //        ChangeUsernameView changeUsernameV = new ChangeUsernameView(
        //            changeUsernameVM);
        openJavaFXWindow(getChangeUsernameScreen());
        break;
      case CreateGroup:
        //        CreateVoteGroupViewModel voteGroupVM = new CreateVoteGroupViewModel(
        //            getModel());
        //        CreateVoteGroupView voteGroupV = new CreateVoteGroupView(
        //            voteGroupVM);
        openJavaFXWindow(getMyUserGroupsScene());
        break;
      case AddUsersGroups:
        AddUsersViewModel addUsersVM = new AddUsersViewModel(getModel());
        AddUsersView addUsersV = new AddUsersView(addUsersVM);
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
      case AvailablePolls:
        openJavaFXWindow(getAvailablePollsScene());
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
    showPopup(Alert.AlertType.ERROR, errorText, "Error", "Error");
  }

  public void showInfoPopup(String infoText)
  {
    showPopup(Alert.AlertType.INFORMATION, infoText, "Information",
        "Information");
  }

  public void showPopup(Alert.AlertType alertType, String text,
      String headerText, String title)
  {
    // Ensuring JavaFX Thread
    Platform.runLater(() -> {
      Alert popup = new Alert(alertType);
      popup.setTitle(title);
      popup.setHeaderText(headerText);
      popup.setContentText(text);

      DialogPane pane = popup.getDialogPane();
      if (!pane.getStylesheets().contains(GENERAL_CSS))
      {
        pane.getStylesheets().add(GENERAL_CSS);
      }

      popup.showAndWait();
    });
  }

  public void showConfirmationPopup(String text, String headerText,
      String title, Consumer<Boolean> resultCallback)
  {
    Platform.runLater(() -> {
      Alert popup = new Alert(Alert.AlertType.CONFIRMATION, text, ButtonType.OK,
          ButtonType.CANCEL);
      popup.setTitle(title);
      popup.setHeaderText(headerText);
      // apply CSS once
      DialogPane pane = popup.getDialogPane();
      if (!pane.getStylesheets().contains(GENERAL_CSS))
      {
        pane.getStylesheets().add(GENERAL_CSS);
      }
      Optional<ButtonType> res = popup.showAndWait();
      resultCallback.accept(res.isPresent() && res.get() == ButtonType.OK);
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

  private Scene getPollResultScene()
  {
    FXMLLoader loader = new FXMLLoader(
        getClass().getResource("/Client/PollResult/PollResultView.fxml"));
    Parent root = null;
    try
    {
      root = loader.load();
    }
    catch (IOException e)
    {
      return null;
    }

    PollResultViewController controller = loader.getController();
    PollResultViewModel viewModel = new PollResultViewModel(getModel());
    controller.init(viewModel);

    return new Scene(root);
  }

  private Scene getMyUserGroupsScene()
  {
    FXMLLoader loader = new FXMLLoader(
        getClass().getResource("/Client/CreateVoteGroup/MyGroupsScreen.fxml"));
    Parent root = null;
    try
    {
      root = loader.load();
    }
    catch (IOException e)
    {
      return null;
    }

    CreateVoteGroupViewController controller = loader.getController();
    CreateVoteGroupViewModelGUI viewModel = new CreateVoteGroupViewModelGUI(
        getModel());
    controller.setViewModel(viewModel);

    return new Scene(root);
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

  private Scene getCreatePollScene()
  {
    FXMLLoader loader = new FXMLLoader(
        getClass().getResource("/Client/CreatePoll/CreatePollScreen.fxml"));
    Parent root = null;
    try
    {
      root = loader.load();
    }
    catch (IOException e)
    {
      return null;
    }

    CreatePollGUIView controller = loader.getController();
    CreatePollGUIViewModel viewModel = new CreatePollGUIViewModel(getModel());
    controller.setViewModel(viewModel);

    return new Scene(root);
  }

  private Scene getAvailablePollsScene()
  {
    FXMLLoader loader = new FXMLLoader(
        getClass().getResource("/Client/DisplayPoll/AvailablePolls.fxml"));
    Parent root = null;
    try
    {
      root = loader.load();
    }
    catch (IOException e)
    {
      return null;
    }

    AvailablePollsController controller = loader.getController();
    AvailablePollsViewModel viewModel = new AvailablePollsViewModel(getModel());
    controller.init(viewModel);

    return new Scene(root);
  }

  private Scene getDisplayPollScene()
  {
    FXMLLoader loader = new FXMLLoader(
        getClass().getResource("/Client/DisplayPoll/VoteScreen.fxml"));
    Parent root = null;
    try
    {
      root = loader.load();
    }
    catch (IOException e)
    {
      return null;
    }
    DisplayPollViewController controller = loader.getController();
    DisplayPollViewModelGUI viewModelGUI = new DisplayPollViewModelGUI(
        getModel());
    controller.init(viewModelGUI);
    return new Scene(root);
    //    return getScene("/Client/DisplayPoll/VoteScreen.fxml");
  }

  public void openJavaFXScene(String fxmlPath)
  {
    showScene(getScene(fxmlPath));
  }

  private Scene getScene(String fxmlPath)
  {
    FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
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

  private void showScene(
      Scene sceneWithNewRoot) // NOTE: this was done to keep the rest compatible, could have been replaced with directly taking the root as a parameter
  {
    String css = getClass().getResource("/general.css").toExternalForm();
    if (!sceneWithNewRoot.getStylesheets().contains(css))
      sceneWithNewRoot.getStylesheets().add(css);

    Platform.runLater(() -> {
      if (mainScene == null)
      {
        mainScene = sceneWithNewRoot;
        primaryStage.setTitle("Electio");
        primaryStage.setScene(mainScene);
        primaryStage.show();
      }
      else
      {
        Parent newRoot = sceneWithNewRoot.getRoot();
        if (newRoot.getScene() != null && newRoot.getScene() != mainScene)
          newRoot.getScene().setRoot(new javafx.scene.Group());

        mainScene.setRoot(newRoot);
        mainScene.setFill(sceneWithNewRoot.getFill());
      }
    });
  }

  public Scene getChangeUsernameScreen()
  {
    FXMLLoader loader = new FXMLLoader(getClass().getResource(
        "/Client/ChangeUsername/ChangeUsernameScreen.fxml"));
    Parent root = null;
    try
    {
      root = loader.load();
    }
    catch (IOException e)
    {
      return null;
    }

    ChangeUsernameController controller = loader.getController();
    ChangeUsernameViewModel viewModel = new ChangeUsernameViewModel(getModel());
    controller.setViewModel(viewModel);

    return new Scene(root);
  }
}
