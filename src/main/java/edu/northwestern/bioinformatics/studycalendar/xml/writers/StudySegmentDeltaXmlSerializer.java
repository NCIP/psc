package edu.northwestern.bioinformatics.studycalendar.xml.writers;

import edu.northwestern.bioinformatics.studycalendar.domain.PlanTreeNode;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.domain.StudySegment;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Delta;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.StudySegmentDelta;

public class StudySegmentDeltaXmlSerializer  extends AbstractDeltaXmlSerializer {
    private static final String STUDY_SEGMENT_DELTA = "study-segment-delta";

    public StudySegmentDeltaXmlSerializer(Study study) {
        super(study);
    }

    protected PlanTreeNode<?> nodeInstance() {
        return new StudySegment();
    }

    protected Delta deltaInstance() {
        return new StudySegmentDelta();
    }

    protected String elementName() {
        return STUDY_SEGMENT_DELTA;
    }
}
