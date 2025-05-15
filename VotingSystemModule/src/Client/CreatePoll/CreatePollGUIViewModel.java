package Client.CreatePoll;

import Common.Poll;

import java.util.List;

public class CreatePollGUIViewModel
{

  private String pollTitle = "";
  private String pollDescription = "";
  private List<CreatePollViewModel.Question> questions;
  private CreatePollService service;
  private boolean isPrivate = false;

  public boolean isPrivate()
  {
    return isPrivate;
  }

  public void togglePrivacy()
  {
    isPrivate = !isPrivate;
  }

  // fake
  public CreatePollGUIViewModel(CreatePollService service)
  {
    this.service =service;
//    {
//      public void createPoll(Poll poll)
//      {
//        System.out.println("Poll created: " + poll.getTitle());
//      }
//    };
  }

  public void setPollTitle(String title)
  {
    this.pollTitle = title;
  }

  public void setPollDescription(String description)
  {
    this.pollDescription = description;
  }

  public void setQuestions(List<CreatePollViewModel.Question> questions)
  {
    this.questions = questions;
  }

  public void createPoll()
  {
    if (questions == null || questions.isEmpty())
    {
      throw new IllegalStateException("No questions to create poll");
    }

    Common.Question[] qArray = new Common.Question[questions.size()];

    for (int i = 0; i < questions.size(); i++)
    {
      CreatePollViewModel.Question localQ = questions.get(i);
      var choiceTexts = localQ.getChoices();
      Common.ChoiceOption[] options = new Common.ChoiceOption[choiceTexts.size()];
      for (int j = 0; j < choiceTexts.size(); j++)
      {
        options[j] = new Common.ChoiceOption(j, choiceTexts.get(j));
      }
      qArray[i] = new Common.Question(options, i, localQ.getTitle(), "");
    }

    Poll poll = new Poll(pollTitle, pollDescription, 0, qArray, false);
    service.createPoll(poll);
    poll.setPrivate(isPrivate);
  }

}
