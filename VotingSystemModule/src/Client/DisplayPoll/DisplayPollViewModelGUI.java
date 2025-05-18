package Client.DisplayPoll;
import Client.Model;
import Client.PropertyChangeSubject;
import Common.ChoiceOption;
import Common.Poll;
import Utils.Logger;
import javafx.application.Platform;
import javafx.beans.property.*;
import javafx.collections.FXCollections;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.List;

public class DisplayPollViewModelGUI implements PropertyChangeListener,
    PropertyChangeSubject
{

  private final StringProperty title = new SimpleStringProperty();
  private final StringProperty description = new SimpleStringProperty();
  private final ListProperty<ChoiceOption> choices = new SimpleListProperty<>(
      FXCollections.observableArrayList());
  private final IntegerProperty currentIndex = new SimpleIntegerProperty(0);
  private final IntegerProperty totalQuestions = new SimpleIntegerProperty(0);

  private Poll currentPoll;
  private Model model;
  private PropertyChangeSupport support = new PropertyChangeSupport(this);

  public DisplayPollViewModelGUI(Model model) {
    this.model = model;
    this.model.addPropertyChangeListener("PollUpdated", this);
  }

  public Model getModel()
  {
    return model;
  }

  public int getUserId()
  {
    return getModel().getProfile().getId();
  }
  public void sendVote(int userId, int[] choices)
  {
    Poll currentPoll = model.getPoll();

    if (currentPoll != null && currentPoll.isClosed())
      Logger.log("Cannot vote: Poll is closed.");

    model.sendVote(userId, choices);
  }


  @Override public void addPropertyChangeListener(
      PropertyChangeListener listener)
  {
    support.addPropertyChangeListener(listener);
  }

  @Override public void addPropertyChangeListener(String name,
      PropertyChangeListener listener)
  {
    support.addPropertyChangeListener(name, listener);
  }

  @Override public void removePropertyChangeListener(
      PropertyChangeListener listener)
  {
    support.removePropertyChangeListener(listener);
  }

  @Override public void removePropertyChangeListener(String name,
      PropertyChangeListener listener)
  {
    support.removePropertyChangeListener(name, listener);
  }


  public StringProperty titleProperty() {
    return title;
  }

  public StringProperty descriptionProperty() {
    return description;
  }

  public ListProperty<ChoiceOption> choicesProperty() {
    return choices;
  }

  public IntegerProperty currentIndexProperty() {
    return currentIndex;
  }

  public IntegerProperty totalQuestionsProperty() {
    return totalQuestions;
  }

  public int getCurrentIndex() {
    return currentIndex.get();
  }

  public int getTotalQuestions() {
    return totalQuestions.get();
  }

  public List<ChoiceOption> getChoices() {
    return choices.get();
  }

  public void nextQuestion() {
    if (currentPoll != null && currentIndex.get() < currentPoll.getQuestions().length - 1) {
      currentIndex.set(currentIndex.get() + 1);
      updateQuestionData();
    }
  }

  public void previousQuestion() {
    if (currentPoll != null && currentIndex.get() > 0) {
      currentIndex.set(currentIndex.get() - 1);
      updateQuestionData();
    }
  }

  private void updateQuestionData() {
    Platform.runLater(()->{
      if (currentPoll == null || currentPoll.getQuestions().length==0) return;

      var question = currentPoll.getQuestions()[currentIndex.get()];
      title.set(question.getTitle());
      Logger.log("Description: " + question.getDescription());

      description.set(question.getDescription());
      choices.set(FXCollections.observableArrayList(question.getChoiceOptions()));
      totalQuestions.set(currentPoll.getQuestions().length);
    });

  }

  @Override
  public void propertyChange(PropertyChangeEvent evt) {
    if ("PollUpdated".equals(evt.getPropertyName()) && evt.getNewValue() instanceof Poll poll) {
      this.currentPoll = poll;
      currentIndex.set(0);
      updateQuestionData();
    }
    support.firePropertyChange(evt);
  }

  public Poll getCurrentPoll()
  {
    return getModel().getPoll();
  }
}
