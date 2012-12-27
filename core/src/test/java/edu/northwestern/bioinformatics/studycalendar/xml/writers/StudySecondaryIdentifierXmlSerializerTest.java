/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.com/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.xml.writers;

import edu.northwestern.bioinformatics.studycalendar.core.StudyCalendarXmlTestCase;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.domain.StudySecondaryIdentifier;
import static edu.northwestern.bioinformatics.studycalendar.domain.Fixtures.createReleasedTemplate;
import static edu.northwestern.bioinformatics.studycalendar.domain.Fixtures.addSecondaryIdentifier;
import edu.northwestern.bioinformatics.studycalendar.xml.XsdElement;
import static edu.northwestern.bioinformatics.studycalendar.xml.XsdAttribute.SECONDARY_IDENTIFIER_TYPE;
import static edu.northwestern.bioinformatics.studycalendar.xml.XsdAttribute.SECONDARY_IDENTIFIER_VALUE;
import org.dom4j.Element;

/**
 * @author Jalpa Patel
 */
public class StudySecondaryIdentifierXmlSerializerTest extends StudyCalendarXmlTestCase {
    private Study study;
    private StudySecondaryIdentifier identifier;
    private StudySecondaryIdentifierXmlSerializer serializer;
    public void setUp() throws Exception {
        super.setUp();
        study = createReleasedTemplate();
        identifier = addSecondaryIdentifier(study, "Type1", "ident1");
        serializer = new StudySecondaryIdentifierXmlSerializer();
    }

    public void testCreateElement() throws Exception {
        Element actual =  serializer.createElement(identifier);
        assertEquals("Wrong element name", XsdElement.SECONDARY_IDENTIFIER.xmlName(), actual.getName());
        assertEquals("Should have no children", 0, actual.elements().size());
        assertEquals("Wrong identifier type", identifier.getType(), SECONDARY_IDENTIFIER_TYPE.from(actual));
        assertEquals("Wrong identifier value", identifier.getValue(), SECONDARY_IDENTIFIER_VALUE.from(actual));
    }

    public void testReadElement() throws Exception {
        Element actual = XsdElement.SECONDARY_IDENTIFIER.create();
        SECONDARY_IDENTIFIER_TYPE.addTo(actual, "Type2");
        SECONDARY_IDENTIFIER_VALUE.addTo(actual, "Identifier2");

        StudySecondaryIdentifier read = serializer.readElement(actual);
        assertNotNull(read);
        assertNull(read.getId());
        assertEquals(null, read.getGridId());
        assertEquals("Wrong identifier type", "Type2", read.getType());
        assertEquals("Wrong identifier value", "Identifier2", read.getValue());
    }
}
