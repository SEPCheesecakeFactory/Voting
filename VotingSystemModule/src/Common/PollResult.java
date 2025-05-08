package Common;

import java.util.Map;

public class PollResult
{
  private Map<Integer, Integer> choiceVoters;
  public int getNumberOfVoters(int choiceID)
  {
    return choiceVoters.get(choiceID);
  }
}
