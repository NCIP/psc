package edu.northwestern.bioinformatics.studycalendar.xml.writers;

import edu.northwestern.bioinformatics.studycalendar.domain.delta.PlannedCalendarDelta;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Delta;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.domain.PlannedCalendar;
import edu.northwestern.bioinformatics.studycalendar.domain.PlanTreeNode;

public class PlannedCalendarDeltaXmlSerializer extends AbstractDeltaXmlSerializer {
    private static final String PLANNED_CALENDAR_DELTA = "planned-calendar-delta";

    public PlannedCalendarDeltaXmlSerializer(Study study) {
        super(study);
    }

    protected PlanTreeNode<?> nodeInstance() {
        return new PlannedCalendar();
    }

    protected Delta deltaInstance() {
        return new PlannedCalendarDelta();
    }

    protected String elementName() {
        return PLANNED_CALENDAR_DELTA;
    }
}