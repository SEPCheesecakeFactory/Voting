import Client.Client;

public class MainAppRunner
{
  public static void main(String[] args)
  {
    Client client = new Client("localhost", 2910);
    client.run();
  }
}
