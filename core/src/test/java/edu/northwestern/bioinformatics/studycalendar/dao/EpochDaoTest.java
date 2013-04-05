/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.io/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.dao;

import edu.northwestern.bioinformatics.studycalendar.domain.*;

/**
 * @author Rhett Sutphin
 */
public class EpochDaoTest extends ContextDaoTestCase<EpochDao> {
    private EpochDao epochDao = getDao();

    public void testGetById() throws Exception {
        Epoch loaded = getDao().getById(-1);
        assertEquals("Wrong name", "Treatment", loaded.getName());
        assertEquals("Wrong planned calendar", -2, (int) loaded.getPlannedCalendar().getId());
        assertEquals("Wrong number of study segments", 3, loaded.getStudySegments().size());
        assertEquals("Wrong 0th study segment", "A", loaded.getStudySegments().get(0).getName());
        assertEquals("Wrong 1st study segment", "B", loaded.getStudySegments().get(1).getName());
        assertEquals("Wrong 2nd study segment", "C", loaded.getStudySegments().get(2).getName());
    }

    public void testDeleteJustPlainOrphans() throws Exception {
        Epoch e = epochDao.getById(-12);
        assertNotNull(e);
        assertTrue("Epoch is attached ", e.isDetached());
        assertNull("Epoch has a parent ", e.getParent());
        epochDao.deleteOrphans();
        assertNull(epochDao.getById(-12));
    }

    public void testDeleteEpochWithParent() throws Exception {
        Epoch e = epochDao.getById(-1);
        assertNotNull(e);
        assertFalse("Epoch is attached ", e.isDetached());
        assertNotNull("Epoch doesnot have a parent ", e.getParent());
        epochDao.deleteOrphans();
        assertNotNull(epochDao.getById(-1));
    }

    public void testDeleteEpochWithParentAndChangeAdd() throws Exception {
        Epoch e = epochDao.getById(-18);
        assertFalse("Epoch is deattached ", e.isDetached());
        assertNotNull("Epoch has a parent ", e.getParent());
        epochDao.deleteOrphans();
        assertNotNull(epochDao.getById(-18));
    }

    public void testToDeleteEpochWithAddOnly() throws Exception {
        Epoch e = epochDao.getById(-199);
        assertTrue("Epoch is deattached ", e.isDetached());
        assertNull("Epoch has a parent ", e.getParent());
        epochDao.deleteOrphans();
        assertNotNull(epochDao.getById(-199));

    }

    public void testToDeleteEpochWithRemoveOnly() throws Exception {
        Epoch e = epochDao.getById(-20);
        assertTrue("Epoch is deattached ", e.isDetached());
        assertNull("Epoch has a parent ", e.getParent());
        epochDao.deleteOrphans();
        assertNotNull(epochDao.getById(-20));

    }

}
