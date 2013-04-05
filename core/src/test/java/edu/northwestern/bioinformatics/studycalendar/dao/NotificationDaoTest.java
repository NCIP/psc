/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.io/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.dao;

import edu.northwestern.bioinformatics.studycalendar.domain.Notification;

/**
 * @author Saurabh Agrawal
 */
public class NotificationDaoTest extends ContextDaoTestCase<NotificationDao> {
    protected String getTestDataFileName() {
        return "testdata/StudySubjectAssignmentDaoTest.xml";
    }

    public void testGet() throws Exception {
        Notification loaded = getDao().getById(-30);
        assertNotNull(loaded);
        assertEquals("Wrong AE", "Apocalyptic",  loaded.getMessage());
        assertTrue("Is dismissed", loaded.isDismissed());
        
    }

    public void testSave() throws Exception {
        {
            Notification loaded = getDao().getById(-30);
            assertTrue(loaded.isDismissed());
            loaded.setDismissed(false);
            getDao().save(loaded);
        }

        interruptSession();

        Notification reloaded = getDao().getById(-30);
        assertFalse(reloaded.isDismissed());
    }
}

