package Client.DisplayPoll;

import Client.WindowManager;
import Common.ChoiceOption;
import Common.Poll;
import Common.Question;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

public class DisplayPollController {

  @FXML private Label questionTitle;
  @FXML private Label questionDescription;
  @FXML private VBox choicesContainer;
  @FXML private Label progressLabel;
  @FXML private Button prevButton;
  @FXML private Button nextButton;

  private Poll poll;
  private int currentQuestionIndex = 0;


  @FXML public void initialize() {
    System.out.println("DisplayPollController initialized");

    poll = WindowManager.getInstance().getModel().getPoll();
    System.out.println("Poll from model: " + (poll != null ? poll.getTitle() : "null"));
    if (poll != null && poll.getQuestions().length > 0) {
      renderQuestion();
    }
  }

  private void renderQuestion() {
    Question question = poll.getQuestions()[currentQuestionIndex];
    questionTitle.setText(question.getTitle());
    questionDescription.setText(question.getDescription());

    choicesContainer.getChildren().clear();
    for (ChoiceOption option : question.getChoiceOptions()) {
      Button btn = new Button(option.getValue());
      btn.setMaxWidth(Double.MAX_VALUE);
      btn.setOnAction(e -> handleVote(option.getId()));
      choicesContainer.getChildren().add(btn);
    }

    progressLabel.setText(String.format("%s - Q %d/%d", poll.getTitle(), currentQuestionIndex + 1, poll.getQuestions().length));
    prevButton.setDisable(currentQuestionIndex == 0);
    nextButton.setDisable(currentQuestionIndex == poll.getQuestions().length - 1);
  }

  private void handleVote(int choiceId) {
    System.out.println("Voted on choice: " + choiceId);
    // You can store user choices here if needed.
  }

  @FXML private void handlePrev() {
    if (currentQuestionIndex > 0) {
      currentQuestionIndex--;
      renderQuestion();
    }
  }

  @FXML private void handleNext() {
    if (currentQuestionIndex < poll.getQuestions().length - 1) {
      currentQuestionIndex++;
      renderQuestion();
    }
  }
}
