package edu.northwestern.bioinformatics.studycalendar.web.template;

import edu.northwestern.bioinformatics.studycalendar.domain.*;
import edu.northwestern.bioinformatics.studycalendar.web.PscSimpleFormController;
import edu.northwestern.bioinformatics.studycalendar.dao.*;
import edu.northwestern.bioinformatics.studycalendar.utils.accesscontrol.ApplicationSecurityManager;
import edu.northwestern.bioinformatics.studycalendar.utils.editors.ControlledVocabularyEditor;
import edu.northwestern.bioinformatics.studycalendar.service.TemplateService;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.validation.BindException;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.beans.propertyeditors.CustomNumberEditor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.acegisecurity.context.SecurityContextHolder;
import org.acegisecurity.context.SecurityContext;
import org.acegisecurity.Authentication;
import org.acegisecurity.GrantedAuthority;

import java.util.*;

import edu.northwestern.bioinformatics.studycalendar.service.ParticipantCoordinatorDashboardService;


public class ScheduleController extends PscSimpleFormController {
	private TemplateService templateService;

    private ScheduledEventDao scheduledEventDao;
	private StudyDao studyDao;
    private UserDao userDao;

    private ParticipantCoordinatorDashboardService participantCoordinatorDashboardService;

    private static final Logger log = LoggerFactory.getLogger(ScheduleController.class.getName());

    public ScheduleController() {
        setCommandClass(ScheduleCommand.class);
        setBindOnNewForm(true);
    }


    protected ModelAndView showForm(HttpServletRequest request, HttpServletResponse response, BindException errors) throws Exception {
        SecurityContext securityContext = SecurityContextHolder.getContext();
        Authentication authentication = securityContext.getAuthentication();
        GrantedAuthority[] authority = authentication.getAuthorities();
        for (int i =0; i< authority.length; i++) {
            if(authority[i].toString().equals(Role.PARTICIPANT_COORDINATOR.toString())) {
                setFormView("participantCoordinatorSchedule");
            } else {
                //nothing so far
            }
        }
        return showForm(request, response, errors, null);
    }

    protected Map<String, Object> referenceData(HttpServletRequest httpServletRequest) throws Exception {
        String userName = ApplicationSecurityManager.getUser();
        List<Study> studies = studyDao.getAll();
        List<Study> ownedStudies = templateService.checkOwnership(userName, studies);
        User user = userDao.getByName(userName);
        List<StudyParticipantAssignment> studyParticipantAssignments = getUserDao().getAssignments(user);

        Map<String, Object> model = new HashMap<String, Object>();
        model.put("userName", userName);
        model.put("ownedStudies", ownedStudies);
        model.put("mapOfUserAndCalendar", getPAService().getMapOfCurrentEvents(studyParticipantAssignments, 7));
        model.put("pastDueActivities", getMapOfOverdueEvents(studyParticipantAssignments));
        model.put("activityTypes", ActivityType.values());
        
        return model;
    }

    public Map<Object, Object> getMapOfOverdueEvents(List<StudyParticipantAssignment> studyParticipantAssignments) {
        Date currentDate = new Date();
        Date endDate = participantCoordinatorDashboardService.shiftStartDayByNumberOfDays(currentDate, -1);

        //list of events overtue
        Map <Object, Object> participantAndOverDueEvents = new HashMap<Object, Object>();
        for (StudyParticipantAssignment studyParticipantAssignment : studyParticipantAssignments) {
            List<ScheduledEvent> events = new ArrayList<ScheduledEvent>();

            Map<Object, Integer> key = new HashMap<Object, Integer>();

            ScheduledCalendar calendar = studyParticipantAssignment.getScheduledCalendar();

            Date startDate = studyParticipantAssignment.getStartDateEpoch();
            Collection<ScheduledEvent> collectionOfEvents = getScheduledEventDao().getEventsByDate(calendar, startDate, endDate);
            Participant participant = studyParticipantAssignment.getParticipant();

            for (ScheduledEvent event : collectionOfEvents) {
                if(event.getCurrentState().getMode().equals(ScheduledEventMode.SCHEDULED)) {
                    events.add(event);
                }
            }
            if (events.size()>0) {
                key.put(participant, events.size());
                participantAndOverDueEvents.put(key, studyParticipantAssignment);
            }
        }
        return participantAndOverDueEvents;
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
        String userName = ApplicationSecurityManager.getUser();
        User user = userDao.getByName(userName);
        scheduleCommand.setUser(user);
        scheduleCommand.setUserDao(userDao);
        scheduleCommand.setScheduledEventDao(scheduledEventDao);
        Map<String, Object> model = scheduleCommand.execute(getPAService());
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
    public void setScheduledEventDao(ScheduledEventDao scheduledEventDao) {
        this.scheduledEventDao = scheduledEventDao;
    }
    public ScheduledEventDao getScheduledEventDao() {
        return scheduledEventDao;
    }

    public ParticipantCoordinatorDashboardService getPAService() {
        return participantCoordinatorDashboardService;
    }

    public void setParticipantCoordinatorDashboardService(ParticipantCoordinatorDashboardService participantCoordinatorDashboardService) {
        this.participantCoordinatorDashboardService = participantCoordinatorDashboardService;
    }
}