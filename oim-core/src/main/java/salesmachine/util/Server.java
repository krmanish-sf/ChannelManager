package salesmachine.util;

import java.io.File;

public class Server {

  public Server() {
  }
  public static boolean isLocalhost() {
    if (System.getProperty("java.class.path").startsWith("C:")) {
      return true;
    }
    return false;
  }
  public static boolean isEntity() {
    File f = new File("/home/httpd/vhosts/kindbuy.com/private");
    if (f.exists()) {
      return true;
    }
    return false;
  }
  public static boolean isKB() {
    File f = new File("/home/kdyer/kbserver.txt");
    if (f.exists()) {
      return true;
    }
    return false;
  }
  public static boolean isAIT() {
    File f = new File("/home/kdyer/aitserver.txt");
    if (f.exists()) {
      return true;
    }
    return false;
  }
  public static boolean isServer4() {
    File f = new File("/home/kdyer/server4.txt");
    if (f.exists()) {
      return true;
    }
    return false;
  }
  public static boolean isRackspaceServer() {
    File f = new File("/home/kdyer/rackspace.txt");
    if (f.exists()) {
      return true;
    }
    return false;
  }

  public static boolean isStaging() {
	  File f = new File(".");
	  String name = f.getAbsolutePath();
	  if (name.indexOf("staging.inventorysource.com") != -1) {
	    return true;
	   }else{
		return false;
	   }
  }
  public static boolean isWWW1() {
	  File f = new File(".");
	  String name = f.getAbsolutePath();
	  if (name.indexOf("inventorysource.com") != -1 && name.indexOf(".inventorysource.com") == -1) {
	    return true;
	   }else{
		return false;
	   }
  }
  
  public static boolean isApp1() {
	  File f = new File(".");
	  String name = f.getAbsolutePath();
	  if (name.indexOf("app1.inventorysource.com") != -1) {
	    return true;
	   }else{
		return false;
	   }
  }

  public static boolean isDB1() {
	  File f = new File(".");
	  String name = f.getAbsolutePath();
	  if (name.indexOf("db1.inventorysource.com") != -1) {
	    return true;
	   }else{
		return false;
	   }
  }
}

