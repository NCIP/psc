package edu.northwestern.bioinformatics.studycalendar.web.schedule;

import edu.northwestern.bioinformatics.studycalendar.dao.ActivityDao;
import edu.northwestern.bioinformatics.studycalendar.dao.StudyDao;
import edu.northwestern.bioinformatics.studycalendar.web.PscCancellableFormController;
import edu.northwestern.bioinformatics.studycalendar.web.ControllerTools;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.utils.accesscontrol.AccessControl;
import edu.northwestern.bioinformatics.studycalendar.utils.accesscontrol.StudyCalendarProtectionGroup;
import edu.northwestern.bioinformatics.studycalendar.service.StudyService;
import edu.nwu.bioinformatics.commons.spring.ValidatableValidator;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.validation.BindException;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.bind.ServletRequestUtils;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import gov.nih.nci.cabig.ctms.lang.NowFactory;

import java.util.Map;
import java.util.HashMap;
import java.util.Date;

@AccessControl(protectionGroups = {StudyCalendarProtectionGroup.STUDY_ADMINISTRATOR, StudyCalendarProtectionGroup.SITE_COORDINATOR})
public class ScheduleReconsentController extends PscCancellableFormController {
    private StudyService studyService;
    private NowFactory nowFactory;
    private StudyDao studyDao;

    public ScheduleReconsentController() {
        setCommandClass(ScheduleReconsentCommand.class);
        setFormView("schedule/scheduleReconsent");
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
        ScheduleReconsentCommand command = (ScheduleReconsentCommand) oCommand;

        command.apply();

        return new ModelAndView(new RedirectView(getSuccessView()));
    }

    protected Object formBackingObject(HttpServletRequest request) throws Exception {
        return new ScheduleReconsentCommand(studyService, nowFactory);
    }

    protected ModelAndView onCancel(Object command) throws Exception {
		return new ModelAndView(new RedirectView(getCancelView()));
	}

    @Required
    public void setStudyService(StudyService studyService) throws Exception {
        this.studyService = studyService;
    }

    @Required
    public void setNowFactory(NowFactory nowFactory) {
        this.nowFactory = nowFactory;
    }

    @Required
    public void setStudyDao(StudyDao studyDao) {
        this.studyDao = studyDao;
    }
}
