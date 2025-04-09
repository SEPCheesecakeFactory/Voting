package Server;

import Common.Poll;
import Common.Vote;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class DatabaseConnection implements DatabaseConnector
{
  public DatabaseConnection()
  {
    DriverManager.registerDriver(new org.postgresql.Driver());
  }

  private Connection openConnection() throws SQLException
  {
    return DriverManager.getConnection("jdbc:posgresql://localhost:5432/postgres","postgres", "password"); // IMPORTANT: I guess we will all have to set it to our password when trying it out unless we get a proper server
  }

  @Override public void storeVote(Vote vote)
  {
    try(Connection connection = openConnection())
    {
      for(int choiceOptionID : vote.getChoices())
      {
        // PreparedStatement insertVoteStatement = connection.prepareStatement("INSERT INTO VotedChoice VALUES ("+vote.getUserId()+","+choiceOptionID+
        PreparedStatement insertVoteStatement = connection.prepareStatement("INSERT INTO VotedChoice VALUES (?,?)");
        // TODO: check if the indexing is really from 1 (seems sus)
        insertVoteStatement.setInt(1,vote.getUserId());
        insertVoteStatement.setInt(2,choiceOptionID);
        insertVoteStatement.executeUpdate();
      }
    }
    catch (SQLException e)
    {
      throw new RuntimeException(e);
    }
  }

  @Override public Poll retrievePoll(int id)
  {
    try(Connection connection = openConnection())
    {
      // ==== Example From - Database Programming in Java - part 2 ====
      // PreparedStatement selectStatement = connection.prepareStatement("SELECT name FROM Author");
      // Result rs = selectStatement.executeQuery();
      // ... work with the result ...
      // while (rs.next()) { rs.getString("name")); }

    }
    catch (SQLException e)
    {
      throw new RuntimeException(e);
    }
    return null;
  }
}
