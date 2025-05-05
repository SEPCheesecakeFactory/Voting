package Client;
//Socket 2 - Michael
import java.io.IOException;
import java.net.Socket;
import java.util.Scanner;

public class Client
{
  public static void main(String[] args)
  {
    try
    {
      Socket socket = new Socket("localhost", 2910);
      ClientConnection clientConnection = new ClientConnection(socket);
      Model model = new Model(clientConnection);
      ClientViewModel viewModel = new ClientViewModel(model);
      ClientView view = new ClientView(viewModel);
      clientConnection.setModel(model);

      new Thread(clientConnection).start();

//      while(true)
//      {
//        System.out.println("Enter a message: ");
//        String stringToSend = scanner.nextLine();
//        clientConnection.send(stringToSend);
//      }
    }
    catch (IOException e)
    {
      throw new RuntimeException(e);
    }
  }
}