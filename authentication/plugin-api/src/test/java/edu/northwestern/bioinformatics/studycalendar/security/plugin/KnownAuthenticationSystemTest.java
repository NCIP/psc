package edu.northwestern.bioinformatics.studycalendar.security.plugin;

import junit.framework.TestCase;

/**
 * @author Rhett Sutphin
 */
public class KnownAuthenticationSystemTest extends TestCase {
    public void testSafeValueOfForKnownValue() throws Exception {
        assertSame(KnownAuthenticationSystem.LOCAL, KnownAuthenticationSystem.safeValueOf("LOCAL"));
    }

    public void testSafeValueOfForUnknownValue() throws Exception {
        assertNull(KnownAuthenticationSystem.safeValueOf("uncle-bob"));
    }
}
