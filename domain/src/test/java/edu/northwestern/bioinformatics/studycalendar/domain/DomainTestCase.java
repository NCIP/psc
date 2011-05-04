package edu.northwestern.bioinformatics.studycalendar.domain;

import edu.northwestern.bioinformatics.studycalendar.domain.tools.Differences;
import gov.nih.nci.cabig.ctms.testing.MockRegistry;
import junit.framework.TestCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;

/**
 * @author Rhett Sutphin
 */
public abstract class DomainTestCase extends TestCase {
    private final Logger log = LoggerFactory.getLogger(getClass());
    private MockRegistry mocks;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mocks = new MockRegistry(log);
    }

    protected <T> T registerMockFor(Class<T> clazz) {
        return getMocks().registerMockFor(clazz);
    }

    protected <T> T registerMockFor(Class<T> clazz, Method... methods) {
        return getMocks().registerMockFor(clazz, methods);
    }

    protected void replayMocks() {
        getMocks().replayMocks();
    }

    protected void verifyMocks() {
        getMocks().verifyMocks();
    }

    protected void resetMocks() {
        getMocks().resetMocks();
    }

    protected MockRegistry getMocks() {
        return mocks;
    }

    public void assertDifferences(Differences actual, String... expectedMessages) {
        assertEquals("Wrong number of differences: " + actual.getMessages(),
            expectedMessages.length, actual.getMessages().size());
        for (int i = 0; i < expectedMessages.length; i++) {
            String expectedMessage = expectedMessages[i];
            assertEquals("Message mismatch at " + i, expectedMessage, actual.getMessages().get(i));
        }
    }
}
