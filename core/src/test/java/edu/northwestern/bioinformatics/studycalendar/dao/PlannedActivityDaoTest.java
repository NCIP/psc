/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.io/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.dao;

import edu.northwestern.bioinformatics.studycalendar.core.Fixtures;
import edu.northwestern.bioinformatics.studycalendar.domain.PlannedActivity;
import edu.northwestern.bioinformatics.studycalendar.domain.PlannedActivityLabel;

import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: nshurupova
 * Date: Nov 9, 2007
 * Time: 12:31:20 PM
 * To change this template use File | Settings | File Templates.
 */
public class PlannedActivityDaoTest extends ContextDaoTestCase<PlannedActivityDao> {
    public void testGetById() throws Exception {
        PlannedActivity loaded = getDao().getById(-12);

        assertEquals("Wrong id", -12, (int) loaded.getId());
        assertEquals("Wrong day number", new Integer(4), loaded.getDay());
        assertNotNull("Period not loaded", loaded.getPeriod());
        assertEquals("Wrong period", -300, (int) loaded.getPeriod().getId());
        assertNotNull("Activity not loaded", loaded.getActivity());
        assertEquals("Wrong activity", -200, (int) loaded.getActivity().getId());
        assertEquals("Wrong condition", "At least 37", loaded.getCondition());
        assertEquals("Wrong population", -45, (int) loaded.getPopulation().getId());
        assertEquals("Wrong number of labels", 1, loaded.getPlannedActivityLabels().size());
        assertEquals("Wrong label", -1200, (int) loaded.getPlannedActivityLabels().first().getId());
    }

    public void testGetPlannedActivitiesForAcivity() throws Exception {
        List<PlannedActivity> plannedActivities = getDao().getPlannedActivitiesForActivity(-200);
        assertNotNull(plannedActivities);
        assertTrue(plannedActivities.size() > 0);

        PlannedActivity loaded = plannedActivities.get(0);
        assertEquals("Wrong id", -12, (int) loaded.getId());
    }

    public void testPeriodBidirectional() throws Exception {
        PlannedActivity loaded = getDao().getById(-12);
        assertTrue(loaded.getPeriod().getPlannedActivities().contains(loaded));
    }

    public void testSaveDetached() throws Exception {
        Integer id;
        {
            PlannedActivity plannedActivity = new PlannedActivity();
            plannedActivity.setDay(5);
            plannedActivity.setActivity(getDao().getById(-12).getActivity());
            getDao().save(plannedActivity);
            assertNotNull("not saved", plannedActivity.getId());
            id = plannedActivity.getId();
        }

        interruptSession();

        PlannedActivity loaded = getDao().getById(id);
        assertNotNull("Could not reload", loaded);
        assertEquals("Wrong event loaded", 5, (int) loaded.getDay());
    }

    public void testSaveCascadesToLabels() throws Exception {
        int id;
        {
            PlannedActivity plannedActivity = new PlannedActivity();
            plannedActivity.setDay(5);
            plannedActivity.setActivity(getDao().getById(-12).getActivity());

            plannedActivity.addPlannedActivityLabel(Fixtures.createPlannedActivityLabel("bar"));
            plannedActivity.addPlannedActivityLabel(Fixtures.createPlannedActivityLabel("zam"));

            getDao().save(plannedActivity);
            assertNotNull("not saved", plannedActivity.getId());
            id = plannedActivity.getId();
        }

        interruptSession();

        PlannedActivity loaded = getDao().getById(id);
        assertNotNull("Could not reload", loaded);
        assertEquals("Wrong number of labels", 2, loaded.getPlannedActivityLabels().size());

        for (PlannedActivityLabel label : loaded.getPlannedActivityLabels()) {
            assertNotNull("Could not reload label with id " + label);
            assertNotNull("Grid ID not automatically added", label.getGridId());

        }
    }

    public void testSavedCloneIsReloadable() throws Exception {
        int id;
        {
            PlannedActivity loaded = getDao().getById(-12);
            PlannedActivity clone = loaded.clone();
            clone.clearIds();
            log.debug("Marking clone for save");
            getDao().save(clone);
            assertEquals("Test setup failure", 1, clone.getPlannedActivityLabels().size());
            id = clone.getId();
        }

        interruptSession();

        PlannedActivity reloaded = getDao().getById(id);
        assertNotNull("Could not reload", reloaded);
        assertEquals("Wrong number of labels", 1, reloaded.getPlannedActivityLabels().size());
    }

    public void testDeleteJustPlainOrphans() throws Exception {
        PlannedActivity pa = getDao().getById(-122);
        assertNotNull(pa);
        assertTrue("PlannedActivity is attached ", pa.isDetached());
        assertNull("PlannedActivity has a parent ", pa.getParent());
        getDao().deleteOrphans();
        assertNull(getDao().getById(-122));
    }

    public void testDeletePAWithParent() throws Exception {
        PlannedActivity pa = getDao().getById(-12);
        assertNotNull(pa);
        assertNotNull("PlannedActivity doesnot have a parent ", pa.getParent());
        getDao().deleteOrphans();
        assertNotNull(getDao().getById(-12));
    }

    public void testDeletePAWithParentAndChangeAdd() throws Exception {
        PlannedActivity pa = getDao().getById(-18);
        assertFalse("Epoch is deattached ", pa.isDetached());
        assertNotNull("Epoch has a parent ", pa.getParent());
        getDao().deleteOrphans();
        assertNotNull(getDao().getById(-18));
    }

    public void testToDeletePAWithAddOnly() throws Exception {
        PlannedActivity pa = getDao().getById(-189);
        assertTrue("PlannedActivity is deattached ", pa.isDetached());
        assertNull("PlannedActivity has a parent ", pa.getParent());
        getDao().deleteOrphans();
        assertNotNull(getDao().getById(-189));
    }

    public void testToDeletePAWithRemoveOnly() throws Exception {
        PlannedActivity pa = getDao().getById(-100);
        assertTrue("PlannedActivity is deattached ", pa.isDetached());
        assertNull("PlannedActivity has a parent ", pa.getParent());
        getDao().deleteOrphans();
        assertNotNull(getDao().getById(-100));

    }    

}
