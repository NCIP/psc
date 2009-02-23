package edu.northwestern.bioinformatics.studycalendar.tools;

import junit.framework.TestCase;
import junit.framework.Assert;

import java.util.LinkedList;
import java.util.List;
import java.util.Arrays;

/**
 * @author Rhett Sutphin
 */
public class ExpandingListTest extends TestCase {
    private List<String> internal = new LinkedList<String>();
    private ExpandingList<String> list = new ExpandingList<String>(new TestFiller(), internal);

    protected void setUp() throws Exception {
        super.setUp();
        internal.add("0");
        internal.add("1");
    }

    public void testGetExisting() throws Exception {
        Assert.assertEquals("0", list.get(0));
        Assert.assertEquals("1", list.get(1));
    }

    public void testGetNonExistent() throws Exception {
        try {
            list.get(12);
        } catch (IndexOutOfBoundsException ioobe) {
            fail("There must not be any IOOBEs from an ExpandingList (" + ioobe.getMessage() + ')');
        }
    }

    public void testGetNonExistentFills() throws Exception {
        Assert.assertEquals(list.get(8), "fill 8");
        assertLength(9);
        assertEquals("Fills not stored in list", "fill 5", internal.get(5));
    }

    public void testDefaultExtenderIsNull() throws Exception {
        Assert.assertEquals(null, new ExpandingList<Object>().get(15));
    }

    public void testSetAtEnd() throws Exception {
        String newVal = "One";
        list.set(1, newVal);
        assertInternalValueAt(1, newVal);
        assertLength(2);
    }

    public void testSetJustPastEnd() throws Exception {
        String newVal = "Two";
        list.set(2, newVal);
        assertInternalValueAt(2, newVal);
        assertLength(3);
    }

    public void testSetFarPastEnd() throws Exception {
        String newVal = "Fourteen";
        list.set(14, newVal);
        assertInternalValueAt(14, newVal);
        assertLength(15);
    }

    public void testAddAllPastEnd() throws Exception {
        list.addAll(6, Arrays.asList("The", "real", "collection"));
        assertFillAt(2, 3, 4, 5);
        assertInternalValueAt(6, "The");
        assertInternalValueAt(7, "real");
        assertInternalValueAt(8, "collection");
        assertLength(9);
    }

    public void testAddPastEnd() throws Exception {
        list.add(9, "Added");
        assertInternalValueAt(9, "Added");
        assertFillAt(2, 3, 4, 5, 6, 7, 8);
        assertLength(10);
    }

    public void testAddInternal() throws Exception {
        list.add(1, "Inserted");
        assertInternalValueAt(0, "0");
        assertInternalValueAt(1, "Inserted");
        assertInternalValueAt(2, "1");
        assertLength(3);
    }

    private void assertInternalValueAt(int index, String expectedValue) {
        assertEquals("Wrong value at " + index, expectedValue, internal.get(index));
    }

    private void assertFillAt(int... index) {
        for (int i : index) {
            assertEquals("Value at " + i + " is not fill", fillValue(i), internal.get(i));
        }
    }

    private void assertLength(int len) {
        Assert.assertEquals("Wrong external length: " + list, len, list.size());
        assertEquals("Wrong internal length: " + internal, len, internal.size());
    }

    private static final class TestFiller implements ExpandingList.Filler<String> {
        public String createNew(int index) {
            return fillValue(index);
        }
    }

    private static String fillValue(int index) {
        return "fill " + index;
    }
}
