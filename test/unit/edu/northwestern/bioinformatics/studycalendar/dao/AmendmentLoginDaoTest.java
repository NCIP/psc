package edu.northwestern.bioinformatics.studycalendar.dao;

import edu.northwestern.bioinformatics.studycalendar.testing.DaoTestCase;
import edu.northwestern.bioinformatics.studycalendar.domain.AmendmentLogin;

import java.util.List;

/**
 * @author Rhett Sutphin
 */
public class AmendmentLoginDaoTest extends DaoTestCase {
    private AmendmentLoginDao amendmentLoginDao = (AmendmentLoginDao) getApplicationContext().getBean("amendmentLoginDao");

    public void testGetByAmendmentNumber() throws Exception {
        AmendmentLogin actual = amendmentLoginDao.getByAmendmentNumber(-4);
        assertNotNull("AmendmentLogin not found", actual);
        assertEquals("Wrong id", -4, (int) actual.getAmendmentNumber());
        assertEquals("Wrong studyId ", -11, (int) actual.getStudyId());
        assertEquals("Wrong date ", "02/2006", actual.getDate());
    }

    public void testSave() throws Exception {
        AmendmentLogin amendmentLogin = new AmendmentLogin();
        amendmentLogin.setStudyId(-3);
        amendmentLogin.setAmendmentNumber(-6);
        amendmentLogin.setDate("02/2008");
        List<AmendmentLogin> listBeforeAdding = amendmentLoginDao.getAll();
        amendmentLoginDao.save(amendmentLogin);
        List<AmendmentLogin> listAfterAdding = amendmentLoginDao.getAll();
        assertEquals("Amendment wasn't added ", listBeforeAdding.size()+1 , listAfterAdding.size());
    }

    public void testGetByStudyId() throws Exception {
        AmendmentLogin actual = amendmentLoginDao.getByStudyId(-22);
        assertNotNull("AmendmentLogin not found", actual);
        assertEquals("Wrong id", -200, (int) actual.getId());
        assertEquals("Wrong amendment number ", -5, (int)actual.getAmendmentNumber());
        assertEquals("Wrong date ", "05/2008", actual.getDate());
    }

    public void testGetAll() throws Exception {
        List<AmendmentLogin> listBeforeAdding = amendmentLoginDao.getAll();
        assertEquals("Amendment wasn't added ", 2 , listBeforeAdding.size());
    }

}