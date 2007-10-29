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
public class SiteCoordinatorDashboardCommandByUser implements AbstractSiteCoordinatorDashboardCommand {
    private Map<Study,Map<Site, StudyAssignmentCell>> studyAssignmentGrid;
    private User user;
    private TemplateService templateService;
    private List<Study> assignableStudies;
    private List<Site> assignableSites;
    private List<User> assignableUsers;


    public SiteCoordinatorDashboardCommandByUser(TemplateService templateService, User user, List<Study> assignableStudies, List<Site> assignableSites, List<User> assignableUsers) {
        this.templateService = templateService;
        this.assignableStudies = assignableStudies;
        this.assignableSites = assignableSites;
        this.assignableUsers = assignableUsers;
        this.user = user;

        if (user != null) {
            buildStudyAssignmentGrid();
        }
    }

    protected void buildStudyAssignmentGrid() {
        studyAssignmentGrid = new TreeMap<Study,Map<Site, StudyAssignmentCell>>(new NamedComparator());

        UserRole userRole = UserRole.findByRole(user.getUserRoles(), Role.PARTICIPANT_COORDINATOR);
        for (Study study : assignableStudies) {
            if (!studyAssignmentGrid.containsKey(study)) studyAssignmentGrid.put(study, new TreeMap<Site, StudyAssignmentCell>(new NamedComparator()));
            for (Site site : assignableSites) {
                    studyAssignmentGrid.get(study)
                            .put(site,
                                    createStudyAssignmentCell(isSiteSelected(userRole, study, site),
                                            isSiteAccessAllowed(userRole, study, site)));
            }
        }
    }

    protected boolean isSiteSelected(UserRole userRole, Study study, Site site) {
        return userRole.getStudySites().contains(findStudySite(study, site));
    }

    protected boolean isSiteAccessAllowed(UserRole userRole, Study study, Site site) {
        return (StudySite.findStudySite(study, site) != null && userRole.getSites().contains(site));
    }

    public Map<Study, Map<Site, StudyAssignmentCell>> getStudyAssignmentGrid() {
        return studyAssignmentGrid;
    }

    public void apply() throws Exception {
        for (Study study : studyAssignmentGrid.keySet()) {
            for (Site site : studyAssignmentGrid.get(study).keySet()) {
                if (studyAssignmentGrid.get(study).get(site).isSelected()) {
                    templateService.assignTemplateToParticipantCoordinator(study,site, user);
                } else {
                    templateService.removeAssignedTemplateFromParticipantCoordinator(study,site, user);
                }
            }
        }
    }

    public static class StudyAssignmentCell {
        private boolean selected;
        private boolean siteAccessAllowed;

        public StudyAssignmentCell(boolean selected, boolean siteAccessAllowed) {
            this.selected = selected;
            this.siteAccessAllowed = siteAccessAllowed;
        }

        public boolean isSelected() {
            return selected;
        }

        public void setSelected(boolean selected) {
            this.selected = selected;
        }

        public boolean isSiteAccessAllowed() {
            return siteAccessAllowed;
        }

        public void setSiteAccessAllowed(boolean siteAccessAllowed) {
            this.siteAccessAllowed = siteAccessAllowed;
        }
    }

    protected static StudyAssignmentCell createStudyAssignmentCell(boolean selected, boolean siteAccessAllowed) {
        return new StudyAssignmentCell(selected, siteAccessAllowed);
    }

    public User getUser() {
        return user;
    }

    public List<Study> getAssignableStudies() {
        return assignableStudies;
    }

    public List<Site> getAssignableSites() {
        return assignableSites;
    }

    public List<User> getAssignableUsers() {
        return assignableUsers;
    }
}