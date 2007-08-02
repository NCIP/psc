package edu.northwestern.bioinformatics.studycalendar.web;

import edu.nwu.bioinformatics.commons.DataAuditInfo;

import edu.northwestern.bioinformatics.studycalendar.testing.StudyCalendarTestCase;
import edu.northwestern.bioinformatics.studycalendar.utils.accesscontrol.ApplicationSecurityManager;

import javax.servlet.FilterChain;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.ServletException;
import java.io.IOException;
import java.util.Date;

/**
 * @author Rhett Sutphin
 */
public class AuditInfoFilterTest extends WebTestCase {
    private static final String REMOTE_ADDR = "123.45.67.8";
    private static final String USERNAME = "jimbo";

    private AuditInfoFilter filter;

    protected void setUp() throws Exception {
        super.setUp();
        filter = new AuditInfoFilter();
        request.setRemoteAddr(REMOTE_ADDR);
        ApplicationSecurityManager.setUser(request, USERNAME);
    }

    protected void tearDown() throws Exception {
        super.tearDown();
        DataAuditInfo.setLocal(null);
    }

    public void testAuditInfoSetForChainHandling() throws Exception {
        filter.doFilter(request, response, new FilterChain() {
            public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse) throws IOException, ServletException {
                DataAuditInfo actualLocal = DataAuditInfo.getLocal();
                assertNotNull(actualLocal);
                assertEquals(USERNAME, actualLocal.getBy());
                assertEquals(REMOTE_ADDR, actualLocal.getIp());
                assertDatesClose(actualLocal.getOn(), new Date(), 100);
            }
        });
    }

    public void testAuditInfoNotSetIfNotLoggedIn() throws Exception {
        ApplicationSecurityManager.removeUserSession(request);

        filter.doFilter(request, response, new FilterChain() {
            public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse) throws IOException, ServletException {
                assertNull(DataAuditInfo.getLocal());
            }
        });
    }
    
    public void testAuditInfoClearAfterExecution() throws Exception {
        FilterChain filterChain = registerMockFor(FilterChain.class);
        filterChain.doFilter(request, response);
        replayMocks();

        filter.doFilter(request, response, filterChain);
        verifyMocks();

        assertNull(DataAuditInfo.getLocal());
    }
}
