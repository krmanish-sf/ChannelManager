package salesmachine.util;

import java.io.BufferedReader;
import java.io.EOFException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.URL;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import HTTPClient.Codecs;
import HTTPClient.HTTPConnection;
import HTTPClient.HTTPResponse;
import HTTPClient.ModuleException;
import HTTPClient.NVPair;

import com.stevesoft.pat.Regex;

public class FormObject {
  public class JspWriter {

	public void println(int i) {
	}

	public void flush() {
	}

	public void println(String line) {
	}	  
  }
  
  public NVPair [] form_data;
  public String codeBase, file, desc, match, conType, error, storeFile,fileName, fromIP;
  public String from_url, accept, accept_language, userAgent;
  public String [] regMatches;
  public boolean print, store, printResponse, okay, getPage, printBytes, printPostingError = true;
  private boolean handleRedirects = false;
  private boolean zipFlag=false;
  public PrintWriter out;
  public JspWriter jspOut;
  public String page, context;
  public InputStream pageStream;
  public int port = -1;
  public int timeOut;
  public String Location;
  public StringBuffer pageBuffer;
  public URL url;
  public String effectiveURI;
  public HTTPResponse rsp;
  private int maxRedirectCount = 10, currentRedirectCount = 1;
	public boolean useFastWay;
  private boolean addAuthorization = false;
  public String authRealm, authUsername, authPassword;
  private boolean saveFile = false;
  public FileOutputStream fileOut;
  private boolean getResponseStream = false;
  private boolean addExtraHeaders = false;
  private NVPair[] extraHeaders;

  private HTTPConnection con;
  private BufferedReader in;
  private String m_Charset;

  public FormObject(String codeBase_, String file_, NVPair form_data_[], String desc_,String match_,
                      String[] regMatches_, boolean print_, boolean store_,
                      boolean printResponse_, PrintWriter out_, String context_) {
      codeBase = codeBase_; file = file_; form_data = form_data_; desc = desc_;
      match = match_; conType = "http"; regMatches = regMatches_; print = print_;
      store = store_; printResponse = printResponse_; out = out_;
      error = ""; getPage = false; context = context_; from_url = "";m_Charset="";
  }
  public FormObject(String codeBase_, String file_, NVPair form_data_[], String desc_,String match_,
                      String[] regMatches_, boolean print_, boolean store_,
                      boolean printResponse_, JspWriter out_, String context_) {
      codeBase = codeBase_; file = file_; form_data = form_data_; desc = desc_;
      match = match_; conType = "http"; regMatches = regMatches_; print = print_;
      store = store_; printResponse = printResponse_; jspOut = out_;
      error = ""; getPage = false; context = context_; from_url = "";m_Charset="";
  }
  public FormObject(String codeBase_, String file_, String desc_,String match_,
                      String[] regMatches_, boolean print_, boolean store_,
                      boolean printResponse_, PrintWriter out_, String context_) {
      codeBase = codeBase_; file = file_; form_data = null; desc = desc_;
      match = match_; conType = "http"; regMatches = regMatches_; print = print_;
      store = store_; printResponse = printResponse_; out = out_;
      error = ""; getPage = false; context = context_; from_url = "";m_Charset="";
  }
  public FormObject(String codeBase_, String file_, String desc_,String match_,
                      String[] regMatches_, boolean print_, boolean store_,
                      boolean printResponse_, JspWriter out_, String context_) {
      codeBase = codeBase_; file = file_; form_data = null; desc = desc_;
      match = match_; conType = "http"; regMatches = regMatches_; print = print_;
      store = store_; printResponse = printResponse_; jspOut = out_;
      error = ""; getPage = false; context = context_; from_url = "";m_Charset="";
  }
  public FormObject(String codeBase_, int port_, String file_, String desc_,String match_,
                      boolean print_, boolean store_,
                      boolean printResponse_, JspWriter out_, String context_) {
      codeBase = codeBase_; file = file_; form_data = null; desc = desc_;
      match = match_; conType = "http"; print = print_;
      store = store_; printResponse = printResponse_; jspOut = out_;
      error = ""; getPage = false; context = context_; from_url = ""; port = port_;m_Charset="";
  }
  public FormObject(String codeBase_, int port_, String file_, String desc_,String match_,
          boolean print_, boolean store_,
          boolean printResponse_) {
	codeBase = codeBase_; file = file_; form_data = null; desc = desc_;
	match = match_; conType = "http"; print = print_;
	store = store_; printResponse = printResponse_; jspOut = null;
	error = ""; getPage = false; context = ""; from_url = ""; port = port_;m_Charset="";
	}  
  public FormObject(String codeBase_, String file_, NVPair form_data_[], String desc_,String match_,
                      boolean print_, boolean store_,
                      boolean printResponse_, PrintWriter out_, String context_) {
      codeBase = codeBase_; file = file_; form_data = form_data_; desc = desc_;
      match = match_; conType = "http"; regMatches = null; print = print_;
      store = store_; printResponse = printResponse_; out = out_;
      error = ""; getPage = false; context = context_; from_url = "";m_Charset="";
  }
  public FormObject(String codeBase_, String file_, NVPair form_data_[], String desc_,String match_,
                      boolean print_, boolean store_,
                      boolean printResponse_, JspWriter out_, String context_) {
      codeBase = codeBase_; file = file_; form_data = form_data_; desc = desc_;
      match = match_; conType = "http"; regMatches = null; print = print_;
      store = store_; printResponse = printResponse_; jspOut = out_;
      error = ""; getPage = false; context = context_; from_url = "";m_Charset="";
  }
  public FormObject(String codeBase_, String file_, String desc_,String match_,
                      boolean print_, boolean store_,
                      boolean printResponse_, PrintWriter out_, String context_) {
	  codeBase = codeBase_; file = file_; form_data = null; desc = desc_;
      match = match_; conType = "http"; regMatches = null; print = print_;
      store = store_; printResponse = printResponse_; out = out_;
      error = ""; getPage = false; context = context_; from_url = "";m_Charset="";
   }
  public FormObject(String codeBase_, String file_, String desc_,String match_,
                      boolean print_, boolean store_,
                      boolean printResponse_, JspWriter out_, String context_) {
      codeBase = codeBase_; file = file_; form_data = null; desc = desc_;
      match = match_; conType = "http"; regMatches = null; print = print_;
      store = store_; printResponse = printResponse_; jspOut = out_;
      error = ""; getPage = false; context = context_; from_url = "";m_Charset="";
  }
  public FormObject(PrintWriter out_, String context_) {
      codeBase = null; file = null; form_data = null; desc = null;
      match = null; conType = "http"; regMatches = null; print = false;
      store = true; printResponse = false; out = out_;
      error = ""; getPage = false; context = context_; from_url = "";m_Charset="";
  }
  public FormObject(JspWriter out_, String context_) {
      codeBase = null; file = null; form_data = null; desc = null;
      match = null; conType = "http"; regMatches = null; print = false;
      store = true; printResponse = false; jspOut = out_;
      error = ""; getPage = false; context = context_; from_url = "";m_Charset="";
  }
  public void setContentType(String contType) {
	  m_Charset=contType;
  }
  public void addData (String[] pairs) {
    form_data = new NVPair[pairs.length/2];
    for (int i=0; i<pairs.length; i+=2) {
      form_data[i/2] = new NVPair(pairs[i],pairs[i+1]);
    }
    return;
  }
  public void addData (Hashtable inputs) {
    try {
      String [] data = new String [inputs.size()*2];
      Enumeration enu = inputs.keys();
      int i = 0;
      while (enu.hasMoreElements()) {
        String key = (String)enu.nextElement();
        data[i++] = key;
        data[i++] = (String) inputs.get(key);
      }
      this.addData(data);
    } catch (Exception e) {
      ExcHandle.printStackTraceToErr(e);
    }
  }
  public void setFromHeader (String from_url_) {
    from_url = from_url_;
    return;
  }

  public void setCurrentRedirectCount (int currentRedirectCount_) {
    currentRedirectCount = currentRedirectCount_;
    return;
  }

  public void https() {
    conType = "https";
    return;
  }

  public void setTimeOut(int time) {
    this.timeOut = time;
  }

  private void printThis(String line) {
    if (jspOut != null) {
      try {
        jspOut.println(line);
        jspOut.flush();
      } catch (Exception e) {
        System.err.println(e);
      }
    }
    if (out != null) {
      out.println(line);
      out.flush();
    }
  }

  private void printThis (int i) {
    if (jspOut != null) {
      try {
        jspOut.println(i);
        jspOut.flush();
      } catch (Exception e) {
        System.err.println(ExcHandle.getStackTraceAsString(e));
      }
    }
  }

  public Hashtable hitForm (String reqType, String uploadFile, String fileName) {
    this.fileName = fileName;
    return hitForm(reqType,uploadFile);
  }

  public Hashtable hitForm (String reqType, String uploadFile) {

    error = "";
    boolean check = false;
    page = "";
    String status;
    String inputLine;
    rsp = null;
    Hashtable matches = new Hashtable();
    storeFile = null;
    int matchesInd = 0;
    int i = 0; int j = 0; int length = 0;

    FormObjectTimeout fot = null;
    con = null;
    in = null;
    pageBuffer = null;
    try {
      if (print) {
        printThis("<font color='blue'>Attempting " + desc + "</font><br>\n");
      }
      if (store) error += "Attempting " + desc + "\n";
      try {
        try {
          // Now you can specify port # directly in the codeBase

          if (this.url != null) {
            con = new HTTPConnection(this.url);
          } else {
            if (port != -1) {
           	  con = new HTTPConnection(conType,codeBase,port);
            } else {
           	  con = new HTTPConnection(conType,codeBase,-1);
            }
          }
        } catch (Exception e) {
          System.err.println(ExcHandle.getStackTraceAsString(e));
        }
        if (context != null) {
          con.setContext(context);
        }
				if (this.userAgent == null || "".equals(this.userAgent)) {
					this.userAgent = "Mozilla/4.0 (compatible; MSIE 5.01; Windows NT 5.0)";
				}

        String authString = "Basic";
        if (addAuthorization) {
          authString = "Basic "+Base64.encodeString(authUsername+":"+authPassword);
          con.addBasicAuthorization(authRealm, authUsername, authPassword);
        }


        NVPair[] defaultHeaders = new NVPair[] { new NVPair("User-Agent", this.userAgent),
                                                 new NVPair("Authorization", authString) };
        if (from_url.length() > 0) {
          defaultHeaders = new NVPair[] { new NVPair("User-Agent", this.userAgent),
                                          new NVPair("Referer", from_url),
                                          new NVPair("Authorization", authString),
                                          new NVPair("Accept", "image/gif, image/x-xbitmap, image/jpeg, image/pjpeg, */*"),
                                          new NVPair("Accept_Language", "en-us")};
        }

        if (this.fromIP != null && !"".equals(this.fromIP)) {
          defaultHeaders = new NVPair[]  { new NVPair("User-Agent", this.userAgent),
                                           new NVPair("Authorization", authString),
                                           new NVPair("REMOTE_ADDR", this.fromIP)
                                           };
        }

       // ADD ANY NECESSARY EXTRA HEADERS
        if (addExtraHeaders) {
          int defaultHeadersLength = java.lang.reflect.Array.getLength(defaultHeaders);
          int extraHeadersLength = java.lang.reflect.Array.getLength(this.extraHeaders);

          NVPair[] tempHeaders = defaultHeaders;
          defaultHeaders = new NVPair[defaultHeadersLength+extraHeadersLength];
          for (int z=0; z<java.lang.reflect.Array.getLength(defaultHeaders); z++) {
            if (z < defaultHeadersLength) {
              defaultHeaders[z] = tempHeaders[z];
            } else {
              defaultHeaders[z] = this.extraHeaders[z-defaultHeadersLength];
            }
          }
        }

        con.setDefaultHeaders(defaultHeaders);





        // Old setup for setting timeouts...
///*
        if (this.timeOut > 0) {
          con.setTimeout(this.timeOut);
        }
//*/

        // We start the timeout thread.
        /*
        if (this.timeOut > 0) {
          fot = new FormObjectTimeout(con, rsp, this.timeOut);
          fot.start();
        }
        */

       	if ("Get".equals(reqType)) rsp = con.Get(file);

       	if ("Post".equals(reqType)) rsp = con.Post(file, form_data);

       	

       	if ("PostFile".equals(reqType)) {
          NVPair[] opts = form_data;
          if (fileName == null) {
            fileName = "filename";
          }
          if (uploadFile != null) {
						NVPair[] thefile = { new NVPair(fileName, uploadFile) };
						NVPair[] hdrs = new NVPair[1];
						byte[]   data = Codecs.mpFormDataEncode(opts, thefile, hdrs);
						rsp = con.Post(file, data, hdrs);

					} else {
						NVPair[] hdrs = new NVPair[1];
						byte[]   data = Codecs.mpFormDataEncode(opts, null,hdrs);

					}
        }

      } catch (Exception e) {
        System.out.println(e);
        ExcHandle.printStackTraceToErr(e);
      }

      int statusCode = 0;
      boolean go = true;
      if (printBytes) {
        try {
          byte [] bytes = rsp.getData();
          for (int in = 0; in < java.lang.reflect.Array.getLength(bytes); in++) {
            System.out.print((char) bytes[in]);
          }
        } catch (Exception ex) {
          ExcHandle.printStackTraceToErr(ex);
        }
      }
      try {
        this.Location = rsp.getHeader("Location");

        statusCode = rsp.getStatusCode();
      } catch (Exception e) {
        if (printPostingError) {
          //System.out.println ("RSP = " + rsp.toString());
          //this.Location = rsp.getHeader("Location");
          System.out.println(e);
          go = false;
        }
      }

      if (statusCode == 400 && rsp.toString().indexOf("Effective-URI") != -1) {
        String effective = "";
        Regex uri = Regex.perlCode("/Effective-URI: (.+?)\\n/gi");
        if (uri.search(rsp.toString())) {
          effective = uri.stringMatched(1).substring(0, uri.stringMatched(1).length()-1);
        }
        if (effective.length() > 0) {
          this.Location = effective;
        }
      }

      if (handleRedirects && statusCode == 302 && currentRedirectCount <= maxRedirectCount && Location != null && !"".equals(Location)) {  // handle redirect!
//System.out.println("\n"+"!!! REDIRECTING TO:\n"+Location);

         boolean hasCodeBase = false;
         if (Location.startsWith("http")) {
            hasCodeBase = true;
         }

         boolean isSecure = false;
         if (Location.startsWith("https:")) {
            isSecure = true;
         }

         String codeBase_="", file_="";
         if (!hasCodeBase) {
            if ("https".equals(conType)) {
               isSecure = true;
            }
            codeBase_ = this.codeBase;
            file_ = Location;
         } else {
            Regex locationMatch = Regex.perlCode("/http:\\/\\/(.+?)(\\/.+)/gi");
            if (isSecure) {
               locationMatch = Regex.perlCode("/https:\\/\\/(.+?)(\\/.+)/gi");
            }

            if (locationMatch.search(Location)) {
               codeBase_ = locationMatch.stringMatched(1);
               file_ = locationMatch.stringMatched(2);
            }
         }

//System.out.println("codeBase_ = \""+codeBase_+"\"");
//System.out.println("file_ = \""+file_+"\"");
//System.out.println("currentRedirectCount = "+currentRedirectCount);

         FormObject formObj = new FormObject(codeBase_,file_,"inside FormObject redirecting...","", regMatches, print, store, printResponse, out, context);
         if (isSecure) {
            formObj.https();
         }
         formObj.handleRedirects();
         formObj.setCurrentRedirectCount((currentRedirectCount+1));
         matches = formObj.hitForm("Get",null);

         this.rsp = formObj.rsp;
         this.Location = formObj.Location;
         statusCode = formObj.rsp.getStatusCode();
         this.page = formObj.page;
         this.pageBuffer = new StringBuffer(formObj.page);
         this.okay = formObj.okay;

         if (store) { error += formObj.error; }

         return matches;
      }

//System.out.println("RSP:\n"+rsp.toString());

      if (rsp != null && rsp.getEffectiveURI() != null) {
        this.effectiveURI = rsp.getEffectiveURI().toString();
      }
    	if (statusCode >= 300 && statusCode > 0 && go) {
      	in = new BufferedReader(new InputStreamReader(rsp.getInputStream()));
        String errorPage = "";
  	    while ((inputLine = in.readLine()) != null)  {
          errorPage += inputLine + "\n";
        }
        System.out.println(rsp.toString());
        System.out.println("*Location = " +Location);
  	    System.err.println("Status Code: "+ statusCode + " "+rsp.getReasonLine() + "\n" + errorPage);
	      //System.err.println(new String(rsp.getData()));
        //if (print) printThis("<font color='blue'>" + new String(rsp.getData()) + "</font><br>\n");
        //if (store) error += new String(rsp.getData()) + "\n";
        okay = false;
        return matches;
    	} else {
        if (!printPostingError) {
          return matches;
        }

       // save file
        if (saveFile) {
         // check if ZIP file
        	System.out.println("found file to downloD");
          if ((file.toLowerCase().endsWith(".zip"))||zipFlag) {
System.out.println("found ZIP file!");
            ZipInputStream zipped_in = new ZipInputStream(rsp.getInputStream());
            ZipOutputStream zipped_out = new ZipOutputStream(fileOut);
            zipped_out.setMethod(ZipOutputStream.DEFLATED);
            byte [] b = new byte [1000];
            int len = -1;
            while (true) {
              // Get the next zip entry.  Break out of the loop if there are no more.
              ZipEntry zipentryIN = zipped_in.getNextEntry();
              if (zipentryIN == null) break;
              ZipEntry zipentry = new ZipEntry (zipentryIN.getName()); // this is a fix
System.out.println("\n"+"New ZipEntry!");
System.out.println("name: "+zipentry.getName());
              zipentry.setTime(zipentryIN.getTime());
System.out.println("time: "+ (new java.util.Date(zipentry.getTime())).toLocaleString() );
              zipped_out.putNextEntry(zipentry);

              b = new byte [1024];
              len = -1;
              while ((len = zipped_in.read(b)) > -1) {
              // write the data to a file.
                zipped_out.write(b, 0, len);
              }

              zipped_in.closeEntry();
              zipped_out.closeEntry();
            }
            
            zipped_in.close();
            zipped_out.close();
          } else {
            int tempchar = -1;
            if (m_Charset.length() > 0) {
            	in = new BufferedReader(new InputStreamReader(rsp.getInputStream(),m_Charset));
            } else {
            	in = new BufferedReader(new InputStreamReader(rsp.getInputStream()));
            }
            while ( (tempchar=in.read()) != -1) {
              fileOut.write( tempchar );
              //System.out.print('.');
            }
            in.close();
          }
          fileOut.close();
        }

				Regex signedout = Regex.perlCode("/" + match + "/");
				boolean returnEarly = false;
				if (regMatches == null) { length = 0; if (!getPage) {returnEarly = true;}} else { length = regMatches.length; }
				storeFile = "";
				boolean keepGoing = true;

       // only get as stream
        if (getResponseStream) {
          this.pageStream = rsp.getInputStream();
          keepGoing = false;
        }

        this.pageBuffer = new StringBuffer(10000);
        in = new BufferedReader(new InputStreamReader(rsp.getInputStream()));

				int lineNum = 0;
				while (keepGoing && (inputLine = in.readLine()) != null)  {
					lineNum++;
					pageBuffer.append(inputLine + "\n");
//					System.out.println(inputLine);
					if (printResponse) {
						if (printPostingError) {
							printThis(inputLine);
						}
					}
					if (signedout.search(inputLine)) {
						check = true;
						if (returnEarly && !match.equals("")) {
							keepGoing = false;
						}
					}

					if (uploadFile instanceof String) {
						if (uploadFile.equals("store")) {
							storeFile += inputLine;
						}
					}
					for (i=0; i<length;i+=2) {
						Filter filt = new Filter("");
						Regex temp = Regex.perlCode(regMatches[i]);
						if (temp.search(inputLine)) {
							//System.err.println(regMatches[i] + " here");
							int absInd = Math.abs(new Integer(regMatches[i+1]).intValue());
							for (j=1;j<=absInd;j++) {
								if (temp.stringMatched(j) instanceof String) {
									matches.put(new Integer(i/2+1).toString()+":"+new Integer(j).toString(),temp.stringMatched(j));
									if (new Integer(regMatches[i+1]).intValue() < 0) {
										if (!getPage) {
											keepGoing = false;
										}
									}
								}
							}
						}
					}
				}
				if (check) {
					if (print) {printThis("<font color='blue'>" + desc + " Successful!</font><br>\n");}
					if (store) error += desc + " Successful!\n";
				} else {
					if (print) {printThis("<font color='blue'>" + desc + " Unsuccessful! Reason:</font><br>\n" + pageBuffer.toString());}
					if (store) error += desc + " Unsuccessful! Reason:\n" + pageBuffer.toString();
					okay = false;
					return matches;
				}
    	}
    } catch (EOFException eofe) {
      String err = "EOFException: " + eofe.toString();
//      System.err.println(ExcHandle.getStackTraceAsString(eofe));
      if (print) printThis("<font color='blue'>" + err + "</font><br>\n");
      if (store) error += err + "\n";
      okay = false;
      return matches;
    } catch (IOException ioe) {
      String err = "IOexception: " + ioe.toString();
      System.err.println(ExcHandle.getStackTraceAsString(ioe));
      if (print) printThis("<font color='blue'>" + err + "</font><br>\n");
      if (store) error += err + "\n";
      okay = false;
      return matches;
    } catch (ModuleException me) {
      String err = "ModuleException: " + me.getMessage();
      System.err.println(err);
      if (print) printThis("<font color='blue'>" + err + "</font><br>\n");
      if (store) error += err + "\n";
      okay = false;
      return matches;
    } catch (Exception e) {
      ExcHandle.printStackTraceToErr(e);
      if (print) printThis("<font color='blue'>" + ExcHandle.getStackTraceAsString(e) + "</font><br>\n");
      if (store) error += ExcHandle.getStackTraceAsString(e) + "\n";
      okay = false;
      return matches;
    } catch (Error e) {
      System.err.println("ERROR! == " + e.toString());
      ExcHandle.printErrorStackTraceToErr(e);

    } finally {
      if (!getResponseStream) {
        try {
          con.stop();
          in.close();
        } catch (Exception e) {
          System.out.println(ExcHandle.getStackTraceAsString(e));
        }
      }
      if (pageBuffer != null)
        page = pageBuffer.toString();

      // We make sure we stop the timeout thread
      /*
      if (fot != null) {
        fot.stopTimeout();
      }
      */
    }
    okay = true;

    return matches;
  }

  public void getPage() {
    getPage = true;
  }

  public void handleRedirects() {
    handleRedirects = true;
  }

  public void addAuthorization(String realm, String username, String password) {
    addAuthorization = true;
    authRealm = realm;
    authUsername = username;
    authPassword = password;
  }

  public void saveReceivedFile(String path) {
    saveFile = true;
    try {
      fileOut = new FileOutputStream(path);
    } catch (Exception e) {
      saveFile = false;
      System.out.println(ExcHandle.getStackTraceAsString(e));
    }
  }

  public void setZipFlag() {
	  zipFlag = true;
	  }
  public void returnResponseStream() {
    getResponseStream = true;
  }

  public void closeStreams() {
    try {
      if (con != null) con.stop();
      if (in != null) in.close();
    } catch (Exception e) {
      System.out.println(ExcHandle.getStackTraceAsString(e));
    }
  }

  public void addExtraHeaders(NVPair[] extraHeadersArray) {
    addExtraHeaders = true;
    this.extraHeaders = extraHeadersArray;
  }


}



