package Client;

import Common.Poll;
import Common.Question;
import Common.ChoiceOption;
import Utils.Logger;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.security.InvalidParameterException;
import java.util.Scanner;

public class ClientView implements PropertyChangeListener
{
  private final ClientViewModel viewModel;

  public ClientView(ClientViewModel viewModel)
  {
    this.viewModel = viewModel;
    this.viewModel.addPropertyChangeListener(this);
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

      while (true) {
        System.out.print("Choose an option (index or 'close_poll'): ");
        String input = scanner.nextLine().trim();

        if (input.startsWith("close_poll")) {
          try {
           // int pollId = Integer.parseInt(input.split(":")[1]);
            viewModel.closePoll(poll); // use the current poll instance
            return; // exit after closing poll
          } catch (Exception e) {
            System.out.println("Invalid close_poll format. Use close_poll");
          }
        } else {
          try {
            int answer = Integer.parseInt(input);
            if (answer >= 0 && answer < options.length) {
              choices[i] = options[answer].getId(); // valid vote
              break;
            } else {
              System.out.println("Invalid choice. Try again.");
            }
          } catch (NumberFormatException e) {
            System.out.println("Invalid input. Enter an index or 'close_poll:<id>'");
          }
        }
      }
    }

    int userId = viewModel.getModel().getProfile().getId();
    System.out.print("Your User ID: " + userId + "\n");
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

  @Override
  public void propertyChange(PropertyChangeEvent evt)
  {
    switch (evt.getPropertyName()) {
      case "PollUpdated":
        displayPoll((Poll) evt.getNewValue());
        break;
      case "NewMessage":
        displayMessage((String) evt.getNewValue());
        break;
      case "ProfileSet":
        displayChangeUsername();
        break;
      default:
        throw new InvalidParameterException(String.format("Event %s does not exist in the current context.", evt.getPropertyName()));
    }
  }
}
