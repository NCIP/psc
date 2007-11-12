package edu.northwestern.bioinformatics.studycalendar.web.template;

import edu.northwestern.bioinformatics.studycalendar.web.PscSimpleFormController;
import edu.northwestern.bioinformatics.studycalendar.service.TemplateService;
import edu.northwestern.bioinformatics.studycalendar.service.SiteService;
import edu.northwestern.bioinformatics.studycalendar.service.ParticipantCoordinatorDashboardService;
import edu.northwestern.bioinformatics.studycalendar.dao.ScheduledActivityDao;
import edu.northwestern.bioinformatics.studycalendar.dao.StudyDao;
import edu.northwestern.bioinformatics.studycalendar.dao.UserDao;
import edu.northwestern.bioinformatics.studycalendar.utils.accesscontrol.ApplicationSecurityManager;
import edu.northwestern.bioinformatics.studycalendar.utils.accesscontrol.AccessControl;
import edu.northwestern.bioinformatics.studycalendar.utils.editors.ControlledVocabularyEditor;
import edu.northwestern.bioinformatics.studycalendar.utils.breadcrumbs.DefaultCrumb;
import edu.northwestern.bioinformatics.studycalendar.utils.breadcrumbs.BreadcrumbContext;
import edu.northwestern.bioinformatics.studycalendar.domain.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.bind.ServletRequestUtils;
import org.springframework.validation.BindException;
import org.springframework.beans.propertyeditors.CustomNumberEditor;
import org.springframework.beans.factory.annotation.Required;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;

@AccessControl(roles = Role.PARTICIPANT_COORDINATOR)
public class ColleagueParticipantCoordinatorController extends PscSimpleFormController {
	private TemplateService templateService;

    private ScheduledActivityDao scheduledActivityDao;
	private StudyDao studyDao;
    private UserDao userDao;
    private SiteService siteService;
    private ParticipantCoordinatorDashboardService participantCoordinatorDashboardService;

    private static final Logger log = LoggerFactory.getLogger(ColleagueParticipantCoordinatorController.class.getName());

    public ColleagueParticipantCoordinatorController() {
        setCommandClass(ScheduleCommand.class);
        setBindOnNewForm(true);
        setCrumb(new Crumb());
    }


    public ModelAndView showForm(HttpServletRequest request, HttpServletResponse response, BindException errors) throws Exception {
        setFormView("/colleagueParticipantCoordinatorSchedule");
        return showForm(request, response, errors, null);
    }

    protected Map<String, Object> referenceData(HttpServletRequest httpServletRequest) throws Exception {
        Map<String, Object> model = new HashMap<String, Object>();
        Integer colleagueId = ServletRequestUtils.getIntParameter(httpServletRequest, "id");

        Collection<Site> sitesForCollegueUser = siteService.getSitesForParticipantCoordinator(userDao.getById(colleagueId).getName());
        List<Site> sitesToDisplay = getSitesToDisplay(ApplicationSecurityManager.getUser(), userDao.getById(colleagueId).getName());

        if (sitesToDisplay.size()< sitesForCollegueUser.size()) {
            int numberOfUnauthorizedSites = sitesForCollegueUser.size() - sitesToDisplay.size();
            model.put("extraSites", "** Please note: There are " + numberOfUnauthorizedSites + " site(s) that are not authorized for viewing");
        }

        model.put("userName", userDao.getById(colleagueId));
        model.put("ownedStudies", getColleaguesStudies(colleagueId));
        model.put("mapOfUserAndCalendar", getPAService().getMapOfCurrentEvents(getStudyParticipantAssignments(colleagueId), 7));
        model.put("pastDueActivities", getPAService().getMapOfOverdueEvents(getStudyParticipantAssignments(colleagueId)));
        model.put("activityTypes", ActivityType.values());

        return model;
    }

    private List<Site> getSitesToDisplay(String userName, String colleagueName) {
        Collection<Site> sitesForCurrentUser = siteService.getSitesForParticipantCoordinator(userName);
        Collection<Site> sitesForCollegueUser = siteService.getSitesForParticipantCoordinator(colleagueName);

        List<Site> sitesToDisplay = new ArrayList<Site>();
        for (Site site: sitesForCurrentUser) {
            if (sitesForCollegueUser.contains(site)) {
                sitesToDisplay.add(site);
            }
        }
        return sitesToDisplay;
    }

    private List<Study> getColleaguesStudies(Integer colleagueId) throws Exception {
        String userName = ApplicationSecurityManager.getUser();
        List<Study> studies = studyDao.getAll();
        List<Study> ownedStudies = templateService.checkOwnership(userName, studies);

        User colleagueUser = userDao.getById(colleagueId);
        List<Site> sitesToDisplay = getSitesToDisplay(userName, colleagueUser.getName());

        List<Study> colleaguesStudies = new ArrayList<Study>();
        for (Site site : sitesToDisplay) {
            List<StudySite> studySites= site.getStudySites();
            for (StudySite studySite : studySites) {
                Study study = studySite.getStudy();
                if (ownedStudies.contains(study)) {
                    colleaguesStudies.add(study);
                }
            }
        }
        return colleaguesStudies;
    }

    private List<StudyParticipantAssignment> getStudyParticipantAssignments(Integer colleagueId) throws Exception {
        List<Study> colleaguesStudies = getColleaguesStudies(colleagueId);

        List<StudyParticipantAssignment> studyParticipantAssignments = getUserDao().getAssignments(userDao.getById(colleagueId));
        List<StudyParticipantAssignment> colleagueOnlystudyParticipantAssignments = new ArrayList<StudyParticipantAssignment>();
        for (StudyParticipantAssignment studyParticipantAssignment: studyParticipantAssignments) {
            Study study = studyParticipantAssignment.getStudySite().getStudy();
            if (colleaguesStudies.contains(study)) {
                colleagueOnlystudyParticipantAssignments.add(studyParticipantAssignment);
            }
        }

        return colleagueOnlystudyParticipantAssignments;
    }


    protected Object formBackingObject(HttpServletRequest httpServletRequest) throws Exception {
        ScheduleCommand command = new ScheduleCommand();
        command.setToDate(7);
        return command;
    }

    protected ModelAndView onSubmit(HttpServletRequest request,
                                    HttpServletResponse response,
                                    Object oCommand, BindException errors) throws Exception {
        ScheduleCommand scheduleCommand = (ScheduleCommand) oCommand;
        Integer colleagueId = ServletRequestUtils.getIntParameter(request, "id");
        User user = userDao.getById(colleagueId);

        scheduleCommand.setUser(user);
        scheduleCommand.setUserDao(userDao);
        scheduleCommand.setScheduledActivityDao(scheduledActivityDao);
        Map<String, Object> model = scheduleCommand.execute(getPAService(), getStudyParticipantAssignments(colleagueId));
        return new ModelAndView("template/ajax/listOfParticipantsAndEvents", model);
    }

    protected void initBinder(HttpServletRequest httpServletRequest,
                              ServletRequestDataBinder servletRequestDataBinder) throws Exception {
        super.initBinder(httpServletRequest, servletRequestDataBinder);
        servletRequestDataBinder.registerCustomEditor(Integer.class, new CustomNumberEditor(Integer.class, false));
        servletRequestDataBinder.registerCustomEditor(ActivityType.class, new ControlledVocabularyEditor(ActivityType.class));

    }

    ////// CONFIGURATION
    @Required
    public void setTemplateService(TemplateService templateService) {
        this.templateService = templateService;
    }
    @Required
    public void setStudyDao(StudyDao studyDao) {
        this.studyDao = studyDao;
    }
    @Required
    public void setUserDao(UserDao userDao) {
        this.userDao = userDao;
    }
    public UserDao getUserDao() {
        return userDao;
    }
    @Required
    public void setScheduledActivityDao(ScheduledActivityDao scheduledActivityDao) {
        this.scheduledActivityDao = scheduledActivityDao;
    }
    public ScheduledActivityDao getScheduledActivityDao() {
        return scheduledActivityDao;
    }

    public ParticipantCoordinatorDashboardService getPAService() {
        return participantCoordinatorDashboardService;
    }

    public void setParticipantCoordinatorDashboardService(ParticipantCoordinatorDashboardService participantCoordinatorDashboardService) {
        this.participantCoordinatorDashboardService = participantCoordinatorDashboardService;
    }

    @Required
    public void setSiteService(SiteService siteService) {
        this.siteService = siteService;
    }

    private static class Crumb extends DefaultCrumb {
       @Override
        public String getName(BreadcrumbContext context) {
            return "Colleague Dashboard";
        }

        @Override
        public Map<String, String> getParameters(BreadcrumbContext context) {
            User user = context.getUser();

            Map<String, String> params = new HashMap<String, String>();
            if (user != null && user.getId() != null) {
                params.put("id", user.getId().toString());
            }
            return params;

        }
    }
}
