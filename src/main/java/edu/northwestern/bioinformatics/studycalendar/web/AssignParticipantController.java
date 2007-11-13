package edu.northwestern.bioinformatics.studycalendar.web;

import edu.northwestern.bioinformatics.studycalendar.dao.*;
import edu.northwestern.bioinformatics.studycalendar.domain.*;
import edu.northwestern.bioinformatics.studycalendar.service.ParticipantService;
import edu.northwestern.bioinformatics.studycalendar.service.StudySiteService;
import edu.northwestern.bioinformatics.studycalendar.service.SiteService;
import edu.northwestern.bioinformatics.studycalendar.utils.accesscontrol.AccessControl;
import edu.northwestern.bioinformatics.studycalendar.utils.accesscontrol.ApplicationSecurityManager;
import edu.northwestern.bioinformatics.studycalendar.utils.breadcrumbs.DefaultCrumb;
import edu.northwestern.bioinformatics.studycalendar.utils.breadcrumbs.BreadcrumbContext;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.validation.BindException;
import org.springframework.validation.Errors;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.bind.ServletRequestUtils;
import org.springframework.web.servlet.ModelAndView;
import org.apache.commons.lang.StringUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;

/**
 * @author Padmaja Vedula
 */
@AccessControl(roles = Role.PARTICIPANT_COORDINATOR)
public class AssignParticipantController extends PscSimpleFormController {
    private ParticipantDao participantDao;
    private ParticipantService participantService;
    private SiteService siteService;
    private StudyDao studyDao;
    private ArmDao armDao;
    private UserDao userDao;
    private SiteDao siteDao;


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
        getControllerTools().registerDomainObjectEditor(binder, "site", siteDao);
        getControllerTools().registerDomainObjectEditor(binder, "study", studyDao);
        getControllerTools().registerDomainObjectEditor(binder, "participant", participantDao);
    }

    protected Map<String, Object> referenceData(HttpServletRequest httpServletRequest) throws Exception {
        Map<String, Object> refdata = new HashMap<String, Object>();
        Collection<Participant> participants = participantDao.getAll();
        Study study = studyDao.getById(ServletRequestUtils.getRequiredIntParameter(httpServletRequest, "id"));

        refdata.put("sites", getAvailableSites(study));
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

        Study study = studyDao.getById(ServletRequestUtils.getRequiredIntParameter(request, "id"));
        Integer siteId = ServletRequestUtils.getIntParameter(request, "siteId");

        List<Site> availableSites = getAvailableSites(study);
        Site defaultSite  = (siteId != null) ? siteDao.getById(siteId) : availableSites.get(0);
        command.setSite(defaultSite);

        return command;
    }

    private List<Site> getAvailableSites(Study study) {
        String userName = ApplicationSecurityManager.getUser();
        Collection<Site> availableSites = siteService.getSitesForParticipantCoordinator(userName, study);
        return new ArrayList<Site>(availableSites);
    }

    ////// CONFIGURATION

    @Required
    public void setParticipantDao(ParticipantDao participantDao) {
        this.participantDao = participantDao;
    }

    @Required
    public void setSiteDao(SiteDao siteDao) {
        this.siteDao = siteDao;
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

    @Required
    public void setSiteService(SiteService siteService) {
        this.siteService = siteService;
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
