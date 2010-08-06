package edu.northwestern.bioinformatics.studycalendar.web;

import edu.northwestern.bioinformatics.studycalendar.domain.Site;
import edu.northwestern.bioinformatics.studycalendar.security.authorization.PscRole;
import edu.northwestern.bioinformatics.studycalendar.security.authorization.PscUser;
import edu.northwestern.bioinformatics.studycalendar.service.SiteService;
import gov.nih.nci.cabig.ctms.suite.authorization.SuiteRoleMembership;

import java.util.List;

import static java.util.Collections.emptyList;

public class ManageSitesCommand {
    private PscUser user;
    private SiteService siteService;

    public ManageSitesCommand(SiteService siteService, PscUser user) {
        this.user = user;
        this.siteService = siteService;
    }

    @SuppressWarnings("unchecked")
    public List<Site> getManageableSites() {
        SuiteRoleMembership m = user.getMembership(PscRole.PERSON_AND_ORGANIZATION_INFORMATION_MANAGER);

        if (m == null) return emptyList();

        if (m.isAllSites()) {
            return siteService.getAll();
        } else {
            return (List<Site>) m.getSites();
        }

    }

    public boolean isSiteCreationEnabled() {
        SuiteRoleMembership m = user.getMembership(PscRole.PERSON_AND_ORGANIZATION_INFORMATION_MANAGER);

        return (m != null) && m.isAllSites();
    }

    public static ManageSitesCommand create(SiteService siteService, PscUser user) {
        return new ManageSitesCommand(siteService, user);
    }
}
