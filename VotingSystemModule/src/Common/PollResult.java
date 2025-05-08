package Common;

import java.util.HashMap;
import java.util.Map;

public class PollResult
{
  private Map<Integer, Integer> choiceVoters;

  public PollResult(){}
  public PollResult(Map<Integer, Integer> resultsMap){
    this.choiceVoters = new HashMap<>(resultsMap);
  }
  public int getNumberOfVoters(int choiceID)
  {
    return choiceVoters.get(choiceID);
  }

  public void setChoiceVoters(Map<Integer, Integer> choiceVoters)
  {
    this.choiceVoters = choiceVoters;
  }
}
