package edu.northwestern.bioinformatics.studycalendar.domain;

import edu.northwestern.bioinformatics.studycalendar.testing.StudyCalendarTestCase;

import java.util.Comparator;

/**
 * @author Rhett Sutphin
 */
public class AbstractDomainObjectTest extends StudyCalendarTestCase {
    private Comparator<AbstractDomainObject> byIdComparator = new AbstractDomainObject.ById<AbstractDomainObject>();
    private AbstractDomainObject o1, o2;

    protected void setUp() throws Exception {
        super.setUp();
        o1 = new TestObject();
        o1.setId(1);
        o2 = new TestObject();
        o2.setId(2);
    }

    public void testByIdComparator() throws Exception {
        assertPositive(byIdComparator.compare(o2, o1));
        assertNegative(byIdComparator.compare(o1, o2));
    }

    public void testByIdComparatorWhenEqual() throws Exception {
        o2.setId(1);
        assertEquals(0, byIdComparator.compare(o1, o2));
        assertEquals(0, byIdComparator.compare(o2, o1));
    }
    
    public void testByIdComparatorNullSafe() throws Exception {
        o1.setId(null);
        assertNegative(byIdComparator.compare(o1, o2));
        assertPositive(byIdComparator.compare(o2, o1));
        assertEquals(0, byIdComparator.compare(o1, o1));
    }

    private static class TestObject extends AbstractDomainObject { }
}
