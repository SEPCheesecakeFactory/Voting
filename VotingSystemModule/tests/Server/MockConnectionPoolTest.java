package Server;

import Common.Message;
import Common.MessageType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class MockConnectionPoolTest {

  private MockConnectionPool pool;

  @BeforeEach
  void setUp() {
    pool = new MockConnectionPool();
  }

  @Test
  void broadcast_addsMessageToList() throws IOException {
    Message message = new Message(MessageType.SendPoll);
    pool.broadcast(message);

    List<Message> messages = pool.getBroadcastedMessages();
    assertEquals(1, messages.size());
    assertEquals(MessageType.SendPoll, messages.get(0).getType());
  }

  @Test
  void broadcast_multipleMessages_allCaptured() throws IOException {
    Message m1 = new Message(MessageType.SendVote);
    Message m2 = new Message(MessageType.ClosePoll);
    pool.broadcast(m1);
    pool.broadcast(m2);

    List<Message> messages = pool.getBroadcastedMessages();
    assertEquals(2, messages.size());
    assertTrue(messages.contains(m1));
    assertTrue(messages.contains(m2));
  }

  @Test
  void clear_removesAllMessages() throws IOException {
    pool.broadcast(new Message(MessageType.RequestPollResult));
    assertFalse(pool.getBroadcastedMessages().isEmpty());

    pool.clear();
    assertTrue(pool.getBroadcastedMessages().isEmpty());
  }
}
