package Client.PollResult;

import Common.ChoiceOption;
import Common.Poll;
import Common.PollResult;
import Common.Question;
import Client.WindowManager;
import Client.ViewType;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.security.InvalidParameterException;
import java.util.Arrays;
import java.util.Map;

public class PollResultViewController implements PropertyChangeListener {

  @FXML private Label messageText;
  @FXML private VBox questionsContainer;

  private PollResultViewModel viewModel;

  /** Called by your application after FXMLLoader.load() */
  public void init(PollResultViewModel viewModel) {
    this.viewModel = viewModel;
    this.viewModel.addPropertyChangeListener(this);
  }



  @Override
  public void propertyChange(PropertyChangeEvent evt) {
    if ("PollResult".equals(evt.getPropertyName())) {
      PollResult result = (PollResult) evt.getNewValue();
      Platform.runLater(() -> displayPollResult(result));
    } else {
      throw new InvalidParameterException("Unknown property: " + evt.getPropertyName());
    }
  }

  private void displayPollResult(PollResult result) {
    Poll poll = result.getPoll();
    Map<Integer, Integer> votes = result.getChoiceVoters();

    messageText.setText("Results for “" + poll.getTitle() + "”");

    for (Question question : poll.getQuestions()) {
      // Question title
      Label qLabel = new Label("Q: " + question.getTitle());
      qLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
      questionsContainer.getChildren().add(qLabel);

      // total votes
      int totalVotes = Arrays.stream(question.getChoiceOptions())
          .mapToInt(opt -> votes.getOrDefault(opt.getId(), 0))
          .sum();

      // each choice row
      for (ChoiceOption option : question.getChoiceOptions()) {
        int count = votes.getOrDefault(option.getId(), 0);
        double fraction = totalVotes > 0 ? (double) count / totalVotes : 0.0;

        Label choiceLbl = new Label(option.getValue());
        ProgressBar bar = new ProgressBar(fraction);
        bar.setPrefWidth(200);

        String pctText = String.format("%.1f%% (%d votes)", fraction * 100, count);
        Label pctLbl = new Label(pctText);

        HBox row = new HBox(10, choiceLbl, bar, pctLbl);
        questionsContainer.getChildren().add(row);
      }
    }
  }
}
