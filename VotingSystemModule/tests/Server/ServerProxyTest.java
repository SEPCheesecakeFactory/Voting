package Server;

import Common.*;
import Utils.JsonUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.sql.SQLException;
import java.util.*;

import static org.mockito.Mockito.*;

class ServerProxyTest {

  private ServerModel mockModel;
  private ServerProxy proxy;
  private DatabaseConnector mockDb;

  @BeforeEach
  void setup() {
    mockModel = mock(ServerModel.class);
    proxy = new ServerProxy(mockModel);
    mockDb = mock(DatabaseConnector.class);
    when(mockModel.getDb()).thenReturn(mockDb);
  }

  @Test
  void handle_vote_callsStoreVote() throws SQLException {
    Vote vote = new Vote(1, new int[]{1, 2});
    proxy.handle(vote);
    verify(mockModel).storeVote(argThat(v ->
        v.getUserId() == 1 && Arrays.equals(v.getChoices(), new int[]{1, 2})
    ));
  }


  @Test
  void handle_closePoll_unauthorized_sendsErrorMessage() {
    Profile user = new Profile("user");
    user.setId(10);
    when(mockModel.getCurrentProfile()).thenReturn(user);
    when(mockDb.isOwner(10, 123)).thenReturn(false);

    proxy.handle("close_poll:123");

    verify(mockModel).sendMessageToUser("You are not authorized to close this poll.");
  }

  @Test
  void handle_resultRequest_logsResults() {
    PollResult mockResult = mock(PollResult.class);
    when(mockModel.retrievePollResult(88)).thenReturn(mockResult);

    proxy.handle("result_request:88");

    verify(mockModel).retrievePollResult(88);
  }

  @Test
  void process_sendPollRequest_callsSendPoll() {
    Message msg = new Message(MessageType.SendPollRequest);
    msg.addParam("pollId", 42);
    msg.addParam("clientConnectionIndex", 1);

    proxy.process(JsonUtil.serialize(msg), this);

    verify(mockModel).sendPoll(42, 1);
  }

  @Test
  void process_sendVote_callsStoreVote() throws SQLException
  {
    Vote vote = new Vote(5, new int[]{3});
    Message msg = new Message(MessageType.SendVote);
    msg.addParam("vote", vote);
    msg.addParam("clientConnectionIndex", 0);

    proxy.process(JsonUtil.serialize(msg), this);

    verify(mockModel).storeVote(argThat(v ->
        v.getUserId() == 5 && Arrays.equals(v.getChoices(), new int[]{3})
    ));
  }

  @Test
  void process_closePoll_authorized_closesPoll()
      throws SQLException, IOException
  {
    Message msg = new Message(MessageType.ClosePoll);
    msg.addParam("pollId", 99);
    msg.addParam("userId", 5);
    msg.addParam("clientConnectionIndex", 2);

    when(mockDb.isOwner(5, 99)).thenReturn(true);

    proxy.process(JsonUtil.serialize(msg), this);

    verify(mockModel).closePoll(99, 2);
  }

  @Test
  void process_lookupUser_userNotFound_sendsDummyProfile() {
    Profile lookup = new Profile("ghost");
    Message msg = new Message(MessageType.LookupUser);
    msg.addParam("profile", lookup);
    msg.addParam("clientConnectionIndex", 0);

    when(mockDb.getProfileByUsername("ghost")).thenReturn(null);

    proxy.process(JsonUtil.serialize(msg), this);

    verify(mockModel).sendLookupUserResults(argThat(p -> p.getUsername().equals("ghost") && p.getId() == -1), eq(0));
  }

  @Test
  void process_lookupGroup_groupNotFound_sendsDummyGroup() {
    Message msg = new Message(MessageType.LookupGroup);
    msg.addParam("groupName", "nonexistent");
    msg.addParam("clientConnectionIndex", 3);

    when(mockDb.getGroupByUsername("nonexistent")).thenReturn(null);

    proxy.process(JsonUtil.serialize(msg), this);

    verify(mockModel).sendLookupGroupResults(argThat(g -> g.getGroupName().equals("nonexistent") && g.getId() == -1), eq(3));
  }

  @Test
  void process_sendLoginOrRegister_registersAndUpdatesProfile() {
    Profile profile = new Profile("newUser");
    Message msg = new Message(MessageType.SendLoginOrRegister);
    msg.addParam("profile", profile);
    msg.addParam("clientConnectionIndex", 4);

    when(mockDb.loginOrRegisterAProfile(any(Profile.class))).thenReturn(123);

    proxy.process(JsonUtil.serialize(msg), this);

    verify(mockModel).sendUpdatedProfile(argThat(p ->
        p.getUsername().equals("newUser") && p.getId() == 123), eq(4));
  }


  @Test
  void process_getAvailablePolls_sendsPolls() {
    List<Poll> polls = List.of(new Poll());
    when(mockDb.getAllAvailablePolls(10)).thenReturn(polls);

    Message msg = new Message(MessageType.GetAvailablePolls);
    msg.addParam("userId", 10);
    msg.addParam("clientConnectionIndex", 1);

    proxy.process(JsonUtil.serialize(msg), this);

    verify(mockModel).sendMessageToUser(anyString());
  }

  @Test
  void process_sendChangeUsername_succeeds() {
    Profile p = new Profile("updated");
    p.setId(50);

    Message msg = new Message(MessageType.SendChangeUsername);
    msg.addParam("username", p);
    msg.addParam("clientConnectionIndex", 0);

    proxy.process(JsonUtil.serialize(msg), this);

    verify(mockDb).changeUsername(p);
    verify(mockModel).sendMessageToUser(contains("Username successfully changed"));
  }

  @Test
  void process_sendPollAccess_grantsAccessToAllUsersAndGroups() {
    Profile u1 = new Profile("u1"); u1.setId(1);
    UserGroup g1 = new UserGroup("g1");

    Message msg = new Message(MessageType.SendPollAccess);
    msg.addParam("pollId", 55);
    msg.addParam("userId", 99);
    msg.addParam("users", Set.of(u1));
    msg.addParam("groups", Set.of(g1));
    msg.addParam("clientConnectionIndex", 0);

    proxy.process(JsonUtil.serialize(msg), this);

    verify(mockModel).grantPollAccessToUsers(eq(55),
        argThat(users -> users.stream().anyMatch(u -> u.getUsername().equals("u1") && u.getId() == 1)),
        eq(99)
    );

    verify(mockModel).grantPollAccessToGroups(eq(55),
        argThat(groups -> groups.stream().anyMatch(g -> g.getGroupName().equals("g1"))),
        eq(99)
    );
  }
}
