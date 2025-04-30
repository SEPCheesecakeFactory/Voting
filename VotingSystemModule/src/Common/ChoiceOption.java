package Common;

import java.io.Serializable;

// This could easily be a record class if we embrace immutability (retrieved from the server anyway, so... why mutable?)
public class ChoiceOption implements Serializable
{
  private int id;
  private String value;

  public ChoiceOption(int id, String value)
  {
    this.id=id;
    this.value=value;
  }

  public int getId()
  {
    return id;
  }

  public void setId(int id)
  {
    this.id = id;
  }

  public String getValue()
  {
    return value;
  }

  public void setValue(String value)
  {
    this.value = value;
  } // needed?
}
