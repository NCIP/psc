/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.io/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.xml.writers;

import edu.northwestern.bioinformatics.studycalendar.core.StudyCalendarXmlTestCase;
import edu.northwestern.bioinformatics.studycalendar.domain.Period;
import edu.northwestern.bioinformatics.studycalendar.domain.StudySegment;
import org.apache.commons.lang.StringUtils;
import org.dom4j.Element;

import static edu.northwestern.bioinformatics.studycalendar.core.Fixtures.*;
import static java.util.Collections.emptyList;
import static org.easymock.EasyMock.expect;

public class StudySegmentXmlSerializerTest extends StudyCalendarXmlTestCase {
    public static final String STUDY_SEGMENT = "study-segment";

    private StudySegmentXmlSerializer serializer;
    private Element element;
    private StudySegment segment;
    private PeriodXmlSerializer periodSerializer;

    protected void setUp() throws Exception {
        super.setUp();
        element = registerMockFor(Element.class);
        periodSerializer = registerMockFor(PeriodXmlSerializer.class);
        serializer = new StudySegmentXmlSerializer();
        serializer.setChildXmlSerializer(periodSerializer);
        segment = setGridId("grid0", createNamedInstance("Segment A", StudySegment.class));
    }

    public void testCreateElement() {
        Element actual = serializer.createElement(segment);

        assertEquals("Wrong attribute size", 2, actual.attributeCount());
        assertEquals("Wrong grid id", "grid0", actual.attribute("id").getValue());
        assertEquals("Wrong segment name", "Segment A", actual.attribute("name").getValue());
    }

    public void testReadElement() {
        expect(element.getName()).andReturn("study-segment");
        expect(element.attributeValue("id")).andReturn("grid0");
        expect(element.attributeValue("name")).andReturn("Segment A");
        expect(element.elements()).andReturn(emptyList());
        replayMocks();

        StudySegment actual = (StudySegment) serializer.readElement(element);
        verifyMocks();

        assertEquals("Wrong grid id", "grid0", actual.getGridId());
        assertEquals("Wrong segment name", "Segment A", actual.getName());
    }

    public void testValidateElement() throws Exception {
        StudySegment studySegment = createStudySegment();
        Element actual = serializer.createElement(studySegment);
        assertTrue(StringUtils.isBlank(serializer.validateElement(studySegment, actual).toString()));
        studySegment.setGridId("wrong grid id");
        assertFalse(StringUtils.isBlank(serializer.validateElement(studySegment, actual).toString()));

        studySegment = createStudySegment();
        assertTrue(StringUtils.isBlank(serializer.validateElement(studySegment, actual).toString()));
        studySegment.setName("wrong name");
        assertFalse(StringUtils.isBlank(serializer.validateElement(studySegment, actual).toString()));

        studySegment = createStudySegment();
        assertTrue(StringUtils.isBlank(serializer.validateElement(studySegment, actual).toString()));
        studySegment.getChildren().add(new Period());
        assertFalse(StringUtils.isBlank(serializer.validateElement(studySegment, actual).toString()));


    }

    private StudySegment createStudySegment() {
        StudySegment segment = setGridId("grid0", createNamedInstance("Segment A", StudySegment.class));
        return segment;
    }
}

