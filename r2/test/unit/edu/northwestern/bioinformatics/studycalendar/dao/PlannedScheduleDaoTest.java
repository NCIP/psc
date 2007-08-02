package edu.northwestern.bioinformatics.studycalendar.dao;

import edu.northwestern.bioinformatics.studycalendar.domain.PlannedSchedule;
import edu.northwestern.bioinformatics.studycalendar.domain.Epoch;
import edu.northwestern.bioinformatics.studycalendar.testing.DaoTestCase;

/**
 * @author Rhett Sutphin
 */
public class PlannedScheduleDaoTest extends DaoTestCase {
    private PlannedScheduleDao dao = (PlannedScheduleDao) getApplicationContext().getBean("plannedScheduleDao");
    private StudyDao studyDao = (StudyDao) getApplicationContext().getBean("studyDao");

    public void testGetById() throws Exception {
        PlannedSchedule sched = dao.getById(-100);
        assertNotNull("Schedule not found", sched);
        assertEquals("Wrong study", Integer.valueOf(-150), sched.getStudy().getId());
    }

    public void testLoadingEpochs() throws Exception {
        PlannedSchedule schedule = dao.getById(-100);
        assertNotNull("Schedule not found", schedule);

        assertEquals("Wrong number of epochs", 2, schedule.getEpochs().size());
        assertEpoch("Wrong epoch 0", -200, "Dexter", schedule.getEpochs().get(0));
        assertEpoch("Wrong epoch 1", -199, "Sinister", schedule.getEpochs().get(1));

        assertSame("Epoch <=> Schedule relationship not bidirectional on load", schedule, schedule.getEpochs().get(0).getPlannedSchedule());
    }

    public void testSaveNewSchedule() throws Exception {
        Integer savedId;
        {
            PlannedSchedule sched = new PlannedSchedule();
            sched.setStudy(studyDao.getById(-150));
            sched.addEpoch(new Epoch());
            sched.getEpochs().get(0).setName("First epoch");
            dao.save(sched);
            savedId = sched.getId();
            assertNotNull("The saved sched didn't get an id", savedId);
        }

        interruptSession();

        {
            PlannedSchedule loaded = dao.getById(savedId);
            assertNotNull("Could not reload study with id " + savedId, loaded);
            assertEquals("Wrong number of arms", 1, loaded.getEpochs().size());
            assertEquals("Wrong name for arm 0", "First epoch", loaded.getEpochs().get(0).getName());
        }
    }

    public void testScheduleCompleted() throws Exception {
        PlannedSchedule sched = dao.getById(-100);
        // TODO: why is this in the DAO test?
        sched.setComplete(true);
        assertEquals("Could not mark sched complete", true, sched.isComplete());
    }

    private static void assertEpoch(
        String message, Integer expectedId, String expectedName, Epoch actualEpoch
    ) {
        assertEquals(message + ": wrong id", expectedId, actualEpoch.getId());
        assertEquals(message + ": wrong name", expectedName, actualEpoch.getName());
    }
}
