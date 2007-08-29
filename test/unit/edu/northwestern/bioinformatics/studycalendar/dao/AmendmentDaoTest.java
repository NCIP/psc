package edu.northwestern.bioinformatics.studycalendar.dao;

import edu.northwestern.bioinformatics.studycalendar.testing.DaoTestCase;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Amendment;
import edu.northwestern.bioinformatics.studycalendar.dao.delta.AmendmentDao;

import java.util.List;

public class AmendmentDaoTest extends DaoTestCase {
    private AmendmentDao amendmentDao = (AmendmentDao) getApplicationContext().getBean("amendmentDao");

    public void testGetByAmendmentNumber() throws Exception {
        Amendment actual = amendmentDao.getById(-100);
        assertNotNull("AmendmentLogin not found", actual);
        assertEquals("Wrong name", "abc", actual.getName());
        assertEquals("Wrong studyId ", -11, (int) actual.getStudyId());
        assertEquals("Wrong date ", "02/2006", actual.getDate());
    }

    public void testSave() throws Exception {
        Amendment amendmentLogin = new Amendment();
        amendmentLogin.setStudyId(-3);
        amendmentLogin.setName("new name");
        amendmentLogin.setDate("02/2008");
        List<Amendment> listBeforeAdding = amendmentDao.getAll();
        amendmentDao.save(amendmentLogin);
        List<Amendment> listAfterAdding = amendmentDao.getAll();
        assertEquals("Amendment wasn't added ", listBeforeAdding.size()+1 , listAfterAdding.size());
    }

    public void testGetByStudyId() throws Exception {
        Amendment actual = amendmentDao.getByStudyId(-22);
        assertNotNull("AmendmentLogin not found", actual);
        assertEquals("Wrong id", -200, (int) actual.getId());
        assertEquals("Wrong amendment number ", "def", actual.getName());
        assertEquals("Wrong date ", "05/2008", actual.getDate());
    }

    public void testGetAll() throws Exception {
        List<Amendment> listBeforeAdding = amendmentDao.getAll();
        assertEquals("Amendment wasn't added ", 2 , listBeforeAdding.size());
    }

}