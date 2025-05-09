package Server;

import Common.Vote;

import java.io.IOException;
import java.sql.SQLException;

public class ServerModel {
  private final DatabaseConnectionProxy db;
  private final ConnectionPool connectionPool;

  public ServerModel(DatabaseConnectionProxy db, ConnectionPool connectionPool) {
    this.db = db;
    this.connectionPool = connectionPool;
  }

  public void storeVote(Vote vote) throws SQLException {
    db.storeVote(vote);//change to edit if u wanna test
  }

  public void closePoll(int pollId)
      throws SQLException, IOException, IOException
  {
    db.closePollAndSaveResults(pollId);
    connectionPool.broadcast("poll_closed:" + pollId); // Notify all clients
  }

}
