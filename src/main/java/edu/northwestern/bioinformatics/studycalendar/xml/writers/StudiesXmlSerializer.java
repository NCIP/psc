package edu.northwestern.bioinformatics.studycalendar.xml.writers;

import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.xml.AbstractStudyCalendarXmlCollectionSerializer;
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
        studyElement.addAttribute(StudyXmlSerializer.ASSIGNED_IDENTIFIER, study.getNaturalKey());

        if (inCollection) {
            return studyElement;

        } else {
            Element root = collectionRootElement().create();
            root.add(studyElement);

            return root;
        }

    }


}
