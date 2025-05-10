package Common;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.lang.reflect.Type;
import java.util.ArrayList;

public class Message
{

  private MessageType type;
  private JsonObject params;
  private static final Gson G = new Gson();

  public Message(MessageType type)
  {
    this.type = type;
    this.params = new JsonObject();
  }

  public <T> T getParam(String name, Type typeOfT)
  {
    JsonElement elem = params.get(name);
    return new Gson().fromJson(elem, typeOfT);
  }

  public <T> void addParam(String name, T object)
  {
    params.add(name, G.toJsonTree(object));
  }

  @Override public String toString()
  {
    return type.name();
  }

  public MessageType getType()
  {
    return type;
  }

  public void setType(MessageType type)
  {
    this.type = type;
  }
}
