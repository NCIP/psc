/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.io/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.xml.writers;

import edu.northwestern.bioinformatics.studycalendar.domain.StudySecondaryIdentifier;
import edu.northwestern.bioinformatics.studycalendar.xml.AbstractStudyCalendarXmlSerializer;
import edu.northwestern.bioinformatics.studycalendar.xml.XsdElement;
import static edu.northwestern.bioinformatics.studycalendar.xml.XsdAttribute.SECONDARY_IDENTIFIER_TYPE;
import static edu.northwestern.bioinformatics.studycalendar.xml.XsdAttribute.SECONDARY_IDENTIFIER_VALUE;
import org.dom4j.Element;

/**
 * @author Jalpa Patel
 */
public class StudySecondaryIdentifierXmlSerializer extends AbstractStudyCalendarXmlSerializer<StudySecondaryIdentifier> {

    public Element createElement(StudySecondaryIdentifier identifier) {
        Element identElement = XsdElement.SECONDARY_IDENTIFIER.create();
        SECONDARY_IDENTIFIER_TYPE.addTo(identElement, identifier.getType());
        SECONDARY_IDENTIFIER_VALUE.addTo(identElement, identifier.getValue());
        return identElement;
    }

    public StudySecondaryIdentifier readElement(Element element) {
        StudySecondaryIdentifier ssIdentifier = new StudySecondaryIdentifier();
        ssIdentifier.setType(SECONDARY_IDENTIFIER_TYPE.from(element));
        ssIdentifier.setValue(SECONDARY_IDENTIFIER_VALUE.from(element));
        return ssIdentifier;
    }
}
