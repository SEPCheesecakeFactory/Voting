package Server;

import Common.*;
import Utils.Logger;

import java.sql.*;
import java.util.*;

public class DatabaseConnection implements DatabaseConnector
{
  public DatabaseConnection() throws SQLException
  {
    DriverManager.registerDriver(new org.postgresql.Driver());
  }

  private Connection openConnection() throws SQLException
  {
    return DriverManager.getConnection(
        "jdbc:postgresql://localhost:5432/postgres?currentSchema=voting_system",
        "postgres",
        "password"); // IMPORTANT: I guess we will all have to set it to our password when trying it out unless we get a proper server
  }

  @Override public void storeVote(Vote vote)
  {
    try (Connection connection = openConnection())
    {
      for (int choiceOptionID : vote.getChoices())
      {
        PreparedStatement insertVoteStatement = connection.prepareStatement(
            "INSERT INTO votedchoice VALUES (?,?)");
        insertVoteStatement.setInt(1, vote.getUserId());
        insertVoteStatement.setInt(2, choiceOptionID);
        insertVoteStatement.executeUpdate();
      }
    }
    catch (SQLException e)
    {
      throw new RuntimeException(e);
    }
  }

  @Override public void editVote(Vote vote)
  {
    try (Connection connection = openConnection())
    {

      // 1. Get poll_id using vote_id
      PreparedStatement getPollIdStmt = connection.prepareStatement(
          "SELECT DISTINCT q.poll_id " + "FROM VotedChoice vc "
              + "JOIN ChoiceOption co ON vc.choice_option_id = co.id "
              + "JOIN Question q ON co.question_id = q.id "
              + "WHERE vc.vote_id = ?");
      getPollIdStmt.setInt(1, vote.getUserId());
      ResultSet pollIdResult = getPollIdStmt.executeQuery();

      int pollId;
      if (pollIdResult.next())
      {
        pollId = pollIdResult.getInt(1);
      }
      else
      {
        throw new RuntimeException("No poll found for the user's vote.");
      }

      // 2. Get all choice_option_ids for the poll
      PreparedStatement getPollChoices = connection.prepareStatement(
          "SELECT co.id FROM ChoiceOption co, Question q "
              + "WHERE co.question_id = q.id AND q.poll_id = ?");
      getPollChoices.setInt(1, pollId);
      ResultSet pollChoicesResult = getPollChoices.executeQuery();

      List<Integer> pollChoiceIds = new ArrayList<>();
      while (pollChoicesResult.next())
      {
        pollChoiceIds.add(pollChoicesResult.getInt(1));
      }

      // 3. Get user's existing votes in this poll
      PreparedStatement getUserVotes = connection.prepareStatement(
          "SELECT choice_option_id FROM VotedChoice " + "WHERE vote_id = ?");
      getUserVotes.setInt(1, vote.getUserId());
      ResultSet userVotesResult = getUserVotes.executeQuery();

      List<Integer> existingChoiceOptionsIds = new ArrayList<>();
      while (userVotesResult.next())
      {
        int choiceOptionId = userVotesResult.getInt(1);
        if (pollChoiceIds.contains(choiceOptionId))
        {
          existingChoiceOptionsIds.add(choiceOptionId);
        }
      }

      // 4. Compare and update
      List<Integer> newChoiceOptionsIds = new ArrayList<>();
      for (int choice : vote.getChoices())
      {
        newChoiceOptionsIds.add(choice);
      }

      List<Integer> toAdd = new ArrayList<>(newChoiceOptionsIds);
      toAdd.removeAll(existingChoiceOptionsIds);

      List<Integer> toRemove = new ArrayList<>(existingChoiceOptionsIds);
      toRemove.removeAll(newChoiceOptionsIds);

      for (int choiceId : toRemove)
      {
        PreparedStatement delete = connection.prepareStatement(
            "DELETE FROM VotedChoice WHERE vote_id = ? AND choice_option_id = ?");
        delete.setInt(1, vote.getUserId());
        delete.setInt(2, choiceId);
        delete.executeUpdate();
      }

      for (int choiceId : toAdd)
      {
        PreparedStatement insert = connection.prepareStatement(
            "INSERT INTO VotedChoice (vote_id, choice_option_id) VALUES (?, ?)");
        insert.setInt(1, vote.getUserId());
        insert.setInt(2, choiceId);
        insert.executeUpdate();
      }

    }
    catch (SQLException e)
    {
      throw new RuntimeException(e);
    }
  }

  @Override public Poll retrievePoll(int id)
  {
    try (Connection connection = openConnection())
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

  @Override public PollResult retrievePollResults(int id)
  {
    try (Connection connection = openConnection())
    {
      //Get the ChoiceOption
      PreparedStatement selectChoiceOptionStatement = connection.prepareStatement(
          "SELECT id FROM ChoiceOption WHERE question_id = ("
              + "SELECT id FROM Question WHERE poll_id = ?))");
      selectChoiceOptionStatement.setInt(1, id);
      ResultSet rsChoiceOption = selectChoiceOptionStatement.executeQuery();

      //Get the count of that choice option
      PreparedStatement selectVotedChoiceCountStatement = connection.prepareStatement(
          "SELECT COUNT(*) as count FROM VotedChoice WHERE choice_option_id = ?");

      Map<Integer, Integer> choiceVoters = new HashMap<>();
      while (rsChoiceOption.next())
      {//While there are more choice options
        selectVotedChoiceCountStatement.setInt(1,
            rsChoiceOption.getInt("id")); //Updating the id to the new question
        ResultSet rsVotedChoiceCount = selectVotedChoiceCountStatement.executeQuery(); //Executing

        if (rsVotedChoiceCount.next())
        { //If there is a count
          //Save question id and count
          choiceVoters.put(rsChoiceOption.getInt("id"),
              rsVotedChoiceCount.getInt("count")); //Getting and saving results
        }
      }
      return new PollResult(retrievePoll(id),
          choiceVoters); //For now we are retrieving the poll
    }
    catch (SQLException e)
    {
      throw new RuntimeException(e);
    }
  }

  @Override public int loginOrRegisterAProfile(Profile profile)
  {
    try (Connection conn = openConnection())
    {
      // Check if the user already exists
      String query = "SELECT id FROM users WHERE username = ?";
      PreparedStatement checkStmt = conn.prepareStatement(query);
      checkStmt.setString(1, profile.getUsername());
      ResultSet rs = checkStmt.executeQuery();

      if (rs.next())
      {
        // User exists, return their ID

        return rs.getInt("id");
      }
      else
      {
        // User doesn't exist, insert them into the database
        String insertQuery = "INSERT INTO users (username) VALUES (?) RETURNING id";
        PreparedStatement insertStmt = conn.prepareStatement(insertQuery);
        insertStmt.setString(1, profile.getUsername());
        rs = insertStmt.executeQuery();
        if (rs.next())
        {
          // Return the generated ID after insertion

          return rs.getInt("id");
        }
        else
        {
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
    try (Connection conn = openConnection())
    {

      String insertQuery = "UPDATE users SET username = ? WHERE id = ?;";
      PreparedStatement insertStmt = conn.prepareStatement(insertQuery);
      insertStmt.setString(1, profile.getUsername());
      insertStmt.setInt(2, profile.getId());
      Logger.log("Updating username to " + profile.getUsername() + " for ID "
          + profile.getId());
      insertStmt.executeUpdate();

    }

    catch (SQLException e)
    {
      throw new RuntimeException(e);
    }
  }

  public Connection getConnection() throws SQLException
  {
    return openConnection();
  }

  public boolean isOwner(int userId, int pollId) {
    try (Connection conn = openConnection()) {
      PreparedStatement stmt = conn.prepareStatement(
          "SELECT 1 FROM pollownership WHERE user_id = ? AND poll_id = ?"
      );
      stmt.setInt(1, userId);
      stmt.setInt(2, pollId);
      ResultSet rs = stmt.executeQuery();
      return rs.next(); // true if record exists
    } catch (SQLException e) {
      throw new RuntimeException("Failed to verify ownership", e);
    }
  }


  public void closePollAndSaveResults(int pollId)
  {
    String sql = "UPDATE poll SET is_closed = TRUE WHERE id = ?";

    try (Connection conn = openConnection();
        PreparedStatement stmt = conn.prepareStatement(sql))
    {
      stmt.setInt(1, pollId);
      stmt.executeUpdate();
      Logger.log("Poll with ID " + pollId + " marked as closed.");
    }
    catch (SQLException e)
    {
      Logger.log(
          "Failed to close poll with ID " + pollId + ": " + e.getMessage());
      throw new RuntimeException(e);
    }
  }

  @Override public void storePoll(Poll poll)
  {
    final String SQL_INSERT_POLL = "INSERT INTO Poll(title, is_closed, is_private) VALUES (?,?,?)";
    final String SQL_INSERT_Q = "INSERT INTO Question(title, description, poll_id) VALUES (?,?,?)";
    final String SQL_INSERT_OPT = "INSERT INTO ChoiceOption(value, question_id) VALUES (?,?)";

    Connection conn = null;
    try
    {
      conn = openConnection();
      conn.setAutoCommit(false);

      // 1) Insert poll
      try (PreparedStatement ps = conn.prepareStatement(SQL_INSERT_POLL,
          Statement.RETURN_GENERATED_KEYS))
      {
        ps.setString(1, poll.getTitle());
        ps.setBoolean(2, poll.isPrivate());
        ps.setBoolean(3, poll.isClosed());
        ps.executeUpdate();

        ResultSet rs = ps.getGeneratedKeys();
        if (!rs.next())
        {
          throw new SQLException("Failed to retrieve poll ID.");
        }
        int pollId = rs.getInt(1);
        poll.setId(pollId);
      }

      // 2) Insert each question + its options
      for (Question q : poll.getQuestions())
      {
        int questionId;
        try (PreparedStatement psQ = conn.prepareStatement(SQL_INSERT_Q,
            Statement.RETURN_GENERATED_KEYS))
        {
          psQ.setString(1, q.getTitle());
          psQ.setString(2, q.getDescription());
          psQ.setInt(3, poll.getId());
          psQ.executeUpdate();

          ResultSet rsQ = psQ.getGeneratedKeys();
          if (!rsQ.next())
          {
            throw new SQLException("Failed to retrieve question ID.");
          }
          questionId = rsQ.getInt(1);
          q.setId(questionId);
        }

        for (ChoiceOption opt : q.getChoiceOptions())
        {
          try (PreparedStatement psO = conn.prepareStatement(SQL_INSERT_OPT,
              Statement.RETURN_GENERATED_KEYS))
          {
            psO.setString(1, opt.getValue());
            psO.setInt(2, questionId);
            psO.executeUpdate();

            ResultSet rsO = psO.getGeneratedKeys();
            if (rsO.next())
            {
              opt.setId(rsO.getInt(1));
            }
          }
        }
      }

      conn.commit();
    }
    catch (SQLException ex)
    {
      if (conn != null)
      {
        try
        {
          conn.rollback();
        }
        catch (SQLException rbEx)
        {
          Logger.log("Rollback failed: " + rbEx.getMessage());
        }
      }
      Logger.log("Failed to store poll: " + ex.getMessage());
      throw new RuntimeException(ex);
    }
    finally
    {
      if (conn != null)
      {
        try
        {
          conn.close();
        }
        catch (SQLException closEx)
        {
          Logger.log("Closing connection failed: " + closEx.getMessage());
        }
      }
    }
  }
}
