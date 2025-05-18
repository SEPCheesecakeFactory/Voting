package Client.DisplayPoll;

import Common.Poll;
import Common.Profile;
import Common.UserGroup;
import javafx.beans.binding.Bindings;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Callback;

import java.util.*;

public class AvailablePollsController {

  @FXML private TableView<Poll> pollTable;
  @FXML private TableColumn<Poll, String> titleColumn;
  @FXML private TableColumn<Poll, Void> voteColumn;
  @FXML private TableColumn<Poll, Void> resultsColumn;
  @FXML private TableColumn<Poll, String> privacyColumn;
  @FXML private TableColumn<Poll, String> openColumn;
  @FXML private TextField searchField;

  private AvailablePollsViewModel viewModel;
  private final Map<String, HBox> nameToRowMap = new HashMap<>();

  public void init(AvailablePollsViewModel viewModel) {
    this.viewModel = viewModel;
    initialize();

    viewModel.addPropertyChangeListener(evt -> {
      if ("UserValidated".equals(evt.getPropertyName()) || "GroupValidated".equals(evt.getPropertyName())) {
        AvailablePollsViewModel.ValidationResult result = (AvailablePollsViewModel.ValidationResult) evt.getNewValue();
        javafx.application.Platform.runLater(() -> handleValidationResult(result));
      }
      else if ("PollAccessUpdated".equals(evt.getPropertyName())) {
        // Handle poll access updates
        javafx.application.Platform.runLater(this::refreshPollsData);
      }
    });
  }

  private void refreshPollsData() {
    // Request fresh poll data from the server
    viewModel.refreshAvailablePolls();

    // Refresh the table view
    pollTable.refresh();
  }

  private void initialize() {
    titleColumn.setCellValueFactory(data -> Bindings.createStringBinding(() -> data.getValue().getTitle()));

    privacyColumn.setCellFactory(column -> new TableCell<>() {
      private final Button addUsersButton = new Button("Poll Access");
      private final Label label = new Label();
      private final HBox box = new HBox(5, label, addUsersButton);

      {
        addUsersButton.setOnAction(event -> {
          Poll poll = getTableView().getItems().get(getIndex());
          openAddUsersChoiceDialog(poll);
        });
      }

      @Override
      protected void updateItem(String item, boolean empty) {
        super.updateItem(item, empty);

        if (empty || getIndex() >= getTableView().getItems().size()) {
          setGraphic(null);
          return;
        }

        Poll poll = getTableView().getItems().get(getIndex());
        boolean isPrivate = poll.isPrivate();
        boolean isOwner = poll.getCreatedById() == viewModel.getLoggedInUserId();

        label.setText(isPrivate ? "Private" : "Public");

        if (isPrivate && isOwner) {
          setGraphic(box);
        } else {
          setGraphic(label);
        }
      }
    });

    openColumn.setCellValueFactory(data ->
        Bindings.createStringBinding(() -> data.getValue().isClosed() ? "Closed" : "Open"));

    addVoteButtonToTable();
    addResultsButtonToTable();

    FilteredList<Poll> filteredData = new FilteredList<>(viewModel.getAvailablePolls(), p -> true);
    viewModel.searchTextProperty().addListener((obs, oldVal, newVal) ->
        filteredData.setPredicate(poll -> poll.getTitle().toLowerCase().contains(newVal.toLowerCase()))
    );

    searchField.textProperty().bindBidirectional(viewModel.searchTextProperty());

    SortedList<Poll> sortedData = new SortedList<>(filteredData);
    sortedData.comparatorProperty().bind(pollTable.comparatorProperty());
    pollTable.setItems(sortedData);
  }

  private void addVoteButtonToTable() {
    voteColumn.setCellFactory(getButtonCellFactory("Vote", poll -> {
      viewModel.requestVote(poll);
      Client.WindowManager.getInstance().showView(Client.ViewType.DisplayPoll);
    }));
  }

  private void addResultsButtonToTable() {
    resultsColumn.setCellFactory(getButtonCellFactory("Results", poll -> {
      viewModel.requestResults(poll);
      Client.WindowManager.getInstance().showView(Client.ViewType.PollResult);
    }));
  }

  private Callback<TableColumn<Poll, Void>, TableCell<Poll, Void>> getButtonCellFactory(String label, java.util.function.Consumer<Poll> action) {
    return param -> new TableCell<>() {
      private final Button btn = new Button(label);
      {
        btn.setOnAction(event -> {
          Poll poll = getTableView().getItems().get(getIndex());
          action.accept(poll);
        });
      }
      @Override protected void updateItem(Void item, boolean empty) {
        super.updateItem(item, empty);
        setGraphic(empty ? null : btn);
      }
    };
  }

  private void openGroupPopup(Poll poll, String mode) {
    Stage popupStage = new Stage();
    popupStage.setTitle("Allowed " + (mode.equals("single") ? "Users" : "Groups"));

    VBox root = new VBox(15);
    root.setPadding(new Insets(15));

    Label listLabel = new Label("Allowed " + (mode.equals("single") ? "Users" : "Groups") + ":");
    listLabel.setStyle("-fx-font-weight: bold;");

    VBox container = new VBox(10);
    nameToRowMap.clear();

    Callback<String, HBox> createEditableRow = (name) -> {
      HBox row = new HBox(10);
      TextField textField = new TextField(name);
      Button validateButton = new Button("Validate");
      Button removeButton = new Button("Remove");

      validateButton.setOnAction(ev -> {
        String input = textField.getText().trim();
        if (input.isEmpty()) {
          showAlert("Validation Error", (mode.equals("single") ? "Username" : "Group name") + " cannot be empty.");
          return;
        }
        nameToRowMap.put(input.toLowerCase(), row);
        if ("single".equals(mode)) {
          viewModel.validateUsername(input);
        } else {
          viewModel.validateGroupName(input);
        }
      });

      removeButton.setOnAction(ev -> {
        container.getChildren().remove(row);
        nameToRowMap.values().remove(row);
      });

      row.getChildren().addAll(textField, validateButton, removeButton);
      nameToRowMap.put(name.toLowerCase(), row);
      return row;
    };

    // Allow adding new empty rows
    Runnable addRow = () -> container.getChildren().add(createEditableRow.call(""));

    // Add existing users/groups
    if ("single".equals(mode)&&!poll.getAllowedUsers().isEmpty()) {
      for (Profile user : poll.getAllowedUsers()) {
        container.getChildren().add(createEditableRow.call(user.getUsername()));
      }
    }
    else if("group".equals(mode)&&!poll.getAllowedGroups().isEmpty())
    {
      for (UserGroup group : poll.getAllowedGroups()) {
        container.getChildren().add(createEditableRow.call(group.getGroupName()));
      }
    }
    else{
      addRow.run();
      addRow.run();
    }



    Button addButton = new Button("Add " + (mode.equals("single") ? "User" : "Group"));
    addButton.setOnAction(e -> addRow.run());

    Button saveButton = new Button("Save");
    saveButton.setOnAction(e -> {
      List<String> names = new ArrayList<>();

      for (var node : container.getChildren()) {
        if (node instanceof HBox hbox && !hbox.getChildren().isEmpty()) {
          boolean stillHasValidate = hbox.getChildren().stream()
              .anyMatch(child -> child instanceof Button b && "Validate".equals(b.getText()));

          if (stillHasValidate) {
            showAlert("Validation Error", "All entries must be validated before saving.");
            return;
          }

          TextField tf = (TextField) hbox.getChildren().get(0);
          String name = tf.getText().trim();
          if (!name.isEmpty()) {
            names.add(name);
          }
        }
      }

      if (names.isEmpty()) {
        showAlert("Validation Error", "Add at least one validated " + (mode.equals("single") ? "user." : "group."));
        return;
      }

      if (poll != null) {
        if ("single".equals(mode)) {
          viewModel.saveAccessToUsers(poll);
        } else {
          viewModel.saveAccessToGroups(poll);
        }

        // Explicitly refresh the UI after saving
        refreshPollsData();
      }

      popupStage.close();
    });

    HBox buttons = new HBox(10, addButton, saveButton);
    root.getChildren().addAll(listLabel, container, buttons);

    Scene scene = new Scene(root, 500, 500);
    popupStage.setScene(scene);
    popupStage.initModality(Modality.APPLICATION_MODAL);
    popupStage.showAndWait();
  }





  private void showAlert(String title, String message) {
    Alert alert = new Alert(Alert.AlertType.WARNING);
    alert.setTitle(title);
    alert.setHeaderText(null);
    alert.setContentText(message);
    alert.showAndWait();
  }

  private void openAddUsersChoiceDialog(Poll poll) {
    List<String> choices = List.of("Single Users", "Groups");

    ChoiceDialog<String> dialog = new ChoiceDialog<>(choices.get(0), choices);
    dialog.setTitle("Add Users or Groups");
    dialog.setHeaderText("Choose what to add");
    dialog.setContentText("Select an option:");

    Optional<String> result = dialog.showAndWait();
    if (result.isPresent()) {
      String choice = result.get();

      if ("Single Users".equals(choice)) {
        openGroupPopup(poll, "single");
      } else if ("Groups".equals(choice)) {
        openGroupPopup(poll, "group");
      }
    }
  }

  private void handleValidationResult(AvailablePollsViewModel.ValidationResult result) {
    String key = result.getName().toLowerCase();
    HBox row = nameToRowMap.get(key);

    if (row == null) return;

    if (result.isValid()) {
      row.getChildren().removeIf(node -> node instanceof Button && ((Button) node).getText().equals("Validate"));
      Label validLabel = new Label("Valid");
      validLabel.setStyle("-fx-text-fill: green;");
      row.getChildren().add(validLabel);
    } else {
      showAlert("Validation Failed", "Validation failed for: " + result.getName());
    }
  }
}