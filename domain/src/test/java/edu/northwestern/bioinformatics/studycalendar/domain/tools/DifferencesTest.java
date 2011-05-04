package edu.northwestern.bioinformatics.studycalendar.domain.tools;

import edu.northwestern.bioinformatics.studycalendar.domain.DomainTestCase;
import gov.nih.nci.cabig.ctms.lang.DateTools;

import java.util.Calendar;

/**
 * @author Rhett Sutphin
 */
public class DifferencesTest extends DomainTestCase {
    private Differences diff = new Differences();

    public void testRegisterEqualValuesDoesNothing() throws Exception {
        diff.registerValueDifference("foo", 5, 5);
        assertNoDifferences();
    }

    public void testRegisterTwoNullValuesDoesNothing() throws Exception {
        diff.registerValueDifference("bar", null, null);
        assertNoDifferences();
    }

    public void testRegisterLeftNullRecordsDifference() throws Exception {
        diff.registerValueDifference("bar", null, "A");
        assertDifferences(diff, "bar <null> does not match \"A\"");
    }

    public void testRegisterRightNullRecordsDifference() throws Exception {
        diff.registerValueDifference("bar", "A", null);
        assertDifferences(diff, "bar \"A\" does not match <null>");
    }

    public void testRegisterDifferentOtherValuesCreatesMessageToStrings() throws Exception {
        diff.registerValueDifference("bar", 5, 7);
        assertDifferences(diff, "bar 5 does not match 7");
    }

    public void testRegisterDifferentStringValuesCreatesMessageWithQuotedValues() throws Exception {
        diff.registerValueDifference("bar", "A", "T");
        assertDifferences(diff, "bar \"A\" does not match \"T\"");
    }

    public void testRegisterDifferentDateValuesFormatsDatesPerFormatTools() throws Exception {
        diff.registerValueDifference("baz",
            DateTools.createDate(2011, Calendar.FEBRUARY, 6),
            DateTools.createDate(2001, Calendar.MARCH, 7));
        assertDifferences(diff, "baz 2011-02-06 does not match 2001-03-07");
    }

    public void testTreeStringWithMessagesOnly() throws Exception {
        diff.addMessage("A");
        diff.addMessage("B");
        diff.addMessage("C");
        assertEquals("* A\n* B\n* C", diff.toTreeString());
    }

    public void testTreeStringWithNestedChildrenMessages() throws Exception {
        diff.addMessage("A");
        diff.addMessage("B");

        Differences childOne = new Differences();
        childOne.addMessage("one");
        diff.getChildDifferences().put("C1", childOne);
        Differences grandchild = new Differences();
        grandchild.addMessage("g one");
        grandchild.addMessage("g two");
        childOne.getChildDifferences().put("G1", grandchild);

        Differences childTwo = new Differences();
        childTwo.addMessage("two");
        diff.getChildDifferences().put("C2", childTwo);

        assertEquals("* A\n" +
            "* B\n" +
            "- C1\n" +
            "  * one\n" +
            "  - G1\n" +
            "    * g one\n" +
            "    * g two\n" +
            "- C2\n" +
            "  * two",
            diff.toTreeString()
        );
    }

    ////// HELPERS

    private void assertNoDifferences() {
        assertEquals(0, diff.getMessages().size());
    }
}
