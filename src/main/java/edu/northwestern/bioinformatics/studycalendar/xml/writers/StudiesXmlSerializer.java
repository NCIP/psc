package edu.northwestern.bioinformatics.studycalendar.xml.writers;

import edu.northwestern.bioinformatics.studycalendar.xml.AbstractStudyCalendarXmlSerializer;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import org.dom4j.Element;

import java.util.Collection;

/**
 * TODO: eventually this will be merged into StudyXmlSerializer so it can implement
 * {@link edu.northwestern.bioinformatics.studycalendar.xml.StudyCalendarXmlCollectionSerializer}
 *
 * @author Rhett Sutphin
 */
public class StudiesXmlSerializer extends AbstractStudyCalendarXmlSerializer<Collection<Study>> {
    private static final String STUDIES_ELT_NAME = "studies";

    @Override
    public Element createElement(Collection<Study> studies) {
        Element root = element(STUDIES_ELT_NAME);
        for (Study study : studies) {
            Element sElt = element("study");
            sElt.addAttribute(StudyXmlSerializer.ASSIGNED_IDENTIFIER, study.getNaturalKey());
            root.add(sElt);
        }
        return root;
    }

    @Override
    public Collection<Study> readElement(Element element) {
        throw new UnsupportedOperationException("This serializer is write-only");
    }
}
