/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.io/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.web.template;

import edu.northwestern.bioinformatics.studycalendar.security.authorization.AuthorizationScopeMappings;
import edu.northwestern.bioinformatics.studycalendar.core.accesscontrol.SecurityContextHolderTestHelper;
import edu.northwestern.bioinformatics.studycalendar.dao.SiteDao;
import edu.northwestern.bioinformatics.studycalendar.dao.StudyDao;
import edu.northwestern.bioinformatics.studycalendar.domain.*;
import edu.northwestern.bioinformatics.studycalendar.security.authorization.PscRole;
import edu.northwestern.bioinformatics.studycalendar.security.authorization.PscUser;
import edu.northwestern.bioinformatics.studycalendar.web.ControllerTestCase;
import edu.northwestern.bioinformatics.studycalendar.web.accesscontrol.ResourceAuthorization;
import gov.nih.nci.cabig.ctms.suite.authorization.SuiteRole;
import gov.nih.nci.cabig.ctms.suite.authorization.SuiteRoleMembership;
import java.util.*;
import static edu.northwestern.bioinformatics.studycalendar.domain.Fixtures.createNamedInstance;
import static edu.northwestern.bioinformatics.studycalendar.domain.Fixtures.setId;
import static edu.northwestern.bioinformatics.studycalendar.security.authorization.PscRole.BUSINESS_ADMINISTRATOR;
import static edu.northwestern.bioinformatics.studycalendar.security.authorization.PscRole.STUDY_CALENDAR_TEMPLATE_BUILDER;
import static edu.northwestern.bioinformatics.studycalendar.security.authorization.PscRole.STUDY_QA_MANAGER;
import static org.easymock.EasyMock.expect;

/**
 * @author Nataliya Shurupova
 */
public class ManagingSitesControllerTest extends ControllerTestCase {
    private ManagingSitesController controller;
    private StudyDao studyDao;
    private SiteDao siteDao;
    private Study study;
    private PscUser user;
    private Site site1, site2;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        study = setId(-22, createNamedInstance("NU123", Study.class));
        site1 = setId(11, createNamedInstance("NU", Site.class));
        site2 = setId(12, createNamedInstance("CMH", Site.class));

        controller = new ManagingSitesController();
        studyDao = registerDaoMockFor(StudyDao.class);
        siteDao = registerDaoMockFor(SiteDao.class);
        controller.setStudyDao(studyDao);
        controller.setSiteDao(siteDao);
        controller.setApplicationSecurityManager(applicationSecurityManager);

        user = setCurrentUser(STUDY_QA_MANAGER, STUDY_CALENDAR_TEMPLATE_BUILDER);
        user.getMemberships().get(SuiteRole.STUDY_QA_MANAGER).forSites(site1);
        user.getMemberships().get(SuiteRole.STUDY_CALENDAR_TEMPLATE_BUILDER).forSites(site2);

        SecurityContextHolderTestHelper.setSecurityContext(user, "pass");

    }

    public void testAuthorizedRoles() {
        Map<String, String[]> queryParameters = new HashMap<String, String[]>();
        String[] ids = {study.getId().toString()};
        queryParameters.put("id", ids);
        expect(studyDao.getById(study.getId())).andReturn(study).anyTimes();
        replayMocks();
            Collection<ResourceAuthorization> actualAuthorizations = controller.authorizations(null, queryParameters);

        assertRolesAllowed(actualAuthorizations, STUDY_QA_MANAGER, STUDY_CALENDAR_TEMPLATE_BUILDER);
        assertNotContains("BUSINESS_ADMINISTRATOR is not allowed ", actualAuthorizations, ResourceAuthorization.create(BUSINESS_ADMINISTRATOR));
    }

    public void testCommand() throws Exception {
        request.setParameter("id", study.getId().toString());
        expect(studyDao.getById(study.getId())).andReturn(study);
        SuiteRoleMembership membershipForStudyQAManager = user.getMembership(PscRole.STUDY_QA_MANAGER);
        SuiteRoleMembership membershipForCalendarTemplateBuilders = user.getMembership(PscRole.STUDY_CALENDAR_TEMPLATE_BUILDER);

        List<Site> sites = new ArrayList<Site>();
        sites.add(site1);
        sites.add(site2);
        expect(siteDao.getByAssignedIdentifiers(membershipForCalendarTemplateBuilders.getSiteIdentifiers())).andReturn(sites);
        expect(siteDao.getByAssignedIdentifiers(membershipForStudyQAManager.getSiteIdentifiers())).andReturn(sites);
        replayMocks();
            Object actual = controller.formBackingObject(request);
        verifyMocks();
        assertTrue(actual instanceof ManagingSitesCommand);
        ManagingSitesCommand command = (ManagingSitesCommand)actual;
        assertTrue("Command's grid is not populated", command.getUserSitesToManageGrid().size() ==2);
    }

    //helper
    private PscUser setCurrentUser(PscRole... desiredRoles) {
        gov.nih.nci.security.authorization.domainobjects.User csmUser = new gov.nih.nci.security.authorization.domainobjects.User();
        csmUser.setLoginName("josephine");
        Map<SuiteRole, SuiteRoleMembership> memberships = new HashMap<SuiteRole, SuiteRoleMembership>();
        for (PscRole desiredRole : desiredRoles) {
            memberships.put(desiredRole.getSuiteRole(),
                new SuiteRoleMembership(desiredRole.getSuiteRole(),
                    AuthorizationScopeMappings.SITE_MAPPING, AuthorizationScopeMappings.STUDY_MAPPING));
        }
        user = new PscUser(csmUser, memberships);

        return user;
    }
}