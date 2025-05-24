package Server;

import Common.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class MockDatabaseConnectionTest {

  private MockDatabaseConnection db;

  @BeforeEach
  void setUp() {
    db = new MockDatabaseConnection();
  }


  @Test
  void retrievePoll_Z_nonexistentId_returnsNull() {
    assertNull(db.retrievePoll(999));
  }

  // Z – Zero: Store a vote with an empty array (no choices)
  @Test
  void storeVote_Z_emptyVoteChoices_doesNotFail() {
    Vote vote = new Vote(1, new int[]{}); // no choices
    assertDoesNotThrow(() -> db.storeVote(vote));
    assertEquals(vote, db.votes.get(1));
  }

  // O - One: Store and retrieve single vote
  @Test
  void storeVote_O_oneVote_storesAndRetrievesCorrectly() {
    Vote vote = new Vote(1, new int[]{101});
    db.storeVote(vote);
    assertEquals(vote, db.votes.get(1));
  }

  // M – Many: Store a vote with multiple choices
  @Test
  void storeVote_M_multipleChoices_storedCorrectly() {
    Vote vote = new Vote(2, new int[]{201, 202, 203});
    db.storeVote(vote);
    assertEquals(vote, db.votes.get(2));
    assertEquals(3, vote.getChoices().length);
  }

  // B – Boundary: Use choice IDs at edge values (e.g., 0, Integer.MAX_VALUE)
  @Test
  void storeVote_B_boundaryValues_storedCorrectly() {
    Vote vote = new Vote(3, new int[]{0, Integer.MAX_VALUE});
    db.storeVote(vote);
    assertEquals(vote, db.votes.get(3));
  }

  // I – Interface: Uses public Vote interface and methods
  @Test
  void storeVote_I_interfaceUsedCorrectly() {
    Vote vote = new Vote(4, new int[]{500});
    db.storeVote(vote);

    Vote stored = db.votes.get(4);
    assertNotNull(stored);
    assertEquals(4, stored.getUserId());
    assertArrayEquals(new int[]{500}, stored.getChoices());
  }

  @Test
  void storeVote_M_multipleVotes_allStoredCorrectly() {
    db.storeVote(new Vote(1, new int[]{101}));
    db.storeVote(new Vote(2, new int[]{102}));
    assertEquals(2, db.votes.size());
  }


  @Test
  void changeUsername_B_duplicateUsername_throwsException() {
    Profile p1 = new Profile("Alice");
    Profile p2 = new Profile("Bob");

    int id1 = db.loginOrRegisterAProfile(p1);
    int id2 = db.loginOrRegisterAProfile(p2);

    Profile change = new Profile("Alice");
    change.setId(id2);  // attempt to rename Bob to Alice

    assertThrows(IllegalArgumentException.class, () -> db.changeUsername(change));
  }

  // E – Exception: Null vote should throw (invalid input)
  @Test
  void storeVote_E_nullVote_throwsException() {
    assertThrows(IllegalArgumentException.class, () -> db.storeVote(null));
  }

  // S – Simple: Typical valid vote submission
  @Test
  void storeVote_S_validVote_succeeds() {
    Vote vote = new Vote(5, new int[]{111});
    assertDoesNotThrow(() -> db.storeVote(vote));
    assertEquals(vote, db.votes.get(5));
  }
  @Test
  void userHasAccessToPoll_I_existingPoll_returnsTrue() {
    Poll poll = new Poll();
    poll.setId(10);
    poll.setQuestions(new Question[0]);
    db.polls.put(10, poll);

    assertTrue(db.userHasAccessToPoll(1, 10));
  }


  @Test
  void changeUsername_E_invalidId_throwsException() {
    Profile ghost = new Profile("Ghost");
    ghost.setId(999);
    assertThrows(IllegalArgumentException.class, () -> db.changeUsername(ghost));
  }


  @Test
  void clear_S_resetsInternalState() {
    db.polls.put(1, new Poll());
    db.votes.put(1, new Vote(1, new int[]{1}));
    db.clear();
    assertTrue(db.polls.isEmpty());
    assertTrue(db.votes.isEmpty());
  }
}
