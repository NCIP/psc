package edu.northwestern.bioinformatics.studycalendar.web;

import edu.northwestern.bioinformatics.studycalendar.dao.*;
import edu.northwestern.bioinformatics.studycalendar.domain.*;
import edu.northwestern.bioinformatics.studycalendar.service.ParticipantService;
import edu.northwestern.bioinformatics.studycalendar.utils.accesscontrol.AccessControl;
import edu.northwestern.bioinformatics.studycalendar.utils.accesscontrol.ApplicationSecurityManager;
import edu.northwestern.bioinformatics.studycalendar.utils.breadcrumbs.DefaultCrumb;
import edu.northwestern.bioinformatics.studycalendar.utils.breadcrumbs.BreadcrumbContext;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.validation.BindException;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.bind.ServletRequestUtils;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Padmaja Vedula
 */
@AccessControl(roles = Role.PARTICIPANT_COORDINATOR)
public class AssignParticipantController extends PscSimpleFormController {
    private ParticipantDao participantDao;
    private ParticipantService participantService;
    private StudyDao studyDao;
    private StudySiteDao studySiteDao;
    private ArmDao armDao;
    private UserDao userDao;

    private User participantCoordinator;

    public AssignParticipantController() {
        setCommandClass(AssignParticipantCommand.class);
        setFormView("assignParticipant");
        setBindOnNewForm(true);
        setCrumb(new Crumb());
    }

    protected void initBinder(HttpServletRequest request, ServletRequestDataBinder binder) throws Exception {
        super.initBinder(request, binder);
        binder.registerCustomEditor(Date.class, getControllerTools().getDateEditor(true));
        getControllerTools().registerDomainObjectEditor(binder, "arm", armDao);
        getControllerTools().registerDomainObjectEditor(binder, "studySite", studySiteDao);
        getControllerTools().registerDomainObjectEditor(binder, "participant", participantDao);
    }

    protected Map<String, Object> referenceData(HttpServletRequest httpServletRequest) throws Exception {
        Map<String, Object> refdata = new HashMap<String, Object>();
        Collection<Participant> participants = participantDao.getAll();
        Study study = studyDao.getById(ServletRequestUtils.getRequiredIntParameter(httpServletRequest, "id"));
        // TODO: for now, there's just a single default site for each study
        refdata.put("studySite", study.getStudySites().get(0));
        refdata.put("study", study);
        refdata.put("participants", participants);
        Epoch epoch = study.getPlannedCalendar().getEpochs().get(0);
        getControllerTools().addHierarchyToModel(epoch, refdata);
        List<Arm> arms = epoch.getArms();
        if (arms.size() > 1) {
            refdata.put("arms", arms);
        } else {
            refdata.put("arms", Collections.emptyList());
        }
        return refdata;
    }

    protected ModelAndView onSubmit(HttpServletRequest request, HttpServletResponse response, Object oCommand, BindException errors) throws Exception {
        AssignParticipantCommand command = (AssignParticipantCommand) oCommand;
        String userName = ApplicationSecurityManager.getUser();
        User user = userDao.getByName(userName);
        command.setParticipantCoordinator(user);
        StudyParticipantAssignment assignment = command.assignParticipant();
        return new ModelAndView("redirectToSchedule", "assignment", assignment.getId().intValue());
    }

    protected Object formBackingObject(HttpServletRequest request) throws Exception {
        AssignParticipantCommand command = new AssignParticipantCommand();
        command.setParticipantService(participantService);
        return command;
    }

    ////// CONFIGURATION

    @Required
    public void setParticipantDao(ParticipantDao participantDao) {
        this.participantDao = participantDao;
    }

    @Required
    public void setStudySiteDao(StudySiteDao studySiteDao) {
        this.studySiteDao = studySiteDao;
    }

    @Required
    public void setStudyDao(StudyDao studyDao) {
        this.studyDao = studyDao;
    }

    @Required
    public void setArmDao(ArmDao armDao) {
        this.armDao = armDao;
    }

    @Required
    public void setUserDao(UserDao userDao) {
        this.userDao = userDao;
    }

    @Required
    public void setParticipantService(ParticipantService participantService) {
        this.participantService = participantService;
    }

    private static class Crumb extends DefaultCrumb {
        public Crumb() {
            super("Assign Participant");
        }

        public Map<String, String> getParameters(BreadcrumbContext context) {
            return createParameters("id", context.getStudy().getId().toString());
        }
    }
}
