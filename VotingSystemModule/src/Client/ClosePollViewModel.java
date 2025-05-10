package Client;

import Common.Poll;
import Utils.Logger;

public class ClosePollViewModel
{
  private Model model;

  public ClosePollViewModel(Model model)
  {
    this.model = model;
  }

  public void closePoll(int pollId)
  {
    model.sendPollCloseRequest(pollId);
  }
}
