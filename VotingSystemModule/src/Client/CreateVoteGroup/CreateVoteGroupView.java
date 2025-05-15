package Client.CreateVoteGroup;

import Common.Profile;

import java.util.Scanner;

public class CreateVoteGroupView {
  private CreateVoteGroupViewModel viewModel;
  private Scanner scanner = new Scanner(System.in);

  public CreateVoteGroupView(CreateVoteGroupViewModel viewModel) {
    this.viewModel = viewModel;
    render();
  }

  public void render() {
    start();
  }
  private void start()
  {
    System.out.println("=== Create a New User Group ===");
    System.out.print("Enter group name: ");
    String groupName = scanner.nextLine();
    viewModel.createGroup(groupName);

    boolean running = true;
    while (running)
    {
      System.out.println("\nChoose an option:");
      System.out.println("1. Add Member");
      System.out.println("2. Remove Member");
      System.out.println("3. Show Group Members");
      System.out.println("4. Send Group to Server");
      System.out.println("5. Exit");
      System.out.print("Your choice: ");
      String input = scanner.nextLine();

      switch (input)
      {
        case "1":
          addMember();
          break;
        case "2":
          removeMember();
          break;
        case "3":
          showMembers();
          break;
        case "4":
          viewModel.sendGroupToServer();
          System.out.println("Group sent to server.");
          break;
        case "5":
          running = false;
          break;
        default:
          System.out.println("Invalid option. Try again.");
      }
    }
    System.out.println("Exiting group manager.");
  }

  private void addMember() {
    System.out.print("Enter member name: ");
    String name = scanner.nextLine();

    viewModel.requestUserLookup(name); // ask server for the real Profile

    System.out.println("Looking up user '" + name + "' on the server...");
    System.out.println("Please wait for confirmation before continuing.");
  }

  private void removeMember()
  {
    System.out.print("Enter member name to remove: ");
    String name= scanner.nextLine();
    Profile profile = new Profile(name);
    if (viewModel.removeMemberFromGroup(profile))
    {
      System.out.println("Member removed.");
    }
    else
    {
      System.out.println("Member not found.");
    }
  }

  private void showMembers()
  {
    System.out.println("Current group members:");
    viewModel.getCurrentGroup().getMembers().forEach(member ->
        System.out.println("- ID: " + member.getId() + ", Name: " + member.getUsername()));
  }
}