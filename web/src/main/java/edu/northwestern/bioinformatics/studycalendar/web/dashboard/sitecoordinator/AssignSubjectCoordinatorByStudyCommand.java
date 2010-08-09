package edu.northwestern.bioinformatics.studycalendar.web.dashboard.sitecoordinator;

import edu.northwestern.bioinformatics.studycalendar.domain.Role;
import edu.northwestern.bioinformatics.studycalendar.domain.Site;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.domain.User;
import edu.northwestern.bioinformatics.studycalendar.domain.UserRole;
import edu.northwestern.bioinformatics.studycalendar.domain.tools.NamedComparator;
import edu.northwestern.bioinformatics.studycalendar.service.TemplateService;
import edu.northwestern.bioinformatics.studycalendar.web.AbstractGridCommand;
import edu.northwestern.bioinformatics.studycalendar.web.osgi.InstalledAuthenticationSystem;
import edu.nwu.bioinformatics.commons.spring.Validatable;
import org.springframework.validation.Errors;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import static edu.northwestern.bioinformatics.studycalendar.domain.StudySite.findStudySite;

/**
 * @author John Dzak
 */
public class AssignSubjectCoordinatorByStudyCommand extends AbstractAssignSubjectCoordinatorCommand<User, Site> implements Validatable {
    private Study selected;
    private TemplateService templateService;
    private Map<User, Map<Site, AbstractGridCommand.GridCell>> studyAssignmentGrid;

    public AssignSubjectCoordinatorByStudyCommand(TemplateService templateService, Study selected, List<Study> assignableStudies, List<Site> assignableSites, List<User> assignableUsers) {
        super(assignableStudies, assignableSites, assignableUsers);
        studyAssignmentGrid = new TreeMap<User, Map<Site, AbstractGridCommand.GridCell>>(new NamedComparator());
        this.templateService = templateService;
        this.selected = selected;

        if (selected != null && assignableUsers != null && assignableSites != null) {
            buildGrid(assignableUsers, assignableSites);
        }
    }


    public Map<User, Map<Site, AbstractGridCommand.GridCell>> getGrid() {
        return studyAssignmentGrid;
    }

    protected boolean isSiteSelected(User user, Site site) {
        UserRole userRole = user.getUserRole(Role.SUBJECT_COORDINATOR);
        return userRole.getStudySites().contains(findStudySite(selected, site));
    }

    protected boolean isSiteAccessAllowed(User user, Site site) {
        UserRole userRole = user.getUserRole(Role.SUBJECT_COORDINATOR);
        return userRole.getSites().contains(site);
    }

    protected void performCheckAction(User user, Site site) throws Exception {
        templateService.assignTemplateToSubjectCoordinator(selected, site, user);
    }

    protected void performUncheckAction(User user, Site site) throws Exception {
        templateService.removeAssignedTemplateFromSubjectCoordinator(selected, site, user);
    }

    ////////// Getters and setters
    public Study getSelected() {
        return selected;
    }

    public void validate(Errors errors) {
        if (getSelected()==null) {
            errors.reject("error.please.select.one.study");
        }
    }
}