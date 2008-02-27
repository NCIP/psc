package edu.northwestern.bioinformatics.studycalendar.web.setup;

import static edu.northwestern.bioinformatics.studycalendar.web.setup.SetupStatus.InitialSetupElement.*;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Required;
import edu.northwestern.bioinformatics.studycalendar.dao.SiteDao;
import edu.northwestern.bioinformatics.studycalendar.dao.UserDao;
import edu.northwestern.bioinformatics.studycalendar.domain.Role;

import java.util.Map;
import java.util.LinkedHashMap;

/**
 * Examines the application (via the injected beans) and determines whether any initial setup steps are required.
 * Caches its results so that the setup-or-not filter is faster.
 *
 * @author Rhett Sutphin
 */
public class SetupStatus implements InitializingBean {
    private Map<InitialSetupElement, SetupChecker> checkers;
    private SiteDao siteDao;
    private UserDao userDao;

    private boolean[] prepared;

    public SetupStatus() {
        prepared = new boolean[InitialSetupElement.values().length];
        checkers = new LinkedHashMap<InitialSetupElement, SetupChecker>();
        checkers.put(SITE, new SetupChecker() {
            public boolean isPrepared() {
                return siteDao.getCount() > 0;
            }
        });
        checkers.put(ADMINISTRATOR, new SetupChecker() {
            public boolean isPrepared() {
                return userDao.getByRole(Role.SYSTEM_ADMINISTRATOR).size() > 0;
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

    public boolean isSetupNeeded() {
        for (boolean b : prepared) {
            if (!b) return true;
        }
        return false;
    }

    public InitialSetupElement next() {
        recheck();
        for (int i = 0; i < prepared.length; i++) {
            if (!prepared[i]) return InitialSetupElement.values()[i];
        }
        return null;
    }

    ////// PROPERTY ACCESSORS

    public boolean isSiteMissing() {
        return !prepared[SITE.ordinal()];
    }

    public boolean isAdministratorMissing() {
        return !prepared[ADMINISTRATOR.ordinal()];
    }

    ////// CONFIGURATION

    @Required
    public void setSiteDao(SiteDao siteDao) {
        this.siteDao = siteDao;
    }

    @Required
    public void setUserDao(UserDao userDao) {
        this.userDao = userDao;
    }

    ////// INNER CLASSES

    private interface SetupChecker {
        boolean isPrepared();
    }

    public enum InitialSetupElement {
        SITE,
        ADMINISTRATOR
    }
}
