package Client.PollResult;

import Common.ChoiceOption;
import Common.Poll;
import Common.PollResult;
import Common.Question;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.security.InvalidParameterException;
import java.util.Map;
import java.util.Scanner;

public class PollResultView implements PropertyChangeListener {
  private final PollResultViewModel viewModel;

  public PollResultView(PollResultViewModel viewModel) {
    this.viewModel = viewModel;
    this.viewModel.addPropertyChangeListener(this);
    displayRequestPollResult();
  }

  private void displayRequestPollResult() {
    Scanner scanner = new Scanner(System.in);
    System.out.print("Enter Poll ID to view results: ");
    int pollID = scanner.nextInt();
    viewModel.requestPollResult(pollID);
  }

  private void displayPollResult(PollResult result) {
    Poll poll = result.getPoll();

    System.out.println("\n=== Poll Results ===");
    System.out.println("Title: " + poll.getTitle());
    System.out.println("Description: " + poll.getDescription());

    Map<Integer, Integer> votes = result.getChoiceVoters();

    for (Question question : poll.getQuestions()) {
      System.out.println("Q: " + question.getTitle());
      System.out.println("Q description: " + question.getDescription());
      for (ChoiceOption option : question.getChoiceOptions()) {
        int choiceId = option.getId();
        int count = votes.getOrDefault(choiceId, 0);
        System.out.println("  - " + option.getValue() + ": " + count + " votes");
      }
    }

    System.out.println("====================\n");
  }


  @Override
  public void propertyChange(PropertyChangeEvent evt) {
    if ("PollResult".equals(evt.getPropertyName())) {
      displayPollResult((PollResult) evt.getNewValue());
    } else {
      throw new InvalidParameterException("Unknown property: " + evt.getPropertyName());
    }
  }
}
