package edu.northwestern.bioinformatics.studycalendar.dao;

import edu.northwestern.bioinformatics.studycalendar.testing.DaoTestCase;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;

/**
 * @author Rhett Sutphin
 */
public class StudyDaoTest extends DaoTestCase {
    private StudyDao dao = (StudyDao) getApplicationContext().getBean("studyDao");

    public void testGetById() throws Exception {
        Study study = dao.getById(1);
        assertNotNull("Study 1 not found", study);
        assertEquals("Wrong name", "First Study", study.getName());
    }
}
