package edu.northwestern.bioinformatics.studycalendar.web.template;

import org.springframework.web.servlet.ModelAndView;
import org.springframework.validation.BindException;
import org.springframework.validation.Errors;
import org.springframework.beans.factory.annotation.Required;

import javax.servlet.http.HttpServletRequest;

import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.ui.ModelMap;

import edu.northwestern.bioinformatics.studycalendar.dao.StudyDao;
import edu.northwestern.bioinformatics.studycalendar.web.PscSimpleFormController;
import edu.northwestern.bioinformatics.studycalendar.utils.accesscontrol.AccessControl;
import edu.northwestern.bioinformatics.studycalendar.utils.accesscontrol.StudyCalendarProtectionGroup;
import edu.northwestern.bioinformatics.studycalendar.utils.accesscontrol.ApplicationSecurityManager;
import edu.northwestern.bioinformatics.studycalendar.utils.breadcrumbs.DefaultCrumb;
import edu.northwestern.bioinformatics.studycalendar.utils.breadcrumbs.BreadcrumbContext;
import edu.northwestern.bioinformatics.studycalendar.service.AmendmentService;
import edu.northwestern.bioinformatics.studycalendar.service.DeltaService;
import edu.northwestern.bioinformatics.studycalendar.domain.*;
import static edu.northwestern.bioinformatics.studycalendar.domain.Role.SUBJECT_COORDINATOR;

import java.util.*;

/**
 * @author Jaron Sampson
 * @author Rhett Sutphin
 */
@AccessControl(roles = Role.STUDY_COORDINATOR)
public class ReleaseAmendmentController extends PscSimpleFormController {
    private StudyDao studyDao;
    private AmendmentService amendmentService;
    private DeltaService deltaService;
    private static final String UNNAMED_EPOCH = "[Unnamed epoch]";

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
        ReleaseAmendmentCommand command = (ReleaseAmendmentCommand) oCommand;
        Study theRevisedStudy = deltaService.revise(command.getStudy(), command.getStudy().getDevelopmentAmendment());

        List<Epoch> epochs = theRevisedStudy.getPlannedCalendar().getEpochs();

        ModelMap model = new ModelMap("epochs", epochs);
        model.addObject("study", command.getStudy());
        return model;

    }

    @Override
    protected void initBinder(HttpServletRequest request, ServletRequestDataBinder binder) throws Exception {
        getControllerTools().registerDomainObjectEditor(binder, "study", studyDao);
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

    @Required
    public void setDeltaService(DeltaService deltaService) {
        this.deltaService = deltaService;
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
