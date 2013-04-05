/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.io/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.web.template;

import edu.northwestern.bioinformatics.studycalendar.web.PscAbstractController;
import edu.northwestern.bioinformatics.studycalendar.utils.breadcrumbs.DefaultCrumb;
import edu.northwestern.bioinformatics.studycalendar.service.DomainContext;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Amendment;
import edu.northwestern.bioinformatics.studycalendar.dao.StudyDao;
import edu.northwestern.bioinformatics.studycalendar.dao.delta.AmendmentDao;
import edu.northwestern.bioinformatics.studycalendar.service.DeltaService;
import edu.northwestern.bioinformatics.studycalendar.web.accesscontrol.PscAuthorizedHandler;
import edu.northwestern.bioinformatics.studycalendar.web.accesscontrol.ResourceAuthorization;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.bind.ServletRequestUtils;
import org.springframework.beans.factory.annotation.Required;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Collection;
import java.util.Map;
import java.util.HashMap;

import static edu.northwestern.bioinformatics.studycalendar.security.authorization.PscRole.*;

/**
 * @author Jalpa Patel
 */
public class SchedulePreviewController  extends PscAbstractController implements PscAuthorizedHandler {
    private StudyDao studyDao;
    private AmendmentDao amendmentDao;
    private Study study;
    private Amendment amendment;
    private DeltaService deltaService;

    public SchedulePreviewController() {
        setCrumb(new Crumb());
    }

    public Collection<ResourceAuthorization> authorizations(String httpMethod, Map<String, String[]> queryParameters) {
        return ResourceAuthorization.createCollection(
                DATA_IMPORTER,
                STUDY_QA_MANAGER, STUDY_TEAM_ADMINISTRATOR,
                STUDY_SITE_PARTICIPATION_ADMINISTRATOR,
                STUDY_CREATOR,
                STUDY_CALENDAR_TEMPLATE_BUILDER,
                STUDY_SUBJECT_CALENDAR_MANAGER,
                DATA_READER
        );
    }

    @Override
    protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) throws Exception {
        Integer studyId = ServletRequestUtils.getIntParameter(request, "study");
        Integer amendmentId = ServletRequestUtils.getIntParameter(request, "amendment");
        Map<String, Object> model = new HashMap<String, Object>();
        if (studyId != null) {
            study = studyDao.getById(studyId);
        }
        if (amendmentId != null) {
            amendment = amendmentDao.getById(amendmentId);
        }

        if (amendment.equals(study.getDevelopmentAmendment())) {
            study = deltaService.revise(study, amendment);
            model.put("amendmentIdentifier", "development");
        }
        else {
            model.put("amendmentIdentifier", "current");
        }

        model.put("study",study);
        model.put("schedulePreview", true);
        return new ModelAndView("subject/schedule", model);
    }

    @Required
    public void setStudyDao(StudyDao studyDao) {
        this.studyDao = studyDao;
    }

    @Required
    public void setDeltaService(DeltaService deltaService) {
        this.deltaService = deltaService;
    }

    @Required
    public void setAmendmentDao(AmendmentDao amendmentDao) {
        this.amendmentDao = amendmentDao;
    }

    private static class Crumb extends DefaultCrumb {
        public Crumb() {
            super("Schedule Preview");
        }
        @Override
        public Map<String, String> getParameters(DomainContext context) {
            Map<String, String> params = createParameters("study", context.getStudy().getId().toString());
            return params;
        }
    }
}
