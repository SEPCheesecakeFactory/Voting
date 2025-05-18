package Common;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ProfileTest {

  @Test
  void ctor_andGetters() {
    Profile p = new Profile("user1");
    assertEquals("user1", p.getUsername());
    assertEquals(0, p.getId());
  }

  @Test
  void changeUsername_updatesField() {
    Profile p = new Profile("old");
    p.changeUsername("new");
    assertEquals("new", p.getUsername());
  }

  @Test
  void idSetter_getter() {
    Profile p = new Profile("u");
    p.setId(123);
    assertEquals(123, p.getId());
  }

  @Test
  void equals_andHashSemantics() {
    Profile a = new Profile("a");
    Profile b = new Profile("b");
    a.setId(5);
    b.setId(5);
    Profile c = new Profile("c");
    c.setId(6);

    assertEquals(a, b,   "Profiles with same id should be equal");
    assertNotEquals(a, c, "Different ids => not equal");
    assertNotEquals(a, null);
    assertNotEquals(a, new Object());
    assertTrue(a.equals(a), "Same instance equals itself");
  }
}