package edu.northwestern.bioinformatics.studycalendar.web.dashboard;

import edu.northwestern.bioinformatics.studycalendar.core.accesscontrol.ApplicationSecurityManager;
import edu.northwestern.bioinformatics.studycalendar.dao.StudyDao;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.security.authorization.PscRole;
import edu.northwestern.bioinformatics.studycalendar.security.authorization.PscUser;
import edu.northwestern.bioinformatics.studycalendar.service.presenter.UserTemplateRelationship;
import edu.northwestern.bioinformatics.studycalendar.web.PscAbstractController;
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

/**
 * @author Rhett Sutphin
 */
public class DashboardController extends PscAbstractController implements PscAuthorizedHandler {
    private ApplicationSecurityManager applicationSecurityManager;
    private StudyDao studyDao;

    public Collection<ResourceAuthorization> authorizations(
        String httpMethod, Map<String, String[]> queryParameters
    ) throws Exception {
        return ResourceAuthorization.createCollection(PscRole.STUDY_SUBJECT_CALENDAR_MANAGER);
    }

    @Override
    protected ModelAndView handleRequestInternal(
        HttpServletRequest request, HttpServletResponse response
    ) throws Exception {
        Map<String, Object> model = new HashMap<String, Object>();
        model.put("studies", getAssignableStudies());

        model.put("dashboardUser", getUser());
        return new ModelAndView("dashboard/display", model);
    }

    private PscUser getUser() {
        return applicationSecurityManager.getUser();
    }

    private List<UserTemplateRelationship> getAssignableStudies() {
        List<Study> studies = studyDao.getVisibleStudies(getUser().
            getVisibleStudyParameters(PscRole.STUDY_SUBJECT_CALENDAR_MANAGER));
        List<UserTemplateRelationship> rels = new ArrayList<UserTemplateRelationship>(studies.size());
        for (Study study : studies) {
            UserTemplateRelationship rel = new UserTemplateRelationship(getUser(), study);
            if (rel.getCanAssignSubjects()) rels.add(rel);
        }
        return rels;
    }

    ////// CONFIGURATION

    @Required
    public void setApplicationSecurityManager(ApplicationSecurityManager applicationSecurityManager) {
        this.applicationSecurityManager = applicationSecurityManager;
    }

    @Required
    public void setStudyDao(StudyDao studyDao) {
        this.studyDao = studyDao;
    }
}
