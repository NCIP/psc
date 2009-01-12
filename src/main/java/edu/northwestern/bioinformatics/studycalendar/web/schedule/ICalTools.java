package edu.northwestern.bioinformatics.studycalendar.web.schedule;

import edu.northwestern.bioinformatics.studycalendar.StudyCalendarSystemException;
import edu.northwestern.bioinformatics.studycalendar.domain.*;
import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.Property;
import net.fortuna.ical4j.model.component.VEvent;
import net.fortuna.ical4j.model.parameter.Value;
import net.fortuna.ical4j.model.property.*;
import net.fortuna.ical4j.util.UidGenerator;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.net.SocketException;
import java.util.Date;
import java.util.List;
import java.util.SortedMap;

/**
 * Utility class which provides functionality of generating ics calendar.
 *
 * @author Saurabh Agrawal
 */
public class ICalTools {

    private static final Log logger = LogFactory.getLog(ICalTools.class);

    /**
     * Generate ICS calendar for a studySubjectAssignment.
     *
     * @param studySubjectAssignment the study subject assignment
     * @return the calendar. Returns empty calendar if there is no schedule for the studySubjectAssignment
     */
    public static Calendar generateICSCalendar(final StudySubjectAssignment studySubjectAssignment) {
        Calendar icsCalendar = new Calendar();
        if (studySubjectAssignment != null) {

            ScheduledCalendar scheduleCalendar = studySubjectAssignment.getScheduledCalendar();
            List<ScheduledStudySegment> scheduledStudySegments = null;

            if (scheduleCalendar != null) {
                // first generate the calendar skeleton
                icsCalendar = generateCalendarSkeleton();
                scheduledStudySegments = studySubjectAssignment.getScheduledCalendar().getScheduledStudySegments();

                // now add the events in calendar.
                for (ScheduledStudySegment scheduledStudySegment : scheduledStudySegments) {
                    SortedMap<Date, List<ScheduledActivity>> events = scheduledStudySegment.getActivitiesByDate();
                    for (Date date : events.keySet()) {
                        List<ScheduledActivity> event = events.get(date);
                        for (final ScheduledActivity scheduleActivity : event) {
                            VEvent vEvent = generateAllDayEventForAnScheduleActivityOfPatient(scheduleActivity, date,
                                    studySubjectAssignment.getSubject());
                            if (vEvent != null) {
                                icsCalendar.getComponents().add(vEvent);
                            }

                        }

                    }
                }
            }
        }

        return icsCalendar;
    }


    /**
     * Generate ICS file name as lastname_firstname_studyprotocolauthorityid
     *
     * @param studySubjectAssignment the study subject assignment
     * @return the string
     */
    public static String generateICSfileName(final StudySubjectAssignment studySubjectAssignment) {
        StringBuffer fileName = new StringBuffer(studySubjectAssignment.getGridId());
        Subject subject = studySubjectAssignment.getSubject();
        if (subject != null) {
            fileName = new StringBuffer("");
            if (subject.getLastName() != null) {
                fileName.append(subject.getLastName() + "-");
            }
            if (subject.getFirstName() != null) {
                fileName.append(subject.getFirstName() + "-");
            }
        }
        if (studySubjectAssignment.getStudySite() != null && studySubjectAssignment.getStudySite().getStudy() != null
                && studySubjectAssignment.getStudySite().getStudy().getAssignedIdentifier() != null) {
            fileName.append(studySubjectAssignment.getStudySite().getStudy().getAssignedIdentifier());
        }
        return fileName.append(".ics").toString();
    }

    /**
     * Generate an all day ics calendar event for an schedule activity of Patient. Currently calendar event is created only for ScheduleActivity
     * of {@link ScheduledActivityMode.SCHEDULED}
     *
     * @param subject           patient on which study is done
     * @param scheduledActivity the scheduled activity for which ics calendar event need to be generated
     * @param date              the date when event occurs.
     * @param subject
     * @return the ics calendar event. Returns null if scheduledActivity has no activity or date is null or ScheduleActivity is not of
     *         {@link ScheduledActivityMode.SCHEDULED}
     */
    private static VEvent generateAllDayEventForAnScheduleActivityOfPatient(final ScheduledActivity scheduledActivity,
                                                                            final Date date, final Subject subject) {
        final Activity activity = scheduledActivity.getActivity();
        if (activity != null && date != null && scheduledActivity.getCurrentState() != null
                && scheduledActivity.getCurrentState().getMode() != null
                && scheduledActivity.getCurrentState().getMode().equals(ScheduledActivityMode.SCHEDULED)) {
            String eventDetails = activity.getName();
            if (scheduledActivity.getDetails() != null && !scheduledActivity.getDetails().trim().equalsIgnoreCase("")) {
                eventDetails = eventDetails + " (" + scheduledActivity.getDetails() + ")";
            }

            VEvent vEvent = new VEvent(new net.fortuna.ical4j.model.Date(date.getTime()), eventDetails);

            // initialize as an all-day event..
            vEvent.getProperties().getProperty(Property.DTSTART).getParameters().add(Value.DATE);

            if (subject != null) {
                String eventDescrtiption = subject.getLastFirst();
                Description description = new Description(eventDescrtiption);
                vEvent.getProperties().add(description);
            }
            return vEvent;
        }
        return null;

    }

    /**
     * Generates the calendar skeleton with basic calendar properties .
     *
     * @return the calendar
     */
    private static Calendar generateCalendarSkeleton() {
        final Calendar icsCalendar = new Calendar();
        icsCalendar.getProperties().add(new ProdId("-//Events Calendar//iCal4j 1.0//EN"));
        // THIS version need to removed otherwise Outlook will not import .ics files.
        // (http://calendarswamp.blogspot.com/2005/08/outlook-2003-for-ical-import-use.html)

        // icsCalendar.getProperties().add(Version.VERSION_2_0);
        icsCalendar.getProperties().add(CalScale.GREGORIAN);

        // this property is required for Outlook (http://en.wikipedia.org/wiki/ICalendar#Microsoft_Outlook)
        icsCalendar.getProperties().add(Method.PUBLISH);
        UidGenerator ug = null;
        try {
            ug = new UidGenerator("uidGen");
            Uid uid = ug.generateUid();
            icsCalendar.getProperties().add(uid);
        }
        catch (SocketException e) {
            logger.error("Error while creating ics calendar" + e.getMessage(), e);
            throw new StudyCalendarSystemException(e.getCause());
        }
        return icsCalendar;
    }
}
