package edu.northwestern.bioinformatics.studycalendar.web.delta;

import edu.northwestern.bioinformatics.studycalendar.dao.StudyDao;
import edu.northwestern.bioinformatics.studycalendar.dao.delta.AmendmentDao;
import edu.northwestern.bioinformatics.studycalendar.domain.Role;
import edu.northwestern.bioinformatics.studycalendar.service.StudyService;
import edu.northwestern.bioinformatics.studycalendar.utils.accesscontrol.AccessControl;
import edu.northwestern.bioinformatics.studycalendar.web.PscCancellableFormController;
import edu.nwu.bioinformatics.commons.spring.ValidatableValidator;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.validation.BindException;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;

@AccessControl(roles = Role.STUDY_COORDINATOR)
public class CreateAmendmentController extends PscCancellableFormController {
    private StudyDao studyDao;
    private AmendmentDao amendmentDao;
    private StudyService studyService;

    public CreateAmendmentController() {
        setCommandClass(CreateAmendmentCommand.class);
        setFormView("delta/createAmendment");
        setBindOnNewForm(true);
        setValidator(new ValidatableValidator());
        setSuccessView("redirectToStudyList");
        setCancelView("redirectToStudyList");
    }

    @Override
    protected Object formBackingObject(HttpServletRequest request) throws Exception {
        return new CreateAmendmentCommand(studyService);
    }

    @Override
    protected void initBinder(HttpServletRequest request, ServletRequestDataBinder binder) throws Exception {
        super.initBinder(request, binder);
        getControllerTools().registerDomainObjectEditor(binder, "study", studyDao);
        binder.registerCustomEditor(Date.class, getControllerTools().getDateEditor(false));
    }

    @Override
    protected ModelAndView onSubmit(HttpServletRequest request, HttpServletResponse response, Object oCommand, BindException errors) throws Exception {
        CreateAmendmentCommand command = (CreateAmendmentCommand) oCommand;
        command.apply();
        return new ModelAndView(getSuccessView());
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
