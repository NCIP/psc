package edu.northwestern.bioinformatics.studycalendar.tools;

import junit.framework.TestCase;

/**
 * @author Rhett Sutphin
 */
public class StringToolsTest extends TestCase {
    public void testHumanizeOneWord() throws Exception {
        assertEquals("windowsill", StringTools.humanizeClassName("Windowsill"));
    }

    public void testHumanizeTwoWords() throws Exception {
        assertEquals("study segment", StringTools.humanizeClassName("StudySegment"));
    }

    public void testHumanizeThreeWords() throws Exception {
        assertEquals("secure section interceptor", StringTools.humanizeClassName("SecureSectionInterceptor"));
    }

    public void testValueOf() {
        Integer i = null;
        assertNull("value should be null. it should  not be 'null' string", StringTools.valueOf(i));
        assertEquals("null", String.valueOf(i));

    }

    public void testHumanizeNull() throws Exception {
        assertNull(StringTools.humanizeClassName(null));
    }
}
