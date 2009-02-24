package edu.northwestern.bioinformatics.studycalendar.xml.writers;

import edu.northwestern.bioinformatics.studycalendar.domain.PlannedCalendar;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Delta;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.PlannedCalendarDelta;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Changeable;

public class PlannedCalendarDeltaXmlSerializer extends AbstractDeltaXmlSerializer {
    public static final String PLANNED_CALENDAR_DELTA = "planned-calendar-delta";

    protected Changeable nodeInstance() {
        return new PlannedCalendar();
    }

    protected Delta deltaInstance() {
        return new PlannedCalendarDelta();
    }

    protected String elementName() {
        return PLANNED_CALENDAR_DELTA;
    }
}