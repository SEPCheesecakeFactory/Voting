package Client;

import Common.Poll;
import Common.Question;
import Common.ChoiceOption;

import java.util.Scanner;

public class ClientView {
  private final ClientViewModel viewModel;

  public ClientView(ClientViewModel viewModel) {
    this.viewModel = viewModel;
    this.viewModel.setView(this);
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

      int answer;
      do {
        System.out.print("Choose an option (index): ");
        answer = scanner.nextInt();
      } while (answer < 0 || answer >= options.length);

      choices[i] = options[answer].getId(); // Store selected option's ID
    }

    System.out.print("Enter your User ID: ");
    int userId = scanner.nextInt();

    viewModel.sendVote(userId, choices);
  }
}
