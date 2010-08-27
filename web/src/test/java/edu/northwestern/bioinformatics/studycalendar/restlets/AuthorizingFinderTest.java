package edu.northwestern.bioinformatics.studycalendar.restlets;

import edu.northwestern.bioinformatics.studycalendar.core.accesscontrol.AuthorizationScopeMappings;
import edu.northwestern.bioinformatics.studycalendar.domain.Fixtures;
import edu.northwestern.bioinformatics.studycalendar.security.authorization.PscRole;
import edu.northwestern.bioinformatics.studycalendar.security.authorization.PscUser;
import edu.northwestern.bioinformatics.studycalendar.web.accesscontrol.ResourceAuthorization;
import gov.nih.nci.cabig.ctms.suite.authorization.SuiteRole;
import gov.nih.nci.cabig.ctms.suite.authorization.SuiteRoleMembership;
import gov.nih.nci.security.authorization.domainobjects.User;
import org.acegisecurity.providers.TestingAuthenticationToken;
import org.easymock.classextension.EasyMock;
import org.restlet.Context;
import org.restlet.data.Method;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.resource.Representation;
import org.restlet.resource.Resource;
import org.restlet.resource.ResourceException;
import org.springframework.beans.factory.BeanFactory;

import java.util.HashMap;
import java.util.Map;

import static org.easymock.classextension.EasyMock.*;
import static org.restlet.data.Method.*;

/**
 * @author Rhett Sutphin
 */
public class AuthorizingFinderTest extends RestletTestCase {
    private static final String BEAN_NAME = "timber";
    private BeanFactory beanFactory;
    private AuthorizingFinder finder;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        beanFactory = registerMockFor(BeanFactory.class);
        finder = new AuthorizingFinder(beanFactory, BEAN_NAME);
    }

    public void testNonAuthorizingResourceAlwaysLetIn() throws Exception {
        request.setMethod(GET);

        Resource mockResource = registerMockFor(Resource.class);
        expect(beanFactory.getBean(BEAN_NAME)).andReturn(mockResource);
        expect(mockResource.allowGet()).andReturn(true);
        mockResource.init((Context) EasyMock.anyObject(), eq(request), eq(response));
        mockResource.handleGet();

        replayMocks();
        finder.handle(request, response);
        verifyMocks();

        assertResponseStatus(Status.SUCCESS_OK);
    }

    public void testRoleAuthorizedResourceLetInWhenAuthorized() throws Exception {
        setCurrentUser(PscRole.SYSTEM_ADMINISTRATOR, PscRole.USER_ADMINISTRATOR);
        expectStatusForMethod(POST, Status.SUCCESS_OK);
    }

    public void testRoleAuthorizedResourceBlockedWhenWrongRole() throws Exception {
        setCurrentUser(PscRole.BUSINESS_ADMINISTRATOR, PscRole.USER_ADMINISTRATOR);
        expectStatusForMethod(POST, Status.CLIENT_ERROR_FORBIDDEN);
    }

    public void testRoleAndScopeAuthorizedResourceLetInWhenAuthorized() throws Exception {
        setCurrentUser(PscRole.STUDY_QA_MANAGER).
            getMemberships().get(SuiteRole.STUDY_QA_MANAGER).forSites("a");
        expectStatusForMethod(DELETE, Status.SUCCESS_OK);
    }

    public void testRoleAndScopeAuthorizedResourceBlockedWithNoScope() throws Exception {
        setCurrentUser(PscRole.STUDY_QA_MANAGER, PscRole.DATA_READER);

        expectStatusForMethod(DELETE, Status.CLIENT_ERROR_FORBIDDEN);
    }

    public void testRoleAndScopeAuthorizedResourceBlockedWhenRoleAndScopeNotForSameMembership() throws Exception {
        setCurrentUser(PscRole.STUDY_QA_MANAGER, PscRole.DATA_READER);
        getCurrentUser().getMemberships().get(SuiteRole.STUDY_QA_MANAGER).forSites("b");
        getCurrentUser().getMemberships().get(SuiteRole.DATA_READER).forSites("a");

        expectStatusForMethod(DELETE, Status.CLIENT_ERROR_FORBIDDEN);
    }

    private PscUser getCurrentUser() {
        return (PscUser) PscGuard.getCurrentAuthenticationToken(request).getPrincipal();
    }

    private PscUser setCurrentUser(PscRole... desiredRoles) {
        User csmUser = new User();
        csmUser.setLoginName("josephine");
        Map<SuiteRole, SuiteRoleMembership> memberships = new HashMap<SuiteRole, SuiteRoleMembership>();
        for (PscRole desiredRole : desiredRoles) {
            memberships.put(desiredRole.getSuiteRole(),
                new SuiteRoleMembership(desiredRole.getSuiteRole(),
                    AuthorizationScopeMappings.SITE_MAPPING, AuthorizationScopeMappings.STUDY_MAPPING));
        }
        PscUser user = new PscUser(csmUser, memberships);

        PscGuard.setCurrentAuthenticationToken(request,
            new TestingAuthenticationToken(user, null, user.getAuthorities()));
        return user;
    }

    private void expectStatusForMethod(Method method, Status status) {
        request.setMethod(method);
        expect(beanFactory.getBean(BEAN_NAME)).andReturn(new TestAuthorizingResource());

        replayMocks();
        finder.handle(request, response);
        verifyMocks();

        assertResponseStatus(status);
    }

    private static class TestAuthorizingResource extends AbstractPscResource {
        @Override
        public void init(Context context, Request request, Response response) {
            super.init(context, request, response);
            addAuthorizationsFor(POST, PscRole.SYSTEM_ADMINISTRATOR);
            addAuthorizationsFor(DELETE,
                ResourceAuthorization.create(PscRole.STUDY_QA_MANAGER, Fixtures.createSite("A", "a")));
            setReadable(true);
            setModifiable(true);
        }

        @Override
        public void acceptRepresentation(Representation entity) throws ResourceException {
            // dummy
        }

        @Override
        public void removeRepresentations() throws ResourceException {
            // dummy
        }
    }
}
