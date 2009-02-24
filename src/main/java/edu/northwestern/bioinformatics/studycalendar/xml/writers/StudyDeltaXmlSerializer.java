package edu.northwestern.bioinformatics.studycalendar.xml.writers;

import edu.northwestern.bioinformatics.studycalendar.domain.delta.Delta;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.StudyDelta;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Changeable;

/**
 * @author Nataliya Shurupova
 */
public class StudyDeltaXmlSerializer extends AbstractDeltaXmlSerializer {

    public static final String STUDY_DELTA = "study-delta";

    protected Changeable nodeInstance() {
        return study;
    }

    protected Delta deltaInstance() {
        return new StudyDelta();
    }

    protected String elementName() {
        return STUDY_DELTA;
    }
}
