package edu.northwestern.bioinformatics.studycalendar.restlets;

import org.springframework.beans.factory.BeanFactory;
import org.easymock.classextension.EasyMock;
import static org.easymock.classextension.EasyMock.*;
import org.restlet.resource.Resource;
import org.restlet.resource.ResourceException;
import org.restlet.resource.Representation;
import org.restlet.Context;
import org.restlet.data.Status;
import org.restlet.data.Method;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.acegisecurity.providers.TestingAuthenticationToken;
import edu.northwestern.bioinformatics.studycalendar.domain.Role;

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
        request.setMethod(Method.GET);

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

    public void testAuthorizingResourceLetInWhenAuthorized() throws Exception {
        request.getAttributes().put(PscGuard.AUTH_TOKEN_ATTRIBUTE_KEY,
            new TestingAuthenticationToken(null, null, new Role[] { Role.SITE_COORDINATOR, Role.STUDY_ADMIN }));
        request.setMethod(Method.DELETE);
        expect(beanFactory.getBean(BEAN_NAME)).andReturn(new TestAuthorizingResource());

        replayMocks();
        finder.handle(request, response);
        verifyMocks();

        assertResponseStatus(Status.SUCCESS_OK);
    }
    
    public void testAuthorizingResource403sWhenNotAuthorized() throws Exception {
        request.getAttributes().put(PscGuard.AUTH_TOKEN_ATTRIBUTE_KEY,
            new TestingAuthenticationToken(null, null, new Role[] { Role.SITE_COORDINATOR, Role.STUDY_ADMIN }));
        request.setMethod(Method.POST);
        expect(beanFactory.getBean(BEAN_NAME)).andReturn(new TestAuthorizingResource());

        replayMocks();
        finder.handle(request, response);
        verifyMocks();

        assertResponseStatus(Status.CLIENT_ERROR_FORBIDDEN);
    }

    private static class TestAuthorizingResource extends AbstractPscResource {
        @Override
        public void init(Context context, Request request, Response response) {
            super.init(context, request, response);
            setAuthorizedFor(Method.POST, Role.SYSTEM_ADMINISTRATOR);
            setAuthorizedFor(Method.DELETE, Role.STUDY_ADMIN);
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
