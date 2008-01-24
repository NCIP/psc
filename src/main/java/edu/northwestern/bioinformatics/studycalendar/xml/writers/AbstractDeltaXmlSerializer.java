package edu.northwestern.bioinformatics.studycalendar.xml.writers;

import edu.northwestern.bioinformatics.studycalendar.domain.delta.Delta;
import edu.northwestern.bioinformatics.studycalendar.xml.AbstractStudyCalendarXmlSerializer;
import org.dom4j.Element;

public abstract class AbstractDeltaXmlSerializer extends AbstractStudyCalendarXmlSerializer<Delta> {
    
    private static final String EPOCH_DELTA = "epoch-delta";
    private static final String STUDY_SEGMENT_DELTA = "study-segment-delta";
    private static final String PERIOD_DELTA = "period-delta";
    private static final String PLANNED_ACTIVITY_DELTA = "planned-activity-delta";
    private static final String NODE_ID = "node-id";


    protected abstract Class<?> nodeClass();
    protected abstract String elementName();

    public Element createElement(Delta delta) {
         Element element = element(elementName());
//            if (delta instanceof PlannedCalendarDelta) {
//                element = element(PLANNED_CALENDAR_DELTA);
//            } else if (delta instanceof EpochDelta) {
//                element = element(EPOCH_DELTA);
//            } else if (delta instanceof StudySegmentDelta) {
//                element = element(STUDY_SEGMENT_DELTA);
//            } else if (delta instanceof PeriodDelta) {
//                element = element(PERIOD_DELTA);
//            } else if (delta instanceof PlannedActivityDelta) {
//                element = element(PLANNED_ACTIVITY_DELTA);
//            } else {
//                throw new StudyCalendarError("Delta is not recognized: %s", delta.getClass());
//            }

            element.addAttribute(ID, delta.getGridId());
            element.addAttribute(NODE_ID, delta.getNode().getGridId());

        // TODO: Add Change Nodes
        return element;
    }

    public Delta readElement(Element element) {
        throw new UnsupportedOperationException();
    }
}
