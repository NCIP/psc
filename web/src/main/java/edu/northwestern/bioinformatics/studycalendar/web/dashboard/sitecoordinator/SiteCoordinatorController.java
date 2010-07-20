package edu.northwestern.bioinformatics.studycalendar.web.dashboard.sitecoordinator;

import edu.northwestern.bioinformatics.studycalendar.core.accesscontrol.ApplicationSecurityManager;
import edu.northwestern.bioinformatics.studycalendar.dao.UserDao;
import edu.northwestern.bioinformatics.studycalendar.domain.Role;
import edu.northwestern.bioinformatics.studycalendar.domain.Site;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.domain.StudySite;
import edu.northwestern.bioinformatics.studycalendar.domain.User;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Amendment;
import edu.northwestern.bioinformatics.studycalendar.domain.tools.NamedComparator;
import edu.northwestern.bioinformatics.studycalendar.utils.breadcrumbs.DefaultCrumb;
import edu.northwestern.bioinformatics.studycalendar.web.PscAbstractController;
import edu.northwestern.bioinformatics.studycalendar.web.accesscontrol.AccessControl;
import edu.northwestern.bioinformatics.studycalendar.web.accesscontrol.PscAuthorizedHandler;
import edu.northwestern.bioinformatics.studycalendar.web.accesscontrol.ResourceAuthorization;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;

import static edu.northwestern.bioinformatics.studycalendar.security.authorization.PscRole.STUDY_TEAM_ADMINISTRATOR;

/**
 * Subclass to apply the access control annotation.
 *
 * @author Rhett Sutphin
 */
@AccessControl(roles = Role.SITE_COORDINATOR)
public class SiteCoordinatorController extends PscAbstractController implements PscAuthorizedHandler {
    private UserDao userDao;
    private ApplicationSecurityManager applicationSecurityManager;

    public SiteCoordinatorController() {
        setCrumb(new DefaultCrumb("Site coordinator"));
    }

    public Collection<ResourceAuthorization> authorizations(String httpMethod, Map<String, String[]> queryParameters) {
        return ResourceAuthorization.createCollection(STUDY_TEAM_ADMINISTRATOR);
    }

    @Override
    protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) throws Exception {
        Map<String, Object> model = new HashMap<String, Object>();
        User user = applicationSecurityManager.getFreshUser();
        Collection<Site> sites = new TreeSet<Site>(new NamedComparator());
        sites.addAll(user.getUserRole(Role.SITE_COORDINATOR).getSites());

        model.put("user", user);
        model.put("sites", sites);
        model.put("studiesAndSites", createStudySiteMap(sites));
        // this is slightly over-engineered in anticipation of a more generalized notification system
        Map<String, List<Notification>> notices = new java.util.LinkedHashMap<String, List<Notification>>();
        notices.put("approvals", createPendingApprovalNotifications(sites));
        model.put("notices", notices);
        return new ModelAndView("dashboard/sitecoordinator/main", model);
    }

    private List<Notification> createPendingApprovalNotifications(Collection<Site> sites) {
        List<Notification> notes = new ArrayList<Notification>();
        for (Site site : sites) {
            for (StudySite studySite : site.getStudySites()) {
                for (Amendment amendment : studySite.getUnapprovedAmendments()) {
                    notes.add(new Notification(studySite, amendment));
                }
            }
        }
        return notes;
    }

    private Map<Study, Map<Site, StudySite>> createStudySiteMap(Collection<Site> sites) {
        Map<Study, Map<Site, StudySite>> map = new TreeMap<Study, Map<Site, StudySite>>(new NamedComparator());
        for (Site site : sites) {
            for (StudySite studySite : site.getStudySites()) {
                Study study = studySite.getStudy();
                if (!map.containsKey(study)) {
                    map.put(study, new TreeMap<Site, StudySite>(new NamedComparator()));
                }
                map.get(study).put(site, studySite);
            }
        }
        // fill in blanks for remaining sites
        for (Study study : map.keySet()) {
            for (Site site : sites) {
                if (!map.get(study).containsKey(site)) {
                    map.get(study).put(site, null);
                }
            }
        }
        return map;
    }

    ////// CONFIGURATION

    @Required
    public void setUserDao(UserDao userDao) {
        this.userDao = userDao;
    }

    @Required
    public void setApplicationSecurityManager(ApplicationSecurityManager applicationSecurityManager) {
        this.applicationSecurityManager = applicationSecurityManager;
    }

    // TODO: this will probably need to be generalized and moved to the actual domain at some point
    public static class Notification {
        private StudySite studySite;
        private Amendment amendment;

        public Notification(StudySite studySite, Amendment amendment) {
            this.studySite = studySite;
            this.amendment = amendment;
        }

        public StudySite getStudySite() {
            return studySite;
        }

        public Amendment getAmendment() {
            return amendment;
        }
    }
}
