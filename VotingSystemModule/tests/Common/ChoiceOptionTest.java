package Common;

import org.junit.jupiter.api.Test;

import java.io.*;

import static org.junit.jupiter.api.Assertions.*;

class ChoiceOptionTest {

  @Test
  void testConstructorAndGetters() {
    ChoiceOption opt = new ChoiceOption(42, "Answer");
    assertEquals(42, opt.getId(),   "ID from constructor should be retained");
    assertEquals("Answer", opt.getValue(), "Value from constructor should be retained");
  }

  @Test
  void testSetters() {
    ChoiceOption opt = new ChoiceOption(1, "one");
    opt.setId(99);
    opt.setValue("ninety-nine");
    assertEquals(99, opt.getId(), "setId should update the id field");
    assertEquals("ninety-nine", opt.getValue(), "setValue should update the value field");
  }

  @Test
  void testSerializationRoundTrip() throws IOException, ClassNotFoundException {
    ChoiceOption original = new ChoiceOption(7, "seven");

    // serialize
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    try (ObjectOutputStream oos = new ObjectOutputStream(baos)) {
      oos.writeObject(original);
    }

    // deserialize
    ChoiceOption restored;
    try (ObjectInputStream ois = new ObjectInputStream(
        new ByteArrayInputStream(baos.toByteArray()))) {
      restored = (ChoiceOption) ois.readObject();
    }

    assertNotSame(original, restored, "Deserialized object should be a new instance");
    assertEquals(original.getId(), restored.getId(),
        "ID must survive serialization");
    assertEquals(original.getValue(), restored.getValue(),
        "Value must survive serialization");
  }

  @Test
  void testMutableBehaviorIndependence() {
    ChoiceOption a = new ChoiceOption(5, "five");
    ChoiceOption b = new ChoiceOption(a.getId(), a.getValue());

    // mutate a
    a.setId(500);
    a.setValue("five-hundred");

    // verify b unchanged
    assertEquals(5, b.getId(),   "Separate instance should not reflect mutations");
    assertEquals("five", b.getValue(),
        "Separate instance should not reflect mutations");
  }
}
