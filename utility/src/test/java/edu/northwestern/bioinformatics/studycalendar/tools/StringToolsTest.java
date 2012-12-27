/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.com/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.tools;

import junit.framework.TestCase;

import static gov.nih.nci.cabig.ctms.testing.MoreJUnitAssertions.*;

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

    public void testCaseInsensitiveOrderWhenFirstNull() throws Exception {
        assertNegative(StringTools.CASE_INSENSITIVE_NULL_SAFE_ORDER.compare(null, "abc"));
    }

    public void testCaseInsensitiveOrderWhenSecondNull() throws Exception {
        assertPositive(StringTools.CASE_INSENSITIVE_NULL_SAFE_ORDER.compare("abc", null));
    }

    public void testCaseInsensitiveOrderWhenBothNull() throws Exception {
        assertEquals(0, StringTools.CASE_INSENSITIVE_NULL_SAFE_ORDER.compare(null, null));
    }

    public void testCaseInsensitiveOrderWhenEquals() throws Exception {
        assertEquals(0, StringTools.CASE_INSENSITIVE_NULL_SAFE_ORDER.compare("abc", "abc"));
    }

    public void testCaseInsensitiveOrderWhenCaseInsensitivelyEquals() throws Exception {
        assertNegative(StringTools.CASE_INSENSITIVE_NULL_SAFE_ORDER.compare("aBc", "abc"));
        assertPositive(StringTools.CASE_INSENSITIVE_NULL_SAFE_ORDER.compare("abc", "aBc"));
    }

    public void testCaseInsensitiveOrderIsCaseInsensitive() throws Exception {
        assertPositive(StringTools.CASE_INSENSITIVE_NULL_SAFE_ORDER.compare("z", "A"));
        assertNegative(StringTools.CASE_INSENSITIVE_NULL_SAFE_ORDER.compare("A", "z"));
    }
}
