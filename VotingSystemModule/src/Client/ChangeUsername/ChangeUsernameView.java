package Client.ChangeUsername;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.security.InvalidParameterException;
import java.util.Scanner;

public class ChangeUsernameView implements PropertyChangeListener
{
  private ChangeUsernameViewModel viewModel;

  public ChangeUsernameView(ChangeUsernameViewModel viewModel) {
    this.viewModel = viewModel;
    viewModel.addPropertyChangeListener(this);
    render();
  }

  public void render() {
    displayChangeUsername();
  }
  public void displayChangeUsername()
  {
    Scanner scanner = new Scanner(System.in);
    System.out.print("Change username - enter new one: ");
    String username = scanner.nextLine();
    viewModel.changeUserName(username);
  }

  @Override
  public void propertyChange(PropertyChangeEvent evt)
  {
    switch (evt.getPropertyName()) {
      case "ProfileSet":
        displayChangeUsername();
        break;
      default:
        throw new InvalidParameterException(String.format("Event %s does not exist in the current context.", evt.getPropertyName()));
    }
  }

}