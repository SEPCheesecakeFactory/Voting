package Client.DisplayPoll;

import Client.ViewType;
import Client.WindowManager;
import Common.ChoiceOption;
import Common.Poll;
import Common.Question;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.text.Text;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.security.InvalidParameterException;

public class DisplayPollView implements PropertyChangeListener {

  @FXML private Label titleLabel;
  @FXML private Label descriptionLabel;
  @FXML private ListView<String> votingListView;
  @FXML private Label navigationLabel;
  @FXML private Button navArrowLeftButton;
  @FXML private Button navArrowRightButton;
  @FXML private Text messageText;
  @FXML private Button submitButton;

  private DisplayPollViewModel viewModel;

  private Poll currentPoll;
  private int currentQuestionIndex = 0;
  private int[] selectedChoices;

  public void init(DisplayPollViewModel viewModel) {
    this.viewModel = viewModel;
    this.viewModel.addPropertyChangeListener("PollUpdated", this);

    // Request poll on load (you can make this more dynamic)
    viewModel.sendPollRequest(1); // replace with real pollId source
  }

  private void renderCurrentQuestion() {
    if (currentPoll == null) return;

    Question q = currentPoll.getQuestions()[currentQuestionIndex];
    titleLabel.setText(currentPoll.getTitle());
    descriptionLabel.setText(q.getDescription());

    navigationLabel.setText("Q " + (currentQuestionIndex + 1) + " / " + currentPoll.getQuestions().length);

    votingListView.setItems(FXCollections.observableArrayList());
    for (ChoiceOption option : q.getChoiceOptions()) {
      votingListView.getItems().add(option.getValue());
    }

    votingListView.getSelectionModel().selectFirst();

    navArrowLeftButton.setDisable(currentQuestionIndex == 0);
    navArrowRightButton.setDisable(currentQuestionIndex == currentPoll.getQuestions().length - 1);
  }

  @FXML
  private void onLeftArrowClicked() {
    if (currentQuestionIndex > 0) {
      storeSelectedChoice();
      currentQuestionIndex--;
      renderCurrentQuestion();
    }
  }

  @FXML
  private void onRightArrowClicked() {
    if (currentQuestionIndex < currentPoll.getQuestions().length - 1) {
      storeSelectedChoice();
      currentQuestionIndex++;
      renderCurrentQuestion();
    }
  }

  private void storeSelectedChoice() {
    int selected = votingListView.getSelectionModel().getSelectedIndex();
    if (selected >= 0) {
      selectedChoices[currentQuestionIndex] = currentPoll.getQuestions()[currentQuestionIndex].getChoiceOptions()[selected].getId();
    }
  }

  @FXML
  private void onSubmitVoteClicked() {
    storeSelectedChoice(); // store last question answer

    int userId = viewModel.getModel().getProfile().getId();
    viewModel.sendVote(userId, selectedChoices);
    messageText.setText("Vote submitted!");
    WindowManager.getInstance().showView(ViewType.HomeScreen); // or wherever you want to go
  }

  @Override
  public void propertyChange(PropertyChangeEvent evt) {
    if ("PollUpdated".equals(evt.getPropertyName())) {
      currentPoll = (Poll) evt.getNewValue();
      selectedChoices = new int[currentPoll.getQuestions().length];
      renderCurrentQuestion();
    } else {
      throw new InvalidParameterException("Unhandled event: " + evt.getPropertyName());
    }
  }
}
