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

  }





  public void displayMessage(String newValue)
  {
    System.out.println(newValue);
  }

  @Override
  public void propertyChange(PropertyChangeEvent evt)
  {
    switch (evt.getPropertyName()) {
      case "NewMessage":
        displayMessage((String) evt.getNewValue());
        break;

      default:
        throw new InvalidParameterException(String.format("Event %s does not exist in the current context.", evt.getPropertyName()));
    }
  }
}
