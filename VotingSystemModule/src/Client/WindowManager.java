package Client;

public class WindowManager
{
  private volatile static WindowManager instance;
  private Model model;
  private ClientConnection clientConnection;

  private WindowManager()
  {

  }

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
      case ViewType.General:
        ClientViewModel generalVM = new ClientViewModel(getModel());
        ClientView generalV = new ClientView(generalVM);
        break;
      case ViewType.PollResult:
        PollResultViewModel pollResultVM = new PollResultViewModel(getModel());
        PollResultView pollResultV = new PollResultView(pollResultVM);
        break;
      case ViewType.DisplayPoll:
        DisplayPollViewModel displayPollVM = new DisplayPollViewModel(getModel());
        DisplayPollView displayPollV = new DisplayPollView(displayPollVM);
        break;
      case Login:
        LoginViewModel loginVM = new LoginViewModel(getModel());
        LoginView loginV = new LoginView(loginVM);
        break;
      case ChangeUsername:
        ChangeUsernameViewModel changeUsernameVM = new ChangeUsernameViewModel(getModel());
        ChangeUsernameView changeUsernameV = new ChangeUsernameView(changeUsernameVM);
        break;
      case ClosePoll:
        ClosePollViewModel closePollVM = new ClosePollViewModel(getModel());
        ClosePollView closePollV = new ClosePollView(closePollVM);
        break;
    }
  }

  public Model getModel()
  {
    if (model == null)
      model = new Model(getClientConnection());
    return model;
  }

  public ClientConnection getClientConnection()
  {
    return clientConnection;
  }

  public void setClientConnection(ClientConnection clientConnection)
  {
    this.clientConnection = clientConnection;
  }
}
