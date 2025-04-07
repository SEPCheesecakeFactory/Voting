package Server;
//Sockets 2 michael
import Common.ChoiceOption;
import Common.Poll;
import Common.Question;
import Common.Vote;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class ServerConnection implements Runnable
{
  private final ObjectInputStream inFromClient;
  private final ObjectOutputStream outToClient;
  private final ConnectionPool connectionPool;

  public ServerConnection(Socket connectionSocket, ConnectionPool connectionPool) throws IOException
  {
    inFromClient = new ObjectInputStream(connectionSocket.getInputStream());
    outToClient = new ObjectOutputStream(connectionSocket.getOutputStream());
    this.connectionPool = connectionPool;
  }

  @Override
  public void run()
  {
    ChoiceOption[] choiceOptions = new ChoiceOption[4];

    choiceOptions[0] = new ChoiceOption(1, "yes");
    choiceOptions[1] = new ChoiceOption(2, "no");
    choiceOptions[2] = new ChoiceOption(3, "yes");
    choiceOptions[3] = new ChoiceOption(4, "no");
    Question[] questions = new Question[2];
    questions[0]=new Question(choiceOptions, 1, "title", "description");
    questions[1]=new Question(choiceOptions, 2, "title2", "description2");
    Poll poll = new Poll("title","description",1,questions);
    try{
      sendPoll(poll);
      Vote vote = (Vote) inFromClient.readObject();
      System.out.println(vote);
    }
    catch (IOException | ClassNotFoundException e)
    {
      throw new RuntimeException(e);
    }

//    while(true)
//    {
//      try
//      {
//        String message = (String) inFromClient.readObject();
//        System.out.println("Received: " + message);
//        connectionPool.broadcast(message);
//      }
//      catch (IOException | ClassNotFoundException e)
//      {
//        throw new RuntimeException(e);
//      }
//    }
  }

//  public void send(String message) throws IOException
//  {
//    outToClient.writeObject(message);
//  }
public void sendPoll(Poll poll) throws IOException
    {
      outToClient.writeObject(poll);
    }
}