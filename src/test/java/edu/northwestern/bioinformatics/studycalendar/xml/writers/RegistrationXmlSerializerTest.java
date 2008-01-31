package edu.northwestern.bioinformatics.studycalendar.xml.writers;

import edu.northwestern.bioinformatics.studycalendar.dao.StudySegmentDao;
import static edu.northwestern.bioinformatics.studycalendar.domain.Fixtures.setGridId;
import edu.northwestern.bioinformatics.studycalendar.domain.StudySegment;
import edu.northwestern.bioinformatics.studycalendar.testing.StudyCalendarXmlTestCase;
import edu.northwestern.bioinformatics.studycalendar.xml.domain.Registration;
import edu.nwu.bioinformatics.commons.DateUtils;
import org.dom4j.Element;
import static org.easymock.EasyMock.expect;

import static java.util.Calendar.JANUARY;
import java.util.Date;

public class RegistrationXmlSerializerTest extends StudyCalendarXmlTestCase {
    private RegistrationXmlSerializer serializer;
    private Element element;
    private Registration registration;
    private StudySegment segment;
    private Date date;
    private StudySegmentDao studySegmentDao;

    protected void setUp() throws Exception {
        super.setUp();

        element = registerMockFor(Element.class);
        studySegmentDao = registerDaoMockFor(StudySegmentDao.class);

        serializer = new RegistrationXmlSerializer();
        serializer.setStudySegmentDao(studySegmentDao);

        segment = setGridId("grid0", new StudySegment());
        date = DateUtils.createDate(2008, JANUARY, 15);

        registration = new Registration();
        registration.setFirstStudySegment(segment);
        registration.setDate(date);
    }

    public void testCreateElement() {
        Element actual = serializer.createElement(registration);

        assertEquals("Element name should be registration", "registration", actual.getName());
        assertEquals("Wrong first study segment id", "grid0", actual.attributeValue("first-study-segment"));
        assertEquals("Wrong registration date", "2008-01-15", actual.attributeValue("date"));
    }

    public void testReadElement() {
        expect(element.attributeValue("first-study-segment")).andReturn("grid0");
        expect(element.attributeValue("date")).andReturn("2008-01-15");

        expect(studySegmentDao.getByGridId("grid0")).andReturn(segment);
        replayMocks();

        Registration actual = serializer.readElement(element);
        verifyMocks();

        assertSame("Wrong first study segment", segment, actual.getFirstStudySegment());
        assertSameDay("Dates should be the same", date, actual.getDate());
    }
}
