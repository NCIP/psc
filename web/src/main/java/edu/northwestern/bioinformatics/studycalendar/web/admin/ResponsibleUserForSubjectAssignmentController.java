package edu.northwestern.bioinformatics.studycalendar.web.admin;

import edu.northwestern.bioinformatics.studycalendar.configuration.Configuration;
import edu.northwestern.bioinformatics.studycalendar.core.accesscontrol.ApplicationSecurityManager;
import edu.northwestern.bioinformatics.studycalendar.dao.StudySubjectAssignmentDao;
import edu.northwestern.bioinformatics.studycalendar.security.authorization.PscUser;
import edu.northwestern.bioinformatics.studycalendar.service.PscUserService;
import edu.northwestern.bioinformatics.studycalendar.utils.editors.PscUserEditor;
import edu.northwestern.bioinformatics.studycalendar.web.PscAbstractCommandController;
import edu.northwestern.bioinformatics.studycalendar.web.accesscontrol.ResourceAuthorization;
import edu.nwu.bioinformatics.commons.spring.ValidatableValidator;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.validation.BindException;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Collection;
import java.util.Map;

import static edu.northwestern.bioinformatics.studycalendar.security.authorization.PscRole.*;

/**
 * @author Rhett Sutphin
 */
public class ResponsibleUserForSubjectAssignmentController
    extends PscAbstractCommandController<ResponsibleUserForSubjectAssignmentCommand>
{
    private ApplicationSecurityManager applicationSecurityManager;
    private StudySubjectAssignmentDao studySubjectAssignmentDao;
    private PscUserService pscUserService;
    private Configuration configuration;

    public ResponsibleUserForSubjectAssignmentController() {
        setValidator(new ValidatableValidator());
        setCommandClass(ResponsibleUserForSubjectAssignmentCommand.class);
    }

    @Override
    public Collection<ResourceAuthorization> authorizations(
        String httpMethod, Map<String, String[]> queryParameters
    ) throws Exception {
        return ResourceAuthorization.createCollection(STUDY_TEAM_ADMINISTRATOR);
    }

    @Override
    protected ResponsibleUserForSubjectAssignmentCommand getCommand(
        HttpServletRequest request
    ) throws Exception {
        return new ResponsibleUserForSubjectAssignmentCommand(
            applicationSecurityManager.getUser(), pscUserService, studySubjectAssignmentDao, configuration);
    }

    @Override
    protected void initBinder(
        HttpServletRequest request, ServletRequestDataBinder binder
    ) throws Exception {
        super.initBinder(request, binder);
        binder.registerCustomEditor(PscUser.class, new PscUserEditor(pscUserService));
        getControllerTools().registerDomainObjectEditor(
            binder, "targetAssignments", studySubjectAssignmentDao);
    }

    @Override
    protected ModelAndView handle(
        ResponsibleUserForSubjectAssignmentCommand command, BindException errors,
        HttpServletRequest request, HttpServletResponse response
    ) throws Exception {
        boolean submitAttempt = request.getMethod().equals("POST");
        if (submitAttempt && !errors.hasErrors()) {
            command.apply();
            return new ModelAndView("redirectToTeamAdmin");
        } else {
            ModelAndView mv = new ModelAndView("admin/responsibleUser", errors.getModel());
            mv.addObject("initialShow", !submitAttempt);
            return mv;
        }
    }

    ////// CONFIGURATION

    @Required
    public void setApplicationSecurityManager(
        ApplicationSecurityManager applicationSecurityManager
    ) {
        this.applicationSecurityManager = applicationSecurityManager;
    }

    @Required
    public void setStudySubjectAssignmentDao(StudySubjectAssignmentDao studySubjectAssignmentDao) {
        this.studySubjectAssignmentDao = studySubjectAssignmentDao;
    }

    @Required
    public void setPscUserService(PscUserService pscUserService) {
        this.pscUserService = pscUserService;
    }

    @Required
    public void setConfiguration(Configuration configuration) {
        this.configuration = configuration;
    }
}
