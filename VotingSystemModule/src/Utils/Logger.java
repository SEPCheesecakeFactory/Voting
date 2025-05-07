package Utils;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class Logger {
  private static final DateTimeFormatter TIME_FORMATTER =
      DateTimeFormatter.ofPattern("HH:mm:ss.SSS");

  public static void log(String message) {
    String actor = getCallerClassName();
    log(actor, message);
  }

  public static void log(String actor, String message) {
    String time = LocalTime.now().format(TIME_FORMATTER);
    System.out.printf("[%s] <%s> %s%n", time, actor, message);
  }

  public static void log()
  {
    log("");
  }

  private static String getCallerClassName() {
    StackTraceElement[] stack = Thread.currentThread().getStackTrace();
    // 0: getStackTrace, 1: getCallerClassName, 2: log(String), 3: caller
    for (int i = 2; i < stack.length; i++) {
      String className = stack[i].getClassName();
      if (!className.equals(Logger.class.getName())) {
        return className.substring(className.lastIndexOf('.') + 1);
      }
    }
    return "UnknownActor";
  }
}
