package salesmachine.util;
import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;

public class ExcHandle {

  /**
   * The default constructor
   */
  public ExcHandle() {
  }

  /**
   * Takes an exception and returns a String of the stack trace, for finding
   * where an error has occured (useful for debugging).
   */
  public static String getStackTraceAsString (Exception e) {
    ByteArrayOutputStream bytes = new ByteArrayOutputStream();
    PrintWriter writer = new PrintWriter(bytes,true);
    e.printStackTrace(writer);
    return bytes.toString();
  }

  /**
   * Takes an error and returns a String of the stack trace, for finding
   * where an error has occured (useful for debugging).
   */
  public static String getStackTraceAsString (Error er) {
    ByteArrayOutputStream bytes = new ByteArrayOutputStream();
    PrintWriter writer = new PrintWriter(bytes,true);
    er.printStackTrace(writer);
    return bytes.toString();
  }

  /**
   * Prints stack trace to err output stream, given an "Exception" object
   * as a parameter.
   */
  public static void printStackTraceToErr (Exception e) {
    System.err.println(getStackTraceAsString(e));
  }

  /**
   * Prints stack trace to err output stream, given an "Error" object as
   * a parameter.
   */
  public static void printErrorStackTraceToErr (Error er) {
    System.err.println(getStackTraceAsString(er));
  }
}

