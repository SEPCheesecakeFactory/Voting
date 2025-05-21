
package Server;

import Common.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.sql.SQLException;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class DatabaseConnectionProxyTest {

  @Mock
  private DatabaseConnection mockDb;

  private DatabaseConnectionProxy proxy;

  @BeforeEach
  void setup() throws SQLException {
    MockitoAnnotations.openMocks(this);
    proxy = new DatabaseConnectionProxy(mockDb) {
      {
        this.databaseConnection = mockDb;
      }
    };
  }

  @Test
  void storeVote_delegatesToDatabase() {
    Vote vote = mock(Vote.class);
    proxy.storeVote(vote);
    verify(mockDb).storeVote(vote);
  }

  @Test
  void editVote_delegatesToDatabase() {
    Vote vote = mock(Vote.class);
    proxy.editVote(vote);
    verify(mockDb).editVote(vote);
  }

  @Test
  void retrievePoll_returnsExpectedPoll() {
    Poll expected = new Poll();
    when(mockDb.retrievePoll(123)).thenReturn(expected);
    Poll actual = proxy.retrievePoll(123);
    assertSame(expected, actual);
    verify(mockDb).retrievePoll(123);
  }

  @Test
  void retrievePollResults_returnsExpected() {
    PollResult result = mock(PollResult.class);
    when(mockDb.retrievePollResults(5)).thenReturn(result);
    assertSame(result, proxy.retrievePollResults(5));
  }

  @Test
  void loginOrRegisterAProfile_delegatesCorrectly() {
    Profile profile = new Profile("sigmaboy");
    when(mockDb.loginOrRegisterAProfile(profile)).thenReturn(99);
    assertEquals(99, proxy.loginOrRegisterAProfile(profile));
  }

  @Test
  void changeUsername_delegatesCall() {
    Profile profile = new Profile("newtest");
    proxy.changeUsername(profile);
    verify(mockDb).changeUsername(profile);
  }

  @Test
  void storePoll_delegatesAndSetsId() {
    Poll poll = new Poll();
    poll.setQuestions(new Question[0]);
    Profile profile = new Profile("testuser123");
    Poll stored = new Poll();
    stored.setId(55);
    when(mockDb.storePoll(poll, profile)).thenReturn(stored);
    Poll returned = proxy.storePoll(poll, profile);
    assertEquals(55, returned.getId());
  }

  @Test
  void userHasAccessToPoll_delegatesCorrectly() {
    when(mockDb.userHasAccessToPoll(10, 20)).thenReturn(true);
    assertTrue(proxy.userHasAccessToPoll(10, 20));
  }

  @Test
  void closePollAndSaveResults_delegates() {
    proxy.closePollAndSaveResults(42);
    verify(mockDb).closePollAndSaveResults(42);
  }

  @Test
  void isOwner_delegatesToDatabase() {
    when(mockDb.isOwner(1, 2)).thenReturn(true);
    assertTrue(proxy.isOwner(1, 2));
  }

  @Test
  void addUserToGroup_delegates() {
    proxy.addUserToGroup(10, 99);
    verify(mockDb).addUserToGroup(10, 99);
  }

  @Test
  void createUserGroup_delegatesAndReturnsId() {
    when(mockDb.createUserGroup("group", 5)).thenReturn(77);
    assertEquals(77, proxy.createUserGroup("group", 5));
  }

  @Test
  void getProfileByUsername_returnsCorrect() {
    Profile profile = new Profile("z");
    when(mockDb.getProfileByUsername("z")).thenReturn(profile);
    assertEquals(profile, proxy.getProfileByUsername("z"));
  }

  @Test
  void getGroupByUsername_returnsCorrect() {
    UserGroup group = new UserGroup("group3");
    when(mockDb.getGroupByUsername("group3")).thenReturn(group);
    assertEquals(group, proxy.getGroupByUsername("group3"));
  }

  @Test
  void grantPollAccessToUser_delegates() {
    proxy.grantPollAccessToUser(1, 2, 3);
    verify(mockDb).grantPollAccessToUser(1, 2, 3);
  }

  @Test
  void grantPollAccessToGroup_delegates() {
    proxy.grantPollAccessToGroup(10, "groupie", 7);
    verify(mockDb).grantPollAccessToGroup(10, "groupie", 7);
  }

  @Test
  void getAllAvailablePolls_returnsExpected() {
    List<Poll> polls = Collections.singletonList(new Poll());
    when(mockDb.getAllAvailablePolls(9)).thenReturn(polls);
    assertEquals(polls, proxy.getAllAvailablePolls(9));
  }

  @Test
  void getGroupsCreatedByUser_delegates() {
    List<UserGroup> groups = Collections.emptyList();
    when(mockDb.getGroupsCreatedByUser(11)).thenReturn(groups);
    assertEquals(groups, proxy.getGroupsCreatedByUser(11));
  }
}
