package edu.northwestern.bioinformatics.studycalendar.web.template;

import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.service.StudyService;
import edu.northwestern.bioinformatics.studycalendar.web.PscAbstractCommandController;
import edu.northwestern.bioinformatics.studycalendar.web.accesscontrol.PscAuthorizedHandler;
import edu.northwestern.bioinformatics.studycalendar.web.accesscontrol.ResourceAuthorization;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Collection;
import java.util.Map;

import static edu.northwestern.bioinformatics.studycalendar.security.authorization.PscRole.*;

/**
 * @author Rhett Sutphin
 */
public class NewStudyController extends PscAbstractCommandController<NewStudyCommand> implements PscAuthorizedHandler {
    private StudyService studyService;


    public Collection<ResourceAuthorization> authorizations(String httpMethod, Map<String, String[]> queryParameters) {
        return ResourceAuthorization.createCollection(STUDY_CREATOR);
    }    

    @Override
    protected Object getCommand(HttpServletRequest request) throws Exception {
        return new NewStudyCommand(studyService);
    }

    @Override
    protected ModelAndView handle(NewStudyCommand command, BindException errors, HttpServletRequest request, HttpServletResponse response) throws Exception {
        Study study = command.create();
        return getControllerTools().redirectToCalendarTemplate(study.getId(), null, study.getDevelopmentAmendment().getId());
    }

    ////// CONFIGURATION

    @Required
    public void setStudyService(StudyService studyService) {
        this.studyService = studyService;
    }
}
