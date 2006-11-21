package edu.northwestern.bioinformatics.studycalendar.dao;

import edu.nwu.bioinformatics.commons.testing.CoreTestCase;
import static edu.nwu.bioinformatics.commons.testing.CoreTestCase.*;

import edu.northwestern.bioinformatics.studycalendar.testing.DaoTestCase;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.domain.Arm;
import edu.northwestern.bioinformatics.studycalendar.domain.StudyParticipantAssignment;
import edu.northwestern.bioinformatics.studycalendar.domain.DomainObject;

import java.util.List;
import java.util.ArrayList;

/**
 * @author Rhett Sutphin
 */
public class StudyDaoTest extends DaoTestCase {
    private StudyDao dao = (StudyDao) getApplicationContext().getBean("studyDao");

    public void testGetById() throws Exception {
        Study study = dao.getById(-100);
        assertNotNull("Study 1 not found", study);
        assertEquals("Wrong name", "First Study", study.getName());
        assertEquals("Wrong grid ID", "long-GUID-string", study.getBigId());
    }

    public void testGetAll() throws Exception {
        List<Study> actual = dao.getAll();
        assertEquals(2, actual.size());
        List<Integer> ids = collectIds(actual);
        assertContains("Wrong study found", ids, -100);
        assertContains("Wrong study found", ids, -101);
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
            assertNotNull("Grid ID not automatically added", loaded.getBigId());
        }
    }
    
    public void testGetStudyParticipantAssigments() throws Exception {
        List<StudyParticipantAssignment> actual = dao.getAssignmentsForStudy(-100);
        assertEquals("Wrong number of assigments", 2, actual.size());
        List<Integer> ids = collectIds(actual);

        assertContains("Missing expected assignment", ids, -10);
        assertContains("Missing expected assignment", ids, -11);
    }

    private List<Integer> collectIds(List<? extends DomainObject> actual) {
        List<Integer> ids = new ArrayList<Integer>(actual.size());
        for (DomainObject object : actual) {
            ids.add(object.getId());
        }
        return ids;
    }
}
