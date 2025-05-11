import Client.Client;
import Client.WindowManager;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class MainAppRunner extends Application
{
  public static void main(String[] args)
  {
    Client client = new Client("localhost", 2910);
    (new Thread(()->{launch(args);})).start();
    client.run();
  }

  @Override public void start(Stage primaryStage) throws Exception
  {
    WindowManager.getInstance().setPrimaryStage(primaryStage);
  }
}
