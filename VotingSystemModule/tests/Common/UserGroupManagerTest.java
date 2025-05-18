package Common;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class UserGroupManagerTest {

  private UserGroupManager mgr;
  private Profile alice;
  private Profile bob;

  @BeforeEach
  void setUp() {
    mgr = new UserGroupManager();
    alice = new Profile("alice");
    alice.setId(1);
    bob = new Profile("bob");
    bob.setId(2);
  }

  @Test
  void constructor_initializesEmptyList() {
    assertNotNull(mgr.groups, "groups list must be initialized");
    assertTrue(mgr.groups.isEmpty(), "no groups initially");
  }

  @Test
  void createGroup_returnsTrueOnce() {
    assertTrue(mgr.createGroup("devs"), "should create new group");
    assertFalse(mgr.createGroup("devs"), "duplicate name must fail");
  }

  @Test
  void getGroup_returnsCorrectOrNull() {
    assertNull(mgr.getGroup("nonexistent"), "no such group");
    mgr.createGroup("team");
    UserGroup g = mgr.getGroup("team");
    assertNotNull(g);
    assertEquals("team", g.getGroupName());
  }

  @Test
  void getAllGroups_returnsCopy() {
    mgr.createGroup("g1");
    mgr.createGroup("g2");
    List<UserGroup> all = mgr.getAllGroups();
    assertEquals(2, all.size());
    // modifying returned list does not affect manager
    all.remove(0);
    assertEquals(2, mgr.getAllGroups().size(), "internal list must be unmodified");
  }

  @Test
  void isUserInGroup_reportsMembership() {
    mgr.createGroup("team");
    assertFalse(mgr.isUserInGroup("team", alice), "no members yet");
    UserGroup team = mgr.getGroup("team");
    team.addMember(alice);
    assertTrue(mgr.isUserInGroup("team", alice), "alice has been added");
    assertFalse(mgr.isUserInGroup("team", bob), "bob not added");
    // non-existent group
    assertFalse(mgr.isUserInGroup("nope", alice));
  }
}
