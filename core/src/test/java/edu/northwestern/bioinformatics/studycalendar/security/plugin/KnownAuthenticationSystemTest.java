package edu.northwestern.bioinformatics.studycalendar.security.plugin;

import edu.northwestern.bioinformatics.studycalendar.core.StudyCalendarTestCase;

/**
 * @author Rhett Sutphin
 */
public class KnownAuthenticationSystemTest extends StudyCalendarTestCase {
    public void testSafeValueOfForKnownValue() throws Exception {
        assertSame(KnownAuthenticationSystem.LOCAL, KnownAuthenticationSystem.safeValueOf("LOCAL"));
    }

    public void testSafeValueOfForUnknownValue() throws Exception {
        assertNull(KnownAuthenticationSystem.safeValueOf("uncle-bob"));
    }
}
