package edu.northwestern.bioinformatics.studycalendar.dao;

import edu.northwestern.bioinformatics.studycalendar.core.CsmUserCache;
import edu.northwestern.bioinformatics.studycalendar.domain.StudySubjectAssignment;
import gov.nih.nci.security.authorization.domainobjects.User;
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

    private CsmUserCache csmUserCache;

    public void onPostLoad(PostLoadEvent event) {
        if (!(event.getEntity() instanceof StudySubjectAssignment)) return;

        StudySubjectAssignment assignment = (StudySubjectAssignment) event.getEntity();
        if (assignment.getManagerCsmUserId() == null) return;

        log.debug("Resolving associated manager for assignment {} (manager user ID = {})",
            assignment.getId(), assignment.getManagerCsmUserId());

        User csmUser = csmUserCache.getCsmUser(assignment.getManagerCsmUserId());
        if (csmUser == null) return;

        assignment.setStudySubjectCalendarManager(csmUser);
    }

    ////// CONFIGURATION

    @Required
    public void setCsmUserCache(CsmUserCache csmUserCache) {
        this.csmUserCache = csmUserCache;
    }
}
