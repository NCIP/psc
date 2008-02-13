package edu.northwestern.bioinformatics.studycalendar.xml.writers;

import edu.northwestern.bioinformatics.studycalendar.StudyCalendarValidationException;
import edu.northwestern.bioinformatics.studycalendar.dao.StudySegmentDao;
import edu.northwestern.bioinformatics.studycalendar.dao.UserDao;
import static edu.northwestern.bioinformatics.studycalendar.domain.Fixtures.*;
import edu.northwestern.bioinformatics.studycalendar.domain.StudySegment;
import edu.northwestern.bioinformatics.studycalendar.domain.Subject;
import edu.northwestern.bioinformatics.studycalendar.domain.User;
import edu.northwestern.bioinformatics.studycalendar.testing.StudyCalendarXmlTestCase;
import edu.northwestern.bioinformatics.studycalendar.xml.domain.Registration;
import edu.nwu.bioinformatics.commons.DateUtils;
import org.dom4j.Element;
import org.dom4j.tree.BaseElement;
import static org.easymock.EasyMock.expect;

import static java.util.Calendar.JANUARY;
import java.util.Date;

public class RegistrationXmlSerializerTest extends StudyCalendarXmlTestCase {
    private RegistrationXmlSerializer serializer;
    private StudySegment segment;
    private Date date;
    private StudySegmentDao studySegmentDao;
    private User subjCoord;
    private UserDao userDao;
    private SubjectXmlSerializer subjectSerializer;
    private Subject subject;
    private String dateString;
    private String desiredAssignmentId;
    private Element element;

    protected void setUp() throws Exception {
        super.setUp();

        userDao = registerDaoMockFor(UserDao.class);
        studySegmentDao = registerDaoMockFor(StudySegmentDao.class);
        subjectSerializer = registerMockFor(SubjectXmlSerializer.class);

        serializer = new RegistrationXmlSerializer();
        serializer.setStudySegmentDao(studySegmentDao);
        serializer.setUserDao(userDao);
        serializer.setSubjectXmlSerializer(subjectSerializer);

        segment = setGridId("grid0", new StudySegment());
        subjCoord = createUser("Sam the Subject Coord");
        subject = createSubject("John", "Doe");
        date = DateUtils.createDate(2008, JANUARY, 15);
        dateString = "2008-01-15";
        desiredAssignmentId = "12345";

        element = createElement(segment, dateString, subjCoord, desiredAssignmentId);
    }

    public void testReadElement() {
        expect(studySegmentDao.getByGridId("grid0")).andReturn(segment);
        expect(userDao.getByName("Sam the Subject Coord")).andReturn(subjCoord);
        expectReadSubjectElement();
        replayMocks();

        Registration actual = serializer.readElement(element);
        verifyMocks();

        assertSame("Wrong first study segment", segment, actual.getFirstStudySegment());
        assertSameDay("Dates should be the same", date, actual.getDate());
        assertSame("Subject Coordinators should be the same", subjCoord, actual.getSubjectCoordinator());
        assertEquals("Wrong desired subject assignment id", "12345", actual.getDesiredStudySubjectAssignmentId());
        assertSame("Subject should be the same", subject,  actual.getSubject());
    }

    public void testReadElementWhenStudySegmentIsNull() {
        Element elt = createElement(null, dateString, subjCoord, desiredAssignmentId);
        try {
            serializer.readElement(elt);
            fail("An exception should be thrown");
        } catch(StudyCalendarValidationException success) {
            assertEquals("Registration first study segment id is required", success.getMessage());
        }
    }

    public void testReadElementWhenDateIsNull() {
        Element elt = createElement(segment, null, subjCoord, desiredAssignmentId);
        try {
            serializer.readElement(elt);
            fail("An exception should be thrown");
        } catch(StudyCalendarValidationException success) {
            assertEquals("Registration date is required", success.getMessage());
        }
    }

    //// Test Helper Methods
    private void expectReadSubjectElement() {
        expect(subjectSerializer.readElement(null)).andReturn(subject);
    }

    private Element createElement(StudySegment segment, String date, User subjCoord, String desiredAssignmentId) {
        Element elt = new BaseElement("registration");
        elt.addAttribute("date", date);
        elt.addAttribute("desired-assignment-id", desiredAssignmentId);
        if (segment != null) elt.addAttribute("first-study-segment-id", segment.getGridId());
        if (subjCoord != null) elt.addAttribute("subject-coordinator-name", subjCoord.getName());
        return elt;
    }
}
