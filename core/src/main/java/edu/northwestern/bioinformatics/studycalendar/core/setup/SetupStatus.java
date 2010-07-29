package edu.northwestern.bioinformatics.studycalendar.core.setup;

import edu.northwestern.bioinformatics.studycalendar.dao.SiteDao;
import edu.northwestern.bioinformatics.studycalendar.dao.SourceDao;
import edu.northwestern.bioinformatics.studycalendar.security.authorization.PscRole;
import edu.northwestern.bioinformatics.studycalendar.service.AuthorizationService;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Required;

import java.util.LinkedHashMap;
import java.util.Map;

import static edu.northwestern.bioinformatics.studycalendar.core.setup.SetupStatus.InitialSetupElement.*;

/**
 * Examines the application (via the injected beans) and determines whether any initial setup steps
 * are required. Caches its results so that the setup-or-not filter is faster.
 *
 * @author Rhett Sutphin
 */
public class SetupStatus implements InitializingBean {
    private Map<InitialSetupElement, SetupChecker> checkers;
    private SiteDao siteDao;
    private SourceDao sourceDao;
    private AuthorizationService authorizationService;

    private boolean[] prepared;

    public SetupStatus() {
        prepared = new boolean[InitialSetupElement.values().length];
        checkers = new LinkedHashMap<InitialSetupElement, SetupChecker>();
        checkers.put(SITE, new SetupChecker() {
            public boolean isPrepared() {
                return siteDao.getCount() > 0;
            }
        });
        checkers.put(SOURCE, new SetupChecker() {
            public boolean isPrepared() {
                return sourceDao.getCount() > 0;
            }
        });

        checkers.put(ADMINISTRATOR, new SetupChecker() {
            public boolean isPrepared() {
                return !authorizationService.getCsmUsers(PscRole.SYSTEM_ADMINISTRATOR).isEmpty();
            }
        });
    }

    public void recheck() {
        for (Map.Entry<InitialSetupElement, SetupChecker> entry : checkers.entrySet()) {
            prepared[entry.getKey().ordinal()] = entry.getValue().isPrepared();
        }
    }

    public void afterPropertiesSet() throws Exception {
        recheck();
    }

    public boolean isPreAuthenticationSetupNeeded() {
        return isAdministratorMissing();
    }

    public boolean isPostAuthenticationSetupNeeded() {
        return (isSiteMissing() || isSourceMissing());
    }

    public InitialSetupElement preAuthenticationSetup() {
        recheck();
        if (isAdministratorMissing())
            return InitialSetupElement.ADMINISTRATOR;
        return null;
    }

    public InitialSetupElement postAuthenticationSetup() {
        recheck();
        if (isSiteMissing())
            return InitialSetupElement.SITE;
        if (isSourceMissing())
            return InitialSetupElement.SOURCE;
        return null;
    }

    ////// PROPERTY ACCESSORS

    public boolean isSiteMissing() {
        return !prepared[SITE.ordinal()];
    }

    public boolean isAdministratorMissing() {
        return !prepared[ADMINISTRATOR.ordinal()];
    }

    public boolean isSourceMissing() {
        return !prepared[SOURCE.ordinal()];
    }

    ////// CONFIGURATION

    @Required
    public void setSiteDao(SiteDao siteDao) {
        this.siteDao = siteDao;
    }

    @Required
    public void setSourceDao(SourceDao sourceDao) {
        this.sourceDao = sourceDao;
    }

    @Required
    public void setAuthorizationService(AuthorizationService authorizationService) {
        this.authorizationService = authorizationService;
    }

    ////// INNER CLASSES

    private interface SetupChecker {
        boolean isPrepared();
    }

    public enum InitialSetupElement {
        SITE,
        SOURCE,
        ADMINISTRATOR
    }
}
