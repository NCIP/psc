package edu.northwestern.bioinformatics.studycalendar.dao;

import edu.northwestern.bioinformatics.studycalendar.core.CsmUserCache;
import edu.northwestern.bioinformatics.studycalendar.domain.UserAction;
import gov.nih.nci.security.authorization.domainobjects.User;
import org.hibernate.event.PostLoadEvent;
import org.hibernate.event.PostLoadEventListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;

/**
 * @author Rhett Sutphin
 */
public class UserActionUserResolverListener implements PostLoadEventListener {
    private final Logger log = LoggerFactory.getLogger(getClass());

    private CsmUserCache csmUserCache;

    public void onPostLoad(PostLoadEvent event) {
        if (!(event.getEntity() instanceof UserAction)) return;

        UserAction action = (UserAction) event.getEntity();
        if (action.getCsmUserId() == null) return;

        log.debug("Resolving associated user for user action {} (user ID = {})",
            action.getId(), action.getCsmUserId());

        User csmUser = csmUserCache.getCsmUser(action.getCsmUserId());
        if (csmUser == null) return;

        action.setUser(csmUser);
    }

    ////// CONFIGURATION

    @Required
    public void setCsmUserCache(CsmUserCache csmUserCache) {
        this.csmUserCache = csmUserCache;
    }
}
