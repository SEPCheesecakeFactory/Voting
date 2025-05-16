package Client.CreateVoteGroup;

import Client.Model;
import Common.Profile;
import Common.UserGroup;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.*;

public class CreateVoteGroupViewController implements PropertyChangeListener {

  @FXML private VBox groupsContainer;
  @FXML private Button addGroupButton;

  private TableView<GroupEntry> groupTable;
  private final ObservableList<GroupEntry> groupData = FXCollections.observableArrayList();

  private CreateVoteGroupViewModelGUI viewModel;

  // Keeps track of UI rows so we can update them when validation results arrive
  private final Map<String, HBox> memberRowMap = new HashMap<>();

  public void setViewModel(CreateVoteGroupViewModelGUI viewModel) {
    this.viewModel = viewModel;
    viewModel.addPropertyChangeListener(this);
    viewModel.requestUserGroups();
  }

  @FXML
  public void initialize() {
    setupGroupTable();
    groupsContainer.getChildren().addAll(groupTable);
    addGroupButton.setOnAction(e -> openGroupPopup(null));
  }

  private void openGroupPopup(GroupEntry groupToEdit) {
    if (groupToEdit == null) {
      TextInputDialog dialog = new TextInputDialog();
      dialog.setTitle("New Group");
      dialog.setHeaderText("Create a New Group");
      dialog.setContentText("Enter group name:");

      Optional<String> result = dialog.showAndWait();
      if (result.isEmpty() || result.get().trim().isEmpty()) {
        showAlert("Invalid Name", "Group name cannot be empty.");
        return;
      }

      String groupName = result.get().trim();
      viewModel.createGroup(groupName);  // Call view model right away
      groupToEdit = new GroupEntry(groupName);
      groupData.add(groupToEdit); // Add to table immediately
    }

    Stage popupStage = new Stage();
    popupStage.setTitle("Configure Group: " + groupToEdit.getGroupName());

    VBox root = new VBox(15);
    root.setPadding(new Insets(15));

    TextField groupNameField = new TextField(groupToEdit.getGroupName());
    groupNameField.setEditable(false); // lock editing
    groupNameField.setPrefWidth(300);

    Label nameLabel = new Label("Group Name:");
    VBox nameBox = new VBox(5, nameLabel, groupNameField);

    VBox memberContainer = new VBox(10);
    memberRowMap.clear();

    Runnable addMemberRow = () -> {
      HBox memberRow = new HBox(10);
      TextField memberField = new TextField();
      memberField.setPromptText("Member name");

      Button validateButton = new Button("Validate");
      Label validLabel = new Label("Valid");
      validLabel.setStyle("-fx-text-fill: green;");

      Button removeButton = new Button("Remove");
      removeButton.setOnAction(ev -> {
        memberContainer.getChildren().remove(memberRow);
        memberRowMap.remove(memberField.getText().trim().toLowerCase());
      });

      validateButton.setOnAction(ev -> {
        String username = memberField.getText().trim();
        if (username.isEmpty()) {
          showAlert("Validation Error", "Username cannot be empty.");
          return;
        }
        memberRowMap.put(username.toLowerCase(), memberRow);
        viewModel.requestUserLookup(username);
      });

      memberRow.getChildren().addAll(memberField, validateButton, removeButton);
      memberContainer.getChildren().add(memberRow);
    };

    if (groupToEdit.getMembers() != null) {
      for (String member : groupToEdit.getMembers()) {
        HBox memberRow = new HBox(10);
        TextField memberField = new TextField(member);
        memberField.setEditable(false);

        Label validLabel = new Label("Valid");
        validLabel.setStyle("-fx-text-fill: green;");
        Button removeButton = new Button("Remove");
        removeButton.setOnAction(ev -> {
          //TODO: remove from database
          memberContainer.getChildren().remove(memberRow);
          memberRowMap.remove(member.toLowerCase());
        });

        memberRow.getChildren().addAll(memberField, validLabel, removeButton);
        memberContainer.getChildren().add(memberRow);
      }
    } else {
      addMemberRow.run();
      addMemberRow.run();
    }

    Button addMemberButton = new Button("Add Member");
    addMemberButton.setOnAction(e -> addMemberRow.run());

    Button saveGroupButton = new Button("Save Group");
    GroupEntry finalGroupToEdit = groupToEdit;
    saveGroupButton.setOnAction(e -> {
      List<String> memberNames = new ArrayList<>();

      for (var node : memberContainer.getChildren()) {
        if (node instanceof HBox hbox && !hbox.getChildren().isEmpty()) {
          boolean stillHasValidateButton = hbox.getChildren().stream()
              .anyMatch(child -> child instanceof Button b && "Validate".equals(b.getText()));

          if (stillHasValidateButton) {
            showAlert("Validation Error", "All members must be validated before saving.");
            return;
          }

          TextField tf = (TextField) hbox.getChildren().get(0);
          String name = tf.getText().trim();
          if (!name.isEmpty()) {
            memberNames.add(name);
          }
        }
      }

      if (memberNames.isEmpty()) {
        showAlert("Validation Error", "Add at least one validated member.");
        return;
      }

      finalGroupToEdit.setMembers(memberNames);
      viewModel.sendGroupToServer();
      groupTable.refresh();
      popupStage.close();
    });


    HBox buttonBar = new HBox(10, addMemberButton, saveGroupButton);
    root.getChildren().addAll(nameBox, new Label("Group Members:"), memberContainer, buttonBar);

    Scene scene = new Scene(root, 450, 400);
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

  private void setupGroupTable() {
    groupTable = new TableView<>();
    groupTable.setEditable(false);
    groupTable.setItems(groupData);
    groupTable.setPrefHeight(300);

    TableColumn<GroupEntry, String> nameColumn = new TableColumn<>("Group Name");
    nameColumn.setCellValueFactory(data -> data.getValue().groupNameProperty());
    nameColumn.setPrefWidth(250);

    TableColumn<GroupEntry, Void> configColumn = new TableColumn<>("Configuration");
    configColumn.setCellFactory(col -> new TableCell<>() {
      private final Button btn = new Button("Configure");

      {
        btn.setOnAction(e -> {
          GroupEntry group = getTableView().getItems().get(getIndex());
          openGroupPopup(group);
        });
      }

      @Override
      protected void updateItem(Void item, boolean empty) {
        super.updateItem(item, empty);
        setGraphic(empty ? null : btn);
      }
    });
    configColumn.setPrefWidth(150);

    TableColumn<GroupEntry, Void> removeColumn = new TableColumn<>("Remove Group");
    removeColumn.setCellFactory(col -> new TableCell<>() {
      private final Button removeButton = new Button("Remove");

      {
        removeButton.setOnAction(e -> {
          GroupEntry entry = getTableView().getItems().get(getIndex());
          groupData.remove(entry);
        });
      }

      @Override
      protected void updateItem(Void item, boolean empty) {
        super.updateItem(item, empty);
        setGraphic(empty ? null : removeButton);
      }
    });
    removeColumn.setPrefWidth(150);

    groupTable.getColumns().addAll(nameColumn, configColumn, removeColumn);
  }

  // Called when Model returns a user lookup result
  @Override
  public void propertyChange(PropertyChangeEvent evt) {
    switch (evt.getPropertyName()) {
      case "LookupUserResults" -> {
        Profile profile = (Profile) evt.getNewValue();
        Platform.runLater(() -> {
          String username = profile.getUsername().trim().toLowerCase();
          HBox memberRow = memberRowMap.get(username);
          if (memberRow != null) {
            if (profile.getId() == -1) {
              showAlert("User Lookup Failed", "User not found.");
            } else {
              viewModel.addMemberToGroup(profile);
              int index = -1;
              for (int i = 0; i < memberRow.getChildren().size(); i++) {
                if (memberRow.getChildren().get(i) instanceof Button b &&
                    "Validate".equals(b.getText())) {
                  index = i;
                  break;
                }
              }
              if (index != -1) {
                Label validLabel = new Label("Valid");
                validLabel.setStyle("-fx-text-fill: green;");
                memberRow.getChildren().remove(index);
                memberRow.getChildren().add(index, validLabel);
              }
            }
          }
        });
      }

      case "receiveUserGroups" -> {
        List<UserGroup> groups = (List<UserGroup>) evt.getNewValue();
        Platform.runLater(() -> {
          groupData.clear(); // clear previous entries

          for (UserGroup group : groups) {
            GroupEntry entry = new GroupEntry(group.getGroupName());
            List<String> memberNames = group.getMembers().stream()
                .map(Profile::getUsername)
                .toList();
            entry.setMembers(memberNames);
            groupData.add(entry);
          }

          groupTable.refresh();
        });
      }
    }
  }


  public static class GroupEntry {
    private final SimpleStringProperty groupName = new SimpleStringProperty();
    private List<String> members = new ArrayList<>();

    public GroupEntry(String name) {
      this.groupName.set(name);
    }

    public String getGroupName() {
      return groupName.get();
    }

    public void setGroupName(String name) {
      groupName.set(name);
    }

    public SimpleStringProperty groupNameProperty() {
      return groupName;
    }

    public List<String> getMembers() {
      return members;
    }

    public void setMembers(List<String> members) {
      this.members = members;
    }
  }
}
