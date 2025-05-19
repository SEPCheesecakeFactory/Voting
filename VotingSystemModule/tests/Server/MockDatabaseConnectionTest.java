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

  // Z - Zero: Retrieve poll that doesn't exist
  @Test
  void retrievePoll_Z_nonexistentId_returnsNull() {
    assertNull(db.retrievePoll(999));
  }

  // O - One: Store and retrieve single vote
  @Test
  void storeVote_O_oneVote_storesAndRetrievesCorrectly() {
    Vote vote = new Vote(1, new int[]{101});
    db.storeVote(vote);
    assertEquals(vote, db.votes.get(1));
  }

  // M - Many: Store multiple votes
  @Test
  void storeVote_M_multipleVotes_allStoredCorrectly() {
    db.storeVote(new Vote(1, new int[]{101}));
    db.storeVote(new Vote(2, new int[]{102}));
    assertEquals(2, db.votes.size());
  }

  // B - Boundary: Change username to existing one should fail
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

  // I - Interface: Use interface method
  @Test
  void userHasAccessToPoll_I_existingPoll_returnsTrue() {
    Poll poll = new Poll();
    poll.setId(10);
    poll.setQuestions(new Question[0]);
    db.polls.put(10, poll);

    assertTrue(db.userHasAccessToPoll(1, 10));
  }

  // E - Exception: changeUsername with invalid ID
  @Test
  void changeUsername_E_invalidId_throwsException() {
    Profile ghost = new Profile("Ghost");
    ghost.setId(999);
    assertThrows(IllegalArgumentException.class, () -> db.changeUsername(ghost));
  }

  // S - State: Clear database resets all
  @Test
  void clear_S_resetsInternalState() {
    db.polls.put(1, new Poll());
    db.votes.put(1, new Vote(1, new int[]{1}));
    db.clear();
    assertTrue(db.polls.isEmpty());
    assertTrue(db.votes.isEmpty());
  }
}
