package edu.northwestern.bioinformatics.studycalendar.web;

import edu.northwestern.bioinformatics.studycalendar.core.accesscontrol.ApplicationSecurityManager;
import gov.nih.nci.cabig.ctms.web.filters.ContextRetainingFilterAdapter;
import org.slf4j.MDC;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import java.io.IOException;

public class LogInfoFilter extends ContextRetainingFilterAdapter {
    private static final String USER_NAME_KEY = "userName";
    private static final String USER_IP_ADDRESS_KEY = "userIpAddress";

    @Override
    public void doFilter(
        final ServletRequest req, final ServletResponse resp, final FilterChain chain
    ) throws IOException, ServletException {
        String userName = getApplicationSecurityManager().getUserName();

        MDC.put(USER_NAME_KEY, userName);
        MDC.put(USER_IP_ADDRESS_KEY, req.getRemoteAddr());

        chain.doFilter(req, resp);

        MDC.clear();
    }

    private ApplicationSecurityManager getApplicationSecurityManager() {
        return (ApplicationSecurityManager) getApplicationContext().getBean("applicationSecurityManager");
    }
    
}
