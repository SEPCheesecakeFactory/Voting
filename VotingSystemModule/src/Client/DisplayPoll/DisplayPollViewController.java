package Client.DisplayPoll;

import Client.ViewType;
import Client.WindowManager;
import Common.ChoiceOption;
import Common.Poll;
import Utils.Logger;
import javafx.beans.binding.Bindings;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DisplayPollViewController
{

  @FXML private Label titleLabel;
  @FXML private Label descriptionLabel;
  @FXML private VBox questionChoicesContainer;
  @FXML private Button navArrowLeftButton;
  @FXML private Button navArrowRightButton;
  @FXML private Label navigationLabel;
  @FXML private Button voteButton;

  private DisplayPollViewModelGUI viewModel;

  // Store user selections: question index -> selected option index
  private final Map<Integer, Integer> selectedOptions = new HashMap<>();

  private ToggleGroup toggleGroup;

  public void init(DisplayPollViewModelGUI viewModel)
  {
    this.viewModel = viewModel;

    toggleGroup = new ToggleGroup();

    // Bind UI elements to viewModel properties
    titleLabel.textProperty().bind(viewModel.titleProperty());
    descriptionLabel.textProperty().bind(viewModel.descriptionProperty());

    // When choices change, update UI
    viewModel.choicesProperty()
        .addListener((obs, oldVal, newVal) -> updateChoices());

    // Display current question index and total
    navigationLabel.textProperty().bind(Bindings.createStringBinding(
        () -> String.format("Q %d/%d", viewModel.getCurrentIndex() + 1,
            viewModel.getTotalQuestions()), viewModel.currentIndexProperty(),
        viewModel.totalQuestionsProperty()));

    // Navigation button actions
    navArrowLeftButton.setOnAction(e -> {
      saveSelectedOption();
      viewModel.previousQuestion();
      restoreSelectedOption();
    });
    navArrowRightButton.setOnAction(e -> {
      saveSelectedOption();
      viewModel.nextQuestion();
      restoreSelectedOption();
    });

    voteButton.setOnAction(e -> {
      saveSelectedOption();
      sendVote();
    });
  }

  private void updateChoices()
  {
    questionChoicesContainer.getChildren().clear();

    // Create a new ToggleGroup each time to avoid state issues
    toggleGroup = new ToggleGroup();

    int index = 0;
    for (ChoiceOption option : viewModel.getChoices())
    {
      RadioButton radioButton = new RadioButton(option.getValue());
      radioButton.setToggleGroup(toggleGroup);
      radioButton.setUserData(index);
      questionChoicesContainer.getChildren().add(radioButton);
      index++;
    }

    restoreSelectedOption();
  }

  private void saveSelectedOption()
  {
    if (toggleGroup == null)
      return;

    Toggle selectedToggle = toggleGroup.getSelectedToggle();
    if (selectedToggle != null)
    {
      int selectedIndex = (int) selectedToggle.getUserData();
      selectedOptions.put(viewModel.getCurrentIndex(), selectedIndex);
    }
    else
    {
      selectedOptions.remove(viewModel.getCurrentIndex());
    }
  }

  private void restoreSelectedOption()
  {
    if (toggleGroup == null)
      return;

    Integer selectedIndex = selectedOptions.get(viewModel.getCurrentIndex());
    if (selectedIndex != null)
    {
      for (Toggle toggle : toggleGroup.getToggles())
      {
        if ((int) toggle.getUserData() == selectedIndex)
        {
          toggleGroup.selectToggle(toggle);
          return;
        }
      }
    }
    toggleGroup.selectToggle(null);
  }

  private void sendVote()
  {
    int totalQuestions = viewModel.getTotalQuestions();
    int[] selectedChoiceIds = new int[totalQuestions];

    for (int questionIndex = 0; questionIndex < totalQuestions; questionIndex++)
    {
      Integer selectedChoiceIndex = selectedOptions.get(questionIndex);
      if (selectedChoiceIndex == null)
      {
        Alert alert = new Alert(Alert.AlertType.WARNING,
            "Please answer all questions before submitting.");
        alert.showAndWait();
        return;
      }

      // Load the choices for this question
      List<ChoiceOption> choicesForQuestion = (List.of(
          viewModel.getCurrentPoll()
              .getQuestions()[questionIndex].getChoiceOptions()));

      if (selectedChoiceIndex < 0
          || selectedChoiceIndex >= choicesForQuestion.size())
      {
        Logger.log("Invalid selected index for question " + questionIndex + ": "
            + selectedChoiceIndex);
        return;
      }

      selectedChoiceIds[questionIndex] = choicesForQuestion.get(
          selectedChoiceIndex).getId();
    }

    int userId = viewModel.getUserId();
    var success = viewModel.sendVote(userId, selectedChoiceIds);
    if (!success)
      WindowManager.getInstance().showErrorPopup("Could not send the vote!");
    else
    {
      WindowManager.getInstance().showInfoPopup("Vote sent successfully!");
      WindowManager.getInstance().showView(ViewType.AvailablePolls);
    }
  }
}
