package edu.northwestern.bioinformatics.studycalendar.web.dashboard.sitecoordinator;

import edu.northwestern.bioinformatics.studycalendar.domain.Role;
import edu.northwestern.bioinformatics.studycalendar.domain.Site;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.domain.StudySite;
import edu.northwestern.bioinformatics.studycalendar.domain.User;
import edu.northwestern.bioinformatics.studycalendar.domain.UserRole;
import edu.northwestern.bioinformatics.studycalendar.domain.tools.NamedComparatorByLetterCase;
import edu.northwestern.bioinformatics.studycalendar.service.TemplateService;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import static edu.northwestern.bioinformatics.studycalendar.domain.StudySite.findStudySite;

/**
 * @author John Dzak
 */
public class AssignSubjectCoordinatorByUserCommand extends AbstractAssignSubjectCoordinatorCommand<Study, Site> {
    private User selected;
    private TemplateService templateService;
    private Map<Study, Map<Site, GridCell>> grid;

    public AssignSubjectCoordinatorByUserCommand(TemplateService templateService, User selected, List<Study> assignableStudies, List<Site> assignableSites, List<User> assignableUsers) {
        super(assignableStudies, assignableSites, assignableUsers);
        grid = new TreeMap<Study, Map<Site, GridCell>>(new NamedComparatorByLetterCase());
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
        UserRole userRole = selected.getUserRole(Role.SUBJECT_COORDINATOR);
        return userRole.getStudySites().contains(findStudySite(study, site));
    }

    protected boolean isSiteAccessAllowed(Study study, Site site) {
        UserRole userRole = selected.getUserRole(Role.SUBJECT_COORDINATOR);
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