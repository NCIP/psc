package edu.northwestern.bioinformatics.studycalendar.web;

import edu.northwestern.bioinformatics.studycalendar.core.accesscontrol.SecurityContextHolderTestHelper;
import gov.nih.nci.cabig.ctms.audit.domain.DataAuditInfo;
import static org.easymock.EasyMock.expect;

import javax.servlet.FilterChain;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import java.util.Date;

/**
 * @author Rhett Sutphin
 */
public class AuditInfoFilterTest extends ContextRetainingFilterTestCase {
    private static final String REMOTE_ADDR = "123.45.67.8";
    private static final String USERNAME = "jimbo";
    private static final String PASSWORD = "password";
    private static final String URL = "/personal/jimbo";

    private AuditInfoFilter filter;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        filter = new AuditInfoFilter();
        initFilter(filter);
        request.setRemoteAddr(REMOTE_ADDR);
        request.setRequestURI(URL);
        SecurityContextHolderTestHelper.setSecurityContext(USERNAME, PASSWORD);
        expect(mockApplicationContext.getBean("applicationSecurityManager")).
            andStubReturn(applicationSecurityManager);
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        DataAuditInfo.setLocal(null);
    }

    public void testAuditInfoSetForChainHandling() throws Exception {
        filter.doFilter(request, response, new FilterChain() {
            public void doFilter(final ServletRequest servletRequest, final ServletResponse servletResponse) {
                gov.nih.nci.cabig.ctms.audit.DataAuditInfo actualLocal 
                    = gov.nih.nci.cabig.ctms.audit.DataAuditInfo.getLocal();
                assertTrue("Local is not of the PSC subclass",
                    actualLocal instanceof gov.nih.nci.cabig.ctms.audit.domain.DataAuditInfo);
                assertNotNull(actualLocal);
                assertEquals(USERNAME, actualLocal.getBy());
                assertEquals(REMOTE_ADDR, actualLocal.getIp());
                assertDatesClose(actualLocal.getOn(), new Date(), 100);
                assertEquals(URL, ((gov.nih.nci.cabig.ctms.audit.domain.DataAuditInfo) actualLocal).getUrl());
            }
        });
    }

    public void testAuditInfoNotSetIfNotLoggedIn() throws Exception {
        applicationSecurityManager.removeUserSession();

        filter.doFilter(request, response, new FilterChain() {
            public void doFilter(final ServletRequest servletRequest, final ServletResponse servletResponse) {
                assertNotNull(DataAuditInfo.getLocal());
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
