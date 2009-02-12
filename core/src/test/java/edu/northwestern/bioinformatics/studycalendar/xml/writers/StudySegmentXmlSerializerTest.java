package edu.northwestern.bioinformatics.studycalendar.xml.writers;

import edu.northwestern.bioinformatics.studycalendar.dao.StudySegmentDao;
import static edu.northwestern.bioinformatics.studycalendar.test.Fixtures.createNamedInstance;
import static edu.northwestern.bioinformatics.studycalendar.test.Fixtures.setGridId;
import edu.northwestern.bioinformatics.studycalendar.domain.Period;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.domain.StudySegment;
import edu.northwestern.bioinformatics.studycalendar.testing.StudyCalendarXmlTestCase;
import org.apache.commons.lang.StringUtils;
import org.dom4j.Element;
import static org.easymock.EasyMock.expect;

import static java.util.Collections.emptyList;

public class StudySegmentXmlSerializerTest extends StudyCalendarXmlTestCase {
    public static final String STUDY_SEGMENT = "study-segment";

    private StudySegmentXmlSerializer serializer;
    private StudySegmentDao studySegmentDao;
    private Element element;
    private StudySegment segment;
    private PeriodXmlSerializer periodSerializer;

    protected void setUp() throws Exception {
        super.setUp();

        element = registerMockFor(Element.class);
        studySegmentDao = registerDaoMockFor(StudySegmentDao.class);
        periodSerializer = registerMockFor(PeriodXmlSerializer.class);

        Study study = createNamedInstance("Study A", Study.class);

        serializer = new StudySegmentXmlSerializer() {
            protected AbstractPlanTreeNodeXmlSerializer getChildSerializer() {
                return periodSerializer;
            }
        };
        serializer.setStudySegmentDao(studySegmentDao);
        serializer.setStudy(study);

        segment = setGridId("grid0", createNamedInstance("Segment A", StudySegment.class));
    }

    public void testCreateElementEpoch() {
        Element actual = serializer.createElement(segment);

        assertEquals("Wrong attribute size", 2, actual.attributeCount());
        assertEquals("Wrong grid id", "grid0", actual.attribute("id").getValue());
        assertEquals("Wrong segment name", "Segment A", actual.attribute("name").getValue());
    }

    public void testReadElementEpoch() {
        expect(element.getName()).andReturn("study-segment");
        expect(element.attributeValue("id")).andReturn("grid0");
        expect(studySegmentDao.getByGridId("grid0")).andReturn(null);
        expect(element.attributeValue("name")).andReturn("Segment A");
        expect(element.elements()).andReturn(emptyList());
        replayMocks();

        StudySegment actual = (StudySegment) serializer.readElement(element);
        verifyMocks();

        assertEquals("Wrong grid id", "grid0", actual.getGridId());
        assertEquals("Wrong segment name", "Segment A", actual.getName());
    }

    public void testReadElementExistsEpoch() {
        expect(element.getName()).andReturn("study-segment");
        expect(element.attributeValue("id")).andReturn("grid0");
        expect(studySegmentDao.getByGridId("grid0")).andReturn(segment);
        replayMocks();

        StudySegment actual = (StudySegment) serializer.readElement(element);
        verifyMocks();

        assertSame("Wrong Segment", segment, actual);
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

