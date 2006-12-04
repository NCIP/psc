package edu.northwestern.bioinformatics.studycalendar.domain;

import edu.northwestern.bioinformatics.studycalendar.testing.StudyCalendarTestCase;
import edu.northwestern.bioinformatics.studycalendar.utils.DomainObjectTools;

import java.util.Comparator;

/**
 * @author Rhett Sutphin
 */
public class AbstractDomainObjectTest extends StudyCalendarTestCase {
    private AbstractDomainObject o1, o2;

    protected void setUp() throws Exception {
        super.setUp();
        o1 = new TestObject(1);
        o2 = new TestObject(2);
    }

    public void testEqualByIdWithTwoNulls() throws Exception {
        assertTrue(AbstractDomainObject.equalById(null, null));
    }

    public void testEqualByIdWithFirstNull() throws Exception {
        assertFalse(AbstractDomainObject.equalById(null, o1));
    }

    public void testEqualByIdWithSecondNull() throws Exception {
        assertFalse(AbstractDomainObject.equalById(o1, null));
    }

    public void testEqualByIdWhenSame() throws Exception {
        assertTrue(AbstractDomainObject.equalById(o1, o1));
    }

    public void testEqualByIdWhenEqual() throws Exception {
        o2.setId(o1.getId());
        assertTrue(AbstractDomainObject.equalById(o1, o2));
    }
    
    public void testEqualByIdWhenNotEqual() throws Exception {
        assertFalse(AbstractDomainObject.equalById(o1, o2));
    }
}
