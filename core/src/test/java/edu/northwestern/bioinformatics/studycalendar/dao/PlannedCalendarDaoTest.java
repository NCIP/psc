/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.com/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.dao;

import edu.northwestern.bioinformatics.studycalendar.domain.PlannedCalendar;
import edu.northwestern.bioinformatics.studycalendar.domain.Epoch;
import edu.northwestern.bioinformatics.studycalendar.domain.Duration;
import edu.northwestern.bioinformatics.studycalendar.domain.Period;
import edu.northwestern.bioinformatics.studycalendar.core.DaoTestCase;

/**
 * @author Rhett Sutphin
 */
public class PlannedCalendarDaoTest extends DaoTestCase {
    private PlannedCalendarDao dao = (PlannedCalendarDao) getApplicationContext().getBean("plannedCalendarDao");
    private StudyDao studyDao = (StudyDao) getApplicationContext().getBean("studyDao");

    public void testGetById() throws Exception {
        PlannedCalendar sched = dao.getById(-100);
        assertNotNull("Schedule not found", sched);
        assertEquals("Wrong study", Integer.valueOf(-150), sched.getStudy().getId());
    }

    public void testLoadingEpochs() throws Exception {
        PlannedCalendar calendar = dao.getById(-100);
        assertNotNull("Schedule not found", calendar);

        assertEquals("Wrong number of epochs", 3, calendar.getEpochs().size());
        assertEpoch("Wrong epoch 0", -200, "Dexter", calendar.getEpochs().get(0));
        assertEpoch("Wrong epoch 1", -201, "Gripping", calendar.getEpochs().get(1));
        assertEpoch("Wrong epoch 2", -199, "Sinister", calendar.getEpochs().get(2));

        assertSame("Epoch <=> Schedule relationship not bidirectional on load", calendar, calendar.getEpochs().get(0).getPlannedCalendar());
    }

    public void testSaveNewSchedule() throws Exception {
        Integer savedId;
        {
            PlannedCalendar sched = new PlannedCalendar();
            sched.setStudy(studyDao.getById(-150));
            sched.addEpoch(new Epoch());
            sched.getEpochs().get(0).setName("First epoch");
            dao.save(sched);
            savedId = sched.getId();
            assertNotNull("The saved sched didn't get an id", savedId);
        }

        interruptSession();

        {
            PlannedCalendar loaded = dao.getById(savedId);
            assertNotNull("Could not reload study with id " + savedId, loaded);
            assertEquals("Wrong number of study segments", 1, loaded.getEpochs().size());
            assertEquals("Wrong name for study segment 0", "First epoch", loaded.getEpochs().get(0).getName());
        }
    }

    private static void assertEpoch(
        String message, Integer expectedId, String expectedName, Epoch actualEpoch
    ) {
        assertEquals(message + ": wrong id", expectedId, actualEpoch.getId());
        assertEquals(message + ": wrong name", expectedName, actualEpoch.getName());
    }

    public void testInitializeMainStructure() throws Exception {
        PlannedCalendar plannedCalendar = assertGetAndInitialize();

        assertEquals("Wrong number of epochs", 2, plannedCalendar.getEpochs().size());

        Epoch e0 = plannedCalendar.getEpochs().get(0);
        assertEquals("Treatment", e0.getName());
        assertEquals("Wrong number of arms in first epoch", 2, e0.getStudySegments().size());
        assertEquals("A", e0.getStudySegments().get(0).getName());
        assertEquals("B", e0.getStudySegments().get(1).getName());

        Epoch e1 = plannedCalendar.getEpochs().get(1);
        assertEquals("Follow up", e1.getName());
        assertEquals("Wrong number of study segments in second epoch", 1, e1.getStudySegments().size());
        assertEquals("Follow up", e1.getStudySegments().get(0).getName());
    }

    public void testPeriodsInitialzed() throws Exception {
        PlannedCalendar calendar = assertGetAndInitialize();

        assertPeriod( 7, Duration.Unit.day, 4, calendar.getEpochs().get(0).getStudySegments().get(0).getPeriods().first());
        assertPeriod(14, Duration.Unit.day, 4, calendar.getEpochs().get(0).getStudySegments().get(1).getPeriods().first());
        assertPeriod(10, Duration.Unit.day, 1, calendar.getEpochs().get(1).getStudySegments().get(0).getPeriods().first());
    }

    private void assertPeriod(int expectedDurationQuantity, Duration.Unit expectedDurationUnit, int expectedRepetitions, Period actual) {
        assertEquals("Wrong quantity", expectedDurationQuantity, (int) actual.getDuration().getQuantity());
        assertEquals("Wrong unit", expectedDurationUnit, actual.getDuration().getUnit());
        assertEquals("Wrong reps", expectedRepetitions, actual.getRepetitions());
    }

    public void testPlannedActivitiesInitialized() throws Exception {
        PlannedCalendar calendar = assertGetAndInitialize();

        assertEquals(3, (int) calendar.getEpochs().get(0).getStudySegments().get(0).getPeriods().first().getPlannedActivities().get(0).getDay());
        assertEquals(3, (int) calendar.getEpochs().get(0).getStudySegments().get(1).getPeriods().first().getPlannedActivities().get(0).getDay());
        assertEquals(8, (int) calendar.getEpochs().get(0).getStudySegments().get(1).getPeriods().first().getPlannedActivities().get(1).getDay());
        assertEquals(1, (int) calendar.getEpochs().get(1).getStudySegments().get(0).getPeriods().first().getPlannedActivities().get(0).getDay());
    }

    public void testActivitiesInitialized() throws Exception {
        PlannedCalendar calendar = assertGetAndInitialize();

        assertEquals("Activity 1", calendar.getEpochs().get(0).getStudySegments().get(0).getPeriods().first().getPlannedActivities().get(0).getActivity().getName());
        assertEquals("Activity 1", calendar.getEpochs().get(0).getStudySegments().get(1).getPeriods().first().getPlannedActivities().get(0).getActivity().getName());
        assertEquals("Activity 2", calendar.getEpochs().get(0).getStudySegments().get(1).getPeriods().first().getPlannedActivities().get(1).getActivity().getName());
        assertEquals("Activity 1", calendar.getEpochs().get(1).getStudySegments().get(0).getPeriods().first().getPlannedActivities().get(0).getActivity().getName());
    }

    private PlannedCalendar assertGetAndInitialize() {
        PlannedCalendar plannedCalendar = dao.getById(-10);
        assertNotNull("PC not found", plannedCalendar);
        dao.initialize(plannedCalendar);
        interruptSession();

        return plannedCalendar;
    }

}
