package Client.CreatePoll;

import java.util.*;
import java.util.regex.*;

public class CreatePollView {
  private final CreatePollViewModel vm;
  private static final Pattern SET_POLL_TITLE =
      Pattern.compile("^set poll title\\s+(.+)$", Pattern.CASE_INSENSITIVE);
  private static final Pattern SET_Q_TITLE =
      Pattern.compile("^set question\\s+(\\d+)\\s+title\\s+(.+)$", Pattern.CASE_INSENSITIVE);
  private static final Pattern ADD_CHOICE =
      Pattern.compile("^add choice\\s+(\\d+)\\s+(.+)$", Pattern.CASE_INSENSITIVE);
  private static final Pattern SET_PRIVATE =
      Pattern.compile("^set private\\s+(true|false)$", Pattern.CASE_INSENSITIVE);

  public CreatePollView(CreatePollViewModel viewModel) {
    this.vm = viewModel;
  }

  public void render() {
    System.out.println("== POLL CREATOR ==");
    Scanner in = new Scanner(System.in);

    loop: while (true) {
      System.out.print("> ");
      String line = in.nextLine().trim();
      Matcher m;

      if ("exit".equalsIgnoreCase(line) || "done".equalsIgnoreCase(line)) {
        vm.createPoll();
        break;
      }
      else if ((m = SET_POLL_TITLE.matcher(line)).matches()) {
        String title = m.group(1);
        vm.setPollTitle(title);
        System.out.printf("-> title set to \"%s\" for the poll%n", title);
      }
      else if ("add question".equalsIgnoreCase(line)) {
        int id = vm.addQuestion();
        System.out.printf("-> added question id %d%n", id);
      }
      else if ((m = SET_Q_TITLE.matcher(line)).matches()) {
        int qid = Integer.parseInt(m.group(1));
        String qtitle = m.group(2);
        if (vm.setQuestionTitle(qid, qtitle)) {
          System.out.printf("-> set the title of question %d to \"%s\"%n", qid, qtitle);
        } else {
          System.out.printf("-> error: question %d does not exist%n", qid);
        }
      }
      else if ((m = ADD_CHOICE.matcher(line)).matches()) {
        int qid = Integer.parseInt(m.group(1));
        String choice = m.group(2);
        int cid = vm.addChoice(qid, choice);
        if (cid >= 0) {
          System.out.printf("-> added choice id %d to question %d%n", cid, qid);
        } else {
          System.out.printf("-> error: question %d does not exist%n", qid);
        }
      }
      else if ((m = SET_PRIVATE.matcher(line)).matches()) {
        boolean priv = Boolean.parseBoolean(m.group(1));
        vm.setPrivate(priv);
        System.out.printf("-> poll privacy set to %s%n", priv ? "PRIVATE" : "PUBLIC");
      }
      else {
        System.out.println("-> unrecognized command");
      }
    }

    // final summary
    System.out.println("\n=== POLL SUMMARY ===");
    System.out.println("Title: " + vm.getPollTitle());
    System.out.println("Privacy: " + (vm.isPrivate() ? "Private" : "Public"));
    vm.getQuestions().forEach((i, q) -> {
      System.out.printf("Q%d: %s%n", i, q.getTitle());
      List<String> choices = q.getChoices();
      for (int j = 0; j < choices.size(); j++) {
        System.out.printf("   C%d: %s%n", j, choices.get(j));
      }
    });
  }
}
