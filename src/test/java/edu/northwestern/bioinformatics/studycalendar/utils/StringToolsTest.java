package edu.northwestern.bioinformatics.studycalendar.utils;

import edu.nwu.bioinformatics.commons.testing.CoreTestCase;

/**
 * @author Rhett Sutphin
 */
public class StringToolsTest extends CoreTestCase {
    public void testHumanizeOneWord() throws Exception {
        assertEquals("windowsill", StringTools.humanizeClassName("Windowsill"));
    }

    public void testHumanizeTwoWords() throws Exception {
        assertEquals("study segment", StringTools.humanizeClassName("StudySegment"));
    }

    public void testHumanizeThreeWords() throws Exception {
        assertEquals("secure section interceptor", StringTools.humanizeClassName("SecureSectionInterceptor"));
    }

    public void testHumanizeNull() throws Exception {
        assertNull(StringTools.humanizeClassName(null));
    }
}
