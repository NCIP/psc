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
public class SiteCoordinatorDashboardCommandByStudy implements AbstractSiteCoordinatorDashboardCommand {
    private Map<User,Map<Site, StudyAssignmentCell>> studyAssignmentGrid;
    private Study study;
    private TemplateService templateService;
    private List<Study> assignableStudies;
    private List<Site> assignableSites;
    private List<User> assignableUsers;


    public SiteCoordinatorDashboardCommandByStudy(TemplateService templateService, Study study, List<Study> assignableStudies, List<Site> assignableSites, List<User> assignableUsers) {
        this.templateService = templateService;
        this.study           = study;
        this.assignableStudies = assignableStudies;
        this.assignableSites = assignableSites;
        this.assignableUsers = assignableUsers;

        if (study != null) {
            buildStudyAssignmentGrid();
        }
    }
    
    protected void buildStudyAssignmentGrid() {
        studyAssignmentGrid = new TreeMap<User,Map<Site, StudyAssignmentCell>>(new NamedComparator());

        for (User user : assignableUsers) {
            UserRole userRole = UserRole.findByRole(user.getUserRoles(), Role.PARTICIPANT_COORDINATOR);
            if (!studyAssignmentGrid.containsKey(user)) studyAssignmentGrid.put(user, new TreeMap<Site, StudyAssignmentCell>(new NamedComparator()));
            for (Site site : assignableSites) {
                    studyAssignmentGrid.get(user)
                            .put(site,
                                    createStudyAssignmentCell(isSiteSelected(userRole, study, site),
                                            isSiteAccessAllowed(userRole, site)));
            }
        }
    }

    protected boolean isSiteSelected(UserRole userRole, Study study, Site site) {
        return userRole.getStudySites().contains(findStudySite(study, site));
    }

    protected boolean isSiteAccessAllowed(UserRole userRole, Site site) {
        return userRole.getSites().contains(site);
    }

    public Map<User, Map<Site, StudyAssignmentCell>> getStudyAssignmentGrid() {
        return studyAssignmentGrid;
    }

    public void apply() throws Exception {
        for (User user : studyAssignmentGrid.keySet()) {
            for (Site site : studyAssignmentGrid.get(user).keySet()) {
                if (studyAssignmentGrid.get(user).get(site).isSelected()) {
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

    public Study getStudy() {
        return study;
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