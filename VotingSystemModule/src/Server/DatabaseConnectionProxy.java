package Server;

import Common.Poll;
import Common.Vote;

public class DatabaseConnectionProxy implements DatabaseConnector
{
  private DatabaseConnection databaseConnection;
  public DatabaseConnectionProxy()
  {
    databaseConnection = new DatabaseConnection();
  }

  @Override public void storeVote(Vote vote)
  {
    databaseConnection.storeVote(vote);
    System.out.println("Vote sent to the database. {"+vote+"}");
  }

  @Override public Poll retrievePoll(int id)
  {
    Poll poll = databaseConnection.retrievePoll(id);
    System.out.println("Poll retrieved. " + poll);
    return poll;
  }
}
