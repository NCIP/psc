package edu.northwestern.bioinformatics.studycalendar.core.accesscontrol;

import edu.northwestern.bioinformatics.studycalendar.core.osgi.OsgiLayerTools;
import gov.nih.nci.cabig.ctms.testing.MockRegistry;
import gov.nih.nci.security.AuthorizationManager;
import gov.nih.nci.security.authorization.domainobjects.Group;
import gov.nih.nci.security.authorization.domainobjects.Privilege;
import gov.nih.nci.security.authorization.domainobjects.User;
import gov.nih.nci.security.exceptions.CSObjectNotFoundException;
import gov.nih.nci.security.exceptions.CSTransactionException;
import org.junit.Before;
import org.junit.Test;

import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

/**
 * @author Rhett Sutphin
 */
@SuppressWarnings( { "unchecked" })
public class OsgiAuthorizationManagerFactoryBeanTest {
    private OsgiAuthorizationManagerFactoryBean factory;

    private MockRegistry mocks = new MockRegistry();
    private OsgiLayerTools osgiLayerTools;

    @Before
    public void before() throws Exception {
        osgiLayerTools = mocks.registerMockFor(OsgiLayerTools.class);

        factory = new OsgiAuthorizationManagerFactoryBean();
        factory.setOsgiLayerTools(osgiLayerTools);
    }

    @Test
    public void objectTypeIsAuthorizationManager() throws Exception {
        assertThat(factory.getObjectType(), equalTo((Class) AuthorizationManager.class));
    }

    @Test
    public void isSingleton() throws Exception {
        assertThat(factory.isSingleton(), is(true));
    }

    @Test
    public void objectReturnsSameInstanceForEveryCall() throws Exception {
        assertThat(factory.getObject(), is(sameInstance(factory.getObject())));
    }

    @Test
    public void createdObjectImplementsAuthorizationManager() throws Exception {
        assertThat(factory.getObject(), is(AuthorizationManager.class));
    }

    @Test
    public void createdObjectImplementsPossiblyReadOnlyAuthorizationManager() throws Exception {
        assertThat(factory.getObject(), is(PossiblyReadOnlyAuthorizationManager.class));
    }

    @Test
    public void createdObjectDelegatesToTheAuthorizationManagerFoundInTheOsgiLayer() throws Exception {
        AuthorizationManager found = mocks.registerMockFor(AuthorizationManager.class);
        expect(osgiLayerTools.getRequiredService(AuthorizationManager.class)).andReturn(found);
        Group expectedGroup = new Group();
        expect(found.getGroupById("76")).andReturn(expectedGroup);

        mocks.replayMocks();
        AuthorizationManager authorizationManager = (AuthorizationManager) factory.getObject();
        assertThat(authorizationManager.getGroupById("76"), is(sameInstance(expectedGroup)));
        mocks.verifyMocks();
    }

    @Test
    public void createdObjectUsesTheCurrentAuthorizationManagerForEachSeparateCall() throws Exception {
        AuthorizationManager one = mocks.registerMockFor(AuthorizationManager.class);
        AuthorizationManager two = mocks.registerMockFor(AuthorizationManager.class);

        expect(osgiLayerTools.getRequiredService(AuthorizationManager.class)).andReturn(one);
        expect(osgiLayerTools.getRequiredService(AuthorizationManager.class)).andReturn(two);

        expect(one.getUserById("11")).andReturn(new User());
        expect(two.getPrivilegeById("12")).andReturn(new Privilege());

        mocks.replayMocks();
        ((AuthorizationManager) factory.getObject()).getUserById("11");
        ((AuthorizationManager) factory.getObject()).getPrivilegeById("12");
        mocks.verifyMocks();
    }

    @Test
    public void createdObjectRethrowsExceptionsFromDelegate() throws Exception {
        AuthorizationManager found = mocks.registerMockFor(AuthorizationManager.class);
        expect(osgiLayerTools.getRequiredService(AuthorizationManager.class)).andReturn(found);
        expect(found.getGroupById("76")).andThrow(new CSObjectNotFoundException("Answer hazy"));

        mocks.replayMocks();
        AuthorizationManager authorizationManager = (AuthorizationManager) factory.getObject();
        try {
            authorizationManager.getGroupById("76");
            fail("Exception not thrown");
        } catch (CSObjectNotFoundException o) {
            assertThat(o.getMessage(), is("Answer hazy"));
        }
        mocks.verifyMocks();
    }

    @Test
    public void createdObjectIsReadOnlyIfModifyingCallsThrowUnsupported() throws Exception {
        AuthorizationManager found = mocks.registerMockFor(AuthorizationManager.class);
        expect(osgiLayerTools.getRequiredService(AuthorizationManager.class)).andReturn(found);
        found.removeRole("foobarquux");
        expectLastCall().andThrow(new UnsupportedOperationException("No dice"));

        mocks.replayMocks();
        PossiblyReadOnlyAuthorizationManager authorizationManager =
            (PossiblyReadOnlyAuthorizationManager) factory.getObject();
        assertThat(authorizationManager.isReadOnly(), is(true));
        mocks.verifyMocks();
    }

    @Test
    public void createdObjectIsNotReadOnlyIfModifyingCallsThrowDeclaredException() throws Exception {
        AuthorizationManager found = mocks.registerMockFor(AuthorizationManager.class);
        expect(osgiLayerTools.getRequiredService(AuthorizationManager.class)).andReturn(found);
        found.removeRole("foobarquux");
        expectLastCall().andThrow(new CSTransactionException("No dice"));

        mocks.replayMocks();
        PossiblyReadOnlyAuthorizationManager authorizationManager =
            (PossiblyReadOnlyAuthorizationManager) factory.getObject();
        assertThat(authorizationManager.isReadOnly(), is(false));
        mocks.verifyMocks();
    }

    @Test
    public void createdObjectIsNotReadOnlyIfModifyingCallsThrowOtherRuntimeException() throws Exception {
        AuthorizationManager found = mocks.registerMockFor(AuthorizationManager.class);
        expect(osgiLayerTools.getRequiredService(AuthorizationManager.class)).andReturn(found);
        found.removeRole("foobarquux");
        expectLastCall().andThrow(new NullPointerException("No dice"));

        mocks.replayMocks();
        PossiblyReadOnlyAuthorizationManager authorizationManager =
            (PossiblyReadOnlyAuthorizationManager) factory.getObject();
        assertThat(authorizationManager.isReadOnly(), is(false));
        mocks.verifyMocks();
    }

    @Test
    public void createdObjectIsNotReadOnlyIfModifyingCallsDoNothing() throws Exception {
        AuthorizationManager found = mocks.registerMockFor(AuthorizationManager.class);
        expect(osgiLayerTools.getRequiredService(AuthorizationManager.class)).andReturn(found);
        found.removeRole("foobarquux");

        mocks.replayMocks();
        PossiblyReadOnlyAuthorizationManager authorizationManager =
            (PossiblyReadOnlyAuthorizationManager) factory.getObject();
        assertThat(authorizationManager.isReadOnly(), is(false));
        mocks.verifyMocks();
    }
}
