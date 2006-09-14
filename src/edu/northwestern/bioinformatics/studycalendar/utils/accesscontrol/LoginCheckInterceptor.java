package edu.northwestern.bioinformatics.studycalendar.utils.accesscontrol;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.ModelAndViewDefiningException;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;
//import org.springframework.web.servlet.view.RedirectView;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.northwestern.bioinformatics.studycalendar.web.LoginCommand;

/**
 * @author Padmaja Vedula
 */;

public class LoginCheckInterceptor extends HandlerInterceptorAdapter {
	private static Log log = LogFactory.getLog(LoginCheckInterceptor.class);
    private ApplicationSecurityManager applicationSecurityManager
             = new ApplicationSecurityManager();
    
	public boolean preHandle(HttpServletRequest request,
							HttpServletResponse response,
							Object handler) throws Exception {
		String userName = applicationSecurityManager.getUser(request);
		if (userName == null) {
			if (log.isDebugEnabled()) {
				log.debug("request path " + request.getPathInfo() + " request url " + request.getRequestURI() + " username in interceptor " + userName);
				log.debug("session id: " + request.getSession().getId());
			}
			
			ModelAndView mv = new ModelAndView("login", "command", new LoginCommand());
			throw new ModelAndViewDefiningException(mv);
			
		}
		return true;
	}
}
