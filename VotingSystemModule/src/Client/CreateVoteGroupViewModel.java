package Client;

import Common.Profile;
import Common.UserGroup;

public class CreateVoteGroupViewModel {
  private Model model;
  private UserGroup currentGroup;
  public CreateVoteGroupViewModel(Model model) {
    this.model = model;
  }

  public void createGroup(String groupName)
  {
    currentGroup = new UserGroup(groupName);
  }

  public boolean addMemberToGroup(Profile profile)
  {
    return currentGroup != null && currentGroup.addMember(profile);
  }

  public boolean removeMemberFromGroup(Profile profile)
  {
    return currentGroup != null && currentGroup.removeMember(profile);
  }

  public UserGroup getCurrentGroup()
  {
    return currentGroup;
  }

  public void sendGroupToServer()
  {
    if (currentGroup != null)
    {
      model.sendVoteGroup(currentGroup);
    }
  }
}