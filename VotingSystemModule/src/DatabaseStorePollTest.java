import Common.ChoiceOption;
import Common.DummyDataMaker;
import Common.Poll;
import Common.Question;
import Server.DatabaseConnection;
import Server.DatabaseConnectionProxy;
import Server.DatabaseConnector;

import java.sql.SQLException;

public class DatabaseStorePollTest
{
  public static void main(String[] args)
  {
    // 1) build a dummy poll in memory
    Poll poll = createDummyPoll();

    // 2) open your connector and store it
    try
    {
      DatabaseConnector db = new DatabaseConnectionProxy();
      db.storePoll(poll);
      System.out.println("✅ Poll stored with generated ID: " + poll.getId());
    }
    catch (SQLException e)
    {
      System.err.println("Failed to store poll:");
      e.printStackTrace();
    }
  }

  private static Poll createDummyPoll()
  {
    // first question with three options
    ChoiceOption[] opts1 = new ChoiceOption[] {new ChoiceOption(0, "Java"),
        new ChoiceOption(0, "Python"), new ChoiceOption(0, "C++")};
    Question q1 = new Question(opts1, 0, "Which language do you prefer?",
        "Pick one of the three.");

    // second question with three options
    ChoiceOption[] opts2 = new ChoiceOption[] {new ChoiceOption(0, "Frontend"),
        new ChoiceOption(0, "Backend"), new ChoiceOption(0, "Fullstack")};
    Question q2 = new Question(opts2, 0, "Which role suits you best?",
        "Pick one role.");

    // assemble the poll
    Poll poll = new Poll();
    poll.setTitle("Developer Survey");
    poll.setDescription("A quick survey about your dev preferences");
    poll.setClosed(false);
    poll.setPrivate(false);
    poll.setQuestions(new Question[] {q1, q2});

    return poll;
  }
}
