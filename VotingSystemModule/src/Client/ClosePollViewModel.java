package Client;

import Common.Poll;
import Utils.Logger;



public class ClosePollViewModel {
  private Model model;


  public ClosePollViewModel(Model model) {
    this.model = model;
  }

  public void closePoll(Poll poll)
  {
    if (poll == null)
      Logger.log("Poll is null, cannot close.");

    if (!poll.isClosed())
    {
      poll.closePoll();
      model.sendPollCloseRequest(poll.getId());

      Logger.log("Poll closed and final result sent.");
    }
    else
      Logger.log("Poll is already closed.");
  }
}
