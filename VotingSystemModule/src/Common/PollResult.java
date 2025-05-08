package Common;

import java.util.Map;

public class PollResult
{
  private Map<Integer, Integer> choiceVoters;
  private Poll poll;
  public PollResult(Poll poll, Map<Integer, Integer> choiceVoters)
  {
    this.choiceVoters = choiceVoters;
    this.poll=poll;
  }
  public int getNumberOfVoters(int choiceID)
  {
    return choiceVoters.get(choiceID);
  }

  public Map<Integer, Integer> getChoiceVoters()
  {
    return choiceVoters;
  }

  public Poll getPoll()
  {
    return poll;
  }
}
