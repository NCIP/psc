package edu.northwestern.bioinformatics.studycalendar.xml.writers;

import edu.northwestern.bioinformatics.studycalendar.dao.SubjectDao;
import edu.northwestern.bioinformatics.studycalendar.domain.Fixtures;
import edu.northwestern.bioinformatics.studycalendar.domain.Subject;
import edu.northwestern.bioinformatics.studycalendar.testing.StudyCalendarXmlTestCase;
import static edu.nwu.bioinformatics.commons.DateUtils.createDate;
import org.dom4j.Element;
import org.dom4j.tree.BaseElement;
import static org.easymock.EasyMock.expect;

import java.util.Calendar;
import static java.util.Collections.singletonList;
import java.util.Date;

/**
 * @author John Dzak
 */
public class SubjectXmlSerializerTest extends StudyCalendarXmlTestCase {
    private SubjectXmlSerializer serializer;
    private Subject subject;
    private Element element;
    private SubjectDao subjectDao;
    private Date birthDate;
    private String birthDateString;
    private String lastName;
    private String firstName;
    private String personId;
    private String gender;

    protected void setUp() throws Exception {
        super.setUp();

        subjectDao = registerDaoMockFor(SubjectDao.class);

        serializer = new SubjectXmlSerializer();
        serializer.setSubjectDao(subjectDao);

        firstName = "john";
        lastName = "doe";
        personId = "1111";
        gender = "Male";
        birthDateString = "1990-01-15";
        birthDate = createDate(1990, Calendar.JANUARY, 15, 0, 0, 0);

        subject = createSubject(firstName, lastName, birthDate, personId, gender);

        element = createElement(subject);
    }

    public void testCreateElement() {
        Element actual = serializer.createElement(subject);
        assertEquals("Wrong subject first name", "john", actual.attributeValue("first-name"));
        assertEquals("Wrong subject last name", "doe", actual.attributeValue("last-name"));
        assertEquals("Wrong subject person id", "1111", actual.attributeValue("person-id"));
        assertEquals("Wrong subject person id", "Male", actual.attributeValue("gender"));
        assertEquals("Wrong subject birth date", "1990-01-15", actual.attributeValue("birth-date"));
    }

    public void testReadElementWithAllAttributes() {
        expectFindSubjectByPersonId();
        replayMocks();

        Subject actual = serializer.readElement(element);
        verifyMocks();

        assertSame("Subjects should be the same", subject,  actual);
    }

    public void testReadElementByPersonId() {
        Element element = createElement(createSubject(null, null, null, personId, gender));
        
        expectFindSubjectByPersonId();
        replayMocks();

        Subject actual = serializer.readElement(element);
        verifyMocks();

        assertSame("Subjects should be the same", subject,  actual);
    }

    public void testReadElementByFirstNameLastNameAndBirthDate() {
        Element element = createElement(createSubject(firstName, lastName, birthDate, null, gender));

        expectFindSubjectByFirstNameLastNameAndBirthDate();
        replayMocks();

        Subject actual = serializer.readElement(element);
        verifyMocks();

        assertSame("Subjects should be the same", subject,  actual);
    }

    ////// Expect Methods
    private void expectFindSubjectByPersonId() {
        expect(subjectDao.findSubjectByPersonId("1111")).andReturn(subject);
    }

    private void expectFindSubjectByFirstNameLastNameAndBirthDate() {
        expect(subjectDao.findSubjectByFirstNameLastNameAndDoB("john", "doe", birthDate)).andReturn(singletonList(subject));
    }

    ////// Helper Methods
    private Subject createSubject(String firstNm, String lastNm, Date birthDate, String personId, String gender) {
        Subject subject = Fixtures.createSubject(firstNm, lastNm);
        subject.setDateOfBirth(birthDate);
        subject.setPersonId(personId);
        subject.setGender(gender);
        return subject;
    }

    private Element createElement(Subject subj) {
        Element elt = new BaseElement("subject");
        elt.addAttribute("gender", subj.getGender());
        elt.addAttribute("birth-date", birthDateString);
        elt.addAttribute("last-name", subj.getLastName());
        elt.addAttribute("person-id", subj.getPersonId());
        elt.addAttribute("first-name", subj.getFirstName());
        return elt;
    }
}
