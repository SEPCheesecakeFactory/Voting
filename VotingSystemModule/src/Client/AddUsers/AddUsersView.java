package Client.AddUsers;

import java.util.Scanner;

public class AddUsersView {
  private final AddUsersViewModel vm;

  public AddUsersView(AddUsersViewModel vm) {
    this.vm = vm;
    render();
  }

  public void render() {
    System.out.println("== GRANT ACCESS TO PRIVATE POLL ==");
    Scanner in = new Scanner(System.in);

    int pollId = -1;
    while (pollId < 0) {
      try {
        System.out.print("Enter poll ID: ");
        pollId = Integer.parseInt(in.nextLine().trim());
      } catch (NumberFormatException e) {
        System.out.println("Invalid number. Please enter a valid poll ID.");
      }
    }

    vm.setPollId(pollId);

    while (true) {
      System.out.print("> ");
      String input = in.nextLine().trim();
      if (input.equalsIgnoreCase("done")) break;

      if (input.startsWith("add user ")) {
        String username = input.substring(9).trim();
        vm.addUser(username);
        System.out.printf("-> looking up user '%s'...\n", username);
      } else if (input.startsWith("add group ")) {
        String group = input.substring(10).trim();
        vm.addGroup(group);
        System.out.printf("-> looking up group '%s'...\n", group);
      } else {
        System.out.println("-> unknown command. Use 'add user <username>' or 'add group <groupname>'");
      }

      System.out.println("   Please wait for confirmation before typing 'done'.");
    }

    vm.saveAccess();
    System.out.println("-> access granted");
  }
}
