package Common;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.List;

class MessageTest {

  @Test
  void testAddAndGetParam_Primitive() {
    Message msg = new Message(MessageType.Test);
    msg.addParam("greeting", "hello");
    String result = msg.getParam("greeting", String.class);
    assertEquals("hello", result, "Should return the exact string added");
  }

  @Test
  void testAddAndGetParam_ComplexType() {
    Message msg = new Message(MessageType.Test);
    List<Integer> numbers = Arrays.asList(1, 2, 3);
    msg.addParam("nums", numbers);

    Type typeOfList = new TypeToken<List<Integer>>() {}.getType();
    List<Integer> result = msg.getParam("nums", typeOfList);

    assertNotNull(result, "Deserialized list should not be null");
    assertEquals(numbers, result, "Should match the original list content");
  }

  @Test
  void testGetParam_MissingKey() {
    Message msg = new Message(MessageType.Test);
    Object result = msg.getParam("absent", Object.class);
    assertNull(result, "Missing key should yield null");
  }

  @Test
  void testTypeAccessorsAndToString() {
    Message msg = new Message(MessageType.Test);
    assertEquals(MessageType.Test, msg.getType(), "Initial type should match constructor");
    assertEquals("Test", msg.toString(), "toString() should return enum name");

    msg.setType(MessageType.SendChangeUsername);
    assertEquals(MessageType.SendChangeUsername, msg.getType(), "Type after setter should match");
    assertEquals("SendChangeUsername", msg.toString(), "toString() should reflect updated enum name");
  }
}
