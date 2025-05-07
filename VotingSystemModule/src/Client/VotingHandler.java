package Client;

import Common.Poll;

public interface VotingHandler
{
  void onPollUpdated(Poll poll);
}