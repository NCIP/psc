package edu.northwestern.bioinformatics.studycalendar.xml.writers;

import edu.northwestern.bioinformatics.studycalendar.domain.StudySegment;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Delta;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.StudySegmentDelta;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Changeable;

public class StudySegmentDeltaXmlSerializer  extends AbstractDeltaXmlSerializer {
    public static final String STUDY_SEGMENT_DELTA = "study-segment-delta";

    protected Changeable nodeInstance() {
        return new StudySegment();
    }

    protected Delta deltaInstance() {
        return new StudySegmentDelta();
    }

    protected String elementName() {
        return STUDY_SEGMENT_DELTA;
    }
}
