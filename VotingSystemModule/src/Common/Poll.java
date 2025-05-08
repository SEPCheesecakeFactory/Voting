package Common;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

// This could easily be a record class if we embrace immutability (retrieved from the server anyway, so... why mutable?)
public class Poll implements Serializable
{
  private String title;
  private String description;
  private int id;
  private Question[] questions;
  private boolean isClosed;
  private boolean isPrivate;

  private List<Profile> allowedUsers;
  private List<UserGroup> allowedGroups;

  public Poll(String title, String description, int id, Question[] questions,
      boolean isClosed)
  {
    this.title = title;
    this.description = description;
    this.id = id;
    this.questions = questions;
    this.isClosed = isClosed;
    this.allowedUsers = new ArrayList<>();
    this.allowedGroups = new ArrayList<>();
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

  public boolean isClosed()
  {
    return isClosed;
  }

  public void closePoll()
  {
    this.isClosed = true;
  }

  public boolean isPrivate()
  {
    return isPrivate;
  }

  public List<Profile> getAllowedUsers()
  {
    return allowedUsers;
  }

  public List<UserGroup> getAllowedGroups()
  {
    return allowedGroups;
  }

  public void addAllowedUser(Profile profile)
  {
    for (Profile p : allowedUsers)
    {
      if (p.getId() == profile.getId())
      {
        return;
      }
    }
    allowedUsers.add(profile);
  }

  public void removeAllowedUser(Profile profile)
  {
    allowedUsers.remove(profile);
  }

  public void addAllowedGroup(UserGroup group)
  {
    for (UserGroup g : allowedGroups)
    {
      if (g.equals(group))
      {
        return;
      }
    }
    allowedGroups.add(group);
  }

  public void removeAllowedGroup(UserGroup group)
  {
    allowedGroups.remove(group);
  }

  public boolean canVote(Profile user)
  {
    if (!isPrivate)
    {
      return true;
    }
    for (Profile allowed : allowedUsers)
    {
      if (allowed.getId() == user.getId())
      {
        return true;
      }
    }
    for (UserGroup group : allowedGroups)
    {
      for (Profile member : group.getMembers())
      {
        if (member.getId() == user.getId())
        {
          return true;
        }
      }
    }
    return false;
  }
}

