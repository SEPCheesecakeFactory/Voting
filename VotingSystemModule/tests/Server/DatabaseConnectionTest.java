package Server;

import Common.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class DatabaseConnectionTest {

  private DatabaseConnection db;
  private Connection mockConn;
  private PreparedStatement mockStmt;
  private ResultSet mockResult;

  @BeforeEach
  void setUp() throws Exception {
    db = spy(new DatabaseConnection());
    mockConn = mock(Connection.class);
    mockStmt = mock(PreparedStatement.class);
    mockResult = mock(ResultSet.class);

    doReturn(mockConn).when(db).getConnection(); // If getConnection used
    doReturn(mockConn).when(db).openConnection(); // If openConnection used
  }

  @Test
  void storeVote_successfulInsert() throws Exception {
    Vote vote = new Vote(99, new int[]{1, 2, 3});

    when(mockConn.prepareStatement(anyString())).thenReturn(mockStmt);
    when(mockStmt.executeQuery()).thenReturn(mockResult);
    when(mockResult.next()).thenReturn(true); // poll exists & not closed
    when(mockResult.getInt(1)).thenReturn(123); // poll ID

    db.storeVote(vote);

    verify(mockConn, atLeastOnce()).prepareStatement(anyString());
    verify(mockStmt, atLeastOnce()).executeUpdate();
  }

  @Test
  void storeVote_pollIsClosed_throws() throws Exception {
    Vote vote = new Vote(99, new int[]{1});

    when(mockConn.prepareStatement(anyString())).thenReturn(mockStmt);
    when(mockStmt.executeQuery()).thenReturn(mockResult);
    when(mockResult.next()).thenReturn(true);
    when(mockResult.getBoolean("is_closed")).thenReturn(true);

    RuntimeException ex = assertThrows(RuntimeException.class, () -> db.storeVote(vote));
    assertTrue(ex.getMessage().contains("Poll is closed"));
  }

  @Test
  void storeVote_choiceNotFound_throws() throws Exception {
    Vote vote = new Vote( 99, new int[]{1});

    when(mockConn.prepareStatement(anyString())).thenReturn(mockStmt);
    when(mockStmt.executeQuery()).thenReturn(mockResult);
    when(mockResult.next()).thenReturn(false); // simulate no poll found

    assertThrows(RuntimeException.class, () -> db.storeVote(vote));
  }

  @Test
  void storeVote_sqlException_wrappedAsRuntime() throws Exception {
    Vote vote = new Vote( 99, new int[]{1});

    when(mockConn.prepareStatement(anyString())).thenThrow(new SQLException("DB failure"));

    assertThrows(RuntimeException.class, () -> db.storeVote(vote));
  }

  @Test
  void editVote_successfulUpdate() throws Exception {
    Vote vote = new Vote(99, new int[]{2, 3});

    when(mockConn.prepareStatement(anyString())).thenReturn(mockStmt);

    when(mockStmt.executeQuery()).thenReturn(mockResult);

    when(mockResult.next()).thenReturn(true).thenReturn(false);

    // Poll ID and is_closed check
    when(mockResult.getInt(1)).thenReturn(123);
    when(mockResult.getBoolean("is_closed")).thenReturn(false);

    db.editVote(vote);

    verify(mockConn, atLeastOnce()).prepareStatement(anyString());
    verify(mockStmt, atLeastOnce()).executeUpdate();
  }

  @Test
  void retrievePoll_pollNotFound_returnsNull() throws Exception {
    int pollId = 42;

    PreparedStatement pollStmt = mock(PreparedStatement.class);
    ResultSet pollResult = mock(ResultSet.class);

    when(mockConn.prepareStatement(contains("FROM Poll"))).thenReturn(pollStmt);
    when(pollStmt.executeQuery()).thenReturn(pollResult);
    when(pollResult.next()).thenReturn(false); // simulate no poll found

    Poll result = db.retrievePoll(pollId);

    assertNull(result);
  }

  @Test
  void retrievePoll_basicPoll_returnsCorrectPoll() throws Exception {
    int pollId = 42;

    // -- Mock poll query --
    PreparedStatement psPoll = mock(PreparedStatement.class);
    ResultSet rsPoll = mock(ResultSet.class);

    when(mockConn.prepareStatement(contains("FROM Poll"))).thenReturn(psPoll);
    when(psPoll.executeQuery()).thenReturn(rsPoll);
    when(rsPoll.next()).thenReturn(true);
    when(rsPoll.getString("title")).thenReturn("Test Poll");
    when(rsPoll.getBoolean("is_private")).thenReturn(false);
    when(rsPoll.getBoolean("is_closed")).thenReturn(false);

    // -- Mock question query --
    PreparedStatement psQ = mock(PreparedStatement.class);
    ResultSet rsQ = mock(ResultSet.class);

    when(mockConn.prepareStatement(contains("FROM Question"))).thenReturn(psQ);
    when(psQ.executeQuery()).thenReturn(rsQ);
    when(rsQ.next()).thenReturn(true).thenReturn(false);
    when(rsQ.getInt("id")).thenReturn(1);
    when(rsQ.getString("title")).thenReturn("Q1");
    when(rsQ.getString("description")).thenReturn("Desc");

    // -- Mock options query --
    PreparedStatement psOpt = mock(PreparedStatement.class);
    ResultSet rsOpt = mock(ResultSet.class);

    when(mockConn.prepareStatement(contains("FROM ChoiceOption"))).thenReturn(psOpt);
    when(psOpt.executeQuery()).thenReturn(rsOpt);
    when(rsOpt.next()).thenReturn(true).thenReturn(false);
    when(rsOpt.getInt("id")).thenReturn(101);
    when(rsOpt.getString("value")).thenReturn("Option A");

    // -- Run --
    Poll poll = db.retrievePoll(pollId);

    // -- Verify --
    assertNotNull(poll);
    assertEquals(pollId, poll.getId());
    assertEquals("Test Poll", poll.getTitle());
    assertFalse(poll.isPrivate());
    assertFalse(poll.isClosed());
    assertEquals(1, poll.getQuestions().length);

    Question q = poll.getQuestions()[0];
    assertEquals("Q1", q.getTitle());
    assertEquals("Desc", q.getDescription());
    assertEquals(1, q.getChoiceOptions().length);
    assertEquals("Option A", q.getChoiceOptions()[0].getValue());
  }

  @Test
  void loginOrRegister_existingUser_returnsId() throws Exception {
    Profile profile = new Profile("alice");

    PreparedStatement selectStmt = mock(PreparedStatement.class);
    ResultSet selectResult = mock(ResultSet.class);

    when(mockConn.prepareStatement(contains("SELECT id FROM users"))).thenReturn(selectStmt);
    when(selectStmt.executeQuery()).thenReturn(selectResult);
    when(selectResult.next()).thenReturn(true);
    when(selectResult.getInt("id")).thenReturn(42); // existing user ID

    int resultId = db.loginOrRegisterAProfile(profile);

    assertEquals(42, resultId);
  }

  @Test
  void loginOrRegister_newUser_insertsAndReturnsId() throws Exception {
    Profile profile = new Profile("newUser");

    // Mock SELECT (user doesn't exist)
    PreparedStatement selectStmt = mock(PreparedStatement.class);
    ResultSet selectResult = mock(ResultSet.class);

    when(mockConn.prepareStatement(contains("SELECT id FROM users"))).thenReturn(selectStmt);
    when(selectStmt.executeQuery()).thenReturn(selectResult);
    when(selectResult.next()).thenReturn(false);  // user not found

    // Mock INSERT
    PreparedStatement insertStmt = mock(PreparedStatement.class);
    ResultSet insertResult = mock(ResultSet.class);

    when(mockConn.prepareStatement(contains("INSERT INTO users"))).thenReturn(insertStmt);
    when(insertStmt.executeQuery()).thenReturn(insertResult);
    when(insertResult.next()).thenReturn(true);
    when(insertResult.getInt("id")).thenReturn(77); // new user ID

    int resultId = db.loginOrRegisterAProfile(profile);

    assertEquals(77, resultId);
  }

  @Test
  void retrievePollResults_noVotes_returnsEmptyCounts() throws Exception {
    int pollId = 100;

    PreparedStatement resultStmt = mock(PreparedStatement.class);
    ResultSet resultRS = mock(ResultSet.class);

    when(mockConn.prepareStatement(contains("SELECT co.id AS choice_id"))).thenReturn(resultStmt);
    when(resultStmt.executeQuery()).thenReturn(resultRS);
    when(resultRS.next()).thenReturn(false); // no vote results

    // Mock retrievePoll dependency
    Poll mockPoll = new Poll("Title", "Desc", pollId, new Question[0], false);
    doReturn(mockPoll).when(db).retrievePoll(pollId);

    PollResult result = db.retrievePollResults(pollId);

    assertNotNull(result);
    assertEquals(mockPoll, result.getPoll());
    assertTrue(result.getChoiceVoters().isEmpty());
  }

  @Test
  void retrievePollResults_oneVoteCount_correctMap() throws Exception {
    int pollId = 100;

    // Mock vote result row: choice_id = 55, vote_count = 3
    PreparedStatement resultStmt = mock(PreparedStatement.class);
    ResultSet resultRS = mock(ResultSet.class);

    when(mockConn.prepareStatement(contains("SELECT co.id AS choice_id"))).thenReturn(resultStmt);
    when(resultStmt.executeQuery()).thenReturn(resultRS);
    when(resultRS.next()).thenReturn(true).thenReturn(false);
    when(resultRS.getInt("choice_id")).thenReturn(55);
    when(resultRS.getInt("vote_count")).thenReturn(3);

    // Mock retrievePoll dependency
    Poll mockPoll = new Poll("Title", "Desc", pollId, new Question[0], false);
    doReturn(mockPoll).when(db).retrievePoll(pollId);

    PollResult result = db.retrievePollResults(pollId);

    assertNotNull(result);
    assertEquals(mockPoll, result.getPoll());
    assertEquals(1, result.getChoiceVoters().size());
    assertEquals(3, result.getChoiceVoters().get(55));
  }

  @Test
  void retrievePollResults_multipleChoices_countsMappedCorrectly() throws Exception {
    int pollId = 200;

    PreparedStatement resultStmt = mock(PreparedStatement.class);
    ResultSet resultRS = mock(ResultSet.class);

    when(mockConn.prepareStatement(contains("SELECT co.id AS choice_id"))).thenReturn(resultStmt);
    when(resultStmt.executeQuery()).thenReturn(resultRS);

    // Simulate: 3 choices with different vote counts
    when(resultRS.next()).thenReturn(true, true, true, false);
    when(resultRS.getInt("choice_id"))
        .thenReturn(11) // first row
        .thenReturn(22) // second row
        .thenReturn(33); // third row

    when(resultRS.getInt("vote_count"))
        .thenReturn(5)  // votes for 11
        .thenReturn(2)  // votes for 22
        .thenReturn(0); // votes for 33

    Poll mockPoll = new Poll("Multi", "Desc", pollId, new Question[0], false);
    doReturn(mockPoll).when(db).retrievePoll(pollId);

    PollResult result = db.retrievePollResults(pollId);

    assertNotNull(result);
    assertEquals(mockPoll, result.getPoll());
    assertEquals(3, result.getChoiceVoters().size());
    assertEquals(5, result.getChoiceVoters().get(11));
    assertEquals(2, result.getChoiceVoters().get(22));
    assertEquals(0, result.getChoiceVoters().get(33));
  }

  @Test
  void storePoll_noQuestions_onlyPollInserted() throws Exception {
    Profile owner = new Profile("owner");
    owner.setId(1);

    Poll poll = new Poll("Test Poll", "Description", -1, new Question[0], false);

    PreparedStatement psInsertPoll = mock(PreparedStatement.class);
    ResultSet pollKeyResult = mock(ResultSet.class);
    PreparedStatement psInsertOwnership = mock(PreparedStatement.class);

    when(mockConn.prepareStatement(contains("INSERT INTO Poll"), anyInt())).thenReturn(psInsertPoll);
    when(psInsertPoll.executeUpdate()).thenReturn(1);
    when(psInsertPoll.getGeneratedKeys()).thenReturn(pollKeyResult);
    when(pollKeyResult.next()).thenReturn(true);
    when(pollKeyResult.getInt(1)).thenReturn(42); // simulated pollId

    when(mockConn.prepareStatement(contains("INSERT INTO PollOwnership"))).thenReturn(psInsertOwnership);

    Poll returnedPoll = db.storePoll(poll, owner);

    assertEquals(42, returnedPoll.getId());
    verify(mockConn).setAutoCommit(false);
    verify(mockConn).commit();
    verify(psInsertPoll).executeUpdate();
    verify(psInsertOwnership).executeUpdate();
  }

  @Test
  void storePoll_oneQuestionOneOption_allInserted() throws Exception {
    Profile owner = new Profile("owner");
    owner.setId(1);

    ChoiceOption[] options = { new ChoiceOption(0, "Yes") };
    Question question = new Question(options, -1, "Q1", "Do you agree?");
    Poll poll = new Poll("Agreement Poll", "Poll Desc", -1, new Question[]{ question }, false);

    // -- Poll INSERT
    PreparedStatement psPoll = mock(PreparedStatement.class);
    ResultSet rsPoll = mock(ResultSet.class);
    when(mockConn.prepareStatement(contains("INSERT INTO Poll"), anyInt())).thenReturn(psPoll);
    when(psPoll.executeUpdate()).thenReturn(1);
    when(psPoll.getGeneratedKeys()).thenReturn(rsPoll);
    when(rsPoll.next()).thenReturn(true);
    when(rsPoll.getInt(1)).thenReturn(100); // pollId

    // -- Ownership INSERT
    PreparedStatement psOwner = mock(PreparedStatement.class);
    when(mockConn.prepareStatement(contains("INSERT INTO PollOwnership"))).thenReturn(psOwner);

    // -- Question INSERT
    PreparedStatement psQ = mock(PreparedStatement.class);
    ResultSet rsQ = mock(ResultSet.class);
    when(mockConn.prepareStatement(contains("INSERT INTO Question"), anyInt())).thenReturn(psQ);
    when(psQ.executeUpdate()).thenReturn(1);
    when(psQ.getGeneratedKeys()).thenReturn(rsQ);
    when(rsQ.next()).thenReturn(true);
    when(rsQ.getInt(1)).thenReturn(200); // questionId

    // -- Option INSERT
    PreparedStatement psOpt = mock(PreparedStatement.class);
    ResultSet rsOpt = mock(ResultSet.class);
    when(mockConn.prepareStatement(contains("INSERT INTO ChoiceOption"), anyInt())).thenReturn(psOpt);
    when(psOpt.executeUpdate()).thenReturn(1);
    when(psOpt.getGeneratedKeys()).thenReturn(rsOpt);
    when(rsOpt.next()).thenReturn(true);
    when(rsOpt.getInt(1)).thenReturn(300); // optionId

    Poll returnedPoll = db.storePoll(poll, owner);

    assertEquals(100, returnedPoll.getId());
    assertEquals(200, poll.getQuestions()[0].getId());
    assertEquals(300, poll.getQuestions()[0].getChoiceOptions()[0].getId());

    verify(mockConn).commit();
  }

  @Test
  void storePoll_multipleQuestionsOptions_allInsertedCorrectly() throws Exception {
    Profile owner = new Profile("multiOwner");
    owner.setId(2);

    // Setup multiple questions/options
    ChoiceOption[] options1 = {
        new ChoiceOption(0, "Yes"),
        new ChoiceOption(0, "No")
    };
    Question q1 = new Question(options1, -1, "Q1", "Agree?");

    ChoiceOption[] options2 = {
        new ChoiceOption(0, "Option A"),
        new ChoiceOption(0, "Option B"),
        new ChoiceOption(0, "Option C")
    };
    Question q2 = new Question(options2, -1, "Q2", "Choose one");

    Poll poll = new Poll("Multi Poll", "With several questions", -1, new Question[]{ q1, q2 }, false);

    // Mock poll insert
    PreparedStatement psPoll = mock(PreparedStatement.class);
    ResultSet rsPoll = mock(ResultSet.class);
    when(mockConn.prepareStatement(contains("INSERT INTO Poll"), anyInt())).thenReturn(psPoll);
    when(psPoll.executeUpdate()).thenReturn(1);
    when(psPoll.getGeneratedKeys()).thenReturn(rsPoll);
    when(rsPoll.next()).thenReturn(true);
    when(rsPoll.getInt(1)).thenReturn(500); // pollId

    // Mock ownership insert
    PreparedStatement psOwn = mock(PreparedStatement.class);
    when(mockConn.prepareStatement(contains("INSERT INTO PollOwnership"))).thenReturn(psOwn);

    // Mock question insert
    PreparedStatement psQ = mock(PreparedStatement.class);
    ResultSet rsQ = mock(ResultSet.class);
    when(mockConn.prepareStatement(contains("INSERT INTO Question"), anyInt())).thenReturn(psQ);
    when(psQ.executeUpdate()).thenReturn(1);
    when(psQ.getGeneratedKeys()).thenReturn(rsQ);
    when(rsQ.next()).thenReturn(true, true);
    when(rsQ.getInt(1)).thenReturn(600, 601); // q1, q2 IDs

    // Mock option insert
    PreparedStatement psOpt = mock(PreparedStatement.class);
    ResultSet rsOpt = mock(ResultSet.class);
    when(mockConn.prepareStatement(contains("INSERT INTO ChoiceOption"), anyInt())).thenReturn(psOpt);
    when(psOpt.executeUpdate()).thenReturn(1);
    when(psOpt.getGeneratedKeys()).thenReturn(rsOpt);
    when(rsOpt.next()).thenReturn(true, true, true, true, true);
    when(rsOpt.getInt(1)).thenReturn(1001, 1002, 1003, 1004, 1005); // option IDs

    Poll returned = db.storePoll(poll, owner);

    assertEquals(500, returned.getId());
    assertEquals(2, returned.getQuestions().length);
    assertEquals(2, returned.getQuestions()[0].getChoiceOptions().length);
    assertEquals(3, returned.getQuestions()[1].getChoiceOptions().length);

    verify(mockConn).commit();
  }

  @Test
  void storePoll_insertFails_triggersRollback() throws Exception {
    Profile owner = new Profile("badOwner");
    owner.setId(3);

    ChoiceOption[] options = { new ChoiceOption(0, "Broken") };
    Question question = new Question(options, -1, "Bad Q", "Fails?");
    Poll poll = new Poll("Bad Poll", "This should fail", -1, new Question[]{ question }, false);

    PreparedStatement psPoll = mock(PreparedStatement.class);

    when(mockConn.prepareStatement(contains("INSERT INTO Poll"), anyInt())).thenReturn(psPoll);
    when(psPoll.executeUpdate()).thenThrow(new SQLException("DB error")); // force failure

    SQLException rollbackEx = new SQLException("rollback ok");
    doNothing().when(mockConn).rollback(); // make rollback succeed

    RuntimeException ex = assertThrows(RuntimeException.class, () -> db.storePoll(poll, owner));

    assertTrue(ex.getCause() instanceof SQLException);
    verify(mockConn).rollback();
  }

  @Test
  void changeUsername_Z_nullUsername_throws() {
    Profile profile = new Profile(null);
    profile.setId(10);

    assertThrows(RuntimeException.class, () -> db.changeUsername(profile));
  }

  @Test
  void changeUsername_O_validChange_executesUpdate() throws Exception {
    Profile profile = new Profile("newName");
    profile.setId(99);

    PreparedStatement checkStmt = mock(PreparedStatement.class);
    PreparedStatement updateStmt = mock(PreparedStatement.class);
    ResultSet checkRS = mock(ResultSet.class);

    when(mockConn.prepareStatement(contains("SELECT COUNT"))).thenReturn(checkStmt);
    when(checkStmt.executeQuery()).thenReturn(checkRS);
    when(checkRS.next()).thenReturn(true);
    when(checkRS.getInt(1)).thenReturn(0); // username not taken

    when(mockConn.prepareStatement(contains("UPDATE users"))).thenReturn(updateStmt);

    db.changeUsername(profile);

    verify(updateStmt).setString(1, "newName");
    verify(updateStmt).setInt(2, 99);
    verify(updateStmt).executeUpdate();
  }

  @Test
  void changeUsername_B_usernameAlreadyExists_throws() throws Exception {
    Profile profile = new Profile("existing");
    profile.setId(88);

    PreparedStatement checkStmt = mock(PreparedStatement.class);
    ResultSet checkRS = mock(ResultSet.class);

    when(mockConn.prepareStatement(contains("SELECT COUNT"))).thenReturn(checkStmt);
    when(checkStmt.executeQuery()).thenReturn(checkRS);
    when(checkRS.next()).thenReturn(true);
    when(checkRS.getInt(1)).thenReturn(1); // already taken

    assertThrows(RuntimeException.class, () -> db.changeUsername(profile));
  }

  @Test
  void changeUsername_I_invalidId_stillRunsUpdate() throws Exception {
    Profile profile = new Profile("newbie");
    profile.setId(0); // technically valid, but test edge case

    PreparedStatement checkStmt = mock(PreparedStatement.class);
    PreparedStatement updateStmt = mock(PreparedStatement.class);
    ResultSet checkRS = mock(ResultSet.class);

    when(mockConn.prepareStatement(contains("SELECT COUNT"))).thenReturn(checkStmt);
    when(checkStmt.executeQuery()).thenReturn(checkRS);
    when(checkRS.next()).thenReturn(true);
    when(checkRS.getInt(1)).thenReturn(0); // username not taken

    when(mockConn.prepareStatement(contains("UPDATE users"))).thenReturn(updateStmt);

    db.changeUsername(profile);

    verify(updateStmt).setInt(2, 0);
    verify(updateStmt).executeUpdate();
  }

  @Test
  void changeUsername_E_sqlExceptionDuringUpdate_throwsRuntime() throws Exception {
    Profile profile = new Profile("failUser");
    profile.setId(999);

    PreparedStatement checkStmt = mock(PreparedStatement.class);
    ResultSet checkRS = mock(ResultSet.class);

    when(mockConn.prepareStatement(contains("SELECT COUNT"))).thenReturn(checkStmt);
    when(checkStmt.executeQuery()).thenReturn(checkRS);
    when(checkRS.next()).thenReturn(true);
    when(checkRS.getInt(1)).thenReturn(0); // not taken

    when(mockConn.prepareStatement(contains("UPDATE users")))
        .thenThrow(new SQLException("DB is down"));

    assertThrows(RuntimeException.class, () -> db.changeUsername(profile));
  }

  @Test
  void changeUsername_S_updatesCorrectlyWithExpectedSQL() throws Exception {
    Profile profile = new Profile("finalCheck");
    profile.setId(5);

    PreparedStatement checkStmt = mock(PreparedStatement.class);
    PreparedStatement updateStmt = mock(PreparedStatement.class);
    ResultSet checkRS = mock(ResultSet.class);

    when(mockConn.prepareStatement(contains("SELECT COUNT"))).thenReturn(checkStmt);
    when(checkStmt.executeQuery()).thenReturn(checkRS);
    when(checkRS.next()).thenReturn(true);
    when(checkRS.getInt(1)).thenReturn(0);

    when(mockConn.prepareStatement(contains("UPDATE users"))).thenReturn(updateStmt);

    db.changeUsername(profile);

    verify(updateStmt).setString(1, "finalCheck");
    verify(updateStmt).setInt(2, 5);
    verify(updateStmt).executeUpdate();
  }

  @Test
  void getProfileByUsername_Z_nullUsername_returnsNull() throws Exception {
    assertNull(db.getProfileByUsername(null));
  }

  @Test
  void getProfileByUsername_O_userFound_returnsCorrectProfile() throws Exception {
    String username = "alice";

    PreparedStatement stmt = mock(PreparedStatement.class);
    ResultSet rs = mock(ResultSet.class);

    when(mockConn.prepareStatement(contains("SELECT id, username"))).thenReturn(stmt);
    when(stmt.executeQuery()).thenReturn(rs);
    when(rs.next()).thenReturn(true);
    when(rs.getString("username")).thenReturn(username);
    when(rs.getInt("id")).thenReturn(101);

    Profile result = db.getProfileByUsername(username);

    assertNotNull(result);
    assertEquals(username, result.getUsername());
    assertEquals(101, result.getId());
  }

  @Test
  void getProfileByUsername_B_userNotFound_returnsNull() throws Exception {
    PreparedStatement stmt = mock(PreparedStatement.class);
    ResultSet rs = mock(ResultSet.class);

    when(mockConn.prepareStatement(contains("SELECT id, username"))).thenReturn(stmt);
    when(stmt.executeQuery()).thenReturn(rs);
    when(rs.next()).thenReturn(false);  // user not found

    Profile result = db.getProfileByUsername("ghost");

    assertNull(result);
  }

  @Test
  void getProfileByUsername_E_sqlError_logsAndReturnsNull() throws Exception {
    when(mockConn.prepareStatement(contains("SELECT id, username")))
        .thenThrow(new SQLException("DB failure"));

    Profile result = db.getProfileByUsername("broken");

    assertNull(result);  // method handles error and returns null
  }

  @Test
  void getProfileByUsername_S_profileObjectHasCorrectState() throws Exception {
    PreparedStatement stmt = mock(PreparedStatement.class);
    ResultSet rs = mock(ResultSet.class);

    when(mockConn.prepareStatement(contains("SELECT id, username"))).thenReturn(stmt);
    when(stmt.executeQuery()).thenReturn(rs);
    when(rs.next()).thenReturn(true);
    when(rs.getInt("id")).thenReturn(202);
    when(rs.getString("username")).thenReturn("tester");

    Profile profile = db.getProfileByUsername("tester");

    assertNotNull(profile);
    assertEquals(202, profile.getId());
    assertEquals("tester", profile.getUsername());
  }

  @Test
  void getGroupByUsername_Z_nullName_returnsNull() {
    assertNull(db.getGroupByUsername(null));
  }

  @Test
  void getGroupByUsername_O_groupFound_returnsFullGroup() throws Exception {
    String groupName = "devTeam";

    PreparedStatement groupStmt = mock(PreparedStatement.class);
    ResultSet groupRS = mock(ResultSet.class);

    PreparedStatement memberStmt = mock(PreparedStatement.class);
    ResultSet memberRS = mock(ResultSet.class);

    // Mock group lookup
    when(mockConn.prepareStatement(contains("FROM UserGroup WHERE name"))).thenReturn(groupStmt);
    when(groupStmt.executeQuery()).thenReturn(groupRS);
    when(groupRS.next()).thenReturn(true);
    when(groupRS.getInt("id")).thenReturn(100);
    when(groupRS.getString("name")).thenReturn(groupName);

    // Mock member lookup
    when(mockConn.prepareStatement(contains("FROM Users u"))).thenReturn(memberStmt);
    when(memberStmt.executeQuery()).thenReturn(memberRS);
    when(memberRS.next()).thenReturn(true, true, false); // 2 members
    when(memberRS.getInt("id")).thenReturn(1, 2);
    when(memberRS.getString("username")).thenReturn("alice", "bob");

    UserGroup result = db.getGroupByUsername(groupName);

    assertNotNull(result);
    assertEquals("devTeam", result.getGroupName());
    assertEquals(100, result.getId());
    assertEquals(2, result.getMembers().size());
    assertTrue(result.getMembers().stream().anyMatch(p -> p.getUsername().equals("alice")));
    assertTrue(result.getMembers().stream().anyMatch(p -> p.getUsername().equals("bob")));
  }

  @Test
  void getGroupByUsername_B_groupNotFound_returnsNull() throws Exception {
    PreparedStatement groupStmt = mock(PreparedStatement.class);
    ResultSet groupRS = mock(ResultSet.class);

    when(mockConn.prepareStatement(contains("FROM UserGroup WHERE name"))).thenReturn(groupStmt);
    when(groupStmt.executeQuery()).thenReturn(groupRS);
    when(groupRS.next()).thenReturn(false); // No group

    assertNull(db.getGroupByUsername("nonexistent"));
  }

  @Test
  void getGroupByUsername_E_sqlError_returnsNull() throws Exception {
    when(mockConn.prepareStatement(contains("FROM UserGroup WHERE name")))
        .thenThrow(new SQLException("DB fail"));

    assertNull(db.getGroupByUsername("failGroup"));
  }

  @Test
  void getGroupByUsername_S_correctMemberMapping() throws Exception {
    String groupName = "qaTeam";

    PreparedStatement groupStmt = mock(PreparedStatement.class);
    ResultSet groupRS = mock(ResultSet.class);
    PreparedStatement memberStmt = mock(PreparedStatement.class);
    ResultSet memberRS = mock(ResultSet.class);

    // Group exists
    when(mockConn.prepareStatement(contains("FROM UserGroup WHERE name"))).thenReturn(groupStmt);
    when(groupStmt.executeQuery()).thenReturn(groupRS);
    when(groupRS.next()).thenReturn(true);
    when(groupRS.getInt("id")).thenReturn(55);
    when(groupRS.getString("name")).thenReturn(groupName);

    // Members: Charlie
    when(mockConn.prepareStatement(contains("FROM Users u"))).thenReturn(memberStmt);
    when(memberStmt.executeQuery()).thenReturn(memberRS);
    when(memberRS.next()).thenReturn(true, false);
    when(memberRS.getInt("id")).thenReturn(3);
    when(memberRS.getString("username")).thenReturn("charlie");

    UserGroup result = db.getGroupByUsername(groupName);

    assertNotNull(result);
    assertEquals(1, result.getMembers().size());
    assertEquals("charlie", result.getMembers().get(0).getUsername());
  }

  @Test
  void grantPollAccessToUser_Z_zeroId_failsWithRuntime() throws Exception {
    when(mockConn.prepareStatement(anyString())).thenReturn(mockStmt);
    ResultSet rs = mock(ResultSet.class);
    when(mockStmt.executeQuery()).thenReturn(rs);
    when(rs.next()).thenReturn(false); // simulate not owner

    assertThrows(RuntimeException.class, () -> db.grantPollAccessToUser(0, 0, 0));
  }

  @Test
  void grantPollAccessToUser_O_validInsert_executesOnce() throws Exception {
    PreparedStatement stmt = mock(PreparedStatement.class);
    ResultSet rs = mock(ResultSet.class);

    when(mockConn.prepareStatement(anyString())).thenReturn(stmt);
    when(stmt.executeQuery()).thenReturn(rs);
    when(rs.next()).thenReturn(true); // simulate owner check success

    db.grantPollAccessToUser(1, 99, 42);

    verify(stmt, times(1)).executeUpdate();
  }

  @Test
  void grantPollAccessToUser_E_sqlFails_throwsRuntime() throws Exception {
    when(mockConn.prepareStatement(anyString())).thenThrow(new SQLException("fail"));

    assertThrows(RuntimeException.class, () -> db.grantPollAccessToUser(1, 1, 1));
  }

  @Test
  void grantPollAccessToGroup_Z_nullName_throwsOrDoesNothing() {
    assertThrows(RuntimeException.class, () -> db.grantPollAccessToGroup(1, null, 1));
  }

  @Test
  void grantPollAccessToGroup_O_validGroup_grantsAccess() throws Exception {
    PreparedStatement checkStmt = mock(PreparedStatement.class);
    ResultSet checkRs = mock(ResultSet.class);
    PreparedStatement getStmt = mock(PreparedStatement.class);
    ResultSet rs = mock(ResultSet.class);
    PreparedStatement insertStmt = mock(PreparedStatement.class);

    when(mockConn.prepareStatement(contains("FROM PollOwnership"))).thenReturn(checkStmt);
    when(checkStmt.executeQuery()).thenReturn(checkRs);
    when(checkRs.next()).thenReturn(true); // simulate owner check passed

    when(mockConn.prepareStatement(contains("SELECT id FROM UserGroup"))).thenReturn(getStmt);
    when(getStmt.executeQuery()).thenReturn(rs);
    when(rs.next()).thenReturn(true);
    when(rs.getInt("id")).thenReturn(123);

    when(mockConn.prepareStatement(contains("INSERT INTO PollAccessControl"))).thenReturn(insertStmt);

    db.grantPollAccessToGroup(5, "teamX", 99);

    verify(insertStmt).executeUpdate();
  }


  @Test
  void grantPollAccessToGroup_B_groupNotFound_throwsRuntime() throws Exception {
    // Mock ownership check
    PreparedStatement checkStmt = mock(PreparedStatement.class);
    ResultSet checkRs = mock(ResultSet.class);

    when(mockConn.prepareStatement(contains("FROM PollOwnership"))).thenReturn(checkStmt);
    when(checkStmt.executeQuery()).thenReturn(checkRs);
    when(checkRs.next()).thenReturn(true); // simulate ownership exists

    // Mock group lookup that fails
    PreparedStatement getStmt = mock(PreparedStatement.class);
    ResultSet rs = mock(ResultSet.class);

    when(mockConn.prepareStatement(contains("SELECT id FROM UserGroup"))).thenReturn(getStmt);
    when(getStmt.executeQuery()).thenReturn(rs);
    when(rs.next()).thenReturn(false); // simulate group not found

    assertThrows(RuntimeException.class, () -> db.grantPollAccessToGroup(7, "missingGroup", 5));
  }


  @Test
  void addUserToGroup_Z_zeroValues_stillExecutesInsert() throws Exception {
    PreparedStatement stmt = mock(PreparedStatement.class);
    when(mockConn.prepareStatement(anyString())).thenReturn(stmt);

    db.addUserToGroup(0, 0);

    verify(stmt).executeUpdate();
  }

  @Test
  void addUserToGroup_O_validInsert_succeeds() throws Exception {
    PreparedStatement stmt = mock(PreparedStatement.class);
    when(mockConn.prepareStatement(anyString())).thenReturn(stmt);

    db.addUserToGroup(1, 99);

    verify(stmt).executeUpdate();
  }

  @Test
  void addUserToGroup_E_sqlException_throwsRuntime() throws Exception {
    when(mockConn.prepareStatement(anyString())).thenThrow(new SQLException("fail"));

    assertThrows(RuntimeException.class, () -> db.addUserToGroup(1, 2));
  }

  @Test
  void getGroupsCreatedByUser_Z_noGroups_returnsEmptyList() throws Exception {
    PreparedStatement groupStmt = mock(PreparedStatement.class);
    ResultSet groupRS = mock(ResultSet.class);

    when(mockConn.prepareStatement(contains("FROM UserGroup"))).thenReturn(groupStmt);
    when(groupStmt.executeQuery()).thenReturn(groupRS);
    when(groupRS.next()).thenReturn(false);

    List<UserGroup> groups = db.getGroupsCreatedByUser(42);

    assertTrue(groups.isEmpty());
  }

  @Test
  void getGroupsCreatedByUser_O_oneGroup_returnsGroupWithMembers() throws Exception {
    PreparedStatement groupStmt = mock(PreparedStatement.class);
    ResultSet groupRS = mock(ResultSet.class);
    PreparedStatement membersStmt = mock(PreparedStatement.class);
    ResultSet membersRS = mock(ResultSet.class);

    when(mockConn.prepareStatement(contains("FROM UserGroup"))).thenReturn(groupStmt);
    when(mockConn.prepareStatement(contains("FROM Users"))).thenReturn(membersStmt);

    when(groupStmt.executeQuery()).thenReturn(groupRS);
    when(membersStmt.executeQuery()).thenReturn(membersRS);

    when(groupRS.next()).thenReturn(true, false);
    when(groupRS.getInt("id")).thenReturn(1);
    when(groupRS.getString("name")).thenReturn("groupX");

    when(membersRS.next()).thenReturn(true, false);
    when(membersRS.getInt("id")).thenReturn(5);
    when(membersRS.getString("username")).thenReturn("bob");

    List<UserGroup> result = db.getGroupsCreatedByUser(99);

    assertEquals(1, result.size());
    assertEquals("groupX", result.get(0).getGroupName());
    assertEquals("bob", result.get(0).getMembers().get(0).getUsername());
  }

  @Test
  void getAllAvailablePolls_Z_noPolls_returnsEmptyList() throws Exception {
    PreparedStatement stmt = mock(PreparedStatement.class);
    ResultSet rs = mock(ResultSet.class);

    when(mockConn.prepareStatement(contains("SELECT"))).thenReturn(stmt);
    when(stmt.executeQuery()).thenReturn(rs);
    when(rs.next()).thenReturn(false);

    List<Poll> polls = db.getAllAvailablePolls(101);

    assertTrue(polls.isEmpty());
  }

  @Test
  void getAllAvailablePolls_O_onePoll_returnsCorrectly() throws Exception {
    PreparedStatement stmt = mock(PreparedStatement.class);
    ResultSet rs = mock(ResultSet.class);

    when(mockConn.prepareStatement(contains("SELECT"))).thenReturn(stmt);
    when(stmt.executeQuery()).thenReturn(rs);

    when(rs.next()).thenReturn(true, false);
    when(rs.getInt("id")).thenReturn(1);
    when(rs.getString("title")).thenReturn("MyPoll");
    when(rs.getBoolean("is_private")).thenReturn(false);
    when(rs.getBoolean("is_closed")).thenReturn(false);
    when(rs.getInt("created_by_id")).thenReturn(5);

    doReturn(Collections.emptyList()).when(db).getAllowedUsersForPoll(any(), anyInt());
    doReturn(Collections.emptyList()).when(db).getAllowedGroupsForPoll(any(), anyInt());

    List<Poll> polls = db.getAllAvailablePolls(5);

    assertEquals(1, polls.size());
    assertEquals("MyPoll", polls.get(0).getTitle());
  }

  @Test
  void createUserGroup_Z_emptyGroupName_throwsOrFails() {
    assertThrows(RuntimeException.class, () -> db.createUserGroup("", 1));
  }

  @Test
  void createUserGroup_O_validGroup_insertsAndReturnsId() throws Exception {
    PreparedStatement stmt = mock(PreparedStatement.class);
    ResultSet rs = mock(ResultSet.class);

    when(mockConn.prepareStatement(contains("INSERT INTO UserGroup"))).thenReturn(stmt);
    when(stmt.executeQuery()).thenReturn(rs);
    when(rs.next()).thenReturn(true);
    when(rs.getInt(1)).thenReturn(123);

    int result = db.createUserGroup("devs", 42);
    assertEquals(123, result);
  }

  @Test
  void createUserGroup_B_zeroCreatorId_failsOrThrows() throws Exception {
    PreparedStatement stmt = mock(PreparedStatement.class);
    when(mockConn.prepareStatement(contains("INSERT INTO UserGroup"))).thenReturn(stmt);
    when(stmt.executeQuery()).thenThrow(new SQLException("invalid creator id"));

    assertThrows(RuntimeException.class, () -> db.createUserGroup("qa", 0));
  }

  @Test
  void createUserGroup_E_sqlException_wrappedAsRuntime() throws Exception {
    when(mockConn.prepareStatement(contains("INSERT INTO UserGroup")))
        .thenThrow(new SQLException("DB down"));

    assertThrows(RuntimeException.class, () -> db.createUserGroup("team", 5));
  }

  @Test
  void createUserGroup_S_parametersSetCorrectly() throws Exception {
    PreparedStatement stmt = mock(PreparedStatement.class);
    ResultSet rs = mock(ResultSet.class);

    when(mockConn.prepareStatement(contains("INSERT INTO UserGroup"))).thenReturn(stmt);
    when(stmt.executeQuery()).thenReturn(rs);
    when(rs.next()).thenReturn(true);
    when(rs.getInt(1)).thenReturn(50);

    db.createUserGroup("engineering", 9);

    verify(stmt).setString(1, "engineering");
    verify(stmt).setInt(2, 9);
  }

  @Test
  void addUserToGroup_Z_zeroIds_shouldFailOrThrow() throws Exception {
    PreparedStatement stmt = mock(PreparedStatement.class);
    when(mockConn.prepareStatement(contains("INSERT INTO UserGroupMembership"))).thenReturn(stmt);
    doThrow(new SQLException("Invalid ID")).when(stmt).executeUpdate();

    assertThrows(RuntimeException.class, () -> db.addUserToGroup(0, 0));
  }

  @Test
  void addUserToGroup_O_validInput_executesInsert() throws Exception {
    PreparedStatement stmt = mock(PreparedStatement.class);
    when(mockConn.prepareStatement(contains("INSERT INTO UserGroupMembership"))).thenReturn(stmt);

    db.addUserToGroup(10, 20);

    verify(stmt).setInt(1, 10);
    verify(stmt).setInt(2, 20);
    verify(stmt).executeUpdate();
  }

  @Test
  void addUserToGroup_B_boundaryIds_executesCorrectly() throws Exception {
    PreparedStatement stmt = mock(PreparedStatement.class);
    when(mockConn.prepareStatement(contains("INSERT INTO UserGroupMembership"))).thenReturn(stmt);

    db.addUserToGroup(1, Integer.MAX_VALUE);

    verify(stmt).setInt(1, 1);
    verify(stmt).setInt(2, Integer.MAX_VALUE);
    verify(stmt).executeUpdate();
  }

  @Test
  void addUserToGroup_E_sqlError_throwsRuntimeException() throws Exception {
    when(mockConn.prepareStatement(contains("INSERT INTO UserGroupMembership")))
        .thenThrow(new SQLException("DB issue"));

    assertThrows(RuntimeException.class, () -> db.addUserToGroup(5, 7));
  }

  @Test
  void grantPollAccessToUser_Z_zeroIds_failsGracefullyOrThrows() throws Exception {
    // Mock ownership check
    PreparedStatement checkStmt = mock(PreparedStatement.class);
    ResultSet checkRs = mock(ResultSet.class);
    when(mockConn.prepareStatement(contains("FROM PollOwnership"))).thenReturn(checkStmt);
    when(checkStmt.executeQuery()).thenReturn(checkRs);
    when(checkRs.next()).thenReturn(true); // simulate valid owner

    // Mock insert access
    PreparedStatement stmt = mock(PreparedStatement.class);
    when(mockConn.prepareStatement(contains("INSERT INTO PollAccessControl"))).thenReturn(stmt);
    doThrow(new SQLException("Invalid input")).when(stmt).executeUpdate();

    assertThrows(RuntimeException.class, () -> db.grantPollAccessToUser(0, 0, 1));
  }

  @Test
  void grantPollAccessToUser_O_validInput_executesInsert() throws Exception {
    PreparedStatement checkStmt = mock(PreparedStatement.class);
    ResultSet checkRs = mock(ResultSet.class);
    PreparedStatement stmt = mock(PreparedStatement.class);

    when(mockConn.prepareStatement(contains("FROM PollOwnership"))).thenReturn(checkStmt);
    when(checkStmt.executeQuery()).thenReturn(checkRs);
    when(checkRs.next()).thenReturn(true); // ownership OK

    when(mockConn.prepareStatement(contains("INSERT INTO PollAccessControl"))).thenReturn(stmt);

    db.grantPollAccessToUser(101, 202, 42);

    verify(stmt).setInt(1, 101);
    verify(stmt).setInt(2, 202);
    verify(stmt).executeUpdate();
  }

  @Test
  void grantPollAccessToUser_B_boundaryIds_executesCorrectly() throws Exception {
    PreparedStatement checkStmt = mock(PreparedStatement.class);
    ResultSet checkRs = mock(ResultSet.class);
    PreparedStatement stmt = mock(PreparedStatement.class);

    when(mockConn.prepareStatement(contains("FROM PollOwnership"))).thenReturn(checkStmt);
    when(checkStmt.executeQuery()).thenReturn(checkRs);
    when(checkRs.next()).thenReturn(true);

    when(mockConn.prepareStatement(contains("INSERT INTO PollAccessControl"))).thenReturn(stmt);

    db.grantPollAccessToUser(Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE);

    verify(stmt).setInt(1, Integer.MAX_VALUE);
    verify(stmt).setInt(2, Integer.MAX_VALUE);
    verify(stmt).executeUpdate();
  }

  @Test
  void grantPollAccessToUser_E_sqlError_throwsRuntimeException() throws Exception {
    PreparedStatement checkStmt = mock(PreparedStatement.class);
    ResultSet checkRs = mock(ResultSet.class);

    when(mockConn.prepareStatement(contains("FROM PollOwnership"))).thenReturn(checkStmt);
    when(checkStmt.executeQuery()).thenReturn(checkRs);
    when(checkRs.next()).thenReturn(true);

    when(mockConn.prepareStatement(contains("INSERT INTO PollAccessControl")))
        .thenThrow(new SQLException("DB failure"));

    assertThrows(RuntimeException.class, () -> db.grantPollAccessToUser(10, 20, 5));
  }

  @Test
  void grantPollAccessToGroup_Z_emptyGroupName_returnsOrThrows() {
    assertThrows(RuntimeException.class, () -> db.grantPollAccessToGroup(1, "", 1));
    assertThrows(RuntimeException.class, () -> db.grantPollAccessToGroup(0, "group", 1));
  }

  @Test
  void grantPollAccessToGroup_O_groupFound_executesInsert() throws Exception {
    String groupName = "devs";

    // Ownership
    PreparedStatement checkStmt = mock(PreparedStatement.class);
    ResultSet checkRs = mock(ResultSet.class);
    when(mockConn.prepareStatement(contains("FROM PollOwnership"))).thenReturn(checkStmt);
    when(checkStmt.executeQuery()).thenReturn(checkRs);
    when(checkRs.next()).thenReturn(true);

    // Group fetch
    PreparedStatement getStmt = mock(PreparedStatement.class);
    ResultSet getRs = mock(ResultSet.class);
    when(mockConn.prepareStatement(contains("FROM UserGroup"))).thenReturn(getStmt);
    when(getStmt.executeQuery()).thenReturn(getRs);
    when(getRs.next()).thenReturn(true);
    when(getRs.getInt("id")).thenReturn(42);

    // Insert
    PreparedStatement insertStmt = mock(PreparedStatement.class);
    when(mockConn.prepareStatement(contains("INSERT INTO PollAccessControl"))).thenReturn(insertStmt);

    db.grantPollAccessToGroup(99, groupName, 1);

    verify(insertStmt).setInt(1, 99);
    verify(insertStmt).setInt(2, 42);
    verify(insertStmt).executeUpdate();
  }

  @Test
  void grantPollAccessToGroup_B_largePollId_stillWorks() throws Exception {
    String groupName = "qa";

    PreparedStatement checkStmt = mock(PreparedStatement.class);
    ResultSet checkRs = mock(ResultSet.class);
    when(mockConn.prepareStatement(contains("FROM PollOwnership"))).thenReturn(checkStmt);
    when(checkStmt.executeQuery()).thenReturn(checkRs);
    when(checkRs.next()).thenReturn(true);

    PreparedStatement getStmt = mock(PreparedStatement.class);
    ResultSet getRs = mock(ResultSet.class);
    when(mockConn.prepareStatement(contains("FROM UserGroup"))).thenReturn(getStmt);
    when(getStmt.executeQuery()).thenReturn(getRs);
    when(getRs.next()).thenReturn(true);
    when(getRs.getInt("id")).thenReturn(123456789);

    PreparedStatement insertStmt = mock(PreparedStatement.class);
    when(mockConn.prepareStatement(contains("INSERT INTO PollAccessControl"))).thenReturn(insertStmt);

    db.grantPollAccessToGroup(Integer.MAX_VALUE, groupName, 1);

    verify(insertStmt).setInt(1, Integer.MAX_VALUE);
    verify(insertStmt).setInt(2, 123456789);
    verify(insertStmt).executeUpdate();
  }


  @Test
  void grantPollAccessToGroup_E_groupNotFound_throws() throws Exception {
    // Mock poll ownership check
    PreparedStatement ownStmt = mock(PreparedStatement.class);
    ResultSet ownRs = mock(ResultSet.class);
    when(mockConn.prepareStatement(contains("FROM PollOwnership"))).thenReturn(ownStmt);
    when(ownStmt.executeQuery()).thenReturn(ownRs);
    when(ownRs.next()).thenReturn(true); // simulate owner

    // Mock group lookup (not found)
    PreparedStatement groupStmt = mock(PreparedStatement.class);
    ResultSet groupRs = mock(ResultSet.class);
    when(mockConn.prepareStatement(contains("FROM UserGroup"))).thenReturn(groupStmt);
    when(groupStmt.executeQuery()).thenReturn(groupRs);
    when(groupRs.next()).thenReturn(false);  // group not found

    assertThrows(RuntimeException.class, () -> db.grantPollAccessToGroup(1, "ghost", 1));
  }

  @Test
  void grantPollAccessToGroup_E_sqlInsertError_throws() throws Exception {
    PreparedStatement checkStmt = mock(PreparedStatement.class);
    ResultSet checkRs = mock(ResultSet.class);
    when(mockConn.prepareStatement(contains("FROM PollOwnership"))).thenReturn(checkStmt);
    when(checkStmt.executeQuery()).thenReturn(checkRs);
    when(checkRs.next()).thenReturn(true);

    PreparedStatement getStmt = mock(PreparedStatement.class);
    ResultSet getRs = mock(ResultSet.class);
    when(mockConn.prepareStatement(contains("FROM UserGroup"))).thenReturn(getStmt);
    when(getStmt.executeQuery()).thenReturn(getRs);
    when(getRs.next()).thenReturn(true);
    when(getRs.getInt("id")).thenReturn(42);

    PreparedStatement insertStmt = mock(PreparedStatement.class);
    when(mockConn.prepareStatement(contains("INSERT INTO PollAccessControl"))).thenReturn(insertStmt);
    doThrow(new SQLException("Insert failed")).when(insertStmt).executeUpdate();

    assertThrows(RuntimeException.class, () -> db.grantPollAccessToGroup(1, "devs", 1));
  }

  @Test
  void getAllAvailablePolls_Z_zeroClientId_returnsEmptyList() throws Exception {
    PreparedStatement stmt = mock(PreparedStatement.class);
    ResultSet rs = mock(ResultSet.class);

    when(mockConn.prepareStatement(anyString())).thenReturn(stmt);
    when(stmt.executeQuery()).thenReturn(rs);
    when(rs.next()).thenReturn(false); // no polls

    List<Poll> result = db.getAllAvailablePolls(0);

    assertNotNull(result);
    assertTrue(result.isEmpty());
  }

  @Test
  void getAllAvailablePolls_O_onePoll_returnsCorrectPoll() throws Exception {
    int clientId = 42;
    int pollId = 100;

    // Mock Connection
    PreparedStatement stmt = mock(PreparedStatement.class);
    ResultSet rs = mock(ResultSet.class);

    // Mock allowed users & groups methods
    doReturn(Collections.emptyList()).when(db).getAllowedUsersForPoll(any(), eq(pollId));
    doReturn(Collections.emptyList()).when(db).getAllowedGroupsForPoll(any(), eq(pollId));

    when(mockConn.prepareStatement(startsWith("SELECT"))).thenReturn(stmt);
    when(stmt.executeQuery()).thenReturn(rs);

    when(rs.next()).thenReturn(true, false); // One poll
    when(rs.getInt("id")).thenReturn(pollId);
    when(rs.getString("title")).thenReturn("Test Poll");
    when(rs.getBoolean("is_private")).thenReturn(false);
    when(rs.getBoolean("is_closed")).thenReturn(false);
    when(rs.getInt("created_by_id")).thenReturn(clientId);

    List<Poll> polls = db.getAllAvailablePolls(clientId);

    assertEquals(1, polls.size());
    Poll poll = polls.get(0);
    assertEquals(pollId, poll.getId());
    assertEquals("Test Poll", poll.getTitle());
    assertFalse(poll.isPrivate());
    assertFalse(poll.isClosed());
    assertEquals(clientId, poll.getCreatedById());
  }



  @Test
  void getAllAvailablePolls_M_multiplePolls_returned() throws Exception {
    int clientId = 42;

    // Prepare mocks
    PreparedStatement stmt = mock(PreparedStatement.class);
    ResultSet rs = mock(ResultSet.class);

    when(mockConn.prepareStatement(startsWith("SELECT"))).thenReturn(stmt);
    when(stmt.executeQuery()).thenReturn(rs);

    // Simulate multiple poll rows
    when(rs.next()).thenReturn(true, true, false);  // 2 polls, then done
    when(rs.getInt("id")).thenReturn(1, 2);
    when(rs.getString("title")).thenReturn("Poll One", "Poll Two");
    when(rs.getBoolean("is_private")).thenReturn(false, true);
    when(rs.getBoolean("is_closed")).thenReturn(false, true);
    when(rs.getInt("created_by_id")).thenReturn(42, 43);

    // Stub access control methods
    doReturn(Collections.emptyList()).when(db).getAllowedUsersForPoll(any(), anyInt());
    doReturn(Collections.emptyList()).when(db).getAllowedGroupsForPoll(any(), anyInt());

    // Run the method
    List<Poll> polls = db.getAllAvailablePolls(clientId);

    // Assertions
    assertEquals(2, polls.size());

    Poll p1 = polls.get(0);
    Poll p2 = polls.get(1);

    assertEquals("Poll One", p1.getTitle());
    assertEquals("Poll Two", p2.getTitle());

    assertFalse(p1.isPrivate());
    assertTrue(p2.isPrivate());

    assertEquals(42, p1.getCreatedById());
    assertEquals(43, p2.getCreatedById());
  }

  @Test
  void getAllAvailablePolls_B_maxClientId_executesSuccessfully() throws Exception {
    PreparedStatement stmt = mock(PreparedStatement.class);
    ResultSet rs = mock(ResultSet.class);

    when(mockConn.prepareStatement(anyString())).thenReturn(stmt);
    when(stmt.executeQuery()).thenReturn(rs);
    when(rs.next()).thenReturn(false); // no polls

    List<Poll> result = db.getAllAvailablePolls(Integer.MAX_VALUE);

    assertNotNull(result);
    assertTrue(result.isEmpty());
  }

  @Test
  void getAllAvailablePolls_E_sqlException_throwsRuntime() throws Exception {
    when(mockConn.prepareStatement(anyString())).thenThrow(new SQLException("DB fail"));

    RuntimeException ex = assertThrows(RuntimeException.class, () -> db.getAllAvailablePolls(1));
    assertTrue(ex.getMessage().contains("Failed to fetch available polls"));
  }

  @Test
  void getAllAvailablePolls_S_pollHasCorrectFields() throws Exception {
    PreparedStatement stmt = mock(PreparedStatement.class);
    ResultSet rs = mock(ResultSet.class);

    when(mockConn.prepareStatement(anyString())).thenReturn(stmt);
    when(stmt.executeQuery()).thenReturn(rs);
    when(rs.next()).thenReturn(true, false);
    when(rs.getInt("id")).thenReturn(777);
    when(rs.getString("title")).thenReturn("Final Poll");
    when(rs.getBoolean("is_private")).thenReturn(true);
    when(rs.getBoolean("is_closed")).thenReturn(false);
    when(rs.getInt("created_by_id")).thenReturn(5);

    doReturn(Collections.emptyList()).when(db).getAllowedUsersForPoll(any(), eq(777));
    doReturn(Collections.emptyList()).when(db).getAllowedGroupsForPoll(any(), eq(777));

    List<Poll> result = db.getAllAvailablePolls(5);

    assertEquals(1, result.size());
    Poll poll = result.get(0);
    assertEquals(777, poll.getId());
    assertEquals("Final Poll", poll.getTitle());
    assertTrue(poll.isPrivate());
    assertFalse(poll.isClosed());
    assertEquals(5, poll.getCreatedById());
  }










}
