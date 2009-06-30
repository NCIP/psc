package edu.northwestern.bioinformatics.studycalendar.web.template;

import edu.northwestern.bioinformatics.studycalendar.web.PscAbstractController;
import edu.northwestern.bioinformatics.studycalendar.utils.breadcrumbs.DefaultCrumb;
import edu.northwestern.bioinformatics.studycalendar.utils.breadcrumbs.BreadcrumbContext;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Amendment;
import edu.northwestern.bioinformatics.studycalendar.dao.StudyDao;
import edu.northwestern.bioinformatics.studycalendar.dao.delta.AmendmentDao;
import edu.northwestern.bioinformatics.studycalendar.service.DeltaService;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.bind.ServletRequestUtils;
import org.springframework.beans.factory.annotation.Required;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;
import java.util.HashMap;

/**
 * @author Jalpa Patel
 */
public class SchedulePreviewController  extends PscAbstractController {
    private StudyDao studyDao;
    private AmendmentDao amendmentDao;
    private Study study;
    private Amendment amendment;
    private DeltaService deltaService;

    public SchedulePreviewController() {
        setCrumb(new Crumb());
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
        public Map<String, String> getParameters(BreadcrumbContext context) {
            Map<String, String> params = createParameters("study", context.getStudy().getId().toString());
            return params;
        }
    }
}
