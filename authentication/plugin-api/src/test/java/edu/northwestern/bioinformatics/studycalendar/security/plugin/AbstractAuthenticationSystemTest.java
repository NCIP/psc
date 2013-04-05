/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.io/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.security.plugin;

import gov.nih.nci.cabig.ctms.tools.configuration.ConfigurationProperties;
import org.acegisecurity.Authentication;
import org.acegisecurity.AuthenticationManager;
import org.acegisecurity.ui.AuthenticationEntryPoint;

import javax.servlet.Filter;

/**
 * Test for behaviors in {@link AbstractAuthenticationSystem}.
 *
 * @author Rhett Sutphin
 */
public final class AbstractAuthenticationSystemTest extends AuthenticationTestCase {
    private TestAuthenticationSystem system;
    private AuthenticationManager expectedAuthenticationManager;
    private AuthenticationEntryPoint expectedEntryPoint;
    private Filter expectedFilter;
    private Filter expectedLogoutFilter;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        system = new TestAuthenticationSystem();

        expectedAuthenticationManager = registerMockFor(AuthenticationManager.class);
        expectedEntryPoint = registerMockFor(AuthenticationEntryPoint.class);
        expectedFilter = registerMockFor(Filter.class);
        expectedLogoutFilter = registerMockFor(Filter.class);

        system.setCreatedAuthenticationManager(expectedAuthenticationManager);
        system.setCreatedAuthenticationEntryPoint(expectedEntryPoint);
        system.setCreatedFilter(expectedFilter);
        system.setCreatedLogoutFilter(expectedLogoutFilter);
        system.setBundleContext(bundleContext);
    }

    public void testReturnsCreatedAuthManager() throws Exception {
        doInitialize();
        assertSame(expectedAuthenticationManager, system.authenticationManager());
    }

    public void testAuthenticationManagerIsRequired() throws Exception {
        system.setCreatedAuthenticationManager(null);
        try {
            doInitialize();
            fail("Exception not thrown");
        } catch (AuthenticationSystemInitializationFailure actual) {
            assertEquals("Wrong failure message",
                "TestAuthenticationSystem must not return null from authenticationManager()",
                actual.getMessage());
        }
    }

    public void testReturnsCreatedEntryPoint() throws Exception {
        doInitialize();
        assertSame(expectedEntryPoint, system.entryPoint());
    }

    public void testEntryPointIsRequired() throws Exception {
        system.setCreatedAuthenticationEntryPoint(null);
        try {
            doInitialize();
            fail("Exception not thrown");
        } catch (AuthenticationSystemInitializationFailure actual) {
            assertEquals("Wrong failure message",
                "TestAuthenticationSystem must not return null from entryPoint()",
                actual.getMessage());
        }
    }

    public void testReturnsCreatedFilter() throws Exception {
        doInitialize();
        assertSame(expectedFilter, system.filter());
    }

    public void testFilterIsNotRequired() throws Exception {
        system.setCreatedFilter(null);
        doInitialize();
        assertNull(system.filter());
    }

    public void testReturnsCreatedLogoutFilter() throws Exception {
        doInitialize();
        assertSame(expectedLogoutFilter, system.logoutFilter());
    }

    public void testLogoutFilterIsNotRequired() throws Exception {
        system.setCreatedLogoutFilter(null);
        doInitialize();
        assertNull(system.logoutFilter());
    }

    public void testMultipleMissingElementsAreReported() throws Exception {
        system.setCreatedAuthenticationManager(null);
        system.setCreatedAuthenticationEntryPoint(null);

        try {
            doInitialize();
            fail("Exception not thrown");
        } catch (AuthenticationSystemInitializationFailure actual) {
            assertEquals("Wrong failure message",
                "TestAuthenticationSystem must not return null from authenticationManager() or entryPoint()",
                actual.getMessage());
        }
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
    
    public void testDoesNotWrapAuthenticationSystemInitializationFailures() throws Exception {
        system.setInitBefore(new Runnable() {
            public void run() {
                throw new AuthenticationSystemInitializationFailure("I'm afraid I can't do that");
            }
        });

        try {
            doInitialize();
        } catch (AuthenticationSystemInitializationFailure asif) {
            assertEquals("I'm afraid I can't do that", asif.getMessage());
        }
    }

    private void doInitialize() {
        system.initialize(blankConfiguration());
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

        public String behaviorDescription() {
            throw new UnsupportedOperationException("behaviorDescription not implemented");
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
