package edu.northwestern.bioinformatics.studycalendar.xml.writers;

import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.xml.AbstractStudyCalendarXmlCollectionSerializer;
import static edu.northwestern.bioinformatics.studycalendar.xml.XsdAttribute.STUDY_ASSIGNED_IDENTIFIER;
import static edu.northwestern.bioinformatics.studycalendar.xml.XsdAttribute.STUDY_PROVIDER;
import edu.northwestern.bioinformatics.studycalendar.xml.XsdElement;
import org.dom4j.Element;

/**
 * @author Rhett Sutphin
 */
public class StudiesXmlSerializer extends AbstractStudyCalendarXmlCollectionSerializer<Study> {
    @Override
    public Study readElement(Element element) {
        throw new UnsupportedOperationException("This serializer is write-only");
    }

    @Override
    protected XsdElement collectionRootElement() {
        return XsdElement.STUDIES;
    }

    @Override
    protected XsdElement rootElement() {
        return XsdElement.STUDY;
    }

    @Override
    protected Element createElement(final Study study, final boolean inCollection) {
        Element studyElement = rootElement().create();
        STUDY_ASSIGNED_IDENTIFIER.addTo(studyElement, study.getNaturalKey());
        if (study.getProvider() != null) {
            STUDY_PROVIDER.addTo(studyElement, study.getProvider());
        }

        return studyElement;
    }
}
