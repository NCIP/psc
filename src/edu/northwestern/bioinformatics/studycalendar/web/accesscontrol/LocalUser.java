package edu.northwestern.bioinformatics.studycalendar.web.accesscontrol;

public class LocalUser { 
	  private static final ThreadLocal<String> threadLocal = new ThreadLocal<String>(); 

	  public static String getInstance() { 
	    String userName = (String) threadLocal.get(); 
	    return userName;
	  } 

	  public static void init(String userName) { 
	    threadLocal.set(userName); 
	  } 

	  public static void release() { 
	    threadLocal.set(null); 
	  } 

} 
