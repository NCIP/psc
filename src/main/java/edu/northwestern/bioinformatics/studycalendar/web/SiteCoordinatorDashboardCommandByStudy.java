package edu.northwestern.bioinformatics.studycalendar.web;

import edu.northwestern.bioinformatics.studycalendar.domain.*;
import static edu.northwestern.bioinformatics.studycalendar.domain.StudySite.findStudySite;
import edu.northwestern.bioinformatics.studycalendar.service.TemplateService;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

/**
 * @author John Dzak
 */
public class SiteCoordinatorDashboardCommandByStudy extends AbstractSiteCoordinatorDashboardCommand<User, Site> {
    private Study study;
    private TemplateService templateService;
    private Map<User, Map<Site, GridCell>> studyAssignmentGrid;


    public SiteCoordinatorDashboardCommandByStudy(TemplateService templateService, Study study, List<Study> assignableStudies, List<Site> assignableSites, List<User> assignableUsers) {
        super(assignableStudies, assignableSites, assignableUsers);
        studyAssignmentGrid = new HashMap<User, Map<Site, GridCell>>();
        this.templateService = templateService;
        this.study           = study;

        if (study != null) {
            buildStudyAssignmentGrid(assignableUsers, assignableSites);
        }
    }


    public Map<User, Map<Site, GridCell>> getStudyAssignmentGrid() {
        return studyAssignmentGrid;
    }

    protected boolean isSiteSelected(User user, Site site) {
        UserRole userRole = UserRole.findByRole(user.getUserRoles(), Role.PARTICIPANT_COORDINATOR);
        return userRole.getStudySites().contains(findStudySite(study, site));
    }

    protected boolean isSiteAccessAllowed(User user, Site site) {
        UserRole userRole = UserRole.findByRole(user.getUserRoles(), Role.PARTICIPANT_COORDINATOR);
        return userRole.getSites().contains(site);
    }

    protected void performCheckAction(User user, Site site) throws Exception {
        templateService.assignTemplateToParticipantCoordinator(study,site, user);
    }

    protected void performUncheckAction(User user, Site site) throws Exception  {
        templateService.removeAssignedTemplateFromParticipantCoordinator(study,site, user);
    }

    ////////// Getters and setters
    public Study getStudy() {
        return study;
    }

}