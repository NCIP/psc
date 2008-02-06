package edu.northwestern.bioinformatics.studycalendar.xml.writers;

import edu.northwestern.bioinformatics.studycalendar.domain.*;
import static edu.northwestern.bioinformatics.studycalendar.domain.Fixtures.*;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Amendment;
import edu.northwestern.bioinformatics.studycalendar.testing.StudyCalendarXmlTestCase;
import static edu.nwu.bioinformatics.commons.DateUtils.createDate;
import org.dom4j.Element;

import java.util.Calendar;

/**
 * @author John Dzak
 */
public class StudySubjectAssignmentXmlSerializerTest extends StudyCalendarXmlTestCase {
    private StudySubjectAssignment assignment;
    private StudySubjectAssignmentXmlSerializer serializer;

    protected void setUp() throws Exception {
        super.setUp();

        serializer = new StudySubjectAssignmentXmlSerializer();

        Study study = Fixtures.createNamedInstance("Study A", Study.class);
        Site site = Fixtures.createNamedInstance("Site Two", Site.class);

        StudySite studySite = createStudySite(study, site);

        Amendment amend = createAmendment("Amendment A", createDate(2008, Calendar.FEBRUARY, 1), true);

        User subjCoord = createUser("Sam the subject coord");

        Subject subject = createSubject("john", "doe");
        subject.setDateOfBirth(createDate(1990, Calendar.JANUARY, 15));
        subject.setPersonId("1111");

        assignment = new StudySubjectAssignment();
        assignment.setStudySite(studySite);
        assignment.setSubject(subject);
        assignment.setCurrentAmendment(amend);
        assignment.setSubjectCoordinator(subjCoord);
        assignment.setStartDateEpoch(createDate(2008, Calendar.JANUARY, 1));
        assignment.setEndDateEpoch(createDate(2008, Calendar.MARCH, 1));
    }

    public void testCreateElementNewSubject() {
        Element actual = serializer.createElement(assignment, true);

        assertEquals("Wrong element name", "subject-assignment", actual.getName());
        assertEquals("Wrong study name", "Study A", actual.attributeValue("study-name"));
        assertEquals("Wrong site name", "Site Two", actual.attributeValue("site-name"));
        assertEquals("Wrong current amendment key", "2008-02-01~Amendment A", actual.attributeValue("current-amendment-key"));
        assertEquals("Wrong subject coordinator name", "Sam the subject coord", actual.attributeValue("subject-coordinator-name"));
        assertEquals("Wrong start date", "2008-01-01", actual.attributeValue("start-date"));
        assertEquals("Wrong end date", "2008-03-01", actual.attributeValue("end-date"));

        assertEquals("Wrong subject first name", "john", actual.element("subject").attributeValue("first-name"));
        assertEquals("Wrong subject last name", "doe", actual.element("subject").attributeValue("last-name"));
        assertEquals("Wrong subject birth date", "1990-01-15", actual.element("subject").attributeValue("date-of-birth"));
        assertEquals("Wrong subject person id", "1111", actual.element("subject").attributeValue("person-id"));
    }
}
