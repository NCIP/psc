package edu.northwestern.bioinformatics.studycalendar.web.schedule;

import edu.northwestern.bioinformatics.studycalendar.dao.ScheduledCalendarDao;
import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledActivity;
import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledActivityMode;
import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledActivityState;
import edu.northwestern.bioinformatics.studycalendar.domain.Site;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.domain.StudySite;
import edu.northwestern.bioinformatics.studycalendar.domain.tools.DateFormat;
import edu.northwestern.bioinformatics.studycalendar.web.accesscontrol.PscAuthorizedCommand;
import edu.northwestern.bioinformatics.studycalendar.web.accesscontrol.ResourceAuthorization;
import edu.nwu.bioinformatics.commons.spring.Validatable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.validation.Errors;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import static edu.northwestern.bioinformatics.studycalendar.security.authorization.PscRole.*;

/**
 * @author Rhett Sutphin
 */
public class ScheduleActivityCommand implements Validatable, PscAuthorizedCommand {
    private static final Logger log = LoggerFactory.getLogger(ScheduleActivityCommand.class.getName());

    private ScheduledActivity event;
    private ScheduledActivityMode newMode;
    private String newReason;
    private Date newDate;
    private String newNotes;
    private String newTime;

    private ScheduledCalendarDao scheduledCalendarDao;

    public ScheduleActivityCommand(ScheduledCalendarDao scheduledCalendarDao) {
        this.scheduledCalendarDao = scheduledCalendarDao;
    }

    ////// LOGIC

    public void apply() {
        if (hasStateChange()) {
            event.changeState(createState());
        }
        event.setNotes(getNewNotes());
        scheduledCalendarDao.save(event.getScheduledStudySegment().getScheduledCalendar());
    }

    private boolean hasStateChange() {
        return getNewMode() != null;
    }

    public ScheduledActivityState createState() {
        ScheduledActivityState instance = getNewMode().createStateInstance();
        instance.setReason(getNewReason());
        String times = newTime;
        Date date =  getNewDate();
        if (times != null) {
            try {
                if (times.contains("AM") || times.contains("PM")){
                    date = DateFormat.generateAmPmDateTime(date, times);
                } else {
                    date = DateFormat.generateDateTime(date, times);
                }
            } catch (ParseException e) {}
            instance.setWithTime(true);
        } else {
            instance.setWithTime(false);
        }
        instance.setDate(date);

        return instance;
    }

    public Collection<ResourceAuthorization> authorizations(Errors bindErrors) {
        Site site = null;
        Study study = null;
        if (getEvent() != null) {
            StudySite studySite = getEvent().getScheduledStudySegment().getScheduledCalendar().getAssignment().getStudySite();
            site = studySite.getSite();
            study = studySite.getStudy();
        }

        return ResourceAuthorization.createCollection(site, study,
            STUDY_SUBJECT_CALENDAR_MANAGER, STUDY_TEAM_ADMINISTRATOR, DATA_READER);
    }

    ////// BOUND PROPERTIES

    public ScheduledActivity getEvent() {
        return event;
    }

    public void setEvent(ScheduledActivity event) {
        this.event = event;
    }

    public ScheduledActivityMode getNewMode() {
        return newMode;
    }

    public void setNewMode(ScheduledActivityMode newMode) {
        this.newMode = newMode;
    }

    public Collection<ScheduledActivityMode> getEventSpecificMode(){
        List<ScheduledActivityMode> availableModes =  new ArrayList<ScheduledActivityMode>(ScheduledActivityMode.values());
        if (event != null && event.isConditionalEvent()) {
            availableModes.remove(ScheduledActivityMode.CANCELED);
        } else {
            availableModes.remove(ScheduledActivityMode.NOT_APPLICABLE);
        }
        availableModes.remove(ScheduledActivityMode.CONDITIONAL);
        return availableModes;
    }

    public String getNewReason() {
        return newReason;
    }

    public void setNewReason(String newReason) {
        this.newReason = newReason;
    }

    public Date getNewDate() {
        if (newDate == null) {
            return getEvent() == null ? null : getEvent().getActualDate();
        } else {
            return newDate;
        }
    }

    public void setNewDate(Date newDate) {
        this.newDate = newDate;
    }

    public String getNewNotes() {
        if (newNotes  == null) {
            return getEvent() == null ? null : getEvent().getNotes();
        } else {
            return newNotes;
        }
    }

    public void setNewNotes(String newNotes) {
        this.newNotes = newNotes;
    }

    public String getNewTime() {
        if (getEvent().getCurrentState().getWithTime()) {
            return DateFormat.generateAmPmTimeFromDate(getEvent().getCurrentState().getDate());
        } else {
            return null;
        }
    }

    public void setNewTime(String newTime) {
        this.newTime = newTime;
    }

    public void validate(Errors errors) {
        String times = newTime;
        Date date =  getNewDate();
        if (times != null) {
            try {
                if (times.contains("AM") || times.contains("PM")){
                    DateFormat.generateAmPmDateTime(date, times);
                } else {
                    DateFormat.generateDateTime(date, times);
                }
            } catch (ParseException e) {
                String message = times + " is not valid time. Please enter time in 24-hour format or with AM/PM.";
                errors.reject("error.time.not.valid.format", new String[] {times}, message);

            }
        }
    }
}
