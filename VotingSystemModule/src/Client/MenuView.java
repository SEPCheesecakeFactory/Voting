package Client;

import java.util.Scanner;

public class MenuView
{
  private MenuViewModel viewModel;

  public MenuView(MenuViewModel viewModel)
  {
    this.viewModel = viewModel;
    displayMenu();
  }

  private void displayMenu()
  {
    var options = viewModel.getOptions();
    System.out.println("== Welcome to the menu, choose a screen to display ==");
    for(var option : options)
    {
      System.out.println(String.format("-> %s", option));
    }
    System.out.println();
    var scanner = new Scanner(System.in);
    String answer = scanner.next();
    viewModel.switchScreen(answer);
  }
}
