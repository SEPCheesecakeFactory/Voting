import Client.Client;
import Client.Login.LoginViewController;
import Client.Login.LoginViewModel;
import Client.Model;
import Client.WindowManager;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import Client.ViewType;

public class MainAppRunnerGUI extends Application {

  @Override
  public void start(Stage primaryStage) throws Exception {
    WindowManager.getInstance().setPrimaryStage(primaryStage);
    WindowManager.getInstance().showView(ViewType.Login);
  }

  public static void main(String[] args) {
    Client client = new Client("localhost", 2910);
    client.run();
    launch(args);
  }
}
