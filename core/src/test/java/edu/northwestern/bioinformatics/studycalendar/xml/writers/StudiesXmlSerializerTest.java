/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.io/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.xml.writers;

import edu.northwestern.bioinformatics.studycalendar.core.Fixtures;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.domain.StudySecondaryIdentifier;
import static edu.northwestern.bioinformatics.studycalendar.domain.Fixtures.addSecondaryIdentifier;
import edu.northwestern.bioinformatics.studycalendar.core.StudyCalendarXmlTestCase;
import edu.northwestern.bioinformatics.studycalendar.xml.XsdAttribute;
import org.dom4j.Element;
import org.dom4j.DocumentHelper;
import static org.easymock.EasyMock.expect;

import java.util.Arrays;

/**
 * @author Rhett Sutphin
 */
public class StudiesXmlSerializerTest extends StudyCalendarXmlTestCase {
    private StudiesXmlSerializer serializer;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        serializer = new StudiesXmlSerializer();
    }

    public void testCreateSingleElement() throws Exception {
        Study sA = Fixtures.createNamedInstance("A", Study.class);

        Element actual = serializer.createElement(sA);
        assertEquals("study", actual.getName());
    }
    
    public void testSerializeCollection() throws Exception {
        Study sA = Fixtures.createNamedInstance("A", Study.class);
        Study sB = Fixtures.createNamedInstance("B", Study.class);

        Element actual = serializer.createDocument(Arrays.asList(sA, sB)).getRootElement();
        assertEquals("studies", actual.getName());
        assertEquals(2, actual.elements().size());
        assertEmbeddedStudyElement("A", (Element) actual.elements().get(0));
        assertEmbeddedStudyElement("B", (Element) actual.elements().get(1));
    }

    private void assertEmbeddedStudyElement(String expectedAssignedIdentifier, Element actual) {
        assertEquals("Wrong element", "study", actual.getName());
        assertEquals("Wrong identifier", expectedAssignedIdentifier,
                XsdAttribute.STUDY_ASSIGNED_IDENTIFIER.from(actual));
        assertEquals("Should have no children", 0, actual.elements().size());
    }

    public void testCreateSingleElementwithProvider() throws Exception {
        Study study = Fixtures.createNamedInstance("A", Study.class);
        study.setProvider("study-provider");
        Element actualElement = serializer.createElement(study);
        assertNotNull(actualElement.attribute("provider"));
        assertEquals("Wrong provider", "study-provider", actualElement.attributeValue("provider"));

    }

    public void testCreateElmentWithSecondaryIdentifiers() throws Exception {
        StudySecondaryIdentifierXmlSerializer xmlSerializer =
                registerMockFor(StudySecondaryIdentifierXmlSerializer.class);
        serializer.setStudySecondaryIdentifierXmlSerializer(xmlSerializer);
        Study study = Fixtures.createNamedInstance("A", Study.class);
        StudySecondaryIdentifier identifier = addSecondaryIdentifier(study, "Type1", "ident1");
        Element eIdentifier = DocumentHelper.createElement("secondary-identifier");
        expect(xmlSerializer.createElement(identifier)).andReturn(eIdentifier);

        replayMocks();
        Element actual = serializer.createElement(study);
        verifyMocks();
        assertNotNull("Secondary Identifier should exist", actual.element("secondary-identifier"));
    }

    public void testCreateElementWithLongTitle() throws Exception {
        Study study = Fixtures.createNamedInstance("A", Study.class);
        study.setLongTitle("study long title");
        replayMocks();
        Element actual = serializer.createElement(study);
        verifyMocks();
        Element eltLongTitle = actual.element("long-title");
        assertNotNull("Long title should exist", eltLongTitle);
        assertEquals("Long title name does not match", study.getLongTitle(), eltLongTitle.getText());
    }
}
