package edu.northwestern.bioinformatics.studycalendar.web;

import edu.northwestern.bioinformatics.studycalendar.utils.accesscontrol.AccessControl;
import edu.northwestern.bioinformatics.studycalendar.utils.accesscontrol.StudyCalendarProtectionGroup;
import edu.northwestern.bioinformatics.studycalendar.dao.StudyDao;
import edu.northwestern.bioinformatics.studycalendar.dao.AmendmentLoginDao;
import edu.northwestern.bioinformatics.studycalendar.web.AmendmentLoginCommand;
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
import java.util.Map;
import java.util.HashMap;

@AccessControl(protectionGroups = {StudyCalendarProtectionGroup.STUDY_ADMINISTRATOR, StudyCalendarProtectionGroup.SITE_COORDINATOR})
public class AmendmentLoginController extends PscCancellableFormController {
    private StudyDao studyDao;



    private AmendmentLoginDao amendmentLoginDao;
    private static final Logger log = LoggerFactory.getLogger(AmendmentLoginController.class.getName());

    public AmendmentLoginController() {
        setCommandClass(AmendmentLoginCommand.class);
        setFormView("amendmentLogin");
        setValidator(new ValidatableValidator());

        setSuccessView("studyList");
        setCancelView("studyList");
    }

    protected void initBinder(HttpServletRequest request, ServletRequestDataBinder binder) throws Exception {
        super.initBinder(request, binder);
        ControllerTools.registerDomainObjectEditor(binder, "study", studyDao);
        binder.registerCustomEditor(Date.class, ControllerTools.getDateEditor(false));
    }

    protected ModelAndView onSubmit(HttpServletRequest request, HttpServletResponse response, Object oCommand, BindException errors) throws Exception {
        AmendmentLoginCommand command = (AmendmentLoginCommand) oCommand;
        command.apply();
        return new ModelAndView( new RedirectView(getSuccessView()));
    }

    protected Object formBackingObject(HttpServletRequest request) throws Exception {
        return new AmendmentLoginCommand(studyDao, amendmentLoginDao);
    }

    protected ModelAndView onCancel(Object command) throws Exception {
		return new ModelAndView(new RedirectView(getCancelView()));
	}

    @Required
    public void setStudyDao(StudyDao studyDao) {
        this.studyDao = studyDao;
    }

    public AmendmentLoginDao getAmendmentLoginDao() {
        return amendmentLoginDao;
    }

    @Required
    public void setAmendmentLoginDao(AmendmentLoginDao amendmentLoginDao) {
        this.amendmentLoginDao = amendmentLoginDao;
    }
}
