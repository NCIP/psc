/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.io/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.web.schedule;

import edu.northwestern.bioinformatics.studycalendar.StudyCalendarSystemException;
import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledActivity;
import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledActivityMode;
import edu.northwestern.bioinformatics.studycalendar.restlets.representations.MultipleAssignmentScheduleJsonRepresentation;
import net.fortuna.ical4j.data.CalendarOutputter;
import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.Property;
import net.fortuna.ical4j.model.ValidationException;
import net.fortuna.ical4j.model.component.VEvent;
import net.fortuna.ical4j.model.property.CalScale;
import net.fortuna.ical4j.model.property.Description;
import net.fortuna.ical4j.model.property.DtEnd;
import net.fortuna.ical4j.model.property.Method;
import net.fortuna.ical4j.model.property.ProdId;
import net.fortuna.ical4j.model.property.Transp;
import net.fortuna.ical4j.model.property.Uid;
import net.fortuna.ical4j.model.property.Url;

import java.io.IOException;
import java.io.StringWriter;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.util.Collection;
import java.util.Date;

/**
 * Utility class which provides functionality of generating ics calendar.
 *
 * @author Saurabh Agrawal
 */
public class ICalTools {
    public static String createICSCalendarRepresentation(final Calendar icsCalendar) {
        try {
            final CalendarOutputter output = new CalendarOutputter();
            output.setValidating(false);
            StringWriter icsCalendarWritter = new StringWriter();
            output.output(icsCalendar, icsCalendarWritter);
            icsCalendarWritter.close();
            return icsCalendarWritter.toString();
        } catch (IOException e) {
            throw new StudyCalendarSystemException("Error while creating ics calendar", e);
        } catch (ValidationException ve) {
            throw new StudyCalendarSystemException("Error while creating ics calendar", ve);
        }
    }

    public static Calendar generateICSCalendarForActivities(Calendar icsCalendar, Date date, Collection<ScheduledActivity> scheduledActivities, String baseUrl, Boolean includeSubject) {
        for (final ScheduledActivity scheduleActivity : scheduledActivities) {
            VEvent vEvent = generateAllDayEventForScheduleActivity(date, scheduleActivity, baseUrl, includeSubject);
            if (vEvent != null) {
                icsCalendar.getComponents().add(vEvent);
            }
        }
        return icsCalendar;
    }

    private static VEvent generateAllDayEventForScheduleActivity(final Date date, final ScheduledActivity scheduledActivity, String baseUrl, Boolean includeSubject) {
        if (scheduledActivity.getCurrentState().getMode().equals(ScheduledActivityMode.SCHEDULED)||
            scheduledActivity.getCurrentState().getMode().equals(ScheduledActivityMode.CONDITIONAL) ) {
            try {
                VEvent vEvent = new VEvent(new net.fortuna.ical4j.model.Date(date.getTime()), createEventSummary(scheduledActivity, includeSubject));
                vEvent.getProperties().add(new Uid(scheduledActivity.getGridId()));
                vEvent.getProperties().add(new Transp(Property.TRANSP));
                vEvent.getProperty(Property.TRANSP).setValue("TRANSPARENT");
                Description description = new Description(createEventDescription(scheduledActivity));
                vEvent.getProperties().add(description);
                vEvent.getProperties().add(new Url());
                vEvent.getProperty(Property.URL).setValue(createActivityURL(scheduledActivity, baseUrl));
                vEvent.getProperties().add(new DtEnd());
                return vEvent;
            } catch (URISyntaxException use) {
                throw new StudyCalendarSystemException("Error while creating ics calendar", use);
            } catch(IOException e) {
                throw new StudyCalendarSystemException("Error while creating ics calendar", e);
            } catch (ParseException pe) {
                throw new StudyCalendarSystemException("Error while creating ics calendar", pe);
            }
        }
        return null;
    }

    private static String createEventSummary(ScheduledActivity scheduledActivity, Boolean includeSubject) {
        StringBuilder sb = new StringBuilder();
        if (includeSubject) {
            sb.append(scheduledActivity.getScheduledStudySegment().getScheduledCalendar().getAssignment().getSubject().getFullName()).append("/");
        }
        sb.append(scheduledActivity.getScheduledStudySegment().getScheduledCalendar().getAssignment().getName());
        sb.append("/").append(scheduledActivity.getActivity().getName());
        return sb.toString();
    }

    private static String createEventDescription(ScheduledActivity scheduledActivity) {
        StringBuilder sb = new StringBuilder();
        sb.append("Subject:" +scheduledActivity.getScheduledStudySegment().getScheduledCalendar().getAssignment().getSubject().getFullName()).append("\n");
        sb.append("Study:" +scheduledActivity.getScheduledStudySegment().getScheduledCalendar().getAssignment().getName()).append("\n");
        sb.append("Study Segment:" +scheduledActivity.getScheduledStudySegment().getName()).append("\n");
        sb.append("Activity:" +scheduledActivity.getActivity().getName()).append("\n");
        if (scheduledActivity.getDetails() != null) {
            sb.append("Details:" +scheduledActivity.getDetails()).append("\n");
        }
        if (scheduledActivity.getPlannedActivity() != null) {
            if (scheduledActivity.getPlannedActivity().getCondition() !=null )
                sb.append("Condition:" +scheduledActivity.getPlannedActivity().getCondition()).append("\n");
            sb.append("Study plan day:" + MultipleAssignmentScheduleJsonRepresentation.formatDaysFromPlan(scheduledActivity)).append("\n");
        }
        return sb.toString();
    }

    private static String createActivityURL(ScheduledActivity scheduledActivity, String baseURL) {
        String activityURL= baseURL.concat("/pages/cal/scheduleActivity?event=").concat(scheduledActivity.getGridId());
        return activityURL;
    }
    /**
     * Generates the calendar skeleton with basic calendar properties .
     *
     * @return the calendar
     */
    public static Calendar generateCalendarSkeleton() {
        final Calendar icsCalendar = new Calendar();
        icsCalendar.getProperties().add(new ProdId("-//Events Calendar//iCal4j 1.0//EN"));
        // THIS version need to removed otherwise Outlook will not import .ics files.
        // (http://calendarswamp.blogspot.com/2005/08/outlook-2003-for-ical-import-use.html)

        // icsCalendar.getProperties().add(Version.VERSION_2_0);
        icsCalendar.getProperties().add(CalScale.GREGORIAN);

        // this property is required for Outlook (http://en.wikipedia.org/wiki/ICalendar#Microsoft_Outlook)
        icsCalendar.getProperties().add(Method.PUBLISH);
        return icsCalendar;
    }
}
