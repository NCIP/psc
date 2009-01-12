package edu.northwestern.bioinformatics.studycalendar.restlets;

import edu.northwestern.bioinformatics.studycalendar.domain.StudySubjectAssignment;
import edu.northwestern.bioinformatics.studycalendar.web.schedule.ICalTools;
import net.fortuna.ical4j.data.CalendarOutputter;
import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.ValidationException;
import org.restlet.data.MediaType;
import org.restlet.resource.Representation;
import org.restlet.resource.StringRepresentation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.StringWriter;

/**
 * @author Saurabh Agrawal
 */
public class ICSRepresentation extends StringRepresentation {
    private static final Logger logger = LoggerFactory.getLogger(ICSRepresentation.class);

    public ICSRepresentation(CharSequence charSequence, MediaType mediaType) {
        super(charSequence, mediaType);
    }


    public static Representation create(final StudySubjectAssignment studySubjectAssignment) {

        Calendar icsCalendar = ICalTools.generateICSCalendar(studySubjectAssignment);

        try {
            final CalendarOutputter output = new CalendarOutputter();
            output.setValidating(false);
            StringWriter icsCalendarWritter = new StringWriter();
            output.output(icsCalendar, icsCalendarWritter);

            icsCalendarWritter.close();

            StringRepresentation stringRepresentation = new StringRepresentation(icsCalendar.toString(), MediaType.TEXT_CALENDAR);

            stringRepresentation.setDownloadable(true);
            stringRepresentation.setDownloadName(ICalTools.generateICSfileName(studySubjectAssignment));
            return stringRepresentation;


        } catch (IOException e) {
            logger.error("error while creating ics calendar for assignment:" + studySubjectAssignment.getGridId() + ". Error is:" + e.toString());

        } catch (ValidationException e) {
            logger.error("error while creating ics calendar for assignment:" + studySubjectAssignment.getGridId() + ". Error is:" + e.toString());

        }
        return null;


    }

}