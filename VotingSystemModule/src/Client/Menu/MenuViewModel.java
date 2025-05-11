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
    WindowManager.getInstance().showView(ViewType.valueOf(option));
  }

  public String[] getOptions()
  {
    return Arrays.stream(ViewType.values())
        .map(Enum::name)
        .toArray(String[]::new);
  }
}
