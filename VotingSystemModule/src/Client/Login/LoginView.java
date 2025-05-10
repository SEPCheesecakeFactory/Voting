package Client.Login;

import java.beans.PropertyChangeEvent;
import java.util.Scanner;

public class LoginView {
  private LoginViewModel viewModel;

  public LoginView(LoginViewModel viewModel) {
    this.viewModel = viewModel;
    render();
  }

  public void render() {
    displayLoginView();
  }
  public void displayLoginView() {
    Scanner scanner = new Scanner(System.in);
    System.out.print("Login or register - enter your username: ");
    String username = scanner.nextLine();
    viewModel.loginOrRegister(username);
  }
  public void propertyChange(PropertyChangeEvent evt)
  {

  }
}