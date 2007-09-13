package edu.northwestern.bioinformatics.studycalendar.web.delta;

import edu.northwestern.bioinformatics.studycalendar.utils.accesscontrol.AccessControl;
import edu.northwestern.bioinformatics.studycalendar.utils.accesscontrol.StudyCalendarProtectionGroup;
import edu.northwestern.bioinformatics.studycalendar.dao.StudyDao;
import edu.northwestern.bioinformatics.studycalendar.dao.delta.AmendmentDao;
import edu.northwestern.bioinformatics.studycalendar.web.delta.AmendmentCommand;
import edu.northwestern.bioinformatics.studycalendar.web.PscCancellableFormController;
import edu.northwestern.bioinformatics.studycalendar.web.ControllerTools;
import edu.northwestern.bioinformatics.studycalendar.service.StudyService;
import edu.nwu.bioinformatics.commons.spring.ValidatableValidator;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;
import org.springframework.validation.BindException;
import org.springframework.beans.factory.annotation.Required;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;

@AccessControl(protectionGroups = {StudyCalendarProtectionGroup.STUDY_ADMINISTRATOR, StudyCalendarProtectionGroup.SITE_COORDINATOR})
public class AmendmentController extends PscCancellableFormController {
    private StudyDao studyDao;
    private AmendmentDao amendmentDao;
    private StudyService studyService;

    public AmendmentController() {
        setCommandClass(AmendmentCommand.class);
        setFormView("amendmentLogin");

        setSuccessView("studyList");
        setCancelView("studyList");
    }

    protected void initBinder(HttpServletRequest request, ServletRequestDataBinder binder) throws Exception {
        super.initBinder(request, binder);
        getControllerTools().registerDomainObjectEditor(binder, "study", studyDao);
        binder.registerCustomEditor(Date.class, getControllerTools().getDateEditor(false));
    }

    protected ModelAndView onSubmit(HttpServletRequest request, HttpServletResponse response, Object oCommand, BindException errors) throws Exception {
        AmendmentCommand command = (AmendmentCommand) oCommand;
        command.apply();
        return new ModelAndView( new RedirectView(getSuccessView()));
    }

    protected Object formBackingObject(HttpServletRequest request) throws Exception {
        return new AmendmentCommand(studyService, amendmentDao);
    }

    protected ModelAndView onCancel(Object command) throws Exception {
		return new ModelAndView(new RedirectView(getCancelView()));
	}

    @Required
    public void setStudyDao(StudyDao studyDao) {
        this.studyDao = studyDao;
    }

    public AmendmentDao getAmendmentDao() {
        return amendmentDao;
    }

    @Required
    public void setAmendmentDao(AmendmentDao amendmentDao) {
        this.amendmentDao = amendmentDao;
    }

    @Required
    public void setStudyService(StudyService studyService) {
        this.studyService = studyService;
    }
}
