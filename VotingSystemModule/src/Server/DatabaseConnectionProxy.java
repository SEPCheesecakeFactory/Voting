package Server;

import Common.Poll;
import Common.Profile;
import Common.Vote;

import java.sql.SQLException;

public class DatabaseConnectionProxy implements DatabaseConnector
{
  private DatabaseConnection databaseConnection;
  public DatabaseConnectionProxy() throws SQLException
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

  @Override public int loginOrRegisterAProfile(Profile profile)
  {
    System.out.println("user "+profile.getUsername()+" logged or registered");
    return databaseConnection.loginOrRegisterAProfile(profile);


  }

  @Override public void changeUsername(Profile profile)
  {

    databaseConnection.changeUsername(profile);
    System.out.println("Username changed");
  }

}
