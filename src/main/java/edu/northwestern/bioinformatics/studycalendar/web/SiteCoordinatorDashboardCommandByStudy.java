package edu.northwestern.bioinformatics.studycalendar.web;

import edu.northwestern.bioinformatics.studycalendar.domain.*;
import static edu.northwestern.bioinformatics.studycalendar.domain.StudySite.findStudySite;
import edu.northwestern.bioinformatics.studycalendar.service.TemplateService;
import edu.northwestern.bioinformatics.studycalendar.utils.NamedComparator;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * @author John Dzak
 */
public class SiteCoordinatorDashboardCommandByStudy extends AbstractSiteCoordinatorDashboardCommand<User, Site> {
    private Study selected;
    private TemplateService templateService;
    private Map<User, Map<Site, GridCell>> studyAssignmentGrid;


    public SiteCoordinatorDashboardCommandByStudy(TemplateService templateService, Study selected, List<Study> assignableStudies, List<Site> assignableSites, List<User> assignableUsers) {
        super(assignableStudies, assignableSites, assignableUsers);
        studyAssignmentGrid = new TreeMap<User, Map<Site, GridCell>>(new NamedComparator());
        this.templateService = templateService;
        this.selected = selected;

        if (selected != null) {
            buildGrid(assignableUsers, assignableSites);
        }
    }


    public Map<User, Map<Site, GridCell>> getGrid() {
        return studyAssignmentGrid;
    }

    protected boolean isSiteSelected(User user, Site site) {
        UserRole userRole = UserRole.findByRole(user.getUserRoles(), Role.PARTICIPANT_COORDINATOR);
        return userRole.getStudySites().contains(findStudySite(selected, site));
    }

    protected boolean isSiteAccessAllowed(User user, Site site) {
        UserRole userRole = UserRole.findByRole(user.getUserRoles(), Role.PARTICIPANT_COORDINATOR);
        return userRole.getSites().contains(site);
    }

    protected void performCheckAction(User user, Site site) throws Exception {
        templateService.assignTemplateToParticipantCoordinator(selected,site, user);
    }

    protected void performUncheckAction(User user, Site site) throws Exception  {
        templateService.removeAssignedTemplateFromParticipantCoordinator(selected,site, user);
    }

    ////////// Getters and setters
    public Study getSelected() {
        return selected;
    }
}