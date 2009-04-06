package edu.northwestern.bioinformatics.studycalendar.security.plugin;

import edu.northwestern.bioinformatics.studycalendar.StudyCalendarValidationException;
import gov.nih.nci.cabig.ctms.tools.configuration.ConfigurationProperties;
import org.acegisecurity.Authentication;
import org.acegisecurity.AuthenticationManager;
import org.acegisecurity.ui.AuthenticationEntryPoint;
import org.springframework.context.ApplicationContext;

import javax.servlet.Filter;

/**
 * Test for behaviors in {@link AbstractAuthenticationSystem}.
 *
 * @author Rhett Sutphin
 */
public final class AbstractAuthenticationSystemTest extends AuthenticationTestCase {
    private ApplicationContext applicationContext;
    private TestAuthenticationSystem system;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        system = new TestAuthenticationSystem();
        applicationContext = registerNiceMockFor(ApplicationContext.class);
    }

    public void testReturnsCreatedAuthManager() throws Exception {
        AuthenticationManager expected = registerMockFor(AuthenticationManager.class);
        system.setCreatedAuthenticationManager(expected);
        doInitialize();
        assertSame(expected, system.authenticationManager());
    }

    public void testReturnsCreatedEntryPoint() throws Exception {
        AuthenticationEntryPoint expected = registerMockFor(AuthenticationEntryPoint.class);
        system.setCreatedAuthenticationEntryPoint(expected);
        doInitialize();
        assertSame(expected, system.entryPoint());
    }

    public void testReturnsCreatedFilter() throws Exception {
        Filter expected = registerMockFor(Filter.class);
        system.setCreatedFilter(expected);
        doInitialize();
        assertSame(expected, system.filter());
    }

    public void testReturnsCreatedLogoutFilter() throws Exception {
        Filter expected = registerMockFor(Filter.class);
        system.setCreatedLogoutFilter(expected);
        doInitialize();
        assertSame(expected, system.logoutFilter());
    }

    public void testDefaultNameIsClassNameWithoutAuthenticationSystemSuffix() throws Exception {
        assertEquals("Test", system.name());
    }

    public void testRaisesAuthenticationSystemInitializationFailureForRandomInitErrors() throws Exception {
        system.setInitBefore(new Runnable() {
            public void run() {
                throw new IllegalStateException("I'm afraid I can't do that");
            }
        });

        try {
            doInitialize();
        } catch (AuthenticationSystemInitializationFailure failure) {
            assertEquals("I'm afraid I can't do that", failure.getMessage());
            assertNotNull("Triggering exception not included", failure.getCause());
            assertTrue("Triggering exception not included", failure.getCause() instanceof IllegalStateException);
        }
    }
    
    public void testDoesNotWrapSCUserException() throws Exception {
        system.setInitBefore(new Runnable() {
            public void run() {
                throw new StudyCalendarValidationException("I'm afraid I can't do that");
            }
        });

        try {
            doInitialize();
        } catch (StudyCalendarValidationException scve) {
            assertEquals("I'm afraid I can't do that", scve.getMessage());
        }
    }

    private void doInitialize() {
        system.initialize(applicationContext, blankConfiguration());
    }

    private static class TestAuthenticationSystem extends AbstractAuthenticationSystem {
        private AuthenticationManager createdAuthenticationManager;
        private AuthenticationEntryPoint createdAuthenticationEntryPoint;
        private Filter createdFilter, createdLogoutFilter;

        private Runnable initBefore, initAfter;

        @Override
        protected void initBeforeCreate() {
            if (initBefore != null) initBefore.run();
        }

        @Override
        protected AuthenticationManager createAuthenticationManager() {
            return createdAuthenticationManager;
        }

        @Override
        protected AuthenticationEntryPoint createEntryPoint() {
            return createdAuthenticationEntryPoint;
        }

        @Override
        protected Filter createFilter() {
            return createdFilter;
        }

        @Override
        protected Filter createLogoutFilter() {
            return createdLogoutFilter;
        }

        @Override
        protected void initAfterCreate() {
            if (initAfter != null) initAfter.run();
        }

        public ConfigurationProperties configurationProperties() {
            throw new UnsupportedOperationException("configurationProperties not implemented");
            // return null;
        }

        public Authentication createUsernamePasswordAuthenticationRequest(String username, String password) {
            throw new UnsupportedOperationException("createUsernamePasswordAuthenticationRequest not implemented");
            // return null;
        }

        public Authentication createTokenAuthenticationRequest(String token) {
            throw new UnsupportedOperationException("createTokenAuthenticationRequest not implemented");
            // return null;
        }

        ////// CONFIGURATION

        public void setCreatedAuthenticationManager(AuthenticationManager createdAuthenticationManager) {
            this.createdAuthenticationManager = createdAuthenticationManager;
        }

        public void setCreatedAuthenticationEntryPoint(AuthenticationEntryPoint createdAuthenticationEntryPoint) {
            this.createdAuthenticationEntryPoint = createdAuthenticationEntryPoint;
        }

        public void setCreatedFilter(Filter createdFilter) {
            this.createdFilter = createdFilter;
        }

        public void setCreatedLogoutFilter(Filter createdLogoutFilter) {
            this.createdLogoutFilter = createdLogoutFilter;
        }

        public void setInitBefore(Runnable initBefore) {
            this.initBefore = initBefore;
        }

        public void setInitAfter(Runnable initAfter) {
            this.initAfter = initAfter;
        }
    }
}
