package edu.northwestern.bioinformatics.studycalendar.domain;

import junit.framework.TestCase;

/**
 * @author Rhett Sutphin
 */
public class SubjectTest extends TestCase {
    private Subject subject = new Subject();

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        subject.setFirstName("Ana");
        subject.setLastName("Ng");
    }

    public void testLastFirst() throws Exception {
        assertEquals("Ng, Ana", subject.getLastFirst());
    }

    public void testLastFirstNoFirst() throws Exception {
        subject.setFirstName(null);
        assertEquals("Ng", subject.getLastFirst());
    }

    public void testLastFirstNoLast() throws Exception {
        subject.setLastName(null);
        assertEquals("Ana", subject.getLastFirst());
    }

    public void testFullName() throws Exception {
        assertEquals("Ana Ng", subject.getFullName());
    }

    public void testFullNameNoLast() throws Exception {
        subject.setLastName(null);
        assertEquals("Ana", subject.getFullName());
    }

    public void testFullNameNoFirst() throws Exception {
        subject.setFirstName(null);
        assertEquals("Ng", subject.getFullName());
    }

    public void testPropertiesDefaultsToAnEmptyList() throws Exception {
        assertNotNull(subject.getProperties());
        assertEquals(0, subject.getProperties().size());
    }
}
