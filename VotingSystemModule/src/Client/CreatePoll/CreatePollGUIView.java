package Client.CreatePoll;

import Client.WindowManager;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.util.ArrayList;
import java.util.List;

public class CreatePollGUIView
{

  @FXML private TextField pollTitleTextField;
  @FXML private TextField pollDescriptionTextField;
  @FXML private Button publicButton;
  @FXML private VBox questionsContainer;
  @FXML private Button addAnotherQuestionButton;
  @FXML private Button createButton;

  private CreatePollGUIViewModel viewModel;


  // injecting ViewModel from WindowManager
  public void setViewModel(CreatePollGUIViewModel viewModel)
  {
    this.viewModel = viewModel;
    updatePrivacyButton();
  }

  @FXML public void initialize()
  {

    System.out.println("Initialize start");
    System.out.println("addAnotherQuestionButton is " + addAnotherQuestionButton);
    System.out.println("createButton is " + createButton);
    System.out.println("publicButton is " + publicButton);
    addAnotherQuestionButton.setOnAction(e -> addQuestionVBox());

    createButton.setOnAction(e -> createPoll());

    publicButton.setOnAction(e -> togglePrivacy());




    // start with one question by default
    addQuestionVBox();

  }

  private void addQuestionVBox()
  {
    VBox questionVBox = new VBox(5);
    questionVBox.setStyle(
        "-fx-padding: 10; -fx-border-color: gray; -fx-border-width: 1;");

    TextField questionTextField = new TextField();
    questionTextField.setPromptText("Question text...");

    VBox choicesContainer = new VBox(5);

    Button addChoiceButton = new Button("Add Choice");
    addChoiceButton.setOnAction(e -> addChoiceHBox(choicesContainer));

    Button removeQuestionButton = new Button("Remove Question");
    removeQuestionButton.setOnAction(
        e -> questionsContainer.getChildren().remove(questionVBox));

    HBox questionControls = new HBox(10, addChoiceButton, removeQuestionButton);

    questionVBox.getChildren()
        .addAll(questionTextField, choicesContainer, questionControls);

    questionsContainer.getChildren().add(questionVBox);

    // start with two choices by default
    addChoiceHBox(choicesContainer);
    addChoiceHBox(choicesContainer);
  }

  private void addChoiceHBox(VBox choicesContainer)
  {
    HBox choiceHBox = new HBox(5);

    TextField choiceTextField = new TextField();
    choiceTextField.setPromptText("Choice text...");

    Button removeChoiceButton = new Button("Remove Choice");
    removeChoiceButton.setOnAction(
        e -> choicesContainer.getChildren().remove(choiceHBox));

    choiceHBox.getChildren().addAll(choiceTextField, removeChoiceButton);

    choicesContainer.getChildren().add(choiceHBox);
  }

  private void createPoll()
  {
    String title = pollTitleTextField.getText().trim();
    String description = pollDescriptionTextField.getText().trim();

    if (title.isEmpty())
    {
      showAlert("Validation Error", "Poll title cannot be empty.");
      return;
    }

    List<CreatePollViewModel.Question> questions = new ArrayList<>();

    for (var node : questionsContainer.getChildren())
    {
      if (!(node instanceof VBox))
        continue;

      VBox questionVBox = (VBox) node;

      if (questionVBox.getChildren().size() < 2)
        continue;

      TextField questionTextField = (TextField) questionVBox.getChildren()
          .get(0);
      String questionText = questionTextField.getText().trim();
      if (questionText.isEmpty())
      {
        showAlert("Validation Error", "A question cannot be empty.");
        return;
      }

      VBox choicesContainer = (VBox) questionVBox.getChildren().get(1);
      List<String> choiceTexts = new ArrayList<>();

      for (var choiceNode : choicesContainer.getChildren())
      {
        if (!(choiceNode instanceof HBox))
          continue;

        HBox choiceHBox = (HBox) choiceNode;
        if (choiceHBox.getChildren().isEmpty())
          continue;

        TextField choiceTextField = (TextField) choiceHBox.getChildren().get(0);
        String choiceText = choiceTextField.getText().trim();
        if (choiceText.isEmpty())
        {
          showAlert("Validation Error", "A choice cannot be empty.");
          return;
        }
        choiceTexts.add(choiceText);
      }

      if (choiceTexts.size() < 2)
      {
        showAlert("Validation Error",
            "Each question must have at least 2 choices.");
        return;
      }

      CreatePollViewModel.Question question = new CreatePollViewModel.Question();
      question.setTitle(questionText);
      for (String choice : choiceTexts)
      {
        question.addChoice(choice);
      }
      questions.add(question);
    }

    if (questions.isEmpty())
    {
      showAlert("Validation Error", "Add at least one question.");
      return;
    }

    viewModel.setPollTitle(title);
    viewModel.setPollDescription(description);
    viewModel.setQuestions(questions);
    viewModel.createPoll();

    showAlert("Wow", "Poll creation well faked");
    clearForm();
  }

  private void clearForm()
  {
    pollTitleTextField.clear();
    pollDescriptionTextField.clear();
    questionsContainer.getChildren().clear();
    addQuestionVBox();
  }

  private void showAlert(String title, String message)
  {
    Alert alert = new Alert(Alert.AlertType.INFORMATION);
    alert.setTitle(title);
    alert.setHeaderText(null);
    alert.setContentText(message);
    alert.showAndWait();
  }

  private void togglePrivacy()
  {
    viewModel.togglePrivacy();
    updatePrivacyButton();
  }

  private void updatePrivacyButton()
  {

    if (viewModel.isPrivate())
    {
      publicButton.setText("Private");
    }
    else
    {
      publicButton.setText("Public");
    }
  }

}
