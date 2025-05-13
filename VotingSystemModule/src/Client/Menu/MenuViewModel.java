package Client.Menu;

import Client.ViewType;
import Client.WindowManager;

import java.util.Arrays;

public class MenuViewModel
{
  public MenuViewModel()
  {
  }

  public void switchScreen(String option)
  {
    try
    {
      WindowManager.getInstance().showView(ViewType.valueOf(option));
    }

    catch (IllegalArgumentException e)
    {
      System.out.println("Invalid option. Please choose a valid menu option.");
    }
  }

  public String[] getOptions()
  {
    return Arrays.stream(ViewType.values())
        .map(Enum::name)
        .toArray(String[]::new);
  }
}
