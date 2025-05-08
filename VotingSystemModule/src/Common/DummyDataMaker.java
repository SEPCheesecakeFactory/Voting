package Common;

import Server.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Random;

public class DummyDataMaker {

  // Removed static modifier to allow each instance of DummyDataMaker to have its own DatabaseConnection
  private DatabaseConnection dbc;

  public DummyDataMaker() {
    try {
      // Initialize DatabaseConnection in the constructor
      dbc = new DatabaseConnection();
    } catch (SQLException e) {
      throw new RuntimeException("Failed to initialize database connection.", e);
    }
  }

  // ====================
  //    GENERAL
  // ====================
  private static String getDummyText(String text) {
    return getDummyText(text, -1);
  }

  private static String getDummyText(String text, int seed) {
    return "Dummy " + text + (seed == -1 ? "" : " " + seed);
  }

  // ====================
  //    CHOICE OPTIONS
  // ====================
  private static String[] dummyChoiceOptions = new String[]{"yes", "no", "maybe", "probably"};

  public static ChoiceOption getDummyChoiceOptionFromSeed(int id, int seed) {
    return new ChoiceOption(id, dummyChoiceOptions[seed]);
  }

  public static ChoiceOption getDummyChoiceOption(int id) {
    return getDummyChoiceOptionFromSeed(id, (new Random()).nextInt(dummyChoiceOptions.length));
  }

  // ====================
  //    QUESTIONS
  // ====================
  public static Question getDummyQuestion(int id, int minChoiceID, int maxChoiceID) {
    ChoiceOption[] choiceOptions = new ChoiceOption[maxChoiceID - minChoiceID];
    for (int i = minChoiceID; i < maxChoiceID; i++) {
      choiceOptions[i - minChoiceID] = getDummyChoiceOptionFromSeed(i, i - minChoiceID);
    }
    return new Question(choiceOptions, id, getDummyText("Title", id), getDummyText("Description", id));
  }

  // ====================
  //    POLLS (with DB persistence)
  // ====================
  public Poll getDummyPoll(int i) throws SQLException {
    try (Connection conn = dbc.getConnection()) {
      String pollTitle = getDummyText("Title", i);
      String pollDesc = getDummyText("Description", i);

      // Insert poll and get generated id
      int pollId = insertIfNotExistsPoll(conn, pollTitle);

      // Insert questions for the poll
      Question[] questions = new Question[2];
      questions[0] = insertIfNotExistsQuestion(conn, 1, 0, 3, pollId);
      questions[1] = insertIfNotExistsQuestion(conn, 2, 4, 7, pollId);

      // Create and return the Poll object
      return new Poll(pollTitle, pollDesc, pollId, questions);
    }
  }

  private static int insertIfNotExistsPoll(Connection conn, String title) throws SQLException {
    // Check if the poll already exists
    PreparedStatement check = conn.prepareStatement("SELECT id FROM voting_system.Poll WHERE title = ?");
    check.setString(1, title);
    ResultSet rs = check.executeQuery();
    if (rs.next()) {
      return rs.getInt("id");  // Poll already exists, return its id
    }

    // If not exists, insert the poll and get its generated id
    PreparedStatement insert = conn.prepareStatement("INSERT INTO voting_system.Poll (title) VALUES (?) RETURNING id");
    insert.setString(1, title);
    rs = insert.executeQuery();
    rs.next();  // Move to the result
    return rs.getInt("id");  // Return the generated id
  }

  private static Question insertIfNotExistsQuestion(Connection conn, int id,
      int minChoiceID, int maxChoiceID, int pollId) throws SQLException {
    String title = getDummyText("Title", id);
    String desc = getDummyText("Description", id);

    // Check if the question already exists
    PreparedStatement check = conn.prepareStatement("SELECT id FROM voting_system.Question WHERE title = ?");
    check.setString(1, title);
    ResultSet rs = check.executeQuery();

    int questionId;
    if (rs.next()) {
      questionId = rs.getInt("id");
    } else {
      // If not exists, insert the question and get its generated id
      PreparedStatement insert = conn.prepareStatement(
          "INSERT INTO voting_system.Question (title, description, poll_id) VALUES (?, ?, ?) RETURNING id");
      insert.setString(1, title);
      insert.setString(2, desc);
      insert.setInt(3, pollId);
      rs = insert.executeQuery();
      rs.next();  // Move to the result
      questionId = rs.getInt("id");  // Get the generated id
    }

    // Create and insert choice options for the question
    ChoiceOption[] choices = new ChoiceOption[maxChoiceID - minChoiceID];
    for (int i = minChoiceID; i < maxChoiceID; i++) {
      choices[i - minChoiceID] = insertIfNotExistsChoiceOption(conn, i, i - minChoiceID, questionId);
    }

    return new Question(choices, questionId, title, desc);
  }

  private static ChoiceOption insertIfNotExistsChoiceOption(Connection conn,
      int id, int seed, int questionId) throws SQLException {
    String value = dummyChoiceOptions[seed];

    // Check if the choice option already exists
    PreparedStatement check = conn.prepareStatement(
        "SELECT id FROM voting_system.ChoiceOption WHERE value = ? AND question_id = ?");
    check.setString(1, value);
    check.setInt(2, questionId);
    ResultSet rs = check.executeQuery();

    int optionId;
    if (rs.next()) {
      optionId = rs.getInt("id");
    } else {
      // If not exists, insert the choice option and get its generated id
      PreparedStatement insert = conn.prepareStatement(
          "INSERT INTO voting_system.ChoiceOption (value, question_id) VALUES (?, ?) RETURNING id");
      insert.setString(1, value);
      insert.setInt(2, questionId);
      rs = insert.executeQuery();
      rs.next();  // Move to the result
      optionId = rs.getInt("id");  // Get the generated id
    }

    return new ChoiceOption(optionId, value);
  }

  // ====================
  //    VOTE
  // ====================
  public static Vote getDummyVote(int userID, int[] options) {
    return new Vote(userID, new int[]{options[(new Random()).nextInt(options.length)]});
  }
}
