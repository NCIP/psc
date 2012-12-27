/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.com/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.xml.writers;

import edu.northwestern.bioinformatics.studycalendar.core.StudyCalendarXmlTestCase;
import edu.northwestern.bioinformatics.studycalendar.domain.Epoch;
import edu.northwestern.bioinformatics.studycalendar.domain.StudySegment;
import org.apache.commons.lang.StringUtils;
import org.dom4j.Element;

import static edu.northwestern.bioinformatics.studycalendar.core.Fixtures.*;
import static java.util.Collections.emptyList;
import static org.easymock.EasyMock.expect;

public class EpochXmlSerializerTest extends StudyCalendarXmlTestCase {
    private EpochXmlSerializer serializer;
    private Element element;
    private Epoch epoch;
    private StudySegmentXmlSerializer studySegmentSerializer;

    protected void setUp() throws Exception {
        super.setUp();

        element = registerMockFor(Element.class);
        studySegmentSerializer = registerMockFor(StudySegmentXmlSerializer.class);
        serializer = new EpochXmlSerializer();
        serializer.setChildXmlSerializer(studySegmentSerializer);
        epoch = setGridId("grid0", createNamedInstance("Epoch A", Epoch.class));
    }

    public void testCreateElementEpoch() {
        Element actual = serializer.createElement(epoch);

        assertEquals("Wrong attribute size", 2, actual.attributeCount());
        assertEquals("Wrong grid id", "grid0", actual.attribute("id").getValue());
        assertEquals("Wrong epoch name", "Epoch A", actual.attribute("name").getValue());
    }

    public void testReadElementEpoch() {
        expect(element.getName()).andReturn("epoch");
        expect(element.attributeValue("id")).andReturn("grid0");
        expect(element.attributeValue("name")).andReturn("Epoch A").anyTimes();
        expect(element.elements()).andReturn(emptyList());
        replayMocks();

        Epoch actual = (Epoch) serializer.readElement(element);
        verifyMocks();

        assertEquals("Wrong grid id", "grid0", actual.getGridId());
        assertEquals("Wrong epoch name", "Epoch A", actual.getName());
    }

    public void testValidateElement() throws Exception {
        Epoch epoch = createEpoch();
        Element actual = serializer.createElement(epoch);
        assertTrue(StringUtils.isBlank(serializer.validateElement(epoch, actual).toString()));
        epoch.setGridId("wrong grid id");
        assertFalse(StringUtils.isBlank(serializer.validateElement(epoch, actual).toString()));

        epoch = createEpoch();
        assertTrue(StringUtils.isBlank(serializer.validateElement(epoch, actual).toString()));
        epoch.setName("wrong name");
        assertFalse(StringUtils.isBlank(serializer.validateElement(epoch, actual).toString()));

        epoch = createEpoch();
        assertTrue(StringUtils.isBlank(serializer.validateElement(epoch, actual).toString()));
        epoch.getChildren().add(new StudySegment());
        assertFalse(StringUtils.isBlank(serializer.validateElement(epoch, actual).toString()));
    }

    private Epoch createEpoch() {
        Epoch epoch = setGridId("grid0", createNamedInstance("Epoch A", Epoch.class));
        return epoch;
    }
}
