package edu.northwestern.bioinformatics.studycalendar.web.dashboard.subjectcoordinator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import edu.northwestern.bioinformatics.studycalendar.domain.*;
import edu.northwestern.bioinformatics.studycalendar.dao.UserDao;
import edu.northwestern.bioinformatics.studycalendar.dao.ScheduledActivityDao;
import edu.northwestern.bioinformatics.studycalendar.dao.NotificationDao;

import java.util.*;

import edu.northwestern.bioinformatics.studycalendar.service.SubjectCoordinatorDashboardService;

public class ScheduleCommand {
    private final Logger log = LoggerFactory.getLogger(getClass());

    private Integer toDate;
    private User user;
    private UserDao userDao;
    private ScheduledActivityDao scheduledActivityDao;
    private Integer notificationId;

    private List<ActivityType> activityTypes = new ArrayList<ActivityType>();
    private ActivityType activityType;
    private Boolean activityValue;

    public Map<String, Object> execute(SubjectCoordinatorDashboardService subjectCoordinatorDashboardService) {
        List<StudySubjectAssignment> studySubjectAssignments = getUserDao().getAssignments(getUser());
        Map<String, Object> model = new HashMap<String, Object>();
        model.put("mapOfUserAndCalendar", subjectCoordinatorDashboardService.getMapOfCurrentEventsForSpecificActivity(studySubjectAssignments, getToDate(), getActivityTypes()));
        model.put("numberOfDays", getToDate());
        return model;
    }

    public Map<String, Object> execute(SubjectCoordinatorDashboardService subjectCoordinatorDashboardService, List<StudySubjectAssignment> studySubjectAssignments ) {
        Map<String, Object> model = new HashMap<String, Object>();
        model.put("mapOfUserAndCalendar", subjectCoordinatorDashboardService.getMapOfCurrentEventsForSpecificActivity(studySubjectAssignments, getToDate(), getActivityTypes()));
        model.put("numberOfDays", getToDate());
        return model;
    }


    ////// BOUND PROPERTIES


    public Integer getToDate() {
        return toDate;
    }

    public void setToDate(Integer toDate) {
        this.toDate = toDate;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }


    public UserDao getUserDao() {
        return userDao;
    }

    public void setUserDao(UserDao userDao) {
        this.userDao = userDao;
    }

    public ScheduledActivityDao getScheduledActivityDao() {
        return scheduledActivityDao;
    }

    public void setScheduledActivityDao(ScheduledActivityDao scheduledActivityDao) {
        this.scheduledActivityDao = scheduledActivityDao;
    }


    public List<ActivityType> getActivityTypes() {
        return activityTypes;
    }

    public void setActivityTypes(List<ActivityType> activityTypes) {
        this.activityTypes = activityTypes;
    }

    public ActivityType getActivityType() {
        return activityType;
    }

    public void setActivityType(ActivityType activityType) {
        this.activityType = activityType;
    }

    public Boolean getActivityValue() {
        return activityValue;
    }

    public void setActivityValue(Boolean activityValue) {
        this.activityValue = activityValue;
    }


    public Integer getNotificationId() {
        return notificationId;
    }

    public void setNotificationId(Integer notificationId) {
        this.notificationId = notificationId;
    }
}
