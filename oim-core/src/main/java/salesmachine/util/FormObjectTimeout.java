package salesmachine.util;

import HTTPClient.HTTPConnection;
import HTTPClient.HTTPResponse;

public class FormObjectTimeout extends Thread {

  public HTTPConnection con;
  public HTTPResponse rsp;
  public long timeout;
  public boolean done;

  public FormObjectTimeout(HTTPConnection con_, HTTPResponse rsp_, long timeout_) {
    con = con_;
    rsp = rsp_;
    timeout = timeout_;
    done = false;
  }

  public synchronized void stopTimeout() {
    done = true;
    notify();
  }

  public synchronized void run() {
    try {
      long start_tm = System.currentTimeMillis();
      while (!done && (System.currentTimeMillis() - start_tm < timeout)) {
        wait(System.currentTimeMillis() - start_tm + timeout);
      }
      long end_tm = System.currentTimeMillis();
      if (!done && con != null) {
        // We reached the timeout and the con is still connected. So we stop it.
        con.stop();
        con = null;
        if (rsp != null) {
          rsp.getInputStream().close();
        } else {
          //System.out.println("##########################   The response is null!!! ");
        }
        rsp = null;
        //System.out.println("Timeout reached... " + (end_tm - start_tm));
      }
      //System.out.println("Connection time: " + (end_tm - start_tm));
    } catch (Exception e) {
      System.out.println("## EXCEPTION OK - Connection timed out!!!");
      ExcHandle.printStackTraceToErr(e);
      System.out.println("## EXCEPTION OK - Connection timed out!!!");
    }
  }
}
