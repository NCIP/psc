package edu.northwestern.bioinformatics.studycalendar;

import junit.framework.TestCase;

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

    private static class PscTestException extends StudyCalendarRuntimeException {
        private PscTestException(String message, Object... messageParameters) {
            super(message, messageParameters);
        }
    }

    private static class PscTestExceptionWithCauseConstructor extends StudyCalendarRuntimeException {
        private PscTestExceptionWithCauseConstructor(
            String message, Throwable cause, Object... messageParameters
        ) {
            super(message, cause, messageParameters);
        }
    }
}
