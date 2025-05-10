package Client;

import Utils.Logger;

public class TestView
{
  private TestViewModel viewModel;
  public TestView(TestViewModel viewModel) { this.viewModel = viewModel; }
  public void render()
  {
    Logger.log("Testing...");
    viewModel.run();
    Logger.log("End of testing...");
  }
}
