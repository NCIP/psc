package edu.northwestern.bioinformatics.studycalendar.web.template;

import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.service.StudyService;
import edu.northwestern.bioinformatics.studycalendar.utils.accesscontrol.AccessControl;
import edu.northwestern.bioinformatics.studycalendar.utils.accesscontrol.StudyCalendarProtectionGroup;
import edu.northwestern.bioinformatics.studycalendar.web.ControllerTools;
import edu.northwestern.bioinformatics.studycalendar.web.PscAbstractCommandController;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractCommandController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author Rhett Sutphin
 */
@AccessControl(protectionGroups = StudyCalendarProtectionGroup.STUDY_COORDINATOR)
public class NewStudyController extends PscAbstractCommandController<NewStudyCommand> {
    private StudyService studyService;

    @Override
    protected Object getCommand(HttpServletRequest request) throws Exception {
        return new NewStudyCommand(studyService);
    }

    @Override
    protected ModelAndView handle(NewStudyCommand command, BindException errors, HttpServletRequest request, HttpServletResponse response) throws Exception {
        Study study = command.create();
        return getControllerTools().redirectToCalendarTemplate(study.getId());
    }

    ////// CONFIGURATION

    @Required
    public void setStudyService(StudyService studyService) {
        this.studyService = studyService;
    }
}
