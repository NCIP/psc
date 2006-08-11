package edu.northwestern.bioinformatics.studycalendar.dao;

import edu.northwestern.bioinformatics.studycalendar.testing.DaoTestCase;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.domain.Arm;

import java.util.List;

/**
 * @author Rhett Sutphin
 */
public class StudyDaoTest extends DaoTestCase {
    private StudyDao dao = (StudyDao) getApplicationContext().getBean("studyDao");

    public void testGetById() throws Exception {
        Study study = dao.getById(100);
        assertNotNull("Study 1 not found", study);
        assertEquals("Wrong name", "First Study", study.getName());
    }

    public void testLoadingArms() throws Exception {
        Study study = dao.getById(100);
        assertNotNull("Study 1 not found", study);

        assertEquals("Wrong number of arms", 2, study.getArms().size());
        assertArm("Wrong arm 0", 200, "Dexter", study.getArms().get(0));
        assertArm("Wrong arm 1", 201, "Sinister", study.getArms().get(1));

        assertSame("Arm <=> Study relationship not bidirectional on load", study, study.getArms().get(0).getStudy());
    }

    public void testGetAll() throws Exception {
        List<Study> actual = dao.getAll();
        assertEquals(1, actual.size());
        assertEquals("Wrong study found", 100, (int) actual.get(0).getId());
    }

    public void testSaveNewStudy() throws Exception {
        Integer savedId;
        {
            Study study = new Study();
            study.setName("New study");
            study.addArm(new Arm());
            study.getArms().get(0).setName("First arm");
            dao.save(study);
            savedId = study.getId();
            assertNotNull("The saved study didn't get an id", savedId);
        }

        interruptSession();

        {
            Study loaded = dao.getById(savedId);
            assertNotNull("Could not reload study with id " + savedId, loaded);
            assertEquals("Wrong name", "New study", loaded.getName());
            assertEquals("Wrong number of arms", 1, loaded.getArms().size());
            assertEquals("Wrong name for arm 0", "First arm", loaded.getArms().get(0).getName());
        }
    }

    private static void assertArm(
        String message, Integer expectedId, String expectedName, Arm actualArm
    ) {
        assertEquals(message + ": wrong id", expectedId, actualArm.getId());
        assertEquals(message + ": wrong name", expectedName, actualArm.getName());
    }
}
