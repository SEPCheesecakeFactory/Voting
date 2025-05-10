package Utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * Utility class for JSON serialization and deserialization using Gson.
 */
public class JsonUtil {
  private static final Gson GSON = new GsonBuilder().create();

  /**
   * Serializes an object to its JSON representation.
   *
   * @param object the object to serialize
   * @return JSON string
   */
  public static String serialize(Object object) {
    return GSON.toJson(object);
  }

  /**
   * Deserializes a JSON string into an object of the specified class.
   *
   * @param json  the JSON string
   * @param clazz the target class
   * @param <T>   type of the target object
   * @return deserialized object
   */
  public static <T> T deserialize(String json, Class<T> clazz) {
    return GSON.fromJson(json, clazz);
  }
}