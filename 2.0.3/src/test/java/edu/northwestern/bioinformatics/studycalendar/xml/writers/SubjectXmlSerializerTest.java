package edu.northwestern.bioinformatics.studycalendar.xml.writers;

import static edu.northwestern.bioinformatics.studycalendar.domain.Fixtures.createSubject;
import edu.northwestern.bioinformatics.studycalendar.domain.Subject;
import edu.northwestern.bioinformatics.studycalendar.service.SubjectService;
import edu.northwestern.bioinformatics.studycalendar.testing.StudyCalendarXmlTestCase;
import static edu.nwu.bioinformatics.commons.DateUtils.createDate;
import org.dom4j.Element;
import org.dom4j.tree.BaseElement;
import org.easymock.EasyMock;
import static org.easymock.EasyMock.expect;
import org.easymock.IArgumentMatcher;

import java.text.ParseException;
import java.util.Calendar;

/**
 * @author John Dzak
 */
public class SubjectXmlSerializerTest extends StudyCalendarXmlTestCase {
    private SubjectXmlSerializer serializer;
    private Subject subject;
    private SubjectService subjectService;

    protected void setUp() throws Exception {
        super.setUp();

        subjectService = registerMockFor(SubjectService.class);

        serializer = new SubjectXmlSerializer();
        serializer.setSubjectService(subjectService);

        subject = createSubject("1111", "john", "doe", createDate(1990, Calendar.JANUARY, 15, 0, 0, 0), "Male");
    }

    public void testCreateElement() {
        Element actual = serializer.createElement(subject);
        assertEquals("Wrong subject first name", "john", actual.attributeValue("first-name"));
        assertEquals("Wrong subject last name", "doe", actual.attributeValue("last-name"));
        assertEquals("Wrong subject person id", "1111", actual.attributeValue("person-id"));
        assertEquals("Wrong subject person id", "Male", actual.attributeValue("gender"));
        assertEquals("Wrong subject birth date", "1990-01-15", actual.attributeValue("birth-date"));
    }

    public void testReadElement() throws ParseException {
        Subject searchCriteria = createSubject("1111", null, null, null, "Male");
        Element element = createElement(searchCriteria);

        expectSubjectFoundFromSearchCriteria(searchCriteria);
        replayMocks();

        Subject actual = serializer.readElement(element);
        assertSame("Subjects should be the same", subject, actual);
    }

    ////// Expect Methods
    private void expectSubjectFoundFromSearchCriteria(Subject searchCriteria) {
        expect(subjectService.findSubject(eqSubject(searchCriteria))).andReturn(subject);
    }

    ////// Helper Methods
    private Element createElement(Subject subj) throws ParseException {
        Element elt = new BaseElement("subject");
        elt.addAttribute("gender", subj.getGender());
        elt.addAttribute("last-name", subj.getLastName());
        elt.addAttribute("person-id", subj.getPersonId());
        elt.addAttribute("first-name", subj.getFirstName());
        elt.addAttribute("birth-date", toDateString(subj.getDateOfBirth()));
        return elt;
    }

    ////// Custom Matchers
    public static Subject eqSubject(Subject in) {
        EasyMock.reportMatcher(new SubjectMatcher(in));
        return null;
    }

    public static class SubjectMatcher implements IArgumentMatcher {
        private Subject expected;

        public SubjectMatcher(Subject expected) {
            this.expected = expected;
        }

        public boolean matches(Object actual) {
            if (!(actual instanceof Subject)) {
                return false;
            }
            String personId = ((Subject) actual).getPersonId();
            return expected.getPersonId().equals(personId);
        }

        public void appendTo(StringBuffer buffer) {
            buffer.append("eqSubject(");
            buffer.append(expected.getClass().getName());
            buffer.append(" with person id \"");
            buffer.append(expected.getPersonId());
            buffer.append("\")");

        }
    }
}
