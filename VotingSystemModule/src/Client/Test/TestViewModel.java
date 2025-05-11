package Client.Test;

import Client.WindowManager;
import Common.Message;
import Common.MessageType;

public class TestViewModel
{
  public void run()
  {
    var message = new Message(MessageType.Test);
    message.addParam("message", "Testing 1 2 3");
    System.out.println("model is null? " + (WindowManager.getInstance().getModel()==null));
    System.out.println("client is null? " + (WindowManager.getInstance().getModel().getClient()==null));
    WindowManager.getInstance().getModel().getClient()
        .send(message);
  }
}
