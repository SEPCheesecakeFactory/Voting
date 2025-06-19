package Client;

import Common.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class ModelTest {

  // Stub Client to capture sent messages
  static class StubClient extends Client {
    List<Message> sent = new ArrayList<>();
    boolean nextReturn = true;
    @Override
    public boolean send(Message m) {
      sent.add(m);
      return nextReturn;
    }
    void setNextReturn(boolean b) { nextReturn = b; }
    Message last() { return sent.isEmpty() ? null : sent.get(sent.size()-1); }
  }

  private StubClient client;
  private Model model;
  private List<PropertyChangeEvent> events;

  @BeforeEach
  void setUp() {
    client = new StubClient();
    model = new Model(client);
    events = new ArrayList<>();
    // global listener
    model.addPropertyChangeListener(events::add);
  }

  @Test
  void ctor_nullClient_throws() {
    assertThrows(IllegalArgumentException.class, () -> new Model(null));
  }

  @Test
  void setPoll_firesPollUpdated() {
    Poll old = new Poll("old","",1,new Question[]{}, false);
    model.setPoll(old);
    events.clear();
    Poll ne = new Poll("new","",2,new Question[]{}, false);
    model.setPoll(ne);
    assertEquals(1, events.size());
    PropertyChangeEvent e = events.get(0);
    assertEquals("PollUpdated", e.getPropertyName());
    assertEquals(old, e.getOldValue());
    assertEquals(ne, e.getNewValue());
  }

  @Test
  void setMessage_firesNewMessage() {
    model.setMessage("hello");
    assertEquals(1, events.size());
    PropertyChangeEvent e = events.get(0);
    assertEquals("NewMessage", e.getPropertyName());
    assertNull(e.getOldValue());
    assertEquals("hello", e.getNewValue());
  }

  @Test
  void setProfile_firesAndGetProfile() {
    Profile p = new Profile("u");
    model.setProfile(p);
    assertEquals(p, model.getProfile());
    assertEquals("ProfileSet", events.get(0).getPropertyName());
  }

  @Test
  void sendLoginOrRegister_sendsProperMessage() {
    Profile p = new Profile("alice");
    p.setId(5);
//    model.sendLoginOrRegister(p);
    Message m = client.last();
    assertEquals(MessageType.SendLoginOrRegister, m.getType());
    assertEquals(p, m.getParam("profile", Profile.class));
  }

  @Test
  void sendChangeUsername_updatesProfileAndSends() {
    Profile p = new Profile("old");
    p.setId(1);
    model.setProfile(p);
    client.sent.clear();
    model.sendChangeUsername("newName");
    assertEquals("newName", model.getProfile().getUsername());
    Message m = client.last();
    assertEquals(MessageType.SendChangeUsername, m.getType());
    Profile sentParam = m.getParam("username", Profile.class);
    assertEquals("newName", sentParam.getUsername());
  }

  @Test
  void fireUsernameChanged_andFailed() {
    model.setProfile(new Profile("x"));
    events.clear();
    model.fireUsernameChanged();
    model.fireUsernameChangeFailed("nope");
    assertEquals(2, events.size());
    assertEquals("UsernameChanged", events.get(0).getPropertyName());
    assertEquals("UsernameChangeFailed", events.get(1).getPropertyName());
    assertEquals("nope", events.get(1).getNewValue());
  }

  @Test
  void sendVote_andClose_andDisplay_andAvailablePolls() {
    model.setProfile(new Profile("u") {{ setId(10); }});
    client.sent.clear();

    model.sendVote(10, new int[]{1,2});
    assertEquals(MessageType.SendVote, client.last().getType());
    Vote v = client.last().getParam("vote", Vote.class);
    assertArrayEquals(new int[]{1,2}, v.getChoices());

    model.sendPollCloseRequest(7);
    assertEquals(MessageType.ClosePoll, client.last().getType());
    assertEquals(7, (int)client.last().getParam("pollId", Integer.class));

    model.sendDisplayPollRequest(3);
    assertEquals(MessageType.DisplayPollRequest, client.last().getType());

    client.sent.clear();
    model.requestAvailablePolls();
    assertEquals(MessageType.GetAvailablePolls, client.last().getType());
    assertEquals(10, (int)client.last().getParam("userId", Integer.class));
  }

  @Test
  void handleAvailablePolls_firesEventAndStores() {
    List<Poll> list = List.of(new Poll());
    events.clear();
    model.handleAvailablePolls(list);
    assertEquals(list, model.getAvailablePolls());
    assertEquals("AvailablePolls", events.get(0).getPropertyName());
    assertEquals(list, events.get(0).getNewValue());
  }

  // The result requesting is incredibly weird!
  /*
  @Test
  void resultRequest_andHandling() {
    client.sent.clear();
    model.sendResultRequest(20);
    assertEquals(MessageType.SendResultRequest, client.last().getType());

    events.clear();
    PollResult pr = new PollResult();
    model.getResult(pr);
    assertEquals("PollResult", events.get(0).getPropertyName());
    assertSame(pr, events.get(0).getNewValue());
  }*/

  @Test
  void voteGroup_andUserLookup_andGroupLookup_andAccess() {
    Profile p = new Profile("bob"); p.setId(2);
    model.setProfile(p);
    client.sent.clear();

    UserGroup g = new UserGroup("grp"); g.setId(8);
    model.sendVoteGroup(g);
    Message m1 = client.last();
    assertEquals(MessageType.SendCreateVoteGroupRequest, m1.getType());

    model.requestUserLookup("foo");
    assertEquals(MessageType.LookupUser, client.last().getType());

    events.clear();
    Profile found = new Profile("found");
    model.handleUserLookupResult(found);
    assertEquals("LookupUserResults", events.get(0).getPropertyName());

    model.requestUserGroups();
    assertEquals(MessageType.SendUserGroupsRequest, client.last().getType());

    events.clear();
    List<UserGroup> groups = List.of(g);
    model.receiveUserGroups(groups);
    assertEquals("receiveUserGroups", events.get(0).getPropertyName());

    events.clear();
    model.handleUserGroupLookupResult1(g);
    assertEquals("LookupGroupResults", events.get(0).getPropertyName());

    client.sent.clear();
    Set<Profile> users = Set.of(p);
    Set<UserGroup> gs = Set.of(g);
    model.sendPollAccess(11, users, gs);
    Message m2 = client.last();
    assertEquals(MessageType.SendPollAccess, m2.getType());

    model.requestGroupLookup1("grp");
    assertEquals(MessageType.LookupGroup, client.last().getType());
  }

  @Test
  void createPoll_andSendPollRequest() {
    Profile p = new Profile("u"); p.setId(3);
    model.setProfile(p);
    client.sent.clear();

    Poll poll = new Poll("T","",4,new Question[]{},false);
    model.createPoll(poll);
    assertEquals(MessageType.CreatePoll, client.last().getType());

    model.sendPollRequest(4);
    assertEquals(MessageType.SendPollRequest, client.last().getType());
  }

  @Test
  void listenerRegistrationAndRemoval() {
    PropertyChangeListener foo = evt -> {};
    model.addPropertyChangeListener("Foo", foo);
    model.removePropertyChangeListener(foo);
    model.removePropertyChangeListener("Foo", foo);
    // no exceptions thrown
  }
}
