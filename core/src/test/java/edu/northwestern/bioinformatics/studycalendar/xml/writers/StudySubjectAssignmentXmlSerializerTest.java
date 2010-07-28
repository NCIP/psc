package edu.northwestern.bioinformatics.studycalendar.xml.writers;

import static edu.northwestern.bioinformatics.studycalendar.core.Fixtures.*;
import edu.northwestern.bioinformatics.studycalendar.domain.*;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Amendment;
import edu.northwestern.bioinformatics.studycalendar.core.StudyCalendarXmlTestCase;
import edu.northwestern.bioinformatics.studycalendar.xml.AbstractStudyCalendarXmlSerializer;
import static edu.nwu.bioinformatics.commons.DateUtils.createDate;
import org.dom4j.Element;
import org.dom4j.DocumentHelper;
import org.dom4j.tree.BaseElement;
import static org.easymock.EasyMock.expect;

import java.util.Calendar;

/**
 * @author John Dzak
 */
public class StudySubjectAssignmentXmlSerializerTest extends StudyCalendarXmlTestCase {
    private StudySubjectAssignment assignment;
    private StudySubjectAssignmentXmlSerializer serializer;
    private AbstractStudyCalendarXmlSerializer<Subject> subjectSerializer;
    private Subject subject;

    protected void setUp() throws Exception {
        super.setUp();

        subjectSerializer = registerMockFor(SubjectXmlSerializer.class);

        serializer = new StudySubjectAssignmentXmlSerializer();
        serializer.setSubjectXmlSerializer(subjectSerializer);

        Study study = createNamedInstance("Study A", Study.class);
        Site site = createNamedInstance("Site Two", Site.class);

        StudySite studySite = createStudySite(study, site);

        Amendment amend = createAmendment("Amendment A", createDate(2008, Calendar.FEBRUARY, 1), true);

        User subjCoord = createUser("Sam the subject coord");

        subject = createSubject("john", "Doe");

        assignment = setGridId("grid0", createSubjectAssignment(studySite, subject, amend, subjCoord));
    }

    public void testCreateElementNewSubject() {
        expectSerializeSubject();
        replayMocks();

        Element actual = serializer.createElement(assignment, true);
        verifyMocks();

        assertEquals("Wrong element name", "subject-assignment", actual.getName());
        assertEquals("Wrong study name", "Study A", actual.attributeValue("study-name"));
        assertEquals("Wrong site name", "Site Two", actual.attributeValue("site-name"));
        assertEquals("Wrong current amendment key", "2008-02-01~Amendment A", actual.attributeValue("current-amendment-key"));
        assertEquals("Wrong subject coordinator name", "Sam the subject coord", actual.attributeValue("subject-coordinator-name"));
        assertEquals("Wrong start date", "2008-01-01", actual.attributeValue("start-date"));
        assertEquals("Wrong end date", "2008-03-01", actual.attributeValue("end-date"));
        assertEquals("Wrong assignment identifier", "grid0", actual.attributeValue("id"));

        assertNotNull("Subject element should exist", actual.element("subject"));
    }

    public void testCreateElementForSubjectCoordinator() throws Exception {
        expectScheduledCalendarElement();
        expectSerializeSubject();
        replayMocks();
        serializer.setIncludeScheduledCalendar(true);
        Element actual = serializer.createElement(assignment, true);
        verifyMocks();
        assertEquals("Should have 2 elements", 2, actual.elements().size());
        assertNotNull("Subject element should exist", actual.element("subject"));
        assertNotNull("Scheduld calendar element should exist", actual.element("scheduled-calendar"));
    }

    public void testCreateElementForSubjectCentric() throws Exception {
        expectScheduledCalendarElement();
        replayMocks();
        serializer.setSubjectCentric(true);
        Element actual = serializer.createElement(assignment, true);
        verifyMocks();
        assertEquals("Should have only 1 element", 1, actual.elements().size());
        assertNull("Subject element should not exist", actual.element("subject"));
        assertNotNull("Scheduld calendar element should exist", actual.element("scheduled-calendar"));
    }

    ////// Expect Methods
    private void expectSerializeSubject() {
        expect(subjectSerializer.createElement(subject)).andReturn(new BaseElement("subject"));
    }

    ////// Helper Methods
    private StudySubjectAssignment createSubjectAssignment(StudySite studySite, Subject subject, Amendment amend, User subjCoord) {
        StudySubjectAssignment assignment = new StudySubjectAssignment();
        assignment.setStudySite(studySite);
        assignment.setSubject(subject);
        assignment.setCurrentAmendment(amend);
        assignment.setSubjectCoordinator(subjCoord);
        assignment.setStartDate(createDate(2008, Calendar.JANUARY, 1));
        assignment.setEndDate(createDate(2008, Calendar.MARCH, 1));
        return assignment;
    }

    private void expectScheduledCalendarElement() {
        ScheduledCalendarXmlSerializer scheduledCalendarXmlSerializer = registerMockFor(ScheduledCalendarXmlSerializer.class);
        serializer.setScheduledCalendarXmlSerializer(scheduledCalendarXmlSerializer);

        Element eCalendar = DocumentHelper.createElement("scheduled-calendar");
        ScheduledCalendar schedule = setGridId("schedule-grid-0", new ScheduledCalendar());
        assignment.setScheduledCalendar(schedule);
        expect(scheduledCalendarXmlSerializer.createElement(schedule)).andReturn(eCalendar);
    }
}
