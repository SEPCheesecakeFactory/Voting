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
      Vote vote = DummyDataMaker.getDummyVote(0,new int[]{0,1,2,3});
      System.out.println("Sending vote " + vote);
      clientConnection.sendVote(vote);
    }
    catch (IOException e)
    {
      throw new RuntimeException(e);
    }
  }
}
