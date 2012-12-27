/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.com/psc/LICENSE.txt for details.
 */

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

    public void testGetPropertyWhenExists() throws Exception {
        subject.getProperties().add(new SubjectProperty("Address", "714 Etc. Ave."));
        assertEquals("714 Etc. Ave.", subject.getProperty("Address"));
    }

    public void testGetPropertyWhenNotExists() throws Exception {
        subject.getProperties().add(new SubjectProperty("Address", "714 Etc. Ave."));
        assertNull(subject.getProperty("Phone number"));
    }
}
