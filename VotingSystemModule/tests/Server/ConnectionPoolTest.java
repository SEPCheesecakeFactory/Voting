package Server;

import Common.Message;
import Common.MessageType;
import Utils.JsonUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ConnectionPoolTest {

  private ConnectionPool pool;
  private ServerConnection mockConn1;
  private ServerConnection mockConn2;
  private Message mockMessage;

  @BeforeEach
  void setUp() {
    pool = new ConnectionPool();
    mockConn1 = mock(ServerConnection.class);
    mockConn2 = mock(ServerConnection.class);
    mockMessage = new Message(MessageType.Test);
    mockMessage.addParam("clientConnectionIndex", 42);
  }

  @Test
  void broadcast_zeroConnections_noError() {
    assertDoesNotThrow(() -> pool.broadcast(mockMessage));
  }

  @Test
  void broadcast_oneConnection_sendsMessage() throws IOException {
    pool.add(mockConn1);
    pool.broadcast(mockMessage);
    verify(mockConn1, times(1)).send(anyString());
  }

  @Test
  void broadcast_multipleConnections_allReceiveMessage() throws IOException {
    pool.add(mockConn1);
    pool.add(mockConn2);
    pool.broadcast(mockMessage);
    verify(mockConn1).send(anyString());
    verify(mockConn2).send(anyString());
  }

  @Test
  void changeToMap_removesFromList_andAddsToMap() throws IOException {
    pool.add(mockConn1);
    pool.changeToMap(mockMessage, mockConn1);
    // Verify it does not receive broadcast after being moved
    pool.broadcast(mockMessage);
    verify(mockConn1, never()).send(anyString());
  }

  @Test
  void sendDirectMessage_validConnectionIndex_sends() throws IOException {
    pool.changeToMap(mockMessage, mockConn1);
    pool.sendDirectMessage(mockMessage);
    verify(mockConn1).send(anyString());
  }

  @Test
  void sendDirectMessage_invalidConnectionIndex_logsOnly() {
    assertDoesNotThrow(() -> pool.sendDirectMessage(mockMessage));
  }

  @Test
  void sendDirectMessage_sendThrowsIOException_propagates() throws IOException {
    doThrow(new IOException("Fail")).when(mockConn1).send(anyString());
    pool.changeToMap(mockMessage, mockConn1);
    assertThrows(IOException.class, () -> pool.sendDirectMessage(mockMessage));
  }
}
