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

  @Override
  public void storeVote(Vote vote)
  {
    try (Connection connection = openConnection())
    {


      // 1. Get poll_id using the first choice_option_id
      PreparedStatement getPollIdStmt = connection.prepareStatement(
          "SELECT q.poll_id " +
              "FROM ChoiceOption co " +
              "JOIN Question q ON co.question_id = q.id " +
              "WHERE co.id = ?");
      getPollIdStmt.setInt(1, vote.getChoices()[0]);  // assume all choices are from the same poll
      ResultSet pollIdResult = getPollIdStmt.executeQuery();

      int pollId;
      if (pollIdResult.next())
      {
        pollId = pollIdResult.getInt(1);
      }
      else
      {
        throw new RuntimeException("No poll found for the provided choice option.");
      }

      // 2. Check if poll is closed
      PreparedStatement checkPollClosedStmt = connection.prepareStatement(
          "SELECT is_closed FROM Poll WHERE id = ?");
      checkPollClosedStmt.setInt(1, pollId);
      ResultSet isClosedResult = checkPollClosedStmt.executeQuery();

      if (isClosedResult.next() && isClosedResult.getBoolean("is_closed"))
      {
        throw new RuntimeException("Poll is closed. Cannot store vote.");
      }

      // 3. Insert selected choices into VotedChoice
      for (int choiceOptionID : vote.getChoices())
      {
        PreparedStatement insertVoteStatement = connection.prepareStatement(
            "INSERT INTO VotedChoice (vote_id, choice_option_id) VALUES (?, ?)");
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

      //  Get poll_id using vote_id
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
      //  Check if poll is closed
      PreparedStatement checkPollClosedStmt = connection.prepareStatement(
          "SELECT is_closed FROM Poll WHERE id = ?");
      checkPollClosedStmt.setInt(1, pollId);
      ResultSet isClosedResult = checkPollClosedStmt.executeQuery();

      if (isClosedResult.next() && isClosedResult.getBoolean("is_closed"))
      {
        throw new RuntimeException("Poll is closed. Cannot edit vote.");
      }

      //  Get all choice_option_ids for the poll
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

      //  Get user's existing votes in this poll
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

      //  Compare and update
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
    final String SQL_POLL =
        "SELECT title, is_private, is_closed FROM Poll WHERE id = ?";
    final String SQL_QUESTIONS =
        "SELECT id, title, description FROM Question WHERE poll_id = ?";
    final String SQL_OPTIONS =
        "SELECT id, value FROM ChoiceOption WHERE question_id = ?";

    try (Connection connection = openConnection())
    {
      Poll poll = null;

      // 1. Retrieve poll
      try (PreparedStatement psPoll = connection.prepareStatement(SQL_POLL))
      {
        psPoll.setInt(1, id);
        try (ResultSet rsPoll = psPoll.executeQuery())
        {
          if (rsPoll.next())
          {
            poll = new Poll();
            poll.setId(id);
            poll.setTitle(rsPoll.getString("title"));
            poll.setPrivate(rsPoll.getBoolean("is_private"));
            poll.setClosed(rsPoll.getBoolean("is_closed"));
            poll.setQuestions(new Question[0]); // Will fill later
          }
          else
          {
            return null; // No poll with that ID
          }
        }
      }

      List<Question> questions = new ArrayList<>();

      // 2. Retrieve questions
      try (PreparedStatement psQ = connection.prepareStatement(SQL_QUESTIONS))
      {
        psQ.setInt(1, id);
        try (ResultSet rsQ = psQ.executeQuery())
        {
          while (rsQ.next())
          {
            int questionId = rsQ.getInt("id");
            String title = rsQ.getString("title");
            String description = rsQ.getString("description");

            List<ChoiceOption> options = new ArrayList<>();

            // 3. Retrieve options for each question
            try (PreparedStatement psOpt = connection.prepareStatement(SQL_OPTIONS))
            {
              psOpt.setInt(1, questionId);
              try (ResultSet rsOpt = psOpt.executeQuery())
              {
                while (rsOpt.next())
                {
                  int optId = rsOpt.getInt("id");
                  String value = rsOpt.getString("value");
                  options.add(new ChoiceOption(optId, value));
                }
              }
            }

            Question question = new Question(
                options.toArray(new ChoiceOption[0]),
                questionId,
                title,
                description
            );
            questions.add(question);
          }
        }
      }

      // Set questions on the poll
      poll.setQuestions(questions.toArray(new Question[0]));

      return poll;
    }
    catch (SQLException e)
    {
      throw new RuntimeException("Failed to retrieve poll with id " + id, e);
    }
  }

  @Override
  public PollResult retrievePollResults(int id) {
    try (Connection connection = openConnection();
        PreparedStatement selectPollResultsStatement = connection.prepareStatement(
            "SELECT co.id AS choice_id, COUNT(vc.choice_option_id) AS vote_count " +
                "FROM Poll p " +
                "JOIN Question q ON p.id = q.poll_id " +
                "JOIN ChoiceOption co ON q.id = co.question_id " +
                "LEFT JOIN VotedChoice vc ON co.id = vc.choice_option_id " +
                "WHERE p.id = ? " +
                "GROUP BY co.id, co.value")) {

      selectPollResultsStatement.setInt(1, id);
      ResultSet rsPollResults = selectPollResultsStatement.executeQuery();

      Map<Integer, Integer> choiceVoters = new HashMap<>();
      while (rsPollResults.next()) {
        int choiceId = rsPollResults.getInt("choice_id");
        int voteCount = rsPollResults.getInt("vote_count");
        choiceVoters.put(choiceId, voteCount);
      }

      return new PollResult(retrievePoll(id), choiceVoters);
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }
  //      //TODO: THIS IS A FUTURE IMPLEMENTATION ON THE CLIENT SIDE, STILL IN PROGRESS.
  //Create a map with choice option id as key and number of votes as value.
  //      for (int i = 0; i <questions.size(); i++){
  //        System.out.println("Question: " + questions.get(i));
  //        for (int j = 0; j < ; j++)
  //        {
  //          System.out.println("Choice: " + choiceValue + ", " + choiceVoters.get(value) + " votes");
  //        }
  //      }

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

  @Override public Poll storePoll(Poll poll, Profile profile)
  {
    final String SQL_INSERT_POLL = "INSERT INTO Poll(title, is_closed, is_private) VALUES (?,?,?)";
    final String SQL_INSERT_Q = "INSERT INTO Question(title, description, poll_id) VALUES (?,?,?)";
    final String SQL_INSERT_OPT = "INSERT INTO ChoiceOption(value, question_id) VALUES (?,?)";
    final String SQL_INSERT_OWNERSHIP = "INSERT INTO PollOwnership(user_id, poll_id) VALUES (?,?)";
    int pollId;
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
        pollId = rs.getInt(1);
        poll.setId(pollId);
      }

      // 2) Insert poll ownership
      try (PreparedStatement psOwn = conn.prepareStatement(SQL_INSERT_OWNERSHIP))
      {
        psOwn.setInt(1, profile.getId());  // assumes profile.getId() returns the user ID
        psOwn.setInt(2, pollId);
        psOwn.executeUpdate();
      }
      // 3) Insert each question + its options
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
    return poll;
  }
  // ===== User Group & Poll Access Methods =====
  public int createUserGroup(String groupName) {
    try (Connection connection = openConnection();
        PreparedStatement stmt = connection.prepareStatement(
            "INSERT INTO UserGroup (name) VALUES (?) RETURNING id")) {
      stmt.setString(1, groupName);
      ResultSet rs = stmt.executeQuery();
      if (rs.next()) {
        return rs.getInt(1);
      }
      throw new SQLException("Group creation failed.");
    }
    catch (SQLException e)
    {
      throw new RuntimeException(e);
    }
  }

  @Override
  public void addUserToGroup(int userId, int groupId){
    try (Connection connection = openConnection();
        PreparedStatement stmt = connection.prepareStatement(
            "INSERT INTO UserGroupMembership (user_id, group_id) VALUES (?, ?)")) {
      stmt.setInt(1, userId);
      stmt.setInt(2, groupId);
      stmt.executeUpdate();
    }
    catch (SQLException e)
    {
      throw new RuntimeException(e);
    }
  }

  public boolean userHasAccessToPoll(int userId, int pollId) {
    String sql = """
          SELECT 1
          FROM Poll p
          LEFT JOIN PollAccessControl pac ON p.id = pac.poll_id
          LEFT JOIN UserGroupMembership ugm ON pac.group_id = ugm.group_id
          WHERE p.id = ? AND (
              p.is_private = FALSE OR
              pac.user_id = ? OR
              ugm.user_id = ?
          )
          """;

    try (Connection connection = openConnection();
        PreparedStatement stmt = connection.prepareStatement(sql)) {
      stmt.setInt(1, pollId);
      stmt.setInt(2, userId);
      stmt.setInt(3, userId);
      ResultSet rs = stmt.executeQuery();
      return rs.next();
    } catch (SQLException e) {
      throw new RuntimeException("Failed to check poll access", e);
    }
  }

  public void addUserToPoll(int userId, int pollId) {
    try (Connection connection = openConnection();
        PreparedStatement stmt = connection.prepareStatement(
            "INSERT INTO PollAccessControl (poll_id, user_id) VALUES (?, ?)")) {
      stmt.setInt(1, pollId);
      stmt.setInt(2, userId);
      stmt.executeUpdate();
    }
    catch (SQLException e)
    {
      throw new RuntimeException(e);
    }
  }

  public void addGroupToPoll(int groupId, int pollId){
    try (Connection connection = openConnection();
        PreparedStatement stmt = connection.prepareStatement(
            "INSERT INTO PollAccessControl (poll_id, group_id) VALUES (?, ?)")) {
      stmt.setInt(1, pollId);
      stmt.setInt(2, groupId);
      stmt.executeUpdate();
    }
    catch (SQLException e)
    {
      throw new RuntimeException(e);
    }
  }

}
