package edu.northwestern.bioinformatics.studycalendar.web.template;

import edu.northwestern.bioinformatics.studycalendar.domain.*;
import edu.northwestern.bioinformatics.studycalendar.web.PscSimpleFormController;
import edu.northwestern.bioinformatics.studycalendar.dao.*;
import edu.northwestern.bioinformatics.studycalendar.utils.accesscontrol.ApplicationSecurityManager;
import edu.northwestern.bioinformatics.studycalendar.service.TemplateService;
import edu.northwestern.bioinformatics.studycalendar.service.SiteService;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.bind.ServletRequestUtils;
import org.springframework.validation.BindException;
import org.springframework.validation.Errors;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.beans.propertyeditors.CustomNumberEditor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.acegisecurity.context.SecurityContextHolder;
import org.acegisecurity.context.SecurityContext;
import org.acegisecurity.Authentication;
import org.acegisecurity.GrantedAuthority;

import java.util.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.text.ParseException;


//@AccessControl(roles = Role.PARTICIPANT_COORDINATOR)
public class ScheduleController extends PscSimpleFormController {
    private SiteDao siteDao;
	private TemplateService templateService;
	private StudyDao studyDao;
    private StudySiteDao studySiteDao;
    private UserDao userDao;

    private SiteService siteService;
    private String userName;
    private User user;
    private ScheduledEventDao scheduledEventDao;

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
        userName = ApplicationSecurityManager.getUser();
        List<Study> studies = studyDao.getAll();
        List<Study> ownedStudies = templateService.checkOwnership(userName, studies);
        List<Site> ownedSites = siteService.getSitesForSiteCd(userName);

        user = userDao.getByName(userName);


        List<StudyParticipantAssignment> studyParticipantAssignments = getUserDao().getAssignments(user);

        Map<String, Object> model = new HashMap<String, Object>();
        model.put("userName", userName);
        model.put("ownedStudies", ownedStudies);
        model.put("sites", ownedSites);

        model.put("mapOfUserAndCalendar", getMapOfCurrentEvents(studyParticipantAssignments));
        model.put("pastDueActivities", getMapOfOverdueEvents(studyParticipantAssignments));
        return model;
    }

    public Map<Object, Object> getMapOfOverdueEvents(List<StudyParticipantAssignment> studyParticipantAssignments) {
        Date endDate = new Date();
        //list of events overtue
        Map <Object, Object> participantAndOverDueEvents = new HashMap<Object, Object>();
        for (StudyParticipantAssignment studyParticipantAssignment : studyParticipantAssignments) {
            List<ScheduledEvent> events = new ArrayList<ScheduledEvent>();

            Map<Object, Integer> key = new HashMap<Object, Integer>();

            ScheduledCalendar calendar = studyParticipantAssignment.getScheduledCalendar();

            Date startDate = studyParticipantAssignment.getStartDateEpoch();
            Collection<ScheduledEvent> collectionOfEvents = getScheduledEventDao().getEventsByDate(calendar, startDate, endDate);
            Participant participant = studyParticipantAssignment.getParticipant();
            String participantName = participant.getFullName();
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

    public Map<String, Object> getMapOfCurrentEvents(List<StudyParticipantAssignment> studyParticipantAssignments) {
        Date startDate = new Date();
        int initialShiftDate = 7;
        Collection<ScheduledEvent> collectionOfEvents = null;
        SortedMap<String, Object> mapOfUserAndCalendar = new TreeMap<String, Object>();
        Map <String, Object> participantAndEvents = new HashMap<String, Object>();
        for (int i =0; i< initialShiftDate; i++) {
            Date tempStartDate = shiftStartDayByNumberOfDays(startDate, i);
            List<ScheduledEvent> events = new ArrayList<ScheduledEvent>();
            for (StudyParticipantAssignment studyParticipantAssignment : studyParticipantAssignments) {
                ScheduledCalendar calendar = studyParticipantAssignment.getScheduledCalendar();
                collectionOfEvents = getScheduledEventDao().getEventsByDate(calendar, tempStartDate, tempStartDate);

                Participant participant = studyParticipantAssignment.getParticipant();
                String participantName = participant.getFullName();

                for (ScheduledEvent event : collectionOfEvents) {
                    String participantAndEventsKey = participantName + " - " + event.getActivity().getName();
                    participantAndEvents.put(participantAndEventsKey, event);
                    events.add(event);
                }
            }
            String keyDate = formatDateToString(tempStartDate);
            mapOfUserAndCalendar.put(keyDate, participantAndEvents);
        }
       return mapOfUserAndCalendar; 
    }

    public Date shiftStartDayByNumberOfDays(Date startDate, Integer numberOfDays) {
        java.sql.Timestamp timestampTo = new java.sql.Timestamp(startDate.getTime());
        long oneDay = numberOfDays * 24 * 60 * 60 * 1000;
        timestampTo.setTime(timestampTo.getTime() + oneDay);
        Date d = (Date)timestampTo;
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        String dateString = df.format(d);
        Date d1 = null;
        try {
            d1 = df.parse(dateString);
        } catch (ParseException e) {
            log.debug("Exception " + e);
            d1 = startDate;
        }
        return d1;
    }

    public String formatDateToString(Date date) {
        DateFormat df = new SimpleDateFormat("MM/dd");
        return df.format(date);
    }


    protected Object formBackingObject(HttpServletRequest httpServletRequest) throws Exception {
        ScheduleCommand command = new ScheduleCommand(siteDao);
        command.setToDate(7);
        return command;
    }

    protected ModelAndView onSubmit(HttpServletRequest request,
                                    HttpServletResponse response,
                                    Object oCommand, BindException errors) throws Exception {
        ScheduleCommand scheduleCommand = (ScheduleCommand) oCommand;
        scheduleCommand.setUser(user);
        scheduleCommand.setUserDao(userDao);
        scheduleCommand.setScheduledEventDao(scheduledEventDao);
        Map<String, Object> model = scheduleCommand.execute();
        return new ModelAndView("template/ajax/listOfParticipantsAndEvents", model);
    }

    protected void initBinder(HttpServletRequest httpServletRequest,
                              ServletRequestDataBinder servletRequestDataBinder) throws Exception {
        super.initBinder(httpServletRequest, servletRequestDataBinder);
        getControllerTools().registerDomainObjectEditor(servletRequestDataBinder, "site", siteDao);
        servletRequestDataBinder.registerCustomEditor(Integer.class, new CustomNumberEditor(Integer.class, false));

    }

    ////// CONFIGURATION
    @Required
    public void setSiteDao(SiteDao siteDao) {
        this.siteDao = siteDao;
    }
    @Required
    public void setTemplateService(TemplateService templateService) {
        this.templateService = templateService;
    }
    @Required
    public void setStudyDao(StudyDao studyDao) {
        this.studyDao = studyDao;
    }
    @Required
    public void setSiteService(SiteService siteService) {
        this.siteService = siteService;
    }
    @Required
    public void setStudySiteDao(StudySiteDao studySiteDao) {
        this.studySiteDao = studySiteDao;
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
}