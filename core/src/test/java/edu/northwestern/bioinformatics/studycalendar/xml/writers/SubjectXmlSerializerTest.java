package edu.northwestern.bioinformatics.studycalendar.xml.writers;

import edu.northwestern.bioinformatics.studycalendar.core.StudyCalendarXmlTestCase;
import edu.northwestern.bioinformatics.studycalendar.domain.Gender;
import edu.northwestern.bioinformatics.studycalendar.domain.Subject;
import edu.northwestern.bioinformatics.studycalendar.domain.SubjectProperty;
import org.dom4j.Element;
import org.dom4j.tree.BaseElement;

import java.text.ParseException;
import java.util.Calendar;
import java.util.List;

import static edu.northwestern.bioinformatics.studycalendar.core.Fixtures.createSubject;
import static edu.nwu.bioinformatics.commons.DateUtils.createDate;

/**
 * @author John Dzak
 */
public class SubjectXmlSerializerTest extends StudyCalendarXmlTestCase {
    private SubjectXmlSerializer serializer;
    private Subject subject;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        serializer = new SubjectXmlSerializer();
        subject = createSubject("1111", "john", "doe", createDate(1990, Calendar.JANUARY, 15, 0, 0, 0), Gender.MALE);
    }

    public void testCreateElement() {
        Element actual = serializer.createElement(subject);
        assertEquals("Wrong subject first name", "john", actual.attributeValue("first-name"));
        assertEquals("Wrong subject last name", "doe", actual.attributeValue("last-name"));
        assertEquals("Wrong subject person id", "1111", actual.attributeValue("person-id"));
        assertEquals("Wrong subject person id", Gender.MALE.getCode(), actual.attributeValue("gender"));
        assertEquals("Wrong subject birth date", "1990-01-15", actual.attributeValue("birth-date"));
    }

    @SuppressWarnings( { "unchecked" })
    public void testCreateElementIncludesProperties() throws Exception {
        subject.getProperties().add(new SubjectProperty("Anonymous", "yes"));
        subject.getProperties().add(new SubjectProperty("A female deer?", "no"));

        Element actual = serializer.createElement(subject);
        List<Element> propertyChildren = actual.elements("property");
        assertEquals("Wrong number of child elements", 2, propertyChildren.size());
        assertPropertyElement("Wrong 1st prop", "Anonymous", "yes", propertyChildren.get(0));
        assertPropertyElement("Wrong 2nd prop", "A female deer?", "no", propertyChildren.get(1));
    }

    public void testReadElement() throws ParseException {
        Element element = createElement(subject);

        replayMocks();
        Subject actual = serializer.readElement(element);
        verifyMocks();
        assertEquals("Wrong Subject", subject, actual);
    }

    public void testReadElementReadsProperties() throws Exception {
        Element subjElement = createElement(subject);
        subjElement.add(elementFromString("<property name='Hat size' value='7'/>"));
        subjElement.add(elementFromString("<property name='Hat color' value='black'/>"));

        Subject actual = serializer.readElement(subjElement);
        assertEquals("Wrong number of properties", 2, actual.getProperties().size());
        assertSubjectProperty("Wrong 1st prop", "Hat size", "7", actual.getProperties().get(0));
        assertSubjectProperty("Wrong 2nd prop", "Hat color", "black", actual.getProperties().get(1));
    }

    ////// Helper Methods

    private void assertPropertyElement(
        String message, String expectedName, String expectedValue, Element actual
    ) {
        assertEquals(message + ": wrong element name", "property", actual.getName());
        assertEquals(message + ": wrong name", expectedName, actual.attribute("name").getValue());
        assertEquals(message + ": wrong value", expectedValue, actual.attribute("value").getValue());
        assertEquals(message + ": wrong number of attributes", 2, actual.attributeCount());
    }

    private void assertSubjectProperty(String message, String expectedName, String expectedValue, SubjectProperty actual) {
        assertEquals(message + ": wrong name", expectedName, actual.getName());
        assertEquals(message + ": wrong value", expectedValue, actual.getValue());
    }

    private Element createElement(Subject subj) {
        Element elt = new BaseElement("subject");
        elt.addAttribute("gender", subj.getGender().getDisplayName());
        elt.addAttribute("last-name", subj.getLastName());
        elt.addAttribute("person-id", subj.getPersonId());
        elt.addAttribute("first-name", subj.getFirstName());
        elt.addAttribute("birth-date", toDateString(subj.getDateOfBirth()));
        return elt;
    }
}
