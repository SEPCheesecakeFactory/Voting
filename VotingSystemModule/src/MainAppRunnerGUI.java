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

public class MainAppRunnerGUI extends Application {

  @Override
  public void start(Stage primaryStage) throws Exception {
    // 1. setting primary stage in WM
    WindowManager windowManager = WindowManager.getInstance();
    windowManager.setPrimaryStage(primaryStage);

    // 2. creating and running the client
    Client client = new Client("localhost", 2910);
    client.run();

    // 3. gettin the model from the WM
    Model model = WindowManager.getInstance().getModel();

    // 4. loadin the LoginView FXML
    FXMLLoader loader = new FXMLLoader(getClass().getResource("/Client/Login/LoginView.fxml"));
    Parent root = loader.load();

    // 5. setting up VM and Comtroller
    LoginViewController controller = loader.getController();
    LoginViewModel viewModel = new LoginViewModel(model);
    controller.init(viewModel);

    // 6. displaying the start screen scene
    Scene scene = new Scene(root);
    primaryStage.setScene(scene);
    primaryStage.setTitle("Start Screen");
    primaryStage.show();
  }

  public static void main(String[] args) {
    launch(args); // This will call start(Stage) automatically
  }
}
