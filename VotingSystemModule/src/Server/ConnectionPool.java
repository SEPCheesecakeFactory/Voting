package Server;
//Michal Sockets 2 class
import Common.Message;
import Utils.JsonUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ConnectionPool
{
  private final List<ServerConnection> connections;

  public ConnectionPool()
  {
    this.connections = new ArrayList<>();
  }

  public void add(ServerConnection serverConnection)
  {
    connections.add(serverConnection);
  }

  public void broadcast(Message message) throws IOException
  {
    for (ServerConnection connection : connections)
    {
      connection.send(JsonUtil.serialize(message));
    }
  }
}