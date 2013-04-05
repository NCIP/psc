/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.io/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.security.internal;

import edu.northwestern.bioinformatics.studycalendar.StudyCalendarValidationException;
import edu.northwestern.bioinformatics.studycalendar.security.AuthenticationSystemConfiguration;
import edu.northwestern.bioinformatics.studycalendar.security.plugin.AuthenticationTestCase;
import static org.easymock.EasyMock.expect;
import org.easymock.classextension.EasyMock;
import org.osgi.service.cm.ConfigurationException;

import java.util.Dictionary;
import java.util.Hashtable;

/**
 * @author Rhett Sutphin
 */
@SuppressWarnings({ "RawUseOfParameterizedType" })
public class CompleteAuthenticationSystemImplTest extends AuthenticationTestCase {
    private CompleteAuthenticationSystemImpl impl;
    private AuthenticationSystemConfiguration asConfiguration;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        asConfiguration = registerMockFor(AuthenticationSystemConfiguration.class);

        impl = new CompleteAuthenticationSystemImpl();
        impl.setConfiguration(asConfiguration);
    }

    public void testManagedServiceUpdatesArePropagatedToConfig() throws Exception {
        Dictionary expected = new Hashtable();
        asConfiguration.updated(expected);
        expect(asConfiguration.getAuthenticationSystem()).andReturn(null); // return ignored

        replayMocks();
        impl.updated(expected);
        verifyMocks();
    }
    
    public void testExpectedExceptionsAreWrappedInOsgiConfigException() throws Exception {
        asConfiguration.updated((Dictionary) EasyMock.notNull());
        StudyCalendarValidationException expectedCause = new StudyCalendarValidationException("Bad news");
        expect(asConfiguration.getAuthenticationSystem()).andThrow(expectedCause);

        replayMocks();
        try {
            impl.updated(new Hashtable());
            fail("Exception not thrown");
        } catch (ConfigurationException ce) {
            assertEquals("Wrong message",
                "Unknown : Problem initializing authentication system: Bad news", ce.getMessage());
            assertEquals("Wrong cause", expectedCause, ce.getCause());
        }
    }

    public void testNullUpdatesNotPropagated() throws Exception {
        replayMocks();
        impl.updated(null);
        verifyMocks();
    }
}
