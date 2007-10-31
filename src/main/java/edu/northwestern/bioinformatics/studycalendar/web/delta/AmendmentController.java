package edu.northwestern.bioinformatics.studycalendar.web.delta;

import edu.northwestern.bioinformatics.studycalendar.dao.StudyDao;
import edu.northwestern.bioinformatics.studycalendar.dao.delta.AmendmentDao;
import edu.northwestern.bioinformatics.studycalendar.domain.Role;
import edu.northwestern.bioinformatics.studycalendar.service.StudyService;
import edu.northwestern.bioinformatics.studycalendar.utils.accesscontrol.AccessControl;
import edu.northwestern.bioinformatics.studycalendar.web.PscCancellableFormController;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.validation.BindException;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;

@AccessControl(roles = {Role.STUDY_ADMIN, Role.SITE_COORDINATOR})
public class AmendmentController extends PscCancellableFormController {
    private StudyDao studyDao;
    private AmendmentDao amendmentDao;
    private StudyService studyService;

    public AmendmentController() {
        setCommandClass(AmendmentCommand.class);
        setFormView("template/createAmendment");
        setBindOnNewForm(true);

        setSuccessView("studyList");
        setCancelView("studyList");
    }

    @Override
    protected Object formBackingObject(HttpServletRequest request) throws Exception {
        return new AmendmentCommand(studyService, amendmentDao);
    }

    @Override
    protected void initBinder(HttpServletRequest request, ServletRequestDataBinder binder) throws Exception {
        super.initBinder(request, binder);
        getControllerTools().registerDomainObjectEditor(binder, "study", studyDao);
        binder.registerCustomEditor(Date.class, getControllerTools().getDateEditor(false));
    }

    @Override
    protected ModelAndView onSubmit(HttpServletRequest request, HttpServletResponse response, Object oCommand, BindException errors) throws Exception {
        AmendmentCommand command = (AmendmentCommand) oCommand;
        command.apply();
        return new ModelAndView( new RedirectView(getSuccessView()));
    }

    @Override
    protected ModelAndView onCancel(Object command) throws Exception {
        return new ModelAndView(new RedirectView(getCancelView()));
    }

    ////// CONFIGURATION

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
