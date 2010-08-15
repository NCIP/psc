package edu.northwestern.bioinformatics.studycalendar.dao;

import edu.northwestern.bioinformatics.studycalendar.StudyCalendarSystemException;
import edu.northwestern.bioinformatics.studycalendar.domain.Site;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import gov.nih.nci.cabig.ctms.suite.authorization.CsmHelper;
import gov.nih.nci.cabig.ctms.suite.authorization.ScopeType;
import org.hibernate.event.PostUpdateEvent;
import org.hibernate.event.PostUpdateEventListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;

import java.util.Arrays;

/**
 * @author Rhett Sutphin
 */
public class AuthorizationScopeUpdaterListener implements PostUpdateEventListener {
    private final Logger log = LoggerFactory.getLogger(getClass());

    private CsmHelper suiteCsmHelper;

    public void onPostUpdate(PostUpdateEvent event) {
        if (event.getEntity() instanceof Study) {
            recordScopeChangeIfAny(event, ScopeType.STUDY, "assignedIdentifier");
        } else if (event.getEntity() instanceof Site) {
            recordScopeChangeIfAny(event, ScopeType.SITE, "assignedIdentifier");
        }
    }

    private void recordScopeChangeIfAny(
        PostUpdateEvent event, ScopeType scope, String sharedIdentityPropertyName
    ) {
        int propertyIndex = find(sharedIdentityPropertyName,
            event.getPersister().getPropertyNames());
        String oldValue = (String) event.getOldState()[propertyIndex];
        String newValue = (String) event.getState()[propertyIndex];

        // don't need to care about null here because these values are required to be not null
        // in the database -- if an update succeeded, they won't be null.
        if (oldValue.equals(newValue)) {
            log.debug("Old and new values of shared identity for scope {} are equal ({}).  No update needed.",
                scope, oldValue);
        } else {
            log.debug("Shared identity {} of scope {} changed from {} to {}.  Updating PG/PE pair if any.",
                new Object[] { sharedIdentityPropertyName, scope, oldValue, newValue });
            suiteCsmHelper.renameScopePair(scope, oldValue, newValue);
        }
    }

    private int find(String sharedIdentityPropertyName, String[] propertyNames) {
        for (int i = 0, propertyNamesLength = propertyNames.length; i < propertyNamesLength; i++) {
            String propertyName = propertyNames[i];
            if (propertyName.equals(sharedIdentityPropertyName)) {
                return i;
            }
        }
        throw new StudyCalendarSystemException("Shared identity %s not found in %s",
            sharedIdentityPropertyName, Arrays.asList(propertyNames));
    }

    ////// CONFIGURATION

    @Required
    public void setSuiteCsmHelper(CsmHelper suiteCsmHelper) {
        this.suiteCsmHelper = suiteCsmHelper;
    }
}
