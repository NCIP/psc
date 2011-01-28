package edu.northwestern.bioinformatics.studycalendar.xml.writers;

import edu.northwestern.bioinformatics.studycalendar.domain.Gender;
import edu.northwestern.bioinformatics.studycalendar.domain.Subject;
import edu.northwestern.bioinformatics.studycalendar.domain.SubjectProperty;
import edu.northwestern.bioinformatics.studycalendar.xml.AbstractStudyCalendarXmlSerializer;
import org.dom4j.Element;

import java.util.Date;
import java.util.List;

import static edu.northwestern.bioinformatics.studycalendar.xml.XsdAttribute.*;
import static edu.northwestern.bioinformatics.studycalendar.xml.XsdElement.*;

/**
 * @author John Dzak
 */
public class SubjectXmlSerializer extends AbstractStudyCalendarXmlSerializer<Subject> {

    public Element createElement(Subject subject) {
        Element elt = SUBJECT.create();
        SUBJECT_FIRST_NAME.addTo(elt, subject.getFirstName());
        SUBJECT_LAST_NAME.addTo(elt, subject.getLastName());
        SUBJECT_PERSON_ID.addTo(elt, subject.getPersonId());
        SUBJECT_BIRTH_DATE.addTo(elt, subject.getDateOfBirth());
        SUBJECT_GENDER.addTo(elt, subject.getGender().getCode());
        for (SubjectProperty property : subject.getProperties()) {
            Element child = SUBJECT_PROPERTY.create();
            PROPERTY_NAME.addTo(child, property.getName());
            PROPERTY_VALUE.addTo(child, property.getValue());
            elt.add(child);
        }
        return elt;
    }

    @SuppressWarnings( { "unchecked" })
    public Subject readElement(Element element) {
        String personId = SUBJECT_PERSON_ID.from(element);
        String firstName = SUBJECT_FIRST_NAME.from(element);
        String lastName = SUBJECT_LAST_NAME.from(element);
        Date birthDate = SUBJECT_BIRTH_DATE.fromDate(element);
        String gender = SUBJECT_GENDER.from(element);
        Subject subject = createSubject(personId, firstName, lastName, birthDate, gender);

        for (Element child : ((List<Element>) element.elements(SUBJECT_PROPERTY.xmlName()))) {
            subject.getProperties().add(
                new SubjectProperty(PROPERTY_NAME.from(child), PROPERTY_VALUE.from(child)));
        }

        return subject;
    }


    private Subject createSubject(String personId, String firstName, String lastName, Date birthDate, String gender) {
        Subject subject = new Subject();
        subject.setPersonId(personId);
        subject.setFirstName(firstName);
        subject.setLastName(lastName);
        subject.setDateOfBirth(birthDate);
        subject.setGender(Gender.getByCode(gender));
        return subject;
    }
}
