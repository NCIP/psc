package edu.northwestern.bioinformatics.studycalendar.web.template;

import org.springframework.web.servlet.ModelAndView;
import org.springframework.validation.BindException;
import org.springframework.validation.Errors;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.beans.propertyeditors.CustomBooleanEditor;

import javax.servlet.http.HttpServletRequest;

import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.ui.ModelMap;

import edu.northwestern.bioinformatics.studycalendar.dao.StudyDao;
import edu.northwestern.bioinformatics.studycalendar.web.ControllerTools;
import edu.northwestern.bioinformatics.studycalendar.web.PscSimpleFormController;
import edu.northwestern.bioinformatics.studycalendar.utils.accesscontrol.AccessControl;
import edu.northwestern.bioinformatics.studycalendar.utils.accesscontrol.StudyCalendarProtectionGroup;
import edu.northwestern.bioinformatics.studycalendar.utils.breadcrumbs.DefaultCrumb;
import edu.northwestern.bioinformatics.studycalendar.utils.breadcrumbs.BreadcrumbContext;
import edu.northwestern.bioinformatics.studycalendar.service.AmendmentService;

import java.util.Map;
import java.util.Collections;

/**
 * @author Jaron Sampson
 * @author Rhett Sutphin
 */
@AccessControl(protectionGroups = StudyCalendarProtectionGroup.STUDY_COORDINATOR)
public class ReleaseAmendmentController extends PscSimpleFormController {
    private StudyDao studyDao;
    private AmendmentService amendmentService;

    public ReleaseAmendmentController() {
        setCommandClass(ReleaseAmendmentCommand.class);
        setFormView("template/releaseAmendment");
        setSuccessView("redirectToStudyList");
        setBindOnNewForm(true);
        setCrumb(new Crumb());
    }

    @Override
    protected Object formBackingObject(HttpServletRequest request) throws Exception {
        return new ReleaseAmendmentCommand(amendmentService);
    }

    @Override
    protected Map referenceData(HttpServletRequest request, Object oCommand, Errors errors) throws Exception {
         // for breadcrumbs
        return new ModelMap("study", ((ReleaseAmendmentCommand) oCommand).getStudy());
    }

    @Override
    protected void initBinder(HttpServletRequest request, ServletRequestDataBinder binder) throws Exception {
        ControllerTools.registerDomainObjectEditor(binder, "study", studyDao);
    }

    @Override
    protected ModelAndView onSubmit(Object oCommand, BindException errors) throws Exception {
        ((ReleaseAmendmentCommand) oCommand).apply();
        // don't want a model because it's a redirect with no params
        return new ModelAndView(getSuccessView());
    }

    ////// CONFIGURATION

    @Required
    public void setStudyDao(StudyDao studyDao) {
        this.studyDao = studyDao;
    }

    @Required
    public void setAmendmentService(AmendmentService amendmentService) {
        this.amendmentService = amendmentService;
    }

    private static class Crumb extends DefaultCrumb {
        public Crumb() {
            super("Release");
        }

        @Override
        public Map<String, String> getParameters(BreadcrumbContext context) {
            return Collections.singletonMap("study", context.getStudy().getId().toString());
        }
    }
}
