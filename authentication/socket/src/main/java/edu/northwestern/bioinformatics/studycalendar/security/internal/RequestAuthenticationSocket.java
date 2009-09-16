package edu.northwestern.bioinformatics.studycalendar.security.internal;

import org.acegisecurity.context.SecurityContextHolder;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import java.io.IOException;
/**
 * attach authentication to the request.
 * @author Jalpa Patel
 */
// TODO: is this filter necessary?  Why not just use ApplicationSecurityManager?
public class RequestAuthenticationSocket implements Filter  {

     public void init(FilterConfig filterConfig) throws ServletException {}

     public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain)
         throws IOException, ServletException {
         servletRequest.setAttribute("authentication",  SecurityContextHolder.getContext().getAuthentication());

         filterChain.doFilter(servletRequest, servletResponse);
     }
     public void destroy() {}
}
