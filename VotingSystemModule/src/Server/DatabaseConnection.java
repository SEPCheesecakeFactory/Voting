package Server;

import Common.Poll;
import Common.Profile;
import Common.Vote;
import Utils.Logger;

import java.sql.*;

public class DatabaseConnection implements DatabaseConnector
{
  public DatabaseConnection() throws SQLException
  {
    DriverManager.registerDriver(new org.postgresql.Driver());
  }

  private Connection openConnection() throws SQLException
  {
    return DriverManager.getConnection("jdbc:postgresql://localhost:5432/postgres?currentSchema=voting_system","postgres", "password"); // IMPORTANT: I guess we will all have to set it to our password when trying it out unless we get a proper server
  }

  @Override public void storeVote(Vote vote)
  {
    try(Connection connection = openConnection())
    {
      for(int choiceOptionID : vote.getChoices())
      {
        PreparedStatement insertVoteStatement = connection.prepareStatement("INSERT INTO votedchoice VALUES (?,?)");
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

  @Override public int loginOrRegisterAProfile(Profile profile)
  {
    try (Connection conn = openConnection()) {
      // Check if the user already exists
      String query = "SELECT id FROM users WHERE username = ?";
      PreparedStatement checkStmt = conn.prepareStatement(query);
      checkStmt.setString(1, profile.getUsername());
      ResultSet rs = checkStmt.executeQuery();

      if (rs.next()) {
        // User exists, return their ID

        return rs.getInt("id");
      } else {
        // User doesn't exist, insert them into the database
        String insertQuery = "INSERT INTO users (username) VALUES (?) RETURNING id";
        PreparedStatement insertStmt = conn.prepareStatement(insertQuery);
        insertStmt.setString(1, profile.getUsername());
        rs = insertStmt.executeQuery();
        if (rs.next()) {
          // Return the generated ID after insertion

          return rs.getInt("id");
        } else {
          throw new SQLException("Failed to insert user.");
        }
      }
    }
    catch (SQLException e)
    {
      throw new RuntimeException(e);
    }
  }

  @Override public void changeUsername(Profile profile)
  {
    try (Connection conn = openConnection()) {


        String insertQuery = "UPDATE users SET username = ? WHERE id = ?;";
        PreparedStatement insertStmt = conn.prepareStatement(insertQuery);
        insertStmt.setString(1, profile.getUsername());
        insertStmt.setInt(2, profile.getId());
        Logger.log("Updating username to " + profile.getUsername() + " for ID " + profile.getId());
        insertStmt.executeUpdate();

      }

    catch (SQLException e)
    {
      throw new RuntimeException(e);
    }
  }

  public Connection getConnection() throws SQLException {
    return openConnection();
  }

  public void closePollAndSaveResults(int pollId)
  {
    String sql = "UPDATE polls SET is_closed = TRUE WHERE id = ?";

    try (Connection conn = openConnection();
        PreparedStatement stmt = conn.prepareStatement(sql))
    {
      stmt.setInt(1, pollId);
      stmt.executeUpdate();
      Logger.log("Poll with ID " + pollId + " marked as closed.");
    }
    catch (SQLException e)
    {
      Logger.log("Failed to close poll with ID " + pollId + ": " + e.getMessage());
      throw new RuntimeException(e);
    }
  }


}
