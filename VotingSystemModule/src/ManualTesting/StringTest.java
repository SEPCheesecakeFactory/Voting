package ManualTesting;

public class StringTest
{
  // Testing - https://docs.oracle.com/javase/8/docs/technotes/guides/language/strings-switch.html
  public static void main(String[] args)
  {
    String string = "something";
    String sttrring = "some";
    sttrring+="thing";

    if(string == "some"+"thing")
      System.out.println("string == something");
    else
      System.out.println("string != something");

    if(string == sttrring)
      System.out.println("string == sttrring");
    else
      System.out.println("string != sttrring");

    System.out.println(sttrring);

    switch (string)
    {
      case "something":
        System.out.println("case something");
        break;
      default:
        System.out.println("default");
        break;
    }
    switch (sttrring)
    {
      case "something":
        System.out.println("case something");
        break;
      default:
        System.out.println("default");
        break;
    }
  }
}
