package Client.ClosePoll;

import Client.Model;

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
