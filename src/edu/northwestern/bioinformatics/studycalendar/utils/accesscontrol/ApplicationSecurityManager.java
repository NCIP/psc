package edu.northwestern.bioinformatics.studycalendar.utils.accesscontrol;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

/**
 * @author Padmaja Vedula
 */

public class ApplicationSecurityManager {

	public static final String USER = "user";
	
	public void setUser(HttpServletRequest request, String user) {
		request.getSession(true).setAttribute(USER, user);
	}
	
	public String getUser(HttpServletRequest request) {
		 String user = "";
		 HttpSession session = request.getSession(false);
		 if (session != null) {
			 user = (String) session.getAttribute(USER);
		 }
		 return user;
	}
	
	public void removeUserSession(HttpServletRequest request) {
		if (request.getSession(false) != null)
			request.getSession().removeAttribute(USER);
	}
	
}
