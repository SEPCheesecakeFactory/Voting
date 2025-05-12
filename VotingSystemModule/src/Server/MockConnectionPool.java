package Server;

import Common.Message;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MockConnectionPool extends ConnectionPool {
  private final List<Message> broadcastedMessages = new ArrayList<>();

  @Override
  public void broadcast(Message message) throws IOException {
    broadcastedMessages.add(message); // Capture the message instead of sending
  }

  public List<Message> getBroadcastedMessages() {
    return broadcastedMessages;
  }

  public void clear() {
    broadcastedMessages.clear();
  }
}
