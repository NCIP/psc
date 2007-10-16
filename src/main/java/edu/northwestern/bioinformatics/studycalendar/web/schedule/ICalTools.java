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
import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledArm;
import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledCalendar;
import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledEvent;
import edu.northwestern.bioinformatics.studycalendar.domain.StudyParticipantAssignment;

/**
 * Utility class which provides functionality of generating ics calendar.
 * @author Saurabh Agrawal
 */
public class ICalTools {

	/**
	 * Generate ICS calendar for a studyParticipantAssignment.
	 * 
	 * @param studyParticipantAssignment the study participant assignment
	 * 
	 * @return the calendar. Returns empty calendar if there is no schedule for the studyParticipantAssignment
	 */
	public static Calendar generateICalendar(final StudyParticipantAssignment studyParticipantAssignment) {
		final Calendar icsCalendar = new Calendar();
		if (studyParticipantAssignment != null) {

			ScheduledCalendar scheduleCalendar = studyParticipantAssignment.getScheduledCalendar();
			List<ScheduledArm> scheduledArms = null;

			if (scheduleCalendar != null) {
				scheduledArms = studyParticipantAssignment.getScheduledCalendar().getScheduledArms();
				icsCalendar.getProperties().add(new ProdId("-//Events Calendar//iCal4j 1.0//EN"));
				// THIS version need to removed other Outlook will not import .ics files.
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
					// e.printStackTrace(); //To change body of catch statement use File | Settings | File Templates.
				}

				for (ScheduledArm scheduledArm : scheduledArms) {
					SortedMap<Date, List<ScheduledEvent>> events = scheduledArm.getEventsByDate();
					for (Date date : events.keySet()) {
						List<ScheduledEvent> event = events.get(date);
						for (final ScheduledEvent scheduleEvent : event) {
							if (scheduleEvent.getActivity() != null) {
								String activityName = scheduleEvent.getActivity().getName();
								VEvent vEvent = new VEvent(new net.fortuna.ical4j.model.Date(date.getTime()),
										activityName);

								// initialize as an all-day event..
								vEvent.getProperties().getProperty(Property.DTSTART).getParameters().add(Value.DATE);
								Description description = new Description(scheduleEvent.getActivity().getDescription());
								vEvent.getProperties().add(description);
								// Add the event
								icsCalendar.getComponents().add(vEvent);
							}

						}

					}
				}
			}
		}

		return icsCalendar;
	}
}
