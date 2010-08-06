package edu.northwestern.bioinformatics.studycalendar.web;

import edu.northwestern.bioinformatics.studycalendar.domain.Site;
import edu.northwestern.bioinformatics.studycalendar.security.authorization.PscRole;
import edu.northwestern.bioinformatics.studycalendar.security.authorization.PscUser;
import edu.northwestern.bioinformatics.studycalendar.service.SiteService;
import gov.nih.nci.cabig.ctms.suite.authorization.SuiteRoleMembership;

import java.util.Collections;
import java.util.List;

import static java.util.Collections.emptyList;

public class ManageSitesCommand {
    private PscUser user;
    private SiteService siteService;

    public ManageSitesCommand(PscUser user, SiteService siteService) {
        this.user = user;
        this.siteService = siteService;
    }

    @SuppressWarnings("unchecked")
    public List<Site> manageableSites() {
        SuiteRoleMembership m = user.getMembership(PscRole.PERSON_AND_ORGANIZATION_INFORMATION_MANAGER);

        if (m == null) return emptyList();

        if (m.isAllSites()) {
            return siteService.getAll();
        } else {
            return (List<Site>) user.getMembership(PscRole.PERSON_AND_ORGANIZATION_INFORMATION_MANAGER).getSites();
        }

    }
}
