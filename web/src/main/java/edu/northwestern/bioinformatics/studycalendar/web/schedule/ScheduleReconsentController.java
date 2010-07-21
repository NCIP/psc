package edu.northwestern.bioinformatics.studycalendar.web.schedule;

import edu.northwestern.bioinformatics.studycalendar.dao.StudyDao;
import edu.northwestern.bioinformatics.studycalendar.web.PscCancellableFormController;
import edu.northwestern.bioinformatics.studycalendar.web.accesscontrol.AccessControl;
import edu.northwestern.bioinformatics.studycalendar.service.StudyService;
import edu.northwestern.bioinformatics.studycalendar.domain.Role;
import edu.northwestern.bioinformatics.studycalendar.web.accesscontrol.PscAuthorizedHandler;
import edu.northwestern.bioinformatics.studycalendar.web.accesscontrol.ResourceAuthorization;
import edu.nwu.bioinformatics.commons.spring.ValidatableValidator;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.validation.BindException;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import gov.nih.nci.cabig.ctms.lang.NowFactory;

import java.util.Collection;
import java.util.Date;
import java.util.Map;

import static edu.northwestern.bioinformatics.studycalendar.security.authorization.PscRole.STUDY_CALENDAR_TEMPLATE_BUILDER;

@AccessControl(roles = {Role.STUDY_ADMIN, Role.SITE_COORDINATOR})
public class ScheduleReconsentController extends PscCancellableFormController implements PscAuthorizedHandler {
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

    public Collection<ResourceAuthorization> authorizations(String httpMethod, Map<String, String[]> queryParameters) {
        return ResourceAuthorization.createCollection(STUDY_CALENDAR_TEMPLATE_BUILDER);
    }

    protected void initBinder(HttpServletRequest request, ServletRequestDataBinder binder) throws Exception {
        super.initBinder(request, binder);
        getControllerTools().registerDomainObjectEditor(binder, "study", studyDao);
        binder.registerCustomEditor(Date.class, getControllerTools().getDateEditor(false));
    }

    protected ModelAndView onSubmit(HttpServletRequest request, HttpServletResponse response, Object oCommand, BindException errors) throws Exception {
        ScheduleReconsentCommand command = (ScheduleReconsentCommand) oCommand;

        command.apply();

        return new ModelAndView(new RedirectView(getSuccessView()));
    }

    @Override
    protected Object formBackingObject(HttpServletRequest request) throws Exception {
        return new ScheduleReconsentCommand(studyService, nowFactory);
    }

    @Override
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
