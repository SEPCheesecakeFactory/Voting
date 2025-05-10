package Client;

import Client.ChangeUsername.ChangeUsernameView;
import Client.ChangeUsername.ChangeUsernameViewModel;
import Client.ClosePoll.ClosePollView;
import Client.ClosePoll.ClosePollViewModel;
import Client.CreatePoll.CreatePollView;
import Client.CreatePoll.CreatePollViewModel;
import Client.DisplayPoll.DisplayPollView;
import Client.DisplayPoll.DisplayPollViewModel;
import Client.Login.LoginView;
import Client.Login.LoginViewModel;
import Client.Menu.MenuView;
import Client.Menu.MenuViewModel;
import Client.PollResult.PollResultView;
import Client.PollResult.PollResultViewModel;
import Client.Test.TestView;
import Client.Test.TestViewModel;

public class WindowManager
{
  private volatile static WindowManager instance;
  private Model model;

  private WindowManager() { }

  public static WindowManager getInstance()
  {
    if (instance == null)
    {
      instance = new WindowManager();
    }
    return instance;
  }

  // TODO: removing the old one could be implemented
  public void showView(ViewType type)
  {
    switch (type)
    {
      case ViewType.Menu:
        MenuViewModel menuVM = new MenuViewModel();
        MenuView menuV = new MenuView(menuVM);
        break;
      case ViewType.PollResult:
        PollResultViewModel pollResultVM = new PollResultViewModel(getModel());
        PollResultView pollResultV = new PollResultView(pollResultVM);
        break;
      case ViewType.CreatePoll:
        CreatePollViewModel createPollViewModel = new CreatePollViewModel(
            getModel());
        CreatePollView createPollView = new CreatePollView(createPollViewModel);
        createPollView.render();
      case ViewType.DisplayPoll:
        DisplayPollViewModel displayPollVM = new DisplayPollViewModel(
            getModel());
        DisplayPollView displayPollV = new DisplayPollView(displayPollVM);
        break;
      case Login:
        LoginViewModel loginVM = new LoginViewModel(getModel());
        LoginView loginV = new LoginView(loginVM);
        break;
      case ChangeUsername:
        ChangeUsernameViewModel changeUsernameVM = new ChangeUsernameViewModel(
            getModel());
        ChangeUsernameView changeUsernameV = new ChangeUsernameView(
            changeUsernameVM);
        break;
      case ClosePoll:
        ClosePollViewModel closePollVM = new ClosePollViewModel(getModel());
        ClosePollView closePollV = new ClosePollView(closePollVM);
        closePollV.render();
        break;
      case Test:
        TestViewModel testVM = new TestViewModel();
        TestView testV = new TestView(testVM);
        testV.render();
        break;
    }
    WindowManager.getInstance().showView(ViewType.Menu);
  }

  public void setModel(Model model)
  {
    this.model = model;
  }

  public Model getModel()
  {
    if (model == null)
      model = new Model(getClient());
    return model;
  }

  public Client getClient()
  {
    if(getModel() != null)
      return getModel().getClient();
    else
      return null;
  }
}
