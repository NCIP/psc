package edu.northwestern.bioinformatics.studycalendar.dao;

import edu.northwestern.bioinformatics.studycalendar.domain.StudySubjectAssignment;
import gov.nih.nci.security.AuthorizationManager;
import gov.nih.nci.security.authorization.domainobjects.User;
import gov.nih.nci.security.exceptions.CSObjectNotFoundException;
import org.hibernate.event.PostLoadEvent;
import org.hibernate.event.PostLoadEventListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;

/**
 * @author Rhett Sutphin
 */
public class AssignmentManagerResolverListener implements PostLoadEventListener {
    private final Logger log = LoggerFactory.getLogger(getClass());

    private AuthorizationManager csmAuthorizationManager;

    public void onPostLoad(PostLoadEvent event) {
        if (!(event.getEntity() instanceof StudySubjectAssignment)) return;

        StudySubjectAssignment assignment = (StudySubjectAssignment) event.getEntity();
        if (assignment.getManagerCsmUserId() == null) return;

        log.debug("Resolving associated manager for assignment {} (manager user ID = {})",
            assignment.getId(), assignment.getManagerCsmUserId());

        User csmUser = findCsmUser(assignment.getManagerCsmUserId());
        if (csmUser == null) return;

        assignment.setStudySubjectCalendarManager(csmUser);
    }

    private User findCsmUser(Integer csmUserId) {
        try {
            return csmAuthorizationManager.getUserById(csmUserId.toString());
        } catch (CSObjectNotFoundException e) {
            return null;
        }
    }

    ////// CONFIGURATION

    @Required
    public void setCsmAuthorizationManager(AuthorizationManager csmAuthorizationManager) {
        this.csmAuthorizationManager = csmAuthorizationManager;
    }
}
