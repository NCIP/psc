package edu.northwestern.bioinformatics.studycalendar.security.internal;

import org.acegisecurity.context.SecurityContextHolder;
import java.io.IOException;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;                                
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
/**
 * attach authentication to the request.
 * @author Jalpa Patel
 */
public class RequestAuthenticationSocket implements Filter  {

     public void init(FilterConfig filterConfig) throws ServletException {}

     public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain)
         throws IOException, ServletException {
         servletRequest.setAttribute("authentication",  SecurityContextHolder.getContext().getAuthentication());

         filterChain.doFilter(servletRequest, servletResponse);
     }
     public void destroy() {}
}
