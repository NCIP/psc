package edu.northwestern.bioinformatics.studycalendar.dao;

import edu.northwestern.bioinformatics.studycalendar.core.DaoTestCase;
import edu.northwestern.bioinformatics.studycalendar.domain.Site;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import gov.nih.nci.security.dao.AuthorizationDAO;

/**
 * This is testing both that
 * {@link AuthorizationScopeUpdaterListener}
 * works and that it is configured into the application's session factory.
 *
 * @author Rhett Sutphin
 */
public class AuthorizationScopeUpdaterListenerIntegratedTest extends DaoTestCase {
    private SiteDao siteDao;
    private StudyDao studyDao;
    private AuthorizationDAO authorizationDao;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        siteDao = (SiteDao) getApplicationContext().getBean("siteDao");
        studyDao = (StudyDao) getApplicationContext().getBean("studyDao");
        authorizationDao =
            (AuthorizationDAO) getApplicationContext().getBean("csmAuthorizationDao");
    }

    public void testStudyIdentUpdateUpdatesPGPE() throws Exception {
        Study loaded = studyDao.getById(90);
        assertNotNull("Test setup failure", loaded);
        loaded.setAssignedIdentifier("GE 1701");

        interruptSession();

        // the details are tested separately in ctms-commons
        assertNotNull(authorizationDao.getProtectionElement("Study.GE 1701"));
        assertNotNull(authorizationDao.getProtectionGroup("Study.GE 1701"));
    }

    public void testSiteIdentUpdateUpdatesPGPE() throws Exception {
        Site loaded = siteDao.getById(75);
        assertNotNull("Test setup failure", loaded);
        loaded.setAssignedIdentifier("IL063");

        interruptSession();

        // the details are tested separately in ctms-commons
        assertNotNull(authorizationDao.getProtectionElement("HealthcareSite.IL063"));
        assertNotNull(authorizationDao.getProtectionGroup("HealthcareSite.IL063"));
    }
}