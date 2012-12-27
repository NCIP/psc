/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.com/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.xml.writers;

import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.domain.StudySecondaryIdentifier;
import edu.northwestern.bioinformatics.studycalendar.xml.AbstractStudyCalendarXmlCollectionSerializer;
import static edu.northwestern.bioinformatics.studycalendar.xml.XsdAttribute.STUDY_ASSIGNED_IDENTIFIER;
import static edu.northwestern.bioinformatics.studycalendar.xml.XsdAttribute.STUDY_PROVIDER;
import edu.northwestern.bioinformatics.studycalendar.xml.XsdElement;
import org.dom4j.Element;
import org.springframework.beans.factory.annotation.Required;

/**
 * @author Rhett Sutphin
 */
public class StudiesXmlSerializer extends AbstractStudyCalendarXmlCollectionSerializer<Study> {
    private StudySecondaryIdentifierXmlSerializer studySecondaryIdentifierXmlSerializer;
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
        if (study.getLongTitle() != null && study.getLongTitle().length() >0) {
            Element eltLongTitle = XsdElement.LONG_TITLE.create();
            eltLongTitle.addText(study.getLongTitle());
            studyElement.add(eltLongTitle);
        }
        for (StudySecondaryIdentifier studySecondaryIdent : study.getSecondaryIdentifiers()) {
            studyElement.add(studySecondaryIdentifierXmlSerializer.createElement(studySecondaryIdent));
        }
        return studyElement;
    }

    @Required
    public void setStudySecondaryIdentifierXmlSerializer(StudySecondaryIdentifierXmlSerializer studySecondaryIdentifierXmlSerializer) {
        this.studySecondaryIdentifierXmlSerializer = studySecondaryIdentifierXmlSerializer;
    }
}
