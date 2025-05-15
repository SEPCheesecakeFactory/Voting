import Client.Client;
import Client.WindowManager;
import javafx.application.Application;
import javafx.stage.Stage;
import Client.ViewType;

public class MainAppRunner extends Application
{
  public static void main(String[] args)
  {
    Client client = new Client("localhost", 2910);
    client.run();

    // WindowManager.getInstance().getModel();
    launch(args);
  }

  @Override public void start(Stage primaryStage) throws Exception
  {
    WindowManager.getInstance().setPrimaryStage(primaryStage);
    WindowManager.getInstance().showView(ViewType.Login);
  }
}
