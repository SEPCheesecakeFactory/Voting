package Client.DisplayPoll;

import Client.ViewType;
import Client.WindowManager;
import Common.Poll;
import Utils.Logger;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.util.Callback;

import java.util.List;

public class AvailablePollsController
{

  @FXML private TableView<Poll> pollTable;

  @FXML private TableColumn<Poll, String> titleColumn;
  @FXML private TableColumn<Poll, Void> voteColumn;
  @FXML private TableColumn<Poll, Void> resultsColumn;
  @FXML private TableColumn<Poll, String> privacyColumn;
  @FXML private TableColumn<Poll, String> openColumn;
  @FXML private TextField searchField;

  private final ObservableList<Poll> masterData = FXCollections.observableArrayList();

  @FXML public void initialize()
  {
    titleColumn.setCellValueFactory(
        data -> new SimpleStringProperty(data.getValue().getTitle()));
    privacyColumn.setCellValueFactory(data -> new SimpleStringProperty(
        String.valueOf(data.getValue().isPrivate())));
    openColumn.setCellValueFactory(data -> new SimpleStringProperty(
        String.valueOf(data.getValue().isClosed())));
    /*
    voteColumn.setCellValueFactory(data -> new SimpleStringProperty(
        data.getValue().getTitle())); // convert to button
    resultsColumn.setCellValueFactory(data -> new SimpleStringProperty(
        data.getValue().getTitle())); // convert to button
    * */

    addVoteButtonToTable();
    addResultsButtonToTable();

    WindowManager.getInstance().getModel().addPropertyChangeListener("AvailablePolls", evt -> {
      List<Poll> polls = (List<Poll>) evt.getNewValue();
      System.out.println("Received available polls: " + polls.size());
      masterData.setAll(polls);
    });

    Logger.log("Requesting available polls...");
    WindowManager.getInstance().getModel().requestAvailablePolls();

    // Wrap in FilteredList
    FilteredList<Poll> filteredData = new FilteredList<>(masterData, p -> true);

    // Listen for search text changes
    searchField.textProperty().addListener((obs, oldVal, newVal) ->
        filteredData.setPredicate(poll -> poll.getTitle().toLowerCase().contains(newVal.toLowerCase()))
    );

    SortedList<Poll> sortedData = new SortedList<>(filteredData);
    sortedData.comparatorProperty().bind(pollTable.comparatorProperty());
    pollTable.setItems(sortedData);
  }

  private void addVoteButtonToTable() {
    Callback<TableColumn<Poll, Void>, TableCell<Poll, Void>> cellFactory = param -> new TableCell<>() {
      private final Button btn = new Button("Vote");
      {
        btn.setOnAction(event -> {
          Poll poll = getTableView().getItems().get(getIndex());
          WindowManager.getInstance().getModel().sendDisplayPollRequest(poll.getId());
        });
      }
      @Override protected void updateItem(Void item, boolean empty) {
        super.updateItem(item, empty);
        setGraphic(empty ? null : btn);
      }
    };
    voteColumn.setCellFactory(cellFactory);
  }

  private void addResultsButtonToTable() {
    Callback<TableColumn<Poll, Void>, TableCell<Poll, Void>> cellFactory = param -> new TableCell<>() {
      private final Button btn = new Button("Results");
      {
        btn.setOnAction(event -> {
          Poll poll = getTableView().getItems().get(getIndex());
          WindowManager.getInstance().getModel().sendResultRequest(poll.getId());
          WindowManager.getInstance().showView(ViewType.PollResult);
        });
      }
      @Override protected void updateItem(Void item, boolean empty) {
        super.updateItem(item, empty);
        setGraphic(empty ? null : btn);
      }
    };
    resultsColumn.setCellFactory(cellFactory);
  }
}
