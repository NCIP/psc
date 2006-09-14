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
        Study study = dao.getById(-100);
        assertNotNull("Study 1 not found", study);
        assertEquals("Wrong name", "First Study", study.getName());
    }

    public void testGetAll() throws Exception {
        List<Study> actual = dao.getAll();
        assertEquals(1, actual.size());
        assertEquals("Wrong study found", -100, (int) actual.get(0).getId());
    }

    public void testSaveNewStudy() throws Exception {
        Integer savedId;
        {
            Study study = new Study();
            study.setName("New study");
            dao.save(study);
            savedId = study.getId();
            assertNotNull("The saved study didn't get an id", savedId);
        }

        interruptSession();

        {
            Study loaded = dao.getById(savedId);
            assertNotNull("Could not reload study with id " + savedId, loaded);
            assertEquals("Wrong name", "New study", loaded.getName());
        }
    }
}
