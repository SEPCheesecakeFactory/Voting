package Common;

import org.junit.jupiter.api.Test;

import java.io.*;

import static org.junit.jupiter.api.Assertions.*;

class VoteTest {

  @Test
  void constructorAndGetters() {
    int[] choices = {5, 10, 15};
    Vote v = new Vote(42, choices);

    assertEquals(42, v.getUserId(), "getUserId should return the constructor value");
    assertArrayEquals(choices, v.getChoices(), "getChoices should return the same array contents");
  }

  @Test
  void toStringFormatsCorrectly_singleChoice() {
    Vote v = new Vote(1, new int[]{99});
    String s = v.toString();
    assertTrue(s.contains("userId=1"), "toString should include userId");
    assertTrue(s.contains("choices=[99]"), "toString should include single choice in brackets");
    assertTrue(s.startsWith("Vote{") && s.endsWith("}"), "toString should wrap in Vote{...}");
  }

  @Test
  void toStringFormatsCorrectly_multipleChoices() {
    Vote v = new Vote(2, new int[]{1, 2, 3});
    String expected = "Vote{userId=2, choices=[1, 2, 3]}";
    assertEquals(expected, v.toString(), "toString should match exact format for multiple choices");
  }

  @Test
  void choicesArrayReferenceIsExposed() {
    int[] orig = {7, 8};
    Vote v = new Vote(3, orig);
    int[] returned = v.getChoices();
    // Mutate returned array
    returned[0] = 100;
    // Internal state should reflect mutation
    assertEquals(100, v.getChoices()[0], "getChoices returns direct reference to internal array");
  }

  @Test
  void serializationRoundTrip_preservesState() throws IOException, ClassNotFoundException {
    Vote original = new Vote(77, new int[]{11, 22});
    byte[] data;
    try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos)) {
      oos.writeObject(original);
      data = baos.toByteArray();
    }

    Vote restored;
    try (ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(data))) {
      restored = (Vote) ois.readObject();
    }

    assertNotSame(original, restored, "Deserialized object should be a new instance");
    assertEquals(original.getUserId(), restored.getUserId(), "userId must survive serialization");
    assertArrayEquals(original.getChoices(), restored.getChoices(), "choices array must survive serialization");
  }
}
