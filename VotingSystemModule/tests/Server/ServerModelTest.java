package Server;

import Common.*;
import Utils.Logger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ServerModelTest {

  private DatabaseConnector mockDb;
  private ConnectionPool mockPool;
  private ServerModel serverModel;
  private ServerConnection mockConnection;

  @BeforeEach
  void setUp() {
    mockDb = mock(DatabaseConnector.class);
    mockPool = mock(ConnectionPool.class);
    mockConnection = mock(ServerConnection.class);
    serverModel = new ServerModel(mockDb, mockPool);
    serverModel.setConnection(mockConnection);
  }

  @Test
  void storeVote_callsDbStoreVote() throws SQLException {
    int[] choices = {1, 2};               // Example choice option IDs
    Vote vote = new Vote(42, choices);    // 42 = dummy user ID

    serverModel.storeVote(vote);

    verify(mockDb).storeVote(vote);
  }

  @Test
  void closePoll_sendsClosePollMessage() throws Exception {
    int pollId = 123;
    serverModel.setConnection(mockConnection);
    serverModel.closePoll(pollId, 1);
    verify(mockDb).closePollAndSaveResults(pollId);
    verify(mockPool).sendDirectMessage(any(Message.class));
  }

  @Test
  void retrievePollResult_returnsFromDb() {
    PollResult expected = mock(PollResult.class);
    when(mockDb.retrievePollResults(42)).thenReturn(expected);
    assertEquals(expected, serverModel.retrievePollResult(42));
  }

  @Test
  void checkPollAccess_returnsTrueIfAccessGranted() throws SQLException {
    Profile profile = new Profile("test");
    profile.setId(1);
    serverModel.setCurrentProfile(profile);
    when(mockDb.userHasAccessToPoll(1, 5)).thenReturn(true);
    assertTrue(serverModel.checkPollAccess(5));
  }

  @Test
  void checkPollAccess_returnsFalseIfNoAccess() throws SQLException {
    Profile profile = new Profile("test");
    profile.setId(1);
    serverModel.setCurrentProfile(profile);
    when(mockDb.userHasAccessToPoll(1, 5)).thenReturn(false);
    assertFalse(serverModel.checkPollAccess(5));
  }

  @Test
  void storeUserGroup_createsGroupAndAddsMembers() {
    UserGroup group = new UserGroup("group");
    group.addMember(new Profile("user1"));
    when(mockDb.createUserGroup(anyString(), anyInt())).thenReturn(10);

    serverModel.storeUserGroup(group, 1);

    verify(mockDb).createUserGroup("group", 1);
    verify(mockDb).addUserToGroup(anyInt(), eq(10));
  }

  @Test
  void grantPollAccessToUsers_callsGrantForEachUser() {
    Profile user1 = new Profile("u1");
    user1.setId(1);
    Profile user2 = new Profile("u2");
    user2.setId(2);

    serverModel.grantPollAccessToUsers(100, Set.of(user1, user2), 42);

    verify(mockDb).grantPollAccessToUser(100, 1, 42);
    verify(mockDb).grantPollAccessToUser(100, 2, 42);
  }

  @Test
  void grantPollAccessToGroups_callsGrantForEachGroup() {
    UserGroup g1 = new UserGroup("group1");
    UserGroup g2 = new UserGroup("group2");

    serverModel.grantPollAccessToGroups(200, Set.of(g1, g2), 99);

    verify(mockDb).grantPollAccessToGroup(200, "group1", 99);
    verify(mockDb).grantPollAccessToGroup(200, "group2", 99);
  }

  @Test
  void getGroupsCreatedByUser_returnsListFromDb() {
    List<UserGroup> dummyGroups = List.of(new UserGroup("grp"));
    when(mockDb.getGroupsCreatedByUser(5)).thenReturn(dummyGroups);
    assertEquals(dummyGroups, serverModel.getGroupsCreatedByUser(5));
  }
}
