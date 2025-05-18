package Common;

import org.junit.jupiter.api.Test;

import java.io.*;

import static org.junit.jupiter.api.Assertions.*;

class QuestionTest {

  @Test
  void constructorAndGetters() {
    ChoiceOption[] opts = {
        new ChoiceOption(1, "A"),
        new ChoiceOption(2, "B")
    };
    Question q = new Question(opts, 99, "Title", "Desc");

    assertArrayEquals(opts, q.getChoiceOptions(), "choiceOptions must match constructor input");
    assertEquals(99, q.getId(),      "id must match constructor");
    assertEquals("Title", q.getTitle(),       "title must match constructor");
    assertEquals("Desc",  q.getDescription(), "description must match constructor");
  }

  @Test
  void settersUpdateFields() {
    Question q = new Question(new ChoiceOption[0], 0, "", "");

    ChoiceOption[] newOpts = { new ChoiceOption(7, "X") };
    q.setChoiceOptions(newOpts);
    q.setId(7);
    q.setTitle("NewTitle");
    q.setDescription("NewDesc");

    assertArrayEquals(newOpts, q.getChoiceOptions(), "setChoiceOptions should replace array");
    assertEquals(7, q.getId(),           "setId should update id");
    assertEquals("NewTitle", q.getTitle(),       "setTitle should update title");
    assertEquals("NewDesc",  q.getDescription(), "setDescription should update description");
  }

  @Test
  void setChoiceOptionsIsIndependent() {
    ChoiceOption[] opts1 = { new ChoiceOption(1, "One") };
    Question q = new Question(opts1, 1, "T", "D");

    ChoiceOption[] opts2 = { new ChoiceOption(2, "Two") };
    q.setChoiceOptions(opts2);
    ChoiceOption[] retrieved = q.getChoiceOptions();

    assertArrayEquals(opts2, retrieved, "After setter, getter returns new array reference");
    assertNotSame(opts1, retrieved,   "New array must not be the old one");
  }

  @Test
  void serializationRoundTrip_preservesData() throws IOException, ClassNotFoundException {
    ChoiceOption[] opts = {
        new ChoiceOption(3, "C"),
        new ChoiceOption(4, "D")
    };
    Question original = new Question(opts, 55, "S", "Desc");

    byte[] bytes;
    try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos)) {
      oos.writeObject(original);
      bytes = baos.toByteArray();
    }

    Question restored;
    try (ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(bytes))) {
      restored = (Question) ois.readObject();
    }

    assertNotSame(original, restored, "Deserialized object should be new instance");
    assertEquals(original.getId(),          restored.getId(),          "id must survive serialization");
    assertEquals(original.getTitle(),       restored.getTitle(),       "title must survive serialization");
    assertEquals(original.getDescription(), restored.getDescription(), "description must survive serialization");

    ChoiceOption[] rOpts = restored.getChoiceOptions();
    assertEquals(opts.length, rOpts.length, "choiceOptions length preserved");
    for (int i = 0; i < opts.length; i++) {
      assertEquals(opts[i].getId(),    rOpts[i].getId(),    "choiceOption id preserved at index " + i);
      assertEquals(opts[i].getValue(), rOpts[i].getValue(), "choiceOption value preserved at index " + i);
    }
  }
}
