package Server;
//Michal Sockets 2 class
import Common.Message;
import Utils.JsonUtil;
import Utils.Logger;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ConnectionPool
{
  private final List<ServerConnection> connections;
  private final Map<Integer, ServerConnection> identifiedConnections;

  public ConnectionPool()
  {
    this.connections = new ArrayList<>();
    this.identifiedConnections = new ConcurrentHashMap<Integer, ServerConnection>();
  }

  public void add(ServerConnection serverConnection)
  {
    connections.add(serverConnection);
  }
  public void changeToMap(Message message, ServerConnection serverConnection){
    identifiedConnections.put(message.getParam("clientConnectionIndex", int.class), serverConnection);
    connections.remove(serverConnection);
  }

  public void broadcast(Message message) throws IOException
  {
    for (ServerConnection connection : connections)
    {
      connection.send(JsonUtil.serialize(message));
    }
  }
  public void sendDirectMessage(Message message) throws IOException {
    int clientConnectionIndex = message.getParam("clientConnectionIndex", int.class);
    ServerConnection conn = identifiedConnections.get(clientConnectionIndex);
    if (conn != null) {
      conn.send(JsonUtil.serialize(message));
    } else {
      Logger.log("No connection with index " + clientConnectionIndex);
    }
  }

}