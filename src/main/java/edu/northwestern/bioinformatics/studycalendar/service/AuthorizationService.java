package edu.northwestern.bioinformatics.studycalendar.service;

import edu.northwestern.bioinformatics.studycalendar.domain.Role;
import edu.northwestern.bioinformatics.studycalendar.domain.StudySubjectAssignment;
import edu.northwestern.bioinformatics.studycalendar.domain.User;
import edu.northwestern.bioinformatics.studycalendar.domain.UserRole;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.domain.StudySite;
import edu.northwestern.bioinformatics.studycalendar.domain.Site;
import static edu.northwestern.bioinformatics.studycalendar.domain.Role.*;
import edu.northwestern.bioinformatics.studycalendar.utils.accesscontrol.StudyCalendarAuthorizationManager;
import org.springframework.beans.factory.annotation.Required;

import java.util.LinkedList;
import java.util.List;

/**
 * @author Rhett Sutphin
 */
public class AuthorizationService {
    private StudyCalendarAuthorizationManager authorizationManager;

    public List<StudySubjectAssignment> filterForVisibility(List<StudySubjectAssignment> source, User visibleTo) {
        UserRole subjCoord = visibleTo.getUserRole(Role.SUBJECT_COORDINATOR);
        List<StudySubjectAssignment> visible = new LinkedList<StudySubjectAssignment>();
        for (StudySubjectAssignment assignment : source) {
            if (subjCoord.getStudySites().contains(assignment.getStudySite())) {
                visible.add(assignment);
            }
        }
        return visible;
    }

    public boolean isTemplateVisible(UserRole userRole, Study study) {
        if (userRole.getRole() == SYSTEM_ADMINISTRATOR) {
            return false;
        } else if (!userRole.getRole().isSiteSpecific()) {
            return true;
        } else if (userRole.getRole() == SITE_COORDINATOR) {
            return isTemplateVisibleToSiteCoordinator(userRole, study);
        } else if (userRole.getRole() == SUBJECT_COORDINATOR ) {
            return isTemplateVisibleToStudySpecificRole(userRole, study);
        } else {
            throw new UnsupportedOperationException("Unexpected role in userRole: " + userRole.getRole());
        }
    }

    private boolean isTemplateVisibleToStudySpecificRole(UserRole studySpecificRole, Study study) {
        for (StudySite studySite : study.getStudySites()) {
            if (studySpecificRole.getStudySites().contains(studySite)) return true;
        }
        return false;
    }

    private boolean isTemplateVisibleToSiteCoordinator(UserRole siteCoordinator, Study study) {
        for (Site siteOnStudy : study.getSites()) {
            if (siteCoordinator.getSites().contains(siteOnStudy)) return true;
        }
        return false;
    }

    ////// CONFIGURATION

    @Required
    public void setStudyCalendarAuthorizationManager(StudyCalendarAuthorizationManager authorizationManager) {
        this.authorizationManager = authorizationManager;
    }
}
