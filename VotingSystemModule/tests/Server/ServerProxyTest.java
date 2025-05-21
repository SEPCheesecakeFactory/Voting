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
  private ServerConnection mockConnection;

  @BeforeEach
  void setup() {
    mockModel = mock(ServerModel.class);
    proxy = new ServerProxy(mockModel);
    mockDb = mock(DatabaseConnector.class);
    mockConnection = mock(ServerConnection.class);
    when(mockModel.getDb()).thenReturn(mockDb);
  }

  @Test
  void handle_vote_delegatesToHandle() {
    Vote vote = new Vote(1, new int[]{1, 2});
    proxy.handle(vote);
    verify(mockModel).handle(vote);
  }


  @Test
  void handle_closePoll_delegatesCall() {
    proxy.handle("close_poll:123");
    verify(mockModel).handle("close_poll:123");
  }

  @Test
  void handle_resultRequest_logsResults() {
    PollResult mockResult = mock(PollResult.class);
    when(mockModel.retrievePollResult(88)).thenReturn(mockResult);

    proxy.handle("result_request:88");

    verify(mockModel).handle("result_request:88");
  }

  @Test
  void process_sendPollRequest_callsSendPoll() {
    Message msg = new Message(MessageType.SendPollRequest);
    msg.addParam("pollId", 42);
    msg.addParam("clientConnectionIndex", 1);
    String serialized = JsonUtil.serialize(msg);

    proxy.process(serialized, mockConnection);

    verify(mockModel).process(serialized, mockConnection);
  }

  @Test
  void process_sendVote_delegatesToModel() throws SQLException {
    Vote vote = new Vote(5, new int[]{3});
    Message msg = new Message(MessageType.SendVote);
    msg.addParam("vote", vote);
    msg.addParam("clientConnectionIndex", 0);

    String serialized = JsonUtil.serialize(msg);
    proxy.process(serialized, mockConnection);

    verify(mockModel).process(serialized, mockConnection);
  }

  @Test
  void process_lookupUser_userNotFound_delegatesToModel() {
    Profile lookup = new Profile("ghost");
    Message msg = new Message(MessageType.LookupUser);
    msg.addParam("profile", lookup);
    msg.addParam("clientConnectionIndex", 0);

    String serialized = JsonUtil.serialize(msg);
    proxy.process(serialized, mockConnection);

    verify(mockModel).process(serialized, mockConnection);
  }

  }

