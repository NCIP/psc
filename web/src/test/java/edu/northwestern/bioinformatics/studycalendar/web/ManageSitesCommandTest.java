package edu.northwestern.bioinformatics.studycalendar.web;

import edu.northwestern.bioinformatics.studycalendar.core.StudyCalendarTestCase;
import edu.northwestern.bioinformatics.studycalendar.core.accesscontrol.AuthorizationScopeMappings;
import edu.northwestern.bioinformatics.studycalendar.domain.Site;
import edu.northwestern.bioinformatics.studycalendar.security.authorization.AuthorizationObjectFactory;
import edu.northwestern.bioinformatics.studycalendar.security.authorization.PscUser;
import edu.northwestern.bioinformatics.studycalendar.service.SiteService;
import gov.nih.nci.cabig.ctms.suite.authorization.SuiteRole;
import gov.nih.nci.cabig.ctms.suite.authorization.SuiteRoleMembership;
import gov.nih.nci.security.authorization.domainobjects.User;

import java.util.*;

import static edu.northwestern.bioinformatics.studycalendar.domain.Fixtures.createSite;
import static edu.northwestern.bioinformatics.studycalendar.security.authorization.AuthorizationObjectFactory.createPscUser;
import static java.util.Arrays.asList;
import static org.easymock.EasyMock.expect;

public class ManageSitesCommandTest extends StudyCalendarTestCase {
    private SiteService siteService;
    private List<Site> sites;
    private Site nu;
    private Site mayo;

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        siteService = registerMockFor(SiteService.class);

        nu = createSite("NU");
        mayo = createSite("Mayo");

        sites = asList(nu, mayo);
    }

    public void testManageableSitesForAllSites() {
        PscUser user = createPscUser(
            "jo", createMembership(SuiteRole.PERSON_AND_ORGANIZATION_INFORMATION_MANAGER).forAllSites()
        );

        expect(siteService.getAll()).andReturn(sites);

        replayMocks();
        List<Site> actual = command(user).getManageableSites();

        verifyMocks();
        assertEquals("Wrong number of sites", 2, actual.size());
        assertContains("Should contain all sites", sites, actual);
    }

    public void testManageableSitesForOneSite() {
        PscUser user = createPscUser(
            "jo", createMembership(SuiteRole.PERSON_AND_ORGANIZATION_INFORMATION_MANAGER).forSites(nu)
        );

        replayMocks();
        List<Site> actual = command(user).getManageableSites();

        verifyMocks();
        assertEquals("Wrong number of sites", 1, actual.size());
        assertEquals("Should only contain NU", nu, actual.get(0));
    }

    public void testManageableSitesForNoRole() {
        PscUser user = createPscUser("jo");

        replayMocks();
        List<Site> actual = command(user).getManageableSites();

        verifyMocks();
        assertEquals("Wrong number of sites", 0, actual.size());
    }

    public void testSiteCreationEnabled() {
        PscUser user = createPscUser(
            "jo", createMembership(SuiteRole.PERSON_AND_ORGANIZATION_INFORMATION_MANAGER).forAllSites()
        );

        replayMocks();
        boolean actual = command(user).isSiteCreationEnabled();

        verifyMocks();
        assertTrue("should be allowed", actual);
    }
    
    public void testSiteCreationEnabledIsFalse() {
        PscUser user = createPscUser(
            "jo", createMembership(SuiteRole.PERSON_AND_ORGANIZATION_INFORMATION_MANAGER).forSites(nu)
        );

        replayMocks();
        boolean actual = command(user).isSiteCreationEnabled();

        verifyMocks();
        assertFalse("should not be allowed", actual);
    }

    public void testSiteCreationEnabledWithoutRole() {
        PscUser user = createPscUser("jo");

        replayMocks();
        boolean actual = command(user).isSiteCreationEnabled();

        verifyMocks();
        assertFalse("should not be allowed", actual);
    }

    ////// HELPERS
    private SuiteRoleMembership createMembership(SuiteRole suiteRole) {
        return new SuiteRoleMembership(suiteRole, AuthorizationScopeMappings.SITE_MAPPING, null);
    }

    private ManageSitesCommand command(PscUser user) {
        return new ManageSitesCommand(siteService, user);
    }
}
