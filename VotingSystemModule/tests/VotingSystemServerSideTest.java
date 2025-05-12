import Client.Client;
import Server.ConnectionPool;
import Server.MockDatabaseConnection;
import Server.ServerConnection;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.SQLException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class VotingSystemServerSideTest
{


  @BeforeEach
  public void setUp() throws IOException
  {


  }



  @Test
  public void testServerReceivesClientRequest() throws Exception {

  }
}

