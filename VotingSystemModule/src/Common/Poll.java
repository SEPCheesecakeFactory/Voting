package Common;

import java.io.Serializable;

// This could easily be a record class if we embrace immutability (retrieved from the server anyway, so... why mutable?)
public class Poll implements Serializable
{
  private String title;
  private String description;
  private int id;
  private Question[] questions;
  private boolean isClosed;

  public Poll(String title, String description, int id, Question[] questions, boolean isClosed)
  {
    this.title = title;
    this.description = description;
    this.id = id;
    this.questions = questions;
    this.isClosed = isClosed;
  }

  public Poll(String title, String description, int id, Question[] questions) {
    this(title, description, id, questions, false); // default to not closed
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

  public int getId()
  {
    return id;
  }

  public void setId(int id)
  {
    this.id = id;
  }

  public Question[] getQuestions()
  {
    return questions;
  }

  public void setQuestions(Question[] questions)
  {
    this.questions = questions;
  }

  public boolean isClosed() {
    return isClosed;
  }
  public void setClosed(boolean closed) {
    isClosed = closed;
  }

  public void closePoll() {
    this.isClosed = true;
  }
}
