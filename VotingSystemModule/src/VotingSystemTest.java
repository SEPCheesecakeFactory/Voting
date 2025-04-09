import Client.ClientConnection;
import Common.DummyDataMaker;
import Common.Vote;

import java.io.IOException;
import java.net.Socket;
import java.util.Scanner;

public class VotingSystemTest
{
  public static void main(String[] args)
  {
    Scanner scanner = new Scanner(System.in);

    try
    {
      Socket socket = new Socket("localhost", 2910);
      ClientConnection clientConnection = new ClientConnection(socket);
      // === INPUTTING ===
      System.out.println("Input userID:");
      int userID = scanner.nextInt();
      System.out.println("Input choiceID:");
      int choiceID = scanner.nextInt();
      Vote vote = new Vote(userID, new int[]{choiceID});
      // ===
      System.out.println("Sending vote " + vote);
      clientConnection.sendVote(vote);
    }
    catch (IOException e)
    {
      throw new RuntimeException(e);
    }
  }
}
