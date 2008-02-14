package edu.northwestern.bioinformatics.studycalendar.xml.writers;

import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledCalendar;
import edu.northwestern.bioinformatics.studycalendar.xml.AbstractStudyCalendarXmlCollectionSerializer;
import edu.northwestern.bioinformatics.studycalendar.xml.XsdElement;
import static edu.northwestern.bioinformatics.studycalendar.xml.XsdElement.SCHEDULED_CALENDAR;
import static edu.northwestern.bioinformatics.studycalendar.xml.XsdElement.SCHEDULED_CALENDARS;
import org.dom4j.Element;

/**
 * @author John Dzak
 */
public class ScheduledCalendarXmlSerializer extends AbstractStudyCalendarXmlCollectionSerializer<ScheduledCalendar> {

    protected XsdElement rootElement() { return SCHEDULED_CALENDAR; }
    protected XsdElement collectionRootElement() { return SCHEDULED_CALENDARS; }

    protected Element createElement(ScheduledCalendar scheduledCalendar, boolean inCollection) {
        Element elt = SCHEDULED_CALENDAR.create();
        return elt;
    }

    public ScheduledCalendar readElement(Element element) {
        throw new UnsupportedOperationException();
    }
}
