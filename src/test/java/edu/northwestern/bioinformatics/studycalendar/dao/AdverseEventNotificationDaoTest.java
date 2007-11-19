package edu.northwestern.bioinformatics.studycalendar.dao;

import edu.northwestern.bioinformatics.studycalendar.domain.AdverseEventNotification;

/**
 * @author Rhett Sutphin
 */
public class AdverseEventNotificationDaoTest extends ContextDaoTestCase<AdverseEventNotificationDao> {
    protected String getTestDataFileName() {
        return "testdata/StudySubjectAssignmentDaoTest.xml";
    }

    public void testGet() throws Exception {
        AdverseEventNotification loaded = getDao().getById(-30);
        assertNotNull(loaded);
        assertEquals("Wrong AE", -29, (int) loaded.getAdverseEvent().getId());
        assertTrue("Is dismissed", loaded.isDismissed());
    }

    public void testSave() throws Exception {
        {
            AdverseEventNotification loaded = getDao().getById(-30);
            assertTrue(loaded.isDismissed());
            loaded.setDismissed(false);
            getDao().save(loaded);
        }

        interruptSession();

        AdverseEventNotification reloaded = getDao().getById(-30);
        assertFalse(reloaded.isDismissed());
    }
}
