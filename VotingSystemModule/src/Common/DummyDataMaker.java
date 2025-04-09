package Common;

import java.util.Random;

public class DummyDataMaker
{
  // ====================
  //    GENERAL
  // ====================
  private static String getDummyText(String text)
  {
    return getDummyText(text, -1);
  }
  private static String getDummyText(String text, int seed)
  {
    return "Dummy " + text + (seed==-1?"":" " + seed);
  }
  // ====================
  //    CHOICE OPTIONS
  // ====================
  private static String[] dummyChoiceOptions = new String[] {"yes", "no", "maybe", "probably"};
  public static ChoiceOption getDummyChoiceOptionFromSeed(int id, int seed)
  {
    return new ChoiceOption(id, dummyChoiceOptions[seed]);
  }
  public static ChoiceOption getDummyChoiceOption(int id)
  {
    return getDummyChoiceOptionFromSeed(id, (new Random()).nextInt(0,dummyChoiceOptions.length)); // !WARNING: length hardcoded regardless of functionality
  }
  // ====================
  //    QUESTIONS
  // ====================
  public static Question getDummyQuestion(int id, int minChoiceID, int maxChoiceID)
  {
    ChoiceOption[] choiceOptions = new ChoiceOption[]{};
    for(int i = minChoiceID; i < maxChoiceID; i++)
      getDummyChoiceOptionFromSeed(i, i-minChoiceID);
    return new Question(choiceOptions, id, getDummyText("Title", id), getDummyText("Description", id));
  }
  // ====================
  //    POLLS
  // ====================
  public static Poll getDummyPoll(int id) // TODO: remake into multiple questions like the getDummyQuestion min max
  {
    Question[] questions = new Question[2];
    questions[0] = getDummyQuestion(1, 0,3);
    questions[1] = getDummyQuestion(2, 4,7);
    Poll poll = new Poll(getDummyText("Title", id),getDummyText("Description", id),1,questions);
    return poll;
  }
  // ====================
  //    VOTE
  // ====================
  public static Vote getDummyVote(int userID, int[] options)
  {
    Vote vote = new Vote(userID, new int[]{options[(new Random()).nextInt(options.length)]});
    return vote;
  }
}
