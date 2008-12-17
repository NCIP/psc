package edu.northwestern.bioinformatics.studycalendar.xml.writers;

import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.xml.StudyCalendarXmlSerializer;

/**
 * @author Nataliya Shurupova
 */
public interface StatefulTemplateXmlSerializer<R> extends StudyCalendarXmlSerializer<R> {
    void setStudy(Study study);
}
