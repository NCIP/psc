package edu.northwestern.bioinformatics.studycalendar.web.dashboard.sitecoordinator;

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
public class AssignSubjectCoordinatorByUserCommand extends AbstractAssignSubjectCoordinatorCommand<Study, Site> {
    private User selected;
    private TemplateService templateService;
    private Map<Study, Map<Site, GridCell>> grid;


    public AssignSubjectCoordinatorByUserCommand(TemplateService templateService, User selected, List<Study> assignableStudies, List<Site> assignableSites, List<User> assignableUsers) {
        super(assignableStudies, assignableSites, assignableUsers);
        grid = new TreeMap<Study, Map<Site, GridCell>>(new NamedComparator());
        this.templateService = templateService;
        this.selected = selected;

        if (selected != null && assignableStudies != null && assignableSites != null) {
            buildGrid(assignableStudies, assignableSites);
        }
    }


    public Map<Study, Map<Site, GridCell>> getGrid() {
        return grid;
    }

    protected boolean isSiteSelected(Study study, Site site) {
        UserRole userRole = UserRole.findByRole(selected.getUserRoles(), Role.SUBJECT_COORDINATOR);
        return userRole.getStudySites().contains(findStudySite(study, site));
    }

    protected boolean isSiteAccessAllowed(Study study, Site site) {
        UserRole userRole = UserRole.findByRole(selected.getUserRoles(), Role.SUBJECT_COORDINATOR);
        return (StudySite.findStudySite(study, site) != null && userRole.getSites().contains(site));
    }

     protected void performCheckAction(Study study, Site site) throws Exception {
        templateService.assignTemplateToSubjectCoordinator(study,site, selected);
    }

    protected void performUncheckAction(Study study, Site site) throws Exception  {
        templateService.removeAssignedTemplateFromSubjectCoordinator(study,site, selected);
    }

    public User getSelected() {
        return selected;
    }
}