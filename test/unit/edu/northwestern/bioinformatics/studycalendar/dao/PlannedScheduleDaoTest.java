package edu.northwestern.bioinformatics.studycalendar.dao;

import edu.northwestern.bioinformatics.studycalendar.domain.PlannedSchedule;
import edu.northwestern.bioinformatics.studycalendar.domain.Arm;
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

    public void testLoadingArms() throws Exception {
        PlannedSchedule schedule = dao.getById(-100);
        assertNotNull("Schedule not found", schedule);

        assertEquals("Wrong number of arms", 2, schedule.getArms().size());
        assertArm("Wrong arm 0", -200, "Dexter", schedule.getArms().get(0));
        assertArm("Wrong arm 1", -199, "Sinister", schedule.getArms().get(1));

        assertSame("Arm <=> Study relationship not bidirectional on load", schedule, schedule.getArms().get(0).getPlannedSchedule());
    }

    public void testSaveNewSchedule() throws Exception {
        Integer savedId;
        {
            PlannedSchedule sched = new PlannedSchedule();
            sched.setStudy(studyDao.getById(-150));
            sched.addArm(new Arm());
            sched.getArms().get(0).setName("First arm");
            dao.save(sched);
            savedId = sched.getId();
            assertNotNull("The saved sched didn't get an id", savedId);
        }

        interruptSession();

        {
            PlannedSchedule loaded = dao.getById(savedId);
            assertNotNull("Could not reload study with id " + savedId, loaded);
            assertEquals("Wrong number of arms", 1, loaded.getArms().size());
            assertEquals("Wrong name for arm 0", "First arm", loaded.getArms().get(0).getName());
        }
    }

    public void testScheduleCompleted() throws Exception {
        PlannedSchedule sched = dao.getById(-100);
        // TODO: why is this in the DAO test?
        sched.setComplete(true);
        assertEquals("Could not mark sched complete", true, sched.isComplete());
    }

    private static void assertArm(
        String message, Integer expectedId, String expectedName, Arm actualArm
    ) {
        assertEquals(message + ": wrong id", expectedId, actualArm.getId());
        assertEquals(message + ": wrong name", expectedName, actualArm.getName());
    }
}
