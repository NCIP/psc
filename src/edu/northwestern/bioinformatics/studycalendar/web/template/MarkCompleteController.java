package edu.northwestern.bioinformatics.studycalendar.web.template;

import org.springframework.web.servlet.mvc.SimpleFormController;
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

import java.util.Map;
import java.util.Collections;

/**
 * @author Jaron Sampson
 * @author Rhett Sutphin
 */
@AccessControl(protectionGroups = StudyCalendarProtectionGroup.STUDY_COORDINATOR)
public class MarkCompleteController extends PscSimpleFormController {
    private StudyDao studyDao;

    public MarkCompleteController() {
        setCommandClass(MarkCompleteCommand.class);
        setFormView("markComplete");
        setSuccessView("redirectToStudyList");
        setBindOnNewForm(true);
        setCrumb(new Crumb());
    }

    @Override
    protected Object formBackingObject(HttpServletRequest request) throws Exception {
        return new MarkCompleteCommand(studyDao);
    }

    @Override
    protected Map referenceData(HttpServletRequest request, Object oCommand, Errors errors) throws Exception {
         // for breadcrumbs
        return new ModelMap("study", ((MarkCompleteCommand) oCommand).getStudy());
    }

    @Override
    protected void initBinder(HttpServletRequest request, ServletRequestDataBinder binder) throws Exception {
        ControllerTools.registerDomainObjectEditor(binder, "study", studyDao);
        binder.registerCustomEditor(Boolean.class, new CustomBooleanEditor(false));
    }

    @Override
    protected ModelAndView onSubmit(Object oCommand, BindException errors) throws Exception {
        ((MarkCompleteCommand) oCommand).apply();
        // don't want a model because it's a redirect with no params
        return new ModelAndView(getSuccessView());
    }

    ////// CONFIGURATION

    @Required
    public void setStudyDao(StudyDao studyDao) {
        this.studyDao = studyDao;
    }

    private static class Crumb extends DefaultCrumb {
        public Crumb() {
            super("Mark Complete");
        }

        public Map<String, String> getParameters(BreadcrumbContext context) {
            return Collections.singletonMap("study", context.getStudy().getId().toString());
        }
    }
}
