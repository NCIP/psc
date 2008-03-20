package edu.northwestern.bioinformatics.studycalendar.web.template;

import edu.northwestern.bioinformatics.studycalendar.web.PscAbstractCommandController;
import edu.northwestern.bioinformatics.studycalendar.dao.StudyCalendarDao;
import edu.northwestern.bioinformatics.studycalendar.dao.StudyDao;
import edu.northwestern.bioinformatics.studycalendar.utils.accesscontrol.AccessControl;
import edu.northwestern.bioinformatics.studycalendar.domain.Role;
import edu.northwestern.bioinformatics.studycalendar.service.AmendmentService;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.validation.BindException;
import org.springframework.beans.factory.annotation.Required;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author Rhett Sutphin
 */
@AccessControl(roles = Role.STUDY_COORDINATOR)
public class DeleteDevelopmentAmendmentController
    extends PscAbstractCommandController<DeleteDevelopmentAmendmentCommand>
{
    private StudyDao studyDao;
    private AmendmentService amendmentService;

    public DeleteDevelopmentAmendmentController() {
        setCommandClass(DeleteDevelopmentAmendmentCommand.class);
    }

    protected void initBinder(HttpServletRequest request, ServletRequestDataBinder binder) throws Exception {
        getControllerTools().registerDomainObjectEditor(binder, "study", studyDao);
    }

    protected Object getCommand(HttpServletRequest request) throws Exception {
        return new DeleteDevelopmentAmendmentCommand(amendmentService);
    }

    @Override
    protected ModelAndView handle(DeleteDevelopmentAmendmentCommand command, BindException errors, HttpServletRequest request, HttpServletResponse response) throws Exception {
        command.apply();
        return new ModelAndView("redirectToStudyList");
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
}
