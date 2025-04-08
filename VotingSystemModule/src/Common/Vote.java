package Common;

import java.util.Arrays;

// This could easily be a record class if we embrace immutability (why mutable?)
public class Vote
{
  private int userId;
  private int[] choices;

  public Vote(int userId, int[] choices){
    this.userId=userId;
    this.choices=choices;
  }

  public int getUserId() { return userId; }
  public int[] getChoices() { return choices; }

  @Override public String toString()
  {
    return "Vote{" + "userId=" + userId + ", choices=" + Arrays.toString(
        choices) + '}';
  }
}
