/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.com/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.web;

import edu.northwestern.bioinformatics.studycalendar.core.accesscontrol.ApplicationSecurityManager;
import edu.northwestern.bioinformatics.studycalendar.core.accesscontrol.SecurityContextHolderTestHelper;
import edu.northwestern.bioinformatics.studycalendar.dao.UserActionDao;
import edu.northwestern.bioinformatics.studycalendar.domain.UserAction;
import edu.northwestern.bioinformatics.studycalendar.domain.auditing.AuditEvent;
import edu.northwestern.bioinformatics.studycalendar.security.authorization.AuthorizationObjectFactory;
import edu.northwestern.bioinformatics.studycalendar.security.authorization.PscUser;
import gov.nih.nci.security.authorization.domainobjects.User;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import java.io.IOException;

import static edu.northwestern.bioinformatics.studycalendar.security.authorization.AuthorizationObjectFactory.createPscUser;
import static org.easymock.EasyMock.expect;

/**
 * @author Jalpa Patel
 */
public class UserActionHeaderFilterTest extends ContextRetainingFilterTestCase {
    private UserActionDao userActionDao;
    private ApplicationSecurityManager applicationSecurityManager;
    final String GRID_ID = "GridId";
    private PscUser pscUser;
    private UserActionHeaderFilter filter;
    private UserAction userAction;

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        filter = new UserActionHeaderFilter();
        initFilter(filter);

        User user = AuthorizationObjectFactory.createCsmUser(12, "josephine");
        pscUser  = createPscUser(user);
        SecurityContextHolderTestHelper.setSecurityContext(pscUser);
        userAction = new UserAction();
        userAction.setGridId(GRID_ID);
        userAction.setUser(user);
    }

    public void testAuditUserActionSetForChainHandling() throws Exception {
        addPSCUserActionHeader();
        expectUserAction();
        expectCurrentUser();
        doFilter(new FilterChain() {
            public void doFilter(final ServletRequest servletRequest, final ServletResponse servletResponse) {
                UserAction actualUserAction
                        = AuditEvent.getUserAction();
                assertNotNull(actualUserAction);
            }
        });
    }

    public void testAuditUserActionClearAfterExecution() throws Exception {
        addPSCUserActionHeader();
        expectUserAction();
        expectCurrentUser();

        FilterChain filterChain = registerMockFor(FilterChain.class);
        filterChain.doFilter(request, response);

        doFilter(filterChain);
        assertNull(AuditEvent.getUserAction());
    }

    public void testAuditUserActionNotSetIfPSCHeaderIsNotIncluded() throws Exception {
        doFilter(new FilterChain() {
            public void doFilter(final ServletRequest servletRequest, final ServletResponse servletResponse) {
                UserAction actualUserAction
                        = AuditEvent.getUserAction();
                assertNull(actualUserAction);
            }
        });
    }

    public void testAuditUserActionNotSetIfNoUserActionFound() throws Exception {
        addPSCUserActionHeader();
        registerUserActionDao();
        expect(userActionDao.getByGridId(GRID_ID)).andReturn(null);
        doFilter(new FilterChain() {
            public void doFilter(final ServletRequest servletRequest, final ServletResponse servletResponse) {
                UserAction actualUserAction
                        = AuditEvent.getUserAction();
                assertNull(actualUserAction);
            }
        });
    }

    public void testAuditUserActionNotSetIfCurrentUserIsNotUserOfUserAction() throws Exception {
        String userName = "perry";
        SecurityContextHolderTestHelper.setSecurityContext(AuthorizationObjectFactory.createPscUser(userName));
        addPSCUserActionHeader();
        expectUserAction();
        registerApplicationSecurityManager();
        expect(applicationSecurityManager.getUserName()).andReturn(userName);
        doFilter(new FilterChain() {
            public void doFilter(final ServletRequest servletRequest, final ServletResponse servletResponse) {
                UserAction actualUserAction
                        = AuditEvent.getUserAction();
                assertNull(actualUserAction);
            }
        });
    }

    private void doFilter(FilterChain filterChain) throws IOException, ServletException {
        replayMocks();
        filter.doFilter(request, response, filterChain);
        verifyMocks();
    }

    // Helper methods
    private void expectUserAction() {
        registerUserActionDao();
        expect(userActionDao.getByGridId(GRID_ID)).andReturn(userAction);
    }

    private void expectCurrentUser() {
        registerApplicationSecurityManager();
        expect(applicationSecurityManager.getUserName()).andReturn(pscUser.getUsername());
    }

    private void addPSCUserActionHeader() {
        request.addHeader("X-PSC-User-Action","{PSC}/api/v1/user-actions/" + GRID_ID);
    }

    private void registerUserActionDao() {
        userActionDao = registerDaoMockFor(UserActionDao.class);
        expect(mockApplicationContext.getBean("userActionDao")).andReturn(userActionDao);
    }

    private void registerApplicationSecurityManager() {
        applicationSecurityManager = registerMockFor(ApplicationSecurityManager.class);
        expect(mockApplicationContext.getBean("applicationSecurityManager")).andReturn(applicationSecurityManager);
    }
}

