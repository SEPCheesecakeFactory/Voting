package Client;

import Common.ChoiceOption;
import Common.Poll;
import Common.Question;

import java.util.Scanner;

public class ClosePollView {
  private ClosePollViewModel viewModel;

  public ClosePollView(ClosePollViewModel viewModel) {
    this.viewModel = viewModel;
  }

  public void render(Poll poll) {
   closePoll(poll);
  }
  public void closePoll(Poll poll) {
    Scanner scanner = new Scanner(System.in);


        System.out.print("write 'close_poll' to close the poll with id: "+poll.getId());
        String input = scanner.nextLine().trim();

        if (input.equals("close_poll")) {
          try {
            // int pollId = Integer.parseInt(input.split(":")[1]);
            viewModel.closePoll(poll); // use the current poll instance
            return; // exit after closing poll
          } catch (Exception e) {
            System.out.println("Invalid close_poll format. Use close_poll");
          }
        }
        }





}