package Client.DisplayPoll;

import Client.WindowManager;
import Common.Poll;
import Common.Profile;
import Common.UserGroup;
import javafx.application.Platform;
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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.*;

public class AvailablePollsController implements PropertyChangeListener
{

  @FXML private TableView<Poll> pollTable;
  @FXML private TableColumn<Poll, String> titleColumn;
  @FXML private TableColumn<Poll, Void> voteColumn;
  @FXML private TableColumn<Poll, Void> resultsColumn;
  @FXML private TableColumn<Poll, String> privacyColumn;
  @FXML private TableColumn<Poll, String> openColumn;
  @FXML private TableColumn<Poll, Void> accessColumn; // NEW COLUMN
  @FXML private TableColumn<Poll, Void> closeColumn; // NEW COLUMN
  @FXML private TextField searchField;

  private AvailablePollsViewModel viewModel;
  private final Map<String, HBox> nameToRowMap = new HashMap<>();

  public void init(AvailablePollsViewModel viewModel)
  {
    this.viewModel = viewModel;
    this.viewModel.addPropertyChangeListener(this);
    initialize();

    viewModel.addPropertyChangeListener(evt -> {
      if ("UserValidated".equals(evt.getPropertyName())
          || "GroupValidated".equals(evt.getPropertyName()))
      {
        AvailablePollsViewModel.ValidationResult result = (AvailablePollsViewModel.ValidationResult) evt.getNewValue();
        Platform.runLater(() -> handleValidationResult(result));
      }
      else if ("PollAccessUpdated".equals(evt.getPropertyName()))
      {
        refresh();
      }
    });
  }

  private void refresh()
  {
    Platform.runLater(this::refreshPollsData);
  }

  private void refreshPollsData()
  {
    viewModel.refreshAvailablePolls();
    pollTable.refresh();

    boolean userHasCreatedPolls = viewModel.getAvailablePolls().stream()
        .anyMatch(
            poll -> poll.getCreatedById() == viewModel.getLoggedInUserId());
    accessColumn.setVisible(userHasCreatedPolls);
  }

  private void initialize()
  {
    titleColumn.setCellValueFactory(
        data -> Bindings.createStringBinding(() -> data.getValue().getTitle()));

    privacyColumn.setCellValueFactory(data -> Bindings.createStringBinding(
        () -> data.getValue().isPrivate() ? "Private" : "Public"));
    openColumn.setCellValueFactory(data -> Bindings.createStringBinding(
        () -> data.getValue().isClosed() ? "Closed" : "Open"));

    addVoteButtonToTable();
    addResultsButtonToTable();
    addAccessEditButtonToTable(); // NEW
    addCloseButtonToTable();

    FilteredList<Poll> filteredData = new FilteredList<>(
        viewModel.getAvailablePolls(), p -> true);
    viewModel.searchTextProperty().addListener(
        (obs, oldVal, newVal) -> filteredData.setPredicate(
            poll -> poll.getTitle().toLowerCase()
                .contains(newVal.toLowerCase())));
    searchField.textProperty()
        .bindBidirectional(viewModel.searchTextProperty());

    SortedList<Poll> sortedData = new SortedList<>(filteredData);
    sortedData.comparatorProperty().bind(pollTable.comparatorProperty());
    pollTable.setItems(sortedData);

  }

  private void setAccessColumnVisibility()
  {
    boolean userHasCreatedPolls = viewModel.getAvailablePolls().stream()
        .anyMatch(
            poll -> poll.getCreatedById() == viewModel.getLoggedInUserId());

    if (userHasCreatedPolls)
      accessColumn.setVisible(userHasCreatedPolls);
  }

  private void addVoteButtonToTable()
  {
    voteColumn.setCellFactory(column -> new TableCell<Poll, Void>()
    {
      private final Button voteBtn = new Button("Vote");

      {
        voteBtn.setOnAction(evt -> {
          Poll p = getTableView().getItems().get(getIndex());
          viewModel.requestVote(p);
          Client.WindowManager.getInstance()
              .showView(Client.ViewType.DisplayPoll);
        });
      }

      @Override protected void updateItem(Void item, boolean empty)
      {
        super.updateItem(item, empty);

        if (empty || getIndex() >= getTableView().getItems().size())
        {
          setGraphic(null);
          return;
        }

        Poll p = getTableView().getItems().get(getIndex());
        if (!p.isClosed())
        {
          setGraphic(voteBtn);
        }
        else
        {
          setGraphic(null);
        }
      }
    });
  }

  private void addResultsButtonToTable()
  {
    resultsColumn.setCellFactory(getButtonCellFactory("Results", poll -> {
      viewModel.requestResults(poll);
      Client.WindowManager.getInstance().showView(Client.ViewType.PollResult);
    }));
  }

  private void addCloseButtonToTable()
  {
    closeColumn.setCellFactory(column -> new TableCell<Poll, Void>()
    {
      private final Button closeBtn = new Button("Close");

      {
        closeBtn.setOnAction(evt -> {
          WindowManager.getInstance().showConfirmationPopup("Do you really want to close this poll?","Close Poll","Close Poll",(confirmed)->{
            if(confirmed)
            {
              Poll p = getTableView().getItems().get(getIndex());
              viewModel.closePoll(p);
              refresh();
            }
          });
        });
      }

      @Override protected void updateItem(Void item, boolean empty)
      {
        super.updateItem(item, empty);

        // no button for empty rows
        if (empty || getIndex() >= getTableView().getItems().size())
        {
          setGraphic(null);
          return;
        }

        Poll p = getTableView().getItems().get(getIndex());
        boolean isOwner = p.getCreatedById() == viewModel.getLoggedInUserId();
        boolean isOpen = !p.isClosed();

        // Only owners see a “Close” button and only if it’s open
        if (isOwner && isOpen)
        {
          setGraphic(closeBtn);
        }
        else
        {
          setGraphic(null);
        }
      }
    });
  }

  private Callback<TableColumn<Poll, Void>, TableCell<Poll, Void>> getButtonCellFactory(
      String label, java.util.function.Consumer<Poll> action)
  {
    return param -> new TableCell<>()
    {
      private final Button btn = new Button(label);

      {
        btn.setOnAction(event -> {
          Poll poll = getTableView().getItems().get(getIndex());
          action.accept(poll);
        });
      }

      @Override protected void updateItem(Void item, boolean empty)
      {
        super.updateItem(item, empty);
        setGraphic(empty ? null : btn);
      }
    };
  }

  private void addAccessEditButtonToTable()
  {
    accessColumn.setCellFactory(column -> new TableCell<>()
    {
      private final Button editAccessButton = new Button("Edit Access");

      {
        editAccessButton.setOnAction(event -> {
          Poll poll = getTableView().getItems().get(getIndex());
          openAddUsersChoiceDialog(poll);
        });
      }

      @Override protected void updateItem(Void item, boolean empty)
      {
        super.updateItem(item, empty);

        if (empty || getIndex() >= getTableView().getItems().size())
        {
          setGraphic(null);
          return;
        }

        Poll poll = getTableView().getItems().get(getIndex());
        boolean isOwner =
            poll.getCreatedById() == viewModel.getLoggedInUserId();

        if (poll.isPrivate() && isOwner)
        {
          setGraphic(editAccessButton);
        }
        else
        {
          setGraphic(null);
        }
      }
    });
  }

  private void openGroupPopup(Poll poll, String mode)
  {
    Stage popupStage = new Stage();
    popupStage.setTitle(
        "Allowed " + (mode.equals("single") ? "Users" : "Groups"));

    VBox root = new VBox(15);
    root.setPadding(new Insets(15));

    Label listLabel = new Label(
        "Allowed " + (mode.equals("single") ? "Users" : "Groups") + ":");
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
        if (input.isEmpty())
        {
          showAlert("Validation Error",
              (mode.equals("single") ? "Username" : "Group name")
                  + " cannot be empty.");
          return;
        }
        nameToRowMap.put(input.toLowerCase(), row);
        if ("single".equals(mode))
        {
          viewModel.validateUsername(input);
        }
        else
        {
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

    Runnable addRow = () -> container.getChildren()
        .add(createEditableRow.call(""));

    if ("single".equals(mode) && !poll.getAllowedUsers().isEmpty())
    {
      for (Profile user : poll.getAllowedUsers())
      {
        container.getChildren().add(createEditableRow.call(user.getUsername()));
      }
    }
    else if ("group".equals(mode) && !poll.getAllowedGroups().isEmpty())
    {
      for (UserGroup group : poll.getAllowedGroups())
      {
        container.getChildren()
            .add(createEditableRow.call(group.getGroupName()));
      }
    }
    else
    {
      addRow.run();
      addRow.run();
    }

    Button addButton = new Button(
        "Add " + (mode.equals("single") ? "User" : "Group"));
    addButton.setOnAction(e -> addRow.run());

    Button saveButton = new Button("Save");
    saveButton.setOnAction(e -> {
      List<String> names = new ArrayList<>();

      for (var node : container.getChildren())
      {
        if (node instanceof HBox hbox && !hbox.getChildren().isEmpty())
        {
          boolean stillHasValidate = hbox.getChildren().stream().anyMatch(
              child -> child instanceof Button b && "Validate".equals(
                  b.getText()));

          if (stillHasValidate)
          {
            showAlert("Validation Error",
                "All entries must be validated before saving.");
            return;
          }

          TextField tf = (TextField) hbox.getChildren().get(0);
          String name = tf.getText().trim();
          if (!name.isEmpty())
          {
            names.add(name);
          }
        }
      }

      if (names.isEmpty())
      {
        showAlert("Validation Error",
            "Add at least one validated " + (mode.equals("single") ?
                "user." :
                "group."));
        return;
      }

      if (poll != null)
      {
        if ("single".equals(mode))
        {
          viewModel.saveAccessToUsers(poll);
        }
        else
        {
          viewModel.saveAccessToGroups(poll);
        }

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

  private void openAddUsersChoiceDialog(Poll poll)
  {
    List<String> choices = List.of("Single Users", "Groups");

    ChoiceDialog<String> dialog = new ChoiceDialog<>(choices.get(0), choices);
    dialog.setTitle("Add Users or Groups");
    dialog.setHeaderText("Choose what to add");
    dialog.setContentText("Select an option:");

    Optional<String> result = dialog.showAndWait();
    result.ifPresent(choice -> {
      if ("Single Users".equals(choice))
      {
        openGroupPopup(poll, "single");
      }
      else if ("Groups".equals(choice))
      {
        openGroupPopup(poll, "group");
      }
    });
  }

  private void handleValidationResult(
      AvailablePollsViewModel.ValidationResult result)
  {
    String key = result.getName().toLowerCase();
    HBox row = nameToRowMap.get(key);

    if (row == null)
      return;

    if (result.isValid())
    {
      row.getChildren().removeIf(
          node -> node instanceof Button && ((Button) node).getText()
              .equals("Validate"));
      Label validLabel = new Label("Valid");
      validLabel.setStyle("-fx-text-fill: green;");
      row.getChildren().add(validLabel);
    }
    else
    {
      showAlert("Validation Failed",
          "Validation failed for: " + result.getName());
    }
  }

  private void showAlert(String title, String message)
  {
    Alert alert = new Alert(Alert.AlertType.WARNING);
    alert.setTitle(title);
    alert.setHeaderText(null);
    alert.setContentText(message);
    alert.showAndWait();
  }

  @Override public void propertyChange(PropertyChangeEvent evt)
  {
    if (evt.getPropertyName().equals("AvailablePolls"))
    {
      setAccessColumnVisibility();
    }
  }
}
