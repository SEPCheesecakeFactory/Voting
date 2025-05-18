package Common;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PollTest {

  private Profile alice;
  private Profile bob;
  private UserGroup group;

  @BeforeEach
  void setUp() {
    alice = new Profile("alice");
    alice.setId(1);
    bob = new Profile("bob");
    bob.setId(2);

    group = new UserGroup("team");
    group.setId(10);
    group.addMember(alice);
    group.addMember(bob);
  }

  @Test
  void defaultCtor_initializesEmptyCollections() {
    Poll p = new Poll();
    assertNotNull(p.getAllowedUsers());
    assertNotNull(p.getAllowedGroups());
    assertTrue(p.getAllowedUsers().isEmpty());
    assertTrue(p.getAllowedGroups().isEmpty());
  }

  @Test
  void parameterizedCtor_setsAllProperties() {
    Question[] qs = { new Question(new ChoiceOption[]{}, 5, "Q", "Desc") };
    Poll p = new Poll("Title", "Desc", 42, qs, true, true);

    assertAll(
        () -> assertEquals("Title",     p.getTitle()),
        () -> assertEquals("Desc",      p.getDescription()),
        () -> assertEquals(42,          p.getId()),
        () -> assertArrayEquals(qs,     p.getQuestions()),
        () -> assertTrue(p.isClosed()),
        () -> assertTrue(p.isPrivate()),
        () -> assertTrue(p.getAllowedUsers().isEmpty()),
        () -> assertTrue(p.getAllowedGroups().isEmpty())
    );
  }

  @Test
  void setters_andClosePoll_workAsExpected() {
    Poll p = new Poll();
    p.setTitle("X");
    p.setDescription("Y");
    p.setId(7);
    p.setQuestions(new Question[]{});
    p.setClosed(true);
    p.setPrivate(true);

    assertAll(
        () -> assertEquals("X", p.getTitle()),
        () -> assertEquals("Y", p.getDescription()),
        () -> assertEquals(7, p.getId()),
        () -> assertEquals(0, p.getQuestions().length),
        () -> assertTrue(p.isClosed()),
        () -> assertTrue(p.isPrivate())
    );

    // idempotent
    p.closePoll();
    assertTrue(p.isClosed());
  }

  @Test
  void addRemoveAllowedUser_noDuplicates() {
    Poll p = new Poll();
    p.addAllowedUser(alice);
    assertTrue(p.getAllowedUsers().contains(alice));
    // duplicate
    p.addAllowedUser(new Profile("alice") {{ setId(1); }});
    assertEquals(1, p.getAllowedUsers().size());
    p.removeAllowedUser(alice);
    assertTrue(p.getAllowedUsers().isEmpty());
  }

  @Test
  void addRemoveAllowedGroup_noDuplicates() {
    Poll p = new Poll();
    p.addAllowedGroup(group);
    assertTrue(p.getAllowedGroups().contains(group));
    // duplicate
    UserGroup same = new UserGroup("team");
    same.setId(10);
    same.addMember(alice);
    same.addMember(bob);
    p.addAllowedGroup(same);
    assertEquals(1, p.getAllowedGroups().size());
    p.removeAllowedGroup(group);
    assertTrue(p.getAllowedGroups().isEmpty());
  }

  @Test
  void canVote_public_alwaysTrue() {
    Poll p = new Poll("t","d",1,new Question[]{}, false);
    p.setPrivate(false);
    assertTrue(p.canVote(alice));
    assertTrue(p.canVote(bob));
  }

  @Test
  void canVote_private_withUserOrGroup() {
    Poll p = new Poll("t","d",1,new Question[]{}, false, true);
    p.addAllowedUser(alice);
    assertTrue(p.canVote(alice));
    assertFalse(p.canVote(bob));

    p = new Poll("t","d",1,new Question[]{}, false, true);
    p.addAllowedGroup(group);
    assertTrue(p.canVote(alice));
    assertTrue(p.canVote(bob));
  }

  @Test
  void canVote_private_deniesOthers() {
    Poll p = new Poll("t","d",1,new Question[]{}, false, true);
    assertFalse(p.canVote(alice));
  }

  @Test
  void createdById_accessor() {
    Poll p = new Poll();
    p.setCreatedById(99);
    assertEquals(99, p.getCreatedById());
  }

  @Test
  void serialization_roundTrip_preservesEverything() throws IOException, ClassNotFoundException {
    Poll orig = new Poll("T","D",8,new Question[]{}, true, true);
    orig.addAllowedUser(alice);
    orig.addAllowedGroup(group);
    orig.setCreatedById(77);

    byte[] data;
    try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos)) {
      oos.writeObject(orig);
      data = baos.toByteArray();
    }

    Poll restored;
    try (ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(data))) {
      restored = (Poll) ois.readObject();
    }

    assertNotSame(orig, restored);
    assertAll(
        () -> assertEquals(orig.getTitle(),        restored.getTitle()),
        () -> assertEquals(orig.getDescription(),  restored.getDescription()),
        () -> assertEquals(orig.getId(),           restored.getId()),
        () -> assertEquals(orig.isClosed(),        restored.isClosed()),
        () -> assertEquals(orig.isPrivate(),       restored.isPrivate()),
        () -> assertEquals(orig.getCreatedById(),  restored.getCreatedById()),
        () -> assertEquals(orig.getAllowedUsers(),  restored.getAllowedUsers()),
        () -> assertEquals(orig.getAllowedGroups(), restored.getAllowedGroups())
    );
  }
}