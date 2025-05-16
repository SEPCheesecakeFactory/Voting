package Client.DisplayPoll;

import Common.Poll;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;

public class AvailablePollsController
{

  @FXML private TableView<Poll> pollTable;
  @FXML private TableColumn<Poll, String> titleColumn;
  @FXML private TableColumn<Poll, String> voteColumn;
  @FXML private TableColumn<Poll, String> resultsColumn;
  @FXML private TableColumn<Poll, String> privacyColumn;
  @FXML private TableColumn<Poll, String> openColumn;
  @FXML private TextField searchField;

  private final ObservableList<Poll> pollList = FXCollections.observableArrayList();

  @FXML public void initialize()
  {
    titleColumn.setCellValueFactory(
        data -> new SimpleStringProperty(data.getValue().getTitle()));
    voteColumn.setCellValueFactory(data -> new SimpleStringProperty(
        data.getValue().getTitle())); // convert to button
    resultsColumn.setCellValueFactory(data -> new SimpleStringProperty(
        data.getValue().getTitle())); // convert to button
    privacyColumn.setCellValueFactory(data -> new SimpleStringProperty(
        String.valueOf(data.getValue().isPrivate())));
    openColumn.setCellValueFactory(data -> new SimpleStringProperty(
        String.valueOf(data.getValue().isClosed())));

    // Dummy data to test
    pollList.addAll(new Poll("Election 2025", "Presidential vote", 1,
            new Common.Question[0], false, true),
        new Poll("Feedback Survey", "Course feedback", 2,
            new Common.Question[0], true, false));

    pollTable.setItems(pollList);
  }
}
