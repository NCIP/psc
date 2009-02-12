package edu.northwestern.bioinformatics.studycalendar.xml.writers;

import edu.northwestern.bioinformatics.studycalendar.domain.PlanTreeNode;
import edu.northwestern.bioinformatics.studycalendar.domain.PlannedCalendar;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Delta;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.StudyDelta;

/**
 * @author Nataliya Shurupova
 */
public class StudyDeltaXmlSerializer extends AbstractDeltaXmlSerializer {

    public static final String STUDY_DELTA = "study-delta";

    protected PlanTreeNode<?> nodeInstance() {
        return new PlannedCalendar();
    }

    protected Delta deltaInstance() {
        return new StudyDelta();
    }

    protected String elementName() {
        return STUDY_DELTA;
    }
}
