package Common;

// This could easily be a record class if we embrace immutability (retrieved from the server anyway, so... why mutable?)
public class Question
{
  private ChoiceOption[] choiceOptions;
  private int id;
  private String title;
  private String description;

  public Question(ChoiceOption[] choiceOptions, int id, String title,
      String description)
  {
    this.choiceOptions = choiceOptions;
    this.id = id;
    this.title = title;
    this.description = description;
  }

  public ChoiceOption[] getChoiceOptions()
  {
    return choiceOptions;
  }

  public void setChoiceOptions(ChoiceOption[] choiceOptions)
  {
    this.choiceOptions = choiceOptions;
  }

  public int getId()
  {
    return id;
  }

  public void setId(int id)
  {
    this.id = id;
  }

  public String getTitle()
  {
    return title;
  }

  public void setTitle(String title)
  {
    this.title = title;
  }

  public String getDescription()
  {
    return description;
  }

  public void setDescription(String description)
  {
    this.description = description;
  }
}
