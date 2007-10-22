package edu.northwestern.bioinformatics.studycalendar.web.schedule;

import java.net.SocketException;
import java.util.Date;
import java.util.List;
import java.util.SortedMap;

import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.Property;
import net.fortuna.ical4j.model.component.VEvent;
import net.fortuna.ical4j.model.parameter.Value;
import net.fortuna.ical4j.model.property.CalScale;
import net.fortuna.ical4j.model.property.Description;
import net.fortuna.ical4j.model.property.Method;
import net.fortuna.ical4j.model.property.ProdId;
import net.fortuna.ical4j.model.property.Uid;
import net.fortuna.ical4j.util.UidGenerator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.northwestern.bioinformatics.studycalendar.StudyCalendarSystemException;
import edu.northwestern.bioinformatics.studycalendar.domain.Activity;
import edu.northwestern.bioinformatics.studycalendar.domain.Participant;
import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledArm;
import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledCalendar;
import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledEvent;
import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledEventMode;
import edu.northwestern.bioinformatics.studycalendar.domain.StudyParticipantAssignment;

/**
 * Utility class which provides functionality of generating ics calendar.
 * @author Saurabh Agrawal
 */
public class ICalTools {

	private static final Log logger = LogFactory.getLog(ICalTools.class);

	/**
	 * Generate ICS calendar for a studyParticipantAssignment.
	 * 
	 * @param studyParticipantAssignment the study participant assignment
	 * 
	 * @return the calendar. Returns empty calendar if there is no schedule for the studyParticipantAssignment
	 */
	public static Calendar generateICalendar(final StudyParticipantAssignment studyParticipantAssignment) {
		Calendar icsCalendar = new Calendar();
		if (studyParticipantAssignment != null) {

			ScheduledCalendar scheduleCalendar = studyParticipantAssignment.getScheduledCalendar();
			List<ScheduledArm> scheduledArms = null;

			if (scheduleCalendar != null) {
				// first generate the calendar skeleton
				icsCalendar = generateCalendarSkeleton();
				scheduledArms = studyParticipantAssignment.getScheduledCalendar().getScheduledArms();

				// now add the events in calendar.
				for (ScheduledArm scheduledArm : scheduledArms) {
					SortedMap<Date, List<ScheduledEvent>> events = scheduledArm.getEventsByDate();
					for (Date date : events.keySet()) {
						List<ScheduledEvent> event = events.get(date);
						for (final ScheduledEvent scheduleEvent : event) {
							VEvent vEvent = generateAllDayEventForAnScheduleEventOfPatient(scheduleEvent, date,
									studyParticipantAssignment.getParticipant());
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
	 * Generate an all day ics calendar event for an schedule event of Patient. Currently calendar event is created only for ScheduleEvent
	 * of {@link ScheduledEventMode.SCHEDULED}
	 * 
	 * @param participant patient on which study is done
	 * @param scheduledEvent the scheduled event for which ics calendar event need to be generated
	 * @param date the date when event occurs.
	 * @param participant
	 * 
	 * @return the ics calendar event. Returns null if scheduledEvent has no activity or date is null or ScheduleEvent is not of
	 *         {@link ScheduledEventMode.SCHEDULED}
	 */
	private static VEvent generateAllDayEventForAnScheduleEventOfPatient(final ScheduledEvent scheduledEvent,
			final Date date, final Participant participant) {
		final Activity activity = scheduledEvent.getActivity();
		if (activity != null && date != null && scheduledEvent.getCurrentState() != null
				&& scheduledEvent.getCurrentState().getMode() != null
				&& scheduledEvent.getCurrentState().getMode().equals(ScheduledEventMode.SCHEDULED)) {
			String eventDetails = activity.getName();
			if (scheduledEvent.getDetails() != null && !scheduledEvent.getDetails().trim().equalsIgnoreCase("")) {
				eventDetails = eventDetails + " (" + scheduledEvent.getDetails() + ")";
			}

			VEvent vEvent = new VEvent(new net.fortuna.ical4j.model.Date(date.getTime()), eventDetails);

			// initialize as an all-day event..
			vEvent.getProperties().getProperty(Property.DTSTART).getParameters().add(Value.DATE);

			if (participant != null) {
				String eventDescrtiption = participant.getLastFirst();
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
