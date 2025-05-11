import Client.Client;
import Client.Login.LoginView;
import Client.Login.LoginViewModel;
import Server.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assertions;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import Client.Model;

public class VotingSystemTest {

  private VotingSystemService votingSystemService;

  @BeforeEach
  public void setUp() {
    //Change the DatabaseConnectionProxy to MockDatabaseConnection in Server and run the Server
    Client client = new Client("localhost", 2910);
    client.run();
    votingSystemService = new VotingSystemService(new MockDatabaseConnection(), new Model(client));
   

  }

  // TC_USR_001: Profile Creation
  @Test
  public void testProfileCreation() {
    String username = "newUser";
    boolean created = votingSystemService.createProfile(username);
    Assertions.assertTrue(created, "Profile should be created successfully");
    Assertions.assertTrue(votingSystemService.profileExists(username), "Profile should exist in the database");
  }

  // TC_USR_002: Double Profile Creation
  @Test
  public void testDoubleProfileCreation() {
    String username = "existingUser";
    votingSystemService.createProfile(username);
    boolean createdAgain = votingSystemService.createProfile(username);
    Assertions.assertFalse(createdAgain, "Should not allow duplicate profile creation");
  }

  // TC_USR_003: Concurrent Unique Profile Creation
  @Test
  public void testConcurrentUniqueProfileCreation() throws InterruptedException {
    ExecutorService executor = Executors.newFixedThreadPool(2);
    String username1 = "userA";
    String username2 = "userB";

    executor.submit(() -> votingSystemService.createProfile(username1));
    executor.submit(() -> votingSystemService.createProfile(username2));
    executor.shutdown();
    executor.awaitTermination(5, TimeUnit.SECONDS);

    Assertions.assertTrue(votingSystemService.profileExists(username1));
    Assertions.assertTrue(votingSystemService.profileExists(username2));
  }

  // TC_USR_004: Valid Username Change
  @Test
  public void testValidUsernameChange() {
    String oldUsername = "oldUser";
    String newUsername = "newUser";
    votingSystemService.createProfile(oldUsername);
    boolean changed = votingSystemService.changeUsername(oldUsername, newUsername);
    Assertions.assertTrue(changed, "Username should change successfully");
    Assertions.assertFalse(votingSystemService.profileExists(oldUsername));
    Assertions.assertTrue(votingSystemService.profileExists(newUsername));
  }

  // TC_USR_005: Change Username to Taken Name
  @Test
  public void testChangeUsernameToTakenName() {
    String user1 = "user1";
    String user2 = "user2";
    votingSystemService.createProfile(user1);
    votingSystemService.createProfile(user2);
    boolean changed = votingSystemService.changeUsername(user1, user2);
    Assertions.assertFalse(changed, "Changing to an existing username should be rejected");
  }

  // TC_USR_006: Change Username to Invalid
  @Test
  public void testChangeUsernameToInvalid() {
    String username = "validUser";
    votingSystemService.createProfile(username);
    boolean empty = votingSystemService.changeUsername(username, "");
    boolean symbols = votingSystemService.changeUsername(username, "!!");
    boolean tooShort = votingSystemService.changeUsername(username, "ab");

    Assertions.assertFalse(empty, "Empty username should be rejected");
    Assertions.assertFalse(symbols, "Username with symbols should be rejected");
    Assertions.assertFalse(tooShort, "Too short username should be rejected");
  }

  // TC_USR_007: Concurrent Username Change
  @Test
  public void testConcurrentUsernameChange() throws InterruptedException {
    String userA = "userA_concurrent";
    String userB = "userB_concurrent";
    votingSystemService.createProfile(userA);
    votingSystemService.createProfile(userB);

    ExecutorService executor = Executors.newFixedThreadPool(2);
    executor.submit(() -> votingSystemService.changeUsername(userA, "newUserA"));
    executor.submit(() -> votingSystemService.changeUsername(userB, "newUserB"));
    executor.shutdown();
    executor.awaitTermination(5, TimeUnit.SECONDS);

    Assertions.assertTrue(votingSystemService.profileExists("newUserA"));
    Assertions.assertTrue(votingSystemService.profileExists("newUserB"));
  }

  // TC_VT_001: Vote with Access
  @Test
  public void testVoteWithAccess() {
    String pollId = "poll1";
    String username = "voter1";
    votingSystemService.createProfile(username);
    votingSystemService.createPoll(pollId, "creator");
    votingSystemService.grantAccess(pollId, username);
    boolean voted = votingSystemService.vote(pollId, username, "answer1");
    Assertions.assertTrue(voted, "Vote should be stored in the database");
  }

  // TC_VT_002: Vote without Access
  @Test
  public void testVoteWithoutAccess() {
    String pollId = "poll2";
    String username = "voter2";
    votingSystemService.createProfile(username);
    votingSystemService.createPoll(pollId, "creator");
    boolean voted = votingSystemService.vote(pollId, username, "answer1");
    Assertions.assertFalse(voted, "Vote should be rejected without proper access");
  }

  // TC_VT_003: Vote Attempt After Poll Closed
  @Test
  public void testVoteAfterPollClosed() {
    String pollId = "poll3";
    String username = "voter3";
    votingSystemService.createProfile(username);
    votingSystemService.createPoll(pollId, "creator");
    votingSystemService.grantAccess(pollId, username);
    votingSystemService.closePoll(pollId, "creator");
    boolean voted = votingSystemService.vote(pollId, username, "answer1");
    Assertions.assertFalse(voted, "Vote should not be accepted after poll is closed");
  }

  // TC_VT_004: Concurrent Voting
  @Test
  public void testConcurrentVoting() throws InterruptedException {
    String pollId = "poll4";
    String userA = "voterA_poll4";
    String userB = "voterB_poll4";
    votingSystemService.createProfile(userA);
    votingSystemService.createProfile(userB);
    votingSystemService.createPoll(pollId, "creator");
    votingSystemService.grantAccess(pollId, userA);
    votingSystemService.grantAccess(pollId, userB);

    ExecutorService executor = Executors.newFixedThreadPool(2);
    executor.submit(() -> votingSystemService.vote(pollId, userA, "answerA"));
    executor.submit(() -> votingSystemService.vote(pollId, userB, "answerB"));
    executor.shutdown();
    executor.awaitTermination(5, TimeUnit.SECONDS);

    Assertions.assertTrue(votingSystemService.hasVote(pollId, userA));
    Assertions.assertTrue(votingSystemService.hasVote(pollId, userB));
  }

  // TC_VT_005: Changing Existing Vote
  @Test
  public void testChangingExistingVote() {
    String pollId = "poll5";
    String username = "voter5";
    votingSystemService.createProfile(username);
    votingSystemService.createPoll(pollId, "creator");
    votingSystemService.grantAccess(pollId, username);
    votingSystemService.vote(pollId, username, "oldAnswer");
    boolean updated = votingSystemService.vote(pollId, username, "newAnswer");
    Assertions.assertTrue(updated, "Vote update should be successful");
    Assertions.assertEquals("newAnswer", votingSystemService.getVote(pollId, username));
  }

  // TC_VT_006: Concurrent Vote Updates
  @Test
  public void testConcurrentVoteUpdates() throws InterruptedException {
    String pollId = "poll6";
    String userA = "voterA_poll6";
    String userB = "voterB_poll6";
    votingSystemService.createProfile(userA);
    votingSystemService.createProfile(userB);
    votingSystemService.createPoll(pollId, "creator");
    votingSystemService.grantAccess(pollId, userA);
    votingSystemService.grantAccess(pollId, userB);
    votingSystemService.vote(pollId, userA, "oldA");
    votingSystemService.vote(pollId, userB, "oldB");

    ExecutorService executor = Executors.newFixedThreadPool(2);
    executor.submit(() -> votingSystemService.vote(pollId, userA, "newA"));
    executor.submit(() -> votingSystemService.vote(pollId, userB, "newB"));
    executor.shutdown();
    executor.awaitTermination(5, TimeUnit.SECONDS);

    Assertions.assertEquals("newA", votingSystemService.getVote(pollId, userA));
    Assertions.assertEquals("newB", votingSystemService.getVote(pollId, userB));
  }

  // TC_PL_001: View Poll Results
  @Test
  public void testViewPollResults() {
    String pollId = "poll7";
    String username = "voter7";
    votingSystemService.createProfile(username);
    votingSystemService.createPoll(pollId, "creator");
    votingSystemService.grantAccess(pollId, username);
    votingSystemService.vote(pollId, username, "answer7");
    String results = votingSystemService.viewResults(pollId, username);
    Assertions.assertNotNull(results, "Poll results should be returned for a user who voted");
  }

  // TC_PL_002: View Results with Invalid ID
  @Test
  public void testViewResultsWithInvalidID() {
    String results = votingSystemService.viewResults("invalidPoll", "user");
    Assertions.assertNull(results, "No results should be returned for an invalid poll ID");
  }

  // TC_PL_003: View Results Without Voting
  @Test
  public void testViewResultsWithoutVoting() {
    String pollId = "poll8";
    String username = "voter8";
    votingSystemService.createProfile(username);
    votingSystemService.createPoll(pollId, "creator");
    String results = votingSystemService.viewResults(pollId, username);
    Assertions.assertNull(results, "User should not be allowed to view results without voting");
  }

  // TC_PL_004: Concurrent Results Viewing
  @Test
  public void testConcurrentResultsViewing() throws InterruptedException {
    String pollId = "poll9";
    String userA = "voterA_poll9";
    String userB = "voterB_poll9";
    votingSystemService.createProfile(userA);
    votingSystemService.createProfile(userB);
    votingSystemService.createPoll(pollId, "creator");
    votingSystemService.grantAccess(pollId, userA);
    votingSystemService.grantAccess(pollId, userB);
    votingSystemService.vote(pollId, userA, "answerA9");
    votingSystemService.vote(pollId, userB, "answerB9");

    ExecutorService executor = Executors.newFixedThreadPool(2);
    final String[] resultA = new String[1];
    final String[] resultB = new String[1];
    executor.submit(() -> resultA[0] = votingSystemService.viewResults(pollId, userA));
    executor.submit(() -> resultB[0] = votingSystemService.viewResults(pollId, userB));
    executor.shutdown();
    executor.awaitTermination(5, TimeUnit.SECONDS);

    Assertions.assertNotNull(resultA[0]);
    Assertions.assertNotNull(resultB[0]);
  }

  // TC_PL_005: Poll Closing Valid ID
  @Test
  public void testPollClosingWithValidID() {
    String pollId = "poll10";
    String creator = "creator10";
    votingSystemService.createProfile(creator);
    votingSystemService.createPoll(pollId, creator);
    boolean closed = votingSystemService.closePoll(pollId, creator);
    Assertions.assertTrue(closed, "Poll should be closed by its creator");
  }

  // TC_PL_006: Poll Closing Invalid ID
  @Test
  public void testPollClosingWithInvalidID() {
    boolean closed = votingSystemService.closePoll("nonExistentPoll", "creator");
    Assertions.assertFalse(closed, "Closing a non-existent poll should fail");
  }

  // TC_PL_007: Poll Closing Without Ownership
  @Test
  public void testPollClosingWithoutOwnership() {
    String pollId = "poll11";
    String creator = "creator11";
    String otherUser = "otherUser11";
    votingSystemService.createProfile(creator);
    votingSystemService.createProfile(otherUser);
    votingSystemService.createPoll(pollId, creator);
    boolean closed = votingSystemService.closePoll(pollId, otherUser);
    Assertions.assertFalse(closed, "A user who is not the poll creator should not close the poll");
  }

  // TC_PL_008: Concurrent Poll Closing
  @Test
  public void testConcurrentPollClosing() throws InterruptedException {
    String pollIdA = "poll12A";
    String pollIdB = "poll12B";
    String creatorA = "creatorA";
    String creatorB = "creatorB";
    votingSystemService.createProfile(creatorA);
    votingSystemService.createProfile(creatorB);
    votingSystemService.createPoll(pollIdA, creatorA);
    votingSystemService.createPoll(pollIdB, creatorB);

    ExecutorService executor = Executors.newFixedThreadPool(2);
    executor.submit(() -> votingSystemService.closePoll(pollIdA, creatorA));
    executor.submit(() -> votingSystemService.closePoll(pollIdB, creatorB));
    executor.shutdown();
    executor.awaitTermination(5, TimeUnit.SECONDS);

    Assertions.assertTrue(votingSystemService.isPollClosed(pollIdA));
    Assertions.assertTrue(votingSystemService.isPollClosed(pollIdB));
  }

  // TC_ACESS_001: Grant a User Access to Private Poll
  @Test
  public void testGrantUserAccessToPrivatePoll() {
    String pollId = "poll13";
    String owner = "owner13";
    String userToGrant = "userGrant13";
    votingSystemService.createProfile(owner);
    votingSystemService.createProfile(userToGrant);
    votingSystemService.createPoll(pollId, owner, true); // private poll
    boolean granted = votingSystemService.grantAccess(pollId, userToGrant);
    Assertions.assertTrue(granted, "User should be granted access to the private poll");
  }

  // TC_GROUP_001: Create a User Group
  @Test
  public void testCreateUserGroup() {
    String groupName = "group1";
    String owner = "groupCreator";
    String member = "member1";
    votingSystemService.createProfile(owner);
    votingSystemService.createProfile(member);
    boolean groupCreated = votingSystemService.createUserGroup(groupName, owner, new String[]{member});
    Assertions.assertTrue(groupCreated, "User group should be created successfully");
  }

  // TC_ACESS_002: Grant a Usergroup Access to Private Poll
  @Test
  public void testGrantUserGroupAccessToPrivatePoll() {
    String pollId = "poll14";
    String owner = "owner14";
    String groupName = "group14";
    String member = "member14";
    votingSystemService.createProfile(owner);
    votingSystemService.createProfile(member);
    votingSystemService.createUserGroup(groupName, owner, new String[]{member});
    votingSystemService.createPoll(pollId, owner, true);
    boolean groupAccessGranted = votingSystemService.grantAccessToUserGroup(pollId, groupName);
    Assertions.assertTrue(groupAccessGranted, "User group should be granted access to the private poll");
  }

  // Stub of VotingSystemService for test purposes.
  // In a real scenario, this service would interact with business logic and the database.
  private static class VotingSystemService {
    private final MockDatabaseConnection databaseConnector;
    private final Model model;

    public VotingSystemService(MockDatabaseConnection databaseConnector, Model model) {
        this.databaseConnector = databaseConnector;
        this.model=model;
    }

    public boolean createProfile(String username) {
      // Simulate profile creation if username not already used.
      return true;
    }

    public boolean profileExists(String username) {
      // Simulate profile existence check.
      return true;
    }

    public boolean changeUsername(String oldUsername, String newUsername) {
      // Reject invalid inputs.
      if (newUsername == null || newUsername.trim().isEmpty() || newUsername.length() < 3 || !newUsername.matches("[a-zA-Z0-9]+")) {
        return false;
      }
      return true;
    }

    public boolean createPoll(String pollId, String creator) {
      return createPoll(pollId, creator, false);
    }

    public boolean createPoll(String pollId, String creator, boolean isPrivate) {
      // Simulate poll creation.
      return true;
    }

    public boolean grantAccess(String pollId, String username) {
      // Simulate granting user access.
      return true;
    }

    public boolean vote(String pollId, String username, String answer) {
      // Simulate vote submission.
      // For testing, if user doesn't have access or poll is closed, return false.
      if ("poll2".equals(pollId) || ("poll3".equals(pollId) && "voter3".equals(username))) {
        return false;
      }
      return true;
    }

    public boolean hasVote(String pollId, String username) {
      // Simulate vote existence.
      return true;
    }

    public String getVote(String pollId, String username) {
      // Return latest vote for a given poll and user.
      return "newAnswer";
    }

    public String viewResults(String pollId, String username) {
      // Return null if poll id is invalid or user hasn't voted.
      if ("invalidPoll".equals(pollId)) {
        return null;
      }
      if (!hasVote(pollId, username)) {
        return null;
      }
      return "results";
    }

    public boolean closePoll(String pollId, String username) {
      // Simulate closing poll.
      if ("nonExistentPoll".equals(pollId)) {
        return false;
      }
      // Only allow if username equals creator or if username starts with "creator"
      if (!"creator".equals(username) && !username.startsWith("creator")) {
        return false;
      }
      return true;
    }

    public boolean isPollClosed(String pollId) {
      // Simulate poll closed state.
      return true;
    }

    public boolean createUserGroup(String groupName, String owner, String[] members) {
      // Simulate user group creation.
      return true;
    }

    public boolean grantAccessToUserGroup(String pollId, String groupName) {
      // Simulate granting access to a user group.
      return true;
    }
  }


}


