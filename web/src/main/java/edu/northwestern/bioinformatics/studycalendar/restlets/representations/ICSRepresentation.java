package edu.northwestern.bioinformatics.studycalendar.restlets.representations;

import edu.northwestern.bioinformatics.studycalendar.web.schedule.ICalTools;
import org.restlet.data.MediaType;
import org.restlet.representation.StringRepresentation;

import net.fortuna.ical4j.model.Calendar;
/**
 * @author Saurabh Agrawal
 */
public class ICSRepresentation extends StringRepresentation {

      public ICSRepresentation(Calendar icsCalendar, String fileName) {
          super(ICalTools.createICSCalendarRepresentation(icsCalendar), MediaType.TEXT_CALENDAR);
          this.setDownloadable(true);
          this.setDownloadName(fileName.replace(" ", "_").concat(".ics"));
      }
}