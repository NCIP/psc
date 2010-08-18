package edu.northwestern.bioinformatics.studycalendar.web.template;

import edu.northwestern.bioinformatics.studycalendar.core.StudyCalendarTestCase;
import edu.northwestern.bioinformatics.studycalendar.core.accesscontrol.AuthorizationScopeMappings;
import edu.northwestern.bioinformatics.studycalendar.dao.SiteDao;
import edu.northwestern.bioinformatics.studycalendar.domain.Site;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.security.authorization.AuthorizationObjectFactory;
import edu.northwestern.bioinformatics.studycalendar.security.authorization.PscRole;
import edu.northwestern.bioinformatics.studycalendar.security.authorization.PscUser;
import edu.northwestern.bioinformatics.studycalendar.service.StudyService;
import gov.nih.nci.cabig.ctms.suite.authorization.SuiteRoleMembership;
import org.apache.commons.lang.StringUtils;
import org.springframework.validation.BindException;
import java.util.*;
import static org.easymock.EasyMock.*;

import static edu.northwestern.bioinformatics.studycalendar.domain.Fixtures.createNamedInstance;
import static edu.northwestern.bioinformatics.studycalendar.domain.Fixtures.setId;


/**
 * @author Nataliya Shurupova
 */
public class ManagingSitesCommandTest extends StudyCalendarTestCase {
    private ManagingSitesCommand command;
    private Study study;
    private Set<Object> sites;
    private List<Site> sitesList;
    private Site site1, site2, site3, site4;
    private Set<Site> managingSites;
    private Boolean allSitesAccess;
    private StudyService studyService;
    private SiteDao siteDao;
    private List<SuiteRoleMembership> listOfRoles;
    private PscUser user;

    protected void setUp() throws Exception {
        super.setUp();
        study = setId(-22, createNamedInstance("NU123", Study.class));
        site1 = setId(11, createNamedInstance("NU", Site.class));
        site2 = setId(12, createNamedInstance("CMH", Site.class));
        site3 = setId(13, createNamedInstance("RUSH", Site.class));
        site4 = setId(14, createNamedInstance("Managing", Site.class));

        sites = new LinkedHashSet<Object>();
        managingSites = new HashSet<Site>();
        sites.add(site1);
        sites.add(site2);
        sites.add(site3);
        sitesList = new ArrayList<Site>();
        sitesList.add(site1);
        sitesList.add(site2);
        sitesList.add(site3);
        allSitesAccess = false;
        studyService = registerMockFor(StudyService.class);
        siteDao = registerDaoMockFor(SiteDao.class);
        user = AuthorizationObjectFactory.createPscUser("jo",
            AuthorizationScopeMappings.createSuiteRoleMembership(PscRole.STUDY_QA_MANAGER).forAllSites(),
            AuthorizationScopeMappings.createSuiteRoleMembership(PscRole.STUDY_CALENDAR_TEMPLATE_BUILDER).forAllSites());
        listOfRoles = new ArrayList<SuiteRoleMembership>();
        listOfRoles.add(user.getMembership(PscRole.STUDY_CALENDAR_TEMPLATE_BUILDER));
        listOfRoles.add(user.getMembership(PscRole.STUDY_QA_MANAGER));
    }

    public void testValidateWithNoSiteSelected() {
        expect(siteDao.getAll()).andReturn(sitesList).anyTimes();
        replayMocks();
        command = new ManagingSitesCommand(study, studyService, siteDao, listOfRoles);
        command.setAllSitesAccess(false);
        BindException errors = new BindException(command, StringUtils.EMPTY);
        command.validate(errors);
        verifyMocks();
        assertEquals("Wrong error count", 1, errors.getErrorCount());
    }

    public void testValidateWithAvailableSiteSelected() {
        managingSites.add(site2);
        expect(siteDao.getAll()).andReturn(sitesList).anyTimes();

        replayMocks();

        command = new ManagingSitesCommand(study, studyService, siteDao, listOfRoles);
        BindException errors = new BindException(command, StringUtils.EMPTY);
        command.validate(errors);

        verifyMocks();
        assertEquals("Wrong error count", 0, errors.getErrorCount());
    }

    public void testRemoveSiteFromStudy() {
        managingSites.add(site1);
        managingSites.add(site2);
        managingSites.add(site3);
        managingSites.add(site4);


        study.addManagingSite(site1);
        study.addManagingSite(site2);
        study.addManagingSite(site4);

        sitesList = new ArrayList();
        sitesList.addAll(managingSites);
        expect(siteDao.getAll()).andReturn(sitesList).anyTimes();
        studyService.save(study);

        replayMocks();
            command = new ManagingSitesCommand(study, studyService, siteDao, listOfRoles);
            Set<Object> before = command.getSelectableSites();
            command.apply();
            Set<Site> after = study.getManagingSites();
        verifyMocks();

        assertEquals("None sites is removed", before.size()-1, after.size());
        assertFalse("Site is not removed", after.contains(site3));
    }

    public void testAddSiteToStudy() {
        managingSites.add(site1);
        managingSites.add(site2);
        managingSites.add(site4);

        sites.add(site1);
        study.addManagingSite(site1);
        study.addManagingSite(site2);
        study.addManagingSite(site3);
        study.addManagingSite(site4);

        expect(siteDao.getAll()).andReturn(sitesList).anyTimes();
        studyService.save(study);

        replayMocks();
            command = new ManagingSitesCommand(study, studyService, siteDao, listOfRoles);
            Set<Object> before = command.getSelectableSites();
            command.apply();
            Set<Site> after = study.getManagingSites();
        verifyMocks();

        assertEquals("None sites is removed", before.size()+1, after.size());
        assertFalse("Site was in the list before", !before.contains(site2));
        assertTrue("Site is not added", after.contains(site2));
    }
}