package edu.northwestern.bioinformatics.studycalendar.xml.writers;

import edu.northwestern.bioinformatics.studycalendar.domain.PlanTreeNode;
import edu.northwestern.bioinformatics.studycalendar.domain.PlannedActivity;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Delta;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.PlannedActivityDelta;

public class PlannedActivityDeltaXmlSerializer extends AbstractDeltaXmlSerializer {
    public static final String PLANNED_ACTIVITY_DELTA = "planned-activity-delta";

    protected PlanTreeNode<?> nodeInstance() {
        return new PlannedActivity();
    }

    protected Delta deltaInstance() {
        return new PlannedActivityDelta();
    }

    protected String elementName() {
        return PLANNED_ACTIVITY_DELTA;
    }
}
