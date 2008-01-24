package edu.northwestern.bioinformatics.studycalendar.xml.writers;

import edu.northwestern.bioinformatics.studycalendar.domain.delta.PlannedCalendarDelta;

public class PlannedCalendarDeltaXmlSerializer extends AbstractDeltaXmlSerializer {
    private static final String PLANNED_CALENDAR_DELTA = "planned-calendar-delta";
    
    protected Class<?> nodeClass() {
        return PlannedCalendarDelta.class;
    }

    protected String elementName() {
        return PLANNED_CALENDAR_DELTA;
    }
}