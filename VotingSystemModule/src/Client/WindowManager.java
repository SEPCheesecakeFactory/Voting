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
      case ViewType.CreatePoll:
        CreatePollViewModel createPollViewModel = new CreatePollViewModel(getModel());
        CreatePollView createPollView = new CreatePollView(createPollViewModel);
        createPollView.render();
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
