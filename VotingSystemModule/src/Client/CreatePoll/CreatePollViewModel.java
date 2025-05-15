package Client.CreatePoll;

import Common.Poll;
import Common.Question;

import java.util.*;

/** Simple backing model for poll creation, now with choices. */
public class CreatePollViewModel {
  private String pollTitle = "";
  private String pollDescription = "";
  private final List<Question> questions = new ArrayList<Question>();
  private CreatePollService model;
  private boolean isPrivate = false;

  public CreatePollViewModel(CreatePollService model)
  {
    this.model = model;
  }
  public void setPrivate(boolean isPrivate)
  {
    this.isPrivate = isPrivate;
  }


  public void setPollTitle(String title) {
    this.pollTitle = title;
  }
  public String getPollTitle() {
    return pollTitle;
  }

  public void setPollDescription(String description) {
    this.pollDescription = description;
  }
  public String getPollDescription() {
    return pollDescription;
  }

  /** Adds a blank question, returns its index. */
  public int addQuestion() {
    questions.add(new Question());
    return questions.size() - 1;
  }

  /** Sets title on question; returns false if out-of-bounds. */
  public boolean setQuestionTitle(int index, String title) {
    if (index < 0 || index >= questions.size()) return false;
    questions.get(index).setTitle(title);
    return true;
  }

  /** Adds a choice to a question; returns choice index or -1 if invalid question. */
  public int addChoice(int questionIndex, String text) {
    if (questionIndex < 0 || questionIndex >= questions.size()) return -1;
    return questions.get(questionIndex).addChoice(text);
  }

  /** Indexed view of current questions. */
  public Map<Integer, Question> getQuestions() {
    Map<Integer, Question> map = new LinkedHashMap<>();
    for (int i = 0; i < questions.size(); i++) {
      map.put(i, questions.get(i));
    }
    return map;
  }

  public boolean isPrivate()
  {
    return isPrivate;
  }

  /** Inner question placeholder, with title and choices. */
  public static class Question {
    private String title = "";
    private final List<String> choices = new ArrayList<>();

    public void setTitle(String t) { this.title = t; }
    public String getTitle() { return title; }

    public int addChoice(String text) {
      choices.add(text);
      return choices.size() - 1;
    }
    public List<String> getChoices() {
      return Collections.unmodifiableList(choices);
    }
  }

  public void createPoll()
  {
    Common.Question[] qArray = new Common.Question[questions.size()];

    for (int i = 0; i < questions.size(); i++) {
      CreatePollViewModel.Question localQ = questions.get(i);

      List<String> choiceTexts = localQ.getChoices();
      Common.ChoiceOption[] options = new Common.ChoiceOption[choiceTexts.size()];

      for (int j = 0; j < choiceTexts.size(); j++) {
        // Using the index `j` as a placeholder ID; in a real app this might come from a DB or counter
        options[j] = new Common.ChoiceOption(j, choiceTexts.get(j));
      }

      qArray[i] = new Common.Question(options, i, localQ.getTitle(), "");
    }

    Poll poll = new Poll(pollTitle, pollDescription, 0, qArray, false);

    poll.setPrivate(isPrivate);
    model.createPoll(poll);
  }
}
