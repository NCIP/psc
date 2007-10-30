package edu.northwestern.bioinformatics.studycalendar.web;

import static edu.northwestern.bioinformatics.studycalendar.domain.StudySite.findStudySite;
import edu.northwestern.bioinformatics.studycalendar.domain.*;
import edu.northwestern.bioinformatics.studycalendar.service.TemplateService;
import edu.northwestern.bioinformatics.studycalendar.utils.NamedComparator;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * @author John Dzak
 */
public class SiteCoordinatorDashboardCommandByUser extends AbstractSiteCoordinatorDashboardCommand<Study, Site> {
    private User user;
    private TemplateService templateService;
    private Map<Study, Map<Site, GridCell>> grid;


    public SiteCoordinatorDashboardCommandByUser(TemplateService templateService, User user, List<Study> assignableStudies, List<Site> assignableSites, List<User> assignableUsers) {
        super(assignableStudies, assignableSites, assignableUsers);
        grid = new TreeMap<Study, Map<Site, GridCell>>(new NamedComparator());
        this.templateService = templateService;
        this.user = user;

        if (user != null) {
            buildGrid(assignableStudies, assignableSites);
        }
    }


    public Map<Study, Map<Site, GridCell>> getGrid() {
        return grid;
    }

    protected boolean isSiteSelected(Study study, Site site) {
        UserRole userRole = UserRole.findByRole(user.getUserRoles(), Role.PARTICIPANT_COORDINATOR);
        return userRole.getStudySites().contains(findStudySite(study, site));
    }

    protected boolean isSiteAccessAllowed(Study study, Site site) {
        UserRole userRole = UserRole.findByRole(user.getUserRoles(), Role.PARTICIPANT_COORDINATOR);
        return (StudySite.findStudySite(study, site) != null && userRole.getSites().contains(site));
    }

     protected void performCheckAction(Study study, Site site) throws Exception {
        templateService.assignTemplateToParticipantCoordinator(study,site, user);
    }

    protected void performUncheckAction(Study study, Site site) throws Exception  {
        templateService.removeAssignedTemplateFromParticipantCoordinator(study,site, user);
    }

    public User getUser() {
        return user;
    }
}