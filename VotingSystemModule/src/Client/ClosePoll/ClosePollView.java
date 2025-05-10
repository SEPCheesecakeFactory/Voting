package Client.ClosePoll;

import java.util.Scanner;

public class ClosePollView
{
  private ClosePollViewModel viewModel;

  public ClosePollView(ClosePollViewModel viewModel)
  {
    this.viewModel = viewModel;
  }

  public void render()
  {
    System.out.println("Poll ID for the poll to be closed:");
    Scanner scanner = new Scanner(System.in);
    int pollId = scanner.nextInt();
    viewModel.closePoll(pollId);
  }

}