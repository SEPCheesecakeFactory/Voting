package Common;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class UserGroupTest {

  @Test
  void ctor_andGetters() {
    UserGroup g = new UserGroup("devs");
    assertEquals("devs", g.getGroupName());
    assertDoesNotThrow(g::getMembers);
    assertTrue(g.getMembers().isEmpty());
    assertEquals(0, g.getId());
  }

  @Test
  void idSetter_getter() {
    UserGroup g = new UserGroup("g");
    g.setId(55);
    assertEquals(55, g.getId());
  }

  @Test
  void addMember_preventsDuplicates() {
    Profile p1 = new Profile("u1");
    p1.setId(1);
    Profile dup = new Profile("u1");
    dup.setId(2);

    UserGroup g = new UserGroup("grp");
    assertTrue(g.addMember(p1));
    // same username => no add
    assertFalse(g.addMember(dup));
    assertEquals(1, g.getMembers().size());
  }

  @Test
  void removeMember_returnsCorrectly() {
    Profile p = new Profile("x");
    p.setId(7);
    UserGroup g = new UserGroup("grp");
    g.addMember(p);
    assertTrue(g.removeMember(p));
    assertFalse(g.removeMember(p));
  }

  @Test
  void serialization_roundTrip_preservesState() throws IOException, ClassNotFoundException {
    UserGroup orig = new UserGroup("team");
    orig.setId(3);
    Profile p = new Profile("a"); p.setId(9);
    orig.addMember(p);

    byte[] data;
    try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos)) {
      oos.writeObject(orig);
      data = baos.toByteArray();
    }

    UserGroup restored;
    try (ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(data))) {
      restored = (UserGroup) ois.readObject();
    }

    assertNotSame(orig, restored);
    assertAll(
        () -> assertEquals(orig.getGroupName(),   restored.getGroupName()),
        () -> assertEquals(orig.getId(),          restored.getId()),
        () -> assertEquals(orig.getMembers(),     restored.getMembers())
    );
  }
}
