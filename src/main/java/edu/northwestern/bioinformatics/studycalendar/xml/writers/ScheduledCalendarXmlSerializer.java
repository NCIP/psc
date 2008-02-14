package edu.northwestern.bioinformatics.studycalendar.xml.writers;

import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledCalendar;
import edu.northwestern.bioinformatics.studycalendar.xml.AbstractStudyCalendarXmlCollectionSerializer;
import static edu.northwestern.bioinformatics.studycalendar.xml.XsdAttribute.SCHEDULED_CALENDAR_ASSIGNMENT_ID;
import static edu.northwestern.bioinformatics.studycalendar.xml.XsdAttribute.SCHEDULED_CALENDAR_ID;
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
        SCHEDULED_CALENDAR_ID.addTo(elt, scheduledCalendar.getGridId());
        SCHEDULED_CALENDAR_ASSIGNMENT_ID.addTo(elt, scheduledCalendar.getAssignment().getGridId());

        // TODO: Call scheculed segment serializer
        return elt;
    }

    public ScheduledCalendar readElement(Element element) {
        throw new UnsupportedOperationException("Functionality to read a schedule element does not exist");
    }
}
