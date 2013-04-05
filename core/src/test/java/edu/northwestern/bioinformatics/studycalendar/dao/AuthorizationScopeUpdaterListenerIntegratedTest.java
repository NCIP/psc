/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.io/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.dao;

import edu.northwestern.bioinformatics.studycalendar.core.DaoTestCase;
import edu.northwestern.bioinformatics.studycalendar.domain.Site;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import gov.nih.nci.security.AuthorizationManager;
import gov.nih.nci.security.authorization.domainobjects.ProtectionGroup;
import gov.nih.nci.security.dao.ProtectionGroupSearchCriteria;

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
    private AuthorizationManager authorizationManager;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        siteDao = (SiteDao) getApplicationContext().getBean("siteDao");
        studyDao = (StudyDao) getApplicationContext().getBean("studyDao");
        authorizationManager =
            (AuthorizationManager) getApplicationContext().getBean("osgiCsmAuthorizationManager");
    }

    public void testStudyIdentUpdateUpdatesPGPE() throws Exception {
        Study loaded = studyDao.getById(90);
        assertNotNull("Test setup failure", loaded);
        loaded.setAssignedIdentifier("GE 1701");

        interruptSession();

        // the details are tested separately in ctms-commons
        assertNotNull(authorizationManager.getProtectionElement("Study.GE 1701"));
        assertNotNull(getPGByName("Study.GE 1701"));
    }

    public void testSiteIdentUpdateUpdatesPGPE() throws Exception {
        Site loaded = siteDao.getById(75);
        assertNotNull("Test setup failure", loaded);
        loaded.setAssignedIdentifier("IL063");

        interruptSession();

        // the details are tested separately in ctms-commons
        assertNotNull(authorizationManager.getProtectionElement("HealthcareSite.IL063"));
        assertNotNull(getPGByName("HealthcareSite.IL063"));
    }

    private ProtectionGroup getPGByName(String name) {
        ProtectionGroup template = new ProtectionGroup();
        template.setProtectionGroupName(name);
        return (ProtectionGroup) authorizationManager.getObjects(new ProtectionGroupSearchCriteria(template)).get(0);
    }
}