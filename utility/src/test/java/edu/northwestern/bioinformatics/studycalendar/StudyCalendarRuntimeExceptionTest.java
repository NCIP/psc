/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.com/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar;

import junit.framework.TestCase;

import java.lang.reflect.Constructor;

/**
 * @author Rhett Sutphin
 */
public class StudyCalendarRuntimeExceptionTest extends TestCase {
    public void testSimpleMessageTextOnlyMessageWorks() throws Exception {
        assertEquals("B", new PscTestException("B").getMessage());
    }

    public void testMessageIsFormattedFromVarargsParameters() throws Exception {
        assertEquals("A B C", new PscTestException("%s B %s", "A", "C").getMessage());
    }

    public void testCauseIsPreserved() throws Exception {
        Exception expected = new Exception();
        assertSame(expected, new PscTestException("Relayed %s", expected, "A").getCause());
    }

    public void testCauseIsExtractedFromEndOfParametersIfNotAtHead() throws Exception {
        Exception expectedCause = new Exception();
        Exception actual = new PscTestException("Relayed %s", "A", expectedCause);
        assertSame(expectedCause, actual.getCause());
        assertEquals("Wrong message", "Relayed A", actual.getMessage());
    }
    
    public void testFirstCauseTrumpsLastCause() throws Exception {
        Exception expectedCause = new Exception("cause");
        Exception parameterCause = new Exception("parameter");
        Exception actual = new PscTestException("Relayed %d: %s", expectedCause, 4, parameterCause);
        assertSame(expectedCause, actual.getCause());
        assertEquals("Wrong message", "Relayed 4: java.lang.Exception: parameter", actual.getMessage());
    }

    public void testConstructorsWithExplicitCauseArgumentWork() throws Exception {
        Exception expectedCause = new Exception();
        Exception actual
            = new PscTestExceptionWithCauseConstructor("Relayed %s", expectedCause, "A");
        assertSame(expectedCause, actual.getCause());
        assertEquals("Wrong message", "Relayed A", actual.getMessage());
    }
    
    public void testNullMessageIsAcceptedForProxying() throws Exception {
        assertNull(new PscTestException(null).getMessage());
    }
    
    @SuppressWarnings({ "ThrowableResultOfMethodCallIgnored" })
    public void testNullMessageParametersAreAcceptedForProxying() throws Exception {
        Constructor<PscTestException> cons = PscTestException.class.getConstructor(String.class, Object[].class);
        assertNotNull("Expected constructor not found", cons);
        PscTestException actual = cons.newInstance(null, null);
        assertNull(actual.getMessage());
    }

    public void testCauseOnlySubclassWorks() throws Exception {
        Exception cause = new Exception("cause");
        PscTestExceptionWithOnlyCauseConstructor actual =
            new PscTestExceptionWithOnlyCauseConstructor(cause);
        assertEquals("I always know the message for this one", actual.getMessage());
        assertSame("Wrong cause", cause, actual.getCause());
    }

    private static class PscTestException extends StudyCalendarRuntimeException {
        public PscTestException(String message, Object... messageParameters) {
            super(message, messageParameters);
        }
    }

    private static class PscTestExceptionWithCauseConstructor extends StudyCalendarRuntimeException {
        public PscTestExceptionWithCauseConstructor(
            String message, Throwable cause, Object... messageParameters
        ) {
            super(message, cause, messageParameters);
        }
    }

    private static class PscTestExceptionWithOnlyCauseConstructor extends StudyCalendarRuntimeException {
        private PscTestExceptionWithOnlyCauseConstructor(Throwable cause) {
            super("I always know the message for this one", cause);
        }
    }
}
