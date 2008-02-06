package edu.northwestern.bioinformatics.studycalendar.xml.writers;

import edu.northwestern.bioinformatics.studycalendar.dao.StudySegmentDao;
import edu.northwestern.bioinformatics.studycalendar.dao.UserDao;
import static edu.northwestern.bioinformatics.studycalendar.domain.Fixtures.createUser;
import static edu.northwestern.bioinformatics.studycalendar.domain.Fixtures.setGridId;
import edu.northwestern.bioinformatics.studycalendar.domain.StudySegment;
import edu.northwestern.bioinformatics.studycalendar.domain.User;
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
    private StudySegment segment;
    private Date date;
    private StudySegmentDao studySegmentDao;
    private User subjCoord;
    private UserDao userDao;

    protected void setUp() throws Exception {
        super.setUp();

        element = registerMockFor(Element.class);
        userDao = registerDaoMockFor(UserDao.class);
        studySegmentDao = registerDaoMockFor(StudySegmentDao.class);

        serializer = new RegistrationXmlSerializer();
        serializer.setStudySegmentDao(studySegmentDao);
        serializer.setUserDao(userDao);

        segment = setGridId("grid0", new StudySegment());
        date = DateUtils.createDate(2008, JANUARY, 15);
        subjCoord = createUser("Sam the Subject Coord");
    }

    public void testReadElement() {
        expect(element.attributeValue("first-study-segment")).andReturn("grid0");
        expect(element.attributeValue("date")).andReturn("2008-01-15");
        expect(element.attributeValue("subject-coordinator-name")).andReturn("Sam the Subject Coord");
        expect(element.attributeValue("desired-assignment-id")).andReturn("12345");

        expect(studySegmentDao.getByGridId("grid0")).andReturn(segment);
        expect(userDao.getByName("Sam the Subject Coord")).andReturn(subjCoord);
        replayMocks();

        Registration actual = serializer.readElement(element);
        verifyMocks();

        assertSame("Wrong first study segment", segment, actual.getFirstStudySegment());
        assertSameDay("Dates should be the same", date, actual.getDate());
        assertSame("Subject Coordinators should be the same", subjCoord, actual.getSubjectCoordinator());
        assertEquals("Wrong desired subject assignment id", "12345", actual.getDesiredStudySubjectAssignmentId());
    }
}
