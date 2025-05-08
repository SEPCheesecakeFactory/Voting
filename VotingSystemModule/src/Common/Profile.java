package Common;

import java.io.Serializable;

public class Profile implements Serializable
{
  private String username;
  private int id;

  public Profile(String username)
  {
    this.username = username;
  }

  public void changeUsername(String username)
  {
    this.username = username;
  }

  public String getUsername()
  {
    return username;
  }

  public void setId(int id)
  {
    this.id = id;
  }

  public int getId()
  {
    return id;
  }
}
