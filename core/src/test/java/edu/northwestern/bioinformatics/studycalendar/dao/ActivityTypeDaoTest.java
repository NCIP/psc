/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.com/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.dao;

import edu.northwestern.bioinformatics.studycalendar.domain.ActivityType;

/**
 * @author Nataliya Shurupova
 */
public class ActivityTypeDaoTest extends ContextDaoTestCase<ActivityTypeDao> {

    public void testGetById() throws Exception {
        ActivityType actual = getDao().getById(-10);
        assertNotNull(actual);
        assertEquals("Wrong instance found", -10, (int) actual.getId());
        assertEquals("Wrong name loaded", "Intervention", actual.getName());
    }

    public void testGetByName() throws Exception {
        ActivityType actual = getDao().getByName("Disease Measure");
        assertNotNull("Nothing found", actual);
        assertEquals("Wrong type returned", new Integer(-20), actual.getId());
    }

    public void testGetByNameIgnoringCaseWhenExact() throws Exception {
        ActivityType actual = getDao().getByNameIgnoringCase("Disease Measure");
        assertNotNull("Nothing found", actual);
        assertEquals("Wrong type returned", new Integer(-20), actual.getId());
    }

    public void testGetByNameIgnoringCaseWithDifferentCase() throws Exception {
        ActivityType actual = getDao().getByNameIgnoringCase("INTERventION");
        assertNotNull("Nothing found", actual);
        assertEquals("Wrong type returned", new Integer(-10), actual.getId());
    }

    public void testGetByNameIgnoringCaseWithNoMatch() throws Exception {
        ActivityType actual = getDao().getByNameIgnoringCase("Zappo");
        assertNull("Nothing should have been found: " + actual, actual);
    }

    public void testGetByNameIgnoringCaseWithSubstring() throws Exception {
        ActivityType actual = getDao().getByNameIgnoringCase("Disease");
        assertNull("Nothing should have been found: " + actual, actual);
    }
}
 