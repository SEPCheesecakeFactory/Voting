import Common.*;
import Server.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class VotingSystemServerSideTest {

  private MockDatabaseConnection mockDb;
  private MockConnectionPool MockConnectionPool;
  private ServerModel serverModel;
  private ServerProxy serverProxy;

  @BeforeEach
  public void setUp() {
    mockDb = new MockDatabaseConnection();
    MockConnectionPool = new MockConnectionPool();
    serverModel = new ServerModel(mockDb, MockConnectionPool);
    serverProxy = new ServerProxy(serverModel);
  }

  @Test
  public void testProcessSendVote_storesVote() {
    Vote vote = new Vote(1, new int[]{1,3,5});
    Message msg = new Message(MessageType.SendVote);
    msg.addParam("vote", vote);
    String json = Utils.JsonUtil.serialize(msg);

    serverProxy.process(json);


    Vote result = mockDb.votes.get(1);
    assertNotNull(result);
    assertEquals(vote.getUserId(), result.getUserId());
    assertArrayEquals(vote.getChoices(), result.getChoices());
  }

  @Test
  public void testProcessSendLoginOrRegister_registersNewProfile() {
    Profile profile = new Profile("newUser");

    Message msg = new Message(MessageType.SendLoginOrRegister);
    msg.addParam("profile", profile);
    String json = Utils.JsonUtil.serialize(msg);

    serverProxy.process(json);

    // Check if profile is now stored
    assertTrue(mockDb.profileExists("newUser"));

    // Check that the updated profile was sent back
    assertFalse(MockConnectionPool.getBroadcastedMessages().isEmpty());
    Message broadcast = MockConnectionPool.getBroadcastedMessages().get(0);
    assertEquals(MessageType.SendProfileBack, broadcast.getType());

    Profile updatedProfile = broadcast.getParam("UpdatedProfile", Profile.class);
    assertEquals("newUser", updatedProfile.getUsername());
    assertNotEquals(0, updatedProfile.getId());
    assertNotNull(updatedProfile.getId());
  }


  @Test
  public void testProcessSendLoginOrRegister_loginProfile() {
    Profile profile = new Profile("newUser1");

    Message msg = new Message(MessageType.SendLoginOrRegister);
    msg.addParam("profile", profile);
    String json = Utils.JsonUtil.serialize(msg);

    serverProxy.process(json);



    profile = new Profile("newUser2");

   msg = new Message(MessageType.SendLoginOrRegister);
    msg.addParam("profile", profile);
     json = Utils.JsonUtil.serialize(msg);

    serverProxy.process(json);



    profile = new Profile("newUser2");

    msg = new Message(MessageType.SendLoginOrRegister);
    msg.addParam("profile", profile);
    json = Utils.JsonUtil.serialize(msg);

    serverProxy.process(json);

    // Check if profile is there
    assertTrue(mockDb.profileExists("newUser2"));




    // Check that the updated profile was sent back
    assertFalse(MockConnectionPool.getBroadcastedMessages().isEmpty());
    Message broadcast = MockConnectionPool.getBroadcastedMessages().getLast();
    assertEquals(MessageType.SendProfileBack, broadcast.getType());

    Profile updatedProfile = broadcast.getParam("UpdatedProfile", Profile.class);
    assertEquals("newUser2", updatedProfile.getUsername());
    assertEquals(2,updatedProfile.getId());
  }

  @Test
  public void testProcessSendChangeUsername_validChange() {
    // First, register a profile
    Profile profile = new Profile("originalUser");
    Message msg = new Message(MessageType.SendLoginOrRegister);
    msg.addParam("profile", profile);
    String json = Utils.JsonUtil.serialize(msg);
    serverProxy.process(json);
    Message broadcast = MockConnectionPool.getBroadcastedMessages().getLast();
    profile = broadcast.getParam("UpdatedProfile", Profile.class);


    // Now change the username
    profile.changeUsername("newValidUser");
    msg = new Message(MessageType.SendChangeUsername);
    msg.addParam("username", profile);
    json = Utils.JsonUtil.serialize(msg);
    serverProxy.process(json);

    // Check that the username has been updated in the mock DB
    Profile updatedProfile = mockDb.profiles.get(profile.getId());
    assertNotNull(updatedProfile);
    assertEquals("newValidUser", updatedProfile.getUsername());

    // Check that the success message was sent back
    assertFalse(MockConnectionPool.getBroadcastedMessages().isEmpty());
    broadcast = MockConnectionPool.getBroadcastedMessages().getLast();
    assertEquals(MessageType.SendProfileBack, broadcast.getType());

    assertEquals("newValidUser", mockDb.profiles.get(profile.getId()).getUsername());
  }
  @Test
  public void testProcessSendChangeUsername_existingUsername() {
    // First, register 2  profiles
    Profile profile = new Profile("OriginalUser");
    Message msg = new Message(MessageType.SendLoginOrRegister);
    msg.addParam("profile", profile);
    String json = Utils.JsonUtil.serialize(msg);
    serverProxy.process(json);
    Message broadcast = MockConnectionPool.getBroadcastedMessages().getLast();
    profile = broadcast.getParam("UpdatedProfile", Profile.class);

    profile = new Profile("newValidUser");
    msg = new Message(MessageType.SendLoginOrRegister);
    msg.addParam("profile", profile);
    json = Utils.JsonUtil.serialize(msg);
    serverProxy.process(json);
    broadcast = MockConnectionPool.getBroadcastedMessages().getLast();
    profile = broadcast.getParam("UpdatedProfile", Profile.class);


    // Now change the username
    profile.changeUsername("OriginalUser");
    msg = new Message(MessageType.SendChangeUsername);
    msg.addParam("username", profile);
    json = Utils.JsonUtil.serialize(msg);
    String finalJson1 = json;
    assertThrows(RuntimeException.class, ()->serverProxy.process(
        finalJson1));
  }
  @Test
  public void testProcessClosePoll_AuthorisedAttempt() {
    //cannot test yet cause closing a poll has only 1 argument for now - pollID and we need to know who is closing
    assertEquals(1,0);
  }
  @Test
  public void testProcessClosePoll_UnauthorisedAttempt() {
    //cannot test yet cause closing a poll has only 1 argument for now - pollID and we need to know who is closing
    assertEquals(1,0);
  }




}
