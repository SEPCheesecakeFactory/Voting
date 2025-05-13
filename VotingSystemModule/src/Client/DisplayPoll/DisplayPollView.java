package Client.DisplayPoll;

import Common.ChoiceOption;
import Common.Poll;
import Common.Question;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.security.InvalidParameterException;
import java.util.Scanner;

public class DisplayPollView implements PropertyChangeListener
{
  private DisplayPollViewModel viewModel;

  public DisplayPollView(DisplayPollViewModel viewModel) {
    this.viewModel = viewModel;
    displayPollRequest();
    this.viewModel.addPropertyChangeListener("PollUpdated", this);

  }

  public void render(Poll poll) {
    displayPoll(poll);
  }
  public void displayPollRequest()
  {
    Scanner scanner = new Scanner(System.in);
    System.out.println("Poll id: ");
    int pollid = scanner.nextInt();
    viewModel.sendPollRequest(pollid);
  }
  public void displayPoll(Poll poll) {
    Scanner scanner = new Scanner(System.in);

    System.out.println("==== New Poll ====");
    System.out.println("Title: " + poll.getTitle());
    System.out.println("Description: " + poll.getDescription());
    System.out.println();

    int[] choices = new int[poll.getQuestions().length];

    for (int i = 0; i < poll.getQuestions().length; i++) {
      Question question = poll.getQuestions()[i];
      System.out.println("Q" + (i + 1) + ": " + question.getTitle());
      System.out.println("Description: " + question.getDescription());

      ChoiceOption[] options = question.getChoiceOptions();
      for (int j = 0; j < options.length; j++) {
        System.out.println("  " + j + ": " + options[j].getValue());
      }

      while (true) {
        System.out.print("Choose an option (index): ");
        String input = scanner.nextLine().trim();


          try {
            int answer = Integer.parseInt(input);
            if (answer >= 0 && answer < options.length) {
              choices[i] = options[answer].getId(); // valid vote
              break;
            } else {
              System.out.println("Invalid choice. Try again.");
            }
          } catch (NumberFormatException e) {
            System.out.println("Invalid input. Enter a correct index");
          }

      }
    }

    int userId = viewModel.getModel().getProfile().getId();
    System.out.print("Your User ID: " + userId + "\n");
    viewModel.sendVote(userId, choices);
  }

  @Override  public void propertyChange(PropertyChangeEvent evt)
  {
    System.out.println("??");
    switch (evt.getPropertyName()) {
      case "PollUpdated":
        System.out.println("???");
        displayPoll((Poll) evt.getNewValue());
        break;
      default:
        throw new InvalidParameterException(String.format("Event %s does not exist in the current context.", evt.getPropertyName()));
    }
  }
}