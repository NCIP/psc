package edu.northwestern.bioinformatics.studycalendar.utils.accesscontrol;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.ModelAndViewDefiningException;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

/**
 * @author Padmaja Vedula
 */

public class LoginCheckInterceptor extends HandlerInterceptorAdapter {
	private static Log log = LogFactory.getLog(SecureOperation.class);

	public boolean preHandle(HttpServletRequest request,
							HttpServletResponse response,
							Object handler) throws Exception {
		String userName = (String) LocalUser.getInstance();
		if (userName == null) {
			if (log.isDebugEnabled()) log.debug("request url " + request.getRequestURI() + "username in interceptor " + userName);
			ModelAndView mv = new ModelAndView("login");
			throw new ModelAndViewDefiningException(mv);
		}
		return true;
	}
}
