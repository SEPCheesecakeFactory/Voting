package Client;

import Common.Poll;
import Common.Question;
import Common.ChoiceOption;
import Utils.Logger;

import java.util.Scanner;

public class ClientView {
  private final ClientViewModel viewModel;

  public ClientView(ClientViewModel viewModel) throws InterruptedException
  {
    this.viewModel = viewModel;
    this.viewModel.setView(this);
    displayLoginView();

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


    int userId = viewModel.getModel().getProfile().getId();
    System.out.print("your User ID: "+userId);
    viewModel.sendVote(userId, choices);
  }
  public void displayLoginView() {
    Scanner scanner = new Scanner(System.in);
    System.out.print("Login or register - enter your username: ");
    String username = scanner.nextLine();
    viewModel.loginOrRegister(username);
  }
  public void displayChangeUsername()
  {
    Scanner scanner = new Scanner(System.in);
    System.out.print("Change username - enter new one: ");
    String username = scanner.nextLine();
    viewModel.changeUserName(username);
  }

  public void displayMessage(String newValue)
  {
    System.out.println(newValue);
  }
}
