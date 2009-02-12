package edu.northwestern.bioinformatics.studycalendar.domain;

import gov.nih.nci.cabig.ctms.testing.MockRegistry;
import junit.framework.TestCase;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.lang.reflect.Method;

/**
 * @author Rhett Sutphin
 */
public class DomainTestCase extends TestCase {
    private final Log log = LogFactory.getLog(getClass());
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
}
